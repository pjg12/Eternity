package launcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

final class JarVersionReader {
    private static final String VERSION_ENTRY = "eternity-app.properties";

    private JarVersionReader() { /* Utility class */ }

    static String readVersion(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            ZipEntry entry = jarFile.getEntry(VERSION_ENTRY);
            if (entry == null) {
                return "";
            }

            try (InputStream stream = jarFile.getInputStream(entry)) {
                Properties properties = new Properties();
                properties.load(stream);
                String version = properties.getProperty("app.version", "");
                return version == null ? "" : version.trim();
            }
        } catch (IOException ignored) {
            return "";
        }
    }
}
