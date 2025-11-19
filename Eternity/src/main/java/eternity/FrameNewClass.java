package eternity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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

        primaryAtt = normalLabel("-");
        role       = normalLabel("-");
        
        subclass1 = normalLabel("-");
        subclass2 = normalLabel("-");

        armor      = normalLabel("-");
        hpScale    = normalLabel("-");
        auraScale  = normalLabel("-");
        
        secondaryAtt1 = normalLabel("-");
        secondaryAtt2 = normalLabel("-");
        proficiency = normalLabel("-");

        fort = normalLabel("-");
        ref  = normalLabel("-");
        will = normalLabel("-");
        atk  = normalLabel("-");

        right.add(className);
        right.add(Box.createVerticalStrut(10));

        // Info grid: two columns
        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 12, 6));
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoGrid.add(infoRow("Primary Attribute:", primaryAtt));
        infoGrid.add(infoRow("Role:", role));

        infoGrid.add(infoRow("Subclass 1:", subclass1));
        infoGrid.add(infoRow("Subclass 2:", subclass2));


        infoGrid.add(infoRow("Armor Type:", armor));
        infoGrid.add(infoRow("HP Scaling:", hpScale));

        infoGrid.add(infoRow("Aura Scaling:", auraScale));
        infoGrid.add(infoRow("Proficiency:", proficiency));

        

        infoGrid.add(infoRow("Secondary Attribute 1:", secondaryAtt1));
        infoGrid.add(infoRow("Secondary Attribute 2:", secondaryAtt2));

        infoGrid.add(infoRow("FORT:", fort));
        infoGrid.add(infoRow("REF:", ref));

        infoGrid.add(infoRow("WILL:", will));
        infoGrid.add(infoRow("ATK:", atk));

        right.add(infoGrid);

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
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (value instanceof JLabel) ((JLabel) value).setHorizontalAlignment(SwingConstants.CENTER);
        value.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(value);
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
        className.setToolTipText(selectedClass.getDescription());
        primaryAtt.setText(selectedClass.getPrimaryAtt());
        role.setText(selectedClass.getRole());

        // Subclasses (DataStore uses sequential IDs for subclasses)
        int id = selectedClass.getID();
        DataClass sub1 = dataQuery.getClassById(id + 1);
        DataClass sub2 = dataQuery.getClassById(id + 2);
        subclass1.setText(sub1 != null ? sub1.getName() : "-");
        subclass2.setText(sub2 != null ? sub2.getName() : "-");

        armor.setText(selectedClass.getArmor());
        hpScale.setText((int)(selectedClass.getHpScaling() * 100) + "%");
        auraScale.setText((int)(selectedClass.getAuraScaling() * 100) + "%");

        

        // Secondary attributes: primary secondaryAtt provided in DataClass
        secondaryAtt1.setText(selectedClass.getSecondaryAtt());
        secondaryAtt2.setText("-");

        // Proficiency label
        proficiency.setText(selectedClass.getProfLabel());

        // Stat scaling: [FORT, REF, WILL, ATK]
        int[] scaling = selectedClass.getStatScaling();
        if (scaling != null && scaling.length >= 4) {
            fort.setText(Integer.toString(scaling[0]));
            ref.setText(Integer.toString(scaling[1]));
            will.setText(Integer.toString(scaling[2]));
            atk.setText(Integer.toString(scaling[3]));
        } else {
            fort.setText("-"); ref.setText("-"); will.setText("-"); atk.setText("-");
        }

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
