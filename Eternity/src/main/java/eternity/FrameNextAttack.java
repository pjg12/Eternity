package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Helper frame for techniques that apply to the next resolved attack.
 */
public class FrameNextAttack extends JFrame {
	private static final long serialVersionUID = 1L;

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private final StoreCharData character;
	private final DataAction action;

	private JComboBox<Integer> alSelect;
	private JLabel sourceValueLabel;
	private JLabel affinityValueLabel;
	private JLabel actionTypeValueLabel;
	private JLabel costValueLabel;
	private JTextArea effectsArea;

	FrameNextAttack(FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Next Attack Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action == null ? new DataAction() : new DataAction(action);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(560, 420);
		setLocationRelativeTo(combatFrame);
		setResizable(false);
		setLayout(new BorderLayout());

		buildUi();
		refreshEffectsPreview();
	}

	private void buildUi() {
		JPanel center = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 10, 6, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int y = 0;
		JLabel title = new JLabel(action.getName(), SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		center.add(title, gbc);

		gbc.gridwidth = 1;
		sourceValueLabel = new JLabel();
		addRow(center, gbc, y++, "Source", sourceValueLabel);
		affinityValueLabel = new JLabel();
		addRow(center, gbc, y++, "Affinity", affinityValueLabel);
		actionTypeValueLabel = new JLabel();
		addRow(center, gbc, y++, "Action Type", actionTypeValueLabel);
		costValueLabel = new JLabel();
		addRow(center, gbc, y++, "Cost", costValueLabel);

		if (isAuraSourceAction()) {
			alSelect = new JComboBox<>();
			populateAlSelect();
			alSelect.addActionListener(e -> refreshEffectsPreview());
			addRow(center, gbc, y++, "AL", alSelect);
		}

		effectsArea = new JTextArea();
		effectsArea.setEditable(false);
		effectsArea.setLineWrap(true);
		effectsArea.setWrapStyleWord(true);
		JScrollPane effectsPane = new JScrollPane(effectsArea);
		effectsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		center.add(effectsPane, gbc);
		add(center, BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		javax.swing.JButton cancel = new javax.swing.JButton("Cancel");
		cancel.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		javax.swing.JButton confirm = new javax.swing.JButton("Confirm");
		confirm.addActionListener(e -> confirmPressed());
		footer.add(cancel);
		footer.add(confirm);
		add(footer, BorderLayout.SOUTH);

		sourceValueLabel.setText(safeDisplay(action.getSource()));
		affinityValueLabel.setText(safeDisplay(action.getAffinity()));
		actionTypeValueLabel.setText(safeDisplay(action.getActionType()));
		costValueLabel.setText(buildCostDisplay(action));
	}

	private void addRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, java.awt.Component field) {
		gbc.gridy = y;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(labelText), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		panel.add(field, gbc);
	}

	private void populateAlSelect() {
		if (alSelect == null) return;
		alSelect.removeAllItems();
		int maxRank = Math.max(1, getTechniqueRank());
		for (int i = 1; i <= maxRank; i++) {
			alSelect.addItem(i);
		}
		int defaultAl = Math.max(1, action.getAl());
		alSelect.setSelectedItem(Math.min(defaultAl, maxRank));
	}

	private void refreshEffectsPreview() {
		if (effectsArea == null) return;
		List<DataStatus> previewStatuses = buildPendingStatuses();
		StringBuilder text = new StringBuilder();
		text.append("Pending effects applied until the next resolved attack.");
		text.append(System.lineSeparator()).append(System.lineSeparator());

		if (previewStatuses.isEmpty()) {
			text.append("No configured next-attack modifiers.");
		} else {
			for (DataStatus status : previewStatuses) {
				text.append(formatStatusEffect(status)).append(System.lineSeparator());
			}
		}

		effectsArea.setText(text.toString());
		effectsArea.setCaretPosition(0);
	}

	private void confirmPressed() {
		List<DataStatus> statuses = buildPendingStatuses();
		if (statuses.isEmpty()) {
			statuses = List.of(buildMarkerStatus());
		}

		for (DataStatus status : statuses) {
			if (combatFrame == null || !combatFrame.applyBuiltStatus(status)) {
				JOptionPane.showMessageDialog(this, "Unable to apply next-attack status.", "Status Failed", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		if (combatFrame != null) {
			combatFrame.finishActionUse(action.getActionType());
		}
		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
		}
		setVisible(false);
		dispose();
	}

	private List<DataStatus> buildPendingStatuses() {
		ArrayList<DataStatus> statuses = new ArrayList<>();
		Map<String, Double> accumulatedEffects = new LinkedHashMap<>();
		for (DataAction.ModifierKey modifier : action.getModifierKey()) {
			accumulatePendingEffect(accumulatedEffects, modifier);
		}
		for (Map.Entry<String, Double> entry : accumulatedEffects.entrySet()) {
			addPendingStatus(statuses, entry.getKey(), entry.getValue(), describeAttribute(entry.getKey()));
		}
		return statuses;
	}

	private void addPendingStatus(List<DataStatus> statuses, String attribute, double severity, String label) {
		if (statuses == null || attribute == null) return;
		double roundedSeverity = roundDownToThreeDecimals(severity);
		if (Math.abs(roundedSeverity) <= 0.0001) return;

		DataStatus status = new DataStatus();
		status.setName(action.getName() + " [" + label + "]");
		status.setAffinity(safeDisplay(action.getAffinity()));
		status.setDescription("Pending next-attack effect from " + action.getName());
		status.setAttribute(attribute);
		status.setSeverity(roundedSeverity);
		status.setDurationType("Next Attack");
		status.setDuration(1);
		statuses.add(status);
	}

	private DataStatus buildMarkerStatus() {
		DataStatus status = new DataStatus();
		status.setName(action.getName() + " [Pending]");
		status.setAffinity(safeDisplay(action.getAffinity()));
		status.setDescription("Pending next-attack marker for " + action.getName());
		status.setAttribute("BATK");
		status.setSeverity(0.0);
		status.setDurationType("Next Attack");
		status.setDuration(1);
		return status;
	}

	private int getSelectedAl() {
		if (alSelect == null || alSelect.getSelectedItem() == null) {
			return Math.max(1, action.getAl());
		}
		Object selected = alSelect.getSelectedItem();
		if (selected instanceof Integer value) {
			return Math.max(1, value);
		}
		return Math.max(1, action.getAl());
	}

	private int getTechniqueRank() {
		if (character == null || character.getTraining() == null || action == null) return 0;

		DataTraining byName = character.getTraining().getTrainingByName(action.getName());
		if (byName != null) {
			return Math.max(0, byName.getRank());
		}

		for (DataTraining training : character.getTraining().getAllTraining()) {
			if (training == null || training.getRank() < 1) continue;
			for (Integer grantId : training.getGrant()) {
				if (grantId != null && grantId == action.getId()) {
					return Math.max(0, training.getRank());
				}
			}
		}

		return 0;
	}

	private boolean isAuraSourceAction() {
		return action != null
				&& action.getSource() != null
				&& action.getSource().equalsIgnoreCase("Aura");
	}

	private String buildCostDisplay(DataAction dataAction) {
		if (dataAction == null || dataAction.getCosts() == null || dataAction.getCosts().isEmpty()) {
			return "None";
		}
		ArrayList<String> values = new ArrayList<>();
		for (DataAction.CostPair cost : dataAction.getCosts()) {
			if (cost == null || cost.getType() == null) continue;
			values.add(cost.getType() + " " + trimNumber(cost.getValue()));
		}
		return values.isEmpty() ? "None" : String.join(", ", values);
	}

	private String formatStatusEffect(DataStatus status) {
		if (status == null) return "";
		return describeAttribute(status.getAttribute()) + ": " + formatSignedNumber(status.getSeverity());
	}

	private String describeAttribute(String attribute) {
		if (attribute == null) return "Effect";
		return switch (attribute.toUpperCase()) {
			case "BATK" -> "Attack";
			case "BBDMG" -> "Base Damage";
			case "BTDMG" -> "Total Damage";
			case "BRANGE" -> "Range";
			case "MBDMG" -> "Base Damage Multiplier";
			case "MTDMG" -> "Total Damage Multiplier";
			default -> attribute;
		};
	}

	private String formatSignedNumber(double value) {
		String prefix = value >= 0 ? "+" : "-";
		return prefix + trimEffectNumber(Math.abs(value));
	}

	private String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) <= 0.0001) {
			return Integer.toString((int)Math.round(value));
		}
		return Double.toString(Math.round(value * 100.0) / 100.0);
	}

