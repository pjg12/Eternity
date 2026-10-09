package eternity;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import websocket.Roll20WebSocketServer;
import websocket.events.Roll20Token;

/**
 * Helper frame for techniques that apply to the next resolved attack.
 */
public class FrameNextAttack extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String STEALTH_STRIKE_ACTION = "Stealth Strike";
	private static final String ANGER_STRIKE_ACTION = "Anger Strike";
	private static final String CALM_STRIKE_ACTION = "Calm Strike";
	private static final String ORDER_STRIKE_ACTION = "Order Strike";
	private static final String CHAOS_STRIKE_ACTION = "Chaos Strike";
	private static final String FLASH_MOB_ACTION = "Flash Mob";
	private static final String SHIVER_ACTION = "Shiver";
	private static final String CROSSFIRE_ACTION = "Crossfire";
	private static final String CROSSFIRE_MARKER = "CROSSFIRE";
	private static final String INCANT_ACTION = "Incant";
	private static final String INCANT_MARKER = "INCANT";
	private static final String CHAIN_ACTION = "Chain";
	private static final String CHAIN_MARKER = "CHAIN";

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
	private JLabel targetHeaderL;
	private JList<Roll20Token> targetList;
	private DefaultListModel<Roll20Token> sourceTargetModel;
	private final DefaultListModel<Roll20Token> visibleTargetModel = new DefaultListModel<>();
	private String selectedTargetId;

	FrameNextAttack(FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Next Attack Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action == null ? new DataAction() : new DataAction(action);
		if (isChargeAction()) {
			this.action.setCosts(List.of(new DataAction.CostPair("None", 0.0)));
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(isChargeAction() ? 810 : 560, 420);
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
			action.setAl(getSelectedAl());
			alSelect.addActionListener(e -> {
				action.setAl(getSelectedAl());
				if (costValueLabel != null) {
					costValueLabel.setText(buildCostDisplay(action));
				}
				refreshEffectsPreview();
			});
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
		if (isChargeAction()) {
			add(buildChargeTargetPanel(), BorderLayout.EAST);
		}

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

	private JPanel buildChargeTargetPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 12));
		panel.setPreferredSize(new Dimension(250, 420));

		targetHeaderL = new JLabel("Charge Targets");
		targetHeaderL.setFont(targetHeaderL.getFont().deriveFont(Font.BOLD, 14f));
		panel.add(targetHeaderL, BorderLayout.NORTH);

		try {
			Roll20WebSocketServer server = Roll20WebSocketServer.getSharedServer();
			sourceTargetModel = server.getTargetModel();
			if (character != null && character.getIdentity() != null) {
				server.setLoadedCharacter(
						character.getIdentity().getName(),
						character.getIdentity().getNickname());
			}
			if (server.getSelectedTarget() != null) {
				selectedTargetId = server.getSelectedTarget().id;
			}
			server.requestTokenSnapshot();
		} catch (Exception ex) {
			sourceTargetModel = new DefaultListModel<>();
			targetHeaderL.setText("Roll20 Targets Unavailable");
			targetHeaderL.setToolTipText(ex.getMessage());
		}

		targetList = new JList<>(visibleTargetModel);
		targetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		targetList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (component instanceof JLabel && value instanceof Roll20Token token) {
					((JLabel) component).setText(formatChargeTargetLabel(token));
				}
				return component;
			}
		});
		targetList.addListSelectionListener(event -> {
			if (event.getValueIsAdjusting()) return;
			Roll20Token selected = targetList.getSelectedValue();
			selectedTargetId = selected == null ? null : selected.id;
			try {
				Roll20WebSocketServer server = Roll20WebSocketServer.getSharedServer();
				server.setSelectedTarget(selected);
				if (selected != null && selected.id != null) {
					server.sendSelectToken(selected.id);
				}
			} catch (Exception ignored) {
				// Keep the local selection even if the bridge is unavailable.
			}
			refreshEffectsPreview();
		});
		sourceTargetModel.addListDataListener(new ListDataListener() {
			@Override public void intervalAdded(ListDataEvent e) { refreshChargeTargets(); }
			@Override public void intervalRemoved(ListDataEvent e) { refreshChargeTargets(); }
			@Override public void contentsChanged(ListDataEvent e) { refreshChargeTargets(); }
		});
		refreshChargeTargets();

		JScrollPane targetPane = new JScrollPane(targetList);
		targetPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		targetPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(targetPane, BorderLayout.CENTER);
		return panel;
	}

	private void refreshChargeTargets() {
		if (visibleTargetModel == null || sourceTargetModel == null || targetHeaderL == null) {
			return;
		}
		Roll20WebSocketServer server = null;
		try {
			server = Roll20WebSocketServer.getSharedServer();
		} catch (Exception ignored) {
			server = null;
		}
		Roll20Token self = server == null ? null : server.getLoadedCharacterToken();
		boolean canFilterRange = self != null && self.left != null && self.top != null;
		double minFeet = getChargeMinimumDistance();
		double maxFeet = getChargeMoveDistance();

		if (canFilterRange) {
			targetHeaderL.setText("Charge Targets");
			targetHeaderL.setToolTipText("Enemy tokens between " + trimNumber(minFeet) + "ft and "
					+ trimNumber(maxFeet) + "ft with a clear path.");
		} else {
			targetHeaderL.setText("Enemy Targets");
			targetHeaderL.setToolTipText("The loaded character token was not found, so charge range cannot be applied.");
		}

		visibleTargetModel.clear();
		for (int i = 0; i < sourceTargetModel.size(); i++) {
			Roll20Token token = sourceTargetModel.get(i);
			if (token == null || token.loadedCharacter || token.isAlly()) {
				continue;
			}
			if (canFilterRange && !server.isWithinRangeOfLoadedCharacter(token, minFeet, maxFeet)) {
				continue;
			}
			if (canFilterRange && !server.isChargePathClearToLoadedCharacter(token)) {
				continue;
			}
			visibleTargetModel.addElement(token);
		}
		restoreChargeTargetSelection();
	}

	private void restoreChargeTargetSelection() {
		if (targetList == null || selectedTargetId == null || selectedTargetId.isBlank()) return;
		for (int i = 0; i < visibleTargetModel.size(); i++) {
			Roll20Token token = visibleTargetModel.get(i);
			if (token != null && selectedTargetId.equals(token.id)) {
				if (targetList.getSelectedIndex() != i) {
					targetList.setSelectedIndex(i);
				}
				return;
			}
		}
	}

	private Roll20Token getSelectedChargeTarget() {
		if (targetList != null && targetList.getSelectedValue() != null) {
			Roll20Token selected = targetList.getSelectedValue();
			if (isUsableChargeTarget(selected)) {
				return selected;
			}
		}
		try {
			Roll20Token selected = Roll20WebSocketServer.getSharedServer().getSelectedTarget();
			if (isUsableChargeTarget(selected)) {
				return selected;
			}
		} catch (Exception ignored) {
			return null;
		}
		return null;
	}

	private boolean isUsableChargeTarget(Roll20Token token) {
		if (token == null || token.isAlly()) {
			return false;
		}
		try {
			Roll20WebSocketServer server = Roll20WebSocketServer.getSharedServer();
			Roll20Token self = server.getLoadedCharacterToken();
			if (self == null || self.left == null || self.top == null) {
				return true;
			}
			return server.isChargePathClearToLoadedCharacter(token);
		} catch (Exception ignored) {
			return true;
		}
	}

	private String formatChargeTargetLabel(Roll20Token token) {
		if (token == null) return "";
		String displayName = token.name == null || token.name.isBlank() ? token.id : token.name;
		try {
			double distance = Roll20WebSocketServer.getSharedServer().getDistanceFeetFromLoadedCharacter(token);
			if (!Double.isNaN(distance)) {
				return displayName + " (" + trimNumber(distance) + " ft)";
			}
		} catch (Exception ignored) {
			// Fall through to the name only.
		}
		return displayName;
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
		appendChargeInstructions(text);
		appendTemperanceInstructions(text);
		appendFlashMobInstructions(text);
		appendShiverInstructions(text);
		appendCrossfireInstructions(text);
		appendIncantInstructions(text);
		appendChainInstructions(text);
		text.append("Pending effects applied until the next resolved attack.");
		text.append(System.lineSeparator()).append(System.lineSeparator());

		if (previewStatuses.isEmpty()) {
			text.append("No configured next-attack modifiers.");
		} else {
			for (DataStatus status : previewStatuses) {
				text.append(formatStatusEffect(status)).append(System.lineSeparator());
			}
		}

		if (isTechnicalAction(previewStatuses)) {
			appendTechnicalDescription(text);
		}

		effectsArea.setText(text.toString());
		effectsArea.setCaretPosition(0);
	}

	private void appendChargeInstructions(StringBuilder text) {
		if (text == null || !isChargeAction()) return;
		text.append("You must move a minimum of ")
				.append(getChargeMinimumDistance())
				.append("ft and a maximum of ")
				.append(getChargeMoveDistance())
				.append("ft.")
				.append(System.lineSeparator());
		text.append("You must move in a direct path toward a visible enemy without deviating more than 45\u00B0.")
				.append(System.lineSeparator());
		text.append("You will have reduced AC during your charge.")
				.append(System.lineSeparator())
				.append(System.lineSeparator());
		if (character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("War Charge")) {
			text.append("You are not subject to attacks of opportunity during a Charge attack.")
					.append(System.lineSeparator());
		}
		text.append(System.lineSeparator());
	}

	private void appendTemperanceInstructions(StringBuilder text) {
		if (text == null || !isTemperanceAbility()) return;
		boolean entropyAbility = isEntropyAbility();
		text.append(entropyAbility
						? "Only one Entropy ability can be activated per turn."
						: "Only one Temperance ability can be activated per turn.")
				.append(System.lineSeparator());
		if (isAngerStrikeAction() || isChaosStrikeAction()) {
			text.append("If this effect is not consumed before the end of your turn, it is lost and the resource cost is not refunded.")
					.append(System.lineSeparator());
		} else {
			text.append("If this effect is not consumed before the end of your turn, it is lost.")
					.append(System.lineSeparator());
		}
		text.append(System.lineSeparator());
	}

	private void appendFlashMobInstructions(StringBuilder text) {
		if (text == null || !isFlashMobAbility()) return;
		text.append("Costs 1 Aggressive Tactic and 1 Strategic Tactic.")
				.append(System.lineSeparator());
		text.append("Only one Tactics ability can be activated per turn.")
				.append(System.lineSeparator());
		text.append("Modifies your next attack this turn with the Technical modifier (no direct attack bonus).")
				.append(System.lineSeparator());
		text.append("If unused before end of turn, Flash Mob is lost and you recover one of the two Tactics spent.")
				.append(System.lineSeparator());
		text.append("For each TP, select a unique minion and an adjacent unoccupied tile to move/create a 1 HP copy.")
				.append(System.lineSeparator())
				.append(System.lineSeparator());
	}

	private void appendShiverInstructions(StringBuilder text) {
		if (text == null || !isShiverAction()) return;
		text.append("Costs 1 Aptitude.")
				.append(System.lineSeparator());
		text.append("Activated as a free action or interrupt when declaring an attack.")
				.append(System.lineSeparator());
		text.append("Modifies your next attack this turn (+1x CL to attack roll) and grants Technical.")
				.append(System.lineSeparator());
		text.append("If unused before the end of your turn, the effect is lost and the Aptitude cost is not refunded.")
				.append(System.lineSeparator())
				.append(System.lineSeparator());
	}

	private void appendCrossfireInstructions(StringBuilder text) {
		if (text == null || !isCrossfireAction()) return;
		text.append("Crossfire affects your next single-target attack.")
				.append(System.lineSeparator());
		text.append("That target is subject to an attack of opportunity from all allies.")
				.append(System.lineSeparator());
		text.append("Each ally must spend 1 Reaction to attack, and these attacks are not limited to melee.")
				.append(System.lineSeparator())
				.append(System.lineSeparator());
	}

	private void appendIncantInstructions(StringBuilder text) {
		if (text == null || !isIncantAction()) return;
		double severityBonus = getIncantSeverityBonus();
		text.append("Your next Impairment ability gains an additional WILL save.")
				.append(System.lineSeparator());
		text.append("If the target fails that save, the base effect severity is increased by ")
				.append(trimNumber(severityBonus))
				.append(" (10% of DEX).")
				.append(System.lineSeparator())
				.append(System.lineSeparator());
	}

	private void appendChainInstructions(StringBuilder text) {
		if (text == null || !isChainAction()) return;
		text.append("Your next Impairment ability gains an additional FORT save.")
				.append(System.lineSeparator());
		text.append("If the target fails that save, the technique is replicated with a -10 APPLY penalty")
				.append(" against an unaffected creature within RANGE.")
				.append(System.lineSeparator());
		text.append("The replicated technique also has this Chain effect.")
				.append(System.lineSeparator())
				.append(System.lineSeparator());
	}

	private boolean isChargeAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase("Charge");
	}

	private double getChargeMoveDistance() {
		double cmanValue = character == null ? 0.0 : character.getEffectiveCombatManeuverValue(action == null ? null : action.getName());
		double moveValue = resolveCharacterStatValue("MOVE") + cmanValue;
		if (character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("War Charge")
				&& character.getIdentity() != null) {
			moveValue += 2.5 * character.getIdentity().getLevel();
		}
		if (hasEnJoustingCharge()) {
			moveValue += 5.0 * getRaceTrainingRank();
		}
		return Math.max(0, moveValue);
	}

	private int getChargeMinimumDistance() {
		return hasEnJoustingCharge() ? 10 : 15;
	}

	private boolean hasEnJoustingCharge() {
		if (character == null || character.getSpecials() == null) return false;
		DataSpecialty racial = character.getSpecials().getRacialSpecialty();
		if (racial == null || racial.getName() == null) return false;
		return racial.getName().equalsIgnoreCase("Jousting Charge (En)");
	}

	private boolean hasJoustingCharge() {
		if (!isChargeAction() || character == null || character.getSpecials() == null) return false;
		DataSpecialty racial = character.getSpecials().getRacialSpecialty();
		if (racial == null || racial.getName() == null) return false;
		return racial.getName().regionMatches(true, 0, "Jousting Charge", 0, "Jousting Charge".length());
	}

	private int getRaceTrainingRank() {
		if (character == null || character.getTraining() == null) return 0;
		DataTraining raceTraining = character.getTraining().getTrainingByName("Race Training");
		return raceTraining == null ? 0 : Math.max(0, raceTraining.getRank());
	}

	private void confirmPressed() {
		if (isChargeAction() && getSelectedChargeTarget() == null) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"No target selected. Continue anyway?",
					"Charge Target",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}

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
			if (isAuraSourceAction()) {
				action.setAl(getSelectedAl());
			}
			if (!combatFrame.finishActionUse(action)) {
				return;
			}
		}
		if (isTemperanceAbility() && character != null && character.getCombat() != null) {
			character.getCombat().setTemperanceAbilityUsedThisTurn(true);
		}
		if (isFlashMobAbility() && character != null && character.getCombat() != null) {
			character.getCombat().setTacticsAbilityUsedThisTurn(true);
			character.getCombat().setFlashMobPendingRefund(true);
		}
		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
		}
		setVisible(false);
		dispose();
	}

	private List<DataStatus> buildPendingStatuses() {
		ArrayList<DataStatus> statuses = new ArrayList<>();
		if (isSmiteAction()) {
			statuses.add(buildMarkerStatus("SMITE", "Smite"));
		}
		if (isStealthStrikeAction()) {
			statuses.add(buildMarkerStatus("STEALTHSTRIKE", "Stealth Strike"));
		}
		if (isFlashMobAbility()) {
			statuses.add(buildMarkerStatus("FLASHMOB", "Flash Mob"));
		}
		if (isCrossfireAction()) {
			statuses.add(buildMarkerStatus(CROSSFIRE_MARKER, "Crossfire"));
		}
		if (isIncantAction()) {
			statuses.add(buildMarkerStatus(INCANT_MARKER, "Incant"));
		}
		if (isChainAction()) {
			statuses.add(buildMarkerStatus(CHAIN_MARKER, "Chain"));
		}
		Map<String, Double> accumulatedEffects = new LinkedHashMap<>();
		for (DataAction.ModifierKey modifier : action.getModifierKey()) {
			accumulatePendingEffect(accumulatedEffects, modifier);
		}
		accumulateChargeRacialEffects(accumulatedEffects);
		if (character != null && character.isOverchargeClassAbility(action)) {
			double bonus = character.getOverchargeDamageMultiplierBonus();
			if (bonus > 0.0) {
				mergeEffect(accumulatedEffects, "MBDMG", bonus);
				mergeEffect(accumulatedEffects, "MTDMG", bonus);
			}
		}
		if (!isStealthStrikeAction() && resolveTechnicalData() != null && !accumulatedEffects.containsKey("TECH")) {
			accumulatedEffects.put("TECH", 1.0);
		}
		for (Map.Entry<String, Double> entry : accumulatedEffects.entrySet()) {
			addPendingStatus(statuses, entry.getKey(), entry.getValue(), describeAttribute(entry.getKey()));
		}
		return statuses;
	}

	private void accumulateChargeRacialEffects(Map<String, Double> accumulatedEffects) {
		if (accumulatedEffects == null || !hasJoustingCharge()) return;
		int raceTrainingRank = getRaceTrainingRank();
		if (raceTrainingRank <= 0) return;
		mergeEffect(accumulatedEffects, "BATK", raceTrainingRank);
		mergeEffect(accumulatedEffects, "BBDMG", raceTrainingRank);
		mergeEffect(accumulatedEffects, "BTDMG", getJoustingChargeTdmgBonus(raceTrainingRank));
	}

	private double getJoustingChargeTdmgBonus(int raceTrainingRank) {
		if (raceTrainingRank <= 0) return 0.0;
		return (getChargeDistanceTraveled() / 5.0) * raceTrainingRank;
	}

	private double getChargeDistanceTraveled() {
		Roll20Token target = getSelectedChargeTarget();
		if (target == null) return 0.0;
		try {
			double distance = Roll20WebSocketServer.getSharedServer().getDistanceFeetFromLoadedCharacter(target);
			return Double.isNaN(distance) ? 0.0 : Math.max(0.0, distance);
		} catch (Exception ignored) {
			return 0.0;
		}
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
		return buildMarkerStatus("BATK", "Pending");
	}

	private DataStatus buildMarkerStatus(String attribute, String label) {
		DataStatus status = new DataStatus();
		status.setName(action.getName() + " [" + label + "]");
		status.setAffinity(safeDisplay(action.getAffinity()));
		status.setDescription("Pending next-attack marker for " + action.getName());
		status.setAttribute(attribute);
		status.setSeverity("BATK".equalsIgnoreCase(attribute) ? 0.0 : 1.0);
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
			if ("None".equalsIgnoreCase(cost.getType())) {
				values.add("None");
				continue;
			}
			double shownValue = character == null
					? cost.getValue()
					: character.getEffectiveActionCostValue(dataAction, cost.getType(), cost.getValue());
			values.add(cost.getType() + " " + trimNumber(shownValue));
		}
		return values.isEmpty() ? "None" : String.join(", ", values);
	}

	private String formatStatusEffect(DataStatus status) {
		if (status == null) return "";
		if ("TECH".equalsIgnoreCase(status.getAttribute())) {
			return "Technical: Yes";
		}
		if ("SMITE".equalsIgnoreCase(status.getAttribute())) {
			return "Smite: The next attack becomes Divine and targets Dodge.";
		}
		if ("STEALTHSTRIKE".equalsIgnoreCase(status.getAttribute())) {
			return "Stealth Strike: Successful attack grants 1 TP, failed WILL save grants 1 TP, then spend TP on rogue follow-up effects.";
		}
		if ("FLASHMOB".equalsIgnoreCase(status.getAttribute())) {
			return "Flash Mob: Next attack is Technical; for each TP, create/move a unique minion copy (1 HP).";
		}
		if (CROSSFIRE_MARKER.equalsIgnoreCase(status.getAttribute())) {
			return "Crossfire: The target is subject to an attack of opportunity from all allies. Each ally spends 1 Reaction, and these attacks are not limited to melee.";
		}
		if (INCANT_MARKER.equalsIgnoreCase(status.getAttribute())) {
			return "Incant: Next Impairment ability provokes a WILL save; on failure, base effect severity +"
					+ trimNumber(getIncantSeverityBonus()) + " (10% of DEX).";
		}
		if (CHAIN_MARKER.equalsIgnoreCase(status.getAttribute())) {
			return "Chain: Next Impairment ability provokes a FORT save; on failure, replicate the technique"
					+ " with APPLY -10 against an unaffected creature within RANGE (replica also Chains).";
		}
		return describeAttribute(status.getAttribute()) + ": " + formatSignedNumber(status.getSeverity());
	}

	private boolean hasTechnicalPendingStatus(List<DataStatus> statuses) {
		if (statuses == null) return false;
		for (DataStatus status : statuses) {
			if (status != null && "TECH".equalsIgnoreCase(status.getAttribute())) {
				return true;
			}
		}
		return false;
	}

	private boolean isTechnicalAction(List<DataStatus> statuses) {
		return hasTechnicalPendingStatus(statuses) || resolveTechnicalData() != null;
	}

	private boolean isSmiteAction() {
		return action != null
				&& action.getName() != null
				&& "Smite".equalsIgnoreCase(action.getName().trim());
	}

	private boolean isStealthStrikeAction() {
		return action != null
				&& action.getName() != null
				&& STEALTH_STRIKE_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean isCrossfireAction() {
		return action != null
				&& action.getName() != null
				&& CROSSFIRE_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean isIncantAction() {
		return action != null
				&& action.getName() != null
				&& INCANT_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean isChainAction() {
		return action != null
				&& action.getName() != null
				&& CHAIN_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private double getIncantSeverityBonus() {
		if (character == null || character.getAttributes() == null) return 0.0;
		return Math.max(0.0, character.getAttributes().calcStatusValue("DEX") * 0.1);
	}

	private boolean isAngerStrikeAction() {
		return action != null
				&& action.getName() != null
				&& ANGER_STRIKE_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean isChaosStrikeAction() {
		return action != null
				&& action.getName() != null
				&& CHAOS_STRIKE_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean isTemperanceAbility() {
		if (action == null || action.getName() == null) return false;
		String actionName = action.getName().trim();
		return ANGER_STRIKE_ACTION.equalsIgnoreCase(actionName)
				|| CALM_STRIKE_ACTION.equalsIgnoreCase(actionName)
				|| ORDER_STRIKE_ACTION.equalsIgnoreCase(actionName)
				|| CHAOS_STRIKE_ACTION.equalsIgnoreCase(actionName);
	}

	private boolean isEntropyAbility() {
		if (action == null || action.getName() == null) return false;
		String actionName = action.getName().trim();
		return ORDER_STRIKE_ACTION.equalsIgnoreCase(actionName)
				|| CHAOS_STRIKE_ACTION.equalsIgnoreCase(actionName);
	}

	private boolean isFlashMobAbility() {
		return action != null
				&& action.getName() != null
				&& FLASH_MOB_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean isShiverAction() {
		return action != null
				&& action.getName() != null
				&& SHIVER_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private void appendTechnicalDescription(StringBuilder text) {
		if (text == null) return;
		text.append(System.lineSeparator())
				.append("The next attack you resolve will be Technical.");

		DataTechnical technical = resolveTechnicalData();
		if (technical == null) return;

		text.append(System.lineSeparator())
				.append("Category: ")
				.append(safeDisplay(technical.getCategory()));

		String save = technical.getSave();
		if (save != null && !save.isBlank() && !"None".equalsIgnoreCase(save.trim())) {
			text.append(System.lineSeparator())
					.append("Save: ")
					.append(save.trim());
		}

		String description = technical.getDescription();
		if (description != null && !description.isBlank()) {
			text.append(System.lineSeparator())
					.append(description.trim());
		}
	}

	private DataTechnical resolveTechnicalData() {
		if (action == null || action.getName() == null || action.getName().isBlank()) return null;
		StoreRuleManager ruleManager = new StoreRuleManager();
		String technicalName = resolveTechnicalNameForAction(action.getName());
		return ruleManager.getTechnicalByName(technicalName);
	}

	private String resolveTechnicalNameForAction(String actionName) {
		if (actionName == null || actionName.isBlank()) return actionName;
		String trimmed = actionName.trim();
		if ("Pulse (Slow)".equalsIgnoreCase(trimmed)) {
			return isCurrentShifterMeleeForm() ? "Pulse (Slow) Melee" : "Pulse (Slow) Ranged";
		}
		if ("Pulse (Steady)".equalsIgnoreCase(trimmed)) {
			return isCurrentShifterMeleeForm() ? "Pulse (Steady) Melee" : "Pulse (Steady) Ranged";
		}
		return trimmed;
	}

	private boolean isCurrentShifterMeleeForm() {
		if (character == null || character.getIdentity() == null) return true;
		if (!"Shifter".equalsIgnoreCase(character.getIdentity().getCharClass())) return true;
		String selectedForm = character.getReminderSelection("Current Form:");
		if (selectedForm == null || selectedForm.isBlank()) return true;
		String normalized = selectedForm.trim().toLowerCase(java.util.Locale.ROOT);
		if (normalized.contains("light") || normalized.contains("ranged")) {
			return false;
		}
		return true;
	}

	private String describeAttribute(String attribute) {
		if (attribute == null) return "Effect";
		return switch (attribute.toUpperCase()) {
			case "BATK" -> "Attack";
			case "BAPP" -> "Application";
			case "BDEF" -> "Defense";
			case "BBDMG" -> "Base Damage";
			case "BTDMG" -> "Total Damage";
			case "BBHEAL" -> "Base Healing";
			case "BTHEAL" -> "Total Healing";
			case "BRANGE" -> "Range";
			case "BCRIT" -> "Critical Increment";
			case "BCRUSH" -> "Crush";
			case "MBDMG" -> "Base Damage Multiplier";
			case "MTDMG" -> "Total Damage Multiplier";
			case "MBHEAL" -> "Base Healing Multiplier";
			case "MTHEAL" -> "Total Healing Multiplier";
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
		String attribute = modifier.getAttribute().trim().toUpperCase();
		if ("TECH".equals(attribute)) {
			mergeEffect(accumulatedEffects, "TECH", 1.0);
			return;
		}
		double effectValue;
		try {
			effectValue = evaluateModifierValue(modifier.getOperator());
		} catch (IllegalArgumentException ignored) {
			return;
		}
		if (Math.abs(effectValue) <= 0.0001) return;
		switch (attribute) {
			case "ATK" -> mergeEffect(accumulatedEffects, "BATK", effectValue);
			case "APP" -> mergeEffect(accumulatedEffects, "BAPP", effectValue);
			case "DEF" -> mergeEffect(accumulatedEffects, "BDEF", effectValue);
			case "BDMG" -> mergeEffect(accumulatedEffects, "BBDMG", effectValue);
			case "TDMG" -> mergeEffect(accumulatedEffects, "BTDMG", effectValue);
			case "BHEAL" -> mergeEffect(accumulatedEffects, "BBHEAL", effectValue);
			case "THEAL" -> mergeEffect(accumulatedEffects, "BTHEAL", effectValue);
			case "RANGED", "RANGE" -> mergeEffect(accumulatedEffects, "BRANGE", effectValue);
			case "CRIT" -> mergeEffect(accumulatedEffects, "BCRIT", effectValue);
			case "CRUSH" -> mergeEffect(accumulatedEffects, "BCRUSH", effectValue);
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
		if (operator == '*' || operator == '/' || operator == '=') {
			expression = trimmed.substring(1).trim();
		}
		if (expression.isBlank()) return 0.0;

		return new ModifierExpressionParser(expression).parse();
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
			case "AL" -> character == null || action == null ? 0.0 : character.getEffectiveTechniqueAl(action.getAffinity(), getSelectedAl());
			case "CL" -> getClassLevel();
			case "BASEHP" -> getLevelBaseHp();
			case "ATK" -> 0.0;
			case "APP" -> 0.0;
			case "BDMG" -> 0.0;
			case "TDMG" -> 0.0;
			case "DMGMULTI" -> 0.0;
			case "RANGED", "RANGE" -> 0.0;
			default -> resolveCharacterStatValue(normalized);
		};
	}

	private double getClassLevel() {
		if (character == null || character.getIdentity() == null) return 0.0;
		return Math.max(0, character.getIdentity().getLevel());
	}

	private double getLevelBaseHp() {
		if (character == null || character.getIdentity() == null) return 0.0;
		DataLevel levelData = new StoreRuleManager().getLevel(character.getIdentity().getLevel());
		return levelData == null ? 0.0 : Math.max(0, levelData.getBaseHP());
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
