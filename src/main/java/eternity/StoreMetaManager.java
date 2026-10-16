// CHECKED

package eternity;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for loading/saving character metadata (StoreMetaChar list)
 */
public class StoreMetaManager {
    // Strings
    private static final Path CHARACTER_DIR = AppPaths.charactersDir();

    // JSON Mappers
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final DateTimeFormatter UPDATED_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, true)
            .optionalEnd()
            .appendOffset("+HHMM", "+0000")
            .toFormatter();

    private static ArrayList<StoreMetaChar> charStore;

    /*
    *   Constructor
    */
    public StoreMetaManager() { /* No Code Needed */ }

    public static void loadCharStore() {
        ArrayList<StoreMetaChar> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\d+\\.json$");
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CHARACTER_DIR)) {
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
                JsonNode updatedNode = nextFile.path("identity").path("updated");
                if (!updatedNode.isMissingNode() && !updatedNode.isNull()) {
                    String updatedText = updatedNode.asText();
                    if (updatedText != null && !updatedText.isBlank()) {
                        try {
                            readIn.setUpdated(Timestamp.from(OffsetDateTime.parse(updatedText, UPDATED_FORMATTER).toInstant()));
                        } catch (Exception ignored) {
                            // Leave constructor default timestamp when stored data is malformed.
                        }
                    }
                }
                list.add(readIn);
            }
        } 
        catch (IOException e) { e.printStackTrace(); }

        sortCharStoreByTime(list);
        
        charStore = list;
    }

    public static ArrayList<StoreMetaChar> getCharStore() { return charStore; }
    public static void sortCharStoreByTime(List<StoreMetaChar> list) { list.sort(Comparator.comparing(StoreMetaChar::getUpdated).reversed()); }
    public static void sortCharStoreByIndex(List<StoreMetaChar> list) { list.sort(Comparator.comparing(StoreMetaChar::getIndex)); }

    /**
     * Returns the smallest positive integer index not currently used by the given StoreMetaChar list.
     */
    @JsonIgnore
    public static int getNextFreeIndex(List<StoreMetaChar> list) {
        sortCharStoreByIndex(list);
        for (int i = 1; i <= list.size(); i++) {
            
            if (list.get(i - 1).getIndex() != i) {
                return i;
            }
        }
        return list.size() + 1;
    }

    @JsonIgnore
    public static StoreMetaChar getLastLoad(List<StoreMetaChar> list) {
    	if (list == null || list.isEmpty()) return null;
    	sortCharStoreByTime(list);
        return list.get(0);
    }
}
