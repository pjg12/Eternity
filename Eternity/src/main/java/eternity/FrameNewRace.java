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

public class FrameNewRace extends JFrame {

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
    private static final int INFO_TITLE_COUNT = 8;

    // UI Strings
    private static final String WINDOW_TITLE = "Race Selection";
    private static final String HEADER_TEXT = "Select a Race";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_NEXT = "Next →";
    private static final String[] FIELD_NAMES = { "Homeworld:", "Affiliation:", "Physical:", "Personality:", "Base Mods:", "Scaling Mods:", "Racial:", "Description:" };
    private static final String[] RACEOPTIONS = { "Alteri","Aquata","Ardian","Azuri","Boxlor","Cetryu","Construct","Deckan","En","Evan","Felsh","Felsh Cat","Forven","Gaian","Irdon","Kenti","Kitsune", "Loben","Loritho","Nohmen","Nosfer","Oon","Poruuk","Quez","Raigon","Reven", "Skren","Theran","Vindis","Vyrek","Xid","Zyan" };

    // UI Tracker
    private int nextInfoIndex = 0;
    private int selectedIndex = -1;
    private final boolean gmMode;
    private static boolean iconsLoaded = false;
    private final RaceDisplayData[] raceDisplayData = new RaceDisplayData[RACEOPTIONS.length];
    private DataRace selectedRace;

    // Icons
    private static ImageIcon[] iconNormal;
    private static ImageIcon[] iconHover;

    // UI Elements
    private JPanel headerPanel, leftPanel, rightPanel, footerPanel;
    private JLabel headerL;
    private JButton[] raceButtons;
    private JLabel[] raceLabels;
    private JScrollPane leftPane;
    private final JPanel[] titlePanel, infoPanel;
    private final JLabel[] titleLabel, infoLabel;
    private JButton nextButton;
    private JButton cancelButton;

    // Frames
    private FrameNewRacePicker pickerFrame;





    



    





    // ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameNewRace(FrameSheet sheetFrame, StoreRuleManager ruleManager, StoreCharData character, FrameNew parent, boolean gmMode) {
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
        preloadRaceDisplayData();
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
        raceButtons = new JButton[RACEOPTIONS.length];
        raceLabels = new JLabel[RACEOPTIONS.length];

        for (int i = 0; i < RACEOPTIONS.length; i++) {
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
            btn.addActionListener(e -> onRaceSelected(index));

            // Add button
            raceButtons[i] = btn;
            leftPanel.add(btn);

            // Build label
            JLabel lbl = buildLabel(RACEOPTIONS[i]);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setBorder(new EmptyBorder(4, 0, 10, 0));
            lbl.setPreferredSize(new Dimension(ICON_SIZE, 20));
            lbl.setFont(LABEL_FONT);

            // Add label
            raceLabels[i] = lbl;
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
            y = tileIndex / 2;
            x = (tileIndex % 2) *2;
            width = 2;
            if (tileIndex > 5) {
                width += 2;
                tileIndex++;
            }
            
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
        if (selectedRace == null) return;
        if (!selectedRace.getRacePick()) {
            commitRaceSelection(java.util.List.of());
            return;
        }

        if (pickerFrame == null || !pickerFrame.isDisplayable()) {
            pickerFrame = new FrameNewRacePicker(ruleManager, character, selectedRace, this, gmMode);
        }
        if (pickerFrame.isDisplayable()) {
            pickerFrame.setVisible(true);
        }
    }

    public void onConfirmPressed(java.util.List<String> raceChoices) {
        commitRaceSelection(raceChoices);
    }

    // --------------------------------------------------------------------------
    // Icons
    // --------------------------------------------------------------------------

