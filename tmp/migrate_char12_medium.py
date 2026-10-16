import json
import re
import shutil
import subprocess
from pathlib import Path

CHAR_PATH = Path(r"C:\EternityTTRPG\Characters\12.json")
SNAPSHOT_PATH = Path(r"C:\EternityTTRPG\tmp\12_pre_medium_migration.json")


def load_head_training():
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


def build_medium_map(old_data):
    """Medium Active id -> Weak Active id, plus metadata."""
    by_id = {t["id"]: t for t in old_data}
    medium_to_weak = {}
    medium_meta = {}
    for t in old_data:
        if t.get("type") != "Active" or "(Medium)" not in t.get("name", ""):
            continue
        weak = by_id.get(t.get("prereq"))
        if weak is None or "(Weak)" not in weak.get("name", ""):
            print(f"WARN bad medium chain: {t['id']} {t['name']} prereq={t['prereq']}")
            continue
        medium_to_weak[t["id"]] = weak["id"]
        medium_meta[t["id"]] = t
    return medium_to_weak, medium_meta


def jackson_dump(data) -> str:
    raw = json.dumps(data, indent=2, ensure_ascii=False)
    raw = re.sub(r'("(?:\\.|[^"\\])*")\s*:\s*', r"\1 : ", raw)
    return raw + "\n"


def scrub_labels(text: str) -> str:
    # Only scrub Medium labels here; leave Weak/Strong tags
    return re.sub(r"\s*\([Mm]edium\)", "", text)


def is_status_dict(obj) -> bool:
    return isinstance(obj, dict) and "durationType" in obj and "attribute" in obj


def filter_statuses(statuses, medium_ids: set[int], report: list):
    kept = []
    seen = set()
    for st in statuses:
        if not is_status_dict(st):
            kept.append(st)
            continue
        name = st.get("name") or ""
        # Active techniques rarely appear as maintained.al — still check any .al.<id>.
        m = re.search(r"\.al\.(\d+)\.", name)
        if m and int(m.group(1)) in medium_ids:
            report.append(f"DROP status {name}")
            continue
        new_name = scrub_labels(name)
        new_desc = scrub_labels(st.get("description") or "")
        if new_name != name or new_desc != (st.get("description") or ""):
            st = dict(st)
            st["name"] = new_name
            st["description"] = new_desc
            report.append(f"RELABEL {name}")
        key = st.get("name")
        if key and key.startswith("Panel Maintained:") and key in seen:
            report.append(f"DROP duplicate {key}")
            continue
        if key:
            seen.add(key)
        kept.append(st)
    return kept


def scrub_tree(obj, medium_ids, report):
    if isinstance(obj, dict):
        for k, v in list(obj.items()):
            if isinstance(v, list):
                if v and is_status_dict(v[0]):
                    obj[k] = filter_statuses(v, medium_ids, report)
                else:
                    scrub_tree(v, medium_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, medium_ids, report)
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            if isinstance(v, list):
                if v and is_status_dict(v[0]):
                    obj[i] = filter_statuses(v, medium_ids, report)
                else:
                    scrub_tree(v, medium_ids, report)
            elif isinstance(v, dict):
                scrub_tree(v, medium_ids, report)


def migrate_reminder_keys(reminders: dict, medium_to_weak: dict[int, int], report: list) -> dict:
    medium_ids = set(medium_to_weak)
    new_reminders = {}
    pending = []
    pattern = re.compile(r"^(.*\.al\.)(\d+)(\..*)$")

    for key, value in reminders.items():
        m = pattern.match(key)
        if m and int(m.group(2)) in medium_ids:
            wid = medium_to_weak[int(m.group(2))]
            new_key = f"{m.group(1)}{wid}{m.group(3)}"
            pending.append((new_key, value, key))
            continue
        new_reminders[key] = value

    for new_key, value, old_key in pending:
        if new_key in new_reminders:
            if str(new_reminders[new_key]).isdigit() and str(value).isdigit():
                chosen = str(max(int(new_reminders[new_key]), int(value)))
            else:
                chosen = new_reminders[new_key]
            report.append(f"LOADOUT merge {old_key}={value} into {new_key} -> {chosen}")
            new_reminders[new_key] = chosen
        else:
            report.append(f"LOADOUT move {old_key} -> {new_key}")
            new_reminders[new_key] = value
    return new_reminders


