package eternity;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JOptionPane;

public class FrameCombatDamage extends JFrame {
	private static final long serialVersionUID = 1;

	private final FrameSheet sheetFrame;
	private final StoreCharData character;

	private JComboBox<String> damageType;
private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME", "DIVINE"};

// UI elements
private final JLabel headerL = new JLabel();
private final JLabel[] labels = new JLabel[5];
private final JSpinner numField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JSpinner crushField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JCheckBox evasionSaveCheckBox = new JCheckBox("Successful damage-reducing save");
private final JTextField resultField = new JTextField();
private final JTextField damageCodeField = new JTextField();
private final JButton cancelButton = new JButton();
private final JButton confirmButton = new JButton();
private final JButton calcButton = new JButton();
private final JButton loadCodeButton = new JButton();
private boolean loadedSmiteDamageCode;

	FrameCombatDamage(FrameSheet sheetFrame, StoreCharData character) {
		super("Under Attack Helper");
		this.sheetFrame = sheetFrame;
		this.character = character;
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
	evasionSaveCheckBox.setVisible(false);
	add(evasionSaveCheckBox);
	resultField.setVisible(false);
	resultField.setEditable(false);
	add(resultField);
	damageCodeField.setVisible(false);
	add(damageCodeField);

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

		headerL.setText("Under Attack Helper");
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

	labels[3].setBounds(25, 185, 120, 20);
	labels[3].setText("Total Damage");
	labels[3].setVisible(true);
	resultField.setBounds(150, 185, 120, 22);
	resultField.setText("");
	resultField.setVisible(true);

	if (hasEvasionSpecialty()) {
		evasionSaveCheckBox.setBounds(25, 220, 260, 22);
		evasionSaveCheckBox.setSelected(false);
		evasionSaveCheckBox.setToolTipText("If this effect allowed a save to reduce damage and the save succeeded, Evasion reduces the damage to 0.");
		evasionSaveCheckBox.setVisible(true);
	}

	calcButton.setBounds(300, 185, 100, 22);
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
	evasionSaveCheckBox.setVisible(false);
	evasionSaveCheckBox.setSelected(false);
	resultField.setVisible(false);
	resultField.setText("");
	damageCodeField.setVisible(false);
	damageCodeField.setText("");
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
		if (dmgresult != null && character != null && character.getResources() != null) {
			double startingShufflePool = character.getCombat() == null ? 0.0 : character.getCombat().getShufflePool();
			double immediateDamage = character.applyIncomingHealthDamage(dmgresult);
			double endingShufflePool = character.getCombat() == null ? 0.0 : character.getCombat().getShufflePool();
			double shuffledDamage = Math.max(0.0, endingShufflePool - startingShufflePool);
			if (shuffledDamage > 0.0) {
				JOptionPane.showMessageDialog(this,
						"You took " + trimNumber(dmgresult) + " damage. "
								+ trimNumber(immediateDamage) + " damage was dealt to HP and "
								+ trimNumber(shuffledDamage) + " damage was added to the shuffle pool.");
			} else {
				JOptionPane.showMessageDialog(this, "You took " + trimNumber(dmgresult) + " damage.");
			}
			if (character.getCombat() != null && character.getCombat().getIncapacitateTokens() > 0) {
				character.getCombat().consumeIncapacitateToken();
				JOptionPane.showMessageDialog(this, "Incapacitate token removed due to taking damage.");
			}
		}
		clearDamage();
		this.setVisible(false);
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
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
		if (damage == 0.0) {
			return 0;
		}
		if (character == null || character.getAttributes() == null) {
			return damage.intValue();
		}
		if (hasEvasionSpecialty() && evasionSaveCheckBox.isVisible() && evasionSaveCheckBox.isSelected()) {
			return 0;
		}

		double crush = (Double) crushField.getValue();
		if (!loadedSmiteDamageCode) {
			double resistAll = Math.max(0.0, character.getAttributes().calcStatusValue("ALL"));
			double effectiveResistAll = Math.max(0.0, resistAll - Math.max(0.0, crush));
			double typedResistance = Math.max(0.0, getTypedResistance(tempString));
			double totalResistance = effectiveResistAll + typedResistance;
			damage *= Math.pow(0.99, totalResistance);
		}
		damage += getDamageTakenIncrease();
		if (damage < 1.0) damage = 1.0;

		return damage.intValue();
	}

	private double getDamageTakenIncrease() {
		if (character == null || character.getCombat() == null || character.getCombat().getCombatStatus() == null) {
			return 0.0;
		}
		double total = 0.0;
		for (DataStatus status : character.getCombat().getCombatStatus()) {
			if (status == null || status.getAttribute() == null) continue;
			if ("DMGTAKEN".equalsIgnoreCase(status.getAttribute().trim())) {
				total += status.getSeverity();
			}
		}
		return total;
	}

	private double getTypedResistance(String damageTypeKey) {
		if (damageTypeKey == null || damageTypeKey.isBlank() || character == null || character.getAttributes() == null) {
			return 0.0;
		}
		String normalized = damageTypeKey.trim().toUpperCase(java.util.Locale.ROOT);
		return switch (normalized) {
			case "RESISTALL", "ALL" -> character.getAttributes().calcStatusValue("ALL");
			default -> character.getAttributes().calcStatusValue(normalized);
		};
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
}

