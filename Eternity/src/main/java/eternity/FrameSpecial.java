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
 * Handles specialty selection controls and applying the selected specialty.
 */
public class FrameSpecial {
	private final StoreRuleManager dataQuery;
	private final JComboBox<String> specialsList;
	private final JComboBox<String> specialsType;
	private boolean specLevel;
	private boolean hideOwnedSpecialties;
	private StoreCharData ownerForFilter;

	private final String[] SPECTYPES = {"Proficiency","Martial","Class"};

	FrameSpecial(StoreRuleManager dataQuery) {
		this.dataQuery = dataQuery;
		specialsList = new JComboBox<>();
		specialsType = new JComboBox<>(SPECTYPES);
		specialsType.addActionListener(e -> updateData());
		hideOwnedSpecialties = false;
		ownerForFilter = null;
		clear();
	}

	public void attachToFrame(JFrame frame) {
		attachToContainer(frame.getContentPane());
	}

	private void attachToContainer(Container container) {
		container.add(specialsList);
		container.add(specialsType);
	}

	public void clear() {
		specLevel = false;
		specialsList.setVisible(false);
		specialsType.setVisible(false);
	}

	public void showSpecialtySelection(JLabel typeLabel, JLabel specialtyLabel) {
		typeLabel.setBounds(60, 240, 120, 20);
		typeLabel.setText("Type");
		typeLabel.setVisible(true);
		specialsType.setVisible(true);
		specialsType.setBounds(60, 265, 120, 20);

		specialtyLabel.setBounds(210, 240, 280, 20);
		specialtyLabel.setText("Specialty");
		specialtyLabel.setVisible(true);
		specialsList.setVisible(true);
		specialsList.setBounds(210, 265, 280, 20);

		specLevel = true;
		updateData();
	}

	public void applySelection(StoreCharData character) {
		if (!specLevel || character == null || character.getSpecials() == null) return;
		DataSpecialty picked = dataQuery.getSpecialtyByName((String)specialsList.getSelectedItem());
		if (picked == null) return;
		character.getSpecials().addTrainedSpecialty(new DataSpecialty(picked));
	}

	private void setOwnedSpecialtyFilter(StoreCharData character, boolean hideOwnedSpecialties) {
		this.ownerForFilter = character;
		this.hideOwnedSpecialties = hideOwnedSpecialties;
	}

	/**
	 * Prompts the user to select a new, unlearned specialty using FrameSpecial controls.
	 */
	public static boolean promptForTrainingSpecialty(JFrame parent, StoreRuleManager dataQuery, StoreCharData character) {
		if (parent == null || dataQuery == null || character == null || character.getSpecials() == null) return false;

		FrameSpecial picker = new FrameSpecial(dataQuery);
		picker.setOwnedSpecialtyFilter(character, true);
		if (!picker.hasAnyAvailableSpecialty()) {
			JOptionPane.showMessageDialog(parent, "No unlearned specialties are available.", "No Specialties", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		JDialog dialog = new JDialog(parent, "New Specialty", true);
		dialog.setLayout(null);
		dialog.setSize(430, 230);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JLabel header = new JLabel("Select a new specialty", SwingConstants.CENTER);
		header.setBounds(20, 15, 380, 24);
		dialog.add(header);

		JLabel typeLabel = new JLabel();
		JLabel specialtyLabel = new JLabel();
		dialog.add(typeLabel);
		dialog.add(specialtyLabel);
		picker.attachToContainer(dialog.getContentPane());
		picker.showSpecialtySelection(typeLabel, specialtyLabel);

		JButton cancel = new JButton("Cancel");
		cancel.setBounds(80, 160, 120, 25);
		cancel.addActionListener(e -> dialog.dispose());
		dialog.add(cancel);

		final boolean[] accepted = {false};
		JButton accept = new JButton("Accept");
		accept.setBounds(230, 160, 120, 25);
		accept.addActionListener(e -> {
			String selectedSpecialty = (String) picker.specialsList.getSelectedItem();
			if (selectedSpecialty == null || selectedSpecialty.isBlank()) {
				JOptionPane.showMessageDialog(dialog, "Select a specialty to continue.");
				return;
			}
			if (picker.hasSpecialty(selectedSpecialty)) {
				JOptionPane.showMessageDialog(dialog, "That specialty is already learned. Choose a different specialty.");
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

	private boolean hasSpecialty(String specialtyName) {
		if (!hideOwnedSpecialties || ownerForFilter == null || ownerForFilter.getSpecials() == null) return false;
		return ownerForFilter.getSpecials().hasSpecialty(specialtyName);
	}

	private boolean hasAnyAvailableSpecialty() {
		for (String type : SPECTYPES) {
			ArrayList<DataSpecialty> options = new ArrayList<>(dataQuery.getSpecialtiesByType(type));
			for (DataSpecialty spec : options) {
				if (spec == null || spec.getName() == null || spec.getName().isBlank()) continue;
				if (hideOwnedSpecialties && hasSpecialty(spec.getName())) continue;
				return true;
			}
		}
		return false;
	}

	private void updateData() {
		specialsList.removeAllItems();
		String selectedType = (String)specialsType.getSelectedItem();
		ArrayList<DataSpecialty> options = new ArrayList<>(dataQuery.getSpecialtiesByType(selectedType));
		for (DataSpecialty spec : options) {
			if (hideOwnedSpecialties && hasSpecialty(spec.getName())) continue;
			specialsList.addItem(spec.getName());
		}
	}
}