def migrate_training(by_cat: dict, medium_to_weak: dict[int, int], report: list):
    for cat, techs in list(by_cat.items()):
        if not isinstance(techs, list):
            continue
        owned_ids = {t.get("id") for t in techs if isinstance(t, dict)}
        new_list = []
        for tech in techs:
            if not isinstance(tech, dict):
                new_list.append(tech)
                continue
            tid = tech.get("id")
            if tid in medium_to_weak:
                wid = medium_to_weak[tid]
                if wid in owned_ids:
                    report.append(
                        f"[{cat}] DROP Medium {tid} rank={tech.get('rank')} (have Weak {wid})"
                    )
                    continue
                replacement = dict(tech)
                replacement["id"] = wid
                report.append(
                    f"[{cat}] REMAP Medium {tid}->Weak {wid} rank={tech.get('rank')}"
                )
                new_list.append(replacement)
                owned_ids.add(wid)
                continue
            new_list.append(tech)
        by_cat[cat] = new_list


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
    return sum(xp_cost_at_rank(r, affinity, natural, domains) for r in range(rank))


def calc_lost_xp(pre_data, post_data, medium_to_weak, medium_meta):
    natural = {a.lower() for a in (post_data["training"].get("naturalAffinities") or [])}
    domains = {a.lower() for a in (post_data["training"].get("derivedAffinities") or [])}

    def owned(data):
        out = {}
        for cat, techs in data["training"]["trainingByCategory"].items():
            for t in techs:
                out[t["id"]] = (cat, t.get("rank", 0))
        return out

    old_owned = owned(pre_data)
    new_owned = owned(post_data)

    lost = []
    remapped = []
    for mid, (cat, rank) in old_owned.items():
        if mid not in medium_to_weak:
            continue
        wid = medium_to_weak[mid]
        meta = medium_meta.get(mid)
        if meta is None:
            continue
        if mid not in new_owned:
            if wid in new_owned and wid in {i for i, _ in [(x[0], x[1]) for x in []]}:
                pass
            # Dropped (weak already existed) => lost XP is medium ranks
            # Remapped (weak didn't exist) => ranks preserved on weak, not lost
            if wid in owned(pre_data):
                # weak was already owned before migration -> DROP -> lost
                xp = total_xp_for_rank(rank, meta["affinity"], natural, domains)
                lost.append(
                    {
                        "id": mid,
                        "name": meta["name"],
                        "affinity": meta["affinity"],
                        "rank": rank,
                        "xp": xp,
                    }
                )
            else:
                # remapped medium->weak, ranks kept
                remapped.append(
                    {
                        "id": mid,
                        "weak": wid,
                        "name": meta["name"],
                        "rank": rank,
                    }
                )
    lost.sort(key=lambda x: (x["affinity"], x["id"]))
    return lost, remapped, natural, domains


