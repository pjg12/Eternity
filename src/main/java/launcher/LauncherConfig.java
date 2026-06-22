package launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class LauncherConfig {
    private static final String RESOURCE_PATH = "/eternity-app.properties";
    private static final Properties PROPERTIES = loadProperties();

    private LauncherConfig() { /* Utility class */ }

    static String getConfiguredUpdateUrl() {
        return readOverride("eternity.update.url", "ETERNITY_UPDATE_URL", "update.url", "");
    }

    static String getConfiguredGitHubRepo() {
        return readOverride("eternity.update.github.repo", "ETERNITY_UPDATE_GITHUB_REPO", "update.github.repo", "");
    }

    static String getConfiguredGitHubLatestReleaseUrl() {
        return readOverride(
            "eternity.update.github.latestReleaseUrl",
            "ETERNITY_UPDATE_GITHUB_LATEST_RELEASE_URL",
            "update.github.latestReleaseUrl",
            "");
    }

    static String getConfiguredGitHubAssetName() {
        return readOverride(
            "eternity.update.github.assetName",
            "ETERNITY_UPDATE_GITHUB_ASSET_NAME",
            "update.github.assetName",
            "EternityTTRPG.jar");
    }

    static String getConfiguredGitHubSha256AssetName() {
        return readOverride(
            "eternity.update.github.sha256AssetName",
            "ETERNITY_UPDATE_GITHUB_SHA256_ASSET_NAME",
            "update.github.sha256AssetName",
            "EternityTTRPG.jar.sha256");
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

    private static String readOverride(String systemKey, String envKey, String propertyKey, String defaultValue) {
        String systemProperty = System.getProperty(systemKey);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String propertyValue = PROPERTIES.getProperty(propertyKey);
        if (propertyValue == null || propertyValue.isBlank()) {
            return defaultValue;
        }
        return propertyValue.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = LauncherConfig.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
            // Fall back to defaults when unavailable.
        }
        return properties;
    }
}
