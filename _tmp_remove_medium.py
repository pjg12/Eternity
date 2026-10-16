"""Remove Active (Medium) techniques for listed affinities; Strong prereq -> Weak."""
from pathlib import Path
import json
import re

AFFINITIES = {
    "body",
    "nature",
    "metal",
    "earth",
    "water",
    "air",
    "fire",
    "electricity",
    "energy",
    "sound",
    "light",
    "darkness",
    "poison",
    "psionic",
    "force",
}

TRAINING = Path("Data/trainingdata.json")
ACTION = Path("Data/actiondata.json")
CHAR12 = Path("Characters/12.json")

trainings = json.loads(TRAINING.read_text(encoding="utf-8"))
by_id = {t["id"]: t for t in trainings}

# Identify Medium actives in target affinities
medium_ids = set()
medium_info = []
for t in trainings:
    name = t.get("name") or ""
    if t.get("type") != "Active":
        continue
    if not name.endswith("(Medium)"):
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFFINITIES:
        continue
    medium_ids.add(t["id"])
    medium_info.append((t["id"], name, aff, t.get("prereq"), t.get("grant")))

print(f"Medium actives to remove: {len(medium_ids)}")
for row in medium_info:
    print(f"  {row[0]} {row[1]} ({row[2]})")

# Map base name -> weak id for Strong retargeting
def base_name(name: str) -> str:
    for suffix in (" (Weak)", " (Medium)", " (Strong)", "(Weak)", "(Medium)", "(Strong)"):
        if name.endswith(suffix):
            return name[: -len(suffix)].rstrip()
    return name

weak_by_key = {}  # (affinity_lower, base) -> id
for t in trainings:
    name = t.get("name") or ""
    if t.get("type") != "Active" or not name.endswith("(Weak)"):
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFFINITIES:
        continue
    weak_by_key[(aff, base_name(name))] = t["id"]

# Update Strong prereqs that point at Medium (or should point at Weak)
strong_updates = []
for t in trainings:
    name = t.get("name") or ""
    if t.get("type") != "Active" or not name.endswith("(Strong)"):
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFFINITIES:
        continue
    key = (aff, base_name(name))
    weak_id = weak_by_key.get(key)
    if weak_id is None:
        print(f"WARNING: no Weak for Strong {t['id']} {name}")
        continue
    old = t.get("prereq")
    if old != weak_id:
        strong_updates.append((t["id"], name, old, weak_id, by_id.get(old, {}).get("name")))
        t["prereq"] = weak_id

print(f"\nStrong prereq updates: {len(strong_updates)}")
for row in strong_updates[:20]:
    print(f"  {row[0]} {row[1]}: {row[2]} ({row[4]}) -> {row[3]}")
if len(strong_updates) > 20:
    print(f"  ... +{len(strong_updates)-20} more")

# Anything else prereq'ing a Medium id?
other_prereq_refs = []
for t in trainings:
    if t["id"] in medium_ids:
        continue
    if t.get("prereq") in medium_ids:
        other_prereq_refs.append((t["id"], t.get("name"), t.get("prereq")))
print(f"\nOther techniques still prereq'ing Medium: {len(other_prereq_refs)}")
for row in other_prereq_refs:
    print(f"  {row}")

# Remove Medium trainings
new_trainings = [t for t in trainings if t["id"] not in medium_ids]
print(f"\nTraining count {len(trainings)} -> {len(new_trainings)}")

# Write trainingdata preserving style where possible via json dump with indent 2
# Prefer surgical text edits to avoid huge reformat - use json for correctness then check
# Actually trainingdata uses "id" : style - use surgical approach for prereq updates + removals

text = TRAINING.read_text(encoding="utf-8")

# First update Strong prereqs in text
for tid, name, old_pre, new_pre, _ in strong_updates:
    id_match = re.search(rf'"id"\s*:\s*{tid}\b', text)
    if not id_match:
        raise SystemExit(f"Strong id not found {tid}")
    start = id_match.start()
    nxt = re.search(r'\n  \{\s*\n\s*"id"\s*:', text[start + 1 :])
    end = start + 1 + nxt.start() if nxt else start + 2000
    window = text[start:end]
    pm = re.search(rf'"prereq"\s*:\s*{old_pre}\b', window)
    if not pm:
        pm = re.search(r'"prereq"\s*:\s*-?\d+', window)
        if not pm:
            raise SystemExit(f"prereq not found for strong {tid}")
    new_frag = re.sub(r"-?\d+", str(new_pre), pm.group(0), count=1)
    text = text[: start + pm.start()] + new_frag + text[start + pm.end() :]

# Remove Medium objects from training JSON array
# Match from `{` containing the id through the closing `},` or `}`
removed = 0
for mid in sorted(medium_ids, reverse=True):
    # Find object start: look for `"id" : mid` then walk back to `{`
    id_pat = re.compile(rf'"id"\s*:\s*{mid}\b')
    m = id_pat.search(text)
    if not m:
        print(f"WARNING: medium id {mid} not found in text for removal")
        continue
    # walk back to nearest `\n{` or `\n  {`
    brace = text.rfind("{", 0, m.start())
    if brace < 0:
        raise SystemExit(f"no brace for {mid}")
    # find matching close - track depth
    depth = 0
    i = brace
    while i < len(text):
        ch = text[i]
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                # include trailing comma and whitespace/newline
                j = end
                while j < len(text) and text[j] in " \t":
                    j += 1
                if j < len(text) and text[j] == ",":
                    j += 1
                while j < len(text) and text[j] in " \t":
                    j += 1
                if j < len(text) and text[j] == "\n":
                    j += 1
                # also handle `}, {` vs `},{` patterns - if previous comma left dangling
                text = text[:brace] + text[j:]
                # clean double commas / comma before ]
                removed += 1
                break
        i += 1
    else:
        raise SystemExit(f"unclosed object {mid}")

