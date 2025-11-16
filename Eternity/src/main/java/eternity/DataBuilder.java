package eternity;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

/**
 * Small, focused JSON file loader. No game logic here.
 * Usage: <T> List<T> list = dataStore.loadList("filename.json", Type[].class);
 */
public final class DataBuilder {
    private final Path basePath;
    private final ObjectMapper mapper;

    /**
     * Constructs DataStore with a base directory (e.g., Paths.get("data")).
     * If base does not exist, it will be created.
     */
    public DataBuilder(Path basePath) {
        this.basePath = basePath;
        this.mapper = new ObjectMapper();
        try {
            if (!Files.exists(basePath)) Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create base data directory: " + basePath, e);
        }
    }

    /**
     * Load a JSON array from disk and return a list.
     * Example: loadList("racedata.json", DataRace[].class)
     */
    public <T> List<T> loadList(String filename, Class<T[]> arrayType) {
        Path p = basePath.resolve(filename);
        if (!Files.exists(p)) return List.of(); // empty list if file missing
        try {
            T[] arr = mapper.readValue(p.toFile(), arrayType);
            if (arr == null) return List.of();
            return Arrays.asList(arr);
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + p.toString(), e);
        }
    }

    /**
     * Convenience: read a single object from file (or null if missing).
     */
    public <T> T loadObject(String filename, Class<T> type) {
        Path p = basePath.resolve(filename);
        if (!Files.exists(p)) return null;
        try {
            return mapper.readValue(p.toFile(), type);
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + p.toString(), e);
        }
    }

    public Path getBasePath() {
        return basePath;
    }
}