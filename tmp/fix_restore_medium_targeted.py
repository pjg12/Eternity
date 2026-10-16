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


def base_name(name: str) -> str:
    for tag in (" (Weak)", " (Medium)", " (Strong)"):
        if name.endswith(tag):
            return name[: -len(tag)]
    return name


def load_head():
    return json.loads(
        subprocess.run(
            ["git", "show", "HEAD:Data/trainingdata.json"],
            cwd=r"C:\EternityTTRPG",
            capture_output=True,
            text=True,
            encoding="utf-8",
        ).stdout
    )


def main():
    head = load_head()
    with TRAINING_PATH.open(encoding="utf-8") as f:
        data = json.load(f)

    # Template Mediums from HEAD keyed by (affinity, base name)
    head_medium_templates = {}
    for t in head:
        if (
            t.get("type") == "Active"
            and t.get("affinity") in RESTORE_AFFINITIES
            and "(Medium)" in t.get("name", "")
        ):
            head_medium_templates[(t["affinity"], base_name(t["name"]))] = t

    # Remove ALL current Mediums for restore affinities (clean slate)
    removed = [
        t
        for t in data
        if t.get("type") == "Active"
        and t.get("affinity") in RESTORE_AFFINITIES
        and "(Medium)" in t.get("name", "")
    ]
    data = [
        t
        for t in data
        if not (
            t.get("type") == "Active"
            and t.get("affinity") in RESTORE_AFFINITIES
            and "(Medium)" in t.get("name", "")
        )
    ]
    print(f"Cleared existing Mediums in target affinities: {len(removed)}")

    by_id = {t["id"]: t for t in data}
    used_ids = set(by_id)

    # Group Weak/Strong by affinity + base name
    groups = {}
    for t in data:
        if t.get("type") != "Active" or t.get("affinity") not in RESTORE_AFFINITIES:
            continue
        name = t.get("name", "")
        if "(Weak)" in name:
            groups.setdefault((t["affinity"], base_name(name)), {})["weak"] = t
        elif "(Strong)" in name:
            groups.setdefault((t["affinity"], base_name(name)), {})["strong"] = t

    created = []
    retargeted = []
    skipped = []
    for (aff, base), g in sorted(groups.items()):
        weak = g.get("weak")
        strong = g.get("strong")
        if not weak or not strong:
            skipped.append((aff, base, sorted(g)))
            continue

        # Prefer medium id = weak_id + 1 if free, else strong_id - 1 if free,
        # else any free id near the pair.
        candidates = [weak["id"] + 1, strong["id"] - 1, weak["id"] + 2]
        # Also prefer HEAD medium id if free and matches base
        template = head_medium_templates.get((aff, base))
        if template is not None:
            candidates.insert(0, template["id"])

        medium_id = None
        for cid in candidates:
            if cid > 0 and cid not in used_ids and cid != weak["id"] and cid != strong["id"]:
                medium_id = cid
                break
        if medium_id is None:
            # scan upward from weak_id+1
            cid = weak["id"] + 1
            while cid in used_ids or cid == strong["id"]:
                cid += 1
            medium_id = cid

        # Build medium entry from template or clone weak with adjusted fields
        if template is not None:
            medium = json.loads(json.dumps(template))
            medium["id"] = medium_id
            medium["name"] = f"{base} (Medium)"
            medium["affinity"] = aff
            medium["prereq"] = weak["id"]
            # Keep grant if template grant was its own id pattern; else use medium_id
            grant = medium.get("grant") or []
            if grant == [template["id"]]:
                medium["grant"] = [medium_id]
            elif len(grant) == 1 and isinstance(grant[0], int):
                # If grant pointed at old medium self-id, update; else keep
                if grant[0] == template["id"]:
                    medium["grant"] = [medium_id]
        else:
            medium = json.loads(json.dumps(weak))
            medium["id"] = medium_id
            medium["name"] = f"{base} (Medium)"
            medium["prereq"] = weak["id"]
            # Typical active pattern: grant self id; levelCoef often 0.75 for medium
            medium["levelCoef"] = 0.75
            medium["grant"] = [medium_id]
            print(f"  WARN no HEAD template for {aff} {base}; cloned Weak with levelCoef=0.75")

        data.append(medium)
        used_ids.add(medium_id)
        by_id[medium_id] = medium
        created.append(medium)

        if strong.get("prereq") != medium_id:
            strong["prereq"] = medium_id
            retargeted.append(strong)

    data.sort(key=lambda t: t["id"])
    TRAINING_PATH.write_text(jackson_dump(data), encoding="utf-8")

    # Verify
    with TRAINING_PATH.open(encoding="utf-8") as f:
        verify = json.load(f)
    v_by_id = {t["id"]: t for t in verify}

    print(f"Created Mediums: {len(created)}")
    print(f"Retargeted Strongs: {len(retargeted)}")
    print(f"Skipped incomplete groups: {len(skipped)}")
    for s in skipped:
        print(" ", s)

    for aff in sorted(RESTORE_AFFINITIES):
        mediums = [
            t
            for t in verify
            if t.get("type") == "Active"
            and t.get("affinity") == aff
            and "(Medium)" in t.get("name", "")
        ]
        strongs = [
            t
            for t in verify
            if t.get("type") == "Active"
            and t.get("affinity") == aff
            and "(Strong)" in t.get("name", "")
        ]
        ok = bad = 0
        print(f"\n{aff}: medium={len(mediums)} strong={len(strongs)}")
        for s in strongs:
            pre = v_by_id.get(s["prereq"])
            if pre and "(Medium)" in pre.get("name", "") and base_name(pre["name"]) == base_name(s["name"]):
                mid = pre
                w = v_by_id.get(mid["prereq"])
                if w and "(Weak)" in w.get("name", "") and base_name(w["name"]) == base_name(s["name"]):
                    ok += 1
                else:
                    bad += 1
                    print(f"  BAD chain {s['id']} {s['name']} medium->{mid['prereq']}")
            else:
                bad += 1
                print(f"  BAD strong {s['id']} {s['name']} prereq={s['prereq']}")
        print(f"  OK chains={ok} bad={bad}")

    # Other affinities must still have no Medium
    other_medium = [
        t
        for t in verify
        if t.get("type") == "Active"
        and "(Medium)" in t.get("name", "")
        and t.get("affinity") not in RESTORE_AFFINITIES
    ]
    print(f"\nMedium in other affinities: {len(other_medium)} (expect 0)")

    # Print Enhancement end of list for sanity
    print("\nEnhancement Weak/Medium/Strong:")
    for t in verify:
        if t.get("affinity") == "Enhancement" and t.get("type") == "Active":
            if any(x in t["name"] for x in ("Weak", "Medium", "Strong")):
                pre = v_by_id.get(t["prereq"])
                print(
                    f"  {t['id']:5d} {t['name']:<32} -> {t['prereq']} "
                    f"({pre['name'] if pre else '?'})"
                )


if __name__ == "__main__":
    main()
