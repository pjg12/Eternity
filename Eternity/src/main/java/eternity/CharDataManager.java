package eternity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for loading/saving character metadata (StoreChar list)
 */
public class CharDataManager {
    private static final String CONFIG_PATH = ("Characters/charStore.json");
    private static final String CHARACTER_DIR = "Characters";
    private static final String BACKUP_DIR = "Characters/Backup";
    private static final String AUTO_DIR = "Characters/Auto";
    static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final ObjectWriter PRETTY_WRITER = mapper.writerWithDefaultPrettyPrinter();
    
    // Training ID ranges for base training seeding
    private static final int[][] TRAINING_RANGES = {{1,12}, {21,24}, {31,49}, {51,55}, {61,65}};
    private static final int[] TRAINING_SINGLES = {101, 141, 181};

    // Lazy holder keeps DataQuery shared without synchronization overhead on reads.
    private static final class DataQueryHolder {
        private static final DataQuery INSTANCE = new DataQuery();
    }
    
    // ---- File path helpers ----
    private static String getCharacterPath(int idx) {
        return CHARACTER_DIR + File.separator + idx + ".json";
    }
    
    private static String getBackupPath(int idx, int backupNum) {
        return BACKUP_DIR + File.separator + idx + "Backup" + backupNum + ".json";
    }
    
    private static String getAutoPath(int idx, int autoNum) {
        return AUTO_DIR + File.separator + idx + "Auto" + autoNum + ".json";
    }
    public static DataQuery getDataQuery() {
        return DataQueryHolder.INSTANCE;
    }

