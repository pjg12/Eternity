import json
from pathlib import Path

AFF = {
    "body", "nature", "metal", "earth", "water", "air", "fire", "electricity",
    "energy", "sound", "light", "darkness", "poison", "psionic", "force",
}

tr = json.loads(Path("Data/trainingdata.json").read_text(encoding="utf-8"))
acts = json.loads(Path("Data/actiondata.json").read_text(encoding="utf-8"))
acts_by_id = {a["id"]: a for a in acts}

print("=== Training Medium leftovers (any type) in target aff ===")
for t in tr:
    n = t.get("name") or ""
    if "(Medium)" in n and (t.get("affinity") or "").lower() in AFF:
        print(t["id"], n, t.get("type"), t.get("affinity"))

print("\n=== Action Medium leftovers in target aff ===")
for a in acts:
    n = a.get("name") or ""
    if n.endswith("(Medium)") and (a.get("affinity") or "").lower() in AFF:
        print(a["id"], n, a.get("affinity"))

print("\n=== Strong prereq check (sample) ===")
for name in ["Electric Smash (Strong)", "Body Smash (Strong)", "Sound Smash (Strong)", "Poison Smash (Strong)"]:
    t = next(x for x in tr if x.get("name") == name)
    pre = next(x for x in tr if x["id"] == t["prereq"])
    print(f"{name} -> {pre['name']} (id {pre['id']})")
    grants = t.get("grant") or []
    for g in grants:
        ag = acts_by_id.get(g)
        print(f"  grant {g} -> {ag.get('name') if ag else 'MISSING ACTION'}")

print("\n=== Char 12 electricity smash entries ===")
c = json.loads(Path("Characters/12.json").read_text(encoding="utf-8"))
by_tr = {t["id"]: t for t in tr}
for cat, lst in c["training"]["trainingByCategory"].items():
    if not isinstance(lst, list):
        continue
    for rec in lst:
        tid = rec.get("id")
        t = by_tr.get(tid)
        if t and "Smash" in (t.get("name") or "") and "Electric" in t["name"]:
            print(cat, tid, t["name"], rec.get("rank"))

# atdata
at_path = Path("Data/atdata.json")
if at_path.exists():
    at = json.loads(at_path.read_text(encoding="utf-8"))
    # structure unknown - search string
    text = at_path.read_text(encoding="utf-8")
    import re
    hits = re.findall(r'"name"\s*:\s*"[^"]*\(Medium\)[^"]*"', text)
    # filter by nearby affinity is hard; count Medium smash/heal etc
    print(f"\natdata (Medium) name hits: {len(hits)}")
