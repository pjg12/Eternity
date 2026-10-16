import json
import subprocess

AFFINITIES = {
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
}

result = subprocess.run(
    ["git", "show", "HEAD:Data/trainingdata.json"],
    cwd=r"C:\EternityTTRPG",
    capture_output=True,
    text=True,
    encoding="utf-8",
)
old = json.loads(result.stdout)
greater_ids = {
    t["id"]
    for t in old
    if t.get("type") == "Maintained"
    and t.get("affinity") in AFFINITIES
    and "(Greater)" in t["name"]
}
lesser_ids = {
    t["id"]
    for t in old
    if t.get("type") == "Maintained"
    and t.get("affinity") in AFFINITIES
    and "(Lesser)" in t["name"]
}

pre = json.load(open(r"C:\EternityTTRPG\tmp\9_pre_migration.json", encoding="utf-8"))
cur = json.load(open(r"C:\EternityTTRPG\Characters\9.json", encoding="utf-8"))

def summarize(data, label):
    owned = []
    for cat, techs in data["training"]["trainingByCategory"].items():
        for t in techs:
            owned.append((cat, t["id"], t.get("rank", 0)))
    g = [(c, i, r) for c, i, r in owned if i in greater_ids]
    l = [(c, i, r) for c, i, r in owned if i in lesser_ids]
    print(f"{label}: total={len(owned)} greater={len(g)} lesser={len(l)}")
    print(f"  name={data['identity']['name']} natural={data['training'].get('naturalAffinities')}")
    if g:
        print("  greater sample:", g[:10])
    # affinity maintained categories
    for cat in sorted({c for c, _, _ in owned}):
        ids = [i for c, i, r in owned if c == cat]
        print(f"  {cat}: {len(ids)} techs")

summarize(pre, "PRE")
summarize(cur, "POST")
