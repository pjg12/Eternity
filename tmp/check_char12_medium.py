import json
import subprocess

AFFINITIES_IGNORE = ()  # not needed

# HEAD medium map
head = json.loads(
    subprocess.run(
        ["git", "show", "HEAD:Data/trainingdata.json"],
        cwd=r"C:\EternityTTRPG",
        capture_output=True,
        text=True,
        encoding="utf-8",
    ).stdout
)
head_by_id = {t["id"]: t for t in head}
medium_ids = {
    t["id"]
    for t in head
    if t.get("type") == "Active" and "(Medium)" in t.get("name", "")
}

# Current training (post Medium removal)
cur_train = json.load(open(r"C:\EternityTTRPG\Data\trainingdata.json", encoding="utf-8"))
cur_ids = {t["id"] for t in cur_train}

pre = json.load(open(r"C:\EternityTTRPG\tmp\12_pre_medium_migration.json", encoding="utf-8"))
post = json.load(open(r"C:\EternityTTRPG\Characters\12.json", encoding="utf-8"))

pre_owned = {
    t["id"]: t.get("rank", 0)
    for techs in pre["training"]["trainingByCategory"].values()
    for t in techs
}
post_owned = {
    t["id"]: t.get("rank", 0)
    for techs in post["training"]["trainingByCategory"].values()
    for t in techs
}

# Owned IDs that are not in current trainingdata
orphans = sorted(i for i in pre_owned if i not in cur_ids)
print(f"Pre-owned IDs missing from current trainingdata: {len(orphans)}")
for i in orphans:
    meta = head_by_id.get(i)
    name = meta["name"] if meta else "?"
    typ = meta.get("type") if meta else "?"
    print(f"  {i}: {name} | {typ} | rank={pre_owned[i]} | in_head_medium={i in medium_ids} | still_owned={i in post_owned}")

# Working-tree medium count was 217; HEAD 216. Find the extra medium from
# analyzing which active mediums were in a reconstructed set.
# Compare HEAD mediums vs what Strong in current points to.
# The missing one might be Metal Blast related.
head_medium = [t for t in head if t.get("type") == "Active" and "(Medium)" in t.get("name", "")]
print(f"HEAD medium count: {len(head_medium)}")

# Find Strong in current whose weak exists, and check if (weak_id+1) was medium historically
# Look for Metal Blast naming quirk in HEAD
for t in head:
    if "Metal" in t.get("name", "") and "Blast" in t.get("name", "") and t.get("type") == "Active":
        print(f"HEAD Metal Blast: {t['id']} {t['name']} prereq={t['prereq']}")
