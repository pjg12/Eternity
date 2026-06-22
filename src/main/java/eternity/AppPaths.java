package eternity;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class AppPaths {
	private static final String APP_SUBDIR = "app";
	private static final String APP_JAR_NAME = "EternityTTRPG.jar";

	private AppPaths() { /* Utility class */ }

	static Path dataDir() {
		return resolveBundledDir("Data");
	}

	static Path charactersDir() {
		return resolveBundledDir("Characters");
	}

	static Path imagesDir() {
		return resolveBundledDir("Images");
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
