package eternity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Completely standalone version of FrameNewRace
 * with no dependency on FrameHelper.
 */
public class FrameNewRace extends JFrame {
    private static final long serialVersionUID = 1;

    DataQuery dataQuery;
    CharData character;
    FrameNew parent;

    // UI components normally provided by FrameHelper
    JLabel headerL;
    JButton[] buttons = new JButton[2];

    // Race UI elements
    private ImageIcon[] raceIcons1, raceIcons2;
    private ArrayList<JRadioButton> raceButtons = new ArrayList<>();

    private JPanel racePanel;
    private JPanel raceDescPanel;
    private JScrollPane racePane;

    private JLabel[] raceDescLabels;
    private JButton[] raceDetailButtons;

    private DataRace selectedRace;

    private static final int DESC_COUNT = 9;

    private static final String[] RACEOPTIONS = {
        "Alteri","Aquata","Ardian","Azuri","Boxlor","Cetryu","Construct","Deckan",
        "En","Evan","Felsh","Felsh Cat","Forven","Gaian","Irdon","Kenti","Kitsune",
        "Loben","Loritho","Nohmen","Nosfer","Oon","Poruuk","Quez","Raigon","Reven",
        "Skren","Theran","Vindis","Vyrek","Xid","Zyan"
    };

    public FrameNewRace(FrameSheet sheetFrame, DataQuery dataQuery,
                         CharData character, FrameNew parent) {
        super("Select Class");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent    = parent;

        setLayout(null);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        buildHeader();
        buildButtons();
        loadIcons();
        buildRaceListPanel();
        buildRaceDescriptionPanel();
    }

    /* ============================================================
        HEADER + BUTTONS (replacing FrameHelper features)
       ============================================================ */
    private void buildHeader() {
        headerL = new JLabel("Race Select", SwingConstants.CENTER);
        headerL.setBounds(0, 5, 600, 30);
        headerL.setFont(new Font("Arial", Font.BOLD, 20));
        add(headerL);
    }

    private void buildButtons() {
        // Back/Clear
        buttons[0] = new JButton("Clear / Back");
        buttons[0].setBounds(25, 320, 120, 30);
        buttons[0].addActionListener(e -> raceClear());
        add(buttons[0]);

        // Next
        buttons[1] = new JButton("Next >>>");
        buttons[1].setBounds(455, 320, 120, 30);
        buttons[1].addActionListener(e -> openRacePicker());
        buttons[1].setVisible(false);
        add(buttons[1]);
    }

    /* ============================================================
        ICON LOADING + RESIZING (from earlier message)
       ============================================================ */
    private ImageIcon loadAndResizeIcon(String path, int w, int h) {
        ImageIcon base = new ImageIcon(path);

        if (base.getIconWidth() <= 0) {
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }

        Image scaled = base.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void loadIcons() {
        raceIcons1 = new ImageIcon[RACEOPTIONS.length];
        raceIcons2 = new ImageIcon[RACEOPTIONS.length];

        for (int i = 0; i < RACEOPTIONS.length; i++) {
            String base = "Images\\" + RACEOPTIONS[i];
            raceIcons1[i] = loadAndResizeIcon(base + "1.png", 100, 100);
            raceIcons2[i] = loadAndResizeIcon(base + "2.png", 100, 100);
        }
    }

    /* ============================================================
        RACE LIST PANEL
       ============================================================ */
    private void buildRaceListPanel() {
        racePanel = new JPanel(null);
        racePanel.setPreferredSize(new Dimension(170, 130 * RACEOPTIONS.length + 20));

        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < RACEOPTIONS.length; i++) {
            JRadioButton rb = new JRadioButton();
            rb.setIcon(raceIcons1[i]);
            rb.setSelectedIcon(raceIcons2[i]);
            rb.setBounds(10, 10 + 130 * i, 100, 100);
            rb.setOpaque(false);
            rb.addActionListener(e -> updateRaceData());

            group.add(rb);
            raceButtons.add(rb);
            racePanel.add(rb);

            JLabel label = new JLabel(RACEOPTIONS[i]);
            label.setBounds(10, 110 + 130 * i, 120, 20);
            racePanel.add(label);
        }

        racePane = new JScrollPane(racePanel);
        racePane.setBounds(25, 50, 170, 250);
        racePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        racePane.getVerticalScrollBar().setUnitIncrement(15);

        add(racePane);
    }

