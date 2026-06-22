package launcher;

import java.awt.Desktop;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
    private static final int FRAME_WIDTH = 520;
    private static final int FRAME_HEIGHT = 230;
    private static final Preferences PREFS =
        Preferences.userNodeForPackage(LauncherFrame.class);

    private final JLabel titleLabel;
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

        installedVersionLabel = new JLabel("Installed Version: Detecting...");
        installedVersionLabel.setAlignmentX(CENTER_ALIGNMENT);
        latestVersionLabel = new JLabel("Release Version: Detecting...");
        latestVersionLabel.setAlignmentX(CENTER_ALIGNMENT);
        statusLabel = new JLabel("Checking for updates...");
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        launchButton = new JButton("Launch");
        launchButton.setEnabled(false);
        launchButton.setAlignmentX(CENTER_ALIGNMENT);
        launchButton.setPreferredSize(new Dimension(160, 32));
        launchButton.addActionListener(e -> launchEternity());

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(18));
        content.add(installedVersionLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(latestVersionLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(statusLabel);
        content.add(Box.createVerticalGlue());
        content.add(launchButton);
    }

    void beginUpdateFlow() {
        setLaunchEnabled(false);
        CompletableFuture
            .supplyAsync(this::checkAndUpdate)
            .whenComplete((result, error) -> SwingUtilities.invokeLater(() -> {
                if (error != null) {
                    String installedVersion = LauncherPaths.readInstalledAppVersion();
                    installedVersionLabel.setText("Installed Version: " + displayVersion(installedVersion));
                    latestVersionLabel.setText("Release Version: Unknown");
                    statusLabel.setText("Update check failed");
                    if (!installedVersion.isBlank()) {
                        setLaunchEnabled(true);
                    }
                    JOptionPane.showMessageDialog(
                        this,
                        error.getCause() == null ? error.getMessage() : error.getCause().getMessage(),
                        "Launcher Update Failed",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                installedVersionLabel.setText("Installed Version: " + displayVersion(result.installedVersion));
                latestVersionLabel.setText("Release Version: " + displayVersion(result.latestVersion));
                statusLabel.setText(result.status);
                setLaunchEnabled(result.launchAvailable);
                if (result.installationMessage != null && !result.installationMessage.isBlank()) {
                    showInstallationCompleteDialog(result.installationMessage, result.installLocation);
                }
            }));
    }

    private UpdateResult checkAndUpdate() {
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
                    null);
            }

            return new UpdateResult(
                installedVersion,
                "",
                "No update source configured. Launching installed version only.",
                true,
                resolveInstallationMessage(installedVersion),
                LauncherPaths.installedAppRootDir());
        }

        LauncherUpdateChecker.UpdateInfo updateInfo = LauncherUpdateChecker.fetchLatestUpdate(installedVersion.isBlank() ? "0" : installedVersion);

        String latestVersion = updateInfo.latestVersion();
        if (!updateInfo.artifactUrl().isBlank() && (installedVersion.isBlank() || LauncherUpdateChecker.isUpdateAvailable(updateInfo))) {
            try {
                LauncherUpdateService.updateInstalledApp(updateInfo, this::setStatusTextSafe);
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
            installedVersion = LauncherPaths.readInstalledAppVersion();
            return new UpdateResult(
                installedVersion,
                latestVersion,
                "Update complete. Ready to launch.",
                true,
                resolveInstallationMessage(installedVersion),
                LauncherPaths.installedAppRootDir());
        }

        if (installedVersion.isBlank()) {
            return new UpdateResult(installedVersion, latestVersion, "No installed version available to launch.", false, null, null);
        }

        return new UpdateResult(
            installedVersion,
            latestVersion,
            "Eternity is up to date. Ready to launch.",
            true,
            resolveInstallationMessage(installedVersion),
            LauncherPaths.installedAppRootDir());
    }

    private void launchEternity() {
        LauncherPaths.LaunchSpec launchSpec = LauncherPaths.launchSpec();
        if (launchSpec == null) {
            JOptionPane.showMessageDialog(this, "Unable to locate the Eternity application jar.", "Launch Failed", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Launch Failed", JOptionPane.ERROR_MESSAGE);
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

    private void setStatusTextSafe(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    private void closeLauncher() {
        setVisible(false);
        dispose();
        System.exit(0);
    }

    private void showInstallationCompleteDialog(String message, Path installLocation) {
        String[] options = { "Open Install Location", "OK" };
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
                        "Open Install Location",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Open Install Location Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String resolveInstallationMessage(String installedVersion) {
        java.nio.file.Path installPath = LauncherPaths.installedAppRootDir();
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
            + "Default install location:\n"
            + normalizedPath;
    }

    private static String displayVersion(String version) {
        return version == null || version.isBlank() ? "Not Installed" : version;
    }

    private static final class UpdateResult {
        private final String installedVersion;
        private final String latestVersion;
        private final String status;
        private final boolean launchAvailable;
        private final String installationMessage;
        private final Path installLocation;

        private UpdateResult(
            String installedVersion,
            String latestVersion,
            String status,
            boolean launchAvailable,
            String installationMessage,
            Path installLocation) {
            this.installedVersion = installedVersion;
            this.latestVersion = latestVersion;
            this.status = status;
            this.launchAvailable = launchAvailable;
            this.installationMessage = installationMessage;
            this.installLocation = installLocation;
        }
    }
}
