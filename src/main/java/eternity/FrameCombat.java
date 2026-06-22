package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import eternity.DataItemWeapon;
import websocket.Roll20WebSocketServer;

/*
 * Combat Helper
 */
public class FrameCombat extends JFrame {
	private static final long serialVersionUID = 1;
	private static final int OPTION_HEADER_HEIGHT = 20;
	private static final int OPTION_ROW_HEIGHT = 50;
	private static final int OPTION_PANEL_MIN_HEIGHT = 130;
	private static final int OPTION_PANEL_WIDTH = 580;
	private static final int OPTION_PANEL_BOTTOM_PADDING = 10;
	private static final int FRAME_WIDTH = 850;
	private static final int FRAME_HEIGHT = 540;
	private static final int LEFT_COLUMN_X = 25;
	private static final int CENTER_COLUMN_X = 195;
	private static final int RIGHT_COLUMN_X = 365;
	private static final int MAIN_BUTTON_Y = 60;
	private static final int MAIN_BUTTON_SIZE = 100;
	private static final int SMALL_BUTTON_SIZE = 50;
	private static final int LEFT_MID_COLUMN_X = 135;
	private static final int RIGHT_MID_COLUMN_X = 305;
	private static final int RESOURCE_ROW_Y = 185;
	private static final int RESOURCE_LABEL_Y = RESOURCE_ROW_Y;
	private static final int RESOURCE_VALUE_Y = RESOURCE_ROW_Y + 18;
	private static final int RESOURCE_CELL_WIDTH = 110;
	private static final int RESOURCE_VALUE_WIDTH = 90;
	private static final int OPTION_PANE_Y = 235;
	private static final int OPTION_PANE_HEIGHT = 220;
	private static final int FOOTER_BUTTON_Y = 470;
	private static final int TARGET_PANEL_X = 535;
	private static final int TARGET_PANEL_Y = 60;
	private static final int TARGET_PANEL_WIDTH = 280;
	private static final int TARGET_PANEL_HEIGHT = 395;
	private static final String RECKONING_SPECIALTY = "Reckoning";
	private static final String RECKONING_STATUS_NAME_PREFIX = "Reckoning ";
	private static final String CRUSADER_SEAL_SPECIALTY = "Crusader Seal";
	private static final String CRUSADER_SEAL_ACTION = "Crusader Seal";
	private static final String UNLEASH_CRUSADER_SEAL_ACTION = "Unleash Crusader Seal";
	private static final String CRUSADER_SEAL_MARKER_NAME = "Crusader Seal";
	private static final String CRUSADER_SEAL_MARKER_ATTRIBUTE = "CRUSADERSEAL";
	private static final String CLERIC_DOMAIN_ROUND_STATUS_PREFIX = "Cleric Domain Round: ";
	private static final String DIVINE_DEDICATION_DAWNING = "Dawning";
	private static final String HOLY_BEACON_ACTION = "Holy Beacon";
	private static final String DUAL_WIELD_SPECIALTY = "Dual Wield";
	private static final String OFFHAND_ATTACK_ACTION = "Offhand Attack";
	private static final String FOLLOW_UP_ACTION = "Follow Up";
	private static final String SALVO_SPECIALTY = "Salvo";
	private static final String STILL_MIND_SPECIALTY = "Still Mind";
	private static final String STILL_MIND_REMINDER_STATUS = "Still Mind Active";
	private static final String FAVORED_TERRAIN_SPECIALTY = "Favored Terrain";
	private static final String FAVORED_TERRAIN_LIST = "Favored Terrain";
	private static final String FAVORED_TERRAIN_COMBAT_STATUS_PREFIX = "Favored Terrain Combat: ";
	private static final String ANGEL_POINTS_LABEL = "Angel Points";
	private static final String UNARMED_PROWESS_SPECIALTY = "Unarmed Prowess";
	private static final String STEALTH_STRIKE_SNEAK_MARKER = "STEALTHSNEAK";
	private final FrameSheet sheetFrame;
	private StoreCharData character;
	private int stdActCount, moveActCount, auraActCount;
	private boolean stdDemoCheck, moveDemoCheck;
	private boolean myTurnActive;
	private int resolvedAttackCountThisTurn;
	private boolean stillMindStartWindowOpen;
	
	private ImageIcon[] buttonPic, buttonPic2;
	private JRadioButton stdButton, moveButton, auraButton, freeButton, intButton;
	private JButton stdDemoButton, moveDemoButton;
	private JButton statusButton, endRoundButton, damageButton;
	private JButton reckoningButton;
	private JButton crusaderSealButton;
	private ButtonGroup buttonGroup;
	
	private JPanel optionPanel;
	private JScrollPane optionPane;
	private JScrollPane targetPane;
	private ArrayList<JButton> optionButtons;
	private List<DataStatus> battleStatus;
	private JComboBox<String> stdActCat;
	private JComboBox<String> stdAbilityCat;
	private JComboBox<String> moveActCat;
	private JComboBox<String> moveAbilityCat;
	private JComboBox<String> auraActCat;
	private JComboBox<String> auraAbilityCat;
	private JComboBox<String> weaponSelect;
	private ArrayList<DataItemWeapon> weaponOptions = new ArrayList<>();
	
	private ArrayList<JButton> actionButtons;
	private ArrayList<DataAction> actionList;
	private final Set<String> hiddenUntilNextTurnActionKeys;
	private boolean salvoSequenceActive;
	private ArrayList<DataItemWeapon> pendingSalvoWeapons;
	private String savedSalvoWeaponSelection;
	private FrameStatus statusFrame;
	private Roll20WebSocketServer roll20Server;
	private final boolean playerMode;
	private boolean roll20ConnectedPopupShown;
	private Roll20WebSocketServer.ConnectionListener roll20ConnectionListener;
	
	private final String[] ICONS = {"stdAct", "moveAct", "auraAct", "damage", "round", "freeAct", "intAct", "stdDemo", "moveDemo"};
	private final String[] STDACTCAT = {"All", "Standard", "Combat Maneuver", "Class", "Aura", "Specialty", "Item"};
	private final String[] MOVEACTCAT = {"All", "Move"};
	private final String[] STDABILITYCAT = {"All", "Attack", "Heal", "Next Attack", "Mitigation", "Remedy", "Other"};
	private final String[] AURAACTCAT = {"All", "Aura"};
	private final String[] DMGTYPE = {"BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME"};
	private static final String[][] AURA_DMG_PAIR = { {"Enhancement", "PHY"},{"Body", "PHY"},{"Nature", "PIERCE"},{"Metal", "PHY"},{"Earth", "PHY"},{"Water", "FROST"},{"Air", "SLASH"},{"Fire", "FIRE"},{"Electricity", "ELEC"},{"Force", "BLUNT"},{"Sound", "SONIC"},{"Light", "LIGHT"},{"Darkness", "DARK"},{"Poison", "TOXIC"},{"Psionic", "PSI"},{"Energy", "ENERGY"},{"Spirit", "SPIRIT"},{"Time", "TIME"},{"Deviant", "TRUE"} };
	private static final String[][] AURA_SAVE_PAIR = { {"PIERCE", "REF"},{"FROST", "FORT"},{"SLASH", "REF"},{"FIRE", "REF"},{"ELEC", "FORT"},{"BLUNT", "FORT"},{"SONIC", "WILL"},{"LIGHT", "WILL"},{"DARK", "WILL"},{"TOXIC", "FORT"},{"PSI", "WILL"},{"ENERGY", "REF"},{"SPIRIT", "WILL"},{"TIME", "FORT"} };

	private final JLabel noCombatImage;

	private final JLabel headerL;
	private final JLabel roundHeaderL;
	private final JLabel targetHeaderL;
	private final JLabel[] labels;
	private final JLabel[] resourceLabels;
	private final JLabel[] resourceValues;
	private final JButton[] buttons;
	private final JCheckBox popupCheckBox;

	/*
	 * DEFAULT CONSTRUCTOR
	 */
	FrameCombat (FrameSheet sheetFrame, StoreCharData character2) {
		this(sheetFrame, character2, false);
	}

	FrameCombat (FrameSheet sheetFrame, StoreCharData character2, boolean playerMode) {
		super("Combat Helper");
		this.sheetFrame = sheetFrame;
		this.character = character2;
		this.playerMode = playerMode;
		setLayout(null); // retains existing absolute positioning logic
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		positionRelativeToSheet();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // keep instance alive; just hide when closed
		setLayout(null); // preserve existing absolute positioning

		headerL = new JLabel();
		headerL.setVisible(false);
		headerL.setBounds(15, 10, 320, 25);
		headerL.setFont(headerL.getFont().deriveFont(java.awt.Font.BOLD, 14f));
		add(headerL);

		roundHeaderL = new JLabel();
		roundHeaderL.setVisible(false);
		roundHeaderL.setHorizontalAlignment(JLabel.RIGHT);
		roundHeaderL.setBounds(360, 10, 165, 25);
		roundHeaderL.setFont(roundHeaderL.getFont().deriveFont(java.awt.Font.BOLD, 14f));
		add(roundHeaderL);

		targetHeaderL = new JLabel("Roll20 Targets");
		targetHeaderL.setVisible(true);
		targetHeaderL.setBounds(TARGET_PANEL_X, 20, TARGET_PANEL_WIDTH, 25);
		targetHeaderL.setFont(targetHeaderL.getFont().deriveFont(java.awt.Font.BOLD, 14f));
		add(targetHeaderL);

		noCombatImage = new JLabel();
		noCombatImage.setVisible(false);
		add(noCombatImage);

		labels = new JLabel[3];
		for (int j = 0; j < labels.length; j++) {
			labels[j] = new JLabel();
			labels[j].setHorizontalAlignment(JLabel.CENTER);
			labels[j].setVisible(false);
			add(labels[j]);
		}

		buttons = new JButton[3];
		for (int j = 0; j < buttons.length; j++) {
			buttons[j] = new JButton();
			buttons[j].setVisible(false);
			add(buttons[j]);
		}

		resourceLabels = new JLabel[4];
		resourceValues = new JLabel[4];
		for (int j = 0; j < resourceLabels.length; j++) {
			resourceLabels[j] = new JLabel("", JLabel.CENTER);
			resourceLabels[j].setVisible(false);
			add(resourceLabels[j]);
			resourceValues[j] = new JLabel("", JLabel.CENTER);
			resourceValues[j].setVisible(false);
			add(resourceValues[j]);
		}
		
		popupCheckBox = new JCheckBox("Popup");
		popupCheckBox.setSelected(resolvePopupDefault()); // default pulled from combat if available
		popupCheckBox.setBounds(LEFT_COLUMN_X, FOOTER_BUTTON_Y, 100, 20); // align with bottom buttons
		popupCheckBox.setVisible(false);
		popupCheckBox.addActionListener(e -> syncPopupStateToCombat());
		add(popupCheckBox);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
		stdActCount = 1;
		moveActCount = 1;
		auraActCount = 1;
		stdDemoCheck = false;
		moveDemoCheck = false;
		myTurnActive = false;
		resolvedAttackCountThisTurn = 0;
		stillMindStartWindowOpen = false;
		
		/*
		 * Set Headers
		*/
		headerL.setText("Combat Helper:");
		headerL.setVisible(true);
		
		/*
		 * Set Main
		 */
		buttonPic = new ImageIcon[9];
		buttonPic2 = new ImageIcon[9];
		
		for (int i = 0; i < ICONS.length; i++) {
			buildIcons(buttonPic, ICONS[i], i);
			buildIcons(buttonPic2, ICONS[i] + "2", i);
		}
		
		buttonGroup = new ButtonGroup();
		int i = 0;
		
		stdButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		stdButton.setBounds(LEFT_COLUMN_X, MAIN_BUTTON_Y, MAIN_BUTTON_SIZE, MAIN_BUTTON_SIZE);
		stdButton.addActionListener(e -> stdOptions());
		stdButton.setToolTipText("Standard Action");
		i++;
		
		moveButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		moveButton.setBounds(CENTER_COLUMN_X, MAIN_BUTTON_Y, MAIN_BUTTON_SIZE, MAIN_BUTTON_SIZE);
		moveButton.addActionListener(e -> moveOptions());
		moveButton.setToolTipText("Move Action");
		i++;
		
		auraButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		auraButton.setBounds(RIGHT_COLUMN_X, MAIN_BUTTON_Y, MAIN_BUTTON_SIZE, MAIN_BUTTON_SIZE);
		auraButton.addActionListener(e -> auraOptions());
		auraButton.setToolTipText("Aura Action");
		i++;
		
		damageButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		damageButton.setBounds(LEFT_COLUMN_X, MAIN_BUTTON_Y, MAIN_BUTTON_SIZE, MAIN_BUTTON_SIZE);
		damageButton.addActionListener(e -> underAttack());
		damageButton.setToolTipText("Incoming Attack");
		i++;
		
		endRoundButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		endRoundButton.setBounds(RIGHT_COLUMN_X, MAIN_BUTTON_Y, MAIN_BUTTON_SIZE, MAIN_BUTTON_SIZE);
		endRoundButton.addActionListener(e -> endRound());
		endRoundButton.setToolTipText("End of Round");
		i++;
		
		freeButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		freeButton.setBounds(LEFT_MID_COLUMN_X, 120, SMALL_BUTTON_SIZE, SMALL_BUTTON_SIZE);
		freeButton.addActionListener(e -> freeOptions());
		freeButton.setToolTipText("Free Action");
		i++;
		
		intButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		intButton.setBounds(RIGHT_MID_COLUMN_X, 120, SMALL_BUTTON_SIZE, SMALL_BUTTON_SIZE);
		intButton.addActionListener(e -> intOptions());
		intButton.setToolTipText("Interrupt Action");
		i++;		
		
		stdDemoButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		stdDemoButton.setBounds(LEFT_MID_COLUMN_X, 50, SMALL_BUTTON_SIZE, SMALL_BUTTON_SIZE);
		stdDemoButton.addActionListener(e -> stdDemoClick());
		i++;	
		
		moveDemoButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		moveDemoButton.setBounds(RIGHT_MID_COLUMN_X, 50, SMALL_BUTTON_SIZE, SMALL_BUTTON_SIZE);
		moveDemoButton.addActionListener(e -> moveDemoClick());		

		reckoningButton = new JButton("Reckoning");
		reckoningButton.setBounds(LEFT_COLUMN_X, 36, 120, 24);
		reckoningButton.setVisible(false);
		reckoningButton.setToolTipText("Gain +1 total damage until end of combat.");
		reckoningButton.addActionListener(e -> applyReckoningStack());
		add(reckoningButton);

		crusaderSealButton = new JButton();
		crusaderSealButton.setBounds(RIGHT_COLUMN_X - 10, 36, 160, 24);
		crusaderSealButton.setVisible(false);
		crusaderSealButton.setToolTipText("Add 1 Crusader Seal charge when an ally in your emanation takes damage.");
		crusaderSealButton.addActionListener(e -> incrementCrusaderSealCharge());
		add(crusaderSealButton);

		labels[0].setBounds(LEFT_COLUMN_X, 160, MAIN_BUTTON_SIZE, 20);
		labels[1].setBounds(CENTER_COLUMN_X, 160, MAIN_BUTTON_SIZE, 20);
		labels[2].setBounds(RIGHT_COLUMN_X, 160, MAIN_BUTTON_SIZE, 20);
		layoutResourceRow();
		
		optionPanel = new JPanel();
		optionPanel.setVisible(true);
		optionPane = buildScrollPane(optionPanel);
		add(optionPane);
		optionButtons = new ArrayList<JButton>();
		optionPanel.setLayout(null);
		optionPanel.setPreferredSize(new Dimension(OPTION_PANEL_WIDTH, OPTION_PANEL_MIN_HEIGHT));
		optionPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		optionPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		optionPane.setBounds(LEFT_COLUMN_X, OPTION_PANE_Y, 500, OPTION_PANE_HEIGHT);
		optionPane.getVerticalScrollBar().setUnitIncrement(15);	//set mouse wheel scroll amount

		initializeRoll20Targets();
		
		buttons[0].setBounds(LEFT_COLUMN_X, FOOTER_BUTTON_Y, 145, 20);
		buttons[0].setText("Status");
		buttons[0].addActionListener(e -> statusChange());
		buttons[1].setBounds(CENTER_COLUMN_X, FOOTER_BUTTON_Y, 145, 20);
		buttons[2].setBounds(RIGHT_COLUMN_X, FOOTER_BUTTON_Y, 145, 20);
		
		battleStatus = character != null && character.getCombat() != null
				? character.getCombat().getCombatStatus()
				: new ArrayList<DataStatus>();
		stdActCat = new JComboBox<String>(STDACTCAT);
		stdActCat.addActionListener(e -> refreshSelectedActionView()); // keep active list in sync with category selection
		stdActCat.setBounds(0, 0, 160, 20);
		optionPanel.add(stdActCat);
		stdAbilityCat = new JComboBox<String>(STDABILITYCAT);
		stdAbilityCat.addActionListener(e -> refreshSelectedActionView());
		stdAbilityCat.setBounds(170, 0, 160, 20);
		stdAbilityCat.setVisible(false);
		optionPanel.add(stdAbilityCat);
		moveActCat = new JComboBox<String>(MOVEACTCAT);
		moveActCat.addActionListener(e -> updateMoveAct());
		moveActCat.setBounds(0, 0, 160, 20);
		moveActCat.setVisible(false);
		optionPanel.add(moveActCat);
		moveAbilityCat = new JComboBox<String>(STDABILITYCAT);
		moveAbilityCat.addActionListener(e -> updateMoveAct());
		moveAbilityCat.setBounds(170, 0, 160, 20);
		moveAbilityCat.setVisible(false);
		optionPanel.add(moveAbilityCat);
		auraActCat = new JComboBox<String>(AURAACTCAT);
		auraActCat.addActionListener(e -> updateAuraAct());
		auraActCat.setBounds(0, 0, 160, 20);
		auraActCat.setVisible(false);
		optionPanel.add(auraActCat);
		auraAbilityCat = new JComboBox<String>(STDABILITYCAT);
		auraAbilityCat.addActionListener(e -> updateAuraAct());
		auraAbilityCat.setBounds(170, 0, 160, 20);
		auraAbilityCat.setVisible(false);
		optionPanel.add(auraAbilityCat);
		weaponSelect = new JComboBox<String>();
		weaponSelect.setBounds(340, 0, 160, 20);
		weaponSelect.addActionListener(e -> onWeaponSelectionChanged());
		weaponSelect.setVisible(false); // only shown when Standard actions are active
		optionPanel.add(weaponSelect);
		
		actionButtons = new ArrayList<JButton>();
		actionList = new ArrayList<DataAction>();
		hiddenUntilNextTurnActionKeys = new LinkedHashSet<>();
		salvoSequenceActive = false;
		pendingSalvoWeapons = new ArrayList<>();
		savedSalvoWeaponSelection = null;
		
		loadNoCombatImage();
		if (character != null && character.getCombat() != null) {
			character.getCombat().rebuildActions(character);
		}
		populateWeaponSelect();
		updateStandardAttackRange();
		refreshResourceRow();

		// Open in the correct state based on current combat flag
		if (character != null && character.getCombat() != null && character.getCombat().isInCombat()) {
			notMyTurn();
		} else {
			nonCombat();
		}
	}

