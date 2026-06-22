package eternity;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class FrameNewAttribute extends JFrame {

    // References
    private final FrameSheet sheetFrame;
    private final StoreCharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    // UI Constants
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder LEFT_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 10, 2, 10);
    private static final Insets RIGHT_GB_INSETS = new Insets(2, 5, 5, 5);
    private static final int[] GB_COLUMN_WIDTHS = new int[] { 90, 90, 90, 90 };
    private static final int FRAME_WIDTH = 560;
    private static final int FRAME_HEIGHT = 360;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int BUTTON_SPACING = 10;

    // UI Strings
    private static final String WINDOW_TITLE = "Attribute Selection";
    private static final String HEADER_CORE_TEXT = "Select Core Attributes";
    private static final String HEADER_CHAR_TEXT = "Select Char Attributes";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_NEXT = "Next →";
    private static final String BUTTON_BACK = "Back";
    private static final String BUTTON_CONFIRM = "Confirm";
    private static final Integer[] ATTVALUES = {8, 9, 10, 11, 12, 13, 14, 15};
    private static final String[] CORE_ATTRIBUTES = { "STR", "DEX", "CON", "FOC", "CTL", "CAP" };
    private static final String[] CHAR_ATTRIBUTES = { "KNOW", "MECH", "PERC", "INT", "CHA", "SUB" };

    // UI Tracker
    private int remainder;
    private boolean warn;
    private boolean corePhase = true;

    // UI Elements
    private JPanel headerPanel, centerPanel, footerPanel;
    private JPanel corePanel, charPanel;
    private JPanel coreBPanel, charBPanel;
    private JLabel headerL;
    private JButton cancelButton, nextButton;
    private JButton backButton, confirmButton;
    private final JComboBox<Integer>[] coreBoxes = new JComboBox[6];
    private final JComboBox<Integer>[] charBoxes = new JComboBox[6];
    private final JLabel[] coreLabels = new JLabel[6];
    private final JLabel[] charLabels = new JLabel[6];
    private final JFormattedTextField[] coreNumFields = new JFormattedTextField[7];
    private final JFormattedTextField[] charNumFields = new JFormattedTextField[7];
    private final CardLayout centerLayout = new CardLayout();
    private final CardLayout footerLayout = new CardLayout();

    // ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameNewAttribute(FrameSheet sheetFrame, StoreCharData character, FrameNew parent, boolean gmMode) {
        super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;

        //ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);      

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout(BUTTON_SPACING, BUTTON_SPACING));

        buildUI();
        resetBoxes();
        corePhase();





        /*ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        initDefaults();
        buildLayout();*/
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        buildHeader();
        buildCenterPanel();
        buildFooter();
    }

    private void buildHeader() {
        // Build panel
        headerPanel = new JPanel(new BorderLayout());

        // Build header
        headerL = new JLabel(HEADER_CORE_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setBorder(HEADER_BORDER);

        // Add elements
        headerPanel.add(headerL, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void buildCenterPanel() {
        // Build panel
        centerPanel = new JPanel(centerLayout);

        // Build subpanels
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = GB_COLUMN_WIDTHS;
        corePanel = new JPanel(layout);
        charPanel = new JPanel(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = RIGHT_GB_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Setup Variables
        int y = 0;
        int x = 0;
        int width = 1;
        gridHelper(gbc, y, x, width);

        // Blank Label
        JLabel lbl = buildLabel(" ");
        corePanel.add(lbl, gbc);
        lbl = buildLabel(" ");
        charPanel.add(lbl, gbc);

        x++;
        gridHelper(gbc, y, x, width);

        // Value Label
        lbl = buildLabel("Value");
        corePanel.add(lbl, gbc);
        lbl = buildLabel("Value");
        charPanel.add(lbl, gbc);

        x++;
        gridHelper(gbc, y, x, width);

        // Cost Label
        lbl = buildLabel("Cost");
        corePanel.add(lbl, gbc);
        lbl = buildLabel("Cost");
        charPanel.add(lbl, gbc);

        x++;
        gridHelper(gbc, y, x, width);

        // Remaining Label
        lbl = buildLabel("Remaining Points");
        corePanel.add(lbl, gbc);
        lbl = buildLabel("Remaining Points");
        charPanel.add(lbl, gbc);

        gridHelper(gbc, y+1, x, width);
        coreNumFields[6] = buildNumField();
        charNumFields[6] = buildNumField();
        corePanel.add(coreNumFields[6], gbc);
        charPanel.add(charNumFields[6], gbc);

        for (int i = 0; i < CORE_ATTRIBUTES.length; i++) {
            y = i +1;
            x = 0;
            gridHelper(gbc, y, x, width);

            coreLabels[i] = new JLabel(CORE_ATTRIBUTES[i]);
            charLabels[i] = new JLabel(CHAR_ATTRIBUTES[i]);
            corePanel.add(coreLabels[i], gbc);
            charPanel.add(charLabels[i], gbc);

            x++;
            gridHelper(gbc, y, x, width);

            coreBoxes[i] = buildComboBox(ATTVALUES);
            charBoxes[i] = buildComboBox(ATTVALUES);
            corePanel.add(coreBoxes[i], gbc);
            charPanel.add(charBoxes[i], gbc);

            x++;
            gridHelper(gbc, y, x, width);

            coreNumFields[i] = buildNumField();
            charNumFields[i] = buildNumField();
            corePanel.add(coreNumFields[i], gbc);
            charPanel.add(charNumFields[i], gbc);
        }

        // Add cards
        centerPanel.add(corePanel, "CORE");
        centerPanel.add(charPanel, "CHAR");

        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildFooter() {
        // Build panel
        footerPanel = new JPanel(footerLayout);
        footerPanel.setBorder(FOOTER_BORDER);

        coreBPanel = new JPanel(new BorderLayout());
        charBPanel = new JPanel(new BorderLayout());

        // Build cancel button
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        coreBPanel.add(cancelButton, BorderLayout.WEST);
        
        // Build next button
        nextButton = new JButton(BUTTON_NEXT);
        nextButton.addActionListener(e -> onNextPressed());
        coreBPanel.add(nextButton, BorderLayout.EAST);

        // Build back button
        backButton = new JButton(BUTTON_BACK);
        backButton.addActionListener(e -> onBackPressed());
        charBPanel.add(backButton, BorderLayout.WEST);
        
        // Build next button
        confirmButton = new JButton(BUTTON_CONFIRM);
        confirmButton.addActionListener(e -> onConfirmPressed());
        charBPanel.add(confirmButton, BorderLayout.EAST);

        // Add cards
        footerPanel.add(coreBPanel, "CORE");
        footerPanel.add(charBPanel, "CHAR");

        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel buildLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JFormattedTextField buildNumField() {
        JFormattedTextField numF = new JFormattedTextField(NumberFormat.getIntegerInstance());
        numF.setFont(LABEL_FONT);
        numF.setHorizontalAlignment(SwingConstants.CENTER);
        numF.setAlignmentX(Component.CENTER_ALIGNMENT);
        numF.setEditable(false);
        return numF;
    }

    private JComboBox<Integer> buildComboBox(Integer[] values) {
        JComboBox<Integer> box = new JComboBox<>(values);
        box.addActionListener(e -> updateRemainder());
        return box;
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------

    private void onCancelPressed() {
        dispose();
    }

    private void onNextPressed() {
        if (gmMode) {
            applyGmSelections();
            applyAttributes();
            parent.setStepConfirmed(2);
            dispose();
            return;
        }
        if (!validateSpend()) return;

        charPhase();
    }

    private void onBackPressed() {
        corePhase();
    }

    public void onConfirmPressed() {
        if (!validateSpend()) return;

        applyAttributes();

        parent.setStepConfirmed(2);
        dispose();
    }

    // --------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------

    private void gridHelper (GridBagConstraints gbc, int y, int x, int width) {
        gbc.gridwidth = width;
        gbc.gridy = y;
        gbc.gridx = x;
    }

    private void corePhase() {
        headerL.setText(HEADER_CORE_TEXT);
        centerLayout.show(centerPanel, "CORE");
        footerLayout.show(footerPanel, "CORE");
        corePhase = true;
    }

    private void charPhase() {
        headerL.setText(HEADER_CHAR_TEXT);
        centerLayout.show(centerPanel, "CHAR");
        footerLayout.show(footerPanel, "CHAR");
        corePhase = false;
    }

    private void resetBoxes() {
        for (int i = 0; i < 6; i++) {
            coreBoxes[i].setSelectedItem(10);
            charBoxes[i].setSelectedItem(10);
        }
    }

    private void updateRemainder() {
        remainder = 0;
        for (int i = 0; i < 6; i++) {
            Object sel;
            if (corePhase) sel = coreBoxes[i].getSelectedItem();
            else sel = charBoxes[i].getSelectedItem();
                
            if (!(sel instanceof Integer)) continue;

            int tempInt = (Integer) sel;
            int attMod = tempInt - 10;
            int attVariant = Math.abs(attMod) + 1;
            attVariant = (attVariant * attMod) / 2;

            if (corePhase) coreNumFields[i].setValue(attVariant);
            else charNumFields[i].setValue(attVariant);

            remainder += attVariant;
        }
        remainder = 25 - remainder;
        if (corePhase) coreNumFields[6].setValue(remainder);
        else charNumFields[6].setValue(remainder);
        warn = false;
    }

    private boolean validateSpend() {
        if (remainder < 0) {
            JOptionPane.showMessageDialog(this,
                    "You do not have enough attribute points for this selection.\nPlease lower your overall attribute selection.");
            return false;
        } else if (remainder > 0 && !warn) {
            JOptionPane.showMessageDialog(this,
                    "You have not spent all of your attribute points.\nIf you are satisfied with your selection, press confirm again.");
            warn = true;
            return false;
        }
        return true;
    }

    private void applyAttributes() {
        String key;
        int value = 0;
        for (int i = 0; i < CORE_ATTRIBUTES.length; i++) {
            key = CORE_ATTRIBUTES[i];
            value = (int)coreBoxes[i].getSelectedItem();
            character.getAttributes().addStatus(new DataStatus("Base", "None", "None", "B" + key, value, "Passive", -1));

            key = CHAR_ATTRIBUTES[i];
            value = (int)charBoxes[i].getSelectedItem();
            character.getAttributes().addStatus(new DataStatus("Base", "None", "None", "B" + key, value, "Passive", -1));
        }
    }

    private void applyGmSelections() {
        applyRandomAllocation(coreBoxes);
        applyRandomAllocation(charBoxes);
    }

    private void applyRandomAllocation(JComboBox<Integer>[] boxes) {
        if (boxes == null) return;
        int[] allocation = generateRandomPointBuyAllocation();
        for (int i = 0; i < boxes.length && i < allocation.length; i++) {
            if (boxes[i] != null) {
                boxes[i].setSelectedItem(allocation[i]);
            }
        }
    }

    private int[] generateRandomPointBuyAllocation() {
        ArrayList<int[]> combinations = new ArrayList<>();
        buildPointBuyAllocations(0, 25, new int[6], combinations);
        if (combinations.isEmpty()) {
            return new int[] {10, 10, 10, 10, 10, 10};
        }
        Collections.shuffle(combinations, ThreadLocalRandom.current());
        return combinations.get(0);
    }

    private void buildPointBuyAllocations(int index, int remainingCost, int[] current, List<int[]> combinations) {
        if (index == current.length) {
            if (remainingCost == 0) {
                combinations.add(current.clone());
            }
            return;
        }

        List<Integer> candidates = new ArrayList<>(List.of(ATTVALUES));
        Collections.shuffle(candidates, ThreadLocalRandom.current());
        for (Integer candidate : candidates) {
            if (candidate == null) continue;
            int cost = attributePointCost(candidate);
            int nextRemaining = remainingCost - cost;
            if (nextRemaining < 0) continue;
            if (!canReachRemainingCost(index + 1, nextRemaining, current.length)) continue;
            current[index] = candidate;
            buildPointBuyAllocations(index + 1, nextRemaining, current, combinations);
        }
    }

    private boolean canReachRemainingCost(int nextIndex, int remainingCost, int totalLength) {
        int slotsRemaining = totalLength - nextIndex;
        int minCost = slotsRemaining * attributePointCost(8);
        int maxCost = slotsRemaining * attributePointCost(15);
        return remainingCost >= minCost && remainingCost <= maxCost;
    }

    private int attributePointCost(int attributeValue) {
        int attMod = attributeValue - 10;
        int attVariant = Math.abs(attMod) + 1;
        return (attVariant * attMod) / 2;
    }
}
