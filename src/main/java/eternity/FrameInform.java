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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import eternity.DataTypes.DataAction;
import eternity.DataTypes.DataColor;
import eternity.DataTypes.DataStatus;
import eternity.DataTypes.DataTraining;
import eternity.Minions.MinionCombatState;

/**
 * Helper frame for informational combat techniques.
 */
public class FrameInform extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String RESTORE_SUMMON_ACTION = "Restore Summon";
	private static final String CHARGE_UP_ACTION = "Charge-Up";
	private static final String POSITIONING_JETS_ACTION = "Positioning Jets";
	private static final String OVERCHARGE_ACTION = "Overcharge";
	private static final String DEPLOY_DRONE_ACTION = "Deploy Drone";
	private static final String RECALL_DRONE_ACTION = "Recall Drone";
	private static final String[] DRONE_TYPE_OPTIONS = { "Attack", "Defense" };
	private static final String CLASH_ACTION = "Clash";
	private static final String SPEAR_STRIKE_ACTION = "Spear Strike";
	private static final String HOLY_BEACON_ACTION = "Holy Beacon";
	private static final String EMBED_ACTION = "Embed";
	private static final String GAMBIT_MINION_ACTION = "Gambit (Minion)";
	private static final String GAMBIT_LEADER_ACTION = "Gambit (Leader)";
	private static final String INSPIRE_ACTION = "Inspire";
	private static final String UPLIFT_ACTION = "Uplift";
	private static final String MUSTER_ACTION = "Muster";
	private static final String RALLY_ACTION = "Rally";
	private static final String SWAP_ACTION = "Swap";
	private static final String FORMATION_ACTION = "Formation";
	private static final String BLINK_ACTION = "Blink";
	private static final String ARCANE_ORB_ACTION = "Arcane Orb";
	private static final String ARCANE_ORB_STATUS = "Arcane Orb";
	private static final String ELECTRIC_TETHER_ACTION = "Electric Tether";
	private static final String ELECTRIC_TETHER_STATUS = "Electric Tether";
	private static final double ELECTRIC_TETHER_AURA_COST_PER_AL = 5.0;
	private static final double ELECTRIC_TETHER_RANGE_FACTOR = 0.1;
	private static final int ELECTRIC_TETHER_DURATION_PER_AL = 2;
	private static final double REMEDY_SAVE_BONUS_PER_AL = 2.5;
	private static final int REMEDY_DURATION_PER_AL = 2;
	private static final double REMEDY_AURA_COST_PER_AL = 1.0;
	private static final String TRANSPOSE_SPECIALTY = "Transpose";
	private static final String SWAP_PARTNER_SELF = "Yourself";
	private static final String SWAP_PARTNER_ALLY = "Willing Ally";
	private static final String FORMATION_TARGET_SELF = "Yourself";
	private static final String FORMATION_TARGET_ALLY = "Ally";
	private static final String INSPIRE_HASTEN = "Hasten";
	private static final String INSPIRE_COMPETENCE = "Competence";
	private static final String INSPIRE_FREEDOM = "Freedom";
	private static final String INSPIRE_CLARITY = "Clarity";
	private static final String INSPIRE_FORTUNE = "Fortune";
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
	private JComboBox<String> gambitMinionBox;
	private JTextField gambitEnemyField;
	private JTextField gambitAllyField;
	private JComboBox<String> swapPartnerBox;
	private JTextField swapAllyField;
	private JLabel swapAllyLabel;
	private JComboBox<String> inspireVersionBox;
	private JTextField inspireAllyField;
	private JTextField inspireOriginalDcField;
	private JLabel inspireOriginalDcLabel;
	private JTextField rallyTargetField;
	private JTextField rallyPsychosisField;
	private JComboBox<String> formationTargetBox;
	private JTextField formationAllyField;
	private JLabel formationAllyLabel;
	private JTextField blinkDestinationField;
	private JTextField arcaneOrbLocationField;
	private JComboBox<String> arcaneOrbCornerBox;
	private JLabel arcaneOrbCornerLabel;
	private JComboBox<Integer> electricTetherAlSelect;
	private JTextField electricTetherTargetField;
	private JComboBox<Integer> remedyAlSelect;
	private JTextField remedyTargetField;
	private JComboBox<String> droneTypeBox;
	private JTextField droneTargetField;
	private JPanel droneListPanel;
	private List<PanelCharMinion.SummonedMinionCombatInfo> formationMinionOptions;
	private List<JCheckBox> formationAdjacentBoxes;
	private List<PanelCharMinion.SummonedMinionCombatInfo> gambitMinionOptions;

	FrameInform(FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		super("Inform Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action == null ? new DataAction() : new DataAction(action);
		this.rollUsed = false;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(560, isDronesAction() ? 620 : (isFormationAction() || isElectricTetherAction() || isRemedyAction() ? 540 : 420));
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
		} else if (isGambitMinionAction()) {
			gambitMinionOptions = resolveActingGambitMinionOptions();
			if (gambitMinionOptions.size() != 1) {
				gambitMinionBox = new JComboBox<>(buildGambitMinionOptionLabels(gambitMinionOptions));
				gambitMinionBox.addActionListener(e -> refreshEffectsPreview());
				addRow(center, gbc, y++, "Minion", gambitMinionBox);
			}

			gambitEnemyField = new JTextField();
			attachRefreshListener(gambitEnemyField);
			addRow(center, gbc, y++, "Enemy Target", gambitEnemyField);

			gambitAllyField = new JTextField();
			attachRefreshListener(gambitAllyField);
			addRow(center, gbc, y++, "Target Ally", gambitAllyField);
		} else if (isGambitLeaderAction()) {
			gambitMinionOptions = getSummonedMinionOptions();
			gambitMinionBox = new JComboBox<>(buildGambitMinionOptionLabels(gambitMinionOptions));
			gambitMinionBox.addActionListener(e -> refreshEffectsPreview());
			addRow(center, gbc, y++, "Target Minion", gambitMinionBox);

			gambitEnemyField = new JTextField();
			attachRefreshListener(gambitEnemyField);
			addRow(center, gbc, y++, "Enemy Target", gambitEnemyField);
		} else if (isSwapAction()) {
			gambitMinionOptions = getSummonedMinionOptions();
			gambitMinionBox = new JComboBox<>(buildGambitMinionOptionLabels(gambitMinionOptions));
			gambitMinionBox.addActionListener(e -> refreshEffectsPreview());
			addRow(center, gbc, y++, "Target Minion", gambitMinionBox);

			if (hasTransposeSpecialty()) {
				swapPartnerBox = new JComboBox<>(new String[] { SWAP_PARTNER_SELF, SWAP_PARTNER_ALLY });
				swapPartnerBox.addActionListener(e -> {
					refreshSwapAllyFieldVisibility();
					refreshEffectsPreview();
				});
				addRow(center, gbc, y++, "Swap With", swapPartnerBox);

				swapAllyField = new JTextField();
				attachRefreshListener(swapAllyField);
				gbc.gridx = 0;
				gbc.gridy = y;
				gbc.gridwidth = 1;
				gbc.weightx = 0;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				swapAllyLabel = new JLabel("Willing Ally", SwingConstants.LEFT);
				center.add(swapAllyLabel, gbc);
				gbc.gridx = 1;
				gbc.weightx = 1.0;
				center.add(swapAllyField, gbc);
				y++;
				refreshSwapAllyFieldVisibility();
			}
		} else if (isInspireAction()) {
			List<String> inspireVariants = getOwnedInspireVariants();
			if (inspireVariants.size() > 1) {
				inspireVersionBox = new JComboBox<>(inspireVariants.toArray(new String[0]));
				inspireVersionBox.addActionListener(e -> {
					refreshInspireFreedomFieldVisibility();
					refreshEffectsPreview();
				});
				addRow(center, gbc, y++, "Version", inspireVersionBox);
			}

			inspireAllyField = new JTextField();
			attachRefreshListener(inspireAllyField);
			addRow(center, gbc, y++, "Target Ally", inspireAllyField);

			inspireOriginalDcLabel = new JLabel("Original DC", SwingConstants.LEFT);
			inspireOriginalDcField = new JTextField();
			attachRefreshListener(inspireOriginalDcField);
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			center.add(inspireOriginalDcLabel, gbc);
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			center.add(inspireOriginalDcField, gbc);
			y++;
			refreshInspireFreedomFieldVisibility();
		} else if (isRallyAction()) {
			rallyTargetField = new JTextField();
			attachRefreshListener(rallyTargetField);
			addRow(center, gbc, y++, "Target", rallyTargetField);

			rallyPsychosisField = new JTextField();
			attachRefreshListener(rallyPsychosisField);
			addRow(center, gbc, y++, "Psychosis", rallyPsychosisField);
		} else if (isFormationAction()) {
			formationTargetBox = new JComboBox<>(new String[] { FORMATION_TARGET_SELF, FORMATION_TARGET_ALLY });
			formationTargetBox.addActionListener(e -> {
				refreshFormationAllyFieldVisibility();
				refreshEffectsPreview();
			});
			addRow(center, gbc, y++, "Target", formationTargetBox);

			formationAllyLabel = new JLabel("Ally Name", SwingConstants.LEFT);
			formationAllyField = new JTextField();
			attachRefreshListener(formationAllyField);
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			center.add(formationAllyLabel, gbc);
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			center.add(formationAllyField, gbc);
			y++;
			refreshFormationAllyFieldVisibility();

			formationMinionOptions = getSummonedMinionOptions();
			formationAdjacentBoxes = new ArrayList<>();
			JPanel adjacentPanel = new JPanel();
			adjacentPanel.setLayout(new BoxLayout(adjacentPanel, BoxLayout.Y_AXIS));
			adjacentPanel.setBorder(BorderFactory.createTitledBorder("Adjacent Minions"));
			if (formationMinionOptions.isEmpty()) {
				adjacentPanel.add(new JLabel("No summoned minions available."));
			} else {
				for (PanelCharMinion.SummonedMinionCombatInfo minion : formationMinionOptions) {
					if (minion == null) continue;
					double gambitBonus = applyGambitPercent(minion.attackBonus());
					JCheckBox box = new JCheckBox(minion.displayName()
							+ "  (gambit +" + trimNumber(gambitBonus) + " ATK)");
					box.addActionListener(e -> refreshEffectsPreview());
					formationAdjacentBoxes.add(box);
					adjacentPanel.add(box);
				}
			}
			JScrollPane adjacentPane = new JScrollPane(adjacentPanel);
			adjacentPane.setPreferredSize(new java.awt.Dimension(0, 140));
			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.gridwidth = 2;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.BOTH;
			center.add(adjacentPane, gbc);
			gbc.gridwidth = 1;
			gbc.weighty = 0.0;
		} else if (isBlinkAction()) {
			blinkDestinationField = new JTextField();
			attachRefreshListener(blinkDestinationField);
			addRow(center, gbc, y++, "Destination", blinkDestinationField);
		} else if (isElectricTetherAction()) {
			electricTetherAlSelect = new JComboBox<>();
			populateElectricTetherAlSelect();
			electricTetherAlSelect.addActionListener(e -> {
				syncElectricTetherAlToAction();
				refreshRestoreSummonConfiguration();
			});
			addRow(center, gbc, y++, "Activation Level", electricTetherAlSelect);

			electricTetherTargetField = new JTextField();
			attachRefreshListener(electricTetherTargetField);
			addRow(center, gbc, y++, "Target Creature", electricTetherTargetField);
		} else if (isRemedyAction()) {
			remedyAlSelect = new JComboBox<>();
			populateRemedyAlSelect();
			remedyAlSelect.addActionListener(e -> {
				syncRemedyAlToAction();
				refreshRestoreSummonConfiguration();
			});
			addRow(center, gbc, y++, "Activation Level", remedyAlSelect);

			remedyTargetField = new JTextField();
			attachRefreshListener(remedyTargetField);
			addRow(center, gbc, y++, "Target Ally", remedyTargetField);
		} else if (isDronesAction()) {
			if (isDeployDroneAction()) {
				droneTypeBox = new JComboBox<>(DRONE_TYPE_OPTIONS);
				droneTypeBox.addActionListener(e -> refreshEffectsPreview());
				addRow(center, gbc, y++, "Drone Type", droneTypeBox);

				droneTargetField = new JTextField("Yourself");
				attachRefreshListener(droneTargetField);
				addRow(center, gbc, y++, "Target", droneTargetField);
			}
			droneListPanel = new JPanel();
			droneListPanel.setLayout(new BoxLayout(droneListPanel, BoxLayout.Y_AXIS));
			JScrollPane dronePane = new JScrollPane(droneListPanel);
			dronePane.setPreferredSize(new java.awt.Dimension(0, 150));
			dronePane.setBorder(BorderFactory.createTitledBorder("Controlled Drones"));
			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.gridwidth = 2;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.BOTH;
			center.add(dronePane, gbc);
			gbc.gridwidth = 1;
			refreshDroneListPanel();
		} else if (isArcaneOrbAction()) {
			arcaneOrbLocationField = new JTextField();
			attachRefreshListener(arcaneOrbLocationField);
			addRow(center, gbc, y++, "Target Location", arcaneOrbLocationField);

			arcaneOrbCornerLabel = new JLabel("Even-Center Corner");
			arcaneOrbCornerBox = new JComboBox<>(new String[] {
					"Northwest", "Northeast", "Southwest", "Southeast"
			});
			arcaneOrbCornerBox.addActionListener(e -> refreshEffectsPreview());
			gbc.gridy = y;
			gbc.gridx = 0;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			center.add(arcaneOrbCornerLabel, gbc);
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			center.add(arcaneOrbCornerBox, gbc);
			y++;
			refreshArcaneOrbCornerVisibility();
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
		confirmButton.setVisible(isChargeUpAction() || isDeployDroneAction() || isPositioningJetsAction() || isOverchargeAction());
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

	private void attachRefreshListener(JTextField field) {
		if (field == null) return;
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { refreshEffectsPreview(); }
			@Override
			public void removeUpdate(DocumentEvent e) { refreshEffectsPreview(); }
			@Override
			public void changedUpdate(DocumentEvent e) { refreshEffectsPreview(); }
		});
	}

	private void refreshEffectsPreview() {
		if (effectsArea == null) return;
		if (isArcaneOrbAction()) {
			refreshArcaneOrbCornerVisibility();
		}
		StringBuilder text = new StringBuilder();
		appendInformDetails(text);
		effectsArea.setText(text.toString());
		effectsArea.setCaretPosition(0);
	}

	private void appendInformDetails(StringBuilder text) {
		if (text == null) return;
		if (isDronesAction()) {
			double bonus = character == null ? 0.0 : character.getDroneBonusAmount();
			int current = character == null ? 0 : character.getDrones().size();
			int max = character == null ? 0 : character.getMaxDroneCount();
			int charge = character == null || character.getResources() == null
					? 0 : character.getResources().calcCurrentResource1();
			text.append(isDeployDroneAction()
					? "Deploy a support drone as an Aura Action."
					: "Recall a drone or mark one as returned if its target left Range.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Controlled: ").append(current).append(" / ").append(max)
					.append(System.lineSeparator());
			text.append("Available Charge: ").append(charge)
					.append(System.lineSeparator());
			text.append("Drone bonus: +").append(trimNumber(bonus))
					.append(" (1/4 PRIM)")
					.append(System.lineSeparator());
			text.append("Range: ").append(getRange() <= 0 ? "Melee" : (getRange() + " ft"))
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			if (isDeployDroneAction()) {
				String type = getSelectedDroneType();
				String target = getSelectedDroneTarget();
				text.append("Selected Type: ").append(type)
						.append(System.lineSeparator());
				text.append("Selected Target: ").append(target)
						.append(System.lineSeparator());
				if (CharDrone.TYPE_ATTACK.equalsIgnoreCase(type)) {
					text.append("Effect: Grant +").append(trimNumber(bonus)).append(" to hit.");
				} else {
					text.append("Effect: Grant +").append(trimNumber(bonus)).append(" AC.");
				}
				text.append(System.lineSeparator())
						.append(System.lineSeparator());
				text.append("The drone affixes to the target and must remain within Range. ");
				text.append("A target may only have 1 drone attached. Deploying occupies 1 Charge.");
			} else {
				text.append("Recalling refunds the occupied Charge. ");
				text.append("If a target leaves Range, the drone returns and can be redeployed on your next turn.");
			}
			return;
		}
		if (isChargeUpAction()) {
			int restoreAmount = getChargeUpRestoreAmount();
			int current = getCurrentCharge();
			int max = getMaxCharge();
			int after = Math.min(max, current + restoreAmount);
			text.append("Charge-Up is activated as a Standard Action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Restore Charge equal to class level: ").append(restoreAmount)
					.append(System.lineSeparator());
			text.append("Charge: ").append(current).append(" / ").append(max)
					.append(" → ").append(after).append(" / ").append(max);
			return;
		}
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
			text.append(" (APPLY + MAN) vs FORT")
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
			text.append(" (APPLY + MAN) vs REF")
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
		else if (isOverchargeAction()) {
			double bonus = character == null ? 0.0 : character.getOverchargeDamageMultiplierBonus();
			text.append("Overcharge is activated as an Aura Action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Until end of turn, the cost of all class abilities is doubled.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Class abilities increase the damage multiplier of their associated attack by +")
					.append(trimNumber(bonus))
					.append(" (+0.5 per class level).");
		}
		else if (isPositioningJetsAction()) {
			text.append("Interrupt: An enemy moves.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Cost: 1 Reaction.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("As a reaction to an enemy's movement, the pilot may take a step.");
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
		else if (isGambitMinionAction()) {
			PanelCharMinion.SummonedMinionCombatInfo selectedMinion = getSelectedGambitMinion();
			double minionAttackBonus = selectedMinion == null ? 0.0 : Math.max(0.0, selectedMinion.attackBonus());
			double gambitBonus = getGambitMinionAttackBonus();
			text.append("Gambit (Minion) is a Standard Action with no resource cost.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Can only be performed by a minion. Designate an enemy within melee range and a target ally.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Selected Minion: ").append(selectedMinion == null ? "None" : selectedMinion.displayName())
					.append(System.lineSeparator());
			text.append("Minion ATK Bonus: ").append(trimNumber(minionAttackBonus))
					.append(System.lineSeparator());
			text.append("Granted Bonus: +").append(trimNumber(gambitBonus)).append(" ATK")
					.append(System.lineSeparator());
			text.append("Formula: (2 × Level)% of minion ATK = ").append(trimNumber(getGambitPercentFactor() * 100.0))
					.append("% × ").append(trimNumber(minionAttackBonus))
					.append(System.lineSeparator());
			text.append("Enemy Target: ").append(getGambitEnemyName())
					.append(System.lineSeparator());
			text.append("Target Ally: ").append(getGambitAllyName())
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("The target ally receives this ATK bonus against the target enemy on their next turn.")
					.append(System.lineSeparator());
			text.append("This bonus is lost if not consumed before the minion's next turn.");
		}
		else if (isGambitLeaderAction()) {
			PanelCharMinion.SummonedMinionCombatInfo selectedMinion = getSelectedGambitMinion();
			double leaderAtk = getLeaderAttackBonus();
			double gambitBonus = getGambitLeaderAttackBonus();
			text.append("Gambit (Leader) is an Aura Action with no resource cost.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Can only be performed by the leader. Designate an enemy target and a target minion.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Selected Minion: ").append(selectedMinion == null ? "None" : selectedMinion.displayName())
					.append(System.lineSeparator());
			text.append("Enemy Target: ").append(getGambitEnemyName())
					.append(System.lineSeparator());
			text.append("Leader ATK Bonus: ").append(trimNumber(leaderAtk))
					.append(System.lineSeparator());
			text.append("Minion AoO Bonus: +").append(trimNumber(gambitBonus)).append(" ATK")
					.append(System.lineSeparator());
			text.append("Formula: (2 × Level)% of leader ATK = ").append(trimNumber(getGambitPercentFactor() * 100.0))
					.append("% × ").append(trimNumber(leaderAtk))
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("The next successful ally attack against the enemy target triggers the selected minion's immediate attack of opportunity on that target.")
					.append(System.lineSeparator());
			text.append("This bonus is lost if not consumed before the leader's next turn.");
		}
		else if (isInspireAction()) {
			text.append("Inspire costs 1 Authority and is activated as an Aura Action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("You cannot inspire yourself.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Selected Version: ").append(getInspireVariantDisplay())
					.append(System.lineSeparator());
			text.append("Target Ally: ").append(getInspireAllyName())
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			if (isInspireHasten()) {
				text.append("Hasten: Target ally gains +").append(trimNumber(getInspireBonus()))
						.append(" to their current Initiative counter.")
						.append(System.lineSeparator());
			} else if (isInspireCompetence()) {
				text.append("Competence: Target ally gains +").append(trimNumber(getInspireBonus()))
						.append(" to attributes for one skill check.")
						.append(System.lineSeparator());
				text.append("This inspired effect persists until consumed or lost on a long rest.")
						.append(System.lineSeparator());
			} else if (isInspireFreedom()) {
				text.append("Freedom: Target ally gains +").append(trimNumber(getInspireBonus()))
						.append(" to a save against a loss of control effect.")
						.append(System.lineSeparator());
				String dcValue = getInspireOriginalDc();
				if (!dcValue.isBlank()) {
					text.append("If already affected, the ally may make an immediate save against the original DC ")
							.append(dcValue).append(".")
							.append(System.lineSeparator());
				} else {
					text.append("If already affected, the ally may make an immediate save against the effect's original DC.")
							.append(System.lineSeparator());
				}
				text.append("This inspired effect persists until consumed or lost on a long rest.")
						.append(System.lineSeparator());
			} else if (isInspireClarity()) {
				text.append("Clarity: Target ally may re-roll one die roll.")
						.append(System.lineSeparator());
				text.append("This inspired effect persists until consumed or lost on a long rest.")
						.append(System.lineSeparator());
			} else if (isInspireFortune()) {
				text.append("Fortune: Target ally may force an enemy to re-roll one die roll made against them.")
						.append(System.lineSeparator());
				text.append("This inspired effect persists until consumed or lost on a long rest.")
						.append(System.lineSeparator());
			} else {
				text.append("No Inspire version is selected on this character.")
						.append(System.lineSeparator());
			}
		}
		else if (isUpliftAction()) {
			text.append("Uplift costs 2 Advantage and is activated as an Interrupt at the beginning of any turn.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Apply any number of cleanse effects once each to allies within range without action cost.")
					.append(System.lineSeparator());
			text.append("Range: ").append(getRange() <= 0 ? "Melee" : (getRange() + " ft"))
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("All allies in range gain +").append(trimNumber(getUpliftPrimaryBonus()))
					.append(" to their primary attribute until end of round.")
					.append(System.lineSeparator());
		}
		else if (isMusterAction()) {
			text.append("Muster costs 1 Advantage and is activated as an Aura Action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Range: ").append(getRange() <= 0 ? "Melee" : (getRange() + " ft"))
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("All allies in range gain +").append(trimNumber(getMusterBaseDamageBonus()))
					.append(" base damage until end of round.")
					.append(System.lineSeparator());
		}
		else if (isRallyAction()) {
			text.append("Rally costs 1 Authority and is activated as an Aura Action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Designate a target afflicted by a Psychosis effect.")
					.append(System.lineSeparator());
			text.append("Select and dispel 1 Psychosis effect from that target.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Target: ").append(getRallyTargetName())
					.append(System.lineSeparator());
			text.append("Psychosis Effect: ").append(getRallyPsychosisName())
					.append(System.lineSeparator());
		}
		else if (isSwapAction()) {
			PanelCharMinion.SummonedMinionCombatInfo selectedMinion = getSelectedGambitMinion();
			text.append("Swap has no resource cost and is activated as an Aura Action.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			if (isTransposeAllySwap()) {
				text.append("Transpose: The selected minion and willing ally immediately swap places.")
						.append(System.lineSeparator())
						.append(System.lineSeparator());
				text.append("Target Minion: ").append(selectedMinion == null ? "None" : selectedMinion.displayName())
						.append(System.lineSeparator());
				text.append("Willing Ally: ").append(getSwapAllyName())
						.append(System.lineSeparator())
						.append(System.lineSeparator());
			} else {
				text.append("Select a target summoned minion. You and that minion immediately swap places.")
						.append(System.lineSeparator())
						.append(System.lineSeparator());
				text.append("Target Minion: ").append(selectedMinion == null ? "None" : selectedMinion.displayName())
						.append(System.lineSeparator())
						.append(System.lineSeparator());
				if (hasTransposeSpecialty()) {
					text.append("Transpose: You may instead swap that minion with any willing ally.")
							.append(System.lineSeparator())
							.append(System.lineSeparator());
				}
			}
			text.append("Swap does not provoke attacks of opportunity.");
		}
		else if (isBlinkAction()) {
			text.append("Blink is a Move Action that costs Aura equal to your class level (")
					.append(trimNumber(getBlinkAuraCost()))
					.append(").")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Destination: ").append(getBlinkDestinationDisplay())
					.append(System.lineSeparator());
			text.append("Move Distance: up to ").append(getDerivedCombatValue("MOVE")).append(" ft.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("You teleport from your starting location to your destination.")
					.append(System.lineSeparator());
			text.append("Your movement is not impaired by difficult terrain or obstacles.")
					.append(System.lineSeparator());
			text.append("You are not subject to attacks of opportunity during your move action.");
		}
		else if (isElectricTetherAction()) {
			int al = getElectricTetherAl();
			int fullDuration = getElectricTetherFullDuration();
			int halfDuration = getElectricTetherHalfDuration();
			double range = getElectricTetherRange();
			double apply = getElectricTetherApplyValue();
			text.append("Electric Tether is an Aura Action.")
					.append(System.lineSeparator());
			text.append("Cost: ").append(trimNumber(getElectricTetherAuraCost()))
					.append(" Aura (").append(trimNumber(ELECTRIC_TETHER_AURA_COST_PER_AL))
					.append(" × AL ").append(al).append(").")
					.append(System.lineSeparator());
			text.append("Range: ").append(trimEffectNumber(range))
					.append(" ft (MOVE × AL × ").append(trimNumber(ELECTRIC_TETHER_RANGE_FACTOR)).append(").")
					.append(System.lineSeparator());
			text.append("Duration: ").append(fullDuration)
					.append(fullDuration == 1 ? " turn" : " turns")
					.append(" (").append(ELECTRIC_TETHER_DURATION_PER_AL).append(" × AL).")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Target Creature: ").append(getElectricTetherTargetDisplay())
					.append(System.lineSeparator());
			text.append("Apply: ").append(trimEffectNumber(apply))
					.append(" (APPLY) vs REF")
					.append(System.lineSeparator());
			text.append("On a successful REF save, duration is reduced by 50% to ")
					.append(halfDuration)
					.append(halfDuration == 1 ? " turn." : " turns.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("While active, any time the affected creature moves,")
					.append(System.lineSeparator());
			text.append("you may spend a reaction to move a distance up to Electric Tether's range")
					.append(System.lineSeparator());
			text.append("(").append(trimEffectNumber(range)).append(" ft) to follow that creature.");
		}
		else if (isRemedyAction()) {
			String effectType = getRemedyEffectType();
			int al = getRemedyAl();
			int duration = getRemedyDuration();
			double bonus = getRemedySaveBonus();
			text.append(getActionTitle()).append(" is an Aura Action.")
					.append(System.lineSeparator());
			text.append("Cost: ").append(trimNumber(getRemedyAuraCost()))
					.append(" Aura (").append(trimNumber(REMEDY_AURA_COST_PER_AL))
					.append(" × AL ").append(al).append(").")
					.append(System.lineSeparator());
			text.append("Range: ").append(getRange() <= 0 ? "Melee" : (getRange() + " ft"))
					.append(System.lineSeparator());
			text.append("Duration: ").append(duration)
					.append(duration == 1 ? " turn" : " turns")
					.append(" (").append(REMEDY_DURATION_PER_AL).append(" × AL).")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Target Ally: ").append(getRemedyTargetDisplay())
					.append(System.lineSeparator());
			text.append("Effect: Grant +").append(trimNumber(bonus))
					.append(" to saves against ").append(effectType).append(" effects.")
					.append(System.lineSeparator());
			text.append(describeRemedyOngoingSave(effectType));
		}
		else if (isArcaneOrbAction()) {
			int crossSection = getArcaneOrbCrossSection();
			int durationTurns = getArcaneOrbDurationTurns();
			double damage = getArcaneOrbDamage();
			text.append("Arcane Orb is a Standard Action.")
					.append(System.lineSeparator());
			text.append("Cost: 3 Aptitude + ").append(trimNumber(getArcaneOrbAuraCost()))
					.append(" Aura (CL × CL).")
					.append(System.lineSeparator());
			text.append("Duration: ").append(durationTurns)
					.append(durationTurns == 1 ? " turn" : " turns")
					.append(" (5% × CTL).")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Target Location: ").append(getArcaneOrbLocationDisplay())
					.append(" (within RANGE ").append(trimNumber(getDerivedCombatValue("RANGE"))).append(" ft)")
					.append(System.lineSeparator());
			text.append("Cross Section: ").append(crossSection)
					.append(" tile").append(crossSection == 1 ? "" : "s")
					.append(" (FLOOR(∛(RANGE/5) × AREA), min 1).")
					.append(System.lineSeparator());
			if ((crossSection % 2) == 0) {
				text.append("Even cross section: center on the ")
						.append(getArcaneOrbCornerDisplay())
						.append(" corner of the chosen tile.")
						.append(System.lineSeparator());
			} else {
				text.append("Odd cross section: centered on the chosen tile.")
						.append(System.lineSeparator());
			}
			text.append("Height equals cross section; confines to available space if needed.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Creatures may move through the orb freely.")
					.append(System.lineSeparator());
			text.append("Adjacent creatures take ").append(trimNumber(damage))
					.append(" damage ((50% × BDMG) + (25% × TDMG))")
					.append(System.lineSeparator());
			text.append("at the start of their turn, and when they move adjacent to the orb.")
					.append(System.lineSeparator());
			text.append("A creature cannot be damaged by the orb more than twice per turn.");
		}
		else if (isFormationAction()) {
			double totalBonus = getFormationAttackBonus();
			List<PanelCharMinion.SummonedMinionCombatInfo> adjacent = getSelectedFormationMinions();
			text.append("Formation costs 1 Defensive Tactic and 1 Strategic Tactic.")
					.append(System.lineSeparator());
			text.append("Activated as an Aura Action. Only one Tactics ability per turn.")
					.append(System.lineSeparator());
			text.append("Only one Formation can be active at a time.")
					.append(System.lineSeparator())
					.append(System.lineSeparator());
			text.append("Target Ally: ").append(getFormationTargetDisplay())
					.append(System.lineSeparator());
			text.append("Adjacent Minions: ").append(adjacent.isEmpty() ? "None" : adjacent.size())
					.append(System.lineSeparator());
			for (PanelCharMinion.SummonedMinionCombatInfo minion : adjacent) {
				if (minion == null) continue;
				text.append("  - ").append(minion.displayName())
						.append(": +").append(trimNumber(applyGambitPercent(minion.attackBonus()))).append(" ATK")
						.append(System.lineSeparator());
			}
			text.append(System.lineSeparator());
			text.append("Total Attack Bonus: +").append(trimNumber(totalBonus)).append(" ATK")
					.append(System.lineSeparator());
			text.append("Each adjacent minion contributes its gambit bonus ((2 × Level)% of that minion's ATK).")
					.append(System.lineSeparator());
			text.append("Check each minion that is adjacent to the target ally.");
		}
	}

	private void confirmPressed() {
		if (!validateInformConfiguration(true)) {
			return;
		}
		if (combatFrame != null) {
			DataAction resolvedAction = buildResolvedActionForConfirm();
			if (resolvedAction == null) {
				return;
			}
			if (!combatFrame.finishActionUse(resolvedAction)) {
				return;
			}
		}
		applyInformPostConfirmEffects();
		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
		}
		setVisible(false);
		dispose();
	}

	private void copyRollToClipboard() {
		if (character == null) return;
		if (!validateInformConfiguration(false)) {
			return;
		}
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
		if (isDronesAction()) {
			tempString += " --+|Controlled: " + (character == null ? 0 : character.getDrones().size())
					+ " / " + (character == null ? 0 : character.getMaxDroneCount());
			tempString += " --+|Bonus: +" + trimNumber(character == null ? 0.0 : character.getDroneBonusAmount())
					+ " (1/4 PRIM)";
			if (isDeployDroneAction()) {
				tempString += " --+|Type: " + sanitizeForMacro(getSelectedDroneType());
				tempString += " --+|Target: " + sanitizeForMacro(getSelectedDroneTarget());
				tempString += " --+|Effect: Affix drone and grant "
						+ (CharDrone.TYPE_ATTACK.equalsIgnoreCase(getSelectedDroneType()) ? "ATK" : "AC")
						+ " bonus; occupies 1 Charge";
			} else {
				tempString += " --+|Effect: Recall refunds Charge; out of Range returns the drone until next turn";
			}
		}
		if (isOverchargeAction()) {
			tempString += " --+|Duration: End of turn";
			tempString += " --+|Effect: Class ability costs are doubled";
			tempString += " --+|Damage Multiplier: +" + trimNumber(character == null ? 0.0 : character.getOverchargeDamageMultiplierBonus())
					+ " on associated class ability attacks (+0.5 per class level)";
		}
		if (isPositioningJetsAction()) {
			tempString += " --+|Trigger: An enemy moves";
			tempString += " --+|Cost: 1 Reaction";
			tempString += " --+|Effect: Take a step";
		}
		if (isChargeUpAction()) {
			tempString += " --+|Restore Charge: " + getChargeUpRestoreAmount();
			tempString += " --+|Charge: " + getCurrentCharge() + " / " + getMaxCharge();
			tempString += " --+|Effect: Restore Charge equal to class level";
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
		if (isGambitMinionAction()) {
			PanelCharMinion.SummonedMinionCombatInfo selectedMinion = getSelectedGambitMinion();
			tempString += " --+|Minion: " + sanitizeForMacro(selectedMinion == null ? "None" : selectedMinion.displayName());
			tempString += " --+|Enemy Target: " + sanitizeForMacro(getGambitEnemyName());
			tempString += " --+|Target Ally: " + sanitizeForMacro(getGambitAllyName());
			tempString += " --+|Granted Bonus: +" + trimNumber(getGambitMinionAttackBonus()) + " ATK";
			tempString += " --+|Formula: (2xLevel)% of minion ATK";
			tempString += " --+|Effect: Ally gains the ATK bonus against the enemy on their next turn; lost if not consumed before the minion's next turn";
		}
		if (isGambitLeaderAction()) {
			PanelCharMinion.SummonedMinionCombatInfo selectedMinion = getSelectedGambitMinion();
			tempString += " --+|Target Minion: " + sanitizeForMacro(selectedMinion == null ? "None" : selectedMinion.displayName());
			tempString += " --+|Enemy Target: " + sanitizeForMacro(getGambitEnemyName());
			tempString += " --+|AoO Bonus: +" + trimNumber(getGambitLeaderAttackBonus()) + " ATK";
			tempString += " --+|Formula: (2xLevel)% of leader ATK";
			tempString += " --+|Effect: Next successful ally attack against the target enemy triggers the selected minion's immediate AoO; lost if not consumed before the leader's next turn";
		}
		if (isInspireAction()) {
			tempString += " --+|Version: " + sanitizeForMacro(getInspireVariantDisplay());
			tempString += " --+|Target Ally: " + sanitizeForMacro(getInspireAllyName());
			if (isInspireHasten()) {
				tempString += " --+|Bonus: +" + trimNumber(getInspireBonus());
				tempString += " --+|Effect: Increase current Initiative counter by +" + trimNumber(getInspireBonus());
			} else if (isInspireCompetence()) {
				tempString += " --+|Bonus: +" + trimNumber(getInspireBonus());
				tempString += " --+|Effect: Gain +" + trimNumber(getInspireBonus()) + " to attributes for one skill check";
			} else if (isInspireFreedom()) {
				tempString += " --+|Bonus: +" + trimNumber(getInspireBonus());
				tempString += " --+|Effect: Gain +" + trimNumber(getInspireBonus()) + " to a save against a loss of control effect";
				if (!getInspireOriginalDc().isBlank()) {
					tempString += " --+|Original DC: " + sanitizeForMacro(getInspireOriginalDc());
				}
			} else if (isInspireClarity()) {
				tempString += " --+|Effect: Ally may re-roll one die roll";
			} else if (isInspireFortune()) {
				tempString += " --+|Effect: Ally may force an enemy to re-roll one die roll made against them";
			}
		}
		if (isUpliftAction()) {
			tempString += " --+|Range: " + sanitizeForMacro(getRange() <= 0 ? "Melee" : (getRange() + " ft"));
			tempString += " --+|Effect: Apply any number of cleanse effects once each to allies within range";
			tempString += " --+|Buff: Allies gain +" + trimNumber(getUpliftPrimaryBonus())
					+ " to their primary attribute until end of round";
		}
		if (isMusterAction()) {
			tempString += " --+|Range: " + sanitizeForMacro(getRange() <= 0 ? "Melee" : (getRange() + " ft"));
			tempString += " --+|Buff: Allies gain +" + trimNumber(getMusterBaseDamageBonus()) + " base damage until end of round";
		}
		if (isRallyAction()) {
			tempString += " --+|Target: " + sanitizeForMacro(getRallyTargetName());
			tempString += " --+|Psychosis Effect: " + sanitizeForMacro(getRallyPsychosisName());
			tempString += " --+|Effect: Dispel 1 Psychosis effect from the target";
		}
		if (isSwapAction()) {
			PanelCharMinion.SummonedMinionCombatInfo selectedMinion = getSelectedGambitMinion();
			tempString += " --+|Target Minion: " + sanitizeForMacro(selectedMinion == null ? "None" : selectedMinion.displayName());
			if (isTransposeAllySwap()) {
				tempString += " --+|Willing Ally: " + sanitizeForMacro(getSwapAllyName());
				tempString += " --+|Effect: The minion and willing ally immediately swap places (Transpose)";
			} else {
				tempString += " --+|Effect: You and the target minion immediately swap places";
			}
			tempString += " --+|Notes: Swap does not provoke attacks of opportunity";
		}
		if (isBlinkAction()) {
			tempString += " --+|Aura Cost: " + trimNumber(getBlinkAuraCost()) + " (class level)";
			tempString += " --+|Destination: " + sanitizeForMacro(getBlinkDestinationDisplay());
			tempString += " --+|Move Distance: up to " + getDerivedCombatValue("MOVE") + " ft";
			tempString += " --+|Effect: Teleport from your starting location to your destination";
			tempString += " --+|Notes: Movement is not impaired by difficult terrain or obstacles";
			tempString += " --+|Notes: You are not subject to attacks of opportunity during this move action";
		}
		if (isElectricTetherAction()) {
			tempString += " --+|AL: " + getElectricTetherAl();
			tempString += " --+|Cost: Aura " + trimNumber(getElectricTetherAuraCost());
			tempString += " --+|Range: " + trimEffectNumber(getElectricTetherRange()) + " ft";
			tempString += " --+|Duration: " + getElectricTetherFullDuration() + " turns"
					+ " (REF save: " + getElectricTetherHalfDuration() + " turns)";
			tempString += " --+|Target Creature: " + sanitizeForMacro(getElectricTetherTargetDisplay());
			tempString += " --+|Apply: " + trimEffectNumber(getElectricTetherApplyValue()) + " vs REF";
			tempString += " --+|Effect: While active, when the target moves you may spend a reaction"
					+ " to move up to Electric Tether's range to follow them";
		}
		if (isRemedyAction()) {
			String effectType = getRemedyEffectType();
			tempString += " --+|AL: " + getRemedyAl();
			tempString += " --+|Cost: Aura " + trimNumber(getRemedyAuraCost());
			tempString += " --+|Duration: " + getRemedyDuration() + " turns";
			tempString += " --+|Target Ally: " + sanitizeForMacro(getRemedyTargetDisplay());
			tempString += " --+|Save Bonus: +" + trimNumber(getRemedySaveBonus())
					+ " vs " + sanitizeForMacro(effectType) + " effects";
			tempString += " --+|Notes: " + sanitizeForMacro(describeRemedyOngoingSave(effectType));
			tempString += " --+|Status Code:[br]&nbsp;&nbsp;" + buildRemedyStatusCode();
		}
		if (isArcaneOrbAction()) {
			int crossSection = getArcaneOrbCrossSection();
			tempString += " --+|Aptitude Cost: 3";
			tempString += " --+|Aura Cost: " + trimNumber(getArcaneOrbAuraCost()) + " (CL x CL)";
			tempString += " --+|Duration: " + getArcaneOrbDurationTurns() + " turns (5% x CTL)";
			tempString += " --+|Target Location: " + sanitizeForMacro(getArcaneOrbLocationDisplay());
			tempString += " --+|Cross Section: " + crossSection + " tiles";
			if ((crossSection % 2) == 0) {
				tempString += " --+|Even Center Corner: " + sanitizeForMacro(getArcaneOrbCornerDisplay());
			} else {
				tempString += " --+|Centering: Chosen tile";
			}
			tempString += " --+|Height: " + crossSection + " tiles (confines to available space)";
			tempString += " --+|Adjacent Damage: " + trimNumber(getArcaneOrbDamage())
					+ " ((50% x BDMG) + (25% x TDMG))";
			tempString += " --+|Notes: Creatures may move through the orb; max 2 damages per creature per turn";
		}
		if (isFormationAction()) {
			tempString += " --+|Target Ally: " + sanitizeForMacro(getFormationTargetDisplay());
			tempString += " --+|Adjacent Minions: " + sanitizeForMacro(formatSelectedFormationMinionNames());
			tempString += " --+|Total ATK Bonus: +" + trimNumber(getFormationAttackBonus());
			tempString += " --+|Effect: Ally gains ATK equal to the sum of adjacent minions' gambit bonuses; replaces any prior Formation";
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

	private boolean isOverchargeAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(OVERCHARGE_ACTION);
	}

	private boolean isPositioningJetsAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(POSITIONING_JETS_ACTION);
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

	private boolean isChargeUpAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(CHARGE_UP_ACTION);
	}

	private boolean isDeployDroneAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(DEPLOY_DRONE_ACTION);
	}

	private boolean isRecallDroneAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(RECALL_DRONE_ACTION);
	}

	private boolean isDronesAction() {
		return isDeployDroneAction() || isRecallDroneAction();
	}

	private String getSelectedDroneType() {
		if (droneTypeBox != null && droneTypeBox.getSelectedItem() != null) {
			return droneTypeBox.getSelectedItem().toString();
		}
		return CharDrone.TYPE_ATTACK;
	}

	private String getSelectedDroneTarget() {
		if (droneTargetField == null || droneTargetField.getText() == null || droneTargetField.getText().isBlank()) {
			return "Yourself";
		}
		return droneTargetField.getText().trim();
	}

	private void refreshDroneListPanel() {
		if (droneListPanel == null) return;
		droneListPanel.removeAll();
		if (character == null || character.getDrones().isEmpty()) {
			droneListPanel.add(new JLabel("No drones controlled."));
		} else {
			for (CharDrone drone : character.getDrones()) {
				if (drone == null) continue;
				JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
				String state = drone.isActive() ? "applying" : (drone.isCanRedeploy() ? "ready" : "returned");
				row.add(new JLabel(drone.getType() + " on " + drone.getTarget() + " (" + state + ")"));
				javax.swing.JButton recall = new javax.swing.JButton("Recall");
				recall.addActionListener(e -> handleRecallDrone(drone));
				row.add(recall);
				if (drone.isActive()) {
					javax.swing.JButton outOfRange = new javax.swing.JButton("Out of Range");
					outOfRange.addActionListener(e -> handleReturnDrone(drone));
					row.add(outOfRange);
				}
				droneListPanel.add(row);
			}
		}
		droneListPanel.revalidate();
		droneListPanel.repaint();
	}

	private void handleRecallDrone(CharDrone drone) {
		if (character == null || drone == null) return;
		character.recallDrone(drone);
		refreshAfterDroneChange();
	}

	private void handleReturnDrone(CharDrone drone) {
		if (character == null || drone == null) return;
		character.returnDroneOutOfRange(drone);
		refreshAfterDroneChange();
	}

	private void refreshAfterDroneChange() {
		if (character != null) {
			character.updateAll();
		}
		if (combatFrame != null) {
			combatFrame.refreshResourceDisplays();
		}
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
		refreshDroneListPanel();
		refreshEffectsPreview();
	}

	private int getChargeUpRestoreAmount() {
		return character == null ? 0 : character.getChargeUpRestoreAmount();
	}

	private int getCurrentCharge() {
		if (character == null || character.getResources() == null) return 0;
		return character.getResources().calcCurrentResource1();
	}

	private int getMaxCharge() {
		if (character == null || character.getResources() == null) return 0;
		return character.getResources().calcMaxResource1();
	}

	private boolean isEmbedAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(EMBED_ACTION);
	}

	private boolean isGambitMinionAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(GAMBIT_MINION_ACTION);
	}

	private boolean isGambitLeaderAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(GAMBIT_LEADER_ACTION);
	}

	private boolean isInspireAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(INSPIRE_ACTION);
	}

	private boolean isUpliftAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(UPLIFT_ACTION);
	}

	private boolean isMusterAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(MUSTER_ACTION);
	}

	private boolean isRallyAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(RALLY_ACTION);
	}

	private boolean isSwapAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(SWAP_ACTION);
	}

	private boolean isBlinkAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(BLINK_ACTION);
	}

	private boolean isElectricTetherAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(ELECTRIC_TETHER_ACTION);
	}

	private void populateElectricTetherAlSelect() {
		if (electricTetherAlSelect == null) return;
		electricTetherAlSelect.removeAllItems();
		int maxRank = getElectricTetherTechniqueRank();
		if (maxRank <= 0) {
			maxRank = 1;
		}
		for (int i = 1; i <= maxRank; i++) {
			electricTetherAlSelect.addItem(i);
		}
		int selectedAl = 1;
		if (action != null && action.getAl() > 0) {
			selectedAl = Math.min(action.getAl(), maxRank);
		}
		electricTetherAlSelect.setSelectedItem(selectedAl);
		syncElectricTetherAlToAction();
	}

	private void syncElectricTetherAlToAction() {
		if (action == null) return;
		action.setAl(getElectricTetherAl());
	}

	private int getElectricTetherAl() {
		if (electricTetherAlSelect != null && electricTetherAlSelect.getSelectedItem() instanceof Integer value) {
			return Math.max(1, value);
		}
		if (action != null && action.getAl() > 0) {
			return action.getAl();
		}
		return 1;
	}

	private int getElectricTetherTechniqueRank() {
		if (character == null || character.getTraining() == null || action == null) return 0;
		DataTraining training = character.getTraining().getTrainingById(action.getId());
		if (training == null && action.getName() != null) {
			for (DataTraining learned : character.getTraining().getAllTraining()) {
				if (learned != null && learned.getName() != null
						&& action.getName().equalsIgnoreCase(learned.getName())) {
					training = learned;
					break;
				}
			}
		}
		return training == null ? 0 : Math.max(0, training.getRank());
	}

	private double getElectricTetherAuraCost() {
		return ELECTRIC_TETHER_AURA_COST_PER_AL * getElectricTetherAl();
	}

	private double getElectricTetherRange() {
		return Math.max(0.0, getDerivedCombatValue("MOVE") * getElectricTetherAl() * ELECTRIC_TETHER_RANGE_FACTOR);
	}

	private int getElectricTetherFullDuration() {
		return Math.max(1, getElectricTetherAl() * ELECTRIC_TETHER_DURATION_PER_AL);
	}

	private int getElectricTetherHalfDuration() {
		return Math.max(1, (int)Math.floor(getElectricTetherFullDuration() / 2.0));
	}

	private double getElectricTetherApplyValue() {
		return Math.max(0.0, resolveCharacterStatValue("APP"));
	}

	private String getElectricTetherTarget() {
		if (electricTetherTargetField == null || electricTetherTargetField.getText() == null) return "";
		return electricTetherTargetField.getText().trim();
	}

	private String getElectricTetherTargetDisplay() {
		String target = getElectricTetherTarget();
		return target.isBlank() ? "Unspecified" : target;
	}


	private boolean isRemedyAction() {
		if (action == null) return false;
		if (action.getCategory() != null && "Remedy".equalsIgnoreCase(action.getCategory().trim())) {
			return true;
		}
		return getRemedyEffectTypeFromName(action.getName()) != null;
	}

	private String getRemedyEffectType() {
		String fromName = getRemedyEffectTypeFromName(action == null ? null : action.getName());
		return fromName == null ? "targeted" : fromName;
	}

	private String getRemedyEffectTypeFromName(String name) {
		if (name == null || name.isBlank()) return null;
		String lower = name.toLowerCase(java.util.Locale.ROOT);
		if (lower.contains("antidote")) return "Poison";
		if (lower.contains("cure")) return "Disease";
		if (lower.contains("cleanse")) return "Curse";
		if (lower.contains("therapy")) return "Psychosis";
		return null;
	}

	private void populateRemedyAlSelect() {
		if (remedyAlSelect == null) return;
		remedyAlSelect.removeAllItems();
		int maxRank = getRemedyTechniqueRank();
		if (maxRank <= 0) {
			maxRank = 1;
		}
		for (int i = 1; i <= maxRank; i++) {
			remedyAlSelect.addItem(i);
		}
		int selectedAl = 1;
		if (action != null && action.getAl() > 0) {
			selectedAl = Math.min(action.getAl(), maxRank);
		}
		remedyAlSelect.setSelectedItem(selectedAl);
		syncRemedyAlToAction();
	}

	private void syncRemedyAlToAction() {
		if (action == null) return;
		action.setAl(getRemedyAl());
	}

	private int getRemedyAl() {
		if (remedyAlSelect != null && remedyAlSelect.getSelectedItem() instanceof Integer value) {
			return Math.max(1, value);
		}
		if (action != null && action.getAl() > 0) {
			return action.getAl();
		}
		return 1;
	}

	private int getRemedyTechniqueRank() {
		if (character == null || character.getTraining() == null || action == null) return 0;
		DataTraining training = character.getTraining().getTrainingById(action.getId());
		if (training == null && action.getName() != null) {
			for (DataTraining learned : character.getTraining().getAllTraining()) {
				if (learned != null && learned.getName() != null
						&& action.getName().equalsIgnoreCase(learned.getName())) {
					training = learned;
					break;
				}
			}
		}
		return training == null ? 0 : Math.max(0, training.getRank());
	}

	private double getRemedyAuraCost() {
		return REMEDY_AURA_COST_PER_AL * getRemedyAl();
	}

	private int getRemedyDuration() {
		return Math.max(1, getRemedyAl() * REMEDY_DURATION_PER_AL);
	}

	private double getRemedySaveBonus() {
		return REMEDY_SAVE_BONUS_PER_AL * getRemedyAl();
	}

	private int getRemedyExtraSaves() {
		if (action == null || !action.hasModifierAttribute("diseasesaves")) return 1;
		syncRemedyAlToAction();
		return Math.max(0, (int) Math.round(action.evaluateModifierAttributeValue("diseasesaves")));
	}

	private String describeRemedyOngoingSave(String effectType) {
		String effect = effectType == null || effectType.isBlank() ? "targeted" : effectType;
		if (action == null || !action.hasModifierAttribute("diseasesaves")) {
			return "If the target is already under a " + effect
					+ " effect, they may make an immediate save against it.";
		}
		int saves = getRemedyExtraSaves();
		String count = saves == 1 ? "1 new save" : "up to " + saves + " new saves";
		return "The target may make " + count + " against each ongoing " + effect
				+ " effect. Recasting grants additional saves.";
	}

	private String getRemedyTarget() {
		if (remedyTargetField == null || remedyTargetField.getText() == null) return "";
		return remedyTargetField.getText().trim();
	}

	private String getRemedyTargetDisplay() {
		String target = getRemedyTarget();
		return target.isBlank() ? "Unspecified" : target;
	}

	private String buildRemedyStatusCode() {
		String actionName = action == null || action.getName() == null || action.getName().isBlank()
				? "Remedy"
				: action.getName();
		String effectType = getRemedyEffectType();
		int duration = getRemedyDuration();
		double bonus = getRemedySaveBonus();
		String desc = "Grant +" + trimNumber(bonus) + " to saves against " + effectType
				+ " effects for " + duration + (duration == 1 ? " turn" : " turns")
				+ ". " + describeRemedyOngoingSave(effectType);
		return "NAME:" + sanitizeStatusCodeText(actionName)
				+ "_DUR:Turn:" + duration
				+ "_DESC:" + sanitizeStatusCodeText(desc);
	}

	private String sanitizeStatusCodeText(String value) {
		if (value == null) return "";
		return value.replace("|", "/").replace("_", " ").trim();
	}

	private void applyRemedyEffects() {
		if (character == null || character.getCombat() == null) return;

		syncRemedyAlToAction();
		String effectType = getRemedyEffectType();
		int duration = getRemedyDuration();
		double bonus = getRemedySaveBonus();
		String target = getRemedyTargetDisplay();
		String actionName = action == null || action.getName() == null || action.getName().isBlank()
				? "Remedy"
				: action.getName();

		String description = actionName + " on " + target + ": +" + trimNumber(bonus)
				+ " to saves against " + effectType + " effects. " + describeRemedyOngoingSave(effectType)
				+ " Duration: " + duration + (duration == 1 ? " turn." : " turns.");

		character.getCombat().removeStatus(actionName);
		DataStatus reminder = new DataStatus();
		reminder.setName(actionName);
		reminder.setAffinity(action != null && action.getAffinity() != null ? action.getAffinity() : "None");
		reminder.setAttribute("REMINDER");
		reminder.setSeverity(bonus);
		reminder.setDurationType("Turn");
		reminder.setDuration(duration);
		reminder.setDescription(description);
		character.getCombat().addStatus(reminder);
		character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
	}

	private boolean isArcaneOrbAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(ARCANE_ORB_ACTION);
	}

	private boolean isFormationAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase(FORMATION_ACTION);
	}

	private boolean hasTransposeSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(TRANSPOSE_SPECIALTY);
	}

	private boolean isTransposeAllySwap() {
		if (!hasTransposeSpecialty() || swapPartnerBox == null) return false;
		Object selected = swapPartnerBox.getSelectedItem();
		return selected != null && SWAP_PARTNER_ALLY.equalsIgnoreCase(selected.toString().trim());
	}

	private String getSwapAllyName() {
		String value = swapAllyField == null ? "" : swapAllyField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
	}

	private void refreshSwapAllyFieldVisibility() {
		boolean showAlly = isTransposeAllySwap();
		if (swapAllyLabel != null) {
			swapAllyLabel.setVisible(showAlly);
		}
		if (swapAllyField != null) {
			swapAllyField.setVisible(showAlly);
			swapAllyField.setEnabled(showAlly);
		}
	}

	private void refreshFormationAllyFieldVisibility() {
		boolean showAlly = isFormationAllyTarget();
		if (formationAllyLabel != null) {
			formationAllyLabel.setVisible(showAlly);
		}
		if (formationAllyField != null) {
			formationAllyField.setVisible(showAlly);
			formationAllyField.setEnabled(showAlly);
		}
	}

	private boolean isFormationAllyTarget() {
		if (formationTargetBox == null) return false;
		Object selected = formationTargetBox.getSelectedItem();
		return selected != null && FORMATION_TARGET_ALLY.equalsIgnoreCase(selected.toString().trim());
	}

	private boolean isFormationSelfTarget() {
		return isFormationAction() && !isFormationAllyTarget();
	}

	private String getFormationTargetDisplay() {
		if (isFormationSelfTarget()) {
			return getCharName();
		}
		String value = formationAllyField == null ? "" : formationAllyField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
	}

	private List<PanelCharMinion.SummonedMinionCombatInfo> getSelectedFormationMinions() {
		ArrayList<PanelCharMinion.SummonedMinionCombatInfo> selected = new ArrayList<>();
		if (formationMinionOptions == null || formationAdjacentBoxes == null) return selected;
		int count = Math.min(formationMinionOptions.size(), formationAdjacentBoxes.size());
		for (int i = 0; i < count; i++) {
			JCheckBox box = formationAdjacentBoxes.get(i);
			if (box != null && box.isSelected()) {
				selected.add(formationMinionOptions.get(i));
			}
		}
		return selected;
	}

	private String formatSelectedFormationMinionNames() {
		List<PanelCharMinion.SummonedMinionCombatInfo> selected = getSelectedFormationMinions();
		if (selected.isEmpty()) return "None";
		ArrayList<String> names = new ArrayList<>();
		for (PanelCharMinion.SummonedMinionCombatInfo minion : selected) {
			if (minion != null && minion.displayName() != null && !minion.displayName().isBlank()) {
				names.add(minion.displayName());
			}
		}
		return names.isEmpty() ? "None" : String.join(", ", names);
	}

	private double getFormationAttackBonus() {
		double total = 0.0;
		for (PanelCharMinion.SummonedMinionCombatInfo minion : getSelectedFormationMinions()) {
			if (minion == null) continue;
			total += applyGambitPercent(minion.attackBonus());
		}
		return Math.max(0.0, total);
	}

	private String getInspireVariant() {
		if (inspireVersionBox != null) {
			Object selected = inspireVersionBox.getSelectedItem();
			return selected == null ? "" : selected.toString().trim();
		}
		List<String> variants = getOwnedInspireVariants();
		return variants.isEmpty() ? "" : variants.get(0);
	}

	private List<String> getOwnedInspireVariants() {
		return FrameSpecialsPicker.getChosenInspireVariants(character);
	}

	private String getInspireVariantDisplay() {
		String variant = getInspireVariant();
		return variant.isBlank() ? "Unchosen" : variant;
	}

	private boolean isInspireHasten() {
		return INSPIRE_HASTEN.equalsIgnoreCase(getInspireVariant());
	}

	private boolean isInspireCompetence() {
		return INSPIRE_COMPETENCE.equalsIgnoreCase(getInspireVariant());
	}

	private boolean isInspireFreedom() {
		return INSPIRE_FREEDOM.equalsIgnoreCase(getInspireVariant());
	}

	private boolean isInspireClarity() {
		return INSPIRE_CLARITY.equalsIgnoreCase(getInspireVariant());
	}

	private boolean isInspireFortune() {
		return INSPIRE_FORTUNE.equalsIgnoreCase(getInspireVariant());
	}

	private void refreshInspireFreedomFieldVisibility() {
		boolean showDc = isInspireFreedom();
		if (inspireOriginalDcLabel != null) {
			inspireOriginalDcLabel.setVisible(showDc);
		}
		if (inspireOriginalDcField != null) {
			inspireOriginalDcField.setVisible(showDc);
			inspireOriginalDcField.setEnabled(showDc);
		}
	}

	private boolean isOverrunAction() {
		return action != null
				&& action.getName() != null
				&& action.getName().equalsIgnoreCase("Overrun");
	}

	private int getRange() {
		if (isElectricTetherAction()) {
			return (int)Math.round(getElectricTetherRange());
		}
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
			double costValue = character == null
					? cost.getValue()
					: character.getEffectiveActionCostValue(dataAction, cost.getType(), cost.getValue());
			values.add(cost.getType() + " " + trimNumber(costValue));
		}
		return values.isEmpty() ? "None" : String.join(", ", values);
	}

	private String[] buildGambitMinionOptionLabels(List<PanelCharMinion.SummonedMinionCombatInfo> minions) {
		if (minions == null || minions.isEmpty()) {
			return new String[] { "*** No Summoned Minions ***" };
		}
		ArrayList<String> labels = new ArrayList<>();
		for (PanelCharMinion.SummonedMinionCombatInfo minion : minions) {
			if (minion == null) continue;
			labels.add(minion.displayName());
		}
		if (labels.isEmpty()) {
			labels.add("*** No Summoned Minions ***");
		}
		return labels.toArray(new String[0]);
	}

	private List<PanelCharMinion.SummonedMinionCombatInfo> getSummonedMinionOptions() {
		if (sheetFrame == null) return List.of();
		List<PanelCharMinion.SummonedMinionCombatInfo> options = sheetFrame.getSummonedMinionCombatInfo();
		return options == null ? List.of() : options;
	}

	/** Prefer the currently acting minion for Gambit (Minion); otherwise all summoned minions. */
	private List<PanelCharMinion.SummonedMinionCombatInfo> resolveActingGambitMinionOptions() {
		List<PanelCharMinion.SummonedMinionCombatInfo> options = getSummonedMinionOptions();
		if (options.isEmpty() || combatFrame == null) return options;
		String activeSlot = combatFrame.getActiveMinionSlot();
		if (activeSlot == null || activeSlot.isBlank()) return options;
		ArrayList<PanelCharMinion.SummonedMinionCombatInfo> acting = new ArrayList<>();
		for (PanelCharMinion.SummonedMinionCombatInfo option : options) {
			if (option != null && activeSlot.equalsIgnoreCase(option.slotTitle())) {
				acting.add(option);
				break;
			}
		}
		return acting.isEmpty() ? options : acting;
	}

	private PanelCharMinion.SummonedMinionCombatInfo getSelectedGambitMinion() {
		if (gambitMinionOptions == null || gambitMinionOptions.isEmpty()) return null;
		if (gambitMinionBox == null) return gambitMinionOptions.get(0);
		int selectedIndex = gambitMinionBox.getSelectedIndex();
		if (selectedIndex < 0 || selectedIndex >= gambitMinionOptions.size()) {
			return gambitMinionOptions.get(0);
		}
		return gambitMinionOptions.get(selectedIndex);
	}

	private String getGambitEnemyName() {
		String value = gambitEnemyField == null ? "" : gambitEnemyField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
	}

	private String getGambitAllyName() {
		String value = gambitAllyField == null ? "" : gambitAllyField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
	}

	private double getGambitMinionAttackBonus() {
		PanelCharMinion.SummonedMinionCombatInfo minion = getSelectedGambitMinion();
		if (minion == null) return 0.0;
		return applyGambitPercent(minion.attackBonus());
	}

	private double getGambitLeaderAttackBonus() {
		return applyGambitPercent(getLeaderAttackBonus());
	}

	private double getLeaderAttackBonus() {
		if (character == null || character.getAttributes() == null) return 0.0;
		return Math.max(0.0, character.getAttributes().calcStatusValue("ATK"));
	}

	/** (2 × level)% as a decimal multiplier of an attack bonus. */
	private double getGambitPercentFactor() {
		double level = character == null ? 0.0 : Math.max(0, character.getLevel());
		return (2.0 * level) / 100.0;
	}

	private double applyGambitPercent(double attackBonus) {
		return Math.max(0.0, Math.max(0.0, attackBonus) * getGambitPercentFactor());
	}

	private String getInspireAllyName() {
		String value = inspireAllyField == null ? "" : inspireAllyField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
	}

	private String getInspireOriginalDc() {
		String value = inspireOriginalDcField == null ? "" : inspireOriginalDcField.getText();
		return value == null ? "" : value.trim();
	}

	private double getInspireBonus() {
		return character == null ? 0.0 : Math.max(0, character.getLevel());
	}

	private double getUpliftPrimaryBonus() {
		if (character == null) return 0.0;
		return character.scaleSupportEffect(1.0);
	}

	private double getMusterBaseDamageBonus() {
		if (character == null) return 0.0;
		return character.scaleSupportEffect(Math.max(0, character.getLevel()));
	}

	private String getRallyTargetName() {
		String value = rallyTargetField == null ? "" : rallyTargetField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
	}

	private String getRallyPsychosisName() {
		String value = rallyPsychosisField == null ? "" : rallyPsychosisField.getText();
		return value == null || value.trim().isBlank() ? "Unspecified" : value.trim();
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
		String name = action != null && action.getName() != null && !action.getName().isBlank()
				? action.getName()
				: "Inform";
		if (isElectricTetherAction()) {
			return name + " AL: " + getElectricTetherAl();
		}
		if (isRemedyAction()) {
			return name + " AL: " + getRemedyAl();
		}
		return name;
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
			} else if (isBlinkAction()) {
				costValueLabel.setText("Aura " + trimNumber(getBlinkAuraCost()));
			} else if (isElectricTetherAction()) {
				costValueLabel.setText("Aura " + trimNumber(getElectricTetherAuraCost()));
			} else if (isRemedyAction()) {
				costValueLabel.setText("Aura " + trimNumber(getRemedyAuraCost()));
			} else if (isArcaneOrbAction()) {
				costValueLabel.setText(buildArcaneOrbCostDisplay());
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
		if (isBlinkAction()) {
			DataAction resolved = new DataAction(action);
			resolved.setActionType("Move");
			ArrayList<DataAction.CostPair> costs = new ArrayList<>();
			costs.add(new DataAction.CostPair("Aura", getBlinkAuraCost()));
			resolved.setCosts(costs);
			return resolved;
		}
		if (isElectricTetherAction()) {
			syncElectricTetherAlToAction();
			DataAction resolved = new DataAction(action);
			resolved.setAl(getElectricTetherAl());
			ArrayList<DataAction.CostPair> costs = new ArrayList<>();
			costs.add(new DataAction.CostPair("Aura", ELECTRIC_TETHER_AURA_COST_PER_AL));
			resolved.setCosts(costs);
			return resolved;
		}
		if (isRemedyAction()) {
			syncRemedyAlToAction();
			DataAction resolved = new DataAction(action);
			resolved.setAl(getRemedyAl());
			ArrayList<DataAction.CostPair> costs = new ArrayList<>();
			costs.add(new DataAction.CostPair("Aura", REMEDY_AURA_COST_PER_AL));
			resolved.setCosts(costs);
			return resolved;
		}
		if (isArcaneOrbAction()) {
			DataAction resolved = new DataAction(action);
			resolved.setActionType("Standard");
			ArrayList<DataAction.CostPair> costs = new ArrayList<>();
			costs.add(new DataAction.CostPair("R1", 3.0));
			costs.add(new DataAction.CostPair("Aura", getArcaneOrbAuraCost()));
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

	private boolean validateInformConfiguration(boolean showDialogs) {
		if (isDeployDroneAction()) {
			String error = character == null ? "No character loaded."
					: character.validateDroneDeploy(getSelectedDroneType(), getSelectedDroneTarget());
			if (error != null) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this, error, DEPLOY_DRONE_ACTION, JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			return true;
		}
		if (!isGambitMinionAction() && !isGambitLeaderAction()) {
			if (isBlinkAction()) {
				String destination = getBlinkDestination();
				if (destination == null || destination.isBlank()) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"Enter a destination for Blink.",
								BLINK_ACTION,
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				return true;
			}
			if (isElectricTetherAction()) {
				String target = getElectricTetherTarget();
				if (target == null || target.isBlank()) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"Enter a target creature for Electric Tether.",
								ELECTRIC_TETHER_ACTION,
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				return true;
			}
			if (isRemedyAction()) {
				String target = getRemedyTarget();
				if (target == null || target.isBlank()) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"Enter a target ally for " + getActionTitle() + ".",
								getActionTitle(),
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				return true;
			}
			if (isArcaneOrbAction()) {
				String location = getArcaneOrbLocation();
				if (location == null || location.isBlank()) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"Enter a target location within RANGE for Arcane Orb.",
								ARCANE_ORB_ACTION,
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				return true;
			}
			if (isSwapAction()) {
				if (getSelectedGambitMinion() == null) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"No summoned minion is available for Swap.",
								"Swap",
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				if (isTransposeAllySwap()) {
					String ally = getSwapAllyName();
					if (ally == null || ally.isBlank() || "Unspecified".equalsIgnoreCase(ally)) {
						if (showDialogs) {
							JOptionPane.showMessageDialog(this,
									"Enter the willing ally for Transpose.",
									"Transpose",
									JOptionPane.WARNING_MESSAGE);
						}
						return false;
					}
				}
				return true;
			}
			if (isRallyAction()) {
				if (rallyTargetField == null || rallyTargetField.getText() == null || rallyTargetField.getText().trim().isBlank()) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"Enter the target affected by Psychosis for Rally.",
								RALLY_ACTION,
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				if (rallyPsychosisField == null || rallyPsychosisField.getText() == null || rallyPsychosisField.getText().trim().isBlank()) {
					if (showDialogs) {
						JOptionPane.showMessageDialog(this,
								"Enter the Psychosis effect dispelled by Rally.",
								RALLY_ACTION,
								JOptionPane.WARNING_MESSAGE);
					}
					return false;
				}
				return true;
			}
			if (isFormationAction()) {
				if (isFormationAllyTarget()) {
					String ally = formationAllyField == null || formationAllyField.getText() == null
							? ""
							: formationAllyField.getText().trim();
					if (ally.isBlank()) {
						if (showDialogs) {
							JOptionPane.showMessageDialog(this,
									"Enter the target ally for Formation.",
									FORMATION_ACTION,
									JOptionPane.WARNING_MESSAGE);
						}
						return false;
					}
				}
				return true;
			}
			if (!isInspireAction()) {
				return true;
			}
			String variant = getInspireVariant();
			if (variant.isBlank()) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this,
							"No Inspire version is selected for this character.",
							"Inspire",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			if (inspireAllyField == null || inspireAllyField.getText() == null || inspireAllyField.getText().trim().isBlank()) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this,
							"Enter the target ally for Inspire.",
							"Inspire",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			String allyName = inspireAllyField.getText().trim();
			String selfName = character != null && character.getIdentity() != null && character.getIdentity().getName() != null
					? character.getIdentity().getName().trim()
					: "";
			if (!selfName.isBlank() && selfName.equalsIgnoreCase(allyName)) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this,
							"You cannot inspire yourself.",
							"Inspire",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			return true;
		}
		if (getSelectedGambitMinion() == null) {
			if (showDialogs) {
				JOptionPane.showMessageDialog(this,
						"No summoned minion is available for Gambit.",
						"Gambit",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		if (isGambitMinionAction()) {
			if (gambitEnemyField == null || gambitEnemyField.getText() == null || gambitEnemyField.getText().trim().isBlank()) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this,
							"Enter the enemy target for Gambit (Minion).",
							"Gambit",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			if (gambitAllyField == null || gambitAllyField.getText() == null || gambitAllyField.getText().trim().isBlank()) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this,
							"Enter the target ally for Gambit (Minion).",
							"Gambit",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
		}
		if (isGambitLeaderAction()) {
			if (gambitEnemyField == null || gambitEnemyField.getText() == null || gambitEnemyField.getText().trim().isBlank()) {
				if (showDialogs) {
					JOptionPane.showMessageDialog(this,
							"Enter the enemy target for Gambit (Leader).",
							"Gambit",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
		}
		return true;
	}

	private void applyInformPostConfirmEffects() {
		if (isDeployDroneAction()) {
			if (character != null) {
				String error = character.deployDrone(getSelectedDroneType(), getSelectedDroneTarget());
				if (error != null) {
					JOptionPane.showMessageDialog(this, error, DEPLOY_DRONE_ACTION, JOptionPane.WARNING_MESSAGE);
					return;
				}
				character.updateAll();
			}
			if (combatFrame != null) {
				combatFrame.refreshResourceDisplays();
			}
			if (sheetFrame != null) {
				sheetFrame.refreshMainPanel();
				sheetFrame.refreshImagePanel();
			}
			return;
		}
		if (isOverchargeAction()) {
			if (character != null) {
				character.setOverchargeActive(true);
				character.updateAll();
			}
			if (combatFrame != null) {
				combatFrame.refreshResourceDisplays();
			}
			if (sheetFrame != null) {
				sheetFrame.refreshMainPanel();
				sheetFrame.refreshImagePanel();
			}
			return;
		}
		if (isChargeUpAction()) {
			if (character != null) {
				character.restoreCharge(getChargeUpRestoreAmount());
				character.updateAll();
			}
			if (combatFrame != null) {
				combatFrame.refreshResourceDisplays();
			}
			if (sheetFrame != null) {
				sheetFrame.refreshMainPanel();
				sheetFrame.refreshImagePanel();
			}
			return;
		}
		if (isFormationAction()) {
			applyFormationEffects();
			return;
		}
		if (isArcaneOrbAction()) {
			applyArcaneOrbEffects();
			return;
		}
		if (isElectricTetherAction()) {
			applyElectricTetherEffects();
			return;
		}
		if (isRemedyAction()) {
			applyRemedyEffects();
			return;
		}
		if (!isGambitMinionAction() && !isGambitLeaderAction()) {
			if (!isInspireAction() && !isUpliftAction() && !isMusterAction()) {
				return;
			}
			DataStatus reminder = buildInformReminderStatus();
			if (reminder == null) {
				return;
			}
			character.getCombat().addStatus(reminder);
			character.updateAll();
			if (sheetFrame != null) {
				sheetFrame.refreshMainPanel();
				sheetFrame.refreshImagePanel();
			}
			return;
		}
		if (character == null || character.getCombat() == null) {
			return;
		}
		DataStatus reminder = buildGambitReminderStatus();
		if (reminder == null) {
			return;
		}
		PanelCharMinion.SummonedMinionCombatInfo minion = getSelectedGambitMinion();
		if (isGambitMinionAction() && minion != null && character.getMinions() != null) {
			MinionCombatState minionCombat = character.getMinions().ensureCombat(minion.slotTitle());
			if (minionCombat != null) {
				reminder.setAttribute("REMINDER");
				reminder.setName(GAMBIT_MINION_ACTION + " [" + minion.slotTitle() + "]");
				minionCombat.addStatus(reminder);
			} else {
				character.getCombat().addStatus(reminder);
			}
		} else {
			character.getCombat().addStatus(reminder);
		}
		character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
	}

	private DataStatus buildGambitReminderStatus() {
		PanelCharMinion.SummonedMinionCombatInfo minion = getSelectedGambitMinion();
		if (minion == null) return null;
		DataStatus reminder = new DataStatus();
		reminder.setAffinity("None");
		reminder.setAttribute("REMINDER");
		reminder.setSeverity(0.0);
		reminder.setDurationType("Turn");
		reminder.setDuration(1);
		if (isGambitMinionAction()) {
			reminder.setName(GAMBIT_MINION_ACTION);
			reminder.setDescription("Gambit (Minion): " + getGambitAllyName()
					+ " gains +" + trimNumber(getGambitMinionAttackBonus())
					+ " ATK against " + getGambitEnemyName()
					+ " on their next turn. Lost if not consumed before "
					+ minion.displayName() + "'s next turn.");
			return reminder;
		}
		if (isGambitLeaderAction()) {
			reminder.setName(GAMBIT_LEADER_ACTION);
			reminder.setDescription("Gambit (Leader): Next successful ally attack against "
					+ getGambitEnemyName() + " triggers " + minion.displayName()
					+ " to make an immediate attack of opportunity at +"
					+ trimNumber(getGambitLeaderAttackBonus())
					+ " ATK. Lost if not consumed before the leader's next turn.");
			return reminder;
		}
		return null;
	}

	private DataStatus buildInformReminderStatus() {
		if (isFormationAction()) {
			return buildFormationReminderStatus();
		}
		if (isInspireAction()) {
			return buildInspireReminderStatus();
		}
		if (isUpliftAction()) {
			DataStatus reminder = new DataStatus();
			reminder.setName(UPLIFT_ACTION);
			reminder.setAffinity("None");
			reminder.setAttribute("REMINDER");
			reminder.setSeverity(0.0);
			reminder.setDurationType("Round");
			reminder.setDuration(1);
			reminder.setDescription("Uplift: Allies within " + (getRange() <= 0 ? "melee range" : getRange() + " ft")
					+ " gain +" + trimNumber(getUpliftPrimaryBonus())
					+ " to their primary attribute until end of round. Apply any number of cleanse effects once each.");
			return reminder;
		}
		if (isMusterAction()) {
			DataStatus reminder = new DataStatus();
			reminder.setName(MUSTER_ACTION);
			reminder.setAffinity("None");
			reminder.setAttribute("REMINDER");
			reminder.setSeverity(0.0);
			reminder.setDurationType("Round");
			reminder.setDuration(1);
			reminder.setDescription("Muster: Allies within " + (getRange() <= 0 ? "melee range" : getRange() + " ft")
					+ " gain +" + trimNumber(getMusterBaseDamageBonus()) + " base damage until end of round.");
			return reminder;
		}
		return null;
	}

	private void applyFormationEffects() {
		if (character == null || character.getCombat() == null) return;
		clearExistingFormationEffects();
		character.getCombat().setTacticsAbilityUsedThisTurn(true);

		double bonus = getFormationAttackBonus();
		if (isFormationSelfTarget() && bonus > 0.0001 && character.getAttributes() != null) {
			DataStatus atkBonus = new DataStatus();
			atkBonus.setName(FORMATION_ACTION);
			atkBonus.setAttribute("BATK");
			atkBonus.setAffinity("None");
			atkBonus.setSeverity(bonus);
			atkBonus.setDurationType("Temporary");
			atkBonus.setDescription("Formation grants +" + trimNumber(bonus) + " ATK from adjacent minion gambit bonuses.");
			character.getAttributes().addStatus(atkBonus);

			DataStatus tracked = new DataStatus(atkBonus);
			tracked.setDurationType("Combat");
			tracked.setDuration(-1);
			character.getCombat().addStatus(tracked);
		}

		DataStatus reminder = buildFormationReminderStatus();
		if (reminder != null) {
			character.getCombat().addStatus(reminder);
		}
		character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
		if (combatFrame != null) {
			combatFrame.myTurn();
		}
	}

	private void clearExistingFormationEffects() {
		if (character == null) return;
		if (character.getAttributes() != null) {
			DataStatus template = new DataStatus();
			template.setName(FORMATION_ACTION);
			template.setAttribute("BATK");
			template.setDurationType("Temporary");
			character.getAttributes().removeStatusByStatus(template);
		}
		if (character.getCombat() != null) {
			character.getCombat().removeStatus(FORMATION_ACTION);
		}
	}

	private DataStatus buildFormationReminderStatus() {
		DataStatus reminder = new DataStatus();
		reminder.setName(FORMATION_ACTION);
		reminder.setAffinity("None");
		reminder.setAttribute("REMINDER");
		reminder.setSeverity(0.0);
		reminder.setDurationType("Combat");
		reminder.setDuration(-1);
		reminder.setDescription("Formation: " + getFormationTargetDisplay()
				+ " gains +" + trimNumber(getFormationAttackBonus())
				+ " ATK from adjacent minions (" + formatSelectedFormationMinionNames()
				+ "). Only one Formation can be active; this replaces any prior Formation.");
		return reminder;
	}

	private DataStatus buildInspireReminderStatus() {
		if (!isInspireAction()) return null;
		if (isInspireHasten()) {
			return null;
		}
		DataStatus reminder = new DataStatus();
		reminder.setAffinity("None");
		reminder.setAttribute("REMINDER");
		reminder.setSeverity(0.0);
		reminder.setDurationType("Combat");
		reminder.setDuration(-1);
		if (isInspireCompetence()) {
			reminder.setName("Inspire (Competence)");
			reminder.setDescription("Inspire (Competence): " + getInspireAllyName()
					+ " gains +" + trimNumber(getInspireBonus())
					+ " to attributes for one skill check. Remove on use or long rest.");
			return reminder;
		}
		if (isInspireFreedom()) {
			String originalDc = getInspireOriginalDc();
			String suffix = originalDc.isBlank()
					? " If already affected, the ally may make an immediate save against the effect's original DC."
					: " If already affected, the ally may make an immediate save against original DC " + originalDc + ".";
			reminder.setName("Inspire (Freedom)");
			reminder.setDescription("Inspire (Freedom): " + getInspireAllyName()
					+ " gains +" + trimNumber(getInspireBonus())
					+ " to a save against a loss of control effect." + suffix
					+ " Remove on use or long rest.");
			return reminder;
		}
		if (isInspireClarity()) {
			reminder.setName("Inspire (Clarity)");
			reminder.setDescription("Inspire (Clarity): " + getInspireAllyName()
					+ " may re-roll one die roll. Remove on use or long rest.");
			return reminder;
		}
		if (isInspireFortune()) {
			reminder.setName("Inspire (Fortune)");
			reminder.setDescription("Inspire (Fortune): " + getInspireAllyName()
					+ " may force an enemy to re-roll one die roll made against them. Remove on use or long rest.");
			return reminder;
		}
		return null;
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

	private double getBlinkAuraCost() {
		if (character == null) return 0.0;
		return Math.max(0, character.getLevel());
	}

	private String getBlinkDestination() {
		if (blinkDestinationField == null || blinkDestinationField.getText() == null) return "";
		return blinkDestinationField.getText().trim();
	}

	private String getBlinkDestinationDisplay() {
		String destination = getBlinkDestination();
		return destination.isBlank() ? "Unspecified" : destination;
	}

	private String buildArcaneOrbCostDisplay() {
		return "R1 3, Aura " + trimNumber(getArcaneOrbAuraCost());
	}

	private double getArcaneOrbAuraCost() {
		if (character == null) return 0.0;
		int level = Math.max(0, character.getLevel());
		return (double) level * level;
	}

	private int getArcaneOrbDurationTurns() {
		double control = Math.max(0.0, resolveCharacterStatValue("CTL"));
		return Math.max(1, (int) Math.ceil(control * 0.05));
	}

	private int getArcaneOrbCrossSection() {
		double range = Math.max(0.0, getDerivedCombatValue("RANGE"));
		double area = Math.max(0.0, resolveCharacterStatValue("AREA"));
		double raw = Math.cbrt(range / 5.0) * area;
		return Math.max(1, (int) Math.floor(raw));
	}

	private double getArcaneOrbDamage() {
		double baseDamage = Math.max(0.0, resolveCharacterStatValue("BDMG"));
		double totalDamage = Math.max(0.0, resolveCharacterStatValue("TDMG"));
		return (0.5 * baseDamage) + (0.25 * totalDamage);
	}

	private String getArcaneOrbLocation() {
		if (arcaneOrbLocationField == null || arcaneOrbLocationField.getText() == null) return "";
		return arcaneOrbLocationField.getText().trim();
	}

	private String getArcaneOrbLocationDisplay() {
		String location = getArcaneOrbLocation();
		return location.isBlank() ? "Unspecified" : location;
	}

	private String getArcaneOrbCornerDisplay() {
		if (arcaneOrbCornerBox == null || arcaneOrbCornerBox.getSelectedItem() == null) {
			return "Northwest";
		}
		return arcaneOrbCornerBox.getSelectedItem().toString();
	}

	private void refreshArcaneOrbCornerVisibility() {
		boolean evenCrossSection = (getArcaneOrbCrossSection() % 2) == 0;
		if (arcaneOrbCornerLabel != null) {
			arcaneOrbCornerLabel.setVisible(evenCrossSection);
		}
		if (arcaneOrbCornerBox != null) {
			arcaneOrbCornerBox.setVisible(evenCrossSection);
		}
	}

	private void applyArcaneOrbEffects() {
		if (character == null || character.getCombat() == null) return;

		int crossSection = getArcaneOrbCrossSection();
		int durationTurns = getArcaneOrbDurationTurns();
		double damage = getArcaneOrbDamage();
		String location = getArcaneOrbLocationDisplay();
		String centering = (crossSection % 2) == 0
				? ("centered on the " + getArcaneOrbCornerDisplay() + " corner of " + location)
				: ("centered on " + location);

		String description = "Arcane Orb at " + location + " (" + centering + "). "
				+ "Cross section/height: " + crossSection + " tiles. "
				+ "Creatures may move through freely. Adjacent creatures take "
				+ trimNumber(damage) + " damage ((50% x BDMG) + (25% x TDMG)) "
				+ "at the start of their turn and when moving adjacent. "
				+ "Max 2 damages per creature per turn. Duration: "
				+ durationTurns + (durationTurns == 1 ? " turn." : " turns.");

		character.getCombat().removeStatus(ARCANE_ORB_STATUS);
		DataStatus reminder = new DataStatus();
		reminder.setName(ARCANE_ORB_STATUS);
		reminder.setAffinity("None");
		reminder.setAttribute("REMINDER");
		reminder.setSeverity(0.0);
		reminder.setDurationType("Turn");
		reminder.setDuration(durationTurns);
		reminder.setDescription(description);
		character.getCombat().addStatus(reminder);
		character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
	}

	private void applyElectricTetherEffects() {
		if (character == null || character.getCombat() == null) return;

		syncElectricTetherAlToAction();
		int fullDuration = getElectricTetherFullDuration();
		int halfDuration = getElectricTetherHalfDuration();
		double range = getElectricTetherRange();
		String target = getElectricTetherTargetDisplay();
		double apply = getElectricTetherApplyValue();

		int saveChoice = JOptionPane.showConfirmDialog(
				this,
				"Electric Tether: " + target + " must make a REF save.\n"
						+ "Your APPLY value is " + trimEffectNumber(apply) + ".\n\n"
						+ "Did the target fail its REF save?\n"
						+ "(Yes = full " + fullDuration + " turns, No = halved "
						+ halfDuration + " turns)",
				ELECTRIC_TETHER_ACTION,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		boolean failedSave = saveChoice == JOptionPane.YES_OPTION;
		int durationTurns = failedSave ? fullDuration : halfDuration;

		String description = "Electric Tether on " + target + ". Range "
				+ trimEffectNumber(range) + " ft. While active, when " + target
				+ " moves, you may spend a reaction to move up to "
				+ trimEffectNumber(range) + " ft to follow them. Duration: "
				+ durationTurns + (durationTurns == 1 ? " turn" : " turns")
				+ (failedSave ? " (full)." : " (REF save halved).");

		character.getCombat().removeStatus(ELECTRIC_TETHER_STATUS);
		DataStatus reminder = new DataStatus();
		reminder.setName(ELECTRIC_TETHER_STATUS);
		reminder.setAffinity("Electricity");
		reminder.setAttribute("REMINDER");
		reminder.setSeverity(0.0);
		reminder.setDurationType("Turn");
		reminder.setDuration(durationTurns);
		reminder.setDescription(description);
		character.getCombat().addStatus(reminder);
		character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
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
		return "NAME:" + sanitizeForMacro(HOLY_BEACON_ACTION)
				+ "_DUR:Turn:1"
				+ "_" + StatusCodeParser.getPreferredAttributeAlias("ATK") + formatPackedStatusSeverity(-penalty)
				+ "_DESC:" + sanitizeForMacro("Reduce ATK by " + trimNumber(penalty) + " until the beginning of your next turn.");
	}

	private String buildEmbedStatusCode() {
		double embedValue = getEmbedValue();
		if (embedValue <= 0.0) return "";
		return "NAME:" + sanitizeForMacro(EMBED_ACTION)
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
