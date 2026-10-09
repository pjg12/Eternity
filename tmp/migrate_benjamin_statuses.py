import json
import re
import subprocess
from pathlib import Path

CHAR_PATH = Path(r"C:\EternityTTRPG\Characters\12.json")

AFFINITIES = {
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
}


def build_greater_map() -> dict[int, int]:
    result = subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    old = json.loads(result.stdout)
    mapping = {}
    for t in old:
        if (
            t.get("type") == "Maintained"
            and t.get("affinity") in AFFINITIES
            and "(Greater)" in t["name"]
        ):
            mapping[t["id"]] = t["prereq"]
    return mapping


def jackson_dump(data) -> str:
    raw = json.dumps(data, indent=2, ensure_ascii=False)
    raw = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", raw)
    return raw + "\n"


def scrub_labels(text: str) -> str:
    text = re.sub(r"\s*\([Gg]reater\)", "", text)
    text = re.sub(r"\s*\([Ll]esser\)", "", text)
    return text


def is_status_dict(obj) -> bool:
    return isinstance(obj, dict) and "durationType" in obj and "attribute" in obj


def filter_statuses(statuses, greater_ids: set[int], report: list, path: str):
    """Filter a flat list of status dicts."""
    kept = []
    seen = set()
    for st in statuses:
        if not is_status_dict(st):
            kept.append(st)
            continue
        name = st.get("name") or ""
        m = re.search(r"maintained\.al\.(\d+)\.", name)
        if m and int(m.group(1)) in greater_ids:
            report.append(f"DROP status {name}")
            continue
        new_name = scrub_labels(name)
        new_desc = scrub_labels(st.get("description") or "")
        if new_name != name or new_desc != (st.get("description") or ""):
            st = dict(st)
            st["name"] = new_name
            st["description"] = new_desc
            report.append(f"RELABEL {name} -> {new_name}")
        key = st.get("name")
        if key and key.startswith("Panel Maintained:") and key in seen:
            report.append(f"DROP duplicate {key}")
            continue
        if key:
            seen.add(key)
        kept.append(st)
    return kept


def scrub_tree(obj, greater_ids, report):
    """In-place scrub of nested status lists."""
    if isinstance(obj, dict):
        for k, v in list(obj.items()):
            if isinstance(v, list):
                if v and is_status_dict(v[0]):
                    obj[k] = filter_statuses(v, greater_ids, report, k)
                else:
                    scrub_tree(v, greater_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, greater_ids, report)
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            if isinstance(v, list):
                if v and is_status_dict(v[0]):
                    obj[i] = filter_statuses(v, greater_ids, report, f"[{i}]")
                else:
                    scrub_tree(v, greater_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, greater_ids, report)


def migrate_loadouts(reminders: dict, greater_to_lesser: dict[int, int], report: list) -> dict:
    greater_ids = set(greater_to_lesser)
    new_reminders = {}
    pending = []

    for key, value in reminders.items():
        m = re.search(r"(maintained\.loadout\.[^.]+\.maintained\.al\.)(\d+)(\..*)", key)
        if m and int(m.group(2)) in greater_ids:
            gid = int(m.group(2))
            lid = greater_to_lesser[gid]
            new_key = f"{m.group(1)}{lid}{m.group(3)}"
            pending.append((new_key, value, key))
            continue
        new_reminders[key] = value

    for new_key, value, old_key in pending:
        if new_key in new_reminders:
            if str(new_reminders[new_key]).isdigit() and str(value).isdigit():
                chosen = str(max(int(new_reminders[new_key]), int(value)))
            else:
                chosen = new_reminders[new_key]
            report.append(
                f"LOADOUT merge {old_key}={value} into {new_key}={new_reminders[new_key]} -> {chosen}"
            )
            new_reminders[new_key] = chosen
        else:
            report.append(f"LOADOUT move {old_key}={value} -> {new_key}")
            new_reminders[new_key] = value
    return new_reminders


def main():
    greater_to_lesser = build_greater_map()
    greater_ids = set(greater_to_lesser)

    with CHAR_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    report = []
    scrub_tree(data, greater_ids, report)

    if isinstance(data.get("reminderSelections"), dict):
        before = len(data["reminderSelections"])
        data["reminderSelections"] = migrate_loadouts(
            data["reminderSelections"], greater_to_lesser, report
        )
        report.append(
            f"LOADOUT keys {before} -> {len(data['reminderSelections'])}"
        )

    CHAR_PATH.write_text(jackson_dump(data), encoding="utf-8")

    print(f"Total changes: {len(report)}")
    for line in report:
        print(" ", line)

    text = CHAR_PATH.read_text(encoding="utf-8")
    remaining = [gid for gid in sorted(greater_ids) if re.search(rf"(?<!\d){gid}(?!\d)", text)]
    print(f"Greater IDs still present: {len(remaining)}")
    if remaining:
        # show context snippets
        for gid in remaining[:20]:
            for m in re.finditer(rf"(?<!\d){gid}(?!\d)", text):
                start = max(0, m.start() - 40)
                end = min(len(text), m.end() + 40)
                print(f"  {gid}: ...{text[start:end].replace(chr(10), ' ')}...")
                break


if __name__ == "__main__":
    main()
