import json

tr = {t["id"]: t for t in json.load(open("Data/trainingdata.json", encoding="utf-8"))}
acts = {a["id"]: a for a in json.load(open("Data/actiondata.json", encoding="utf-8"))}

print("=== Smash trainings ===")
for tid, t in sorted(tr.items()):
    n = t.get("name") or ""
    if "Smash" not in n:
        continue
    pre = t.get("prereq", -1)
    grant = t.get("grant") or []
    grant_names = [acts.get(g, {}).get("name", f"?{g}") for g in grant]
    print(
        f"{tid} {n} type={t['type']} coef={t.get('levelCoef')} "
        f"prereq={pre}:{tr.get(pre, {}).get('name', '')} grant={grant}->{grant_names}"
    )
