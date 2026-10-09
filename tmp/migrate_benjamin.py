import json
import re
import subprocess
from pathlib import Path

CHAR_PATH = Path(r"C:\EternityTTRPG\Characters\12.json")
TRAINING_PATH = Path(r"C:\EternityTTRPG\Data\trainingdata.json")

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
    return re.sub(r"\s*\([Ll]esser\)\s*", "", name).strip()


def strip_greater_tag(name: str) -> str:
    return re.sub(r"\s*\([Gg]reater\)\s*", "", name).strip()


def jackson_dump(data) -> str:
    raw = json.dumps(data, indent=2, ensure_ascii=False)
    raw = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", raw)
    return raw + "\n"


def build_greater_map() -> dict[int, int]:
    result = subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    if result.returncode != 0:
        raise SystemExit(result.stderr)
    old = json.loads(result.stdout)
    by_id = {t["id"]: t for t in old}
    mapping = {}
    for t in old:
        if (
            t.get("type") == "Maintained"
            and t.get("affinity") in AFFINITIES
            and is_greater_name(t["name"])
        ):
            pre = by_id.get(t["prereq"])
            if pre is not None:
                mapping[t["id"]] = pre["id"]
    return mapping


def main():
    greater_to_lesser = build_greater_map()

    with TRAINING_PATH.open(encoding="utf-8") as f:
        training_data = json.load(f)
    by_id = {t["id"]: t for t in training_data}

    with CHAR_PATH.open(encoding="utf-8") as f:
        data = json.loads(f.read())

    training = data.get("training") or {}
    by_cat = training.get("trainingByCategory") or {}
    report = []

    # Extend map from any Greater still on the character (covers post-HEAD additions)
    for techs in by_cat.values():
        if not isinstance(techs, list):
            continue
        for tech in techs:
            if not isinstance(tech, dict):
                continue
            if (
                tech.get("type") == "Maintained"
                and tech.get("affinity") in AFFINITIES
                and is_greater_name(tech.get("name", ""))
            ):
                lesser_id = tech.get("prereq")
                if isinstance(lesser_id, int) and lesser_id > 0:
                    greater_to_lesser[tech["id"]] = lesser_id

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
            name = tech.get("name", "")

            if tid in greater_to_lesser:
                lid = greater_to_lesser[tid]
                if lid in owned_ids:
                    report.append(f"[{cat}] DROP Greater {tid} {name} (already have {lid})")
                    continue
                template = by_id.get(lid)
                if template is None:
                    tech["id"] = lid
                    tech["name"] = strip_greater_tag(strip_lesser_tag(name))
                    report.append(f"[{cat}] REMAP {tid}->{lid} {tech['name']} (no template)")
                    new_list.append(tech)
                    owned_ids.add(lid)
                    continue
                replacement = dict(template)
                for keep in ("rank", "al", "exp", "listName", "listMaxPerRank", "listMaxBase"):
                    if keep in tech:
                        replacement[keep] = tech[keep]
                if "permStatus" in tech:
                    replacement["permStatus"] = tech["permStatus"]
                report.append(
                    f"[{cat}] REMAP {tid}->{lid} {name} -> {replacement['name']} "
                    f"(rank={replacement.get('rank')}, al={replacement.get('al')})"
                )
                new_list.append(replacement)
                owned_ids.add(lid)
                continue

            if (
                tech.get("type") == "Maintained"
                and tech.get("affinity") in AFFINITIES
                and is_lesser_name(name)
            ):
                new_name = strip_lesser_tag(name)
                report.append(f"[{cat}] RENAME {tid} {name} -> {new_name}")
                template = by_id.get(tid)
                if template is not None:
                    preserved = {
                        k: tech[k]
                        for k in ("rank", "al", "exp", "listName", "listMaxPerRank", "listMaxBase")
                        if k in tech
                    }
                    tech = dict(template)
                    tech.update(preserved)
                else:
                    tech["name"] = new_name
                new_list.append(tech)
                continue

            if tech.get("prereq") in greater_to_lesser:
                old_pre = tech["prereq"]
                tech["prereq"] = greater_to_lesser[old_pre]
                report.append(
                    f"[{cat}] PREREQ {tid} {name}: {old_pre} -> {tech['prereq']}"
                )

            new_list.append(tech)

        by_cat[cat] = new_list

    CHAR_PATH.write_text(jackson_dump(data), encoding="utf-8")

    print(f"Changes: {len(report)}")
    for line in report:
        print(" ", line)

    leftovers = []
    for cat, techs in by_cat.items():
        if not isinstance(techs, list):
            continue
        for t in techs:
            if not isinstance(t, dict):
                continue
            n = t.get("name", "")
            if t.get("affinity") in AFFINITIES and (
                is_greater_name(n) or is_lesser_name(n) or t.get("id") in greater_to_lesser
            ):
                leftovers.append((cat, t.get("id"), n))
            if t.get("prereq") in greater_to_lesser:
                leftovers.append((cat, t.get("id"), n, f"prereq={t.get('prereq')}"))
    print(f"Leftovers: {len(leftovers)}")
    for item in leftovers:
        print(" ", item)


if __name__ == "__main__":
    main()
