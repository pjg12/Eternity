package eternity;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for loading/saving character metadata (StoreMetaChar list)
 */
public class StoreMetaManager {
    // Strings
    private static final String CHARACTER_DIR = "Characters";

    // JSON Mappers
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /*
    *   Constructor
    */
    private StoreMetaManager() { /* No Code Needed */ }

    public static ArrayList<StoreMetaChar> loadCharStore() {
        ArrayList<StoreMetaChar> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\d+\\.json$");
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(CHARACTER_DIR))) {
            StoreMetaChar readIn;
            for (Path path : stream) {
                if (!Files.isRegularFile(path) || !pattern.matcher(path.getFileName().toString()).matches()) {
                    continue;
                }
                readIn = new StoreMetaChar();
                JsonNode nextFile = mapper.readTree(new File(path.toString()));
                readIn.setIndex(nextFile.get("identity").get("index").asInt());
                readIn.setName(nextFile.get("identity").get("name").asText());
                readIn.setCampaign(nextFile.get("identity").get("campaign").asText());
                readIn.setRace(nextFile.get("identity").get("race").asText());
                readIn.setCharClass(nextFile.get("identity").get("charClass").asText());
                readIn.setLevel(nextFile.get("identity").get("level").asInt());
                list.add(readIn);
            }
        } 
        catch (IOException e) { e.printStackTrace(); }

        sortCharStoreByTime(list);
        return list;
    }

    public static void sortCharStoreByTime(List<StoreMetaChar> list) {
        list.sort(Comparator.comparing(StoreMetaChar::getUpdated).reversed());
    }

    public static void sortCharStoreByIndex(List<StoreMetaChar> list) {
        list.sort(Comparator.comparing(StoreMetaChar::getIndex));
    }

    /**
     * Returns the smallest positive integer index not currently used by the given StoreMetaChar list.
     */
    public static int getNextFreeIndex(List<StoreMetaChar> list) {
        sortCharStoreByIndex(list);

        for (int i = 1; i <= list.size(); i++) {
            if (list.get(i - 1).getIndex() != i) {
                return i;
            }
        }

        return list.size() + 1;
    }
}
