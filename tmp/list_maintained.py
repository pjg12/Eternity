import json

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
print(f"Count: {len(maintained)}")
for t in maintained:
    print(
        f"{t['id']}: {t['name']} | {t['affinity']} | prereq={t['prereq']}"
    )

# Also show passives that prereq any of these maintained
maint_ids = {t["id"] for t in maintained}
greater_ids = {t["id"] for t in maintained if "(Greater)" in t["name"]}
print("\n--- Passives that prereq Greater maintained ---")
for t in data:
    if t.get("type") == "Passive" and t.get("prereq") in greater_ids:
        print(f"{t['id']}: {t['name']} prereq={t['prereq']} aff={t['affinity']}")

print("\n--- Passives that prereq any affinity maintained ---")
for t in data:
    if t.get("type") == "Passive" and t.get("prereq") in maint_ids:
        print(f"{t['id']}: {t['name']} prereq={t['prereq']} aff={t['affinity']}")
