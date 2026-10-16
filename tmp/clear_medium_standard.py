#!/usr/bin/env python3
"""Clear removed Medium + Standard techniques from local characters and report XP refunds."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
from pathlib import Path

ROOT = Path(r"C:\EternityTTRPG")
LOCAL = Path(os.environ["LOCALAPPDATA"]) / "Eternity" / "Characters"
CHAR_IDS = [1, 5, 6, 7]


def load_json(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def dump_json(path: Path, data) -> None:
    text = json.dumps(data, indent=2, ensure_ascii=False)
    text = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", text)
    path.write_text(text + "\n", encoding="utf-8")


def load_pre_medium_training() -> dict[int, dict]:
    result = subprocess.run(
        ["git", "show", "0ad2820^:Data/trainingdata.json"],
        cwd=ROOT,
        capture_output=True,
    )
    if result.returncode != 0:
        raise SystemExit(result.stderr.decode("utf-8", errors="replace"))
    return {t["id"]: t for t in json.loads(result.stdout.decode("utf-8"))}


def xp_cost_at_rank(rank: int, affinity: str, natural: set[str], domains: set[str]) -> int:
    value = rank * 4 + 10
    if affinity.lower() in ("spirit", "time"):
        value = int(value * 1.5)
    aff_l = affinity.lower()
    if aff_l in natural:
        value //= 2
    elif aff_l in domains:
        value = int(round(value * 0.75))
    return value


def total_xp_for_rank(rank: int, affinity: str, natural: set[str], domains: set[str]) -> int:
    return sum(xp_cost_at_rank(r, affinity, natural, domains) for r in range(max(0, rank)))


def scrub_tree(obj, remove_ids: set[int], report: list) -> None:
    if isinstance(obj, dict):
        for k, v in list(obj.items()):
            if isinstance(v, list):
                if v and isinstance(v[0], dict) and "durationType" in v[0] and "attribute" in v[0]:
                    kept = []
                    for st in v:
                        name = st.get("name") or ""
                        m = re.search(r"\.al\.(\d+)\.", name)
                        if m and int(m.group(1)) in remove_ids:
                            report.append(f"DROP status {name}")
                            continue
                        kept.append(st)
                    obj[k] = kept
                else:
                    scrub_tree(v, remove_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, remove_ids, report)
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            if isinstance(v, list):
                if v and isinstance(v[0], dict) and "durationType" in v[0] and "attribute" in v[0]:
                    kept = []
                    for st in v:
                        name = st.get("name") or ""
                        m = re.search(r"\.al\.(\d+)\.", name)
                        if m and int(m.group(1)) in remove_ids:
                            report.append(f"DROP status {name}")
                            continue
                        kept.append(st)
                    obj[i] = kept
                else:
                    scrub_tree(v, remove_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, remove_ids, report)


def scrub_reminder_keys(reminders: dict, remove_ids: set[int], report: list) -> dict:
    pattern = re.compile(r"^(.*\.al\.)(\d+)(\..*)$")
    out = {}
    for key, value in reminders.items():
        m = pattern.match(key)
        if m and int(m.group(2)) in remove_ids:
            report.append(f"DROP reminder {key}")
            continue
        out[key] = value
    return out


def main() -> None:
    current = {t["id"]: t for t in load_json(ROOT / "Data" / "trainingdata.json")}
    historic = load_pre_medium_training()

    removed_medium_ids = {
        tid
        for tid, t in historic.items()
        if "(Medium)" in (t.get("name") or "") and tid not in current
    }
    standard_ids = {
        tid for tid, t in current.items() if "(Standard)" in (t.get("name") or "")
    }

    print(f"Removed Medium IDs: {len(removed_medium_ids)}")
    print(f"Current Standard IDs: {len(standard_ids)}")

    grand_total = 0
    all_rows = []

    for char_id in CHAR_IDS:
        path = LOCAL / f"{char_id}.json"
        if not path.exists():
            print(f"MISSING {path}")
            continue

        backup = LOCAL / "Backup" / f"{char_id}Backup_pre_medium_standard.json"
        backup.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, backup)

        data = load_json(path)
        name = data["identity"]["name"]
        natural = {a.lower() for a in (data["training"].get("naturalAffinities") or [])}
        # Domains can grant derived affinity discounts at runtime for some classes;
        # use persisted domains as a conservative stand-in when derivedAffinities is empty.
        domains = {a.lower() for a in (data["training"].get("derivedAffinities") or [])}
        if not domains:
            domains = {a.lower() for a in (data["training"].get("domains") or [])}

        remove_ids: set[int] = set()
        refunds = []
        by_cat = data["training"]["trainingByCategory"]

        for cat, techs in list(by_cat.items()):
            if not isinstance(techs, list):
                continue
            kept = []
            for tech in techs:
                if not isinstance(tech, dict):
                    kept.append(tech)
                    continue
                tid = tech.get("id")
                rank = int(tech.get("rank") or 0)
                exp = float(tech.get("exp") or 0.0)

                if tid in removed_medium_ids:
                    meta = historic[tid]
                    affinity = meta.get("affinity") or ""
                    xp = total_xp_for_rank(rank, affinity, natural, domains) + int(exp)
                    refunds.append(
                        {
                            "char": char_id,
                            "name": name,
                            "id": tid,
                            "tech": meta.get("name"),
                            "affinity": affinity,
                            "tier": "Medium",
                            "rank": rank,
                            "xp": xp,
                        }
                    )
                    remove_ids.add(tid)
                    continue

                if tid in standard_ids:
                    meta = current[tid]
                    affinity = meta.get("affinity") or ""
                    xp = total_xp_for_rank(rank, affinity, natural, domains) + int(exp)
                    refunds.append(
                        {
                            "char": char_id,
                            "name": name,
                            "id": tid,
                            "tech": meta.get("name"),
                            "affinity": affinity,
                            "tier": "Standard",
                            "rank": rank,
                            "xp": xp,
                        }
                    )
                    remove_ids.add(tid)
                    continue

                kept.append(tech)
            by_cat[cat] = kept

        scrub_report = []
        if remove_ids:
            scrub_tree(data, remove_ids, scrub_report)
            if isinstance(data.get("reminderSelections"), dict):
                data["reminderSelections"] = scrub_reminder_keys(
                    data["reminderSelections"], remove_ids, scrub_report
                )
            dump_json(path, data)

        char_xp = sum(r["xp"] for r in refunds)
        grand_total += char_xp
        all_rows.extend(refunds)

        print()
        print(f"=== Character {char_id}: {name} ===")
        print(f"Natural: {sorted(natural) or '(none)'}  Domains/derived: {sorted(domains) or '(none)'}")
        print(f"Removed techniques: {len(refunds)}")
        print(f"Refund XP: {char_xp}")
        print(f"Backup: {backup}")
        for r in refunds:
            print(
                f"  [{r['tier']}] {r['id']} {r['tech']} rank={r['rank']} xp={r['xp']} ({r['affinity']})"
            )
        if scrub_report:
            print(f"Scrubbed refs: {len(scrub_report)}")

    print()
    print("=" * 60)
    print(f"TOTAL training XP to refund across characters 1,5,6,7: {grand_total}")
    print(f"TOTAL techniques removed: {len(all_rows)}")


if __name__ == "__main__":
    main()
