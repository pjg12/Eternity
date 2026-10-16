package launcher;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

final class LauncherFrame extends JFrame {
    private static final int FRAME_WIDTH = 560;
    private static final int FRAME_HEIGHT = 320;
    private static final Preferences PREFS =
        Preferences.userNodeForPackage(LauncherFrame.class);

    private final JLabel titleLabel;
    private final JLabel launcherVersionLabel;
    private final JLabel installedVersionLabel;
    private final JLabel latestVersionLabel;
    private final JLabel statusLabel;
    private final JButton launchButton;

    LauncherFrame() {
        super("Eternity Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        root.add(content, BorderLayout.CENTER);

        titleLabel = new JLabel("Eternity Launcher", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        launcherVersionLabel = new JLabel("Launcher Version: " + displayKnownValue(LauncherPaths.readLauncherVersion()));
        launcherVersionLabel.setAlignmentX(CENTER_ALIGNMENT);

        installedVersionLabel = new JLabel("Installed Version: Detecting...");
        installedVersionLabel.setAlignmentX(CENTER_ALIGNMENT);

        latestVersionLabel = new JLabel("Latest Version: Detecting...");
        latestVersionLabel.setAlignmentX(CENTER_ALIGNMENT);

        statusLabel = new JLabel("Update Status: Checking for updates...");
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        launchButton = new JButton("Launch");
        launchButton.setEnabled(false);
        launchButton.setAlignmentX(CENTER_ALIGNMENT);
        launchButton.setPreferredSize(new Dimension(160, 32));
        launchButton.addActionListener(e -> launchEternity());

        JButton characterFilesButton = new JButton("Character Files");
        characterFilesButton.addActionListener(e ->
            openUserFolder(LauncherPaths.installedCharactersDir(), "Character Files"));

        JButton imageFilesButton = new JButton("Image Files");
        imageFilesButton.addActionListener(e ->
            openUserFolder(LauncherPaths.installedImagesDir(), "Image Files"));

        JButton extensionFilesButton = new JButton("Extensions");
        extensionFilesButton.addActionListener(e ->
            openUserFolder(LauncherPaths.installedExtensionsDir(), "Extensions"));

        JPanel folderButtons = new JPanel();
        folderButtons.setLayout(new BoxLayout(folderButtons, BoxLayout.X_AXIS));
        folderButtons.setAlignmentX(CENTER_ALIGNMENT);
        folderButtons.setOpaque(false);
        folderButtons.add(characterFilesButton);
        folderButtons.add(Box.createHorizontalStrut(12));
        folderButtons.add(imageFilesButton);
        folderButtons.add(Box.createHorizontalStrut(12));
        folderButtons.add(extensionFilesButton);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(18));
        content.add(launcherVersionLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(installedVersionLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(latestVersionLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(statusLabel);
        content.add(Box.createVerticalGlue());
        content.add(launchButton);
        content.add(Box.createVerticalStrut(10));
        content.add(folderButtons);
    }

    void beginUpdateFlow() {
        setLaunchEnabled(false);

        CompletableFuture
            .supplyAsync(this::checkAndUpdate)
            .whenComplete((result, error) -> SwingUtilities.invokeLater(() -> {
                if (error != null) {
                    String installedVersion = LauncherPaths.readInstalledAppVersion();

                    installedVersionLabel.setText("Installed Version: " + displayVersion(installedVersion));
                    latestVersionLabel.setText("Latest Version: Unknown");
                    setStatusText("Update check failed");

                    if (!installedVersion.isBlank()) {
                        setLaunchEnabled(true);
                    }

                    JOptionPane.showMessageDialog(
                        this,
                        buildUpdateErrorMessage(error),
                        "Launcher Update Failed",
                        JOptionPane.ERROR_MESSAGE);

                    return;
                }

                installedVersionLabel.setText("Installed Version: " + displayVersion(result.installedVersion));
                latestVersionLabel.setText("Latest Version: " + displayKnownValue(result.latestVersion));
                setStatusText(result.status);
                setLaunchEnabled(result.launchAvailable);

                if (result.installationMessage != null && !result.installationMessage.isBlank()) {
                    showInstallationCompleteDialog(result.installationMessage, result.installLocation);
                }
                if (result.extensionsUpdated) {
                    showExtensionReloadDialog(result.latestVersion);
                }
            }));
    }

    private UpdateResult checkAndUpdate() {
        setStatusTextSafe("Checking local installation...");
        LauncherUpdateService.restoreBackupIfNecessary();
        try {
            setStatusTextSafe("Installing bundled application if newer...");
            LauncherUpdateService.syncBundledAppIfNewer();
            setStatusTextSafe("Preparing user data folders...");
            LauncherUpdateService.seedUserDataDirectories();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to prepare the local Eternity application files.", ex);
        }

        String installedVersion = LauncherPaths.readInstalledAppVersion();
        String updateSource = LauncherConfig.getConfiguredUpdateSource();

        if (updateSource.isBlank()) {
            if (installedVersion.isBlank()) {
                return new UpdateResult(
                    installedVersion,
                    "",
                    "No update source configured and no installed version found.",
                    false,
                    null,
                    null,
                    false);
            }

            return new UpdateResult(
                installedVersion,
                "",
                "No update source configured. Launching installed version only.",
                true,
                resolveInstallationMessage(installedVersion),
                LauncherPaths.installedAppRootDir(),
                false);
        }

        setStatusTextSafe("Checking GitHub releases...");

        LauncherUpdateChecker.UpdateInfo updateInfo =
            LauncherUpdateChecker.fetchLatestUpdate(installedVersion.isBlank() ? "0" : installedVersion);

        String latestVersion = updateInfo.latestVersion();
        boolean appMissing = installedVersion.isBlank();
        boolean updateAvailable = LauncherUpdateChecker.isUpdateAvailable(updateInfo);

        if (!updateInfo.artifactUrl().isBlank() && (appMissing || updateAvailable)) {
            try {
                LauncherUpdateService.updateInstalledApp(updateInfo, this::setStatusTextSafe);
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }

            installedVersion = LauncherPaths.readInstalledAppVersion();

            if (installedVersion.isBlank() || !LauncherPaths.hasInstalledApp()) {
                return new UpdateResult(
                    installedVersion,
                    latestVersion,
                    "Download finished, but the installed application jar could not be verified.",
                    false,
                    null,
                    null,
                    syncExtensionsSafe(updateInfo));
            }

            String status = appMissing
                ? "Installed latest version. Ready to launch."
                : "Update installed. Ready to launch.";

            return new UpdateResult(
                installedVersion,
                latestVersion,
                status,
                true,
                resolveInstallationMessage(installedVersion),
                LauncherPaths.installedAppRootDir(),
                syncExtensionsSafe(updateInfo));
        }

        if (installedVersion.isBlank()) {
            return new UpdateResult(
                installedVersion,
                latestVersion,
                "No installed version available to launch.",
                false,
                null,
                null,
                syncExtensionsSafe(updateInfo));
        }

        return new UpdateResult(
            installedVersion,
            latestVersion,
            "No update available. Ready to launch.",
            true,
            resolveInstallationMessage(installedVersion),
            LauncherPaths.installedAppRootDir(),
            syncExtensionsSafe(updateInfo));
    }

    private boolean syncExtensionsSafe(LauncherUpdateChecker.UpdateInfo updateInfo) {
        if (updateInfo == null || !updateInfo.hasExtensionAsset()) {
            return false;
        }
        try {
            return LauncherUpdateService.syncBrowserExtensions(updateInfo, this::setStatusTextSafe);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void openUserFolder(Path folder, String title) {
        try {
            Files.createDirectories(folder);
            Path toOpen = folder.toAbsolutePath().normalize();

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(toOpen.toFile());
                return;
            }

            if (LauncherPaths.isWindows()) {
                new ProcessBuilder("explorer.exe", toOpen.toString()).start();
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "Automatic folder opening is not supported on this system.\n\n" + toOpen,
                title,
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to open " + title.toLowerCase() + ".\n\n" + ex.getMessage(),
                title,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void launchEternity() {
        LauncherPaths.LaunchSpec launchSpec = LauncherPaths.launchSpec();

        if (launchSpec == null) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to launch Eternity.\n\n"
                    + LauncherPaths.diagnoseLaunchFailure()
                    + "\n\n"
                    + "Expected application jar:\n"
                    + LauncherPaths.installedAppJarPath()
                    + "\n\n"
                    + LauncherPaths.debugPaths(),
                "Launch Failed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(buildCommand(launchSpec));

            if (launchSpec.workingDirectory() != null) {
                builder.directory(launchSpec.workingDirectory().toFile());
            }

            builder.start();
            closeLauncher();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage() + "\n\n" + LauncherPaths.debugPaths(),
                "Launch Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private java.util.List<String> buildCommand(LauncherPaths.LaunchSpec launchSpec) {
        java.util.ArrayList<String> command = new java.util.ArrayList<>();
        command.add(launchSpec.executable());
        command.addAll(launchSpec.arguments());
        return command;
    }

    private void setLaunchEnabled(boolean enabled) {
        launchButton.setEnabled(enabled);
    }

    private void setStatusText(String text) {
        statusLabel.setText("Update Status: " + text);
    }

    private void setStatusTextSafe(String text) {
        SwingUtilities.invokeLater(() -> setStatusText(text));
    }

    private void closeLauncher() {
        setVisible(false);
        dispose();
        System.exit(0);
    }

    private void showExtensionReloadDialog(String version) {
        String versionLabel = displayKnownValue(version);
        Path extensionsDir = LauncherPaths.installedExtensionsDir();
        String message = "Browser extensions were staged for Eternity " + versionLabel + ".\n\n"
            + "The launcher cannot load them into the browser. Reload or load unpacked from:\n"
            + extensionsDir.toAbsolutePath().normalize()
            + "\n\nChrome: chrome://extensions → Developer mode → Load unpacked (or Reload)\n"
            + "and select the chrome folder.\n\n"
            + "Firefox: about:debugging → This Firefox → Load Temporary Add-on\n"
            + "and select a file in the firefox folder.";

        String[] options = { "Open Extensions Folder", "OK" };
        int selection = JOptionPane.showOptionDialog(
            this,
            message,
            "Reload Browser Extensions",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (selection == 0) {
            openUserFolder(extensionsDir, "Extensions");
        }
    }

    private void showInstallationCompleteDialog(String message, Path installLocation) {
        String[] options = { "Open App Files Location", "OK" };

        int selection = JOptionPane.showOptionDialog(
            this,
            message,
            "Installation Complete",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[1]);

        if (selection == 0 && installLocation != null) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(installLocation.toFile());
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Automatic folder opening is not supported on this system.",
                        "Open App Files Location",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Open App Files Location Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String resolveInstallationMessage(String installedVersion) {
        Path installPath = LauncherPaths.installedAppRootDir();

        if (installPath == null || installedVersion == null || installedVersion.isBlank()) {
            return null;
        }

        String normalizedPath = installPath.toAbsolutePath().normalize().toString();
        String preferenceKey = "installMessageShown:" + normalizedPath + ":" + installedVersion;

        if (PREFS.getBoolean(preferenceKey, false)) {
            return null;
        }

        PREFS.putBoolean(preferenceKey, true);

        return "Installation completed successfully.\n\n"
            + "Eternity app files location:\n"
            + normalizedPath
            + "\n\nImages:\n"
            + LauncherPaths.installedImagesDir().toAbsolutePath().normalize()
            + "\n\nCharacters:\n"
            + LauncherPaths.installedCharactersDir().toAbsolutePath().normalize()
            + "\n\nExtensions:\n"
            + LauncherPaths.installedExtensionsDir().toAbsolutePath().normalize();
    }

    private static String buildUpdateErrorMessage(Throwable error) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        String message = cause.getMessage();

        if (message == null || message.isBlank()) {
            message = cause.toString();
        }

        return message + "\n\n" + LauncherPaths.debugPaths();
    }

    private static String displayVersion(String version) {
        return version == null || version.isBlank() ? "Not Installed" : version;
    }

    private static String displayKnownValue(String value) {
        return value == null || value.isBlank() ? "Unknown" : value;
    }

    private static final class UpdateResult {
        private final String installedVersion;
        private final String latestVersion;
        private final String status;
        private final boolean launchAvailable;
        private final String installationMessage;
        private final Path installLocation;
        private final boolean extensionsUpdated;

        private UpdateResult(
            String installedVersion,
            String latestVersion,
            String status,
            boolean launchAvailable,
            String installationMessage,
            Path installLocation,
            boolean extensionsUpdated) {

            this.installedVersion = installedVersion;
            this.latestVersion = latestVersion;
            this.status = status;
            this.launchAvailable = launchAvailable;
            this.installationMessage = installationMessage;
            this.installLocation = installLocation;
            this.extensionsUpdated = extensionsUpdated;
        }
    }
}
