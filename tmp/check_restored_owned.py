import json
from pathlib import Path

RESTORE_AFFINITIES = {"Enhancement", "Spirit", "Time"}

# Current trainingdata has restored Mediums — use it for names/affinities.
# Also build medium id set from current data for those affinities.
training = json.load(open(r"C:\EternityTTRPG\Data\trainingdata.json", encoding="utf-8"))
by_id = {t["id"]: t for t in training}

medium_ids = {
    t["id"]
    for t in training
    if t.get("type") == "Active"
    and t.get("affinity") in RESTORE_AFFINITIES
    and "(Medium)" in t.get("name", "")
}

# Also include Weak/Strong for those affinities in case user means the whole restored chains
related_ids = {
    t["id"]
    for t in training
    if t.get("type") == "Active"
    and t.get("affinity") in RESTORE_AFFINITIES
    and any(tag in t.get("name", "") for tag in ("(Weak)", "(Medium)", "(Strong)"))
}

snapshots = {
    12: Path(r"C:\EternityTTRPG\tmp\12_pre_medium_migration.json"),
    3: Path(r"C:\EternityTTRPG\tmp\3_pre_medium_migration.json"),
    2: Path(r"C:\EternityTTRPG\tmp\2_pre_medium_migration.json"),
}

for char_id, path in snapshots.items():
    data = json.load(path.open(encoding="utf-8"))
    name = data["identity"]["name"]
    owned = []
    for cat, techs in data["training"]["trainingByCategory"].items():
        for t in techs:
            owned.append((cat, t["id"], t.get("rank", 0)))

    medium_hits = []
    related_hits = []
    for cat, tid, rank in owned:
        meta = by_id.get(tid)
        if tid in medium_ids:
            medium_hits.append((cat, tid, rank, meta["name"] if meta else "?"))
        elif tid in related_ids:
            related_hits.append(
                (
                    cat,
                    tid,
                    rank,
                    meta["name"] if meta else "?",
                    meta.get("affinity") if meta else "?",
                )
            )

    print(f"=== Character {char_id}: {name} ===")
    print(f"Restored Medium techniques owned before migration: {len(medium_hits)}")
    for row in medium_hits:
        print(f"  {row}")
    print(f"Other Enhancement/Spirit/Time Weak|Strong owned: {len(related_hits)}")
    # Only show if useful for context, keep brief
    by_aff = {}
    for cat, tid, rank, n, aff in related_hits:
        by_aff.setdefault(aff, []).append((tid, rank, n))
    for aff, rows in sorted(by_aff.items()):
        print(f"  {aff}: {len(rows)} techs")
        for r in rows[:8]:
            print(f"    {r}")
        if len(rows) > 8:
            print(f"    ... +{len(rows)-8} more")
    print()
