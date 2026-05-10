package eternity;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class FrameMitigation extends JFrame {
	private static final long serialVersionUID = 1;

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private CharData character;
	private DataAction action;
	private JComboBox<String> select1;
	private JComboBox<Integer> alSelect;
	private JComboBox<String> damageType;
	
	private double attackBonus, result;
	
	private final String[] YESNO = {"Yes", "No"};
	private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME"};

	// UI elements (replacing FrameHelper utilities)
	private final JLabel headerL = new JLabel();
	private final JLabel[] labels = new JLabel[8];
	private final JTextField[] textFields = new JTextField[6];
	private final JSpinner[] numFields = new JSpinner[1];
	private final JButton[] buttons = new JButton[4];
	
	FrameMitigation(FrameSheet sheetFrame, FrameCombat combatFrame, CharData character, DataAction action) {
		super("Mitigation Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(null); // preserve existing absolute positioning
		setSize(550, 400);
		setLocationRelativeTo(null);
		setResizable(false);

		initComponents();
		attackStage();
	}

	/** Builds base UI components used by the legacy layout. */
	private void initComponents() {
		headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 14f));
		headerL.setBounds(25, 20, 500, 30);
		headerL.setHorizontalAlignment(JLabel.CENTER);
		headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 18f));
		headerL.setVisible(false);
		add(headerL);

		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel();
			labels[i].setVisible(false);
			add(labels[i]);
		}

		for (int i = 0; i < textFields.length; i++) {
			textFields[i] = new JTextField();
			textFields[i].setVisible(false);
			add(textFields[i]);
		}

		numFields[0] = new JSpinner(new SpinnerNumberModel(0.0, -9999.0, 9999.0, 1.0));
		numFields[0].setVisible(false);
		add(numFields[0]);

		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton();
			buttons[i].setVisible(false);
			add(buttons[i]);
		}
	}
	
	public void attackStage() {
		clearStdAction();
		syncActionFromCurrentAl();
		/*
		 * Set Headers
		*/
		headerL.setText(action.getName());
		headerL.setVisible(true);
		
		labels[2].setBounds(145, 80, 100, 20);
		labels[2].setText("Attack");
		labels[2].setVisible(true);
		textFields[2].setBounds(145, 103, 100, 22);
		attackBonus = getAttack() + getPrimaryAttributeMod(); 
		textFields[2].setText(formatAttackDisplay((int) attackBonus));
		textFields[2].setVisible(true);
		textFields[2].setEditable(false);

		labels[1].setBounds(25, 80, 100, 20);
		labels[1].setText("Affinity");
		labels[1].setVisible(true);
		textFields[1].setBounds(25, 103, 100, 22);
		textFields[1].setText(action.getAffinity());
		textFields[1].setVisible(true);
		textFields[1].setEditable(false);

		labels[4].setBounds(265, 80, 120, 20);
		labels[4].setText("Damage");
		labels[4].setVisible(true);
		textFields[3].setBounds(265, 103, 240, 22);
		textFields[3].setText(formatDamageDisplay(getBaseDamage(), getCharDmgMulti(), getTotalDamage()));
		textFields[3].setVisible(true);
		textFields[3].setEditable(false);

		labels[5].setBounds(25, 150, 100, 20);
		labels[5].setText("Range");
		labels[5].setVisible(true);
		textFields[4].setBounds(25, 173, 100, 22);
		textFields[4].setText(getRange() <= 0 ? "Melee" : (getRange() + " ft"));
		textFields[4].setVisible(true);
		textFields[4].setEditable(false);

		if (isAuraTechniqueAction()) {
			labels[7].setBounds(285, 150, 120, 20);
			labels[7].setText("AL");
			labels[7].setVisible(true);
			ensureAlSelect();
			populateAlSelect();
			alSelect.setBounds(285, 173, 100, 22);
			alSelect.setVisible(true);
		}

		if (isFullAttackAction()) {
			labels[0].setBounds(285, 150, 120, 20);
			labels[0].setText("Extra Attacks");
			labels[0].setVisible(true);
			textFields[0].setBounds(285, 173, 100, 22);
			textFields[0].setText(String.valueOf(getExtraAttacks()));
			textFields[0].setVisible(true);
			textFields[0].setEditable(false);
		}

		labels[6].setBounds(145, 150, 120, 20);
		labels[6].setText("Damage Type");
		labels[6].setVisible(true);
		if (damageType == null) {
			damageType = new JComboBox<String>(DMGTYPE);
		}
		if (damageType.getParent() == null) {
			add(damageType);
		}
		damageType.setBounds(145, 173, 130, 22);
		damageType.setVisible(true);
		// Default to action affinity when it matches a known damage type
		String affinity = action.getAffinity();
		if (affinity != null) {
			for (int i = 0; i < DMGTYPE.length; i++) {
				if (affinity.equalsIgnoreCase(DMGTYPE[i])) {
					damageType.setSelectedIndex(i);
					break;
				}
			}
		}
		
		labels[3].setBounds(220, 280, 100, 20);
		labels[3].setText("Result");
		numFields[0].setBounds(220, 305, 100, 20);
		numFields[0].setValue(0.0);
		
		buttons[2].setBounds(25, 250, 145, 20);
		buttons[2].setText("Roll");
		buttons[2].setVisible(true);
		buttons[2].setEnabled(true);
		buttons[2].addActionListener(e -> copyRollToClipboard());
		
		buttons[0].setBounds(25, 325, 145, 20);
		buttons[0].setText("Cancel");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> cancelPressed());
		buttons[1].setBounds(195, 325, 145, 20);
		buttons[1].setText("Complete");
		buttons[1].setVisible(true);
		buttons[1].addActionListener(e -> completePressed());
		buttons[3].setBounds(365, 325, 145, 20);
		buttons[3].setText("Confirm");
		buttons[3].setVisible(true);
		buttons[3].addActionListener(e -> confirmPressed());
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
		textFields[0].setText(getDamage());
		textFields[0].setVisible(true);
		textFields[0].setEditable(false);
		
		labels[1].setBounds(145, 80, 120, 20);
		labels[1].setText("Base Bonus");
		labels[1].setVisible(true);
		textFields[1].setBounds(145, 103, 120, 22);
		textFields[1].setText("+" + (getBaseDamage() + getPrimaryAttributeMod()));
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
			add(damageType);
		}
		damageType.setBounds(25, 173, 130, 22);
		// Try to default to the action affinity if it matches a known type
		String affinity = action.getAffinity();
		if (affinity != null) {
			for (int i = 0; i < DMGTYPE.length; i++) {
				if (affinity.equalsIgnoreCase(DMGTYPE[i])) {
					damageType.setSelectedIndex(i);
					break;
				}
			}
		}
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
		
		buttons[2].setBounds(25, 250, 145, 20);
		buttons[2].setText("Roll");
		buttons[2].setVisible(true);
		buttons[2].setEnabled(true);
		buttons[2].addActionListener(e -> copyRollToClipboard());
		
		buttons[0].setBounds(25, 325, 145, 20);
		buttons[0].setText("Cancel");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> cancelPressed());
		buttons[1].setBounds(365, 325, 145, 20);
		buttons[1].setText("Complete");
		buttons[1].setVisible(true);
		buttons[1].addActionListener(e -> completePressed());
	}
	
	public void updateSneak() {
		if (select1.getSelectedIndex()==0) {
			textFields[0].setText(getDamage());
		}
		else {
			textFields[0].setText(getDamage() + "+" + getLevel() + "d6");
		}
	}

	public void updateCharacter(CharData character, DataAction action) {
		this.character = character;
		this.action = action;
		attackStage();
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
			if (alSelect.getParent() == this) {
				remove(alSelect);
			}
		}
		if (damageType != null) {
			damageType.setVisible(false);
			if (damageType.getParent() == this) {
				remove(damageType);
			}
		}
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
		result = (double)numFields[0].getValue();
		damageStage();
	}
	
	public void completePressed() {
		if (combatFrame != null && action != null && action.getActionType() != null) {
			combatFrame.stdActionFinish(action.getActionType());
		}
		this.setVisible(false);
		this.dispose();
	}

	private void copyRollToClipboard() {
		if (character == null) return;
		// Safe color lookup with sensible fallback
		DataColor raceColor = null;
		DataQuery dataQuery = new DataQuery();
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
		tempString += action.getName() + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
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
		tempString += " --+|Attack Roll --=PercentRoll|1d21 + 9 * 5 / 100 --=AttackRoll|[$PercentRoll] * " + getAttack() + " {FLOOR} --+| [$AttackRoll] = [$PercentRoll] x " + getAttack();
		tempString += " --+|Damage Roll --=PercentRoll|1d21 + 9 * 5 / 100 --=DamageRoll|[$PercentRoll] * " + bdmgMod + " * " + dmgMulti + " + " + tdmgMod + " {FLOOR} --+| [$DamageRoll] = ([$PercentRoll] x " + bdmgMod + ") x " +  dmgMulti + " + "+ tdmgMod + "[br]&nbsp;&nbsp;&nbsp;&nbsp;" + damageType.getSelectedItem().toString();;
		tempString += " --+|Critical Threat --=Critx1|2 --=Critx2|3 --=Critx3|4 --=Critx4|5 --=Crit1|1d6 --=Crit2|1d6 --=Crit3|1d6 --=Crit4|1d6 --=Critd1|1d6 --=Critd2|1d6 --=Critd3|1d6 --=Critd4|1d6";
		tempString += " --+|Crit Multi: [$Critx1] [$Critx2] [$Critx3] [$Critx4][br]&nbsp;&nbsp; Crit AC #: [$Crit1] [$Crit2] [$Crit3] [$Crit4] [br]&nbsp;&nbsp; Crit DMG [$Critd1] [$Critd2] [$Critd3] [$Critd4]";
		tempString += " --+|Crush --=Crush| " + getCrush() + " --+| [$Crush] }}";

		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private CharAttributes attrs() {
		return character != null ? character.getAttributes() : null;
	}

	private CharIdentity id() {
		return character != null ? character.getIdentity() : null;
	}

	private int getRange() {
		if (action != null) {
			return action.getRanged();
		}
		CharAttributes a = attrs();
		return a == null ? 0 : a.getCombat("RANGE");
	}

	private int getAttack() {
		CharAttributes a = attrs();
		int charAtk = a == null ? 0 : a.getCombat("ATK");
		int actionAtk = action == null ? 0 : action.getAtk();
		return Math.max(0, charAtk + actionAtk);
	}

	private int getExtraAttacks() {
		CharAttributes a = attrs();
		int maxAtk = a == null ? 0 : a.getSecondary("MAXATK");
		return Math.max(0, maxAtk - 1);
	}

	private int getBaseDamage() {
		CharAttributes a = attrs();
		int charBdmg = a == null ? 0 : a.getDamage("BDMG");
		int actionBdmg = action == null ? 0 : action.getBdmg();
		return charBdmg + actionBdmg;
	}

	private int getTotalDamage() {
		CharAttributes a = attrs();
		int charTdmg = a == null ? 0 : a.getDamage("TDMG");
		int actionTdmg = action == null ? 0 : action.getTdmg();
		return charTdmg + actionTdmg;
	}

	private int getCharLevelDieCount() {
		return Integer.parseInt(getDamage().split("d")[0]);
	}

	private int getCharLevelDieSides() {
		return Integer.parseInt(getDamage().split("d")[1]);
	}

	private double getCharDmgMulti() {
		double charMulti = 1.0; // placeholder until a character damage multiplier source is defined
		double actionMulti = action == null ? 0.0 : action.getDmgMulti();
		return charMulti + actionMulti;
	}

	private int getPrimaryAttributeMod() {
		DataClass cls = null;
		CharIdentity identity = id();
		if (identity != null && identity.getCharClass() != null) {
			DataQuery dq = new DataQuery();
			cls = dq.getClassByName(identity.getCharClass());
		}
		String prim = cls != null ? cls.getPrimaryAtt() : null;
		if (prim == null) return 0;
		CharAttributes a = attrs();
		if (a == null) return 0;
		int val = a.getAttribute(prim.toUpperCase());
		return val - 10;
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
			return character.getAttributes().getCombat("CRUSH");
		}
		return 0.0;
	}

	private DataLevel getDataLevel() {
		DataQuery dq = new DataQuery();
		return dq.getLevel(getLevel());
	}

	private String getCharName() {
		CharIdentity identity = id();
		return identity != null ? identity.getName() : "Character";
	}

	private boolean isFullAttackAction() {
		return action != null && "Full Attack".equalsIgnoreCase(action.getName());
	}

	private boolean isAuraTechniqueAction() {
		return action != null
				&& action.getSource() != null
				&& "Aura".equalsIgnoreCase(action.getSource());
	}

	private int getActionAl() {
		return action == null ? 0 : Math.max(0, action.getAl());
	}

	private void ensureAlSelect() {
		if (alSelect == null) {
			alSelect = new JComboBox<Integer>();
		}
		if (alSelect.getParent() == null) {
			add(alSelect);
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

	private String formatAttackDisplay(int value) {
		return Math.max(0, value) + " +/- 50%";
	}

	private String formatDamageDisplay(int baseDamage, double damageMultiplier, int totalDamage) {
		return "(" + baseDamage + " +/- 50%) * " + damageMultiplier + " + " + totalDamage;
	}

	private DataColor getDisplayColor() {
		DataQuery dq = new DataQuery();
		String race = Optional.ofNullable(id()).map(CharIdentity::getRace).orElse("Default");
		DataColor color = dq.getColorByTitle(race);
		if (color != null) return color;
		// Fallback neutral colors
		return new DataColor("Default", 0, 0, 0, 255, 255, 255);
	}
}
