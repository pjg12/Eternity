import json
import re
import shutil
import subprocess
from pathlib import Path

CHAR_PATH = Path(r"C:\EternityTTRPG\Characters\2.json")
SNAPSHOT_PATH = Path(r"C:\EternityTTRPG\tmp\2_pre_migration.json")

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


def load_greater_meta():
    result = subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    old = json.loads(result.stdout)
    meta = {}
    for t in old:
        if (
            t.get("type") == "Maintained"
            and t.get("affinity") in AFFINITIES
            and "(Greater)" in t["name"]
        ):
            meta[t["id"]] = t
    return meta


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


def filter_statuses(statuses, greater_ids: set[int], report: list):
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
            report.append(f"RELABEL {name}")
        key = st.get("name")
        if key and key.startswith("Panel Maintained:") and key in seen:
            report.append(f"DROP duplicate {key}")
            continue
        if key:
            seen.add(key)
        kept.append(st)
    return kept


def scrub_tree(obj, greater_ids, report):
    if isinstance(obj, dict):
        for k, v in list(obj.items()):
            if isinstance(v, list):
                if v and is_status_dict(v[0]):
                    obj[k] = filter_statuses(v, greater_ids, report)
                else:
                    scrub_tree(v, greater_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, greater_ids, report)
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            if isinstance(v, list):
                if v and is_status_dict(v[0]):
                    obj[i] = filter_statuses(v, greater_ids, report)
                else:
                    scrub_tree(v, greater_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, greater_ids, report)


def migrate_reminder_keys(reminders: dict, greater_to_lesser: dict[int, int], report: list) -> dict:
    greater_ids = set(greater_to_lesser)
    new_reminders = {}
    pending = []
    pattern = re.compile(r"^(.*\.al\.)(\d+)(\..*)$")

    for key, value in reminders.items():
        m = pattern.match(key)
        if m and int(m.group(2)) in greater_ids:
            lid = greater_to_lesser[int(m.group(2))]
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
            report.append(f"LOADOUT merge {old_key}={value} into {new_key} -> {chosen}")
            new_reminders[new_key] = chosen
        else:
            report.append(f"LOADOUT move {old_key} -> {new_key}")
            new_reminders[new_key] = value
    return new_reminders


def migrate_training(by_cat: dict, greater_to_lesser: dict[int, int], report: list):
    for techs in by_cat.values():
        if not isinstance(techs, list):
            continue
        for tech in techs:
            if not isinstance(tech, dict):
                continue
            name = tech.get("name") or ""
            if (
                tech.get("type") == "Maintained"
                and tech.get("affinity") in AFFINITIES
                and "(Greater)" in name
                and isinstance(tech.get("prereq"), int)
                and tech["prereq"] > 0
            ):
                greater_to_lesser[tech["id"]] = tech["prereq"]

    for cat, techs in list(by_cat.items()):
        if not isinstance(techs, list):
            continue
        owned_ids = {t.get("id") for t in techs if isinstance(t, dict)}
        new_list = []
        for tech in techs:
            if not isinstance(tech, dict):
                new_list.append(tech)
                continue
            tid = tech.get("id")
            if tid in greater_to_lesser:
                lid = greater_to_lesser[tid]
                if lid in owned_ids:
                    report.append(
                        f"[{cat}] DROP Greater {tid} rank={tech.get('rank')} (have {lid})"
                    )
                    continue
                replacement = dict(tech)
                replacement["id"] = lid
                report.append(f"[{cat}] REMAP {tid}->{lid} rank={tech.get('rank')}")
                new_list.append(replacement)
                owned_ids.add(lid)
                continue
            new_list.append(tech)
        by_cat[cat] = new_list


def xp_cost_at_rank(rank: int, affinity: str, natural: set[str], domains: set[str]) -> int:
    value = rank * 4 + 10
    if affinity.lower() in ("spirit", "time"):
        value = int(value * 1.5)
    aff_l = affinity.lower()
    if aff_l in natural:
        value //= 2
    elif aff_l in domains:
        value = int(round(value * 0.75))
    return value


