import json
import subprocess

head = json.loads(
    subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    ).stdout
)
cur = json.load(open(r"C:\EternityTTRPG\Data\trainingdata.json", encoding="utf-8"))

def list_aff(data, aff):
    rows = [
        t
        for t in data
        if t.get("affinity") == aff and t.get("type") == "Active"
        and any(tag in t.get("name", "") for tag in ("(Weak)", "(Medium)", "(Strong)"))
    ]
    rows.sort(key=lambda t: t["id"])
    return rows

print("=== Enhancement CUR ===")
for t in list_aff(cur, "Enhancement"):
    print(f"{t['id']:5d} {t['name']:<35} prereq={t['prereq']}")

print("\n=== Enhancement HEAD ===")
for t in list_aff(head, "Enhancement"):
    print(f"{t['id']:5d} {t['name']:<35} prereq={t['prereq']}")

print("\n=== Spirit CUR vs HEAD id/name diffs ===")
cur_s = {(t["name"], t["id"], t["prereq"]) for t in list_aff(cur, "Spirit")}
head_s = {(t["name"], t["id"], t["prereq"]) for t in list_aff(head, "Spirit")}
print("only cur", sorted(cur_s - {(n,i,p) for n,i,p in head_s if True})[:20])
# simpler name->id maps
cur_map = {t["name"]: t for t in list_aff(cur, "Spirit") + list_aff(cur, "Time") + list_aff(cur, "Enhancement")}
head_map = {t["name"]: t for t in list_aff(head, "Spirit") + list_aff(head, "Time") + list_aff(head, "Enhancement")}
mismatched = []
for name in sorted(set(cur_map) | set(head_map)):
    c = cur_map.get(name)
    h = head_map.get(name)
    if c and h and c["id"] != h["id"]:
        mismatched.append((name, h["id"], c["id"]))
    elif c and not h:
        mismatched.append((name, None, c["id"]))
    elif h and not c:
        mismatched.append((name, h["id"], None))
print(f"Name id mismatches / missing: {len(mismatched)}")
for row in mismatched[:40]:
    print(" ", row)
