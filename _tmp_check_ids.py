import json

acts = {a["id"]: a for a in json.load(open("Data/actiondata.json", encoding="utf-8"))}
tr = {t["id"]: t for t in json.load(open("Data/trainingdata.json", encoding="utf-8"))}

names = [
    "Body Eruption (Weak)",
    "Body Eruption (Strong)",
    "Body Heal (Weak)",
    "Body Heal (Strong)",
    "Body Mend (Weak)",
    "Body Smash (Weak)",
    "Body Smash (Strong)",
    "Electric Smash (Weak)",
    "Electric Smash (Strong)",
    "Sound Smash (Weak)",
    "Sound Smash (Strong)",
    "Poison Smash (Weak)",
    "Poison Smash (Strong)",
]
for name in names:
    aids = [(i, a["name"]) for i, a in acts.items() if a.get("name") == name]
    t = next((x for x in tr.values() if x.get("name") == name), None)
    print(name)
    print("  actions", aids)
    print("  training", None if not t else {"id": t["id"], "grant": t.get("grant"), "prereq": t.get("prereq")})

print("600xx eruptions:")
for i in sorted(acts):
    if 60000 <= i <= 60050:
        print(i, acts[i]["name"])