    /* ============================================================
        DESCRIPTION PANEL
       ============================================================ */
    private void buildRaceDescriptionPanel() {
        raceDescPanel = new JPanel(null);
        raceDescPanel.setBounds(220, 50, 350, 250);
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
                int y = 60 + row * 45;

                lbl.setBounds(x, y - 20, 140, 20);
                btn.setBounds(x, y, 140, 20);
            } else {
                int y = 155 + (i - 7) * 45;
                lbl.setBounds(15, y - 20, 300, 20);
                btn.setBounds(15, y, 300, 20);
            }
        }

        add(raceDescPanel);
    }

    /* ============================================================
        LOGIC
       ============================================================ */
    private void raceClear() {
        this.setVisible(false);
        //parent.raceClear();
    }

    private int getSelectedRaceIndex() {
        for (int i = 0; i < raceButtons.size(); i++)
            if (raceButtons.get(i).isSelected())
                return i;
        return -1;
    }

    private void setRaceDetail(int index, String text, String tt) {
        raceDetailButtons[index].setText(text);
        raceDetailButtons[index].setToolTipText(tt);
        raceDescLabels[index].setToolTipText(tt);
    }

    void updateRaceData() {
        int index = getSelectedRaceIndex();
        if (index < 0) return;

        selectedRace = dataQuery.getRaceByName(RACEOPTIONS[index]);
        DataColor color = dataQuery.getColorByTitle(RACEOPTIONS[index]);

        Color bg = color.getBackColor();
        Color fg = color.getForeColor();

        raceDescPanel.setBackground(bg);

        setRaceDetail(0, selectedRace.getName(), selectedRace.getNamett());
        setRaceDetail(1, selectedRace.getHomeworld(), selectedRace.getHomeworldtt());
        setRaceDetail(2, selectedRace.getAffiliation(), selectedRace.getAffiliationtt());
        setRaceDetail(3, selectedRace.getPhysical(), selectedRace.getPhysicaltt());
        setRaceDetail(4, selectedRace.getPersonality(), selectedRace.getPersonalitytt());
        setRaceDetail(5, selectedRace.getBaseStatusDesc(), selectedRace.getBaseStatusDesctt());
        setRaceDetail(6, selectedRace.getScalingStatusDesc(), selectedRace.getScalingStatusDesctt());
        setRaceDetail(7, selectedRace.getRacialDesc(), selectedRace.getRacialDesctt());
        setRaceDetail(8, "A brief history...", selectedRace.getDescription());

        for (int i = 0; i < DESC_COUNT; i++) {
            raceDetailButtons[i].setBackground(fg);
            raceDetailButtons[i].setForeground(bg);
            raceDescLabels[i].setForeground(fg);
        }

        raceDescPanel.setVisible(true);
        buttons[1].setVisible(true);
    }

    void openRacePicker() {
        if (selectedRace == null) return;

        if (selectedRace.getRacePick()) {
            /*FrameNewRacePicker picker =
                new FrameNewRacePicker(null, dataStore, character, selectedRace, this);
            picker.setVisible(true);*/
        } else {
            raceChoicesConfirmed(new ArrayList<>());
        }
    }

    void raceChoicesConfirmed(ArrayList<String> choices) {
        /*character.setCharRace(selectedRace.getName());
        character.setRacePick(choices);
        parent.raceConfirmed();*/
    }

}
