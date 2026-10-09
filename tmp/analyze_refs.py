import json

# Check atdata for corresponding Greater entries referenced by maintained grants
with open(r"C:\EternityTTRPG\Data\trainingdata.json") as f:
    training = json.load(f)

affinities = [
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
]

greater = [
    t for t in training
    if t.get("type") == "Maintained"
    and t.get("affinity") in affinities
    and "(Greater)" in t["name"]
]
lesser = [
    t for t in training
    if t.get("type") == "Maintained"
    and t.get("affinity") in affinities
    and "(Lesser)" in t["name"]
]

greater_grants = sorted({g for t in greater for g in t.get("grant", []) if g != -1})
lesser_grants = sorted({g for t in lesser for g in t.get("grant", []) if g != -1})
print(f"Greater grant IDs: {len(greater_grants)}")
print(f"Lesser grant IDs: {len(lesser_grants)}")
print(f"Overlap: {len(set(greater_grants) & set(lesser_grants))}")
print("Sample greater grants:", greater_grants[:20])
print("Sample lesser grants:", lesser_grants[:20])

# Check if any other training entries grant those greater grant IDs or use greater as prereq beyond passives
greater_ids = {t["id"] for t in greater}
refs = []
for t in training:
    if t["id"] in greater_ids:
        continue
    if t.get("prereq") in greater_ids:
        refs.append(("prereq", t["id"], t["name"], t["type"], t["prereq"]))
    grants = t.get("grant") or []
    for g in grants:
        if g in greater_ids:
            refs.append(("grant", t["id"], t["name"], t["type"], g))

print(f"\nOther training refs to greater IDs: {len(refs)}")
for r in refs[:30]:
    print(r)

# Check characters for greater maintained IDs
import os
greater_id_set = greater_ids
char_hits = []
for root, dirs, files in os.walk(r"C:\EternityTTRPG\Characters"):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        path = os.path.join(root, fn)
        try:
            with open(path, encoding="utf-8") as f:
                text = f.read()
        except Exception:
            continue
        # rough scan
        for gid in greater_id_set:
            if str(gid) in text:
                # more precise later if needed
                char_hits.append((path, gid))
                break
print(f"\nCharacter files mentioning any greater id (rough): {len(char_hits)}")
