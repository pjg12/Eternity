package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Helper frame for informational combat techniques.
 */
public class FrameInform extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String RESTORE_SUMMON_ACTION = "Restore Summon";
	private static final String CLASH_ACTION = "Clash";
	private static final String SPEAR_STRIKE_ACTION = "Spear Strike";
	private static final String HOLY_BEACON_ACTION = "Holy Beacon";
	private static final String EMBED_ACTION = "Embed";
	private static final String[] RESTORE_SUMMON_SPEED_OPTIONS = { "Fast", "Standard", "Slow" };
	private static final String[] RESTORE_SUMMON_MINION_OPTIONS = { "Minor", "Medium", "Major" };

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private final StoreCharData character;
	private final DataAction action;
	private boolean rollUsed;

	private JLabel sourceValueLabel;
	private JLabel affinityValueLabel;
	private JLabel actionTypeValueLabel;
	private JLabel costValueLabel;
	private JTextArea effectsArea;
	private javax.swing.JButton confirmButton;
	private JComboBox<String> restoreSummonSpeedBox;
	private JComboBox<String> restoreSummonMinionBox;
	private JTextField spearStrikeAuraCostField;
	private JTextField embedDamageDealtField;

	FrameInform(FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Inform Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action == null ? new DataAction() : new DataAction(action);
		this.rollUsed = false;

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
		if (isRestoreSummonAction()) {
			restoreSummonSpeedBox = new JComboBox<>(RESTORE_SUMMON_SPEED_OPTIONS);
			restoreSummonSpeedBox.setSelectedItem("Standard");
			restoreSummonSpeedBox.addActionListener(e -> refreshRestoreSummonConfiguration());
			addRow(center, gbc, y++, "Speed", restoreSummonSpeedBox);

			restoreSummonMinionBox = new JComboBox<>(RESTORE_SUMMON_MINION_OPTIONS);
			restoreSummonMinionBox.setSelectedItem("Minor");
			restoreSummonMinionBox.addActionListener(e -> refreshRestoreSummonConfiguration());
			addRow(center, gbc, y++, "Minion Type", restoreSummonMinionBox);
		} else if (isSpearStrikeAction()) {
			spearStrikeAuraCostField = new JTextField("0");
			addRow(center, gbc, y++, "Cancelled Aura Cost", spearStrikeAuraCostField);
		} else if (isEmbedAction()) {
			embedDamageDealtField = new JTextField("0");
			embedDamageDealtField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) { refreshEffectsPreview(); }
				@Override
				public void removeUpdate(DocumentEvent e) { refreshEffectsPreview(); }
				@Override
				public void changedUpdate(DocumentEvent e) { refreshEffectsPreview(); }
			});
			addRow(center, gbc, y++, "Damage Dealt", embedDamageDealtField);
		}

		effectsArea = new JTextArea();
		effectsArea.setEditable(false);
		effectsArea.setLineWrap(true);
		effectsArea.setWrapStyleWord(true);
		effectsArea.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(java.awt.Color.BLACK, 2),
				BorderFactory.createEmptyBorder(2, 4, 2, 4)));
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
		javax.swing.JButton roll = new javax.swing.JButton("Roll");
		roll.addActionListener(e -> copyRollToClipboard());
		confirmButton = new javax.swing.JButton("Confirm");
		confirmButton.addActionListener(e -> confirmPressed());
		confirmButton.setVisible(false);
		footer.add(cancel);
		footer.add(roll);
		footer.add(confirmButton);
		add(footer, BorderLayout.SOUTH);

		sourceValueLabel.setText(safeDisplay(action.getSource()));
		affinityValueLabel.setText(safeDisplay(action.getAffinity()));
		refreshRestoreSummonConfiguration();
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

	private void refreshEffectsPreview() {
		if (effectsArea == null) return;
		StringBuilder text = new StringBuilder();
		appendInformDetails(text);
		effectsArea.setText(text.toString());
		effectsArea.setCaretPosition(0);
	}

	private void appendInformDetails(StringBuilder text) {
		if (text == null) return;
		if (isRestoreSummonAction()) {
			text.append("This ability will restore a minion.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Restore Summon can be used on any minion at any time provided you pay its action and aura cost.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Selected Speed: ").append(getRestoreSummonSpeed())
					.append(" (").append(getRestoreSummonActionTypeDisplay()).append(" action)")
					.append(System.lineSeparator());
			text.append("Selected Minion Type: ").append(getRestoreSummonMinionType())
					.append(System.lineSeparator());
			text.append("Aura Cost: ").append(trimNumber(getRestoreSummonAuraCost()))
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("The restored minion will materialize within the leader's controlled area,")
					.append(System.lineSeparator());
			text.append("a radius of ").append(trimEffectNumber(getRestoreSummonControlledAreaRadius())).append(" ft.")
					.append(" (sqrt(RANGE) * AREA)")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Fast is a move action, Standard is a standard action, and Slow is a full turn action.");
		}
		else if (isPushAction()) {
			text.append("You may move up to ")
					.append(0.5*getDerivedCombatValue("MOVE"))
					.append("ft before starting a Push.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Push Value: ")
					.append(trimEffectNumber(getPushValue()));
			text.append(" (APPLY + CMAN) vs FORT")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Opposing Save - Push Value = Feet Pushed")
					.append(System.lineSeparator());
			text.append("Move opponent up to this distance within 45 degrees.")
					.append(System.lineSeparator());
			text.append("Move self with opponent. You are still subject to opportunity attacks.");							
		}
		else if (isOverrunAction()) {
			text.append("Select a target tile within ")
					.append(getDerivedCombatValue("MOVE"))
					.append("ft. The target tile does not need to be unoccupied.")
					.append(System.lineSeparator());
			text.append("Move directly toward that tile in a straight line.")		
					.append(System.lineSeparator());
			text.append("Each enemy along your path is afforded a REF save to stop you.")		
					.append(System.lineSeparator());
			text.append("Overrun Value: ")
					.append(trimEffectNumber(getPushValue()));
			text.append(" (APPLY + CMAN) vs REF")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("If an enemy succeeds on their save, you stop before entering their tile.")
					.append(System.lineSeparator());
			text.append("If an enemy fails on their save,")
					.append(System.lineSeparator());
			text.append("displace the enemy 5ft orthagonally to your path and continue your overrun.")
					.append(System.lineSeparator());
			text.append("If there are no adjacent tiles, the enemy is knocked prone instead.")
					.append(System.lineSeparator());	
			text.append("An enemy can choose to allow you through their tile.");							
		}
		else if (isClashAction()) {
			text.append("Use this interrupt when an ally is the victim of an attack.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Clash costs 1 R2 and 1 REACT.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("If used successfully, the target of the attack is switched to you.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("You must be within stepping distance of the ally being attacked.");
		}
		else if (isSpearStrikeAction()) {
			text.append("Use this interrupt when an enemy is performing an aura ability within step range of you.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Spear Strike costs 1 R1 and 1 REACT.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("You move into striking range and the triggering action is cancelled.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("You and the target each lose aura equal to half of the cancelled action's aura cost.")
					.append(System.lineSeparator());
			text.append("Current self aura loss: ").append(trimNumber(getSpearStrikeAuraLoss()));
		}
		else if (isHolyBeaconAction()) {
			text.append("Use this interrupt in response to any action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Holy Beacon costs 1 Faith.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("All allies within your base range instantly heal ")
					.append(trimNumber(getHolyBeaconHealingAmount()))
					.append(" HP.")
					.append(System.lineSeparator());
			text.append("Formula: 1/2 CTL + class level")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("All enemies within your base range are afflicted with a curse that reduces ATK by ")
					.append(trimNumber(getHolyBeaconAttackPenalty()))
					.append(" until the beginning of your next turn.")
					.append(System.lineSeparator());
			text.append("Range: ").append(getRange() <= 0 ? "Melee" : (getRange() + " ft"));
		}
		else if (isEmbedAction()) {
			text.append("Use this interrupt when you deal damage with an attack.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Embed costs 1 Convection.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Enter the damage dealt by the triggering attack to determine the Embed Value.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Embed Value: ").append(trimEffectNumber(getEmbedValue()))
					.append(System.lineSeparator());
			text.append("Formula: (BDMG * CLVL / 20) + 5% of damage dealt")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Embed applies a Wound debuff for 4 turns.")
					.append(System.lineSeparator());
			text.append("At the beginning of each of the target's turns, it suffers true damage equal to the Embed Value.");
		}
	}

	private void confirmPressed() {
		if (combatFrame != null) {
			DataAction resolvedAction = buildResolvedActionForConfirm();
			if (resolvedAction == null) {
				return;
			}
			if (!combatFrame.finishActionUse(resolvedAction)) {
				return;
			}
		}
		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
		}
		setVisible(false);
		dispose();
	}

	private void copyRollToClipboard() {
		if (character == null) return;
		rollUsed = true;
		if (confirmButton != null) {
			confirmButton.setVisible(true);
			confirmButton.getParent().revalidate();
			confirmButton.getParent().repaint();
		}
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
		tempString += getCharName() + " --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|" + colorString2 + " --#leftSub|";
		tempString += getActionTitle() + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		tempString += " --+|Range: " + (getRange() <= 0 ? "Melee" : (getRange() + " ft"));
		if (isRestoreSummonAction()) {
			tempString += " --+|Action Type: " + sanitizeForMacro(getRestoreSummonActionTypeDisplay());
			tempString += " --+|Aura Cost: " + trimNumber(getRestoreSummonAuraCost());
			tempString += " --+|Minion Type: " + sanitizeForMacro(getRestoreSummonMinionType());
			tempString += " --+|Controlled Area Radius: " + trimEffectNumber(getRestoreSummonControlledAreaRadius()) + " ft";
		}
		if (isPushAction()) {
			tempString += " --+|Push Value: " + trimEffectNumber(getPushValue());
			tempString += "[br]&nbsp;&nbsp; vs FORT";
		}
		if (isOverrunAction()) {
			tempString += " --+|Overrun Value: " + trimEffectNumber(getPushValue());
			tempString += "[br]&nbsp;&nbsp; vs REF";
		}
		if (isSpearStrikeAction()) {
			tempString += " --+|Cancelled Aura Cost: " + trimNumber(getSpearStrikeCancelledAuraCost());
			tempString += " --+|Aura Lost: " + trimNumber(getSpearStrikeAuraLoss());
			tempString += " --+|Effect: Move into striking range and cancel the triggering aura action";
		}
		if (isHolyBeaconAction()) {
			tempString += " --+|Healing: " + trimNumber(getHolyBeaconHealingAmount());
			tempString += " --+|Curse: -" + trimNumber(getHolyBeaconAttackPenalty()) + " ATK until the beginning of your next turn";
			tempString += " --+|Status Code:[br]&nbsp;&nbsp;" + buildHolyBeaconStatusCode();
		}
		if (isEmbedAction()) {
			Double damageDealt = parseEmbedDamageDealt();
			if (damageDealt == null) {
				return;
			}
			tempString += " --+|Damage Dealt: " + trimNumber(damageDealt);
			tempString += " --+|Embed Value: " + trimEffectNumber(getEmbedValue());
			tempString += " --+|Wound Debuff: 4 turns";
			tempString += " --+|Status Code:[br]&nbsp;&nbsp;" + buildEmbedStatusCode();
		}
		tempString += " }}";

		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private boolean isPushAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase("Push");
	}

	private boolean isRestoreSummonAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(RESTORE_SUMMON_ACTION);
	}

	private boolean isClashAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(CLASH_ACTION);
	}

	private boolean isSpearStrikeAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(SPEAR_STRIKE_ACTION);
	}

	private boolean isHolyBeaconAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(HOLY_BEACON_ACTION);
	}

	private boolean isEmbedAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(EMBED_ACTION);
	}

	private boolean isOverrunAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase("Overrun");
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

	private int getDerivedCombatValue(String key) {
		CharAttributes a = attrs();
		if (a == null || key == null) return 0;
		return (int)Math.round(Math.max(0.0, a.calcStatusValue(key)));
	}

	private CharAttributes attrs() {
		return character != null ? character.getAttributes() : null;
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

	private double getPushValue() {
		double cmanValue = character == null ? 0.0 : character.getEffectiveCombatManeuverValue(action == null ? null : action.getName());
		return resolveCharacterStatValue("APP") + cmanValue;
	}

	private String getActionTitle() {
		return action != null && action.getName() != null && !action.getName().isBlank()
				? action.getName()
				: "Inform";
	}

	private String getCharName() {
		CharIdentity identity = character == null ? null : character.getIdentity();
		return identity != null && identity.getName() != null ? identity.getName() : "Character";
	}

	private String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) <= 0.0001) {
			return Integer.toString((int)Math.round(value));
		}
		return Double.toString(Math.round(value * 100.0) / 100.0);
	}

	private String trimEffectNumber(double value) {
		double rounded = Math.floor(value * 1000.0) / 1000.0;
		if (Math.abs(rounded - Math.rint(rounded)) <= 0.0001) {
			return Integer.toString((int)Math.round(rounded));
		}
		String text = String.format(java.util.Locale.ROOT, "%.3f", rounded);
		while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	private void refreshRestoreSummonConfiguration() {
		if (actionTypeValueLabel != null) {
			actionTypeValueLabel.setText(isRestoreSummonAction() ? getRestoreSummonActionTypeDisplay() : safeDisplay(action.getActionType()));
		}
		if (costValueLabel != null) {
			if (isRestoreSummonAction()) {
				costValueLabel.setText("Aura " + trimNumber(getRestoreSummonAuraCost()));
			} else if (isSpearStrikeAction()) {
				costValueLabel.setText(buildSpearStrikeCostDisplay());
			} else {
				costValueLabel.setText(buildCostDisplay(action));
			}
		}
		refreshEffectsPreview();
	}

	private DataAction buildResolvedActionForConfirm() {
		if (isSpearStrikeAction()) {
			Double cancelledAuraCost = parseSpearStrikeCancelledAuraCost();
			if (cancelledAuraCost == null) {
				return null;
			}
			DataAction resolved = new DataAction(action);
			ArrayList<DataAction.CostPair> costs = new ArrayList<>();
			if (action.getCosts() != null) {
				for (DataAction.CostPair cost : action.getCosts()) {
					if (cost == null || cost.getType() == null) continue;
					costs.add(new DataAction.CostPair(cost.getType(), cost.getValue()));
				}
			}
			double auraLoss = Math.max(0.0, cancelledAuraCost * 0.5);
			if (auraLoss > 0.0) {
				costs.add(new DataAction.CostPair("Aura", auraLoss));
			}
			resolved.setCosts(costs);
			return resolved;
		}
		if (!isRestoreSummonAction()) {
			return action;
		}
		DataAction resolved = new DataAction(action);
		String speed = getRestoreSummonSpeed();
		ArrayList<DataAction.CostPair> costs = new ArrayList<>();
		costs.add(new DataAction.CostPair("Aura", getRestoreSummonAuraCost()));
		if ("Fast".equalsIgnoreCase(speed)) {
			resolved.setActionType("Move");
		} else if ("Standard".equalsIgnoreCase(speed)) {
			resolved.setActionType("Standard");
		} else {
			resolved.setActionType("Full Turn");
			costs.add(new DataAction.CostPair("StandardAction", 1.0));
			costs.add(new DataAction.CostPair("MoveAction", 1.0));
		}
		resolved.setCosts(costs);
		return resolved;
	}

	private String getRestoreSummonSpeed() {
		Object selected = restoreSummonSpeedBox == null ? null : restoreSummonSpeedBox.getSelectedItem();
		return selected == null ? "Standard" : selected.toString().trim();
	}

	private String getRestoreSummonMinionType() {
		Object selected = restoreSummonMinionBox == null ? null : restoreSummonMinionBox.getSelectedItem();
		return selected == null ? "Minor" : selected.toString().trim();
	}

	private String getRestoreSummonActionTypeDisplay() {
		String speed = getRestoreSummonSpeed();
		if ("Fast".equalsIgnoreCase(speed)) return "Move";
		if ("Slow".equalsIgnoreCase(speed)) return "Full Turn";
		return "Standard";
	}

	private double getRestoreSummonAuraCost() {
		double base = character != null && character.getIdentity() != null ? Math.max(0, character.getIdentity().getLevel()) : 0;
		return base * getRestoreSummonSpeedMultiplier() * getRestoreSummonMinionMultiplier();
	}

	private double getRestoreSummonSpeedMultiplier() {
		String speed = getRestoreSummonSpeed();
		if ("Fast".equalsIgnoreCase(speed)) return 4.0;
		if ("Slow".equalsIgnoreCase(speed)) return 1.0;
		return 2.0;
	}

	private double getRestoreSummonMinionMultiplier() {
		String minionType = getRestoreSummonMinionType();
		if ("Major".equalsIgnoreCase(minionType)) return 4.0;
		if ("Medium".equalsIgnoreCase(minionType)) return 2.0;
		return 1.0;
	}

	private double getRestoreSummonControlledAreaRadius() {
		double range = Math.max(0.0, getDerivedCombatValue("RANGE"));
		double area = Math.max(0.0, resolveCharacterStatValue("AREA"));
		return Math.sqrt(range) * area;
	}

	private String buildSpearStrikeCostDisplay() {
		String baseCost = buildCostDisplay(action);
		double auraLoss = getSpearStrikeAuraLoss();
		if (auraLoss <= 0.0) {
			return baseCost;
		}
		return baseCost + ", Aura " + trimNumber(auraLoss);
	}

	private double getSpearStrikeCancelledAuraCost() {
		Double parsed = parseSpearStrikeCancelledAuraCost();
		return parsed == null ? 0.0 : parsed;
	}

	private double getSpearStrikeAuraLoss() {
		return Math.max(0.0, getSpearStrikeCancelledAuraCost() * 0.5);
	}

	private double getHolyBeaconHealingAmount() {
		double control = Math.max(0.0, resolveCharacterStatValue("CTL"));
		double classLevel = character == null ? 0.0 : Math.max(0, character.getLevel());
		return (0.5 * control) + classLevel;
	}

	private double getHolyBeaconAttackPenalty() {
		return Math.max(0.0, resolveCharacterStatValue("CTL"));
	}

	private double getEmbedValue() {
		double baseDamage = Math.max(0.0, resolveCharacterStatValue("BDMG"));
		double classLevel = character == null ? 0.0 : Math.max(0, character.getLevel());
		double damageDealt = getEmbedDamageDealt();
		return (baseDamage * classLevel / 20.0) + (damageDealt * 0.05);
	}

	private String buildHolyBeaconStatusCode() {
		double penalty = getHolyBeaconAttackPenalty();
		if (penalty <= 0.0) return "";
		return "ENEMY_AOE"
				+ "_NAME:" + sanitizeForMacro(HOLY_BEACON_ACTION)
				+ "_DUR:Turn:1"
				+ "_" + StatusCodeParser.getPreferredAttributeAlias("ATK") + formatPackedStatusSeverity(-penalty)
				+ "_DESC:" + sanitizeForMacro("Reduce ATK by " + trimNumber(penalty) + " until the beginning of your next turn.");
	}

	private String buildEmbedStatusCode() {
		double embedValue = getEmbedValue();
		if (embedValue <= 0.0) return "";
		return "ENEMY_SINGLE"
				+ "_NAME:" + sanitizeForMacro(EMBED_ACTION)
				+ "_DUR:Turn:4"
				+ "_HOTHP:" + formatPackedStatusSeverity(-embedValue)
				+ "_DESC:" + sanitizeForMacro("Wound Debuff. At the beginning of each turn for 4 turns, suffer true damage equal to " + trimEffectNumber(embedValue) + ".");
	}

	private String formatPackedStatusSeverity(double severity) {
		String sign = severity >= 0.0 ? "+" : "-";
		return sign + trimNumber(Math.abs(severity));
	}

	private Double parseSpearStrikeCancelledAuraCost() {
		String text = spearStrikeAuraCostField == null ? "0" : spearStrikeAuraCostField.getText();
		String trimmed = text == null ? "" : text.trim();
		if (trimmed.isBlank()) {
			return 0.0;
		}
		try {
			double value = Double.parseDouble(trimmed);
			if (value < 0.0) {
				javax.swing.JOptionPane.showMessageDialog(
						this,
						"Cancelled Aura Cost must be zero or greater.",
						SPEAR_STRIKE_ACTION,
						javax.swing.JOptionPane.WARNING_MESSAGE);
				return null;
			}
			return value;
		} catch (NumberFormatException ex) {
			javax.swing.JOptionPane.showMessageDialog(
					this,
					"Cancelled Aura Cost must be a valid number.",
					SPEAR_STRIKE_ACTION,
					javax.swing.JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}

	private double getEmbedDamageDealt() {
		Double parsed = parseEmbedDamageDealt(false);
		return parsed == null ? 0.0 : parsed;
	}

	private Double parseEmbedDamageDealt() {
		return parseEmbedDamageDealt(true);
	}

	private Double parseEmbedDamageDealt(boolean showDialogs) {
		String text = embedDamageDealtField == null ? "0" : embedDamageDealtField.getText();
		String trimmed = text == null ? "" : text.trim();
		if (trimmed.isBlank()) {
			return 0.0;
		}
		try {
			double value = Double.parseDouble(trimmed);
			if (value < 0.0) {
				if (showDialogs && isShowing()) {
					javax.swing.JOptionPane.showMessageDialog(
							this,
							"Damage Dealt must be zero or greater.",
							EMBED_ACTION,
							javax.swing.JOptionPane.WARNING_MESSAGE);
				}
				return null;
			}
			return value;
		} catch (NumberFormatException ex) {
			if (showDialogs && isShowing()) {
				javax.swing.JOptionPane.showMessageDialog(
						this,
						"Damage Dealt must be a valid number.",
						EMBED_ACTION,
						javax.swing.JOptionPane.WARNING_MESSAGE);
			}
			return null;
		}
	}

	private String sanitizeForMacro(String value) {
		if (value == null) return "";
		return value.replace("{", "").replace("}", "").trim();
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

	private String safeDisplay(String value) {
		return value == null || value.isBlank() ? "None" : value;
	}
}
