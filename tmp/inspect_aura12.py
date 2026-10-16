import json
from pathlib import Path
import re

c = json.loads(Path(r"Characters/12.json").read_text(encoding="utf-8"))
training = json.loads(Path(r"Data/trainingdata.json").read_text(encoding="utf-8"))
techperm = json.loads(Path(r"Data/techpermdata.json").read_text(encoding="utf-8"))
train_by_id = {int(t["id"]): t for t in training}
perm_by_id = {int(p["id"]): p for p in techperm}

def walk_statuses(node, path=""):
    if isinstance(node, list):
        for i, item in enumerate(node):
            yield from walk_statuses(item, f"{path}[{i}]")
    elif isinstance(node, dict):
        if "attribute" in node and "magnitude" in node:
            yield path, node
        else:
            for k,v in node.items():
                yield from walk_statuses(v, f"{path}.{k}" if path else k)

print("=== CAP-related statuses ===")
cap_total = 0.0
for path, s in walk_statuses(c.get("attributes")):
    attr = (s.get("attribute") or "").upper()
    if attr in ("BCAP", "MCAP", "CAP"):
        mag = float(s.get("magnitude") or 0)
        if mag:
            print(f"{attr} {s.get('durationType')} mag={mag} name={s.get('name')}")
            if attr == "BCAP":
                cap_total += mag
            elif attr == "MCAP":
                cap_total += mag  # if additive in same calc; actual formula may differ
print("naive sum", cap_total)

print("\n=== Reminder maxaura/auramulti (default loadout) ===")
for k,v in sorted(c.get("reminderSelections", {}).items()):
    m = re.search(r"maintained\.al\.(\d+)\.(maxaura|auramulti)", k)
    if not m or "default" not in k:
        continue
    tid = int(m.group(1)); kind = m.group(2); al = int(v)
    t = train_by_id.get(tid)
    print(f"AL={al:>2} {kind:<8} tech={tid} {(t or {}).get('name')}")
    for g in (t or {}).get("grant") or []:
        p = perm_by_id.get(int(g))
        if p:
            print(f"         perm {g}: attr={p.get('attribute')} sev={p.get('severity')} desc={p.get('description')}")
        else:
            print(f"         grant {g}: NOT IN TECHPERM")

print("\n=== Saved multiAura non-zero ===")
for bucket in c["resources"]["multiAura"]:
    for s in bucket or []:
        if not isinstance(s, dict):
            continue
        if float(s.get("magnitude") or 0) != 0:
            print(s)

print("\n=== Saved baseAura total ===")
base=0
for bucket in c["resources"]["baseAura"]:
    for s in bucket or []:
        if isinstance(s, dict):
            base += float(s.get("magnitude") or 0)
multi=0
for bucket in c["resources"]["multiAura"]:
    for s in bucket or []:
        if isinstance(s, dict):
            multi += float(s.get("magnitude") or 0)
print(f"base={base} multi={multi} max={int(base*(1+multi))}")
