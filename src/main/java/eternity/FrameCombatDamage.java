package eternity;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import eternity.DataTypes.DataStatus;
import eternity.Minions.CharMinions;
import eternity.Minions.MinionCombatState;
import eternity.Minions.MinionSlotState;

import javax.swing.JOptionPane;

public class FrameCombatDamage extends JFrame {
	private static final long serialVersionUID = 1;
	private static final String ADAPTIVE_TECHNIQUE_SPECIALTY = "Adaptive Technique";
	private static final String LIFE_LINK_SPECIALTY = "Life Link";
	private static final String ADAPTIVE_OUTCOME_NONE = "No Change";
	private static final String ADAPTIVE_OUTCOME_STRUCK = "Struck";
	private static final String ADAPTIVE_OUTCOME_AVOIDED = "Avoided/Saved";

	private record LifeLinkRedirect(String slotTitle, String displayName, int amount) {}

	private final FrameSheet sheetFrame;
	private final StoreCharData character;
	/** When set, incoming damage is applied to this summoned minion slot instead of the Leader. */
	private final String targetMinionSlot;

	private JComboBox<String> damageType;
private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME", "DIVINE"};

// UI elements
private final JLabel headerL = new JLabel();
private final JLabel[] labels = new JLabel[5];
private final JSpinner numField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JSpinner crushField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JSpinner displacementField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JLabel displacementLabel = new JLabel();
private final JCheckBox evasionSaveCheckBox = new JCheckBox("Successful damage-reducing save");
private final JTextField resultField = new JTextField();
private final JTextField damageCodeField = new JTextField();
private final JLabel adaptiveDefenseLabel = new JLabel();
private final JLabel adaptiveOutcomeLabel = new JLabel();
private final JComboBox<String> adaptiveDefenseSelect =
		new JComboBox<String>(new String[] {"None", "DEF", "DODGE", "FORT", "REF", "WILL"});
private final JComboBox<String> adaptiveOutcomeSelect =
		new JComboBox<String>(new String[] {ADAPTIVE_OUTCOME_NONE, ADAPTIVE_OUTCOME_STRUCK, ADAPTIVE_OUTCOME_AVOIDED});
private final JButton cancelButton = new JButton();
private final JButton confirmButton = new JButton();
private final JButton calcButton = new JButton();
private final JButton loadCodeButton = new JButton();
private boolean loadedSmiteDamageCode;

	FrameCombatDamage(FrameSheet sheetFrame, StoreCharData character) {
		this(sheetFrame, character, null);
	}

	FrameCombatDamage(FrameSheet sheetFrame, StoreCharData character, String targetMinionSlot) {
		super("Under Attack Helper");
		this.sheetFrame = sheetFrame;
		this.character = character;
		this.targetMinionSlot = targetMinionSlot == null || targetMinionSlot.isBlank()
				? null
				: targetMinionSlot.trim();
		setLayout(null);
		setSize(550, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		initComponents();
		underAttack();
	}

	private void initComponents() {
		headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 14f));
		headerL.setBounds(15, 10, 500, 25);
		add(headerL);

	for (int i = 0; i < labels.length; i++) {
		labels[i] = new JLabel();
		labels[i].setVisible(false);
		add(labels[i]);
	}

	numField.setVisible(false);
	add(numField);
	crushField.setVisible(false);
	add(crushField);
	displacementLabel.setVisible(false);
	add(displacementLabel);
	displacementField.setVisible(false);
	add(displacementField);
	evasionSaveCheckBox.setVisible(false);
	add(evasionSaveCheckBox);
	resultField.setVisible(false);
	resultField.setEditable(false);
	add(resultField);
	damageCodeField.setVisible(false);
	add(damageCodeField);
	adaptiveDefenseLabel.setVisible(false);
	add(adaptiveDefenseLabel);
	adaptiveOutcomeLabel.setVisible(false);
	add(adaptiveOutcomeLabel);
	adaptiveDefenseSelect.setVisible(false);
	add(adaptiveDefenseSelect);
	adaptiveOutcomeSelect.setVisible(false);
	add(adaptiveOutcomeSelect);