def main():
    shutil.copy2(CHAR_PATH, SNAPSHOT_PATH)
    print(f"Snapshot saved: {SNAPSHOT_PATH}")

    # Prefer current working-tree pre-edit via HEAD (still has Medium if uncommitted
    # changes only removed them from working copy). Also merge any Medium still
    # present only in a stash of current Data — HEAD is correct here.
    head_data = load_head_training()
    # If HEAD no longer has Medium (already committed), rebuild from snapshot of
    # pairs using current Strong->Weak and known id adjacency is unsafe.
    # Also try reading from git working tree backup: use HEAD first.
    medium_to_weak, medium_meta = build_medium_map(head_data)

    # If HEAD already lacks Medium (unlikely), try reconstructing from current
    # trainingdata Strong->Weak plus typical Medium id = weak+1 — not needed if HEAD has them.
    if not medium_to_weak:
        raise SystemExit("No Medium techniques found in HEAD trainingdata")

    # Also include Mediums that exist in current uncommitted trainingdata before
    # our edit — they're gone from working tree. Optionally scan agent was using
    # HEAD which may be older than the pre-Medium-removal working tree.
    # Working tree before Medium removal isn't saved; HEAD may miss newer Mediums.
    # Rebuild additional pairs from Strong techniques in CURRENT trainingdata:
    # current Strong.prereq = Weak; the Medium was between them. We don't have Medium ids
    # unless HEAD has them. Check count:
    print(f"Medium map size from HEAD: {len(medium_to_weak)}")

    # Augment from pre-migration character ownership: can't get names.
    # Also load from tmp if we saved anything — no.
    # Use git diff / show :Data/trainingdata.json for index? Try:
    # git show :Data/trainingdata.json is staged version.
    # Best: parse from the remove script logic against a recovered file.
    # Recover pre-removal trainingdata via: the parent content is in git stash? No.
    # Compare HEAD medium count vs what we removed (217). If HEAD has ~217 we're good.
    print(f"Medium meta entries: {len(medium_meta)}")

    medium_ids = set(medium_to_weak)

    with CHAR_PATH.open(encoding="utf-8") as f:
        data = json.load(f)
    pre_data = json.load(SNAPSHOT_PATH.open(encoding="utf-8"))
    report = []

    migrate_training(data["training"]["trainingByCategory"], medium_to_weak, report)
    scrub_tree(data, medium_ids, report)
    if isinstance(data.get("reminderSelections"), dict):
        before = len(data["reminderSelections"])
        data["reminderSelections"] = migrate_reminder_keys(
            data["reminderSelections"], medium_to_weak, report
        )
        report.append(f"reminder keys {before} -> {len(data['reminderSelections'])}")

    CHAR_PATH.write_text(jackson_dump(data), encoding="utf-8")

    train_lines = [r for r in report if r.startswith("[")]
    print(f"Training changes: {len(train_lines)}")
    for line in train_lines:
        print(" ", line)
    print(f"Total migration changes: {len(report)}")

    text = CHAR_PATH.read_text(encoding="utf-8")
    # Only flag medium ids in training-like or .al. contexts to reduce specialty false positives
    remaining = []
    for mid in sorted(medium_ids):
        if re.search(rf"\.al\.{mid}\.", text) or re.search(
            rf'"id"\s*:\s*{mid}\b', text
        ):
            remaining.append(mid)
    print(f"Medium IDs still in training/loadout contexts: {len(remaining)}")
    if remaining:
        print(" ", remaining[:30])

    lost, remapped, natural, domains = calc_lost_xp(
        pre_data, data, medium_to_weak, medium_meta
    )
    # Fix lost detection: DROP when weak was in pre_owned
    pre_owned_ids = {
        t["id"]
        for techs in pre_data["training"]["trainingByCategory"].values()
        for t in techs
    }
    post_owned_ids = {
        t["id"]
        for techs in data["training"]["trainingByCategory"].values()
        for t in techs
    }
    lost = []
    remapped = []
    for mid, techs_info in {
        t["id"]: t
        for techs in pre_data["training"]["trainingByCategory"].values()
        for t in techs
    }.items():
        if mid not in medium_to_weak:
            continue
        wid = medium_to_weak[mid]
        meta = medium_meta[mid]
        rank = techs_info.get("rank", 0)
        if mid in post_owned_ids:
            continue
        if wid in pre_owned_ids:
            xp = total_xp_for_rank(
                rank,
                meta["affinity"],
                natural,
                domains,
            )
            lost.append(
                {
                    "id": mid,
                    "name": meta["name"],
                    "affinity": meta["affinity"],
                    "rank": rank,
                    "xp": xp,
                }
            )
        else:
            remapped.append(
                {
                    "id": mid,
                    "weak": wid,
                    "name": meta["name"],
                    "rank": rank,
                }
            )

    lost.sort(key=lambda x: (x["affinity"], x["id"]))
    total_xp = sum(x["xp"] for x in lost)
    total_ranks = sum(x["rank"] for x in lost)

    print()
    print(f"Character: {data['identity']['name']}")
    print(f"Natural affinities: {sorted(natural) or '(none)'}")
    print(f"Remapped Medium->Weak (ranks kept): {len(remapped)}")
    for r in remapped:
        print(f"  {r['id']}->{r['weak']} {r['name']} rank={r['rank']}")
    print(f"Lost Medium techniques (dropped): {len(lost)}")
    print(f"Lost ranks (sum): {total_ranks}")
    print(f"Total training XP value: {total_xp}")
    print()
    print(f"{'ID':>6}  {'Rank':>4}  {'XP':>6}  {'Affinity':<12}  Name")
    print("-" * 72)
    by_aff = {}
    for x in lost:
        print(f"{x['id']:6d}  {x['rank']:4d}  {x['xp']:6d}  {x['affinity']:<12}  {x['name']}")
        by_aff.setdefault(x["affinity"], {"xp": 0, "ranks": 0, "count": 0})
        by_aff[x["affinity"]]["xp"] += x["xp"]
        by_aff[x["affinity"]]["ranks"] += x["rank"]
        by_aff[x["affinity"]]["count"] += 1
    print()
    print("By affinity:")
    for aff, s in sorted(by_aff.items()):
        note = " (1/2 cost - natural)" if aff.lower() in natural else ""
        print(f"  {aff}: {s['count']} techs, {s['ranks']} ranks, {s['xp']} XP{note}")


if __name__ == "__main__":
    main()
