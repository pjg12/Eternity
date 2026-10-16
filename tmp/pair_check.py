import json
from collections import Counter

with open(r"C:\EternityTTRPG\Data\trainingdata.json") as f:
    data = json.load(f)

by_id = {t["id"]: t for t in data}
affinities = [
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
]

greater = [
    t for t in data
    if t.get("type") == "Maintained"
    and t.get("affinity") in affinities
    and "(Greater)" in t["name"].replace(" ", "")  # rough
]
# Better: find by name suffix more carefully
greater = []
lesser = []
for t in data:
    if t.get("type") != "Maintained" or t.get("affinity") not in affinities:
        continue
    name = t["name"]
    if "(Greater)" in name or name.endswith("Greater)"):
        greater.append(t)
    elif "(Lesser)" in name:
        lesser.append(t)

print("Greater count", len(greater), "Lesser count", len(lesser))

# Pair via prereq: greater.prereq should be lesser
pairs = []
bad = []
for g in greater:
    pre = by_id.get(g["prereq"])
    if pre and "(Lesser)" in pre["name"]:
        pairs.append((pre, g))
    else:
        bad.append(g)
        print("BAD greater:", g["id"], g["name"], "prereq", g["prereq"], pre)

print("Pairs via prereq:", len(pairs), "bad:", len(bad))

# Who prereqs lesser?
lesser_ids = {t["id"] for t in lesser}
who = [t for t in data if t.get("prereq") in lesser_ids]
print("Entries prereqing lesser:", len(who))
print(Counter(t["type"] for t in who))
print("Sample:")
for t in who[:15]:
    print(f"  {t['id']} {t['type']} {t['name']} -> {t['prereq']}")

# Flight names
flights = [t for t in data if "Flight" in t["name"] and t.get("affinity") in affinities]
print("\nFlight techniques:")
for t in flights:
    print(f"  {t['id']} {t['type']} {t['name']} prereq={t['prereq']}")
