package launcher;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

final class LauncherPaths {
    private static final String LAUNCHER_SUBDIR = "launcher";
    private static final String LAUNCHER_JAR_NAME = "EternityLauncher.jar";
    private static final String GAME_LAUNCHER_NAME = "EternityGame";
    private static final String RUNTIME_SUBDIR = "runtime";

    private static final String APP_SUBDIR = "app";
    private static final String APP_JAR_NAME = "EternityTTRPG.jar";
    private static final String APP_JAR_BACKUP_NAME = APP_JAR_NAME + ".old";
    private static final String IMAGES_SUBDIR = "Images";
    private static final String CHARACTERS_SUBDIR = "Characters";
    private static final String EXTENSIONS_SUBDIR = "extensions";
    private static final String IMAGES_ZIP_NAME = "Images.zip";
    private static final String STAGED_CHROME_EXTENSION_ZIP_NAME = "roll20-bridge-chrome.zip.new";
    private static final String STAGED_FIREFOX_EXTENSION_ZIP_NAME = "roll20-bridge-firefox.zip.new";
    private static final String EXTENSION_VERSION_FILE = "staged-version.txt";

    private static final String UPDATES_SUBDIR = "updates";
    private static final String STAGED_APP_JAR_NAME = APP_JAR_NAME + ".new";
    private static final String STAGED_IMAGES_ZIP_NAME = IMAGES_ZIP_NAME + ".new";

    private static final String VERSION_RESOURCE = "/eternity-app.properties";

    private LauncherPaths() {
        // Utility class.
    }

