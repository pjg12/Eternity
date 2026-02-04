package eternity;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

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
		
		resistStatL = buildTextField("Resist");
		attackStatL = buildTextField("Attack");
		moveStatL = buildTextField("Move");
		flyStatL = buildTextField("Fly");
		flyStatL.setVisible(false); // removed from utility display order
		rangeStatL = buildTextField("Range");
		initStatL = buildTextField("Init");
		
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
		pageHeight = 120;
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
		supStatL.setBounds(385, pageHeight, 60, 20);
		charSup.setBounds(450, pageHeight, 40, 20);
		pageHeight +=20;
		
		fortStatL.setBounds(5, pageHeight, 60, 20);
		charFort.setBounds(70, pageHeight, 40, 20);
		fortRoll.setBounds(115, pageHeight, 65, 19);
		dcStatL.setBounds(195, pageHeight, 60, 20);
		charDC.setBounds(260, pageHeight, 40, 20);
		dbStatL.setBounds(385, pageHeight, 60, 20);
		charDb.setBounds(450, pageHeight, 40, 20);
		pageHeight +=20;
		
		refStatL.setBounds(5, pageHeight, 60, 20);
		charRef.setBounds(70, pageHeight, 40, 20);
		refRoll.setBounds(115, pageHeight, 65, 19);
		moveStatL.setBounds(195, pageHeight, 60, 20);
		charMove.setBounds(260, pageHeight, 40, 20);
		grantStatL.setBounds(385, pageHeight, 60, 20);
		charGrant.setBounds(450, pageHeight, 40, 20);
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
		initStatL.setBounds(195, pageHeight, 60, 20);
		charInit.setBounds(260, pageHeight, 40, 20);
		initRoll.setBounds(305, pageHeight, 65, 19);
		bdmgStatL.setBounds(385, pageHeight, 60, 20);
		charBdmg.setBounds(450, pageHeight, 40, 20);
		pageHeight +=20;
		
		dodgeStatL.setBounds(5, pageHeight, 60, 20);
		charDodge.setBounds(70, pageHeight, 40, 20);
		maxattStatL.setBounds(195, pageHeight, 60, 20);
		charMaxatt.setBounds(260, pageHeight, 40, 20);
		bhealStatL.setBounds(385, pageHeight, 60, 20);
		charBheal.setBounds(450, pageHeight, 40, 20);
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

		// Basic identity fields
	    String displayName = (id.getNickname() != null && !id.getNickname().isBlank()) ? id.getNickname() : id.getName();
	    charName.setText(displayName);
	    campName.setText(id.getCampaign());
	    charLevel.setValue(id.getLevel());
	    charExp.setValue(id.getExp());
	    charClass.setText(id.getCharClass());
	    charRace.setText(id.getRace());
	    fitTextToField(charName, 8); // shrink if needed to fit box

	    // Size: if race has a size defined, use it; otherwise keep stored size
	    if (dq != null) {
	    	DataRace race = dq.getRaceByName(id.getRace());
	    	if (race != null && race.getSize() != null && !race.getSize().isBlank()) {
	    		id.setSize(race.getSize());
	    	}
	    }

	    // Simple tooltips using available data
	    charLevelL.setToolTipText("Level: " + id.getLevel());
	    charLevel.setToolTipText("Level: " + id.getLevel());
	    charExpL.setToolTipText("Experience: " + id.getExp());
	    charExp.setToolTipText("Experience: " + id.getExp());
	    charClassL.setToolTipText("Class: " + id.getCharClass());
	    charClass.setToolTipText("Class: " + id.getCharClass());
	    charRaceL.setToolTipText("Race: " + id.getRace());
	    charRace.setToolTipText("Race: " + id.getRace());

	    // Physical descriptors
		charGender.setText(id.getGender());
		charSize.setText(id.getSize());
		charAge.setValue(id.getAge());
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

			String tip = "<html>" + ATTRIBUTES[i] + ": " + val + " (mod " + mod + ")";
			attLabels[i].setToolTipText(tip);
			valFields[i].setToolTipText(tip);
			modFields[i].setToolTipText(tip);
			rollButtons[i].setToolTipText("/roll d20 + " + mod);
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

		charAC.setValue(ac);
		charArmor.setValue(armor);
		charDodge.setValue(dodge);
		charFort.setValue(fort);
		charRef.setValue(ref);
		charWill.setValue(will);

		acStatL.setToolTipText("AC: " + ac + " (Armor " + armor + ", Def " + def + ", Dodge " + dodge + ")");
		charAC.setToolTipText(acStatL.getToolTipText());
		armorStatL.setToolTipText("Armor: " + armor);
		charArmor.setToolTipText(armorStatL.getToolTipText());
		dodgeStatL.setToolTipText("Avoid: " + dodge + " (Dodge bonus)");
		charDodge.setToolTipText(dodgeStatL.getToolTipText());
		fortStatL.setToolTipText("Fortitude: " + fort);
		charFort.setToolTipText(fortStatL.getToolTipText());
		armorStatL.setToolTipText("Armor: " + armor + " (Def " + def + " included in AC)");
		refStatL.setToolTipText("Reflex: " + ref);
		charRef.setToolTipText(refStatL.getToolTipText());
		willStatL.setToolTipText("Will: " + will);
		charWill.setToolTipText(willStatL.getToolTipText());
		fortRoll.setToolTipText("/roll d20 + " + fort);
		refRoll.setToolTipText("/roll d20 + " + ref);
		willRoll.setToolTipText("/roll d20 + " + will);

		// Combat values
		int atk   = attrs.getCombat("ATK");
		int dc    = attrs.getCombat("APP");
		int move  = attrs.getCombat("MOVE");
		int fly   = attrs.getCombat("FLY");
		int range = attrs.getCombat("RANGE");
		int init  = attrs.getCombat("INIT");

		charAttack.setValue(atk);
		charDC.setValue(dc);
		charMove.setValue(move);
		charFly.setValue(fly);
		charRange.setValue(range);
		charInit.setValue(init);

		attackStatL.setToolTipText("Attack: " + atk);
		charAttack.setToolTipText(attackStatL.getToolTipText());
		dcStatL.setToolTipText("Apply: " + dc);
		charDC.setToolTipText(dcStatL.getToolTipText());
		moveStatL.setToolTipText("Move: " + move);
		charMove.setToolTipText(moveStatL.getToolTipText());
		flyStatL.setToolTipText("Fly: " + fly);
		charFly.setToolTipText(flyStatL.getToolTipText());
		rangeStatL.setToolTipText("Range: " + range);
		charRange.setToolTipText(rangeStatL.getToolTipText());
		initStatL.setToolTipText("Initiative: " + init);
		charInit.setToolTipText(initStatL.getToolTipText());
		initRoll.setToolTipText("/roll d20 + " + init);

		// Resist / secondary values
		int resistAll = attrs.getResist("ALL");
		int sup       = attrs.getSecondary("SUP");
		int imp       = attrs.getSecondary("IMP");
		int ben       = attrs.getSecondary("BEN");
		int excl      = attrs.getSecondary("EXCL");
		int maxAtk    = attrs.getSecondary("MAXATK");
		int baseDmg   = attrs.getDamage("BDMG");
		int baseHeal  = attrs.getDamage("BHEAL");
		double critMulti = character != null && character.getCombat() != null ? character.getCombat().getCritDamage() : 1.0;
		double crush   = attrs.getCombat("CRUSH");

		charResist.setValue(resistAll);
		charSup.setValue(critMulti);
		charDb.setValue(sup);
		charGrant.setValue(excl);
		charExcl.setValue(crush);
		charMaxatt.setValue(maxAtk);
		charBdmg.setValue(baseDmg);
		charBheal.setValue(baseHeal);

		resistStatL.setToolTipText("Resist All: " + resistAll);
		charResist.setToolTipText(resistStatL.getToolTipText());
		supStatL.setToolTipText("Crit Damage Multiplier: " + critMulti);
		charSup.setToolTipText(supStatL.getToolTipText());
		dbStatL.setToolTipText("Support: " + sup);
		charDb.setToolTipText(dbStatL.getToolTipText());
		grantStatL.setToolTipText("Area/Exclusion: " + excl);
		charGrant.setToolTipText(grantStatL.getToolTipText());
		exclStatL.setToolTipText("Crush: " + crush);
		charExcl.setToolTipText(exclStatL.getToolTipText());
		maxattStatL.setToolTipText("Max Attacks: " + maxAtk);
		charMaxatt.setToolTipText(maxattStatL.getToolTipText());
		bdmgStatL.setToolTipText("Base Damage: " + baseDmg);
		charBdmg.setToolTipText(bdmgStatL.getToolTipText());
		bhealStatL.setToolTipText("Base Heal: " + baseHeal);
		charBheal.setToolTipText(bhealStatL.getToolTipText());
	}  /*--------------
		END UPDATESTATISTICS
		--------------*/
	
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
			skillsAtt.add(buildTextField(tempAtt));
			
			String tempName = tempSkill.getName();
			skillsName.add(buildTextField(tempName));
		
			skillsRoll.add(buildCheckButton(tempName + " Check", true, tempAtt));
			add(skillsAtt.get(i));
			add(skillsName.get(i));
			add(skillsRoll.get(i));
		}
		revalidate();
		repaint();
	}  /*--------------
		END UPDATESKILLS
		--------------*/
	
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
		if (racial != null && racial.getName() != null) {
			JTextField tf = buildTextField(racial.getName()); // Racial
			tf.setForeground(new java.awt.Color(128, 0, 128)); // purple
			specialtiesName.add(tf);
		}

		for (DataSpecialty spec : specials.getClassSpecialties()) {
			if (spec != null && spec.getName() != null) {
				JTextField tf = buildTextField(spec.getName());
				tf.setForeground(new java.awt.Color(0, 128, 0)); // green
				specialtiesName.add(tf);
			}
		}

		for (DataSpecialty spec : specials.getTrainedSpecialties()) {
			if (spec != null && spec.getName() != null) {
				JTextField tf = buildTextField(spec.getName());
				tf.setForeground(new java.awt.Color(0, 0, 192)); // blue
				specialtiesName.add(tf);
			}
		}

		revalidate();
		repaint();
	}  /*--------------
		END UPDATESPECIALTIES
		--------------*/

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
