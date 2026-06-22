// CHECKED

package eternity;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Welcome screen for the Eternity TTRPG Helper.
 */
public class FrameFirst extends JFrame {

    // References
    private final FrameSheet sheetFrame;
    
    // UI Constants
    private static final int FRAME_WIDTH = 550;
    private static final int FRAME_HEIGHT = 300;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font SUBHEADER_FONT = new Font(null, Font.PLAIN, 17);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int PADDING_TOP_BOTTOM = 25;
    private static final int PADDING_LEFT_RIGHT = 20;
    private static final int SPACING_HEADER = 10;
    private static final int SPACING_STATUS = 10;
    private static final int SPACING_BEFORE_BUTTONS = 15;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_SPACING = 20;
    
    // UI Strings
    private static final String WINDOW_TITLE = "Eternity TTRPG Helper";
    private static final String HEADER_TEXT = "Welcome to the Eternity TTRPG Helper";
    private static final String SUBHEADER_TEXT = "Version %s";
    private static final String STATUS_LOADING = "Character Editor";
    private static final String GAME_HELPER_LABEL = "Game Helper";
    private static final String BUTTON_NEW = "New";
    private static final String BUTTON_LOAD = "Load";
    private static final String BUTTON_PLAYER = "Player";
    private static final String BUTTON_NARRATOR = "Narrator";