    public static ArrayList<StoreChar> loadCharStore() {
        ArrayList<StoreChar> list = new ArrayList<>();
        try {
            File f = new File(CONFIG_PATH);
            if (f.exists()) {
                StoreChar[] array = mapper.readValue(f, StoreChar[].class);
                for (StoreChar c : array) {
                    if (c == null) continue;
                    StoreChar resolved = resolveStoreEntry(c);
                    if (resolved != null) list.add(resolved);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading StoreChar config: " + e.getMessage());
        }
        return list;
    }

    /**
     * Load the full character JSON for the given id.
     * Returns null if the file is missing or can't be parsed.
     */
    public static CharData loadCharacter(int id) {
        return loadCharacterFromFile(new File(getCharacterPath(id)), "character " + id);
    }

    /**
     * Convenience overload for loading with a StoreChar entry.
     */
    public static CharData loadCharacter(StoreChar store) {
        if (store == null) return null;
        String ref = store.getReference();
        if (ref != null && !ref.isBlank()) {
            CharData data = loadCharacterFromFile(new File(ref), "character reference " + ref);
            if (data != null) return data;
        }
        return loadCharacter(store.getIndex());
    }

    public static void saveCharStore(List<StoreChar> list) {
        try {
            ArrayList<StoreChar> normalized = new ArrayList<>();
            if (list != null) {
                Set<Integer> seen = new HashSet<>(Math.max(16, list.size()));
                for (StoreChar entry : list) {
                    if (entry == null) continue;
                    int idx = entry.getIndex();
                    if (!seen.add(idx)) continue;
                    StoreChar resolved = prepareStoreEntryForSave(entry);
                    if (resolved != null) normalized.add(resolved);
                }
            }
            File f = new File(CONFIG_PATH);
            PRETTY_WRITER.writeValue(f, normalized);
        } catch (Exception e) {
            System.err.println("Error saving StoreChar config: " + e.getMessage());
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
        if (!ensureDirectory(autoDir, "Cannot autosave character: failed to create directory")) {
            return false;
        }

        File auto1 = new File(getAutoPath(idx, 1));
        File auto2 = new File(getAutoPath(idx, 2));
        File auto3 = new File(getAutoPath(idx, 3));

        try {
            // Rotate older snapshots first
            if (auto2.exists()) {
                Files.move(auto2.toPath(), auto3.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if (auto1.exists()) {
                Files.move(auto1.toPath(), auto2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // Write newest autosave to Auto1
            PRETTY_WRITER.writeValue(auto1, character);
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
        if (!ensureDirectory(dir, "Cannot save character: failed to create directory")) {
            return false;
        }

        File target = new File(getCharacterPath(idx));
        boolean isNewCharacter = !target.exists();
        try {
            if (rotateManualBackups && !isNewCharacter) {
                rotateBackupsForManualSave(idx, target);
            }
            PRETTY_WRITER.writeValue(target, character);
            if (isNewCharacter) {
                createInitialBackups(target, idx);
                createInitialAutos(target, idx);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error saving character " + idx + ": " + e.getMessage());
            return false;
        }
    }

    public static StoreChar getLastLoaded(List<StoreChar> list) {
    	if (list == null || list.isEmpty()) return null;
    	
    	StoreChar newest = null;
    	
        for (StoreChar c : list) {
            if (c == null || c.getUpdated() == null) continue;
        	if (newest == null) newest = c;
        	else if (c.getUpdated().compareTo(newest.getUpdated()) > 0) newest = c;
        }
        return newest;
    }

    /**
     * Returns the smallest positive integer index not currently used by the given StoreChar list.
     */
    public static int getNextFreeIndex(List<StoreChar> list) {
        Set<Integer> used = new HashSet<>(list == null ? 16 : Math.max(16, list.size()));
        if (list != null) {
            for (StoreChar c : list) {
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
        DataQuery dq = getDataQuery();
        Set<Integer> existingIds = collectExistingTrainingIds(character);
        Map<Integer, DataTraining> trainingById = new HashMap<>();
        
        for (int[] r : TRAINING_RANGES) {
            for (int id = r[0]; id <= r[1]; id++) {
                addTrainingIfMissing(character, dq, id, existingIds, trainingById);
            }
        }
        for (int id : TRAINING_SINGLES) {
            addTrainingIfMissing(character, dq, id, existingIds, trainingById);
        }
    }

    private static Set<Integer> collectExistingTrainingIds(CharData character) {
        Set<Integer> existingIds = new HashSet<>();
        var training = character.getTraining();
        if (training == null) return existingIds;
        for (DataTraining entry : training.getAllTraining()) {
            if (entry != null) existingIds.add(entry.getId());
        }
        return existingIds;
    }

    private static void addTrainingIfMissing(CharData character, DataQuery dq, int trainingId, Set<Integer> existingIds, Map<Integer, DataTraining> trainingById) {
        if (existingIds.contains(trainingId)) return;
        DataTraining tech = trainingById.computeIfAbsent(trainingId, dq::getTrainingById);
        if (tech == null) return;
        DataTraining clone = new DataTraining(tech);
        clone.setRank(0);
        character.getTraining().addTraining(clone);
        existingIds.add(trainingId);
    }

    private static void createInitialBackups(File source, int idx) {
        File backupDir = new File(BACKUP_DIR);
        if (!ensureDirectory(backupDir, "Failed to create backup directory")) {
            return;
        }

        try {
            copyFile(source, new File(getBackupPath(idx, 1)));
            copyFile(source, new File(getBackupPath(idx, 2)));
        } catch (Exception e) {
            System.err.println("Error creating initial backups for character " + idx + ": " + e.getMessage());
        }
    }

    private static void createInitialAutos(File source, int idx) {
        File autoDir = new File(AUTO_DIR);
        if (!ensureDirectory(autoDir, "Failed to create auto directory")) {
            return;
        }

        try {
            copyFile(source, new File(getAutoPath(idx, 1)));
            copyFile(source, new File(getAutoPath(idx, 2)));
            copyFile(source, new File(getAutoPath(idx, 3)));
        } catch (Exception e) {
            System.err.println("Error creating initial autos for character " + idx + ": " + e.getMessage());
        }
    }

    private static void copyFile(File source, File target) throws Exception {
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean ensureDirectory(File dir, String errorContext) {
        if (dir.exists()) return true;
        if (dir.mkdirs()) return true;
        System.err.println(errorContext + " " + dir.getPath());
        return false;
    }

    private static CharData loadCharacterFromFile(File file, String description) {
        try {
            if (!file.exists()) {
                return null;
            }
            return mapper.readValue(file, CharData.class);
        } catch (Exception e) {
            System.err.println("Error loading " + description + ": " + e.getMessage());
            return null;
        }
    }

    private static void rotateBackupsForManualSave(int idx, File currentMainFile) {
        File backupDir = new File(BACKUP_DIR);
        if (!ensureDirectory(backupDir, "Failed to create backup directory")) {
            return;
        }

        File backup1 = new File(getBackupPath(idx, 1));
        File backup2 = new File(getBackupPath(idx, 2));

        try {
            // Backup1 becomes Backup2
            if (backup1.exists()) {
                Files.move(backup1.toPath(), backup2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // Current main file becomes Backup1 before overwrite
            if (currentMainFile.exists()) {
                Files.move(currentMainFile.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("Error rotating backups for character " + idx + ": " + e.getMessage());
        }
    }

    private static StoreChar resolveStoreEntry(StoreChar entry) {
        if (entry == null) return null;
        String reference = entry.getReference();
        if (reference != null && !reference.isBlank() && new File(reference).exists()) {
            return entry;
        }
        return buildLatestStoreEntry(entry.getIndex(), entry);
    }

    private static StoreChar prepareStoreEntryForSave(StoreChar entry) {
        if (entry == null) return null;

        String reference = entry.getReference();
        if (reference != null && !reference.isBlank() && new File(reference).exists()) {
            return entry;
        }

        File mainFile = new File(getCharacterPath(entry.getIndex()));
        if (mainFile.exists()) {
            entry.setReference(mainFile.getPath());
            return entry;
        }

        return buildLatestStoreEntry(entry.getIndex(), entry);
    }

    /** Lightweight container for extracting only identity metadata from JSON, avoiding full CharData deserialization. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CharMetadataOnly {
        public CharIdentity identity;
    }

    /** Builds StoreChar metadata/reference from the newest snapshot among main, backup, and auto files. */
    private static StoreChar buildLatestStoreEntry(int idx, StoreChar fallback) {
        File latest = getLatestSnapshotFile(idx);
        if (latest == null) {
            System.err.println("Missing character snapshots for id " + idx + " - removing entry from store.");
            return null;
        }

        try {
            // Deserialize only the identity field to avoid expensive full CharData deserialization
            CharMetadataOnly metadata = mapper.readValue(latest, CharMetadataOnly.class);
            CharIdentity id = metadata != null ? metadata.identity : null;
            if (id == null) {
                if (fallback != null) fallback.setReference(latest.getPath());
                return fallback;
            }
            Timestamp updated = id.getUpdated() != null ? id.getUpdated() : new Timestamp(latest.lastModified());
            return new StoreChar(
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
        File latest = null;
        long latestTs = Long.MIN_VALUE;
        latest = newerSnapshot(new File(getCharacterPath(idx)), latest, latestTs);
        if (latest != null) latestTs = latest.lastModified();
        latest = newerSnapshot(new File(getBackupPath(idx, 1)), latest, latestTs);
        if (latest != null) latestTs = latest.lastModified();
        latest = newerSnapshot(new File(getBackupPath(idx, 2)), latest, latestTs);
        if (latest != null) latestTs = latest.lastModified();
        latest = newerSnapshot(new File(getAutoPath(idx, 1)), latest, latestTs);
        if (latest != null) latestTs = latest.lastModified();
        latest = newerSnapshot(new File(getAutoPath(idx, 2)), latest, latestTs);
        if (latest != null) latestTs = latest.lastModified();
        latest = newerSnapshot(new File(getAutoPath(idx, 3)), latest, latestTs);
        return latest;
    }

    private static File newerSnapshot(File candidate, File currentLatest, long currentLatestTs) {
        if (!candidate.exists()) return currentLatest;
        long candidateTs = candidate.lastModified();
        if (currentLatest == null || candidateTs > currentLatestTs) {
            return candidate;
        }
        return currentLatest;
    }
}
