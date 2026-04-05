package eternity;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
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
import javax.swing.SwingConstants;

/*
 * 		MAIN PLAY PANEL
 */
public class PanelCharMain extends PanelCharBase {
	private static final long serialVersionUID = 1L;

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
	private JButton addExpButton, editCharacterButton, restButton;
	
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
	private JTextField attackStatL, dcStatL, moveStatL, flyStatL, rangeStatL, initStatL;
	private JFormattedTextField charAttack, charDC, charMove, charFly, charRange, charInit;
	private JTextField resistStatL, supStatL, dbStatL, grantStatL, exclStatL, maxattStatL, bdmgStatL, bhealStatL;
	private JFormattedTextField charResist, charSup, charDb, charGrant, charExcl, charMaxatt, charBdmg, charBheal;
	
	//Skills / Specialties
	private JLabel skillsL, specialtiesL, skillsAttL, skillsNameL, skillsRollL, specialtiesNameL;
	private ArrayList<JTextField> skillsAtt, skillsName, specialtiesName;
	private ArrayList<JButton> skillsRoll;
	
	/*
	 * 		DEFAULT CONSTRUCTOR
	 */
	PanelCharMain (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(244, 222, 222));
		
		charNameL = buildLabel("Character Name");
		charName = buildTextField("");
		
		campNameL = buildLabel("Campaign Name");
		charLevelL = buildLabel("Level");
			charLevelL.setToolTipText(" ");
	    charExpL = buildLabel("Exp");
	    	charExpL.setToolTipText("<html>" + "Remaining XP to Level:");
	    charClassL = buildLabel("Class");
	    	charClassL.setToolTipText("<html>" + "Base Class:" + "<br>" + "Specialization:");
	    charRaceL = buildLabel("Race");
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
	    
		charGenderL = buildLabel("Gender");
		charSizeL = buildLabel("Size"); 
			charSizeL.setToolTipText("");
		charAgeL = buildLabel("Age");
			charAgeL.setToolTipText("");
		charHeightL = buildLabel("Height");
		charWeightL = buildLabel("Weight");
		charEyesL = buildLabel("Eyes");
		charHairL = buildLabel("Hair"); 
		
		charGender = buildTextField("");
		charSize = buildTextField("");
			charSize.setToolTipText("");
		charAge = buildNumTextField(0);
			charAge.setToolTipText("");
		charHeight = buildTextField("");
		charWeight = buildTextField("");
		charEyes = buildTextField("");
		charHair = buildTextField("");
		
		charPhysicalL = buildLabel("Physical Features");
		charPersonalityL = buildLabel("Personality Traits");
		
		charPhysical = buildTextArea("");
		physicalPane = buildScrollPane(charPhysical);
		charPersonality = buildTextArea("");
		personalityPane = buildScrollPane(charPersonality);
		
		addExpButton = buildButton("Add Experience");
		addExpButton.addActionListener (e -> sheetFrame.expPressed());
			addExpButton.setToolTipText("Open a dialog to add or subtract experience."); 
		
		restButton = buildButton("Rest / Advance");
		restButton.addActionListener (e -> sheetFrame.restPressed(character));
			restButton.setToolTipText("Open a dialog to rest or advance time."); 
		
		editCharacterButton = buildButton("Edit Details");
		editCharacterButton.addActionListener (e -> sheetFrame.editPressed());
			editCharacterButton.setToolTipText("Open a dialog to edit the above character information."); 
		
		coreAttL = buildLabel("Core Attribute");
		coreValueL = buildLabel("Value");
		coreModL = buildLabel("Modifier");
		coreRollL = buildLabel("Roll Check");
		charAttL = buildLabel("Char Attribute");
		charValueL = buildLabel("Value");
		charModL = buildLabel("Modifier");
		charRollL = buildLabel("Roll Check");

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
		
