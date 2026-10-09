import json
from collections import defaultdict

with open(r"C:\EternityTTRPG\Data\trainingdata.json") as f:
    data = json.load(f)

affinities = [
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
]

maintained = [
    t for t in data
    if t.get("type") == "Maintained" and t.get("affinity") in affinities
]
greater = [t for t in maintained if "(Greater)" in t["name"]]
lesser = [t for t in maintained if "(Lesser)" in t["name"]]
other = [t for t in maintained if "(Greater)" not in t["name"] and "(Lesser)" not in t["name"]]

print(f"maintained={len(maintained)} lesser={len(lesser)} greater={len(greater)} other={len(other)}")
print("Other maintained:")
for t in other:
    print(f"  {t['id']}: {t['name']} | {t['affinity']}")

# Map greater -> lesser by name pairing
by_base = defaultdict(dict)
for t in lesser + greater:
    name = t["name"]
    if "(Lesser)" in name:
        base = name.replace(" (Lesser)", "")
        by_base[base]["lesser"] = t
    elif "(Greater)" in name:
        base = name.replace(" (Greater)", "")
        by_base[base]["greater"] = t

unpaired = []
for base, pair in by_base.items():
    if "lesser" not in pair or "greater" not in pair:
        unpaired.append((base, pair.keys()))
print(f"\nPairs: {len(by_base)} unpaired: {len(unpaired)}")
for u in unpaired[:20]:
    print(u)

# Passives prereqing greater
greater_ids = {t["id"] for t in greater}
lesser_ids = {t["id"] for t in lesser}
passives_on_greater = [t for t in data if t.get("prereq") in greater_ids]
passives_on_lesser = [t for t in data if t.get("prereq") in lesser_ids]
print(f"\nPassives prereq Greater: {len(passives_on_greater)}")
print(f"Passives prereq Lesser: {len(passives_on_lesser)}")
print(f"Any other refs to greater as prereq: {sum(1 for t in data if t.get('prereq') in greater_ids)}")

# Check grant IDs of greater - do they matter for atdata?
print("\nSample greater grants:", greater[0]["grant"] if greater else None)
print("Sample lesser grants:", lesser[0]["grant"] if lesser else None)
