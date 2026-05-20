package eternity;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.Font;
import javax.swing.text.NumberFormatter;
import javax.swing.text.DefaultFormatterFactory;

/*
 * 		BASE FOR ALL CHARACTER PANELS
 */
public class PanelCharBase extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Color hpLColor = new Color(180, 0, 0);
	private static final Color auraLColor = new Color(0, 70, 180);
	private static final Set<String> ATTRIBUTE_SHORT_KEYS = Set.of("STR", "DEX", "CON", "FOC", "CTL", "CAP", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB");
	private static final int REMINDER_MAX_ROWS = 2;
	private static final DataColor DEFAULT_DISPLAY_COLOR = new DataColor("Default", 0, 0, 0, 255, 255, 255);
	private static final ThreadLocal<DecimalFormat> UI_DECIMAL_FORMAT = ThreadLocal.withInitial(() -> {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
		DecimalFormat format = new DecimalFormat("0.00", symbols);
		format.setGroupingUsed(false);
		return format;
	});



	StoreRuleManager dataQuery;
	StoreCharData character;
	FrameSheet sheetFrame;
	int pageHeight;
	boolean alternate;
	
	//HP, Aura
	private JLabel hpL, auraL, charCurrHPL, charMaxHPL, charAvailAuraL, charOccAuraL, charSpentAuraL, charMaxAuraL;
	private JProgressBar charCurrHP;
	private JProgressBar charAvailAura;
	private JProgressBar charOccAura;
	private JFormattedTextField charMaxHP, charSpentAura, charMaxAura;
	
	//Reminder Bars
	JPanel racePanel;
	JLabel raceRemind;
	private JLabel tabTitleL;
	private static final int REMINDER_PANEL_MIN_HEIGHT = 40;
	private static final int REMINDER_ROW_HEIGHT = 20;
	private final JLabel[] reminderLabels = new JLabel[REMINDER_MAX_ROWS];
	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] reminderCombos = new JComboBox[REMINDER_MAX_ROWS];
	
	final String[] ATTRIBUTES = {"Strength", "Dexterity", "Constitution", "Focus", "Control", "Capacity", "Knowledge", "Mechanical", "Perception", "Intuition", "Charisma", "Subtlety"};
	final String[] ATTSHORT = {"STR", "DEX", "CON", "FOC", "CTL", "CAP", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB"};
	final String[] AFFINITIES = {"None", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Darkness", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	final String[] TRAININGTITLE = {"Attribute Training", "Misc Training", "Affinity Training", "Fundamental Principles Training", "Standard Technique Training", "Crafting Training", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Darkness", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	final String[] TRAINING = {"Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Darkness", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	
	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharBase (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		this.dataQuery = dataQuery;
		this.sheetFrame = sheetFrame;
		alternate = true;
		setLayout(null);
		
		/*
		 * 	HP, Aura
		 */	
			// Labels
		hpL = buildLabel("HP", hpLColor);
		auraL = buildLabel("Aura", hpLColor);

		charCurrHPL = buildLabel("Current HP", null);
		charMaxHPL = buildLabel("Max HP", null);
		charAvailAuraL = buildLabel("Available Aura", null);
		charOccAuraL = buildLabel("Occupied Aura", null);
		charSpentAuraL = buildLabel("Spent Aura", null);
		charMaxAuraL = buildLabel("Max Aura", null);
			
			// Fields
		charCurrHP = new JProgressBar();
			charCurrHP.setMinimum(0);
			charCurrHP.setMaximum(1);
			charCurrHP.setValue(0);
			charCurrHP.setStringPainted(true);
			charCurrHP.setString("0/0");
			charCurrHP.setForeground(new Color(185, 0, 0));
			charCurrHP.setBackground(new Color(255, 225, 225));
			charCurrHP.setToolTipText("");
			add(charCurrHP);
		charMaxHP = buildNumTextField(0);
			charMaxHP.setToolTipText(""); 
		charMaxAura = buildNumTextField(0);
			charMaxAura.setToolTipText(""); 
		charOccAura = new JProgressBar();
			charOccAura.setMinimum(0);
			charOccAura.setMaximum(1);
			charOccAura.setValue(0);
			charOccAura.setStringPainted(true);
			charOccAura.setString("0");
			charOccAura.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			charOccAura.setForeground(new Color(140, 50, 190));
			charOccAura.setBackground(new Color(239, 226, 255));
			charOccAura.setToolTipText(""); 
			add(charOccAura);
		charSpentAura = buildNumTextField(0);
			charSpentAura.setToolTipText(""); 
		charAvailAura = new JProgressBar();
			charAvailAura.setMinimum(0);
			charAvailAura.setMaximum(1);
			charAvailAura.setValue(0);
			charAvailAura.setStringPainted(true);
			charAvailAura.setString("0");
			charAvailAura.setForeground(new Color(0, 90, 190));
			charAvailAura.setBackground(new Color(225, 238, 255));
			charAvailAura.setToolTipText(""); 
			add(charAvailAura);
		
		/*
		 * Reminders
		 */
		racePanel = new JPanel();
		racePanel.setLayout(null);
		raceRemind = buildLabel("-", null);
		raceRemind.setBounds(0,0,555,REMINDER_PANEL_MIN_HEIGHT);
		racePanel.add(raceRemind);
		raceRemind.setVisible(true);
		initializeReminderRows();
		add(racePanel);
		racePanel.setVisible(true);
		tabTitleL = buildLabel("", null);
		tabTitleL.setFont(new Font("Arial", Font.BOLD, 16));
		tabTitleL.setForeground(Color.BLACK);
		tabTitleL.setVisible(true);
		
		resizeHeader();
	}  /*--------------
		END DEFAULTCONSTRUCTOR
		--------------*/

	public int resizeHeader() {
		/*
		 * Place HP & Aura
		 */
		pageHeight = 0;
		// Top Labels
		hpL.setBounds(5, pageHeight, 175, 20);
		auraL.setBounds(205, pageHeight, 340, 20);
		pageHeight += 15;

			// Labels
		charCurrHPL.setBounds(5, pageHeight, 85, 20);
		charMaxHPL.setBounds(95, pageHeight, 85, 20);
		charAvailAuraL.setBounds(205, pageHeight, 85, 20);
		charOccAuraL.setBounds(295, pageHeight, 85, 20);
		charSpentAuraL.setBounds(385, pageHeight, 85, 20);
		charMaxAuraL.setBounds(475, pageHeight, 85, 20);
		pageHeight += 20;

			// Fields
		charCurrHP.setBounds(5, pageHeight, 85, 20);
		charMaxHP.setBounds(95, pageHeight, 85, 20);
		charAvailAura.setBounds(205, pageHeight, 85, 20);
		charOccAura.setBounds(295, pageHeight, 85, 20);
		charSpentAura.setBounds(385, pageHeight, 85, 20);
		charMaxAura.setBounds(475, pageHeight, 85, 20);
		pageHeight += 20;	
		
		/*
		 * Place Reminder
		 */
		pageHeight += 5;
		racePanel.setBounds(5,pageHeight,555,REMINDER_PANEL_MIN_HEIGHT);
		pageHeight += 45;
		tabTitleL.setBounds(5, pageHeight, 555, 20);
		pageHeight += 25;

		return pageHeight;
	}

	private void initializeReminderRows() {
		for (int i = 0; i < REMINDER_MAX_ROWS; i++) {
			JLabel lineLabel = new JLabel();
			lineLabel.setVisible(false);
			lineLabel.setForeground(Color.WHITE);
			racePanel.add(lineLabel);
			reminderLabels[i] = lineLabel;

			JComboBox<String> drop = new JComboBox<>();
			drop.setVisible(false);
			racePanel.add(drop);
			racePanel.setComponentZOrder(drop, 0);
			reminderCombos[i] = drop;
		}
	}

	public void setTabTitle(String title) {
		if (title == null) {
			tabTitleL.setText("");
		} else {
			tabTitleL.setText(title);
		}
	}
	
	/*
	 * 		CHECK PRESSED
	 */
	public void checkPressed(String checkName, boolean skill, String att, String att2) {
		if (character == null || character.getAttributes() == null) return;

		double mod = safeAttribute(character.getAttributes(), att);

		if (skill) {
			mod *= 1.5;
			//mod += (character.getAttributes().getAttribute("INT") * 0.5);
		}
		
		if (att2 != null && att2.compareTo("") != 0) {
			mod += safeAttribute(character.getAttributes(), att2);
		}
		
		// Safe color lookup with sensible fallback
		DataColor raceColor = null;
		if (dataQuery != null && character.getIdentity() != null) {
			raceColor = dataQuery.getColorByTitle(character.getIdentity().getRace());
		}
		if (raceColor == null) {
			raceColor = DEFAULT_DISPLAY_COLOR;
		}
		String colorString1 = toHexColor(raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = toHexColor(raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String tempString = "!scriptcard {{ --#titleCardBackground|" + colorString1 + " --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|" + colorString1;
		tempString += " --#titleCardBottomBorder|4px solid #000000; --#title|";
		String charName = (character.getIdentity() != null && character.getIdentity().getName() != null)
				? character.getIdentity().getName()
				: "Character";
		tempString += charName + " --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|" + colorString2 + " --#leftSub|";
		tempString += checkName + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em  --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		tempString += buildPercentRollBlock("Check", "RawPercentRoll", "RawPercentBonus", "RawPercentCount", "CheckPercentRoll", "FinalRawPercentRoll", "PercentRoll");
		tempString += " --=SkillCheck|[$PercentRoll] * " + fmt(mod) + " {FLOOR}";
		if (att.compareTo("INIT") != 0) {
			tempString += " --+|Percent Roll: [$FinalRawPercentRoll] x 5% = [$PercentRoll] [br]&nbsp;&nbsp; ";
			tempString += checkName + ": [$SkillCheck] = [$PercentRoll] x " + fmt(mod) + "}}";
		}
		else {
			tempString += " --=InitTotal|[$SkillCheck]";
			tempString += " --+|Percent Roll: [$FinalRawPercentRoll] x 5% = [$PercentRoll]";
			tempString += " --+|Initiative Check: [$SkillCheck] = [$PercentRoll] x " + fmt(mod);
			tempString += " --+|Total Initiative: [$InitTotal]";
			tempString += " --~|turnorder;replacetoken;@{selected|token_id};[$InitTotal]}}";
		}
		
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
		block += buildPercentRollDisplayBlock(labelPrefix, "Initial", rawVar, countVar, rolledPercentVar, false);
		block += " --%" + labelPrefix + "Explode|while;[$" + rawVar + ".Total] -eq 30 -or [$" + rawVar + ".Total] -eq 10";
		block += buildPercentRollAdjustmentBlock(labelPrefix, rawVar, bonusVar);
		block += " --=" + rawVar + "|1d21 + 9";
		block += " --=" + countVar + "|[$" + countVar + "] + 1";
		block += " --=" + rolledPercentVar + "|[$" + rawVar + "] * 5 / 100";
		block += buildPercentRollDisplayBlock(labelPrefix, "Loop", rawVar, countVar, rolledPercentVar, true);
		block += " --%|";
		block += " --=" + finalRawVar + "|[$" + rawVar + "] + [$" + bonusVar + "]";
		block += " --=" + percentVar + "|[$" + finalRawVar + "] * 5 / 100";
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

	private String buildPercentRollDisplayBlock(String labelPrefix, String labelSuffix, String rawVar, String countVar, String rolledPercentVar, boolean showNormalRow) {
		String baseLabel = labelPrefix + "PercentDisplay" + labelSuffix;
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

	/** Returns derived stat value or 0 if key is null/unknown. */
	protected double safeAttribute(CharAttributes attrs, String key) {
		return getDerivedStatusValue(attrs, key);
	}

	protected double getDerivedStatusValue(CharAttributes attrs, String key) {
		if (attrs == null || key == null) return 0;
		String upper = key.toUpperCase();
		ArrayList<DataStatus>[] baseBlock = resolveStatusBlock(attrs, upper, true);
		ArrayList<DataStatus>[] multiBlock = resolveStatusBlock(attrs, upper, false);
		return computeStatusValue(baseBlock, multiBlock);
	}

	protected ArrayList<DataStatus>[] getBaseStatusBlock(CharAttributes attrs, String key) {
		return resolveStatusBlock(attrs, key, true);
	}

	protected ArrayList<DataStatus>[] getMultiplierStatusBlock(CharAttributes attrs, String key) {
		return resolveStatusBlock(attrs, key, false);
	}

	protected double sumStatusSeverity(List<DataStatus> statuses) {
		if (statuses == null) return 0.0;
		double total = 0.0;
		for (DataStatus status : statuses) {
			if (status != null) total += status.getSeverity();
		}
		return total;
	}

	private double computeStatusValue(ArrayList<DataStatus>[] baseBlock, ArrayList<DataStatus>[] multiBlock) {
		if (baseBlock == null) return 0.0;
		double baseValue = 0.0;
		double multiplier = 1.0;
		for (int i = 0; i < baseBlock.length; i++) {
			baseValue += sumStatusSeverity(baseBlock[i]);
		}
		if (multiBlock != null) {
			for (int i = 0; i < multiBlock.length; i++) {
				multiplier += sumStatusSeverity(multiBlock[i]);
			}
		}
		return Math.max(0.0, baseValue * multiplier);
	}

	private ArrayList<DataStatus>[] resolveStatusBlock(CharAttributes attrs, String key, boolean base) {
		if (attrs == null || key == null) return null;
		String upper = key.toUpperCase();
		String prefix = base ? "B" : "M";
		if (ATTRIBUTE_SHORT_KEYS.contains(upper)) {
			return findStatusBlock(base ? attrs.getBAttributes() : attrs.getMAttributes(), prefix + upper);
		}
		if (matchesStatusKey(upper, CharAttributes.getDefenseKeys())) {
			return findStatusBlock(base ? attrs.getBDefense() : attrs.getMDefense(), prefix + upper);
		}
		if (matchesStatusKey(upper, CharAttributes.getDamageTypeKeys())) {
			return findStatusBlock(base ? attrs.getBResist() : attrs.getMResist(), prefix + upper);
		}
		if (matchesStatusKey(upper, CharAttributes.getCombatKeys())) {
			return findStatusBlock(base ? attrs.getBCombat() : attrs.getMCombat(), prefix + upper);
		}
		if (matchesStatusKey(upper, CharAttributes.getSecondaryKeys())) {
			return findStatusBlock(base ? attrs.getBSecondary() : attrs.getMSecondary(), prefix + upper);
		}
		if (matchesStatusKey(upper, CharAttributes.getDamageKeys())) {
			return findStatusBlock(base ? attrs.getBDamage() : attrs.getMDamage(), prefix + upper);
		}
		return null;
	}

	private boolean matchesStatusKey(String key, String[] keys) {
		for (String candidate : keys) {
			if (candidate.equalsIgnoreCase(key)) return true;
		}
		return false;
	}

	private ArrayList<DataStatus>[] findStatusBlock(ArrayList<DataStatus>[][] category, String attributeKey) {
		if (category == null || attributeKey == null) return null;
		for (ArrayList<DataStatus>[] block : category) {
			if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
			DataStatus first = block[0].get(0);
			if (first != null && attributeKey.equalsIgnoreCase(first.getAttribute())) {
				return block;
			}
		}
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	* 
	* 		UPDATER
	* 
	*/
	/*
	 * 		UPDATE CHARACTER
	 */
	public void updateCharacter(StoreCharData character) {
		this.character = character;
		refreshBaseState();
		updateAll();
	}  /*--------------
		END UPDATE CHARACTER
		--------------*/

	protected void refreshBaseState() {
		refreshHPAuraOnly();
		refreshReminderOnly();
	}

	protected void refreshHPAuraOnly() {
		updateHPAura();
	}

	protected void refreshReminderOnly() {
		updateReminder();
	}
	
	/*
	 * 		UPDATE HP & AURA
	 */
	public void updateHPAura() {
		if (character == null || character.getResources() == null) return;
		CharResources res = character.getResources();
		int maxHp = Math.max(0, res.calcMaxHP());
		int currentHp = Math.max(0, res.calcCurrentHP());
		double lostHp = Math.max(0.0, res.getLostHP());
		int maxAura = Math.max(0, res.calcMaxAura());
		int availableAura = Math.max(0, res.calcCurrentAura());
		double occupiedAura = Math.max(0.0, res.calcOccupiedAura());
		double spentAura = Math.max(0.0, res.getSpentAura());

		charCurrHP.setMaximum(Math.max(1, maxHp));
		charCurrHP.setValue(Math.min(currentHp, Math.max(1, maxHp)));
		charCurrHP.setString(currentHp + "/" + maxHp);
		charMaxHP.setValue(maxHp);

		charAvailAura.setMaximum(Math.max(1, maxAura));
		charAvailAura.setValue(Math.min(availableAura, Math.max(1, maxAura)));
		charAvailAura.setString(availableAura + "/" + maxAura);
		charOccAura.setMaximum(Math.max(1, maxAura));
		charOccAura.setValue((int)Math.min(Math.round(occupiedAura), Math.max(1, maxAura)));
		charOccAura.setString(fmt(occupiedAura));
		charSpentAura.setValue(round2(spentAura));
		charMaxAura.setValue(maxAura);

		String tempString = "<html>You have " + currentHp + " HP.<br>You have lost " + fmt(lostHp) + " HP.<br>You are ";
		double hpRatio = maxHp <= 0 ? 0.0 : currentHp / (double)maxHp;
		if (hpRatio >= 1) {
			tempString += "not wounded.";
		}
		else if (hpRatio >= 0.9) {
			tempString += "barely wounded.";
		}
		else if (hpRatio >= 0.7) {
			tempString += "lightly wounded.";
		}
		else if (hpRatio >= 0.5) {
			tempString += "moderately wounded.";
		}
		else if (hpRatio >= 0.3) {
			tempString += "heavily wounded.";
		}
		else if (hpRatio >= 0.1) {
			tempString += "severely wounded.";
		}
		else {
			tempString += "critically wounded.";
		}
		charCurrHPL.setToolTipText(tempString);
		charCurrHP.setToolTipText(tempString);
		
		tempString = "<html>Maximum HP: " + maxHp + "</html>";
		charMaxHPL.setToolTipText(tempString);
		charMaxHP.setToolTipText(tempString);
		
		tempString = "<html>Maximum Aura: " + maxAura + "</html>";
		charMaxAuraL.setToolTipText(tempString);
		charMaxAura.setToolTipText(tempString);

		tempString = "<html>You have spent " + fmt(spentAura) + " Aura.<br>You are ";
		double auraRatio = maxAura <= 0 ? 0.0 : availableAura / (double)maxAura;
		if (auraRatio >= 1) {
			tempString += "not drained.";
		}
		else if (auraRatio >= 0.9) {
			tempString += "barely drained.";
		}
		else if (auraRatio >= 0.7) {
			tempString += "lightly drained.";
		}
		else if (auraRatio >= 0.5) {
			tempString += "moderately drained.";
		}
		else if (auraRatio >= 0.3) {
			tempString += "heavily drained.";
		}
		else if (auraRatio >= 0.1) {
			tempString += "severely drained.";
		}
		else {
			tempString += "critically drained.";
		}
		charSpentAuraL.setToolTipText(tempString);
		charSpentAura.setToolTipText(tempString);

		String occupiedTip = "<html>Occupied Aura: " + fmt(occupiedAura) + "<br>Main: " + fmt(res.getMainOccupiedAura()) + "<br>Granted: " + fmt(res.getGrantOccupiedAura()) + "</html>";
		charOccAuraL.setToolTipText(occupiedTip);
		charOccAura.setToolTipText(occupiedTip);

		tempString = "<html>Available Aura: " + availableAura + "/" + maxAura + "<br>Spent: " + fmt(spentAura) + "<br>Occupied: " + fmt(occupiedAura) + "<br>You are ";
		if (auraRatio >= 1) {
			tempString += "not drained.";
		}
		else if (auraRatio >= 0.9) {
			tempString += "barely drained.";
		}
		else if (auraRatio >= 0.7) {
			tempString += "lightly drained.";
		}
		else if (auraRatio >= 0.5) {
			tempString += "moderately drained.";
		}
		else if (auraRatio >= 0.3) {
			tempString += "heavily drained.";
		}
		else if (auraRatio >= 0.1) {
			tempString += "severely drained.";
		}
		else {
			tempString += "critically drained.";
		}
		charAvailAura.setToolTipText(tempString);
		charAvailAuraL.setToolTipText(tempString);
	}  /*--------------
		END UPDATEHPAURA
		--------------*/
	
	/*
	 * 		UPDATE REMINDER
	 */
	public void updateReminder() {
		String reminder = (character != null) ? character.getPanelReminder() : null;
		resetReminderRows();
		if (reminder == null || reminder.isBlank()) {
			raceRemind.setText("");
			raceRemind.setVisible(false);
		} else {
			String normalized = reminder.replace("\r\n", "\n").replace("\r", "\n");
			String[] lines = normalized.split("\n");
			int displayRowCount = countReminderRows(lines);
			int reminderTop = getReminderTop(displayRowCount);
			int row = 0;
			for (String rawLine : lines) {
				if (rawLine == null) continue;
				String line = rawLine.trim();
				if (line.isBlank()) continue;
				if (row >= REMINDER_MAX_ROWS) break; // fits existing 50px reminder panel footprint with top margin

				String[] parts = line.split("::", 2);
				if (parts.length == 2) {
					String entryText = parts[0].trim();
					String optionsRaw = parts[1].trim();
					String[] optParts = optionsRaw.contains("|") ? optionsRaw.split("\\|") : optionsRaw.split(",");
					ArrayList<String> options = new ArrayList<>();
					for (String op : optParts) {
						if (op != null) {
							String t = op.trim();
							if (!t.isBlank()) options.add(t);
						}
					}
					boolean isShapeshift = entryText.toLowerCase().contains("shapeshift");
					if (isShapeshift) {
						ArrayList<String> shapeshiftOptions = getShapeshiftOptionsFromLists();
						if (!shapeshiftOptions.isEmpty()) {
							options = shapeshiftOptions;
						}
					}
					if (!options.isEmpty()) {
						JLabel lineLabel = reminderLabels[row];
						lineLabel.setText(entryText);
						lineLabel.setHorizontalAlignment(JTextField.RIGHT);
						lineLabel.setVerticalAlignment(JLabel.CENTER);
						lineLabel.setBounds(5, reminderTop + (row * REMINDER_ROW_HEIGHT), 255, REMINDER_ROW_HEIGHT);
						lineLabel.setVisible(true);

						JComboBox<String> drop = reminderCombos[row];
						drop.removeAllItems();
						for (String option : options) {
							drop.addItem(option);
						}
						drop.setBounds(275, reminderTop + (row * REMINDER_ROW_HEIGHT), 142, REMINDER_ROW_HEIGHT);
						boolean isFelshify = entryText.toLowerCase().contains("felshify");
						if (isFelshify) {
							drop.setSelectedItem("Cat");
						}
						if (character != null) {
							String savedValue = character.getReminderSelection(entryText);
							if (savedValue != null && !savedValue.isBlank()) {
								drop.setSelectedItem(savedValue);
							}
							if (isFelshify) {
								Object selected = drop.getSelectedItem();
								applyFelshifySize(selected == null ? "" : selected.toString());
							}
							drop.addActionListener(e -> {
								Object selected = drop.getSelectedItem();
								character.setReminderSelection(entryText, selected == null ? "" : selected.toString());
								if (isFelshify) {
									boolean sizeChanged = applyFelshifySize(selected == null ? "" : selected.toString());
									if (sizeChanged && sheetFrame != null) {
										sheetFrame.refreshMainPanel();
									}
								}
							});
						}
						drop.setVisible(true);
						row++;
						continue;
					}
				}

				JLabel lineLabel = reminderLabels[row];
				lineLabel.setText(line);
				lineLabel.setHorizontalAlignment(JTextField.CENTER);
				lineLabel.setVerticalAlignment(JLabel.CENTER);
				lineLabel.setBounds(0, reminderTop + (row * REMINDER_ROW_HEIGHT), 555, REMINDER_ROW_HEIGHT);
				lineLabel.setVisible(true);
				row++;
			}
		}
		racePanel.setBackground(Color.DARK_GRAY);
		raceRemind.setForeground(Color.WHITE);
		racePanel.revalidate();
		racePanel.repaint();
	}  /*--------------
		END UPDATEREMINDER
		--------------*/

	private int countReminderRows(String[] lines) {
		if (lines == null) return 0;
		int count = 0;
		for (String rawLine : lines) {
			if (rawLine == null) continue;
			String line = rawLine.trim();
			if (line.isBlank()) continue;
			count++;
			if (count >= REMINDER_MAX_ROWS) {
				return REMINDER_MAX_ROWS;
			}
		}
		return count;
	}

	private int getReminderTop(int rowCount) {
		int visibleRows = Math.max(1, Math.min(REMINDER_MAX_ROWS, rowCount));
		return Math.max(0, (REMINDER_PANEL_MIN_HEIGHT - (visibleRows * REMINDER_ROW_HEIGHT)) / 2);
	}

	private boolean applyFelshifySize(String selection) {
		if (character == null || character.getIdentity() == null) return false;
		String current = character.getIdentity().getSize();
		if ("Cat".equalsIgnoreCase(selection)) {
			if (!"Tiny".equalsIgnoreCase(current)) {
				character.getIdentity().setSize("Tiny");
				return true;
			}
			return false;
		} else if ("Human".equalsIgnoreCase(selection)) {
			if (!"Medium".equalsIgnoreCase(current)) {
				character.getIdentity().setSize("Medium");
				return true;
			}
			return false;
		}
		return false;
	}

	private void resetReminderRows() {
		raceRemind.setVisible(false);
		for (int i = 0; i < REMINDER_MAX_ROWS; i++) {
			reminderLabels[i].setText("");
			reminderLabels[i].setVisible(false);
			JComboBox<String> combo = reminderCombos[i];
			combo.setVisible(false);
			combo.removeAllItems();
			for (var listener : combo.getActionListeners()) {
				combo.removeActionListener(listener);
			}
		}
	}

	private ArrayList<String> getShapeshiftOptionsFromLists() {
		LinkedHashSet<String> optionSet = new LinkedHashSet<>();
		optionSet.add("** None **");
		if (character == null || character.getLists() == null) return new ArrayList<>(optionSet);

		for (List<DataList> listGroup : character.getLists()) {
			if (listGroup == null) continue;
			for (DataList entry : listGroup) {
				if (entry == null || entry.getList() == null) continue;
				if (!"Shapeshift".equalsIgnoreCase(entry.getList().trim())) continue;

				String name = entry.getName();
				if (name == null) continue;
				String trimmed = name.trim();
				if (trimmed.isBlank()) continue;
				optionSet.add(trimmed);
			}
		}
		return new ArrayList<>(optionSet);
	}
	
	/*
	 * 		UPDATE ALL (Placeholder for Override)
	 */
	public void updateAll() {
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	* 
	* 		BUILDER
	* 
	*/
	/*
	 * 		BUILD LABEL
	 */
	public final JLabel buildLabel (String tempString, Color tempColor) {
		JLabel tempField = new JLabel(tempString);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		if (tempColor != null) tempField.setForeground(tempColor);
		add(tempField);
		return tempField;
	}  /*--------------
		END BUILDLABEL
		--------------*/

	/*
	 * 		BUILD TEXTFIELD
	 */
	public JTextField buildTextField (String tempString) {
		JTextField tempField = new JTextField(tempString);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		tempField.setEditable(false);
		if (alternate)
			{tempField.setBackground(Color.WHITE);}
		else 
			{tempField.setBackground(Color.LIGHT_GRAY);}
		add(tempField);
		return tempField;
	}  /*--------------
		END BUILDTEXTFIELD
		--------------*/
	
	/*
	 * 		BUILD NUMTEXTFIELD
	 */
	public JFormattedTextField buildNumTextField (double tempDouble) {
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(0);
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Double.class);
	    formatter.setMinimum(0.0);
	    formatter.setMaximum(Double.MAX_VALUE);
	    formatter.setAllowsInvalid(false);	
	    
		JFormattedTextField tempField = new JFormattedTextField(new DefaultFormatterFactory(formatter));
		tempField.setValue(round2(tempDouble));
		tempField.setHorizontalAlignment(JTextField.CENTER);
		tempField.setEditable(false);
		if (alternate) 
			{tempField.setBackground(Color.WHITE);}
		else 
			{tempField.setBackground(Color.LIGHT_GRAY);}
		add(tempField);
		return tempField;
	}
	public JFormattedTextField buildNumTextField (int tempInt) {
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);		
		
		JFormattedTextField tempField = new JFormattedTextField(new DefaultFormatterFactory(formatter));
		tempField.setValue(tempInt);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		tempField.setEditable(false);
		if (alternate) 
			{tempField.setBackground(Color.WHITE);}
		else 
			{tempField.setBackground(Color.LIGHT_GRAY);}
		add(tempField);
		return tempField;
	}  /*--------------
		END BUILDNUMTEXTFIELD
		--------------*/
	
	/*
	 * 		BUILD TEXTAREA
	 */
	public JTextArea buildTextArea (String tempString) {
		JTextArea tempField = new JTextArea(tempString);
		tempField.setMargin(new Insets (5, 5, 5, 5));
		tempField.setEditable(false);		
		return tempField;
	}  /*--------------
		END BUILDTEXTAREA
		--------------*/
	
	/*
	 * 		BUILD SCROLLPANE
	 */
	public JScrollPane buildScrollPane (JTextArea tempArea) {
		JScrollPane tempPane = new JScrollPane(tempArea);
		tempPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(tempPane);
		return tempPane;
	}  /*--------------
		END BUILDSCROLLPANE
		--------------*/
	
	public JScrollPane buildScrollPane (JTextPane tempArea) {
		JScrollPane tempPane = new JScrollPane(tempArea);
		tempPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(tempPane);
		return tempPane;
	}
	
	/*
	 * 		BUILD COMBOBOX
	 */
	public JComboBox<String> buildComboBox () {
		JComboBox<String> tempBox = new JComboBox<String>();
		tempBox.addItem("*** Empty ***");
		add(tempBox);
		
		return tempBox;
	}  /*--------------
		END BUILDCOMBOBOX
		--------------*/
	
	/*
	 * 		BUILD BUTTON
	 */
	public JButton buildButton (String tempString) {
		JButton tempButton = new JButton(tempString);
		add(tempButton);
		return tempButton;
	}  /*--------------
		END BUILDCOMBOBOX
		--------------*/
	
	/*
	 * 		BUILD CHECK BUTTON
	 */
	public JButton buildCheckButton (String checkName, boolean skill, String att) {
		JButton tempButton = new JButton("Roll");
		tempButton.addActionListener(f -> checkPressed(checkName, skill, att, ""));
		add(tempButton);
		return tempButton;
	}
	public JButton buildCheckButton (String checkName, boolean skill, String att, String att2) {
		JButton tempButton = new JButton("Roll");
		tempButton.addActionListener(f -> checkPressed(checkName, skill, att, att2));
		add(tempButton);
		return tempButton;
	}  /*--------------
		END BUILDCHECKBUTTON
		--------------*/

	/** Formats doubles to two decimal places for UI/tooltips. */
	protected String fmt(double val) {
		return UI_DECIMAL_FORMAT.get().format(val);
	}

	/** Rounds a double to two decimal places for numeric fields. */
	protected double round2(double val) {
		return Math.round(val * 100.0) / 100.0;
	}

	private static String toHexColor(int red, int green, int blue) {
		char[] hex = new char[7];
		hex[0] = '#';
		writeHexByte(hex, 1, red);
		writeHexByte(hex, 3, green);
		writeHexByte(hex, 5, blue);
		return new String(hex);
	}

	private static void writeHexByte(char[] chars, int offset, int value) {
		int clamped = Math.max(0, Math.min(255, value));
		chars[offset] = Character.toLowerCase(Character.forDigit((clamped >>> 4) & 0xF, 16));
		chars[offset + 1] = Character.toLowerCase(Character.forDigit(clamped & 0xF, 16));
	}
	
} ///////////////////////////////////////////////END OF CLASS////////////////////////////////////////////////////////////////////////

