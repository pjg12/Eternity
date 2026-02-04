package eternity;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.NumberFormatter;

/*
 * 		BASE FOR ALL CHARACTER PANELS
 */
public class PanelCharBase extends JPanel {
	private static final long serialVersionUID = 1L;
	DataQuery dataQuery;
	CharData character;
	FrameSheet sheetFrame;
	int pageHeight;
	boolean alternate;
	
	//HP, Aura
	private JLabel hpL, auraL, charCurrHPL, charMaxHPL, charAvailAuraL, charOccAuraL, charSpentAuraL, charMaxAuraL;
	private JFormattedTextField charCurrHP, charMaxHP, charAvailAura, charOccAura, charSpentAura, charMaxAura;
	
	//Reminder Bars
	JPanel racePanel;
	JLabel raceRemind;
	
	final String[] ATTRIBUTES = {"Strength", "Dexterity", "Constitution", "Focus", "Control", "Capacity", "Knowledge", "Mechanical", "Perception", "Intuition", "Charisma", "Subtlety"};
	final String[] ATTSHORT = {"STR", "DEX", "CON", "FOC", "CTL", "CAP", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB"};
	final String[] AFFINITIES = {"None", "Reinforcement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Dark", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	final String[] TRAININGTITLE = {"Attribute Training", "Misc Training", "Affinitiy Training", "Fundamental Princples Training", "Standard Technique Training", "Crafting Training", "Reinforcement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Dark", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	final String[] TRAINING = {"Attribute", "Misc", "Affinitiy", "Fundamental", "Standard", "Crafting", "Reinforcement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Dark", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	
	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharBase (DataQuery dataQuery, FrameSheet sheetFrame){
		this.dataQuery = dataQuery;
		this.sheetFrame = sheetFrame;
		alternate = true;
		setLayout(null);
		
		/*
		 * 	HP, Aura
		 */	
			// Labels
		hpL = buildLabel("HP");
		auraL = buildLabel("Aura");
		charCurrHPL = buildLabel("Current HP");
			charCurrHPL.setToolTipText(""); 
		charMaxHPL = buildLabel("Max HP");
			charMaxHPL.setToolTipText(""); 
		charAvailAuraL = buildLabel("Available Aura");
			charAvailAuraL.setToolTipText(""); 
		charOccAuraL = buildLabel("Occupied Aura");
			charOccAuraL.setToolTipText(""); 
		charSpentAuraL = buildLabel("Spent Aura");
			charSpentAuraL.setToolTipText(""); 
		charMaxAuraL = buildLabel("Max Aura");
			charMaxAuraL.setToolTipText("");
			
			// Fields
		charCurrHP = buildNumTextField(0);
			charCurrHP.setToolTipText(""); 
		charMaxHP = buildNumTextField(0);
			charMaxHP.setToolTipText(""); 
		charMaxAura = buildNumTextField(0);
			charMaxAura.setToolTipText(""); 
		charOccAura = buildNumTextField(0);
			charOccAura.setToolTipText(""); 
		charSpentAura = buildNumTextField(0);
			charSpentAura.setToolTipText(""); 
		charAvailAura = buildNumTextField(0);
			charAvailAura.setToolTipText(""); 
		
		/*
		 * Reminders
		 */
		racePanel = new JPanel();
		racePanel.setLayout(null);
		raceRemind = buildLabel("-");
		raceRemind.setBounds(0,0,555,40);
		racePanel.add(raceRemind);
		raceRemind.setVisible(true);
		add(racePanel);
		racePanel.setVisible(true);
		
		/*
		 * Place HP & Aura
		 */
		pageHeight = 5;
		// Top Labels
		hpL.setBounds(5, pageHeight, 175, 20);
		auraL.setBounds(205, pageHeight, 340, 20);
		pageHeight += 20;

			// Labels
		charCurrHPL.setBounds(5, pageHeight, 85, 20);
		charMaxHPL.setBounds(95, pageHeight, 85, 20);
		charAvailAuraL.setBounds(205, pageHeight, 85, 20);
		charOccAuraL.setBounds(295, pageHeight, 85, 20);
		charSpentAuraL.setBounds(385, pageHeight, 85, 20);
		charMaxAuraL.setBounds(475, pageHeight, 85, 20);
		pageHeight += 20;

			// Fields
		charCurrHP.setBounds(5, pageHeight, 85, 20);
		charMaxHP.setBounds(95, pageHeight, 85, 20);
		charAvailAura.setBounds(205, pageHeight, 85, 20);
		charOccAura.setBounds(295, pageHeight, 85, 20);
		charSpentAura.setBounds(385, pageHeight, 85, 20);
		charMaxAura.setBounds(475, pageHeight, 85, 20);
		pageHeight += 25;	
		
		/*
		 * Place Reminder
		 */
		pageHeight += 5;
		racePanel.setBounds(5,pageHeight,555,40);
		pageHeight += 45;
	}  /*--------------
		END DEFAULTCONSTRUCTOR
		--------------*/
	
	/*
	 * 		CHECK PRESSED
	 */
	public void checkPressed(String checkName, boolean skill, String att, String att2) {
		if (character == null || character.getAttributes() == null) return;

		double mod = safeAttribute(character.getAttributes(), att);
		for (String e: ATTSHORT) {
			if (e.equalsIgnoreCase(att)) {
				mod -= 10;
			}
		}

		if (skill) {
			mod *= 1.5;
			mod += (character.getAttributes().getAttribute("INT") * 0.5);
		}
		
		if (att2 != null && att2.compareTo("") != 0) {
			mod += safeAttribute(character.getAttributes(), att2);
		}
		
		// Safe color lookup with sensible fallback
		DataColor raceColor = null;
		if (dataQuery != null && character.getIdentity() != null) {
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
		tempString += checkName + " --#LineHeight|1.5em --#rollHilightLineHeight|1.5em  --#evenRowBackground|" + colorString1 + " --#evenRowFontColor|" + colorString2 + " --#oddRowBackground|" + colorString2 + " --#oddRowFontColor|" + colorString1;
		tempString += " --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;";
		tempString += " --=SkillCheck|1d20+" + mod + " --+| [$SkillCheck] = [$SkillCheck.Base] + " + (int)mod;
		if (att.compareTo("INIT") != 0) tempString += "}}";
		else tempString += " --=InitTotal| [$SkillCheck] + @{tracker|" + charName + "} &{noerror} --+|Total: --+| [$InitTotal] = [$SkillCheck] +  @{tracker|" + charName + "} &{noerror} --~|turnorder;replacetoken;@{selected|token_id};[$InitTotal]}}";
		
		StringSelection stringSelection = new StringSelection(tempString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	/** Returns attribute value or 0 if key is null/unknown to avoid AIOOB. */
	private int safeAttribute(CharAttributes attrs, String key) {
		if (attrs == null || key == null) return 0;
		String upper = key.toUpperCase();
		for (String k : ATTSHORT) {
			if (k.equals(upper)) {
				return attrs.getAttribute(upper);
			}
		}
		return 0;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	* 
	* 		UPDATER
	* 
	*/
	/*
	 * 		UPDATE CHARACTER
	 */
	public void updateCharacter(CharData character) {
		this.character = character;
		updateHPAura();
		updateReminder();
		updateAll();
	}  /*--------------
		END UPDATE CHARACTER
		--------------*/
	
	/*
	 * 		UPDATE HP & AURA
	 */
	public void updateHPAura() {
		if (character == null || character.getResources() == null) return;
		String tempString = "";
		double tempDouble = 0.0;
		double tempDouble2 = 0.0;
		ArrayList<DataStatus> tempList, tempList2;
		
			// Tooltips
		CharResources res = character.getResources();

		tempString = "<html>You have lost " + (int)res.getLostHP() + " HP.<br>You are ";
		tempDouble = res.getCurrentHP() / (double)res.getMaxHP();
		if (tempDouble >= 1) {
			tempString += "not wounded.";
		}
		else if (tempDouble >= 0.9) {
			tempString += "barely wounded.";
		}
		else if (tempDouble >= 0.7) {
			tempString += "lightly wounded.";
		}
		else if (tempDouble >= 0.5) {
			tempString += "moderately wounded.";
		}
		else if (tempDouble >= 0.3) {
			tempString += "heavily wounded.";
		}
		else if (tempDouble >= 0.1) {
			tempString += "severely wounded.";
		}
		else {
			tempString += "critically wounded.";
		}
		charCurrHPL.setToolTipText(tempString);
		charCurrHP.setToolTipText(tempString);
		
		tempString = "<html>Maximum HP: " + res.getMaxHP() + "<br>-------(Base)-------<br>";
		StatBlock[] tempStatuses = null;
		tempStatuses = character.getResources().getMaxHPBlocks();
		if (tempStatuses != null) for (StatBlock sb : tempStatuses) {
			List<DataStatus> statuses = sb.getAllStatuses();
			if (statuses != null && !statuses.isEmpty()) {
				for (DataStatus status : statuses) {
					tempString += "+ " + status.getName() + ": " + status.getSeverity() + "<br>";
				}
			}
			tempString += "--------(Multi)-------<br>";
			statuses = sb.getAllMultipliers();
			if (statuses != null && !statuses.isEmpty()) {
				for (DataStatus status : statuses) {
					if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
					else tempString += "+ " + status.getName() + ": " + status.getSeverity() + "<br>";
				}
			}
		}
		charMaxHPL.setToolTipText(tempString);
		charMaxHP.setToolTipText(tempString);
		
		tempString = "<html>Maximum Aura: " + res.getMaxAura() + "<br>-------(Base)-------<br>";
		tempStatuses = null;
		tempStatuses = character.getResources().getMaxAuraBlocks();
		if (tempStatuses != null) for (StatBlock sb : tempStatuses) {
			List<DataStatus> statuses = sb.getAllStatuses();
			if (statuses != null && !statuses.isEmpty()) {
				for (DataStatus status : statuses) {
					tempString += "+ " + status.getName() + ": " + status.getSeverity() + "<br>";
				}
			}
			tempString += "--------(Multi)-------<br>";
			statuses = sb.getAllMultipliers();
			if (statuses != null && !statuses.isEmpty()) {
				for (DataStatus status : statuses) {
					if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
					else tempString += "+ " + status.getName() + ": " + status.getSeverity() + "<br>";
				}
			}
		}
		charMaxAuraL.setToolTipText(tempString);
		charMaxAura.setToolTipText(tempString);

		tempString = "<html>You have spent " + (int)res.getSpentAura() + " Aura.<br>You are ";
		tempDouble = res.getCurrentAura() / ((double)res.getMaxAura() - res.getOccupiedAura());
		if (tempDouble >= 1) {
			tempString += "not drained.";
		}
		else if (tempDouble >= 0.9) {
			tempString += "barely drained.";
		}
		else if (tempDouble >= 0.7) {
			tempString += "lightly drained.";
		}
		else if (tempDouble >= 0.5) {
			tempString += "moderately drained.";
		}
		else if (tempDouble >= 0.3) {
			tempString += "heavily drained.";
		}
		else if (tempDouble >= 0.1) {
			tempString += "severely drained.";
		}
		else {
			tempString += "critically drained.";
		}
		charSpentAuraL.setToolTipText(tempString);
		charSpentAura.setToolTipText(tempString);
		
		tempString = "<html>Occupied Aura: " + res.getOccupiedAura() + "<br>--------------<br>";
		tempString += "+ Maintained Aura: " + res.getMainOccupiedAura() + "<br>";
		tempString += "+ Grant Aura: " + res.getGrantOccupiedAura() + "<br>";
		charOccAura.setToolTipText(tempString);
		charOccAuraL.setToolTipText(tempString);

		tempString = "<html>You have " + (int)res.getCurrentAura() + " Aura.<br>You are ";
		tempDouble = res.getCurrentAura() / ((double)res.getMaxAura() - res.getOccupiedAura());
		if (tempDouble >= 1) {
			tempString += "not drained.";
		}
		else if (tempDouble >= 0.9) {
			tempString += "barely drained.";
		}
		else if (tempDouble >= 0.7) {
			tempString += "lightly drained.";
		}
		else if (tempDouble >= 0.5) {
			tempString += "moderately drained.";
		}
		else if (tempDouble >= 0.3) {
			tempString += "heavily drained.";
		}
		else if (tempDouble >= 0.1) {
			tempString += "severely drained.";
		}
		else {
			tempString += "critically drained.";
		}
		charAvailAura.setToolTipText(tempString);
		charAvailAuraL.setToolTipText(tempString);
		
			// Text fields
		charCurrHP.setValue(res.getCurrentHP());
		charMaxHP.setValue(res.getMaxHP());
		charMaxAura.setValue(res.getMaxAura());
		charOccAura.setValue(res.getOccupiedAura());
		charSpentAura.setValue(res.getSpentAura());
		charAvailAura.setValue(res.getCurrentAura());
	}  /*--------------
		END UPDATEHPAURA
		--------------*/
	
	/*
	 * 		UPDATE REMINDER
	 */
	public void updateReminder() {
		// Placeholder: no race reminder logic available in current model
		raceRemind.setText("This is where your reminder will go.");
		racePanel.setBackground(Color.DARK_GRAY);
		raceRemind.setForeground(Color.WHITE);
	}  /*--------------
		END UPDATEREMINDER
		--------------*/
	
	/*
	 * 		UPDATE ALL (Placeholder for Override)
	 */
	public void updateAll() {
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	* 
	* 		BUILDER
	* 
	*/
	/*
	 * 		BUILD LABEL
	 */
	public JLabel buildLabel (String tempString) {
		JLabel tempField = new JLabel(tempString);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		add(tempField);
		return tempField;
	}  /*--------------
		END BUILDLABEL
		--------------*/

	/*
	 * 		BUILD TEXTFIELD
	 */
	public JTextField buildTextField (String tempString) {
		JTextField tempField = new JTextField(tempString);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		tempField.setEditable(false);
		if (alternate)
			{tempField.setBackground(Color.WHITE);}
		else 
			{tempField.setBackground(Color.LIGHT_GRAY);}
		add(tempField);
		return tempField;
	}  /*--------------
		END BUILDTEXTFIELD
		--------------*/
	
	/*
	 * 		BUILD NUMTEXTFIELD
	 */
	public JFormattedTextField buildNumTextField (double tempDouble) {
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Double.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);	
	    
		JFormattedTextField tempField = new JFormattedTextField(tempDouble);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		tempField.setEditable(false);
		if (alternate) 
			{tempField.setBackground(Color.WHITE);}
		else 
			{tempField.setBackground(Color.LIGHT_GRAY);}
		add(tempField);
		return tempField;
	}
	public JFormattedTextField buildNumTextField (int tempInt) {
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);		
		
		JFormattedTextField tempField = new JFormattedTextField(tempInt);
		tempField.setHorizontalAlignment(JTextField.CENTER);
		tempField.setEditable(false);
		if (alternate) 
			{tempField.setBackground(Color.WHITE);}
		else 
			{tempField.setBackground(Color.LIGHT_GRAY);}
		add(tempField);
		return tempField;
	}  /*--------------
		END BUILDNUMTEXTFIELD
		--------------*/
	
	/*
	 * 		BUILD TEXTAREA
	 */
	public JTextArea buildTextArea (String tempString) {
		JTextArea tempField = new JTextArea(tempString);
		tempField.setMargin(new Insets (5, 5, 5, 5));
		tempField.setEditable(false);		
		return tempField;
	}  /*--------------
		END BUILDTEXTAREA
		--------------*/
	
	/*
	 * 		BUILD SCROLLPANE
	 */
	public JScrollPane buildScrollPane (JTextArea tempArea) {
		JScrollPane tempPane = new JScrollPane(tempArea);
		tempPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(tempPane);
		return tempPane;
	}  /*--------------
		END BUILDSCROLLPANE
		--------------*/
	
	public JScrollPane buildScrollPane (JTextPane tempArea) {
		JScrollPane tempPane = new JScrollPane(tempArea);
		tempPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(tempPane);
		return tempPane;
	}
	
	/*
	 * 		BUILD COMBOBOX
	 */
	public JComboBox<String> buildComboBox () {
		JComboBox<String> tempBox = new JComboBox<String>();
		tempBox.addItem("*** Empty ***");
		add(tempBox);
		
		return tempBox;
	}  /*--------------
		END BUILDCOMBOBOX
		--------------*/
	
	/*
	 * 		BUILD BUTTON
	 */
	public JButton buildButton (String tempString) {
		JButton tempButton = new JButton(tempString);
		add(tempButton);
		return tempButton;
	}  /*--------------
		END BUILDCOMBOBOX
		--------------*/
	
	/*
	 * 		BUILD CHECK BUTTON
	 */
	public JButton buildCheckButton (String checkName, boolean skill, String att) {
		JButton tempButton = new JButton("Roll");
		tempButton.addActionListener(f -> checkPressed(checkName, skill, att, ""));
		add(tempButton);
		return tempButton;
	}
	public JButton buildCheckButton (String checkName, boolean skill, String att, String att2) {
		JButton tempButton = new JButton("Roll");
		tempButton.addActionListener(f -> checkPressed(checkName, skill, att, att2));
		add(tempButton);
		return tempButton;
	}  /*--------------
		END BUILDCHECKBUTTON
		--------------*/
	
} ///////////////////////////////////////////////END OF CLASS////////////////////////////////////////////////////////////////////////
