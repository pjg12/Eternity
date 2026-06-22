package eternity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class AppVersion {
    private static final String RESOURCE_PATH = "/eternity-app.properties";
    private static final Properties PROPERTIES = loadProperties();

    private AppVersion() { /* Utility class */ }

    static String getCurrentVersion() {
        return getProperty("app.version", "dev");
    }

    static boolean hasConfiguredUpdateUrl() {
        return !getConfiguredUpdateSource().isBlank();
    }

    static String getConfiguredUpdateUrl() {
        String systemProperty = System.getProperty("eternity.update.url");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envValue = System.getenv("ETERNITY_UPDATE_URL");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return getProperty("update.url", "");
    }

    static String getConfiguredGitHubRepo() {
        String systemProperty = System.getProperty("eternity.update.github.repo");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envValue = System.getenv("ETERNITY_UPDATE_GITHUB_REPO");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return getProperty("update.github.repo", "");
    }

    static String getConfiguredGitHubLatestReleaseUrl() {
        String systemProperty = System.getProperty("eternity.update.github.latestReleaseUrl");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envValue = System.getenv("ETERNITY_UPDATE_GITHUB_LATEST_RELEASE_URL");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return getProperty("update.github.latestReleaseUrl", "");
    }

    static String getConfiguredGitHubAssetName() {
        String systemProperty = System.getProperty("eternity.update.github.assetName");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envValue = System.getenv("ETERNITY_UPDATE_GITHUB_ASSET_NAME");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return getProperty("update.github.assetName", "EternityTTRPG.jar");
    }

    static String getConfiguredGitHubSha256AssetName() {
        String systemProperty = System.getProperty("eternity.update.github.sha256AssetName");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envValue = System.getenv("ETERNITY_UPDATE_GITHUB_SHA256_ASSET_NAME");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return getProperty("update.github.sha256AssetName", "EternityTTRPG.jar.sha256");
    }

    static String getConfiguredUpdateSource() {
        String githubLatestReleaseUrl = getConfiguredGitHubLatestReleaseUrl();
        if (!githubLatestReleaseUrl.isBlank()) {
            return githubLatestReleaseUrl;
        }

        String githubRepo = getConfiguredGitHubRepo();
        if (!githubRepo.isBlank()) {
            return githubRepo;
        }

        return getConfiguredUpdateUrl();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = AppVersion.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
            // Fall back to defaults when the properties file is unavailable.
        }
        return properties;
    }

    private static String getProperty(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
