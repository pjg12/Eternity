package launcher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

final class LauncherUpdateService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private LauncherUpdateService() { /* Utility class */ }

    static void updateInstalledApp(LauncherUpdateChecker.UpdateInfo updateInfo, Listener listener) throws Exception {
        Path appJar = LauncherPaths.installedAppJarPath();
        if (appJar == null) {
            throw new IOException("Unable to resolve installed application jar");
        }

        Path parentDir = appJar.getParent();
        if (parentDir == null) {
            throw new IOException("Unable to resolve installed application directory");
        }
        Files.createDirectories(parentDir);

        Path stagedJar = Files.createTempFile("eternity-launcher-update-", ".jar");
        listener.onStatus("Downloading Eternity " + updateInfo.latestVersion() + "...");
        downloadTo(updateInfo.artifactUrl(), stagedJar);

        if (updateInfo.sha256() != null && !updateInfo.sha256().isBlank()) {
            listener.onStatus("Verifying downloaded version...");
            verifySha256(stagedJar, updateInfo.sha256());
        }

        listener.onStatus("Installing Eternity " + updateInfo.latestVersion() + "...");
        Files.move(stagedJar, appJar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static void downloadTo(String url, Path destination) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Accept", "application/java-archive, application/octet-stream, */*")
            .header("User-Agent", "EternityLauncher")
            .GET()
            .build();

        HttpResponse<Path> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(destination));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Update download responded with " + response.statusCode());
        }
    }

    private static void verifySha256(Path file, String expectedHash) throws Exception {
        String expected = expectedHash.trim().toLowerCase();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] actualBytes = digest.digest(Files.readAllBytes(file));
        StringBuilder actual = new StringBuilder();
        for (byte current : actualBytes) {
            actual.append(String.format("%02x", current));
        }
        if (!actual.toString().equals(expected)) {
            throw new IOException("Downloaded update checksum did not match");
        }
    }

    interface Listener {
        void onStatus(String text);
    }
}
