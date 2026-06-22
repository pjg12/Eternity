package eternity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import websocket.Roll20WebSocketServer;

/**
 * Displays the character portrait, name, date, and load/save/combat controls.
 */
public class PanelImage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String[] SUPPORTED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final String MISSING_IMAGE_TEXT = "<html><center>Image Not Found.</center></html>";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Color COMBAT_READY_COLOR = new Color(0, 180, 0);
    private static final Color COMBAT_PENDING_COLOR = Color.YELLOW;
    private static final int PORTRAIT_MAX_WIDTH = 600;
    private static final int PORTRAIT_MAX_HEIGHT = 190;

    private final FrameSheet sheetFrame;
    private StoreCharData character;

    // UI components
    private final JLabel picLabel;
    private final JLabel nameLine1, nameLine2;
    private final JLabel dateLine1, dateLine2;

    private final JButton loadButton;
    private final JButton saveButton;
    private final JButton combatButton;

    private FrameCombat combatFrame; // reused for the session
    private Roll20WebSocketServer roll20Server;
    private Roll20WebSocketServer.ConnectionListener playerModeConnectionListener;

    private BufferedImage charPic;
    private final Map<Integer, ImageIcon> portraitCache = new HashMap<>();
    private int loadedPictureIndex = Integer.MIN_VALUE;
    private StoreCharData combatFrameCharacter;
    private String renderedName = null;
    private LocalDateTime renderedStartDate = null;
    private Duration renderedElapsed = null;
    private Boolean renderedCombatState = null;
    private boolean playerModePendingConnection;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public PanelImage(FrameSheet sheetFrame, StoreCharData character) {
        this.sheetFrame = sheetFrame;
        this.character = character;

        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        // === Portrait Area ===
        picLabel = new JLabel(MISSING_IMAGE_TEXT, SwingConstants.CENTER);
        picLabel.setForeground(Color.WHITE);
        picLabel.setPreferredSize(new Dimension(600, 190));
        JPanel picWrapper = new JPanel(new BorderLayout());
        picWrapper.setOpaque(false);
        picWrapper.setBorder(new EmptyBorder(0, 0, 0, 13)); // nudge image 13px left
        picWrapper.add(picLabel, BorderLayout.CENTER);
        add(picWrapper, BorderLayout.NORTH);

        // === Bottom Controls ===
        JPanel controls = new JPanel(new GridLayout(1, 5, 10, 0));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(5, 5, 7, 20));
        add(controls, BorderLayout.SOUTH);

        // Buttons
        loadButton = new JButton("Load");
        loadButton.addActionListener(e -> loadPressed());

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> savePressed());

        combatButton = new JButton("Combat");
        combatButton.addActionListener(e -> combatPressed());
        combatButton.setBackground(COMBAT_READY_COLOR);

        // Name + Date labels
        nameLine1 = createInfoLabel();
        nameLine2 = createInfoLabel();
        dateLine1 = createInfoLabel();
        dateLine2 = createInfoLabel();

        controls.add(loadButton);
        controls.add(buildInfoPanel(nameLine1, nameLine2));
        controls.add(combatButton);
        controls.add(buildInfoPanel(dateLine1, dateLine2));
        controls.add(saveButton);

        // Initial state
        updateCharacter(character);
    }

    private JLabel createInfoLabel() {
        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JPanel buildInfoPanel(JLabel topLine, JLabel bottomLine) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 0));
        panel.setOpaque(false);
        panel.add(topLine);
        panel.add(bottomLine);
        return panel;
    }

    // ---------------------------------------------------------
    // Update Picture
    // ---------------------------------------------------------

    public void updatePicture(int index) {
        if (index == loadedPictureIndex && picLabel.getIcon() != null) {
            return;
        }

        try {
            ImageIcon icon = portraitCache.get(index);
            if (icon == null) {
                icon = loadPortrait(index);
                portraitCache.put(index, icon);
            }
            picLabel.setIcon(icon);
            picLabel.setText(null);
            loadedPictureIndex = index;

        } catch (Exception e) {
            picLabel.setIcon(null);
            picLabel.setText(MISSING_IMAGE_TEXT);
            loadedPictureIndex = Integer.MIN_VALUE;
        }

        repaint();
    }

    public void invalidatePortrait(int index) {
        portraitCache.remove(index);
        if (loadedPictureIndex == index) {
            loadedPictureIndex = Integer.MIN_VALUE;
        }
    }

    // ---------------------------------------------------------
    // Update Character
    // ---------------------------------------------------------

    public void updateCharacter(StoreCharData character) {
        if (character == null) return;
        this.character = character;

        if (combatFrame != null && combatFrameCharacter != character) {
            combatFrame.updateCharacter(character);
            combatFrameCharacter = character;
        }

        CharIdentity id = character.getIdentity();
        if (id != null) {
            String name = id.getName();
            LocalDateTime start = id.getCampaignStartDate();
            Duration elapsed = id.getCampaignElapsedTime();
            if (!Objects.equals(name, renderedName)) {
                updateName(name);
                renderedName = name;
            }
            if (!Objects.equals(start, renderedStartDate) || !Objects.equals(elapsed, renderedElapsed)) {
                updateDate(start, elapsed);
                renderedStartDate = start;
                renderedElapsed = elapsed;
            }
            updatePicture(id.getIndex());
        }

        refreshCombatButtonColor();
    }

    // ---------------------------------------------------------
    // Name Handling
    // ---------------------------------------------------------

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            nameLine1.setText("No Name");
            nameLine2.setText("");
            return;
        }

        String[] parts = name.split(" ");
        nameLine1.setText(parts.length >= 1 ? join(parts, 0, 2) : name);
        nameLine2.setText(parts.length > 2 ? join(parts, 2, 4) : "");
    }

    private String join(String[] parts, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < Math.min(end, parts.length); i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    // ---------------------------------------------------------
    // Date Handling
    // ---------------------------------------------------------

    public void updateDate(LocalDateTime start, Duration elapsed) {
        if (start == null) {
            dateLine1.setText("No campaign date");
            dateLine2.setText("");
            return;
        }

        Duration safeElapsed = (elapsed == null) ? Duration.ZERO : elapsed;
        LocalDateTime current = start.plus(safeElapsed);
        dateLine1.setText(DATE_FMT.format(current));
        dateLine2.setText(TIME_FMT.format(current) + "  (Day " + (safeElapsed.toDays() + 1) + ")");
    }

    // ---------------------------------------------------------
    // Save Operation
    // ---------------------------------------------------------

    public void savePressed() {
        if (sheetFrame != null) {
            sheetFrame.onSavePressed();
        } else if (character != null) {
            //StoreMetaManager.saveCharacter(character);
        }
    }

    // ---------------------------------------------------------
    // Load Operation
    // ---------------------------------------------------------

    private void loadPressed() {
        if (sheetFrame != null) {
            sheetFrame.onLoadPressed();
        }
    }

    // ---------------------------------------------------------
    // Combat Operation
    // ---------------------------------------------------------

    private void combatPressed() {
        combatPressed(false);
    }

    private void combatPressed(boolean playerMode) {
        if (sheetFrame == null || character == null) return;
        ensureCombatFrame(playerMode);
        // Reuse the same window; it hides on close so bring it back each time
        combatFrame.setVisible(true);
        combatFrame.toFront();
        combatFrame.requestFocus();
    }

    public void openCombatHelper() {
        openCombatHelper(false);
    }

    public void openCombatHelper(boolean playerMode) {
        combatPressed(playerMode);
    }

    public void disposeOwnedWindows() {
        detachPlayerModeListener();
        if (combatFrame != null) {
            combatFrame.disposeOwnedWindows();
            combatFrame.dispose();
            combatFrame = null;
            combatFrameCharacter = null;
        }
    }

    public void enterPlayerMode() {
        playerModePendingConnection = true;
        renderedCombatState = null;
        refreshCombatButtonColor();
        try {
            roll20Server = Roll20WebSocketServer.getSharedServer();
            registerPlayerModeListener();
            if (roll20Server.isServiceConnected()) {
                onRoll20ServiceConnected();
            }
        } catch (Exception ex) {
            // Leave the button in pending state until the user retries or the service becomes available.
        }
    }

    // ---------------------------------------------------------
    // Combat Button State
    // ---------------------------------------------------------

    /** Refreshes the combat button color based on combat state. */
    public void refreshCombatButtonColor() {
        if (playerModePendingConnection) {
            combatButton.setText("Pending");
            combatButton.setBackground(COMBAT_PENDING_COLOR);
            return;
        }
        combatButton.setText("Combat");
        boolean inCombat = false;
        if (character != null && character.getCombat() != null) {
            inCombat = character.getCombat().isInCombat();
        }
        if (renderedCombatState != null && renderedCombatState == inCombat) {
            return;
        }
        combatButton.setBackground(inCombat ? Color.RED : COMBAT_READY_COLOR);
        renderedCombatState = inCombat;
    }

    /** Lazily create or refresh the shared combat window for this session. */
    private void ensureCombatFrame(boolean playerMode) {
        if (sheetFrame == null || character == null) return;
        if (combatFrame == null) {
            combatFrame = new FrameCombat(sheetFrame, character, playerMode);
            combatFrameCharacter = character;
        } else {
            combatFrame.updateCharacter(character);
            combatFrameCharacter = character;
        }
    }

    private void registerPlayerModeListener() {
        if (roll20Server == null || playerModeConnectionListener != null) {
            return;
        }
        playerModeConnectionListener = () -> SwingUtilities.invokeLater(this::onRoll20ServiceConnected);
        roll20Server.addConnectionListener(playerModeConnectionListener);
    }

    private void detachPlayerModeListener() {
        if (roll20Server != null && playerModeConnectionListener != null) {
            roll20Server.removeConnectionListener(playerModeConnectionListener);
        }
        playerModeConnectionListener = null;
    }

    private void onRoll20ServiceConnected() {
        if (!playerModePendingConnection) {
            return;
        }
        playerModePendingConnection = false;
        detachPlayerModeListener();
        renderedCombatState = null;
        refreshCombatButtonColor();
        JOptionPane.showMessageDialog(sheetFrame != null ? sheetFrame : this,
                "Roll20 Service is Connected.",
                "Player Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private ImageIcon loadPortrait(int index) throws Exception {
        File file = resolvePortraitFile(index);

        charPic = ImageIO.read(file);
        int srcW = charPic.getWidth();
        int srcH = charPic.getHeight();
        double scale = Math.min((double) PORTRAIT_MAX_WIDTH / srcW, (double) PORTRAIT_MAX_HEIGHT / srcH);
        int tgtW = (int) Math.round(srcW * scale);
        int tgtH = (int) Math.round(srcH * scale);

        Image scaled = charPic.getScaledInstance(tgtW, tgtH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private File resolvePortraitFile(int index) throws Exception {
        for (String extension : SUPPORTED_IMAGE_EXTENSIONS) {
            Path imagePath = AppPaths.imagesDir().resolve(index + "." + extension);
            File file = imagePath.toFile();
            if (file.exists()) {
                return file;
            }
        }
        throw new Exception("Missing image");
    }
}