	private void accumulatePendingEffect(Map<String, Double> accumulatedEffects, DataAction.ModifierKey modifier) {
		if (accumulatedEffects == null || modifier == null || modifier.getAttribute() == null) return;
		double effectValue;
		try {
			effectValue = evaluateModifierValue(modifier.getOperator());
		} catch (IllegalArgumentException ignored) {
			return;
		}
		if (Math.abs(effectValue) <= 0.0001) return;

		String attribute = modifier.getAttribute().trim().toUpperCase();
		switch (attribute) {
			case "ATK" -> mergeEffect(accumulatedEffects, "BATK", effectValue);
			case "APP" -> mergeEffect(accumulatedEffects, "BAPP", effectValue);
			case "BDMG" -> mergeEffect(accumulatedEffects, "BBDMG", effectValue);
			case "TDMG" -> mergeEffect(accumulatedEffects, "BTDMG", effectValue);
			case "BHEAL" -> mergeEffect(accumulatedEffects, "BBHEAL", effectValue);
			case "THEAL" -> mergeEffect(accumulatedEffects, "BTHEAL", effectValue);
			case "RANGED", "RANGE" -> mergeEffect(accumulatedEffects, "BRANGE", effectValue);
			case "DMGMULTI" -> {
				mergeEffect(accumulatedEffects, "MBDMG", effectValue);
				mergeEffect(accumulatedEffects, "MTDMG", effectValue);
			}
			case "HEALMULTI" -> {
				mergeEffect(accumulatedEffects, "MBHEAL", effectValue);
				mergeEffect(accumulatedEffects, "MTHEAL", effectValue);
			}
			default -> {
				// Unsupported modifier target for next-attack status generation.
			}
		}
	}

