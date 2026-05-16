package eternity;

import java.awt.Font;

import javax.swing.JButton;
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
private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME"};

// UI elements
private final JLabel headerL = new JLabel();
private final JLabel[] labels = new JLabel[4];
private final JSpinner numField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JSpinner crushField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));
private final JTextField resultField = new JTextField();
private final JButton cancelButton = new JButton();
private final JButton confirmButton = new JButton();
private final JButton calcButton = new JButton();

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
	resultField.setVisible(false);
	resultField.setEditable(false);
	add(resultField);

	cancelButton.setVisible(false);
	add(cancelButton);
	confirmButton.setVisible(false);
	add(confirmButton);
	calcButton.setVisible(false);
	add(calcButton);
	}

	public void underAttack() {
		clearDamage();

		headerL.setText("Under Attack Helper");
		headerL.setVisible(true);

		labels[0].setBounds(25, 100, 100, 20);
		labels[0].setText("Damage Type");
		labels[0].setVisible(true);

		damageType = new JComboBox<String>(DMGTYPE);
		damageType.setBounds(25, 125, 120, 22);
		add(damageType);

		labels[1].setBounds(170, 100, 120, 20);
	labels[1].setText("Damage Amount");
	labels[1].setVisible(true);
	numField.setBounds(170, 125, 100, 22);
	numField.setValue(0.0);
	numField.setVisible(true);

	labels[2].setBounds(300, 100, 120, 20);
	labels[2].setText("Enemy Crush");
	labels[2].setVisible(true);
	crushField.setBounds(300, 125, 100, 22);
	crushField.setValue(0.0);
	crushField.setVisible(true);

	labels[3].setBounds(25, 170, 120, 20);
	labels[3].setText("Total Damage");
	labels[3].setVisible(true);
	resultField.setBounds(150, 170, 120, 22);
	resultField.setText("");
	resultField.setVisible(true);

	calcButton.setBounds(300, 170, 100, 22);
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
	resultField.setVisible(false);
	resultField.setText("");
	cancelButton.setVisible(false);
	for (var al : cancelButton.getActionListeners()) cancelButton.removeActionListener(al);
	confirmButton.setVisible(false);
	for (var al : confirmButton.getActionListeners()) confirmButton.removeActionListener(al);
	calcButton.setVisible(false);
	for (var al : calcButton.getActionListeners()) calcButton.removeActionListener(al);
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
		Integer dmgresult = computeDamage();
		if (dmgresult != null && character != null && character.getResources() != null) {
			JOptionPane.showMessageDialog(this, "You took " + dmgresult + " damage.");
			double newLost = character.getResources().getLostHP() + dmgresult;
			character.getResources().setLostHP(newLost);
		}
		clearDamage();
		this.setVisible(false);
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
		}
	}

	private void calculateOnly() {
		Integer dmgresult = computeDamage();
		if (dmgresult != null) {
			resultField.setText(String.valueOf(dmgresult));
		}
	}

	/** Computes adjusted damage; returns null if invalid input. */
	private Integer computeDamage() {
		Double damage = (Double) numField.getValue();
		String tempString = (String) damageType.getSelectedItem();
		if (damage == null || damage <= 0.0) {
			JOptionPane.showMessageDialog(this, "Please insert a valid damage amount.");
			return null;
		}
		if (character == null || character.getAttributes() == null) {
			return damage.intValue();
		}

		//int resAll = character.getAttributes().getResist("ALL");
        //int typeRes = character.getAttributes().getResist(tempString);
		double crush = (Double) crushField.getValue();
		//int effectiveResAll = (int)Math.max(0, resAll - crush);
		// Increase effectiveResAll by the specific type resistance
		//effectiveResAll += typeRes;
        //double resPercent = 1.0 - Math.pow(0.99, effectiveResAll);
        //damage *= (1.0 - resPercent);
        if (damage < 1.0) damage = 1.0;

		return damage.intValue();
	}
}

