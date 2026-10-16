package eternity;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public static record SpecialtyGrant(DataSpecialty specialty, List<DataSkill> grantedSkills) {}
	private static final String ARCANE_ANALYSIS_SPECIALTY = "Arcane Analysis";
	private static final String ARCANE_ANALYSIS_ATTRIBUTE = "CTL";
	private static final String ANALYZE_SKILL = "Analyze";
	private static final String IDENTIFY_SKILL = "Identify";
	private static final String[] RESERVED_SKILL_ATTRIBUTES = {"STR","DEX","CON","FOC","CAP","CTL","KNOW","MECH","PERC","INT","CHA","SUB"};
	private static final Map<String, int[]> BASE_CLASS_PROHIBITED_SPECIALTY_RANGES = Map.ofEntries(
			Map.entry("Warrior", new int[] {1000, 1999}),
			Map.entry("Paladin", new int[] {2000, 2999}),
			Map.entry("Rogue", new int[] {3000, 3999}),
			Map.entry("Monk", new int[] {4000, 4999}),
			Map.entry("Archer", new int[] {5000, 5999}),
			Map.entry("Leader", new int[] {6000, 6999}),
			Map.entry("Cleric", new int[] {7000, 7999}),
			Map.entry("Caster", new int[] {8000, 8999}),
			Map.entry("Shifter", new int[] {9000, 9999}),
			Map.entry("Pilot", new int[] {10000, 10999}));

	private final StoreRuleManager dataQuery;
	private final JComboBox<String> specialsList;
	private final JComboBox<String> specialsType;
	private static final String STANCE_SPECIALTY = "Stance";
	private boolean specLevel;
	private boolean hideOwnedSpecialties;
	private StoreCharData ownerForFilter;
	private final ArrayList<DataSpecialty> reservedSpecialties;

	private final String[] SPECTYPES = {"Proficiency","Martial","Skill","Class"};

	FrameSpecial(StoreRuleManager dataQuery) {
		this.dataQuery = dataQuery;
		specialsList = new JComboBox<>();
		specialsType = new JComboBox<>(SPECTYPES);
		specialsType.addActionListener(e -> updateData());
		hideOwnedSpecialties = false;
		ownerForFilter = null;
		reservedSpecialties = new ArrayList<>();
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
		typeLabel.setText("Type");
		typeLabel.setVisible(true);
		specialsType.setVisible(true);

		specialtyLabel.setText("Specialty");
		specialtyLabel.setVisible(true);
		specialsList.setVisible(true);

		specLevel = true;
		updateData();
	}

	JComboBox<String> getSpecialsList() {
		return specialsList;
	}

	JComboBox<String> getSpecialsType() {
		return specialsType;
	}

	public boolean applySelection(StoreCharData character, Component parent) {
		if (!specLevel || character == null || character.getSpecials() == null) return true;
		DataSpecialty picked = dataQuery.getSpecialtyByName((String)specialsList.getSelectedItem());
		if (picked == null) return true;
		DataSpecialty resolved = FrameSpecialsPicker.resolveSpecialtyChoice(parent, dataQuery, character, picked);
		if (resolved == null) return false;
		return applyResolvedSpecialtyGrant(parent, dataQuery, character, resolved);
	}

	public void setOwnedSpecialtyFilter(StoreCharData character, boolean hideOwnedSpecialties) {
		this.ownerForFilter = character;
		this.hideOwnedSpecialties = hideOwnedSpecialties;
	}

	public void setReservedSpecialtyFilter(List<DataSpecialty> specialties) {
		reservedSpecialties.clear();
		if (specialties == null) return;
		for (DataSpecialty specialty : specialties) {
			if (specialty != null) reservedSpecialties.add(new DataSpecialty(specialty));
		}
	}

	/**
	 * Prompts the user to select a new, unlearned specialty using FrameSpecial controls.
	 */
	public static boolean promptForTrainingSpecialty(JFrame parent, StoreRuleManager dataQuery, StoreCharData character) {
		List<SpecialtyGrant> grants = promptForTrainingSpecialtyGrants(parent, dataQuery, character, 1, null, null);
		if (grants == null || grants.isEmpty()) return false;
		for (SpecialtyGrant grant : grants) {
			applyResolvedSpecialtyGrant(character, grant.specialty(), grant.grantedSkills());
		}
		return true;
	}

	public static List<SpecialtyGrant> promptForTrainingSpecialtyGrants(Component parent, StoreRuleManager dataQuery,
			StoreCharData character, int selectionCount, List<DataSpecialty> reservedSpecialties, List<DataSkill> reservedSkills) {
		return promptForTrainingSpecialtyGrants(parent, dataQuery, character, selectionCount, reservedSpecialties, reservedSkills, null);
	}

	public static List<SpecialtyGrant> promptForTrainingSpecialtyGrants(Component parent, StoreRuleManager dataQuery,
			StoreCharData character, int selectionCount, List<DataSpecialty> reservedSpecialties, List<DataSkill> reservedSkills,
			String dialogTitle) {
		if (dataQuery == null || character == null || character.getSpecials() == null) return null;
		if (selectionCount <= 0) return new ArrayList<>();
		String resolvedTitle = (dialogTitle == null || dialogTitle.isBlank()) ? "New Specialty" : dialogTitle.trim();

		ArrayList<DataSpecialty> allReservedSpecialties = new ArrayList<>();
		if (reservedSpecialties != null) {
			for (DataSpecialty specialty : reservedSpecialties) {
				if (specialty != null) allReservedSpecialties.add(new DataSpecialty(specialty));
			}
		}
		ArrayList<DataSkill> allReservedSkills = new ArrayList<>();
		if (reservedSkills != null) {
			for (DataSkill skill : reservedSkills) {
				if (skill != null) allReservedSkills.add(new DataSkill(skill));
			}
		}

		ArrayList<SpecialtyGrant> grants = new ArrayList<>();
		for (int pickIndex = 0; pickIndex < selectionCount; pickIndex++) {
			FrameSpecial picker = new FrameSpecial(dataQuery);
			picker.setOwnedSpecialtyFilter(character, true);
			picker.setReservedSpecialtyFilter(allReservedSpecialties);
			if (!picker.hasAnyAvailableSpecialty()) {
				String message = selectionCount == 1
						? "No unlearned specialties are available."
						: "Not enough eligible specialties are available to satisfy this training.";
				JOptionPane.showMessageDialog(parent, message, "No Specialties", JOptionPane.INFORMATION_MESSAGE);
				return null;
			}

			JDialog dialog = new JDialog(parent == null ? null : JOptionPane.getFrameForComponent(parent),
					selectionCount == 1 ? resolvedTitle : resolvedTitle + " (" + (pickIndex + 1) + " of " + selectionCount + ")",
					true);
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
			typeLabel.setBounds(60, 75, 120, 20);
			picker.specialsType.setBounds(60, 100, 120, 20);
			specialtyLabel.setBounds(210, 75, 180, 20);
			picker.specialsList.setBounds(210, 100, 180, 20);
			picker.showSpecialtySelection(typeLabel, specialtyLabel);

			JButton cancel = new JButton("Cancel");
			cancel.setBounds(80, 160, 120, 25);
			cancel.addActionListener(e -> dialog.dispose());
			dialog.add(cancel);

			final boolean[] accepted = {false};
			final SpecialtyGrant[] resolvedGrant = {null};
			JButton accept = new JButton("Accept");
			accept.setBounds(230, 160, 120, 25);
			accept.addActionListener(e -> {
				String selectedSpecialty = (String) picker.specialsList.getSelectedItem();
				if (selectedSpecialty == null || selectedSpecialty.isBlank()) {
					JOptionPane.showMessageDialog(dialog, "Select a specialty to continue.");
					return;
				}
				if (!CharSpecials.isRepeatableSpecialtyName(selectedSpecialty) && picker.hasSpecialty(selectedSpecialty)) {
					JOptionPane.showMessageDialog(dialog, "That specialty is already learned. Choose a different specialty.");
					return;
				}

				DataSpecialty picked = dataQuery.getSpecialtyByName(selectedSpecialty);
				if (picked == null) return;
				DataSpecialty resolved = FrameSpecialsPicker.resolveSpecialtyChoice(dialog, dataQuery, character, picked);
				if (resolved == null) return;

				List<DataSkill> grantedSkills = resolveGrantedSkills(dialog, dataQuery, character, resolved, allReservedSkills);
				if (grantedSkills == null) {
					return;
				}

				resolvedGrant[0] = new SpecialtyGrant(resolved, grantedSkills);
				accepted[0] = true;
				dialog.dispose();
			});
			dialog.add(accept);

			dialog.setVisible(true);
			if (!accepted[0] || resolvedGrant[0] == null) {
				return null;
			}

			grants.add(resolvedGrant[0]);
			allReservedSpecialties.add(new DataSpecialty(resolvedGrant[0].specialty()));
			for (DataSkill skill : resolvedGrant[0].grantedSkills()) {
				if (skill != null) allReservedSkills.add(new DataSkill(skill));
			}
		}
		return grants;
	}

	private boolean hasSpecialty(String specialtyName) {
		if (specialtyName == null || specialtyName.isBlank()) return false;
		if (hasReservedSpecialty(specialtyName)) return true;
		if (!hideOwnedSpecialties || ownerForFilter == null || ownerForFilter.getSpecials() == null) return false;
		return ownerForFilter.getSpecials().hasSpecialty(specialtyName);
	}

	private boolean hasReservedSpecialty(String specialtyName) {
		if (specialtyName == null || specialtyName.isBlank()) return false;
		for (DataSpecialty specialty : reservedSpecialties) {
			if (specialty == null || specialty.getName() == null) continue;
			if (specialtyName.equalsIgnoreCase(specialty.getName().trim())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAnyAvailableSpecialty() {
		for (String type : SPECTYPES) {
			ArrayList<DataSpecialty> options = new ArrayList<>(dataQuery.getSpecialtiesByType(type));
			for (DataSpecialty spec : options) {
				if (spec == null || spec.getName() == null || spec.getName().isBlank()) continue;
				if (shouldExcludeSpecialty(spec)) continue;
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
			if (shouldExcludeSpecialty(spec)) continue;
			specialsList.addItem(spec.getName());
		}
	}

	private boolean shouldExcludeSpecialty(DataSpecialty spec) {
		if (spec == null) return true;
		if (spec.getPrereq() < 0) return true;
		if (isBaseClassProhibitedSpecialty(spec)) return true;
		if (STANCE_SPECIALTY.equalsIgnoreCase(spec.getName())
				&& FrameSpecialsPicker.getAvailableStanceOptions(ownerForFilter).isEmpty()) return true;
		if (hideOwnedSpecialties && !CharSpecials.isRepeatableSpecialty(spec) && hasSpecialty(spec.getName())) return true;
		if (lacksRequiredSpecialty(spec)) return true;
		return isCurrentClassSpecialty(spec);
	}

	private boolean isBaseClassProhibitedSpecialty(DataSpecialty spec) {
		if (spec == null || ownerForFilter == null || ownerForFilter.getIdentity() == null) return false;
		String baseClass = ownerForFilter.getIdentity().getCharClass();
		if (baseClass == null || baseClass.isBlank()) return false;
		int[] range = BASE_CLASS_PROHIBITED_SPECIALTY_RANGES.get(baseClass);
		if (range == null || range.length < 2) return false;
		int specialtyId = spec.getId();
		return specialtyId >= range[0] && specialtyId <= range[1];
	}

	private boolean lacksRequiredSpecialty(DataSpecialty spec) {
		if (spec == null || ownerForFilter == null || ownerForFilter.getSpecials() == null) return false;
		return ownerForFilter.getSpecials().lacksRequiredSpecialty(spec, dataQuery);
	}

	private boolean isCurrentClassSpecialty(DataSpecialty spec) {
		if (spec == null || ownerForFilter == null || ownerForFilter.getIdentity() == null) return false;
		if (!"Class".equalsIgnoreCase(spec.getCategory())) return false;

		int specialtyFamily = resolveSpecialtyFamily(spec);
		if (specialtyFamily <= 0) return false;

		DataClass currentClass = resolveCurrentClass();
		return specialtyFamily == resolveClassFamily(currentClass);
	}

	private DataClass resolveCurrentClass() {
		if (ownerForFilter == null || ownerForFilter.getIdentity() == null) return null;

		CharIdentity identity = ownerForFilter.getIdentity();
		String subclassName = identity.getCharSubclass();
		if (subclassName != null && !subclassName.isBlank() && !"?".equals(subclassName)) {
			DataClass subclass = dataQuery.getClassByName(subclassName);
			if (subclass != null) return subclass;
		}

		String className = identity.getCharClass();
		if (className == null || className.isBlank() || "?".equals(className)) return null;
		return dataQuery.getClassByName(className);
	}

	private int resolveClassFamily(DataClass dataClass) {
		if (dataClass == null || dataClass.getID() <= 0) return -1;
		return ((dataClass.getID() - 1) / 3) + 1;
	}

	private int resolveSpecialtyFamily(DataSpecialty spec) {
		if (spec == null || spec.getId() <= 0) return -1;
		return spec.getId() / 1000;
	}

	public static boolean applyResolvedSpecialtyGrant(Component parent, StoreRuleManager dataQuery, StoreCharData character, DataSpecialty specialty) {
		return applyResolvedSpecialtyGrant(parent, dataQuery, character, specialty, null);
	}

	public static boolean applyResolvedSpecialtyGrant(Component parent, StoreRuleManager dataQuery, StoreCharData character, DataSpecialty specialty, List<DataSkill> reservedSkills) {
		if (character == null || character.getSpecials() == null || specialty == null) return false;

		List<DataSkill> grantedSkills = resolveGrantedSkills(parent, dataQuery, character, specialty, reservedSkills);
		if (grantedSkills == null) return false;

		character.getSpecials().addTrainedSpecialty(specialty);
		for (DataSkill skill : grantedSkills) {
			character.getSpecials().addSkill(skill);
		}
		character.syncSpecialtyChoiceLists();
		return true;
	}

	public static List<DataSkill> resolveGrantedSkills(Component parent, StoreRuleManager dataQuery, StoreCharData character,
			DataSpecialty specialty, List<DataSkill> reservedSkills) {
		if (character == null || character.getSpecials() == null || specialty == null) return null;
		if (isArcaneAnalysisSpecialty(specialty)) {
			return resolveArcaneAnalysisGrantedSkills(parent, dataQuery, character, reservedSkills);
		}

		int grantedSkillCount = resolveGrantedSkillCount(dataQuery, specialty);
		return FrameSkill.promptForTrainingSkills(parent, dataQuery, character, grantedSkillCount, reservedSkills);
	}

	public static void applyResolvedSpecialtyGrant(StoreCharData character, DataSpecialty specialty, List<DataSkill> grantedSkills) {
		if (character == null || character.getSpecials() == null || specialty == null) return;
		character.getSpecials().addTrainedSpecialty(specialty);
		if (grantedSkills != null) {
			for (DataSkill skill : grantedSkills) {
				if (skill != null) character.getSpecials().addSkill(skill);
			}
		}
		character.syncSpecialtyChoiceLists();
	}

	public static int resolveGrantedSkillCount(StoreRuleManager dataQuery, DataSpecialty specialty) {
		if (specialty == null) return 0;
		DataSpecialty base = null;
		if (dataQuery != null) {
			if (specialty.getId() > 0) {
				base = dataQuery.getSpecialtyById(specialty.getId());
			}
			if (base == null && specialty.getName() != null && !specialty.getName().isBlank()) {
				base = dataQuery.getSpecialtyByName(specialty.getName());
			}
		}
		if (base != null) {
			return base.getGrantedSkillSelectionCount();
		}
		return specialty.getGrantedSkillSelectionCount();
	}

	private static boolean isArcaneAnalysisSpecialty(DataSpecialty specialty) {
		return specialty != null
				&& specialty.getName() != null
				&& ARCANE_ANALYSIS_SPECIALTY.equalsIgnoreCase(specialty.getName().trim());
	}

	private static List<DataSkill> resolveArcaneAnalysisGrantedSkills(Component parent, StoreRuleManager dataQuery,
			StoreCharData character, List<DataSkill> reservedSkills) {
		if (dataQuery == null || character == null || character.getSpecials() == null) return null;

		ArrayList<DataSkill> grantedSkills = new ArrayList<>();
		ArrayList<DataSkill> promptReservedSkills = new ArrayList<>();
		if (reservedSkills != null) {
			for (DataSkill reservedSkill : reservedSkills) {
				if (reservedSkill != null) {
					promptReservedSkills.add(new DataSkill(reservedSkill));
				}
			}
		}

		int replacementCount = 0;
		replacementCount += processArcaneAnalysisSkillGrant(dataQuery, character, ANALYZE_SKILL, grantedSkills, promptReservedSkills);
		replacementCount += processArcaneAnalysisSkillGrant(dataQuery, character, IDENTIFY_SKILL, grantedSkills, promptReservedSkills);

		List<DataSkill> replacementSkills = FrameSkill.promptForTrainingSkills(parent, dataQuery, character, replacementCount, promptReservedSkills);
		if (replacementSkills == null) {
			return null;
		}
		grantedSkills.addAll(replacementSkills);
		return grantedSkills;
	}

	private static int processArcaneAnalysisSkillGrant(StoreRuleManager dataQuery, StoreCharData character, String skillName,
			List<DataSkill> grantedSkills, List<DataSkill> promptReservedSkills) {
		if (dataQuery == null || character == null || character.getSpecials() == null || skillName == null || skillName.isBlank()) {
			return 0;
		}
		if (character.getSpecials().getSkillByName(skillName) != null) {
			addReservedSkillName(promptReservedSkills, skillName);
			return 1;
		}

		DataSkill template = dataQuery.getSkillByName(skillName);
		if (template == null) return 0;

		DataSkill grantedSkill = new DataSkill(template);
		grantedSkill.addChosenAttribute(ARCANE_ANALYSIS_ATTRIBUTE);
		grantedSkills.add(grantedSkill);
		addReservedSkillName(promptReservedSkills, skillName);
		promptReservedSkills.add(new DataSkill(grantedSkill));
		return 0;
	}

	private static void addReservedSkillName(List<DataSkill> reservedSkills, String skillName) {
		if (reservedSkills == null || skillName == null || skillName.isBlank()) return;
		for (String attribute : RESERVED_SKILL_ATTRIBUTES) {
			DataSkill reservedSkill = new DataSkill();
			reservedSkill.setName(skillName);
			reservedSkill.addChosenAttribute(attribute);
			reservedSkills.add(reservedSkill);
		}
	}
}

