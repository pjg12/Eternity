// CHECKED

package eternity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class FrameNewClass extends JFrame {

    // References
    private final FrameSheet sheetFrame;
    private final StoreRuleManager ruleManager;
    private final StoreCharData character;
    private final FrameNew parent;

    // UI Constants
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder LEFT_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 10, 2, 10);
    private static final Insets RIGHT_GB_INSETS = new Insets(2, 5, 5, 5);
    private static final int[] GB_COLUMN_WIDTHS = new int[] { 90, 90, 90, 90 };
    private static final int FRAME_WIDTH = 550;
    private static final int FRAME_HEIGHT = 480;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int BUTTON_SPACING = 10;
    private static final int ICON_SIZE = 80;
    private static final int INFO_TITLE_COUNT = 17;

    // UI Strings
    private static final String WINDOW_TITLE = "Class Selection";
    private static final String HEADER_TEXT = "Select a Class";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_NEXT = "Next →";
    private static final String[] FIELD_NAMES = { "HP Scaling:", "Primary Attribute:", "Aura Scaling:", "Armor:", "Fortitude:", "Reflex:", "Will:", "Weapons:", "Attack:", "Apply:", "Range:", "Subclass 1:", "Subclass 2:", "Attribute:", "Role:", "Role:", "Attribute:" };
    private static final String[] CLASSOPTIONS = { "Warrior", "Paladin", "Rogue", "Monk", "Archer", "Leader", "Cleric", "Caster", "Shifter", "Pilot" };

    // UI Tracker
    private int nextInfoIndex = 0;
    private int selectedIndex = -1;
    private final boolean gmMode;
    private static boolean iconsLoaded = false;
    private final ClassDisplayData[] classDisplayData = new ClassDisplayData[CLASSOPTIONS.length];
    private DataClass selectedClass;

    // Icons
    private static ImageIcon[] iconNormal;
    private static ImageIcon[] iconHover;

    // UI Elements
    private JPanel headerPanel, leftPanel, rightPanel, footerPanel;
    private JLabel headerL;
    private JButton[] classButtons;
    private JLabel[] classLabels;
    private JScrollPane leftPane;
    private final JPanel[] titlePanel, infoPanel;
    private final JLabel[] titleLabel, infoLabel;
    private JButton nextButton;
    private JButton cancelButton;

    // Frames
    private FrameNewClassPicker pickerFrame;

    // ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameNewClass(FrameSheet sheetFrame, StoreRuleManager ruleManager, StoreCharData character, FrameNew parent, boolean gmMode) {
        super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        this.ruleManager = ruleManager;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;

        titlePanel = new JPanel[INFO_TITLE_COUNT];
        titleLabel = new JLabel[INFO_TITLE_COUNT];
        infoPanel = new JPanel[INFO_TITLE_COUNT];
        infoLabel = new JLabel[INFO_TITLE_COUNT];
        //ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);      
        SwingUtilities.invokeLater(this::loadIconsAsync);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout(BUTTON_SPACING, BUTTON_SPACING));

        buildUI();
        preloadClassDisplayData();
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        buildHeader();
        buildLeftPanel();
        buildRightPanel();
        buildFooter();
    }

    private void buildHeader() {
        // Build panel
        headerPanel = new JPanel(new BorderLayout());

        // Build header
        headerL = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setBorder(HEADER_BORDER);

        // Add elements
        headerPanel.add(headerL, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void buildLeftPanel() {
        // Build panel
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(LEFT_BORDER);

        // Build button arrays
        classButtons = new JButton[CLASSOPTIONS.length];
        classLabels = new JLabel[CLASSOPTIONS.length];

        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            // Build button
            JButton btn = new JButton(getNormalIcon(i));
            btn.setRolloverIcon(getHoverIcon(i));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));

            // Add Listener
            final int index = i;
            btn.addActionListener(e -> onClassSelected(index));

            // Add button
            classButtons[i] = btn;
            leftPanel.add(btn);

            // Build label
            JLabel lbl = buildLabel(CLASSOPTIONS[i]);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setBorder(new EmptyBorder(4, 0, 10, 0));
            lbl.setPreferredSize(new Dimension(ICON_SIZE, 20));
            lbl.setFont(LABEL_FONT);

            // Add label
            classLabels[i] = lbl;
            leftPanel.add(lbl);
        }

        // Build scroll pane
        leftPane = new JScrollPane(leftPanel);
        leftPane.setPreferredSize(new Dimension(140, 400));
        leftPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        leftPane.getVerticalScrollBar().setUnitIncrement(15);

        // Add pane
        add(leftPane, BorderLayout.WEST);
    }

    private void buildRightPanel() {
        // Build panel
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = GB_COLUMN_WIDTHS;
        rightPanel = new JPanel(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = RIGHT_GB_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Setup Variables
        int tileIndex = 0;
        int y;
        int x;
        int width;
        
        for (int i = 0; i < INFO_TITLE_COUNT; i++) {
            y = tileIndex / 4;
            x = tileIndex % 4;
            if (i == 1 || i == 11 || i == 12) {
                width = 2;
                tileIndex++;
            }
            else width = 1;
            
            gridHelper(gbc, y, x, width);

            rightPanel.add(buildInfoRow(i), gbc);
            tileIndex++;
        }
        add(rightPanel, BorderLayout.CENTER);
    }

    private void buildFooter() {
        // Build panel
        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(FOOTER_BORDER);

        // Build cancel button
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        JPanel pan = new JPanel();
        pan.add(cancelButton);
        footerPanel.add(pan, BorderLayout.WEST);

        // Build next button
        nextButton = new JButton(BUTTON_NEXT);
        nextButton.addActionListener(e -> onNextPressed());
        pan = new JPanel();
        pan.add(nextButton);
        footerPanel.add(pan, BorderLayout.EAST);
        nextButton.setVisible(false);

        // Add panels
        add(footerPanel, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------

    private void onCancelPressed() {
        dispose();
    }

    private void onNextPressed() {
        if (selectedClass == null) {
            return;
        }
        if (pickerFrame != null) {
            pickerFrame.dispose();
        }
        pickerFrame = new FrameNewClassPicker(ruleManager, character, selectedClass, this, gmMode);
        pickerFrame.setVisible(true);
    }

    // Called by FrameNewClassPicker
    public void onConfirmPressed() {
        parent.setStepConfirmed(0);
        dispose();
    }

    // --------------------------------------------------------------------------
    // Icons
    // --------------------------------------------------------------------------

    private static synchronized void loadIcons() {
        if (iconsLoaded) return;

        iconNormal = new ImageIcon[CLASSOPTIONS.length];
        iconHover  = new ImageIcon[CLASSOPTIONS.length];

        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            ImageIcon raw1 = loadIcon(CLASSOPTIONS[i], "1");
            ImageIcon raw2 = loadIcon(CLASSOPTIONS[i], "2");

            iconNormal[i] = scaleIcon(raw1, ICON_SIZE, ICON_SIZE);
            iconHover[i]  = scaleIcon(raw2, ICON_SIZE, ICON_SIZE);
        }

        iconsLoaded = true;
    }

    private static ImageIcon scaleIcon(ImageIcon src, int width, int height) { return new ImageIcon(src.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)); }
    private static ImageIcon loadIcon(String name, String variant) { return new ImageIcon("images/" + name + variant + ".png"); }

    /**
     * Loads icons asynchronously to avoid blocking UI creation.
     */
    private void loadIconsAsync() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadIcons();
                return null;
            }

            @Override
            protected void done() {
                applyLoadedIcons();
                //updateFrame();
            }
        };
        worker.execute();
    }

    private static ImageIcon getNormalIcon(int stepIndex) { return iconsLoaded ? iconNormal[stepIndex] : null; }
    private static ImageIcon getHoverIcon(int stepIndex) { return iconsLoaded ? iconHover[stepIndex] : null; }

    private void applyLoadedIcons() {
        if (classButtons == null || !iconsLoaded) return;
        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            classButtons[i].setIcon(getNormalIcon(i));
            classButtons[i].setRolloverIcon(getHoverIcon(i));
        }
    }

    // --------------------------------------------------------------------------
    // Builders
    // --------------------------------------------------------------------------

    private JLabel buildLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JPanel buildInfoRow(int i) {
        // Generate full element panel
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        // Generate title subpanel
        JPanel tPanel = new JPanel();
        root.add(tPanel);
        titlePanel[nextInfoIndex] = tPanel;

        // Generate title label
        JLabel tLabel = new JLabel(FIELD_NAMES[i], SwingConstants.CENTER);
        tLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tPanel.add(tLabel);
        titleLabel[nextInfoIndex] = tLabel;

        // Generate info subpanel
        JPanel iPanel = new JPanel();
        root.add(iPanel);
        infoPanel[nextInfoIndex] = iPanel;

        // Generate info JLabel
        JLabel iLabel = buildLabel("-");
        iPanel.add(iLabel);
        infoLabel[nextInfoIndex] = iLabel;

        // Increment info
        nextInfoIndex++;

        return root;
    }

    // ---------------------------------------------------
    // CLASS SELECTED
    // ---------------------------------------------------
    private void onClassSelected(int index) {
        if (selectedIndex == index) return;
        selectedIndex = index;

        if (pickerFrame != null) {
            pickerFrame.dispose();
            pickerFrame = null;
        }

        ClassDisplayData display = classDisplayData[index];
        selectedClass = display.selectedClass;

        headerL.setText(display.name());

        for (int i = 0; i < INFO_TITLE_COUNT; i++) {
            infoLabel[i].setText(display.fields()[i]);
        }

        rightPanel.setBackground(display.background);
        rightPanel.setForeground(display.foreground);

        for (int i = 0; i < INFO_TITLE_COUNT; i++) {
            titlePanel[i].setBackground(display.background);
            titleLabel[i].setForeground(display.foreground);
        }

        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            classButtons[i].setIcon(getNormalIcon(i));
        }
        classButtons[index].setIcon(getHoverIcon(index));

        if (!nextButton.isVisible()) {
            nextButton.setVisible(true);
        }

        if (gmMode) onNextPressed();
    }

    public void classChoicesConfirmed() {
        character.getIdentity().setCharClass(selectedClass.getName());
        parent.setStepConfirmed(0);                     // notify FrameNew
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

    private static String[] resolveScalingText(int[] scaling) {
        String[] scalText = {"Bad", "Bad", "Bad", "Bad", "Bad", "Bad"};
        for (int i = 0; i < 6; i++) {
            if (scaling != null && i < scaling.length) {
                if (scaling[i] == 1) scalText[i] = "Good";
                else if (scaling[i] == 2) scalText[i] = "Average";
            }
        }
        return scalText;
    }
    
    private record ClassDisplayData(DataClass selectedClass, Color background, Color foreground, String name, String[] fields) {}

    private void preloadClassDisplayData() {
        int index = 0;
        for (String name : CLASSOPTIONS) {
            DataClass dataClass = ruleManager.getClassByName(name);
            DataColor color = ruleManager.getColorByTitle(name);

            if (dataClass == null || color == null) continue;

            DataClass sub1 = ruleManager.getClassById(dataClass.getID() + 1);
            DataClass sub2 = ruleManager.getClassById(dataClass.getID() + 2);
            String[] scaling = resolveScalingText(dataClass.getStatScaling());

            classDisplayData[index] = new ClassDisplayData(dataClass, color.getBackColor(), color.getForeColor(), dataClass.getName(), new String[] 
                    {String.valueOf(dataClass.getHpScaling()), dataClass.getPrimaryAtt(), String.valueOf(dataClass.getAuraScaling()), dataClass.getArmor(),
                    scaling[0], scaling[1], scaling[2], dataClass.getProfLabel(), scaling[3], scaling[4], scaling[5], sub1 != null ? sub1.getName() : "-",
                    sub2 != null ? sub2.getName() : "-", sub1 != null ? sub1.getSecondaryAtt() : "-", sub1 != null ? sub1.getRole() : "-", sub2 != null ? sub2.getRole() : "-",
                    sub2 != null ? sub2.getSecondaryAtt() : "-" });

            index++;
        }
    }
}
