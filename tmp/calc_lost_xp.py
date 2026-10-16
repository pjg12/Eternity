import json
import subprocess
from pathlib import Path

AFFINITIES = {
    "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire",
    "Electricity", "Force", "Sound", "Light", "Darkness", "Poison",
    "Psionic", "Energy",
}


def build_greater_map():
    result = subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    old = json.loads(result.stdout)
    by_id = {t["id"]: t for t in old}
    mapping = {}
    meta = {}
    for t in old:
        if (
            t.get("type") == "Maintained"
            and t.get("affinity") in AFFINITIES
            and "(Greater)" in t["name"]
        ):
            mapping[t["id"]] = t["prereq"]
            meta[t["id"]] = t
    return mapping, meta, by_id


def xp_cost_at_rank(rank: int, affinity: str, natural: set[str], domains: set[str]) -> int:
    """Cost to go from `rank` to `rank+1`, matching DataTraining.getNextAt."""
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
    """Total XP spent to reach `rank` from 0."""
    return sum(xp_cost_at_rank(r, affinity, natural, domains) for r in range(rank))


def main():
    backup = json.load(open(r"C:\EternityTTRPG\Characters\Backup\12Backup3.json", encoding="utf-8"))
    current = json.load(open(r"C:\EternityTTRPG\Characters\12.json", encoding="utf-8"))

    greater_to_lesser, greater_meta, by_id = build_greater_map()

    natural = {a.lower() for a in (current["training"].get("naturalAffinities") or [])}
    # Domain XP discount uses derivedAffinities at runtime; not persisted. Use empty unless present.
    domains = {a.lower() for a in (current["training"].get("derivedAffinities") or [])}

    def owned(data):
        ids = {}
        for cat, techs in data["training"]["trainingByCategory"].items():
            for t in techs:
                ids[t["id"]] = (cat, t.get("rank", 0))
        return ids

    old_owned = owned(backup)
    new_owned = owned(current)

    lost = []
    for gid, (cat, rank) in old_owned.items():
        if gid in greater_to_lesser and gid not in new_owned:
            meta = greater_meta.get(gid)
            if meta is None:
                # not in HEAD map — skip or try character-side
                continue
            affinity = meta["affinity"]
            name = meta["name"]
            xp = total_xp_for_rank(rank, affinity, natural, domains)
            lost.append(
                {
                    "id": gid,
                    "name": name,
                    "affinity": affinity,
                    "category": cat,
                    "rank": rank,
                    "xp": xp,
                    "lesser_id": greater_to_lesser[gid],
                    "lesser_kept_rank": new_owned.get(greater_to_lesser[gid], (None, None))[1],
                }
            )

    lost.sort(key=lambda x: (x["affinity"], x["id"]))

    total_xp = sum(x["xp"] for x in lost)
    total_ranks = sum(x["rank"] for x in lost)

    print(f"Character: {current['identity']['name']}")
    print(f"Natural affinities: {sorted(natural) or '(none)'}")
    print(f"Domain/derived affinities for XP: {sorted(domains) or '(none)'}")
    print(f"Lost Greater techniques: {len(lost)}")
    print(f"Lost ranks (sum): {total_ranks}")
    print(f"Total training XP value: {total_xp}")
    print()
    print(f"{'ID':>6}  {'Rank':>4}  {'XP':>6}  {'Affinity':<12}  Name")
    print("-" * 72)
    by_aff = {}
    for x in lost:
        print(
            f"{x['id']:6d}  {x['rank']:4d}  {x['xp']:6d}  {x['affinity']:<12}  {x['name']}"
        )
        by_aff.setdefault(x["affinity"], {"xp": 0, "ranks": 0, "count": 0})
        by_aff[x["affinity"]]["xp"] += x["xp"]
        by_aff[x["affinity"]]["ranks"] += x["rank"]
        by_aff[x["affinity"]]["count"] += 1

    print()
    print("By affinity:")
    for aff, s in sorted(by_aff.items()):
        note = " (½ cost — natural)" if aff.lower() in natural else ""
        print(
            f"  {aff}: {s['count']} techs, {s['ranks']} ranks, {s['xp']} XP{note}"
        )

    # Formula reminder
    print()
    print("Formula: cost to go from rank k -> k+1 is (k*4+10), modified by affinity;")
    print("total for rank R is sum of those costs for k=0..R-1.")


if __name__ == "__main__":
    main()
