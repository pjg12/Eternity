package eternity;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

/**
 * Handles skill selection controls and applying the selected skill to a character.
 */
public class FrameSkill {
	private final StoreRuleManager dataQuery;
	private final JComboBox<String> skillList;
	private final JComboBox<String> skillAttributes;
	private final JTextField skillSubtype;
	private JLabel boundSubtypeLabel;
	private boolean skillLevel;
	private boolean hideOwnedSkills;
	private StoreCharData ownerForFilter;
	private final ArrayList<DataSkill> reservedSkills;

	private final String[] ATTRIBUTES = {"STR","DEX","CON","FOC","CAP","CTL","KNOW","MECH","PERC","INT","CHA","SUB"};

	FrameSkill(StoreRuleManager dataQuery) {
		this.dataQuery = dataQuery;
		skillList = new JComboBox<>();
		skillList.addActionListener(e -> updateSubtypeFieldVisibility());
		skillAttributes = new JComboBox<>(ATTRIBUTES);
		skillAttributes.addActionListener(e -> updateData());
		skillSubtype = new JTextField();
		hideOwnedSkills = false;
		ownerForFilter = null;
		reservedSkills = new ArrayList<>();
		clear();
	}

	public void attachToFrame(JFrame frame) {
		attachToContainer(frame.getContentPane());
	}

	private void attachToContainer(Container container) {
		container.add(skillList);
		container.add(skillAttributes);
		container.add(skillSubtype);
	}

	public void clear() {
		skillLevel = false;
		skillList.setVisible(false);
		skillAttributes.setVisible(false);
		skillSubtype.setText("");
		skillSubtype.setVisible(false);
		if (boundSubtypeLabel != null) {
			boundSubtypeLabel.setVisible(false);
		}
		boundSubtypeLabel = null;
	}

	public void showSkillSelection(JLabel attributeLabel, JLabel skillLabel) {
		attributeLabel.setText("Attribute");
		attributeLabel.setVisible(true);
		skillAttributes.setVisible(true);

		skillLabel.setText("Skill");
		skillLabel.setVisible(true);
		skillList.setVisible(true);

		boundSubtypeLabel = null;
		skillLevel = true;
		updateData();
		updateSubtypeFieldVisibility();
	}

	public void showSkillSelection(JLabel attributeLabel, JLabel skillLabel, JLabel subtypeLabel) {
		showSkillSelection(attributeLabel, skillLabel);
		boundSubtypeLabel = subtypeLabel;
		if (boundSubtypeLabel != null) {
			boundSubtypeLabel.setText("Subtype");
		}
		updateSubtypeFieldVisibility();
	}

	JComboBox<String> getSkillList() {
		return skillList;
	}

	JComboBox<String> getSkillAttributes() {
		return skillAttributes;
	}

	JTextField getSkillSubtype() {
		return skillSubtype;
	}

	public boolean applySelection(StoreCharData character) {
		if (!skillLevel || character == null || character.getSpecials() == null) return true;
		DataSkill copy = buildSelection();
		if (copy == null) return false;
		character.getSpecials().addSkill(copy);
		return true;
	}

	public void setOwnedSkillFilter(StoreCharData character, boolean hideOwnedSkills) {
		this.ownerForFilter = character;
		this.hideOwnedSkills = hideOwnedSkills;
	}

	public void setReservedSkillFilter(List<DataSkill> skills) {
		reservedSkills.clear();
		if (skills == null) return;
		for (DataSkill skill : skills) {
			if (skill != null) reservedSkills.add(new DataSkill(skill));
		}
	}

	public DataSkill buildSelection() {
		DataSkill picked = dataQuery.getSkillByName((String)skillList.getSelectedItem());
		if (picked == null) return null;
		DataSkill copy = new DataSkill(picked);
		copy.addChosenAttribute((String)skillAttributes.getSelectedItem());
		if (picked.requiresSubtype()) {
			String subtype = skillSubtype.getText() == null ? "" : skillSubtype.getText().trim();
			if (subtype.isEmpty()) {
				JOptionPane.showMessageDialog(skillSubtype, "A subtype is required for " + copy.getName() + ".");
				return null;
			}
			copy.setChosenSubtype(subtype);
		}
		return copy;
	}

