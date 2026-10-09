import json
import re
from collections import Counter
from pathlib import Path

TRAINING_PATH = Path(r"C:\EternityTTRPG\Data\trainingdata.json")


def jackson_dump(data) -> str:
    raw = json.dumps(data, indent=2, ensure_ascii=False)
    raw = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", raw)
    if isinstance(data, list) and data and isinstance(data[0], dict):
        raw = raw.replace("[\n  {", "[ {", 1)
        raw = raw.replace("\n  },\n  {", "\n}, {")
    return raw + "\n"


def main():
    with TRAINING_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    by_id = {t["id"]: t for t in data}

    medium = [
        t for t in data
        if t.get("type") == "Active" and "(Medium)" in t.get("name", "")
    ]
    strong = [
        t for t in data
        if t.get("type") == "Active" and "(Strong)" in t.get("name", "")
    ]

    # Pair: Strong.prereq should be Medium; Medium.prereq should be Weak
    medium_ids = {t["id"] for t in medium}
    triples = []
    bad_strong = []
    for s in strong:
        mid = s.get("prereq")
        m = by_id.get(mid)
        if m is None or "(Medium)" not in m.get("name", "") or m.get("type") != "Active":
            bad_strong.append(s)
            continue
        wid = m.get("prereq")
        w = by_id.get(wid)
        if w is None or "(Weak)" not in w.get("name", "") or w.get("type") != "Active":
            bad_strong.append(s)
            continue
        triples.append((w, m, s))

    print(f"Triples via prereq chain: {len(triples)}")
    print(f"Bad strong (no clean Medium->Weak chain): {len(bad_strong)}")
    for s in bad_strong:
        print(f"  {s['id']}: {s['name']} prereq={s['prereq']}")

    # Mediums not in any triple
    used_medium = {m["id"] for _, m, _ in triples}
    orphan_medium = [t for t in medium if t["id"] not in used_medium]
    print(f"Orphan medium: {len(orphan_medium)}")
    for t in orphan_medium:
        print(f"  {t['id']}: {t['name']} prereq={t['prereq']}")

    # Retarget Strong -> Weak
    for w, m, s in triples:
        s["prereq"] = w["id"]

    remove_ids = {m["id"] for _, m, _ in triples} | {t["id"] for t in orphan_medium}

    # Any other refs to medium ids?
    other_refs = []
    for t in data:
        if t["id"] in remove_ids:
            continue
        if t.get("prereq") in remove_ids:
            other_refs.append(("prereq", t))
        for g in t.get("grant") or []:
            if g in remove_ids:
                other_refs.append(("grant", t, g))
    print(f"Other refs to medium IDs after retarget: {len(other_refs)}")
    for r in other_refs[:20]:
        print(" ", r[0], r[1]["id"], r[1]["name"], r[1:])

    new_data = [t for t in data if t["id"] not in remove_ids]
    TRAINING_PATH.write_text(jackson_dump(new_data), encoding="utf-8")

    # Verify
    with TRAINING_PATH.open(encoding="utf-8") as f:
        verify = json.load(f)
    v_by_id = {t["id"]: t for t in verify}
    remaining_medium = [
        t for t in verify
        if t.get("type") == "Active" and "(Medium)" in t.get("name", "")
    ]
    strong_ok = 0
    strong_bad = []
    for t in verify:
        if t.get("type") != "Active" or "(Strong)" not in t.get("name", ""):
            continue
        pre = v_by_id.get(t["prereq"])
        if pre and "(Weak)" in pre.get("name", ""):
            strong_ok += 1
        else:
            strong_bad.append(t)

    dangling = [t for t in verify if t.get("prereq") in remove_ids]

    print(f"Wrote {TRAINING_PATH}")
    print(f"Removed medium: {len(remove_ids)}")
    print(f"Entries {len(data)} -> {len(new_data)}")
    print(f"Remaining Medium: {len(remaining_medium)}")
    print(f"Strong with Weak prereq: {strong_ok}")
    print(f"Strong bad: {len(strong_bad)}")
    for t in strong_bad[:10]:
        print(f"  {t['id']}: {t['name']} prereq={t['prereq']}")
    print(f"Dangling prereqs: {len(dangling)}")

    # Sample
    for sid in (1006, 19006, 4003):
        t = v_by_id.get(sid)
        if t:
            pre = v_by_id.get(t["prereq"])
            print(
                f"Sample {t['id']}: {t['name']} prereq={t['prereq']} "
                f"({pre['name'] if pre else 'MISSING'})"
            )


if __name__ == "__main__":
    main()
