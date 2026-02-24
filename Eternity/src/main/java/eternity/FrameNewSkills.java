package eternity;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private final ArrayList<JComboBox<String>> skillAttributes = new ArrayList<>();
    private final ArrayList<JComboBox<String>> skillPick = new ArrayList<>();

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
            skillAttributes.add(attBox);
            add(attBox);

            JComboBox<String> skillBox = new JComboBox<>();
            skillBox.addItem("***");
            skillBox.setBounds(225, 100 + 50 * i, 250, 20);
            skillPick.add(skillBox);
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
        JComboBox<String> attBox = skillAttributes.get(k);
        JComboBox<String> skillBox = skillPick.get(k);

        String selectedAttr = (String) attBox.getSelectedItem();
        if (selectedAttr == null) selectedAttr = "***";

        // Gather skills already chosen for this attribute in other rows
        Set<String> disallowed = new HashSet<>();
        for (int i = 0; i < skillPick.size(); i++) {
            if (i == k) continue;
            String otherAttr = (String) skillAttributes.get(i).getSelectedItem();
            String otherSkill = (String) skillPick.get(i).getSelectedItem();
            if (selectedAttr.equals(otherAttr) && otherSkill != null && !"***".equals(otherSkill)) {
                disallowed.add(otherSkill);
            }
        }

        skillBox.removeAllItems();
        skillBox.addItem("***");

        List<DataSkill> options = dataQuery.getSkillsByAttribute(selectedAttr);
        for (DataSkill s : options) {
            if (!disallowed.contains(s.getName())) {
                skillBox.addItem(s.getName());
            }
        }
    }

    private void skillConfirm() {
        if (gmMode) {
            // Pre-fill three picks and proceed.
            skillAttributes.get(0).setSelectedItem("STR");
            skillAttributes.get(1).setSelectedItem("DEX");
            skillAttributes.get(2).setSelectedItem("CTL");
            // refresh dependent options after setting attributes
            updateSkillPick(0);
            updateSkillPick(1);
            updateSkillPick(2);
            skillPick.get(0).setSelectedItem("Climb");
            skillPick.get(1).setSelectedItem("Acrobatics");
            skillPick.get(2).setSelectedItem("Charge Device");
        }

        for (int i = 0; i < 3; i++) {
            String att = (String) skillAttributes.get(i).getSelectedItem();
            String skillName = (String) skillPick.get(i).getSelectedItem();

            if (att == null || "***".equals(att) || skillName == null || "***".equals(skillName)) {
                JOptionPane.showMessageDialog(this, "Select an attribute and skill for all three choices.");
                return;
            }
        }

        for (int i = 0; i < 3; i++) {
            String skillName = (String) skillPick.get(i).getSelectedItem();
            String att = (String) skillAttributes.get(i).getSelectedItem();

            DataSkill base = dataQuery.getSkillByName(skillName);
            if (base == null) continue;

            DataSkill chosen = new DataSkill(base); // copy so we don't mutate shared data
            chosen.addChosenAttribute(att);
            character.getSpecials().addSkill(chosen);
        }

        parent.skillsConfirmed();
        dispose();
    }
}