	private void mergeEffect(Map<String, Double> accumulatedEffects, String attribute, double value) {
		if (accumulatedEffects == null || attribute == null) return;
		accumulatedEffects.merge(attribute, value, Double::sum);
	}

	private double evaluateModifierValue(String operatorExpression) {
		if (operatorExpression == null || operatorExpression.isBlank()) return 0.0;
		String trimmed = operatorExpression.trim();
		char operator = trimmed.charAt(0);
		String expression = trimmed;
		if (operator == '+' || operator == '-' || operator == '*' || operator == '/' || operator == '=') {
			expression = trimmed.substring(1).trim();
		} else {
			operator = '+';
		}
		if (expression.isBlank()) return 0.0;

		double value = new ModifierExpressionParser(expression).parse();
		return operator == '-' ? -value : value;
	}

	private double roundDownToThreeDecimals(double value) {
		double scaled = value * 1000.0;
		return Math.floor(scaled) / 1000.0;
	}

	private String trimEffectNumber(double value) {
		double rounded = roundDownToThreeDecimals(value);
		if (Math.abs(rounded - Math.rint(rounded)) <= 0.0001) {
			return Integer.toString((int)Math.round(rounded));
		}
		String text = String.format(java.util.Locale.ROOT, "%.3f", rounded);
		while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	private double resolveExpressionIdentifier(String token) {
		if (token == null || token.isBlank()) return 0.0;
		String normalized = token.trim().toUpperCase();
		return switch (normalized) {
			case "AL" -> getSelectedAl();
			case "ATK" -> 0.0;
			case "APP" -> 0.0;
			case "BDMG" -> 0.0;
			case "TDMG" -> 0.0;
			case "DMGMULTI" -> 0.0;
			case "RANGED", "RANGE" -> 0.0;
			default -> resolveCharacterStatValue(normalized);
		};
	}

	private double resolveCharacterStatValue(String key) {
		if (character == null) return 0.0;
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

	private String safeDisplay(String value) {
		return value == null || value.isBlank() ? "None" : value;
	}
}