    private static synchronized void loadIcons() {
        if (iconsLoaded) return;

        iconNormal = new ImageIcon[RACEOPTIONS.length];
        iconHover  = new ImageIcon[RACEOPTIONS.length];

        for (int i = 0; i < RACEOPTIONS.length; i++) {
            ImageIcon raw1 = loadIcon(RACEOPTIONS[i], "1");
            ImageIcon raw2 = loadIcon(RACEOPTIONS[i], "2");

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
        if (raceButtons == null || !iconsLoaded) return;
        for (int i = 0; i < RACEOPTIONS.length; i++) {
            raceButtons[i].setIcon(getNormalIcon(i));
            raceButtons[i].setRolloverIcon(getHoverIcon(i));
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
    // RACE SELECTED
    // ---------------------------------------------------
    private void onRaceSelected(int index) {
        if (selectedIndex == index) return;
        selectedIndex = index;

        RaceDisplayData display = raceDisplayData[index];
        selectedRace = display.selectedRace;

        headerL.setText(display.name());

        for (int i = 0; i < INFO_TITLE_COUNT; i++) {
            String textString = "<html><body style='text-align: center; width: ";
            if (i > 5) textString += "220";
            else textString += "100";
            textString += "px'>" + display.fields()[i] + "</body></html>";
            infoLabel[i].setText(textString);
        }

        rightPanel.setBackground(display.background);
        rightPanel.setForeground(display.foreground);

        for (int i = 0; i < INFO_TITLE_COUNT; i++) {
            titlePanel[i].setBackground(display.background);
            titleLabel[i].setForeground(display.foreground);
        }

        for (int i = 0; i < RACEOPTIONS.length; i++) {
            raceButtons[i].setIcon(getNormalIcon(i));
        }
        raceButtons[index].setIcon(getHoverIcon(index));

        if (!nextButton.isVisible()) {
            nextButton.setVisible(true);
        }

        if (gmMode) onNextPressed();
    }

    private void commitRaceSelection(java.util.List<String> raceChoices) {
        if (selectedRace == null) return;
        character.getIdentity().setRace(selectedRace.getName());
        character.getIdentity().setCharRacePick(new java.util.ArrayList<>(raceChoices));
        parent.setStepConfirmed(1);
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

    private record RaceDisplayData(DataRace selectedRace, Color background, Color foreground, String name, String[] fields) {}

    private void preloadRaceDisplayData() {
        for (int i = 0; i < RACEOPTIONS.length; i++) {
            String name = RACEOPTIONS[i];
            DataRace dataRace = ruleManager.getRaceByName(name);
            DataColor color = ruleManager.getColorByTitle(name);

            if (dataRace == null || color == null) continue;

            raceDisplayData[i] = new RaceDisplayData(dataRace, color.getBackColor(), color.getForeColor(), dataRace.getName(), new String[] 
                    {dataRace.getHomeworld(),dataRace.getAffiliation(),dataRace.getPhysical(),dataRace.getPersonality(),dataRace.getBaseStatusDesc(),dataRace.getScalingStatusDesc(),dataRace.getRacialDesc(),dataRace.getDescription() });
        }
    }




























    



    /* ============================================================
        DESCRIPTION PANEL
       ============================================================ 
    private JComponent buildRaceDescriptionPanel() {
        raceDescPanel = new JPanel(null);
        raceDescPanel.setPreferredSize(new Dimension(350, 300));
        raceDescPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        raceDescPanel.setVisible(false);

        raceDetailButtons = new JButton[DESC_COUNT];
        raceDescLabels    = new JLabel[DESC_COUNT];

        String[] titles = {
            "Name", "Homeworld", "Affiliation", "Physical",
            "Personality", "Attributes", "Scaling",
            "Racial", "Description"
        };

        for (int i = 0; i < DESC_COUNT; i++) {
            JLabel lbl = new JLabel(titles[i]);
            raceDescLabels[i] = lbl;
            raceDescPanel.add(lbl);

            JButton btn = new JButton("-");
            raceDetailButtons[i] = btn;
            raceDescPanel.add(btn);

            if (i == 0) {
                lbl.setBounds(15, 10, 300, 20);
                btn.setBounds(15, 30, 300, 20);
            } else if (i <= 6) {
                int row = (i - 1) / 2;
                int col = (i - 1) % 2;
                int x = 15 + col * 160;
                int y = 75 + row * 45;

                lbl.setBounds(x, y - 20, 140, 20);
                btn.setBounds(x, y, 140, 20);
            } else {
                int y = 210 + (i - 7) * 45;
                lbl.setBounds(15, y - 20, 300, 20);
                btn.setBounds(15, y, 300, 20);
            }
        }

        return raceDescPanel;
    }

    /* ============================================================
        LOGIC
       ============================================================ 
    private void raceClear() {
        selectedRace = null;
        raceDescPanel.setVisible(false);
        nextButton.setEnabled(false);
        setVisible(false);
        //parent.raceClear();
    }



    private void setRaceDetail(int index, String text, String tt) {
        raceDetailButtons[index].setText(text);
        raceDetailButtons[index].setToolTipText(tt);
        raceDescLabels[index].setToolTipText(tt);
    }

    void updateRaceData(int index) {
        RaceDisplayData display = raceDisplayData[index];
        if (display == null) return;

        selectedRace = display.race();
        raceDescPanel.setBackground(display.background());

        setRaceDetail(0, display.name(), display.nameTt());
        setRaceDetail(1, display.homeworld(), display.homeworldTt());
        setRaceDetail(2, display.affiliation(), display.affiliationTt());
        setRaceDetail(3, display.physical(), display.physicalTt());
        setRaceDetail(4, display.personality(), display.personalityTt());
        setRaceDetail(5, display.baseStatus(), display.baseStatusTt());
        setRaceDetail(6, display.scalingStatus(), display.scalingStatusTt());
        setRaceDetail(7, display.racial(), display.racialTt());
        setRaceDetail(8, "A brief history...", display.description());

        for (int i = 0; i < DESC_COUNT; i++) {
            raceDetailButtons[i].setBackground(display.foreground());
            raceDetailButtons[i].setForeground(display.background());
            raceDescLabels[i].setForeground(display.foreground());
        }

        raceDescPanel.setVisible(true);
        nextButton.setEnabled(true);

        if (gmMode) {
            raceChoicesConfirmed(EMPTY_CHOICES);
        }
    }





    void raceChoicesConfirmed(List<String> choices) {
        commitRaceSelection(choices);
    }

    private void commitRaceSelection(List<String> choices) {
        character.getIdentity().setRace(selectedRace.getName());
        character.getIdentity().setCharRacePick(new java.util.ArrayList<>(choices));
        parent.setStepConfirmed(1);
        dispose();
    }

    private void preloadRaceDisplayData() {
        for (int i = 0; i < RACEOPTIONS.length; i++) {
            String raceName = RACEOPTIONS[i];
            DataRace race = dataQuery.getRaceByName(raceName);
            if (race == null) continue;

            DataColor color = dataQuery.getColorByTitle(raceName);
            Color bg = color != null ? color.getBackColor() : Color.WHITE;
            Color fg = color != null ? color.getForeColor() : Color.BLACK;

            raceDisplayData[i] = new RaceDisplayData(
                    race,
                    bg,
                    fg,
                    race.getName(),
                    race.getNamett(),
                    race.getHomeworld(),
                    race.getHomeworldtt(),
                    race.getAffiliation(),
                    race.getAffiliationtt(),
                    race.getPhysical(),
                    race.getPhysicaltt(),
                    race.getPersonality(),
                    race.getPersonalitytt(),
                    race.getBaseStatusDesc(),
                    race.getBaseStatusDesctt(),
                    race.getScalingStatusDesc(),
                    race.getScalingStatusDesctt(),
                    race.getRacialDesc(),
                    race.getRacialDesctt(),
                    race.getDescription()
            );
        }
    }

    private record RaceDisplayData(
            DataRace race,
            Color background,
            Color foreground,
            String name,
            String nameTt,
            String homeworld,
            String homeworldTt,
            String affiliation,
            String affiliationTt,
            String physical,
            String physicalTt,
            String personality,
            String personalityTt,
            String baseStatus,
            String baseStatusTt,
            String scalingStatus,
            String scalingStatusTt,
            String racial,
            String racialTt,
            String description
    ) {}*/

}