		charStr = buildNumTextField(0);
		charDex = buildNumTextField(0);
		charCon = buildNumTextField(0);
		charFoc = buildNumTextField(0);
		charCtl = buildNumTextField(0);
		charCap = buildNumTextField(0);
		charKnow = buildNumTextField(0);
		charMech = buildNumTextField(0);
		charPerc = buildNumTextField(0);
		charInt = buildNumTextField(0);
		charCha = buildNumTextField(0);
		charSub = buildNumTextField(0);
		
		charStrMod = buildNumTextField(0);
		charDexMod = buildNumTextField(0);
		charConMod = buildNumTextField(0);
		charFocMod = buildNumTextField(0);
		charCtlMod = buildNumTextField(0);
		charCapMod = buildNumTextField(0);
		charKnowMod = buildNumTextField(0);
		charMechMod = buildNumTextField(0);
		charPercMod = buildNumTextField(0);
		charIntMod = buildNumTextField(0);
		charChaMod = buildNumTextField(0);
		charSubMod = buildNumTextField(0);
		
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
		
		defenseL = buildLabel("Defense");
		utilityL = buildLabel("Utility");
		miscL = buildLabel("Misc");

		defNameL = buildLabel("Name");
		defModL = buildLabel("Mod");
		defRollL = buildLabel("Roll");
		utilNameL = buildLabel("Name");
		utilModL = buildLabel("Mod");
		utilRollL = buildLabel("Roll");
		miscNameL = buildLabel("Name");
		miscModL = buildLabel("Mod");
		miscRollL = buildLabel("Roll");
    
		acStatL = buildTextField("AC");
		armorStatL = buildTextField("Armor");
		armorStatL.setVisible(false); // no longer displayed in the defense list
		dodgeStatL = buildTextField("Avoid");
		fortStatL = buildTextField("Fort");
		refStatL = buildTextField("Ref");
		willStatL = buildTextField("Will");
			
		charAC = buildNumTextField(0);
		charArmor = buildNumTextField(0);
		charArmor.setVisible(false); // hidden; AC tooltips still include armor component
		charDodge = buildNumTextField(0);
		charFort = buildNumTextField(0);
		charRef = buildNumTextField(0);
		charWill = buildNumTextField(0);

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
		
		attackRoll = buildTextField("Other");
		moveRoll = buildTextField("Other");
		critRoll = buildTextField("Other");
		areaRoll = buildTextField("Other");
		bdmgRoll = buildTextField("Other");
		bhealRoll = buildTextField("Other");
		supRoll = buildTextField("Other");
		charResist = buildNumTextField(0);
		charAttack = buildNumTextField(0);
		charMove = buildNumTextField(0);
		charFly = buildNumTextField(0);
		charFly.setVisible(false); // removed from utility display order
		charRange = buildNumTextField(0);
		charInit = buildNumTextField(0);

		initRoll = buildCheckButton("Initiative Roll", false, "INIT");
					
		supStatL = buildTextField("Crit");
		dcStatL = buildTextField("Apply");
		grantStatL = buildTextField("Area");
		exclStatL = buildTextField("Crush");
		maxattStatL = buildTextField("Max Atk");
		dbStatL = buildTextField("Sup");
		bdmgStatL = buildTextField("B Dmg");
		bhealStatL = buildTextField("B Heal");
					
		charSup = buildNumTextField(0);
		charDC = buildNumTextField(0);
		charGrant = buildNumTextField(0);
		charExcl = buildNumTextField(0);
		charMaxatt = buildNumTextField(0);
		charDb = buildNumTextField(0);
		charBdmg = buildNumTextField(0);
		charBheal = buildNumTextField(0);
		
		skillsAtt = new ArrayList<JTextField>();
		skillsName = new ArrayList<JTextField>();
		skillsRoll = new ArrayList<JButton>();
		specialtiesName = new ArrayList<JTextField>();
		
		skillsL = buildLabel("Skills");
		specialtiesL = buildLabel("Specialties");
		
		skillsAttL = buildLabel("Attribute");
		skillsNameL = buildLabel("Name");
		skillsRollL = buildLabel("Roll");
		specialtiesNameL = buildLabel("Name");
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
		addExpButton.setBounds(25,pageHeight,136,19);
		restButton.setBounds(211,pageHeight,136,19);
		editCharacterButton.setBounds(397,pageHeight,136,19);
		pageHeight += 25;
		

