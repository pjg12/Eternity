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


def migrate_reminder_keys(reminders: dict, greater_to_lesser: dict[int, int], report: list) -> dict:
    """Remap any reminder key containing a greater tech id in an .al.<id>. segment."""
    greater_ids = set(greater_to_lesser)
    new_reminders = {}
    pending = []

    pattern = re.compile(r"^(.*\.al\.)(\d+)(\..*)$")

    for key, value in reminders.items():
        m = pattern.match(key)
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
                f"merge {old_key}={value} into {new_key}={new_reminders[new_key]} -> {chosen}"
            )
            new_reminders[new_key] = chosen
        else:
            report.append(f"move {old_key}={value} -> {new_key}")
            new_reminders[new_key] = value
    return new_reminders


def main():
    greater_to_lesser = build_greater_map()
    greater_ids = set(greater_to_lesser)

    with CHAR_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    report = []
    before = len(data.get("reminderSelections") or {})
    data["reminderSelections"] = migrate_reminder_keys(
        data.get("reminderSelections") or {}, greater_to_lesser, report
    )
    after = len(data["reminderSelections"])

    CHAR_PATH.write_text(jackson_dump(data), encoding="utf-8")

    print(f"Changes: {len(report)}")
    for line in report[:40]:
        print(" ", line)
    if len(report) > 40:
        print(f"  ... +{len(report) - 40} more")
    print(f"reminder keys: {before} -> {after}")

    text = CHAR_PATH.read_text(encoding="utf-8")
    remaining = [gid for gid in sorted(greater_ids) if re.search(rf"(?<!\d){gid}(?!\d)", text)]
    print(f"Greater IDs still present: {len(remaining)}")
    for gid in remaining[:20]:
        m = re.search(rf"(?<!\d){gid}(?!\d)", text)
        start = max(0, m.start() - 50)
        end = min(len(text), m.end() + 40)
        print(f"  {gid}: ...{text[start:end].replace(chr(10), ' ')}...")


if __name__ == "__main__":
    main()
