package eternity;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import java.awt.Font;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Skill selection window: pick 3 skills, each tied to an attribute.
 */
public class FrameNewSkills extends JFrame {
    private static final long serialVersionUID = 1L;

    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private static final String[] ATTRIBUTES = {"***", "STR", "DEX", "FOC", "CTL", "KNOW", "MECH", "PERC", "CHA", "SUB"};
    private static final String EMPTY_OPTION = "***";

    private final JComboBox<String>[] skillAttributes = new JComboBox[3];
    private final JComboBox<String>[] skillPick = new JComboBox[3];
    private final String[] lastSelectedAttribute = new String[3];
    private final Map<String, String[]> skillOptionsByAttribute = new HashMap<>();

    public FrameNewSkills(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent, boolean gmMode) {
        super("Skill Select");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(520, 330);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildHeader();
        buildLabels();
        buildPickers();
        buildButtons();
    }

    private void buildHeader() {
        JLabel headerL = new JLabel("Skill Select", SwingConstants.CENTER);
        headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 20f));
        headerL.setBounds(20, 15, 480, 24);
        add(headerL);
    }

    private void buildLabels() {
        JLabel attrLabel = new JLabel("Attribute");
        attrLabel.setBounds(25, 60, 125, 20);
        add(attrLabel);

        JLabel skillLabel = new JLabel("Skill");
        skillLabel.setBounds(225, 60, 250, 20);
        add(skillLabel);
    }

    private void buildPickers() {
        for (int i = 0; i < 3; i++) {
            int idx = i;

            JComboBox<String> attBox = new JComboBox<>(ATTRIBUTES);
            attBox.setBounds(25, 100 + 50 * i, 125, 20);
            attBox.addActionListener(e -> updateSkillPick(idx));
            skillAttributes[i] = attBox;
            add(attBox);

            JComboBox<String> skillBox = new JComboBox<>();
            skillBox.addItem(EMPTY_OPTION);
            skillBox.setBounds(225, 100 + 50 * i, 250, 20);
            skillPick[i] = skillBox;
            add(skillBox);
        }
    }

    private void buildButtons() {
        JButton back = new JButton("Back");
        back.setBounds(140, 240, 100, 28);
        back.addActionListener(e -> dispose());
        add(back);

        JButton confirm = new JButton("Confirm");
        confirm.setBounds(280, 240, 120, 28);
        confirm.addActionListener(e -> skillConfirm());
        add(confirm);
    }

    private void updateSkillPick(int k) {
        JComboBox<String> attBox = skillAttributes[k];
        JComboBox<String> skillBox = skillPick[k];

        String selectedAttr = (String) attBox.getSelectedItem();
        if (selectedAttr == null) selectedAttr = EMPTY_OPTION;
        String previousSkill = (String) skillBox.getSelectedItem();

        // Gather skills already chosen for this attribute in other rows
        Set<String> disallowed = new HashSet<>();
        for (int i = 0; i < skillPick.length; i++) {
            if (i == k) continue;
            String otherAttr = (String) skillAttributes[i].getSelectedItem();
            String otherSkill = (String) skillPick[i].getSelectedItem();
            if (selectedAttr.equals(otherAttr) && otherSkill != null && !EMPTY_OPTION.equals(otherSkill)) {
                disallowed.add(otherSkill);
            }
        }

        if (selectedAttr.equals(lastSelectedAttribute[k]) && isSelectionStillAllowed(previousSkill, disallowed)) {
            return;
        }
        lastSelectedAttribute[k] = selectedAttr;

        skillBox.removeAllItems();
        skillBox.addItem(EMPTY_OPTION);

        for (String option : getSkillOptions(selectedAttr)) {
            if (!disallowed.contains(option)) {
                skillBox.addItem(option);
            }
        }

        if (previousSkill != null && !disallowed.contains(previousSkill)) {
            skillBox.setSelectedItem(previousSkill);
        }
    }

    private void skillConfirm() {
        if (gmMode) {
            // Pre-fill three picks and proceed.
            skillAttributes[0].setSelectedItem("STR");
            skillAttributes[1].setSelectedItem("DEX");
            skillAttributes[2].setSelectedItem("CTL");
            // refresh dependent options after setting attributes
            updateSkillPick(0);
            updateSkillPick(1);
            updateSkillPick(2);
            skillPick[0].setSelectedItem("Climb");
            skillPick[1].setSelectedItem("Acrobatics");
            skillPick[2].setSelectedItem("Charge Device");
        }

        for (int i = 0; i < 3; i++) {
            String att = (String) skillAttributes[i].getSelectedItem();
            String skillName = (String) skillPick[i].getSelectedItem();

            if (att == null || EMPTY_OPTION.equals(att) || skillName == null || EMPTY_OPTION.equals(skillName)) {
                JOptionPane.showMessageDialog(this, "Select an attribute and skill for all three choices.");
                return;
            }
        }

        for (int i = 0; i < 3; i++) {
            String skillName = (String) skillPick[i].getSelectedItem();
            String att = (String) skillAttributes[i].getSelectedItem();

            DataSkill base = dataQuery.getSkillByName(skillName);
            if (base == null) continue;

            DataSkill chosen = new DataSkill(base); // copy so we don't mutate shared data
            chosen.addChosenAttribute(att);
            character.getSpecials().addSkill(chosen);
        }

        parent.skillsConfirmed();
        dispose();
    }

    private String[] getSkillOptions(String attribute) {
        return skillOptionsByAttribute.computeIfAbsent(attribute, this::buildSkillOptions);
    }

    private String[] buildSkillOptions(String attribute) {
        List<DataSkill> options = dataQuery.getSkillsByAttribute(attribute);
        if (options == null || options.isEmpty()) {
            return new String[0];
        }
        String[] names = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            names[i] = options.get(i).getName();
        }
        return names;
    }

    private boolean isSelectionStillAllowed(String previousSkill, Set<String> disallowed) {
        return previousSkill != null && !EMPTY_OPTION.equals(previousSkill) && !disallowed.contains(previousSkill);
    }
}