		coreAttL.setBounds(5, pageHeight, 80, 20);
		coreValueL.setBounds(90, pageHeight, 50, 20);
		coreModL.setBounds(145, pageHeight, 50, 20);
		coreRollL.setBounds(200, pageHeight, 80, 20);
		charAttL.setBounds(285, pageHeight, 80, 20);
		charValueL.setBounds(370, pageHeight, 50, 20);
		charModL.setBounds(425, pageHeight, 50, 20);
		charRollL.setBounds(480, pageHeight, 80, 20);
		pageHeight += 20;
		
		strAttL.setBounds(5, pageHeight, 80, 20);
		charStr.setBounds(90, pageHeight, 50, 20);
		charStrMod.setBounds(145, pageHeight, 50, 20);
		strRoll.setBounds(200, pageHeight, 80, 19);
		knowAttL.setBounds(285, pageHeight, 80, 20);
		charKnow.setBounds(370, pageHeight, 50, 20);
		charKnowMod.setBounds(425, pageHeight, 50, 19);
		knowRoll.setBounds(480, pageHeight, 80, 19);
		pageHeight += 20;
		
		dexAttL.setBounds(5, pageHeight, 80, 20);
		charDex.setBounds(90, pageHeight, 50, 20);
		charDexMod.setBounds(145, pageHeight, 50, 20);
		dexRoll.setBounds(200, pageHeight, 80, 19);
		mechAttL.setBounds(285, pageHeight, 80, 20);
		charMech.setBounds(370, pageHeight, 50, 20);
		charMechMod.setBounds(425, pageHeight, 50, 19);
		mechRoll.setBounds(480, pageHeight, 80, 19);
		pageHeight += 20;
		
		conAttL.setBounds(5, pageHeight, 80, 20);
		charCon.setBounds(90, pageHeight, 50, 20);
		charConMod.setBounds(145, pageHeight, 50, 20);
		conRoll.setBounds(200, pageHeight, 80, 19);
		percAttL.setBounds(285, pageHeight, 80, 20);
		charPerc.setBounds(370, pageHeight, 50, 20);
		charPercMod.setBounds(425, pageHeight, 50, 19);
		percRoll.setBounds(480, pageHeight, 80, 19);
		pageHeight += 20;
		
		focAttL.setBounds(5, pageHeight, 80, 20);
		charFoc.setBounds(90, pageHeight, 50, 20);
		charFocMod.setBounds(145, pageHeight, 50, 20);
		focRoll.setBounds(200, pageHeight, 80, 19);
		intAttL.setBounds(285, pageHeight, 80, 20);
		charInt.setBounds(370, pageHeight, 50, 20);
		charIntMod.setBounds(425, pageHeight, 50, 19);
		intRoll.setBounds(480, pageHeight, 80, 19);
		pageHeight += 20;
		
		ctlAttL.setBounds(5, pageHeight, 80, 20);
		charCtl.setBounds(90, pageHeight, 50, 20);	
		charCtlMod.setBounds(145, pageHeight, 50, 20);
		ctlRoll.setBounds(200, pageHeight, 80, 19);
		chaAttL.setBounds(285, pageHeight, 80, 20);
		charCha.setBounds(370, pageHeight, 50, 20);
		charChaMod.setBounds(425, pageHeight, 50, 19);
		chaRoll.setBounds(480, pageHeight, 80, 19);
		pageHeight += 20;
		
