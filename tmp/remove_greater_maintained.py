import json
import re
from pathlib import Path

TRAINING_PATH = Path(r"C:\EternityTTRPG\Data\trainingdata.json")
CHAR_DIR = Path(r"C:\EternityTTRPG\Characters")

AFFINITIES = {
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
}


def is_greater_name(name: str) -> bool:
    return "(Greater)" in name


def is_lesser_name(name: str) -> bool:
    return "(Lesser)" in name or "(lesser)" in name


def strip_lesser_tag(name: str) -> str:
    # Handle "Name (Lesser)", "Name (lesser)", and accidental double spaces
    name = re.sub(r"\s*\([Ll]esser\)\s*", "", name)
    return name.strip()


def jackson_dump(data) -> str:
    """Approximate Jackson DefaultPrettyPrinter with spaces around colons."""
    raw = json.dumps(data, indent=2, ensure_ascii=False)
    # "key": value  ->  "key" : value
    raw = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", raw)
    # Match trainingdata array-of-objects style when dumping a list of dicts
    if isinstance(data, list) and data and isinstance(data[0], dict):
        raw = raw.replace("[\n  {", "[ {", 1)
        raw = raw.replace("\n  },\n  {", "\n}, {")
    return raw + "\n"


def transform_training(data: list) -> tuple[list, dict[int, int], dict[int, dict]]:
    by_id = {t["id"]: t for t in data}

    greater = [
        t for t in data
        if t.get("type") == "Maintained"
        and t.get("affinity") in AFFINITIES
        and is_greater_name(t["name"])
    ]
    lesser = [
        t for t in data
        if t.get("type") == "Maintained"
        and t.get("affinity") in AFFINITIES
        and is_lesser_name(t["name"])
    ]

    greater_to_lesser: dict[int, int] = {}
    for g in greater:
        pre = by_id.get(g["prereq"])
        if not pre or not is_lesser_name(pre["name"]):
            raise SystemExit(
                f"Greater {g['id']} {g['name']} prereq {g['prereq']} is not a Lesser"
            )
        greater_to_lesser[g["id"]] = pre["id"]

    greater_ids = set(greater_to_lesser.keys())
    lesser_ids = {t["id"] for t in lesser}

    # Update prereqs pointing at Greater -> Lesser
    prereq_updates = 0
    for t in data:
        if t.get("prereq") in greater_ids:
            t["prereq"] = greater_to_lesser[t["prereq"]]
            prereq_updates += 1

    # Rename Lesser maintained techniques
    renamed = 0
    for t in lesser:
        new_name = strip_lesser_tag(t["name"])
        if new_name != t["name"]:
            t["name"] = new_name
            renamed += 1

    # Remove Greater entries
    new_data = [t for t in data if t["id"] not in greater_ids]

    # Snapshot of updated lesser techs for character migration
    lesser_by_id = {t["id"]: dict(t) for t in new_data if t["id"] in lesser_ids}

    print(f"Greater removed: {len(greater_ids)}")
    print(f"Lesser renamed: {renamed}")
    print(f"Prereq retargeted: {prereq_updates}")
    print(f"Entries before/after: {len(data)} -> {len(new_data)}")
    return new_data, greater_to_lesser, lesser_by_id


def migrate_char_file(path: Path, greater_to_lesser: dict[int, int], lesser_by_id: dict[int, dict]) -> bool:
    with path.open(encoding="utf-8") as f:
        text = f.read()
    # Fast skip
    if not any(str(gid) in text for gid in greater_to_lesser):
        return False

    data = json.loads(text)
    training = data.get("training") or {}
    by_cat = training.get("trainingByCategory") or {}
    changed = False

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
                changed = True
                # If lesser already owned, drop greater
                if lid in owned_ids:
                    continue
                # Replace greater with updated lesser template, preserve rank/al/exp
                template = lesser_by_id.get(lid)
                if template is None:
                    # fallback: just retarget id/name
                    tech["id"] = lid
                    tech["name"] = strip_lesser_tag(tech.get("name", ""))
                    new_list.append(tech)
                    owned_ids.add(lid)
                    continue
                replacement = dict(template)
                for keep in ("rank", "al", "exp", "listName", "listMaxPerRank", "listMaxBase"):
                    if keep in tech:
                        replacement[keep] = tech[keep]
                # Keep character's permStatus if present
                if "permStatus" in tech:
                    replacement["permStatus"] = tech["permStatus"]
                new_list.append(replacement)
                owned_ids.add(lid)
            else:
                # Also rename any stored lesser copies still tagged
                if (
                    tech.get("type") == "Maintained"
                    and tech.get("affinity") in AFFINITIES
                    and is_lesser_name(tech.get("name", ""))
                ):
                    tech["name"] = strip_lesser_tag(tech["name"])
                    changed = True
                new_list.append(tech)
        by_cat[cat] = new_list

    if not changed:
        return False

    # Preserve character file formatting style as much as possible
    out = jackson_dump(data)
    path.write_text(out, encoding="utf-8")
    return True


def main():
    with TRAINING_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    new_data, greater_to_lesser, lesser_by_id = transform_training(data)
    TRAINING_PATH.write_text(jackson_dump(new_data), encoding="utf-8")
    print(f"Wrote {TRAINING_PATH}")

    # Verify
    with TRAINING_PATH.open(encoding="utf-8") as f:
        verify = json.load(f)
    remaining_greater = [
        t for t in verify
        if t.get("type") == "Maintained"
        and t.get("affinity") in AFFINITIES
        and is_greater_name(t["name"])
    ]
    remaining_lesser_tag = [
        t for t in verify
        if t.get("type") == "Maintained"
        and t.get("affinity") in AFFINITIES
        and is_lesser_name(t["name"])
    ]
    bad_prereq = [
        t for t in verify
        if t.get("prereq") in greater_to_lesser
    ]
    print(f"Verify remaining Greater: {len(remaining_greater)}")
    print(f"Verify remaining (Lesser) tags: {len(remaining_lesser_tag)}")
    print(f"Verify dangling greater prereqs: {len(bad_prereq)}")
    if remaining_greater:
        for t in remaining_greater[:10]:
            print(" ", t["id"], t["name"])
    if remaining_lesser_tag:
        for t in remaining_lesser_tag[:10]:
            print(" ", t["id"], t["name"])
    if bad_prereq:
        for t in bad_prereq[:10]:
            print(" ", t["id"], t["name"], t["prereq"])

    # Sample renamed + retargeted
    samples = [5001, 6001, 8001, 9001, 11302, 12302, 18217]
    by_id = {t["id"]: t for t in verify}
    print("\nSamples:")
    for sid in samples:
        t = by_id.get(sid)
        if t:
            print(f"  {t['id']}: {t['name']} | {t['type']} | prereq={t['prereq']}")
        else:
            print(f"  {sid}: REMOVED")


if __name__ == "__main__":
    main()
