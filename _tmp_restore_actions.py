"""Restore non-Medium actions wrongly deleted; keep current file formatting where possible."""
import json
import subprocess
from pathlib import Path

ACTION = Path("Data/actiondata.json")
AFFINITIES = {
    "body", "nature", "metal", "earth", "water", "air", "fire", "electricity",
    "energy", "sound", "light", "darkness", "poison", "psionic", "force",
}

current_list = json.loads(ACTION.read_text(encoding="utf-8"))
current = {a["id"]: a for a in current_list}
head_list = json.loads(
    subprocess.check_output(["git", "show", "HEAD:Data/actiondata.json"], cwd=r"C:\EternityTTRPG").decode("utf-8")
)
head = {a["id"]: a for a in head_list}

should_delete_medium = {
    aid
    for aid, a in head.items()
    if (a.get("name") or "").endswith("(Medium)")
    and (a.get("affinity") or "").strip().lower() in AFFINITIES
}

# Rebuild as: all HEAD actions except target Mediums, but prefer CURRENT version when present
# so we keep session fixes on overlapping IDs.
restored = []
seen = set()
for a in head_list:
    aid = a["id"]
    if aid in should_delete_medium:
        continue
    if aid in current:
        restored.append(current[aid])
    else:
        restored.append(a)  # restore wrongly deleted
    seen.add(aid)

# Keep any current-only actions that aren't deleted mediums
for a in current_list:
    if a["id"] not in seen and a["id"] not in should_delete_medium:
        restored.append(a)

wrongly_restored = [a for a in restored if a["id"] not in current and a["id"] not in should_delete_medium]
print(f"Restoring {len(wrongly_restored)} actions:")
for a in wrongly_restored:
    print(f"  {a['id']} {a.get('name')}")

# Write with indent 2 - actiondata may already be mixed; validate content first
leftover_medium = [
    a
    for a in restored
    if (a.get("name") or "").endswith("(Medium)")
    and (a.get("affinity") or "").strip().lower() in AFFINITIES
]
print(f"Leftover target Mediums in restored set: {len(leftover_medium)}")

# Use git checkout of actiondata then re-delete only Mediums surgically from text
# to preserve formatting and prior session edits on training... 
# Simpler: write JSON pretty for actiondata (large but correct)
ACTION.write_text(json.dumps(restored, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

cur2 = {a["id"]: a for a in json.loads(ACTION.read_text(encoding="utf-8"))}
print(f"action count: {len(cur2)}")
print(
    "remaining target Mediums:",
    sum(
        1
        for a in cur2.values()
        if (a.get("name") or "").endswith("(Medium)")
        and (a.get("affinity") or "").strip().lower() in AFFINITIES
    ),
)

# Check training grants pointing at missing actions
tr = json.loads(Path("Data/trainingdata.json").read_text(encoding="utf-8"))
missing_grants = []
for t in tr:
    for g in t.get("grant") or []:
        if g not in cur2 and g > 0:
            missing_grants.append((t["id"], t.get("name"), g))
print(f"Training grants -> missing actions: {len(missing_grants)}")
for row in missing_grants[:30]:
    print(" ", row)

# Char 12 medium leftovers
c = json.loads(Path("Characters/12.json").read_text(encoding="utf-8"))
owned_medium = []

def walk(o):
    if isinstance(o, dict):
        if "id" in o and o["id"] in should_delete_medium:
            owned_medium.append(o["id"])
        for v in o.values():
            walk(v)
    elif isinstance(o, list):
        for v in o:
            walk(v)

walk(c.get("training"))
print(f"Char12 still has deleted medium ids: {owned_medium}")
