#!/usr/bin/env python3
"""Fix actiondata ID collisions and align Active training grants to action IDs by name."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(r"C:\EternityTTRPG")
ACTION_PATH = ROOT / "Data" / "actiondata.json"
TRAINING_PATH = ROOT / "Data" / "trainingdata.json"


def load(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def dump(path: Path, data) -> None:
    # Match repo style: pretty, 2-space indent, spaces around colons.
    text = json.dumps(data, indent=2, ensure_ascii=False)
    text = text.replace('": ', '" : ')
    with path.open("w", encoding="utf-8", newline="\n") as f:
        f.write(text)
        f.write("\n")


def find_actions(actions, name: str, affinity: str | None = None):
    out = []
    for a in actions:
        if a.get("name") != name:
            continue
        if affinity is not None and a.get("affinity") != affinity:
            continue
        out.append(a)
    return out


def set_id_by_name(actions, name: str, new_id: int, affinity: str | None = None) -> int:
    matches = find_actions(actions, name, affinity)
    if not matches:
        raise SystemExit(f"Missing action to renumber: {affinity}/{name}")
    for a in matches:
        a["id"] = new_id
    return len(matches)


def rename_action(actions, old_name: str, new_name: str, affinity: str, new_affinity: str | None = None) -> int:
    count = 0
    for a in actions:
        if a.get("name") == old_name and a.get("affinity") == affinity:
            a["name"] = new_name
            if new_affinity is not None:
                a["affinity"] = new_affinity
            count += 1
    return count


def renumber_heal_chain(actions, affinity: str, mapping: dict[str, int]) -> None:
    for name, new_id in mapping.items():
        set_id_by_name(actions, name, new_id, affinity)


def main() -> None:
    actions = load(ACTION_PATH)
    trainings = load(TRAINING_PATH)

    # --- 1) Fix actiondata collisions / missing Poison Eruption ---

    # Poison: convert misplaced Enhanced Eruption copies into Poison Eruption
    renamed = 0
    for weak_med_strong, old_ids in (
        ("Weak", None),
        ("Medium", None),
        ("Strong", None),
    ):
        pass
    for suffix in ("Weak", "Medium", "Strong"):
        old = f"Enhanced Eruption ({suffix})"
        new = f"Poison Eruption ({suffix})"
        # Only rename Enhancement-affinity rows that currently sit in the poison id band
        for a in actions:
            if (
                a.get("name") == old
                and a.get("affinity") == "Enhancement"
                and 40021 <= int(a.get("id", -1)) <= 40023
            ):
                a["name"] = new
                a["affinity"] = "Poison"
                renamed += 1
    print(f"Renamed misplaced Enhanced Eruption -> Poison Eruption: {renamed}")

    # Renumber Heal+ chains so they no longer collide with Eruption IDs.
    # Target IDs follow the Fire/training pattern (training id == action id for W/S).
    chains = {
        "Darkness": {
            "Dark Heal (Weak)": 37022,
            "Dark Heal (Medium)": 37023,
            "Dark Heal (Strong)": 37024,
            "Dark Mend (Weak)": 37025,
            "Dark Mend (Medium)": 37026,
            "Dark Mend (Strong)": 37027,
            "Dark Shield (Weak)": 37028,
            "Dark Shield (Medium)": 37029,
            "Dark Shield (Strong)": 37030,
            "Dark Shell (Weak)": 37031,
            "Dark Shell (Medium)": 37032,
            "Dark Shell (Strong)": 37033,
            "Dark Cleanse": 37034,
        },
        "Psionic": {
            "Psionic Heal (Weak)": 43022,
            "Psionic Heal (Medium)": 43023,
            "Psionic Heal (Strong)": 43024,
            "Psionic Mend (Weak)": 43025,
            "Psionic Mend (Medium)": 43026,
            "Psionic Mend (Strong)": 43027,
            "Psionic Shield (Weak)": 43028,
            "Psionic Shield (Medium)": 43029,
            "Psionic Shield (Strong)": 43030,
            "Psionic Shell (Weak)": 43031,
            "Psionic Shell (Medium)": 43032,
            "Psionic Shell (Strong)": 43033,
            "Psionic Therapy": 43034,
        },
        "Energy": {
            "Energy Heal (Weak)": 46022,
            "Energy Heal (Medium)": 46023,
            "Energy Heal (Strong)": 46024,
            "Energy Mend (Weak)": 46025,
            "Energy Mend (Medium)": 46026,
            "Energy Mend (Strong)": 46027,
            "Energy Shield (Weak)": 46028,
            "Energy Shield (Medium)": 46029,
            "Energy Shield (Strong)": 46030,
            "Energy Shell (Weak)": 46031,
            "Energy Shell (Medium)": 46032,
            "Energy Shell (Strong)": 46033,
            "Energy Cleanse": 46034,
        },
        "Spirit": {
            "Spirit Heal (Weak)": 49028,
            "Spirit Heal (Medium)": 49029,
            "Spirit Heal (Strong)": 49030,
            "Spirit Mend (Weak)": 49031,
            "Spirit Mend (Medium)": 49032,
            "Spirit Mend (Strong)": 49033,
            "Spirit Shield (Weak)": 49034,
            "Spirit Shield (Medium)": 49035,
            "Spirit Shield (Strong)": 49036,
            "Spirit Shell (Weak)": 49037,
            "Spirit Shell (Medium)": 49038,
            "Spirit Shell (Strong)": 49039,
            "Spirit Therapy": 49040,
            "Spirit Cleanse": 49041,
        },
        "Time": {
            "Time Heal (Weak)": 52028,
            "Time Heal (Medium)": 52029,
            "Time Heal (Strong)": 52030,
            "Time Mend (Weak)": 52031,
            "Time Mend (Medium)": 52032,
            "Time Mend (Strong)": 52033,
            "Time Shield (Weak)": 52034,
            "Time Shield (Medium)": 52035,
            "Time Shield (Strong)": 52036,
            "Time Shell (Weak)": 52037,
            "Time Shell (Medium)": 52038,
            "Time Shell (Strong)": 52039,
            "Time Cure": 52040,
            "Time Antidote": 52041,
        },
        "Poison": {
            # Align with poison training IDs (Fire-style).
            "Poison Eruption (Weak)": 40022,
            "Poison Eruption (Medium)": 40023,
            "Poison Eruption (Strong)": 40024,
            "Poison Heal (Weak)": 40025,
            "Poison Heal (Medium)": 40026,
            "Poison Heal (Strong)": 40027,
            "Poison Mend (Weak)": 40028,
            "Poison Mend (Medium)": 40029,
            "Poison Mend (Strong)": 40030,
            "Poison Shield (Weak)": 40031,
            "Poison Shield (Medium)": 40032,
            "Poison Shield (Strong)": 40033,
            "Poison Shell (Weak)": 40034,
            "Poison Shell (Medium)": 40035,
            "Poison Shell (Strong)": 40036,
            "Poison Antidote": 40037,
        },
    }

    for affinity, mapping in chains.items():
        renumber_heal_chain(actions, affinity, mapping)
        print(f"Renumbered {affinity} heal-chain actions: {len(mapping)}")

    # Venom Shot (Weak) duplicated Corrosive Shot (Weak) at 40001
    set_id_by_name(actions, "Venom Shot (Weak)", 40000, "Poison")
    print("Assigned Venom Shot (Weak) -> 40000")

    # Sound actions colliding with Force Shell/Cure
    set_id_by_name(actions, "Sound Smash (Weak)", 28106, "Sound")
    set_id_by_name(actions, "Sonic Shot (Weak)", 28107, "Sound")
    set_id_by_name(actions, "Sonic Shot (Strong)", 28108, "Sound")
    print("Moved colliding Sound actions -> 28106-28108")

    # --- 2) Rebuild action lookup by exact name (last write wins if somehow dup names) ---
    action_by_name: dict[str, int] = {}
    dup_ids_after: dict[int, list[str]] = {}
    for a in actions:
        aid = int(a["id"])
        aname = a["name"]
        action_by_name[aname] = aid
        dup_ids_after.setdefault(aid, []).append(f"{a.get('affinity')}/{aname}")

    still_dup = {k: v for k, v in dup_ids_after.items() if len(v) > 1}
    if still_dup:
        print("WARNING: remaining duplicate action IDs:")
        for k, v in sorted(still_dup.items()):
            print(f"  {k}: {v}")

    # Alias: Enhanced Burst trainings have no Burst actions; grant Eruption instead.
    burst_aliases = {
        "Enhanced Burst (Weak)": "Enhanced Eruption (Weak)",
        "Enhanced Burst (Medium)": "Enhanced Eruption (Medium)",
        "Enhanced Burst (Strong)": "Enhanced Eruption (Strong)",
    }

    # --- 3) Fix Active training grants ---
    fixed = 0
    skipped_crafting = 0
    unresolved = []
    for t in trainings:
        if t.get("type") != "Active":
            continue
        name = t.get("name")
        grants = t.get("grant") or []
        # Crafting actives intentionally have empty/sentinel grants
        if t.get("affinity") == "Crafting":
            skipped_crafting += 1
            continue

        target_name = burst_aliases.get(name, name)
        if target_name not in action_by_name:
            unresolved.append((t.get("affinity"), t.get("id"), name, grants))
            continue

        expected = action_by_name[target_name]
        new_grant = [expected]
        if grants != new_grant:
            t["grant"] = new_grant
            fixed += 1

    print(f"Updated Active grants: {fixed}")
    print(f"Skipped Crafting actives: {skipped_crafting}")
    if unresolved:
        print("Unresolved Active trainings (no action by name):")
        for row in unresolved:
            print(f"  {row}")

    dump(ACTION_PATH, actions)
    dump(TRAINING_PATH, trainings)
    print("Wrote actiondata.json and trainingdata.json")

    # --- 4) Verify ---
    actions = load(ACTION_PATH)
    trainings = load(TRAINING_PATH)
    by_id = {}
    for a in actions:
        by_id.setdefault(int(a["id"]), []).append(a["name"])
    by_name = {a["name"]: int(a["id"]) for a in actions}

    ok = wrong = missing = noname = 0
    wrong_rows = []
    for t in trainings:
        if t.get("type") != "Active" or t.get("affinity") == "Crafting":
            continue
        name = t["name"]
        target_name = burst_aliases.get(name, name)
        g = [x for x in (t.get("grant") or []) if x and x > 0]
        if not g:
            missing += 1
            wrong_rows.append(("NO_GRANT", name, None, None))
            continue
        gid = g[0]
        if target_name not in by_name:
            noname += 1
            wrong_rows.append(("NO_NAME", name, gid, None))
            continue
        expected = by_name[target_name]
        gnames = by_id.get(gid, [])
        # Accept if grant id maps to expected name (unique id) or expected id matches
        if gid == expected and target_name in gnames and len(gnames) == 1:
            ok += 1
        elif gid == expected and target_name in gnames:
            # id still shared somehow
            wrong += 1
            wrong_rows.append(("DUP_ID", name, gid, gnames))
        else:
            wrong += 1
            wrong_rows.append(("WRONG", name, gid, gnames))

    print(f"VERIFY Active(non-Crafting): OK={ok} WRONG={wrong} NO_GRANT={missing} NO_NAME={noname}")
    if wrong_rows:
        print("Remaining issues:")
        for row in wrong_rows[:50]:
            print(f"  {row}")


if __name__ == "__main__":
    main()