	/**
	 * Prompts the user to select a new, unlearned skill using FrameSkill controls.
	 */
	public static boolean promptForTrainingSkill(JFrame parent, StoreRuleManager dataQuery, StoreCharData character) {
		List<DataSkill> skills = promptForTrainingSkills(parent, dataQuery, character, 1);
		if (skills == null) return false;
		for (DataSkill skill : skills) {
			character.getSpecials().addSkill(skill);
		}
		return true;
	}

	public static List<DataSkill> promptForTrainingSkills(Component parent, StoreRuleManager dataQuery, StoreCharData character, int selectionCount) {
		return promptForTrainingSkills(parent, dataQuery, character, selectionCount, null);
	}

	public static List<DataSkill> promptForTrainingSkills(Component parent, StoreRuleManager dataQuery, StoreCharData character, int selectionCount, List<DataSkill> reservedSkills) {
		if (dataQuery == null || character == null || character.getSpecials() == null) return null;
		if (selectionCount <= 0) return new ArrayList<>();

		ArrayList<DataSkill> allReservedSkills = new ArrayList<>();
		if (reservedSkills != null) {
			for (DataSkill skill : reservedSkills) {
				if (skill != null) allReservedSkills.add(new DataSkill(skill));
			}
		}

		ArrayList<DataSkill> grantedSkills = new ArrayList<>();
		for (int pickIndex = 0; pickIndex < selectionCount; pickIndex++) {
			FrameSkill picker = new FrameSkill(dataQuery);
			picker.setOwnedSkillFilter(character, true);
			picker.setReservedSkillFilter(allReservedSkills);
			if (!picker.hasAnyAvailableSkill()) {
				String message = selectionCount == 1
						? "No unlearned skills are available."
						: "Not enough eligible skills are available to satisfy this specialty.";
				JOptionPane.showMessageDialog(parent, message, "No Skills", JOptionPane.INFORMATION_MESSAGE);
				return null;
			}

			JDialog dialog = new JDialog(parent == null ? null : JOptionPane.getFrameForComponent(parent),
					selectionCount == 1 ? "New Skill" : "New Skill (" + (pickIndex + 1) + " of " + selectionCount + ")",
					true);
			dialog.setLayout(null);
			dialog.setSize(430, 270);
			dialog.setLocationRelativeTo(parent);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			JLabel header = new JLabel("Select a new skill", SwingConstants.CENTER);
			header.setBounds(20, 15, 380, 24);
			dialog.add(header);

			JLabel attributeLabel = new JLabel();
			JLabel skillLabel = new JLabel();
			JLabel subtypeLabel = new JLabel("Subtype");
			dialog.add(attributeLabel);
			dialog.add(skillLabel);
			dialog.add(subtypeLabel);
			picker.attachToContainer(dialog.getContentPane());
			attributeLabel.setBounds(60, 75, 120, 20);
			picker.skillAttributes.setBounds(60, 100, 120, 20);
			skillLabel.setBounds(210, 75, 180, 20);
			picker.skillList.setBounds(210, 100, 180, 20);
			subtypeLabel.setBounds(60, 130, 120, 20);
			picker.skillSubtype.setBounds(60, 155, 330, 20);
			picker.showSkillSelection(attributeLabel, skillLabel, subtypeLabel);

			JButton cancel = new JButton("Cancel");
			cancel.setBounds(80, 205, 120, 25);
			cancel.addActionListener(e -> dialog.dispose());
			dialog.add(cancel);

			final boolean[] accepted = {false};
			final DataSkill[] resolvedSkill = {null};
			JButton accept = new JButton("Accept");
			accept.setBounds(230, 205, 120, 25);
			accept.addActionListener(e -> {
				String selectedSkill = (String) picker.skillList.getSelectedItem();
				String selectedAttribute = (String) picker.skillAttributes.getSelectedItem();
				if (selectedSkill == null || selectedSkill.isBlank()) {
					JOptionPane.showMessageDialog(dialog, "Select a skill to continue.");
					return;
				}
				if (picker.hasSkillForAttribute(selectedSkill, selectedAttribute)) {
					JOptionPane.showMessageDialog(dialog, "That attribute and skill combination is already learned. Choose a different option.");
					return;
				}
				DataSkill selected = picker.buildSelection();
				if (selected == null) {
					return;
				}
				resolvedSkill[0] = selected;
				accepted[0] = true;
				dialog.dispose();
			});
			dialog.add(accept);

			dialog.setVisible(true);
			if (!accepted[0] || resolvedSkill[0] == null) {
				return null;
			}

			grantedSkills.add(resolvedSkill[0]);
			allReservedSkills.add(new DataSkill(resolvedSkill[0]));
		}
		return grantedSkills;
	}

