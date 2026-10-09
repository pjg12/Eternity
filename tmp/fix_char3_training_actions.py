#!/usr/bin/env python3
"""
Repair action ID collisions and Active training grants so ranked techniques
resolve to the correctly named actions.

Focus issues for character 3:
- Sound Shield (Strong) granted Light Smash (Medium)
- Sound/Poison Eruption trainings had no matching actions
- Poison Shield/Antidote grants pointed at wrong/missing IDs
"""
from __future__ import annotations

import copy
import json
from pathlib import Path

ROOT = Path(r"C:\EternityTTRPG")
ACTION_PATH = ROOT / "Data" / "actiondata.json"
TRAINING_PATH = ROOT / "Data" / "trainingdata.json"
CHAR_PATH = ROOT / "Characters" / "3.json"


def load(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def dump(path: Path, data) -> None:
    text = json.dumps(data, indent=2, ensure_ascii=False)
    text = text.replace('": ', '" : ')
    with path.open("w", encoding="utf-8", newline="\n") as f:
        f.write(text)
        f.write("\n")


def used_ids(actions) -> set[int]:
    return {int(a["id"]) for a in actions}


def next_free_id(used: set[int], start: int) -> int:
    i = start
    while i in used:
        i += 1
    used.add(i)
    return i


def find_by_name(actions, name: str, affinity: str | None = None):
    out = []
    for a in actions:
        if a.get("name") != name:
            continue
        if affinity is not None and a.get("affinity") != affinity:
            continue
        out.append(a)
    return out


def eruption_template(actions):
    templates = {}
    for tier in ("Weak", "Medium", "Strong"):
        matches = find_by_name(actions, f"Enhanced Eruption ({tier})", "Enhancement")
        if not matches:
            raise SystemExit(f"Missing Enhanced Eruption ({tier}) template")
        templates[tier] = matches[0]
    return templates


def make_eruption(template, affinity: str, name: str, new_id: int, atk_type: str = "REF"):
    a = copy.deepcopy(template)
    a["id"] = new_id
    a["name"] = name
    a["affinity"] = affinity
    a["atkType"] = atk_type
    return a


def set_unique_ids(actions, affinity: str, ordered_names: list[str], start_id: int, used: set[int]) -> dict[str, int]:
    """Assign contiguous free IDs to named actions of an affinity. Creates nothing."""
    mapping: dict[str, int] = {}
    cursor = start_id
    for name in ordered_names:
        matches = find_by_name(actions, name, affinity)
        if not matches:
            print(f"  SKIP missing {affinity}/{name}")
            continue
        # Deduplicate same-name rows onto one ID
        new_id = next_free_id(used, cursor)
        cursor = new_id + 1
        for a in matches:
            # free old id occupancy tracking loosely; rebuild used later if needed
            a["id"] = new_id
        mapping[name] = new_id
        print(f"  {affinity}/{name} -> {new_id}")
    return mapping


def main() -> None:
    actions = load(ACTION_PATH)
    trainings = load(TRAINING_PATH)
    used = used_ids(actions)
    templates = eruption_template(actions)

    # --- 1) Give Venom Shot (Weak) a unique ID ---
    venom = find_by_name(actions, "Venom Shot (Weak)", "Poison")
    if venom:
        for a in venom:
            a["id"] = next_free_id(used, 40000)
        print(f"Venom Shot (Weak) -> {venom[0]['id']}")

    # --- 2) Renumber ALL Sound combat actions into a free block (keep Thunder 31001-3) ---
    print("Renumbering Sound actions...")
    sound_names = [
        "Sonic Shot (Weak)", "Sonic Shot (Medium)", "Sonic Shot (Strong)",
        "Reverb Shot (Weak)", "Reverb Shot (Medium)", "Reverb Shot (Strong)",
        "Sound Smash (Weak)", "Sound Smash (Medium)", "Sound Smash (Strong)",
        "Sound Jab (Weak)", "Sound Jab (Medium)", "Sound Jab (Strong)",
        "Sound Blast (Weak)", "Sound Blast (Medium)", "Sound Blast (Strong)",
        "Sound Bolt (Weak)", "Sound Bolt (Medium)", "Sound Bolt (Strong)",
        "Sound Heal (Weak)", "Sound Heal (Medium)", "Sound Heal (Strong)",
        "Sound Mend (Weak)", "Sound Mend (Medium)", "Sound Mend (Strong)",
        "Sound Shield (Weak)", "Sound Shield (Medium)", "Sound Shield (Strong)",
        "Sound Shell (Weak)", "Sound Shell (Medium)", "Sound Shell (Strong)",
        "Sound Therapy",
    ]
    # Rebuild used after venom change
    used = used_ids(actions)
    # Temporarily move Sound off Force/Light collision range by assigning high free IDs
    set_unique_ids(actions, "Sound", sound_names, 28110, used)

    # Thunder already unique at 31001-31003; ensure grants can find them
    for name, tid in (
        ("Thunder Shot (Weak)", 31001),
        ("Thunder Shot (Medium)", 31002),
        ("Thunder Shot (Strong)", 31003),
    ):
        matches = find_by_name(actions, name, "Sound")
        for a in matches:
            a["id"] = tid
        used.add(tid)

    # --- 3) Renumber Light combat actions into a free contiguous block ---
    print("Renumbering Light actions...")
    used = used_ids(actions)
    light_names = [
        "Luminous Shot (Weak)", "Luminous Shot (Medium)", "Luminous Shot (Strong)",
        "Bright Shot (Weak)", "Bright Shot (Medium)", "Bright Shot (Strong)",
        "Light Smash (Weak)", "Light Smash (Medium)", "Light Smash (Strong)",
        "Light Jab (Weak)", "Light Jab (Medium)", "Light Jab (Strong)",
        "Light Blast (Weak)", "Light Blast (Medium)", "Light Blast (Strong)",
        "Light Bolt (Weak)", "Light Bolt (Medium)", "Light Bolt (Strong)",
        "Light Heal (Weak)", "Light Heal (Medium)", "Light Heal (Strong)",
        "Light Mend (Weak)", "Light Mend (Medium)", "Light Mend (Strong)",
        "Light Shield (Weak)", "Light Shield (Medium)", "Light Shield (Strong)",
        "Light Shell (Weak)", "Light Shell (Medium)", "Light Shell (Strong)",
        "Light Cleanse",
    ]
    set_unique_ids(actions, "Light", light_names, 28200, used)

    # --- 4) Create missing Eruption actions for every affinity that trains them ---
    print("Creating missing Eruption actions...")
    used = used_ids(actions)
    # Prefer atkType REF like Enhanced Eruption (AoE)
    eruption_trainings = [
        t for t in trainings
        if t.get("type") == "Active" and t.get("name", "").endswith("Eruption (Weak)")
    ]
    created = 0
    for t in eruption_trainings:
        aff = t.get("affinity")
        base = t["name"].replace(" (Weak)", "")
        for tier, cost in (("Weak", 1.0), ("Medium", 3.0), ("Strong", 6.0)):
            name = f"{base} ({tier})"
            if find_by_name(actions, name, aff):
                continue
            # Seed ID near training id when free, else next free from 60000+
            preferred = int(t["id"]) + (0 if tier == "Weak" else 1 if tier == "Medium" else 2)
            # Sound/Light/Poison training ids collide with other namespaces; use free high band
            new_id = next_free_id(used, max(preferred, 60000) if preferred in used or preferred < 1000 else preferred)
            # If preferred was taken/low, still ensure uniqueness
            if new_id in used_ids(actions):
                new_id = next_free_id(used, 60000)
            tmpl = templates[tier]
            actions.append(make_eruption(tmpl, aff, name, new_id))
            # Ensure cost matches tier
            actions[-1]["costs"] = [{"type": "Aura", "value": cost}]
            created += 1
            print(f"  CREATE {aff}/{name} id={new_id}")
    print(f"Created {created} Eruption actions")

    # --- 5) Rebuild name->id map (unique names; last write wins if any leftovers) ---
    used = used_ids(actions)
    dups = {}
    for a in actions:
        dups.setdefault(int(a["id"]), []).append(f"{a.get('affinity')}/{a.get('name')}")
    remaining = {k: v for k, v in dups.items() if len(v) > 1}
    if remaining:
        print("WARNING duplicate IDs remain:")
        for k, v in sorted(remaining.items()):
            print(f"  {k}: {v}")

    by_name: dict[str, int] = {}
    for a in actions:
        by_name[a["name"]] = int(a["id"])

    burst_alias = {
        "Enhanced Burst (Weak)": "Enhanced Eruption (Weak)",
        "Enhanced Burst (Medium)": "Enhanced Eruption (Medium)",
        "Enhanced Burst (Strong)": "Enhanced Eruption (Strong)",
    }

    # --- 6) Fix all Active grants by name ---
    fixed = 0
    unresolved = []
    for t in trainings:
        if t.get("type") != "Active" or t.get("affinity") == "Crafting":
            continue
        name = t.get("name")
        target = burst_alias.get(name, name)
        if target not in by_name:
            unresolved.append((t.get("affinity"), t.get("id"), name))
            continue
        expected = [by_name[target]]
        if t.get("grant") != expected:
            t["grant"] = expected
            fixed += 1
    print(f"Updated grants: {fixed}; unresolved: {len(unresolved)}")
    for row in unresolved[:30]:
        print(f"  unresolved {row}")

    dump(ACTION_PATH, actions)
    dump(TRAINING_PATH, trainings)

    # --- 7) Validate character 3 ---
    actions = load(ACTION_PATH)
    trainings = load(TRAINING_PATH)
    char = load(CHAR_PATH)
    train_by_id = {int(t["id"]): t for t in trainings}
    action_by_id = {}
    for a in actions:
        action_by_id.setdefault(int(a["id"]), a)
    action_by_name = {a["name"]: a for a in actions}

    def resolve(tech):
        templates = []
        for g in tech.get("grant") or []:
            if g and g > 0 and int(g) in action_by_id:
                templates.append(action_by_id[int(g)])
        if templates:
            return templates
        if tech.get("name") in action_by_name:
            return [action_by_name[tech["name"]]]
        return []

    print("\n=== Character 3 Active technique validation ===")
    mismatches = []
    ok = 0
    for cat, entries in (char.get("training") or {}).get("trainingByCategory", {}).items():
        for e in entries:
            tech = train_by_id.get(int(e["id"]))
            if not tech or tech.get("type") != "Active" or int(e.get("rank", 0)) < 1:
                continue
            if tech.get("affinity") == "Crafting":
                continue
            got = resolve(tech)
            names = [a["name"] for a in got]
            if tech["name"] in names and len(got) == 1:
                ok += 1
            else:
                mismatches.append((tech["name"], e["rank"], tech.get("grant"), names))

    print(f"OK={ok} MISMATCH={len(mismatches)}")
    for row in mismatches:
        print(f"  BAD {row}")

    # Highlight reported examples
    print("\n=== Reported examples ===")
    for name in (
        "Sound Shield (Strong)",
        "Sound Shield (Weak)",
        "Light Smash (Weak)",
        "Light Smash (Medium)",
        "Poison Shield (Strong)",
        "Poison Antidote",
        "Sound Eruption (Weak)",
        "Sound Eruption (Strong)",
    ):
        t = next((x for x in trainings if x.get("name") == name), None)
        a = action_by_name.get(name)
        on_char = None
        for cat, entries in char["training"]["trainingByCategory"].items():
            for e in entries:
                tech = train_by_id.get(int(e["id"]))
                if tech and tech.get("name") == name:
                    on_char = e["rank"]
        grant_name = None
        if t and t.get("grant"):
            ga = action_by_id.get(int(t["grant"][0]))
            grant_name = ga["name"] if ga else "MISSING"
        print(
            f"{name}: train={(t or {}).get('id')} grant={(t or {}).get('grant')}->{grant_name} "
            f"action={(a or {}).get('id')} onCharRank={on_char}"
        )


if __name__ == "__main__":
    main()
