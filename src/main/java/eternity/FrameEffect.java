package eternity;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Helper for Mitigation / Support / Apply techniques: shows resolved effects
 * and copies a ScriptCard with summary + pasteable status code.
 */
public class FrameEffect extends JFrame {
	private static final long serialVersionUID = 1L;

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private final StoreCharData character;
	private final DataAction action;
	private boolean shareUsed;
	private boolean actionResolved;

	private JLabel sourceValueLabel;
	private JLabel affinityValueLabel;
	private JLabel actionTypeValueLabel;
	private JLabel costValueLabel;
	private JComboBox<Integer> alSelect;
	private JTextArea effectsArea;
	private JButton shareButton;
	private JButton confirmButton;

	FrameEffect(FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Effect Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action == null ? new DataAction() : new DataAction(action);
		if (this.action.getCharacter() == null && character != null) {
			this.action.setCharacter(character);
		}
		this.shareUsed = false;
		this.actionResolved = false;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(560, 480);
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
		JLabel title = new JLabel(action.getName() == null ? "Effect" : action.getName(), SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
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

		if (isAuraTechniqueAction() || needsAlSelect()) {
			alSelect = new JComboBox<>();
			populateAlSelect();
			alSelect.addActionListener(e -> {
				syncAlToAction();
				refreshEffectsPreview();
			});
			addRow(center, gbc, y++, "AL", alSelect);
		}

		effectsArea = new JTextArea(8, 40);
		effectsArea.setEditable(false);
		effectsArea.setLineWrap(true);
		effectsArea.setWrapStyleWord(true);
		effectsArea.setFont(effectsArea.getFont().deriveFont(13f));
		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		center.add(new JScrollPane(effectsArea), gbc);

		JPanel footer = new JPanel();
		shareButton = new JButton("Share");
		shareButton.addActionListener(e -> copyMacroToClipboard());
		confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(e -> confirmPressed());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		footer.add(shareButton);
		footer.add(confirmButton);
		footer.add(cancelButton);

		add(center, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);

		sourceValueLabel.setText(safe(action.getSource()));
		affinityValueLabel.setText(safe(action.getAffinity()));
		actionTypeValueLabel.setText(safe(action.getActionType()));
		costValueLabel.setText(buildCostDisplay());
	}

	private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, java.awt.Component field) {
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		panel.add(new JLabel(label), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(field, gbc);
	}

	private boolean isAuraTechniqueAction() {
		return action.getSource() != null && "Aura".equalsIgnoreCase(action.getSource().trim());
	}

	private boolean needsAlSelect() {
		List<DataAction.ModifierKey> modifiers = action.getModifierKey();
		if (modifiers == null) return false;
		for (DataAction.ModifierKey entry : modifiers) {
			if (entry == null || entry.getOperator() == null) continue;
			String op = entry.getOperator().toUpperCase(java.util.Locale.ROOT);
			if (op.contains("AL")) return true;
		}
		return false;
	}

	private void populateAlSelect() {
		if (alSelect == null) return;
		for (ActionListener listener : alSelect.getActionListeners()) {
			alSelect.removeActionListener(listener);
		}
		alSelect.removeAllItems();
		int maxRank = getTechniqueRank();
		if (maxRank <= 0) maxRank = 1;
		for (int i = 1; i <= maxRank; i++) {
			alSelect.addItem(i);
		}
		int selected = Math.max(1, action.getAl());
		selected = Math.min(selected, maxRank);
		alSelect.setSelectedItem(selected);
		syncAlToAction();
	}

	private void syncAlToAction() {
		if (action == null) return;
		int al = 1;
		if (alSelect != null && alSelect.getSelectedItem() instanceof Integer value) {
			al = Math.max(1, value);
		} else if (action.getAl() > 0) {
			al = action.getAl();
		}
		action.setAl(al);
	}

	private int getTechniqueRank() {
		if (character == null || character.getTraining() == null || action == null) return 1;
		DataTraining direct = character.getTraining().getTrainingById(action.getId());
		if (direct != null && direct.getRank() > 0) return direct.getRank();

		StoreRuleManager rules = new StoreRuleManager();
		for (DataTraining learned : character.getTraining().getAllTraining()) {
			if (learned == null) continue;
			for (Integer grantId : learned.getGrant()) {
				if (grantId != null && grantId == action.getId() && learned.getRank() > 0) {
					return learned.getRank();
				}
			}
			DataTraining rule = rules.getTrainingById(learned.getId());
			if (rule != null) {
				for (Integer grantId : rule.getGrant()) {
					if (grantId != null && grantId == action.getId() && learned.getRank() > 0) {
						return learned.getRank();
					}
				}
			}
			if (action.getName() != null && learned.getName() != null
					&& action.getName().equalsIgnoreCase(learned.getName())
					&& learned.getRank() > 0) {
				return learned.getRank();
			}
		}
		return Math.max(1, action.getAl() > 0 ? action.getAl() : 1);
	}

	private void refreshEffectsPreview() {
		syncAlToAction();
		AuraEffectMacroBuilder.EffectMacroResult result = AuraEffectMacroBuilder.build(action);
		StringBuilder text = new StringBuilder();
		text.append(result.summaryText);
		if (!result.statusCode.isBlank()) {
			text.append("\n\nStatus Code:\n").append(result.statusCode);
		}
		effectsArea.setText(text.toString());
		effectsArea.setCaretPosition(0);
	}

	private void copyMacroToClipboard() {
		if (character == null) return;
		syncAlToAction();
		shareUsed = true;

		DataColor raceColor = null;
		StoreRuleManager dataQuery = new StoreRuleManager();
		if (character.getIdentity() != null) {
			raceColor = dataQuery.getColorByTitle(character.getIdentity().getRace());
		}
		if (raceColor == null) {
			raceColor = new DataColor("Default", 0, 0, 0, 255, 255, 255);
		}
		String colorString1 = String.format("#%02x%02x%02x",
				raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x",
				raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());

		AuraEffectMacroBuilder.EffectMacroResult result = AuraEffectMacroBuilder.build(action);
		String charName = character.getIdentity() != null && character.getIdentity().getName() != null
				? character.getIdentity().getName()
				: "Character";
		String subtitle = action.getName() == null ? "Effect" : action.getName();
		if (alSelect != null && alSelect.getSelectedItem() != null) {
			subtitle += " AL: " + alSelect.getSelectedItem();
		}

		StringBuilder macro = new StringBuilder();
		macro.append("!scriptcard {{ --#titleCardBackground|").append(colorString1)
				.append(" --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|").append(colorString1)
				.append(" --#titleCardBottomBorder|4px solid #000000; --#title|").append(sanitizeMacro(charName))
				.append(" --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|").append(colorString2)
				.append(" --#leftSub|").append(sanitizeMacro(subtitle))
				.append(" --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|").append(colorString1)
				.append(" --#evenRowFontColor|").append(colorString2)
				.append(" --#oddRowBackground|").append(colorString2)
				.append(" --#oddRowFontColor|").append(colorString1)
				.append(" --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;");

		macro.append(" --+|Category: ").append(sanitizeMacro(safe(action.getCategory())));
		macro.append(" --+|Cost: ").append(sanitizeMacro(buildCostDisplay()));
		if (action.getRanged() > 0) {
			macro.append(" --+|Range: ").append(action.getRanged()).append(" ft");
		} else if (action.getRanged() == 0) {
			macro.append(" --+|Range: Self / Touch");
		}

		for (String line : result.summaryLines) {
			if (line == null || line.isBlank()) continue;
			macro.append(" --+|").append(sanitizeMacro(line));
		}
		macro.append(" --+|Status Code:[br]&nbsp;&nbsp;").append(sanitizeMacro(result.statusCode));
		macro.append(" }}");

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(macro.toString()), null);
	}

