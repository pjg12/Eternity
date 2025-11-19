package eternity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Character Creation Wizard
 */
public class FrameNew extends JFrame {
    private final DataQuery dataQuery;
    private final FrameSheet sheetFrame;
    private final CharData character;

    private static final int ICON_SIZE = 100;
    private static final int STEP_COUNT = 6;
    private static final String[] STEPS = { "Class", "Race", "Attributes", "Skills", "Specialties", "Affinity" };

    // Icons
    private ImageIcon[] iconNormal;
    private ImageIcon[] iconHover;
    private ImageIcon[] iconDone;

    // UI Elements
    private JLabel headerLabel;
    private JButton[] stepButtons;
    private JLabel[] stepLabels;
    private JButton cancelButton;
    private JButton finalizeButton;

    // Step completion state
    private boolean[] stepDone;

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    public FrameNew(FrameSheet sheetFrame, DataQuery dataQuery, CharData character) {
        super("Character Builder");
        this.sheetFrame = sheetFrame;
        this.dataQuery = dataQuery;
        this.character = character;
        this.stepDone = new boolean[STEP_COUNT];

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);      

        loadIcons();
        initWindow();
        buildHeader();
        buildCenter();
        buildFooter();

        setVisible(true);
    }

    // --------------------------------------------------------------------------
    // Window Setup
    // --------------------------------------------------------------------------
    private void initWindow() {
        setSize(500, 520);
        setMinimumSize(new Dimension(500, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }

    private void buildHeader() {
        headerLabel = new JLabel("Character Builder", SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 22f));
        headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    // --------------------------------------------------------------------------
    // Icons
    // --------------------------------------------------------------------------
    private void loadIcons() {
        iconNormal = new ImageIcon[STEP_COUNT];
        iconHover  = new ImageIcon[STEP_COUNT];
        iconDone   = new ImageIcon[STEP_COUNT];

        for (int i = 0; i < STEP_COUNT; i++) {
            ImageIcon raw1 = loadIcon(STEPS[i], "1");
            ImageIcon raw2 = loadIcon(STEPS[i], "2");
            ImageIcon raw3 = loadIcon(STEPS[i], "3");

            iconNormal[i] = scaleIcon(raw1, ICON_SIZE, ICON_SIZE);
            iconHover[i]  = scaleIcon(raw2, ICON_SIZE, ICON_SIZE);
            iconDone[i]   = scaleIcon(raw3, ICON_SIZE, ICON_SIZE);
        }
    }

    private ImageIcon scaleIcon(ImageIcon src, int width, int height) {
        Image img = src.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
    
    private ImageIcon loadIcon(String name, String variant) {
        return new ImageIcon("eternity/images/" + name + variant + ".png");
    }

    // --------------------------------------------------------------------------
    // Center Buttons + Labels (Modern GridBag)
    // --------------------------------------------------------------------------
    private void buildCenter() {

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gc = new GridBagConstraints();

        stepButtons = new JButton[STEP_COUNT];
        stepLabels  = new JLabel[STEP_COUNT];

        gc.insets = new Insets(12, 18, 4, 18);
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;

        int row = 0;

        for (int i = 0; i < STEP_COUNT; i++) {

            gc.gridx = i % 3;
            gc.gridy = row;

            // Button
            JButton btn = new JButton(iconNormal[i]);
            btn.setRolloverIcon(iconHover[i]);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));

            final int index = i;
            btn.addActionListener(e -> onStepSelected(index));

            stepButtons[i] = btn;
            centerPanel.add(btn, gc);

            // Label under button
            gc.gridy = row + 1;
            JLabel lbl = new JLabel(STEPS[i], SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(100, 20));
            lbl.setFont(lbl.getFont().deriveFont(14f));
            stepLabels[i] = lbl;
            centerPanel.add(lbl, gc);

            // Move down after each row of 3
            if (i % 3 == 2) {
                row += 2;
            }
        }

        add(centerPanel, BorderLayout.CENTER);
    }

    // --------------------------------------------------------------------------
    // Footer (Cancel + Finalize)
    // --------------------------------------------------------------------------
    private void buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        cancelButton = new JButton("Cancel / Load");
        cancelButton.addActionListener(e -> onCancelPressed());

        finalizeButton = new JButton("Finalize");
        finalizeButton.setVisible(false);
        finalizeButton.addActionListener(e -> onFinalizePressed());

        footer.add(cancelButton);
        footer.add(finalizeButton);

        add(footer, BorderLayout.SOUTH);
    }

    // --------------------------------------------------------------------------
    // Button Actions
    // --------------------------------------------------------------------------
    private void onCancelPressed() {
        //sheetFrame.onLoadPressed();
        dispose();
    }

    private void onFinalizePressed() {
        JOptionPane.showMessageDialog(this,
            "Finalize pressed.\n(Insert your finalize logic here.)");
    }

    private void onStepSelected(int index) {
    switch (index) {
        case 0 -> {
            // --- CLASS SELECTION WINDOW ---
            FrameNewClass classFrame =
                    new FrameNewClass(
                        sheetFrame,
                        dataQuery,   // DataStore access
                        character,               // active character
                        this                        // parent FrameNew
                    );

            classFrame.setVisible(true);
        }

        // --------------------------------------------------------------------
        // Other steps still use placeholder dialogs for now
        // --------------------------------------------------------------------
        case 1 -> JOptionPane.showMessageDialog(
                this, "Open Race selection frame here."
        );

        case 2 -> JOptionPane.showMessageDialog(
                this, "Open Attribute selection frame here."
        );

        case 3 -> JOptionPane.showMessageDialog(
                this, "Open Skills selection frame here."
        );

        case 4 -> JOptionPane.showMessageDialog(
                this, "Open Specialties selection frame here."
        );

        case 5 -> JOptionPane.showMessageDialog(
                this, "Open Affinity selection frame here."
        );
    }
}

    // --------------------------------------------------------------------------
    // Update Step Progress (unchanged from your logic)
    // --------------------------------------------------------------------------
    public void updateFrame() {

        boolean unlocked = stepDone[0];

        for (int i = 1; i < STEP_COUNT; i++) {

            if (unlocked) {
                stepButtons[i - 1].setIcon(iconDone[i - 1]);
                stepButtons[i].setVisible(true);
                stepLabels[i].setVisible(true);
                unlocked = stepDone[i];
            }
            else {
                stepButtons[i - 1].setIcon(iconNormal[i - 1]);
                stepButtons[i].setVisible(false);
                stepLabels[i].setVisible(false);
            }
        }

        // Final button appears only if all steps done
        finalizeButton.setVisible(unlocked);

        if (unlocked) {
            stepButtons[STEP_COUNT - 1].setIcon(iconDone[STEP_COUNT - 1]);
        }
    }
}
