package eternity;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class FrameAttack extends JFrame {
	private static final long serialVersionUID = 1;
	private static final String ANGEL_REPLICATION_PROMPT = "Spend an Angel Point to replicate this technique?";
	private static final double ANGEL_REPLICATION_DAMAGE_MULTIPLIER = 0.5;
	private static final String FOLLOW_UP_SPECIALTY = "Follow Up";
	private static final String STEALTH_STRIKE_MARKER = "STEALTHSTRIKE";
	private static final String STEALTH_STRIKE_SNEAK_MARKER = "STEALTHSNEAK";
	private static final String STEALTH_STRIKE_SNEAK_STATUS = "Stealth Strike: Sneak Attack";
	private static final String STEALTH_STRIKE_STEALTH_STATUS = "Stealth Strike: Enter Stealth";
	private static final String STEALTH_STRIKE_FLANKING_STATUS = "Stealth Strike: Flanking";

	private final FrameSheet sheetFrame;
	private final FrameCombat combatFrame;
	private StoreCharData character;
	private DataAction action;
	private JComboBox<String> select1;
	private JComboBox<Integer> alSelect;
	private JComboBox<Double> radiusSelect;
	private JComboBox<String> damageType;
	private final boolean sneakAttackSelected;
	private final boolean unarmedProwessSelected;
	private final boolean favoredEnemySelected;
	private final int snipersDomainTdmgBonus;
	private final boolean technicalAttackSelected;
	private final boolean smiteAttackSelected;
	private final boolean stealthStrikeAttackSelected;
	private final DataTechnical activeTechnical;
	private final boolean consumeActionUseOnResolve;
	private boolean actionResolved;
	private boolean attackRollUsed;
	private boolean smiteReminderShown;
	private boolean smiteTargetDispositionResolved;
	private boolean smiteTargetAlliedToDeity;
	private boolean angelReplicationSelected;
	private boolean angelReplicationPromptHandled;
	
	private double attackBonus, result;
	private CardLayout cardLayout;
	private JPanel cardHolderPanel;
	private JPanel attackCardPanel;
	private JPanel castCardPanel;
	private JPanel activeCardPanel;
	private JTextArea descriptionArea;
	private JScrollPane descriptionPane;
	private JCheckBox flankingCheckBox;
	private JLabel costLabel;
	private JTextField costField;
	private static final String CARD_ATTACK = "attack";
	private static final String CARD_CAST = "cast";
	
	private final String[] YESNO = {"Yes", "No"};
	private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME", "DIVINE"};

	// UI elements (replacing FrameHelper utilities)
	private final JLabel headerL = new JLabel();
	private final JLabel[] labels = new JLabel[11];
	private final JTextField[] textFields = new JTextField[8];
	private final JSpinner[] numFields = new JSpinner[1];
	private final JButton[] buttons = new JButton[4];
	
	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action) {
		this(sheetFrame, combatFrame, character, action, false, false, false, 0, true);
	}

	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action, boolean sneakAttackSelected) {
		this(sheetFrame, combatFrame, character, action, sneakAttackSelected, false, false, 0, true);
	}

	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected) {
		this(sheetFrame, combatFrame, character, action, sneakAttackSelected, unarmedProwessSelected, false, 0, true);
	}

	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected, boolean favoredEnemySelected) {
		this(sheetFrame, combatFrame, character, action, sneakAttackSelected, unarmedProwessSelected, favoredEnemySelected, 0, true);
	}

	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected, boolean favoredEnemySelected, int snipersDomainTdmgBonus) {
		this(sheetFrame, combatFrame, character, action, sneakAttackSelected, unarmedProwessSelected, favoredEnemySelected, snipersDomainTdmgBonus, true);
	}

	FrameAttack (FrameSheet sheetFrame, FrameCombat combatFrame, StoreCharData character, DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected, boolean favoredEnemySelected, int snipersDomainTdmgBonus, boolean consumeActionUseOnResolve) {
		super("Attack Helper");
		this.sheetFrame = sheetFrame;
		this.combatFrame = combatFrame;
		this.character = character;
		this.action = action;
		this.sneakAttackSelected = sneakAttackSelected;
		this.unarmedProwessSelected = unarmedProwessSelected;
		this.favoredEnemySelected = favoredEnemySelected;
		this.snipersDomainTdmgBonus = Math.max(0, snipersDomainTdmgBonus);
		this.activeTechnical = combatFrame != null ? combatFrame.getPendingTechnicalData() : null;
		this.consumeActionUseOnResolve = consumeActionUseOnResolve;
		this.technicalAttackSelected = (combatFrame != null && combatFrame.hasPendingNextAttackStatusAttribute("TECH"))
				|| activeTechnical != null;
		this.smiteAttackSelected = combatFrame != null && combatFrame.hasPendingNextAttackStatusAttribute("SMITE");
		this.stealthStrikeAttackSelected = combatFrame != null
				&& combatFrame.hasPendingNextAttackStatusAttribute(STEALTH_STRIKE_MARKER);
		this.actionResolved = false;
		this.attackRollUsed = false;
		this.smiteReminderShown = false;
		this.smiteTargetDispositionResolved = false;
		this.smiteTargetAlliedToDeity = false;
		this.angelReplicationSelected = false;
		this.angelReplicationPromptHandled = false;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(550, 420);
		setLocationRelativeTo(null);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (!actionResolved && combatFrame != null) {
					combatFrame.cancelSalvoSequenceIfPending();
				}
			}
		});

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

		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(java.awt.Color.BLACK, 2),
				BorderFactory.createEmptyBorder(2, 4, 2, 4)));
		descriptionPane = new JScrollPane(descriptionArea);
		descriptionPane.setVisible(false);
		activeCardPanel.add(descriptionPane);

		flankingCheckBox = new JCheckBox("Flanking");
		flankingCheckBox.setVisible(false);
		flankingCheckBox.addActionListener(e -> refreshDisplayedCombatValues());
		activeCardPanel.add(flankingCheckBox);

		costLabel = new JLabel("Cost");
		costLabel.setVisible(false);
		activeCardPanel.add(costLabel);

		costField = new JTextField();
		costField.setEditable(false);
		costField.setVisible(false);
		activeCardPanel.add(costField);
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
		textFields[2].setText("*" + trimDisplayDouble(getDisplayedDamageMultiplier()));
		textFields[2].setVisible(true);
		textFields[2].setEditable(false);
		
		labels[3].setBounds(405, 80, 100, 20);
		labels[3].setText("Total Bonus");
		labels[3].setVisible(true);
		textFields[3].setBounds(405, 103, 100, 22);
		textFields[3].setText("+" + trimDisplayDouble(getDisplayedTotalDamage()));
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

		if (hasVisibleCost(action)) {
			costLabel.setBounds(285, 150, 120, 20);
			costLabel.setText("Cost");
			costLabel.setVisible(true);
			costField.setBounds(285, 173, 220, 22);
			costField.setText(buildCostDisplay(action));
			costField.setVisible(true);
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
		if (radiusSelect != null) {
			for (ActionListener al : radiusSelect.getActionListeners()) {
				radiusSelect.removeActionListener(al);
			}
			radiusSelect.setVisible(false);
			if (radiusSelect.getParent() != null) {
				radiusSelect.getParent().remove(radiusSelect);
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
		if (descriptionArea != null) {
			descriptionArea.setText("");
		}
		if (descriptionPane != null) {
			descriptionPane.setVisible(false);
		}
		if (flankingCheckBox != null) {
			flankingCheckBox.setSelected(false);
			flankingCheckBox.setEnabled(true);
			flankingCheckBox.setVisible(false);
		}
		if (costLabel != null) {
			costLabel.setVisible(false);
		}
		if (costField != null) {
			costField.setText("");
			costField.setVisible(false);
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
		attachComponent(targetPanel, radiusSelect);
		attachComponent(targetPanel, damageType);
		attachComponent(targetPanel, descriptionPane);
		attachComponent(targetPanel, flankingCheckBox);
		attachComponent(targetPanel, costLabel);
		attachComponent(targetPanel, costField);
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
		headerL.setText(buildActionHeaderText());
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

		if (!isDisarmAction()) {
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
			textFields[4].setText(trimDisplayDouble(getDisplayedDamageMultiplier()));
			textFields[4].setVisible(true);
			textFields[4].setEditable(false);

			labels[6].setBounds(385, 80, 100, 20);
			labels[6].setText("Total Damage");
			labels[6].setVisible(true);
			textFields[5].setBounds(385, 103, 100, 22);
			textFields[5].setText(trimDisplayDouble(getDisplayedTotalDamage()));
			textFields[5].setVisible(true);
			textFields[5].setEditable(false);
		}

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
			if (isEruptionAction()) {
				labels[0].setBounds(145, 205, 120, 20);
				labels[0].setText("Radius");
				labels[0].setVisible(true);
				ensureRadiusSelect();
				populateRadiusSelect();
				radiusSelect.setBounds(145, 228, 100, 22);
				radiusSelect.setVisible(true);
			}
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
		if (isDisarmAction()) {
			labels[0].setBounds(25, 205, 120, 20);
			labels[0].setText("Use Move");
			labels[0].setVisible(true);
			ensureYesNoSelect();
			select1.setBounds(25, 228, 100, 22);
			configureDisarmMoveToggle();
			select1.setVisible(true);
		}

		if (isAuraTechniqueAction() && isEruptionAction()) {
			refreshCostFieldDisplay(265, 205, 340, 228);
		} else {
			refreshCostFieldDisplay(145, 205, 220, 228);
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

		flankingCheckBox.setBounds(385, 228, 120, 22);
		configureFlankingCheckBox();

		descriptionPane.setBounds(25, 255, 485, 80);
		descriptionArea.setText(buildAttackDescription());
		descriptionArea.setCaretPosition(0);
		descriptionPane.setVisible(true);
		
		labels[3].setBounds(220, 330, 100, 20);
		labels[3].setText("Result");
		numFields[0].setBounds(220, 355, 100, 20);
		numFields[0].setValue(0.0);
		
		buttons[2].setBounds(195, 350, 145, 20);
		buttons[2].setText("Roll");
		buttons[2].setVisible(true);
		buttons[2].setEnabled(true);
		buttons[2].addActionListener(e -> copyRollToClipboard());
		
		buttons[0].setBounds(25, 350, 145, 20);
		buttons[0].setText("Cancel");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> cancelPressed());
		buttons[3].setBounds(365, 350, 145, 20);
		buttons[3].setText("Confirm");
		buttons[3].setVisible(true);
		buttons[3].addActionListener(e -> confirmPressed());
	}
	
	public void cancelPressed() {
		if (combatFrame != null) {
			combatFrame.cancelSalvoSequenceIfPending();
		}
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
		tempString += " --=SkillCheck|" + textFields[0].getText() + "*" + trimDisplayDouble(getDisplayedDamageMultiplier())
				+ "+" + getBaseDamage() + "*" + trimDisplayDouble(getDisplayedDamageMultiplier())
				+ "+" + trimDisplayDouble(getDisplayedTotalDamage()) + " [DieRoll] --+| Damage: [$SkillCheck]";
		tempString += " [br]&nbsp;&nbsp;Damage Code:[br]&nbsp;&nbsp;" + buildDamageCodeMacroExpression("[$SkillCheck]") + "}}";
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
		if (!resolveSmiteTargetDispositionIfNeeded()) {
			return;
		}
		if (openTechnicalFrameIfNeeded()) {
			this.setVisible(false);
			return;
		}
		if (!finishCombatActionIfNeeded()) {
			return;
		}
		completeResolvedAttackFlow();
	}
	
	private boolean finishCombatActionIfNeeded() {
		if (actionResolved) return true;
		if (combatFrame != null && action != null && action.getActionType() != null) {
			boolean resolved = angelReplicationSelected
					? combatFrame.resolveAttackAction(action, true, true, false)
					: combatFrame.resolveAttackAction(action, false, true, consumeActionUseOnResolve);
			if (!resolved) {
				return false;
			}
		}
		actionResolved = true;
		return true;
	}

	private void copyRollToClipboard() {
		if (character == null) return;
		if (!resolveSmiteTargetDispositionIfNeeded()) return;
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
		String actionSubtitle = buildActionHeaderText();
		if (alSelect != null && alSelect.getSelectedItem() != null) {
			actionSubtitle += " AL: " + alSelect.getSelectedItem();
		}
		if (isEruptionAction()) {
			actionSubtitle += " Radius: " + trimDisplayDouble(getSelectedEruptionRadius()) + " ft";
		}
		tempString += actionSubtitle + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		int bdmgMod = 0;
		double tdmgMod = 0.0;
		double dmgMulti = 1.0;
		if (character.getAttributes() != null) {
			bdmgMod = getBaseDamage();
			tdmgMod = getDisplayedTotalDamage();
			dmgMulti = getDisplayedDamageMultiplier();
		}
		tempString += " --+|Range: " + (getRange() <= 0 ? "Melee" : (getRange() + " ft"));
		if (isEruptionAction()) {
			tempString += " [br]&nbsp;&nbsp;Radius: " + trimDisplayDouble(getSelectedEruptionRadius()) + " ft";
		}
		
		tempString += buildPrimaryRollMacroBlock();

		tempString += "[br]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; vs " + textFields[7].getText();
		tempString += " --=RawPercentRoll2|1d21 + 9";
		tempString += " --=DamagePercentCount|1";
		tempString += " --=PercentRoll2|[$RawPercentRoll2] * 5 / 100";
		tempString += " --=BaseDamageRoll|[$PercentRoll2] * " + bdmgMod + " * " + dmgMulti + " + " + tdmgMod + " {FLOOR}";
		tempString += " --=DamageRoll|[$BaseDamageRoll]";
		tempString += " --+|Dmg Percent Roll: [$RawPercentRoll2] x 5% = [$PercentRoll2] [br]&nbsp;&nbsp;";
		tempString += "Damage Roll: [$DamageRoll] = [br]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ([$PercentRoll2] x " +  bdmgMod + ") x " + dmgMulti + " + " + tdmgMod;
		tempString += "[br]&nbsp;&nbsp;";
		if (isSneakAttackActive()) {
			tempString += "Sneak Attack: +0.25 damage multiplier, +" + getSneakAttackTotalDamageBonus() + " total damage[br]&nbsp;&nbsp;";
		}
		if (isUnarmedProwessActive()) {
			tempString += "Unarmed Prowess: +" + trimDisplayDouble(getUnarmedProwessAttackBonus()) + " attack[br]&nbsp;&nbsp;";
		}
		if (isFavoredEnemyActive()) {
			tempString += "Favored Enemy: +" + trimDisplayDouble(getFavoredEnemyAttackBonus())
					+ " attack, +" + trimDisplayDouble(getFavoredEnemyCritBonus()) + " crit[br]&nbsp;&nbsp;";
		}
		if (isStealthStrikeAttackActive()) {
			tempString += "Stealth Strike: circumstance bonuses to attack are doubled.[br]&nbsp;&nbsp;";
		}
		if (isFlankingActive()) {
			tempString += "Flanking bonus active: +" + getFlankingBonus() + "[br]&nbsp;&nbsp;";
		}
		if (isSmiteAttackActive()) {
			if (isSmiteTargetAlliedToDeity()) {
				tempString += "Smite: target is an ally of your deity; this attack deals no damage.[br]&nbsp;&nbsp;";
			} else {
				tempString += "Smite: Divine damage, targets DODGE, and ignores Resist.[br]&nbsp;&nbsp;";
			}
		}
		if (getSnipersDomainTotalDamageBonus() > 0) {
			tempString += "Sniper's Domain: +" + getSnipersDomainTotalDamageBonus() + " total damage[br]&nbsp;&nbsp;";
		}
		if (isEruptionAction()) {
			tempString += "Eruption: successful save halves damage; save by more than "
					+ trimDisplayDouble(getEruptionNoDamageMargin()) + " deals no damage.[br]&nbsp;&nbsp;";
		}
		tempString += "Damage Type: " + damageType.getSelectedItem().toString();
		tempString += "[br]&nbsp;&nbsp;Damage Code:[br]&nbsp;&nbsp;" + buildDamageCodeMacroExpression("[$DamageRoll]");
		
		if (!isSunderAction()) {
			tempString += "--=Critx1|" + trimDisplayDouble(2.0 + getCritIncrement())
					+ " --=Critx2|" + trimDisplayDouble(3.0 + getCritIncrement())
					+ " --=Critx3|" + trimDisplayDouble(4.0 + getCritIncrement())
					+ " --=Critx4|" + trimDisplayDouble(5.0 + getCritIncrement())
					+ " --=Crit1|[$BaseAttackRoll]/[$Critx1] {FLOOR} + " + getFlankingBonus()
					+ " --=Crit2|[$BaseAttackRoll]/[$Critx2] {FLOOR} + " + getFlankingBonus()
					+ " --=Crit3|[$BaseAttackRoll]/[$Critx3] {FLOOR} + " + getFlankingBonus()
					+ " --=Crit4|[$BaseAttackRoll]/[$Critx4] {FLOOR} + " + getFlankingBonus()
					+ " --=Critd1|[$DamageRoll]*[$Critx1]*" + getCritDamage()
					+ " --=Critd2|[$DamageRoll]*[$Critx2]*" + getCritDamage()
					+ " --=Critd3|[$DamageRoll]*[$Critx3]*" + getCritDamage()
					+ " --=Critd4|[$DamageRoll]*[$Critx4]*" + getCritDamage();
			tempString += " --+|Critical Threat [br]&nbsp;&nbsp;"; 
			tempString += "  Crit Multi: [$Critx1] [$Critx2] [$Critx3] [$Critx4] [br]&nbsp;&nbsp; Crit AC #: [$Crit1] [$Crit2] [$Crit3] [$Crit4] [br]&nbsp;&nbsp; Crit DMG [$Critd1] [$Critd2] [$Critd3] [$Critd4]";
		}
		
		tempString += " --=Crush|" + getCrush() + " --+|Crush:  [$Crush]";
		tempString += buildTechnicalSaveMacroSuffix();
		tempString += " }}";

		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private String buildDamageCodeMacroExpression(String amountExpression) {
		String selectedDamageType = getSelectedDamageType();
		String normalizedType = selectedDamageType == null || selectedDamageType.isBlank()
				? "PHY"
				: selectedDamageType.trim().toUpperCase(java.util.Locale.ROOT);
		String damageAmount;
		if (isSmiteAttackActive() && isSmiteTargetAlliedToDeity()) {
			damageAmount = "0";
		} else {
			damageAmount = amountExpression == null || amountExpression.isBlank() ? "0" : amountExpression;
		}
		StringBuilder code = new StringBuilder();
		code.append("DMG_TYPE:").append(normalizedType);
		code.append("_AMT:").append(damageAmount);
		code.append("_CRUSH:").append(trimDisplayDouble(getCrush()));
		if (isSmiteAttackActive()) {
			code.append("_SMITE:1");
		}
		return code.toString();
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
		int flankingBonus = getFlankingBonus();
		if (isStandardCastAction()) {
			return " --=BaseAttackRoll|" + getApplication()
					+ " --=AttackRoll|[$BaseAttackRoll] + " + flankingBonus
					+ " --+|Application: [$AttackRoll]";
		}
		String block = "";
		block += buildPercentRollBlock("Attack", "RawPercentRoll", "RawPercentBonus", "RawPercentCount", "AttackPercentRoll", "FinalRawPercentRoll", "PercentRoll");
		block += " --=BaseAttackRoll|[$PercentRoll] * " + getPrimaryRollValue() + " {FLOOR}";
		block += " --=AttackRoll|[$BaseAttackRoll] + " + flankingBonus;
		block += " --+|Atk Percent Roll: [$FinalRawPercentRoll] x 5% = [$PercentRoll] [br]&nbsp;&nbsp; ";
		block += " " + getPrimaryRollLabel() + " Roll: [$AttackRoll] = [$PercentRoll] x " + getPrimaryRollValue();
		if (flankingBonus > 0) {
			block += " + " + flankingBonus;
		}
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
		if (isStandardBaselineAction()) {
			return getBaselineWeaponAttackRange();
		}
		if (action != null) {
			if (action.getRanged() > 0) {
				return getDerivedCombatValue("RANGE");
			}
			return action.getRanged();
		}
		return 0;
	}

	private boolean isEruptionAction() {
		return action != null && action.hasModifierAttribute("RADIUS");
	}

	private double getEruptionMaxRadius() {
		if (!isEruptionAction() || action == null) return 0.0;
		return Math.max(0.0, action.evaluateModifierAttributeValue("RADIUS"));
	}

	private Double getSelectedEruptionRadiusOrNull() {
		if (radiusSelect == null) return null;
		Object selected = radiusSelect.getSelectedItem();
		return selected instanceof Double value ? value : null;
	}

	private double getSelectedEruptionRadius() {
		Double selected = getSelectedEruptionRadiusOrNull();
		if (selected != null) {
			return Math.max(0.0, selected);
		}
		double maxRadius = getEruptionMaxRadius();
		return maxRadius <= 0.0 ? 0.0 : roundRadius(Math.max(5.0, maxRadius));
	}

	private double roundRadius(double radius) {
		return Math.round(radius * 2.0) / 2.0;
	}

	private double getEruptionNoDamageMargin() {
		return Math.max(0.0, getApplication() * 0.25);
	}

	private int getAttack() {
		if (action != null) {
			return Math.max(0, action.getAtk()
					+ (int)Math.round(getUnarmedProwessAttackBonus())
					+ (int)Math.round(getFavoredEnemyAttackBonus()));
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
		if (isSmiteAttackActive()) {
			return "DODGE";
		}
		if (isStandardBaselineAction() && combatFrame != null) {
			DataItemWeapon weapon = combatFrame.getSelectedWeaponForAttackFrame();
			String weaponAttack = weapon == null ? null : weapon.getAttack();
			if (weaponAttack != null && !weaponAttack.isBlank()) {
				return weaponAttack.trim().toUpperCase();
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
		if (isSmiteAttackActive()) {
			resetDamageTypeOptions(DMGTYPE);
			selectDamageType("DIVINE");
			damageType.setEnabled(false);
			return;
		}
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
		if (combatFrame == null || !usesWeaponDamageType()) return null;
		DataItemWeapon weapon = combatFrame.getSelectedWeaponForAttackFrame();
		if (weapon == null) return null;
		String damage = weapon.getDamage();
		return (damage == null || damage.isBlank()) ? null : damage.trim();
	}

	private boolean usesWeaponDamageType() {
		if (action == null) return false;
		String affinity = action.getAffinity();
		return affinity == null
				|| affinity.isBlank()
				|| "None".equalsIgnoreCase(affinity.trim());
	}

	private boolean isSmiteAttackActive() {
		return smiteAttackSelected;
	}

	private void showSmiteReminderIfNeeded() {
		if (!isSmiteAttackActive() || smiteReminderShown) return;
		smiteReminderShown = true;
		if (isSmiteTargetAlliedToDeity()) {
			JOptionPane.showMessageDialog(
					this,
					"This smite target is an ally of your deity, so the attack dealt no damage.",
					"Smite Reminder",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		JOptionPane.showMessageDialog(
				this,
				"If this smite attack dealt damage equal to or greater than 40% of the target's HP, they may be overcome by divine wrath. Bosses, deviants, or creatures devoted to another deity may resist or ignore this at narrator discretion.",
				"Smite Reminder",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private boolean resolveSmiteTargetDispositionIfNeeded() {
		if (!isSmiteAttackActive() || smiteTargetDispositionResolved) return true;
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Is the target an ally of your deity?",
				"Smite",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
			return false;
		}
		smiteTargetDispositionResolved = true;
		smiteTargetAlliedToDeity = choice == JOptionPane.YES_OPTION;
		return true;
	}

	private boolean isSmiteTargetAlliedToDeity() {
		return smiteTargetDispositionResolved && smiteTargetAlliedToDeity;
	}

	private boolean isStandardBaselineAction() {
		if (action == null || action.getName() == null) return false;
		return "Standard Attack".equalsIgnoreCase(action.getName())
				|| "Standard Cast".equalsIgnoreCase(action.getName())
				|| isFollowUpAction();
	}

	private boolean isFollowUpAction() {
		return action != null
				&& action.getName() != null
				&& FOLLOW_UP_SPECIALTY.equalsIgnoreCase(action.getName().trim());
	}

	private int getBaselineWeaponAttackRange() {
		if (combatFrame == null) {
			return action != null ? action.getRanged() : 0;
		}
		DataItemWeapon weapon = combatFrame.getSelectedWeaponForAttackFrame();
		if (weapon == null || isMeleeWeapon(weapon)) {
			return 0;
		}
		return getDerivedCombatValue("RANGE");
	}

	private boolean isMeleeWeapon(DataItemWeapon weapon) {
		if (weapon == null) return true;
		String category = weapon.getCategory() == null ? "" : weapon.getCategory().toLowerCase();
		String type = weapon.getType() == null ? "" : weapon.getType().toLowerCase();
		String slot = weapon.getSlot() == null ? "" : weapon.getSlot().toLowerCase();
		return category.contains("melee") || type.contains("melee") || slot.contains("melee");
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
			return (int)Math.round(action.getBdmg() + getActiveWeaponSpecializationDamageBonus());
		}
		return getDerivedCombatValue("BDMG");
	}

	private int getTotalDamage() {
		if (action != null) {
			return (int)Math.round(action.getTdmg() + getActiveWeaponSpecializationDamageBonus());
		}
		return getDerivedCombatValue("TDMG");
	}

	private double getActiveWeaponSpecializationDamageBonus() {
		if (character == null || character.getSpecials() == null || combatFrame == null) return 0.0;
		DataItemWeapon activeWeapon = combatFrame.getSelectedWeaponForAttackFrame();
		if (activeWeapon == null) return 0.0;
		String activeWeaponType = normalizeWeaponType(activeWeapon.getType());
		if (activeWeaponType == null) return 0.0;
		if (!hasMatchingActiveWeaponSpecialization(activeWeaponType)) return 0.0;
		return getPrimaryAttributeDamageContribution() * 0.1;
	}

	private boolean hasMatchingActiveWeaponSpecialization(String activeWeaponType) {
		if (activeWeaponType == null || character == null || character.getSpecials() == null) return false;
		for (DataSpecialty specialty : character.getSpecials().getAllSpecialties()) {
			if (specialty == null) continue;
			int specialtyId = specialty.getId();
			if (specialtyId < 201 || specialtyId > 228) continue;
			String specializedWeaponType = extractSpecializationWeaponType(specialty);
			if (specializedWeaponType != null && specializedWeaponType.equalsIgnoreCase(activeWeaponType)) {
				return true;
			}
		}
		return false;
	}

	private String extractSpecializationWeaponType(DataSpecialty specialty) {
		if (specialty == null || specialty.getName() == null) return null;
		String name = specialty.getName().trim();
		int open = name.indexOf('(');
		int close = name.lastIndexOf(')');
		if (open < 0 || close <= open) return null;
		return normalizeWeaponType(name.substring(open + 1, close));
	}

	private String normalizeWeaponType(String weaponType) {
		if (weaponType == null || weaponType.isBlank()) return null;
		return weaponType.trim();
	}

	private double getPrimaryAttributeDamageContribution() {
		CharAttributes attributes = attrs();
		if (attributes == null) return 0.0;
		String primaryAttribute = resolvePrimaryAttributeKey();
		if (primaryAttribute == null) return 0.0;
		return attributes.calcStatusValue(primaryAttribute) * 0.25;
	}

	private String resolvePrimaryAttributeKey() {
		CharIdentity identity = id();
		if (identity == null || identity.getCharClass() == null) return null;
		StoreRuleManager dq = new StoreRuleManager();
		DataClass cls = dq.getClassByName(identity.getCharClass());
		if (cls == null || cls.getPrimaryAtt() == null || cls.getPrimaryAtt().isBlank()) return null;
		return cls.getPrimaryAtt().trim().toUpperCase(java.util.Locale.ROOT);
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
		if (character != null && character.getAttributes() != null) {
			double val = character.getAttributes().calcStatusValue("CRITDMG");
			return val > 0 ? val : 1.0;
		}
		return 1.0;
	}

	private double getCritIncrement() {
		if (character != null && character.getAttributes() != null) {
			return Math.max(0.0, character.getAttributes().calcStatusValue("CRIT")) + getFavoredEnemyCritBonus();
		}
		return getFavoredEnemyCritBonus();
	}

	private double getCrush() {
		if (character != null && character.getAttributes() != null) {
			return Math.max(0.0, character.getAttributes().calcStatusValue("CRUSH"));
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

	private boolean isSunderAction() {
		return action != null && "Sunder".equalsIgnoreCase(action.getName());
	}

	private boolean isDisarmAction() {
		return action != null && "Disarm".equalsIgnoreCase(action.getName());
	}

	private boolean isStandardCastAction() {
		return action != null && "Standard Cast".equalsIgnoreCase(action.getName());
	}

	private boolean isAuraTechniqueAction() {
		return action != null
				&& action.getSource() != null
				&& "Aura".equalsIgnoreCase(action.getSource());
	}

	private void ensureYesNoSelect() {
		if (select1 == null) {
			select1 = new JComboBox<String>(YESNO);
		}
		if (select1.getParent() == null) {
			activeCardPanel.add(select1);
		}
	}

	private void configureDisarmMoveToggle() {
		if (select1 == null) return;
		for (ActionListener listener : select1.getActionListeners()) {
			select1.removeActionListener(listener);
		}
		select1.setSelectedIndex(hasMoveActionCost(action) ? 0 : 1);
		applyDisarmMoveCostSelection();
		select1.addActionListener(e -> {
			applyDisarmMoveCostSelection();
			refreshCostFieldDisplay(145, 205, 220, 228);
		});
	}

	private void applyDisarmMoveCostSelection() {
		if (!isDisarmAction() || action == null || select1 == null) return;
		ArrayList<DataAction.CostPair> costs = new ArrayList<>();
		if (select1.getSelectedIndex() == 0) {
			costs.add(new DataAction.CostPair("MoveAction", 1.0));
		} else {
			costs.add(new DataAction.CostPair("None", 0.0));
		}
		action.setCosts(costs);
	}

	private boolean hasMoveActionCost(DataAction dataAction) {
		if (dataAction == null || dataAction.getCosts() == null) return false;
		for (DataAction.CostPair cost : dataAction.getCosts()) {
			if (cost == null || cost.getType() == null) continue;
			if ("MoveAction".equalsIgnoreCase(cost.getType().trim()) && Math.abs(cost.getValue()) > 0.0001) {
				return true;
			}
		}
		return false;
	}

	private void refreshCostFieldDisplay(int labelX, int labelY, int fieldWidth, int fieldY) {
		if (costLabel == null || costField == null) return;
		if (hasVisibleCost(action)) {
			costLabel.setBounds(labelX, labelY, 100, 20);
			costLabel.setText("Cost");
			costLabel.setVisible(true);
			costField.setBounds(labelX, fieldY, fieldWidth, 22);
			costField.setText(buildCostDisplay(action));
			costField.setVisible(true);
			return;
		}
		costLabel.setVisible(false);
		costField.setText("");
		costField.setVisible(false);
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
		String attackType = action.getAtkType();
		if (attackType == null) return false;
		return "AC".equalsIgnoreCase(attackType)
				|| "Dodge".equalsIgnoreCase(attackType)
				|| "Armor".equalsIgnoreCase(attackType);
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

	private void ensureRadiusSelect() {
		if (radiusSelect == null) {
			radiusSelect = new JComboBox<Double>();
		}
		if (radiusSelect.getParent() == null) {
			activeCardPanel.add(radiusSelect);
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

	private void populateRadiusSelect() {
		if (radiusSelect == null || action == null) return;
		Double selectedRadius = getSelectedEruptionRadiusOrNull();
		radiusSelect.removeAllItems();
		double maxRadius = Math.max(5.0, getEruptionMaxRadius());
		for (double radius = 5.0; radius <= maxRadius + 0.0001; radius += 2.5) {
			radiusSelect.addItem(roundRadius(radius));
		}
		if (selectedRadius == null || selectedRadius < 5.0 || selectedRadius > maxRadius + 0.0001) {
			selectedRadius = roundRadius(maxRadius);
		} else {
			selectedRadius = roundRadius(selectedRadius);
		}
		radiusSelect.setSelectedItem(selectedRadius);
		for (ActionListener listener : radiusSelect.getActionListeners()) {
			radiusSelect.removeActionListener(listener);
		}
		radiusSelect.addActionListener(e -> refreshDisplayedCombatValues());
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

	private String buildAttackDescription() {
		StringBuilder text = new StringBuilder();
		if (isEruptionAction()) {
			text.append("Eruption radius: ")
					.append(trimDisplayDouble(getSelectedEruptionRadius()))
					.append(" ft.")
					.append(System.lineSeparator());
			text.append("Successful save: half damage. Save by more than ")
					.append(trimDisplayDouble(getEruptionNoDamageMargin()))
					.append(": no damage.")
					.append(System.lineSeparator());
		}
		if (isFollowUpAction()) {
			text.append("Follow Up active: increase total damage by ")
					.append(trimDisplayDouble(getFollowUpTotalDamageBonus()))
					.append(".")
					.append(System.lineSeparator());
		}
		if (isTechnicalAttackActive()) {
			text.append("Technical attack active.")
					.append(System.lineSeparator());
			appendTechnicalDetails(text);
		}
		if (isStealthStrikeAttackActive()) {
			text.append("Stealth Strike active: circumstance bonuses to attack are doubled.")
					.append(System.lineSeparator());
		}
		if (isSneakAttackActive()) {
			text.append("Sneak Attack active: increase damage multiplier by 0.25 and total damage by ")
					.append(getSneakAttackTotalDamageBonus())
					.append(".")
					.append(System.lineSeparator());
		}
		if (isUnarmedProwessActive()) {
			text.append("Unarmed Prowess active: increase attack by ")
					.append(trimDisplayDouble(getUnarmedProwessAttackBonus()))
					.append(".")
					.append(System.lineSeparator());
		}
		if (isFavoredEnemyActive()) {
			text.append("Favored Enemy active: increase attack by ")
					.append(trimDisplayDouble(getFavoredEnemyAttackBonus()))
					.append(" and crit by ")
					.append(trimDisplayDouble(getFavoredEnemyCritBonus()))
					.append(".")
					.append(System.lineSeparator());
		}
		if (isFlankingActive()) {
			text.append("Flanking active: target has a penalty to DEF of ")
					.append(getFlankingBonus())
					.append(".")
					.append(System.lineSeparator());
		}
		if (getSnipersDomainTotalDamageBonus() > 0) {
			text.append("Sniper's Domain active: increase total damage by ")
					.append(getSnipersDomainTotalDamageBonus())
					.append(".")
					.append(System.lineSeparator());
		}
		if (angelReplicationSelected) {
			text.append("Angel replication active: damage multiplier halved.")
					.append(System.lineSeparator());
		}
		if (isAuraTechniqueAction()) {
			text.append(System.lineSeparator())
					.append("This action scales with the selected AL.");
		}
		if (isFullAttackAction()) {
			text.append(System.lineSeparator())
					.append("This action includes ")
					.append(getExtraAttacks())
					.append(" extra attacks.");
		}
		if (isSunderAction()) {
			text.append("Select target weapon/equipment worn by target character.")
					.append(System.lineSeparator());
			text.append("If this attack succeeds, deal all damage of this attack to that item.")
					.append(System.lineSeparator());
			text.append("The item is afforded all mitigation of its bearer (mitigation, shield, etc).")
					.append(System.lineSeparator());
			text.append("If the item is dealt cumulative damage equal to 1/4 the user's max hp,")
					.append(System.lineSeparator());
			text.append("the item is damaged and becomes unusable until repaired.");					 
		}
		if (isDisarmAction()) {
			if (!text.isEmpty()) {
				text.append(System.lineSeparator()).append(System.lineSeparator());
			}
			text.append("A successful Disarm will force a creature to drop a target held object.")
					.append(System.lineSeparator());
			text.append("If you expend a move action as well, you may displace the object up to ")
					.append(10)
					.append("ft.")
					.append(System.lineSeparator());
			text.append("If you displace the object into tile with a creature in it, they can catch it.");
		}
		return text.toString();
	}

	private void appendTechnicalDetails(StringBuilder text) {
		if (text == null || activeTechnical == null) return;
		text.append("Category: ")
				.append(safeTechnicalValue(activeTechnical.getCategory(), "None"))
				.append(System.lineSeparator());
		String save = activeTechnical.getSave();
		if (save != null && !save.isBlank() && !"None".equalsIgnoreCase(save.trim())) {
			text.append("Save: ")
					.append(save.trim())
					.append(System.lineSeparator());
		}
		String description = activeTechnical.getDescription();
		if (description != null && !description.isBlank()) {
			text.append(description.trim())
					.append(System.lineSeparator());
		}
		text.append(System.lineSeparator());
	}

	private String buildTechnicalSaveMacroSuffix() {
		if (!isTechnicalAttackActive() || activeTechnical == null) return "";
		String save = activeTechnical.getSave();
		if (save == null || save.isBlank() || "None".equalsIgnoreCase(save.trim())) return "";
		return " --+|Technical Save: " + save.trim() + " [br]&nbsp;&nbsp;APPLY: " + getApplication();
	}

	private String buildActionHeaderText() {
		String name = action != null && action.getName() != null ? action.getName() : "Attack";
		if (isTechnicalAttackActive()) {
			return name + " [Technical]";
		}
		return name;
	}

	private String formatDamageDisplay(int baseDamage, double damageMultiplier, int totalDamage) {
		return "(" + baseDamage + " +/- 50%) * " + damageMultiplier + " + " + totalDamage;
	}

	private boolean isSneakAttackActive() {
		return sneakAttackSelected
				&& character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Sneak Attack");
	}

	private boolean isUnarmedProwessActive() {
		return unarmedProwessSelected
				&& character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Unarmed Prowess");
	}

	private boolean isFavoredEnemyActive() {
		return favoredEnemySelected
				&& character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Favored Enemy");
	}

	private boolean isStealthStrikeAttackActive() {
		return stealthStrikeAttackSelected;
	}

	private boolean isFlankingActive() {
		return flankingCheckBox != null && flankingCheckBox.isSelected();
	}

	private void configureFlankingCheckBox() {
		if (flankingCheckBox == null) return;
		boolean forcedFlanking = hasActiveFlankingStatus();
		flankingCheckBox.setSelected(forcedFlanking);
		flankingCheckBox.setEnabled(!forcedFlanking);
		flankingCheckBox.setVisible(true);
	}

	private boolean hasActiveFlankingStatus() {
		if (character == null || character.getCombat() == null || character.getCombat().getCombatStatus() == null) return false;
		for (DataStatus status : character.getCombat().getCombatStatus()) {
			if (status == null || status.getAttribute() == null) continue;
			if (!"FLANKING".equalsIgnoreCase(status.getAttribute().trim())) continue;
			if (status.getSeverity() <= 0) continue;
			return true;
		}
		return false;
	}

	private int getFlankingBonus() {
		if (!isFlankingActive()) return 0;
		int baseBonus = Math.max(0, getLevel());
		return isStealthStrikeAttackActive() ? baseBonus * 2 : baseBonus;
	}

	private boolean isTechnicalAttackActive() {
		return technicalAttackSelected;
	}

	private boolean isAttackAction() {
		return action != null
				&& action.getCategory() != null
				&& "Attack".equalsIgnoreCase(action.getCategory().trim());
	}

	private boolean openTechnicalFrameIfNeeded() {
		if (!isTechnicalAttackActive() || activeTechnical == null) return false;
		FrameTechnical technicalFrame = new FrameTechnical(
				this,
				character,
				activeTechnical,
				this::confirmCombatAfterTechnical,
				this::restoreAfterTechnicalCancel);
		technicalFrame.setVisible(true);
		return true;
	}

	private void confirmCombatAfterTechnical() {
		if (!finishCombatActionIfNeeded()) {
			restoreAfterTechnicalCancel();
			return;
		}
		completeResolvedAttackFlow();
	}

	private void restoreAfterTechnicalCancel() {
		if (!this.isDisplayable()) return;
		this.setVisible(true);
	}

	private double getDisplayedDamageMultiplier() {
		double multiplier = getCharDmgMulti();
		if (isSneakAttackActive()) {
			multiplier += 0.25;
		}
		if (angelReplicationSelected) {
			multiplier *= ANGEL_REPLICATION_DAMAGE_MULTIPLIER;
		}
		return multiplier;
	}

	private double getDisplayedTotalDamage() {
		double totalDamage = getTotalDamage();
		totalDamage += getFollowUpTotalDamageBonus();
		if (isSneakAttackActive()) {
			totalDamage += getSneakAttackTotalDamageBonus();
		}
		totalDamage += getSnipersDomainTotalDamageBonus();
		return totalDamage;
	}

	private double getFollowUpTotalDamageBonus() {
		if (!isFollowUpAction() || character == null || character.getCombat() == null) {
			return 0.0;
		}
		return Math.max(0.0, character.getCombat().getDamageDealtThisTurn() * 0.1);
	}

	private int getSneakAttackTotalDamageBonus() {
		if (character == null || character.getIdentity() == null) return 0;
		int level = Math.max(0, character.getIdentity().getLevel());
		return (level+1) * (level+1);
	}

	private double getUnarmedProwessAttackBonus() {
		if (!isUnarmedProwessActive() || character == null || character.getIdentity() == null) return 0.0;
		int level = Math.max(0, character.getIdentity().getLevel());
		return level * level * 0.5;
	}

	private double getFavoredEnemyAttackBonus() {
		if (!isFavoredEnemyActive() || character == null || character.getIdentity() == null) return 0.0;
		double baseBonus = Math.max(0, character.getIdentity().getLevel());
		return isStealthStrikeAttackActive() ? baseBonus * 2.0 : baseBonus;
	}

	private double getFavoredEnemyCritBonus() {
		if (!isFavoredEnemyActive() || character == null || character.getIdentity() == null) return 0.0;
		return Math.max(0, character.getIdentity().getLevel());
	}

	private int getSnipersDomainTotalDamageBonus() {
		return Math.max(0, snipersDomainTdmgBonus);
	}

	private String trimDisplayDouble(double value) {
		if (Math.abs(value - Math.rint(value)) <= 0.0001) {
			return Integer.toString((int)Math.round(value));
		}
		return String.format(java.util.Locale.ROOT, "%.3f", value)
				.replaceAll("0+$", "")
				.replaceAll("\\.$", "");
	}

	private String buildCostDisplay(DataAction dataAction) {
		if (dataAction == null || dataAction.getCosts() == null || dataAction.getCosts().isEmpty()) {
			return "None";
		}
		ArrayList<String> values = new ArrayList<>();
		for (DataAction.CostPair cost : dataAction.getCosts()) {
			if (cost == null || cost.getType() == null) continue;
			values.add(cost.getType() + " " + trimDisplayDouble(cost.getValue()));
		}
		return values.isEmpty() ? "None" : String.join(", ", values);
	}

	private boolean hasVisibleCost(DataAction dataAction) {
		if (dataAction == null || dataAction.getCosts() == null) return false;
		for (DataAction.CostPair cost : dataAction.getCosts()) {
			if (cost == null || cost.getType() == null) continue;
			if ("None".equalsIgnoreCase(cost.getType().trim()) && Math.abs(cost.getValue()) <= 0.0001) {
				continue;
			}
			return true;
		}
		return false;
	}

	private DataColor getDisplayColor() {
		StoreRuleManager dq = new StoreRuleManager();
		String race = Optional.ofNullable(id()).map(CharIdentity::getRace).orElse("Default");
		DataColor color = dq.getColorByTitle(race);
		if (color != null) return color;
		// Fallback neutral colors
		return new DataColor("Default", 0, 0, 0, 255, 255, 255);
	}

	private String safeTechnicalValue(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value.trim();
	}

	private void refreshDisplayedCombatValues() {
		if (textFields[4] != null && textFields[4].isVisible()) {
			textFields[4].setText(trimDisplayDouble(getDisplayedDamageMultiplier()));
		}
		if (textFields[5] != null && textFields[5].isVisible()) {
			textFields[5].setText(trimDisplayDouble(getDisplayedTotalDamage()));
		}
		if (descriptionArea != null) {
			descriptionArea.setText(buildAttackDescription());
			descriptionArea.setCaretPosition(0);
		}
	}

	private void closeFrame() {
		this.setVisible(false);
		this.dispose();
	}

	private void completeResolvedAttackFlow() {
		showSmiteReminderIfNeeded();
		resolveStealthStrikeIfNeeded();
		maybeOfferClericDomainShare();
		promptFollowUpDamageDealtIfNeeded();
		if (handleAngelReplicationChoiceAfterResolution()) {
			return;
		}
		if (combatFrame != null) {
			combatFrame.updateCharacter(character);
		}
		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshMainPanel();
		}
		if (combatFrame != null) {
			combatFrame.continueSalvoSequenceAfterResolvedAttack();
		}
		closeFrame();
	}

	private void resolveStealthStrikeIfNeeded() {
		if (!isStealthStrikeAttackActive() || character == null || character.getCombat() == null) {
			return;
		}
		int techPoints = 0;
		int successChoice = JOptionPane.showConfirmDialog(
				this,
				"Was the Stealth Strike attack successful?",
				"Stealth Strike",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (successChoice != JOptionPane.YES_OPTION) {
			return;
		}
		techPoints++;
		int saveDc = resolveStealthStrikeSaveDc();
		int saveChoice = JOptionPane.showConfirmDialog(
				this,
				"The target must make a WILL save (DC = " + saveDc + ").\nDid the target fail the save?",
				"Stealth Strike",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (saveChoice == JOptionPane.YES_OPTION) {
			techPoints++;
		}
		applyStealthStrikeTechPointEffects(techPoints);
	}

	private int resolveStealthStrikeSaveDc() {
		double dexValue = attrs() == null ? 0.0 : Math.max(0.0, attrs().calcStatusValue("DEX"));
		return Math.max(0, getLevel() + (int)Math.round(dexValue));
	}

	private void applyStealthStrikeTechPointEffects(int techPoints) {
		if (techPoints <= 0 || character == null || character.getCombat() == null) return;

		ArrayList<String> options = new ArrayList<>();
		options.add("All attacks can deal Sneak Attack damage until end of turn");
		options.add("Enter stealth at end of turn");
		options.add("Gain flanking on all attacks until end of turn");

		int remaining = techPoints;
		while (remaining > 0 && !options.isEmpty()) {
			Object selection = JOptionPane.showInputDialog(
					this,
					"Stealth Strike TP remaining: " + remaining,
					"Stealth Strike",
					JOptionPane.PLAIN_MESSAGE,
					null,
					options.toArray(),
					options.get(0));
			if (!(selection instanceof String choice) || choice.isBlank()) {
				break;
			}
			applyStealthStrikeChoice(choice);
			options.remove(choice);
			remaining--;
		}
		character.updateAll();
	}

	private void applyStealthStrikeChoice(String choice) {
		if (choice == null || character == null || character.getCombat() == null) return;
		if ("All attacks can deal Sneak Attack damage until end of turn".equalsIgnoreCase(choice)) {
			removeCombatStatusByName(STEALTH_STRIKE_SNEAK_STATUS);
			removeCombatStatusByAttribute(STEALTH_STRIKE_SNEAK_MARKER);
			character.getCombat().addStatus(buildCombatMarkerStatus(
					STEALTH_STRIKE_SNEAK_STATUS,
					STEALTH_STRIKE_SNEAK_MARKER,
					1.0,
					"Your attacks may deal Sneak Attack damage until end of turn."));
			character.getCombat().addStatus(buildReminderStatus(
					STEALTH_STRIKE_SNEAK_STATUS,
					"Stealth Strike: All of your attacks can deal Sneak Attack damage until end of turn."));
			return;
		}
		if ("Enter stealth at end of turn".equalsIgnoreCase(choice)) {
			removeCombatStatusByName(STEALTH_STRIKE_STEALTH_STATUS);
			character.getCombat().addStatus(buildReminderStatus(
					STEALTH_STRIKE_STEALTH_STATUS,
					"Stealth Strike: Enter stealth at end of turn."));
			return;
		}
		if ("Gain flanking on all attacks until end of turn".equalsIgnoreCase(choice)) {
			removeCombatStatusByName(STEALTH_STRIKE_FLANKING_STATUS);
			character.getCombat().addStatus(buildCombatMarkerStatus(
					STEALTH_STRIKE_FLANKING_STATUS,
					"FLANKING",
					1.0,
					"Gain flanking on all attacks until end of turn."));
		}
	}

	private DataStatus buildReminderStatus(String name, String description) {
		DataStatus status = new DataStatus();
		status.setName(name);
		status.setAffinity("None");
		status.setAttribute("REMINDER");
		status.setSeverity(0.0);
		status.setDurationType("Turn");
		status.setDuration(1);
		status.setDescription(description);
		return status;
	}

	private DataStatus buildCombatMarkerStatus(String name, String attribute, double severity, String description) {
		DataStatus status = new DataStatus();
		status.setName(name);
		status.setAffinity("None");
		status.setAttribute(attribute);
		status.setSeverity(severity);
		status.setDurationType("Turn");
		status.setDuration(1);
		status.setDescription(description);
		return status;
	}

	private void removeCombatStatusByName(String name) {
		if (name == null || name.isBlank() || character == null || character.getCombat() == null) return;
		character.getCombat().removeStatus(name);
	}

	private void removeCombatStatusByAttribute(String attribute) {
		if (attribute == null || attribute.isBlank() || character == null || character.getCombat() == null) return;
		character.getCombat().getCombatStatus().removeIf(status ->
				status != null
				&& status.getAttribute() != null
				&& attribute.equalsIgnoreCase(status.getAttribute().trim()));
	}

	private boolean handleAngelReplicationChoiceAfterResolution() {
		if (angelReplicationPromptHandled || combatFrame == null || action == null) {
			return false;
		}
		if (!isAuraTechniqueAction() || !combatFrame.shouldOfferAngelPointSpend(action)) {
			return false;
		}
		int choice = JOptionPane.showConfirmDialog(
				this,
				ANGEL_REPLICATION_PROMPT,
				"Angel Points",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			closeFrame();
			return true;
		}
		prepareReplicatedAttack();
		return true;
	}

	private void promptFollowUpDamageDealtIfNeeded() {
		if (!shouldTrackFollowUpDamage()) {
			return;
		}
		Double damageDealt = promptDamageDealtValue();
		if (damageDealt == null || damageDealt <= 0.0) {
			return;
		}
		character.getCombat().addDamageDealtThisTurn(damageDealt);
	}

	private boolean shouldTrackFollowUpDamage() {
		return character != null
				&& character.getCombat() != null
				&& combatFrame != null
				&& combatFrame.isMyTurnActive()
				&& action != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(FOLLOW_UP_SPECIALTY)
				&& isAttackAction();
	}

	private Double promptDamageDealtValue() {
		while (true) {
			String response = JOptionPane.showInputDialog(
					this,
					"How much damage was dealt?",
					FOLLOW_UP_SPECIALTY,
					JOptionPane.QUESTION_MESSAGE);
			if (response == null) {
				return null;
			}
			String trimmed = response.trim();
			if (trimmed.isBlank()) {
				return 0.0;
			}
			try {
				return Math.max(0.0, Double.parseDouble(trimmed));
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(
						this,
						"Please enter a valid number.",
						FOLLOW_UP_SPECIALTY,
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void prepareReplicatedAttack() {
		String selectedDamageType = getSelectedDamageType();
		angelReplicationPromptHandled = true;
		angelReplicationSelected = true;
		actionResolved = false;
		if (action != null) {
			action.update();
		}
		showInitialStage();
		restoreDamageTypeSelection(selectedDamageType);
		if (combatFrame != null) {
			combatFrame.updateCharacter(character);
		}
		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshMainPanel();
		}
		this.setVisible(true);
		toFront();
		repaint();
	}

	private void maybeOfferClericDomainShare() {
		String damageType = resolveMatchingClericDomainDamageType();
		if (damageType == null || damageType.isBlank()) {
			return;
		}
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Did you deal " + damageType + " damage?",
				"Domain Trigger",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		showClericDomainShareDialog();
	}

	private void showClericDomainShareDialog() {
		if (character == null || !character.hasShareableDomainStatusEffects()) {
			return;
		}
		Object[] options = {"Share", "Close"};
		int choice = JOptionPane.showOptionDialog(
				this,
				"Share the current domain status effect?",
				"Domain Share",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[0]);
		if (choice != 0) {
			return;
		}
		StringSelection stringSelection = new StringSelection(character.buildDomainStatusMacro());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private String resolveMatchingClericDomainDamageType() {
		if (character == null || character.getIdentity() == null || character.getTraining() == null) {
			return null;
		}
		if (!"Cleric".equalsIgnoreCase(character.getIdentity().getCharClass())) {
			return null;
		}
		if (!character.hasShareableDomainStatusEffects()) {
			return null;
		}
		List<String> domains = character.getTraining().getDomains();
		if (domains.isEmpty()) {
			return null;
		}
		String domainName = domains.get(0);
		if (domainName == null || domainName.isBlank() || !isNamedAuraAffinity(domainName)) {
			return null;
		}
		String requiredDamageType = resolveDomainDamageType(domainName);
		String selectedDamageType = getSelectedDamageType();
		if (requiredDamageType == null || selectedDamageType == null) {
			return null;
		}
		return requiredDamageType.equalsIgnoreCase(selectedDamageType) ? requiredDamageType : null;
	}

	private void restoreDamageTypeSelection(String selectedDamageType) {
		if (selectedDamageType == null || selectedDamageType.isBlank() || damageType == null) {
			return;
		}
		damageType.setSelectedItem(selectedDamageType);
	}

	private String resolveDomainDamageType(String domainName) {
		if (domainName == null || domainName.isBlank()) {
			return null;
		}
		StoreRuleManager ruleManager = new StoreRuleManager();
		DataDomain domain = ruleManager.getDomainByName(domainName);
		if (domain == null || domain.getCondition() == null) {
			return null;
		}
		String condition = domain.getCondition().trim();
		if (!condition.regionMatches(true, 0, "Deal ", 0, 5) || !condition.toLowerCase().endsWith(" damage")) {
			return null;
		}
		String damageName = condition.substring(5, condition.length() - " Damage".length()).trim();
		return switch (damageName.toUpperCase(java.util.Locale.ROOT)) {
			case "TEMPORAL" -> "TIME";
			case "ELECTRIC" -> "ELEC";
			case "DARK" -> "DARK";
			case "PHYSICAL" -> "PHY";
			default -> damageName.toUpperCase(java.util.Locale.ROOT);
		};
	}

	private boolean isNamedAuraAffinity(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		return switch (value.trim().toUpperCase(java.util.Locale.ROOT)) {
			case "ENHANCEMENT", "BODY", "NATURE", "METAL", "EARTH", "WATER", "AIR", "FIRE",
					"ELECTRICITY", "FORCE", "SOUND", "LIGHT", "DARKNESS", "POISON",
					"PSIONIC", "ENERGY", "SPIRIT", "TIME" -> true;
			default -> false;
		};
	}
}

