package eternity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

public class FrameNewClass extends JFrame {
    private static final long serialVersionUID = 1L;
    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private FrameNewClassPicker picker;
    private Color bg;
    private Color fg;

    private static final int ICON_SIZE = 80;
    private static final int INFO_TITLE_COUNT = 14;

    private static final String[] CLASSOPTIONS = { "Warrior", "Paladin", "Rogue", "Monk", "Archer", "Leader", "Cleric", "Caster", "Shifter", "Pilot" };
    private final ClassDisplayData[] classDisplayData = new ClassDisplayData[CLASSOPTIONS.length];

    private JButton[] classButtons;
    private static ImageIcon[] iconsNormal;
    private static ImageIcon[] iconsHover;
    private static boolean iconsLoaded = false;
    private int selectedIndex = -1;

    // Right-side info panel
    private JPanel right, nameGrid;
    private JPanel[] infoTitleBox;
    private JLabel[] infoTitle;
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
    public FrameNewClass(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent, boolean gmMode) {
        super("Select Class");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;
        infoTitleBox = new JPanel[INFO_TITLE_COUNT];
        infoTitle = new JLabel[INFO_TITLE_COUNT];

        loadIcons();
        preloadClassDisplayData();
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
        infoTitleBox[nextInfoTitleIndex] = t;

        JLabel tLabel = new JLabel(title, SwingConstants.CENTER);
        tLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        t.add(tLabel);
        t.add(Box.createVerticalStrut(4));
        infoTitle[nextInfoTitleIndex] = tLabel;
        nextInfoTitleIndex++;

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
        if (selectedIndex == index) return;
        selectedIndex = index;

        ClassDisplayData display = classDisplayData[index];
        selectedClass = display.selectedClass;
        bg = display.background;
        fg = display.foreground;

        className.setText(display.className);
        className.setToolTipText(display.description);
        primaryAtt.setText(display.primaryAtt);
        role.setText(display.role);
        subclass1.setText(display.subclass1);
        subclass2.setText(display.subclass2);
        secondaryAtt1.setText(display.secondaryAtt1);
        secondaryAtt2.setText(display.secondaryAtt2);
        hpScale.setText(display.hpScale);
        auraScale.setText(display.auraScale);
        armor.setText(display.armor);
        proficiency.setText(display.proficiency);
        fort.setText(display.fort);
        ref.setText(display.ref);
        will.setText(display.will);
        atk.setText(display.atk);

        right.setBackground(display.background);
        right.setForeground(display.foreground);
        for (int i = 0; i < INFO_TITLE_COUNT; i++) {
            infoTitleBox[i].setBackground(display.background);
            infoTitle[i].setForeground(display.foreground);
        }

        if (!confirmButton.isEnabled()) {
            confirmButton.setEnabled(true);
        }

        if (gmMode) {
            classChoicesConfirmed();
        }
    }

    public void classChoicesConfirmed() {
        character.getIdentity().setCharClass(selectedClass.getName());
        parent.classConfirmed();                     // notify FrameNew
        dispose();
    }

    private int nextInfoTitleIndex = 0;

    private void preloadClassDisplayData() {
        for (int i = 0; i < CLASSOPTIONS.length; i++) {
            String className = CLASSOPTIONS[i];
            DataClass dataClass = dataQuery.getClassByName(className);
            DataColor color = dataQuery.getColorByTitle(className);
            if (dataClass == null || color == null) continue;

            int id = dataClass.getID();
            DataClass sub1 = dataQuery.getClassById(id + 1);
            DataClass sub2 = dataQuery.getClassById(id + 2);
            String[] scaling = resolveScalingText(dataClass.getStatScaling());

            classDisplayData[i] = new ClassDisplayData(
                    dataClass,
                    color.getBackColor(),
                    color.getForeColor(),
                    dataClass.getName(),
                    dataClass.getDescription(),
                    dataClass.getPrimaryAtt(),
                    dataClass.getRole(),
                    sub1 != null ? sub1.getName() : "-",
                    sub2 != null ? sub2.getName() : "-",
                    sub1 != null ? sub1.getSecondaryAtt() : "-",
                    sub2 != null ? sub2.getSecondaryAtt() : "-",
                    (int) (dataClass.getHpScaling() * 100) + "%",
                    (int) (dataClass.getAuraScaling() * 100) + "%",
                    dataClass.getArmor(),
                    dataClass.getProfLabel(),
                    scaling[0],
                    scaling[1],
                    scaling[2],
                    scaling[3]
            );
        }
    }

    private static String[] resolveScalingText(int[] scaling) {
        String[] scalText = {"Bad", "Bad", "Bad", "Bad"};
        for (int i = 0; i < 4; i++) {
            if (scaling != null && i < scaling.length) {
                if (scaling[i] == 1) scalText[i] = "Good";
                else if (scaling[i] == 2) scalText[i] = "Average";
            }
        }
        return scalText;
    }

    private record ClassDisplayData(
            DataClass selectedClass,
            Color background,
            Color foreground,
            String className,
            String description,
            String primaryAtt,
            String role,
            String subclass1,
            String subclass2,
            String secondaryAtt1,
            String secondaryAtt2,
            String hpScale,
            String auraScale,
            String armor,
            String proficiency,
            String fort,
            String ref,
            String will,
            String atk
    ) {}
}
