package launcher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

final class LauncherUpdateChecker {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private LauncherUpdateChecker() { /* Utility class */ }

    static UpdateInfo fetchLatestUpdate(String currentVersion) {
        String updateSource = LauncherConfig.getConfiguredUpdateSource();
        if (updateSource.isBlank()) {
            throw new IllegalStateException("No update source configured");
        }

        if (looksLikeGitHubReleaseSource(updateSource)) {
            return fetchGitHubReleaseInfo(currentVersion, updateSource);
        }
        return fetchManifestUpdateInfo(currentVersion, updateSource);
    }

    private static UpdateInfo fetchManifestUpdateInfo(String currentVersion, String updateUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(updateUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .header("User-Agent", "EternityLauncher/" + currentVersion)
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Update server responded with " + response.statusCode());
            }

            UpdateManifest manifest = MAPPER.readValue(response.body(), UpdateManifest.class);
            if (manifest.version == null || manifest.version.isBlank()) {
                throw new IOException("Update manifest did not include a version");
            }

            return new UpdateInfo(
                currentVersion,
                manifest.version.trim(),
                firstNonBlank(manifest.artifactUrl, manifest.jarUrl),
                safeTrim(manifest.downloadUrl),
                safeTrim(manifest.sha256),
                safeTrim(manifest.notes));
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private static UpdateInfo fetchGitHubReleaseInfo(String currentVersion, String updateSource) {
        try {
            String apiUrl = normalizeGitHubLatestReleaseUrl(updateSource);
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2026-03-10")
                .header("User-Agent", "EternityLauncher/" + currentVersion)
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("GitHub release lookup responded with " + response.statusCode());
            }

            GitHubRelease release = MAPPER.readValue(response.body(), GitHubRelease.class);
            String latestVersion = firstNonBlank(release.tag_name, release.name);
            if (latestVersion.isBlank()) {
                throw new IOException("GitHub release did not include a tag name");
            }

            String artifactName = LauncherConfig.getConfiguredGitHubAssetName();
            String shaAssetName = LauncherConfig.getConfiguredGitHubSha256AssetName();
            GitHubReleaseAsset artifact = findAssetByName(release.assets, artifactName);
            if (artifact == null) {
                throw new IOException("GitHub release is missing asset " + artifactName);
            }

            String sha256 = "";
            if (artifact.digest != null && artifact.digest.toLowerCase(Locale.ROOT).startsWith("sha256:")) {
                sha256 = artifact.digest.substring("sha256:".length()).trim();
            }
            if (sha256.isBlank()) {
                GitHubReleaseAsset checksumAsset = findAssetByName(release.assets, shaAssetName);
                if (checksumAsset != null && checksumAsset.browser_download_url != null && !checksumAsset.browser_download_url.isBlank()) {
                    sha256 = downloadChecksumAsset(checksumAsset.browser_download_url, currentVersion);
                }
            }

            return new UpdateInfo(
                currentVersion,
                stripLeadingV(latestVersion.trim()),
                safeTrim(artifact.browser_download_url),
                safeTrim(release.html_url),
                safeTrim(sha256),
                safeTrim(release.body));
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    static boolean isUpdateAvailable(UpdateInfo updateInfo) {
        return compareVersions(updateInfo.latestVersion(), updateInfo.currentVersion()) > 0;
    }

    private static int compareVersions(String left, String right) {
        List<Integer> leftParts = parseVersionParts(left);
        List<Integer> rightParts = parseVersionParts(right);
        int maxParts = Math.max(leftParts.size(), rightParts.size());
        for (int i = 0; i < maxParts; i++) {
            int leftValue = i < leftParts.size() ? leftParts.get(i) : 0;
            int rightValue = i < rightParts.size() ? rightParts.get(i) : 0;
            if (leftValue != rightValue) {
                return Integer.compare(leftValue, rightValue);
            }
        }
        return left.trim().compareToIgnoreCase(right.trim());
    }

    private static List<Integer> parseVersionParts(String version) {
        String[] rawParts = version.trim().split("[^0-9]+");
        List<Integer> parts = new ArrayList<>();
        for (String rawPart : rawParts) {
            if (!rawPart.isBlank()) {
                parts.add(Integer.parseInt(rawPart));
            }
        }
        if (parts.isEmpty()) {
            parts.add(0);
        }
        return parts;
    }

    private static boolean looksLikeGitHubReleaseSource(String updateSource) {
        String source = updateSource == null ? "" : updateSource.trim();
        if (source.isBlank()) {
            return false;
        }
        if (source.matches("^[^/\\s]+/[^/\\s]+$")) {
            return true;
        }
        return source.contains("api.github.com/repos/") || source.contains("github.com/");
    }

    private static String normalizeGitHubLatestReleaseUrl(String updateSource) {
        String source = updateSource.trim();
        if (source.matches("^[^/\\s]+/[^/\\s]+$")) {
            return "https://api.github.com/repos/" + source + "/releases/latest";
        }
        if (source.contains("github.com/") && !source.contains("api.github.com/")) {
            String normalized = source.replace("https://github.com/", "").replace("http://github.com/", "");
            normalized = normalized.replaceAll("/+$", "");
            if (normalized.endsWith("/releases/latest")) {
                normalized = normalized.substring(0, normalized.length() - "/releases/latest".length());
            }
            return "https://api.github.com/repos/" + normalized + "/releases/latest";
        }
        return source;
    }

    private static String stripLeadingV(String version) {
        if (version == null) {
            return "";
        }
        String trimmed = version.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    private static GitHubReleaseAsset findAssetByName(List<GitHubReleaseAsset> assets, String assetName) {
        if (assets == null || assetName == null || assetName.isBlank()) {
            return null;
        }
        for (GitHubReleaseAsset asset : assets) {
            if (asset != null && assetName.equals(asset.name)) {
                return asset;
            }
        }
        return null;
    }

    private static String downloadChecksumAsset(String assetUrl, String currentVersion) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(assetUrl))
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "text/plain, application/octet-stream, */*")
            .header("User-Agent", "EternityLauncher/" + currentVersion)
            .GET()
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Checksum asset download responded with " + response.statusCode());
        }
        String body = safeTrim(response.body());
        if (body.isBlank()) {
            return "";
        }
        String[] parts = body.split("\\s+");
        return parts.length == 0 ? "" : safeTrim(parts[0]);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    record UpdateInfo(
        String currentVersion,
        String latestVersion,
        String artifactUrl,
        String downloadUrl,
        String sha256,
        String notes) {
    }

    private static final class UpdateManifest {
        public String version;
        public String artifactUrl;
        public String jarUrl;
        public String downloadUrl;
        public String sha256;
        public String notes;
    }

    private static final class GitHubRelease {
        public String tag_name;
        public String name;
        public String body;
        public String html_url;
        public List<GitHubReleaseAsset> assets;
    }

    private static final class GitHubReleaseAsset {
        public String name;
        public String browser_download_url;
        public String digest;
    }
}
