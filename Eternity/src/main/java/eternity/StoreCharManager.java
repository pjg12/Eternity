// CHECKED

package eternity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for loading/saving character metadata (StoreMetaChar list)
 */
public class StoreCharManager {
    // Strings
    private static final String CHARACTER_DIR = "Characters";
    private static final String MANUAL_DIR = "Characters/Backup";
    private static final String AUTO_DIR = "Characters/Auto";

    // JSON Mappers
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final ObjectWriter PRETTY_WRITER = mapper.writerWithDefaultPrettyPrinter();

    /*
    *   Constructor
    */
    public StoreCharManager() { /* No Code Needed */ }

    // ---- File path helpers ----
    private static String getCharacterPath(int idx) { return CHARACTER_DIR + File.separator + idx + ".json"; }
    private static String getManBackPath(int idx, int backupNum) { return MANUAL_DIR + File.separator + idx + "Backup" + backupNum + ".json"; }
    private static String getAutoBackPath(int idx, int autoNum) { return AUTO_DIR + File.separator + idx + "Auto" + autoNum + ".json"; }

    /**
     * Load the full character JSON for the given id.
     * Returns null if the file is missing or can't be parsed.
     */
    public static StoreCharData loadCharacter(int index) {
        if (index < 1) return null;
        
        File file = new File(getCharacterPath(index));
        try {
            if (!file.exists()) {
                System.err.println("Character file not found for index " + index + ": " + file.getPath());
                return null;
           }
           return mapper.readValue(file, StoreCharData.class);
        } catch (Exception e) {
            System.err.println("Error loading character " + index + ".json: " + e.getMessage());
            return null;
        }

    }

    /**
     * Saves the full StoreCharData JSON to the Characters directory using its index as filename.
     * Returns true on success, false on failure.
     */
    public static boolean saveCharacterNew(StoreCharData character) {
        String backupDirPath = CHARACTER_DIR;
        File backDir = new File(backupDirPath);
        if (!ensureDirectory(backDir, "Cannot backup autosave character: failed to create directory")) {
            return false;
        }
        backupDirPath = AUTO_DIR;
        backDir = new File(backupDirPath);
        if (!ensureDirectory(backDir, "Cannot backup autosave character: failed to create directory")) {
            return false;
        }
        backupDirPath = MANUAL_DIR;
        backDir = new File(backupDirPath);
        if (!ensureDirectory(backDir, "Cannot backup autosave character: failed to create directory")) {
            return false;
        }
        return saveCharacter(character);
    }

    /**
     * Saves character and rotates backups for explicit user-initiated saves:
     * Backup1 -> Backup2, current main file -> Backup1, then write new main file.
     */
    public static boolean saveCharacterManual(StoreCharData character) {
        generateBackups(character, true);
        return saveCharacter(character);
    }

    /**
     * Autosave snapshot rotation:
     * Auto2 -> Auto3, Auto1 -> Auto2, current autosave -> Auto1.
     */
    public static boolean saveCharacterAuto(StoreCharData character) {
        generateBackups(character, false);
        // Finally save the new main file (even if backup rotation failed, to avoid losing progress)
        return saveCharacter(character);
    }

    private static boolean saveCharacter(StoreCharData character) {
        int idx = character.getIdentity().getIndex();
        File dir = new File(CHARACTER_DIR);
        if (!ensureDirectory(dir, "Cannot save character: failed to create directory")) {
            return false;
        }

        File target = new File(getCharacterPath(idx));
        try {
            PRETTY_WRITER.writeValue(target, character);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving character " + idx + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Backup snapshot rotation:
     * back2 -> back3, back1 -> back2, current save -> back1.
     */
    public static void generateBackups(StoreCharData character, boolean isManualSave) {
        if (character == null || character.getIdentity() == null) {
            System.err.println("Cannot generate backups: character or identity is null.");
            return;
        }

        int idx = character.getIdentity().getIndex();
        if (idx < 0) {
            System.err.println("Cannot save character: invalid index " + idx);
            return;
        }

        File main = new File(getCharacterPath(idx));
        File back1, back2, back3;
        if (isManualSave) {
            back1 = new File(getManBackPath(idx, 1));
            back2 = new File(getManBackPath(idx, 2));
            back3 = new File(getManBackPath(idx, 3));
        } else {
            back1 = new File(getAutoBackPath(idx, 1));
            back2 = new File(getAutoBackPath(idx, 2));
            back3 = new File(getAutoBackPath(idx, 3));
        }

        String backupDirPath = isManualSave ? MANUAL_DIR : AUTO_DIR;
        File backDir = new File(backupDirPath);
        if (!ensureDirectory(backDir, "Cannot backup character: failed to create directory")) {
            return;
        }

        try {
            // Rotate older snapshots first
            if (back2.exists()) Files.move(back2.toPath(), back3.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (back1.exists()) Files.move(back1.toPath(), back2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (main.exists()) Files.move(main.toPath(), back1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        } catch (Exception e) {
            System.err.println("Error during autosave backups " + idx + ": " + e.getMessage());
        }
    }

    private static boolean ensureDirectory(File dir, String errorContext) {
        if (dir.exists()) return true;
        if (dir.mkdirs()) return true;
        System.err.println(errorContext + " " + dir.getPath());
        return false;
    }
}
