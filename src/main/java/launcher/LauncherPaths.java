package launcher;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class LauncherPaths {
    private static final String APP_SUBDIR = "app";
    private static final String APP_JAR_NAME = "EternityTTRPG.jar";
    private static final String APP_LAUNCHER_NAME = "EternityTTRPG";

    private LauncherPaths() { /* Utility class */ }

    static Path installedAppJarPath() {
        Path payloadDir = installedPayloadDir();
        if (payloadDir != null) {
            Path candidate = payloadDir.resolve(APP_JAR_NAME);
            if (candidate.toFile().exists()) {
                return candidate;
            }
        }

        Path launcherJarPath = currentLauncherJarPath();
        if (launcherJarPath != null) {
            Path candidate = launcherJarPath.getParent().resolve(APP_JAR_NAME);
            if (candidate.toFile().exists()) {
                return candidate;
            }
        }

        Path workingCopy = Paths.get(System.getProperty("user.dir")).resolve(APP_JAR_NAME);
        if (workingCopy.toFile().exists()) {
            return workingCopy;
        }

        return payloadDir == null ? null : payloadDir.resolve(APP_JAR_NAME);
    }

    static Path installedAppRootDir() {
        String launcherPath = System.getProperty("jpackage.app-path");
        if (launcherPath == null || launcherPath.isBlank()) {
            Path launcherJarPath = currentLauncherJarPath();
            if (launcherJarPath != null) {
                Path launcherJarDir = launcherJarPath.getParent();
                if (launcherJarDir != null) {
                    return launcherJarDir.getParent() == null ? launcherJarDir : launcherJarDir.getParent();
                }
            }
            return Paths.get(System.getProperty("user.dir"));
        }

        Path launcherDir = Paths.get(launcherPath).getParent();
        if (launcherDir == null) {
            return Paths.get(System.getProperty("user.dir"));
        }

        Path siblingAppDir = launcherDir.resolve(APP_SUBDIR);
        if (siblingAppDir.toFile().exists()) {
            return launcherDir;
        }

        Path parentDir = launcherDir.getParent();
        if (parentDir != null) {
            Path parentAppDir = parentDir.resolve(APP_SUBDIR);
            if (parentAppDir.toFile().exists()) {
                return parentDir;
            }
        }

        return launcherDir;
    }

    static LaunchSpec launchSpec() {
        Path appLauncher = installedAppLauncherPath();
        if (appLauncher != null) {
            return new LaunchSpec(appLauncher.toString(), List.of(), installedAppRootDir());
        }

        Path appJar = installedAppJarPath();
        if (appJar == null) {
            return null;
        }

        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isBlank()) {
            return null;
        }

        Path javaBinary = Paths.get(javaHome, "bin", isWindows() ? "javaw.exe" : "java");
        if (!javaBinary.toFile().exists()) {
            Path fallback = Paths.get(javaHome, "bin", "java");
            if (!fallback.toFile().exists()) {
                return null;
            }
            javaBinary = fallback;
        }

        List<String> arguments = new ArrayList<>();
        String launcherPath = System.getProperty("jpackage.app-path");
        if (launcherPath != null && !launcherPath.isBlank()) {
            arguments.add("-Djpackage.app-path=" + launcherPath);
        }
        arguments.add("-jar");
        arguments.add(appJar.toString());
        return new LaunchSpec(javaBinary.toString(), arguments, installedAppRootDir());
    }

    private static Path installedAppLauncherPath() {
        Path rootDir = installedAppRootDir();
        if (rootDir != null) {
            Path candidate = rootDir.resolve(platformLauncherFileName(APP_LAUNCHER_NAME));
            if (candidate.toFile().exists()) {
                return candidate;
            }
        }

        Path workingCopy = Paths.get(System.getProperty("user.dir")).resolve(platformLauncherFileName(APP_LAUNCHER_NAME));
        if (workingCopy.toFile().exists()) {
            return workingCopy;
        }

        return null;
    }

    private static Path installedPayloadDir() {
        Path rootDir = installedAppRootDir();
        if (rootDir == null) {
            return null;
        }
        Path payloadDir = rootDir.resolve(APP_SUBDIR);
        if (payloadDir.toFile().exists()) {
            return payloadDir;
        }

        Path launcherJarPath = currentLauncherJarPath();
        if (launcherJarPath != null) {
            Path launcherJarDir = launcherJarPath.getParent();
            if (launcherJarDir != null && launcherJarDir.toFile().exists()) {
                return launcherJarDir;
            }
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
            if (path.toString().toLowerCase().endsWith(".jar") && path.toFile().exists()) {
                return path;
            }
        } catch (Exception ignored) {
            // Fall through to null when code source is unavailable.
        }
        return null;
    }

    static String readInstalledAppVersion() {
        Path appJar = installedAppJarPath();
        if (appJar == null || !appJar.toFile().exists()) {
            return "";
        }
        return JarVersionReader.readVersion(appJar);
    }

    static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static String platformLauncherFileName(String baseName) {
        return isWindows() ? baseName + ".exe" : baseName;
    }

    record LaunchSpec(String executable, List<String> arguments, Path workingDirectory) { }
}
