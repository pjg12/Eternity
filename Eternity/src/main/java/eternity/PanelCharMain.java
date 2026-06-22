package eternity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * 		MAIN PLAY PANEL
 */
public class PanelCharMain extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	private static final Color MULTI_ATTRIBUTE_COLOR = new Color(0, 0, 255);
	private static final Color RACIAL_SPECIALTY_COLOR = new Color(128, 0, 128);
	private static final Color CLASS_SPECIALTY_COLOR = new Color(0, 128, 0);
	private static final Color TRAINED_SPECIALTY_COLOR = new Color(0, 0, 192);

	//Row 1
	private JLabel charNameL, campNameL, charLevelL, charExpL, charClassL, charRaceL;
	private JTextField charName, campName, charClass, charRace;
	private JFormattedTextField charLevel, charExp;
	
	//Row 2
	private JLabel charGenderL, charSizeL, charAgeL, charHeightL, charWeightL, charEyesL, charHairL;
	private JTextField charGender, charSize;
	private JFormattedTextField charAge;
	private JTextField charHeight, charWeight, charEyes, charHair;
	
	//Row 3
	private JLabel charPhysicalL, charPersonalityL;
	private JTextArea charPhysical, charPersonality;
	private	JScrollPane physicalPane, personalityPane;
	
	//Buttons
	private JButton addExpButton, editCharacterButton, restButton, statusButton;
	
	//Attributes
	private JLabel coreAttL, coreValueL, coreModL, coreRollL, charAttL, charValueL, charModL, charRollL;
	private JTextField strAttL, dexAttL, conAttL, focAttL, capAttL, ctlAttL, knowAttL, mechAttL, percAttL, intAttL, chaAttL, subAttL;
	private JFormattedTextField charStr, charDex, charCon, charFoc, charCap, charCtl, charKnow, charMech, charPerc, charInt, charCha, charSub;
	private JFormattedTextField charStrMod, charDexMod, charConMod, charFocMod, charCapMod, charCtlMod, charKnowMod, charMechMod, charPercMod, charIntMod, charChaMod, charSubMod;
	private JButton strRoll, dexRoll, conRoll, focRoll, capRoll, ctlRoll, knowRoll, mechRoll, percRoll, intRoll, chaRoll, subRoll;
	
	//Defenses & Statistics
	private JLabel defenseL, utilityL, miscL;
	private JLabel defNameL, defModL, defRollL, utilNameL, utilModL, utilRollL, miscNameL, miscModL, miscRollL;
	private JTextField acStatL, armorStatL, dodgeStatL, fortStatL, refStatL, willStatL;
	private JFormattedTextField charAC, charArmor, charDodge, charFort, charRef, charWill;
	private JButton fortRoll, refRoll, willRoll, initRoll;
	private JTextField resistRoll;
	private JTextField attackRoll;
	private JTextField moveRoll;
	private JTextField critRoll;
	private JTextField areaRoll;
	private JTextField bdmgRoll;
	private JTextField bhealRoll;
	private JTextField supRoll;
	private static final String[] RESIST_KEYS = { "ALL", "PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME" };
	private JTextField attackStatL, dcStatL, moveStatL, flyStatL, rangeStatL, initStatL, powerStatL;
	private JFormattedTextField charAttack, charDC, charMove, charFly, charRange, charInit, charPower;
	private JTextField resistStatL, supStatL, dbStatL, grantStatL, exclStatL, maxattStatL, bdmgStatL, bhealStatL;
	private JFormattedTextField charResist, charSup, charDb, charGrant, charExcl, charMaxatt, charBdmg, charBheal;
	
	//Skills / Specialties
	private JLabel skillsL, specialtiesL, skillsAttL, skillsNameL, skillsRollL, specialtiesNameL;
	private ArrayList<JTextField> skillsAtt, skillsName, specialtiesName;
	private ArrayList<JButton> skillsRoll;
	private String cachedResistTooltipSignature = "";
	private String cachedResistTooltip = "Resists: unknown";
	
	/*
	 * 		DEFAULT CONSTRUCTOR
	 */
	PanelCharMain (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(244, 222, 222));
		
		charNameL = buildLabel("Character Name", null);
		charName = buildTextField("");
		
		campNameL = buildLabel("Campaign Name", null);
		charLevelL = buildLabel("Level", null);
			charLevelL.setToolTipText(" ");
	    charExpL = buildLabel("Exp", null);
	    	charExpL.setToolTipText("<html>" + "Remaining XP to Level:");
	    charClassL = buildLabel("Class", null);
	    	charClassL.setToolTipText("<html>" + "Base Class:" + "<br>" + "Specialization:");
	    charRaceL = buildLabel("Race", null);
	    	charRaceL.setToolTipText("<html>" + "Race:");
	    
	    
	    campName = buildTextField("");
	    charLevel = buildNumTextField(1);
	    	charLevel.setToolTipText(" ");
	    charExp = buildNumTextField(0);
	    	charExp.setToolTipText("<html>" + "Remaining XP to Level:"); 
	    charClass = buildTextField(" ");
	    	charClass.setToolTipText("<html>" + "Base Class:" + "<br>" + "Specialization:"); 
	    charRace = buildTextField(" ");
	    	charRace.setToolTipText("<html>" + "Race:"); 
	    
		charGenderL = buildLabel("Gender", null);
		charSizeL = buildLabel("Size", null); 
			charSizeL.setToolTipText("");
		charAgeL = buildLabel("Age", null);
			charAgeL.setToolTipText("");
		charHeightL = buildLabel("Height", null);
		charWeightL = buildLabel("Weight", null);
		charEyesL = buildLabel("Eyes", null);
		charHairL = buildLabel("Hair", null); 
		
		charGender = buildTextField("");
		charSize = buildTextField("");
			charSize.setToolTipText("");
		charAge = buildNumTextField(0);
			charAge.setToolTipText("");
		charHeight = buildTextField("");
		charWeight = buildTextField("");
		charEyes = buildTextField("");
		charHair = buildTextField("");
		
		charPhysicalL = buildLabel("Physical Features", null);
		charPersonalityL = buildLabel("Personality Traits", null);
		
		charPhysical = buildTextArea("");
		physicalPane = buildScrollPane(charPhysical);
		charPersonality = buildTextArea("");
		personalityPane = buildScrollPane(charPersonality);
		
		addExpButton = buildButton("Add Exp");
		addExpButton.addActionListener (e -> sheetFrame.expPressed());
			addExpButton.setToolTipText("Open a dialog to add or subtract experience."); 
		
		restButton = buildButton("Rest");
		restButton.addActionListener (e -> sheetFrame.restPressed(character));
			restButton.setToolTipText("Open a dialog to rest or advance time."); 
		
		editCharacterButton = buildButton("Edit Details");
		editCharacterButton.addActionListener (e -> sheetFrame.editPressed());
			editCharacterButton.setToolTipText("Open a dialog to edit the above character information."); 

		statusButton = buildButton("Status");
		statusButton.addActionListener(e -> sheetFrame.statusPressed());
			statusButton.setToolTipText("Open the shorthand status preview/apply dialog.");
		
		coreAttL = buildLabel("Core Attribute", null);
		coreValueL = buildLabel("Value", null);
		coreModL = buildLabel("Modifier", null);
		coreModL.setVisible(false);
		coreRollL = buildLabel("Roll Check", null);
		charAttL = buildLabel("Char Attribute", null);
		charValueL = buildLabel("Value", null);
		charModL = buildLabel("Modifier", null);
		charModL.setVisible(false);
		charRollL = buildLabel("Roll Check", null);

		strAttL = buildTextField("Strength");
		dexAttL = buildTextField("Dexterity");
		conAttL = buildTextField("Constitution");
		focAttL = buildTextField("Focus");
		ctlAttL = buildTextField("Control");
		capAttL = buildTextField("Capacity");
		knowAttL = buildTextField("Knowledge");
		mechAttL = buildTextField("Mechanical");
		percAttL = buildTextField("Perception");
		intAttL = buildTextField("Intuition");
		chaAttL = buildTextField("Charisma");
		subAttL = buildTextField("Subtlety");
		
		charStr = buildNumTextField(0.0);
		charDex = buildNumTextField(0.0);
		charCon = buildNumTextField(0.0);
		charFoc = buildNumTextField(0.0);
		charCtl = buildNumTextField(0.0);
		charCap = buildNumTextField(0.0);
		charKnow = buildNumTextField(0.0);
		charMech = buildNumTextField(0.0);
		charPerc = buildNumTextField(0.0);
		charInt = buildNumTextField(0.0);
		charCha = buildNumTextField(0.0);
		charSub = buildNumTextField(0.0);
		
		charStrMod = buildNumTextField(0.0);
		charStrMod.setVisible(false);
		charDexMod = buildNumTextField(0.0);
		charDexMod.setVisible(false);
		charConMod = buildNumTextField(0.0);
		charConMod.setVisible(false);
		charFocMod = buildNumTextField(0.0);
		charFocMod.setVisible(false);
		charCtlMod = buildNumTextField(0.0);
		charCtlMod.setVisible(false);
		charCapMod = buildNumTextField(0.0);
		charCapMod.setVisible(false);
		charKnowMod = buildNumTextField(0.0);
		charKnowMod.setVisible(false);
		charMechMod = buildNumTextField(0.0);
		charMechMod.setVisible(false);
		charPercMod = buildNumTextField(0.0);
		charPercMod.setVisible(false);
		charIntMod = buildNumTextField(0.0);
		charIntMod.setVisible(false);
		charChaMod = buildNumTextField(0.0);
		charChaMod.setVisible(false);
		charSubMod = buildNumTextField(0.0);
		charSubMod.setVisible(false);
		
		strRoll = buildCheckButton("Strength Check", false, "STR");
		dexRoll = buildCheckButton("Dexterity Check", false, "DEX");
		conRoll = buildCheckButton("Constitution Check", false, "CON");
		focRoll = buildCheckButton("Focus Check", false, "FOC");
		ctlRoll = buildCheckButton("Control Check", false, "CTL");
		capRoll = buildCheckButton("Capacity Check", false, "CAP");
		knowRoll = buildCheckButton("Knowledge Check", false, "KNOW");
		mechRoll = buildCheckButton("Mechanics Check", false, "MECH");
		percRoll = buildCheckButton("Perception Check", false, "PERC");
		intRoll = buildCheckButton("Intuition Check", false, "INT");
		chaRoll = buildCheckButton("Charisma Check", false, "CHA");
		subRoll = buildCheckButton("Subtlety Check", false, "SUB");
		
		defenseL = buildLabel("Defense", null);
		utilityL = buildLabel("Utility", null);
		miscL = buildLabel("Misc", null);

		defNameL = buildLabel("Name", null);
		defModL = buildLabel("Mod", null);
		defRollL = buildLabel("Roll", null);
		utilNameL = buildLabel("Name", null);
		utilModL = buildLabel("Mod", null);
		utilRollL = buildLabel("Roll", null);
		miscNameL = buildLabel("Name", null);
		miscModL = buildLabel("Mod", null);
		miscRollL = buildLabel("Roll", null);
    
		acStatL = buildTextField("AC");
		armorStatL = buildTextField("Armor");
		armorStatL.setVisible(false); // no longer displayed in the defense list
		dodgeStatL = buildTextField("Avoid");
		fortStatL = buildTextField("Fort");
		refStatL = buildTextField("Ref");
		willStatL = buildTextField("Will");
			
		charAC = buildNumTextField(0.0);
		charArmor = buildNumTextField(0.0);
		charArmor.setVisible(false); // hidden; AC tooltips still include armor component
		charDodge = buildNumTextField(0.0);
		charFort = buildNumTextField(0.0);
		charRef = buildNumTextField(0.0);
		charWill = buildNumTextField(0.0);

		fortRoll = buildCheckButton("Fortitude Save", false, "FORT");
		refRoll = buildCheckButton("Reflex Save", false, "REF");
		willRoll = buildCheckButton("Will Save", false, "WILL");
		resistRoll = buildTextField("Other");
		
		resistStatL = buildTextField("Resist");
		attackStatL = buildTextField("Attack");
		moveStatL = buildTextField("Move");
		flyStatL = buildTextField("Fly");
		flyStatL.setVisible(false); // removed from utility display order
		rangeStatL = buildTextField("Range");
		initStatL = buildTextField("Init");
		powerStatL = buildTextField("Power");
		powerStatL.setVisible(false);
		
		attackRoll = buildTextField("Other");
		moveRoll = buildTextField("Other");
		critRoll = buildTextField("Other");
		areaRoll = buildTextField("Other");
		bdmgRoll = buildTextField("Other");
		bhealRoll = buildTextField("Other");
		supRoll = buildTextField("Other");
		charResist = buildNumTextField(0.0);
		charAttack = buildNumTextField(0.0);
		charMove = buildNumTextField(0.0);
		charFly = buildNumTextField(0.0);
		charFly.setVisible(false); // removed from utility display order
		charRange = buildNumTextField(0.0);
		charInit = buildNumTextField(0.0);
		charPower = buildNumTextField(0.0);
		charPower.setVisible(false);

		initRoll = buildCheckButton("Initiative Roll", false, "INIT");
					
		supStatL = buildTextField("Crit");
		dcStatL = buildTextField("Apply");
		grantStatL = buildTextField("Area");
		exclStatL = buildTextField("Crush");
		maxattStatL = buildTextField("Max Atk");
		dbStatL = buildTextField("Sup");
		bdmgStatL = buildTextField("B Dmg");
		bhealStatL = buildTextField("B Heal");
					
		charSup = buildNumTextField(0.0);
		charDC = buildNumTextField(0.0);
		charGrant = buildNumTextField(0.0);
		charExcl = buildNumTextField(0.0);
		charMaxatt = buildNumTextField(0.0);
		charDb = buildNumTextField(0.0);
		charBdmg = buildNumTextField(0.0);
		charBheal = buildNumTextField(0.0);
		
		skillsAtt = new ArrayList<JTextField>();
		skillsName = new ArrayList<JTextField>();
		skillsRoll = new ArrayList<JButton>();
		specialtiesName = new ArrayList<JTextField>();
		
		skillsL = buildLabel("Skills", null);
		specialtiesL = buildLabel("Specialties", null);
		
		skillsAttL = buildLabel("Attribute", null);
		skillsNameL = buildLabel("Name", null);
		skillsRollL = buildLabel("Roll", null);
		specialtiesNameL = buildLabel("Name", null);
	}  /*--------------
		END DEFAULTCONSTRUCTOR
		--------------*/
	
	/*
	 * 		RESIZE SHEET
	 */
	public void resizeSheet() {
		pageHeight = resizeHeader();
		charNameL.setBounds(3, pageHeight, 130, 20);
		campNameL.setBounds(138, pageHeight, 130, 20);
		charLevelL.setBounds(273, pageHeight, 40, 20);
		charExpL.setBounds(318, pageHeight, 60, 20);
		charClassL.setBounds(383, pageHeight, 85, 20);
		charRaceL.setBounds(473, pageHeight, 85, 20);
		pageHeight += 20;
		
		charName.setBounds(3, pageHeight, 130, 20);
		campName.setBounds(138, pageHeight, 130, 20);
		charLevel.setBounds(273, pageHeight, 40, 20);
		charExp.setBounds(318, pageHeight, 60, 20);
		charClass.setBounds(383, pageHeight, 85, 19);
		charRace.setBounds(473, pageHeight, 85, 20);
		pageHeight += 20;
		
		charGenderL.setBounds(3, pageHeight, 90, 20);
		charSizeL.setBounds(98, pageHeight, 75, 20);;
		charAgeL.setBounds(178, pageHeight, 60, 20);
		charHeightL.setBounds(243, pageHeight, 60, 20);
		charWeightL.setBounds(308, pageHeight, 70, 20);
		charEyesL.setBounds(383, pageHeight, 85, 20);
		charHairL.setBounds(473, pageHeight, 85, 20);
		pageHeight += 20;
							
		charGender.setBounds(3, pageHeight, 90, 20);
		charSize.setBounds(98, pageHeight, 75, 20);
		charAge.setBounds(178, pageHeight, 60, 20);
		charHeight.setBounds(243, pageHeight, 60, 20);
		charWeight.setBounds(308, pageHeight, 70, 20);
		charEyes.setBounds(383, pageHeight, 85, 19);
		charHair.setBounds(473, pageHeight, 85, 20);
		pageHeight += 20;
		
		charPhysicalL.setBounds(5, pageHeight, 275, 20);
		charPersonalityL.setBounds(285, pageHeight, 275, 20);
		pageHeight += 20;
						
		physicalPane.setBounds(5, pageHeight, 275, 60);
		personalityPane.setBounds(285, pageHeight, 275, 60);
		pageHeight += 60;
		

		pageHeight += 5;
		addExpButton.setBounds(25,pageHeight,116,19);
		restButton.setBounds(160,pageHeight,116,19);
		editCharacterButton.setBounds(295,pageHeight,116,19);
		statusButton.setBounds(430,pageHeight,116,19);
		pageHeight += 25;
		

		coreAttL.setBounds(5, pageHeight, 95, 20);
		coreValueL.setBounds(105, pageHeight, 60, 20);
		coreModL.setBounds(145, pageHeight, 50, 20);
		coreRollL.setBounds(170, pageHeight, 95, 20);
		charAttL.setBounds(295, pageHeight, 95, 20);
		charValueL.setBounds(395, pageHeight, 60, 20);
		charModL.setBounds(425, pageHeight, 50, 20);
		charRollL.setBounds(460, pageHeight, 95, 20);
		pageHeight += 20;
		
		strAttL.setBounds(5, pageHeight, 95, 20);
		charStr.setBounds(105, pageHeight, 60, 20);
		charStrMod.setBounds(145, pageHeight, 50, 20);
		strRoll.setBounds(170, pageHeight, 95, 19);
		knowAttL.setBounds(295, pageHeight, 95, 20);
		charKnow.setBounds(395, pageHeight, 60, 20);
		charKnowMod.setBounds(425, pageHeight, 50, 19);
		knowRoll.setBounds(460, pageHeight, 95, 19);
		pageHeight += 20;
		
		dexAttL.setBounds(5, pageHeight, 95, 20);
		charDex.setBounds(105, pageHeight, 60, 20);
		charDexMod.setBounds(145, pageHeight, 50, 20);
		dexRoll.setBounds(170, pageHeight, 95, 19);
		mechAttL.setBounds(295, pageHeight, 95, 20);
		charMech.setBounds(395, pageHeight, 60, 20);
		charMechMod.setBounds(425, pageHeight, 50, 19);
		mechRoll.setBounds(460, pageHeight, 95, 19);
		pageHeight += 20;
		
		conAttL.setBounds(5, pageHeight, 95, 20);
		charCon.setBounds(105, pageHeight, 60, 20);
		charConMod.setBounds(145, pageHeight, 50, 20);
		conRoll.setBounds(170, pageHeight, 95, 19);
		percAttL.setBounds(295, pageHeight, 95, 20);
		charPerc.setBounds(395, pageHeight, 60, 20);
		charPercMod.setBounds(425, pageHeight, 50, 19);
		percRoll.setBounds(460, pageHeight, 95, 19);
		pageHeight += 20;
		
		focAttL.setBounds(5, pageHeight, 95, 20);
		charFoc.setBounds(105, pageHeight, 60, 20);
		charFocMod.setBounds(145, pageHeight, 50, 20);
		focRoll.setBounds(170, pageHeight, 95, 19);
		intAttL.setBounds(295, pageHeight, 95, 20);
		charInt.setBounds(395, pageHeight, 60, 20);
		charIntMod.setBounds(425, pageHeight, 50, 19);
		intRoll.setBounds(460, pageHeight, 95, 19);
		pageHeight += 20;
		
		ctlAttL.setBounds(5, pageHeight, 95, 20);
		charCtl.setBounds(105, pageHeight, 60, 20);	
		charCtlMod.setBounds(145, pageHeight, 50, 20);
		ctlRoll.setBounds(170, pageHeight, 95, 19);
		chaAttL.setBounds(295, pageHeight, 95, 20);
		charCha.setBounds(395, pageHeight, 60, 20);
		charChaMod.setBounds(425, pageHeight, 50, 19);
		chaRoll.setBounds(460, pageHeight, 95, 19);
		pageHeight += 20;
		
		capAttL.setBounds(5, pageHeight, 95, 20);
		charCap.setBounds(105, pageHeight, 60, 20);
		charCapMod.setBounds(145, pageHeight, 50, 20);
		capRoll.setBounds(170, pageHeight, 95, 19);
		subAttL.setBounds(295, pageHeight, 95, 20);
		charSub.setBounds(395, pageHeight, 60, 20);
		charSubMod.setBounds(425, pageHeight, 50, 19);
		subRoll.setBounds(460, pageHeight, 95, 19);
		pageHeight += 20;		
		

		defenseL.setBounds(5, pageHeight, 180, 20);
		utilityL.setBounds(195, pageHeight, 180, 20);
		miscL.setBounds(385, pageHeight, 180, 20);
		pageHeight += 20;
		
		defNameL.setBounds(5, pageHeight, 60, 20);
		defModL.setBounds(70, pageHeight, 40, 20);
		defRollL.setBounds(115, pageHeight, 65, 20);
		utilNameL.setBounds(195, pageHeight, 60, 20);
		utilModL.setBounds(260, pageHeight, 40, 20);
		utilRollL.setBounds(305, pageHeight, 65, 20);
		miscNameL.setBounds(385, pageHeight, 60, 20);
		miscModL.setBounds(450, pageHeight, 40, 20);
		miscRollL.setBounds(495, pageHeight, 65, 20);
		pageHeight +=20;
		
		// Defense column reordered: AC, Fort, Ref, Will, Resist, Avoid
		acStatL.setBounds(5, pageHeight, 60, 20);
		charAC.setBounds(70, pageHeight, 40, 20);
		attackStatL.setBounds(195, pageHeight, 60, 20);
		charAttack.setBounds(260, pageHeight, 40, 20);
		attackRoll.setBounds(305, pageHeight, 65, 19);
		supStatL.setBounds(385, pageHeight, 60, 20);
		charSup.setBounds(450, pageHeight, 40, 20);
		critRoll.setBounds(495, pageHeight, 65, 19);
		pageHeight +=20;
		
		fortStatL.setBounds(5, pageHeight, 60, 20);
		charFort.setBounds(70, pageHeight, 40, 20);
		fortRoll.setBounds(115, pageHeight, 65, 19);
		dcStatL.setBounds(195, pageHeight, 60, 20);
		charDC.setBounds(260, pageHeight, 40, 20);
		dbStatL.setBounds(385, pageHeight, 60, 20);
		charDb.setBounds(450, pageHeight, 40, 20);
		supRoll.setBounds(495, pageHeight, 65, 19);
		pageHeight +=20;
		
		refStatL.setBounds(5, pageHeight, 60, 20);
		charRef.setBounds(70, pageHeight, 40, 20);
		refRoll.setBounds(115, pageHeight, 65, 19);
		moveStatL.setBounds(195, pageHeight, 60, 20);
		charMove.setBounds(260, pageHeight, 40, 20);
		moveRoll.setBounds(305, pageHeight, 65, 19);
		grantStatL.setBounds(385, pageHeight, 60, 20);
		charGrant.setBounds(450, pageHeight, 40, 20);
		areaRoll.setBounds(495, pageHeight, 65, 19);
		pageHeight +=20;
		
		willStatL.setBounds(5, pageHeight, 60, 20);
		charWill.setBounds(70, pageHeight, 40, 20);
		willRoll.setBounds(115, pageHeight, 65, 19);
		rangeStatL.setBounds(195, pageHeight, 60, 20);
		charRange.setBounds(260, pageHeight, 40, 20);
		exclStatL.setBounds(385, pageHeight, 60, 20);
		charExcl.setBounds(450, pageHeight, 40, 20);
		pageHeight +=20;
		
		resistStatL.setBounds(5, pageHeight, 60, 20);
		charResist.setBounds(70, pageHeight, 40, 20);
		resistRoll.setBounds(115, pageHeight, 65, 19);
		initStatL.setBounds(195, pageHeight, 60, 20);
		charInit.setBounds(260, pageHeight, 40, 20);
		initRoll.setBounds(305, pageHeight, 65, 19);
		bdmgStatL.setBounds(385, pageHeight, 60, 20);
		charBdmg.setBounds(450, pageHeight, 40, 20);
		bdmgRoll.setBounds(495, pageHeight, 65, 19);
		pageHeight +=20;
		
		dodgeStatL.setBounds(5, pageHeight, 60, 20);
		charDodge.setBounds(70, pageHeight, 40, 20);
		maxattStatL.setBounds(195, pageHeight, 60, 20);
		charMaxatt.setBounds(260, pageHeight, 40, 20);
		bhealStatL.setBounds(385, pageHeight, 60, 20);
		charBheal.setBounds(450, pageHeight, 40, 20);
		bhealRoll.setBounds(495, pageHeight, 65, 19);
		pageHeight += 20;

		if (shouldShowLeaderPower()) {
			powerStatL.setBounds(195, pageHeight, 60, 20);
			charPower.setBounds(260, pageHeight, 40, 20);
			pageHeight += 20;
		} else {
			powerStatL.setVisible(false);
			charPower.setVisible(false);
		}
		

		skillsL.setBounds(5, pageHeight, 275, 20);
		specialtiesL.setBounds(285, pageHeight, 275, 20);
		pageHeight += 20;	

		skillsAttL.setBounds(5, pageHeight, 50, 20);
		skillsNameL.setBounds(60, pageHeight, 145, 20);
		skillsRollL.setBounds(210, pageHeight, 70, 20);
		specialtiesNameL.setBounds(285, pageHeight, 275, 20);
		pageHeight += 20;	
		
		int skillHeight = 0, specHeight = 0;
		for (int i = 0; i < skillsAtt.size(); i++) {
			skillsAtt.get(i).setBounds(5, pageHeight + skillHeight, 50, 20);
			skillsName.get(i).setBounds(60, pageHeight + skillHeight, 145, 20);
			skillsRoll.get(i).setBounds(210, pageHeight + skillHeight, 70, 19);
			skillHeight += 20;
		}
	
		for (int i = 0; i < specialtiesName.size(); i++) {
			specialtiesName.get(i).setBounds(285, pageHeight + (20 * i), 275, 20);
			specHeight += 20;
		}

		if (skillHeight > specHeight) {
			pageHeight += skillHeight;
		}
		else {
			pageHeight += specHeight;
		}	
		
		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}  /*--------------
		END RESIZESHEET
		--------------*/
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	* 
	* 		UPDATER
	* 
	*/
	/*
	 * 		UPDATE ALL
	 */
	@Override
	public void updateAll() {
		refreshDetailsOnly();
		refreshAttributesOnly();
		refreshStatisticsOnly();
		refreshSkillsOnly();
		refreshSpecialtiesOnly();
		resizeSheet();
		// Ensure name font fits after layout sizing
		fitTextToField(charName, 8);
		revalidate();
		repaint();
	}  /*--------------
		END UPDATEALL
		--------------*/

	public void refreshDetailsOnly() { updateDetails(); }
	public void refreshAttributesOnly() { updateAttributes(); }
	public void refreshStatisticsOnly() { updateStatistics(); }
	public void refreshSkillsOnly() { updateSkills(); }
	public void refreshSpecialtiesOnly() { updateSpecialties(); }
	
	/*
	 * 		UPDATE DETAILS
	 */
	public void updateDetails() {
		CharIdentity id = character.getIdentity();
		if (id == null) return;
		StoreRuleManager dq = this.dataQuery;

		// Ensure new timing fields are initialized to safe defaults
		if (id.getTimeSinceLastLevel() == null) {
			id.setTimeSinceLastLevel(Duration.ZERO);
		}

		// Basic identity fields
	    String displayName = (id.getNickname() != null && !id.getNickname().isBlank()) ? id.getNickname() : id.getName();
	    charName.setText(displayName);
	    charName.setToolTipText(id.getName());
	    charNameL.setToolTipText(id.getName());
	    campName.setText(id.getCampaign());
	    charLevel.setValue(id.getLevel());
	    charExp.setValue(id.getExp());
	    charClass.setText(resolveDisplayedClassName(id));
	    charRace.setText(id.getRace());
	    String elapsedToolTip = buildCampaignElapsedTooltip(id);
	    campName.setToolTipText(elapsedToolTip);
	    campNameL.setToolTipText(elapsedToolTip);
	    String levelElapsedTip = buildLevelElapsedTooltip(id);
	    charLevel.setToolTipText(levelElapsedTip);
	    charLevelL.setToolTipText(levelElapsedTip);
	    String classTip = buildClassTooltip(id);
	    charClass.setToolTipText(classTip);
	    charClassL.setToolTipText(classTip);
	    String raceTip = buildRaceTooltip(id);
	    charRace.setToolTipText(raceTip);
	    charRaceL.setToolTipText(raceTip);
	    fitTextToField(charName, 8); // shrink if needed to fit box

	    // Size: preserve explicit selections (e.g., Felshify), fall back to race size only when unset
	    String displaySize = id.getSize();
	    if ((displaySize == null || displaySize.isBlank() || "?".equals(displaySize.trim())) && dq != null) {
	    	DataRace race = dq.getRaceByName(id.getRace());
	    	if (race != null && race.getSize() != null && !race.getSize().isBlank()) {
	    		displaySize = race.getSize();
	    		id.setSize(displaySize);
	    	}
	    }

	    // Simple tooltips using available data
	    // Keep elapsed tooltip as primary; append level number for context
	    String levelTipWithValue = levelElapsedTip.replace("</html>", "<br>Level: " + id.getLevel() + "</html>");
	    charLevelL.setToolTipText(levelTipWithValue);
	    charLevel.setToolTipText(levelTipWithValue);

	    String expTip = buildExpTooltip(id);
	    charExpL.setToolTipText(expTip);
	    charExp.setToolTipText(expTip);

	    // Physical descriptors
	    charGender.setText(id.getGender());
	    charSize.setText(displaySize == null ? "" : displaySize);
	    charSizeL.setToolTipText("Size: " + (displaySize == null ? "" : displaySize));
	    charSize.setToolTipText("Size: " + (displaySize == null ? "" : displaySize));
		charAge.setValue(id.getAge());
		if (id.getBirthday() != null) {
			String bdayTip = "Birthday: " + id.getBirthday().toString();
			charAge.setToolTipText(bdayTip);
			charAgeL.setToolTipText(bdayTip);
		}
	    charHeight.setText(id.getHeight());
	    charWeight.setText(id.getWeight());
	    charEyes.setText(id.getEyes());
	    charHair.setText(id.getHair());

		charPhysical.setText(id.getPhysical());
		charPersonality.setText(id.getPersonality());
	}  /*--------------
		END UPDATE ALL
		--------------*/

	/**
	 * Reduce font size until text fits within the current field width, or until minSize.
	 */
	private void fitTextToField(JTextField field, int minSize) {
		if (field == null) return;
		int available = field.getWidth();
		if (available <= 0) {
			available = field.getPreferredSize().width;
		}
		Font font = field.getFont();
		int size = font.getSize();
		String text = field.getText() == null ? "" : field.getText();
		while (size > minSize) {
			int width = field.getFontMetrics(font).stringWidth(text);
			if (width <= available - 4) break; // small padding
			size--;
			font = font.deriveFont((float) size);
		}
		field.setFont(font);
	}
	
	private String buildCampaignElapsedTooltip(CharIdentity id) {
		String line1 = "Campaign elapsed: unknown";
		Duration stored = null;
		try {
			stored = id.getCampaignElapsedTime();
		} catch (Exception ignored) {}
		if (stored != null) {
			line1 = "Campaign elapsed: " + humanDuration(stored);
		}

		String line2 = "Since creation: unknown";
		if (id.getCreatedAt() != null) {
			Duration sinceCreated = Duration.between(id.getCreatedAt().toInstant(), Instant.now());
			line2 = "Since creation: " + humanDuration(sinceCreated);
		}
		return "<html>" + line1 + "<br>" + line2 + "</html>";
	}

	private String humanDuration(Duration d) {
		if (d == null) return "unknown";
		long days = d.toDays();
		long hours = d.minusDays(days).toHours();
		StringBuilder sb = new StringBuilder();
		sb.append(days).append(" days");
		if (hours > 0) {
			sb.append(", ").append(hours).append(" hours");
		}
		return sb.toString();
	}

	private String buildLevelElapsedTooltip(CharIdentity id) {
		String line1 = "In-game since last level: unknown";
		Duration baseline = id.getTimeSinceLastLevel();
		Duration currentElapsed = id.getCampaignElapsedTime();
		if (baseline != null && currentElapsed != null) {
			Duration diff = currentElapsed.minus(baseline);
			if (diff.isNegative()) diff = Duration.ZERO;
			line1 = "In-game since last level: " + humanDuration(diff);
		}

		String line2 = "Real time since last level: unknown";
		if (id.getLastLevelUp() != null) {
			Duration real = Duration.between(id.getLastLevelUp().toInstant(), Instant.now());
			line2 = "Real time since last level: " + humanDuration(real);
		}

		return "<html>" + line1 + "<br>" + line2 + "</html>";
	}

	private String buildExpTooltip(CharIdentity id) {
		int currentLevel = id.getLevel();
		int nextReq = id.getNextAt();
		double exp = id.getExp();
		double remaining = Math.max(0, nextReq - exp);
		return "<html>EXP to next level: " + nextReq + "<br>EXP remaining: " + (int)remaining + "</html>";
	}

	private String resolveDisplayedClassName(CharIdentity id) {
		if (id == null) return "";
		String baseClass = id.getCharClass();
		int level = Math.max(1, id.getLevel());
		if (level <= 4) {
			return baseClass == null ? "" : baseClass;
		}
		String subclass = id.getCharSubclass();
		if (subclass != null && !subclass.isBlank() && !"?".equals(subclass.trim())) {
			return subclass;
		}
		return baseClass == null ? "" : baseClass;
	}

	private String buildClassTooltip(CharIdentity id) {
		String baseClass = id.getCharClass();
		String subclass = id.getCharSubclass();
		DataClass effectiveClass = null;
		if (dataQuery != null) {
			if (subclass != null && !subclass.isBlank() && !"?".equals(subclass.trim())) {
				effectiveClass = dataQuery.getClassByName(subclass);
			}
			if (effectiveClass == null) {
				effectiveClass = dataQuery.getClassByName(baseClass);
			}
		}

		StringBuilder sb = new StringBuilder("<html>");
		sb.append("Base Class: ").append(baseClass == null ? "" : baseClass);
		if (subclass != null && !subclass.isBlank() && !"?".equals(subclass.trim())) {
			sb.append("<br>Specialization: ").append(subclass);
		}
		if (effectiveClass != null) {
			sb.append("<br>Role: ").append(effectiveClass.getRole());
			sb.append("<br>HP Scaling: ").append(effectiveClass.getHpScaling());
			sb.append("<br>Aura Scaling: ").append(effectiveClass.getAuraScaling());
		}
		sb.append("</html>");
		return sb.toString();
	}

	private String buildRaceTooltip(CharIdentity id) {
		if (dataQuery == null) return "Race: " + id.getRace();
		DataRace race = dataQuery.getRaceByName(id.getRace());
		if (race == null) return "Race: " + id.getRace();
		String baseDesc = race.getBaseStatusDesc() == null ? "" : race.getBaseStatusDesc();
		String scalingDesc = race.getScalingStatusDesc() == null ? "" : race.getScalingStatusDesc();
		String racialSpec = "";
		try {
			DataSpecialty spec = dataQuery.getSpecialtyById(race.getRacialID());
			if (spec != null && spec.getName() != null) {
				racialSpec = spec.getName();
			}
		} catch (Exception ignored) {}
		return "<html>Race: " + race.getName() +
				"<br>Base Status: " + baseDesc +
				"<br>Scaling Status: " + scalingDesc +
				(racialSpec.isEmpty() ? "" : "<br>Racial Specialty: " + racialSpec) +
				"</html>";
	}

	private String buildStatTooltip(String name, double value, String key) {
		StringBuilder sb = new StringBuilder("<html>");
		sb.append(name).append(": ").append(fmt1(value));
		appendStatusBreakdown(sb, key);
		sb.append("</html>");
		return sb.toString();
	}

	private void appendStatusBreakdown(StringBuilder sb, String key) {
		appendStatusLines(sb, "-------(Base)-------", getBaseStatusBlock(character.getAttributes(), key), false);
		appendStatusLines(sb, "------(Multi)-----", getMultiplierStatusBlock(character.getAttributes(), key), true);
	}

	private void appendStatusLines(StringBuilder sb, String header, ArrayList<DataStatus>[] block, boolean multiplier) {
		if (block == null) return;
		sb.append("<br>").append(header).append("<br>");
		for (ArrayList<DataStatus> list : block) {
			if (list == null || list.isEmpty()) continue;
			for (DataStatus status : list) {
				if (status == null) continue;
				double shownValue = status.getSeverity();
				if (multiplier && "Base".equalsIgnoreCase(status.getName())) {
					shownValue = 1.0;
				}
				sb.append("+ ").append(status.getName()).append(": ").append(fmt1(shownValue)).append("<br>");
			}
		}
	}

	private String buildAttributeTooltip(String name, String key, DataClass resolvedClass) {
		CharAttributes displayedAttributes = getDisplayedAttributes();
		double value = resolveDisplayedAttributeValue(displayedAttributes, key);
		StringBuilder sb = new StringBuilder("<html>");
		sb.append(name).append(": ").append(fmt1(value));
		if (isPrimaryAttribute(key, resolvedClass)) sb.append(" <b>(Primary Attribute)</b>");
		if (isSecondaryAttribute(key, resolvedClass)) sb.append(" <b>(Secondary Attribute)</b>");
		appendStatusBreakdown(sb, key);
		sb.append("</html>");
		return sb.toString();
	}

	/*
	 * 		UPDATE ATTRIBUTES
	 */
	public void updateAttributes() {
		CharAttributes attrs = getDisplayedAttributes();
		if (attrs == null) return;
		DataClass resolvedClass = resolveClass();

		String[] keys = ATTSHORT;
		JTextField[] attLabels = {strAttL, dexAttL, conAttL, focAttL, ctlAttL, capAttL, knowAttL, mechAttL, percAttL, intAttL, chaAttL, subAttL};
		JFormattedTextField[] valFields = {charStr, charDex, charCon, charFoc, charCtl, charCap, charKnow, charMech, charPerc, charInt, charCha, charSub};
		JButton[] rollButtons = {strRoll, dexRoll, conRoll, focRoll, ctlRoll, capRoll, knowRoll, mechRoll, percRoll, intRoll, chaRoll, subRoll};

		for (int i = 0; i < keys.length; i++) {
			double val = resolveDisplayedAttributeValue(attrs, keys[i]);
			valFields[i].setValue(round1(val));

			String tip = buildAttributeTooltip(ATTRIBUTES[i], ATTSHORT[i], resolvedClass);
			attLabels[i].setToolTipText(tip);
			valFields[i].setToolTipText(tip);
			rollButtons[i].setToolTipText("/roll d20 + " + fmt1(val));
		}
	}  /*--------------
		END UPDATEATTRIBUTES
		--------------*/

	/*
	 * 		UPDATE STATISTICS
	 */
	public void updateStatistics() {
		CharAttributes attrs = getDisplayedAttributes();
		if (attrs == null) return;

		// Defense values
		double def = getDerivedStatusValue(attrs, "DEF");
		double armorBonus = getDerivedStatusValue(attrs, "ARMOR");
		double dodgeBonus = getDerivedStatusValue(attrs, "DODGE");
		double armor = armorBonus + def;
		double dodge = dodgeBonus + def;
		double fort = getDerivedStatusValue(attrs, "FORT");
		double ref = getDerivedStatusValue(attrs, "REF");
		double will = getDerivedStatusValue(attrs, "WILL");
		double resistAll = getDerivedStatusValue(attrs, "ALL");
		double avoid = getDerivedStatusValue(attrs, "AVOID");
		double ac = def + armorBonus + dodgeBonus;

		charAC.setValue(round1(ac));
		charArmor.setValue(round1(armor));
		charDodge.setValue(round1(dodge));
		charFort.setValue(round1(fort));
		charRef.setValue(round1(ref));
		charWill.setValue(round1(will));
		charResist.setValue(round1(resistAll));

		acStatL.setToolTipText(html("AC: " + fmt1(ac), "DEF: " + fmt1(def), "Armor: " + fmt1(armorBonus), "Dodge: " + fmt1(dodgeBonus)));
		charAC.setToolTipText(acStatL.getToolTipText());
		armorStatL.setToolTipText(html("Armor: " + fmt1(armor), "DEF: " + fmt1(def), "Armor Bonus: " + fmt1(armorBonus)));
		charArmor.setToolTipText(armorStatL.getToolTipText());
		dodgeStatL.setToolTipText(html("Dodge: " + fmt1(dodge), "DEF: " + fmt1(def), "Dodge Bonus: " + fmt1(dodgeBonus)));
		charDodge.setToolTipText(dodgeStatL.getToolTipText());
		fortStatL.setToolTipText(buildStatTooltip("Fortitude", fort, "FORT"));
		charFort.setToolTipText(fortStatL.getToolTipText());
		refStatL.setToolTipText(buildStatTooltip("Reflex", ref, "REF"));
		charRef.setToolTipText(refStatL.getToolTipText());
		willStatL.setToolTipText(buildStatTooltip("Will", will, "WILL"));
		charWill.setToolTipText(willStatL.getToolTipText());
		resistStatL.setToolTipText(buildStatTooltip("Resist All", resistAll, "ALL"));
		charResist.setToolTipText(resistStatL.getToolTipText());
		resistRoll.setToolTipText(getCachedResistTooltip(attrs));

		double atk = getDerivedStatusValue(attrs, "ATK");
		double dc = getDerivedStatusValue(attrs, "APP");
		double move = getDerivedStatusValue(attrs, "MOVE");
		double power = getDerivedStatusValue(attrs, "POWER");
		double fly = getDerivedStatusValue(attrs, "FLY");
		double range = getDerivedStatusValue(attrs, "RANGE");
		double init = resolveDisplayedInitiativeValue(attrs);
		double cman = getDerivedStatusValue(attrs, "CMAN");
		double maxAtk = getDerivedStatusValue(attrs, "MAXATK");

		charAttack.setValue(round1(atk));
		charDC.setValue(round1(dc));
		charMove.setValue(round1(move));
		charPower.setValue(round1(power));
		charFly.setValue(round1(fly));
		charRange.setValue(round1(range));
		charInit.setValue(round1(init));
		charMaxatt.setValue(round1(maxAtk));

		attackStatL.setToolTipText(buildStatTooltip("Attack", atk, "ATK"));
		charAttack.setToolTipText(attackStatL.getToolTipText());
		attackRoll.setToolTipText(buildStatTooltip("Combat Maneuvers", cman, "CMAN"));
		dcStatL.setToolTipText(buildStatTooltip("Application", dc, "APP"));
		charDC.setToolTipText(dcStatL.getToolTipText());
		moveStatL.setToolTipText(buildStatTooltip("Move Speed", move, "MOVE"));
		charMove.setToolTipText(moveStatL.getToolTipText());
		powerStatL.setToolTipText(buildStatTooltip("Power", power, "POWER"));
		charPower.setToolTipText(powerStatL.getToolTipText());
		moveRoll.setToolTipText(buildStatTooltip("Fly Speed", fly, "FLY"));
		rangeStatL.setToolTipText(buildStatTooltip("Range", range, "RANGE"));
		charRange.setToolTipText(rangeStatL.getToolTipText());
		initStatL.setToolTipText(buildStatTooltip("Initiative", init, "INIT"));
		charInit.setToolTipText(initStatL.getToolTipText());
		initRoll.setToolTipText("/roll d20 + " + fmt1(init));
		initRoll.setVisible(shouldShowInitiativeRollButton());
		maxattStatL.setToolTipText(buildStatTooltip("Max Attacks", maxAtk, "MAXATK"));
		charMaxatt.setToolTipText(maxattStatL.getToolTipText());

		double support = getDerivedStatusValue(attrs, "SUP");
		double impairment = getDerivedStatusValue(attrs, "IMP");
		double mastery = getDerivedStatusValue(attrs, "MAST");
		double excl = getDerivedStatusValue(attrs, "EXCL");
		double grant = getDerivedStatusValue(attrs, "GRANT");
		double crush = getDerivedStatusValue(attrs, "CRUSH");
		double area = getDerivedStatusValue(attrs, "AREA");
		double baseDmg = getDerivedStatusValue(attrs, "BDMG");
		double totalDmg = getDerivedStatusValue(attrs, "TDMG");
		double baseHeal = getDerivedStatusValue(attrs, "BHEAL");
		double totalHeal = getDerivedStatusValue(attrs, "THEAL");
		double crit = getDerivedStatusValue(attrs, "CRIT");
		double critMulti = getDerivedStatusValue(attrs, "CRITDMG");

		charSup.setValue(round1(crit));
		charDb.setValue(round1(support));
		charGrant.setValue(round1(area));
		charExcl.setValue(round1(crush));
		charBdmg.setValue(round1(baseDmg));
		charBheal.setValue(round1(baseHeal));

		supStatL.setToolTipText(buildStatTooltip("Critical Increment", crit, "CRIT"));
		charSup.setToolTipText(supStatL.getToolTipText());
		critRoll.setToolTipText(buildStatTooltip("Critical Damage Multiplier", critMulti, "CRITDMG"));

		String dbTitle = "Support";
		String dbKey = "SUP";
		double dbValue = support;
		String role = resolveClass() != null ? resolveClass().getRole() : "";
		if ("Impairment".equalsIgnoreCase(role) || "Control".equalsIgnoreCase(role)) {
			dbTitle = "Impairment";
			dbKey = "IMP";
			dbValue = impairment;
			dbStatL.setText("Imp");
			supRoll.setToolTipText(buildStatTooltip("Mastery", mastery, "MAST"));
		} else {
			dbStatL.setText("Sup");
			supRoll.setToolTipText(buildStatTooltip("Grant", grant, "GRANT"));
		}
		dbStatL.setToolTipText(buildStatTooltip(dbTitle, dbValue, dbKey));
		charDb.setValue(round1(dbValue));
		charDb.setToolTipText(dbStatL.getToolTipText());

		grantStatL.setToolTipText(buildStatTooltip("Area Multiplier", area, "AREA"));
		charGrant.setToolTipText(grantStatL.getToolTipText());
		areaRoll.setToolTipText(buildStatTooltip("Exclusion Count", excl, "EXCL"));
		exclStatL.setToolTipText(buildStatTooltip("Crush", crush, "CRUSH"));
		charExcl.setToolTipText(exclStatL.getToolTipText());
		bdmgStatL.setToolTipText(buildStatTooltip("Base Damage", baseDmg, "BDMG"));
		charBdmg.setToolTipText(bdmgStatL.getToolTipText());
		bdmgRoll.setToolTipText(buildStatTooltip("Total Damage", totalDmg, "TDMG"));
		bhealStatL.setToolTipText(buildStatTooltip("Base Healing", baseHeal, "BHEAL"));
		charBheal.setToolTipText(bhealStatL.getToolTipText());
		bhealRoll.setToolTipText(buildStatTooltip("Total Healing", totalHeal, "THEAL"));

		boolean showPower = shouldShowLeaderPower();
		powerStatL.setVisible(showPower);
		charPower.setVisible(showPower);
	}  /*--------------
		END UPDATESTATISTICS
		--------------*/

	protected boolean shouldShowLeaderPower() {
		if (character == null || character.getIdentity() == null) return false;
		String className = character.getIdentity().getCharClass();
		return className != null && className.equalsIgnoreCase("Leader");
	}

	protected CharAttributes getDisplayedAttributes() {
		return character == null ? null : character.getAttributes();
	}

	protected double resolveDisplayedAttributeValue(CharAttributes attrs, String key) {
		return getDerivedStatusValue(attrs, key);
	}

	protected double resolveDisplayedInitiativeValue(CharAttributes attrs) {
		return getDerivedStatusValue(attrs, "INIT");
	}

	protected boolean shouldShowInitiativeRollButton() {
		return true;
	}

	private String html(String... lines) {
		StringBuilder sb = new StringBuilder("<html>");
		for (int i = 0; i < lines.length; i++) {
			sb.append(lines[i]);
			if (i < lines.length - 1) sb.append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}
	
	private String buildResistTooltip(CharAttributes attrs) {
		if (attrs == null) return "Resists: unknown";
		StringBuilder sb = new StringBuilder("<html>Resists:<br>");
		for (String key : RESIST_KEYS) {
			sb.append(key).append(": ").append(fmt1(getDerivedStatusValue(attrs, key))).append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}

	private String getCachedResistTooltip(CharAttributes attrs) {
		String signature = buildResistTooltipSignature(attrs);
		if (!signature.equals(cachedResistTooltipSignature)) {
			cachedResistTooltipSignature = signature;
			cachedResistTooltip = buildResistTooltip(attrs);
		}
		return cachedResistTooltip;
	}

	private String buildResistTooltipSignature(CharAttributes attrs) {
		if (attrs == null) return "";
		StringBuilder signature = new StringBuilder();
		for (String key : RESIST_KEYS) {
			signature.append(key).append('=').append(fmt1(getDerivedStatusValue(attrs, key))).append(';');
		}
		return signature.toString();
	}

	/*
	 * 		UPDATE SKILLS
	 */
	public void updateSkills() {
		if (character == null) {
			hideUnusedSkillRows(0);
			return;
		}
		CharSpecials specials = character.getSpecials();
		if (specials == null) {
			hideUnusedSkillRows(0);
			return;
		}

		List<DataSkill> skillList = specials.getSkills();
		if (skillList == null) skillList = List.of();
		ensureSkillRowCapacity(skillList.size());
		for (int i = 0; i < skillList.size(); i++) {
			DataSkill tempSkill = skillList.get(i);
			String tempAtt = "-";
			List<String> chosenForDisplay = tempSkill.getChosenAttributes();
			if (chosenForDisplay == null) chosenForDisplay = List.of();
			for (String attChoice : chosenForDisplay) {
				if (attChoice != null && !attChoice.trim().isEmpty() && !attChoice.equals("-")) {
					tempAtt = attChoice;
					break;
				}
			}
			JTextField attField = skillsAtt.get(i);
			attField.setText(tempAtt);
			attField.setToolTipText(buildSkillAttributeTooltip(tempSkill));
			attField.setForeground(Color.BLACK);
			int realChosenCount = 0;
			List<String> chosenAttrs = tempSkill.getChosenAttributes();
			if (chosenAttrs != null) {
				for (String attChoice : chosenAttrs) {
					if (attChoice == null) continue;
					String trimmed = attChoice.trim();
					if (trimmed.isEmpty() || "-".equals(trimmed)) continue;
					realChosenCount++;
				}
			}
			if (realChosenCount > 1) {
				attField.setForeground(MULTI_ATTRIBUTE_COLOR);
			}
			attField.setVisible(true);
			
			String tempName = tempSkill.getName();
			String tempSubtype = tempSkill.getChosenSubtype();
			if (tempSubtype != null && !tempSubtype.isBlank()) {
				tempName += " (" + tempSubtype + ")";
			}
			JTextField nameField = skillsName.get(i);
			nameField.setText(tempName);
			nameField.setToolTipText(buildSkillDescriptionTooltip(tempSkill));
			nameField.setVisible(true);
		
			JButton rollBtn = skillsRoll.get(i);
			final String selectedAtt = tempAtt;
			final String checkName = tempName + " Check";
			replaceButtonAction(rollBtn, () -> checkPressed(checkName, true, selectedAtt, ""));
			rollBtn.setText("Roll");
			rollBtn.setToolTipText(buildSkillRollTooltip(tempSkill, selectedAtt));
			rollBtn.setVisible(true);
		}
		hideUnusedSkillRows(skillList.size());
	}  /*--------------
		END UPDATESKILLS
		--------------*/
	
	private String buildSkillAttributeTooltip(DataSkill skill) {
		if (skill == null) return "Attributes: unknown";
		String tooltipFinal = "";
		String tipTemp = "";

		List<String> chosen = skill.getChosenAttributes();
		if (chosen == null) chosen = List.of();
		List<String> avail = skill.getAvailAttributes();
		if (avail == null) avail = List.of();
		double value = 0.0;
		for (String s : chosen) {
			if (s == null) continue;
			String key = s.trim().toUpperCase();
			if (key.isEmpty() || "-".equals(key)) continue;
			double cur = getDerivedStatusValue(character.getAttributes(), key) * 1.5;
			value += cur;
			tipTemp += "+ " + key + ": " + fmt1(cur) + "<br>";
		}
		if (tipTemp.isEmpty()) {
			tipTemp = "No chosen attribute<br>";
		}
		double intuitionBonus = getUniversalSkillBonus();
		if (intuitionBonus != 0.0) {
			value += intuitionBonus;
			tipTemp += "+ Intuition: " + fmt1(intuitionBonus) + "<br>";
		}
		double dedicationBonus = getSkillDedicationBonus(skill);
		if (dedicationBonus != 0.0) {
			value += dedicationBonus;
			tipTemp += "+ Skill Dedication: " + fmt1(dedicationBonus) + "<br>";
		}
		double divineVowBonus = getDivineVowSkillBonus(skill);
		if (divineVowBonus != 0.0) {
			value += divineVowBonus;
			tipTemp += "+ Divine Vow: " + fmt1(divineVowBonus) + "<br>";
		}
		double statusSkillBonus = getStatusSkillBonus(skill);
		if (statusSkillBonus != 0.0) {
			value += statusSkillBonus;
			tipTemp += "+ Skill Status: " + fmt1(statusSkillBonus) + "<br>";
		}

		tooltipFinal = "<html>Total Skill Value: " + fmt1(value) + "<br>-------(Attributes)-------<br>" + tipTemp;
		tooltipFinal += "---Available Attributes---<br>";
		for (String s : avail) {
			tooltipFinal += "- " + s + "<br>";
		}
		tooltipFinal += "</html>";

		return tooltipFinal;
	}
	
	private String buildSkillDescriptionTooltip(DataSkill skill) {
		if (skill == null) return "Description: unknown";
		String desc = skill.getDescription();
		if (desc == null || desc.isBlank()) desc = "No description available.";
		// Basic newline to <br> conversion for readability
		desc = desc.replace("\n", "<br>");
		return "<html>" + desc + "</html>";
	}
	
	private String buildSkillRollTooltip(DataSkill skill, String selectedAtt) {
		if (skill == null || character == null || character.getAttributes() == null) return "Roll: unknown";
		String att = (selectedAtt == null || selectedAtt.isBlank() || "-".equals(selectedAtt)) ? "INT" : selectedAtt.toUpperCase();
		double mod = getDerivedStatusValue(character.getAttributes(), att) * 1.5;
		mod += getUniversalSkillBonus();
		mod += getSkillSpecialBonus(skill);
		return "/roll d20 + " + fmt1(mod);
	}

	private double getUniversalSkillBonus() {
		if (character == null) return 0.0;
		return character.getUniversalSkillBonus();
	}

	private double getSkillSpecialBonus(DataSkill skill) {
		return getSkillDedicationBonus(skill) + getDivineVowSkillBonus(skill) + getStatusSkillBonus(skill);
	}

	private double getSkillDedicationBonus(DataSkill skill) {
		if (skill == null || character == null) return 0.0;
		return character.getSkillDedicationBonusForDisplayName(CharSpecials.formatSkillDisplayName(skill));
	}

	private double getDivineVowSkillBonus(DataSkill skill) {
		if (skill == null || character == null) return 0.0;
		return character.getDivineVowSkillBonusForDisplayName(CharSpecials.formatSkillDisplayName(skill));
	}

	private double getStatusSkillBonus(DataSkill skill) {
		if (skill == null || character == null) return 0.0;
		return character.getStatusSkillBonusForDisplayName(CharSpecials.formatSkillDisplayName(skill));
	}

	private String fmt1(double val) {
		return String.format(java.util.Locale.ROOT, "%.1f", round1(val));
	}

	private double round1(double val) {
		return Math.round(val * 10.0) / 10.0;
	}

	private String buildSpecialtyTooltip(DataSpecialty spec) {
		if (spec == null) return "Description: unknown";
		String holyDomainTooltip = resolveHolyDomainTooltip(spec);
		if (holyDomainTooltip != null && !holyDomainTooltip.isBlank()) {
			return "<html>" + holyDomainTooltip.replace("\n", "<br>") + "</html>";
		}
		String vowTooltip = resolveDivineVowTooltip(spec);
		if (vowTooltip != null && !vowTooltip.isBlank()) {
			return "<html>" + vowTooltip.replace("\n", "<br>") + "</html>";
		}
		String divineAttunementTooltip = resolveDivineAttunementTooltip(spec);
		if (divineAttunementTooltip != null && !divineAttunementTooltip.isBlank()) {
			return "<html>" + divineAttunementTooltip.replace("\n", "<br>") + "</html>";
		}
		String combatDisciplineTooltip = resolveCombatDisciplineTooltip(spec);
		if (combatDisciplineTooltip != null && !combatDisciplineTooltip.isBlank()) {
			return "<html>" + combatDisciplineTooltip.replace("\n", "<br>") + "</html>";
		}
		String favoredTooltip = resolveFavoredSpecialtyTooltip(spec);
		if (favoredTooltip != null && !favoredTooltip.isBlank()) {
			return "<html>" + favoredTooltip.replace("\n", "<br>") + "</html>";
		}
		String desc = spec.getDescription();
		if (desc == null || desc.isBlank()) desc = "No description available.";
		return "<html>" + desc.replace("\n", "<br>") + "</html>";
	}

	private String resolveHolyDomainTooltip(DataSpecialty spec) {
		if (spec == null || character == null || character.getIdentity() == null) return null;
		if (!"Holy Domain".equalsIgnoreCase(spec.getName())) return null;

		String deityName = resolveSelectedDeityName();
		String domainName = resolveSelectedDomainName(spec);
		if (deityName == null || deityName.isBlank() || domainName == null || domainName.isBlank()) return null;

		StringBuilder tooltip = new StringBuilder();
		tooltip.append("Deity: ").append(deityName.trim());
		tooltip.append("<br>Domain: ").append(domainName.trim());

		if ("Cleric".equalsIgnoreCase(character.getIdentity().getCharClass())) {
			DataDomain domain = dataQuery.getDomainByName(domainName.trim());
			String condition = domain == null || domain.getCondition() == null || domain.getCondition().isBlank()
					? "-"
					: domain.getCondition().trim();
			tooltip.append("<br>Condition: ").append(condition);
			tooltip.append("<br>Cleric Note: At the start of combat and each round, you apply your domain effects to yourself at 50% effectiveness until end of round.");
		} else if ("Paladin".equalsIgnoreCase(character.getIdentity().getCharClass())) {
			tooltip.append("<br>You may activate your emanation to provide domain effects to nearby allies.");
		}

		return tooltip.toString();
	}

	private String resolveDivineVowTooltip(DataSpecialty spec) {
		if (spec == null || character == null) return null;
		if (!"Divine Vow".equalsIgnoreCase(spec.getName())) return null;
		String vowName = resolveSelectedVowName();
		if (vowName == null || vowName.isBlank()) return null;
		DataVow vow = dataQuery.getVowByName(vowName.trim());
		if (vow == null) return null;
		String vowText = vow.getVowText();
		if ((vowText == null || vowText.isBlank()) && vow.getVowEffect() != null) {
			vowText = vow.getVowEffect().trim();
		}
		if (vowText == null || vowText.isBlank()) return null;
		return "Vow of " + vow.getName() + ": " + vowText;
	}

	private String resolveDivineAttunementTooltip(DataSpecialty spec) {
		if (spec == null || character == null) return null;
		if (!"Divine Attunement".equalsIgnoreCase(spec.getName())) return null;
		String deityName = resolveSelectedDeityName();
		if (deityName == null || deityName.isBlank()) return null;

		DataDeity deity = dataQuery.getDeityByName(deityName.trim());
		if (deity == null) {
			return "Deity: " + deityName.trim();
		}

		String virtues = deity.getVirtues() == null || deity.getVirtues().isEmpty()
				? "-"
				: deity.getVirtues().stream()
						.filter(value -> value != null && !value.isBlank())
						.map(String::trim)
						.reduce((left, right) -> left + ", " + right)
						.orElse("-");
		String weapon = deity.getWeapon() == null || deity.getWeapon().isBlank()
				? "-"
				: deity.getWeapon().trim();

		return "Deity: " + deityName.trim()
				+ "<br>Virtues: " + virtues
				+ "<br>Granted Weapon Proficiency: " + weapon;
	}

	private String resolveCombatDisciplineTooltip(DataSpecialty spec) {
		if (spec == null) return null;
		if (!"Martial Focus".equalsIgnoreCase(spec.getName())
				&& !"Combat Discipline".equalsIgnoreCase(spec.getName())) return null;

		String focus = normalizeMartialFocusChoice(simplifySpecialtyRefName(spec.getRefName()));
		if (focus == null || focus.isBlank()) return null;

		if ("Mobility".equalsIgnoreCase(focus)) {
			return "Mobility: Gain +2.5 MOVE per level.<br>Any time you would move exactly 5ft, you may move an additional 5ft.";
		}
		if ("Avoidance".equalsIgnoreCase(focus)) {
			return "Avoidance: Gain +1 REF per level.";
		}
		if ("Harm".equalsIgnoreCase(focus)) {
			return "Harm: Gain +(level^1.5) TDMG.";
		}
		return null;
	}

	private String normalizeMartialFocusChoice(String focus) {
		if (focus == null) return null;
		return "Martial".equalsIgnoreCase(focus.trim()) ? "Harm" : focus.trim();
	}

	private String resolveFavoredSpecialtyTooltip(DataSpecialty spec) {
		if (spec == null || spec.getName() == null) return null;
		if ("Favored Enemy and Terrain".equalsIgnoreCase(spec.getName())) {
			String enemyEntries = String.join(", ", getListEntries("Favored Enemies"));
			String terrainEntries = String.join(", ", getListEntries("Favored Terrain"));
			String enemyText = enemyEntries.isBlank() ? "None" : enemyEntries;
			String terrainText = terrainEntries.isBlank() ? "None" : terrainEntries;
			String desc = spec.getDescription();
			if (desc == null || desc.isBlank()) {
				desc = "Gain + level to ATK and CRIT when fighting favored enemies.<br>"
						+ "Gain + level to APPLY, SUP, and MAST when fighting in favored terrain.";
			}
			return desc + "<br><br>Favored Enemies: " + enemyText + "<br>Favored Terrain: " + terrainText;
		}
		if (!"Favored Enemy".equalsIgnoreCase(spec.getName()) && !"Favored Terrain".equalsIgnoreCase(spec.getName())) {
			return null;
		}

		String desc = spec.getDescription();
		if (desc == null || desc.isBlank()) return null;
		String listName = "Favored Enemy".equalsIgnoreCase(spec.getName()) ? "Favored Enemies" : "Favored Terrain";
		String entries = String.join(", ", getListEntries(listName));
		String resolvedDesc = entries.isBlank() ? desc.replace("@ ", "") : desc.replace("@", entries);
		String listText = entries.isBlank() ? "None" : entries;
		return resolvedDesc + "<br><br>" + listName + ": " + listText;
	}

	private boolean shouldUnifyArcherFavoredSpecialties(CharSpecials specials) {
		if (specials == null || character == null || character.getIdentity() == null) return false;
		if (!"Archer".equalsIgnoreCase(character.getIdentity().getCharClass())) return false;
		return specials.hasSpecialty("Favored Enemy") && specials.hasSpecialty("Favored Terrain");
	}

	private boolean isArcherFavoredSpecialty(DataSpecialty spec) {
		if (spec == null || spec.getName() == null) return false;
		return "Favored Enemy".equalsIgnoreCase(spec.getName())
				|| "Favored Terrain".equalsIgnoreCase(spec.getName());
	}

	private DataSpecialty resolveUnifiedArcherFavoredSpecialty() {
		DataSpecialty specialty = dataQuery.getSpecialtyByName("Favored Enemy and Terrain");
		return specialty == null ? null : new DataSpecialty(specialty);
	}

	private List<String> getListEntries(String listName) {
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

	private String resolveSelectedVowName() {
		if (character == null) return null;
		if (character.getSpecials() != null) {
			DataSpecialty divineVow = character.getSpecials().findSpecialty("Divine Vow");
			if (divineVow != null && divineVow.getRefName() != null && !divineVow.getRefName().isBlank()) {
				return divineVow.getRefName().trim();
			}
		}
		if (character.getIdentity() == null || character.getIdentity().getCharClassPick() == null) return null;
		List<String> classPicks = character.getIdentity().getCharClassPick();
		if (classPicks.size() < 2) return null;
		String vowName = classPicks.get(1);
		return vowName == null || vowName.isBlank() ? null : vowName.trim();
	}

	private String resolveSelectedDeityName() {
		if (character == null || character.getIdentity() == null || character.getIdentity().getCharClassPick() == null) return null;
		List<String> classPicks = character.getIdentity().getCharClassPick();
		if (classPicks.isEmpty()) return null;
		String deityName = classPicks.get(0);
		return deityName == null || deityName.isBlank() ? null : deityName.trim();
	}

	private String resolveSelectedDomainName(DataSpecialty spec) {
		if (spec != null && spec.getRefName() != null && !spec.getRefName().isBlank()) {
			return spec.getRefName().trim();
		}
		if (character == null || character.getIdentity() == null || character.getIdentity().getCharClassPick() == null) return null;
		List<String> classPicks = character.getIdentity().getCharClassPick();
		String className = character.getIdentity().getCharClass();
		int domainIndex = "Paladin".equalsIgnoreCase(className) ? 2 : 1;
		if (domainIndex < 0 || domainIndex >= classPicks.size()) return null;
		String domainName = classPicks.get(domainIndex);
		return domainName == null || domainName.isBlank() ? null : domainName.trim();
	}

	/** Checks if the provided attribute key matches the character class' primary attribute. */
	private boolean isPrimaryAttribute(String key, DataClass cls) {
		if (key == null || cls == null) return false;
		String primary = cls != null ? cls.getPrimaryAtt() : null;
		return primary != null && primary.equalsIgnoreCase(key);
	}

	/** Checks if the provided attribute key matches the character class' secondary attribute. */
	private boolean isSecondaryAttribute(String key, DataClass cls) {
		if (key == null || cls == null) return false;
		String secondary = cls != null ? cls.getSecondaryAtt() : null;
		return secondary != null && secondary.equalsIgnoreCase(key);
	}

	/** Resolves subclass first, then base class if subclass not found. */
	private DataClass resolveClass() {
		String subclass = character.getIdentity().getCharSubclass();
		DataClass cls = dataQuery.getClassByName(subclass);
		if (cls == null) cls = dataQuery.getClassByName(character.getIdentity().getCharClass());
		return cls;
	}
	
	/*
	 * 		UPDATE SPECIALTIES
	 */
	public void updateSpecialties() {
		if (character == null) {
			hideUnusedSpecialtyRows(0);
			return;
		}
		CharSpecials specials = character.getSpecials();
		if (specials == null) {
			hideUnusedSpecialtyRows(0);
			return;
		}

		ArrayList<DataSpecialty> visibleSpecialties = new ArrayList<>();
		DataSpecialty racial = specials.getRacialSpecialty();
		if (racial != null && racial.getName() != null && !isHiddenSpecialty(racial)) {
			visibleSpecialties.add(racial);
		}

		List<DataSpecialty> visibleClassSpecialties = getVisibleClassSpecialties(specials);
		for (DataSpecialty spec : visibleClassSpecialties) {
			if (spec != null && spec.getName() != null && !isHiddenSpecialty(spec)) {
				visibleSpecialties.add(spec);
			}
		}

		for (DataSpecialty spec : specials.getTrainedSpecialties()) {
			if (spec != null && spec.getName() != null && !isHiddenSpecialty(spec)) {
				visibleSpecialties.add(spec);
			}
		}
		ensureSpecialtyRowCapacity(visibleSpecialties.size());
		int idx = 0;
		if (racial != null && racial.getName() != null && !isHiddenSpecialty(racial)) {
			bindSpecialtyRow(idx++, racial, RACIAL_SPECIALTY_COLOR, false);
		}
		for (DataSpecialty spec : visibleClassSpecialties) {
			if (spec != null && spec.getName() != null && !isHiddenSpecialty(spec)) {
				bindSpecialtyRow(idx++, spec, CLASS_SPECIALTY_COLOR, true);
			}
		}
		for (DataSpecialty spec : specials.getTrainedSpecialties()) {
			if (spec != null && spec.getName() != null && !isHiddenSpecialty(spec)) {
				bindSpecialtyRow(idx++, spec, TRAINED_SPECIALTY_COLOR, false);
			}
		}
		hideUnusedSpecialtyRows(visibleSpecialties.size());
	}  /*--------------
		END UPDATESPECIALTIES
		--------------*/

	private List<DataSpecialty> getVisibleClassSpecialties(CharSpecials specials) {
		ArrayList<DataSpecialty> visible = new ArrayList<>();
		if (specials == null) return visible;
		boolean unifyArcherFavoredSpecialties = shouldUnifyArcherFavoredSpecialties(specials);
		boolean addedUnifiedFavoredSpecialty = false;
		for (DataSpecialty spec : specials.getClassSpecialties()) {
			if (spec == null || spec.getName() == null) continue;
			if (unifyArcherFavoredSpecialties && isArcherFavoredSpecialty(spec)) {
				if (!addedUnifiedFavoredSpecialty) {
					DataSpecialty combined = resolveUnifiedArcherFavoredSpecialty();
					if (combined != null) {
						visible.add(combined);
						addedUnifiedFavoredSpecialty = true;
					}
				}
				continue;
			}
			visible.add(spec);
		}
		return visible;
	}

	private void ensureSkillRowCapacity(int size) {
		while (skillsAtt.size() < size) {
			skillsAtt.add(buildTextField(""));
			skillsName.add(buildTextField(""));
			skillsRoll.add(buildButton("Roll"));
		}
	}

	private void hideUnusedSkillRows(int usedCount) {
		for (int i = usedCount; i < skillsAtt.size(); i++) {
			skillsAtt.get(i).setVisible(false);
			skillsName.get(i).setVisible(false);
			skillsRoll.get(i).setVisible(false);
		}
	}

	private void ensureSpecialtyRowCapacity(int size) {
		while (specialtiesName.size() < size) {
			specialtiesName.add(buildTextField(""));
		}
	}

	private void bindSpecialtyRow(int index, DataSpecialty spec, Color color, boolean showChoiceRef) {
		JTextField field = specialtiesName.get(index);
		field.setText(formatSpecialtyName(spec, showChoiceRef));
		field.setForeground(color);
		field.setToolTipText(buildSpecialtyTooltip(spec));
		field.setVisible(true);
	}

	private String formatSpecialtyName(DataSpecialty spec, boolean showChoiceRef) {
		if (spec == null) return "";
		String name = spec.getName();
		String refName = simplifySpecialtyRefName(spec.getRefName());
		if (name == null) return "";
		if ("Martial Focus".equalsIgnoreCase(name) || "Combat Discipline".equalsIgnoreCase(name)) {
			refName = normalizeMartialFocusChoice(refName);
			name = "Combat Discipline I";
		}
		if (isWarriorMartialFeatureDisplay(spec, showChoiceRef)) {
			return "Martial Feature (" + name + ")";
		}
		if (CharSpecials.SKILL_DEDICATION_SPECIALTY.equalsIgnoreCase(name)
				&& refName != null && !refName.isBlank()) {
			return name + " (" + refName + ")";
		}
		boolean showRef = showChoiceRef || spec.getPick();
		if (!showRef || refName == null || refName.isBlank()) return name;
		return name + ": " + refName;
	}

	private String simplifySpecialtyRefName(String refName) {
		if (refName == null) return null;
		String trimmed = refName.trim();
		if (trimmed.isBlank() || trimmed.contains("|")) return trimmed;
		int colonIndex = trimmed.indexOf(':');
		if (colonIndex >= 0 && colonIndex + 1 < trimmed.length()) {
			String suffix = trimmed.substring(colonIndex + 1).trim();
			if (!suffix.isBlank()) {
				return suffix;
			}
		}
		return trimmed;
	}

	private boolean isWarriorMartialFeatureDisplay(DataSpecialty spec, boolean showChoiceRef) {
		if (!showChoiceRef || spec == null || character == null || character.getIdentity() == null) return false;
		if (!"Warrior".equalsIgnoreCase(character.getIdentity().getCharClass())) return false;
		String specialtyName = spec.getName();
		if (specialtyName == null || specialtyName.isBlank()) return false;
		List<String> classPicks = character.getIdentity().getCharClassPick();
		if (classPicks == null || classPicks.isEmpty()) return false;
		String martialFeaturePick = classPicks.get(0);
		return martialFeaturePick != null && martialFeaturePick.equalsIgnoreCase(specialtyName);
	}

	private void hideUnusedSpecialtyRows(int usedCount) {
		for (int i = usedCount; i < specialtiesName.size(); i++) {
			specialtiesName.get(i).setVisible(false);
			specialtiesName.get(i).setToolTipText(null);
		}
	}

	private void replaceButtonAction(JButton button, Runnable action) {
		for (var listener : button.getActionListeners()) {
			button.removeActionListener(listener);
		}
		button.addActionListener(e -> action.run());
	}

	private boolean isProficiencySpecialty(DataSpecialty spec) {
		if (spec == null) return false;
		String category = spec.getCategory();
		if (category != null && (category.equalsIgnoreCase("Proficiency") || category.equalsIgnoreCase("Level"))) return true;
		String type = spec.getType();
		return type != null && (type.equalsIgnoreCase("Proficiency") || type.equalsIgnoreCase("Level"));
	}

	private boolean isHiddenSpecialty(DataSpecialty spec) {
		return isProficiencySpecialty(spec)
				|| (character != null && character.getSpecials() != null && character.getSpecials().shouldHideFromSpecialtyList(spec));
	}

	/*public int[] getAttData(String att) {
	int[] returnData = {0, 0, 0, 0};
	ArrayList<DataStatus> tempList = character.getAttArrayByShort(att);
	returnData[0] = (int)tempList.get(0).getSeverity();
	for (int i = 1; i < tempList.size(); i++) {
		if (tempList.get(i).getDurationType().compareTo("Permanent") != 0) {
			returnData[3] += tempList.get(i).getSeverity();
		}
		else {
			if (tempList.get(i).getDuration() != -1) {
				returnData[2] += tempList.get(i).getSeverity();
			}
			else {
				returnData[1] += tempList.get(i).getSeverity();
			}
		}
	}

	return returnData;
	}*/

} ///////////////////////////////////////////////END OF CLASS////////////////////////////////////////////////////////////////////////

