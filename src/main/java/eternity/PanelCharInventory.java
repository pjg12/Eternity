package eternity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private JPanel dollIconPanel;
	private ArrayList<JLabel> equipL;  //headL, shoulderL, chestL, waistL, legsL, feetL, handsL, backL, fingerRL, fingerLL, neckL, trinkL, w1L, w2L, w3L, w4L;
	private ArrayList<JComboBox<DataItemEquipment>> equipped; // equipHead, equipShoulder, equipChest, equipWaist, equipLegs, equipFeet, equipHands, equipBack, equipFingerR, equipFingerL, equipNeck, equipTrinket, equipW1, equipW2, equipW3, equipW4;
	private ArrayList<JLabel> equippedIcons;
	private boolean suppressEquipAutoSave = false;
	
	private JLabel equipmentL;
	private ArrayList<JLabel> equipmentNameL, equipmentTierL, equipmentCatL, equipmentEquippedL, equipmentEnchL, equipmentGemL, equipmentStorL, equipmentOilL, equipmentModL, equipmentAugL;
	private ArrayList<ArrayList<JTextField>> equipmentName, equipmentCat;
	private ArrayList<ArrayList<JCheckBox>> equipmentEquipped;
	private ArrayList<ArrayList<DataItemEquipment>> equipmentRowItems;
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
	private static final int EQUIP_ICON_SIZE = 80;
	private static final int DEFAULT_DOLL_IMAGE_WIDTH = 550;
	private static final int DEFAULT_DOLL_IMAGE_HEIGHT = 550;
	private static final int DOLL_ICON_GRID_COLUMNS = 6;
	private static final int DOLL_ICON_GRID_ROWS = 6;
	private static final int DOLL_ICON_CELL_WIDTH = 80;
	private static final int DOLL_ICON_CELL_HEIGHT = 80;
	private static final int DOLL_ICON_GRID_GAP = 10;
	private static final int[] DOLL_ICON_PLACEHOLDER_COLUMNS = {2, 3};
	private static final int DOLL_ICON_GRID_WIDTH = (DOLL_ICON_GRID_COLUMNS * DOLL_ICON_CELL_WIDTH) + ((DOLL_ICON_GRID_COLUMNS - 1) * DOLL_ICON_GRID_GAP);
	private static final int DOLL_ICON_GRID_HEIGHT = (DOLL_ICON_GRID_ROWS * DOLL_ICON_CELL_HEIGHT) + ((DOLL_ICON_GRID_ROWS - 1) * DOLL_ICON_GRID_GAP);
	private String cachedEquipmentSignature = "";
	private boolean suppressEquipToggleEvents = false;
	private final Map<String, ImageIcon> emptySlotIcons;
	private final Map<String, ImageIcon> equippedItemIcons;
	private int dollImageWidth = DEFAULT_DOLL_IMAGE_WIDTH;
	private int dollImageHeight = DEFAULT_DOLL_IMAGE_HEIGHT;
	
	private static final int SLOT_HEAD = 0;
	private static final int SLOT_HALO = 1;
	private static final int SLOT_NECK = 2;
	private static final int SLOT_SHOULDERS = 3;
	private static final int SLOT_BACK = 4;
	private static final int SLOT_CHEST = 5;
	private static final int SLOT_TRINKET_1 = 6;
	private static final int SLOT_HANDS = 7;
	private static final int SLOT_WAIST = 8;
	private static final int SLOT_RIGHT_FINGER = 9;
	private static final int SLOT_LEFT_FINGER = 10;
	private static final int SLOT_LEGS = 11;
	private static final int SLOT_FEET = 12;
	private static final int SLOT_TRINKET_2 = 13;
	private static final int SLOT_TRINKET_3 = 14;
	private static final int SLOT_TRINKET_4 = 15;
	private static final int FIRST_WEAPON_SLOT = 16;
	private static final int LAST_ACTIVE_WEAPON_SLOT = 19;
	private static final int LAST_WEAPON_SLOT = 21;
	private static final int SLOT_UNARMED_PROWESS_WEAPON = 20;
	private static final int SLOT_COUNT = 22;
	private static final String UNARMED_PROWESS_SPECIALTY = "Unarmed Prowess";
	private static final int UNARMED_PROWESS_ITEM_IID = -5005;

	private final String[] SLOTS = {"Head", "Halo", "Neck", "Shoulders", "Back", "Chest", "Trinket 1", "Hands", "Waist", "Right Finger", "Left Finger", "Legs", "Feet", "Trinket 2", "Trinket 3", "Trinket 4", "Weapon 1", "Weapon 2", "Weapon 3", "Weapon 4", "Weapon 5", "Weapon 6"};

	/*
	 * 		DEFAULT CONSTRUCTOR
	 */
	PanelCharInventory (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(255, 255, 204));
		emptySlotIcons = new HashMap<>();
		equippedItemIcons = new HashMap<>();
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
		for (int i = 0; i < SLOT_COUNT; i++) {
			tempLabel = buildLabel(SLOTS[i], null);
			tempLabel.setVisible(false);
			equipL.add(tempLabel);
		}

		equipped = new ArrayList<JComboBox<DataItemEquipment>>();
		JComboBox<DataItemEquipment> tempBox;
		for (int i = 0; i < SLOT_COUNT; i++) {
			tempBox = buildEquipBox();
			tempBox.addActionListener(e -> {
				if (suppressEquipAutoSave) return;
				autoSaveEquipmentSelection();
			});
			equipped.add(tempBox);
			tempBox.setVisible(false);
		}
		dollIconPanel = new JPanel(new GridBagLayout());
		dollIconPanel.setOpaque(false);
		dollIconPanel.setVisible(true);
		equippedIcons = new ArrayList<JLabel>();
		for (int i = 0; i < SLOT_COUNT; i++) {
			JLabel equipIcon = buildEquipIconLabel();
			equippedIcons.add(equipIcon);
		}
		rebuildDollIconGrid();

		dollLabel = new JLabel("<html><center>Image Not Found<br>If you are using a gender<br>that is neither 'Male' nor 'Female'<br>the doll image will not display.</center></html>", SwingConstants.CENTER);
    	add(dollLabel);
    	dollLabel.setHorizontalAlignment(JLabel.CENTER);
		add(dollIconPanel);
		setComponentZOrder(dollIconPanel, 0);

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
    	equipmentRowItems = new ArrayList<ArrayList<DataItemEquipment>>();
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
	    	equipmentRowItems.add(new ArrayList<DataItemEquipment>());
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
		
		int dollY = pageHeight + 10;
		dollLabel.setBounds(7, dollY, dollImageWidth, dollImageHeight);
		int iconGridX = dollLabel.getX() + Math.max(0, (dollImageWidth - DOLL_ICON_GRID_WIDTH) / 2);
		int iconGridY = dollLabel.getY() + Math.max(0, (dollImageHeight - DOLL_ICON_GRID_HEIGHT) / 2);
		dollIconPanel.setBounds(iconGridX, iconGridY, DOLL_ICON_GRID_WIDTH, DOLL_ICON_GRID_HEIGHT);
		dollIconPanel.setVisible(true);
		setComponentZOrder(dollIconPanel, 0);
		pageHeight = dollY + Math.max(dollImageHeight, DOLL_ICON_GRID_HEIGHT) + 5;

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
				updateDollLists();
	}  /*--------------
		END UPDATEALL
		--------------*/

	@Override
	public void updateCharacter(StoreCharData character) {
		super.updateCharacter(character);
		enforceReadOnlyChecks();
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
		rebuildDollIconGrid();
		for (int i = 0; i < equipped.size(); i++) {
			DataItemEquipment selected = getSelectedEquipment(i);
			JLabel equipIcon = equippedIcons.get(i);
			boolean visible = shouldShowDollSlot(i);
			equipIcon.setVisible(visible);
			if (!visible) {
				equipIcon.setIcon(null);
				equipIcon.setToolTipText(null);
				continue;
			}
			ImageIcon slotIcon = isEmptyEquipment(selected)
					? resolveEmptySlotIcon(SLOTS[i])
					: resolveEquippedItemIcon(i, selected);
			equipIcon.setIcon(slotIcon != null ? slotIcon : resolveEmptySlotIcon(SLOTS[i]));
			equipIcon.setText(null);
			equipIcon.setToolTipText(isEmptyEquipment(selected) ? SLOTS[i] : selectedEquipmentDisplayName(selected));
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
				dollPic = ImageIO.read(AppPaths.imagesDir().resolve(dollName + ".jpg").toFile());
				dollLabel.setIcon(buildScaledDollIcon());
				dollLabel.setText(null);
			} catch (Exception e) {
				dollPic = null;
				dollLabel.setIcon(null);
				dollLabel.setText("<html><center><br><br><br><br>Image Not Found<br>If you are using a gender<br>that is neither 'Male' nor 'Female'<br>the doll image will not display<br>without a jpg file<br>in the Images folder<br>with the same name<br>as your chosen gender.</center></html>");
			}
		} else if (dollPic != null) {
			dollLabel.setIcon(buildScaledDollIcon());
			dollLabel.setText(null);
		}
		updateDollLists();
	}  /*--------------
		END UPDATEDOLL
		--------------*/

	public void setDollImageScale(int width, int height) {
		if (width <= 0 || height <= 0) {
			return;
		}
		dollImageWidth = width;
		dollImageHeight = height;
		if (dollLabel != null) {
			dollLabel.setBounds(dollLabel.getX(), dollLabel.getY(), dollImageWidth, dollImageHeight);
		}
		if (dollPic != null) {
			dollLabel.setIcon(buildScaledDollIcon());
			dollLabel.setText(null);
		}
		resizeSheet();
		revalidate();
		repaint();
	}
	
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
		
		for (int i = 0; i < SLOT_COUNT; i++) {
			tempList.add((DataItemEquipment)equipped.get(i).getSelectedItem());
			equipped.get(i).removeAllItems();
			
			DataItemEquipment tempEquip = new DataItemEquipment();
			tempEquip.setDname("*** Empty ***");
			equipped.get(i).addItem(tempEquip);
		}
		
		for (int i = 0; i < tempWeapons.size(); i++) {
			if (!isItemEligibleByLevel(tempWeapons.get(i))) continue;
			if (isLockedUnarmedProwessWeapon(tempWeapons.get(i))) continue;
			for (int j = FIRST_WEAPON_SLOT; j <= LAST_ACTIVE_WEAPON_SLOT; j++) {
				equipped.get(j).addItem(tempWeapons.get(i));
			}
		}
		// Select equipped weapons into the first available weapon slots
		int weaponSlot = FIRST_WEAPON_SLOT;
		for (DataItemEquipment w : tempWeapons) {
			if (isLockedUnarmedProwessWeapon(w)) continue;
			if (w.isEquipped() && weaponSlot <= LAST_ACTIVE_WEAPON_SLOT) {
				equipped.get(weaponSlot).setSelectedItem(w);
				weaponSlot++;
			}
		}
		DataItemEquipment lockedUnarmedWeapon = findLockedUnarmedProwessWeapon(tempWeapons);
		if (lockedUnarmedWeapon != null) {
			equipped.get(SLOT_UNARMED_PROWESS_WEAPON).addItem(lockedUnarmedWeapon);
			equipped.get(SLOT_UNARMED_PROWESS_WEAPON).setSelectedItem(lockedUnarmedWeapon);
		}
		
		for (int i = 0; i < tempArmor.size(); i++) {
			if (!isItemEligibleByLevel(tempArmor.get(i))) continue;
			if (isMatrixEquipment(tempArmor.get(i))) {
				addMatrixToArmorSlots(tempArmor.get(i), tempList);
				continue;
			}
			String tempSlot = tempArmor.get(i).getSlot();
			if (tempSlot.compareTo("Head") == 0) {
				equipped.get(SLOT_HEAD).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_HEAD).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Shoulders") == 0) {
				equipped.get(SLOT_SHOULDERS).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_SHOULDERS).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Chest") == 0) {
				equipped.get(SLOT_CHEST).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_CHEST).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Hands") == 0) {
				equipped.get(SLOT_HANDS).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_HANDS).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Waist") == 0) {
				equipped.get(SLOT_WAIST).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_WAIST).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Legs") == 0) {
				equipped.get(SLOT_LEGS).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_LEGS).setSelectedItem(tempArmor.get(i));
			}
			else if (tempSlot.compareTo("Feet") == 0) {
				equipped.get(SLOT_FEET).addItem(tempArmor.get(i));
				if (tempArmor.get(i).isEquipped()) equipped.get(SLOT_FEET).setSelectedItem(tempArmor.get(i));
			}
		}
		
		for (int i = 0; i < tempAccessories.size(); i++) {
			if (!isItemEligibleByLevel(tempAccessories.get(i))) continue;
			String tempSlot = tempAccessories.get(i).getSlot();
			if (tempSlot.compareTo("Halo") == 0) {
				equipped.get(SLOT_HALO).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(SLOT_HALO).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Neck") == 0) {
				equipped.get(SLOT_NECK).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(SLOT_NECK).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Back") == 0) {
				equipped.get(SLOT_BACK).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(SLOT_BACK).setSelectedItem(tempAccessories.get(i));
			}
			else if (isTrinketSlotName(tempSlot)) {
				addItemToTrinketSlots(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) {
					selectEquippedTrinket(tempAccessories.get(i));
				}
			}
			else if (tempSlot.compareTo("Right Finger") == 0) {
				equipped.get(SLOT_RIGHT_FINGER).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(SLOT_RIGHT_FINGER).setSelectedItem(tempAccessories.get(i));
			}
			else if (tempSlot.compareTo("Left Finger") == 0) {
				equipped.get(SLOT_LEFT_FINGER).addItem(tempAccessories.get(i));
				if (tempAccessories.get(i).isEquipped()) equipped.get(SLOT_LEFT_FINGER).setSelectedItem(tempAccessories.get(i));
			}
		}
	}

	private boolean isItemEligibleByLevel(DataItemEquipment item) {
		if (item == null) return false;
		if (character == null || character.getIdentity() == null) return true;
		return item.getLevelReq() <= character.getIdentity().getLevel();
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
				if (isMatrixEquipment(eq)) {
					int slotIndex = equipped.indexOf(box);
					String matrixSlotName = resolveMatrixArmorSlotName(slotIndex);
					if (matrixSlotName != null) {
						eq.setSlot(matrixSlotName);
					}
				}
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
			if ("Armor".equals(cat) || "Matrix".equals(cat)) {
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
			int rowIndex = equipmentName.get(category).size();
			equipmentName.get(category).add(buildTextField("-"));
			equipmentTier.get(category).add(buildNumTextField(0));
			equipmentCat.get(category).add(buildTextField("-"));
			equipmentEquipped.get(category).add(buildEquipToggleCheck(category, rowIndex));
			equipmentRowItems.get(category).add(null);
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
		equipmentRowItems.get(category).set(row, item);
		String displayName = "-";
		if (!blank) {
			displayName = (item.getIname() != null && !item.getIname().isBlank()) ? item.getIname() : item.getDname();
		}
		equipmentName.get(category).get(row).setText(displayName);
		equipmentTier.get(category).get(row).setValue(blank ? 0 : item.getTier());
		equipmentCat.get(category).get(row).setText(blank ? "-" : item.getSlot() + " " + item.getCategory());
		JCheckBox equipToggle = equipmentEquipped.get(category).get(row);
		suppressEquipToggleEvents = true;
		try {
			equipToggle.setSelected(!blank && item.isEquipped());
		} finally {
			suppressEquipToggleEvents = false;
		}
		equipToggle.setEnabled(!blank && !isLockedUnarmedProwessWeapon(item));
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

	private DataItemEquipment getSelectedEquipment(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= equipped.size()) return null;
		Object selected = equipped.get(slotIndex).getSelectedItem();
		return selected instanceof DataItemEquipment item ? item : null;
	}

	private boolean isEmptyEquipment(DataItemEquipment equipmentItem) {
		return equipmentItem == null
				|| equipmentItem.getDname() == null
				|| equipmentItem.getDname().equalsIgnoreCase("*** Empty ***");
	}

	private String selectedEquipmentDisplayName(DataItemEquipment equipmentItem) {
		if (isEmptyEquipment(equipmentItem)) return "-";
		String inventoryName = equipmentItem.getIname();
		if (inventoryName != null && !inventoryName.isBlank()) {
			return inventoryName;
		}
		String displayName = equipmentItem.getDname();
		return (displayName == null || displayName.isBlank()) ? "-" : displayName;
	}

	private JLabel buildEquipIconLabel() {
		JLabel iconLabel = new JLabel();
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		iconLabel.setIcon(null);
		iconLabel.setPreferredSize(new Dimension(DOLL_ICON_CELL_WIDTH, DOLL_ICON_CELL_HEIGHT));
		iconLabel.setMinimumSize(new Dimension(DOLL_ICON_CELL_WIDTH, DOLL_ICON_CELL_HEIGHT));
		iconLabel.setMaximumSize(new Dimension(DOLL_ICON_CELL_WIDTH, DOLL_ICON_CELL_HEIGHT));
		return iconLabel;
	}

	private JLabel buildDollIconPlaceholder() {
		JLabel placeholder = new JLabel();
		placeholder.setOpaque(false);
		placeholder.setPreferredSize(new Dimension(DOLL_ICON_CELL_WIDTH, DOLL_ICON_CELL_HEIGHT));
		placeholder.setMinimumSize(new Dimension(DOLL_ICON_CELL_WIDTH, DOLL_ICON_CELL_HEIGHT));
		placeholder.setMaximumSize(new Dimension(DOLL_ICON_CELL_WIDTH, DOLL_ICON_CELL_HEIGHT));
		return placeholder;
	}

	private void rebuildDollIconGrid() {
		if (dollIconPanel == null) return;
		dollIconPanel.removeAll();
		boolean[][] occupiedCells = new boolean[DOLL_ICON_GRID_ROWS][DOLL_ICON_GRID_COLUMNS];
		addDollIconPlaceholders(occupiedCells);
		for (int i = 0; i < equippedIcons.size(); i++) {
			if (!shouldShowDollSlot(i)) {
				equippedIcons.get(i).setVisible(false);
				continue;
			}
			JLabel iconLabel = equippedIcons.get(i);
			iconLabel.setVisible(true);
			int[] preferredCell = resolvePreferredGridCell(i);
			int gridX = preferredCell[0];
			int gridY = preferredCell[1];
			if (gridX < 0 || gridY < 0 || gridX >= DOLL_ICON_GRID_COLUMNS || gridY >= DOLL_ICON_GRID_ROWS || occupiedCells[gridY][gridX]) {
				int[] fallbackCell = findNextAvailableGridCell(occupiedCells);
				gridX = fallbackCell[0];
				gridY = fallbackCell[1];
			}
			occupiedCells[gridY][gridX] = true;

			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = gridX;
			gc.gridy = gridY;
			gc.insets = new Insets(0, 0, gc.gridy < DOLL_ICON_GRID_ROWS - 1 ? DOLL_ICON_GRID_GAP : 0, gc.gridx < DOLL_ICON_GRID_COLUMNS - 1 ? DOLL_ICON_GRID_GAP : 0);
			gc.anchor = GridBagConstraints.CENTER;
			gc.fill = GridBagConstraints.NONE;
			dollIconPanel.add(iconLabel, gc);
		}
		dollIconPanel.revalidate();
		dollIconPanel.repaint();
	}

	private void addDollIconPlaceholders(boolean[][] occupiedCells) {
		for (int row = 0; row < DOLL_ICON_GRID_ROWS; row++) {
			for (int col : DOLL_ICON_PLACEHOLDER_COLUMNS) {
				if (col < 0 || col >= DOLL_ICON_GRID_COLUMNS) continue;
				occupiedCells[row][col] = true;

				GridBagConstraints gc = new GridBagConstraints();
				gc.gridx = col;
				gc.gridy = row;
				gc.insets = new Insets(0, 0, gc.gridy < DOLL_ICON_GRID_ROWS - 1 ? DOLL_ICON_GRID_GAP : 0, gc.gridx < DOLL_ICON_GRID_COLUMNS - 1 ? DOLL_ICON_GRID_GAP : 0);
				gc.anchor = GridBagConstraints.CENTER;
				gc.fill = GridBagConstraints.NONE;
				dollIconPanel.add(buildDollIconPlaceholder(), gc);
			}
		}
	}

	private boolean shouldShowDollSlot(int slotIndex) {
		if (slotIndex == SLOT_UNARMED_PROWESS_WEAPON) return false;
		if (slotIndex > LAST_ACTIVE_WEAPON_SLOT && slotIndex <= LAST_WEAPON_SLOT) return false;
		if (slotIndex != SLOT_HALO) return true;
		return isPilotClass();
	}

	private boolean hasUnarmedProwess() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty(UNARMED_PROWESS_SPECIALTY);
	}

	private boolean isPilotClass() {
		if (character == null || character.getIdentity() == null) return false;
		String className = character.getIdentity().getCharClass();
		return className != null && className.equalsIgnoreCase("Pilot");
	}

	private int[] resolvePreferredGridCell(int slotIndex) {
		return switch (slotIndex) {
			case SLOT_TRINKET_1 -> new int[] {0, 0};
			case SLOT_TRINKET_2 -> new int[] {1, 0};
			case SLOT_TRINKET_3 -> new int[] {4, 0};
			case SLOT_TRINKET_4 -> new int[] {5, 0};
			case SLOT_CHEST -> new int[] {1, 2};
			case SLOT_HALO -> new int[] {4, 2};
			case SLOT_HANDS -> new int[] {0, 3};
			case SLOT_LEFT_FINGER -> new int[] {1, 3};
			case SLOT_RIGHT_FINGER -> new int[] {4, 3};
			case SLOT_WAIST -> new int[] {5, 3};
			case SLOT_SHOULDERS -> new int[] {0, 1};
			case SLOT_HEAD -> new int[] {1, 1};
			case SLOT_NECK -> new int[] {4, 1};
			case SLOT_BACK -> new int[] {5, 1};
			case FIRST_WEAPON_SLOT + 4 -> new int[] {0, 4};
			case FIRST_WEAPON_SLOT -> new int[] {0, 5};
			case FIRST_WEAPON_SLOT + 1 -> new int[] {1, 5};
			case FIRST_WEAPON_SLOT + 5 -> new int[] {5, 4};
			case FIRST_WEAPON_SLOT + 2 -> new int[] {4, 5};
			case SLOT_LEGS -> new int[] {1, 4};
			case SLOT_FEET -> new int[] {4, 4};
			case FIRST_WEAPON_SLOT + 3 -> new int[] {5, 5};
			default -> new int[] {-1, -1};
		};
	}

	private int[] findNextAvailableGridCell(boolean[][] occupiedCells) {
		for (int row = 0; row < DOLL_ICON_GRID_ROWS; row++) {
			for (int col = 0; col < DOLL_ICON_GRID_COLUMNS; col++) {
				if (!occupiedCells[row][col]) {
					return new int[] {col, row};
				}
			}
		}
		return new int[] {0, 0};
	}

	private ImageIcon resolveEmptySlotIcon(String slotName) {
		if (slotName == null || slotName.isBlank()) {
			return null;
		}
		ImageIcon cached = emptySlotIcons.get(slotName);
		if (cached != null || emptySlotIcons.containsKey(slotName)) {
			return cached;
		}

		String normalizedSlotName = slotName.replaceAll("\\s+", "");
		ImageIcon loaded = loadScaledIcon("no" + normalizedSlotName + ".png");
		emptySlotIcons.put(slotName, loaded);
		return loaded;
	}

	private ImageIcon resolveEquippedItemIcon(int slotIndex, DataItemEquipment item) {
		String fileName = deriveEquippedItemIconFileName(slotIndex, item);
		if (fileName == null || fileName.isBlank()) {
			return null;
		}
		ImageIcon cached = equippedItemIcons.get(fileName);
		if (cached != null || equippedItemIcons.containsKey(fileName)) {
			return cached;
		}

		ImageIcon loaded = loadScaledIcon(fileName + ".png");
		equippedItemIcons.put(fileName, loaded);
		return loaded;
	}

	private String deriveEquippedItemIconFileName(int slotIndex, DataItemEquipment item) {
		if (item == null) return null;
		if (slotIndex == SLOT_TRINKET_1) {
			return ("aba" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		if (slotIndex == SLOT_TRINKET_2) {
			return ("abr" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		if (slotIndex == SLOT_TRINKET_3) {
			return ("alo" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		if (slotIndex == SLOT_TRINKET_4) {
			return ("ame" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		if (slotIndex == SLOT_LEFT_FINGER) {
			return ("ari" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		if (slotIndex == SLOT_RIGHT_FINGER) {
			return ("arn" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		if (slotIndex == SLOT_HALO) {
			return ("ehl" + item.getTier()).toLowerCase(Locale.ROOT);
		}
		String prefix = deriveEquippedItemIconPrefix(item);
		String suffix = deriveEquippedItemIconSuffix(item);
		String tier = deriveEquippedItemIconNumber(item);
		if (prefix == null || suffix == null || suffix.length() < 2) {
			return null;
		}
		return (prefix + suffix + tier).toLowerCase(Locale.ROOT);
	}

	private String deriveEquippedItemIconPrefix(DataItemEquipment item) {
		if (item == null) return null;
		if (isWeaponEquipment(item)) {
			return "w";
		}

		String category = item.getCategory() == null ? "" : item.getCategory().trim();
		String type = item.getType() == null ? "" : item.getType().trim();
		if ("Accessory".equalsIgnoreCase(category)) {
			return "a";
		}
		if ("Armor".equalsIgnoreCase(category)) {
			if (type.regionMatches(true, 0, "Exo", 0, 4)) return "e";
			if (type.regionMatches(true, 0, "Heavy", 0, 5)) return "h";
			if (type.regionMatches(true, 0, "Medium", 0, 6)) return "m";
			if (type.regionMatches(true, 0, "Light", 0, 5)) return "l";
		}
		return null;
	}

	private String deriveEquippedItemIconSuffix(DataItemEquipment item) {
		if (item == null) return null;
		String source = isWeaponEquipment(item) ? item.getType() : item.getSlot();
		if (source == null) return null;
		String normalized = source.trim().replaceAll("\\s+", "");
		if (normalized.length() < 2) return null;
		return normalized.substring(0, 2);
	}

	private String deriveEquippedItemIconNumber(DataItemEquipment item) {
		String result = "";
		if (item == null) return null;
		if (isWeaponEquipment(item)) {
			if (item.getDname().compareTo(item.getType()) == 0) {
				result = "1";
			} else {
				result = "2";
			}
		}

		result += "" + item.getTier();
		return result;
	}

	private ImageIcon loadScaledIcon(String fileName) {
		try {
			File iconFile = AppPaths.imagesDir().resolve(fileName).toFile();
			if (!iconFile.isFile()) {
				return null;
			}
			ImageIcon rawIcon = new ImageIcon(iconFile.getAbsolutePath());
			Image scaled = rawIcon.getImage().getScaledInstance(EQUIP_ICON_SIZE, EQUIP_ICON_SIZE, Image.SCALE_SMOOTH);
			return new ImageIcon(scaled);
		} catch (Exception ignored) {
			return null;
		}
	}

	private ImageIcon buildScaledDollIcon() {
		if (dollPic == null || dollImageWidth <= 0 || dollImageHeight <= 0) {
			return null;
		}
		Image scaled = dollPic.getScaledInstance(dollImageWidth, dollImageHeight, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}

	private JCheckBox buildEquipToggleCheck(int category, int row) {
		JCheckBox box = new JCheckBox();
		box.setFocusable(false);
		box.setHorizontalAlignment(SwingConstants.CENTER);
		box.setOpaque(true);
		box.setBackground(alternate ? Color.WHITE : Color.LIGHT_GRAY);
		box.addActionListener(e -> {
			if (suppressEquipToggleEvents) return;
			handleEquipmentToggle(category, row);
		});
		add(box);
		return box;
	}

	private void handleEquipmentToggle(int category, int row) {
		if (character == null || character.getInventory() == null) return;
		if (category < 0 || category >= equipmentRowItems.size()) return;
		if (row < 0 || row >= equipmentRowItems.get(category).size()) return;

		DataItemEquipment item = equipmentRowItems.get(category).get(row);
		JCheckBox toggle = equipmentEquipped.get(category).get(row);
		if (item == null || toggle == null) return;

		boolean shouldEquip = toggle.isSelected();
		boolean success = shouldEquip ? equipItemFromRow(item) : unequipItemFromRow(item);
		if (success) return;

		suppressEquipToggleEvents = true;
		try {
			toggle.setSelected(!shouldEquip);
		} finally {
			suppressEquipToggleEvents = false;
		}
	}

	private boolean equipItemFromRow(DataItemEquipment item) {
		if (item == null) return false;
		if (isLockedUnarmedProwessWeapon(item)) {
			syncEquipmentUiAfterToggle();
			return true;
		}
		if (!isItemEligibleByLevel(item)) {
			JOptionPane.showMessageDialog(this,
					selectedEquipmentDisplayName(item) + " cannot be equipped until the required level is met.",
					"Equip Failed",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		int existingSlot = findSelectedSlotIndexForItem(item);
		if (existingSlot >= 0) {
			syncEquipmentUiAfterToggle();
			return true;
		}

		int slotIndex = resolveAvailableSlotIndex(item);
		if (slotIndex < 0) {
			showNoAvailableSlotMessage(item);
			return false;
		}

		equipped.get(slotIndex).setSelectedItem(item);
		syncEquipmentUiAfterToggle();
		return true;
	}

	private boolean unequipItemFromRow(DataItemEquipment item) {
		if (item == null) return false;
		if (isLockedUnarmedProwessWeapon(item)) {
			JOptionPane.showMessageDialog(this,
					selectedEquipmentDisplayName(item) + " is locked by Unarmed Prowess and cannot be unequipped.",
					"Equip Locked",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		int slotIndex = findSelectedSlotIndexForItem(item);
		if (slotIndex >= 0 && equipped.get(slotIndex).getItemCount() > 0) {
			equipped.get(slotIndex).setSelectedIndex(0);
		} else {
			item.setEquipped(false);
		}
		syncEquipmentUiAfterToggle();
		return true;
	}

	private void syncEquipmentUiAfterToggle() {
		if (character == null) return;
		applyEquipSelections();
		refreshSelectedEquipFlags(buildEquipmentGroups());
		updateDollLists();
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			refreshHeaderState(character);
		} else {
			character.updateAll();
			refreshHPAuraOnly();
			repaint();
		}
		equipSaveDebounceTimer.restart();
	}

	private int resolveAvailableSlotIndex(DataItemEquipment item) {
		if (item == null) return -1;
		if (isMatrixEquipment(item)) {
			String preferredSlot = item.getSlot();
			int preferredIndex = resolveFixedSlotIndex(preferredSlot);
			if (isMatrixArmorSlot(preferredIndex) && isEmptyEquipment(getSelectedEquipment(preferredIndex))) {
				return preferredIndex;
			}
			for (int slotIndex : getMatrixArmorSlotIndexes()) {
				if (isEmptyEquipment(getSelectedEquipment(slotIndex))) {
					item.setSlot(resolveMatrixArmorSlotName(slotIndex));
					return slotIndex;
				}
			}
			return -1;
		}
		if (isWeaponEquipment(item)) {
			for (int slotIndex = FIRST_WEAPON_SLOT; slotIndex <= LAST_ACTIVE_WEAPON_SLOT; slotIndex++) {
				if (isEmptyEquipment(getSelectedEquipment(slotIndex))) {
					return slotIndex;
				}
			}
			return -1;
		}
		if (isTrinketSlotName(item.getSlot())) {
			for (int slotIndex : getTrinketSlotIndexes()) {
				if (isEmptyEquipment(getSelectedEquipment(slotIndex))) {
					return slotIndex;
				}
			}
			return -1;
		}

		int slotIndex = resolveFixedSlotIndex(item.getSlot());
		if (slotIndex < 0) return -1;
		return isEmptyEquipment(getSelectedEquipment(slotIndex)) ? slotIndex : -1;
	}

	private int findSelectedSlotIndexForItem(DataItemEquipment item) {
		if (item == null) return -1;
		for (int slotIndex = 0; slotIndex < equipped.size(); slotIndex++) {
			DataItemEquipment selected = getSelectedEquipment(slotIndex);
			if (selected == item) {
				return slotIndex;
			}
		}
		return -1;
	}

	private DataItemEquipment findLockedUnarmedProwessWeapon(List<DataItemEquipment> items) {
		if (items == null) return null;
		for (DataItemEquipment item : items) {
			if (isLockedUnarmedProwessWeapon(item)) return item;
		}
		return null;
	}

	private boolean isLockedUnarmedProwessWeapon(DataItemEquipment item) {
		return item instanceof DataItemWeapon
				&& item.getIid() == UNARMED_PROWESS_ITEM_IID
				&& "Unarmed".equalsIgnoreCase(item.getDname());
	}

	private int resolveFixedSlotIndex(String slotName) {
		if (slotName == null) return -1;
		return switch (slotName.trim()) {
			case "Head" -> SLOT_HEAD;
			case "Halo" -> SLOT_HALO;
			case "Neck" -> SLOT_NECK;
			case "Shoulders" -> SLOT_SHOULDERS;
			case "Back" -> SLOT_BACK;
			case "Chest" -> SLOT_CHEST;
			case "Trinket", "Trinket 1" -> SLOT_TRINKET_1;
			case "Hands" -> SLOT_HANDS;
			case "Waist" -> SLOT_WAIST;
			case "Right Finger" -> SLOT_RIGHT_FINGER;
			case "Left Finger" -> SLOT_LEFT_FINGER;
			case "Legs" -> SLOT_LEGS;
			case "Feet" -> SLOT_FEET;
			case "Trinket 2" -> SLOT_TRINKET_2;
			case "Trinket 3" -> SLOT_TRINKET_3;
			case "Trinket 4" -> SLOT_TRINKET_4;
			default -> -1;
		};
	}

	private boolean isWeaponEquipment(DataItemEquipment item) {
		if (item == null) return false;
		String slot = item.getSlot() == null ? "" : item.getSlot().toLowerCase();
		String category = item.getCategory() == null ? "" : item.getCategory().toLowerCase();
		String type = item.getType() == null ? "" : item.getType().toLowerCase();
		if (category.contains("matrix")) return false;
		return slot.contains("weapon")
				|| slot.contains("hand")
				|| category.contains("weapon")
				|| category.contains("melee")
				|| category.contains("ranged")
				|| category.contains("aura")
				|| type.contains("bow")
				|| type.contains("crossbow")
				|| type.contains("gun")
				|| type.contains("rifle")
				|| type.contains("pistol")
				|| type.contains("sword")
				|| type.contains("axe")
				|| type.contains("spear")
				|| type.contains("dagger")
				|| type.contains("staff")
				|| type.contains("mace")
				|| type.contains("hammer");
	}

	private void showNoAvailableSlotMessage(DataItemEquipment item) {
		String itemName = selectedEquipmentDisplayName(item);
		String slotLabel = isMatrixEquipment(item)
				? "armor slots"
				: isWeaponEquipment(item)
				? "weapon slots"
				: isTrinketSlotName(item.getSlot())
				? "trinket slots"
				: ((item.getSlot() == null || item.getSlot().isBlank()) ? "matching slot" : item.getSlot() + " slot");
		JOptionPane.showMessageDialog(this,
				"No available " + slotLabel + " for " + itemName + ".",
				"Equip Failed",
				JOptionPane.WARNING_MESSAGE);
	}

	private boolean isTrinketSlotName(String slotName) {
		if (slotName == null) return false;
		String normalized = slotName.trim();
		return normalized.equalsIgnoreCase("Trinket")
				|| normalized.equalsIgnoreCase("Trinket 1")
				|| normalized.equalsIgnoreCase("Trinket 2")
				|| normalized.equalsIgnoreCase("Trinket 3")
				|| normalized.equalsIgnoreCase("Trinket 4");
	}

	private int[] getTrinketSlotIndexes() {
		return new int[] {SLOT_TRINKET_1, SLOT_TRINKET_2, SLOT_TRINKET_3, SLOT_TRINKET_4};
	}

	private void addItemToTrinketSlots(DataItemEquipment item) {
		for (int slotIndex : getTrinketSlotIndexes()) {
			equipped.get(slotIndex).addItem(item);
		}
	}

	private void selectEquippedTrinket(DataItemEquipment item) {
		for (int slotIndex : getTrinketSlotIndexes()) {
			if (isEmptyEquipment(getSelectedEquipment(slotIndex))) {
				equipped.get(slotIndex).setSelectedItem(item);
				return;
			}
		}
	}

	private boolean isMatrixEquipment(DataItemEquipment item) {
		if (item == null || item.getCategory() == null) return false;
		return "Matrix".equalsIgnoreCase(item.getCategory().trim());
	}

	private void addMatrixToArmorSlots(DataItemEquipment item, ArrayList<DataItemEquipment> previousSelections) {
		if (item == null) return;
		for (int slotIndex : getMatrixArmorSlotIndexes()) {
			equipped.get(slotIndex).addItem(item);
		}
		int selectedSlotIndex = resolveMatrixSelectedSlot(item, previousSelections);
		if (selectedSlotIndex >= 0) {
			equipped.get(selectedSlotIndex).setSelectedItem(item);
		}
	}

	private int resolveMatrixSelectedSlot(DataItemEquipment item, ArrayList<DataItemEquipment> previousSelections) {
		if (item == null) return -1;
		if (previousSelections != null) {
			for (int slotIndex : getMatrixArmorSlotIndexes()) {
				if (slotIndex < previousSelections.size() && previousSelections.get(slotIndex) == item) {
					return slotIndex;
				}
			}
		}
		if (!item.isEquipped()) return -1;
		int preferredIndex = resolveFixedSlotIndex(item.getSlot());
		return isMatrixArmorSlot(preferredIndex) ? preferredIndex : SLOT_CHEST;
	}

	private int[] getMatrixArmorSlotIndexes() {
		return new int[] { SLOT_HEAD, SLOT_SHOULDERS, SLOT_CHEST, SLOT_WAIST, SLOT_LEGS, SLOT_FEET, SLOT_HANDS };
	}

	private boolean isMatrixArmorSlot(int slotIndex) {
		for (int armorSlot : getMatrixArmorSlotIndexes()) {
			if (armorSlot == slotIndex) return true;
		}
		return false;
	}

	private String resolveMatrixArmorSlotName(int slotIndex) {
		return switch (slotIndex) {
			case SLOT_HEAD -> "Head";
			case SLOT_SHOULDERS -> "Shoulders";
			case SLOT_CHEST -> "Chest";
			case SLOT_HANDS -> "Hands";
			case SLOT_WAIST -> "Waist";
			case SLOT_LEGS -> "Legs";
			case SLOT_FEET -> "Feet";
			default -> null;
		};
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}


