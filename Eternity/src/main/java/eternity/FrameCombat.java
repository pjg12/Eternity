package eternity;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import eternity.DataItemEquipment;

/*
 * Combat Helper
 */
public class FrameCombat extends JFrame {
	private static final long serialVersionUID = 1;
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
	private JComboBox<String> weaponSelect;
	private ArrayList<DataItemEquipment> weaponOptions = new ArrayList<>();
	
	private ArrayList<JButton> actionButtons;
	private ArrayList<DataAction> actionList;
	
	private final String[] ICONS = {"stdAct", "moveAct", "auraAct", "damage", "round", "freeAct", "intAct", "stdDemo", "moveDemo"};
	private final String[] STDACTCAT = {"Standard", "Combat Maneuver", "Class", "Aura", "Specialty", "Item"};
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
		optionPanel.setPreferredSize(new Dimension(580, 130));
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
		stdActCat.setBounds(0, 0, 200, 20);
		optionPanel.add(stdActCat);
		weaponSelect = new JComboBox<String>();
		weaponSelect.setBounds(210, 0, 200, 20);
		weaponSelect.setVisible(false); // only shown when Standard actions are active
		optionPanel.add(weaponSelect);
		
		actionButtons = new ArrayList<JButton>();
		actionList = new ArrayList<DataAction>();
		
