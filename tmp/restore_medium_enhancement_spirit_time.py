import json
import re
import subprocess
from pathlib import Path

TRAINING_PATH = Path(r"C:\EternityTTRPG\Data\trainingdata.json")
RESTORE_AFFINITIES = {"Enhancement", "Spirit", "Time"}


def jackson_dump(data) -> str:
    raw = json.dumps(data, indent=2, ensure_ascii=False)
    raw = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", raw)
    if isinstance(data, list) and data and isinstance(data[0], dict):
        raw = raw.replace("[\n  {", "[ {", 1)
        raw = raw.replace("\n  },\n  {", "\n}, {")
    return raw + "\n"


def load_head():
    result = subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    if result.returncode != 0:
        raise SystemExit(result.stderr)
    return json.loads(result.stdout)


def main():
    head = load_head()
    with TRAINING_PATH.open(encoding="utf-8") as f:
        current = json.load(f)

    current_by_id = {t["id"]: t for t in current}
    head_by_id = {t["id"]: t for t in head}

    # Medium actives to restore from HEAD for target affinities
    mediums = [
        t
        for t in head
        if t.get("type") == "Active"
        and t.get("affinity") in RESTORE_AFFINITIES
        and "(Medium)" in t.get("name", "")
    ]

    # Strong actives in HEAD for those affinities (to restore prereq)
    strongs_head = [
        t
        for t in head
        if t.get("type") == "Active"
        and t.get("affinity") in RESTORE_AFFINITIES
        and "(Strong)" in t.get("name", "")
    ]

    print(f"Mediums to restore: {len(mediums)}")
    print(f"Strongs to retarget: {len(strongs_head)}")
    for aff in sorted(RESTORE_AFFINITIES):
        m = sum(1 for t in mediums if t["affinity"] == aff)
        s = sum(1 for t in strongs_head if t["affinity"] == aff)
        print(f"  {aff}: medium={m} strong={s}")

    restored_ids = []
    for m in mediums:
        if m["id"] in current_by_id:
            print(f"  already present medium {m['id']} {m['name']}")
            continue
        # Deep copy via json to avoid shared refs
        copy = json.loads(json.dumps(m))
        current.append(copy)
        current_by_id[copy["id"]] = copy
        restored_ids.append(copy["id"])

    # Restore Strong prereqs to Medium (from HEAD)
    retargeted = 0
    missing_strong = []
    for s_head in strongs_head:
        s_cur = current_by_id.get(s_head["id"])
        if s_cur is None:
            missing_strong.append(s_head)
            continue
        # HEAD strong.prereq should be the Medium id
        medium_id = s_head["prereq"]
        medium = head_by_id.get(medium_id)
        if medium is None or "(Medium)" not in medium.get("name", ""):
            print(
                f"WARN strong {s_head['id']} {s_head['name']} "
                f"HEAD prereq {medium_id} is not Medium"
            )
            continue
        if s_cur.get("prereq") != medium_id:
            s_cur["prereq"] = medium_id
            retargeted += 1

    # Keep stable-ish order: sort by id like original data tends to be
    current.sort(key=lambda t: t["id"])

    TRAINING_PATH.write_text(jackson_dump(current), encoding="utf-8")

    # Verify
    with TRAINING_PATH.open(encoding="utf-8") as f:
        verify = json.load(f)
    v_by_id = {t["id"]: t for t in verify}

    ok = bad = 0
    for aff in RESTORE_AFFINITIES:
        mediums_v = [
            t
            for t in verify
            if t.get("type") == "Active"
            and t.get("affinity") == aff
            and "(Medium)" in t.get("name", "")
        ]
        strongs_v = [
            t
            for t in verify
            if t.get("type") == "Active"
            and t.get("affinity") == aff
            and "(Strong)" in t.get("name", "")
        ]
        print(f"\n{aff} after restore: medium={len(mediums_v)} strong={len(strongs_v)}")
        for s in strongs_v:
            pre = v_by_id.get(s["prereq"])
            if pre and "(Medium)" in pre.get("name", ""):
                ok += 1
            else:
                bad += 1
                print(f"  BAD {s['id']} {s['name']} prereq={s['prereq']} ({pre})")

    # Confirm other affinities still have no Medium
    other_medium = [
        t
        for t in verify
        if t.get("type") == "Active"
        and "(Medium)" in t.get("name", "")
        and t.get("affinity") not in RESTORE_AFFINITIES
    ]
    print(f"\nRestored medium IDs: {len(restored_ids)}")
    print(f"Retargeted strong prereqs: {retargeted}")
    print(f"Strong->Medium OK: {ok} bad: {bad}")
    print(f"Missing strong in current: {len(missing_strong)}")
    print(f"Medium still removed for other affinities: {len(other_medium)} remaining (should be 0)")
    if other_medium[:5]:
        for t in other_medium[:5]:
            print(" ", t["id"], t["name"], t["affinity"])

    # Samples
    for sid in (1006, 1005, 56003 if False else None):
        pass
    for name_part in ("Agile Shot", "Spirit", "Time"):
        hits = [
            t
            for t in verify
            if name_part in t.get("name", "")
            and t.get("type") == "Active"
            and t.get("affinity") in RESTORE_AFFINITIES
        ][:6]
        if hits:
            print(f"\nSamples containing '{name_part}':")
            for t in hits:
                pre = v_by_id.get(t["prereq"])
                print(
                    f"  {t['id']}: {t['name']} | {t['affinity']} | "
                    f"prereq={t['prereq']} ({pre['name'] if pre else 'MISSING'})"
                )


if __name__ == "__main__":
    main()
