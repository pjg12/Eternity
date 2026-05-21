package eternity;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class FrameAttack extends JFrame {
	private static final long serialVersionUID = 1;

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private StoreCharData character;
	private DataAction action;
	private JComboBox<String> select1;
	private JComboBox<Integer> alSelect;
	private JComboBox<String> damageType;
	private boolean actionResolved;
	private boolean attackRollUsed;
	
	private double attackBonus, result;
	private CardLayout cardLayout;
	private JPanel cardHolderPanel;
	private JPanel attackCardPanel;
	private JPanel castCardPanel;
	private JPanel activeCardPanel;
	private static final String CARD_ATTACK = "attack";
	private static final String CARD_CAST = "cast";
	
	private final String[] YESNO = {"Yes", "No"};
	private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME"};

	// UI elements (replacing FrameHelper utilities)
	private final JLabel headerL = new JLabel();
	private final JLabel[] labels = new JLabel[11];
	private final JTextField[] textFields = new JTextField[8];
	private final JSpinner[] numFields = new JSpinner[1];
	private final JButton[] buttons = new JButton[4];
	
	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Attack Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action;
		this.actionResolved = false;
		this.attackRollUsed = false;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(550, 400);
		setLocationRelativeTo(null);
		setResizable(false);

		initComponents();
		showInitialStage();
	}

	/** Builds base UI components used by the legacy layout. */
	private void initComponents() {
		cardLayout = new CardLayout();
		cardHolderPanel = new JPanel(cardLayout);
		attackCardPanel = buildCardPanel();
		castCardPanel = buildCardPanel();
		cardHolderPanel.add(attackCardPanel, CARD_ATTACK);
		cardHolderPanel.add(castCardPanel, CARD_CAST);
		add(cardHolderPanel, BorderLayout.CENTER);
		activeCardPanel = attackCardPanel;

		headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 14f));
		headerL.setBounds(25, 20, 500, 30);
		headerL.setHorizontalAlignment(JLabel.CENTER);
		headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 18f));
		headerL.setVisible(false);
		activeCardPanel.add(headerL);

		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel();
			labels[i].setVisible(false);
			activeCardPanel.add(labels[i]);
		}

		for (int i = 0; i < textFields.length; i++) {
			textFields[i] = new JTextField();
			textFields[i].setVisible(false);
			activeCardPanel.add(textFields[i]);
		}

		numFields[0] = new JSpinner(new SpinnerNumberModel(0.0, -9999.0, 9999.0, 1.0));
		numFields[0].setVisible(false);
		activeCardPanel.add(numFields[0]);

		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton();
			buttons[i].setVisible(false);
			activeCardPanel.add(buttons[i]);
		}
	}

	private JPanel buildCardPanel() {
		JPanel panel = new JPanel(null);
		return panel;
	}
	
	public void attackStage() {
		showCard(CARD_ATTACK);
		attackStageLayout(false);
	}

	public void castStage() {
		showCard(CARD_CAST);
		attackStageLayout(true);
	}
	
	public void damageStage() {
		clearStdAction();
		syncActionFromCurrentAl();
		/*
		 * Set Headers
		*/
		headerL.setText("Standard Action Helper:  " + action.getName());
		headerL.setVisible(true);
		
		labels[0].setBounds(25, 80, 100, 20);
		labels[0].setText("Dice");
		labels[0].setVisible(true);
		textFields[0].setBounds(25, 103, 100, 22);
		textFields[0].setText("" + damage());
		textFields[0].setVisible(true);
		textFields[0].setEditable(false);
		
		labels[1].setBounds(145, 80, 120, 20);
		labels[1].setText("Base Bonus");
		labels[1].setVisible(true);
		textFields[1].setBounds(145, 103, 120, 22);
		textFields[1].setText("+" + getBaseDamage());
		textFields[1].setVisible(true);
		textFields[1].setEditable(false);
		
		labels[2].setBounds(285, 80, 100, 20);
		labels[2].setText("Multiplier");
		labels[2].setVisible(true);
		textFields[2].setBounds(285, 103, 100, 22);
		textFields[2].setText("*1");
		textFields[2].setVisible(true);
		textFields[2].setEditable(false);
		
		labels[3].setBounds(405, 80, 100, 20);
		labels[3].setText("Total Bonus");
		labels[3].setVisible(true);
		textFields[3].setBounds(405, 103, 100, 22);
		textFields[3].setText("+" + getTotalDamage());
		textFields[3].setVisible(true);
		textFields[3].setEditable(false);

		labels[4].setBounds(25, 150, 120, 20);
		labels[4].setText("Damage Type");
		labels[4].setVisible(true);
		if (damageType == null) {
			damageType = new JComboBox<String>(DMGTYPE);
		}
		if (damageType.getParent() == null) {
			activeCardPanel.add(damageType);
		}
		damageType.setBounds(25, 173, 130, 22);
		applyDamageTypeSelection();
		damageType.setVisible(true);

		if (isAuraTechniqueAction()) {
			labels[6].setBounds(170, 150, 120, 20);
			labels[6].setText("AL");
			labels[6].setVisible(true);
			ensureAlSelect();
			populateAlSelect();
			alSelect.setBounds(170, 173, 100, 22);
			alSelect.setVisible(true);
		}
		
		/*if (character.getTraining() != null && character.getTraining().hasSpec(3011)) {
			labels[4].setBounds(140, 160, 100, 20);
			labels[4].setText("Sneak Attack?");
			labels[4].setVisible(true);
			select1 = new JComboBox<String>(YESNO);
			select1.addActionListener(e -> updateSneak());
			select1.setBounds(340, 125, 100, 20);
			if (select1.getParent() == null) {
				add(select1);
			}
			select1.setVisible(true);
		}*/
		
		labels[5].setBounds(220, 280, 100, 20);
		labels[5].setText("Result");
		numFields[0].setBounds(220, 305, 100, 20);
		numFields[0].setValue(0);
		
		buttons[2].setBounds(195, 325, 145, 20);
		buttons[2].setText("Roll");
		buttons[2].setVisible(true);
		buttons[2].setEnabled(true);
		buttons[2].addActionListener(e -> copyRollToClipboard());
		
		buttons[0].setBounds(25, 325, 145, 20);
		buttons[0].setText("Cancel");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> cancelPressed());
	}
	
	public void updateSneak() {
		if (select1.getSelectedIndex()==0) {
			textFields[0].setText(getDamage());
		}
		else {
			textFields[0].setText(getDamage() + "+" + getLevel() + "d6");
		}
	}

	public void updateCharacter(StoreCharData character, DataAction action) {
		this.character = character;
		this.action = action;
		showInitialStage();
	}
	
	public void clearStdAction() {
		clearUiState();
	}

	/** Resets/hides reusable UI widgets and removes stale listeners. */
	private void clearUiState() {
		headerL.setVisible(false);
		for (JLabel l : labels) {
			l.setVisible(false);
			if (l != null) l.setText("");
		}
		for (JTextField tf : textFields) {
			if (tf == null) continue;
			tf.setText("");
			tf.setVisible(false);
			tf.setEditable(true);
		}
		for (JSpinner sp : numFields) {
			if (sp == null) continue;
			sp.setValue(0.0);
			sp.setVisible(false);
		}
		for (JButton b : buttons) {
			if (b == null) continue;
			for (ActionListener al : b.getActionListeners()) {
				b.removeActionListener(al);
			}
			b.setVisible(false);
			b.setText("");
		}
		if (select1 != null) {
			for (ActionListener al : select1.getActionListeners()) {
				select1.removeActionListener(al);
			}
			select1.setVisible(false);
		}
		if (alSelect != null) {
			for (ActionListener al : alSelect.getActionListeners()) {
				alSelect.removeActionListener(al);
			}
			alSelect.setVisible(false);
			if (alSelect.getParent() != null) {
				alSelect.getParent().remove(alSelect);
			}
		}
		if (damageType != null) {
			for (ActionListener al : damageType.getActionListeners()) {
				damageType.removeActionListener(al);
			}
			damageType.setVisible(false);
			if (damageType.getParent() != null) {
				damageType.getParent().remove(damageType);
			}
		}
	}

	private void showCard(String cardName) {
		JPanel targetPanel = CARD_CAST.equals(cardName) ? castCardPanel : attackCardPanel;
		if (cardLayout != null && cardHolderPanel != null) {
			cardLayout.show(cardHolderPanel, CARD_CAST.equals(cardName) ? CARD_CAST : CARD_ATTACK);
		}
		if (activeCardPanel != targetPanel) {
			remountSharedComponents(targetPanel);
			activeCardPanel = targetPanel;
		}
	}

	private void remountSharedComponents(JPanel targetPanel) {
		if (targetPanel == null) return;
		attachComponent(targetPanel, headerL);
		for (JLabel label : labels) attachComponent(targetPanel, label);
		for (JTextField field : textFields) attachComponent(targetPanel, field);
		for (JSpinner spinner : numFields) attachComponent(targetPanel, spinner);
		for (JButton button : buttons) attachComponent(targetPanel, button);
		attachComponent(targetPanel, select1);
		attachComponent(targetPanel, alSelect);
		attachComponent(targetPanel, damageType);
		targetPanel.revalidate();
		targetPanel.repaint();
	}

	private void attachComponent(JPanel targetPanel, Component component) {
		if (targetPanel == null || component == null) return;
		if (component.getParent() != null) {
			component.getParent().remove(component);
		}
		targetPanel.add(component);
	}

	private void attackStageLayout(boolean castLayout) {
		clearStdAction();
		syncActionFromCurrentAl();
		attackRollUsed = false;
		/*
		 * Set Headers
		*/
		headerL.setText(action.getName());
		headerL.setVisible(true);
		
		labels[2].setBounds(25, 80, 100, 20);
		labels[2].setText(castLayout ? "Application" : "Attack");
		labels[2].setVisible(true);
		textFields[2].setBounds(25, 103, 100, 22);
		attackBonus = castLayout ? getApplication() : getAttack();
		textFields[2].setText(formatAttackDisplay((int) attackBonus));
		textFields[2].setVisible(true);
		textFields[2].setEditable(false);

		labels[1].setBounds(25, 150, 100, 20);
		labels[1].setText("Affinity");
		labels[1].setVisible(true);
		textFields[1].setBounds(25, 173, 100, 22);
		textFields[1].setText(action.getAffinity());
		textFields[1].setVisible(true);
		textFields[1].setEditable(false);

		labels[4].setBounds(145, 80, 100, 20);
		labels[4].setText("Base Damage");
		labels[4].setVisible(true);
		textFields[3].setBounds(145, 103, 100, 22);
		textFields[3].setText(String.valueOf(getBaseDamage()));
		textFields[3].setVisible(true);
		textFields[3].setEditable(false);

		labels[5].setBounds(265, 80, 100, 20);
		labels[5].setText("Multiplier");
		labels[5].setVisible(true);
		textFields[4].setBounds(265, 103, 100, 22);
		textFields[4].setText(String.valueOf(getCharDmgMulti()));
		textFields[4].setVisible(true);
		textFields[4].setEditable(false);

		labels[6].setBounds(385, 80, 100, 20);
		labels[6].setText("Total Damage");
		labels[6].setVisible(true);
		textFields[5].setBounds(385, 103, 100, 22);
		textFields[5].setText(String.valueOf(getTotalDamage()));
		textFields[5].setVisible(true);
		textFields[5].setEditable(false);

		labels[7].setBounds(145, 150, 100, 20);
		labels[7].setText("Range");
		labels[7].setVisible(true);
		textFields[6].setBounds(145, 173, 100, 22);
		textFields[6].setText(getRange() <= 0 ? "Melee" : (getRange() + " ft"));
		textFields[6].setVisible(true);
		textFields[6].setEditable(false);

		if (isAuraTechniqueAction()) {
			labels[8].setBounds(25, 205, 120, 20);
			labels[8].setText("AL");
			labels[8].setVisible(true);
			ensureAlSelect();
			populateAlSelect();
			alSelect.setBounds(25, 228, 100, 22);
			alSelect.setVisible(true);
		}

		if (isFullAttackAction()) {
			labels[0].setBounds(25, 205, 120, 20);
			labels[0].setText("Extra Attacks");
			labels[0].setVisible(true);
			textFields[0].setBounds(25, 228, 100, 22);
			textFields[0].setText(String.valueOf(getExtraAttacks()));
			textFields[0].setVisible(true);
			textFields[0].setEditable(false);
		}

		labels[9].setBounds(385, 150, 120, 20);
		labels[9].setText("Damage Type");
		labels[9].setVisible(true);
		if (damageType == null) {
			damageType = new JComboBox<String>(DMGTYPE);
		}
		if (damageType.getParent() == null) {
			activeCardPanel.add(damageType);
		}
		damageType.setBounds(385, 173, 100, 22);
		damageType.setVisible(true);
		applyDamageTypeSelection();
		attachDamageTypeListener();

		labels[10].setBounds(265, 150, 100, 20);
		labels[10].setText("Attack vs:");
		labels[10].setVisible(true);
		textFields[7].setBounds(265, 173, 100, 22);
		updateAttackVsField();
		textFields[7].setVisible(true);
		textFields[7].setEditable(false);
		
		labels[3].setBounds(220, 280, 100, 20);
		labels[3].setText("Result");
		numFields[0].setBounds(220, 305, 100, 20);
		numFields[0].setValue(0.0);
		
		buttons[2].setBounds(195, 325, 145, 20);
		buttons[2].setText("Roll");
		buttons[2].setVisible(true);
		buttons[2].setEnabled(true);
		buttons[2].addActionListener(e -> copyRollToClipboard());
		
		buttons[0].setBounds(25, 325, 145, 20);
		buttons[0].setText("Cancel");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> cancelPressed());
		buttons[3].setBounds(365, 325, 145, 20);
		buttons[3].setText("Confirm");
		buttons[3].setVisible(true);
		buttons[3].addActionListener(e -> confirmPressed());
	}
	
	public void cancelPressed() {
		this.setVisible(false);
		this.dispose();
	}
	
	public void atkRollPressed() {
		buttons[1].setVisible(true);
		numFields[0].setVisible(true);
		labels[3].setVisible(true);
		DataColor raceColor = getDisplayColor();
		String colorString1 = String.format("#%02x%02x%02x", raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x", raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String tempString = "!scriptcard {{ --#titleCardBackground|" + colorString1 + " --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|" + colorString1;
		tempString += " --#titleCardBottomBorder|4px solid #000000; --#title|";
		tempString += getCharName() + " --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|" + colorString2 + " --#leftSub|";
		tempString += action.getName() + " - Attack --#LineHeight|1.5em --#rollHilightLineHeight|1.5em  --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		tempString += " --=SkillCheck|1d20+" + attackBonus + " --+| [$SkillCheck] }}";
		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	public void dmgRollPressed() {
		buttons[1].setVisible(true);
		numFields[0].setVisible(true);
		labels[3].setVisible(true);
		DataColor raceColor = getDisplayColor();
		String colorString1 = String.format("#%02x%02x%02x", raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x", raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String tempString = "!scriptcard {{ --#titleCardBackground|" + colorString1 + " --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|" + colorString1;
		tempString += " --#titleCardBottomBorder|4px solid #000000; --#title|";
		tempString += getCharName() + " --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|" + colorString2 + " --#leftSub|";
		tempString += action.getName() + " - Damage --#LineHeight|1.5em --#rollHilightLineHeight|1.5em  --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		tempString += " --=SkillCheck|" + textFields[0].getText() + "*" + 1 + "+" + getBaseDamage() + "*" + 1 + "+" + getTotalDamage() + " [DieRoll] --+| Damage: [$SkillCheck]}}";
		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	public void confirmPressed() {
		if (!attackRollUsed) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"You have not clicked Roll. Do you want to continue anyway?",
					"Proceed Without Roll",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}
		finishCombatActionIfNeeded();
		this.setVisible(false);
		this.dispose();
	}
	
	private void finishCombatActionIfNeeded() {
		if (actionResolved) return;
		if (combatFrame != null && action != null && action.getActionType() != null) {
			combatFrame.resolveAttackAction(action.getActionType());
		}
		actionResolved = true;
	}

	private void copyRollToClipboard() {
		if (character == null) return;
		if (buttons[3] != null && buttons[3].isVisible()) {
			attackRollUsed = true;
		}
		// Safe color lookup with sensible fallback
		DataColor raceColor = null;
		StoreRuleManager dataQuery = new StoreRuleManager();
		if (character.getIdentity() != null) {
			raceColor = dataQuery.getColorByTitle(character.getIdentity().getRace());
		}
		if (raceColor == null) {
			raceColor = new DataColor("Default", 0, 0, 0, 255, 255, 255);
		}
		String colorString1 = String.format("#%02x%02x%02x", raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x", raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String tempString = "!scriptcard {{ --#titleCardBackground|" + colorString1 + " --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|" + colorString1;
		tempString += " --#titleCardBottomBorder|4px solid #000000; --#title|";
		String charName = (character.getIdentity() != null && character.getIdentity().getName() != null)
				? character.getIdentity().getName()
				: "Character";
		tempString += charName + " --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|" + colorString2 + " --#leftSub|";
		String actionSubtitle = action.getName();
		if (alSelect != null && alSelect.getSelectedItem() != null) {
			actionSubtitle += " AL: " + alSelect.getSelectedItem();
		}
		tempString += actionSubtitle + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		int bdmgMod = 0;
		int tdmgMod = 0;
		double dmgMulti = 1.0;
		if (character.getAttributes() != null) {
			bdmgMod = getBaseDamage();
			tdmgMod = getTotalDamage();
			dmgMulti = getCharDmgMulti();
		}
		tempString += " --+|Range: " + (getRange() <= 0 ? "Melee" : (getRange() + " ft"));
		
		tempString += buildPrimaryRollMacroBlock();

		tempString += "[br]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; vs " + textFields[7].getText();
		tempString += " --=RawPercentRoll2|1d21 + 9";
		tempString += " --=DamagePercentCount|1";
		tempString += " --=PercentRoll2|[$RawPercentRoll2] * 5 / 100";
		tempString += " --=DamageRoll|[$PercentRoll2] * " + bdmgMod + " * " + dmgMulti + " + " + tdmgMod + " {FLOOR} ";
		tempString += " --+|Dmg Percent Roll: [$RawPercentRoll2] x 5% = [$PercentRoll2] [br]&nbsp;&nbsp;";
		tempString += "Damage Roll: [$DamageRoll] = [br]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ([$PercentRoll2] x " +  bdmgMod + ") x " + dmgMulti + " + " + tdmgMod + "[br]&nbsp;&nbsp;";
		tempString += "Damage Type: " + damageType.getSelectedItem().toString();
		
		tempString += "--=Critx1|2 --=Critx2|3 --=Critx3|4 --=Critx4|5 --=Crit1|[$AttackRoll]/2 {FLOOR} --=Crit2|[$AttackRoll]/3 {FLOOR} --=Crit3|[$AttackRoll]/4 {FLOOR} --=Crit4|[$AttackRoll]/5 {FLOOR} --=Critd1|[$DamageRoll]*2*" + getCritDamage() + " --=Critd2|[$DamageRoll]*3*" + getCritDamage() + " --=Critd3|[$DamageRoll]*4*" + getCritDamage() + " --=Critd4|[$DamageRoll]*5*" + getCritDamage();
		tempString += " --+|Critical Threat [br]&nbsp;&nbsp;"; 
		tempString += "  Crit Multi: [$Critx1] [$Critx2] [$Critx3] [$Critx4] [br]&nbsp;&nbsp; Crit AC #: [$Crit1] [$Crit2] [$Crit3] [$Crit4] [br]&nbsp;&nbsp; Crit DMG [$Critd1] [$Critd2] [$Critd3] [$Critd4]";
		tempString += " --=Crush|" + getCrush() + " --+|Crush:  [$Crush] }}";

		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private String buildPercentRollBlock(String labelPrefix, String rawVar, String bonusVar, String countVar,
			String rolledPercentVar, String finalRawVar, String percentVar) {
		String block = "";
		block += " --=" + rawVar + "|1d21 + 9";
		block += " --=" + bonusVar + "|0";
		block += " --=" + countVar + "|1";
		block += " --=" + rolledPercentVar + "|[$" + rawVar + "] * 5 / 100";
		block += buildAttackPercentRollDisplayBlock(labelPrefix, "Initial", rawVar, countVar, rolledPercentVar, false);
		block += " --%" + labelPrefix + "Explode|while;[$" + rawVar + ".Total] -eq 30 -or [$" + rawVar + ".Total] -eq 10";
		block += buildPercentRollAdjustmentBlock(labelPrefix, rawVar, bonusVar);
		block += " --=" + rawVar + "|1d21 + 9";
		block += " --=" + countVar + "|[$" + countVar + "] + 1";
		block += " --=" + rolledPercentVar + "|[$" + rawVar + "] * 5 / 100";
		block += buildAttackPercentRollDisplayBlock(labelPrefix, "Loop", rawVar, countVar, rolledPercentVar, true);
		block += " --%|";
		block += " --=" + finalRawVar + "|[$" + rawVar + "] + [$" + bonusVar + "]";
		block += " --=" + percentVar + "|[$" + finalRawVar + "] * 5 / 100";
		return block;
	}

	private String buildAttackPercentRollDisplayBlock(String labelPrefix, String labelSuffix, String rawVar, String countVar, String rolledPercentVar, boolean showNormalRow) {
		String baseLabel = labelPrefix + "AttackPercentDisplay" + labelSuffix;
		String block = "";
		block += " --?[$" + rawVar + ".Total] -eq 30|>" + baseLabel + "Green|>" + baseLabel + "CheckRed";
		block += " --^" + baseLabel + "After|";
		block += " --:" + baseLabel + "CheckRed|";
		block += " --?[$" + rawVar + ".Total] -eq 10|>" + baseLabel + "Red|>" + baseLabel + (showNormalRow ? "Normal" : "After");
		block += " --^" + baseLabel + "After|";
		block += " --:" + baseLabel + "Green|";
		block += " --+|[#0a7a0a]" + labelPrefix + " Percent Roll [$" + countVar + "]: [$" + rawVar + "] x 5% = [br]&nbsp;&nbsp; +0.5[/#]";
		block += " --<|";
		block += " --:" + baseLabel + "Red|";
		block += " --+|[#aa2222]" + labelPrefix + " Percent Roll [$" + countVar + "]: [$" + rawVar + "] x 5% = [br]&nbsp;&nbsp; -0.5[/#]";
		block += " --<|";
		if (showNormalRow) {
			block += " --:" + baseLabel + "Normal|";
			block += " --+|" + labelPrefix + " Percent Roll [$" + countVar + "]: [$" + rawVar + "] x 5% = [br]&nbsp;&nbsp; [$" + rolledPercentVar + "]";
			block += " --<|";
		}
		block += " --:" + baseLabel + "After|";
		return block;
	}

	private String buildPrimaryRollMacroBlock() {
		if (isStandardCastAction()) {
			return " --=AttackRoll|" + getApplication()
					+ " --+|Application: [$AttackRoll]";
		}
		String block = "";
		block += buildPercentRollBlock("Attack", "RawPercentRoll", "RawPercentBonus", "RawPercentCount", "AttackPercentRoll", "FinalRawPercentRoll", "PercentRoll");
		block += " --=AttackRoll|[$PercentRoll] * " + getPrimaryRollValue() + " {FLOOR}";
		block += " --+|Atk Percent Roll: [$FinalRawPercentRoll] x 5% = [$PercentRoll] [br]&nbsp;&nbsp; ";
		block += " " + getPrimaryRollLabel() + " Roll: [$AttackRoll] = [$PercentRoll] x " + getPrimaryRollValue();
		return block;
	}

	private String buildPercentRollDisplayBlock(String labelPrefix, String labelSuffix, String rawVar, String countVar, String rolledPercentVar) {
		String baseLabel = labelPrefix + "PercentDisplay" + labelSuffix;
		String block = "";
		block += " --?[$" + rawVar + ".Total] -eq 30|>" + baseLabel + "Green|>" + baseLabel + "CheckRed";
		block += " --^" + baseLabel + "After|";
		block += " --:" + baseLabel + "CheckRed|";
		block += " --?[$" + rawVar + ".Total] -eq 10|>" + baseLabel + "Red|>" + baseLabel + "Normal";
		block += " --^" + baseLabel + "After|";
		block += " --:" + baseLabel + "Green|";
		block += " --+|[#0a7a0a]" + labelPrefix + " Percent Roll [$" + countVar + "]: [$" + rawVar + "] x 5% = [$" + rolledPercentVar + "][/#]";
		block += " --<|";
		block += " --:" + baseLabel + "Red|";
		block += " --+|[#aa2222]" + labelPrefix + " Percent Roll [$" + countVar + "]: [$" + rawVar + "] x 5% = [$" + rolledPercentVar + "][/#]";
		block += " --<|";
		block += " --:" + baseLabel + "Normal|";
		block += " --+|" + labelPrefix + " Percent Roll [$" + countVar + "]: [$" + rawVar + "] x 5% = [$" + rolledPercentVar + "]";
		block += " --<|";
		block += " --:" + baseLabel + "After|";
		return block;
	}

	private String buildPercentRollAdjustmentBlock(String labelPrefix, String rawVar, String bonusVar) {
		String baseLabel = labelPrefix + "PercentAdjust";
		String block = "";
		block += " --?[$" + rawVar + ".Total] -eq 30|>" + baseLabel + "Up|>" + baseLabel + "Down";
		block += " --^" + baseLabel + "After|";
		block += " --:" + baseLabel + "Up|";
		block += " --=" + bonusVar + "|[$" + bonusVar + "] + 10";
		block += " --<|";
		block += " --:" + baseLabel + "Down|";
		block += " --=" + bonusVar + "|[$" + bonusVar + "] - 10";
		block += " --<|";
		block += " --:" + baseLabel + "After|";
		return block;
	}

	private CharAttributes attrs() {
		return character != null ? character.getAttributes() : null;
	}

	private CharIdentity id() {
		return character != null ? character.getIdentity() : null;
	}

	private int getRange() {
		if (action != null) {
			if (action.getRanged() > 0) {
				return getDerivedCombatValue("RANGE");
			}
			return action.getRanged();
		}
		return 0;
	}

	private int getAttack() {
		if (action != null) {
			return Math.max(0, action.getAtk());
		}
		return getDerivedCombatValue("ATK");
	}

	private int getApplication() {
		if (action != null) {
			return Math.max(0, action.getAtk());
		}
		return getDerivedCombatValue("APP");
	}

	private String getAttackVs() {
		DataItemWeapon weapon = combatFrame == null ? null : combatFrame.getSelectedWeaponForAttackFrame();
		if (weapon != null) {
			String weaponAttack = weapon.getAttack();
			if (weaponAttack != null && !weaponAttack.isBlank()) {
				String normalizedAttack = weaponAttack.trim().toUpperCase();
				if ("SPELL".equalsIgnoreCase(normalizedAttack)) {
					String selectedDamageType = getSelectedDamageType();
					String mappedAttackVs = combatFrame == null ? "" : combatFrame.getAttackVsForDamageType(selectedDamageType);
					if (!mappedAttackVs.isBlank()) {
						return mappedAttackVs;
					}
				}
				return normalizedAttack;
			}
		}
		String attackType = action == null ? null : action.getAtkType();
		return attackType == null ? "" : attackType.trim().toUpperCase();
	}

	private void updateAttackVsField() {
		if (textFields[7] == null) return;
		textFields[7].setText(getAttackVs());
	}

	private String getSelectedDamageType() {
		if (damageType == null) return "";
		Object selectedItem = damageType.getSelectedItem();
		return selectedItem == null ? "" : selectedItem.toString().trim().toUpperCase();
	}

	private void attachDamageTypeListener() {
		if (damageType == null) return;
		for (ActionListener listener : damageType.getActionListeners()) {
			damageType.removeActionListener(listener);
		}
		damageType.addActionListener(e -> updateAttackVsField());
	}

	private void applyDamageTypeSelection() {
		if (damageType == null) return;
		String lockedDamageType = getLockedWeaponDamageType();
		if (lockedDamageType != null) {
			resetDamageTypeOptions(DMGTYPE);
			selectDamageType(lockedDamageType);
			damageType.setEnabled(false);
			return;
		}
		if (isStandardBaselineAction()) {
			List<String> standardDamageTypes = getStandardDamageTypeOptions();
			if (!standardDamageTypes.isEmpty()) {
				resetDamageTypeOptions(standardDamageTypes.toArray(new String[0]));
				damageType.setSelectedIndex(0);
				damageType.setEnabled(standardDamageTypes.size() > 1);
				return;
			}
		}
		resetDamageTypeOptions(DMGTYPE);
		damageType.setEnabled(true);
		String mappedAuraDamageType = getAuraTechniqueDamageType();
		if (!mappedAuraDamageType.isBlank()) {
			selectDamageType(mappedAuraDamageType);
			damageType.setEnabled(false);
			return;
		}
		String affinity = action == null ? null : action.getAffinity();
		if (affinity != null) {
			selectDamageType(affinity);
		}
	}

	private void resetDamageTypeOptions(String[] values) {
		if (damageType == null) return;
		damageType.removeAllItems();
		if (values == null) return;
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				damageType.addItem(value);
			}
		}
	}

	private void selectDamageType(String value) {
		if (damageType == null || value == null) return;
		for (int i = 0; i < DMGTYPE.length; i++) {
			if (value.equalsIgnoreCase(DMGTYPE[i])) {
				damageType.setSelectedIndex(i);
				return;
			}
		}
	}

	private String getLockedWeaponDamageType() {
		if (!isStandardBaselineAction() || combatFrame == null) return null;
		DataItemWeapon weapon = combatFrame.getSelectedWeaponForAttackFrame();
		if (weapon == null) return null;
		String damage = weapon.getDamage();
		return (damage == null || damage.isBlank()) ? null : damage.trim();
	}

	private boolean isStandardBaselineAction() {
		if (action == null || action.getName() == null) return false;
		return "Standard Attack".equalsIgnoreCase(action.getName())
				|| "Standard Cast".equalsIgnoreCase(action.getName());
	}

	private List<String> getStandardDamageTypeOptions() {
		if (combatFrame == null) return List.of();
		return combatFrame.getStandardDamageTypeOptions();
	}

	private String getAuraTechniqueDamageType() {
		if (!isAuraTechniqueAction() || combatFrame == null || action == null) return "";
		String affinity = action.getAffinity();
		String mappedDamageType = combatFrame.getDamageTypeForAuraAffinity(affinity);
		return mappedDamageType == null ? "" : mappedDamageType;
	}

	private int getExtraAttacks() {
		int maxAtk = getDerivedCombatValue("MAXATK");
		return Math.max(0, maxAtk - 1);
	}

	private int getBaseDamage() {
		if (action != null) {
			return action.getBdmg();
		}
		return getDerivedCombatValue("BDMG");
	}

	private int getTotalDamage() {
		if (action != null) {
			return action.getTdmg();
		}
		return getDerivedCombatValue("TDMG");
	}

	private int getCharLevelDieCount() {
		return Integer.parseInt(getDamage().split("d")[0]);
	}

	private int getCharLevelDieSides() {
		return Integer.parseInt(getDamage().split("d")[1]);
	}

	private double getCharDmgMulti() {
		double actionMulti = action == null ? 0.0 : action.getDmgMulti();
		return 1.0 + actionMulti;
	}

	private int getPrimaryAttributeMod() {
		DataClass cls = null;
		CharIdentity identity = id();
		if (identity != null && identity.getCharClass() != null) {
			StoreRuleManager dq = new StoreRuleManager();
			cls = dq.getClassByName(identity.getCharClass());
		}
		String prim = cls != null ? cls.getPrimaryAtt() : null;
		if (prim == null) return 0;
		CharAttributes a = attrs();
		if (a == null) return 0;
		int val = 1;
		return val - 10;
	}

	private int damage() {
		DataLevel lvl = getDataLevel();
		return lvl != null ? Integer.parseInt(lvl.getDamage().split("d")[1]) : 6;
	}

	private String getDamage() {
		DataLevel lvl = getDataLevel();
		return lvl != null ? lvl.getDamage() : "1d6";
	}

	private int getLevel() {
		CharIdentity identity = id();
		return identity == null ? 1 : identity.getLevel();
	}

	private double getCritDamage() {
		if (character != null && character.getCombat() != null) {
			double val = character.getCombat().getCritDamage();
			return val > 0 ? val : 1.0;
		}
		return 1.0;
	}

	private double getCrush() {
		if (character != null && character.getAttributes() != null) {
			return 1;
		}
		return 0.0;
	}

	private DataLevel getDataLevel() {
		StoreRuleManager dq = new StoreRuleManager();
		return dq.getLevel(getLevel());
	}

	private String getCharName() {
		CharIdentity identity = id();
		return identity != null ? identity.getName() : "Character";
	}

	private boolean isFullAttackAction() {
		return action != null && "Full Attack".equalsIgnoreCase(action.getName());
	}

	private boolean isStandardCastAction() {
		return action != null && "Standard Cast".equalsIgnoreCase(action.getName());
	}

	private boolean isAuraTechniqueAction() {
		return action != null
				&& action.getSource() != null
				&& "Aura".equalsIgnoreCase(action.getSource());
	}

	private void showInitialStage() {
		if (usesAttackLayout()) {
			attackStage();
		} else {
			castStage();
		}
	}

	private boolean usesAttackLayout() {
		if (action == null) return true;
		if ("Standard Attack".equalsIgnoreCase(action.getName())) return true;
		String attackType = action.getAtkType();
		return attackType != null && "AC".equalsIgnoreCase(attackType);
	}

	private int getActionAl() {
		return action == null ? 0 : Math.max(0, action.getAl());
	}

	private void ensureAlSelect() {
		if (alSelect == null) {
			alSelect = new JComboBox<Integer>();
		}
		if (alSelect.getParent() == null) {
			activeCardPanel.add(alSelect);
		}
	}

	private void populateAlSelect() {
		if (alSelect == null || action == null) return;
		alSelect.removeAllItems();
		int maxRank = getTechniqueRank();
		if (maxRank <= 0) {
			maxRank = 1;
		}
		for (int i = 1; i <= maxRank; i++) {
			alSelect.addItem(i);
		}
		int selectedAl = Math.max(1, Math.min(getActionAl(), maxRank));
		action.setAl(selectedAl);
		alSelect.setSelectedItem(selectedAl);
		for (ActionListener listener : alSelect.getActionListeners()) {
			alSelect.removeActionListener(listener);
		}
		alSelect.addActionListener(e -> alSelectionChanged());
	}

	private void alSelectionChanged() {
		if (alSelect == null || action == null) return;
		Object selected = alSelect.getSelectedItem();
		if (!(selected instanceof Integer value)) return;
		action.setAl(value);
		action.update();
		if (buttons[3].isVisible()) {
			attackStage();
			return;
		}
		damageStage();
	}

	private void syncActionFromCurrentAl() {
		if (action == null) return;
		if (isAuraTechniqueAction()) {
			int maxRank = getTechniqueRank();
			int selectedAl = Math.max(1, getActionAl());
			if (maxRank > 0) {
				selectedAl = Math.min(selectedAl, maxRank);
			}
			action.setAl(selectedAl);
		}
		action.update();
	}

	private int getTechniqueRank() {
		if (character == null || character.getTraining() == null || action == null) return 0;
		DataTraining training = character.getTraining().getTrainingById(action.getId());
		return training == null ? 0 : Math.max(0, training.getRank());
	}

	private int getDerivedCombatValue(String key) {
		CharAttributes a = attrs();
		if (a == null || key == null) return 0;
		return (int)Math.round(Math.max(0.0, a.calcStatusValue(key)));
	}

	private String formatAttackDisplay(int value) {
		return Math.max(0, value) + "";
	}

	private int getPrimaryRollValue() {
		return usesAttackLayout() ? getAttack() : getApplication();
	}

	private String getPrimaryRollLabel() {
		return usesAttackLayout() ? "Attack" : "Application";
	}

	private String formatDamageDisplay(int baseDamage, double damageMultiplier, int totalDamage) {
		return "(" + baseDamage + " +/- 50%) * " + damageMultiplier + " + " + totalDamage;
	}

	private DataColor getDisplayColor() {
		StoreRuleManager dq = new StoreRuleManager();
		String race = Optional.ofNullable(id()).map(CharIdentity::getRace).orElse("Default");
		DataColor color = dq.getColorByTitle(race);
		if (color != null) return color;
		// Fallback neutral colors
		return new DataColor("Default", 0, 0, 0, 255, 255, 255);
	}
}

