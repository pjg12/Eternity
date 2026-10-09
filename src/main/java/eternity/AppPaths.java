package eternity;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class AppPaths {
	private static final String APP_SUBDIR = "app";
	private static final String APP_JAR_NAME = "EternityTTRPG.jar";
	private static final String IMAGES_SUBDIR = "Images";
	private static final String CHARACTERS_SUBDIR = "Characters";

	private AppPaths() { /* Utility class */ }

	static Path dataDir() {
		return resolveBundledDir("Data");
	}

	static Path charactersDir() {
		if (usesUserDataDirs()) {
			return userDataRootDir().resolve(CHARACTERS_SUBDIR);
		}
		return resolveBundledDir(CHARACTERS_SUBDIR);
	}

	static Path imagesDir() {
		if (usesUserDataDirs()) {
			return userDataRootDir().resolve(IMAGES_SUBDIR);
		}
		Path userImages = userDataImagesDir();
		if (userImages != null && userImages.toFile().isDirectory()) {
			return userImages;
		}
		return resolveBundledDir(IMAGES_SUBDIR);
	}

	private static boolean usesUserDataDirs() {
		return launcherExecutablePath() != null && userDataRootDir() != null;
	}

	static boolean isInstalledApp() {
		return installedAppRootDir() != null && currentAppJarPath() != null && launcherExecutablePath() != null;
	}

	static Path currentAppJarPath() {
		Path payloadDir = installedPayloadDir();
		if (payloadDir != null) {
			Path candidate = payloadDir.resolve(APP_JAR_NAME);
			if (candidate.toFile().exists()) {
				return candidate;
			}
		}
		return null;
	}

	static Path launcherExecutablePath() {
		String launcherPath = System.getProperty("jpackage.app-path");
		if (launcherPath == null || launcherPath.isBlank()) {
			return null;
		}
		Path path = Paths.get(launcherPath);
		return path.toFile().exists() ? path : null;
	}

	static RelaunchSpec relaunchSpec() {
		Path launcherPath = launcherExecutablePath();
		if (launcherPath != null) {
			return new RelaunchSpec(launcherPath.toString(), List.of());
		}

		String javaBinary = System.getProperty("java.home");
		if (javaBinary == null || javaBinary.isBlank()) {
			return null;
		}

		Path jarPath = currentExecutableJarPath();
		if (jarPath == null) {
			return null;
		}

		Path javaw = Paths.get(javaBinary, "bin", isWindows() ? "javaw.exe" : "java");
		if (!javaw.toFile().exists()) {
			Path java = Paths.get(javaBinary, "bin", "java");
			if (!java.toFile().exists()) {
				return null;
			}
			javaw = java;
		}

		List<String> arguments = new ArrayList<>();
		arguments.add("-jar");
		arguments.add(jarPath.toString());
		return new RelaunchSpec(javaw.toString(), arguments);
	}

	private static Path userDataImagesDir() {
		Path userRoot = userDataRootDir();
		return userRoot == null ? null : userRoot.resolve(IMAGES_SUBDIR);
	}

	/** Writable user app root used by the launcher (%LOCALAPPDATA%\Eternity on Windows). */
	private static Path userDataRootDir() {
		String override = System.getProperty("eternity.app.root");
		if (override != null && !override.isBlank()) {
			return Paths.get(override).toAbsolutePath().normalize();
		}

		String localAppData = System.getenv("LOCALAPPDATA");
		if (localAppData != null && !localAppData.isBlank()) {
			return Paths.get(localAppData, "Eternity").toAbsolutePath().normalize();
		}

		String userHome = System.getProperty("user.home");
		if (userHome != null && !userHome.isBlank()) {
			return Paths.get(userHome, ".eternity").toAbsolutePath().normalize();
		}
		return null;
	}

	private static Path resolveBundledDir(String dirName) {
		Path payloadDir = installedPayloadDir();
		if (payloadDir != null) {
			Path candidate = payloadDir.resolve(dirName);
			if (candidate.toFile().exists()) {
				return candidate;
			}
		}

		Path appRoot = installedAppRootDir();
		if (appRoot != null) {
			Path candidate = appRoot.resolve(dirName);
			if (candidate.toFile().exists()) {
				return candidate;
			}
		}

		return Paths.get(System.getProperty("user.dir")).resolve(dirName);
	}

	private static Path installedPayloadDir() {
		Path appRoot = installedAppRootDir();
		if (appRoot == null) return null;
		Path payloadDir = appRoot.resolve(APP_SUBDIR);
		return payloadDir.toFile().exists() ? payloadDir : null;
	}

	private static Path installedAppRootDir() {
		String launcherPath = System.getProperty("jpackage.app-path");
		if (launcherPath == null || launcherPath.isBlank()) {
			return null;
		}
		Path launcherDir = Paths.get(launcherPath).getParent();
		if (launcherDir == null) {
			return null;
		}

		Path siblingAppDir = launcherDir.resolve("app");
		if (siblingAppDir.toFile().exists()) {
			return siblingAppDir;
		}

		Path parentDir = launcherDir.getParent();
		if (parentDir != null) {
			Path parentAppDir = parentDir.resolve("app");
			if (parentAppDir.toFile().exists()) {
				return parentAppDir;
			}
		}

		return launcherDir;
	}

	private static Path currentExecutableJarPath() {
		String classPath = System.getProperty("java.class.path");
		if (classPath == null || classPath.isBlank()) {
			return null;
		}
		String[] entries = classPath.split(File.pathSeparator);
		if (entries.length != 1) {
			return null;
		}
		Path path = Paths.get(entries[0]);
		if (!path.toString().toLowerCase().endsWith(".jar")) {
			return null;
		}
		return path.toFile().exists() ? path : null;
	}

	private static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase().contains("win");
	}

	record RelaunchSpec(String executable, List<String> arguments) { }
}
