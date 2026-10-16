import json
from collections import defaultdict
from pathlib import Path

td = json.loads(Path(r"C:\EternityTTRPG\Data\trainingdata.json").read_text(encoding="utf-8"))
by_grant = defaultdict(list)
for t in td:
    for g in t.get("grant") or []:
        if g in (501, 502, 503):
            by_grant[g].append(f"{t['id']}: {t['name']} [{t.get('type')}] {t.get('description')}")

for g in (501, 502, 503):
    print(f"=== grant {g} ({len(by_grant[g])}) ===")
    for line in by_grant[g]:
        print(line)

# also check actiondata for 501-503
ad = json.loads(Path(r"C:\EternityTTRPG\Data\actiondata.json").read_text(encoding="utf-8"))
for a in ad:
    if a.get("id") in (501, 502, 503):
        print("action", a.get("id"), a.get("name"), a.get("modifierKey"))
