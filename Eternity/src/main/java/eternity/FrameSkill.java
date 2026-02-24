package eternity;

import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Handles skill selection controls and applying the selected skill to a character.
 */
public class FrameSkill {
	private final DataQuery dataQuery;
	private final JComboBox<String> skillList;
	private final JComboBox<String> skillAttributes;
	private boolean skillLevel;
	private boolean hideOwnedSkills;
	private CharData ownerForFilter;

	private final String[] ATTRIBUTES = {"STR","DEX","CON","FOC","CAP","CTL","KNOW","MECH","PERC","INT","CHA","SUB"};

	FrameSkill(DataQuery dataQuery) {
		this.dataQuery = dataQuery;
		skillList = new JComboBox<>();
		skillAttributes = new JComboBox<>(ATTRIBUTES);
		skillAttributes.addActionListener(e -> updateData());
		hideOwnedSkills = false;
		ownerForFilter = null;
		clear();
	}

	public void attachToFrame(JFrame frame) {
		attachToContainer(frame.getContentPane());
	}

	private void attachToContainer(Container container) {
		container.add(skillList);
		container.add(skillAttributes);
	}

	public void clear() {
		skillLevel = false;
		skillList.setVisible(false);
		skillAttributes.setVisible(false);
	}

	public void showSkillSelection(JLabel attributeLabel, JLabel skillLabel) {
		attributeLabel.setBounds(60, 115, 120, 20);
		attributeLabel.setText("Attribute");
		attributeLabel.setVisible(true);
		skillAttributes.setVisible(true);
		skillAttributes.setBounds(60, 140, 120, 20);

		skillLabel.setBounds(210, 115, 280, 20);
		skillLabel.setText("Skill");
		skillLabel.setVisible(true);
		skillList.setVisible(true);
		skillList.setBounds(210, 140, 280, 20);

		skillLevel = true;
		updateData();
	}

	public void applySelection(CharData character) {
		if (!skillLevel || character == null || character.getSpecials() == null) return;
		DataSkill picked = dataQuery.getSkillByName((String)skillList.getSelectedItem());
		if (picked == null) return;
		DataSkill copy = new DataSkill(picked);
		copy.addChosenAttribute((String)skillAttributes.getSelectedItem());
		character.getSpecials().addSkill(copy);
	}

	private void setOwnedSkillFilter(CharData character, boolean hideOwnedSkills) {
		this.ownerForFilter = character;
		this.hideOwnedSkills = hideOwnedSkills;
	}

	/**
	 * Prompts the user to select a new, unlearned skill using FrameSkill controls.
	 */
	public static boolean promptForTrainingSkill(JFrame parent, DataQuery dataQuery, CharData character) {
		if (parent == null || dataQuery == null || character == null || character.getSpecials() == null) return false;

		FrameSkill picker = new FrameSkill(dataQuery);
		picker.setOwnedSkillFilter(character, true);
		if (!picker.hasAnyAvailableSkill()) {
			JOptionPane.showMessageDialog(parent, "No unlearned skills are available.", "No Skills", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		JDialog dialog = new JDialog(parent, "New Skill", true);
		dialog.setLayout(null);
		dialog.setSize(430, 230);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JLabel header = new JLabel("Select a new skill", SwingConstants.CENTER);
		header.setBounds(20, 15, 380, 24);
		dialog.add(header);

		JLabel attributeLabel = new JLabel();
		JLabel skillLabel = new JLabel();
		dialog.add(attributeLabel);
		dialog.add(skillLabel);
		picker.attachToContainer(dialog.getContentPane());
		picker.showSkillSelection(attributeLabel, skillLabel);

		JButton cancel = new JButton("Cancel");
		cancel.setBounds(80, 160, 120, 25);
		cancel.addActionListener(e -> dialog.dispose());
		dialog.add(cancel);

		final boolean[] accepted = {false};
		JButton accept = new JButton("Accept");
		accept.setBounds(230, 160, 120, 25);
		accept.addActionListener(e -> {
			String selectedSkill = (String) picker.skillList.getSelectedItem();
			if (selectedSkill == null || selectedSkill.isBlank()) {
				JOptionPane.showMessageDialog(dialog, "Select a skill to continue.");
				return;
			}
			if (picker.hasSkill(selectedSkill)) {
				JOptionPane.showMessageDialog(dialog, "That skill is already learned. Choose a different skill.");
				return;
			}
			picker.applySelection(character);
			accepted[0] = true;
			dialog.dispose();
		});
		dialog.add(accept);

		dialog.setVisible(true);
		return accepted[0];
	}

	private boolean hasSkill(String skillName) {
		if (!hideOwnedSkills || ownerForFilter == null || ownerForFilter.getSpecials() == null) return false;
		return ownerForFilter.getSpecials().getSkillByName(skillName) != null;
	}

	private boolean hasAnyAvailableSkill() {
		for (String attribute : ATTRIBUTES) {
			ArrayList<DataSkill> options = new ArrayList<>(dataQuery.getSkillsByAttribute(attribute));
			for (DataSkill dataSkill : options) {
				if (dataSkill == null || dataSkill.getName() == null || dataSkill.getName().isBlank()) continue;
				if (hideOwnedSkills && hasSkill(dataSkill.getName())) continue;
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
			if (hideOwnedSkills && hasSkill(dataSkill.getName())) continue;
			skillList.addItem(dataSkill.getName());
		}
	}
}
