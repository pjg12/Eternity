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

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public PanelImage(FrameSheet sheetFrame, CharData character) {
        this.sheetFrame = sheetFrame;
        this.character = character;

        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        // === Portrait Area ===
        picLabel = new JLabel("<html><center>Image Not Found<br>To utilize images,<br>place .jpg files in the 'Images' folder<br>named by character index.</center></html>", SwingConstants.CENTER);
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
        combatButton.setBackground(new Color(0, 180, 0));

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
        String path = "Images/" + index + ".jpg";

        try {
            File file = new File(path);
            if (!file.exists()) throw new Exception("Missing image");

            charPic = ImageIO.read(file);
            // Preserve aspect ratio while fitting within the display area
            final int maxW = 600;
            final int maxH = 190;
            int srcW = charPic.getWidth();
            int srcH = charPic.getHeight();
            double scale = Math.min((double) maxW / srcW, (double) maxH / srcH);
            int tgtW = (int) Math.round(srcW * scale);
            int tgtH = (int) Math.round(srcH * scale);

            Image scaled = charPic.getScaledInstance(tgtW, tgtH, Image.SCALE_SMOOTH);
            picLabel.setIcon(new ImageIcon(scaled));
            picLabel.setText(null);

        } catch (Exception e) {
            picLabel.setIcon(null);
            picLabel.setText("<html><center>Image Not Found<br>To utilize images,<br>place .jpg files in the 'Images' folder<br>named by character index.</center></html>");
        }

        revalidate();
        repaint();
    }

    // ---------------------------------------------------------
    // Update Character
    // ---------------------------------------------------------

    public void updateCharacter(CharData character) {
        if (character == null) return;
        this.character = character;

        ensureCombatFrame();

        CharIdentity id = character.getIdentity();
        if (id != null) {
            updateName(id.getName());
            updateDate(id.getCampaignStartDate(), id.getCampaignElapsedTime());
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

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        dateLine1.setText(dateFmt.format(current));
        dateLine2.setText(timeFmt.format(current) + "  (Day " + (safeElapsed.toDays() + 1) + ")");
    }

    // ---------------------------------------------------------
    // Save Operation
    // ---------------------------------------------------------

    public void savePressed() {
        if (sheetFrame != null) {
            sheetFrame.onSavePressed();
        } else if (character != null) {
            CharacterDataManager.saveCharacter(character);
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
        combatButton.setBackground(inCombat ? Color.RED : new Color(0, 180, 0));
    }

    /** Lazily create or refresh the shared combat window for this session. */
    private void ensureCombatFrame() {
        if (sheetFrame == null || character == null) return;
        if (combatFrame == null) {
            combatFrame = new FrameCombat(sheetFrame, character);
        } else {
            combatFrame.updateCharacter(character);
        }
    }
}