	private void confirmPressed() {
		if (!shareUsed) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"You have not clicked Share. Do you want to continue anyway?",
					"Proceed Without Share",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}
		if (!finishCombatActionIfNeeded()) {
			return;
		}
		setVisible(false);
		dispose();
	}

	private boolean finishCombatActionIfNeeded() {
		if (actionResolved) return true;
		syncAlToAction();
		if (combatFrame != null && action != null && action.getActionType() != null) {
			if (!combatFrame.resolveAttackAction(action)) {
				return false;
			}
		}
		actionResolved = true;
		return true;
	}

	private String buildCostDisplay() {
		if (action == null || action.getCosts() == null || action.getCosts().isEmpty()) {
			return "None";
		}
		StringBuilder text = new StringBuilder();
		for (DataAction.CostPair cost : action.getCosts()) {
			if (cost == null || cost.getType() == null) continue;
			if (text.length() > 0) text.append(", ");
			String type = cost.getType().trim();
			if ("None".equalsIgnoreCase(type)) {
				text.append("None");
			} else {
				text.append(type);
				if (Math.abs(cost.getValue()) > 0.0001) {
					text.append(" ").append(trimNumber(cost.getValue()));
				}
			}
		}
		return text.length() == 0 ? "None" : text.toString();
	}

	private static String safe(String value) {
		return value == null || value.isBlank() ? "—" : value;
	}

	private static String sanitizeMacro(String value) {
		if (value == null) return "";
		return value.replace("|", "/");
	}

	private static String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return Long.toString(Math.round(value));
		}
		String text = String.format(java.util.Locale.ROOT, "%.2f", value);
		if (text.indexOf('.') >= 0) {
			text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
		}
		return text;
	}
}
