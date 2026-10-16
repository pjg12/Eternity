import json
from pathlib import Path
c = json.loads(Path(r"Characters/12.json").read_text(encoding="utf-8"))
ba = c["resources"]["baseAura"]
print(type(ba), len(ba))
print(type(ba[0]), len(ba[0]) if ba[0] is not None else None)
print(json.dumps(ba[0][:2], indent=2)[:500])
print("---")
# techperm 551-553
tp = json.loads(Path(r"Data/techpermdata.json").read_text(encoding="utf-8"))
for p in tp:
    if int(p.get("id",-1)) in (551,552,553,561):
        print(json.dumps(p, indent=2)[:600])
        print("---")
# training 2261
tr = json.loads(Path(r"Data/trainingdata.json").read_text(encoding="utf-8"))
for t in tr:
    if int(t.get("id",-1)) in (2261,2262,2263,53,26223):
        print(t.get("id"), t.get("name"), t.get("type"), "grant", t.get("grant"), "perm", t.get("permStatus"))
