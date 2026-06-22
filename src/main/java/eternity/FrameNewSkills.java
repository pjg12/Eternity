package eternity;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Skill selection window: pick 3 skills, each tied to an attribute.
 */
public class FrameNewSkills extends JFrame {
    // References
    private final FrameSheet sheetFrame;
    private final StoreRuleManager ruleManager;
    private final StoreCharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    // UI Constants
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder LEFT_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 10, 2, 10);
    private static final Insets RIGHT_GB_INSETS = new Insets(2, 5, 5, 5);
    private static final int[] GB_COLUMN_WIDTHS = new int[] { 70, 95, 170, 130 };
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 360;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int BUTTON_SPACING = 10;
    private static final int SKILL_COUNT = 3;

    // UI Strings
    private static final String WINDOW_TITLE = "Skill Selection";
    private static final String HEADER_CORE_TEXT = "Select Skills";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_CONFIRM = "Confirm";
    private static final String[] ATTRIBUTES = {"***", "STR", "DEX", "FOC", "CTL", "KNOW", "MECH", "PERC", "CHA", "SUB"};
    private static final String EMPTY_OPTION = "***";

    // UI Elements
    private JPanel headerPanel, centerPanel, footerPanel;
    private JLabel headerL;
    private JButton cancelButton, confirmButton;
    private final JComboBox<String>[] attBoxes = new JComboBox[SKILL_COUNT];
    private final JComboBox<String>[] skillBoxes = new JComboBox[SKILL_COUNT];
    private final JTextField[] subtypeFields = new JTextField[SKILL_COUNT];
    private boolean updatingSkillLists;


    

    
    



    // ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameNewSkills(FrameSheet sheetFrame, StoreRuleManager ruleManager, StoreCharData character, FrameNew parent, boolean gmMode) {
        super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        this.ruleManager = ruleManager;
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

        /*buildHeader();
        buildLabels();
        buildPickers();
        buildButtons();*/
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
        // Build layout
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = GB_COLUMN_WIDTHS;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = RIGHT_GB_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Build panel
        centerPanel = new JPanel(layout);

        // Setup Variables
        int y = 0;
        int x = 0;
        int width = 1;
        gridHelper(gbc, y, x, width);

        // Blank Label
        JLabel lbl = buildLabel(" ");
        centerPanel.add(lbl, gbc);

        x++;
        gridHelper(gbc, y, x, width);

        // Attribute Label
        lbl = buildLabel("Attribute");
        centerPanel.add(lbl, gbc);

        x++;
        gridHelper(gbc, y, x, width);

        // Skill Label
        lbl = buildLabel("Skill");
        centerPanel.add(lbl, gbc);

        x++;
        gridHelper(gbc, y, x, width);

        // Subtype Label
        lbl = buildLabel("Subtype");
        centerPanel.add(lbl, gbc);

        for (int i = 0; i < SKILL_COUNT; i++) {
            final int index = i;
            y = i +1;
            x = 0;
            gridHelper(gbc, y, x, width);

            JLabel sLabel = new JLabel("Skill " + i + ":");
            centerPanel.add(sLabel, gbc);

            x++;
            gridHelper(gbc, y, x, width);

            attBoxes[i] = buildAttributeBox(index);
            centerPanel.add(attBoxes[i], gbc);

            x++;
            gridHelper(gbc, y, x, width);

            skillBoxes[i] = buildSkillBox(index);
            centerPanel.add(skillBoxes[i], gbc);

            x++;
            gridHelper(gbc, y, x, width);

            subtypeFields[i] = buildSubtypeField();
            centerPanel.add(subtypeFields[i], gbc);
        }

        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildFooter() {
        // Build panel
        footerPanel = new JPanel();
        footerPanel.setBorder(FOOTER_BORDER);

        // Build cancel button
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        footerPanel.add(cancelButton, BorderLayout.WEST);
        
        // Build next button
        confirmButton = new JButton(BUTTON_CONFIRM);
        confirmButton.addActionListener(e -> onConfirmPressed());
        footerPanel.add(confirmButton, BorderLayout.EAST);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel buildLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JComboBox<String> buildComboBox(String[] values) {
        JComboBox<String> box = new JComboBox<>(values);
        return box;
    }

    private JComboBox<String> buildAttributeBox(int index) {
        JComboBox<String> box = buildComboBox(ATTRIBUTES);
        box.addActionListener(e -> onSelectionChanged(index));
        return box;
    }

    private JComboBox<String> buildSkillBox(int index) {
        JComboBox<String> box = buildComboBox(new String[] { EMPTY_OPTION });
        box.addActionListener(e -> onSelectionChanged(index));
        return box;
    }

    private JTextField buildSubtypeField() {
        JTextField field = new JTextField();
        field.setVisible(false);
        return field;
    }

    // --------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------

    private void gridHelper (GridBagConstraints gbc, int y, int x, int width) {
        gbc.gridwidth = width;
        gbc.gridy = y;
        gbc.gridx = x;
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------

    private void onCancelPressed() {
        dispose();
    }

    public void onConfirmPressed() {
        if (gmMode) {
            applyGmSelections();
        }

        if (!validateSkills()) {
            JOptionPane.showMessageDialog(this, "Please ensure all skills have been chosen and are unique.");
            return;
        }

        ArrayList<DataSkill> skills = new ArrayList<>();
        for (int i = 0; i < SKILL_COUNT; i++) {
            DataSkill baseSkill = ruleManager.getSkillByName((String) skillBoxes[i].getSelectedItem());
            if (baseSkill == null) continue;

            DataSkill newSkill = new DataSkill(baseSkill);
            newSkill.setChosenAttributes(new ArrayList<>());
            newSkill.addChosenAttribute((String) attBoxes[i].getSelectedItem());
            if (baseSkill.requiresSubtype()) {
                newSkill.setChosenSubtype(subtypeFields[i].getText());
            }
            skills.add(newSkill);
        }

        character.getSpecials().setSkills(skills);

        parent.setStepConfirmed(3);
        dispose();
    }

    private boolean validateSkills() {
        String att1, att2, skill1, skill2;

        // Validate no unchosen skills
        for (int i = 0; i < SKILL_COUNT; i++) {
            skill1 = (String) skillBoxes[i].getSelectedItem();
            if (skill1.compareTo(EMPTY_OPTION) == 0) return false;
            if (requiresSubtype(skill1) && subtypeFields[i].getText().trim().isEmpty()) return false;
        }
        
        // Validate each skill unique
        for (int i = 0; i < SKILL_COUNT-1; i++) {
            att1 = (String) attBoxes[i].getSelectedItem();
            for (int j = i+1; j < SKILL_COUNT; j++) {
                att2 = (String) attBoxes[j].getSelectedItem();
                if (att1.compareTo(att2) != 0) continue;
                skill1 = (String) skillBoxes[i].getSelectedItem();
                skill2 = (String) skillBoxes[j].getSelectedItem();
                if (skill1.compareTo(skill2) == 0) return false;
            }
        }

        return true;
    }

    private void updateSkillList(int index) {
        JComboBox<String> sbox = skillBoxes[index];
        String att = (String)attBoxes[index].getSelectedItem();
        String skill = (String)skillBoxes[index].getSelectedItem();
        
        sbox.removeAllItems();
        sbox.addItem(EMPTY_OPTION);

        Set<String> filter = updateFilter(index);

        List<DataSkill> options = ruleManager.getSkillsByAttribute(att);
        if (options == null || options.isEmpty()) return;

        for (DataSkill opt : options) {
            if (filter == null || !filter.contains(opt.getName())) sbox.addItem(opt.getName());
            if (skill != null && opt.getName().compareTo(skill) ==0) sbox.setSelectedItem(skill);
        }
    }

    private void refreshAllSkillLists() {
        updatingSkillLists = true;
        try {
            for (int i = 0; i < SKILL_COUNT; i++) {
                updateSkillList(i);
            }
        } finally {
            updatingSkillLists = false;
        }
        refreshSubtypeFields();
    }

    private void onSelectionChanged(int index) {
        if (updatingSkillLists) return;
        refreshAllSkillLists();
    }

    private void refreshSubtypeFields() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            updateSubtypeFieldVisibility(i);
        }
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void updateSubtypeFieldVisibility(int index) {
        JTextField field = subtypeFields[index];
        if (field == null) return;
        boolean show = requiresSubtype((String) skillBoxes[index].getSelectedItem());
        field.setVisible(show);
    }

    private boolean requiresSubtype(String skillName) {
        DataSkill skill = ruleManager.getSkillByName(skillName);
        return skill != null && skill.requiresSubtype();
    }

    private Set<String> updateFilter(int index) {
        Set<String> list = new HashSet<>();
        String att = (String)attBoxes[index].getSelectedItem();

        for (int i = 0; i < SKILL_COUNT; i++) {
            if (i == index) continue;
            if (att.compareTo((String)attBoxes[i].getSelectedItem()) != 0) continue;
            list.add((String)skillBoxes[i].getSelectedItem());
        }
        if (!list.isEmpty()) return list;
        return null;
    }

    private void applyGmSelections() {
        String[] preferredAttributes = {"STR", "DEX", "CTL"};
        Set<String> usedSkills = new HashSet<>();

        updatingSkillLists = true;
        try {
            for (int i = 0; i < SKILL_COUNT; i++) {
                String preferredAttribute = preferredAttributes[i];
                if (hasAvailableSkill(preferredAttribute, usedSkills)) {
                    attBoxes[i].setSelectedItem(preferredAttribute);
                    continue;
                }

                for (String attribute : ATTRIBUTES) {
                    if (EMPTY_OPTION.equals(attribute)) continue;
                    if (!hasAvailableSkill(attribute, usedSkills)) continue;
                    attBoxes[i].setSelectedItem(attribute);
                    break;
                }
            }

            for (int i = 0; i < SKILL_COUNT; i++) {
                updateSkillList(i);
                String selectedSkill = findFirstAvailableSkill(i, usedSkills);
                if (selectedSkill != null) {
                    skillBoxes[i].setSelectedItem(selectedSkill);
                    usedSkills.add(selectedSkill);
                }
            }
        } finally {
            updatingSkillLists = false;
        }
    }

    private boolean hasAvailableSkill(String attribute, Set<String> usedSkills) {
        List<DataSkill> options = ruleManager.getSkillsByAttribute(attribute);
        if (options == null || options.isEmpty()) return false;

        for (DataSkill option : options) {
            if (option == null || option.getName() == null) continue;
            if (!usedSkills.contains(option.getName())) return true;
        }
        return false;
    }

    private String findFirstAvailableSkill(int index, Set<String> usedSkills) {
        JComboBox<String> skillBox = skillBoxes[index];
        for (int i = 0; i < skillBox.getItemCount(); i++) {
            String option = skillBox.getItemAt(i);
            if (option == null || EMPTY_OPTION.equals(option)) continue;
            if (!usedSkills.contains(option)) return option;
        }
        return null;
    }
}
