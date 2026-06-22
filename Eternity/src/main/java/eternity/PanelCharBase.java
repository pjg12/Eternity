package eternity;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.ImageIcon;
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
	private static final int REMINDER_VERTICAL_PADDING = 5;
	private static final String TECHNICAL_REMINDER_MARKER = "[[TECH]]";
	private static final String REMINDER_ICON_PREFIX = "[[ICON:";
	private static final String MOLDING_REMINDER_TEXT = "Molding:";
	private static final int REMINDER_ICON_SIZE = 18;
	private final ArrayList<JLabel> reminderLabels = new ArrayList<>();
	private final ArrayList<JComboBox<String>> reminderCombos = new ArrayList<>();
	private final ArrayList<JButton> reminderButtons = new ArrayList<>();
	private final Map<String, ImageIcon> reminderIconCache = new HashMap<>();
	private boolean suppressReminderEvents = false;
	private final JComboBox<String> headerModeToggle;
	private boolean suppressHeaderToggleEvents = false;
	
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
		headerModeToggle = new JComboBox<>();
			headerModeToggle.setVisible(false);
			headerModeToggle.addActionListener(e -> {
				if (suppressHeaderToggleEvents) return;
				Object selected = headerModeToggle.getSelectedItem();
				onHeaderToggleChanged(selected == null ? "" : selected.toString());
				resizeHeader();
				refreshHPAuraOnly();
				revalidate();
				repaint();
			});
			add(headerModeToggle);
		
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
		configureHeaderToggle();
		if (useCombinedPowerHpHeader()) {
			hpL.setText("HP");
			hpL.setBounds(5, pageHeight, 175, 20);
			auraL.setText(getPowerHeaderTitle());
			auraL.setVisible(true);
			auraL.setBounds(385, pageHeight, 175, 20);
			if (useHeaderToggle()) {
				headerModeToggle.setBounds(205, pageHeight, 150, 20);
			}
			pageHeight += 25;

			charCurrHPL.setBounds(5, pageHeight, 85, 20);
			charMaxHPL.setBounds(95, pageHeight, 85, 20);
			charMaxAuraL.setBounds(385, pageHeight, 90, 20);
			pageHeight += 20;

			charCurrHP.setBounds(5, pageHeight, 85, 20);
			charMaxHP.setBounds(95, pageHeight, 85, 20);
			charMaxAura.setBounds(385, pageHeight, 85, 20);
			pageHeight += 20;
		} else if (usePowerHeader()) {
			hpL.setText(getPowerHeaderTitle());
			hpL.setBounds(5, pageHeight, 175, 20);
			auraL.setVisible(false);
			if (useHeaderToggle()) {
				headerModeToggle.setBounds(205, pageHeight, 150, 20);
			}
			pageHeight += 25;

			charMaxAuraL.setBounds(5, pageHeight, 175, 20);
			pageHeight += 20;

			charMaxAura.setBounds(5, pageHeight, 85, 20);
			pageHeight += 20;
		} else if (useHpOnlyHeader()) {
			hpL.setText("HP");
			hpL.setBounds(5, pageHeight, 175, 20);
			auraL.setVisible(false);
			if (useHeaderToggle()) {
				headerModeToggle.setBounds(205, pageHeight, 150, 20);
			}
			pageHeight += 25;

			charCurrHPL.setBounds(5, pageHeight, 85, 20);
			charMaxHPL.setBounds(95, pageHeight, 85, 20);
			pageHeight += 20;

			charCurrHP.setBounds(5, pageHeight, 85, 20);
			charMaxHP.setBounds(95, pageHeight, 85, 20);
			pageHeight += 20;
		} else {
		// Top Labels
			hpL.setText("HP");
			auraL.setText("Aura");
			auraL.setVisible(true);
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
		}
		
		/*
		 * Place Reminder
		 */
		int additionalHeaderHeight = getAdditionalHeaderControlsHeight();
		if (additionalHeaderHeight > 0) {
			layoutAdditionalHeaderControls(pageHeight);
			pageHeight += additionalHeaderHeight;
		}
		pageHeight += 5;
		int reminderPanelHeight = getReminderPanelHeight(getReminderRowCount());
		racePanel.setBounds(5,pageHeight,555,reminderPanelHeight);
		raceRemind.setBounds(0,0,555,reminderPanelHeight);
		pageHeight += reminderPanelHeight + 5;
		tabTitleL.setBounds(5, pageHeight, 555, 20);
		pageHeight += 25;
		updateReminder();

		return pageHeight;
	}

	private void configureHeaderToggle() {
		if (!useHeaderToggle()) {
			headerModeToggle.setVisible(false);
			return;
		}
		suppressHeaderToggleEvents = true;
		try {
			headerModeToggle.removeAllItems();
			for (String option : getHeaderToggleOptions()) {
				headerModeToggle.addItem(option);
			}
			String selection = getHeaderToggleSelection();
			headerModeToggle.setSelectedItem(selection);
			if (headerModeToggle.getSelectedIndex() < 0 && headerModeToggle.getItemCount() > 0) {
				headerModeToggle.setSelectedIndex(0);
			}
			headerModeToggle.setVisible(true);
		} finally {
			suppressHeaderToggleEvents = false;
		}
	}

	private void initializeReminderRows() {
		ensureReminderRowCapacity(1);
	}

	private void ensureReminderRowCapacity(int size) {
		while (reminderLabels.size() < size) {
			JLabel lineLabel = new JLabel();
			lineLabel.setVisible(false);
			lineLabel.setForeground(Color.WHITE);
			racePanel.add(lineLabel);
			reminderLabels.add(lineLabel);

			JComboBox<String> drop = new JComboBox<>();
			drop.setVisible(false);
			racePanel.add(drop);
			racePanel.setComponentZOrder(drop, 0);
			reminderCombos.add(drop);

			JButton button = new JButton("Share");
			button.setVisible(false);
			racePanel.add(button);
			racePanel.setComponentZOrder(button, 0);
			reminderButtons.add(button);
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
			mod += resolveUniversalSkillBonus();
			mod += resolveSkillCheckBonus(checkName);
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
			tempString += buildSummonedMinionInitiativeLines();
			tempString += " --~|turnorder;replacetoken;@{selected|token_id};[$InitTotal]}}";
		}
		
		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private String buildSummonedMinionInitiativeLines() {
		if (sheetFrame == null) return "";
		List<PanelCharMinion.MinionInitiativeInfo> minions = sheetFrame.getSummonedMinionInitiativeInfo();
		if (minions == null || minions.isEmpty()) return "";
		StringBuilder block = new StringBuilder();
		for (PanelCharMinion.MinionInitiativeInfo minion : minions) {
			if (minion == null || minion.slotTitle() == null || minion.slotTitle().isBlank()) continue;
			String safeVar = sanitizeInitiativeVarName(minion.slotTitle());
			block.append(" --=").append(safeVar).append("|[$InitTotal] + ").append(minion.initiativeOffset());
			block.append(" --+|").append(minion.slotTitle()).append(" Initiative: [$").append(safeVar).append("]");
		}
		return block.toString();
	}

	private String sanitizeInitiativeVarName(String title) {
		if (title == null || title.isBlank()) return "MinionInit";
		String cleaned = title.replaceAll("[^A-Za-z0-9]", "");
		if (cleaned.isBlank()) return "MinionInit";
		return cleaned + "Init";
	}

	private double resolveSkillCheckBonus(String checkName) {
		if (character == null || checkName == null || checkName.isBlank()) return 0.0;
		String displayName = checkName;
		if (displayName.endsWith(" Check")) {
			displayName = displayName.substring(0, displayName.length() - " Check".length()).trim();
		}
		return character.getSkillSpecialBonusForDisplayName(displayName);
	}

	private double resolveUniversalSkillBonus() {
		if (character == null) return 0.0;
		return character.getUniversalSkillBonus();
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

	public void refreshHeaderState(StoreCharData character) {
		this.character = character;
		refreshBaseState();
		repaint();
	}

	protected void synchronizeCharacterState() {
		if (character == null || dataQuery == null) return;
		character.syncIdentityDerivedState(dataQuery);
		character.syncLevelBaseResources(dataQuery);
		character.syncLevelCombatScalers(dataQuery);
		character.updateAll();
	}

	protected void refreshBaseState() {
		refreshHPAuraOnly();
		refreshReminderOnly();
	}

	protected void refreshHPAuraOnly() {
		if (useCombinedPowerHpHeader()) {
			updateCombinedPowerHpHeader();
			return;
		}
		updateHPAura();
		if (usePowerHeader()) {
			updatePowerHeader();
		} else if (useHpOnlyHeader()) {
			updateHpOnlyHeader();
		}
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

	protected boolean usePowerHeader() {
		return false;
	}

	protected boolean useCombinedPowerHpHeader() {
		return false;
	}

	protected boolean useHpOnlyHeader() {
		return false;
	}

	protected boolean useHeaderToggle() {
		return false;
	}

	protected int getAdditionalHeaderControlsHeight() {
		return 0;
	}

	protected void layoutAdditionalHeaderControls(int startY) {
	}

	protected String[] getHeaderToggleOptions() {
		return new String[0];
	}

	protected String getHeaderToggleSelection() {
		return "";
	}

	protected void onHeaderToggleChanged(String selection) {
	}

	protected String getPowerHeaderTitle() {
		return "Power";
	}

	protected String getPowerHeaderLabel() {
		return "Provided Value";
	}

	protected double getPowerHeaderValue() {
		return 0.0;
	}

	protected void updatePowerHeader() {
		double powerValue = Math.max(0.0, getPowerHeaderValue());
		hpL.setVisible(true);
		auraL.setVisible(false);

		charCurrHPL.setVisible(false);
		charMaxHPL.setVisible(false);
		charCurrHP.setVisible(false);
		charMaxHP.setVisible(false);

		charAvailAuraL.setVisible(false);
		charOccAuraL.setVisible(false);
		charSpentAuraL.setVisible(false);
		charAvailAura.setVisible(false);
		charOccAura.setVisible(false);
		charSpentAura.setVisible(false);

		charMaxAuraL.setVisible(true);
		charMaxAuraL.setText(getPowerHeaderLabel());
		charMaxAuraL.setToolTipText(getPowerHeaderTitle() + ": " + fmt(powerValue));
		charMaxAura.setVisible(true);
		charMaxAura.setValue(round2(powerValue));
		charMaxAura.setToolTipText(getPowerHeaderTitle() + ": " + fmt(powerValue));
	}

	protected void updateHpOnlyHeader() {
		hpL.setVisible(true);
		auraL.setVisible(false);
		charCurrHPL.setVisible(true);
		charMaxHPL.setVisible(true);
		charCurrHP.setVisible(true);
		charMaxHP.setVisible(true);
		charAvailAuraL.setVisible(false);
		charOccAuraL.setVisible(false);
		charSpentAuraL.setVisible(false);
		charMaxAuraL.setVisible(false);
		charAvailAura.setVisible(false);
		charOccAura.setVisible(false);
		charSpentAura.setVisible(false);
		charMaxAura.setVisible(false);
	}

	protected boolean showCombinedHpFields() {
		return true;
	}

	protected int getCombinedHeaderCurrentHp() {
		if (character == null || character.getResources() == null) return 0;
		return Math.max(0, character.getResources().calcCurrentHP());
	}

	protected int getCombinedHeaderMaxHp() {
		if (character == null || character.getResources() == null) return 0;
		return Math.max(0, character.getResources().calcMaxHP());
	}

	protected void updateCombinedPowerHpHeader() {
		double powerValue = Math.max(0.0, getPowerHeaderValue());
		boolean showHp = showCombinedHpFields();
		int maxHp = Math.max(0, getCombinedHeaderMaxHp());
		int currentHp = Math.max(0, Math.min(getCombinedHeaderCurrentHp(), maxHp));

		hpL.setVisible(true);
		hpL.setText("HP");
		auraL.setVisible(true);
		auraL.setText(getPowerHeaderTitle());
		auraL.setToolTipText(getPowerHeaderTitle() + ": " + fmt(powerValue));

		charCurrHPL.setVisible(showHp);
		charMaxHPL.setVisible(showHp);
		charCurrHP.setVisible(showHp);
		charMaxHP.setVisible(showHp);
		if (showHp) {
			charCurrHP.setMaximum(Math.max(1, maxHp));
			charCurrHP.setValue(Math.min(currentHp, Math.max(1, maxHp)));
			charCurrHP.setString(currentHp + "/" + maxHp);
			charMaxHP.setValue(maxHp);
			String currentTip = "Current HP: " + currentHp + " / " + maxHp;
			charCurrHPL.setToolTipText(currentTip);
			charCurrHP.setToolTipText(currentTip);
			String maxTip = "Max HP: " + maxHp;
			charMaxHPL.setToolTipText(maxTip);
			charMaxHP.setToolTipText(maxTip);
		}

		charAvailAuraL.setVisible(false);
		charOccAuraL.setVisible(false);
		charSpentAuraL.setVisible(false);
		charAvailAura.setVisible(false);
		charOccAura.setVisible(false);
		charSpentAura.setVisible(false);

		charMaxAuraL.setVisible(true);
		charMaxAuraL.setText(getPowerHeaderLabel());
		charMaxAuraL.setToolTipText(getPowerHeaderTitle() + ": " + fmt(powerValue));
		charMaxAura.setVisible(true);
		charMaxAura.setValue(round2(powerValue));
		charMaxAura.setToolTipText(getPowerHeaderTitle() + ": " + fmt(powerValue));
	}
	
	/*
	 * 		UPDATE REMINDER
	 */
	public void updateReminder() {
		String reminder = (character != null) ? character.getPanelReminder() : null;
		suppressReminderEvents = true;
		try {
			resetReminderRows();
			if (reminder == null || reminder.isBlank()) {
				raceRemind.setText("");
				raceRemind.setVisible(false);
			} else {
				String normalized = StoreCharData.normalizeReminderText(reminder);
				String[] lines = normalized.split("\n");
				int displayRowCount = countReminderRows(lines);
				int reminderPanelHeight = getReminderPanelHeight(displayRowCount);
				raceRemind.setBounds(0, 0, 555, reminderPanelHeight);
				racePanel.setBounds(racePanel.getX(), racePanel.getY(), racePanel.getWidth(), reminderPanelHeight);
				ensureReminderRowCapacity(Math.max(1, displayRowCount));
				int reminderTop = getReminderTop(displayRowCount, reminderPanelHeight);
				int row = 0;
				for (String rawLine : lines) {
					if (rawLine == null) continue;
					String line = rawLine.trim();
					if (line.isBlank()) continue;

					if (isFightingFormReminderLine(line)) {
						ArrayList<String> formOptions = getFormsOptionsFromLists();
						if (!formOptions.isEmpty()) {
							String entryText = line;
							String reminderTooltip = resolveFightingFormReminderTooltip();
							JLabel lineLabel = reminderLabels.get(row);
							lineLabel.setIcon(null);
							lineLabel.setIconTextGap(4);
							lineLabel.setText(entryText);
							lineLabel.setToolTipText(reminderTooltip);
							lineLabel.setHorizontalAlignment(JTextField.RIGHT);
							lineLabel.setVerticalAlignment(JLabel.CENTER);
							lineLabel.setBounds(5, reminderTop + (row * REMINDER_ROW_HEIGHT), 255, REMINDER_ROW_HEIGHT);
							lineLabel.setVisible(true);

							JComboBox<String> drop = reminderCombos.get(row);
							drop.removeAllItems();
							for (String option : formOptions) {
								drop.addItem(option);
							}
							int rowY = reminderTop + (row * REMINDER_ROW_HEIGHT);
							drop.setBounds(275, rowY, 142, REMINDER_ROW_HEIGHT);
							drop.setToolTipText(reminderTooltip);
							if (character != null) {
								String savedValue = character.getReminderSelection(entryText);
								if (savedValue != null && !savedValue.isBlank()) {
									drop.setSelectedItem(savedValue);
								} else if (drop.getItemCount() > 0) {
									Object selected = drop.getItemAt(0);
									character.setReminderSelection(entryText, selected == null ? "" : selected.toString());
								}
								drop.addActionListener(e -> {
									if (suppressReminderEvents) return;
									Object selected = drop.getSelectedItem();
									String selectedText = selected == null ? "" : selected.toString();
									character.setReminderSelection(entryText, selectedText);
									refreshAfterReminderSelection(false);
								});
							}
							drop.setVisible(true);
							row++;
							continue;
						}
					}

					if (isMoldingReminderLine(line)) {
						JLabel lineLabel = reminderLabels.get(row);
						lineLabel.setIcon(null);
						lineLabel.setIconTextGap(4);
						lineLabel.setText(line);
						lineLabel.setHorizontalAlignment(JTextField.RIGHT);
						lineLabel.setVerticalAlignment(JLabel.CENTER);
						lineLabel.setBounds(5, reminderTop + (row * REMINDER_ROW_HEIGHT), 255, REMINDER_ROW_HEIGHT);
						lineLabel.setVisible(true);

						JButton moldButton = reminderButtons.get(row);
						int rowY = reminderTop + (row * REMINDER_ROW_HEIGHT);
						moldButton.setText("Mold");
						moldButton.setBounds(275, rowY, 80, REMINDER_ROW_HEIGHT);
						moldButton.setToolTipText("Select the specific molds you would like to activate.");
						moldButton.setEnabled(character != null && character.hasActiveMoldingTechnique());
						moldButton.addActionListener(e -> openMoldFrame());
						moldButton.setVisible(true);
						row++;
						continue;
					}

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
							JLabel lineLabel = reminderLabels.get(row);
							lineLabel.setIcon(null);
							lineLabel.setIconTextGap(4);
							lineLabel.setText(entryText);
							lineLabel.setHorizontalAlignment(JTextField.RIGHT);
							lineLabel.setVerticalAlignment(JLabel.CENTER);
							lineLabel.setBounds(5, reminderTop + (row * REMINDER_ROW_HEIGHT), 255, REMINDER_ROW_HEIGHT);
							lineLabel.setVisible(true);

							JComboBox<String> drop = reminderCombos.get(row);
							drop.removeAllItems();
							for (String option : options) {
								drop.addItem(option);
							}
							boolean isFelshify = entryText.toLowerCase().contains("felshify");
							boolean isDomainEmanation = "Domain Emanation".equalsIgnoreCase(entryText);
							int rowY = reminderTop + (row * REMINDER_ROW_HEIGHT);
							drop.setBounds(275, rowY, isDomainEmanation ? 70 : 142, REMINDER_ROW_HEIGHT);
							if (isFelshify) {
								drop.setSelectedItem("Cat");
							}
							if (character != null) {
								String savedValue = character.getReminderSelection(entryText);
								if (savedValue != null && !savedValue.isBlank()) {
									if (isFelshify && "Human".equalsIgnoreCase(savedValue)) {
										savedValue = "Felsh";
									}
									drop.setSelectedItem(savedValue);
								} else if (isDomainEmanation) {
									drop.setSelectedItem(character.isDomainEmanationEnabled() ? "On" : "Off");
								}
								if (isFelshify) {
									Object selected = drop.getSelectedItem();
									applyFelshifySize(selected == null ? "" : selected.toString());
								}
								drop.addActionListener(e -> {
									if (suppressReminderEvents) return;
									Object selected = drop.getSelectedItem();
									String selectedText = selected == null ? "" : selected.toString();
									if (isDomainEmanation) {
										character.setDomainEmanationEnabled("On".equalsIgnoreCase(selectedText));
									}
									character.setReminderSelection(entryText, selectedText);
									boolean refreshPanels = isDomainEmanation;
									if (isFelshify) {
										refreshPanels = applyFelshifySize(selectedText) || refreshPanels;
									}
									refreshAfterReminderSelection(refreshPanels);
								});
							}
							drop.setVisible(true);

							JButton shareButton = reminderButtons.get(row);
							if (isDomainEmanation) {
								shareButton.setBounds(355, rowY, 80, REMINDER_ROW_HEIGHT);
								shareButton.setToolTipText("Copy the current domain bonus status code as a Scriptcards macro.");
								shareButton.setEnabled(character != null && character.isDomainEmanationEnabled());
								shareButton.addActionListener(e -> copyDomainEmanationMacroToClipboard());
								shareButton.setVisible(character != null && character.isDomainEmanationEnabled());
							}
							row++;
							continue;
						}
					}

					JLabel lineLabel = reminderLabels.get(row);
					ReminderLineDisplay reminderDisplay = parseReminderLineDisplay(line);
					lineLabel.setText(reminderDisplay.text());
					lineLabel.setIcon(reminderDisplay.icon());
					lineLabel.setToolTipText(null);
					lineLabel.setIconTextGap(reminderDisplay.icon() == null ? 0 : 4);
					lineLabel.setHorizontalAlignment(reminderDisplay.centered() ? JTextField.CENTER
							: (reminderDisplay.icon() == null ? JTextField.CENTER : JTextField.LEFT));
					lineLabel.setVerticalAlignment(JLabel.CENTER);
					lineLabel.setBounds(0, reminderTop + (row * REMINDER_ROW_HEIGHT), 555, REMINDER_ROW_HEIGHT);
					lineLabel.setVisible(true);
					row++;
				}
			}
		} finally {
			suppressReminderEvents = false;
		}
		racePanel.setBackground(Color.DARK_GRAY);
		raceRemind.setForeground(Color.WHITE);
		racePanel.revalidate();
		racePanel.repaint();
	}  /*--------------
		END UPDATEREMINDER
		--------------*/

	private boolean hasShareableDomainStatusEffects() {
		return character != null && character.hasShareableDomainStatusEffects();
	}

	private boolean isShareableDomainStatus(DataStatus status) {
		if (status == null) return false;
		String attribute = status.getAttribute();
		if (attribute == null || attribute.isBlank()) return false;
		if ("REMINDER".equalsIgnoreCase(attribute)) return false;
		return Math.abs(status.getSeverity()) >= 0.0001;
	}

	private void copyDomainEmanationMacroToClipboard() {
		StringSelection stringSelection = new StringSelection(character == null ? "" : character.buildDomainStatusMacro());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private void refreshAfterReminderSelection(boolean refreshPanels) {
		if (character == null) return;
		character.updateAll();
		if (sheetFrame != null && refreshPanels) {
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshTrainingPanel();
			sheetFrame.refreshInventoryPanel();
			return;
		}
		refreshBaseState();
		updateAll();
		revalidate();
		repaint();
	}

	private String buildDomainEmanationMacro() {
		DataColor raceColor = null;
		if (dataQuery != null && character != null && character.getIdentity() != null) {
			raceColor = dataQuery.getColorByTitle(character.getIdentity().getRace());
		}
		if (raceColor == null) {
			raceColor = DEFAULT_DISPLAY_COLOR;
		}

		String colorString1 = toHexColor(raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = toHexColor(raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String charName = character != null && character.getIdentity() != null && character.getIdentity().getName() != null
				? character.getIdentity().getName()
				: "Character";
		String domainName = "";
		if (character != null && character.getTraining() != null && !character.getTraining().getDomains().isEmpty()) {
			domainName = character.getTraining().getDomains().get(0);
		}

		StringBuilder tempString = new StringBuilder();
		tempString.append("!scriptcard {{ --#titleCardBackground|").append(colorString1)
				.append(" --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|").append(colorString1)
				.append(" --#titleCardBottomBorder|4px solid #000000; --#title|").append(charName)
				.append(" --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|").append(colorString2)
				.append(" --#leftSub|Domain Emanation")
				.append(" --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|").append(colorString1)
				.append(" --#evenRowFontColor|").append(colorString2)
				.append(" --#oddRowBackground|").append(colorString2)
				.append(" --#oddRowFontColor|").append(colorString1)
				.append(" --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;");

		if (!domainName.isBlank()) {
			tempString.append(" --+|Domain: ").append(sanitizeStatusCodeText(domainName));
		}
		String statusCode = buildDomainEmanationStatusCode();
		if (statusCode.isBlank()) {
			tempString.append(" --+|No shareable domain bonus statuses are active.");
		} else {
			tempString.append(" --+|Status Code:[br]&nbsp;&nbsp;").append(statusCode);
		}
		tempString.append(" }}");
		return tempString.toString();
	}

	private String buildDomainEmanationStatusCode() {
		if (character == null || character.getTraining() == null) return "";
		Map<String, Double> totals = new LinkedHashMap<>();
		for (DataStatus status : character.getTraining().getDomainStatusEffects()) {
			if (!isShareableDomainStatus(status)) continue;
			addStatusCodeContribution(totals, status.getAttribute(), status.getSeverity());
		}
		if (totals.isEmpty()) return "";

		StringBuilder code = new StringBuilder();
		code.append("NM").append(buildDomainStatusCodeName());
		String durationToken = buildDomainStatusDurationToken();
		if (!durationToken.isBlank()) {
			code.append('_').append(durationToken);
		}
		boolean appendedAnyStatus = false;
		for (Map.Entry<String, Double> entry : totals.entrySet()) {
			double severity = entry.getValue() == null ? 0.0 : entry.getValue();
			if (Math.abs(severity) < 0.0001) continue;
			if (!appendedAnyStatus) {
				code.append('_');
				appendedAnyStatus = true;
			} else {
				code.append('_');
			}
			code.append(StatusCodeParser.getPreferredAttributeAlias(entry.getKey()))
					.append(formatPackedStatusSeverity(severity));
		}
		return code.toString();
	}

	private String buildDomainStatusCodeName() {
		return "DOM";
	}

	private String buildDomainStatusDurationToken() {
		if (character == null || character.getIdentity() == null) return "";
		String className = character.getIdentity().getCharClass();
		if (className == null || className.isBlank()) return "";
		if ("Paladin".equalsIgnoreCase(className)) {
			return "DUR:Turn:1";
		}
		if ("Cleric".equalsIgnoreCase(className)) {
			int duration = Math.max(1, (int) Math.floor(character.getLevel() / 2.0) + 1);
			return "DUR:Turn:" + duration;
		}
		return "";
	}

	private void addStatusCodeContribution(Map<String, Double> totals, String rawAttribute, double severity) {
		if (totals == null || rawAttribute == null || rawAttribute.isBlank() || Math.abs(severity) < 0.0001) return;
		String normalized = normalizeStatusCodeAttribute(rawAttribute);
		if (normalized == null || normalized.isBlank()) return;
		switch (normalized) {
			case "AC" -> mergeStatusSeverity(totals, "BDEF", severity);
			case "MAXHP" -> mergeStatusSeverity(totals, "BASEHP", severity);
			case "HPMULTI" -> mergeStatusSeverity(totals, "MULTIHP", severity);
			case "MAXAURA" -> mergeStatusSeverity(totals, "BASEAURA", severity);
			case "AURAMULTI" -> mergeStatusSeverity(totals, "MULTIAURA", severity);
			case "REACT" -> mergeStatusSeverity(totals, "BASEREACT", severity);
			case "R1" -> mergeStatusSeverity(totals, "BASER1", severity);
			case "R2" -> mergeStatusSeverity(totals, "BASER2", severity);
			case "R3" -> mergeStatusSeverity(totals, "BASER3", severity);
			case "DMGMULTI" -> {
				mergeStatusSeverity(totals, "MBDMG", severity);
				mergeStatusSeverity(totals, "MTDMG", severity);
			}
			case "HEALMULTI" -> {
				mergeStatusSeverity(totals, "MBHEAL", severity);
				mergeStatusSeverity(totals, "MTHEAL", severity);
			}
			default -> {
				String outputAttribute = normalized.startsWith("B") || normalized.startsWith("M")
						? normalized
						: "B" + normalized;
				if (isSupportedStatusCodeAttribute(outputAttribute)) {
					mergeStatusSeverity(totals, outputAttribute, severity);
				}
			}
		}
	}

	private void mergeStatusSeverity(Map<String, Double> totals, String attribute, double severity) {
		if (totals == null || attribute == null || attribute.isBlank() || Math.abs(severity) < 0.0001) return;
		totals.merge(attribute, severity, Double::sum);
	}

	private String normalizeStatusCodeAttribute(String key) {
		if (key == null) return null;
		String upper = key.trim().toUpperCase(Locale.ROOT);
		if (upper.isBlank()) return null;
		return switch (upper) {
			case "SPENTAURA", "LOSTHP", "KBRES" -> null;
			case "APPLY" -> "APP";
			case "IMPAIR" -> "IMP";
			case "RESPHY" -> "PHY";
			default -> {
				if (upper.startsWith("RESIST")) yield upper.substring("RESIST".length());
				if (upper.startsWith("RES") && upper.length() > 3) yield upper.substring(3);
				yield upper;
			}
		};
	}

	private boolean isSupportedStatusCodeAttribute(String attribute) {
		if (attribute == null || attribute.isBlank()) return false;
		String upper = attribute.trim().toUpperCase(Locale.ROOT);
		return switch (upper) {
			case "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
					"BASEREACT", "BASER1", "BASER2", "BASER3",
					"BSTR", "BDEX", "BCON", "BFOC", "BCTL", "BCAP", "BKNOW", "BMECH", "BPERC", "BINT", "BCHA", "BSUB",
					"BARMOR", "BDODGE", "BDEF", "BFORT", "BREF", "BWILL", "BAVOID",
					"BATK", "BAPP", "BMOVE", "BFLY", "BRANGE", "BINIT", "BCMAN", "BMAXATK",
					"BSUP", "BIMP", "BMAST", "BEXCL", "BGRANT", "BCRUSH", "BAREA", "BPOWER",
					"BBDMG", "BTDMG", "BBHEAL", "BTHEAL",
					"MBDMG", "MTDMG", "MBHEAL", "MTHEAL",
					"BALL", "BPHY", "BBLUNT", "BPIERCE", "BSLASH", "BFIRE", "BFROST", "BELEC", "BENERGY", "BSONIC",
					"BLIGHT", "BTOXIC", "BDARK", "BPSI", "BSPIRIT", "BTIME" -> true;
			default -> false;
		};
	}

	private String formatPackedStatusSeverity(double severity) {
		double rounded = round2(severity);
		String sign = rounded < 0 ? "N" : "P";
		double absolute = Math.abs(rounded);
		if (Math.abs(absolute - Math.rint(absolute)) < 0.0001) {
			return sign + Integer.toString((int) Math.rint(absolute));
		}
		String text = fmt(absolute).replace(".", "D");
		return sign + text;
	}

	private String sanitizeStatusCodeText(String value) {
		if (value == null) return "";
		return value.replace("|", "/").trim();
	}

	private int countReminderRows(String[] lines) {
		if (lines == null) return 0;
		int count = 0;
		for (String rawLine : lines) {
			if (rawLine == null) continue;
			String line = rawLine.trim();
			if (line.isBlank()) continue;
			count++;
		}
		return count;
	}

	private int getReminderRowCount() {
		if (character == null) return 0;
		String reminder = StoreCharData.normalizeReminderText(character.getPanelReminder());
		if (reminder.isBlank()) return 0;
		return countReminderRows(reminder.split("\n"));
	}

	private int getReminderPanelHeight(int rowCount) {
		int visibleRows = Math.max(1, rowCount);
		return Math.max(REMINDER_PANEL_MIN_HEIGHT, (visibleRows * REMINDER_ROW_HEIGHT) + (REMINDER_VERTICAL_PADDING * 2));
	}

	private int getReminderTop(int rowCount, int panelHeight) {
		int visibleRows = Math.max(1, rowCount);
		return Math.max(0, (panelHeight - (visibleRows * REMINDER_ROW_HEIGHT)) / 2);
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
		} else if ("Felsh".equalsIgnoreCase(selection) || "Human".equalsIgnoreCase(selection)) {
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
		for (int i = 0; i < reminderLabels.size(); i++) {
			reminderLabels.get(i).setText("");
			reminderLabels.get(i).setIcon(null);
			reminderLabels.get(i).setIconTextGap(0);
			reminderLabels.get(i).setVisible(false);
			JComboBox<String> combo = reminderCombos.get(i);
			combo.setVisible(false);
			combo.removeAllItems();
			for (var listener : combo.getActionListeners()) {
				combo.removeActionListener(listener);
			}
			JButton button = reminderButtons.get(i);
			button.setVisible(false);
			button.setText("Share");
			for (var listener : button.getActionListeners()) {
				button.removeActionListener(listener);
			}
		}
	}

	private ArrayList<String> getShapeshiftOptionsFromLists() {
		LinkedHashSet<String> optionSet = new LinkedHashSet<>();
		if (character == null || character.getLists() == null) return new ArrayList<>(optionSet);

		for (List<DataList> listGroup : character.getLists()) {
			if (listGroup == null) continue;
			for (DataList entry : listGroup) {
				if (entry == null || entry.getList() == null) continue;
				String listName = entry.getList().trim();
				if (!"Shapeshift".equalsIgnoreCase(listName) && !"Shapeshifts".equalsIgnoreCase(listName)) continue;

				String name = entry.getName();
				if (name == null) continue;
				String trimmed = name.trim();
				if (trimmed.isBlank()) continue;
				optionSet.add(trimmed);
			}
		}
		return new ArrayList<>(optionSet);
	}

	private ArrayList<String> getFormsOptionsFromLists() {
		LinkedHashSet<String> optionSet = new LinkedHashSet<>();
		if (character == null || character.getLists() == null) return new ArrayList<>(optionSet);

		for (List<DataList> listGroup : character.getLists()) {
			if (listGroup == null) continue;
			for (DataList entry : listGroup) {
				if (entry == null || entry.getList() == null) continue;
				if (!"Forms".equalsIgnoreCase(entry.getList().trim())) continue;

				String name = entry.getName();
				if (name == null) continue;
				String trimmed = name.trim();
				if (trimmed.isBlank()) continue;
				optionSet.add(trimmed);
			}
		}
		return new ArrayList<>(optionSet);
	}

	private boolean isFightingFormReminderLine(String line) {
		if (line == null || character == null || character.getIdentity() == null) return false;
		if (!"Shifter".equalsIgnoreCase(character.getIdentity().getCharClass())) return false;
		return "Current Form:".equalsIgnoreCase(line.trim());
	}

	private boolean isMoldingReminderLine(String line) {
		return line != null && MOLDING_REMINDER_TEXT.equalsIgnoreCase(line.trim());
	}

	private void openMoldFrame() {
		if (character == null || !character.hasActiveMoldingTechnique()) return;
		FrameMold frame = new FrameMold(sheetFrame, character);
		frame.setVisible(true);
	}

	private String resolveFightingFormReminderTooltip() {
		if (character == null || character.getSpecials() == null) return null;
		for (DataSpecialty specialty : character.getSpecials().getAllSpecialties()) {
			if (specialty == null || specialty.getName() == null) continue;
			if (!specialty.getName().trim().toLowerCase().startsWith("fighting form")) continue;
			String description = specialty.getDescription();
			return (description == null || description.isBlank()) ? null : "<html>" + description.trim() + "</html>";
		}
		return null;
	}

	private ReminderLineDisplay parseReminderLineDisplay(String line) {
		if (line == null || line.isBlank()) {
			return new ReminderLineDisplay("", null, false);
		}
		String trimmed = line.trim();
		boolean centered = false;
		if (trimmed.startsWith(TECHNICAL_REMINDER_MARKER)) {
			centered = true;
			trimmed = trimmed.substring(TECHNICAL_REMINDER_MARKER.length()).trim();
		}
		if (!trimmed.startsWith(REMINDER_ICON_PREFIX)) {
			return new ReminderLineDisplay(trimmed, null, centered);
		}
		int markerEnd = trimmed.indexOf("]]");
		if (markerEnd <= REMINDER_ICON_PREFIX.length()) {
			return new ReminderLineDisplay(trimmed, null, centered);
		}
		String iconFileName = trimmed.substring(REMINDER_ICON_PREFIX.length(), markerEnd).trim();
		String text = trimmed.substring(markerEnd + 2).trim();
		return new ReminderLineDisplay(text, loadReminderIcon(iconFileName), centered);
	}

	private ImageIcon loadReminderIcon(String fileName) {
		if (fileName == null || fileName.isBlank()) return null;
		ImageIcon cached = reminderIconCache.get(fileName);
		if (cached != null) return cached;
		try {
			File iconFile = AppPaths.imagesDir().resolve(fileName).toFile();
			if (!iconFile.exists()) return null;
			ImageIcon rawIcon = new ImageIcon(iconFile.getAbsolutePath());
			Image scaled = rawIcon.getImage().getScaledInstance(REMINDER_ICON_SIZE, REMINDER_ICON_SIZE, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(scaled);
			reminderIconCache.put(fileName, icon);
			return icon;
		} catch (Exception ignored) {
			return null;
		}
	}

	private record ReminderLineDisplay(String text, ImageIcon icon, boolean centered) {}
	
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