	private void initializeRoll20Targets() {
		try {
			roll20Server = Roll20WebSocketServer.getSharedServer();
			registerRoll20ConnectionListener();
			targetPane = new JScrollPane(roll20Server.getTargetList());
			targetPane.setBounds(TARGET_PANEL_X, TARGET_PANEL_Y, TARGET_PANEL_WIDTH, TARGET_PANEL_HEIGHT);
			targetPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			targetPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			add(targetPane);
			if (playerMode && roll20Server.isServiceConnected()) {
				showRoll20ConnectedPopup();
			}
		} catch (Exception ex) {
			targetHeaderL.setText("Roll20 Targets Unavailable");
			targetHeaderL.setToolTipText(ex.getMessage());
		}
	}
	
	public void clearCombat() {
		clearHelper();
		
		stdButton.setVisible(false);
		moveButton.setVisible(false);
		auraButton.setVisible(false);
		freeButton.setVisible(false);
		intButton.setVisible(false);
		stdDemoButton.setVisible(false);
		moveDemoButton.setVisible(false);
		endRoundButton.setVisible(false);
		damageButton.setVisible(false);
		if (reckoningButton != null) {
			reckoningButton.setVisible(false);
		}
		if (crusaderSealButton != null) {
			crusaderSealButton.setVisible(false);
		}
		
		optionPane.setVisible(false);
		
		for (int i = buttons[2].getActionListeners().length - 1; i >= 0; i--) {
			buttons[2].removeActionListener(buttons[2].getActionListeners()[i]);
		}
		
		stdActCat.setVisible(false);
		stdActCat.setEnabled(true);
		stdAbilityCat.setVisible(false);
		stdAbilityCat.setEnabled(true);
		moveActCat.setVisible(false);
		moveActCat.setEnabled(true);
		moveAbilityCat.setVisible(false);
		moveAbilityCat.setEnabled(true);
		auraActCat.setVisible(false);
		auraActCat.setEnabled(true);
		auraAbilityCat.setVisible(false);
		auraAbilityCat.setEnabled(true);
		weaponSelect.setVisible(false);
		setResourceRowVisible(false);
	}
	
	public void updateCharacter(StoreCharData character) {
		this.character = character;
		battleStatus = this.character != null && this.character.getCombat() != null
				? this.character.getCombat().getCombatStatus()
				: new ArrayList<DataStatus>();
		if (statusFrame != null) {
			statusFrame.updateCharacter(character);
		}
		if (this.character != null && this.character.getCombat() != null) {
			this.character.getCombat().rebuildActions(this.character);
		}
		populateWeaponSelect();
		updateStandardAttackRange();
		refreshPopupCheckboxFromCombat();
		refreshResourceRow();
		refreshReckoningButtonVisibility();
		refreshCrusaderSealButtonVisibility();
	}
	
	public void notMyTurn() {
		clearCombat();
		showNoCombatImage(false);
		myTurnActive = false;
		
		advanceTrackedStatuses("Turn");
		
		/*
		 * Set Headers
		*/
		updateTurnHeader(false);
		updateRoundHeader();
		
		freeButton.setVisible(false);
		intButton.setVisible(true);
		refreshReckoningButtonVisibility();
		refreshCrusaderSealButtonVisibility();
		refreshResourceRow();
		optionPane.setVisible(true);
		endRoundButton.setVisible(true);
		damageButton.setVisible(true);
		buttonGroup.clearSelection();
		intButton.setSelected(true);
		intOptions();
		
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> statusChange());
		
		buttons[1].setText("My Turn");
		buttons[1].setVisible(true);
		buttons[1].addActionListener(e -> startMyTurn());
		
