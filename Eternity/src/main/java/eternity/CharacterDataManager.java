package eternity;

import java.io.File;
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
                    File charFile = new File(CHARACTER_DIR, c.getIndex() + ".json");
                    if (charFile.exists()) {
                        list.add(c);
                    } else {
                        System.err.println("Missing character file for id " + c.getIndex() + " at " + charFile.getPath() + " — removing entry from store.");
                    }
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
        return (store != null) ? loadCharacter(store.getIndex()) : null;
    }

    public static void saveCharStore(List<CharStore> list) {
        try {
            File f = new File(CONFIG_PATH);
            mapper.writerWithDefaultPrettyPrinter().writeValue(f, list);
        } catch (Exception e) {
            System.err.println("Error saving CharStore config: " + e.getMessage());
        }
    }

    /**
     * Saves the full CharData JSON to the Characters directory using its index as filename.
     * Returns true on success, false on failure.
     */
    public static boolean saveCharacter(CharData character) {
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
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(target, character);
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

}
