package eternity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for loading/saving character metadata (CharStore list)
 */
public class CharacterDataManager {
    private static final String CONFIG_PATH = "c:/Eternity/Eternity/Data/charStore.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ArrayList<CharStore> loadCharStore() {
        ArrayList<CharStore> list = new ArrayList<>();
        try {
            File f = new File(CONFIG_PATH);
            if (f.exists()) {
                CharStore[] array = mapper.readValue(f, CharStore[].class);
                for (CharStore c : array) list.add(c);
            }
        } catch (Exception e) {
            System.err.println("Error loading CharStore config: " + e.getMessage());
        }
        return list;
    }

    public static void saveCharStore(List<CharStore> list) {
        try {
            File f = new File(CONFIG_PATH);
            mapper.writerWithDefaultPrettyPrinter().writeValue(f, list);
        } catch (Exception e) {
            System.err.println("Error saving CharStore config: " + e.getMessage());
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
}