		buttons[2].setText("End Combat");
		buttons[2].setBackground(new java.awt.Color(0, 180, 0)); // green
		buttons[2].setForeground(java.awt.Color.BLACK);
		buttons[2].setVisible(true);
		buttons[2].addActionListener(e -> exitCombat());
	}
	
	public void myTurn() {
		clearCombat();
		showNoCombatImage(false);
		myTurnActive = true;
		
		/*
		 * Set Headers
		*/
		updateTurnHeader(true);
		updateRoundHeader();
		
		labels[0].setVisible(true);
		labels[0].setText("" + stdActCount);
		labels[1].setVisible(true);
		labels[1].setText("" + moveActCount);
		labels[2].setVisible(true);
		labels[2].setText("" + auraActCount);
		refreshResourceRow();
		
		optionPane.setVisible(true);
		stdButton.setVisible(true);
		moveButton.setVisible(true);
		auraButton.setVisible(true);
		freeButton.setVisible(true);
		intButton.setVisible(true);
		stdDemoButton.setVisible(true);
		moveDemoButton.setVisible(true);
		refreshReckoningButtonVisibility();
		refreshCrusaderSealButtonVisibility();
		buttonGroup.clearSelection();
		stdButton.setSelected(true);
		stdOptions();
		
		buttons[0].setVisible(true);
		buttons[0].addActionListener(e -> statusChange());
		
		buttons[1].setText("End My Turn");
		buttons[1].setVisible(true);
		buttons[1].addActionListener(e -> notMyTurn());
		
		buttons[2].setText("End Combat");
		buttons[2].setVisible(true);
		buttons[2].addActionListener(e -> exitCombat());
	}
	
	public void startMyTurn() {
		applyTurnStartCombatMarkers();
		applyShuffleTurnStartDamageIfNeeded();
		if (character != null
				&& character.getCombat() != null
				&& character.getCombat().getHeavyTokens() > 0) {
			character.getCombat().consumeHeavyToken();
			JOptionPane.showMessageDialog(this, "Heavy Token: Your initiative cost is 11 instead of 10. Heavy token removed.");
		}
		if (character != null
				&& character.getCombat() != null
				&& character.getCombat().getRootTokens() > 0) {
			JOptionPane.showMessageDialog(this, "You are rooted and cannot move this turn.");
		}
		if (character != null
				&& character.getCombat() != null
				&& character.getCombat().getIncapacitateTokens() > 0) {
			character.getCombat().consumeIncapacitateToken();
			JOptionPane.showMessageDialog(this, "You are incapacitated. Turn skipped and incapacitate token removed.");
			advanceTrackedStatuses("Cycle");
			hiddenUntilNextTurnActionKeys.clear();
			resetActionCountsToMaximum();
			notMyTurn();
			return;
		}
		if (character != null
				&& character.getCombat() != null
				&& character.getCombat().getStunTokens() > 0) {
			character.getCombat().consumeStunToken();
			JOptionPane.showMessageDialog(this, "You are stunned. Turn skipped and stun token removed.");
			advanceTrackedStatuses("Cycle");
			hiddenUntilNextTurnActionKeys.clear();
			resetActionCountsToMaximum();
			notMyTurn();
			return;
		}

		clearCombat();
		
		advanceTrackedStatuses("Cycle");
		hiddenUntilNextTurnActionKeys.clear();
		resolvedAttackCountThisTurn = 0;
		stillMindStartWindowOpen = true;
		if (character != null && character.getCombat() != null) {
			character.getCombat().resetDamageDealtThisTurn();
		}

		resetActionCountsToMaximum();
		
		myTurn();
		showStillMindStartTurnPopupIfNeeded();
		showDomainEmanationStartTurnPopupIfNeeded();
	}

	boolean isMyTurnActive() {
		return myTurnActive;
	}

	private void applyTurnStartCombatMarkers() {
		if (character == null || character.getCombat() == null || character.getCombat().getCombatStatus() == null) return;
		for (DataStatus status : character.getCombat().getCombatStatus()) {
			if (status == null || status.getAttribute() == null) continue;
			if ("HOTHP".equalsIgnoreCase(status.getAttribute().trim())) {
				applyTurnStartHealing(status);
			} else if ("HOTSHIELD".equalsIgnoreCase(status.getAttribute().trim())) {
				applyTurnStartShield(status);
			}
		}
	}

	private void applyTurnStartHealing(DataStatus status) {
		if (status == null || character == null || character.getResources() == null) return;
		double severity = status.getSeverity();
		if (Math.abs(severity) <= 0.0001) return;
		DataStatus instantHeal = new DataStatus(status);
		instantHeal.setAttribute("HP");
		instantHeal.setDurationType("Instant");
		instantHeal.setDuration(0);
		applyInstantResourceDelta(instantHeal);
	}

	private void applyTurnStartShield(DataStatus status) {
		if (status == null || character == null || character.getResources() == null) return;
		double severity = status.getSeverity();
		if (Math.abs(severity) <= 0.0001) return;
		character.getResources().setShield(character.getResources().getShield() + severity);
	}
	
	public void nonCombat() {
		clearCombat();
		showNoCombatImage(true);
		myTurnActive = false;
		
		/*
		 * Set Headers
		*/
		headerL.setText("Combat Helper: You are not in combat.");
		headerL.setVisible(false); // hide header when out of combat
		roundHeaderL.setVisible(false);
		
		buttons[2].setText("Enter Combat");
		buttons[2].setBackground(new java.awt.Color(139, 0, 0)); // dark red
		buttons[2].setForeground(java.awt.Color.WHITE);
		buttons[2].setVisible(true);
		buttons[2].addActionListener(e -> enterCombat());

		popupCheckBox.setVisible(true);
		refreshReckoningButtonVisibility();
		refreshCrusaderSealButtonVisibility();
	}
	
	public void stdOptions() {
		stdActCat.setVisible(true);
		stdActCat.setEnabled(true);
		stdAbilityCat.setVisible(true);
		stdAbilityCat.setEnabled(true);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		populateWeaponSelect();
		updateStandardAttackRange();
		weaponSelect.setVisible(true);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateStdAct();
	}
	
	public void moveOptions() {
		stdActCat.setVisible(false);
		stdAbilityCat.setVisible(false);
		moveActCat.setVisible(true);
		moveActCat.setEnabled(true);
		moveAbilityCat.setVisible(true);
		moveAbilityCat.setEnabled(true);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		weaponSelect.setVisible(true);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateMoveAct();
	}
	
	public void auraOptions() {
		stdActCat.setVisible(false);
		stdAbilityCat.setVisible(false);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(true);
		auraActCat.setEnabled(true);
		auraAbilityCat.setVisible(true);
		auraAbilityCat.setEnabled(true);
		weaponSelect.setVisible(true);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateAuraAct();
	}
	
	public void freeOptions() {
		stdActCat.setVisible(true);
		stdActCat.setEnabled(true);
		stdAbilityCat.setVisible(true);
		stdAbilityCat.setEnabled(true);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		weaponSelect.setVisible(true);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateFreeAct();
	}
	
	public void intOptions() {
		stdActCat.setVisible(true);
		stdActCat.setEnabled(true);
		stdAbilityCat.setVisible(true);
		stdAbilityCat.setEnabled(true);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		weaponSelect.setVisible(true);
		stdDemoCheck = false;
		moveDemoCheck = false;

		updateIntAct();
	}
	
	public void stdDemoClick() {
		if (stdActCount >= 1) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"<html>You are about to lose one standard action to gain one move action.<br>Do you want to continue?</html>",
					"Demote Standard Action",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION) {
				stdActCount--;
				moveActCount++;
				JOptionPane.showMessageDialog(this, "<html>You have demoted a standard action to a move action.");
			}
		}
		else {
			JOptionPane.showMessageDialog(this, "You do not have any standard actions to demote.");			
		}
		stdDemoCheck = false;
		moveDemoCheck = false;
		myTurn();
	}
	
	public void moveDemoClick() {
		if (moveActCount >= 1) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"<html>You are about to lose one move action to gain one aura action.<br>Do you want to continue?</html>",
					"Demote Move Action",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION) {
				moveActCount--;
				auraActCount++;
				JOptionPane.showMessageDialog(this, "<html>You have demoted a move action to an aura action.");
			}
		}
		else {
			JOptionPane.showMessageDialog(this, "You do not have any move actions to demote.");			
		}
		moveDemoCheck = false;
		stdDemoCheck = false;
		myTurn();
	}
	
	public void endRound() {
		advanceTrackedStatuses("Round");
		if (character != null && character.getCombat() != null) {
			character.getCombat().nextRound();
			updateRoundHeader();
		}
		applyClericDomainRoundEffectsIfNeeded();

		copyInitiativeToClipboard();
		
		notMyTurn();
	}

	public void underAttack() {
		FrameCombatDamage dmgFrame = new FrameCombatDamage(sheetFrame, character);
		dmgFrame.setVisible(true);
	}	
	
	public void statusChange() {
		if (statusFrame == null) {
			statusFrame = new FrameStatus(this, character);
		}
		statusFrame.updateCharacter(character);
		statusFrame.setVisible(true);
	}	
	
	public void enterCombat() {
		if (character != null && character.getCombat() != null) {
			character.getCombat().startCombat();
			applyClericDomainRoundEffectsIfNeeded();
			applyDawningHolyBeaconIfNeeded();
			offerFavoredTerrainCombatBonusIfNeeded();
			if (sheetFrame != null) {
				sheetFrame.refreshImagePanel();
			}
			boolean showPopup = popupCheckBox != null ? popupCheckBox.isSelected() : resolvePopupDefault();
			character.getCombat().setShowInitiativePopup(showPopup);
			copyInitiativeToClipboard(showPopup);
		}
		notMyTurn();
	}	
	
	public void exitCombat() {
		for (int i = battleStatus.size()-1; i >= 0; i--) {
			DataStatus trackedStatus = battleStatus.get(i);
			battleStatus.remove(i);
			removeTimedStatus(trackedStatus);
		}
		applyRemainingShuffleDamageIfNeeded();
		if (character != null && character.getCombat() != null) {
			character.getCombat().endCombat();
			if (sheetFrame != null) {
				sheetFrame.refreshImagePanel();
				sheetFrame.refreshMainPanel();
			}
		}
		nonCombat();
	}

	private void applyShuffleTurnStartDamageIfNeeded() {
		if (character == null || character.getCombat() == null) return;
		double damage = character.applyShuffleTurnStartDamage();
		if (damage <= 0.0) return;

		JOptionPane.showMessageDialog(
				this,
				"Shuffle: You take " + trimNumber(damage) + " unresisted damage. "
						+ trimNumber(character.getCombat().getShufflePool()) + " damage remains in the shuffle pool.");
		if (character.getCombat().getIncapacitateTokens() > 0) {
			character.getCombat().consumeIncapacitateToken();
			JOptionPane.showMessageDialog(this, "Incapacitate token removed due to taking damage.");
		}
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
		}
	}

	private void applyRemainingShuffleDamageIfNeeded() {
		if (character == null || character.getCombat() == null) return;
		double remainingDamage = character.flushShufflePoolDamage();
		if (remainingDamage <= 0.0) return;

		JOptionPane.showMessageDialog(
				this,
				"Shuffle: Combat ended. You take " + trimNumber(remainingDamage)
						+ " unresisted damage from the remaining shuffle pool.");
	}

	private void applyClericDomainRoundEffectsIfNeeded() {
		if (!shouldApplyClericDomainRoundEffects()) return;
		List<DataStatus> domainStatuses = character.getTraining().getDomainStatusEffects();
		if (domainStatuses == null || domainStatuses.isEmpty()) return;
		for (DataStatus status : domainStatuses) {
			DataStatus roundStatus = buildClericDomainRoundStatus(status);
			if (roundStatus != null) {
				applyBuiltStatus(roundStatus);
			}
		}
	}

	private void applyDawningHolyBeaconIfNeeded() {
		if (!shouldApplyDawningHolyBeacon()) return;

		double healingAmount = calculateHolyBeaconHealingAmount();
		if (healingAmount > 0.0 && character.getResources() != null) {
			double maxHp = Math.max(0.0, character.getResources().calcMaxHP());
			double newLostHp = clamp(character.getResources().getLostHP() - healingAmount, 0.0, maxHp);
			character.getResources().setLostHP(newLostHp);
		}

		if (sheetFrame != null) {
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshMainPanel();
		}

		showDawningHolyBeaconPopup();
	}

	private boolean shouldApplyDawningHolyBeacon() {
		return character != null
				&& character.getCombat() != null
				&& character.getCombat().isInCombat()
				&& character.getIdentity() != null
				&& "Cleric".equalsIgnoreCase(character.getIdentity().getCharClass())
				&& character.hasDivineDedicationChoice(DIVINE_DEDICATION_DAWNING);
	}

	private void showDawningHolyBeaconPopup() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		JLabel message = new JLabel(buildDawningHolyBeaconMessage(), JLabel.CENTER);
		panel.add(message, BorderLayout.CENTER);

		JOptionPane optionPane = new JOptionPane(
				panel,
				JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,
				new Object[] {},
				null);

		JDialog dialog = optionPane.createDialog(this, "Dawning Beacon");
		JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		JButton copyButton = new JButton("Copy Macro");
		copyButton.addActionListener(e -> copyDawningHolyBeaconMacroToClipboard());
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(e -> dialog.dispose());
		buttonPanel.add(copyButton);
		buttonPanel.add(confirmButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.setModal(true);
		dialog.setVisible(true);
	}

	private String buildDawningHolyBeaconMessage() {
		double healingAmount = calculateHolyBeaconHealingAmount();
		double attackPenalty = calculateHolyBeaconAttackPenalty();
		String rangeText = calculateHolyBeaconRange() <= 0.0 ? "Melee" : trimNumber(calculateHolyBeaconRange()) + " ft";
		return "<html>Dawning Beacon activates automatically.<br>"
				+ "All allies within " + rangeText + " heal " + trimNumber(healingAmount) + ".<br>"
				+ "All enemies within " + rangeText + " suffer -" + trimNumber(attackPenalty)
				+ " ATK until end of round.</html>";
	}

	private double calculateHolyBeaconHealingAmount() {
		if (character == null || character.getAttributes() == null) return 0.0;
		double control = Math.max(0.0, character.getAttributes().calcStatusValue("CTL"));
		double classLevel = Math.max(0, character.getLevel());
		return (0.5 * control) + classLevel;
	}

	private double calculateHolyBeaconAttackPenalty() {
		if (character == null || character.getAttributes() == null) return 0.0;
		return Math.max(0.0, character.getAttributes().calcStatusValue("CTL"));
	}

	private double calculateHolyBeaconRange() {
		if (character == null || character.getAttributes() == null) return 0.0;
		return Math.max(0.0, character.getAttributes().calcStatusValue("RANGE"));
	}

	private void copyDawningHolyBeaconMacroToClipboard() {
		StringSelection stringSelection = new StringSelection(buildDawningHolyBeaconMacro());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private String buildDawningHolyBeaconMacro() {
		DataColor raceColor = null;
		if (character != null && character.getIdentity() != null && character.getIdentity().getRace() != null) {
			StoreRuleManager ruleManager = new StoreRuleManager();
			raceColor = ruleManager.getColorByTitle(character.getIdentity().getRace());
		}
		if (raceColor == null) {
			raceColor = new DataColor("Default", 0, 0, 0, 255, 255, 255);
		}

		String colorString1 = String.format("#%02x%02x%02x", raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x", raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String charName = character != null && character.getIdentity() != null && character.getIdentity().getName() != null
				? character.getIdentity().getName()
				: "Character";

		StringBuilder macro = new StringBuilder();
		macro.append("!scriptcard {{ --#titleCardBackground|").append(colorString1)
				.append(" --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|").append(colorString1)
				.append(" --#titleCardBottomBorder|4px solid #000000; --#title|").append(charName)
				.append(" --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|").append(colorString2)
				.append(" --#leftSub|").append(HOLY_BEACON_ACTION)
				.append(" --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|").append(colorString1)
				.append(" --#evenRowFontColor|").append(colorString2)
				.append(" --#oddRowBackground|").append(colorString2)
				.append(" --#oddRowFontColor|").append(colorString1)
				.append(" --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;");

		String rangeText = calculateHolyBeaconRange() <= 0.0 ? "Melee" : trimNumber(calculateHolyBeaconRange()) + " ft";
		macro.append(" --+|Range: ").append(rangeText);
		macro.append(" --+|Healing: ").append(trimNumber(calculateHolyBeaconHealingAmount()));
		macro.append(" --+|Curse: -").append(trimNumber(calculateHolyBeaconAttackPenalty())).append(" ATK until end of round");
		macro.append(" --+|Status Code:[br]&nbsp;&nbsp;").append(buildDawningHolyBeaconStatusCode());
		macro.append(" }}");
		return macro.toString();
	}

	private String buildDawningHolyBeaconStatusCode() {
		double attackPenalty = calculateHolyBeaconAttackPenalty();
		if (attackPenalty <= 0.0) return "";
		return "ENEMY_AOE"
				+ "_NAME:" + HOLY_BEACON_ACTION
				+ "_DUR:Round:1"
				+ "_" + StatusCodeParser.getPreferredAttributeAlias("ATK") + "-" + trimNumber(attackPenalty)
				+ "_DESC:Reduce ATK by " + trimNumber(attackPenalty) + " until end of round.";
	}

	private boolean shouldApplyClericDomainRoundEffects() {
		return character != null
				&& character.getIdentity() != null
				&& "Cleric".equalsIgnoreCase(character.getIdentity().getCharClass())
				&& character.getTraining() != null
				&& character.getCombat() != null
				&& character.getCombat().isInCombat();
	}

	private DataStatus buildClericDomainRoundStatus(DataStatus source) {
		if (source == null || source.getAttribute() == null || source.getAttribute().isBlank()) return null;
		DataStatus roundStatus = new DataStatus(source);
		roundStatus.setName(CLERIC_DOMAIN_ROUND_STATUS_PREFIX + (source.getName() == null ? source.getAttribute() : source.getName()));
		roundStatus.setDurationType("Round");
		roundStatus.setDuration(1);
		roundStatus.setSeverity(source.getSeverity() * 0.5);
		String description = source.getDescription() == null ? "" : source.getDescription().trim();
		if (description.isBlank()) {
			description = "Cleric self-applied domain effect at 50% effectiveness until end of round.";
		} else {
			description += " Self-applied at 50% effectiveness until end of round.";
		}
		roundStatus.setDescription(description);
		return roundStatus;
	}

	private void offerFavoredTerrainCombatBonusIfNeeded() {
		if (!shouldOfferFavoredTerrainCombatBonus()) return;
		String favoredTerrain = String.join(", ", getCharacterListEntries(FAVORED_TERRAIN_LIST));
		if (favoredTerrain.isBlank()) {
			favoredTerrain = "None";
		}
		int choice = JOptionPane.showConfirmDialog(
				this,
				"<html>Is the battle taking place in favored terrain?<br>Favored Terrain: " + favoredTerrain + "</html>",
				"Favored Terrain",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		applyFavoredTerrainCombatBonus();
	}

	private boolean shouldOfferFavoredTerrainCombatBonus() {
		return character != null
				&& character.getIdentity() != null
				&& character.getIdentity().getLevel() > 0
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(FAVORED_TERRAIN_SPECIALTY)
				&& character.getAttributes() != null
				&& character.getCombat() != null;
	}

	private void applyFavoredTerrainCombatBonus() {
		if (character == null || character.getIdentity() == null || character.getAttributes() == null || character.getCombat() == null) {
			return;
		}
		double severity = Math.max(0, character.getIdentity().getLevel());
		applyFavoredTerrainCombatStatus("BAPP", severity, "Favored Terrain grants +" + trimNumber(severity) + " APPLY until end of combat.");
		applyFavoredTerrainCombatStatus("BSUP", severity, "Favored Terrain grants +" + trimNumber(severity) + " SUP until end of combat.");
		applyFavoredTerrainCombatStatus("BMAST", severity, "Favored Terrain grants +" + trimNumber(severity) + " MAST until end of combat.");
		refreshAfterStatusChange();
	}

	private void applyFavoredTerrainCombatStatus(String attribute, double severity, String description) {
		if (attribute == null || attribute.isBlank()) return;
		DataStatus applied = new DataStatus();
		applied.setName(FAVORED_TERRAIN_COMBAT_STATUS_PREFIX + attribute);
		applied.setAttribute(attribute);
		applied.setDurationType("Temporary");
		applied.setSeverity(severity);
		applied.setAffinity("None");
		applied.setDescription(description);
		character.getAttributes().addStatus(applied);

		DataStatus tracked = new DataStatus(applied);
		tracked.setDurationType("Combat");
		character.getCombat().addStatus(tracked);
	}

	private void positionRelativeToSheet() {
		setLocationRelativeTo(null);
		if (sheetFrame == null) return;

		int x = getX();
		int y = sheetFrame.getY() + sheetFrame.getHeight() - getHeight();
		setLocation(x, y);
	}

	public boolean applyBuiltStatus(DataStatus status) {
		if (character == null || status == null || status.getAttribute() == null || status.getAttribute().isBlank()) return false;

		DataStatus applied = new DataStatus(status);
		String originalDurationType = applied.getDurationType();
		boolean reminderStatus = character.isReminderStatus(applied);
		boolean combatMarkerStatus = isCombatMarkerAttribute(applied.getAttribute());
		if (isTimedDuration(originalDurationType)) {
			applied.setDurationType("Temporary");
		}

		boolean appliedOk = combatMarkerStatus || applyStatusToCharacter(applied);
		if (!appliedOk) return false;

		if (reminderStatus || combatMarkerStatus || isTimedDuration(originalDurationType)) {
			DataStatus tracked = new DataStatus(status);
			if (character.getCombat() != null) {
				character.getCombat().addStatus(tracked);
			} else {
				battleStatus.add(new DataStatus(tracked));
			}
		}

		refreshAfterStatusChange();
		return true;
	}
	
	void updateStdAct() {
		String tempString = (String)stdActCat.getSelectedItem();
		String abilityCategory = (String)stdAbilityCat.getSelectedItem();
		boolean showAllTypes = tempString != null && tempString.equalsIgnoreCase("All");
		boolean standardLayoutActive = stdActCat.isVisible();
		boolean isStandard = showAllTypes || (tempString != null && tempString.equalsIgnoreCase("Standard"));
		clearDisplayedActions();
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getStandardActions();
		ArrayList<DataAction> filtered = new ArrayList<>();
		for (DataAction action : tempActions) {
			if (showAllTypes || (action.getSource() != null && action.getSource().equalsIgnoreCase(tempString))) {
				filtered.add(action);
			}
		}
		if (isStandard && character.getAttributes() != null) {
			int maxAtk = 1;
			boolean alreadyHasFullAttack = false;
			for (DataAction action : filtered) {
				if (action != null && "Full Attack".equalsIgnoreCase(action.getName())) {
					alreadyHasFullAttack = true;
					break;
				}
			}
			if (maxAtk > 1 && !alreadyHasFullAttack) {
				DataAction fullAttack = new DataAction();
				fullAttack.setName("Full Attack");
				fullAttack.setCategory("Attack");
				fullAttack.setSource("Standard");
				fullAttack.setAffinity("None");
				fullAttack.setActionType("Standard");
				int range = isSelectedWeaponMelee() ? 0 : 1;
				fullAttack.setRanged(range);
				fullAttack.setCharacter(character);
				filtered.add(0, fullAttack);
			}
		}
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)) {
			filtered.removeIf(action -> !matchesActionCategory(action, abilityCategory));
		}
		filtered.removeIf(this::shouldHideForCurrentTurnState);
		if (standardLayoutActive && character.getCombat() != null) {
			pinSelectedStandardBaseline(filtered, tempString, abilityCategory);
		}
		
		for (int i = 0; i < filtered.size(); i++) {
			JButton tempButton = buildButton(filtered.get(i));
			tempButton.setText(filtered.get(i).getName());
			if (tempButton instanceof ActionEntryButton)
				applyActionButtonColor(tempButton, filtered.get(i));

			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickStdAction(e));
			actionList.add(filtered.get(i));
		}
		refreshOptionPanelSize();
	}	

	private void onWeaponSelectionChanged() {
		updateStandardAttackRange();
		if (weaponSelect != null && weaponSelect.isVisible()
				&& stdButton != null && stdButton.isSelected()) {
			updateStdAct();
		}
	}

	private void pinSelectedStandardBaseline(ArrayList<DataAction> filtered, String sourceFilter, String abilityCategory) {
		if (filtered == null || character == null || character.getCombat() == null) return;
		DataAction preferred = getPreferredStandardBaseline();
		if (preferred == null) return;
		if (!matchesActionSource(preferred, sourceFilter)) return;
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)
				&& !matchesActionCategory(preferred, abilityCategory)) {
			return;
		}

		filtered.removeIf(action -> isNamedAction(action, "Standard Attack")
				|| isNamedAction(action, "Standard Cast")
				|| isNamedAction(action, "Standard Spell"));
		filtered.add(0, preferred);
	}

	private DataAction getPreferredStandardBaseline() {
		if (character == null || character.getCombat() == null) return null;
		DataItemWeapon selectedWeapon = getSelectedWeapon();
		String attackString = selectedWeapon == null ? "" : selectedWeapon.getAttack();
		if ("AC".equalsIgnoreCase(attackString)) {
			return character.getCombat().getStandardAttackAction();
		}
		return character.getCombat().getStandardSpellAction();
	}

	private DataItemWeapon getSelectedWeapon() {
		int idx = weaponSelect == null ? -1 : weaponSelect.getSelectedIndex();
		if (idx < 0 || idx >= weaponOptions.size()) return null;
		return weaponOptions.get(idx);
	}

	DataItemWeapon getSelectedWeaponForAttackFrame() {
		return getSelectedWeapon();
	}

	private boolean isSalvoAction(DataAction action) {
		return action != null
				&& action.getName() != null
				&& SALVO_SPECIALTY.equalsIgnoreCase(action.getName().trim());
	}

	private void startSalvoSequence() {
		ArrayList<DataItemWeapon> equippedSalvoWeapons = getEquippedSalvoWeapons();
		if (equippedSalvoWeapons.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No equipped weapons are available for Salvo.");
			return;
		}
		salvoSequenceActive = true;
		pendingSalvoWeapons = equippedSalvoWeapons;
		Object selectedItem = weaponSelect == null ? null : weaponSelect.getSelectedItem();
		savedSalvoWeaponSelection = selectedItem == null ? null : selectedItem.toString();
		if (!openNextSalvoAttack(true)) {
			endSalvoSequence();
		}
	}

	void continueSalvoSequenceAfterResolvedAttack() {
		if (!salvoSequenceActive) return;
		SwingUtilities.invokeLater(() -> {
			if (!openNextSalvoAttack(false)) {
				endSalvoSequence();
			}
		});
	}

	void cancelSalvoSequenceIfPending() {
		if (!salvoSequenceActive) return;
		endSalvoSequence();
	}

	private boolean openNextSalvoAttack(boolean consumeActionUseOnResolve) {
		if (!salvoSequenceActive || pendingSalvoWeapons == null || pendingSalvoWeapons.isEmpty()) {
			return false;
		}
		DataItemWeapon selectedWeapon = promptForSalvoWeaponSelection();
		if (selectedWeapon == null) {
			return false;
		}
		pendingSalvoWeapons.removeIf(candidate -> isSameWeapon(candidate, selectedWeapon));
		if (!selectWeaponForAttackFrame(selectedWeapon)) {
			JOptionPane.showMessageDialog(this, "Unable to select the chosen weapon for Salvo.");
			return false;
		}
		DataAction salvoAttack = buildSalvoAttackAction();
		if (salvoAttack == null) {
			JOptionPane.showMessageDialog(this, "Unable to open a Salvo attack for the selected weapon.");
			return false;
		}
		openActionFrameWithSneakPrompt(salvoAttack, consumeActionUseOnResolve);
		return true;
	}

	private DataAction buildSalvoAttackAction() {
		DataAction preferred = getPreferredStandardBaseline();
		if (preferred == null && character != null && character.getCombat() != null) {
			preferred = character.getCombat().getStandardAttackAction();
		}
		if (preferred == null) return null;
		DataAction action = new DataAction(preferred);
		action.setCharacter(character);
		return action;
	}

	private ArrayList<DataItemWeapon> getEquippedSalvoWeapons() {
		ArrayList<DataItemWeapon> equippedWeapons = new ArrayList<>();
		if (character == null || character.getInventory() == null) return equippedWeapons;
		for (DataItemWeapon weapon : character.getInventory().getWeapons()) {
			if (weapon == null || !weapon.isEquipped()) continue;
			if (!isEligibleSalvoWeapon(weapon)) continue;
			equippedWeapons.add(weapon);
		}
		return equippedWeapons;
	}

	private boolean isEligibleSalvoWeapon(DataItemWeapon weapon) {
		if (weapon == null) return false;
		String slot = weapon.getSlot() == null ? "" : weapon.getSlot().trim().toLowerCase();
		if (slot.contains("weapon")
				|| slot.contains("hand")
				|| slot.contains("shoulder")
				|| slot.contains("arm")
				|| slot.contains("leg")
				|| slot.contains("torso")
				|| slot.contains("halo")) {
			return true;
		}
		String category = weapon.getCategory() == null ? "" : weapon.getCategory().trim().toLowerCase();
		return category.contains("weapon") || category.contains("melee") || category.contains("ranged");
	}

	private DataItemWeapon promptForSalvoWeaponSelection() {
		if (pendingSalvoWeapons == null || pendingSalvoWeapons.isEmpty()) return null;
		String[] options = new String[pendingSalvoWeapons.size() + 1];
		for (int i = 0; i < pendingSalvoWeapons.size(); i++) {
			options[i] = buildSalvoWeaponLabel(pendingSalvoWeapons.get(i));
		}
		options[options.length - 1] = "End Salvo";
		Object selection = JOptionPane.showInputDialog(
				this,
				"Select a weapon to fire as part of Salvo.",
				SALVO_SPECIALTY,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]);
		if (!(selection instanceof String chosen) || chosen.isBlank() || "End Salvo".equalsIgnoreCase(chosen)) {
			return null;
		}
		for (DataItemWeapon weapon : pendingSalvoWeapons) {
			if (chosen.equals(buildSalvoWeaponLabel(weapon))) {
				return weapon;
			}
		}
		return null;
	}

	private String buildSalvoWeaponLabel(DataItemWeapon weapon) {
		if (weapon == null) return "Weapon";
		String name = weapon.getIname() != null && !weapon.getIname().isBlank() && !"-".equals(weapon.getIname().trim())
				? weapon.getIname().trim()
				: (weapon.getDname() == null || weapon.getDname().isBlank() ? "Weapon" : weapon.getDname().trim());
		String slot = weapon.getSlot() == null || weapon.getSlot().isBlank() ? "Weapon" : weapon.getSlot().trim();
		return name + " (" + slot + ")";
	}

	private boolean selectWeaponForAttackFrame(DataItemWeapon selectedWeapon) {
		if (selectedWeapon == null || weaponSelect == null) return false;
		for (int i = 0; i < weaponOptions.size(); i++) {
			DataItemWeapon option = weaponOptions.get(i);
			if (!isSameWeapon(option, selectedWeapon)) continue;
			weaponSelect.setSelectedIndex(i);
			return true;
		}
		return false;
	}

	private boolean isSameWeapon(DataItemWeapon left, DataItemWeapon right) {
		if (left == null || right == null) return false;
		if (left.getIid() > 0 && right.getIid() > 0) {
			return left.getIid() == right.getIid();
		}
		String leftName = left.getIname() != null && !left.getIname().isBlank() ? left.getIname() : left.getDname();
		String rightName = right.getIname() != null && !right.getIname().isBlank() ? right.getIname() : right.getDname();
		String leftSlot = left.getSlot() == null ? "" : left.getSlot().trim();
		String rightSlot = right.getSlot() == null ? "" : right.getSlot().trim();
		return leftName != null
				&& rightName != null
				&& leftName.equalsIgnoreCase(rightName)
				&& leftSlot.equalsIgnoreCase(rightSlot);
	}

	private void endSalvoSequence() {
		salvoSequenceActive = false;
		if (pendingSalvoWeapons != null) {
			pendingSalvoWeapons.clear();
		}
		restoreSavedSalvoWeaponSelection();
		savedSalvoWeaponSelection = null;
	}

	private void restoreSavedSalvoWeaponSelection() {
		if (weaponSelect == null || savedSalvoWeaponSelection == null || savedSalvoWeaponSelection.isBlank()) return;
		for (int i = 0; i < weaponSelect.getItemCount(); i++) {
			String item = weaponSelect.getItemAt(i);
			if (item != null && item.equals(savedSalvoWeaponSelection)) {
				weaponSelect.setSelectedIndex(i);
				return;
			}
		}
	}

	List<String> getStandardDamageTypeOptions() {
		LinkedHashSet<String> damageTypes = new LinkedHashSet<>();
		if (character == null || character.getTraining() == null) {
			return new ArrayList<>(damageTypes);
		}

		Set<String> unlockedAffinities = new LinkedHashSet<>();
		for (DataTraining training : character.getTraining().getAllTraining()) {
			if (training == null || training.getRank() < 1) continue;
			String name = training.getName();
			String affinity = training.getAffinity();
			if (name == null || affinity == null) continue;
			if (!name.startsWith("Aura Affinity")) continue;
			unlockedAffinities.add(affinity.trim());
		}

		for (String[] pair : AURA_DMG_PAIR) {
			if (pair == null || pair.length < 2) continue;
			String auraType = pair[0];
			String damageType = pair[1];
			if (auraType == null || damageType == null) continue;
			if (!containsIgnoreCase(unlockedAffinities, auraType)) continue;
			if ("PHY".equalsIgnoreCase(damageType)) {
				damageTypes.add("BLUNT");
				damageTypes.add("PIERCE");
				damageTypes.add("SLASH");
			} else {
				damageTypes.add(damageType.toUpperCase());
			}
		}

		return new ArrayList<>(damageTypes);
	}

	String getAttackVsForDamageType(String damageType) {
		if (damageType == null) return "";
		for (String[] pair : AURA_SAVE_PAIR) {
			if (pair == null || pair.length < 2) continue;
			String pairDamageType = pair[0];
			String attackVs = pair[1];
			if (pairDamageType == null || attackVs == null) continue;
			if (damageType.equalsIgnoreCase(pairDamageType)) {
				return attackVs.toUpperCase();
			}
		}
		return "";
	}

	String getDamageTypeForAuraAffinity(String affinity) {
		if (affinity == null) return "";
		for (String[] pair : AURA_DMG_PAIR) {
			if (pair == null || pair.length < 2) continue;
			String auraAffinity = pair[0];
			String damageType = pair[1];
			if (auraAffinity == null || damageType == null) continue;
			if (affinity.equalsIgnoreCase(auraAffinity)) {
				return damageType.toUpperCase();
			}
		}
		return "";
	}

	private boolean containsIgnoreCase(Set<String> values, String target) {
		if (values == null || target == null) return false;
		for (String value : values) {
			if (value != null && value.equalsIgnoreCase(target)) {
				return true;
			}
		}
		return false;
	}

	private boolean isNamedAction(DataAction action, String name) {
		return action != null && action.getName() != null && action.getName().equalsIgnoreCase(name);
	}

	private void showStillMindStartTurnPopupIfNeeded() {
		if (!isStillMindCurrentlyAvailable()
				|| character == null
				|| character.getCombat() == null
				|| !character.getCombat().isShowStillMindPopup()) {
			return;
		}
		JCheckBox suppressBox = new JCheckBox("Do not show again");
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.add(new JLabel("Would you like to activate Still Mind?"), BorderLayout.NORTH);
		panel.add(suppressBox, BorderLayout.CENTER);
		int choice = JOptionPane.showConfirmDialog(
				this,
				panel,
				STILL_MIND_SPECIALTY,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (suppressBox.isSelected()) {
			character.getCombat().setShowStillMindPopup(false);
		}
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		DataAction action = findStillMindAction();
		if (action != null) {
			activateStillMindAction(action, true);
		}
	}

	private DataAction findStillMindAction() {
		if (character == null || character.getCombat() == null) return null;
		for (DataAction action : character.getCombat().getInterruptActions()) {
			if (isStillMindAction(action)) {
				return action;
			}
		}
		return null;
	}

	private void activateStillMindAction(DataAction action, boolean fromPopup) {
		if (!isStillMindCurrentlyAvailable()) {
			if (!fromPopup) {
				JOptionPane.showMessageDialog(
						this,
						"Still Mind can only be activated at the start of your turn.",
						STILL_MIND_SPECIALTY,
						JOptionPane.INFORMATION_MESSAGE);
			}
			return;
		}
		if (action == null || !finishActionUse(action)) {
			return;
		}
		applyStillMindReminder();
	}

	private void applyStillMindReminder() {
		if (character == null || character.getCombat() == null) return;
		removeCombatStatusByName(STILL_MIND_REMINDER_STATUS);
		DataStatus status = new DataStatus();
		status.setName(STILL_MIND_REMINDER_STATUS);
		status.setAffinity("None");
		status.setAttribute("REMINDER");
		status.setSeverity(0.0);
		status.setDurationType("Turn");
		status.setDuration(1);
		status.setDescription(resolveStillMindReminderText());
		character.getCombat().addStatus(status);
		character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
		refreshSelectedActionView();
	}

	private void removeCombatStatusByName(String statusName) {
		if (character == null || character.getCombat() == null || statusName == null) return;
		List<DataStatus> statuses = character.getCombat().getCombatStatus();
		for (int i = statuses.size() - 1; i >= 0; i--) {
			DataStatus status = statuses.get(i);
			if (status != null && statusName.equalsIgnoreCase(status.getName())) {
				statuses.remove(i);
			}
		}
	}

	private String resolveStillMindReminderText() {
		if (character != null && character.getSpecials() != null) {
			for (DataSpecialty specialty : character.getSpecials().getAllSpecialties()) {
				if (specialty == null || specialty.getName() == null) continue;
				if (!STILL_MIND_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) continue;
				String description = specialty.getDescription();
				if (description != null) {
					String trimmed = description.trim();
					if (!trimmed.isBlank() && !"Stuff".equalsIgnoreCase(trimmed)) {
						return trimmed;
					}
				}
				break;
			}
		}
		return STILL_MIND_SPECIALTY;
	}

	private boolean shouldHideForCurrentTurnState(DataAction action) {
		if (action == null) return false;
		if (isNamedAction(action, FOLLOW_UP_ACTION)) {
			return !hasFollowUpDamageThisTurn();
		}
		if (isStillMindAction(action)) {
			return !isStillMindCurrentlyAvailable();
		}
		return false;
	}

	private boolean hasFollowUpDamageThisTurn() {
		return character != null
				&& character.getCombat() != null
				&& character.getCombat().getDamageDealtThisTurn() > 0.0001;
	}

	private boolean isStillMindAction(DataAction action) {
		return isNamedAction(action, STILL_MIND_SPECIALTY);
	}

	private boolean hasStillMindSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(STILL_MIND_SPECIALTY);
	}

	private boolean isStillMindCurrentlyAvailable() {
		return myTurnActive && stillMindStartWindowOpen && hasStillMindSpecialty();
	}
	
	void updateMoveAct() {
		clearDisplayedActions();
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getMoveActions();
		String tempString = (String)moveActCat.getSelectedItem();
		String abilityCategory = (String)moveAbilityCat.getSelectedItem();
		boolean showAllTypes = tempString == null || tempString.equalsIgnoreCase("All");
		ArrayList<DataAction> filtered = new ArrayList<>();
		for (DataAction action : tempActions) {
			if (showAllTypes || (action != null && "Move".equalsIgnoreCase(action.getActionType()))) {
				filtered.add(action);
			}
		}
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)) {
			filtered.removeIf(action -> !matchesActionCategory(action, abilityCategory));
		}

		if (filtered.isEmpty()) {
			JButton tempButton = buildButton(null);
			tempButton.setText("No move actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			refreshOptionPanelSize();
			return;
		}

		for (int i = 0; i < filtered.size(); i++) {
			JButton tempButton = buildButton(filtered.get(i));
			tempButton.setText(filtered.get(i).getName());
			if (tempButton instanceof ActionEntryButton)
				applyActionButtonColor(tempButton, filtered.get(i));

			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickMoveAction(e));
			actionList.add(filtered.get(i));
		}
		refreshOptionPanelSize();
	}
	
	void updateAuraAct() {
		clearDisplayedActions();
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getAuraActions();
		String tempString = (String)auraActCat.getSelectedItem();
		String abilityCategory = (String)auraAbilityCat.getSelectedItem();
		boolean showAllTypes = tempString == null || tempString.equalsIgnoreCase("All");
		ArrayList<DataAction> filtered = new ArrayList<>();
		for (DataAction action : tempActions) {
			if (showAllTypes || (action.getSource() != null && action.getSource().equalsIgnoreCase(tempString))) {
				filtered.add(action);
			}
		}
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)) {
			filtered.removeIf(action -> !matchesActionCategory(action, abilityCategory));
		}
		
		if (filtered.isEmpty()) {
			JButton tempButton = buildButton(null);
			tempButton.setText("No aura actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			refreshOptionPanelSize();
			return;
		}
		
		for (int i = 0; i < filtered.size(); i++) {
			JButton tempButton = buildButton(filtered.get(i));
			tempButton.setText(filtered.get(i).getName());
			if (tempButton instanceof ActionEntryButton)
				applyActionButtonColor(tempButton, filtered.get(i));

			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickAuraAction(e));
			actionList.add(filtered.get(i));
		}
		refreshOptionPanelSize();
	}
	
	void updateFreeAct() {
		clearDisplayedActions();
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getFreeActions();
		String tempString = (String)stdActCat.getSelectedItem();
		String abilityCategory = (String)stdAbilityCat.getSelectedItem();
		boolean showAllTypes = tempString == null || tempString.equalsIgnoreCase("All");
		ArrayList<DataAction> filtered = new ArrayList<>();

		for (DataAction action : tempActions) {
			if (showAllTypes || (action != null && action.getSource() != null && action.getSource().equalsIgnoreCase(tempString))) {
				filtered.add(action);
			}
		}
		filtered.removeIf(this::isHiddenUntilNextTurn);
		filtered.removeIf(this::shouldHideForCurrentTurnState);
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)) {
			filtered.removeIf(action -> !matchesActionCategory(action, abilityCategory));
		}

		if (filtered.isEmpty()) {
			JButton tempButton = buildButton(null);
			tempButton.setText("No free actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			refreshOptionPanelSize();
			return;
		}
		
		for (int i = 0; i < filtered.size(); i++) {
			JButton tempButton = buildButton(filtered.get(i));
			tempButton.setText(filtered.get(i).getName());
			if (tempButton instanceof ActionEntryButton) 
				applyActionButtonColor(tempButton, filtered.get(i));

			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickFreeAction(e));
			actionList.add(filtered.get(i));
		}
		refreshOptionPanelSize();
	}
	
	void updateIntAct() {
		clearDisplayedActions();
		
		if (character == null || character.getCombat() == null) {
			JButton tempButton = buildButton(null);
			tempButton.setText("No actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			refreshOptionPanelSize();
			return;
		}

		List<DataAction> tempActions = character.getCombat().getInterruptActions();
		String tempString = (String)stdActCat.getSelectedItem();
		String abilityCategory = (String)stdAbilityCat.getSelectedItem();
		boolean showAllTypes = tempString == null || tempString.equalsIgnoreCase("All");
		ArrayList<DataAction> filtered = new ArrayList<>();
		for (DataAction action : tempActions) {
			if (showAllTypes || (action != null && action.getSource() != null && action.getSource().equalsIgnoreCase(tempString))) {
				filtered.add(action);
			}
		}
		filtered.removeIf(this::isHiddenUntilNextTurn);
		filtered.removeIf(this::shouldHideForCurrentTurnState);
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)) {
			filtered.removeIf(action -> !matchesActionCategory(action, abilityCategory));
		}
		if (filtered.isEmpty()) {
			JButton tempButton = buildButton(null);
			tempButton.setText("No actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			refreshOptionPanelSize();
			return;
		}

		for (int i = 0; i < filtered.size(); i++) {
			JButton tempButton = buildButton(filtered.get(i));
			tempButton.setText(filtered.get(i).getName());
			if (tempButton instanceof ActionEntryButton) {
				applyActionButtonColor(tempButton, filtered.get(i));
			}
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickIntAction(e));
			actionList.add(filtered.get(i));
		}
		refreshOptionPanelSize();
	}
	
	void pickStdAction(ActionEvent e) {
		if (stdActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of standard Actions.");
		}
		else {
			DataAction action = getClickedAction(e);
			if (action != null && ensureActionRequirementsAvailable(action)) {
				if (isSalvoAction(action)) {
					startSalvoSequence();
					return;
				}
				if (openStepPopupIfNeeded(action)) {
					return;
				}
				openActionFrameWithSneakPrompt(action);
			}
		}
	}
	
	void pickMoveAction(ActionEvent e) {
		if (moveActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of move Actions.");
		}
		else {
			DataAction action = getClickedAction(e);
			if (action != null) {
				if (!ensureActionRequirementsAvailable(action)) {
					return;
				}
				if (openStepPopupIfNeeded(action)) {
					return;
				}
				if (openNextAttackFrameIfNeeded(action)) {
					return;
				}
				if (openInformFrameIfNeeded(action)) {
					return;
				}
				if (openAttackFrameIfNeeded(action)) {
					return;
				}
				if (isStandardMoveAction(action)) {
					int moveDistance = getDerivedCombatValue("MOVE");
					int moveChoice = JOptionPane.showConfirmDialog(
							this,
							"You can move up to " + moveDistance + " ft.\nConfirm this move action?",
							"Move",
							JOptionPane.OK_CANCEL_OPTION);
					if (moveChoice == JOptionPane.OK_OPTION) {
						finishActionUse("Move");
					}
				} else {
					finishActionUse("Move");
				}
			}
		}
	}
	
	void pickAuraAction(ActionEvent e) {
		if (auraActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of aura Actions.");
		}
		else {
			DataAction action = getClickedAction(e);
			if (action != null) {
				if (!ensureActionRequirementsAvailable(action)) {
					return;
				}
				if (openStepPopupIfNeeded(action)) {
					return;
				}
				if (openNextAttackFrameIfNeeded(action)) {
					return;
				}
				if (openInformFrameIfNeeded(action)) {
					return;
				}
				if (openAttackFrameIfNeeded(action)) {
					return;
				}
				if (isNamedAction(action, CRUSADER_SEAL_ACTION)) {
					if (finishActionUse(action)) {
						activateCrusaderSeal();
					}
					return;
				}
				finishActionUse(action);
			}
		}
	}
	
	void pickFreeAction(ActionEvent e) {
		DataAction action = getClickedAction(e);
		if (action != null && ensureActionRequirementsAvailable(action)) {
			if (isNamedAction(action, UNLEASH_CRUSADER_SEAL_ACTION)) {
				unleashCrusaderSeal(action);
				return;
			}
			if (openStepPopupIfNeeded(action)) {
				return;
			}
			if (openNextAttackFrameIfNeeded(action)) {
				return;
			}
			if (openInformFrameIfNeeded(action)) {
				return;
			}
			openAttackFrameIfNeeded(action);
		}
	}
	
	void pickIntAction(ActionEvent e) {
		DataAction action = getClickedAction(e);
		if (action != null && ensureActionRequirementsAvailable(action)) {
			if (isStillMindAction(action)) {
				activateStillMindAction(action, false);
				return;
			}
			if (openStepPopupIfNeeded(action)) {
				return;
			}
			openActionFrame(action);
		}
	}
	
	private void buildIcons(ImageIcon[] list, String image, int index) {
		try {
			Path imagePath = AppPaths.imagesDir().resolve(image + ".png");
			File src = imagePath.toFile();
			if (!src.exists()) {
				list[index] = new ImageIcon(new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB));
				return;
			}

			BufferedImage tempBuffImage = ImageIO.read(src);
			int targetSize = (index < 5) ? 100 : 50;
			Image tempImage = tempBuffImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
			list[index] = new ImageIcon(tempImage);
		} catch (Exception e) {
			// Fallback: empty icon to avoid null deref at runtime
			list[index] = new ImageIcon(new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB));
			System.err.println("Failed to load combat icon '" + image + "': " + e.getMessage());
		}
	}
	
	private JRadioButton buildRadioButton(ImageIcon image, ImageIcon image2) {
		JRadioButton tempButton = null;
		tempButton = new JRadioButton(image);

		tempButton.setBorder(BorderFactory.createEmptyBorder());
		tempButton.setContentAreaFilled(false);
		tempButton.setSelectedIcon(image2);
		buttonGroup.add(tempButton);
		add(tempButton);
		
		return tempButton;
	}
	
	private JButton buildDemoButton(ImageIcon image, ImageIcon image2) {
		JButton tempButton = null;
		tempButton = new JButton(image);

		tempButton.setBorder(BorderFactory.createEmptyBorder());
		tempButton.setContentAreaFilled(false);
		tempButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				((AbstractButton) e.getComponent()).setIcon(image2);
			}
			public void mouseReleased(MouseEvent e) {
				((AbstractButton) e.getComponent()).setIcon(image);
			}
		});
		add(tempButton);
		
		return tempButton;
	}
	
	
	
	void stdActionFinish(String at) {
		resolveAttackAction(at);
	}

	boolean resolveAttackAction(DataAction action) {
		return resolveAttackAction(action, false, false, true);
	}

	boolean resolveAttackAction(DataAction action, boolean spendAngelPoint, boolean suppressAngelPointPrompt) {
		return resolveAttackAction(action, spendAngelPoint, suppressAngelPointPrompt, true);
	}

	boolean resolveAttackAction(DataAction action, boolean spendAngelPoint, boolean suppressAngelPointPrompt, boolean consumeActionUse) {
		if (action == null) return false;
		advanceTrackedStatuses("Next Attack");
		return finishActionUse(action, spendAngelPoint, suppressAngelPointPrompt, consumeActionUse);
	}

	void resolveAttackAction(String at) {
		advanceTrackedStatuses("Next Attack");
		finishActionUse(at);
	}

	boolean finishActionUse(DataAction action) {
		return finishActionUse(action, false, false, true);
	}

	boolean finishActionUse(DataAction action, boolean spendAngelPoint, boolean suppressAngelPointPrompt) {
		return finishActionUse(action, spendAngelPoint, suppressAngelPointPrompt, true);
	}

	boolean finishActionUse(DataAction action, boolean spendAngelPoint, boolean suppressAngelPointPrompt, boolean consumeActionUse) {
		if (action == null) return false;
		if (consumeActionUse && !ensureActionRequirementsAvailable(action)) {
			return false;
		}
		if (spendAngelPoint && !canSpendAngelPoint()) {
			return false;
		}
		boolean offerDualWieldOffhand = shouldOfferDualWieldOffhand(action);
		if (consumeActionUse) {
			consumeActionUse(action);
		}
		if (spendAngelPoint) {
			spendAngelPoint();
		}
		else if (!suppressAngelPointPrompt) {
			offerAngelPointSpendIfNeeded(action);
		}
		if (isAttackAction(action)) {
			resolvedAttackCountThisTurn++;
		}
		stillMindStartWindowOpen = false;
		markHiddenUntilNextTurn(action);
		refreshResourceDisplays();
		myTurn();
		if (offerDualWieldOffhand) {
			SwingUtilities.invokeLater(this::offerDualWieldOffhandAttack);
		}
		return true;
	}

	void finishActionUse(String at) {
		consumeActionUse(at);
		myTurn();
	}

	private void consumeActionUse(DataAction action) {
		if (action == null) return;
		ActionRequirements requirements = summarizeActionRequirements(action);
		consumeActionSlots(requirements.standardActions, requirements.moveActions, requirements.auraActions);
		if (character == null || character.getResources() == null) return;
		if (requirements.aura > 0.0) {
			double spentAura = character.getResources().getSpentAura();
			character.getResources().setSpentAura(Math.max(0.0, spentAura + requirements.aura));
		}
		if (requirements.reactions > 0) {
			double spentReactions = character.getResources().getSpentReactions();
			character.getResources().setSpentReactions(Math.max(0.0, spentReactions + requirements.reactions));
		}
		if (requirements.resource1 > 0) {
			double spentR1 = character.getResources().getSpentR1();
			character.getResources().setSpentR1(Math.max(0.0, spentR1 + requirements.resource1));
		}
		if (requirements.resource2 > 0) {
			double spentR2 = character.getResources().getSpentR2();
			character.getResources().setSpentR2(Math.max(0.0, spentR2 + requirements.resource2));
		}
		if (requirements.resource3 > 0) {
			double spentR3 = character.getResources().getSpentR3();
			character.getResources().setSpentR3(Math.max(0.0, spentR3 + requirements.resource3));
		}
	}

	private void consumeActionUse(String at) {
		if ("Standard".equalsIgnoreCase(at)) {
			consumeActionSlots(1, 0, 0);
		}
		else if ("Move".equalsIgnoreCase(at)) {
			consumeActionSlots(0, 1, 0);
		}
		else if ("Aura".equalsIgnoreCase(at)) {
			consumeActionSlots(0, 0, 1);
		}
	}

	private void consumeActionSlots(int standardActions, int moveActions, int auraActions) {
		if (standardActions > 0) {
			stdActCount = Math.max(0, stdActCount - standardActions);
		}
		if (moveActions > 0) {
			moveActCount = Math.max(0, moveActCount - moveActions);
		}
		if (auraActions > 0) {
			auraActCount = Math.max(0, auraActCount - auraActions);
		}
	}

	private void offerAngelPointSpendIfNeeded(DataAction action) {
		if (!shouldOfferAngelPointSpend(action)) return;
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Spend 1 Angel Point as an interrupt for this aura technique?",
				ANGEL_POINTS_LABEL,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		spendAngelPoint();
	}

	boolean shouldOfferAngelPointSpend(DataAction action) {
		return isAuraTechniqueAction(action) && canSpendAngelPoint();
	}

	private boolean canSpendAngelPoint() {
		return character != null
				&& character.hasAngelPoints()
				&& character.getResources() != null
				&& character.getCurrentAngelPoints() > 0;
	}

	private boolean spendAngelPoint() {
		if (!canSpendAngelPoint()) {
			return false;
		}
		double spentAngelPoints = character.getResources().getSpentAngelPoints();
		character.getResources().setSpentAngelPoints(Math.max(0.0, spentAngelPoints + 1.0));
		return true;
	}

	private boolean isAuraTechniqueAction(DataAction action) {
		return action != null
				&& action.getSource() != null
				&& "Aura".equalsIgnoreCase(action.getSource());
	}

	private void markHiddenUntilNextTurn(DataAction action) {
		if (action == null || !isHiddenAfterUseActionType(action.getActionType())) return;
		String key = buildActionUsageKey(action);
		if (key != null) {
			hiddenUntilNextTurnActionKeys.add(key);
		}
	}

	private boolean isHiddenUntilNextTurn(DataAction action) {
		if (action == null) return false;
		String key = buildActionUsageKey(action);
		return key != null && hiddenUntilNextTurnActionKeys.contains(key);
	}

	private boolean isHiddenAfterUseActionType(String actionType) {
		return "Free".equalsIgnoreCase(actionType) || "Interrupt".equalsIgnoreCase(actionType);
	}

	private String buildActionUsageKey(DataAction action) {
		if (action == null) return null;
		if (action.getId() > 0) {
			return "ID:" + action.getId();
		}
		String name = action.getName() == null ? "" : action.getName().trim().toLowerCase();
		String source = action.getSource() == null ? "" : action.getSource().trim().toLowerCase();
		String type = action.getActionType() == null ? "" : action.getActionType().trim().toLowerCase();
		if (name.isBlank() && source.isBlank() && type.isBlank()) return null;
		return "KEY:" + name + "|" + source + "|" + type;
	}

	private void openActionFrame(DataAction action) {
		openActionFrame(action, false, false, false, 0, true);
	}

	private void openActionFrameWithSneakPrompt(DataAction action) {
		openActionFrameWithSneakPrompt(action, true);
	}

	private void openActionFrameWithSneakPrompt(DataAction action, boolean consumeActionUseOnResolve) {
		boolean sneakAttackSelected = promptForSneakAttackIfNeeded(action);
		boolean unarmedProwessSelected = promptForUnarmedProwessIfNeeded(action);
		boolean favoredEnemySelected = promptForFavoredEnemyIfNeeded(action);
		int snipersDomainTdmgBonus = promptForSnipersDomainDamageBonusIfNeeded(action);
		openActionFrame(action, sneakAttackSelected, unarmedProwessSelected, favoredEnemySelected, snipersDomainTdmgBonus, consumeActionUseOnResolve);
	}

	private void openActionFrame(DataAction action, boolean sneakAttackSelected) {
		openActionFrame(action, sneakAttackSelected, false, false, 0, true);
	}

	private void openActionFrame(DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected) {
		openActionFrame(action, sneakAttackSelected, unarmedProwessSelected, false, 0, true);
	}

	private void openActionFrame(DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected, boolean favoredEnemySelected) {
		openActionFrame(action, sneakAttackSelected, unarmedProwessSelected, favoredEnemySelected, 0, true);
	}

	private void openActionFrame(DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected, boolean favoredEnemySelected, int snipersDomainTdmgBonus) {
		openActionFrame(action, sneakAttackSelected, unarmedProwessSelected, favoredEnemySelected, snipersDomainTdmgBonus, true);
	}

	private void openActionFrame(DataAction action, boolean sneakAttackSelected, boolean unarmedProwessSelected, boolean favoredEnemySelected, int snipersDomainTdmgBonus, boolean consumeActionUseOnResolve) {
		if (action == null) return;
		JFrame actionFrame;
		if (isNextAttackAction(action)) {
			actionFrame = new FrameNextAttack(sheetFrame, this, character, action);
		} else if (isInformAction(action)) {
			actionFrame = new FrameInform(sheetFrame, this, character, action);
		} else if (isHealAction(action)) {
			actionFrame = new FrameHeal(sheetFrame, this, character, action);
		} else {
			actionFrame = new FrameAttack(sheetFrame, this, character, action, sneakAttackSelected, unarmedProwessSelected, favoredEnemySelected, snipersDomainTdmgBonus, consumeActionUseOnResolve);
		}
		actionFrame.setVisible(true);
	}

	private boolean promptForSneakAttackIfNeeded(DataAction action) {
		if (!shouldPromptForSneakAttack(action)) {
			return false;
		}
		String message = hasStealthStrikeSneakStatus()
				? "Stealth Strike allows all of your attacks to deal Sneak Attack damage until end of turn.\nIs this attack a Sneak Attack?"
				: "Is this attack a Sneak Attack?";
		int choice = JOptionPane.showConfirmDialog(
				this,
				message,
				"Sneak Attack",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return choice == JOptionPane.YES_OPTION;
	}

	private boolean shouldPromptForSneakAttack(DataAction action) {
		return isAttackAction(action)
				&& character != null
				&& ((character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Sneak Attack"))
				|| hasStealthStrikeSneakStatus());
	}

	private boolean hasStealthStrikeSneakStatus() {
		return character != null
				&& character.getCombat() != null
				&& character.getCombat().hasCombatStatusAttribute(STEALTH_STRIKE_SNEAK_MARKER);
	}

	private boolean promptForUnarmedProwessIfNeeded(DataAction action) {
		if (!shouldPromptForUnarmedProwess(action)) {
			return false;
		}
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Is this an Unarmed Attack?",
				"Unarmed Prowess",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return choice == JOptionPane.YES_OPTION;
	}

	private boolean shouldPromptForUnarmedProwess(DataAction action) {
		return isAttackAction(action)
				&& character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Unarmed Prowess");
	}

	private boolean promptForFavoredEnemyIfNeeded(DataAction action) {
		if (!shouldPromptForFavoredEnemy(action)) {
			return false;
		}
		String favoredEnemies = String.join(", ", getCharacterListEntries("Favored Enemies"));
		if (favoredEnemies.isBlank()) {
			favoredEnemies = "None";
		}
		int choice = JOptionPane.showConfirmDialog(
				this,
				"<html>Is the target a favored enemy?<br>Favored Enemies: " + favoredEnemies + "</html>",
				"Favored Enemy",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return choice == JOptionPane.YES_OPTION;
	}

	private boolean shouldPromptForFavoredEnemy(DataAction action) {
		return isAttackAction(action)
				&& character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Favored Enemy");
	}

	private int promptForSnipersDomainDamageBonusIfNeeded(DataAction action) {
		if (!shouldPromptForSnipersDomainDistance(action)) {
			return 0;
		}
		while (true) {
			String input = JOptionPane.showInputDialog(
					this,
					"Distance to target:",
					"Sniper's Domain",
					JOptionPane.PLAIN_MESSAGE);
			if (input == null) {
				return 0;
			}
			String trimmed = input.trim();
			if (trimmed.isBlank()) {
				return 0;
			}
			try {
				double distance = Double.parseDouble(trimmed);
				if (distance < 0) {
					JOptionPane.showMessageDialog(this, "Please enter a non-negative distance.", "Sniper's Domain", JOptionPane.WARNING_MESSAGE);
					continue;
				}
				int level = character != null && character.getIdentity() != null
						? Math.max(0, character.getIdentity().getLevel())
						: 0;
				return Math.max(0, (int)(distance * level / 2.5));
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Sniper's Domain", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private boolean shouldPromptForSnipersDomainDistance(DataAction action) {
		return isAttackAction(action)
				&& character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Sniper's Domain");
	}

	private List<String> getCharacterListEntries(String listName) {
		ArrayList<String> entries = new ArrayList<>();
		if (character == null || character.getLists() == null || listName == null || listName.isBlank()) return entries;
		for (List<DataList> group : character.getLists()) {
			if (group == null) continue;
			for (DataList entry : group) {
				if (entry == null || entry.getList() == null || entry.getName() == null) continue;
				if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
				String name = entry.getName().trim();
				if (!name.isBlank()) {
					entries.add(name);
				}
			}
		}
		return entries;
	}

	private DataAction getClickedAction(ActionEvent e) {
		if (e == null) return null;
		for (int i = 0; i < actionButtons.size(); i++) {
			if (e.getSource() == actionButtons.get(i)) {
				return actionList.get(i);
			}
		}
		return null;
	}

	private boolean openNextAttackFrameIfNeeded(DataAction action) {
		if (!isNextAttackAction(action)) return false;
		openActionFrame(action);
		return true;
	}

	private boolean openInformFrameIfNeeded(DataAction action) {
		if (!isInformAction(action)) return false;
		openActionFrame(action);
		return true;
	}

	private boolean openAttackFrameIfNeeded(DataAction action) {
		if (!isAttackAction(action)) return false;
		openActionFrameWithSneakPrompt(action);
		return true;
	}

	private boolean openStepPopupIfNeeded(DataAction action) {
		if (!isStepLikeAction(action)) return false;
		String message = isDefensiveStepAction(action)
				? "This Defensive Step allows you to move 5 ft and does not provoke attacks of opportunity."
				: "This Step allows you to move 5 ft and does not prevent attacks of opportunity.";
		String title = isDefensiveStepAction(action) ? "Defensive Step" : "Step";
		int choice = JOptionPane.showConfirmDialog(
				this,
				message,
				title,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return true;
		}
		finishActionUse(action);
		return true;
	}

	private boolean isNextAttackAction(DataAction action) {
		return hasActionCategory(action, "Next Attack");
	}

	private boolean isHealAction(DataAction action) {
		return hasActionCategory(action, "Heal");
	}

	private boolean isInformAction(DataAction action) {
		return hasActionCategory(action, "Inform");
	}

	private boolean isAttackAction(DataAction action) {
		return hasActionCategory(action, "Attack");
	}

	private boolean shouldOfferDualWieldOffhand(DataAction action) {
		return myTurnActive
				&& resolvedAttackCountThisTurn == 0
				&& isAttackAction(action)
				&& !isOffhandAttackAction(action)
				&& hasDualWieldSpecialty();
	}

	private boolean isOffhandAttackAction(DataAction action) {
		return action != null
				&& action.getName() != null
				&& OFFHAND_ATTACK_ACTION.equalsIgnoreCase(action.getName().trim());
	}

	private boolean hasDualWieldSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(DUAL_WIELD_SPECIALTY);
	}

	private void offerDualWieldOffhandAttack() {
		if (!myTurnActive || !hasDualWieldSpecialty()) return;
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Would you like to perform an offhand attack?",
				DUAL_WIELD_SPECIALTY,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		DataAction offhandAttack = buildOffhandAttackAction();
		if (offhandAttack == null) {
			JOptionPane.showMessageDialog(
					this,
					"Offhand Attack is unavailable.",
					DUAL_WIELD_SPECIALTY,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		openActionFrame(offhandAttack);
	}

	private DataAction buildOffhandAttackAction() {
		StoreRuleManager dataQuery = new StoreRuleManager();
		DataAction template = dataQuery.getActionByName(OFFHAND_ATTACK_ACTION);
		DataAction offhandAttack = template == null ? new DataAction() : new DataAction(template);
		if (template == null) {
			offhandAttack.setName(OFFHAND_ATTACK_ACTION);
			offhandAttack.setCategory("Attack");
			offhandAttack.setSource("Class");
			offhandAttack.setAffinity("None");
			offhandAttack.setAtkType("AC");
			offhandAttack.setActionType("Free");
			offhandAttack.setCosts(List.of(new DataAction.CostPair("None", 0.0)));
			offhandAttack.setModifierKey(List.of());
		}
		offhandAttack.setCharacter(character);
		return offhandAttack;
	}

	private boolean isStepLikeAction(DataAction action) {
		return isStepAction(action) || isDefensiveStepAction(action);
	}

	private boolean isStepAction(DataAction action) {
		return action != null
				&& action.getName() != null
				&& action.getName().trim().equalsIgnoreCase("Step");
	}

	private boolean isDefensiveStepAction(DataAction action) {
		return action != null
				&& action.getName() != null
				&& action.getName().trim().equalsIgnoreCase("Defensive Step");
	}

	private boolean ensureActionRequirementsAvailable(DataAction action) {
		if (action == null) return false;
		ActionRequirements requirements = summarizeActionRequirements(action);
		ArrayList<String> shortages = new ArrayList<>();
		if (requirements.standardActions > stdActCount) {
			shortages.add("Standard Actions: " + requirements.standardActions + " required, " + stdActCount + " available");
		}
		if (requirements.moveActions > moveActCount) {
			shortages.add("Move Actions: " + requirements.moveActions + " required, " + moveActCount + " available");
		}
		if (requirements.auraActions > auraActCount) {
			shortages.add("Aura Actions: " + requirements.auraActions + " required, " + auraActCount + " available");
		}

		double availableAura = getCurrentAuraAvailable();
		if (requirements.aura - availableAura > 0.0001) {
			shortages.add("Aura: " + trimNumber(requirements.aura) + " required, " + trimNumber(availableAura) + " available");
		}
		int availableReactions = getCurrentReactionsAvailable();
		if (requirements.reactions > availableReactions) {
			shortages.add("Reactions: " + requirements.reactions + " required, " + availableReactions + " available");
		}
		int availableR1 = getCurrentResourceAvailable(1);
		if (requirements.resource1 > availableR1) {
			shortages.add("R1: " + requirements.resource1 + " required, " + availableR1 + " available");
		}
		int availableR2 = getCurrentResourceAvailable(2);
		if (requirements.resource2 > availableR2) {
			shortages.add("R2: " + requirements.resource2 + " required, " + availableR2 + " available");
		}
		int availableR3 = getCurrentResourceAvailable(3);
		if (requirements.resource3 > availableR3) {
			shortages.add("R3: " + requirements.resource3 + " required, " + availableR3 + " available");
		}

		if (shortages.isEmpty()) {
			return true;
		}

		String actionName = action.getName() == null || action.getName().isBlank() ? "this action" : action.getName();
		String message = "You do not have enough resources to use " + actionName + ".\n"
				+ String.join("\n", shortages);
		JOptionPane.showMessageDialog(this, message, "Insufficient Resources", JOptionPane.WARNING_MESSAGE);
		return false;
	}

	private ActionRequirements summarizeActionRequirements(DataAction action) {
		ActionRequirements requirements = new ActionRequirements();
		if (action == null) return requirements;

		String actionType = action.getActionType();
		if ("Standard".equalsIgnoreCase(actionType)) {
			requirements.standardActions += 1;
		} else if ("Move".equalsIgnoreCase(actionType)) {
			requirements.moveActions += 1;
		} else if ("Aura".equalsIgnoreCase(actionType)) {
			requirements.auraActions += 1;
		}

		for (DataAction.CostPair cost : action.getCosts()) {
			if (cost == null || cost.getType() == null) continue;
			String costType = cost.getType().trim();
			if (costType.equalsIgnoreCase("StandardAction")) {
				requirements.standardActions += normalizeDiscreteActionCost(cost.getValue());
			} else if (costType.equalsIgnoreCase("MoveAction")) {
				requirements.moveActions += normalizeDiscreteActionCost(cost.getValue());
			} else if (costType.equalsIgnoreCase("AuraAction")) {
				requirements.auraActions += normalizeDiscreteActionCost(cost.getValue());
			} else if (costType.equalsIgnoreCase("Aura")) {
				requirements.aura += Math.max(0.0, cost.getValue());
			} else if (costType.equalsIgnoreCase("Reaction")
					|| costType.equalsIgnoreCase("Reactions")
					|| costType.equalsIgnoreCase("React")) {
				requirements.reactions += normalizeDiscreteActionCost(cost.getValue());
			} else if (costType.equalsIgnoreCase("R1")) {
				requirements.resource1 += normalizeDiscreteActionCost(cost.getValue());
			} else if (costType.equalsIgnoreCase("R2")) {
				requirements.resource2 += normalizeDiscreteActionCost(cost.getValue());
			} else if (costType.equalsIgnoreCase("R3")) {
				requirements.resource3 += normalizeDiscreteActionCost(cost.getValue());
			}
		}
		return requirements;
	}

	private int normalizeDiscreteActionCost(double amount) {
		return (int)Math.max(0, Math.ceil(amount));
	}

	private double getCurrentAuraAvailable() {
		if (character == null || character.getResources() == null) return 0.0;
		return Math.max(0.0, character.getResources().calcCurrentAura());
	}

	private int getCurrentReactionsAvailable() {
		if (character == null || character.getResources() == null) return 0;
		return Math.max(0, character.getResources().calcCurrentReactions());
	}

	private int getCurrentResourceAvailable(int resourceNumber) {
		if (character == null || character.getResources() == null) return 0;
		return switch (resourceNumber) {
			case 1 -> Math.max(0, character.getResources().calcCurrentResource1());
			case 2 -> Math.max(0, character.getResources().calcCurrentResource2());
			case 3 -> Math.max(0, character.getResources().calcCurrentResource3());
			default -> 0;
		};
	}

	private void refreshResourceDisplays() {
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
	}

	private void resetActionCountsToMaximum() {
		stdActCount = getMaxStandardActionCount();
		moveActCount = getMaxMoveActionCount();
		auraActCount = getMaxAuraActionCount();
	}

	private int getMaxStandardActionCount() {
		return 1;
	}

	private int getMaxMoveActionCount() {
		return 1;
	}

	private int getMaxAuraActionCount() {
		return 1;
	}

	// -------------------------
	// Local helpers (formerly from FrameHelper)
	// -------------------------

	private JScrollPane buildScrollPane(JPanel panel) {
		JScrollPane pane = new JScrollPane(panel);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return pane;
	}

	private JButton buildButton(DataAction action) {
		JButton btn = new JButton();
		if (action != null) {
			String affinity = action.getAffinity();
			if (!(affinity == null || affinity.isBlank() || "None".equalsIgnoreCase(affinity)))
				btn = new ActionEntryButton();
		}
		btn.setFocusPainted(false);
		return btn;
	}

	private void applyActionButtonColor(JButton button, DataAction action) {
		if (!(button instanceof ActionEntryButton actionButton)) return;
		actionButton.clearAffinityColors();

		if (action == null) return;
		String affinity = action.getAffinity();
		if (affinity == null || affinity.isBlank() || "None".equalsIgnoreCase(affinity)) return;

		//DataColor color = StoreMetaManager.getDataQuery().getColorByTitle(affinity);
		//if (color == null) return;

		//actionButton.setAffinityColors(color.getBackColor(), color.getForeColor());
	}

	private static final class ActionEntryButton extends JButton {
		private static final long serialVersionUID = 1L;
		private java.awt.Color affinityBack;
		private java.awt.Color affinityFore;

		ActionEntryButton() {
			setContentAreaFilled(false);
			setOpaque(false);
		}

		void clearAffinityColors() {
			affinityBack = null;
			affinityFore = null;
			java.awt.Color defaultFore = UIManager.getColor("Button.foreground");
			setForeground(defaultFore != null ? defaultFore : java.awt.Color.BLACK);
			repaint();
		}

		void setAffinityColors(java.awt.Color background, java.awt.Color foreground) {
			affinityBack = background;
			affinityFore = foreground;
			setForeground(foreground != null ? foreground : java.awt.Color.BLACK);
			repaint();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			if (affinityBack == null) {
				super.paintComponent(graphics);
				return;
			}

			Graphics2D g2 = (Graphics2D) graphics.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			java.awt.Color top = blend(affinityBack, java.awt.Color.WHITE, getModel().isPressed() ? 0.18f : 0.35f);
			java.awt.Color bottom = darken(affinityBack, getModel().isPressed() ? 0.20f : 0.10f);
			if (!isEnabled()) {
				top = blend(top, java.awt.Color.LIGHT_GRAY, 0.45f);
				bottom = blend(bottom, java.awt.Color.GRAY, 0.45f);
			}

			g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setColor(blend(top, java.awt.Color.WHITE, 0.55f));
			g2.drawLine(1, 1, Math.max(1, getWidth() - 2), 1);
			g2.setColor(darken(affinityBack, 0.30f));
			g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			g2.dispose();

			super.paintComponent(graphics);
		}

		@Override
		protected void paintBorder(Graphics graphics) {
			if (affinityBack == null) {
				super.paintBorder(graphics);
			}
		}

		private static java.awt.Color darken(java.awt.Color base, float amount) {
			int red = Math.max(0, Math.round(base.getRed() * (1.0f - amount)));
			int green = Math.max(0, Math.round(base.getGreen() * (1.0f - amount)));
			int blue = Math.max(0, Math.round(base.getBlue() * (1.0f - amount)));
			return new java.awt.Color(red, green, blue);
		}

		private static java.awt.Color blend(java.awt.Color left, java.awt.Color right, float ratio) {
			float bounded = Math.max(0.0f, Math.min(1.0f, ratio));
			float inverse = 1.0f - bounded;
			int red = Math.round(left.getRed() * inverse + right.getRed() * bounded);
			int green = Math.round(left.getGreen() * inverse + right.getGreen() * bounded);
			int blue = Math.round(left.getBlue() * inverse + right.getBlue() * bounded);
			return new java.awt.Color(red, green, blue);
		}
	}

	private void clearDisplayedActions() {
		for (int i = actionButtons.size() - 1; i >= 0; i--) {
			JButton button = actionButtons.get(i);
			if (button != null) {
				ActionListener[] listens = button.getActionListeners();
				while (listens.length > 0) {
					button.removeActionListener(listens[0]);
					listens = button.getActionListeners();
				}
				button.setVisible(false);
				optionPanel.remove(button);
			}
			actionButtons.remove(i);
			if (i < actionList.size()) {
				actionList.remove(i);
			}
		}
		refreshOptionPanelSize();
	}

	private void refreshOptionPanelSize() {
		int rowCount = Math.max(1, actionButtons.size());
		int preferredHeight = Math.max(
				OPTION_PANEL_MIN_HEIGHT,
				OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * rowCount + OPTION_PANEL_BOTTOM_PADDING);
		optionPanel.setPreferredSize(new Dimension(OPTION_PANEL_WIDTH, preferredHeight));
		optionPanel.revalidate();
		optionPanel.repaint();
		if (optionPane != null) {
			optionPane.revalidate();
			optionPane.repaint();
			optionPane.getVerticalScrollBar().setValue(0);
		}
	}

	private void clearHelper() {
		headerL.setVisible(false);
		roundHeaderL.setVisible(false);
		for (JLabel lbl : labels) {
			if (lbl != null) {
				lbl.setVisible(false);
			}
		}
		setResourceRowVisible(false);
		showNoCombatImage(false);
		for (JButton btn : buttons) {
			if (btn != null) {
				for (var al : btn.getActionListeners()) {
					btn.removeActionListener(al);
				}
				btn.setVisible(false);
			}
		}
		popupCheckBox.setVisible(false);
	}

	private void advanceTrackedStatuses(String durationType) {
		if (durationType == null || battleStatus == null) return;
		for (int i = 0; i < battleStatus.size(); i++) {
			DataStatus trackedStatus = battleStatus.get(i);
			if (trackedStatus == null || trackedStatus.getDurationType() == null) continue;
			if (!durationType.equalsIgnoreCase(trackedStatus.getDurationType())) continue;
			trackedStatus.setDuration(trackedStatus.getDuration() - 1);
			if (trackedStatus.getDuration() <= 0) {
				battleStatus.remove(i);
				removeTimedStatus(trackedStatus);
				i--;
			}
		}
	}

	private boolean applyStatusToCharacter(DataStatus status) {
		if (status == null || character == null || status.getAttribute() == null) return false;
		if (character.isReminderStatus(status)) {
			return true;
		}
		String attribute = status.getAttribute().toUpperCase();
		if (isInstantResourceDeltaAttribute(attribute)) {
			return applyInstantResourceDelta(status);
		}
		if (isStunAttribute(attribute)) {
			if (character.getCombat() == null) return false;
			character.getCombat().adjustStunTokens((int)Math.round(status.getSeverity()));
			return true;
		}
		if (isHeavyAttribute(attribute)) {
			if (character.getCombat() == null) return false;
			character.getCombat().adjustHeavyTokens((int)Math.round(status.getSeverity()));
			return true;
		}
		if (isIncapacitateAttribute(attribute)) {
			if (character.getCombat() == null) return false;
			character.getCombat().adjustIncapacitateTokens((int)Math.round(status.getSeverity()));
			return true;
		}
		if (isRootAttribute(attribute)) {
			if (character.getCombat() == null) return false;
			character.getCombat().adjustRootTokens((int)Math.round(status.getSeverity()));
			return true;
		}
		try {
			if (isResourceAttribute(attribute)) {
				character.getResources().addStatus(status);
			} else {
				character.getAttributes().addStatus(status);
			}
			return true;
		} catch (RuntimeException ex) {
			return false;
		}
	}

	private void removeTimedStatus(DataStatus status) {
		if (status == null || character == null || status.getAttribute() == null) return;
		if (character.isReminderStatus(status)
				|| isCombatMarkerAttribute(status.getAttribute())
				|| isStunAttribute(status.getAttribute())
				|| isHeavyAttribute(status.getAttribute())
				|| isIncapacitateAttribute(status.getAttribute())
				|| isRootAttribute(status.getAttribute())) {
			refreshAfterStatusChange();
			return;
		}
		DataStatus applied = new DataStatus(status);
		applied.setDurationType("Temporary");
		String attribute = applied.getAttribute().toUpperCase();
		if (isResourceAttribute(attribute)) {
			character.getResources().removeStatusByStatus(applied);
		} else {
			character.getAttributes().removeStatusByStatus(applied);
		}
		refreshAfterStatusChange();
	}

	private boolean isTimedDuration(String durationType) {
		return durationType != null
				&& ("TURN".equalsIgnoreCase(durationType)
				|| "ROUND".equalsIgnoreCase(durationType)
				|| "CYCLE".equalsIgnoreCase(durationType)
				|| "NEXT ATTACK".equalsIgnoreCase(durationType));
	}

	private boolean isTrackedDuration(String durationType) {
		return isTimedDuration(durationType);
	}

	public boolean hasPendingNextAttackStatusAttribute(String attribute) {
		if (attribute == null || attribute.isBlank()) return false;
		return findPendingNextAttackStatusAttribute(attribute) != null;
	}

	public DataTechnical getPendingTechnicalData() {
		DataStatus technicalStatus = findPendingNextAttackStatusAttribute("TECH");
		if (technicalStatus == null) return null;

		StoreRuleManager ruleManager = new StoreRuleManager();
		String sourceActionName = extractPendingNextAttackSourceName(technicalStatus);
		if (sourceActionName != null && !sourceActionName.isBlank()) {
			DataTechnical technical = ruleManager.getTechnicalByName(resolveTechnicalNameForAction(sourceActionName));
			if (technical != null) return technical;
		}

		String statusName = technicalStatus.getName();
		if (statusName != null && !statusName.isBlank()) {
			return ruleManager.getTechnicalByName(resolveTechnicalNameForAction(statusName));
		}
		return null;
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

	private DataStatus findPendingNextAttackStatusAttribute(String attribute) {
		if (attribute == null || attribute.isBlank()) return null;
		List<DataStatus> statuses = character != null && character.getCombat() != null
				? character.getCombat().getCombatStatus()
				: battleStatus;
		if (statuses == null) return null;
		for (DataStatus status : statuses) {
			if (status == null || status.getAttribute() == null || status.getDurationType() == null) continue;
			if (!"Next Attack".equalsIgnoreCase(status.getDurationType())) continue;
			if (attribute.equalsIgnoreCase(status.getAttribute().trim())) return status;
		}
		return null;
	}

	private String extractPendingNextAttackSourceName(DataStatus status) {
		if (status == null) return null;
		String name = status.getName();
		if (name != null) {
			int suffixIndex = name.lastIndexOf(" [");
			if (suffixIndex > 0) {
				return name.substring(0, suffixIndex).trim();
			}
		}

		String description = status.getDescription();
		String effectPrefix = "Pending next-attack effect from ";
		String markerPrefix = "Pending next-attack marker for ";
		if (description != null) {
			if (description.regionMatches(true, 0, effectPrefix, 0, effectPrefix.length())) {
				return description.substring(effectPrefix.length()).trim();
			}
			if (description.regionMatches(true, 0, markerPrefix, 0, markerPrefix.length())) {
				return description.substring(markerPrefix.length()).trim();
			}
		}
		return null;
	}

	private boolean isCombatMarkerAttribute(String attribute) {
		if (attribute == null) return false;
		String normalized = attribute.trim();
		return "TECH".equalsIgnoreCase(normalized)
				|| "SMITE".equalsIgnoreCase(normalized)
				|| "STEALTHSTRIKE".equalsIgnoreCase(normalized)
				|| "FLANKING".equalsIgnoreCase(normalized)
				|| "HOTHP".equalsIgnoreCase(normalized)
				|| "HOTSHIELD".equalsIgnoreCase(normalized)
				|| "DMGTAKEN".equalsIgnoreCase(normalized)
				|| CRUSADER_SEAL_MARKER_ATTRIBUTE.equalsIgnoreCase(normalized);
	}

	private boolean isIncapacitateAttribute(String attribute) {
		return attribute != null && "INCAP".equalsIgnoreCase(attribute.trim());
	}

	private boolean isRootAttribute(String attribute) {
		return attribute != null && "ROOT".equalsIgnoreCase(attribute.trim());
	}

	private boolean isResourceAttribute(String attribute) {
		return "BASEHP".equalsIgnoreCase(attribute)
				|| "MULTIHP".equalsIgnoreCase(attribute)
				|| "BASEAURA".equalsIgnoreCase(attribute)
				|| "MULTIAURA".equalsIgnoreCase(attribute)
				|| "BASER1".equalsIgnoreCase(attribute)
				|| "MULTIR1".equalsIgnoreCase(attribute)
				|| "BASER2".equalsIgnoreCase(attribute)
				|| "MULTIR2".equalsIgnoreCase(attribute)
				|| "BASER3".equalsIgnoreCase(attribute)
				|| "MULTIR3".equalsIgnoreCase(attribute)
				|| "BASEREACT".equalsIgnoreCase(attribute)
				|| "MULTIREACT".equalsIgnoreCase(attribute);
	}

	private boolean isStunAttribute(String attribute) {
		return attribute != null && "STUN".equalsIgnoreCase(attribute.trim());
	}

	private boolean isHeavyAttribute(String attribute) {
		return attribute != null && "HEAVY".equalsIgnoreCase(attribute.trim());
	}

	private boolean isInstantResourceDeltaAttribute(String attribute) {
		return attribute != null
				&& ("HP".equalsIgnoreCase(attribute.trim()) || "AURA".equalsIgnoreCase(attribute.trim()));
	}

	private boolean applyInstantResourceDelta(DataStatus status) {
		if (status == null || character == null || character.getResources() == null || status.getAttribute() == null) {
			return false;
		}
		String attribute = status.getAttribute().trim();
		double severity = status.getSeverity();
		if ("HP".equalsIgnoreCase(attribute)) {
			double maxHp = Math.max(0.0, character.getResources().calcMaxHP());
			double newLostHp = clamp(character.getResources().getLostHP() - severity, 0.0, maxHp);
			character.getResources().setLostHP(newLostHp);
			return true;
		}
		if ("AURA".equalsIgnoreCase(attribute)) {
			double maxAura = Math.max(0.0, character.getResources().calcMaxAura());
			double newSpentAura = clamp(character.getResources().getSpentAura() - severity, 0.0, maxAura);
			character.getResources().setSpentAura(newSpentAura);
			return true;
		}
		return false;
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private void refreshAfterStatusChange() {
		if (character != null) {
			character.updateAll();
		}
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
	}

	private void refreshReckoningButtonVisibility() {
		if (reckoningButton == null) return;
		boolean visible = character != null
				&& character.getCombat() != null
				&& character.getCombat().isInCombat()
				&& hasReckoningSpecialty();
		reckoningButton.setVisible(visible);
	}

	private void refreshCrusaderSealButtonVisibility() {
		if (crusaderSealButton == null) return;
		boolean visible = character != null
				&& character.getCombat() != null
				&& character.getCombat().isInCombat()
				&& hasCrusaderSealSpecialty();
		crusaderSealButton.setVisible(visible);
		if (!visible) {
			crusaderSealButton.setEnabled(false);
			crusaderSealButton.setText("Crusader Seal");
			return;
		}

		int charges = getCrusaderSealCharges();
		boolean active = isCrusaderSealActive();
		crusaderSealButton.setEnabled(active);
		crusaderSealButton.setText(active
				? "Crusader Seal +1 (" + charges + ")"
				: "Crusader Seal (Inactive)");
	}

	private boolean hasReckoningSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(RECKONING_SPECIALTY);
	}

	private boolean hasCrusaderSealSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(CRUSADER_SEAL_SPECIALTY);
	}

	private DataStatus findCrusaderSealStatus() {
		if (battleStatus == null) return null;
		for (DataStatus status : battleStatus) {
			if (status == null || status.getAttribute() == null) continue;
			if (CRUSADER_SEAL_MARKER_ATTRIBUTE.equalsIgnoreCase(status.getAttribute().trim())) {
				return status;
			}
		}
		return null;
	}

	private boolean isCrusaderSealActive() {
		return findCrusaderSealStatus() != null;
	}

	private int getCrusaderSealCharges() {
		DataStatus status = findCrusaderSealStatus();
		if (status == null) return 0;
		return Math.max(0, (int)Math.round(status.getSeverity()));
	}

	private void activateCrusaderSeal() {
		if (character == null || character.getCombat() == null) return;
		DataStatus status = findCrusaderSealStatus();
		if (status == null) {
			status = new DataStatus();
			status.setName(CRUSADER_SEAL_MARKER_NAME);
			status.setAffinity("None");
			status.setDescription("Crusader Seal is active and gains charges when allies in your emanation take damage.");
			status.setAttribute(CRUSADER_SEAL_MARKER_ATTRIBUTE);
			status.setSeverity(0.0);
			status.setDurationType("Combat");
			status.setDuration(0);
			character.getCombat().addStatus(status);
		} else {
			status.setSeverity(0.0);
			status.setDurationType("Combat");
		}
		synchronizeCombatStateAfterSealChange(false);
	}

	private void incrementCrusaderSealCharge() {
		DataStatus status = findCrusaderSealStatus();
		if (status == null) {
			JOptionPane.showMessageDialog(
					this,
					"Crusader Seal is not active.",
					"Crusader Seal",
					JOptionPane.INFORMATION_MESSAGE);
			refreshCrusaderSealButtonVisibility();
			return;
		}
		status.setSeverity(Math.max(0.0, status.getSeverity()) + 1.0);
		refreshCrusaderSealButtonVisibility();
	}

	private void unleashCrusaderSeal(DataAction action) {
		if (action == null) return;
		DataStatus marker = findCrusaderSealStatus();
		if (marker == null) {
			JOptionPane.showMessageDialog(
					this,
					"Crusader Seal is not active.",
					"Crusader Seal",
					JOptionPane.INFORMATION_MESSAGE);
			synchronizeCombatStateAfterSealChange(false);
			return;
		}

		int charges = getCrusaderSealCharges();
		removeCrusaderSealStatus();
		if (charges > 0) {
			DataStatus unleashed = new DataStatus();
			unleashed.setName(CRUSADER_SEAL_MARKER_NAME);
			unleashed.setAffinity("None");
			unleashed.setDescription("Crusader Seal grants +" + trimNumber(0.5 * charges) + " MAXATK until end of turn.");
			unleashed.setAttribute("BMAXATK");
			unleashed.setSeverity(0.5 * charges);
			unleashed.setDurationType("Turn");
			unleashed.setDuration(1);
			applyBuiltStatus(unleashed);
		}
		finishActionUse(action);
		synchronizeCombatStateAfterSealChange(true);
	}

	private void removeCrusaderSealStatus() {
		if (battleStatus == null) return;
		for (int i = battleStatus.size() - 1; i >= 0; i--) {
			DataStatus status = battleStatus.get(i);
			if (status == null || status.getAttribute() == null) continue;
			if (CRUSADER_SEAL_MARKER_ATTRIBUTE.equalsIgnoreCase(status.getAttribute().trim())) {
				battleStatus.remove(i);
			}
		}
	}

	private void synchronizeCombatStateAfterSealChange(boolean refreshSelectedView) {
		if (character != null) {
			updateCharacter(character);
		}
		refreshCrusaderSealButtonVisibility();
		if (refreshSelectedView) {
			refreshSelectedActionView();
		}
	}

	private void applyReckoningStack() {
		if (character == null || character.getCombat() == null || character.getAttributes() == null) return;

		int stackNumber = countReckoningStacks() + 1;
		String statusName = RECKONING_STATUS_NAME_PREFIX + stackNumber;

		DataStatus applied = new DataStatus();
		applied.setName(statusName);
		applied.setAttribute("BTDMG");
		applied.setDurationType("Temporary");
		applied.setSeverity(1.0);
		applied.setAffinity("None");
		applied.setDescription("Reckoning grants +1 total damage until end of combat.");
		character.getAttributes().addStatus(applied);

		DataStatus tracked = new DataStatus(applied);
		tracked.setDurationType("Combat");
		character.getCombat().addStatus(tracked);

		refreshAfterStatusChange();
	}

	private int countReckoningStacks() {
		if (battleStatus == null) return 0;
		int count = 0;
		for (DataStatus status : battleStatus) {
			if (status == null || status.getName() == null) continue;
			if (status.getName().startsWith(RECKONING_STATUS_NAME_PREFIX)) {
				count++;
			}
		}
		return count;
	}

	private void loadNoCombatImage() {
		try {
			File src = AppPaths.imagesDir().resolve("NoCombat.jpg").toFile();
			if (!src.exists()) {
				return;
			}
			BufferedImage img = ImageIO.read(src);
			int availableWidth = getWidth();
			if (availableWidth <= 0) {
				availableWidth = 550; // fallback to default frame width if not yet realized
			}
			int maxW = Math.max(0, availableWidth - 35); // frame width minus 10px border on each side
			int srcW = img.getWidth();
			int srcH = img.getHeight();
			// Stretch to full width; let height grow naturally (no fixed ratio clamp)
			double scale = (double) maxW / srcW;
			int tgtW = (int)Math.round(srcW * scale);
			int tgtH = 400;
			Image scaled = img.getScaledInstance(tgtW, tgtH, Image.SCALE_SMOOTH);
			noCombatImage.setIcon(new ImageIcon(scaled));
			noCombatImage.setBounds(10, 10, tgtW, tgtH);
		} catch (Exception e) {
			System.err.println("Failed to load NoCombat.jpg: " + e.getMessage());
		}
	}

	private void showNoCombatImage(boolean show) {
		if (show) {
			loadNoCombatImage(); // rescale to current frame width
			refreshPopupCheckboxFromCombat();
		}
		noCombatImage.setVisible(show);
		if (popupCheckBox != null) {
			popupCheckBox.setVisible(show);
		}
	}

	private boolean resolvePopupDefault() {
		if (character != null && character.getCombat() != null) {
			return character.getCombat().isShowInitiativePopup();
		}
		return true;
	}

	private void refreshPopupCheckboxFromCombat() {
		if (popupCheckBox != null) {
			popupCheckBox.setSelected(resolvePopupDefault());
		}
	}

	private void syncPopupStateToCombat() {
		if (character != null && character.getCombat() != null && popupCheckBox != null) {
			character.getCombat().setShowInitiativePopup(popupCheckBox.isSelected());
		}
	}

	/** Updates header to reflect combat state and whose turn it is. */
	private void updateTurnHeader(boolean isMyTurn) {
		headerL.setText(isMyTurn ? "Combat: Your turn" : "Combat: Waiting (not your turn)");
		headerL.setVisible(true);
	}

	/** Updates the round header from the character's combat state. */
	private void updateRoundHeader() {
		if (character != null && character.getCombat() != null && character.getCombat().isInCombat()) {
			roundHeaderL.setText("Round: " + character.getCombat().getRound());
			roundHeaderL.setVisible(true);
		} else {
			roundHeaderL.setVisible(false);
		}
	}

	private void populateWeaponSelect() {
		Object previous = weaponSelect.getSelectedItem();
		weaponSelect.removeAllItems();
		weaponOptions.clear();
		if (character == null || character.getInventory() == null) {
			weaponSelect.addItem("No weapons equipped");
			updateStandardAttackRange();
			return;
		}
		List<DataItemWeapon> eq = character.getInventory().getWeapons();
		boolean added = false;
		for (DataItemWeapon item : eq) {
			if (item == null) continue;
			if (item.isEquipped()) {
				String iname = item.getIname();
				String name = (iname != null && !iname.isBlank() && iname.compareTo("-") != 0) ? iname : item.getDname();
				weaponSelect.addItem(name != null ? name : "Weapon");
				weaponOptions.add(item);
				added = true;
			}
		}
		if (hasUnarmedProwess()) {
			DataItemWeapon unarmedOption = buildCombatUnarmedWeapon();
			if (unarmedOption != null) {
				weaponSelect.addItem("Unarmed");
				weaponOptions.add(unarmedOption);
				added = true;
			}
		}
		if (!added) {
			weaponSelect.addItem("No weapons equipped");
			weaponOptions.clear();
		}
		// restore previous selection if still present
		if (previous != null) {
			for (int i = 0; i < weaponSelect.getItemCount(); i++) {
				if (previous.equals(weaponSelect.getItemAt(i))) {
					weaponSelect.setSelectedIndex(i);
					break;
				}
			}
		}
		updateStandardAttackRange();
	}

	private boolean hasUnarmedProwess() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(UNARMED_PROWESS_SPECIALTY);
	}

	private DataItemWeapon buildCombatUnarmedWeapon() {
		if (character == null) return null;
		int targetTier = Math.max(0, Math.min(10, character.getLevel() / 2));
		StoreRuleManager ruleManager = new StoreRuleManager();
		for (DataItemWeapon weapon : ruleManager.getItemWeaponData()) {
			if (weapon == null) continue;
			if (!"Unarmed".equalsIgnoreCase(weapon.getDname())) continue;
			if (weapon.getTier() != targetTier) continue;
			DataItemWeapon copy = new DataItemWeapon(weapon);
			copy.setEquipped(true);
			return copy;
		}
		return null;
	}

	private void copyInitiativeToClipboard() {
		copyInitiativeToClipboard(true);
	}

	private void copyInitiativeToClipboard(boolean showDialog) {
		if (character == null) return;
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
		tempString += "Initiative Check" + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em  --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		int initMod = 0;
		if (character.getAttributes() != null) {
			//initMod = character.getAttributes().getCombat("INIT");
		}
		tempString += " --=SkillCheck|1d20+" + initMod + " --+| [$SkillCheck] = [$SkillCheck.Base] + " + initMod;
		tempString += " --=InitTotal| [$SkillCheck] + @{tracker|" + charName + "} &{noerror} --+|Total: --+| [$InitTotal] = [$SkillCheck] +  @{tracker|" + charName + "} &{noerror} --~|turnorder;replacetoken;@{selected|token_id};[$InitTotal]}}";
		
		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);

		if (showDialog) {
			JOptionPane.showMessageDialog(this, "<html><center>A new round has begun.<br>The initiative command has been copied to your clipboard.<br>Select your token and paste the initiative command into Roll20 to update your initiative total.</center></html>");
		}
	}

	private void updateStandardAttackRange() {
		if (character == null) return;
		boolean melee = isSelectedWeaponMelee();
		int charRange = 0;
		if (character.getAttributes() != null) {
			//charRange = character.getAttributes().getCombat("RANGE");
		}
		int newRange = melee ? 0 : charRange;
		if (character.getCombat() != null) {
			character.getCombat().updateStandardAttackRange(newRange);
		}
		// Update any Standard Attack already in the local list
		for (DataAction action : actionList) {
			if (action != null && "Standard Attack".equalsIgnoreCase(action.getName())) {
				action.setRanged(newRange);
			}
		}
	}

	private boolean isSelectedWeaponMelee() {
		int idx = weaponSelect.getSelectedIndex();
		if (idx < 0 || idx >= weaponOptions.size()) return true; // default to melee if none
		DataItemWeapon w = weaponOptions.get(idx);
		if (w == null) return true;
		String category = w.getCategory() == null ? "" : w.getCategory().toLowerCase();
		String type = w.getType() == null ? "" : w.getType().toLowerCase();
		String slot = w.getSlot() == null ? "" : w.getSlot().toLowerCase();
		// If any hint of melee in category/type/slot, treat as melee
		if (category.contains("melee") || type.contains("melee") || slot.contains("melee")) return true;
		// Otherwise consider ranged
		return false;
	}

	private boolean matchesActionCategory(DataAction action, String selectedCategory) {
		if (action == null || selectedCategory == null || selectedCategory.isBlank()) return true;
		return hasActionCategory(action, selectedCategory);
	}

	private boolean hasActionCategory(DataAction action, String category) {
		if (action == null || category == null || category.isBlank()) return false;
		String actionCategory = action.getCategory();
		if (actionCategory == null || actionCategory.isBlank()) return false;
		return actionCategory.trim().equalsIgnoreCase(category.trim());
	}

	private String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) <= 0.0001) {
			return Integer.toString((int)Math.round(value));
		}
		return Double.toString(Math.round(value * 100.0) / 100.0);
	}

	private void showDomainEmanationStartTurnPopupIfNeeded() {
		if (!shouldShowDomainEmanationStartTurnPopup()) return;

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		JLabel message = new JLabel("Domain Emanation: " + trimNumber(calculateDomainEmanationValue()) + "ft", JLabel.CENTER);
		panel.add(message, BorderLayout.CENTER);

		JOptionPane optionPane = new JOptionPane(
				panel,
				JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,
				new Object[] {},
				null);

		JDialog dialog = optionPane.createDialog(this, "Domain Emanation");
		JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		JButton copyButton = new JButton("Copy Macro");
		copyButton.addActionListener(e -> copyDomainEmanationMacroToClipboard());
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(e -> dialog.dispose());
		buttonPanel.add(copyButton);
		buttonPanel.add(confirmButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.setModal(true);
		dialog.setVisible(true);
	}

	private boolean shouldShowDomainEmanationStartTurnPopup() {
		return character != null
				&& character.hasPaladinDomainEmanationAccess()
				&& character.isDomainEmanationEnabled();
	}

	private double calculateDomainEmanationValue() {
		if (character == null || character.getAttributes() == null) return 0.0;
		double range = Math.max(0.0, character.getAttributes().calcStatusValue("RANGE"));
		double area = Math.max(0.0, character.getAttributes().calcStatusValue("AREA"));
		return 5 + Math.sqrt(range) * area;
	}

	private void copyDomainEmanationMacroToClipboard() {
		StringSelection stringSelection = new StringSelection(character == null ? "" : character.buildDomainStatusMacro());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private static final class ActionRequirements {
		private int standardActions;
		private int moveActions;
		private int auraActions;
		private double aura;
		private int reactions;
		private int resource1;
		private int resource2;
		private int resource3;
	}

	private boolean matchesActionSource(DataAction action, String selectedSource) {
		if (action == null || selectedSource == null || selectedSource.isBlank()
				|| "All".equalsIgnoreCase(selectedSource)) {
			return true;
		}
		String actionSource = action.getSource();
		if (actionSource == null || actionSource.isBlank()) return false;
		return actionSource.equalsIgnoreCase(selectedSource);
	}

	private boolean isStandardMoveAction(DataAction action) {
		if (action == null) return false;
		if (!"Move".equalsIgnoreCase(action.getName())) return false;
		if (!"Standard".equalsIgnoreCase(action.getSource())) return false;
		return "Move".equalsIgnoreCase(action.getActionType());
	}

	private int getDerivedCombatValue(String key) {
		if (character == null || character.getAttributes() == null || key == null) {
			return 0;
		}
		double value = character.getAttributes().calcStatusValue(key);
		return (int) Math.round(Math.max(0.0, value));
	}

	private void refreshSelectedActionView() {
		if (stdButton != null && stdButton.isSelected()) {
			updateStdAct();
			return;
		}
		if (moveButton != null && moveButton.isSelected()) {
			updateMoveAct();
			return;
		}
		if (auraButton != null && auraButton.isSelected()) {
			updateAuraAct();
			return;
		}
		if (freeButton != null && freeButton.isSelected()) {
			updateFreeAct();
			return;
		}
		if (intButton != null && intButton.isSelected()) {
			updateIntAct();
		}
	}

	private void layoutResourceRow() {
		int[] xPositions = { LEFT_COLUMN_X, LEFT_COLUMN_X + 125, LEFT_COLUMN_X + 250, LEFT_COLUMN_X + 375 };
		for (int i = 0; i < resourceLabels.length && i < xPositions.length; i++) {
			resourceLabels[i].setBounds(xPositions[i], RESOURCE_LABEL_Y, RESOURCE_CELL_WIDTH, 18);
			resourceValues[i].setBounds(xPositions[i] + 10, RESOURCE_VALUE_Y, RESOURCE_VALUE_WIDTH, 18);
		}
	}

	private void refreshResourceRow() {
		if (character == null || character.getResources() == null
				|| character.getCombat() == null || !character.getCombat().isInCombat()) {
			setResourceRowVisible(false);
			return;
		}

		CharResources resources = character.getResources();
		String resource1Name = findResourceSpecialtyName(1);
		String resource2Name = character.hasAngelPoints() ? ANGEL_POINTS_LABEL : findResourceSpecialtyName(2);
		String resource3Name = findResourceSpecialtyName(3);
		setResourceCell(0, resource1Name, resources.calcCurrentResource1(), resources.calcMaxResource1(), resource1Name != null);
		setResourceCell(1, resource2Name,
				character.hasAngelPoints() ? resources.calcCurrentAngelPoints() : resources.calcCurrentResource2(),
				character.hasAngelPoints() ? resources.calcMaxAngelPoints() : resources.calcMaxResource2(),
				resource2Name != null);
		setResourceCell(2, resource3Name, resources.calcCurrentResource3(), resources.calcMaxResource3(), resource3Name != null);
		setResourceCell(3, "Reactions", resources.calcCurrentReactions(), resources.calcMaxReactions(), true);
	}

	private void setResourceCell(int index, String label, int current, int max, boolean visible) {
		if (index < 0 || index >= resourceLabels.length) return;
		resourceLabels[index].setText(label);
		resourceValues[index].setText(Math.max(0, current) + "/" + Math.max(0, max));
		resourceLabels[index].setVisible(visible);
		resourceValues[index].setVisible(visible);
	}

	private void setResourceRowVisible(boolean visible) {
		for (int i = 0; i < resourceLabels.length; i++) {
			resourceLabels[i].setVisible(visible && resourceLabels[i].isVisible());
			resourceValues[i].setVisible(visible && resourceValues[i].isVisible());
		}
		if (!visible) {
			for (JLabel resourceLabel : resourceLabels) {
				resourceLabel.setVisible(false);
			}
			for (JLabel resourceValue : resourceValues) {
				resourceValue.setVisible(false);
			}
		}
	}

	private String findResourceSpecialtyName(int resourceNumber) {
		if (resourceNumber < 1 || resourceNumber > 3) return null;
		if (character == null || character.getSpecials() == null) return null;
		String expectedType = "resource" + resourceNumber;
		StoreRuleManager ruleManager = new StoreRuleManager();
		for (DataSpecialty specialty : character.getSpecials().getAllSpecialties()) {
			if (specialty == null) continue;
			DataSpecialty resolved = specialty;
			if (specialty.getId() > 0) {
				DataSpecialty byId = ruleManager.getSpecialtyById(specialty.getId());
				if (byId != null) {
					resolved = byId;
				}
			}
			String type = resolved.getType();
			if (type != null && expectedType.equalsIgnoreCase(type.trim())) {
				String name = specialty.getName();
				if (name == null || name.isBlank()) {
					name = resolved.getName();
				}
				return (name == null || name.isBlank()) ? null : name.trim();
			}
		}
		return null;
	}

	public void disposeOwnedWindows() {
		if (statusFrame != null) {
			statusFrame.dispose();
			statusFrame = null;
		}
	}

	@Override
	public void dispose() {
		if (roll20Server != null && roll20ConnectionListener != null) {
			roll20Server.removeConnectionListener(roll20ConnectionListener);
			roll20ConnectionListener = null;
		}
		super.dispose();
	}

	private void registerRoll20ConnectionListener() {
		if (!playerMode || roll20Server == null || roll20ConnectionListener != null) {
			return;
		}
		roll20ConnectionListener = this::showRoll20ConnectedPopup;
		roll20Server.addConnectionListener(roll20ConnectionListener);
	}

	private void showRoll20ConnectedPopup() {
		if (!playerMode || roll20ConnectedPopupShown) {
			return;
		}
		roll20ConnectedPopupShown = true;
		JOptionPane.showMessageDialog(this, "Roll20 Service is Connected.", "Player Mode", JOptionPane.INFORMATION_MESSAGE);
	}

}
