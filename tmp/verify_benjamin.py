import json

data = json.load(open(r"C:\EternityTTRPG\Characters\12.json", encoding="utf-8"))
print("name", data["identity"]["name"])
by = data["training"]["trainingByCategory"]
for cat in ["nature", "electricity", "psionic", "fire", "energy"]:
    techs = by.get(cat, [])
    maint = [t for t in techs if t.get("type") == "Maintained"]
    print(f"--- {cat}: {len(maint)} maintained ---")
    for t in maint[:10]:
        print(f"  {t['id']}: {t['name']}")
    if len(maint) > 10:
        print(f"  ... +{len(maint) - 10} more")
    bad = [
        t
        for t in techs
        if "Lesser" in t.get("name", "") or "Greater" in t.get("name", "")
    ]
    print(f"  tagged names: {len(bad)}")