		loadNoCombatImage();
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
	}
	
	public void updateCharacter(CharData character) {
		this.character = character;
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
		populateWeaponSelect();
		updateStandardAttackRange();
		weaponSelect.setVisible(true);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateStdAct();
	}
	
	public void moveOptions() {
		stdActCat.setVisible(false);
		weaponSelect.setVisible(false);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateMoveAct();
	}
	
	public void auraOptions() {
		stdActCat.setVisible(false);
		weaponSelect.setVisible(false);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateAuraAct();
	}
	
	public void freeOptions() {
		stdActCat.setVisible(false);
		weaponSelect.setVisible(false);
		stdDemoCheck = false;
		moveDemoCheck = false;
		
		updateFreeAct();
	}
	
	public void intOptions() {
		stdActCat.setVisible(false);
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
		boolean isStandard = tempString != null && tempString.equalsIgnoreCase("Standard");

		for (int i = actionButtons.size()-1; i >= 0 ; i--) {
			ActionListener[] listens = actionButtons.get(i).getActionListeners();
			while (listens.length > 0) {
				actionButtons.get(i).removeActionListener(listens[0]);
				listens = actionButtons.get(i).getActionListeners();
			}
			actionButtons.get(i).setVisible(false);
			actionButtons.remove(i);
			actionList.remove(i);
		} //End of Buttons Loop
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getStandardActions();
		ArrayList<DataAction> filtered = new ArrayList<>();
		for (DataAction action : tempActions) {
			if (action.getType() != null && action.getType().equalsIgnoreCase(tempString)) {
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
				fullAttack.setType("Standard");
				fullAttack.setAffinity("None");
				fullAttack.setActionType("Standard");
				int range = isSelectedWeaponMelee() ? 0 : character.getAttributes().getCombat("RANGE");
				fullAttack.setRanged(range);
				fullAttack.setCharacter(character);
				filtered.add(0, fullAttack);
			}
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
			JButton tempButton = buildButton();
			tempButton.setText(filtered.get(i).getName());
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20 + 50*i, 500, 50);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickStdAction(e));
			actionList.add(filtered.get(i));
		}
	}	
	
	void updateMoveAct() {
		for (int i = actionButtons.size()-1; i >= 0 ; i--) {
			ActionListener[] listens = actionButtons.get(i).getActionListeners();
			while (listens.length > 0) {
				actionButtons.get(i).removeActionListener(listens[0]);
				listens = actionButtons.get(i).getActionListeners();
			}
			actionButtons.get(i).setVisible(false);
			actionButtons.remove(i);
			actionList.remove(i);
		} //End of Buttons Loop
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getMoveActions();
		
		for (int i = 0; i < tempActions.size(); i++) {
			JButton tempButton = buildButton();
			tempButton.setText(tempActions.get(i).getName());
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20 + 50*i, 500, 50);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickMoveAction(e));
			actionList.add(tempActions.get(i));
		}
	}
	
	void updateAuraAct() {
		for (int i = actionButtons.size()-1; i >= 0 ; i--) {
			ActionListener[] listens = actionButtons.get(i).getActionListeners();
			while (listens.length > 0) {
				actionButtons.get(i).removeActionListener(listens[0]);
				listens = actionButtons.get(i).getActionListeners();
			}
			actionButtons.get(i).setVisible(false);
			actionButtons.remove(i);
			actionList.remove(i);
		} //End of Buttons Loop
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getAuraActions();
		
		for (int i = 0; i < tempActions.size(); i++) {
			JButton tempButton = buildButton();
			tempButton.setText(tempActions.get(i).getName());
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20 + 50*i, 500, 50);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickAuraAction(e));
			actionList.add(tempActions.get(i));
		}
	}
	
	void updateFreeAct() {
		for (int i = actionButtons.size()-1; i >= 0 ; i--) {
			ActionListener[] listens = actionButtons.get(i).getActionListeners();
			while (listens.length > 0) {
				actionButtons.get(i).removeActionListener(listens[0]);
				listens = actionButtons.get(i).getActionListeners();
			}
			actionButtons.get(i).setVisible(false);
			actionButtons.remove(i);
			actionList.remove(i);
		} //End of Buttons Loop
		
		if (character == null) return;
		List<DataAction> tempActions = character.getCombat().getFreeActions();
		
		for (int i = 0; i < tempActions.size(); i++) {
			JButton tempButton = buildButton();
			tempButton.setText(tempActions.get(i).getName());
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20 + 50*i, 500, 50);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickFreeAction(e));
			actionList.add(tempActions.get(i));
		}
	}
	
	void updateIntAct() {
		for (int i = actionButtons.size()-1; i >= 0 ; i--) {
			ActionListener[] listens = actionButtons.get(i).getActionListeners();
			while (listens.length > 0) {
				actionButtons.get(i).removeActionListener(listens[0]);
				listens = actionButtons.get(i).getActionListeners();
			}
			actionButtons.get(i).setVisible(false);
			actionButtons.remove(i);
			if (i < actionList.size()) {
				actionList.remove(i);
			}
		} //End of Buttons Loop
		
		if (character == null || character.getCombat() == null) {
			JButton tempButton = buildButton();
			tempButton.setText("No actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20, 500, 50);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			return;
		}

		List<DataAction> tempActions = character.getCombat().getInterruptActions();
		if (tempActions == null || tempActions.isEmpty()) {
			JButton tempButton = buildButton();
			tempButton.setText("No actions available");
			tempButton.setEnabled(false);
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20, 500, 50);
			actionButtons.add(tempButton);
			actionList.add(null);
			tempButton.setVisible(true);
			return;
		}

		for (int i = 0; i < tempActions.size(); i++) {
			JButton tempButton = buildButton();
			tempButton.setText(tempActions.get(i).getName());
			optionPanel.add(tempButton);
			tempButton.setBounds(0, 20 + 50*i, 500, 50);
			actionButtons.add(tempButton);
			tempButton.setVisible(true);
			tempButton.addActionListener(e -> pickIntAction(e));
			actionList.add(tempActions.get(i));
		}
	}
	
	void pickStdAction(ActionEvent e) {
		if (stdActCount < 1) {
			JOptionPane.showMessageDialog(this, "You are out of standard Actions.");
		}
		else {
			for (int i = 0; i < actionButtons.size(); i ++) {
				if (e.getSource() == actionButtons.get(i)) {
					FrameStdAction stdActionFrame = new FrameStdAction(sheetFrame, character, actionList.get(i));
				
					stdActionFrame.setVisible(true);
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
			auraActCount--;
		}
	}
	
	void pickFreeAction(ActionEvent e) {
		
	}
	
	void pickIntAction(ActionEvent e) {
		for (int i = 0; i < actionButtons.size(); i ++) {
			if (e.getSource() == actionButtons.get(i)) {
				FrameStdAction stdActionFrame = new FrameStdAction(sheetFrame, character, actionList.get(i));
				
				stdActionFrame.setVisible(true);
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

	private JButton buildButton() {
		JButton btn = new JButton();
		btn.setFocusPainted(false);
		return btn;
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

}
