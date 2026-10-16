#!/usr/bin/env python3
import json
import os
import shutil
import subprocess
from pathlib import Path

ROOT = Path(r"C:\EternityTTRPG")
LOCAL = Path(os.environ["LOCALAPPDATA"]) / "Eternity" / "Characters"
BACKUP = LOCAL / "Backup"
IDS = [1, 5, 6, 7]


def main() -> None:
    for n in IDS:
        src = BACKUP / f"{n}Backup_pre_medium_standard.json"
        dst = LOCAL / f"{n}.json"
        if not src.exists():
            print(f"MISSING backup for {n}: {src}")
            continue
        shutil.copy2(src, dst)
        data = json.loads(dst.read_text(encoding="utf-8"))
        print(f"Restored {n} ({data['identity']['name']}) from {src.name}")

    pre = subprocess.run(
        ["git", "show", "0ad2820^:Data/trainingdata.json"],
        cwd=ROOT,
        capture_output=True,
    )
    historic = {t["id"]: t for t in json.loads(pre.stdout.decode("utf-8"))}
    current = {
        t["id"]: t
        for t in json.loads((ROOT / "Data" / "trainingdata.json").read_text(encoding="utf-8"))
    }
    removed_medium = {
        i
        for i, t in historic.items()
        if "(Medium)" in (t.get("name") or "") and i not in current
    }
    standard = {i for i, t in current.items() if "(Standard)" in (t.get("name") or "")}

    print()
    for n in IDS:
        data = json.loads((LOCAL / f"{n}.json").read_text(encoding="utf-8"))
        owned = [
            t["id"]
            for techs in data["training"]["trainingByCategory"].values()
            for t in techs
        ]
        med = sorted(i for i in owned if i in removed_medium)
        std = sorted(i for i in owned if i in standard)
        print(f"{n}: Medium={med} Standard={std}")


if __name__ == "__main__":
    main()
