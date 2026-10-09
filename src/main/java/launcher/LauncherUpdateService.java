package launcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class LauncherUpdateService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private LauncherUpdateService() {
        // Utility class.
    }

    static void restoreBackupIfNecessary() {
        Path appJar = LauncherPaths.installedAppJarPath();
        Path backupJar = LauncherPaths.installedAppBackupJarPath();

        try {
            if (!Files.isRegularFile(backupJar)) {
                return;
            }

            String backupVersion = JarVersionReader.readVersion(backupJar);

            if (backupVersion.isBlank()) {
                return;
            }

            boolean appMissing = !Files.exists(appJar);

            boolean appUnreadable =
                Files.exists(appJar)
                    && JarVersionReader.readVersion(appJar).isBlank();

            if (appMissing || appUnreadable) {
                Path parentDir = appJar.getParent();

                if (parentDir != null) {
                    Files.createDirectories(parentDir);
                }

                moveFile(backupJar, appJar);
            }
        } catch (Exception ignored) {
            // Best effort recovery only.
        }
    }

    static void updateInstalledApp(
            LauncherUpdateChecker.UpdateInfo updateInfo,
            Listener listener) throws Exception {

        Path appJar = LauncherPaths.installedAppJarPath();
        Path backupJar = LauncherPaths.installedAppBackupJarPath();
        Path stagedJar = LauncherPaths.stagedUpdateJarPath();
        Path stagedImagesZip = LauncherPaths.stagedImagesZipPath();

        Path appDir = appJar.getParent();
        Path stagedDir = stagedJar.getParent();

        if (appDir == null) {
            throw new IOException("Unable to resolve installed application directory.");
        }

        if (stagedDir == null) {
            throw new IOException("Unable to resolve update staging directory.");
        }

        Files.createDirectories(appDir);
        Files.createDirectories(stagedDir);
        Files.deleteIfExists(stagedJar);
        Files.deleteIfExists(stagedImagesZip);

        listener.onStatus("Downloading Eternity " + updateInfo.latestVersion() + "...");
        downloadTo(updateInfo.artifactUrl(), stagedJar);

        if (updateInfo.sha256() != null && !updateInfo.sha256().isBlank()) {
            listener.onStatus("Verifying downloaded version...");
            verifySha256(stagedJar, updateInfo.sha256());
        }

        boolean hasImagesAsset = updateInfo.hasImagesAsset();
        if (hasImagesAsset) {
            listener.onStatus("Downloading Images update...");
            downloadTo(updateInfo.imagesUrl(), stagedImagesZip);

            if (updateInfo.imagesSha256() != null && !updateInfo.imagesSha256().isBlank()) {
                listener.onStatus("Verifying Images update...");
                verifySha256(stagedImagesZip, updateInfo.imagesSha256());
            }
        }

        listener.onStatus("Installing Eternity " + updateInfo.latestVersion() + "...");
        installStagedJar(stagedJar, appJar, backupJar);

        if (hasImagesAsset) {
            listener.onStatus("Updating Images...");
            installImagesZip(stagedImagesZip);
            Files.deleteIfExists(stagedImagesZip);
        } else {
            seedUserDataDirectories();
        }
    }

    /**
     * Copies the installer-bundled game jar into %LOCALAPPDATA%\\Eternity\\app
     * when that jar is missing or older than the bundled version.
     */
    static void syncBundledAppIfNewer() throws IOException {
        Path bundledJar = LauncherPaths.bundledAppJarPath();
        if (bundledJar == null || !Files.isRegularFile(bundledJar)) {
            return;
        }

        Path appJar = LauncherPaths.installedAppJarPath();
        Path backupJar = LauncherPaths.installedAppBackupJarPath();
        Path bundledFull = bundledJar.toAbsolutePath().normalize();
        Path appFull = appJar.toAbsolutePath().normalize();
        if (bundledFull.equals(appFull)) {
            return;
        }

        String bundledVersion = JarVersionReader.readVersion(bundledJar);
        boolean appMissing = !Files.isRegularFile(appJar);
        if (!appMissing) {
            if (bundledVersion.isBlank()) {
                return;
            }
            String installedVersion = JarVersionReader.readVersion(appJar);
            if (LauncherUpdateChecker.compareVersions(bundledVersion, installedVersion) <= 0) {
                return;
            }
        }

        Path appDir = appJar.getParent();
        if (appDir == null) {
            throw new IOException("Unable to resolve installed application directory.");
        }
        Files.createDirectories(appDir);

        boolean existingJarBackedUp = false;
        try {
            if (Files.isRegularFile(appJar)) {
                Files.deleteIfExists(backupJar);
                moveFile(appJar, backupJar);
                existingJarBackedUp = true;
            }
            Files.copy(bundledJar, appJar, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            if (existingJarBackedUp && !Files.exists(appJar) && Files.exists(backupJar)) {
                try {
                    moveFile(backupJar, appJar);
                } catch (IOException restoreError) {
                    ex.addSuppressed(restoreError);
                }
            }
            throw ex;
        }
    }

    /**
     * Creates writable LOCALAPPDATA user-data folders and seeds Images from the
     * installer payload when those folders are missing. Existing files are left alone.
     */
    static void seedUserDataDirectories() throws IOException {
        Path charactersDir = LauncherPaths.installedCharactersDir();
        Files.createDirectories(charactersDir.resolve("Auto"));
        Files.createDirectories(charactersDir.resolve("Backup"));
        Path extensionsDir = LauncherPaths.installedExtensionsDir();
        Files.createDirectories(extensionsDir);
        writeExtensionsReadme(extensionsDir);

        Path targetImagesDir = LauncherPaths.installedImagesDir();
        if (hasAnyFiles(targetImagesDir)) {
            return;
        }

        Path bundledZip = LauncherPaths.bundledImagesZipPath();
        if (bundledZip != null && Files.isRegularFile(bundledZip)) {
            mergeZipIntoDirectory(bundledZip, targetImagesDir);
            return;
        }

        Path bundledImagesDir = LauncherPaths.bundledImagesDir();
        if (bundledImagesDir != null && Files.isDirectory(bundledImagesDir)) {
            copyDirectory(bundledImagesDir, targetImagesDir);
        } else {
            Files.createDirectories(targetImagesDir);
        }
    }

    /**
     * Downloads matching Chrome/Firefox extension zips from the release when present
     * and unpacks them under LOCALAPPDATA\\Eternity\\extensions. Missing assets are skipped.
     */
    static boolean syncBrowserExtensions(
            LauncherUpdateChecker.UpdateInfo updateInfo,
            Listener listener) {
        if (updateInfo == null || !updateInfo.hasExtensionAsset()) {
            return false;
        }

        String targetVersion = updateInfo.latestVersion() == null ? "" : updateInfo.latestVersion().trim();
        if (targetVersion.isBlank()) {
            return false;
        }

        try {
            Path extensionsDir = LauncherPaths.installedExtensionsDir();
            Files.createDirectories(extensionsDir);
            writeExtensionsReadme(extensionsDir);
            Files.createDirectories(LauncherPaths.updateStagingDir());
        } catch (IOException ignored) {
            return false;
        }

        boolean versionMatches = extensionsAlreadyStaged(targetVersion);
        boolean needChrome = updateInfo.hasChromeExtensionAsset()
            && (!versionMatches || !hasExtensionManifest(LauncherPaths.installedChromeExtensionDir()));
        boolean needFirefox = updateInfo.hasFirefoxExtensionAsset()
            && (!versionMatches || !hasExtensionManifest(LauncherPaths.installedFirefoxExtensionDir()));
        if (!needChrome && !needFirefox) {
            return false;
        }

        boolean chromeUpdated = false;
        boolean firefoxUpdated = false;

        if (needChrome) {
            if (listener != null) {
                listener.onStatus("Updating Chrome extension...");
            }
            chromeUpdated = installExtensionZip(
                updateInfo.chromeExtensionUrl(),
                updateInfo.chromeExtensionSha256(),
                LauncherPaths.stagedChromeExtensionZipPath(),
                LauncherPaths.installedChromeExtensionDir());
        }

        if (needFirefox) {
            if (listener != null) {
                listener.onStatus("Updating Firefox extension...");
            }
            firefoxUpdated = installExtensionZip(
                updateInfo.firefoxExtensionUrl(),
                updateInfo.firefoxExtensionSha256(),
                LauncherPaths.stagedFirefoxExtensionZipPath(),
                LauncherPaths.installedFirefoxExtensionDir());
        }

        boolean updated = chromeUpdated || firefoxUpdated;
        if (updated) {
            writeStagedExtensionVersion(targetVersion);
        }
        return updated;
    }

    private static boolean extensionsAlreadyStaged(String targetVersion) {
        Path versionFile = LauncherPaths.stagedExtensionVersionPath();
        if (!Files.isRegularFile(versionFile)) {
            return false;
        }
        try {
            String staged = Files.readString(versionFile).trim();
            return !staged.isBlank() && LauncherUpdateChecker.compareVersions(targetVersion, staged) == 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean hasExtensionManifest(Path extensionDir) {
        return extensionDir != null && Files.isRegularFile(extensionDir.resolve("manifest.json"));
    }

    private static void writeStagedExtensionVersion(String version) {
        try {
            Files.createDirectories(LauncherPaths.installedExtensionsDir());
            Files.writeString(LauncherPaths.stagedExtensionVersionPath(), version == null ? "" : version.trim());
        } catch (Exception ignored) {
            // Best effort; the unpacked files are still usable.
        }
    }

    private static boolean installExtensionZip(
            String url,
            String sha256,
            Path stagedZip,
            Path targetDir) {
        if (url == null || url.isBlank() || stagedZip == null || targetDir == null) {
            return false;
        }

        try {
            Files.deleteIfExists(stagedZip);
            downloadTo(url, stagedZip);
            if (sha256 != null && !sha256.isBlank()) {
                verifySha256(stagedZip, sha256);
            }
            replaceDirectoryFromZip(stagedZip, targetDir);
            Files.deleteIfExists(stagedZip);
            return hasExtensionManifest(targetDir);
        } catch (Exception ignored) {
            try {
                Files.deleteIfExists(stagedZip);
            } catch (Exception suppressed) {
                // Best effort cleanup.
            }
            return false;
        }
    }

    private static void replaceDirectoryFromZip(Path zipFile, Path targetDir) throws IOException {
        Path tempDir = Files.createTempDirectory("eternity-ext-");
        try {
            extractZipRaw(zipFile, tempDir);
            Path source = resolveExtensionRoot(tempDir);
            deleteDirectory(targetDir);
            copyDirectory(source, targetDir);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private static void extractZipRaw(Path zipFile, Path targetDir) throws IOException {
        Path normalizedTarget = targetDir.toAbsolutePath().normalize();
        Files.createDirectories(normalizedTarget);

        try (InputStream fileStream = Files.newInputStream(zipFile);
             ZipInputStream zipStream = new ZipInputStream(fileStream)) {

            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                try {
                    String relativePath = sanitizeZipEntryPath(entry.getName());
                    if (relativePath == null || relativePath.isBlank()) {
                        continue;
                    }

                    Path destination = normalizedTarget.resolve(relativePath).normalize();
                    if (!destination.startsWith(normalizedTarget)) {
                        throw new IOException("Blocked zip entry outside extension directory: " + entry.getName());
                    }

                    if (entry.isDirectory() || relativePath.endsWith("/")) {
                        Files.createDirectories(destination);
                        continue;
                    }

                    Path parent = destination.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(zipStream, destination, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    zipStream.closeEntry();
                }
            }
        }
    }

    private static Path resolveExtensionRoot(Path extracted) throws IOException {
        if (extracted == null) {
            return null;
        }
        if (Files.isRegularFile(extracted.resolve("manifest.json"))) {
            return extracted;
        }
        try (var stream = Files.list(extracted)) {
            java.util.List<Path> directories = stream.filter(Files::isDirectory).toList();
            if (directories.size() == 1 && Files.isRegularFile(directories.get(0).resolve("manifest.json"))) {
                return directories.get(0);
            }
        }
        return extracted;
    }

    private static String sanitizeZipEntryPath(String rawName) {
        if (rawName == null) {
            return null;
        }
        String name = rawName.replace('\\', '/').trim();
        while (name.startsWith("./")) {
            name = name.substring(2);
        }
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (name.isBlank() || name.contains(":")) {
            return null;
        }
        return name;
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void writeExtensionsReadme(Path extensionsDir) {
        Path readme = extensionsDir.resolve("README.txt");
        if (Files.exists(readme)) {
            return;
        }
        try {
            Files.writeString(readme,
                "Roll20 browser extensions are unpacked here when the launcher finds matching zips on the release.\n\n"
                    + "chrome\\  Chrome: chrome://extensions → Developer mode → Load unpacked (or Reload)\n"
                    + "firefox\\ Firefox: about:debugging → This Firefox → Load Temporary Add-on\n");
        } catch (Exception ignored) {
            // Best effort helper file only.
        }
    }

    private static boolean hasAnyFiles(Path directory) throws IOException {
        if (directory == null || !Files.isDirectory(directory)) {
            return false;
        }
        try (var stream = Files.walk(directory)) {
            return stream.anyMatch(Files::isRegularFile);
        }
    }

    private static void installStagedJar(
            Path stagedJar,
            Path appJar,
            Path backupJar) throws IOException {

        boolean existingJarBackedUp = false;

        try {
            if (Files.isRegularFile(appJar)) {
                Files.deleteIfExists(backupJar);
                moveFile(appJar, backupJar);
                existingJarBackedUp = true;
            }

            moveFile(stagedJar, appJar);
        } catch (IOException ex) {
            if (existingJarBackedUp && !Files.exists(appJar) && Files.exists(backupJar)) {
                try {
                    moveFile(backupJar, appJar);
                } catch (IOException restoreError) {
                    ex.addSuppressed(restoreError);
                }
            }

            throw ex;
        }
    }

    /**
     * Optional Images.zip install: only overwrites files present in the zip.
     * Files not in the zip are left untouched. If the writable Images directory
     * does not exist yet, seed it from the bundled installer Images first.
     */
    private static void installImagesZip(Path stagedImagesZip) throws IOException {
        if (!Files.isRegularFile(stagedImagesZip)) {
            return;
        }

        Path targetImagesDir = LauncherPaths.installedImagesDir();
        if (!Files.isDirectory(targetImagesDir)) {
            Path bundledImagesDir = LauncherPaths.bundledImagesDir();
            if (bundledImagesDir != null && Files.isDirectory(bundledImagesDir)) {
                copyDirectory(bundledImagesDir, targetImagesDir);
            } else {
                Files.createDirectories(targetImagesDir);
            }
        }

        mergeZipIntoDirectory(stagedImagesZip, targetImagesDir);
    }

    private static void mergeZipIntoDirectory(Path zipFile, Path targetDir) throws IOException {
        Path normalizedTarget = targetDir.toAbsolutePath().normalize();
        Files.createDirectories(normalizedTarget);

        try (InputStream fileStream = Files.newInputStream(zipFile);
             ZipInputStream zipStream = new ZipInputStream(fileStream)) {

            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                try {
                    String relativePath = normalizeZipEntryPath(entry.getName());
                    if (relativePath == null || relativePath.isBlank()) {
                        continue;
                    }

                    Path destination = normalizedTarget.resolve(relativePath).normalize();
                    if (!destination.startsWith(normalizedTarget)) {
                        throw new IOException("Blocked zip entry outside Images directory: " + entry.getName());
                    }

                    if (entry.isDirectory() || relativePath.endsWith("/")) {
                        Files.createDirectories(destination);
                        continue;
                    }

                    Path parent = destination.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(zipStream, destination, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    zipStream.closeEntry();
                }
            }
        }
    }

    /**
     * Accepts either flat zip contents or an Images/ root folder.
     * Returns null for skippable entries (blank, Windows drive paths, etc.).
     */
    private static String normalizeZipEntryPath(String rawName) {
        if (rawName == null) {
            return null;
        }

        String name = rawName.replace('\\', '/').trim();
        while (name.startsWith("./")) {
            name = name.substring(2);
        }
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (name.isBlank() || name.contains(":")) {
            return null;
        }

        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.equals("images") || lower.equals("images/")) {
            return null;
        }
        if (lower.startsWith("images/")) {
            name = name.substring("images/".length());
        }

        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name.isBlank() ? null : name;
    }

    private static void copyDirectory(Path source, Path destination) throws IOException {
        Path normalizedSource = source.toAbsolutePath().normalize();
        Path normalizedDestination = destination.toAbsolutePath().normalize();
        Files.createDirectories(normalizedDestination);

        Files.walkFileTree(normalizedSource, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = normalizedSource.relativize(dir);
                Path target = normalizedDestination.resolve(relative.toString());
                Files.createDirectories(target);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = normalizedSource.relativize(file);
                Path target = normalizedDestination.resolve(relative.toString());
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void downloadTo(String url, Path destination) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Accept", "application/java-archive, application/zip, application/octet-stream, */*")
            .header("User-Agent", "EternityLauncher")
            .GET()
            .build();

        HttpResponse<Path> response =
            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(destination));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Update download responded with " + response.statusCode());
        }
    }

    private static void verifySha256(Path file, String expectedHash) throws Exception {
        String expected = expectedHash.trim().toLowerCase(Locale.ROOT);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream stream = Files.newInputStream(file);
             DigestInputStream digestStream = new DigestInputStream(stream, digest)) {

            byte[] buffer = new byte[8192];

            while (digestStream.read(buffer) != -1) {
                // Stream file contents through the digest.
            }
        }

        byte[] actualBytes = digest.digest();

        StringBuilder actual = new StringBuilder();

        for (byte current : actualBytes) {
            actual.append(String.format("%02x", current));
        }

        if (!actual.toString().equals(expected)) {
            throw new IOException("Downloaded update checksum did not match");
        }
    }

    private static void moveFile(Path source, Path destination) throws IOException {
        try {
            Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING);
        }
    }

    interface Listener {
        void onStatus(String text);
    }
}
