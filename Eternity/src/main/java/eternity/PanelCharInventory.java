package eternity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/*
 * 		INVENTORY PANEL
 */
public class PanelCharInventory extends PanelCharBase {
	private static final long serialVersionUID = 1L;

	private JLabel currencyL, weapProfL, armorProfL;
	private JTextArea charCurrency, charWeapProf;
	private JTextPane charArmorProf;
	private JScrollPane currencyPane, weapProfPane, armorProfPane;

	private BufferedImage dollPic;
	private JLabel dollLabel; 
	private ArrayList<JLabel> equipL;  //headL, shoulderL, chestL, waistL, legsL, feetL, handsL, backL, fingerRL, fingerLL, neckL, trinkL, w1L, w2L, w3L, w4L;
	private ArrayList<JComboBox<DataItemEquipment>> equipped; // equipHead, equipShoulder, equipChest, equipWaist, equipLegs, equipFeet, equipHands, equipBack, equipFingerR, equipFingerL, equipNeck, equipTrinket, equipW1, equipW2, equipW3, equipW4;
	private boolean enforcingHeavy = false;
	private boolean suppressEquipAutoSave = false;
	
	private JLabel equipmentL;
	private ArrayList<JLabel> equipmentNameL, equipmentTierL, equipmentCatL, equipmentEquippedL, equipmentEnchL, equipmentGemL, equipmentStorL, equipmentOilL, equipmentModL, equipmentAugL;
	private ArrayList<ArrayList<JTextField>> equipmentName, equipmentCat;
	private ArrayList<ArrayList<JCheckBox>> equipmentEquipped;
	private ArrayList<ArrayList<JCheckBox>> equipmentEnch, equipmentGem, equipmentStor, equipmentOil, equipmentMod, equipmentAug;
	private ArrayList<ArrayList<JFormattedTextField>> equipmentTier;
	
	private JLabel consumableL;
	private JLabel consumableNameL, consumableQtyL, consumableNoteL;
	private ArrayList<JTextField> consumableName, consumableNote;
	private ArrayList<JFormattedTextField> consumableQty;
	
	private JLabel goodsL;
	private JLabel goodsNameL, goodsQtyL, goodsNoteL;
	private ArrayList<JTextField> goodsName, goodsNote;
	private ArrayList<JFormattedTextField> goodsQty;
	
	private JLabel itemsL;
	private JLabel itemsNameL, itemsQtyL, itemsNoteL;
	private ArrayList<JTextField> itemsName, itemsNote;
	private ArrayList<JFormattedTextField> itemsQty;
	
	private JLabel inventoryL, inventoryNameL, inventoryQuanL, inventoryGemL, inventoryEnchantL, inventoryStoreL, inventoryCatL;
	private ArrayList<JTextField> invenName, invenNote, invenStore, invenCat, invenGem, invenEnchant, natAffinity;
	private ArrayList<JFormattedTextField> invenQuan;
	private ArrayList<JLabel> invenNoteL;
	private JButton changeInvenButton, removeInvenButton;

	private JLabel currencyLabel;
	private JLabel currencyValue;
	private JLabel weaponProfLabel;
	private JLabel weaponProfValue;
	private JLabel armorProfLabel;
	private JLabel armorProfValue;
	private JButton saveButton;
	private final Timer equipSaveDebounceTimer;
	private String loadedDollName = "";
	private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
	private String cachedEquipmentSignature = "";
	
	private final String[] SLOTS = {"Head", "Neck", "Shoulders", "Back", "Chest", "Trinket", "Hands", "Waist", "Right Finger", "Left Finger", "Legs", "Feet", "Weapon 1", "Weapon 2", "Weapon 3", "Weapon 4"};

	/*
	 * 		DEFAULT CONSTRUCTOR
	 */
	PanelCharInventory (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(255, 255, 204));
		equipSaveDebounceTimer = new Timer(350, e -> {
			if (character == null) return;
			StoreCharData toSave = character;
			/*Thread saver = new Thread(() -> StoreMetaManager.saveCharacter(toSave), "equip-auto-save");
			saver.setDaemon(true);
			saver.start();*/
		});
		equipSaveDebounceTimer.setRepeats(false);

		/*	
		 * 	Currency
		 */
		currencyL = buildLabel("Currency", null);
		charCurrency = buildTextArea("-");
		charCurrency.setEditable(true);
		currencyPane = buildScrollPane(charCurrency);

		armorProfL = buildLabel("Armor Proficiency", null);
		charArmorProf = new JTextPane();
		charArmorProf.setText("-");
		charArmorProf.setEditable(false);
		StyledDocument armorDoc = charArmorProf.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		armorDoc.setParagraphAttributes(0, armorDoc.getLength(), center, false);
		armorProfPane = buildScrollPane(charArmorProf);
		armorProfPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		weapProfL = buildLabel("Weapon Proficiency", null);
		charWeapProf = buildTextArea("-");
		charWeapProf.setEditable(false);
		weapProfPane = buildScrollPane(charWeapProf);

		/*
		 * 	Doll Equipment
		 */
		equipL = new ArrayList<JLabel>(); 
		JLabel tempLabel;
		for (int i = 0; i < 16; i++) {
			tempLabel = buildLabel(SLOTS[i], null);
			equipL.add(tempLabel);
		}

