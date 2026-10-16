"""Remove only Active Medium actions for target affinities from actiondata.json."""
from pathlib import Path
import json
import re

AFFINITIES = {
    "body", "nature", "metal", "earth", "water", "air", "fire", "electricity",
    "energy", "sound", "light", "darkness", "poison", "psionic", "force",
}

ACTION = Path("Data/actiondata.json")
text = ACTION.read_text(encoding="utf-8")
actions = json.loads(text)

remove_ids = []
for a in actions:
    name = a.get("name") or ""
    aff = (a.get("affinity") or "").strip().lower()
    if name.endswith("(Medium)") and aff in AFFINITIES:
        remove_ids.append((a["id"], name, a.get("affinity")))

print(f"Removing {len(remove_ids)} Medium actions")
for row in remove_ids[:10]:
    print(" ", row)
if len(remove_ids) > 10:
    print(f"  ... +{len(remove_ids)-10} more")

remove_set = {i for i, _, _ in remove_ids}
removed = 0
for aid in sorted(remove_set, reverse=True):
    m = re.search(rf'"id"\s*:\s*{aid}\b', text)
    if not m:
        print("WARNING missing", aid)
        continue
    brace = text.rfind("{", 0, m.start())
    depth = 0
    i = brace
    while i < len(text):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                j = end
                while j < len(text) and text[j] in " \t":
                    j += 1
                if j < len(text) and text[j] == ",":
                    j += 1
                while j < len(text) and text[j] in " \t":
                    j += 1
                if j < len(text) and text[j] == "\n":
                    j += 1
                text = text[:brace] + text[j:]
                removed += 1
                break
        i += 1

text = re.sub(r",\s*\]", "\n]", text)
ACTION.write_text(text, encoding="utf-8")
data = json.loads(ACTION.read_text(encoding="utf-8"))
left = [
    a
    for a in data
    if (a.get("name") or "").endswith("(Medium)")
    and (a.get("affinity") or "").strip().lower() in AFFINITIES
]
print(f"Removed {removed}; remaining target Mediums: {len(left)}")
print(
    "Electric Smash:",
    [(a["id"], a["name"]) for a in data if "Electric Smash" in (a.get("name") or "")],
)

# Verify trainingdata still good
tr = json.loads(Path("Data/trainingdata.json").read_text(encoding="utf-8"))
med_tr = [
    t
    for t in tr
    if t.get("type") == "Active"
    and (t.get("name") or "").endswith("(Medium)")
    and (t.get("affinity") or "").strip().lower() in AFFINITIES
]
print(f"Remaining Medium trainings: {len(med_tr)}")
bad_strong = []
for t in tr:
    name = t.get("name") or ""
    if t.get("type") != "Active" or not name.endswith("(Strong)"):
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFFINITIES:
        continue
    pre = t.get("prereq")
    pre_t = next((x for x in tr if x["id"] == pre), None)
    if not pre_t or not (pre_t.get("name") or "").endswith("(Weak)"):
        bad_strong.append((t["id"], name, pre, pre_t.get("name") if pre_t else None))
print(f"Strong with non-Weak prereq: {len(bad_strong)}")
for row in bad_strong[:10]:
    print(" ", row)

# Char 12
c = json.loads(Path("Characters/12.json").read_text(encoding="utf-8"))
tr_ids = {t["id"] for t in tr}
owned_bad = []

def walk(o):
    if isinstance(o, dict):
        if "id" in o and "rank" in o:
            tid = o["id"]
            t = next((x for x in tr if x["id"] == tid), None)
            if t and (t.get("name") or "").endswith("(Medium)") and (t.get("affinity") or "").lower() in AFFINITIES:
                owned_bad.append((tid, t["name"]))
            if tid in remove_set:
                owned_bad.append((tid, f"removed-action-id/{tid}"))
        for v in o.values():
            walk(v)
    elif isinstance(o, list):
        for v in o:
            walk(v)

walk(c.get("training"))
print(f"Char12 bad medium refs: {owned_bad}")
