"""Fix Active Weak/Strong grants that point at removed Medium or missing actions."""
from pathlib import Path
import json
import re

AFF = {
    "body", "nature", "metal", "earth", "water", "air", "fire", "electricity",
    "energy", "sound", "light", "darkness", "poison", "psionic", "force",
}

TRAINING = Path("Data/trainingdata.json")
ACTION = Path("Data/actiondata.json")
ATDATA = Path("Data/atdata.json")

tr_list = json.loads(TRAINING.read_text(encoding="utf-8"))
acts = {a["id"]: a for a in json.loads(ACTION.read_text(encoding="utf-8"))}
# name -> id for actions (prefer exact)
acts_by_name = {}
for a in acts.values():
    acts_by_name[a.get("name") or ""] = a["id"]

fixes = []
for t in tr_list:
    if t.get("type") != "Active":
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFF:
        continue
    name = t.get("name") or ""
    if not (name.endswith("(Weak)") or name.endswith("(Strong)")):
        continue
    grants = t.get("grant") or []
    if len(grants) != 1:
        # still try first grant
        pass
    new_grants = []
    changed = False
    for g in grants:
        a = acts.get(g)
        if a and a.get("name") == name:
            new_grants.append(g)
            continue
        # remap by technique name
        target = acts_by_name.get(name)
        if target is None:
            print(f"NO ACTION for training {t['id']} {name}")
            new_grants.append(g)
            continue
        if g != target:
            fixes.append((t["id"], name, g, target, a.get("name") if a else "MISSING"))
            changed = True
        new_grants.append(target)
    if changed:
        t["grant"] = new_grants

print(f"Grant fixes: {len(fixes)}")
for row in fixes:
    print(f"  {row[0]} {row[1]}: {row[2]} ({row[4]}) -> {row[3]}")

# Apply surgically to training text
text = TRAINING.read_text(encoding="utf-8")
for tid, name, old_g, new_g, _ in fixes:
    id_match = re.search(rf'"id"\s*:\s*{tid}\b', text)
    if not id_match:
        raise SystemExit(f"missing {tid}")
    start = id_match.start()
    nxt = re.search(r'\n  \{\s*\n\s*"id"\s*:', text[start + 1 :])
    end = start + 1 + nxt.start() if nxt else start + 2000
    window = text[start:end]
    # replace grant array contents
    gm = re.search(r'"grant"\s*:\s*\[(.*?)\]', window, re.S)
    if not gm:
        raise SystemExit(f"no grant {tid}")
    inner = gm.group(1)
    if str(old_g) not in inner and str(new_g) in inner:
        continue
    # simple single-id grants
    new_inner = re.sub(rf"\b{old_g}\b", str(new_g), inner, count=1)
    new_block = '"grant" : [' + new_inner + ']'
    # preserve original bracket style roughly
    new_block = gm.group(0)
    new_block = re.sub(rf"\b{old_g}\b", str(new_g), new_block, count=1)
    text = text[: start + gm.start()] + new_block + text[start + gm.end() :]

TRAINING.write_text(text, encoding="utf-8")
json.loads(TRAINING.read_text(encoding="utf-8"))

# Remove Medium entries from atdata for target affinities
if ATDATA.exists():
    at = json.loads(ATDATA.read_text(encoding="utf-8"))
    before = len(at)
    at2 = [
        e
        for e in at
        if not (
            isinstance(e, dict)
            and (e.get("name") or "").endswith("(Medium)")
            and (e.get("affinity") or "").strip().lower() in AFF
        )
    ]
    print(f"atdata {before} -> {len(at2)} (removed {before - len(at2)} Medium)")
    ATDATA.write_text(json.dumps(at, indent=4, ensure_ascii=False) + "\n", encoding="utf-8") if False else None
    # write at2
    ATDATA.write_text(json.dumps(at2, indent=4, ensure_ascii=False) + "\n", encoding="utf-8")

# Final verify
tr2 = json.loads(TRAINING.read_text(encoding="utf-8"))
acts2 = {a["id"]: a for a in json.loads(ACTION.read_text(encoding="utf-8"))}
missing = 0
for t in tr2:
    if t.get("type") != "Active":
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFF:
        continue
    name = t.get("name") or ""
    if "(Medium)" in name:
        print("STILL MEDIUM TRAINING", t["id"], name)
    if name.endswith("(Strong)"):
        pre = next(x for x in tr2 if x["id"] == t["prereq"])
        assert pre["name"].endswith("(Weak)"), (t["name"], pre["name"])
    for g in t.get("grant") or []:
        if g not in acts2:
            print("MISSING GRANT", t["id"], t["name"], g)
            missing += 1
        elif name.endswith("(Weak)") or name.endswith("(Strong)"):
            if acts2[g].get("name") != name:
                print("GRANT NAME MISMATCH", t["id"], name, "->", acts2[g].get("name"))
print(f"missing grants after fix: {missing}")
print("Electric Smash char12:", end=" ")
c = json.loads(Path("Characters/12.json").read_text(encoding="utf-8"))
for rec in c["training"]["trainingByCategory"].get("electricity", []):
    if rec["id"] in (25010, 25011, 25012):
        print(rec, end=" ")
print()