		capAttL.setBounds(5, pageHeight, 80, 20);
		charCap.setBounds(90, pageHeight, 50, 20);
		charCapMod.setBounds(145, pageHeight, 50, 20);
		capRoll.setBounds(200, pageHeight, 80, 19);
		subAttL.setBounds(285, pageHeight, 80, 20);
		charSub.setBounds(370, pageHeight, 50, 20);
		charSubMod.setBounds(425, pageHeight, 50, 19);
		subRoll.setBounds(480, pageHeight, 80, 19);
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
		updateDetails();
		updateAttributes();
		updateStatistics();
		updateSkills();
		updateSpecialties();
		resizeSheet();
		// Ensure name font fits after layout sizing
		fitTextToField(charName, 8);
		revalidate();
		repaint();
	}  /*--------------
		END UPDATEALL
		--------------*/
	
	/*
	 * 		UPDATE DETAILS
	 */
	public void updateDetails() {
		CharIdentity id = character.getIdentity();
		if (id == null) return;
		DataQuery dq = this.dataQuery;

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
	    charClass.setText(id.getCharClass());
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

	private String buildClassTooltip(CharIdentity id) {
		if (dataQuery == null) return "Class: " + id.getCharClass();
		DataClass dc = dataQuery.getClassByName(id.getCharClass());
		if (dc == null) return "Class: " + id.getCharClass();
		return "<html>Class: " + dc.getName() +
				"<br>Role: " + dc.getRole() +
				"<br>HP Scaling: " + dc.getHpScaling() +
				"<br>Aura Scaling: " + dc.getAuraScaling() +
				"</html>";
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

	private String buildAttributeTooltip(String name, String key) {
		String tempString = "<html>" + name + ": " + character.getAttributes().getAttribute(key);
		if (isPrimaryAttribute(key)) tempString += " <b>(Primary Attribute)</b>";
		if (isSecondaryAttribute(key)) tempString += " <b>(Secondary Attribute)</b>";
		tempString += "<br>-------(Base)-------<br>";
		StatBlock tempBlock = character.getAttributes().getBlock("attribute", key);
		List<DataStatus> statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		tempString += "--------(Multi)-------<br>";
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		return tempString + "</html>";
	}

	/*
	 * 		UPDATE ATTRIBUTES
	 */
	public void updateAttributes() {
		CharAttributes attrs = character.getAttributes();
		if (attrs == null) return;

		String[] keys = ATTSHORT;
		JTextField[] attLabels = {strAttL, dexAttL, conAttL, focAttL, ctlAttL, capAttL, knowAttL, mechAttL, percAttL, intAttL, chaAttL, subAttL};
		JFormattedTextField[] valFields = {charStr, charDex, charCon, charFoc, charCtl, charCap, charKnow, charMech, charPerc, charInt, charCha, charSub};
		JFormattedTextField[] modFields = {charStrMod, charDexMod, charConMod, charFocMod, charCtlMod, charCapMod, charKnowMod, charMechMod, charPercMod, charIntMod, charChaMod, charSubMod};
		JButton[] rollButtons = {strRoll, dexRoll, conRoll, focRoll, ctlRoll, capRoll, knowRoll, mechRoll, percRoll, intRoll, chaRoll, subRoll};

		for (int i = 0; i < keys.length; i++) {
			int val = attrs.getAttribute(keys[i]);
			int mod = val - 10;
			valFields[i].setValue(val);
			modFields[i].setValue(mod);

			String tip = buildAttributeTooltip(ATTRIBUTES[i], ATTSHORT[i]);
			attLabels[i].setToolTipText(tip);
			valFields[i].setToolTipText(tip);
			modFields[i].setToolTipText(tip);
			rollButtons[i].setToolTipText("/roll d20 + " + fmt(mod));
		}
	}  /*--------------
		END UPDATEATTRIBUTES
		--------------*/

	/*
	 * 		UPDATE STATISTICS
	 */
	public void updateStatistics() {
		CharAttributes attrs = character.getAttributes();
		if (attrs == null) return;

		// Defense values
		int def   = attrs.getDefense("DEF");
		int armor = attrs.getDefense("ARMOR") + def;
		int dodge = attrs.getDefense("DODGE") + def;
		int fort  = attrs.getDefense("FORT");
		int ref   = attrs.getDefense("REF");
		int will  = attrs.getDefense("WILL");
		int ac    = armor + dodge - def;
		int resistAll = attrs.getResist("ALL");
		int avoid = attrs.getDefense("AVOID");

		charAC.setValue(ac);
		charArmor.setValue(armor);
		charDodge.setValue(dodge);
		charFort.setValue(fort);
		charRef.setValue(ref);
		charWill.setValue(will);
		charResist.setValue(resistAll);

		String tempString = "<html>AC: " + ac + "<br>-------(Def)-------<br>";
		StatBlock tempBlock = character.getAttributes().getBlock("defense", "DEF");
		List<DataStatus> statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Def Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		tempBlock = character.getAttributes().getBlock("defense", "ARMOR");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Armor: " + armor + ")-----<br>";
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Armor Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		tempBlock = character.getAttributes().getBlock("defense", "DODGE");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Dodge: " + dodge + ")-----<br>";
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Dodge Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		acStatL.setToolTipText(tempString + "</html>");
		charAC.setToolTipText(acStatL.getToolTipText());

		tempString = "<html>Fortitude: " + fort + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("defense", "FORT");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		fortStatL.setToolTipText(tempString + "</html>");
		charFort.setToolTipText(fortStatL.getToolTipText());

		tempString = "<html>Reflex: " + ref + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("defense", "REF");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		refStatL.setToolTipText(tempString + "</html>");
		charRef.setToolTipText(refStatL.getToolTipText());

		tempString = "<html>Will: " + will + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("defense", "WILL");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		willStatL.setToolTipText(tempString + "</html>");
		charWill.setToolTipText(willStatL.getToolTipText());

		tempString = "<html>Resist All: " + resistAll + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("resist", "ALL");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		resistStatL.setToolTipText(tempString + "</html>");
		charResist.setToolTipText(resistStatL.getToolTipText());
		resistRoll.setToolTipText(buildResistTooltip(attrs));

		tempString = "<html>Avoid: " + avoid + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("defense", "AVOID");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		dodgeStatL.setToolTipText(tempString + "</html>");
		charDodge.setToolTipText(dodgeStatL.getToolTipText());


		// Combat values
		int atk   = attrs.getCombat("ATK");
		int dc    = attrs.getCombat("APP");
		int move  = attrs.getCombat("MOVE");
		int fly   = attrs.getCombat("FLY");
		int range = attrs.getCombat("RANGE");
		int init  = attrs.getCombat("INIT");
		int maxatk = attrs.getSecondary("MAXATK");

		charAttack.setValue(atk);
		charDC.setValue(dc);
		charMove.setValue(move);
		charFly.setValue(fly);
		charRange.setValue(range);
		charInit.setValue(init);
		charMaxatt.setValue(maxatk);

		tempString = "<html>Attack: " + atk + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("combat", "ATK");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		attackStatL.setToolTipText(tempString + "</html>");
		charAttack.setToolTipText(attackStatL.getToolTipText());

		tempString = "<html>Combat Maneuvers: " + atk + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "CMAN");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		attackRoll.setToolTipText(tempString + "</html>");

		tempString = "<html>Application: " + dc + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("combat", "APP");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		dcStatL.setToolTipText(tempString + "</html>");
		charDC.setToolTipText(dcStatL.getToolTipText());

		tempString = "<html>Move Speed: " + move + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("combat", "MOVE");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		moveStatL.setToolTipText(tempString + "</html>");
		charMove.setToolTipText(moveStatL.getToolTipText());

		tempString = "<html>Fly Speed: " + fly + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("combat", "FLY");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		moveRoll.setToolTipText(tempString + "</html>");

		tempString = "<html>Range: " + range + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("combat", "RANGE");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		rangeStatL.setToolTipText(tempString + "</html>");
		charRange.setToolTipText(rangeStatL.getToolTipText());

		tempString = "<html>Initiative: " + init + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("combat", "INIT");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		initStatL.setToolTipText(tempString + "</html>");
		charInit.setToolTipText(initStatL.getToolTipText());
		initRoll.setToolTipText("/roll d20 + " + fmt(init));

		tempString = "<html>Max Attacks: " + maxatk + "<br>";
		if (maxatk > 1) tempString += "Your full attack grants " + Math.ceil(maxatk-1) + " attacks.<br>";
		if ((double)Math.ceil(maxatk-1) != maxatk-1) tempString += "Your final attack will have reduced damage.<br>";
		tempString += "-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "MAXATK");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		maxattStatL.setToolTipText(tempString + "</html>");
		charMaxatt.setToolTipText(maxattStatL.getToolTipText());



		// Resist / secondary values
		int sup       = attrs.getSecondary("SUP");
		int imp       = attrs.getSecondary("IMP");
		int ben       = attrs.getSecondary("BEN");
		int excl      = attrs.getSecondary("EXCL");
		int maxAtk    = attrs.getSecondary("MAXATK");
		int baseDmg   = attrs.getDamage("BDMG");
		int baseHeal  = attrs.getDamage("BHEAL");
		int totalDmg  = attrs.getDamage("TDMG");
		int totalHeal = attrs.getDamage("THEAL");
		double critMulti = attrs.getDamage("CRITDMG");
		double crush   = attrs.getSecondary("CRUSH");
		int crit = attrs.getDamage("CRIT");
		double area = attrs.getSecondary("AREA");

		charSup.setValue(crit);
		charDb.setValue(sup);
		charGrant.setValue(round2(area));
		charExcl.setValue(round2(crush));
		charMaxatt.setValue(maxAtk);
		charBdmg.setValue(baseDmg);
		charBheal.setValue(baseHeal);

		tempString = "<html>Critical Increment: " + crit + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("damage", "CRIT");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		supStatL.setToolTipText(tempString + "</html>");
		charSup.setToolTipText(supStatL.getToolTipText());

		tempString = "<html>Critical Damage Multiplier: " + fmt(critMulti) + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("damage", "CRITDMG");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		critRoll.setToolTipText(tempString + "</html>");

		//***************************************** */
		tempString = character.getIdentity().getCharSubclass();
		DataClass charClass = dataQuery != null ? dataQuery.getClassByName(tempString) : null;
		if (charClass == null && dataQuery != null) {
			// Fallback to base class if subclass lookup failed
			charClass = dataQuery.getClassByName(character.getIdentity().getCharClass());
		}
		boolean impSub = charClass != null && ("Impairment".equals(charClass.getRole()) || "Control".equals(charClass.getRole()));

		tempString = "<html>Support: " + sup + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "SUP");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}

		tempString += "<<<----------------->>><br>Grant: " + fmt(sup) + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "GRANT");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}


		if (impSub) {
			dbStatL.setText("Imp");
			charDb.setValue(imp);
			supRoll.setToolTipText(tempString + "</html>");
		}
		else {
			dbStatL.setToolTipText(tempString + "</html>");
			charDb.setToolTipText(dbStatL.getToolTipText());
		}

		tempString = "<html>Impairment: " + imp + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "IMP");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}

		tempString += "<<<----------------->>><br>Mastery: " + sup + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "MAST");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}

		if (!impSub) supRoll.setToolTipText(tempString + "</html>");
		else {
			dbStatL.setToolTipText(tempString + "</html>");
			charDb.setToolTipText(dbStatL.getToolTipText());
		}

		tempString = "<html>Area Multiplier: " + fmt(area) + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "AREA");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		grantStatL.setToolTipText(tempString + "</html>");
		charGrant.setToolTipText(grantStatL.getToolTipText());

		tempString = "<html>Exclusion Count: " + excl + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "EXCL");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		areaRoll.setToolTipText(tempString + "</html>");

		tempString = "<html>Crush: " + fmt(crush) + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("secondary", "CRUSH");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		exclStatL.setToolTipText(tempString + "</html>");
		charExcl.setToolTipText(exclStatL.getToolTipText());
		
		tempString = "<html>Base Damage: " + baseDmg + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("damage", "BDMG");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		bdmgStatL.setToolTipText(tempString + "</html>");
		charBdmg.setToolTipText(bdmgStatL.getToolTipText());

		tempString = "<html>Total Damage: " + totalDmg + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("damage", "TDMG");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		bdmgRoll.setToolTipText(tempString + "</html>");

		tempString = "<html>Base Healing: " + baseHeal + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("damage", "BHEAL");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		bhealStatL.setToolTipText(tempString + "</html>");
		charBheal.setToolTipText(bhealStatL.getToolTipText());

		tempString = "<html>Total Healing: " + totalHeal + "<br>-------(Base)-------<br>";
		tempBlock = character.getAttributes().getBlock("damage", "THEAL");
		statuses = null;
		if (tempBlock != null) statuses = tempBlock.getAllStatuses();
		if (statuses != null && !statuses.isEmpty()) {
			for (DataStatus status : statuses) {
				tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		if (tempBlock != null) statuses = tempBlock.getAllMultipliers();
		if (statuses != null && !statuses.isEmpty()) {
			tempString += "------(Multi)-----<br>";
			for (DataStatus status : statuses) {
				if (status.getName().compareTo("Base") == 0) tempString += "+ " + status.getName() + ": 1.0<br>";
				else tempString += "+ " + status.getName() + ": " + fmt(status.getSeverity()) + "<br>";
			}
		}
		bhealRoll.setToolTipText(tempString + "</html>");

	}  /*--------------
		END UPDATESTATISTICS
		--------------*/

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
			sb.append(key).append(": ").append(attrs.getResist(key)).append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}

	/*
	 * 		UPDATE SKILLS
	 */
	public void updateSkills() {
		for (int i = skillsAtt.size(); i > 0; i--) {
			remove(skillsAtt.get(i-1));
			remove(skillsName.get(i-1));
			remove(skillsRoll.get(i-1));
		}
		
		skillsAtt = new ArrayList<JTextField>();
		skillsName = new ArrayList<JTextField>();
		skillsRoll = new ArrayList<JButton>();

		if (character == null) {
			revalidate();
			repaint();
			return;
		}
		CharSpecials specials = character.getSpecials();
		if (specials == null) {
			revalidate();
			repaint();
			return;
		}
				
		for (int i = 0; i < specials.getSkills().size(); i++) {
			DataSkill tempSkill = specials.getSkills().get(i);
			String tempAtt = "-";
			for (String attChoice : tempSkill.getChosenAttributes()) {
				if (attChoice != null && !attChoice.trim().isEmpty() && !attChoice.equals("-")) {
					tempAtt = attChoice;
					break;
				}
			}
			JTextField attField = buildTextField(tempAtt);
			attField.setToolTipText(buildSkillAttributeTooltip(tempSkill));
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
				attField.setForeground(java.awt.Color.BLUE);
			}
			skillsAtt.add(attField);
			
			String tempName = tempSkill.getName();
			JTextField nameField = buildTextField(tempName);
			nameField.setToolTipText(buildSkillDescriptionTooltip(tempSkill));
			skillsName.add(nameField);
		
			JButton rollBtn = buildCheckButton(tempName + " Check", true, tempAtt);
			rollBtn.setToolTipText(buildSkillRollTooltip(tempSkill, tempAtt));
			skillsRoll.add(rollBtn);
			add(skillsAtt.get(i));
			add(skillsName.get(i));
			add(skillsRoll.get(i));
		}
		revalidate();
		repaint();
	}  /*--------------
		END UPDATESKILLS
		--------------*/
	
	private String buildSkillAttributeTooltip(DataSkill skill) {
		if (skill == null) return "Attributes: unknown";
		String tooltipFinal = "";
		String tipTemp = "";

		List<String> chosen = skill.getChosenAttributes();
		List<String> avail = skill.getAvailAttributes();
		double value = 0.0;
		double cur = 0.0;
		for (String s : chosen) {
			if (s.compareTo("-") == 0) continue;
			cur = (character.getAttributes().getAttribute(s) - 10) * 1.5;
			value += cur;
			tipTemp += "+ " + s + ": " + fmt(cur) + "<br>";
		}
		cur = (character.getAttributes().getAttribute("INT") - 10) * 0.5;
		value += cur;
		tipTemp += "+ " + "INT" + ": " + fmt(cur) + "<br>";

		tooltipFinal = "<html>Total Skill Value: " + fmt(value) + "<br>-------(Attributes)-------<br>" + tipTemp;
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
		CharAttributes attrs = character.getAttributes();
		String att = (selectedAtt == null || selectedAtt.isBlank() || "-".equals(selectedAtt)) ? "INT" : selectedAtt.toUpperCase();
		double mod = attrs.getAttribute(att);
		for (String k : ATTSHORT) {
			if (k.equalsIgnoreCase(att)) {
				mod -= 10;
				break;
			}
		}
		// Skill formula mirrors checkPressed
		mod *= 1.5;
		mod += ((attrs.getAttribute("INT") - 10) * 0.5);
		return "/roll d20 + " + fmt(mod);
	}

	private String buildSpecialtyTooltip(DataSpecialty spec) {
		if (spec == null) return "Description: unknown";
		String desc = spec.getDescription();
		if (desc == null || desc.isBlank()) desc = "No description available.";
		return "<html>" + desc.replace("\n", "<br>") + "</html>";
	}

	/** Checks if the provided attribute key matches the character class' primary attribute. */
	private boolean isPrimaryAttribute(String key) {
		if (key == null || character == null || character.getIdentity() == null || dataQuery == null) return false;
		DataClass cls = resolveClass();
		String primary = cls != null ? cls.getPrimaryAtt() : null;
		return primary != null && primary.equalsIgnoreCase(key);
	}

	/** Checks if the provided attribute key matches the character class' secondary attribute. */
	private boolean isSecondaryAttribute(String key) {
		if (key == null || character == null || character.getIdentity() == null || dataQuery == null) return false;
		DataClass cls = resolveClass();
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
		// Ensure specialties (including class/racial) are freshly synced before display
		if (character != null) {
			character.updateAll();
		}

		for (int i = specialtiesName.size(); i > 0; i--) {
			remove(specialtiesName.get(i-1));
		}
		
		specialtiesName = new ArrayList<JTextField>();

		if (character == null) {
			revalidate();
			repaint();
			return;
		}
		CharSpecials specials = character.getSpecials();
		if (specials == null) {
			revalidate();
			repaint();
			return;
		}

		DataSpecialty racial = specials.getRacialSpecialty();
		if (racial != null && racial.getName() != null && !isProficiencySpecialty(racial)) {
			JTextField tf = buildTextField(racial.getName()); // Racial
			tf.setForeground(new java.awt.Color(128, 0, 128)); // purple
			tf.setToolTipText(buildSpecialtyTooltip(racial));
			specialtiesName.add(tf);
		}

		for (DataSpecialty spec : specials.getClassSpecialties()) {
			if (spec != null && spec.getName() != null && !isProficiencySpecialty(spec)) {
				JTextField tf = buildTextField(spec.getName());
				tf.setForeground(new java.awt.Color(0, 128, 0)); // green
				tf.setToolTipText(buildSpecialtyTooltip(spec));
				specialtiesName.add(tf);
			}
		}

		for (DataSpecialty spec : specials.getTrainedSpecialties()) {
			if (spec != null && spec.getName() != null && !isProficiencySpecialty(spec)) {
				JTextField tf = buildTextField(spec.getName());
				tf.setForeground(new java.awt.Color(0, 0, 192)); // blue
				tf.setToolTipText(buildSpecialtyTooltip(spec));
				specialtiesName.add(tf);
			}
		}

		revalidate();
		repaint();
	}  /*--------------
		END UPDATESPECIALTIES
		--------------*/

	private boolean isProficiencySpecialty(DataSpecialty spec) {
		if (spec == null) return false;
		String category = spec.getCategory();
		if (category != null && (category.equalsIgnoreCase("Proficiency") || category.equalsIgnoreCase("Level"))) return true;
		String type = spec.getType();
		return type != null && (type.equalsIgnoreCase("Proficiency") || type.equalsIgnoreCase("Level"));
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

