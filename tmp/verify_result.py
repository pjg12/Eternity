import json

with open(r"C:\EternityTTRPG\Data\trainingdata.json", encoding="utf-8") as f:
    data = json.load(f)

AFFINITIES = {
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
}

# Spot-check a few affinities for maintained + their passives
checks = [
    ("Body", "Evolved"),
    ("Nature", "Feral"),
    ("Metal", "Steel"),
    ("Energy", "Empowered"),
    ("Psionic", "Mental"),
]

by_id = {t["id"]: t for t in data}
maintained = [
    t for t in data
    if t.get("type") == "Maintained" and t.get("affinity") in AFFINITIES
]
print(f"Affinity maintained remaining: {len(maintained)}")
tagged = [t for t in maintained if "(Lesser)" in t["name"] or "(Greater)" in t["name"] or "(lesser)" in t["name"]]
print(f"Still tagged Lesser/Greater: {len(tagged)}")

# For each affinity, count maintained and passives that prereq them
for aff in sorted(AFFINITIES):
    m = [t for t in data if t.get("type") == "Maintained" and t.get("affinity") == aff]
    m_ids = {t["id"] for t in m}
    p = [t for t in data if t.get("type") == "Passive" and t.get("prereq") in m_ids]
    # passives whose prereq was a former greater should now point at existing maintained
    missing = [t for t in p if t["prereq"] not in by_id]
    print(f"{aff}: maintained={len(m)} passives_on_maint={len(p)} missing_prereq={len(missing)}")

# Special names that had quirks
for name_part in ["Steel Severity", "Mental Precision", "Aquatic Flight", "Water Flight"]:
    hits = [t for t in data if name_part in t["name"]]
    for t in hits:
        print(f"  {t['id']}: {t['name']} | {t['type']} | prereq={t['prereq']}")