		equipped = new ArrayList<JComboBox<DataItemEquipment>>();
		JComboBox<DataItemEquipment> tempBox;
		for (int i = 0; i < 16; i++) {
			tempBox = buildEquipBox();
			tempBox.addActionListener(e -> {
				if (suppressEquipAutoSave) return;
				autoSaveEquipmentSelection();
			});
			equipped.add(tempBox);
		}
		// Weapon 1 change listener controls visibility of Weapon 4 when using Heavy weapons
		equipped.get(12).addActionListener(e -> applyWeaponFourRule());
		// Weapon 2 also participates in Heavy check
		equipped.get(13).addActionListener(e -> applyWeaponFourRule());
		// Keep heavy weapons in slots 1/2 when selections change
		equipped.get(12).addActionListener(e -> enforceHeavyPlacement());
		equipped.get(13).addActionListener(e -> enforceHeavyPlacement());
		equipped.get(14).addActionListener(e -> enforceHeavyPlacement());
		equipped.get(15).addActionListener(e -> enforceHeavyPlacement());

		dollLabel = new JLabel("<html><center>Image Not Found<br>If you are using a gender<br>that is neither 'Male' nor 'Female'<br>the doll image will not display.</center></html>", SwingConstants.CENTER);
    	add(dollLabel);
    	dollLabel.setHorizontalAlignment(JLabel.CENTER);

    	// Summary row at top
    	currencyLabel = buildLabel("Currency", null);
    	currencyValue = buildLabel("-", null);
    	weaponProfLabel = buildLabel("Weapon Proficiencies", null);
    	weaponProfValue = buildLabel("-", null);
    	armorProfLabel = buildLabel("Armor Proficiency", null);
    	armorProfValue = buildLabel("-", null);
    	saveButton = buildButton("Save Currency");
    	add(currencyLabel);
    	add(currencyValue);
    	add(weaponProfLabel);
    	add(weaponProfValue);
    	add(armorProfLabel);
    	add(armorProfValue);
    	add(saveButton);

    	saveButton.addActionListener(e -> {
    		if (character != null) {
    			//StoreMetaManager.saveCharacter(character);
    		}
    	});
	    
    	equipmentL = buildLabel("Equipment", null);
    	
    	equipmentNameL = new ArrayList<JLabel>();
    	equipmentTierL = new ArrayList<JLabel>();
    	equipmentCatL = new ArrayList<JLabel>();
    	equipmentEquippedL = new ArrayList<JLabel>();
    	equipmentEnchL = new ArrayList<JLabel>();
    	equipmentGemL = new ArrayList<JLabel>();
    	equipmentStorL = new ArrayList<JLabel>();
    	equipmentOilL = new ArrayList<JLabel>();
    	equipmentModL = new ArrayList<JLabel>();
    	equipmentAugL = new ArrayList<JLabel>();
    	
    	equipmentNameL.add(buildLabel("Weapon", null));
    	equipmentNameL.add(buildLabel("Armor", null));
    	equipmentNameL.add(buildLabel("Accessory", null));
    	
    	for (int i = 0; i < 3; i++) {
	    	equipmentTierL.add(buildLabel("Tier", null));
	    	equipmentCatL.add(buildLabel("Category", null));
	    	equipmentEquippedL.add(buildLabel("Equip", null));
	    	equipmentEnchL.add(buildLabel("Ench", null));
	    	equipmentGemL.add(buildLabel("Gem", null));
	    	equipmentStorL.add(buildLabel("Stor", null));
	    	equipmentOilL.add(buildLabel("Oil", null));
	    	equipmentModL.add(buildLabel("Mod", null));
	    	equipmentAugL.add(buildLabel("Aug", null));
    	}

    	equipmentName = new ArrayList<ArrayList<JTextField>>();
    	equipmentTier = new ArrayList<ArrayList<JFormattedTextField>>();
    	equipmentCat = new ArrayList<ArrayList<JTextField>>();
    	equipmentEquipped = new ArrayList<ArrayList<JCheckBox>>();
    	equipmentEnch = new ArrayList<ArrayList<JCheckBox>>();
    	equipmentGem = new ArrayList<ArrayList<JCheckBox>>();
    	equipmentStor = new ArrayList<ArrayList<JCheckBox>>();
    	equipmentOil = new ArrayList<ArrayList<JCheckBox>>();
		equipmentMod = new ArrayList<ArrayList<JCheckBox>>();
		equipmentAug = new ArrayList<ArrayList<JCheckBox>>();
		
		for (int i = 0; i < 3; i++) {
	    	equipmentName.add(new ArrayList<JTextField>());
	    	equipmentTier.add(new ArrayList<JFormattedTextField>());
	    	equipmentCat.add(new ArrayList<JTextField>());
	    	equipmentEquipped.add(new ArrayList<JCheckBox>());
	    	equipmentEnch.add(new ArrayList<JCheckBox>());
	    	equipmentGem.add(new ArrayList<JCheckBox>());
	    	equipmentStor.add(new ArrayList<JCheckBox>());
	    	equipmentOil.add(new ArrayList<JCheckBox>());
	    	equipmentMod.add(new ArrayList<JCheckBox>());
	    	equipmentAug.add(new ArrayList<JCheckBox>());
    	}
    	
    	consumableL = buildLabel("Consumables", null);
    	consumableNameL = buildLabel("Name", null);
    	consumableQtyL = buildLabel("Qty", null);
    	consumableNoteL = buildLabel("Note", null);
    	consumableName = new ArrayList<>();
    	consumableQty = new ArrayList<>();
    	consumableNote = new ArrayList<>();
    	
    	goodsL = buildLabel("Goods", null);
    	goodsNameL = buildLabel("Name", null);
    	goodsQtyL = buildLabel("Qty", null);
    	goodsNoteL = buildLabel("Note", null);
    	goodsName = new ArrayList<>();
    	goodsQty = new ArrayList<>();
    	goodsNote = new ArrayList<>();
    	
    	itemsL = buildLabel("Items", null);
    	itemsNameL = buildLabel("Name", null);
    	itemsQtyL = buildLabel("Qty", null);
    	itemsNoteL = buildLabel("Note", null);
    	itemsName = new ArrayList<>();
    	itemsQty = new ArrayList<>();
    	itemsNote = new ArrayList<>();
    	