    // UI Components
    private JPanel root;
    private JLabel headerL;
    private JLabel subHeaderL;
    private JLabel updateStatusL;
    private JLabel statusL;
    private JLabel helperL;
    private JButton newBtn;
    private JButton loadBtn;
    private JButton playerBtn;
    private JButton narratorBtn;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public FrameFirst(FrameSheet sheetFrame) {
        super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        buildUI();
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT, PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT));

        // Header
        headerL = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subheader
        subHeaderL = new JLabel(String.format(SUBHEADER_TEXT, AppVersion.getCurrentVersion()));
        subHeaderL.setFont(SUBHEADER_FONT);
        subHeaderL.setAlignmentX(Component.CENTER_ALIGNMENT);

        updateStatusL = new JLabel("", SwingConstants.CENTER);
        updateStatusL.setFont(LABEL_FONT);
        updateStatusL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Spacer
        root.add(headerL);
        root.add(Box.createVerticalStrut(SPACING_HEADER));
        root.add(subHeaderL);
        root.add(Box.createVerticalStrut(SPACING_STATUS));
        root.add(updateStatusL);
        root.add(Box.createVerticalStrut(SPACING_STATUS));

        // Character editor label
        statusL = new JLabel(STATUS_LOADING, SwingConstants.CENTER);
        statusL.setFont(LABEL_FONT);
        statusL.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(statusL);
        root.add(Box.createVerticalStrut(SPACING_BEFORE_BUTTONS));

        // Button Setup
        Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);

        // New Button
        newBtn = new JButton(BUTTON_NEW);
        newBtn.setPreferredSize(btnSize);

        // Load Button
        loadBtn = new JButton(BUTTON_LOAD);
        loadBtn.setPreferredSize(btnSize);

        // Add Buttons
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(newBtn);
        buttonPanel.add(Box.createHorizontalStrut(BUTTON_SPACING));
        buttonPanel.add(loadBtn);
        buttonPanel.add(Box.createHorizontalGlue());
        root.add(buttonPanel);
        root.add(Box.createVerticalStrut(SPACING_BEFORE_BUTTONS));

        helperL = new JLabel(GAME_HELPER_LABEL, SwingConstants.CENTER);
        helperL.setFont(LABEL_FONT);
        helperL.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(helperL);
        root.add(Box.createVerticalStrut(SPACING_BEFORE_BUTTONS));

        JPanel helperButtonPanel = new JPanel();
        helperButtonPanel.setLayout(new BoxLayout(helperButtonPanel, BoxLayout.X_AXIS));
        helperButtonPanel.setOpaque(false);

        playerBtn = new JButton(BUTTON_PLAYER);
        playerBtn.setPreferredSize(btnSize);

        narratorBtn = new JButton(BUTTON_NARRATOR);
        narratorBtn.setPreferredSize(btnSize);

        helperButtonPanel.add(Box.createHorizontalGlue());
        helperButtonPanel.add(playerBtn);
        helperButtonPanel.add(Box.createHorizontalStrut(BUTTON_SPACING));
        helperButtonPanel.add(narratorBtn);
        helperButtonPanel.add(Box.createHorizontalGlue());
        root.add(helperButtonPanel);
        root.add(Box.createVerticalGlue());

        // Add root to frame
        add(root);

        // Listeners
        newBtn.addActionListener(e -> onNewPressed());
        loadBtn.addActionListener(e -> onLoadPressed());
        playerBtn.addActionListener(e -> onPlayerPressed());
        narratorBtn.addActionListener(e -> onNarratorPressed());
    }

    public void beginUpdateCheck() {
        UpdateChecker.checkForUpdatesAsync(new UpdateChecker.Listener() {
            @Override
            public void onChecking(String currentVersion) {
                setStatusText("Version " + currentVersion + " | Checking for updates...");
            }

            @Override
            public void onUpToDate(String currentVersion) {
                setStatusText("Version " + currentVersion + " | Up to date");
            }

            @Override
            public void onUpdateAvailable(UpdateChecker.UpdateInfo updateInfo) {
                setStatusText("Version " + updateInfo.currentVersion() + " | Update available: " + updateInfo.latestVersion());
                promptForUpdate(updateInfo);
            }

            @Override
            public void onUnavailable(String currentVersion, String reason) {
                if (AppVersion.hasConfiguredUpdateUrl()) {
                    setStatusText("Version " + currentVersion + " | Update check unavailable");
                } else {
                    setStatusText("Version " + currentVersion);
                }
            }
        });
    }

    private void setStatusText(String text) {
        updateStatusL.setText(text);
    }

    private void setButtonsEnabled(boolean enabled) {
        newBtn.setEnabled(enabled);
        loadBtn.setEnabled(enabled);
        playerBtn.setEnabled(enabled);
        narratorBtn.setEnabled(enabled);
    }

    private void promptForUpdate(UpdateChecker.UpdateInfo updateInfo) {
        StringBuilder message = new StringBuilder();
        message.append("Current version: ").append(updateInfo.currentVersion())
                .append("\nLatest version: ").append(updateInfo.latestVersion());

        if (updateInfo.notes() != null && !updateInfo.notes().isBlank()) {
            message.append("\n\n").append(updateInfo.notes().trim());
        }

        boolean canAutoUpdate = AppUpdater.canAutoUpdate(updateInfo);
        boolean hasDownloadPage = updateInfo.downloadUrl() != null && !updateInfo.downloadUrl().isBlank();
        if (!canAutoUpdate && !hasDownloadPage) {
            JOptionPane.showMessageDialog(this, message.toString(), "Update Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> options = new ArrayList<>();
        if (canAutoUpdate) {
            options.add("Update Now");
        }
        if (hasDownloadPage) {
            options.add("Open Download Page");
        }
        options.add("Later");

        Object selected = JOptionPane.showInputDialog(
                this,
                message.append(canAutoUpdate
                        ? "\n\nChoose how you would like to update."
                        : "\n\nOpen the download page?")
                        .toString(),
                "Update Available",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options.toArray(),
                options.get(0));

        if (selected == null || "Later".equals(selected)) {
            return;
        }

        if ("Update Now".equals(selected)) {
            beginAutomaticUpdate(updateInfo);
        } else if ("Open Download Page".equals(selected)) {
            openDownloadPage(updateInfo.downloadUrl());
        }
    }

    private void beginAutomaticUpdate(UpdateChecker.UpdateInfo updateInfo) {
        setButtonsEnabled(false);
        AppUpdater.installUpdate(updateInfo, new AppUpdater.Listener() {
            @Override
            public void onStatus(String text) {
                javax.swing.SwingUtilities.invokeLater(() -> setStatusText(text));
            }

            @Override
            public void onReadyToExit() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatusText("Restarting to apply update...");
                    dispose();
                    if (sheetFrame != null) {
                        sheetFrame.dispose();
                    }
                    System.exit(0);
                });
            }

            @Override
            public void onFailure(String message) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    setButtonsEnabled(true);
                    setStatusText("Automatic update failed");
                    JOptionPane.showMessageDialog(FrameFirst.this, message, "Update Failed", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void openDownloadPage(String downloadUrl) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create(downloadUrl));
        } catch (Exception ex) {
            setStatusText("Unable to open update page");
        }
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------
    
    private void onNewPressed() {
        sheetFrame.onNewPressed();
        dispose();
    }

    private void onLoadPressed() {
        sheetFrame.onLoadPressed();
        dispose();
    }

    private void onPlayerPressed() {
        if (sheetFrame == null) {
            return;
        }
        if (!sheetFrame.hasActiveCharacter()) {
            sheetFrame.onLoadPressedForPlayer();
            dispose();
            return;
        }
        sheetFrame.enterPlayerMode();
        dispose();
    }

    private void onNarratorPressed() {
        NarratorFrame narratorFrame = new NarratorFrame();
        narratorFrame.setVisible(true);
        if (sheetFrame != null) {
            sheetFrame.dispose();
        }
        dispose();
    }
}
