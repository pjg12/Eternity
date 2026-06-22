package eternity;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class FrameHeal extends JFrame {
	private static final long serialVersionUID = 1L;

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private StoreCharData character;
	private DataAction action;
	private JComboBox<Integer> alSelect;
	private JComboBox<String> rangeSelect;
	private boolean actionResolved;
	private boolean healRollUsed;

	private final JPanel mainPanel = new JPanel(null);
	private final JLabel headerL = new JLabel();
	private final JLabel[] labels = new JLabel[7];
	private final JTextField[] textFields = new JTextField[5];
	private final JSpinner resultField = new JSpinner(new SpinnerNumberModel(0.0, -9999.0, 9999.0, 1.0));
	private final JButton[] buttons = new JButton[3];

	FrameHeal(FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Heal Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action;
		this.actionResolved = false;
		this.healRollUsed = false;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(550, 400);
		setLocationRelativeTo(null);
		setResizable(false);

		add(mainPanel, BorderLayout.CENTER);
		initComponents();
		showHealStage();
	}

	private void initComponents() {
		headerL.setBounds(25, 20, 500, 30);
		headerL.setHorizontalAlignment(JLabel.CENTER);
		headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 18f));
		headerL.setVisible(false);
		mainPanel.add(headerL);

		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel();
			labels[i].setVisible(false);
			mainPanel.add(labels[i]);
		}

		for (int i = 0; i < textFields.length; i++) {
			textFields[i] = new JTextField();
			textFields[i].setVisible(false);
			mainPanel.add(textFields[i]);
		}

		resultField.setVisible(false);
		mainPanel.add(resultField);

		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton();
			buttons[i].setVisible(false);
			mainPanel.add(buttons[i]);
		}
	}

	private void showHealStage() {
		boolean meleeRangeSelected = isMeleeRangeSelected();
		clearUiState();
		syncActionFromCurrentAl();
		healRollUsed = false;

		headerL.setText(action == null ? "Heal Helper" : action.getName());
		headerL.setVisible(true);

		labels[0].setBounds(25, 80, 100, 20);
		labels[0].setText("Affinity");
		labels[0].setVisible(true);
		textFields[0].setBounds(25, 103, 100, 22);
		textFields[0].setText(action == null ? "" : safeText(action.getAffinity()));
		textFields[0].setEditable(false);
		textFields[0].setVisible(true);

		labels[1].setBounds(145, 80, 100, 20);
		labels[1].setText("Base Healing");
		labels[1].setVisible(true);
		textFields[1].setBounds(145, 103, 100, 22);
		textFields[1].setText(formatNumber(getBaseHeal()));
		textFields[1].setEditable(false);
		textFields[1].setVisible(true);

		labels[2].setBounds(265, 80, 100, 20);
		labels[2].setText("Multiplier");
		labels[2].setVisible(true);
		textFields[2].setBounds(265, 103, 100, 22);
		textFields[2].setText(formatNumber(getHealMultiplier()));
		textFields[2].setEditable(false);
		textFields[2].setVisible(true);

		labels[3].setBounds(385, 80, 100, 20);
		labels[3].setText("Total Healing");
		labels[3].setVisible(true);
		textFields[3].setBounds(385, 103, 100, 22);
		textFields[3].setText(formatNumber(getTotalHeal()));
		textFields[3].setEditable(false);
		textFields[3].setVisible(true);

		labels[4].setBounds(25, 150, 100, 20);
		labels[4].setText("Range");
		labels[4].setVisible(true);
		ensureRangeSelect();
		populateRangeSelect(meleeRangeSelected);
		rangeSelect.setBounds(25, 173, 100, 22);
		rangeSelect.setVisible(true);

		if (isAuraTechniqueAction()) {
			labels[5].setBounds(145, 150, 100, 20);
			labels[5].setText("AL");
			labels[5].setVisible(true);
			ensureAlSelect();
			populateAlSelect();
			alSelect.setBounds(145, 173, 100, 22);
			alSelect.setVisible(true);
		}

		labels[6].setBounds(220, 280, 100, 20);
		labels[6].setText("Result");
		labels[6].setVisible(true);
		resultField.setBounds(220, 305, 100, 20);
		resultField.setValue(0.0);
		resultField.setVisible(true);

		buttons[0].setBounds(25, 250, 145, 20);
		buttons[0].setText("Roll");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> copyRollToClipboard());

		buttons[1].setBounds(25, 325, 145, 20);
		buttons[1].setText("Cancel");
		buttons[1].setVisible(true);
		buttons[1].addActionListener(e -> cancelPressed());

		buttons[2].setBounds(365, 325, 145, 20);
		buttons[2].setText("Confirm");
		buttons[2].setVisible(true);
		buttons[2].addActionListener(e -> confirmPressed());
	}

	private void clearUiState() {
		headerL.setVisible(false);
		for (JLabel label : labels) {
			if (label == null) continue;
			label.setText("");
			label.setVisible(false);
		}
		for (JTextField field : textFields) {
			if (field == null) continue;
			field.setText("");
			field.setEditable(true);
			field.setVisible(false);
		}
		resultField.setValue(0.0);
		resultField.setVisible(false);
		for (JButton button : buttons) {
			if (button == null) continue;
			for (ActionListener listener : button.getActionListeners()) {
				button.removeActionListener(listener);
			}
			button.setText("");
			button.setVisible(false);
		}
		if (alSelect != null) {
			for (ActionListener listener : alSelect.getActionListeners()) {
				alSelect.removeActionListener(listener);
			}
			alSelect.setVisible(false);
			if (alSelect.getParent() != null) {
				alSelect.getParent().remove(alSelect);
			}
		}
		if (rangeSelect != null) {
			for (ActionListener listener : rangeSelect.getActionListeners()) {
				rangeSelect.removeActionListener(listener);
			}
			rangeSelect.setVisible(false);
			if (rangeSelect.getParent() != null) {
				rangeSelect.getParent().remove(rangeSelect);
			}
		}
	}

	private void cancelPressed() {
		setVisible(false);
		dispose();
	}

	private void confirmPressed() {
		if (!healRollUsed) {
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
		setVisible(false);
		dispose();
	}

	private void finishCombatActionIfNeeded() {
		if (actionResolved) return;
		if (combatFrame != null && action != null && action.getActionType() != null) {
			if (!combatFrame.resolveAttackAction(action)) {
				return;
			}
		}
		actionResolved = true;
	}

	private void copyRollToClipboard() {
		if (character == null) return;
		healRollUsed = true;

		DataColor raceColor = getDisplayColor();
		String colorString1 = String.format("#%02x%02x%02x", raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x", raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String tempString = "!scriptcard {{ --#titleCardBackground|" + colorString1 + " --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|" + colorString1;
		tempString += " --#titleCardBottomBorder|4px solid #000000; --#title|";
		tempString += getCharName() + " --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|" + colorString2 + " --#leftSub|";
		String actionSubtitle = action == null ? "Heal" : action.getName();
		if (alSelect != null && alSelect.getSelectedItem() != null) {
			actionSubtitle += " AL: " + alSelect.getSelectedItem();
		}
		tempString += actionSubtitle + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		tempString += " --+|Range: " + getSelectedRangeLabel();
		tempString += buildPercentRollBlock("Heal", "RawPercentRoll", "RawPercentBonus", "RawPercentCount", "HealPercentRoll", "FinalRawPercentRoll", "PercentRoll");
		tempString += " --=HealRoll|[$PercentRoll] * " + formatNumber(getBaseHeal()) + " * " + formatNumber(getHealMultiplier()) + " + " + formatNumber(getTotalHeal()) + " {FLOOR}";
		tempString += " --+|Heal Percent Roll: [$FinalRawPercentRoll] x 5% = [$PercentRoll] [br]&nbsp;&nbsp;";
		tempString += "Healing Roll: [$HealRoll] = [br]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ([$PercentRoll] x " + formatNumber(getBaseHeal()) + ") x " + formatNumber(getHealMultiplier()) + " + " + formatNumber(getTotalHeal()) + " }}";

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

	private String buildPercentRollDisplayBlock(String labelPrefix, String labelSuffix, String rawVar, String countVar,
			String rolledPercentVar, boolean showNormalRow) {
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

	private void ensureAlSelect() {
		if (alSelect == null) {
			alSelect = new JComboBox<Integer>();
		}
		if (alSelect.getParent() == null) {
			mainPanel.add(alSelect);
		}
	}

	private void ensureRangeSelect() {
		if (rangeSelect == null) {
			rangeSelect = new JComboBox<String>();
		}
		if (rangeSelect.getParent() == null) {
			mainPanel.add(rangeSelect);
		}
	}

	private void populateRangeSelect(boolean meleeSelected) {
		if (rangeSelect == null) return;
		rangeSelect.removeAllItems();
		rangeSelect.addItem(getDefaultRangeLabel());
		rangeSelect.addItem("Melee");
		rangeSelect.setSelectedItem(meleeSelected ? "Melee" : getDefaultRangeLabel());
		rangeSelect.addActionListener(e -> rangeSelectionChanged());
	}

	private void rangeSelectionChanged() {
		if (textFields[2] != null) {
			textFields[2].setText(formatNumber(getHealMultiplier()));
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
		int selectedAl = Math.max(1, Math.min(Math.max(0, action.getAl()), maxRank));
		action.setAl(selectedAl);
		alSelect.setSelectedItem(selectedAl);
		alSelect.addActionListener(e -> alSelectionChanged());
	}

	private void alSelectionChanged() {
		if (alSelect == null || action == null) return;
		Object selected = alSelect.getSelectedItem();
		if (!(selected instanceof Integer value)) return;
		action.setAl(value);
		action.update();
		showHealStage();
	}

	private void syncActionFromCurrentAl() {
		if (action == null) return;
		if (isAuraTechniqueAction()) {
			int maxRank = getTechniqueRank();
			int selectedAl = Math.max(1, action.getAl());
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

	private boolean isAuraTechniqueAction() {
		return action != null
				&& action.getSource() != null
				&& "Aura".equalsIgnoreCase(action.getSource());
	}

	private String getDefaultRangeLabel() {
		int range = getRange();
		return range <= 0 ? "Melee" : (range + " ft");
	}

	private String getSelectedRangeLabel() {
		if (rangeSelect == null || rangeSelect.getSelectedItem() == null) {
			return getDefaultRangeLabel();
		}
		return safeText(String.valueOf(rangeSelect.getSelectedItem()));
	}

	private boolean isMeleeRangeSelected() {
		return "Melee".equalsIgnoreCase(getSelectedRangeLabel());
	}

	private int getRange() {
		if (action == null) return 0;
		if (action.getRanged() > 0) {
			return (int) Math.round(getDerivedStatusValue("RANGE"));
		}
		return action.getRanged();
	}

	private double getBaseHeal() {
		return Math.max(0.0, getDerivedStatusValue("BHEAL") + evaluateActionModifierTotal("BHEAL"));
	}

	private double getTotalHeal() {
		return Math.max(0.0, getDerivedStatusValue("THEAL") + evaluateActionModifierTotal("THEAL"));
	}

	private double getHealMultiplier() {
		double meleeBonus = isMeleeRangeSelected() ? 0.5 : 0.0;
		return Math.max(0.0, 1.0 + evaluateActionModifierTotal("HEALMULTI") + meleeBonus);
	}

	private double evaluateActionModifierTotal(String targetAttribute) {
		if (action == null || action.getModifierKey() == null || targetAttribute == null) return 0.0;
		double result = 0.0;
		for (DataAction.ModifierKey modifier : action.getModifierKey()) {
			if (modifier == null || modifier.getAttribute() == null) continue;
			if (!targetAttribute.equalsIgnoreCase(modifier.getAttribute())) continue;
			result = applyModifier(result, modifier.getOperator());
		}
		return result;
	}

	private double applyModifier(double currentValue, String operatorExpression) {
		String normalizedOperator = operatorExpression == null ? "+" : operatorExpression.trim().toUpperCase();
		char mathOperator = resolveMathOperator(normalizedOperator);
		double operand = resolveOperand(normalizedOperator, mathOperator);
		return switch (mathOperator) {
			case '=' -> operand;
			case '-' -> currentValue - operand;
			case '*' -> currentValue * operand;
			case '/' -> operand == 0.0 ? currentValue : currentValue / operand;
			default -> currentValue + operand;
		};
	}

	private char resolveMathOperator(String operator) {
		if (operator == null || operator.isBlank()) return '+';
		char first = operator.trim().charAt(0);
		return switch (first) {
			case '=', '*', '/', '-', '+' -> first;
			default -> '+';
		};
	}

	private double resolveOperand(String operatorExpression, char mathOperator) {
		if (operatorExpression == null || operatorExpression.isBlank()) {
			return defaultOperand(mathOperator);
		}
		String operandExpression = stripLeadingOperator(operatorExpression.trim(), mathOperator);
		if (operandExpression.isBlank()) {
			return defaultOperand(mathOperator);
		}
		try {
			return evaluateModifierExpression(operandExpression);
		} catch (IllegalArgumentException ignored) {
			return defaultOperand(mathOperator);
		}
	}

	private String stripLeadingOperator(String operatorExpression, char mathOperator) {
		if (operatorExpression == null) return "";
		String trimmed = operatorExpression.trim();
		if (!trimmed.isEmpty() && trimmed.charAt(0) == mathOperator) {
			return trimmed.substring(1).trim();
		}
		return trimmed;
	}

	private double evaluateModifierExpression(String expression) {
		return new ModifierExpressionParser(expression).parse();
	}

	private double resolveExpressionIdentifier(String token) {
		if (token == null || token.isBlank()) return 0.0;
		String normalized = token.trim().toUpperCase();
		return switch (normalized) {
			case "AL" -> action == null || character == null ? 0.0 : character.getEffectiveTechniqueAl(action);
			case "CL" -> getClassLevel();
			case "BHEAL" -> getDerivedStatusValue("BHEAL");
			case "THEAL" -> getDerivedStatusValue("THEAL");
			case "HEALMULTI" -> 0.0;
			default -> resolveCharacterStatValue(normalized);
		};
	}

	private double getClassLevel() {
		if (character == null || character.getIdentity() == null) return 0.0;
		return Math.max(0, character.getIdentity().getLevel());
	}

	private double resolveCharacterStatValue(String key) {
		if (character == null || key == null || key.isBlank()) return 0.0;
		if (character.getAttributes() != null && isKnownAttributeKey(key)) {
			return character.getAttributes().calcStatusValue(key);
		}
		double resourceValue = character.calcResourceValue(key);
		return resourceValue >= 0.0 ? resourceValue : 0.0;
	}

	private boolean isKnownAttributeKey(String key) {
		return containsIgnoreCase(CharAttributes.getAttributeKeys(), key)
				|| containsIgnoreCase(CharAttributes.getDefenseKeys(), key)
				|| containsIgnoreCase(CharAttributes.getDamageTypeKeys(), key)
				|| containsIgnoreCase(CharAttributes.getCombatKeys(), key)
				|| containsIgnoreCase(CharAttributes.getSecondaryKeys(), key)
				|| containsIgnoreCase(CharAttributes.getDamageKeys(), key);
	}

	private boolean containsIgnoreCase(String[] values, String target) {
		if (values == null || target == null) return false;
		for (String value : values) {
			if (value != null && value.equalsIgnoreCase(target)) {
				return true;
			}
		}
		return false;
	}

	private double defaultOperand(char mathOperator) {
		return switch (mathOperator) {
			case '*', '/' -> 1.0;
			default -> 0.0;
		};
	}

	private double getDerivedStatusValue(String key) {
		if (character == null || character.getAttributes() == null || key == null) return 0.0;
		return Math.max(0.0, character.getAttributes().calcStatusValue(key));
	}

	private String getCharName() {
		CharIdentity identity = character == null ? null : character.getIdentity();
		return identity != null && identity.getName() != null ? identity.getName() : "Character";
	}

	private DataColor getDisplayColor() {
		StoreRuleManager dq = new StoreRuleManager();
		String race = Optional.ofNullable(character)
				.map(StoreCharData::getIdentity)
				.map(CharIdentity::getRace)
				.orElse("Default");
		DataColor color = dq.getColorByTitle(race);
		if (color != null) return color;
		return new DataColor("Default", 0, 0, 0, 255, 255, 255);
	}

	private String safeText(String value) {
		return value == null ? "" : value;
	}

	private String formatNumber(double value) {
		double normalized = Math.abs(value) < 0.0005 ? 0.0 : value;
		if (Math.rint(normalized) == normalized) {
			return String.valueOf((int) normalized);
		}
		return String.format("%.3f", normalized).replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private final class ModifierExpressionParser {
		private final String expression;
		private int index = 0;

		private ModifierExpressionParser(String expression) {
			this.expression = expression == null ? "" : expression;
		}

		private double parse() {
			double value = parseExpression();
			skipWhitespace();
			if (index < expression.length()) {
				throw new IllegalArgumentException("Unexpected token in modifier expression");
			}
			return value;
		}

		private double parseExpression() {
			double value = parseTerm();
			while (true) {
				skipWhitespace();
				if (match('+')) {
					value += parseTerm();
				} else if (match('-')) {
					value -= parseTerm();
				} else {
					return value;
				}
			}
		}

		private double parseTerm() {
			double value = parseFactor();
			while (true) {
				skipWhitespace();
				if (match('*')) {
					value *= parseFactor();
				} else if (match('/')) {
					double divisor = parseFactor();
					if (divisor != 0.0) {
						value /= divisor;
					}
				} else {
					return value;
				}
			}
		}

		private double parseFactor() {
			skipWhitespace();
			if (match('+')) return parseFactor();
			if (match('-')) return -parseFactor();
			if (match('(')) {
				double value = parseExpression();
				if (!match(')')) {
					throw new IllegalArgumentException("Unclosed modifier expression group");
				}
				return value;
			}
			if (index >= expression.length()) {
				throw new IllegalArgumentException("Unexpected end of modifier expression");
			}
			char current = expression.charAt(index);
			if (Character.isDigit(current) || current == '.') {
				return parseNumber();
			}
			if (Character.isLetter(current)) {
				return parseIdentifier();
			}
			throw new IllegalArgumentException("Unsupported modifier expression token");
		}

		private double parseNumber() {
			int start = index;
			while (index < expression.length()) {
				char current = expression.charAt(index);
				if (!Character.isDigit(current) && current != '.') break;
				index++;
			}
			return Double.parseDouble(expression.substring(start, index));
		}

		private double parseIdentifier() {
			int start = index;
			while (index < expression.length()) {
				char current = expression.charAt(index);
				if (!Character.isLetterOrDigit(current) && current != '_') break;
				index++;
			}
			String identifier = expression.substring(start, index);
			return resolveExpressionIdentifier(identifier);
		}

		private boolean match(char expected) {
			skipWhitespace();
			if (index < expression.length() && expression.charAt(index) == expected) {
				index++;
				return true;
			}
			return false;
		}

		private void skipWhitespace() {
			while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
				index++;
			}
		}
	}
}