		/*
		 * 	Inventory
		 */	
    	inventoryL = buildLabel("Inventory", null);

		inventoryNameL = buildLabel("Name", null);
		inventoryGemL = buildLabel("Gem", null);
		inventoryQuanL = buildLabel("Quantity", null);
		inventoryEnchantL = buildLabel("Enchant", null);
		inventoryStoreL = buildLabel("Storage", null);
		inventoryCatL = buildLabel("Category", null);
		
		invenName = new ArrayList<JTextField>();
		invenNote = new ArrayList<JTextField>();
		invenStore = new ArrayList<JTextField>();
		invenCat = new ArrayList<JTextField>();
		invenQuan = new ArrayList<JFormattedTextField>();
		invenGem = new ArrayList<JTextField>();
		invenEnchant = new ArrayList<JTextField>();
		invenNoteL = new ArrayList<JLabel>();
		
		changeInvenButton = buildButton("Add Item");
		changeInvenButton.addActionListener (e -> sheetFrame.inventoryCharacter());
		removeInvenButton = buildButton("Remove Item");
		removeInvenButton.addActionListener (e -> sheetFrame.removeInventoryCharacter());
		
		/*
		 * 	Updates
		 */	
	  //  updateData();
	 //   resizeSheet();
	}  /*--------------
		END DEFAULTCONSTRUCTOR
		--------------*/
	
	/*
	 * 		RESIZE SHEET
	 */
	public void resizeSheet() {
		pageHeight = resizeHeader();

		currencyL.setBounds(5,pageHeight,225,19);
		armorProfL.setBounds(235,pageHeight,158,19);
		weapProfL.setBounds(400,pageHeight,160,19);
		
		pageHeight += 20;
		currencyPane.setBounds(5,pageHeight,225, 80);
		armorProfPane.setBounds(235,pageHeight,158, 25);
		weapProfPane.setBounds(400,pageHeight,160, 80);
		pageHeight += 48;

		saveButton.setBounds(235, pageHeight, 158, 30);
		pageHeight += 27;
		
		dollLabel.setBounds(20, pageHeight + 10, 525, 500);
		pageHeight += 20;
		
		for (int i = 0; i < 6; i++) {
			int x = 0;
			if (i == 1 || i == 4) {
				x = -15;
			}
			else if (i == 2 || i == 3) {
				x = -30;
			}
			equipL.get(2*i).setBounds(60+x, pageHeight, 175, 20);
			equipL.get(2*i + 1).setBounds(330-x, pageHeight, 175, 20);
			pageHeight += 20;
			equipped.get(2*i).setBounds(60+x, pageHeight, 175, 20);
			equipped.get(2*i + 1).setBounds(330-x, pageHeight, 175, 20);
			pageHeight += 45;
		}
		
		for (int i = 0; i < 2; i++) {
			equipL.get(2*i + 12).setBounds(60, pageHeight, 175, 20);
			equipL.get(2*i + 13).setBounds(330, pageHeight, 175, 20);
			pageHeight += 20;	
			equipped.get(2*i + 12).setBounds(60, pageHeight, 175, 20);
			equipped.get(2*i + 13).setBounds(330, pageHeight, 175, 20);
			pageHeight += 30;
		}
		pageHeight += 5;	

			//move buttons
			pageHeight += 5;
			//currencyL.setBounds(5,pageHeight,210,19);
			changeInvenButton.setBounds(160,pageHeight,125,29);
			removeInvenButton.setBounds(295,pageHeight,125,29);
			pageHeight += 35;
		
		equipmentL.setBounds(5, pageHeight, 555, 20);	///////////////////////////////////////////////////////////////
		pageHeight += 20;
		
		for (int i = 0; i < 3; i++) {
	    	equipmentNameL.get(i).setBounds(5, pageHeight, 120, 20);
	    	equipmentCatL.get(i).setBounds(130, pageHeight, 135, 20);
	    	equipmentTierL.get(i).setBounds(270, pageHeight, 30, 20);
	    	equipmentEquippedL.get(i).setBounds(305, pageHeight, 40, 20);
	    	equipmentEnchL.get(i).setBounds(350, pageHeight, 30, 20);
	    	equipmentGemL.get(i).setBounds(385, pageHeight, 30, 20);
	    	equipmentStorL.get(i).setBounds(420, pageHeight, 30, 20);
	    	equipmentOilL.get(i).setBounds(455, pageHeight, 30, 20);
	    	equipmentModL.get(i).setBounds(490, pageHeight, 30, 20);
	    	equipmentAugL.get(i).setBounds(525, pageHeight, 30, 20);
			pageHeight += 20;
			
			for (int j = 0; j < equipmentName.get(i).size(); j++) {
				equipmentName.get(i).get(j).setBounds(5, pageHeight, 120, 20);
		    	equipmentCat.get(i).get(j).setBounds(130, pageHeight, 135, 20);
		    	equipmentTier.get(i).get(j).setBounds(270, pageHeight, 30, 20);
		    	equipmentEquipped.get(i).get(j).setBounds(305, pageHeight, 40, 20);
		    	equipmentEnch.get(i).get(j).setBounds(350, pageHeight, 30, 20);
		    	equipmentGem.get(i).get(j).setBounds(385, pageHeight, 30, 20);
		    	equipmentStor.get(i).get(j).setBounds(420, pageHeight, 30, 20);
		    	equipmentOil.get(i).get(j).setBounds(455, pageHeight, 30, 20);
		    	equipmentMod.get(i).get(j).setBounds(490, pageHeight, 30, 20);
		    	equipmentAug.get(i).get(j).setBounds(525, pageHeight, 30, 20);
				pageHeight += 20;
			}
		}
		
		
		// Consumables section
		pageHeight += 10;
		consumableL.setBounds(5, pageHeight, 555, 20);
		pageHeight += 20;
		consumableNameL.setBounds(5, pageHeight, 200, 20);
		consumableQtyL.setBounds(210, pageHeight, 60, 20);
		consumableNoteL.setBounds(275, pageHeight, 285, 20);
		pageHeight += 20;
		for (int i = 0; i < consumableName.size(); i++) {
			consumableName.get(i).setBounds(5, pageHeight, 200, 20);
			consumableQty.get(i).setBounds(210, pageHeight, 60, 20);
			consumableNote.get(i).setBounds(275, pageHeight, 285, 20);
			pageHeight += 20;
		}
		
		// Goods section
		pageHeight += 10;
		goodsL.setBounds(5, pageHeight, 555, 20);
		pageHeight += 20;
		goodsNameL.setBounds(5, pageHeight, 200, 20);
		goodsQtyL.setBounds(210, pageHeight, 60, 20);
		goodsNoteL.setBounds(275, pageHeight, 285, 20);
		pageHeight += 20;
		for (int i = 0; i < goodsName.size(); i++) {
			goodsName.get(i).setBounds(5, pageHeight, 200, 20);
			goodsQty.get(i).setBounds(210, pageHeight, 60, 20);
			goodsNote.get(i).setBounds(275, pageHeight, 285, 20);
			pageHeight += 20;
		}
		
		// Items section
		pageHeight += 10;
		itemsL.setBounds(5, pageHeight, 555, 20);
		pageHeight += 20;
		itemsNameL.setBounds(5, pageHeight, 260, 20);
		itemsQtyL.setVisible(false);
		itemsNoteL.setBounds(270, pageHeight, 290, 20);
		pageHeight += 20;
		for (int i = 0; i < itemsName.size(); i++) {
			itemsName.get(i).setBounds(5, pageHeight, 260, 20);
			itemsQty.get(i).setVisible(false);
			itemsNote.get(i).setBounds(270, pageHeight, 290, 20);
			pageHeight += 20;
		}
		
			
		pageHeight += 10;
		
			/**************
			* ***********		Add Exp, Edit Character
			*/// ***********	
			
	
		/*
		 * Set Window Size
		 */	
		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}  /*--------------
		END RESIZESHEET
		--------------*/
	
	/*
	 * 		BUILD EQUIPBOX
	 */
	public JComboBox<DataItemEquipment> buildEquipBox () {
		JComboBox<DataItemEquipment> tempBox = new JComboBox<DataItemEquipment>();
		DataItemEquipment tempEquip = new DataItemEquipment();
		tempEquip.setDname("*** Empty ***");
		tempBox.addItem(tempEquip);
		
		// Color and alignment: all options blue/center except placeholder
		tempBox.setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer() {
			private final java.awt.Color BLUE = new java.awt.Color(0, 102, 204);
		

			@Override
			public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (c instanceof javax.swing.JLabel && value instanceof DataItemEquipment) {
					javax.swing.JLabel lbl = (javax.swing.JLabel) c;
					DataItemEquipment eq = (DataItemEquipment) value;
					boolean isEmpty = eq.getDname() != null && eq.getDname().equals("*** Empty ***");
					lbl.setHorizontalAlignment(isEmpty ? javax.swing.SwingConstants.LEFT : javax.swing.SwingConstants.CENTER);
					
					// Closed (index == -1) or non-empty entries should appear blue even when combo isn't focused
					if (isEmpty) {
						lbl.setForeground(java.awt.Color.BLACK);
					} else {
						lbl.setForeground(BLUE);
					}
				}
				return c;
			}
		});

		add(tempBox);
		
		return tempBox;
	}  /*--------------
		END BUILDEQUIPBOX
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
	public void updateAll() {
		updateSummary();
		updateDoll();
		updateEquipment();
		updateConsumables();
		updateGoods();
		updateItems();
		resizeSheet();
		enforceReadOnlyChecks();
		enforceHeavyPlacement();
				updateDollLists();
	}  /*--------------
		END UPDATEALL
		--------------*/

	@Override
	public void updateCharacter(StoreCharData character) {
		super.updateCharacter(character);
		enforceReadOnlyChecks();
		enforceHeavyPlacement();
	}

	private void updateSummary() {
		CharInventory inv = character != null ? character.getInventory() : null;

		if (inv == null) {
			currencyValue.setText("-");
			weaponProfValue.setText("-");
			armorProfValue.setText("-");
			return;
		}

		currencyValue.setText(CURRENCY_FORMAT.format(inv.getCredits()));

		List<String> wp = inv.getWeaponProficiencies();
		if ((wp == null || wp.isEmpty()) && dataQuery != null && character != null && character.getIdentity() != null) {
			String cls = character.getIdentity().getCharClass();
			if (cls != null && !cls.isBlank()) {
				DataClass dataClass = dataQuery.getClassByName(cls);
				if (dataClass != null && dataClass.getProfAuto() != null && !dataClass.getProfAuto().isEmpty()) {
					wp = new ArrayList<>(dataClass.getProfAuto());
					inv.setWeaponProficiencies(wp); // hydrate inventory with inherited profs
				}
			}
		}

		weaponProfValue.setText((wp == null || wp.isEmpty()) ? "-" : String.join(", ", wp));
		// Also show in the detailed text area
		if (charWeapProf != null) {
			if (wp == null || wp.isEmpty()) {
				charWeapProf.setText("-");
			} else {
				charWeapProf.setText(String.join("\n", wp));
			}
		}

		String armor = inv.getArmor();
		if ((armor == null || armor.isBlank()) && character != null && character.getIdentity() != null) {
			String cls = character.getIdentity().getCharClass();
			if (cls != null && !cls.isBlank() && dataQuery != null) {
				DataClass dataClass = dataQuery.getClassByName(cls);
				if (dataClass != null && dataClass.getArmor() != null && !dataClass.getArmor().isBlank()) {
					armor = dataClass.getArmor();
				}
			}
		}
		String armorText = (armor == null || armor.isBlank()) ? "-" : armor;
		armorProfValue.setText(armorText);

		// Also mirror into armorProficiency pane (centered)
		charArmorProf.setText(armorText);
		StyledDocument doc = charArmorProf.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
	}
	
	/*
	 * 		UPDATE DOLL LISTS
	 */
	public void updateDollLists() {
		java.awt.Color BLUE = new java.awt.Color(0, 102, 204);
		for (JComboBox<DataItemEquipment> tempBox : equipped) {
			if (!((DataItemEquipment) tempBox.getSelectedItem()).getDname().equals("*** Empty ***")) tempBox.setForeground(BLUE);
			else tempBox.setForeground(java.awt.Color.BLACK);
		}
		
		
		// TODO
	}  /*--------------
		END UPDATEDOLLLISTS
		--------------*/
	
	/*
	 * 		UPDATE DOLL
	 */	
	public void updateDoll() {
		CharIdentity id = character.getIdentity();
		String dollName = (id != null && id.getGender() != null && !id.getGender().isBlank())
				? id.getGender()
				: "default";

		if (!dollName.equalsIgnoreCase(loadedDollName)) {
			loadedDollName = dollName;
			try {
				dollPic = ImageIO.read(new File("images/" + dollName + ".jpg"));
				dollLabel.setIcon(new ImageIcon(dollPic));
				dollLabel.setText(null);
			} catch (Exception e) {
				dollPic = null;
				dollLabel.setIcon(null);
				dollLabel.setText("<html><center><br><br><br><br>Image Not Found<br>If you are using a gender<br>that is neither 'Male' nor 'Female'<br>the doll image will not display<br>without a jpg file<br>in the Images folder<br>with the same name<br>as your chosen gender.</center></html>");
			}
		}
		updateDollLists();
	}  /*--------------
		END UPDATEDOLL
		--------------*/
	
	/*
	 * 		UPDATE EQUIPMENT
	 */	
	public void updateEquipment() {
		suppressEquipAutoSave = true;
		try {
			ArrayList<ArrayList<DataItemEquipment>> groupedEquipment = buildEquipmentGroups();
			for (int i = 0; i < 3; i++) {
				ArrayList<DataItemEquipment> items = groupedEquipment.get(i);
				int visibleRows = Math.max(1, items.size());
				ensureEquipmentCategoryCapacity(i, visibleRows);
				if (items.isEmpty()) {
					bindEquipmentRow(i, 0, null);
				} else {
					for (int j = 0; j < items.size(); j++) {
						bindEquipmentRow(i, j, items.get(j));
					}
				}
				hideUnusedEquipmentRows(i, visibleRows);
			}

			String newSignature = buildEquipmentSignature(groupedEquipment);
			if (!newSignature.equals(cachedEquipmentSignature)) {
				updateEquipLists(groupedEquipment);
				cachedEquipmentSignature = newSignature;
			} else {
				refreshSelectedEquipFlags(groupedEquipment);
			}
			applyWeaponFourRule();
		} finally {
			suppressEquipAutoSave = false;
		}
	}  /*--------------
		END UPDATEEQUIPMENT
		--------------*/

	private JCheckBox buildFlagCheck(boolean checked) {
		JCheckBox box = new JCheckBox();
		box.setSelected(checked);
		box.setEnabled(false);
		box.setFocusable(false);
		box.setHorizontalAlignment(SwingConstants.CENTER);
		box.setOpaque(true);
		box.setBackground(alternate ? Color.WHITE : Color.LIGHT_GRAY);
		add(box);
		return box;
	}
	
	/** Ensures all flag checkboxes remain non-interactive for the user. */
	public void enforceReadOnlyChecks() {
		disableChecks(equipmentEquipped);
		disableChecks(equipmentEnch);
		disableChecks(equipmentGem);
		disableChecks(equipmentStor);
		disableChecks(equipmentOil);
		disableChecks(equipmentMod);
		disableChecks(equipmentAug);
	}

	private void disableChecks(ArrayList<ArrayList<JCheckBox>> groups) {
		if (groups == null) return;
		for (ArrayList<JCheckBox> row : groups) {
			if (row == null) continue;
			for (JCheckBox box : row) {
				if (box == null) continue;
				box.setEnabled(false);
				box.setFocusable(false);
			}
		}
	}
	
	/*
	 * 		UPDATE CONSUMABLES
	 */
	public void updateConsumables() {
		CharInventory inv = character != null ? character.getInventory() : null;
		List<DataItem> list = inv != null ? inv.getConsumables() : List.of();
		
		if (list.isEmpty()) {
			ensureSimpleRowCapacity(consumableName, consumableQty, consumableNote, 1);
			bindSimpleItemRow(consumableName, consumableQty, consumableNote, 0, "-", 0, "-");
			hideUnusedSimpleRows(consumableName, consumableQty, consumableNote, 1);
			return;
		}
		
		ensureSimpleRowCapacity(consumableName, consumableQty, consumableNote, list.size());
		for (int i = 0; i < list.size(); i++) {
			DataItem item = list.get(i);
			String name = (item.getIname() != null && !item.getIname().isBlank()) ? item.getIname() : item.getDname();
			String note = (item.getInote() != null && !item.getInote().isBlank()) ? item.getInote() : item.getDnote();
			bindSimpleItemRow(consumableName, consumableQty, consumableNote, i, name == null || name.isBlank() ? "-" : name, item.getQuantity(), note == null ? "" : note);
		}
		hideUnusedSimpleRows(consumableName, consumableQty, consumableNote, list.size());
	}
	
	/*
	 * 		UPDATE GOODS
	 */
	public void updateGoods() {
		CharInventory inv = character != null ? character.getInventory() : null;
		List<DataItem> list = inv != null ? inv.getGoods() : List.of();
		
		if (list.isEmpty()) {
			ensureSimpleRowCapacity(goodsName, goodsQty, goodsNote, 1);
			bindSimpleItemRow(goodsName, goodsQty, goodsNote, 0, "-", 0, "-");
			hideUnusedSimpleRows(goodsName, goodsQty, goodsNote, 1);
			return;
		}
		
		ensureSimpleRowCapacity(goodsName, goodsQty, goodsNote, list.size());
		for (int i = 0; i < list.size(); i++) {
			DataItem item = list.get(i);
			String name = (item.getIname() != null && !item.getIname().isBlank()) ? item.getIname() : item.getDname();
			String note = (item.getInote() != null && !item.getInote().isBlank()) ? item.getInote() : item.getDnote();
			bindSimpleItemRow(goodsName, goodsQty, goodsNote, i, name == null || name.isBlank() ? "-" : name, item.getQuantity(), note == null ? "" : note);
		}
		hideUnusedSimpleRows(goodsName, goodsQty, goodsNote, list.size());
	}
	
	/*
	 * 		UPDATE ITEMS
	 */
	public void updateItems() {
		CharInventory inv = character != null ? character.getInventory() : null;
		List<DataItem> list = inv != null ? inv.getItems() : List.of();
		
		if (list.isEmpty()) {
			ensureSimpleRowCapacity(itemsName, itemsQty, itemsNote, 1);
			bindSimpleItemRow(itemsName, itemsQty, itemsNote, 0, "-", 0, "-");
			hideUnusedSimpleRows(itemsName, itemsQty, itemsNote, 1);
			return;
		}
		
		ensureSimpleRowCapacity(itemsName, itemsQty, itemsNote, list.size());
		for (int i = 0; i < list.size(); i++) {
			DataItem item = list.get(i);
			String name = (item.getIname() != null && !item.getIname().isBlank()) ? item.getIname() : item.getDname();
			String note = (item.getInote() != null && !item.getInote().isBlank()) ? item.getInote() : item.getDnote();
			bindSimpleItemRow(itemsName, itemsQty, itemsNote, i, name == null || name.isBlank() ? "-" : name, item.getQuantity(), note == null ? "" : note);
		}
		hideUnusedSimpleRows(itemsName, itemsQty, itemsNote, list.size());
	}
	
	
	public void updateEquipLists () {
		updateEquipLists(buildEquipmentGroups());
	}

	private void updateEquipLists(ArrayList<ArrayList<DataItemEquipment>> groupedEquipment) {
		ArrayList<DataItemEquipment> tempList = new ArrayList<DataItemEquipment>();
		ArrayList<DataItemEquipment> tempWeapons = groupedEquipment.get(0);
		ArrayList<DataItemEquipment> tempArmor = groupedEquipment.get(1);
		ArrayList<DataItemEquipment> tempAccessories = groupedEquipment.get(2);
		
		for (int i = 0; i < 16; i++) {
			tempList.add((DataItemEquipment)equipped.get(i).getSelectedItem());
			equipped.get(i).removeAllItems();
			
			DataItemEquipment tempEquip = new DataItemEquipment();
			tempEquip.setDname("*** Empty ***");
			equipped.get(i).addItem(tempEquip);
		}
		
		for (int i = 0; i < tempWeapons.size(); i++) {
			if (!isItemEligibleByLevel(tempWeapons.get(i))) continue;
			for (int j = 12; j < 16; j++) {
				equipped.get(j).addItem(tempWeapons.get(i));
			}
		}
		// Select equipped weapons into the first available weapon slots
		int weaponSlot = 12;
		for (DataItemEquipment w : tempWeapons) {
			if (w.isEquipped() && weaponSlot < 16) {
				equipped.get(weaponSlot).setSelectedItem(w);
				weaponSlot++;
			}
		}
		
		for (int i = 0; i < tempArmor.size(); i++) {
			if (!isItemEligibleByLevel(tempArmor.get(i))) continue;
			String tempSlot = tempArmor.get(i).getSlot();
			if (tempSlot.compareTo("Head") == 0) {
				equipped.get(0).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(0).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Shoulders") == 0) {
				equipped.get(2).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(2).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Chest") == 0) {
				equipped.get(4).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(4).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Hands") == 0) {
				equipped.get(6).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(6).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Waist") == 0) {
				equipped.get(7).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(7).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Legs") == 0) {
				equipped.get(10).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(10).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Feet") == 0) {
				equipped.get(11).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(11).setSelectedItem(tempArmor.get(i));
			}
		}
		
		for (int i = 0; i < tempAccessories.size(); i++) {
			if (!isItemEligibleByLevel(tempAccessories.get(i))) continue;
			String tempSlot = tempAccessories.get(i).getSlot();
			if (tempSlot.compareTo("Neck") == 0) {
				equipped.get(1).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(1).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Back") == 0) {
				equipped.get(3).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(3).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Trinket") == 0) {
				equipped.get(5).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(5).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Right Finger") == 0) {
				equipped.get(8).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(8).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Left Finger") == 0) {
				equipped.get(9).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(9).setSelectedItem(tempAccessories.get(i));
			}
		}
	}

	private boolean isItemEligibleByLevel(DataItemEquipment item) {
		if (item == null) return false;
		if (character == null || character.getIdentity() == null) return true;
		return item.getLevelReq() <= character.getIdentity().getLevel();
	}
	
	/** Hides/shows Weapon 3 and 4 based on whether Weapon 1 or 2 is Heavy. */
	private void applyWeaponFourRule() {
		if (equipped.size() < 16 || equipL.size() < 16) return;
		var w1 = equipped.get(12).getSelectedItem();
		var w2 = equipped.get(13).getSelectedItem();
		boolean heavy1 = false;
		boolean heavy2 = false;

		if (w1 instanceof DataItemEquipment equip) {
			String slot = equip.getSlot();
			String cat = equip.getCategory();
			if ((slot != null && slot.toLowerCase().contains("heavy")) ||
				(cat != null && cat.toLowerCase().contains("heavy"))) {
				heavy1 = true;
			}
		}
		if (w2 instanceof DataItemEquipment equip2) {
			String slot2 = equip2.getSlot();
			String cat2 = equip2.getCategory();
			if ((slot2 != null && slot2.toLowerCase().contains("heavy")) ||
				(cat2 != null && cat2.toLowerCase().contains("heavy"))) {
				heavy2 = true;
			}
		}

		boolean anyHeavy = heavy1 || heavy2;
		boolean bothHeavy = heavy1 && heavy2;

		if (anyHeavy) {
			if (equipped.get(15).getItemCount() > 0) {
				equipped.get(15).setSelectedIndex(0); // empty selection
			}
			equipL.get(15).setVisible(false);
			equipped.get(15).setVisible(false);
		} else {
			equipL.get(15).setVisible(true);
			equipped.get(15).setVisible(true);
		}

		// Weapon 3 hides only when both weapons are heavy
		if (bothHeavy) {
			if (equipped.get(14).getItemCount() > 0) {
				equipped.get(14).setSelectedIndex(0); // empty selection
			}
			equipL.get(14).setVisible(false);
			equipped.get(14).setVisible(false);
		} else {
			equipL.get(14).setVisible(true);
			equipped.get(14).setVisible(true);
		}

		// Ensure heavy weapons sit in slots 1 and 2 after visibility toggles
		enforceHeavyPlacement();
	}

	/** Forces heavy weapons into slots 1/2 and reflows others. */
	private void enforceHeavyPlacement() {
		if (enforcingHeavy) return;
		if (equipped.size() < 16) return;
		enforcingHeavy = true;
		try {
			var slots = new ArrayList<DataItemEquipment>();
			for (int i = 12; i < 16; i++) {
				Object o = equipped.get(i).getSelectedItem();
				if (o instanceof DataItemEquipment di) {
					// treat the sentinel "*** Empty ***" as null
					if (di.getDname() != null && di.getDname().equalsIgnoreCase("*** Empty ***")) continue;
					slots.add(di);
				}
			}

			ArrayList<DataItemEquipment> heavy = new ArrayList<>();
			ArrayList<DataItemEquipment> light = new ArrayList<>();
			for (DataItemEquipment di : slots) {
				String slot = di.getSlot();
				String cat = di.getCategory();
				boolean isHeavy = (slot != null && slot.toLowerCase().contains("heavy")) ||
				                  (cat != null && cat.toLowerCase().contains("heavy"));
				if (isHeavy) heavy.add(di); else light.add(di);
			}

			// Reset selections to empty first
			for (int i = 12; i < 16; i++) {
				if (equipped.get(i).getItemCount() > 0) {
					equipped.get(i).setSelectedIndex(0);
				}
			}

			int idx = 12;
			for (DataItemEquipment di : heavy) {
				if (idx > 13) break; // only first two slots reserved for heavy
				equipped.get(idx).setSelectedItem(di);
				idx++;
			}
			for (DataItemEquipment di : light) {
				if (idx > 15) break;
				equipped.get(idx).setSelectedItem(di);
				idx++;
			}
		} finally {
			enforcingHeavy = false;
		}
	}
	
	/**
	 * Applies the current equip dropdown selections back to the character's inventory
	 * by toggling each DataItemEquipment's equipped flag. Intended to be called from
	 * FrameSheet before saving.
	 */
	public void applyEquipSelections() {
		if (character == null) return;
		CharInventory inv = character.getInventory();
		if (inv == null) return;
		
		// First, clear all equipped flags
		for (DataItem item : inv.getEquipment()) {
			if (item instanceof DataItemEquipment) {
				((DataItemEquipment) item).setEquipped(false);
			}
		}
		
		// Mark selected items as equipped
		for (JComboBox<DataItemEquipment> box : equipped) {
			if (box == null) continue;
			Object sel = box.getSelectedItem();
			if (sel instanceof DataItemEquipment) {
				DataItemEquipment eq = (DataItemEquipment) sel;
				// Skip placeholder
				if (eq.getDname() != null && eq.getDname().equalsIgnoreCase("*** Empty ***")) continue;
				eq.setEquipped(true);
			}
		}
		// Rebuild passive equipment statuses so deselected items are removed immediately.
		character.refreshEquipmentPassiveBonuses();
	}

	/** Saves equipment immediately after user selection changes in equip dropdowns. */
	private void autoSaveEquipmentSelection() {
		if (character == null || character.getInventory() == null) return;
		applyEquipSelections();
		refreshSelectedEquipFlags(buildEquipmentGroups());
		updateDollLists();
		applyWeaponFourRule();
		// Refresh the full character sheet so PanelCharMain reflects AC/Armor changes immediately.
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
		} else {
			refreshHPAuraOnly();
			repaint();
		}
		equipSaveDebounceTimer.restart();
	}

	private void ensureSimpleRowCapacity(ArrayList<JTextField> names, ArrayList<JFormattedTextField> quantities, ArrayList<JTextField> notes, int size) {
		while (names.size() < size) {
			names.add(buildTextField("-"));
			quantities.add(buildNumTextField(0));
			notes.add(buildTextField("-"));
		}
	}

	private void bindSimpleItemRow(ArrayList<JTextField> names, ArrayList<JFormattedTextField> quantities, ArrayList<JTextField> notes, int index, String name, double quantity, String note) {
		names.get(index).setText(name);
		names.get(index).setVisible(true);
		quantities.get(index).setValue(quantity);
		quantities.get(index).setVisible(true);
		notes.get(index).setText(note);
		notes.get(index).setVisible(true);
	}

	private void hideUnusedSimpleRows(ArrayList<JTextField> names, ArrayList<JFormattedTextField> quantities, ArrayList<JTextField> notes, int usedCount) {
		for (int i = usedCount; i < names.size(); i++) {
			names.get(i).setVisible(false);
			quantities.get(i).setVisible(false);
			notes.get(i).setVisible(false);
		}
	}

	private ArrayList<ArrayList<DataItemEquipment>> buildEquipmentGroups() {
		ArrayList<ArrayList<DataItemEquipment>> grouped = new ArrayList<>(3);
		ArrayList<DataItemEquipment> weapons = new ArrayList<>();
		ArrayList<DataItemEquipment> armor = new ArrayList<>();
		ArrayList<DataItemEquipment> accessories = new ArrayList<>();
		grouped.add(weapons);
		grouped.add(armor);
		grouped.add(accessories);

		CharInventory inv = character != null ? character.getInventory() : null;
		if (inv == null) return grouped;
		for (DataItem item : inv.getEquipment()) {
			if (!(item instanceof DataItemEquipment equip)) continue;
			String cat = equip.getCategory() != null ? equip.getCategory() : "";
			if ("Armor".equals(cat)) {
				armor.add(equip);
			} else if ("Accessory".equals(cat)) {
				accessories.add(equip);
			} else {
				weapons.add(equip);
			}
		}
		return grouped;
	}

	private void ensureEquipmentCategoryCapacity(int category, int size) {
		while (equipmentName.get(category).size() < size) {
			equipmentName.get(category).add(buildTextField("-"));
			equipmentTier.get(category).add(buildNumTextField(0));
			equipmentCat.get(category).add(buildTextField("-"));
			equipmentEquipped.get(category).add(buildFlagCheck(false));
			equipmentEnch.get(category).add(buildFlagCheck(false));
			equipmentGem.get(category).add(buildFlagCheck(false));
			equipmentStor.get(category).add(buildFlagCheck(false));
			equipmentOil.get(category).add(buildFlagCheck(false));
			equipmentMod.get(category).add(buildFlagCheck(false));
			equipmentAug.get(category).add(buildFlagCheck(false));
		}
	}

	private void bindEquipmentRow(int category, int row, DataItemEquipment item) {
		boolean blank = item == null;
		equipmentName.get(category).get(row).setText(blank ? "-" : item.getDname());
		equipmentTier.get(category).get(row).setValue(blank ? 0 : item.getTier());
		equipmentCat.get(category).get(row).setText(blank ? "-" : item.getSlot() + " " + item.getCategory());
		equipmentEquipped.get(category).get(row).setSelected(!blank && item.isEquipped());
		equipmentEnch.get(category).get(row).setSelected(!blank && item.getEnch() != 0);
		equipmentGem.get(category).get(row).setSelected(!blank && item.getGem() != 0);
		equipmentStor.get(category).get(row).setSelected(!blank && item.getStore() != 0);
		equipmentOil.get(category).get(row).setSelected(!blank && item.getOil() != 0);
		equipmentMod.get(category).get(row).setSelected(!blank && item.getMod() != 0);
		equipmentAug.get(category).get(row).setSelected(!blank && item.getAug() != 0);
		setEquipmentRowVisible(category, row, true);
	}

	private void hideUnusedEquipmentRows(int category, int usedCount) {
		for (int i = usedCount; i < equipmentName.get(category).size(); i++) {
			setEquipmentRowVisible(category, i, false);
		}
	}

	private void setEquipmentRowVisible(int category, int row, boolean visible) {
		equipmentName.get(category).get(row).setVisible(visible);
		equipmentTier.get(category).get(row).setVisible(visible);
		equipmentCat.get(category).get(row).setVisible(visible);
		equipmentEquipped.get(category).get(row).setVisible(visible);
		equipmentEnch.get(category).get(row).setVisible(visible);
		equipmentGem.get(category).get(row).setVisible(visible);
		equipmentStor.get(category).get(row).setVisible(visible);
		equipmentOil.get(category).get(row).setVisible(visible);
		equipmentMod.get(category).get(row).setVisible(visible);
		equipmentAug.get(category).get(row).setVisible(visible);
	}

	private String buildEquipmentSignature(ArrayList<ArrayList<DataItemEquipment>> groupedEquipment) {
		StringBuilder signature = new StringBuilder();
		for (ArrayList<DataItemEquipment> group : groupedEquipment) {
			for (DataItemEquipment item : group) {
				if (item == null) continue;
				signature.append(item.getIid()).append('|')
						.append(item.getDid()).append('|')
						.append(item.getLevelReq()).append('|')
						.append(item.getSlot()).append('|')
						.append(item.getCategory()).append(';');
			}
			signature.append('#');
		}
		return signature.toString();
	}

	private void refreshSelectedEquipFlags(ArrayList<ArrayList<DataItemEquipment>> groupedEquipment) {
		for (int category = 0; category < groupedEquipment.size(); category++) {
			ArrayList<DataItemEquipment> items = groupedEquipment.get(category);
			if (items.isEmpty()) {
				bindEquipmentRow(category, 0, null);
				continue;
			}
			for (int row = 0; row < items.size() && row < equipmentName.get(category).size(); row++) {
				bindEquipmentRow(category, row, items.get(row));
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}


