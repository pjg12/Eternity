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
    private Color bg;
    private Color fg;

    private static final int ICON_SIZE = 80;

    private final String[] CLASSOPTIONS = { "Warrior", "Paladin", "Rogue", "Monk", "Archer", "Leader", "Cleric", "Caster", "Shifter", "Pilot" };

    private JButton[] classButtons;
    private ImageIcon[] iconsNormal;
    private ImageIcon[] iconsHover;
    private int selectedIndex = -1;

    // Right-side info panel
    private JPanel right, nameGrid;
    private ArrayList<JPanel> infoTitleBox;
    private ArrayList<JLabel> infoTitle;
    private JLabel className;
    private JLabel primaryAtt;
    private JLabel role;
    private JLabel armor;
    private JLabel hpScale;
    private JLabel auraScale;
    private JLabel subclass1;
    private JLabel subclass2;
    private JLabel secondaryAtt1;
    private JLabel secondaryAtt2;
    private JLabel proficiency;
    private JLabel fort;
    private JLabel ref;
    private JLabel will;
    private JLabel atk;

    private JButton confirmButton;
    private JButton cancelButton;

    // Selected class data
    private DataClass selectedClass;

    // ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameNewClass(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent) {
        super("Select Class");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        infoTitleBox = new ArrayList<>();
        infoTitle = new ArrayList<>();

        loadIcons();
        buildWindow();

        setSize(550, 450);
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
                    new ImageIcon("images/" + CLASSOPTIONS[i] + "1.png"),
                    ICON_SIZE, ICON_SIZE
            );
            iconsHover[i] = scaleIcon(
                    new ImageIcon("images/" + CLASSOPTIONS[i] + "2.png"),
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

        right = new JPanel(new GridBagLayout());
        right.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        className = bigLabel("-");
        className.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        nameGrid = new JPanel();
        nameGrid.add(className);
        right.add(nameGrid, gbc);

        // Row 1
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        gbc.gridx = 0;
        right.add(infoRow("Primary Attribute:", primaryAtt = normalLabel("-")), gbc);
        gbc.gridx = 2;
        right.add(infoRow("Role:", role = normalLabel("-")), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        right.add(infoRow("Subclass 1:", subclass1 = normalLabel("-")), gbc);
        gbc.gridx = 2;
        right.add(infoRow("Subclass 2:", subclass2 = normalLabel("-")), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        right.add(infoRow("Secondary Attribute 1:", secondaryAtt1 = normalLabel("-")), gbc);
        gbc.gridx = 2;
        right.add(infoRow("Secondary Attribute 2:", secondaryAtt2 = normalLabel("-")), gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 4;
        gbc.gridx = 0;
        right.add(infoRow("HP Scaling:", hpScale = normalLabel("-")), gbc);
        gbc.gridx = 1;
        right.add(infoRow("Aura Scaling:", auraScale = normalLabel("-")), gbc);
        gbc.gridx = 2;
        right.add(infoRow("Armor Type:", armor = normalLabel("-")), gbc);
        gbc.gridx = 3;
        right.add(infoRow("Proficiency:", proficiency = normalLabel("-")), gbc);
        
        gbc.gridy = 5;
        gbc.gridx = 0;
        right.add(infoRow("FORT:", fort = normalLabel("-")), gbc);
        gbc.gridx = 1;
        right.add(infoRow("REF:", ref = normalLabel("-")), gbc);
        gbc.gridx = 2;
        right.add(infoRow("WILL:", will = normalLabel("-")), gbc);
        gbc.gridx = 3;
        right.add(infoRow("ATK:", atk = normalLabel("-")), gbc);
     
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
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel t = new JPanel();
        p.add(t);
        infoTitleBox.add(t);

        JLabel tLabel = new JLabel(title, SwingConstants.CENTER);
        tLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        t.add(tLabel);
        t.add(Box.createVerticalStrut(4));
        infoTitle.add(tLabel);

        JPanel v = new JPanel();
        p.add(v);

        if (value instanceof JLabel) {
            ((JLabel) value).setHorizontalAlignment(SwingConstants.CENTER);
            value.setAlignmentX(Component.CENTER_ALIGNMENT);
            v.add(value);
        }

        p.setBorder(new EmptyBorder(0, 0, 0, 0));
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
        bg = color.getBackColor();
        fg = color.getForeColor();

        className.setText(selectedClass.getName());
        className.setToolTipText(selectedClass.getDescription());

        primaryAtt.setText(selectedClass.getPrimaryAtt());


        role.setText(selectedClass.getRole());

        // Subclasses (DataStore uses sequential IDs for subclasses)
        int id = selectedClass.getID();
        DataClass sub1 = dataQuery.getClassById(id + 1);
        DataClass sub2 = dataQuery.getClassById(id + 2);
        subclass1.setText(sub1 != null ? sub1.getName() : "-");
        subclass2.setText(sub2 != null ? sub2.getName() : "-");

        secondaryAtt1.setText(sub1 != null ? sub1.getSecondaryAtt() : "-");
        secondaryAtt2.setText(sub2 != null ? sub2.getSecondaryAtt() : "-");

        hpScale.setText((int)(selectedClass.getHpScaling() * 100) + "%");
        auraScale.setText((int)(selectedClass.getAuraScaling() * 100) + "%");
        
        armor.setText(selectedClass.getArmor());
        proficiency.setText(selectedClass.getProfLabel());

        // Stat scaling: [FORT, REF, WILL, ATK]
        int[] scaling = selectedClass.getStatScaling();
        String[] scalText = {"Bad", "Bad", "Bad", "Bad"};
        for (int i = 0; i < 4; i++) {
            if (scaling != null && i < scaling.length) {
                if (scaling[i] == 1) scalText[i] = "Good";
                 else if (scaling[i] == 2) scalText[i] = "Average";
            }
        }
        fort.setText(scalText[0]);
        ref.setText(scalText[1]);
        will.setText(scalText[2]);
        atk.setText(scalText[3]);

        //color background
        right.setBackground(color.getBackColor());
        right.setForeground(color.getForeColor());
        for (int i = 0; i < 14; i++) {
            infoTitleBox.get(i).setBackground(color.getBackColor());
            infoTitle.get(i).setForeground(color.getForeColor());
        }

        confirmButton.setEnabled(true);
    }

    public void classChoicesConfirmed() {
        character.getIdentity().setCharClass(selectedClass.getName());
        parent.classConfirmed();                     // notify FrameNew
        dispose();
    }
}
