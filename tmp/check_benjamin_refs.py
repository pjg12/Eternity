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
    by_id = {t["id"]: t for t in old}
    mapping = {}
    for t in old:
        if (
            t.get("type") == "Maintained"
            and t.get("affinity") in AFFINITIES
            and "(Greater)" in t["name"]
        ):
            mapping[t["id"]] = t["prereq"]
    return mapping


def find_refs(obj, path, greater_ids, hits):
    if isinstance(obj, dict):
        for k, v in obj.items():
            find_refs(v, f"{path}.{k}", greater_ids, hits)
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            find_refs(v, f"{path}[{i}]", greater_ids, hits)
    elif isinstance(obj, int):
        if obj in greater_ids:
            hits.append((path, obj))
    elif isinstance(obj, str):
        for gid in greater_ids:
            if str(gid) in obj:
                # avoid substring false positives: require non-digit boundaries
                if re.search(rf"(?<!\d){gid}(?!\d)", obj):
                    hits.append((path, obj, gid))


def main():
    mapping = build_greater_map()
    with CHAR_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    # Owned training ids after migration
    owned = set()
    for techs in data["training"]["trainingByCategory"].values():
        for t in techs:
            owned.add(t["id"])

    leftover_greater = sorted(owned & set(mapping))
    print(f"Owned greater IDs remaining: {leftover_greater}")

    hits = []
    find_refs(data, "$", set(mapping), hits)
    # Filter to meaningful hits outside trainingByCategory ids we already cleaned
    interesting = []
    for h in hits:
        path = h[0]
        if "trainingByCategory" in path and path.endswith(".id"):
            continue
        interesting.append(h)
    print(f"Other references to greater IDs: {len(interesting)}")
    for h in interesting[:50]:
        print(" ", h)

    # Summarize removed vs kept for nature/electricity from backup comparison
    backup = json.load(open(r"C:\EternityTTRPG\Characters\Backup\12Backup3.json", encoding="utf-8"))
    for cat in ["nature", "electricity", "psionic", "fire", "energy"]:
        old_ids = {t["id"] for t in backup["training"]["trainingByCategory"].get(cat, [])}
        new_ids = {t["id"] for t in data["training"]["trainingByCategory"].get(cat, [])}
        removed = sorted(old_ids - new_ids)
        added = sorted(new_ids - old_ids)
        print(f"{cat}: removed {removed} added {added}")


if __name__ == "__main__":
    main()
