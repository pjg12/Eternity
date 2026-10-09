import json
from collections import defaultdict

with open(r"C:\EternityTTRPG\Data\trainingdata.json", encoding="utf-8") as f:
    data = json.load(f)

by_id = {t["id"]: t for t in data}

active = [t for t in data if t.get("type") == "Active"]
weak = [t for t in active if "(Weak)" in t.get("name", "")]
medium = [t for t in active if "(Medium)" in t.get("name", "")]
strong = [t for t in active if "(Strong)" in t.get("name", "")]

print(f"Active total={len(active)} weak={len(weak)} medium={len(medium)} strong={len(strong)}")

# Pair by base name
def base(name, tag):
    return name.replace(f" ({tag})", "").strip()

groups = defaultdict(dict)
for t in weak:
    groups[base(t["name"], "Weak")]["weak"] = t
for t in medium:
    groups[base(t["name"], "Medium")]["medium"] = t
for t in strong:
    groups[base(t["name"], "Strong")]["strong"] = t

complete = incomplete = 0
prereq_patterns = defaultdict(int)
samples = []
for name, g in sorted(groups.items()):
    keys = set(g)
    if keys == {"weak", "medium", "strong"}:
        complete += 1
        w, m, s = g["weak"], g["medium"], g["strong"]
        # expected: medium.prereq=weak, strong.prereq=medium
        pattern = f"m->{m['prereq']} s->{s['prereq']}"
        prereq_patterns[pattern] += 1
        if len(samples) < 5:
            samples.append((name, w["id"], m["id"], s["id"], m["prereq"], s["prereq"], w["affinity"]))
    else:
        incomplete += 1
        if incomplete <= 10:
            print(f"INCOMPLETE {name}: {keys}")

print(f"Complete triples: {complete} incomplete: {incomplete}")
print("Prereq patterns (top):")
for p, c in sorted(prereq_patterns.items(), key=lambda x: -x[1])[:10]:
    print(f"  {c}: {p}")
print("Samples:")
for s in samples:
    print(" ", s)

# Who prereqs medium?
medium_ids = {t["id"] for t in medium}
refs = [t for t in data if t.get("prereq") in medium_ids]
print(f"Entries prereqing Medium: {len(refs)}")
print(Counter := __import__("collections").Counter(t["type"] for t in refs))
# mostly strong?
print("Sample refs:", [(t["id"], t["name"], t["type"], t["prereq"]) for t in refs[:8]])