    static Path installedAppRootDir() {
        String override = System.getProperty("eternity.app.root");

        if (override != null && !override.isBlank()) {
            return Paths.get(override)
                .toAbsolutePath()
                .normalize();
        }

        String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, "Eternity")
                .toAbsolutePath()
                .normalize();
        }

        return Paths.get(System.getProperty("user.home"), ".eternity")
            .toAbsolutePath()
            .normalize();
    }

    static Path installedLauncherJarPath() {
        return installedAppRootDir()
            .resolve(LAUNCHER_SUBDIR)
            .resolve(LAUNCHER_JAR_NAME);
    }

    static Path installedAppJarPath() {
        return installedAppRootDir()
            .resolve(APP_SUBDIR)
            .resolve(APP_JAR_NAME);
    }

    static Path installedAppBackupJarPath() {
        return installedAppRootDir()
            .resolve(APP_SUBDIR)
            .resolve(APP_JAR_BACKUP_NAME);
    }

    static Path updateStagingDir() {
        return installedAppRootDir()
            .resolve(UPDATES_SUBDIR);
    }

    static Path stagedUpdateJarPath() {
        return updateStagingDir()
            .resolve(STAGED_APP_JAR_NAME);
    }

    static Path stagedImagesZipPath() {
        return updateStagingDir()
            .resolve(STAGED_IMAGES_ZIP_NAME);
    }

    /** Writable Images directory under the user app root (LOCALAPPDATA on Windows). */
    static Path installedImagesDir() {
        return installedAppRootDir()
            .resolve(IMAGES_SUBDIR);
    }

    /** Writable Characters directory under the user app root (LOCALAPPDATA on Windows). */
    static Path installedCharactersDir() {
        return installedAppRootDir()
            .resolve(CHARACTERS_SUBDIR);
    }

    /** Unpacked browser extensions under the user app root (LOCALAPPDATA on Windows). */
    static Path installedExtensionsDir() {
        return installedAppRootDir()
            .resolve(EXTENSIONS_SUBDIR);
    }

    static Path installedChromeExtensionDir() {
        return installedExtensionsDir().resolve("chrome");
    }

    static Path installedFirefoxExtensionDir() {
        return installedExtensionsDir().resolve("firefox");
    }

    static Path stagedChromeExtensionZipPath() {
        return updateStagingDir().resolve(STAGED_CHROME_EXTENSION_ZIP_NAME);
    }

    static Path stagedFirefoxExtensionZipPath() {
        return updateStagingDir().resolve(STAGED_FIREFOX_EXTENSION_ZIP_NAME);
    }

    static Path stagedExtensionVersionPath() {
        return installedExtensionsDir().resolve(EXTENSION_VERSION_FILE);
    }

    /** Game jar shipped with the installer, used to refresh LOCALAPPDATA after a new install. */
    static Path bundledAppJarPath() {
        Path executableDir = launcherExecutableDir();
        if (executableDir == null) {
            return null;
        }

        Path[] candidates = {
            executableDir.resolve(APP_SUBDIR).resolve(APP_SUBDIR).resolve(APP_JAR_NAME),
            executableDir.resolve(APP_SUBDIR).resolve(APP_JAR_NAME),
            executableDir.resolve(APP_JAR_NAME)
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        Path parent = executableDir.getParent();
        if (parent != null) {
            Path parentAppJar = parent.resolve(APP_SUBDIR).resolve(APP_SUBDIR).resolve(APP_JAR_NAME);
            if (Files.isRegularFile(parentAppJar)) {
                return parentAppJar;
            }
        }
        return null;
    }

    /** Seed Images.zip shipped with the installer payload, if present. */
    static Path bundledImagesZipPath() {
        Path executableDir = launcherExecutableDir();
        if (executableDir == null) {
            return null;
        }

        Path[] candidates = {
            executableDir.resolve(APP_SUBDIR).resolve(IMAGES_ZIP_NAME),
            executableDir.resolve(APP_SUBDIR).resolve(APP_SUBDIR).resolve(IMAGES_ZIP_NAME),
            executableDir.resolve(IMAGES_ZIP_NAME)
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        Path parent = executableDir.getParent();
        if (parent != null) {
            Path parentZip = parent.resolve(APP_SUBDIR).resolve(IMAGES_ZIP_NAME);
            if (Files.isRegularFile(parentZip)) {
                return parentZip;
            }
        }
        return null;
    }

    /**
     * Baseline Images shipped with the packaged installer, when resolvable from jpackage.app-path.
     * Used only to seed the writable Images directory before the first Images.zip merge.
     */
    static Path bundledImagesDir() {
        Path executableDir = launcherExecutableDir();
        if (executableDir == null) {
            return null;
        }

        Path siblingAppImages = executableDir.resolve(APP_SUBDIR).resolve(IMAGES_SUBDIR);
        if (Files.isDirectory(siblingAppImages)) {
            return siblingAppImages;
        }

        Path siblingImages = executableDir.resolve(IMAGES_SUBDIR);
        if (Files.isDirectory(siblingImages)) {
            return siblingImages;
        }

        Path parent = executableDir.getParent();
        if (parent != null) {
            Path parentAppImages = parent.resolve(APP_SUBDIR).resolve(IMAGES_SUBDIR);
            if (Files.isDirectory(parentAppImages)) {
                return parentAppImages;
            }
        }

        return null;
    }

    static boolean hasInstalledApp() {
        return Files.isRegularFile(installedAppJarPath());
    }

    static LaunchSpec launchSpec() {
        Path appJar = installedAppJarPath();

        if (!Files.isRegularFile(appJar)) {
            return null;
        }

        Path gameLauncher = installedGameLauncherPath();

        if (Files.isRegularFile(gameLauncher)) {
            return new LaunchSpec(
                gameLauncher.toString(),
                List.of(),
                launcherExecutableDir());
        }

        Path javaBinary = resolveJavaBinary();

        if (javaBinary == null) {
            return null;
        }

        List<String> arguments = new ArrayList<>();

        String launcherExecutablePath = System.getProperty("jpackage.app-path");

        if (launcherExecutablePath != null && !launcherExecutablePath.isBlank()) {
            arguments.add("-Djpackage.app-path=" + launcherExecutablePath);
        }

        arguments.add("-jar");
        arguments.add(appJar.toString());

        return new LaunchSpec(
            javaBinary.toString(),
            List.copyOf(arguments),
            installedAppRootDir());
    }

    static String diagnoseLaunchFailure() {
        Path appJar = installedAppJarPath();

        if (!Files.isRegularFile(appJar)) {
            return "Installed application jar was not found.";
        }

        if (!Files.isRegularFile(installedGameLauncherPath()) && resolveJavaBinary() == null) {
            return "No game launcher executable or Java runtime was found for launch.";
        }

        if (!Files.isRegularFile(installedGameLauncherPath())) {
            return "The packaged EternityGame launcher executable was not found.";
        }

        if (resolveJavaBinary() == null) {
            return "No Java runtime was found for launch. The launcher could not resolve javaw.exe from java.home or the bundled runtime.";
        }

        return "The launcher could not prepare the game launch command.";
    }

    static String readInstalledAppVersion() {
        Path appJar = installedAppJarPath();

        if (!Files.isRegularFile(appJar)) {
            return "";
        }

        return JarVersionReader.readVersion(appJar);
    }

    static String readLauncherVersion() {
        Properties properties = new Properties();

        try (InputStream stream = LauncherPaths.class.getResourceAsStream(VERSION_RESOURCE)) {
            if (stream == null) {
                return "";
            }

            properties.load(stream);

            String launcherVersion = properties.getProperty("launcher.version", "");

            if (launcherVersion != null && !launcherVersion.isBlank()) {
                return launcherVersion.trim();
            }

            String legacyVersion = properties.getProperty("app.version", "");

            return legacyVersion == null ? "" : legacyVersion.trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    static String debugPaths() {
        return "App root: " + installedAppRootDir()
            + "\nGame launcher: " + installedGameLauncherPath()
            + "\nApp jar: " + installedAppJarPath()
            + "\nBackup jar: " + installedAppBackupJarPath()
            + "\nStaged jar: " + stagedUpdateJarPath()
            + "\nImages: " + installedImagesDir()
            + "\nCharacters: " + installedCharactersDir()
            + "\nExtensions: " + installedExtensionsDir()
            + "\nBundled app jar: " + bundledAppJarPath()
            + "\nBundled images: " + bundledImagesDir()
            + "\nBundled images zip: " + bundledImagesZipPath()
            + "\nStaged images zip: " + stagedImagesZipPath()
            + "\nJava: " + resolveJavaBinary();
    }

    static boolean isWindows() {
        return System.getProperty("os.name", "")
            .toLowerCase()
            .contains("win");
    }

    private static Path resolveJavaBinary() {
        Path javaHomeBinary = javaBinaryFromJavaHome();

        if (javaHomeBinary != null) {
            return javaHomeBinary;
        }

        Path bundledBinary = bundledRuntimeJavaBinary();

        if (bundledBinary != null) {
            return bundledBinary;
        }

        return null;
    }

    private static Path currentLauncherJarPath() {
        try {
            URI location = LauncherPaths.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI();

            Path path = Paths.get(location);

            if (path.toString().toLowerCase().endsWith(".jar") && Files.exists(path)) {
                return path.toAbsolutePath().normalize();
            }
        } catch (Exception ignored) {
            // Fall through to null when code source is unavailable.
        }

        return null;
    }

    private static Path installedGameLauncherPath() {
        Path executableDir = launcherExecutableDir();

        if (executableDir == null) {
            return Paths.get(
                System.getProperty("user.dir"),
                platformLauncherName(GAME_LAUNCHER_NAME))
                .toAbsolutePath()
                .normalize();
        }

        return executableDir.resolve(platformLauncherName(GAME_LAUNCHER_NAME));
    }

    private static Path launcherExecutableDir() {
        String launcherExecutablePath = System.getProperty("jpackage.app-path");

        if (launcherExecutablePath != null && !launcherExecutablePath.isBlank()) {
            Path executablePath = Paths.get(launcherExecutablePath).toAbsolutePath().normalize();
            Path parent = executablePath.getParent();

            if (parent != null) {
                return parent;
            }
        }

        return null;
    }

    private static String platformLauncherName(String baseName) {
        if (isWindows()) {
            return baseName + ".exe";
        }
        return baseName;
    }

    private static Path javaBinaryFromJavaHome() {
        String javaHome = System.getProperty("java.home");

        if (javaHome == null || javaHome.isBlank()) {
            return null;
        }

        Path preferred = Paths.get(
            javaHome,
            "bin",
            isWindows() ? "javaw.exe" : "java");

        if (Files.isRegularFile(preferred)) {
            return preferred;
        }

        Path fallback = Paths.get(javaHome, "bin", "java");

        return Files.isRegularFile(fallback) ? fallback : null;
    }

    private static Path bundledRuntimeJavaBinary() {
        String launcherExecutablePath = System.getProperty("jpackage.app-path");

        if (launcherExecutablePath == null || launcherExecutablePath.isBlank()) {
            return null;
        }

        Path executablePath = Paths.get(launcherExecutablePath).toAbsolutePath().normalize();
        Path launcherDir = executablePath.getParent();

        if (launcherDir == null) {
            return null;
        }

        Path preferred = launcherDir
            .resolve(RUNTIME_SUBDIR)
            .resolve("bin")
            .resolve(isWindows() ? "javaw.exe" : "java");

        if (Files.isRegularFile(preferred)) {
            return preferred;
        }

        Path fallback = launcherDir
            .resolve(RUNTIME_SUBDIR)
            .resolve("bin")
            .resolve("java.exe");

        if (Files.isRegularFile(fallback)) {
            return fallback;
        }

        Path genericFallback = launcherDir
            .resolve(RUNTIME_SUBDIR)
            .resolve("bin")
            .resolve("java");

        return Files.isRegularFile(genericFallback) ? genericFallback : null;
    }

    record LaunchSpec(String executable, List<String> arguments, Path workingDirectory) { }
}
