package eternity;

import java.io.File;
import java.sql.Timestamp;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for loading/saving character metadata (CharStore list)
 */
public class CharacterDataManager {
    private static final String CONFIG_PATH = ("Characters/charStore.json");
    private static final String CHARACTER_DIR = "Characters";
    private static final String BACKUP_DIR = "Characters/Backup";
    private static final String AUTO_DIR = "Characters/Auto";
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static ArrayList<CharStore> loadCharStore() {
        ArrayList<CharStore> list = new ArrayList<>();
        try {
            File f = new File(CONFIG_PATH);
            if (f.exists()) {
                CharStore[] array = mapper.readValue(f, CharStore[].class);
                for (CharStore c : array) {
                    if (c == null) continue;
                    CharStore resolved = buildLatestStoreEntry(c.getIndex(), c);
                    if (resolved != null) list.add(resolved);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CharStore config: " + e.getMessage());
        }
        saveCharStore(list);
        return list;
    }

    /**
     * Load the full character JSON for the given id.
     * Returns null if the file is missing or can't be parsed.
     */
    public static CharData loadCharacter(int id) {
        try {
            File f = new File(CHARACTER_DIR, id + ".json");
            if (!f.exists()) {
                System.err.println("Character file not found: " + f.getPath());
                return null;
            }
            System.out.println("Loading character file: " + f.getPath());
            return mapper.readValue(f, CharData.class);
        } catch (Exception e) {
            System.err.println("Error loading character " + id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Convenience overload for loading with a CharStore entry.
     */
    public static CharData loadCharacter(CharStore store) {
        if (store == null) return null;
        String ref = store.getReference();
        if (ref != null && !ref.isBlank()) {
            try {
                File f = new File(ref);
                if (f.exists()) {
                    System.out.println("Loading character file: " + f.getPath());
                    return mapper.readValue(f, CharData.class);
                }
            } catch (Exception e) {
                System.err.println("Error loading character from reference " + ref + ": " + e.getMessage());
            }
        }
        return loadCharacter(store.getIndex());
    }

    public static void saveCharStore(List<CharStore> list) {
        try {
            ArrayList<CharStore> normalized = new ArrayList<>();
            if (list != null) {
                java.util.Set<Integer> seen = new java.util.HashSet<>();
                for (CharStore entry : list) {
                    if (entry == null) continue;
                    int idx = entry.getIndex();
                    if (!seen.add(idx)) continue;
                    CharStore resolved = buildLatestStoreEntry(idx, entry);
                    if (resolved != null) normalized.add(resolved);
                }
            }
            File f = new File(CONFIG_PATH);
            mapper.writerWithDefaultPrettyPrinter().writeValue(f, normalized);
        } catch (Exception e) {
            System.err.println("Error saving CharStore config: " + e.getMessage());
        }
    }

    /**
     * Saves the full CharData JSON to the Characters directory using its index as filename.
     * Returns true on success, false on failure.
     */
    public static boolean saveCharacter(CharData character) {
        return saveCharacterInternal(character, false);
    }

    /**
     * Saves character and rotates backups for explicit user-initiated saves:
     * Backup1 -> Backup2, current main file -> Backup1, then write new main file.
     */
    public static boolean saveCharacterManual(CharData character) {
        return saveCharacterInternal(character, true);
    }

    /**
     * Autosave snapshot rotation:
     * Auto2 -> Auto3, Auto1 -> Auto2, current autosave -> Auto1.
     */
    public static boolean saveCharacterAuto(CharData character) {
        if (character == null || character.getIdentity() == null) {
            System.err.println("Cannot autosave character: identity is null.");
            return false;
        }

        int idx = character.getIdentity().getIndex();
        if (idx < 0) {
            System.err.println("Cannot autosave character: invalid index " + idx);
            return false;
        }

        File autoDir = new File(AUTO_DIR);
        if (!autoDir.exists() && !autoDir.mkdirs()) {
            System.err.println("Cannot autosave character: failed to create directory " + autoDir.getPath());
            return false;
        }

        File auto1 = new File(autoDir, idx + "Auto1.json");
        File auto2 = new File(autoDir, idx + "Auto2.json");
        File auto3 = new File(autoDir, idx + "Auto3.json");

        try {
            // Rotate older snapshots first
            if (auto2.exists()) {
                Files.copy(auto2.toPath(), auto3.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if (auto1.exists()) {
                Files.copy(auto1.toPath(), auto2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // Write newest autosave to Auto1
            mapper.writerWithDefaultPrettyPrinter().writeValue(auto1, character);
            return true;
        } catch (Exception e) {
            System.err.println("Error autosaving character " + idx + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean saveCharacterInternal(CharData character, boolean rotateManualBackups) {
        if (character == null || character.getIdentity() == null) {
            System.err.println("Cannot save character: identity is null.");
            return false;
        }

        // Ensure base training seeds are present before saving
        seedBaseTraining(character);

        int idx = character.getIdentity().getIndex();
        if (idx < 0) {
            System.err.println("Cannot save character: invalid index " + idx);
            return false;
        }

        File dir = new File(CHARACTER_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Cannot save character: failed to create directory " + dir.getPath());
            return false;
        }

        File target = new File(dir, idx + ".json");
        boolean isNewCharacter = !target.exists();
        try {
            if (rotateManualBackups && !isNewCharacter) {
                rotateBackupsForManualSave(idx, target);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(target, character);
            if (isNewCharacter) {
                createInitialBackups(character, idx);
                createInitialAutos(character, idx);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error saving character " + idx + ": " + e.getMessage());
            return false;
        }
    }

    public static CharStore getLastLoaded(List<CharStore> list) {
    	if (list == null || list.isEmpty()) return null;
    	
    	CharStore newest = null;
    	
        for (CharStore c : list) {
        	if (newest == null) newest = c;
        	else if (c.getUpdated().compareTo(newest.getUpdated()) > 0.0) newest = c;
        }
        return newest;
    }

    /**
     * Returns the smallest positive integer index not currently used by the given CharStore list.
     */
    public static int getNextFreeIndex(List<CharStore> list) {
        java.util.Set<Integer> used = new java.util.HashSet<>();
        if (list != null) {
            for (CharStore c : list) {
                if (c != null) used.add(c.getIndex());
            }
        }
        int candidate = 1;
        while (used.contains(candidate)) {
            candidate++;
        }
        return candidate;
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private static void seedBaseTraining(CharData character) {
        if (character == null || character.getTraining() == null) return;
        DataQuery dq = new DataQuery();
        int[][] ranges = {
            {1,12}, {21,24}, {31,49}, {51,55}, {61,65}
        };
        int[] singles = {101, 141, 181};

        for (int[] r : ranges) {
            for (int id = r[0]; id <= r[1]; id++) {
                addTrainingIfMissing(character, dq, id);
            }
        }
        for (int id : singles) {
            addTrainingIfMissing(character, dq, id);
        }
    }

    private static void addTrainingIfMissing(CharData character, DataQuery dq, int trainingId) {
        var training = character.getTraining();
        if (training.getTrainingById(trainingId) != null) return;
        DataTraining tech = dq.getTrainingById(trainingId);
        if (tech == null) return;
        DataTraining clone = new DataTraining(tech);
        clone.setRank(0);
        training.addTraining(clone);
    }

    private static void createInitialBackups(CharData character, int idx) {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            System.err.println("Failed to create backup directory: " + backupDir.getPath());
            return;
        }

        File backup1 = new File(backupDir, idx + "Backup1.json");
        File backup2 = new File(backupDir, idx + "Backup2.json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(backup1, character);
            mapper.writerWithDefaultPrettyPrinter().writeValue(backup2, character);
        } catch (Exception e) {
            System.err.println("Error creating initial backups for character " + idx + ": " + e.getMessage());
        }
    }

    private static void createInitialAutos(CharData character, int idx) {
        File autoDir = new File(AUTO_DIR);
        if (!autoDir.exists() && !autoDir.mkdirs()) {
            System.err.println("Failed to create auto directory: " + autoDir.getPath());
            return;
        }

        File auto1 = new File(autoDir, idx + "Auto1.json");
        File auto2 = new File(autoDir, idx + "Auto2.json");
        File auto3 = new File(autoDir, idx + "Auto3.json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(auto1, character);
            mapper.writerWithDefaultPrettyPrinter().writeValue(auto2, character);
            mapper.writerWithDefaultPrettyPrinter().writeValue(auto3, character);
        } catch (Exception e) {
            System.err.println("Error creating initial autos for character " + idx + ": " + e.getMessage());
        }
    }

    private static void rotateBackupsForManualSave(int idx, File currentMainFile) {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            System.err.println("Failed to create backup directory: " + backupDir.getPath());
            return;
        }

        File backup1 = new File(backupDir, idx + "Backup1.json");
        File backup2 = new File(backupDir, idx + "Backup2.json");

        try {
            // Backup1 becomes Backup2
            if (backup1.exists()) {
                Files.copy(backup1.toPath(), backup2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // Current main file becomes Backup1 before overwrite
            if (currentMainFile.exists()) {
                Files.copy(currentMainFile.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("Error rotating backups for character " + idx + ": " + e.getMessage());
        }
    }

    /** Builds CharStore metadata/reference from the newest snapshot among main, backup, and auto files. */
    private static CharStore buildLatestStoreEntry(int idx, CharStore fallback) {
        File latest = getLatestSnapshotFile(idx);
        if (latest == null) {
            System.err.println("Missing character snapshots for id " + idx + " - removing entry from store.");
            return null;
        }

        try {
            CharData data = mapper.readValue(latest, CharData.class);
            CharIdentity id = data != null ? data.getIdentity() : null;
            if (id == null) {
                if (fallback != null) fallback.setReference(latest.getPath());
                return fallback;
            }
            Timestamp updated = id.getUpdated() != null ? id.getUpdated() : new Timestamp(latest.lastModified());
            return new CharStore(
                idx,
                id.getName(),
                id.getCampaign(),
                id.getRace(),
                id.getCharClass(),
                id.getLevel(),
                updated,
                latest.getPath()
            );
        } catch (Exception e) {
            System.err.println("Error reading latest snapshot for id " + idx + ": " + e.getMessage());
            if (fallback != null) fallback.setReference(latest.getPath());
            return fallback;
        }
    }

    /** Returns newest existing file among main, Backup1/2, and Auto1/2/3 for a character id. */
    private static File getLatestSnapshotFile(int idx) {
        File[] candidates = new File[] {
            new File(CHARACTER_DIR, idx + ".json"),
            new File(BACKUP_DIR, idx + "Backup1.json"),
            new File(BACKUP_DIR, idx + "Backup2.json"),
            new File(AUTO_DIR, idx + "Auto1.json"),
            new File(AUTO_DIR, idx + "Auto2.json"),
            new File(AUTO_DIR, idx + "Auto3.json")
        };
        File latest = null;
        long latestTs = Long.MIN_VALUE;
        for (File file : candidates) {
            if (file == null || !file.exists()) continue;
            long ts = file.lastModified();
            if (latest == null || ts > latestTs) {
                latest = file;
                latestTs = ts;
            }
        }
        return latest;
    }
}