# Clean up `,]` or `[ ,` artifacts
text = re.sub(r",\s*\]", "\n]", text)
text = re.sub(r",\s*,", ",", text)

TRAINING.write_text(text, encoding="utf-8")
print(f"Removed {removed} Medium objects from trainingdata.json")

# Validate JSON
data2 = json.loads(TRAINING.read_text(encoding="utf-8"))
by2 = {t["id"]: t for t in data2}
assert not any(i in by2 for i in medium_ids)
# Strong prereqs
for t in data2:
    name = t.get("name") or ""
    if t.get("type") != "Active" or not name.endswith("(Strong)"):
        continue
    aff = (t.get("affinity") or "").strip().lower()
    if aff not in AFFINITIES:
        continue
    weak_id = weak_by_key[(aff, base_name(name))]
    assert t["prereq"] == weak_id, (t["id"], t["name"], t["prereq"], weak_id)
    assert weak_id in by2
print("trainingdata.json validated")

# Remove Medium from actiondata if those action IDs match medium training IDs
# (grants often use same id)
actions = json.loads(ACTION.read_text(encoding="utf-8"))
# Collect action ids granted only by medium trainings we removed
medium_grants = set()
for _id, name, aff, pre, grant in medium_info:
    for g in grant or []:
        medium_grants.add(g)

# Also remove actions named (Medium) for those affinities
action_remove = set()
for a in actions:
    name = a.get("name") or ""
    if not name.endswith("(Medium)"):
        continue
    aff = (a.get("affinity") or "").strip().lower()
    # action affinity field may differ; also match by id in medium_grants or medium_ids
    if a["id"] in medium_ids or a["id"] in medium_grants or aff in AFFINITIES:
        # only if it looks like aura technique medium
        if aff in AFFINITIES or a["id"] in medium_ids or a["id"] in medium_grants:
            action_remove.add(a["id"])

# Safer: remove actions whose id is in medium_ids or medium_grants, or name ends with (Medium) and affinity matches
action_remove = set()
for a in actions:
    name = a.get("name") or ""
    aff = (a.get("affinity") or "").strip().lower()
    if a["id"] in medium_ids or a["id"] in medium_grants:
        action_remove.add(a["id"])
    elif name.endswith("(Medium)") and aff in AFFINITIES:
        action_remove.add(a["id"])

print(f"\nActions to remove: {len(action_remove)}")
for aid in sorted(action_remove):
    a = next(x for x in actions if x["id"] == aid)
    print(f"  {aid} {a.get('name')} aff={a.get('affinity')}")

atext = ACTION.read_text(encoding="utf-8")
removed_a = 0
for aid in sorted(action_remove, reverse=True):
    m = re.search(rf'"id"\s*:\s*{aid}\b', atext)
    if not m:
        print(f"WARNING: action {aid} not in text")
        continue
    brace = atext.rfind("{", 0, m.start())
    depth = 0
    i = brace
    while i < len(atext):
        if atext[i] == "{":
            depth += 1
        elif atext[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                j = end
                while j < len(atext) and atext[j] in " \t":
                    j += 1
                if j < len(atext) and atext[j] == ",":
                    j += 1
                while j < len(atext) and atext[j] in " \t":
                    j += 1
                if j < len(atext) and atext[j] == "\n":
                    j += 1
                atext = atext[:brace] + atext[j:]
                removed_a += 1
                break
        i += 1
atext = re.sub(r",\s*\]", "\n]", atext)
ACTION.write_text(atext, encoding="utf-8")
json.loads(ACTION.read_text(encoding="utf-8"))
print(f"Removed {removed_a} actions; actiondata validated")

# Character 12: remove medium ids from trainingByCategory
ctext = CHAR12.read_text(encoding="utf-8")
cremoved = 0
for mid in sorted(medium_ids | action_remove):
    # remove blocks like `{ "id" : 25011, "rank" : 5 },`
    pattern = re.compile(
        rf',\s*\{{\s*"id"\s*:\s*{mid}\s*,\s*"rank"\s*:\s*\d+\s*\}}'
        rf'|\s*\{{\s*"id"\s*:\s*{mid}\s*,\s*"rank"\s*:\s*\d+\s*\}}\s*,?'
    )
    new_ctext, n = pattern.subn("", ctext)
    if n:
        cremoved += n
        ctext = new_ctext
        print(f"  removed id {mid} from 12.json ({n})")

# cleanup possible double commas in arrays
ctext = re.sub(r",\s*,", ",", ctext)
CHAR12.write_text(ctext, encoding="utf-8")
json.loads(CHAR12.read_text(encoding="utf-8"))
print(f"Character 12 removals: {cremoved}; validated")

# Final summary: any remaining Medium actives in those affinities?
left = [
    t
    for t in json.loads(TRAINING.read_text(encoding="utf-8"))
    if t.get("type") == "Active"
    and (t.get("name") or "").endswith("(Medium)")
    and (t.get("affinity") or "").strip().lower() in AFFINITIES
]
print(f"Remaining Medium actives in target affinities: {len(left)}")
