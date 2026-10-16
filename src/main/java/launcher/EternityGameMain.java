package launcher;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class EternityGameMain {
    private static URLClassLoader appClassLoader;

    private EternityGameMain() {
        // Utility class.
    }

    public static void main(String[] args) {
        try {
            launchInstalledApp(args);
        } catch (Exception ex) {
            showLaunchFailure(ex);
            System.exit(1);
        }
    }

    private static void launchInstalledApp(String[] args) throws Exception {
        LauncherUpdateService.restoreBackupIfNecessary();
        LauncherUpdateService.syncBundledAppIfNewer();
        LauncherUpdateService.seedUserDataDirectories();

        Path appJar = LauncherPaths.installedAppJarPath();

        if (!Files.isRegularFile(appJar)) {
            throw new IllegalStateException(
                "Installed application jar was not found at:\n" + appJar);
        }

        appClassLoader = new URLClassLoader(
            new URL[] { appJar.toUri().toURL() },
            ClassLoader.getPlatformClassLoader());

        Thread.currentThread().setContextClassLoader(appClassLoader);

        Class<?> mainClass = Class.forName(
            "eternity.EternityMain",
            true,
            appClassLoader);

        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    private static void showLaunchFailure(Exception ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        String message = cause.getMessage();

        if (message == null || message.isBlank()) {
            message = cause.toString();
        }

        String dialogMessage = message
            + "\n\nExpected application jar:\n"
            + LauncherPaths.installedAppJarPath()
            + "\n\n"
            + LauncherPaths.debugPaths();

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
            null,
            dialogMessage,
            "Eternity Launch Failed",
            JOptionPane.ERROR_MESSAGE));
    }
}