	private boolean hasReservedSkillForAttribute(String skillName, String attribute) {
		if (reservedSkills.isEmpty()) return false;
		if (skillName == null || skillName.isBlank() || attribute == null || attribute.isBlank()) return false;
		for (DataSkill reservedSkill : reservedSkills) {
			if (reservedSkill == null || reservedSkill.getName() == null) continue;
			if (!reservedSkill.getName().equalsIgnoreCase(skillName)) continue;
			for (String chosenAttribute : reservedSkill.getChosenAttributes()) {
				if (chosenAttribute != null && chosenAttribute.equalsIgnoreCase(attribute)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasSkillForAttribute(String skillName, String attribute) {
		if (hasReservedSkillForAttribute(skillName, attribute)) {
			return true;
		}
		if (!hideOwnedSkills || ownerForFilter == null || ownerForFilter.getSpecials() == null) return false;
		if (skillName == null || skillName.isBlank() || attribute == null || attribute.isBlank()) return false;
		DataSkill learnedSkill = ownerForFilter.getSpecials().getSkillByName(skillName);
		if (learnedSkill == null) return false;
		for (String chosenAttribute : learnedSkill.getChosenAttributes()) {
			if (chosenAttribute != null && chosenAttribute.equalsIgnoreCase(attribute)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAnyAvailableSkill() {
		for (String attribute : ATTRIBUTES) {
			ArrayList<DataSkill> options = new ArrayList<>(dataQuery.getSkillsByAttribute(attribute));
			for (DataSkill dataSkill : options) {
				if (dataSkill == null || dataSkill.getName() == null || dataSkill.getName().isBlank()) continue;
				if (hideOwnedSkills && hasSkillForAttribute(dataSkill.getName(), attribute)) continue;
				return true;
			}
		}
		return false;
	}

	private void updateData() {
		skillList.removeAllItems();
		String selectedAttribute = (String)skillAttributes.getSelectedItem();
		ArrayList<DataSkill> options = new ArrayList<>(dataQuery.getSkillsByAttribute(selectedAttribute));
		for (DataSkill dataSkill : options) {
			if (hideOwnedSkills && hasSkillForAttribute(dataSkill.getName(), selectedAttribute)) continue;
			skillList.addItem(dataSkill.getName());
		}
		updateSubtypeFieldVisibility();
	}

	private void updateSubtypeFieldVisibility() {
		boolean showSubtype = requiresSubtype((String) skillList.getSelectedItem());
		if (!showSubtype) {
			skillSubtype.setText("");
		}
		skillSubtype.setVisible(showSubtype);
		if (boundSubtypeLabel != null) {
			boundSubtypeLabel.setVisible(showSubtype);
		}
	}

	private boolean requiresSubtype(String skillName) {
		DataSkill skill = dataQuery.getSkillByName(skillName);
		return skill != null && skill.requiresSubtype();
	}
}

