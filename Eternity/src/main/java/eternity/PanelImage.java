package eternity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
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
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Displays the character portrait, name, date, and load/save/combat controls.
 */
public class PanelImage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String MISSING_IMAGE_TEXT = "<html><center>Image Not Found<br>To utilize images,<br>place .jpg files in the 'Images' folder<br>named by character index.</center></html>";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Color COMBAT_READY_COLOR = new Color(0, 180, 0);
    private static final int PORTRAIT_MAX_WIDTH = 600;
    private static final int PORTRAIT_MAX_HEIGHT = 190;

    private final FrameSheet sheetFrame;
    private CharData character;

    // UI components
    private final JLabel picLabel;
    private final JLabel nameLine1, nameLine2;
    private final JLabel dateLine1, dateLine2;

    private final JButton loadButton;
    private final JButton saveButton;
    private final JButton combatButton;

    private FrameCombat combatFrame; // reused for the session

    private BufferedImage charPic;
    private final Map<Integer, ImageIcon> portraitCache = new HashMap<>();
    private int loadedPictureIndex = Integer.MIN_VALUE;
    private CharData combatFrameCharacter;
    private String renderedName = null;
    private LocalDateTime renderedStartDate = null;
    private Duration renderedElapsed = null;
    private Boolean renderedCombatState = null;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public PanelImage(FrameSheet sheetFrame, CharData character) {
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
        JPanel controls = new JPanel(new GridLayout(2, 5, 10, 5));
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

        controls.add(new JLabel()); // spacer under Load
        controls.add(nameLine2);
        controls.add(new JLabel()); // spacer under Combat
        controls.add(dateLine2);
        controls.add(new JLabel()); // spacer under Save

        // Layout entries (row-major order)
        controls.add(loadButton);
        controls.add(nameLine1);
        controls.add(combatButton);
        controls.add(dateLine1);
        controls.add(saveButton);

        // Initial state
        updateCharacter(character);
    }

    private JLabel createInfoLabel() {
        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        return lbl;
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

    // ---------------------------------------------------------
    // Update Character
    // ---------------------------------------------------------

    public void updateCharacter(CharData character) {
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
            CharDataManager.saveCharacter(character);
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
        if (sheetFrame == null || character == null) return;
        ensureCombatFrame();
        // Reuse the same window; it hides on close so bring it back each time
        combatFrame.setVisible(true);
        combatFrame.toFront();
        combatFrame.requestFocus();
    }

    // ---------------------------------------------------------
    // Combat Button State
    // ---------------------------------------------------------

    /** Refreshes the combat button color based on combat state. */
    public void refreshCombatButtonColor() {
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
    private void ensureCombatFrame() {
        if (sheetFrame == null || character == null) return;
        if (combatFrame == null) {
            combatFrame = new FrameCombat(sheetFrame, character);
            combatFrameCharacter = character;
        } else {
            combatFrame.updateCharacter(character);
            combatFrameCharacter = character;
        }
    }

    private ImageIcon loadPortrait(int index) throws Exception {
        File file = new File("Images/" + index + ".jpg");
        if (!file.exists()) {
            throw new Exception("Missing image");
        }

        charPic = ImageIO.read(file);
        int srcW = charPic.getWidth();
        int srcH = charPic.getHeight();
        double scale = Math.min((double) PORTRAIT_MAX_WIDTH / srcW, (double) PORTRAIT_MAX_HEIGHT / srcH);
        int tgtW = (int) Math.round(srcW * scale);
        int tgtH = (int) Math.round(srcH * scale);

        Image scaled = charPic.getScaledInstance(tgtW, tgtH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