	cancelButton.setVisible(false);
	add(cancelButton);
	confirmButton.setVisible(false);
	add(confirmButton);
	calcButton.setVisible(false);
	add(calcButton);
	loadCodeButton.setVisible(false);
	add(loadCodeButton);
	}

	public void underAttack() {
		clearDamage();

		String targetName = resolveTargetDisplayName();
		headerL.setText(isTargetingMinion()
				? "Under Attack Helper — " + targetName
				: "Under Attack Helper");
		headerL.setVisible(true);

		labels[4].setBounds(25, 55, 100, 20);
		labels[4].setText("Damage Code");
		labels[4].setVisible(true);
		damageCodeField.setBounds(25, 78, 375, 22);
		damageCodeField.setText("");
		damageCodeField.setVisible(true);
		loadCodeButton.setBounds(410, 78, 100, 22);
		loadCodeButton.setText("Load Code");
		loadCodeButton.addActionListener(e -> loadDamageCode(true));
		loadCodeButton.setVisible(true);

		labels[0].setBounds(25, 115, 100, 20);
		labels[0].setText("Damage Type");
		labels[0].setVisible(true);

		damageType = new JComboBox<String>(DMGTYPE);
		damageType.setBounds(25, 140, 120, 22);
		add(damageType);

		labels[1].setBounds(170, 115, 120, 20);
	labels[1].setText("Damage Amount");
	labels[1].setVisible(true);
	numField.setBounds(170, 140, 100, 22);
	numField.setValue(0.0);
	numField.setVisible(true);

	labels[2].setBounds(300, 115, 120, 20);
	labels[2].setText("Enemy Crush");
	labels[2].setVisible(true);
	crushField.setBounds(300, 140, 100, 22);
	crushField.setValue(0.0);
	crushField.setVisible(true);

	displacementLabel.setBounds(25, 175, 160, 20);
	displacementLabel.setText("Displacement (ft)");
	displacementLabel.setVisible(true);
	displacementField.setBounds(170, 175, 100, 22);
	displacementField.setValue(0.0);
	displacementField.setToolTipText("If this effect displaces you, enter the original distance.");
	displacementField.setVisible(true);

	labels[3].setBounds(25, 210, 120, 20);
	labels[3].setText("Total Damage");
	labels[3].setVisible(true);
	resultField.setBounds(150, 210, 120, 22);
	resultField.setText("");
	resultField.setVisible(true);

	if (!isTargetingMinion() && hasEvasionSpecialty()) {
		evasionSaveCheckBox.setBounds(25, 245, 260, 22);
		evasionSaveCheckBox.setSelected(false);
		evasionSaveCheckBox.setToolTipText("If this effect allowed a save to reduce damage and the save succeeded, Evasion reduces the damage to 0.");
		evasionSaveCheckBox.setVisible(true);
	}

	if (!isTargetingMinion() && hasAdaptiveTechniqueSpecialty()) {
		adaptiveDefenseLabel.setBounds(25, 275, 180, 20);
		adaptiveDefenseLabel.setText("Defense Used");
		adaptiveDefenseLabel.setVisible(true);
		adaptiveDefenseSelect.setBounds(25, 300, 120, 22);
		adaptiveDefenseSelect.setSelectedIndex(0);
		adaptiveDefenseSelect.setVisible(true);

		adaptiveOutcomeLabel.setBounds(170, 275, 180, 20);
		adaptiveOutcomeLabel.setText("Adaptive Technique");
		adaptiveOutcomeLabel.setVisible(true);
		adaptiveOutcomeSelect.setBounds(170, 300, 150, 22);
		adaptiveOutcomeSelect.setSelectedItem(ADAPTIVE_OUTCOME_NONE);
		adaptiveOutcomeSelect.setVisible(true);
	}

	calcButton.setBounds(300, 210, 100, 22);
	calcButton.setText("Calculate");
	calcButton.addActionListener(e -> calculateOnly());
	calcButton.setVisible(true);

	cancelButton.setBounds(25, 325, 145, 24);
	cancelButton.setText("Cancel");
	cancelButton.setVisible(true);
	cancelButton.addActionListener(e -> cancelPressed());

	confirmButton.setBounds(365, 325, 145, 24);
	confirmButton.setText("Confirm");
	confirmButton.addActionListener(e -> confirmPressed());
	confirmButton.setVisible(true);
	}

	public void clearDamage() {
		headerL.setVisible(false);
		for (JLabel l : labels) {
			l.setVisible(false);
		}
	numField.setVisible(false);
	crushField.setVisible(false);
	displacementLabel.setVisible(false);
	displacementField.setVisible(false);
	displacementField.setValue(0.0);
	evasionSaveCheckBox.setVisible(false);
	evasionSaveCheckBox.setSelected(false);
	resultField.setVisible(false);
	resultField.setText("");
	damageCodeField.setVisible(false);
	damageCodeField.setText("");
	adaptiveDefenseLabel.setVisible(false);
	adaptiveOutcomeLabel.setVisible(false);
	adaptiveDefenseSelect.setVisible(false);
	adaptiveDefenseSelect.setSelectedIndex(0);
	adaptiveOutcomeSelect.setVisible(false);
	adaptiveOutcomeSelect.setSelectedItem(ADAPTIVE_OUTCOME_NONE);
	loadedSmiteDamageCode = false;
	cancelButton.setVisible(false);
	for (var al : cancelButton.getActionListeners()) cancelButton.removeActionListener(al);
	confirmButton.setVisible(false);
	for (var al : confirmButton.getActionListeners()) confirmButton.removeActionListener(al);
	calcButton.setVisible(false);
	for (var al : calcButton.getActionListeners()) calcButton.removeActionListener(al);
	loadCodeButton.setVisible(false);
	for (var al : loadCodeButton.getActionListeners()) loadCodeButton.removeActionListener(al);
	if (damageType != null) {
		damageType.setVisible(false);
		remove(damageType);
	}
}

	public void cancelPressed() {
		clearDamage();
		this.setVisible(false);
	}

	public void confirmPressed() {
		if (!loadDamageCode(true)) return;
		Integer dmgresult = computeDamage();
		boolean minionHpChanged = false;
		if (dmgresult != null && character != null) {
			if (isTargetingMinion()) {
				applyMinionDamage(dmgresult);
				minionHpChanged = true;
			} else if (character.getResources() != null) {
				LifeLinkRedirect lifeLink = resolveLifeLinkRedirect(dmgresult);
				int redirected = lifeLink == null ? 0 : Math.max(0, lifeLink.amount());
				int leaderTaken = Math.max(0, dmgresult - redirected);

				double startingShufflePool = character.getCombat() == null ? 0.0 : character.getCombat().getShufflePool();
				double immediateDamage = character.applyIncomingHealthDamage(leaderTaken);
				double endingShufflePool = character.getCombat() == null ? 0.0 : character.getCombat().getShufflePool();
				double shuffledDamage = Math.max(0.0, endingShufflePool - startingShufflePool);

				double minionRedirected = 0.0;
				if (lifeLink != null && redirected > 0 && character.getMinions() != null) {
					minionRedirected = character.getMinions().applyUnmitigatedDamage(lifeLink.slotTitle(), redirected);
					minionHpChanged = minionRedirected > 0.0;
				}

				showDamageResultMessage(dmgresult, immediateDamage, shuffledDamage, lifeLink, minionRedirected);

				if (character.getCombat() != null && character.getCombat().getIncapacitateTokens() > 0) {
					character.getCombat().consumeIncapacitateToken();
					JOptionPane.showMessageDialog(this, "Incapacitate token removed due to taking damage.");
				}
			}
		}
		applyIncomingDisplacementIfNeeded();
		if (dmgresult != null && !isTargetingMinion()) {
			applyAdaptiveTechniqueOutcome();
		}
		clearDamage();
		this.setVisible(false);
		if (sheetFrame != null) {
			if (!isTargetingMinion()) {
				sheetFrame.refreshMainPanel();
			}
			if (minionHpChanged) {
				sheetFrame.refreshMinionPanel();
			}
		}
	}

	private void applyMinionDamage(int mitigatedDamage) {
		if (character == null || character.getMinions() == null || !isTargetingMinion()) return;
		double applied = character.getMinions().applyUnmitigatedDamage(targetMinionSlot, mitigatedDamage);
		String name = resolveTargetDisplayName();
		int remainingHp = character.getMinions().getCurrentHp(targetMinionSlot);
		JOptionPane.showMessageDialog(
				this,
				name + " took " + trimNumber(applied) + " damage after mitigation. "
						+ name + " has " + remainingHp + " HP remaining.");
	}

	private void applyIncomingDisplacementIfNeeded() {
		Object raw = displacementField.getValue();
		double original = raw instanceof Number number ? number.doubleValue() : 0.0;
		if (original <= 0.0) return;
		double chosen = FrameCombat.promptDisplacementChoice(this, character, original);
		JOptionPane.showMessageDialog(
				this,
				"Displace " + trimNumber(chosen) + " ft"
						+ (Math.abs(chosen - original) > 0.0001
								? " (original " + trimNumber(original) + " ft)."
								: "."),
				"Displacement",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void showDamageResultMessage(
			int totalMitigated,
			double immediateDamage,
			double shuffledDamage,
			LifeLinkRedirect lifeLink,
			double minionRedirected) {
		StringBuilder message = new StringBuilder();
		message.append("You took ").append(trimNumber(totalMitigated)).append(" damage after mitigation.");
		if (lifeLink != null && minionRedirected > 0.0) {
			message.append(" Life Link redirected ").append(trimNumber(minionRedirected))
					.append(" damage to ").append(lifeLink.displayName())
					.append(" (unmitigated).");
		}
		if (shuffledDamage > 0.0) {
			message.append(" ").append(trimNumber(immediateDamage))
					.append(" damage was dealt to HP and ")
					.append(trimNumber(shuffledDamage))
					.append(" damage was added to the shuffle pool.");
		} else if (lifeLink != null && minionRedirected > 0.0) {
			message.append(" You suffered ").append(trimNumber(immediateDamage)).append(" HP damage.");
		}
		JOptionPane.showMessageDialog(this, message.toString());
	}

	private LifeLinkRedirect resolveLifeLinkRedirect(int mitigatedDamage) {
		if (isTargetingMinion()) return null;
		if (!hasLifeLinkSpecialty() || mitigatedDamage <= 0) return null;
		if (character == null || character.getMinions() == null) return null;
		List<PanelCharMinion.SummonedMinionCombatInfo> minions = getSummonedMinionOptions();
		if (minions.isEmpty()) return null;

		int maxRedirect = mitigatedDamage / 2;
		if (maxRedirect <= 0) return null;

		int choice = JOptionPane.showConfirmDialog(
				this,
				"Use Life Link to redirect up to " + maxRedirect + " damage (50%) to a summoned minion?",
				"Life Link",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) return null;

		PanelCharMinion.SummonedMinionCombatInfo selectedMinion = promptForLifeLinkMinion(minions);
		if (selectedMinion == null) return null;

		Integer redirectAmount = promptForLifeLinkAmount(maxRedirect);
		if (redirectAmount == null || redirectAmount <= 0) return null;

		return new LifeLinkRedirect(selectedMinion.slotTitle(), selectedMinion.displayName(), redirectAmount);
	}

	private List<PanelCharMinion.SummonedMinionCombatInfo> getSummonedMinionOptions() {
		if (sheetFrame != null) {
			List<PanelCharMinion.SummonedMinionCombatInfo> fromSheet = sheetFrame.getSummonedMinionCombatInfo();
			if (fromSheet != null && !fromSheet.isEmpty()) return fromSheet;
		}
		if (character == null || character.getMinions() == null) return List.of();
		ArrayList<PanelCharMinion.SummonedMinionCombatInfo> options = new ArrayList<>();
		for (CharMinions.SummonedMinionActor actor : character.getSummonedMinionActors()) {
			if (actor == null) continue;
			options.add(new PanelCharMinion.SummonedMinionCombatInfo(
					actor.slotTitle(), actor.displayName(), actor.attackBonus()));
		}
		return options;
	}

	private PanelCharMinion.SummonedMinionCombatInfo promptForLifeLinkMinion(
			List<PanelCharMinion.SummonedMinionCombatInfo> minions) {
		if (minions == null || minions.isEmpty()) return null;
		if (minions.size() == 1) return minions.get(0);

		String[] labels = new String[minions.size()];
		for (int i = 0; i < minions.size(); i++) {
			PanelCharMinion.SummonedMinionCombatInfo minion = minions.get(i);
			labels[i] = minion == null ? "Unknown" : minion.displayName();
		}
		Object selected = JOptionPane.showInputDialog(
				this,
				"Select a minion to receive redirected damage:",
				"Life Link",
				JOptionPane.QUESTION_MESSAGE,
				null,
				labels,
				labels[0]);
		if (selected == null) return null;
		for (int i = 0; i < labels.length; i++) {
			if (selected.equals(labels[i])) {
				return minions.get(i);
			}
		}
		return null;
	}

	private Integer promptForLifeLinkAmount(int maxRedirect) {
		while (true) {
			String input = JOptionPane.showInputDialog(
					this,
					"How much damage should be redirected? (1-" + maxRedirect + ")",
					Integer.toString(maxRedirect));
			if (input == null) return null;
			try {
				int value = Integer.parseInt(input.trim());
				if (value < 1 || value > maxRedirect) {
					JOptionPane.showMessageDialog(
							this,
							"Enter a whole number between 1 and " + maxRedirect + ".",
							"Life Link",
							JOptionPane.WARNING_MESSAGE);
					continue;
				}
				return value;
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(
						this,
						"Enter a whole number between 1 and " + maxRedirect + ".",
						"Life Link",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void calculateOnly() {
		if (!loadDamageCode(true)) return;
		Integer dmgresult = computeDamage();
		if (dmgresult != null) {
			resultField.setText(String.valueOf(dmgresult));
		}
	}

	private boolean loadDamageCode(boolean showErrors) {
		if (damageCodeField == null) return true;
		String codeText = damageCodeField.getText();
		if (codeText == null || codeText.trim().isBlank()) return true;
		DamageCodeParser.DamageCodeParseResult parsed = DamageCodeParser.parse(codeText);
		if (!parsed.isValid()) {
			if (showErrors) {
				JOptionPane.showMessageDialog(this, parsed.getError(), "Invalid Damage Code", JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		applyDamageCode(parsed.getCode());
		return true;
	}

	private void applyDamageCode(DamageCodeParser.DamageCode code) {
		if (code == null) return;
		selectDamageType(code.getDamageType());
		numField.setValue(code.getAmount());
		crushField.setValue(code.getCrush());
		loadedSmiteDamageCode = code.isSmite();
	}

	private void selectDamageType(String type) {
		if (damageType == null || type == null || type.isBlank()) return;
		String normalized = type.trim().toUpperCase(java.util.Locale.ROOT);
		boolean found = false;
		for (int i = 0; i < damageType.getItemCount(); i++) {
			String item = damageType.getItemAt(i);
			if (item != null && normalized.equalsIgnoreCase(item.trim())) {
				found = true;
				damageType.setSelectedIndex(i);
				break;
			}
		}
		if (!found) {
			damageType.addItem(normalized);
			damageType.setSelectedItem(normalized);
		}
	}

	/** Computes adjusted damage; returns null if invalid input. */
	private Integer computeDamage() {
		Double damage = (Double) numField.getValue();
		String tempString = (String) damageType.getSelectedItem();
		if (damage == null || damage < 0.0) {
			JOptionPane.showMessageDialog(this, "Please insert a valid damage amount.");
			return null;
		}
		double crush = crushField.getValue() instanceof Number ? ((Number) crushField.getValue()).doubleValue() : 0.0;
		if (isTargetingMinion()) {
			return computeMitigatedDamage(
					resolveTargetAttributes(),
					resolveTargetCombat(),
					damage,
					tempString,
					crush,
					false,
					false);
		}
		boolean evasionSave = hasEvasionSpecialty() && evasionSaveCheckBox.isVisible() && evasionSaveCheckBox.isSelected();
		return computeMitigatedDamage(character, damage, tempString, crush, loadedSmiteDamageCode, evasionSave);
	}

	static Integer computeMitigatedDamage(StoreCharData character, double incomingAmount, String damageTypeKey,
			double crush, boolean ignoreResist, boolean evasionSaveSucceeded) {
		CharAttributes attributes = character == null ? null : character.getAttributes();
		CharCombat combat = character == null ? null : character.getCombat();
		return computeMitigatedDamage(attributes, combat, incomingAmount, damageTypeKey, crush, ignoreResist, evasionSaveSucceeded);
	}

	static Integer computeMitigatedDamage(CharAttributes attributes, CharCombat combat, double incomingAmount,
			String damageTypeKey, double crush, boolean ignoreResist, boolean evasionSaveSucceeded) {
		if (incomingAmount < 0.0) {
			return null;
		}
		if (incomingAmount == 0.0) {
			return 0;
		}
		if (attributes == null) {
			return (int) incomingAmount;
		}
		if (evasionSaveSucceeded) {
			return 0;
		}

		double damage = incomingAmount;
		if (!ignoreResist) {
			double resistAll = Math.max(0.0, attributes.calcStatusValue("ALL"));
			double effectiveResistAll = Math.max(0.0, resistAll - Math.max(0.0, crush));
			double typedResistance = Math.max(0.0, typedResistanceOf(attributes, damageTypeKey));
			double totalResistance = effectiveResistAll + typedResistance;
			damage *= Math.pow(0.99, totalResistance);
		}
		damage += damageTakenIncreaseOf(combat);
		if (damage < 1.0) {
			damage = 1.0;
		}
		return (int) damage;
	}

	static double damageTakenIncreaseOf(StoreCharData character) {
		return damageTakenIncreaseOf(character == null ? null : character.getCombat());
	}

	static double damageTakenIncreaseOf(CharCombat combat) {
		if (combat == null || combat.getCombatStatus() == null) {
			return 0.0;
		}
		double total = 0.0;
		for (DataStatus status : combat.getCombatStatus()) {
			if (status == null || status.getAttribute() == null) continue;
			if ("DMGTAKEN".equalsIgnoreCase(status.getAttribute().trim())) {
				total += status.getSeverity();
			}
		}
		return total;
	}

	static double typedResistanceOf(StoreCharData character, String damageTypeKey) {
		return typedResistanceOf(character == null ? null : character.getAttributes(), damageTypeKey);
	}

	static double typedResistanceOf(CharAttributes attributes, String damageTypeKey) {
		if (damageTypeKey == null || damageTypeKey.isBlank() || attributes == null) {
			return 0.0;
		}
		String normalized = damageTypeKey.trim().toUpperCase(java.util.Locale.ROOT);
		return switch (normalized) {
			case "RESISTALL", "ALL" -> attributes.calcStatusValue("ALL");
			default -> attributes.calcStatusValue(normalized);
		};
	}

	private boolean isTargetingMinion() {
		return targetMinionSlot != null && !targetMinionSlot.isBlank();
	}

	private String resolveTargetDisplayName() {
		if (!isTargetingMinion() || character == null || character.getMinions() == null) {
			return "Leader";
		}
		MinionSlotState slot = character.getMinions().findSlot(targetMinionSlot);
		return slot == null ? targetMinionSlot : slot.buildDisplayName();
	}

	private CharAttributes resolveTargetAttributes() {
		if (!isTargetingMinion() || character == null || character.getMinions() == null) {
			return character == null ? null : character.getAttributes();
		}
		MinionSlotState slot = character.getMinions().findSlot(targetMinionSlot);
		return character.getMinions().buildMinionAttributes(slot);
	}

	private CharCombat resolveTargetCombat() {
		if (!isTargetingMinion() || character == null || character.getMinions() == null) {
			return character == null ? null : character.getCombat();
		}
		MinionCombatState minionCombat = character.getMinions().ensureCombat(targetMinionSlot);
		return minionCombat == null ? null : minionCombat.getCombat();
	}

	private String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return Integer.toString((int) Math.rint(value));
		}
		return Double.toString(Math.round(value * 100.0) / 100.0);
	}

	private boolean hasEvasionSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Evasion");
	}

	private boolean hasLifeLinkSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(LIFE_LINK_SPECIALTY);
	}

	private boolean hasAdaptiveTechniqueSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(ADAPTIVE_TECHNIQUE_SPECIALTY);
	}

	private void applyAdaptiveTechniqueOutcome() {
		if (!hasAdaptiveTechniqueSpecialty() || character == null || character.getCombat() == null) {
			return;
		}
		String defenseKey = getSelectedAdaptiveDefenseKey();
		if (defenseKey == null) {
			return;
		}
		Object selectedOutcome = adaptiveOutcomeSelect.getSelectedItem();
		String outcome = selectedOutcome == null ? ADAPTIVE_OUTCOME_NONE : selectedOutcome.toString();
		int delta = 0;
		if (ADAPTIVE_OUTCOME_STRUCK.equalsIgnoreCase(outcome)) {
			delta = 1;
		} else if (ADAPTIVE_OUTCOME_AVOIDED.equalsIgnoreCase(outcome)) {
			delta = -1;
		}
		if (delta == 0) {
			return;
		}
		character.getCombat().adjustAdaptiveTechniqueBonus(defenseKey, delta);
		character.updateAll();
	}

	private String getSelectedAdaptiveDefenseKey() {
		Object selectedItem = adaptiveDefenseSelect.getSelectedItem();
		if (selectedItem == null) {
			return null;
		}
		String value = selectedItem.toString();
		if (value == null || value.isBlank() || "None".equalsIgnoreCase(value.trim())) {
			return null;
		}
		return value.trim().toUpperCase(java.util.Locale.ROOT);
	}
}

