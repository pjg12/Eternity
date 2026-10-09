import json
from collections import Counter

data = json.load(open(r"C:\EternityTTRPG\Characters\12.json", encoding="utf-8"))
by = data["training"]["trainingByCategory"]
print("categories:", list(by.keys()))
for cat, techs in by.items():
    if not isinstance(techs, list):
        print(cat, type(techs))
        continue
    types = Counter(t.get("type") for t in techs if isinstance(t, dict))
    print(f"{cat}: {len(techs)} techs {dict(types)}")
    for t in techs[:5]:
        if isinstance(t, dict):
            print(f"  {t.get('id')}: {t.get('name')} | {t.get('type')}")