def total_xp_for_rank(rank: int, affinity: str, natural: set[str], domains: set[str]) -> int:
    return sum(xp_cost_at_rank(r, affinity, natural, domains) for r in range(rank))


def calc_lost_xp(pre_data, post_data, greater_to_lesser, greater_meta):
    natural = {a.lower() for a in (post_data["training"].get("naturalAffinities") or [])}
    domains = {a.lower() for a in (post_data["training"].get("derivedAffinities") or [])}

    def owned(data):
        out = {}
        for cat, techs in data["training"]["trainingByCategory"].items():
            for t in techs:
                out[t["id"]] = (cat, t.get("rank", 0))
        return out

    old_owned = owned(pre_data)
    new_owned = owned(post_data)

    lost = []
    for gid, (cat, rank) in old_owned.items():
        if gid in greater_to_lesser and gid not in new_owned:
            meta = greater_meta.get(gid)
            if meta is None:
                continue
            xp = total_xp_for_rank(rank, meta["affinity"], natural, domains)
            lost.append(
                {
                    "id": gid,
                    "name": meta["name"],
                    "affinity": meta["affinity"],
                    "rank": rank,
                    "xp": xp,
                }
            )
    lost.sort(key=lambda x: (x["affinity"], x["id"]))
    return lost, natural, domains


def main():
    shutil.copy2(CHAR_PATH, SNAPSHOT_PATH)
    print(f"Snapshot saved: {SNAPSHOT_PATH}")

    greater_to_lesser = build_greater_map()
    greater_ids = set(greater_to_lesser)
    greater_meta = load_greater_meta()

    with CHAR_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    pre_data = json.load(SNAPSHOT_PATH.open(encoding="utf-8"))
    report = []

    migrate_training(data["training"]["trainingByCategory"], greater_to_lesser, report)
    scrub_tree(data, greater_ids, report)
    if isinstance(data.get("reminderSelections"), dict):
        before = len(data["reminderSelections"])
        data["reminderSelections"] = migrate_reminder_keys(
            data["reminderSelections"], greater_to_lesser, report
        )
        report.append(f"reminder keys {before} -> {len(data['reminderSelections'])}")

    CHAR_PATH.write_text(jackson_dump(data), encoding="utf-8")

    train_lines = [r for r in report if r.startswith("[")]
    print(f"Training changes: {len(train_lines)}")
    for line in train_lines:
        print(" ", line)
    print(f"Total migration changes: {len(report)}")

    text = CHAR_PATH.read_text(encoding="utf-8")
    remaining = [gid for gid in sorted(greater_ids) if re.search(rf"(?<!\d){gid}(?!\d)", text)]
    print(f"Greater IDs still present: {len(remaining)}")
    if remaining:
        print(" ", remaining[:30])

    lost, natural, domains = calc_lost_xp(pre_data, data, greater_to_lesser, greater_meta)
    total_xp = sum(x["xp"] for x in lost)
    total_ranks = sum(x["rank"] for x in lost)

    print()
    print(f"Character: {data['identity']['name']}")
    print(f"Natural affinities: {sorted(natural) or '(none)'}")
    print(f"Lost Greater techniques: {len(lost)}")
    print(f"Lost ranks (sum): {total_ranks}")
    print(f"Total training XP value: {total_xp}")
    print()
    print(f"{'ID':>6}  {'Rank':>4}  {'XP':>6}  {'Affinity':<12}  Name")
    print("-" * 72)
    by_aff = {}
    for x in lost:
        print(f"{x['id']:6d}  {x['rank']:4d}  {x['xp']:6d}  {x['affinity']:<12}  {x['name']}")
        by_aff.setdefault(x["affinity"], {"xp": 0, "ranks": 0, "count": 0})
        by_aff[x["affinity"]]["xp"] += x["xp"]
        by_aff[x["affinity"]]["ranks"] += x["rank"]
        by_aff[x["affinity"]]["count"] += 1
    print()
    print("By affinity:")
    for aff, s in sorted(by_aff.items()):
        note = " (1/2 cost - natural)" if aff.lower() in natural else ""
        print(f"  {aff}: {s['count']} techs, {s['ranks']} ranks, {s['xp']} XP{note}")


if __name__ == "__main__":
    main()
