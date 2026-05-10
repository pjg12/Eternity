package eternity;

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
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.UIManager;
import eternity.DataItemEquipment;

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
	private final FrameSheet sheetFrame;
	private CharData character;
	private int stdActCount, moveActCount, auraActCount;
	private boolean stdDemoCheck, moveDemoCheck;
	
	private ImageIcon[] buttonPic, buttonPic2;
	private JRadioButton stdButton, moveButton, auraButton, freeButton, intButton;
	private JButton stdDemoButton, moveDemoButton;
	private JButton statusButton, endRoundButton, damageButton;
	private ButtonGroup buttonGroup;
	
	private JPanel optionPanel;
	private JScrollPane optionPane;
	private ArrayList<JButton> optionButtons;
	private ArrayList<DataStatus> battleStatus;
	private JComboBox<String> stdActCat;
	private JComboBox<String> stdAbilityCat;
	private JComboBox<String> moveActCat;
	private JComboBox<String> moveAbilityCat;
	private JComboBox<String> auraActCat;
	private JComboBox<String> auraAbilityCat;
	private JComboBox<String> weaponSelect;
	private ArrayList<DataItemEquipment> weaponOptions = new ArrayList<>();
	
	private ArrayList<JButton> actionButtons;
	private ArrayList<DataAction> actionList;
	
	private final String[] ICONS = {"stdAct", "moveAct", "auraAct", "damage", "round", "freeAct", "intAct", "stdDemo", "moveDemo"};
	private final String[] STDACTCAT = {"All", "Standard", "Combat Maneuver", "Class", "Aura", "Specialty", "Item"};
	private final String[] MOVEACTCAT = {"All", "Move"};
	private final String[] STDABILITYCAT = {"All", "Attack", "Heal", "Next Attack", "Mitigation", "Remedy", "Other"};
	private final String[] AURAACTCAT = {"All", "Aura"};
	private final String[] DMGTYPE = {"PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME"};
	
	private final JLabel noCombatImage;

	private final JLabel headerL;
	private final JLabel roundHeaderL;
	private final JLabel[] labels;
	private final JButton[] buttons;
	private final JCheckBox popupCheckBox;

	/*
	 * DEFAULT CONSTRUCTOR
	 */
	FrameCombat (FrameSheet sheetFrame, CharData character2) {
		super("Combat Helper");
		this.sheetFrame = sheetFrame;
		this.character = character2;
		setLayout(null); // retains existing absolute positioning logic
		setSize(550, 500);
		setLocationRelativeTo(null);
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

		noCombatImage = new JLabel();
		noCombatImage.setVisible(false);
		add(noCombatImage);

		labels = new JLabel[3];
		for (int j = 0; j < labels.length; j++) {
			labels[j] = new JLabel();
			labels[j].setVisible(false);
			add(labels[j]);
		}

		buttons = new JButton[3];
		for (int j = 0; j < buttons.length; j++) {
			buttons[j] = new JButton();
			buttons[j].setVisible(false);
			add(buttons[j]);
		}
		
		popupCheckBox = new JCheckBox("Popup");
		popupCheckBox.setSelected(resolvePopupDefault()); // default pulled from combat if available
		popupCheckBox.setBounds(25, 425, 100, 20); // align with bottom buttons
		popupCheckBox.setVisible(false);
		popupCheckBox.addActionListener(e -> syncPopupStateToCombat());
		add(popupCheckBox);
		setSize(550, 500);
		
		stdActCount = 1;
		moveActCount = 1;
		auraActCount = 1;
		stdDemoCheck = false;
		moveDemoCheck = false;
		
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
		stdButton.setBounds(35, 60, 100, 100);
		stdButton.addActionListener(e -> stdOptions());
		stdButton.setToolTipText("Standard Action");
		i++;
		
		moveButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		moveButton.setBounds(215 , 60, 100, 100);
		moveButton.addActionListener(e -> moveOptions());
		moveButton.setToolTipText("Move Action");
		i++;
		
		auraButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		auraButton.setBounds(395 , 60, 100, 100);
		auraButton.addActionListener(e -> auraOptions());
		auraButton.setToolTipText("Aura Action");
		i++;
		
		damageButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		damageButton.setBounds(35, 60, 100, 100);
		damageButton.addActionListener(e -> underAttack());
		damageButton.setToolTipText("Incoming Attack");
		i++;
		
		endRoundButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		endRoundButton.setBounds(395 , 60, 100, 100);
		endRoundButton.addActionListener(e -> endRound());
		endRoundButton.setToolTipText("End of Round");
		i++;
		
		freeButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		freeButton.setBounds(150, 120, 50, 50);
		freeButton.addActionListener(e -> freeOptions());
		freeButton.setToolTipText("Free Action");
		i++;
		
		intButton = buildRadioButton(buttonPic[i], buttonPic2[i]);
		intButton.setBounds(330, 120, 50, 50);
		intButton.addActionListener(e -> intOptions());
		intButton.setToolTipText("Interrupt Action");
		i++;		
		
		stdDemoButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		stdDemoButton.setBounds(150, 50, 50, 50);
		stdDemoButton.addActionListener(e -> stdDemoClick());
		i++;	
		
		moveDemoButton = buildDemoButton(buttonPic[i], buttonPic2[i]);
		moveDemoButton.setBounds(330, 50, 50, 50);
		moveDemoButton.addActionListener(e -> moveDemoClick());		
		
		labels[0].setBounds(35, 160, 100, 20);
		labels[1].setBounds(215, 160, 100, 20);
		labels[2].setBounds(395, 160, 100, 20);
		
		optionPanel = new JPanel();
		optionPanel.setVisible(true);
		optionPane = buildScrollPane(optionPanel);
		add(optionPane);
		optionButtons = new ArrayList<JButton>();
		optionPanel.setLayout(null);
		optionPanel.setPreferredSize(new Dimension(OPTION_PANEL_WIDTH, OPTION_PANEL_MIN_HEIGHT));
		optionPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		optionPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		optionPane.setBounds(25, 190, 500, 220);
		optionPane.getVerticalScrollBar().setUnitIncrement(15);	//set mouse wheel scroll amount
		
		buttons[0].setBounds(25, 425, 145, 20);
		buttons[0].setText("Status");
		//buttons[0].addActionListener(e -> statusChange());
		buttons[1].setBounds(195, 425, 145, 20);
		buttons[2].setBounds(365, 425, 145, 20);
		
		battleStatus = new ArrayList<DataStatus>();
		stdActCat = new JComboBox<String>(STDACTCAT);
		stdActCat.addActionListener(e -> updateStdAct()); // keep list in sync with category selection
		stdActCat.setBounds(0, 0, 160, 20);
		optionPanel.add(stdActCat);
		stdAbilityCat = new JComboBox<String>(STDABILITYCAT);
		stdAbilityCat.addActionListener(e -> updateStdAct());
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
		weaponSelect.setVisible(false); // only shown when Standard actions are active
		optionPanel.add(weaponSelect);
		
		actionButtons = new ArrayList<JButton>();
		actionList = new ArrayList<DataAction>();
		
		loadNoCombatImage();
		if (character != null && character.getCombat() != null) {
			character.getCombat().rebuildActions(character);
		}
		populateWeaponSelect();
		updateStandardAttackRange();

		// Open in the correct state based on current combat flag
		if (character != null && character.getCombat() != null && character.getCombat().isInCombat()) {
			notMyTurn();
		} else {
			nonCombat();
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
		
		optionPane.setVisible(false);
		
		for (int i = buttons[2].getActionListeners().length - 1; i >= 0; i--) {
			buttons[2].removeActionListener(buttons[2].getActionListeners()[i]);
		}
		
		stdActCat.setVisible(false);
		stdAbilityCat.setVisible(false);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
	}
	
	public void updateCharacter(CharData character) {
		this.character = character;
		if (this.character != null && this.character.getCombat() != null) {
			this.character.getCombat().rebuildActions(this.character);
		}
		populateWeaponSelect();
		updateStandardAttackRange();
		refreshPopupCheckboxFromCombat();
	}
	
	public void notMyTurn() {
		clearCombat();
		showNoCombatImage(false);
		
		for (int i = 0; i < battleStatus.size(); i++) {
			if (battleStatus.get(i).getDurationType().compareTo("Turn") <= 0) {
				battleStatus.get(i).setDuration(battleStatus.get(i).getDuration() - 1);
				if (battleStatus.get(i).getDuration() == 0) {
					//character.removeStatus(battleStatus.get(i));
					battleStatus.remove(i);
				}
			}
		}
		
		/*
		 * Set Headers
		*/
		updateTurnHeader(false);
		updateRoundHeader();
		
		freeButton.setVisible(false);
		intButton.setVisible(true);
		optionPane.setVisible(true);
		endRoundButton.setVisible(true);
		damageButton.setVisible(true);
		buttonGroup.clearSelection();
		intButton.setSelected(true);
		intOptions();
		
		buttons[0].setVisible(true);
		
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
		
		optionPane.setVisible(true);
		stdButton.setVisible(true);
		moveButton.setVisible(true);
		auraButton.setVisible(true);
		freeButton.setVisible(true);
		intButton.setVisible(true);
		stdDemoButton.setVisible(true);
		moveDemoButton.setVisible(true);
		buttonGroup.clearSelection();
		stdButton.setSelected(true);
		stdOptions();
		
		buttons[0].setVisible(true);
		
		buttons[1].setText("End My Turn");
		buttons[1].setVisible(true);
		buttons[1].addActionListener(e -> notMyTurn());
		
		buttons[2].setText("End Combat");
		buttons[2].setVisible(true);
		buttons[2].addActionListener(e -> exitCombat());
	}
	
	public void startMyTurn() {
		clearCombat();
		
		for (int i = 0; i < battleStatus.size(); i++) {
			if (battleStatus.get(i).getDurationType().compareTo("Cycle") == 0) {
				battleStatus.get(i).setDuration(battleStatus.get(i).getDuration() - 1);
				if (battleStatus.get(i).getDuration() <= 0) {
					//character.removeStatus(battleStatus.get(i));
					battleStatus.remove(i);
				}
			}
		}

		
		myTurn();
	}
	
	public void nonCombat() {
		clearCombat();
		showNoCombatImage(true);
		
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
	}
	
	public void stdOptions() {
		stdActCat.setVisible(true);
		stdAbilityCat.setVisible(true);
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
		moveAbilityCat.setVisible(true);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		weaponSelect.setVisible(false);
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
		auraAbilityCat.setVisible(true);
		weaponSelect.setVisible(false);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateAuraAct();
	}
	
	public void freeOptions() {
		stdActCat.setVisible(false);
		stdAbilityCat.setVisible(false);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		weaponSelect.setVisible(false);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateFreeAct();
	}
	
	public void intOptions() {
		stdActCat.setVisible(false);
		stdAbilityCat.setVisible(false);
		moveActCat.setVisible(false);
		moveAbilityCat.setVisible(false);
		auraActCat.setVisible(false);
		auraAbilityCat.setVisible(false);
		weaponSelect.setVisible(false);
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
		for (int i = 0; i < battleStatus.size(); i++) {
			if (battleStatus.get(i).getDurationType().compareTo("Round") == 0) {
				battleStatus.get(i).setDuration(battleStatus.get(i).getDuration() - 1);
				if (battleStatus.get(i).getDuration() == 0) {
					//character.removeStatus(battleStatus.get(i));
					battleStatus.remove(i);
				}
			}
		}
		if (character != null && character.getCombat() != null) {
			character.getCombat().nextRound();
			updateRoundHeader();
		}

		copyInitiativeToClipboard();
		
		notMyTurn();
	}

	public void underAttack() {
		FrameCombatDamage dmgFrame = new FrameCombatDamage(sheetFrame, character);
		dmgFrame.setVisible(true);
	}	
	
	public void statusChange() {
		JDialog statusBox = new JDialog();
		statusBox.setSize(550, 500);
		System.out.println("select add, remove, or share status");
		System.out.println("if remove status, pick status to remove");
		System.out.println("remove status and close window");
		System.out.println("if add status, paste status code");
		System.out.println("add status and close window");
		System.out.println("if give status, generate code with button to copy to clipboard.");
		System.out.println("instruct user to paste to discord or roll20, then close when done");
	}	
	
	public void enterCombat() {
		if (character != null && character.getCombat() != null) {
			character.getCombat().startCombat();
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
			if (battleStatus.get(i).getDurationType().compareTo("Round") == 0 || battleStatus.get(i).getDurationType().compareTo("Cycle") == 0 || battleStatus.get(i).getDurationType().compareTo("Turn") == 0) {
				// No backing status container to remove from; just clear the local list for now.
				battleStatus.remove(i);
			}
		}
		if (character != null && character.getCombat() != null) {
			character.getCombat().endCombat();
			if (sheetFrame != null) {
				sheetFrame.refreshImagePanel();
			}
		}
		nonCombat();
	}	
	
	void updateStdAct() {
		String tempString = (String)stdActCat.getSelectedItem();
		String abilityCategory = (String)stdAbilityCat.getSelectedItem();
		boolean showAllTypes = tempString != null && tempString.equalsIgnoreCase("All");
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
			int maxAtk = character.getAttributes().getSecondary("MAXATK");
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
				int range = isSelectedWeaponMelee() ? 0 : character.getAttributes().getCombat("RANGE");
				fullAttack.setRanged(range);
				fullAttack.setCharacter(character);
				filtered.add(0, fullAttack);
			}
		}
		if (abilityCategory != null && !"All".equalsIgnoreCase(abilityCategory)) {
			filtered.removeIf(action -> !matchesActionCategory(action, abilityCategory));
		}

		// Always pin Standard Attack to the top of the standard-action list.
		for (int i = 0; i < filtered.size(); i++) {
			DataAction action = filtered.get(i);
			if (action != null && "Standard Attack".equalsIgnoreCase(action.getName())) {
				if (i != 0) {
					filtered.remove(i);
					filtered.add(0, action);
				}
				break;
			}
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
		
		for (int i = 0; i < tempActions.size(); i++) {
			JButton tempButton = buildButton(tempActions.get(i));
			tempButton.setText(tempActions.get(i).getName());
			if (tempButton instanceof ActionEntryButton) 
				applyActionButtonColor(tempButton, tempActions.get(i));

			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickFreeAction(e));
			actionList.add(tempActions.get(i));
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
		if (tempActions == null || tempActions.isEmpty()) {
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

		for (int i = 0; i < tempActions.size(); i++) {
			JButton tempButton = buildButton(tempActions.get(i));
			tempButton.setText(tempActions.get(i).getName());
			if (tempButton instanceof ActionEntryButton) {
				applyActionButtonColor(tempButton, tempActions.get(i));
			}
			optionPanel.add(tempButton);
			tempButton.setBounds(0, OPTION_HEADER_HEIGHT + OPTION_ROW_HEIGHT * i, 500, OPTION_ROW_HEIGHT);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickIntAction(e));
			actionList.add(tempActions.get(i));
		}
		refreshOptionPanelSize();
	}
	
	void pickStdAction(ActionEvent e) {
		if (stdActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of standard Actions.");
		}
		else {
			for (int i = 0; i < actionButtons.size(); i ++) {
				if (e.getSource() == actionButtons.get(i)) {
					FrameAttack attackFrame = new FrameAttack(sheetFrame, this, character, actionList.get(i));
				
					attackFrame.setVisible(true);
				}
			}
		}
	}
	
	void pickMoveAction(ActionEvent e) {
		if (moveActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of move Actions.");
		}
		else {
			moveActCount--;
		}
	}
	
	void pickAuraAction(ActionEvent e) {
		if (auraActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of aura Actions.");
		}
		else {
			for (int i = 0; i < actionButtons.size(); i ++) {
				if (e.getSource() == actionButtons.get(i) && actionList.get(i) != null) {
					auraActCount--;
					myTurn();
					return;
				}
			}
		}
	}
	
	void pickFreeAction(ActionEvent e) {
		
	}
	
	void pickIntAction(ActionEvent e) {
		for (int i = 0; i < actionButtons.size(); i ++) {
			if (e.getSource() == actionButtons.get(i)) {
				FrameAttack attackFrame = new FrameAttack(sheetFrame, this, character, actionList.get(i));
				
				attackFrame.setVisible(true);
			}
		}
	}
	
	private void buildIcons(ImageIcon[] list, String image, int index) {
		try {
			File src = new File("Images", image + ".png");
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
		if (at.compareTo("Standard")==0) {
			stdActCount--;
		}
		else if (at.compareTo("Aura")==0) {
			auraActCount--;
		}
		myTurn();
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

		DataColor color = CharDataManager.getDataQuery().getColorByTitle(affinity);
		if (color == null) return;

		actionButton.setAffinityColors(color.getBackColor(), color.getForeColor());
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

	private void loadNoCombatImage() {
		try {
			File src = new File("Images", "NoCombat.jpg");
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
		List<DataItemEquipment> eq = character.getInventory().getEquipment();
		boolean added = false;
		for (DataItemEquipment item : eq) {
			if (item == null) continue;
			String slot = item.getSlot() == null ? "" : item.getSlot();
			if (item.isEquipped() && slot.toLowerCase().contains("weapon")) {
				String name = item.getIname() != null && item.getIname().compareTo("-") != 0 ? item.getIname() : item.getDname();
				weaponSelect.addItem(name != null ? name : "Weapon");
				weaponOptions.add(item);
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

	private void copyInitiativeToClipboard() {
		copyInitiativeToClipboard(true);
	}

	private void copyInitiativeToClipboard(boolean showDialog) {
		if (character == null) return;
		// Safe color lookup with sensible fallback
		DataColor raceColor = null;
		DataQuery dataQuery = new DataQuery();
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
			initMod = character.getAttributes().getCombat("INIT");
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
			charRange = character.getAttributes().getCombat("RANGE");
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
		DataItemEquipment w = weaponOptions.get(idx);
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
		String actionCategory = action.getCategory();
		if (actionCategory == null || actionCategory.isBlank()) return false;
		return actionCategory.equalsIgnoreCase(selectedCategory);
	}

}
