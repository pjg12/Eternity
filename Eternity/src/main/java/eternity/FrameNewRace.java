package eternity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class FrameNewRace extends JFrame {
    private static final long serialVersionUID = 1L;
    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private static final int ICON_SIZE = 80;
    private static final String[] RACEOPTIONS = { "Alteri","Aquata","Ardian","Azuri","Boxlor","Cetryu","Construct","Deckan",
        "En","Evan","Felsh","Felsh Cat","Forven","Gaian","Irdon","Kenti","Kitsune", "Loben","Loritho","Nohmen","Nosfer","Oon",
        "Poruuk","Quez","Raigon","Reven", "Skren","Theran","Vindis","Vyrek","Xid","Zyan" };

    private static final int DESC_COUNT = 9;
    private static final List<String> EMPTY_CHOICES = List.of();

    private static ImageIcon[] raceIcons1, raceIcons2;
    private static boolean iconsLoaded = false;
    private final RaceDisplayData[] raceDisplayData = new RaceDisplayData[RACEOPTIONS.length];
    private JButton[] raceButtons;
    private int selectedIndex = -1;

    private JLabel headerL;
    private JButton clearButton;
    private JButton nextButton;

    private JLabel[] raceDescLabels;
    private JButton[] raceDetailButtons;
    private JPanel raceDescPanel;

    private DataRace selectedRace;

    public FrameNewRace(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent, boolean gmMode) {
        super("Select Race");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent    = parent;
        this.gmMode    = gmMode;

        loadIcons();
        preloadRaceDisplayData();
        buildWindow();

        setSize(550, 450);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // ---------------------------------------------------
    // Load Icons
    // ---------------------------------------------------
    private static synchronized void loadIcons() {
        if (iconsLoaded) return;
        raceIcons1 = new ImageIcon[RACEOPTIONS.length];
        raceIcons2 = new ImageIcon[RACEOPTIONS.length];

        for (int i = 0; i < RACEOPTIONS.length; i++) {
            raceIcons1[i] = scaleIcon(
                    new ImageIcon("Images/" + RACEOPTIONS[i] + "1.png"),
                    ICON_SIZE, ICON_SIZE
            );
            raceIcons2[i] = scaleIcon(
                    new ImageIcon("Images/" + RACEOPTIONS[i] + "2.png"),
                    ICON_SIZE, ICON_SIZE
            );
        }
        iconsLoaded = true;
    }

    private static ImageIcon scaleIcon(ImageIcon src, int w, int h) {
        Image img = src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // ---------------------------------------------------
    // Layout
    // ---------------------------------------------------
    private void buildWindow() {
        setLayout(new BorderLayout());

        add(buildLeftList(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ---------------------------------------------------
    // LEFT COLUMN: Vertical list of icon buttons
    // ---------------------------------------------------
    private JComponent buildLeftList() {

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        raceButtons = new JButton[RACEOPTIONS.length];

        for (int i = 0; i < RACEOPTIONS.length; i++) {
            JButton btn = new JButton(raceIcons1[i]);
            btn.setRolloverIcon(raceIcons2[i]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);

            final int index = i;
            btn.addActionListener(e -> onRaceSelected(index));

            raceButtons[i] = btn;

            // Label under each button
            JLabel lbl = new JLabel(RACEOPTIONS[i], SwingConstants.CENTER);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setBorder(new EmptyBorder(4, 0, 10, 0));

            listPanel.add(btn);
            listPanel.add(lbl);
        }

        JScrollPane pane = new JScrollPane(listPanel);
        pane.setPreferredSize(new Dimension(140, 400));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        pane.getVerticalScrollBar().setUnitIncrement(15);

        return pane;
    }

    // ---------------------------------------------------
    // RIGHT COLUMN: Race detail panel
    // ---------------------------------------------------
    private JComponent buildRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        rightPanel.add(buildHeader(), BorderLayout.NORTH);
        rightPanel.add(buildRaceDescriptionPanel(), BorderLayout.CENTER);

        return rightPanel;
    }

    private JComponent buildHeader() {
        headerL = new JLabel("Race Select", SwingConstants.CENTER);
        headerL.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerL, BorderLayout.CENTER);
        return headerPanel;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));

        clearButton = new JButton("Clear / Back");
        clearButton.addActionListener(e -> raceClear());

        nextButton = new JButton("Next >>>");
        nextButton.addActionListener(e -> openRacePicker());
        nextButton.setEnabled(false);

        footer.add(clearButton);
        footer.add(nextButton);

        return footer;
    }

    /* ============================================================
        DESCRIPTION PANEL
       ============================================================ */
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
       ============================================================ */
    private void raceClear() {
        selectedRace = null;
        raceDescPanel.setVisible(false);
        nextButton.setEnabled(false);
        setVisible(false);
        //parent.raceClear();
    }

    private void onRaceSelected(int index) {
        if (selectedIndex == index) return;
        selectedIndex = index;
        updateRaceData(index);
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

    void openRacePicker() {
        if (selectedRace == null) return;

        if (selectedRace.getRacePick()) {
            FrameNewRacePicker picker =
                new FrameNewRacePicker(null, dataQuery, character, selectedRace, this);
            picker.setVisible(true);
        } else {
            raceChoicesConfirmed(EMPTY_CHOICES);
        }
    }

    void raceChoicesConfirmed(List<String> choices) {
        character.getIdentity().setRace(selectedRace.getName());
        character.getIdentity().setCharRacePick(new java.util.ArrayList<>(choices));
        parent.raceConfirmed();
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
    ) {}

}
