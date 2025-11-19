package eternity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class FrameNewClass extends JFrame {

    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;
    private FrameNewClassPicker picker;

    private static final int ICON_SIZE = 80;

    private final String[] CLASSOPTIONS = {
            "Warrior", "Paladin", "Rogue", "Monk", "Archer",
            "Leader", "Cleric", "Caster", "Shifter", "Pilot"
    };

    private JButton[] classButtons;
    private ImageIcon[] iconsNormal;
    private ImageIcon[] iconsHover;
    private int selectedIndex = -1;

    // Right-side info panel
    private JLabel className;
    private JTextArea classDesc;
    private JLabel primaryAtt;
    private JLabel role;
    private JLabel armor;
    private JLabel hpScale;
    private JLabel auraScale;

    private JButton confirmButton;
    private JButton cancelButton;

    // Selected class data
    private DataClass selectedClass;

    // ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameNewClass(FrameSheet sheetFrame, DataQuery dataQuery,
                         CharData character, FrameNew parent) {
        super("Select Class");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;

        loadIcons();
        buildWindow();

        setSize(650, 450);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // ---------------------------------------------------
    // Load Icons
    // ---------------------------------------------------
    private void loadIcons() {
        iconsNormal = new ImageIcon[CLASSOPTIONS.length];
        iconsHover  = new ImageIcon[CLASSOPTIONS.length];

        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            iconsNormal[i] = scaleIcon(
                    new ImageIcon("eternity/images/" + CLASSOPTIONS[i] + "1.png"),
                    ICON_SIZE, ICON_SIZE
            );
            iconsHover[i] = scaleIcon(
                    new ImageIcon("eternity/images/" + CLASSOPTIONS[i] + "2.png"),
                    ICON_SIZE, ICON_SIZE
            );
        }
    }

    private ImageIcon scaleIcon(ImageIcon src, int w, int h) {
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

        classButtons = new JButton[CLASSOPTIONS.length];

        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            JButton btn = new JButton(iconsNormal[i]);
            btn.setRolloverIcon(iconsHover[i]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);

            final int index = i;
            btn.addActionListener(e -> onClassSelected(index));

            classButtons[i] = btn;

            // Label under each button
            JLabel lbl = new JLabel(CLASSOPTIONS[i], SwingConstants.CENTER);
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
    // RIGHT COLUMN: Class detail panel
    // ---------------------------------------------------
    private JComponent buildRightPanel() {

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(20, 20, 20, 20));

        className = bigLabel("-");
        classDesc = new JTextArea(4, 30);
        classDesc.setLineWrap(true);
        classDesc.setWrapStyleWord(true);
        classDesc.setEditable(false);
        classDesc.setBackground(new Color(245, 245, 245));
        classDesc.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        primaryAtt = normalLabel("-");
        role       = normalLabel("-");
        armor      = normalLabel("-");
        hpScale    = normalLabel("-");
        auraScale  = normalLabel("-");

        right.add(className);
        right.add(Box.createVerticalStrut(10));
        right.add(new JScrollPane(classDesc));
        right.add(Box.createVerticalStrut(10));

        right.add(infoRow("Primary Attribute:", primaryAtt));
        right.add(infoRow("Role:", role));
        right.add(infoRow("Armor Type:", armor));
        right.add(infoRow("HP Scaling:", hpScale));
        right.add(infoRow("Aura Scaling:", auraScale));

        return right;
    }

    private JLabel bigLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 20f));
        return lbl;
    }

    private JLabel normalLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(lbl.getFont().deriveFont(14f));
        return lbl;
    }

    private JPanel infoRow(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(value, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(4, 0, 4, 0));
        return p;
    }

    // ---------------------------------------------------
    // FOOTER BUTTONS
    // ---------------------------------------------------
    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));

        cancelButton = new JButton("Cancel");
        confirmButton = new JButton("Next →");
        confirmButton.setEnabled(false);

        cancelButton.addActionListener(e -> dispose());

        confirmButton.addActionListener(e -> {
            if (selectedClass != null) {
                this.picker = new FrameNewClassPicker(dataQuery, character, selectedClass, this);
                //parent.classConfirmed();                     // notify FrameNew
                //dispose();
            }
        });

        footer.add(cancelButton);
        footer.add(confirmButton);

        return footer;
    }

    // ---------------------------------------------------
    // CLASS SELECTED
    // ---------------------------------------------------
    private void onClassSelected(int index) {
        selectedIndex = index;

        selectedClass = dataQuery.getClassByName(CLASSOPTIONS[index]);
        DataColor color = dataQuery.getColorByTitle(CLASSOPTIONS[index]);

        className.setText(selectedClass.getName());
        classDesc.setText(selectedClass.getDescription());
        primaryAtt.setText(selectedClass.getPrimaryAtt());
        role.setText(selectedClass.getRole());
        armor.setText(selectedClass.getArmor());
        hpScale.setText((int)(selectedClass.getHpScaling() * 100) + "%");
        auraScale.setText((int)(selectedClass.getAuraScaling() * 100) + "%");

        //color background
        getContentPane().setBackground(color.getBackColor());

        confirmButton.setEnabled(true);
    }

    public void classChoicesConfirmed() {
        character.getIdentity().setCharClass(selectedClass.getName());
        //parent.classConfirmed();                     // notify FrameNew
        dispose();
    }
}
