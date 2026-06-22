package eternity;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class PanelCharGranted extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_LOADOUT_NAME = "Default";
	private static final String LOADOUT_NAMES_KEY = "granted.loadouts";
	private static final String ACTIVE_LOADOUT_KEY = "granted.loadout.active";
	private static final String LOADOUT_ENABLED_PREFIX = "granted.loadout.enabled.";
	
	// granted techniques (maintained-style presentation)
	private JLabel grantedTechsL, mtNameL, mtMaxL, mtActLevelL, mtCostPerALL, mtCostL, mtAffinityL;
	private JLabel grantL, grantUsedL, thisLoadoutL;
	private ArrayList<JTextField> mtName, mtAffinity;
	private ArrayList<JFormattedTextField> mtMax, mtActLevel, mtCostPerAL, mtCost;
	private JFormattedTextField grantField, grantUsedField, thisLoadoutField;
	private ArrayList<DataTraining> mtTechRefs;
	private ArrayList<String> mtRowKeys;
	private ArrayList<String> mtRowAffinities;
	private ArrayList<String> mtSectionOrder;
	private ArrayList<JLabel> mtSectionTitles, mtSectionAffinityL, mtSectionNameL, mtSectionMaxL, mtSectionActLevelL, mtSectionCostPerALL, mtSectionCostL;
	private JLabel loadoutL;
	private JComboBox<String> loadoutBox;
	private JButton loadoutNewButton, loadoutSaveButton, loadoutToggleButton, mtUpdateButton, mtMaxButton, mtOffButton;
	private ArrayList<GrantedRow> cachedGrantedRows;
	private String cachedStructureSignature;
	private String currentLoadoutName;
	private boolean suppressLoadoutEvents;

	private record GrantedRow(String affinity, String name, int maxRank, int activeLevel, double costPer, double occupiedCost,
			DataTraining tech, String rowKey, String statusAttribute, double statusRatio) {}
	
	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharGranted (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(222, 244, 228));
			
		/**************
		* ***********		Maintained
		*/// ***********			
		grantedTechsL = buildLabel("Granted Techniques", null);
		mtNameL = buildLabel("Name", null);
		mtMaxL = buildLabel("Max", null);
		mtActLevelL = buildLabel("AL", null);
		mtCostPerALL = buildLabel("Cost", null);
		mtCostL = buildLabel("Occ", null);
		mtAffinityL = buildLabel("Affinity", null);
		grantL = buildLabel("Grant", null);
		grantUsedL = buildLabel("Grant Used", null);
		thisLoadoutL = buildLabel("This Loadout", null);
		grantField = buildNumTextField(0.0);
		grantUsedField = buildNumTextField(0.0);
		thisLoadoutField = buildNumTextField(0.0);
		styleSummaryField(grantField);
		styleSummaryField(grantUsedField);
		styleSummaryField(thisLoadoutField);

		mtName = new ArrayList<JTextField>();
		mtAffinity = new ArrayList<JTextField>();
		mtMax = new ArrayList<JFormattedTextField>();
		mtActLevel = new ArrayList<JFormattedTextField>();
		mtCostPerAL = new ArrayList<JFormattedTextField>();
		mtCost = new ArrayList<JFormattedTextField>();
		mtTechRefs = new ArrayList<DataTraining>();
		mtRowKeys = new ArrayList<String>();
		mtRowAffinities = new ArrayList<String>();
		mtSectionOrder = new ArrayList<String>();
		mtSectionTitles = new ArrayList<JLabel>();
		mtSectionAffinityL = new ArrayList<JLabel>();
		mtSectionNameL = new ArrayList<JLabel>();
		mtSectionMaxL = new ArrayList<JLabel>();
		mtSectionActLevelL = new ArrayList<JLabel>();
		mtSectionCostPerALL = new ArrayList<JLabel>();
		mtSectionCostL = new ArrayList<JLabel>();
		cachedGrantedRows = new ArrayList<GrantedRow>();
		cachedStructureSignature = "";
		currentLoadoutName = DEFAULT_LOADOUT_NAME;
		loadoutL = buildLabel("Loadout", null);
		loadoutBox = buildComboBox();
		loadoutBox.removeAllItems();
		loadoutBox.addActionListener(e -> loadoutSelectionChanged());
		loadoutNewButton = buildButton("New");
		loadoutNewButton.addActionListener(e -> createLoadout());
		loadoutSaveButton = buildButton("Save");
		loadoutSaveButton.addActionListener(e -> saveCurrentLoadout());
		loadoutToggleButton = buildButton("Activate");
		loadoutToggleButton.addActionListener(e -> toggleCurrentLoadoutActivation());
		mtUpdateButton = buildButton("Share");
		mtUpdateButton.addActionListener(e -> copyShareToClipboard());
		mtMaxButton = buildButton("Maximize");
		mtMaxButton.addActionListener(e -> mtMax());
		mtOffButton = buildButton("Off");
		mtOffButton.addActionListener(e -> mtOff());
		
		
		/*
		 * 	Updates
		 */	
		updateMaintained();
	    resizeSheet();
	} //END OF PARAMETERIZED CONSTRUCTOR
	
	public void resizeSheet() {
		/*
		 * 	Maintained
		 */	
		pageHeight = resizeHeader();
		grantedTechsL.setVisible(false);
		loadoutL.setBounds(5, pageHeight, 60, 20);
		loadoutBox.setBounds(70, pageHeight, 180, 20);
		loadoutNewButton.setBounds(265, pageHeight, 80, 20);
		loadoutSaveButton.setBounds(355, pageHeight, 80, 20);
		loadoutToggleButton.setBounds(445, pageHeight, 95, 20);
		pageHeight += 25;
		grantL.setBounds(5, pageHeight, 80, 20);
		grantField.setBounds(90, pageHeight, 70, 20);
		grantUsedL.setBounds(180, pageHeight, 90, 20);
		grantUsedField.setBounds(275, pageHeight, 70, 20);
		thisLoadoutL.setBounds(365, pageHeight, 95, 20);
		thisLoadoutField.setBounds(465, pageHeight, 70, 20);
		pageHeight += 25;
		mtAffinityL.setVisible(false);
		mtNameL.setVisible(false);
		mtMaxL.setVisible(false);
		mtActLevelL.setVisible(false);
		mtCostPerALL.setVisible(false);
		mtCostL.setVisible(false);

		String prevAffinity = null;
		int sectionIndex = -1;
		for (int i = 0; i < mtName.size(); i++) {
			if (mtTechRefs.get(i) == null) continue;
			String affinity = (mtRowAffinities != null && i < mtRowAffinities.size()) ? mtRowAffinities.get(i) : "";
			if (prevAffinity == null || !prevAffinity.equalsIgnoreCase(affinity)) {
				sectionIndex++;
				if (sectionIndex < mtSectionTitles.size()) {
					mtSectionTitles.get(sectionIndex).setBounds(5, pageHeight, 555, 20);
					mtSectionTitles.get(sectionIndex).setVisible(true);
					pageHeight += 20;
					mtSectionAffinityL.get(sectionIndex).setBounds(5, pageHeight, 120, 20);
					mtSectionNameL.get(sectionIndex).setBounds(130, pageHeight, 210, 20);
					mtSectionMaxL.get(sectionIndex).setBounds(345, pageHeight, 50, 20);
					mtSectionActLevelL.get(sectionIndex).setBounds(400, pageHeight, 50, 20);
					mtSectionCostPerALL.get(sectionIndex).setBounds(455, pageHeight, 50, 20);
					mtSectionCostL.get(sectionIndex).setBounds(510, pageHeight, 50, 20);
					mtSectionAffinityL.get(sectionIndex).setVisible(true);
					mtSectionNameL.get(sectionIndex).setVisible(true);
					mtSectionMaxL.get(sectionIndex).setVisible(true);
					mtSectionActLevelL.get(sectionIndex).setVisible(true);
					mtSectionCostPerALL.get(sectionIndex).setVisible(true);
					mtSectionCostL.get(sectionIndex).setVisible(true);
					pageHeight += 20;
				}
			}
			prevAffinity = affinity;

			if (mtAffinity.get(i).getText().compareTo("None") != 0) {
				mtAffinity.get(i).setBounds(5, pageHeight, 120, 20);
			}
			else {
				mtAffinity.get(i).setVisible(false);
			}
			mtName.get(i).setBounds(130, pageHeight, 210, 20);
			mtMax.get(i).setBounds(345, pageHeight, 50, 20);
			mtActLevel.get(i).setBounds(400, pageHeight, 50, 20);
			mtActLevel.get(i).setEditable(true);
			mtCostPerAL.get(i).setBounds(455, pageHeight, 50, 20);
			mtCost.get(i).setBounds(510, pageHeight, 50, 20);
			
			pageHeight += 20;
		}
		pageHeight += 5;
		mtUpdateButton.setBounds(250, pageHeight, 105, 20);
		mtMaxButton.setBounds(100, pageHeight, 105, 20);
		mtOffButton.setBounds(400, pageHeight, 105, 20);
		pageHeight += 20;
		
		
		/*
		 * Set Window Size
		 */	
		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}
	
	/*
	 * 
	 * UPDATERS
	 * 
	 */

	
	/*
	 * updateMain - updates the main panel
	 */
	public void updateMaintained() {
		refreshLoadoutControls();
		ArrayList<GrantedRow> rows = getGrantedRows();
		resetSectionMetadata();
		ensureRowCapacity(rows.size());
		hideUnusedRows(rows.size());
		for (int i = 0; i < rows.size(); i++) {
			bindGrantedRow(i, rows.get(i));
		}
		buildAffinitySections();
		hideUnusedSections(mtSectionOrder.size());
		syncGrantOccupiedAuraFromOcc();
		synchronizeCharacterState();
		updateGrantSummary();
		refreshHPAuraOnly();

		resizeSheet();
	}

	@Override
	public void updateCharacter(StoreCharData character) {
		this.character = character;
		updateMaintained();
		refreshReminderOnly();
	}

	@Override
	protected void refreshBaseState() {
		super.refreshBaseState();
		updateGrantSummary();
	}

	public void mtUpdate () {
		if (!canApplyProjectedGrantedAura(calculateProjectedGrantedOccupiedAuraFromFields())) {
			refreshGrantedValuesOnly();
			resizeSheet();
			return;
		}
		if (character != null && mtActLevel != null && mtTechRefs != null) {
			for (int i = 0; i < mtActLevel.size() && i < mtTechRefs.size(); i++) {
				if (mtTechRefs.get(i) == null) continue;
				try {
					int newAl = ((Number) mtActLevel.get(i).getValue()).intValue();
					int safeAl = Math.max(0, newAl);
					saveGrantedAl(i, safeAl);
				} catch (Exception ignored) {
					// leave AL unchanged on parse error
				}
			}

			for (int i = 0; i < mtCost.size() && i < mtCostPerAL.size() && i < mtActLevel.size(); i++) {
				try {
					double costPer = ((Number) mtCostPerAL.get(i).getValue()).doubleValue();
					double al = ((Number) mtActLevel.get(i).getValue()).doubleValue();
					mtCost.get(i).setValue(costPer * al);
				} catch (Exception ignored) {
					mtCost.get(i).setValue(0);
				}
			}
			syncGrantOccupiedAuraFromOcc();
			refreshAfterGrantedChange(false);
		}
	    resizeSheet();
	}

	private void copyShareToClipboard() {
		if (character == null) return;
		DataColor raceColor = null;
		if (dataQuery != null && character.getIdentity() != null) {
			raceColor = dataQuery.getColorByTitle(character.getIdentity().getRace());
		}
		if (raceColor == null) {
			raceColor = new DataColor("Default", 0, 0, 0, 255, 255, 255);
		}
		String colorString1 = String.format("#%02x%02x%02x", raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
		String colorString2 = String.format("#%02x%02x%02x", raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
		String charName = character.getIdentity() != null && character.getIdentity().getName() != null
				? character.getIdentity().getName()
				: "Character";
		String loadoutName = normalizeLoadoutName(currentLoadoutName);
		if (loadoutName == null) loadoutName = DEFAULT_LOADOUT_NAME;
		boolean active = isLoadoutActivated(loadoutName);
		double grantValue = character.getAttributes() == null ? 0.0 : getDerivedStatusValue(character.getAttributes(), "GRANT");
		int grantUsedValue = calculateActivatedGrantUsed();
		int loadoutTotal = calculateLoadoutAlTotal(loadoutName);

		StringBuilder tempString = new StringBuilder();
		tempString.append("!scriptcard {{ --#titleCardBackground|").append(colorString1)
				.append(" --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|").append(colorString1)
				.append(" --#titleCardBottomBorder|4px solid #000000; --#title|").append(charName)
				.append(" --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|").append(colorString2)
				.append(" --#leftSub|Granted Loadout")
				.append(" --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|").append(colorString1)
				.append(" --#evenRowFontColor|").append(colorString2)
				.append(" --#oddRowBackground|").append(colorString2)
				.append(" --#oddRowFontColor|").append(colorString1)
				.append(" --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;");


		boolean addedAny = false;
		for (int i = 0; i < mtName.size() && i < mtActLevel.size() && i < mtCost.size(); i++) {
			JTextField nameField = mtName.get(i);
			JFormattedTextField alField = mtActLevel.get(i);
			JFormattedTextField costField = mtCost.get(i);
			if (nameField == null || alField == null || costField == null || !nameField.isVisible()) continue;
			int alValue = 0;
			try {
				Number alNumber = (Number) alField.getValue();
				if (alNumber != null) alValue = Math.max(0, alNumber.intValue());
			} catch (Exception ignored) {
				alValue = 0;
			}
			if (alValue <= 0) continue;

			addedAny = true;
		}
		if (!addedAny) {
			tempString.append(" --+|No active techniques in the selected loadout.");
		}
		appendActivatedLoadoutCodes(tempString);
		tempString.append(" }}");

		StringSelection stringSelection = new StringSelection(tempString.toString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	public void mtMax () {
		if (!canApplyProjectedGrantedAura(calculateProjectedGrantedOccupiedAuraFromMax())) {
			refreshGrantedValuesOnly();
			resizeSheet();
			return;
		}
		for (int i = 0; i < mtActLevel.size() && i < mtMax.size(); i++) {
			int newAl = 0;
			try {
				Number maxVal = (Number) mtMax.get(i).getValue();
				newAl = maxVal == null ? 0 : Math.max(0, maxVal.intValue());
				mtActLevel.get(i).setValue(newAl);
			} catch (Exception ignored) {
				mtActLevel.get(i).setValue(0);
				newAl = 0;
			}
			saveGrantedAl(i, newAl);
		}
		for (int i = 0; i < mtCost.size() && i < mtCostPerAL.size() && i < mtActLevel.size(); i++) {
			try {
				double costPer = ((Number) mtCostPerAL.get(i).getValue()).doubleValue();
				double al = ((Number) mtActLevel.get(i).getValue()).doubleValue();
				mtCost.get(i).setValue(costPer * al);
			} catch (Exception ignored) {
				mtCost.get(i).setValue(0);
			}
		}
		if (character != null) {
			syncGrantOccupiedAuraFromOcc();
			refreshAfterGrantedChange(false);
		}
		resizeSheet();
	}
	
	public void mtOff () {
		for (int i = 0; i < mtActLevel.size(); i++) {
			JFormattedTextField alField = mtActLevel.get(i);
			alField.setValue(0);
			saveGrantedAl(i, 0);
		}
		for (int i = 0; i < mtCost.size() && i < mtCostPerAL.size(); i++) {
			try {
				mtCost.get(i).setValue(0);
			} catch (Exception ignored) {
				mtCost.get(i).setValue(0);
			}
		}
		if (character != null) {
			syncGrantOccupiedAuraFromOcc();
			refreshAfterGrantedChange(false);
		}
		resizeSheet();
	}

	private void syncGrantOccupiedAuraFromOcc() {
		if (character == null || character.getResources() == null) return;
		character.getResources().setGrantOccupiedAura(Math.max(0.0, calculateActivatedOccupiedAura()));
	}

	private double calculateProjectedGrantedOccupiedAuraFromFields() {
		return calculateProjectedGrantedOccupiedAuraForLoadout(calculateDisplayedLoadoutOccupiedAuraFromFields());
	}

	private double calculateProjectedGrantedOccupiedAuraFromMax() {
		return calculateProjectedGrantedOccupiedAuraForLoadout(calculateDisplayedLoadoutOccupiedAuraFromMax());
	}

	private double calculateProjectedGrantedOccupiedAuraForLoadout(double proposedCurrentLoadoutOccupiedAura) {
		if (character == null || character.getResources() == null) return 0.0;
		double currentGrantOccupiedAura = Math.max(0.0, character.getResources().getGrantOccupiedAura());
		if (!isLoadoutActivated(currentLoadoutName)) {
			return currentGrantOccupiedAura;
		}
		double currentLoadoutOccupiedAura = Math.max(0.0, calculateLoadoutOccupiedAura(currentLoadoutName));
		return Math.max(0.0, currentGrantOccupiedAura - currentLoadoutOccupiedAura + Math.max(0.0, proposedCurrentLoadoutOccupiedAura));
	}

	private double calculateDisplayedLoadoutOccupiedAuraFromFields() {
		double total = 0.0;
		int limit = Math.min(mtActLevel.size(), mtCostPerAL.size());
		for (int i = 0; i < limit; i++) {
			double costPer = safeDoubleFieldValue(mtCostPerAL.get(i));
			int al = Math.max(0, safeIntFieldValue(mtActLevel.get(i)));
			total += costPer * al;
		}
		return total;
	}

	private double calculateDisplayedLoadoutOccupiedAuraFromMax() {
		double total = 0.0;
		int limit = Math.min(mtMax.size(), mtCostPerAL.size());
		for (int i = 0; i < limit; i++) {
			double costPer = safeDoubleFieldValue(mtCostPerAL.get(i));
			int al = Math.max(0, safeIntFieldValue(mtMax.get(i)));
			total += costPer * al;
		}
		return total;
	}

	private boolean canApplyProjectedGrantedAura(double projectedGrantOccupiedAura) {
		if (character == null || character.getResources() == null) return true;
		CharResources resources = character.getResources();
		double availableAfter = resources.calcMaxAura()
				- resources.getSpentAura()
				- resources.getMainOccupiedAura()
				- Math.max(0.0, projectedGrantOccupiedAura);
		if (availableAfter >= -0.0001) return true;
		JOptionPane.showMessageDialog(this,
				"You do not have enough aura for that.",
				"Not Enough Aura",
				JOptionPane.WARNING_MESSAGE);
		return false;
	}

	private int safeIntFieldValue(JFormattedTextField field) {
		if (field == null) return 0;
		try {
			Number value = (Number) field.getValue();
			return value == null ? 0 : value.intValue();
		} catch (Exception ignored) {
			return 0;
		}
	}

	private double safeDoubleFieldValue(JFormattedTextField field) {
		if (field == null) return 0.0;
		try {
			Number value = (Number) field.getValue();
			return value == null ? 0.0 : value.doubleValue();
		} catch (Exception ignored) {
			return 0.0;
		}
	}

	private void updateGrantSummary() {
		double grantValue = 0.0;
		int grantUsedValue = 0;
		int loadoutTotal = calculateLoadoutAlTotal(currentLoadoutName);
		if (character != null) {
			if (character.getAttributes() != null) {
				grantValue = getDerivedStatusValue(character.getAttributes(), "GRANT");
			}
			grantUsedValue = calculateActivatedGrantUsed();
		}
		grantField.setValue(round2(grantValue));
		grantUsedField.setValue(grantUsedValue);
		thisLoadoutField.setValue(loadoutTotal);
		updateLoadoutToggleButton();
	}

	private void styleSummaryField(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(new Color(240, 248, 240));
		field.setForeground(new Color(26, 70, 34));
	}

	private void styleMaxField(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(new Color(226, 236, 250));
		field.setForeground(new Color(22, 50, 87));
	}

	private void styleCostField(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(new Color(244, 236, 214));
		field.setForeground(new Color(102, 66, 12));
	}

	private void styleOccField(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(new Color(231, 245, 232));
		field.setForeground(new Color(24, 84, 33));
	}

	private void styleAffinityField(JTextField field, String affinity) {
		if (field == null) return;
		field.setBackground(Color.WHITE);
		field.setForeground(Color.BLACK);
		if (affinity == null || affinity.isBlank() || "Standard".equalsIgnoreCase(affinity)) return;
		DataColor color = dataQuery != null ? dataQuery.getColorByTitle(affinity) : null;
		if (color == null) return;
		field.setBackground(color.getBackColor());
		field.setForeground(color.getForeColor());
	}

	private void buildAffinitySections() {
		if (mtRowAffinities == null || mtRowAffinities.isEmpty()) return;
		String prev = null;
		for (String affinity : mtRowAffinities) {
			String safeAffinity = (affinity == null || affinity.isBlank()) ? "Other" : affinity;
			if (prev != null && prev.equalsIgnoreCase(safeAffinity)) continue;
			prev = safeAffinity;
			mtSectionOrder.add(safeAffinity);
			int idx = mtSectionOrder.size() - 1;
			ensureSectionCapacity(idx + 1);
			mtSectionTitles.get(idx).setText(safeAffinity + " Techniques");
			mtSectionAffinityL.get(idx).setText("Affinity");
			mtSectionNameL.get(idx).setText("Name");
			mtSectionMaxL.get(idx).setText("Max");
			mtSectionActLevelL.get(idx).setText("AL");
			mtSectionCostPerALL.get(idx).setText("Cost");
			mtSectionCostL.get(idx).setText("Occ");
		}
	}

	private void loadoutSelectionChanged() {
		if (suppressLoadoutEvents) return;
		String selected = getSelectedLoadoutName();
		if (selected == null || selected.isBlank()) return;
		if (selected.equalsIgnoreCase(currentLoadoutName)) return;
		persistDisplayedStateForLoadout(currentLoadoutName);
		currentLoadoutName = selected;
		setActiveLoadoutName(selected);
		refreshAfterGrantedChange(false);
	}

	private void toggleCurrentLoadoutActivation() {
		if (character == null) return;
		String loadoutName = normalizeLoadoutName(currentLoadoutName);
		if (loadoutName == null) loadoutName = DEFAULT_LOADOUT_NAME;
		boolean active = isLoadoutActivated(loadoutName);
		if (!active) {
			double grantValue = character.getAttributes() == null ? 0.0 : getDerivedStatusValue(character.getAttributes(), "GRANT");
			int currentUsed = calculateActivatedGrantUsed();
			int loadoutTotal = calculateLoadoutAlTotal(loadoutName);
			if ((currentUsed + loadoutTotal) > grantValue + 0.0001) {
				JOptionPane.showMessageDialog(this,
						"Activating " + loadoutName + " would exceed the character's Grant value.",
						"Grant Exceeded",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		setLoadoutActivated(loadoutName, !active);
		refreshAfterGrantedChange(false);
	}

	private void createLoadout() {
		if (character == null) return;
		String rawName = JOptionPane.showInputDialog(this, "New granted loadout name:", "Create Loadout", JOptionPane.PLAIN_MESSAGE);
		String loadoutName = normalizeLoadoutName(rawName);
		if (loadoutName == null) return;
		persistDisplayedStateForLoadout(currentLoadoutName);
		List<String> names = getStoredLoadoutNames();
		if (!containsIgnoreCase(names, loadoutName)) {
			names.add(loadoutName);
			storeLoadoutNames(names);
		}
		persistDisplayedStateForLoadout(loadoutName);
		currentLoadoutName = loadoutName;
		setActiveLoadoutName(loadoutName);
		refreshLoadoutControls();
	}

	private void saveCurrentLoadout() {
		persistDisplayedStateForLoadout(currentLoadoutName);
		mtUpdate();
	}

	private void refreshLoadoutControls() {
		if (character == null) return;
		List<String> names = getStoredLoadoutNames();
		String activeLoadout = resolveActiveLoadoutName(names);
		suppressLoadoutEvents = true;
		loadoutBox.removeAllItems();
		for (String name : names) {
			loadoutBox.addItem(name);
		}
		loadoutBox.setSelectedItem(activeLoadout);
		suppressLoadoutEvents = false;
		currentLoadoutName = activeLoadout;
		updateLoadoutToggleButton();
	}

	private List<String> getStoredLoadoutNames() {
		LinkedHashSet<String> names = new LinkedHashSet<>();
		names.add(DEFAULT_LOADOUT_NAME);
		if (character != null) {
			String raw = character.getReminderSelection(LOADOUT_NAMES_KEY);
			if (raw != null && !raw.isBlank()) {
				String[] split = raw.split("\\R");
				for (String part : split) {
					String name = normalizeLoadoutName(part);
					if (name != null) {
						names.add(name);
					}
				}
			}
		}
		return new ArrayList<>(names);
	}

	private void storeLoadoutNames(List<String> names) {
		if (character == null) return;
		LinkedHashSet<String> unique = new LinkedHashSet<>();
		unique.add(DEFAULT_LOADOUT_NAME);
		if (names != null) {
			for (String name : names) {
				String normalized = normalizeLoadoutName(name);
				if (normalized != null) unique.add(normalized);
			}
		}
		character.setReminderSelection(LOADOUT_NAMES_KEY, String.join("\n", unique));
	}

	private String resolveActiveLoadoutName(List<String> names) {
		String raw = character == null ? null : character.getReminderSelection(ACTIVE_LOADOUT_KEY);
		String normalized = normalizeLoadoutName(raw);
		if (normalized != null && containsIgnoreCase(names, normalized)) {
			return normalized;
		}
		if (names != null && !names.isEmpty()) {
			return names.get(0);
		}
		return DEFAULT_LOADOUT_NAME;
	}

	private void setActiveLoadoutName(String loadoutName) {
		if (character == null) return;
		String normalized = normalizeLoadoutName(loadoutName);
		character.setReminderSelection(ACTIVE_LOADOUT_KEY, normalized == null ? DEFAULT_LOADOUT_NAME : normalized);
	}

	private String getSelectedLoadoutName() {
		Object selected = loadoutBox.getSelectedItem();
		return selected == null ? DEFAULT_LOADOUT_NAME : selected.toString();
	}

	private String normalizeLoadoutName(String raw) {
		if (raw == null) return null;
		String trimmed = raw.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean containsIgnoreCase(List<String> names, String candidate) {
		if (names == null || candidate == null) return false;
		for (String name : names) {
			if (name != null && name.equalsIgnoreCase(candidate)) {
				return true;
			}
		}
		return false;
	}

	private String buildGrantedAlKey(DataTraining tech, String variant) {
		if (tech == null) return "granted.al.unknown";
		String rawVariant = (variant == null || variant.isBlank()) ? "base" : variant.trim().toLowerCase();
		return "granted.al." + tech.getId() + "." + sanitizeVariantKey(rawVariant);
	}

	private String buildLoadoutValueKey(String loadoutName, String rowKey) {
		String safeLoadout = sanitizeVariantKey(loadoutName == null ? DEFAULT_LOADOUT_NAME.toLowerCase() : loadoutName.trim().toLowerCase());
		return "granted.loadout." + safeLoadout + "." + rowKey;
	}

	private int loadGrantedAl(String rowKey, int fallback) {
		return loadGrantedAlForLoadout(currentLoadoutName, rowKey, fallback);
	}

	private int loadGrantedAlForLoadout(String loadoutName, String rowKey, int fallback) {
		if (character == null || rowKey == null || rowKey.isBlank()) return Math.max(0, fallback);
		String raw = character.getReminderSelection(buildLoadoutValueKey(loadoutName, rowKey));
		if (raw == null || raw.isBlank()) return Math.max(0, fallback);
		try {
			return Math.max(0, Integer.parseInt(raw.trim()));
		} catch (NumberFormatException ignored) {
			return Math.max(0, fallback);
		}
	}

	private void saveGrantedAl(int rowIndex, int al) {
		if (character == null || mtRowKeys == null || rowIndex < 0 || rowIndex >= mtRowKeys.size()) return;
		String rowKey = mtRowKeys.get(rowIndex);
		if (rowKey == null || rowKey.isBlank()) return;
		character.setReminderSelection(buildLoadoutValueKey(currentLoadoutName, rowKey), Integer.toString(Math.max(0, al)));
	}

	private void persistDisplayedStateForLoadout(String loadoutName) {
		if (character == null || mtActLevel == null || mtRowKeys == null) return;
		String normalizedLoadout = normalizeLoadoutName(loadoutName);
		if (normalizedLoadout == null) normalizedLoadout = DEFAULT_LOADOUT_NAME;
		for (int i = 0; i < mtActLevel.size() && i < mtRowKeys.size(); i++) {
			String rowKey = mtRowKeys.get(i);
			if (rowKey == null || rowKey.isBlank()) continue;
			int value = 0;
			try {
				Number raw = (Number) mtActLevel.get(i).getValue();
				if (raw != null) value = Math.max(0, raw.intValue());
			} catch (Exception ignored) {
				value = 0;
			}
			character.setReminderSelection(buildLoadoutValueKey(normalizedLoadout, rowKey), Integer.toString(value));
		}
	}

	private String buildLoadoutEnabledKey(String loadoutName) {
		String safeLoadout = sanitizeVariantKey(loadoutName == null ? DEFAULT_LOADOUT_NAME.toLowerCase() : loadoutName.trim().toLowerCase());
		return LOADOUT_ENABLED_PREFIX + safeLoadout;
	}

	private boolean isLoadoutActivated(String loadoutName) {
		if (character == null) return false;
		String raw = character.getReminderSelection(buildLoadoutEnabledKey(loadoutName));
		return Boolean.parseBoolean(raw);
	}

	private void setLoadoutActivated(String loadoutName, boolean active) {
		if (character == null) return;
		character.setReminderSelection(buildLoadoutEnabledKey(loadoutName), Boolean.toString(active));
		updateLoadoutToggleButton();
	}

	private void updateLoadoutToggleButton() {
		if (loadoutToggleButton == null) return;
		boolean active = isLoadoutActivated(currentLoadoutName);
		loadoutToggleButton.setText(active ? "Deactivate" : "Activate");
	}

	private int calculateLoadoutAlTotal(String loadoutName) {
		int total = 0;
		for (GrantedRow row : getGrantedRowDefinitions()) {
			if (row == null) continue;
			total += loadGrantedAlForLoadout(loadoutName, row.rowKey(), 0);
		}
		return total;
	}

	private double calculateLoadoutOccupiedAura(String loadoutName) {
		double total = 0.0;
		for (GrantedRow row : getGrantedRowDefinitions()) {
			if (row == null) continue;
			int al = loadGrantedAlForLoadout(loadoutName, row.rowKey(), 0);
			total += row.costPer() * al;
		}
		return total;
	}

	private int calculateActivatedGrantUsed() {
		int total = 0;
		for (String loadoutName : getStoredLoadoutNames()) {
			if (!isLoadoutActivated(loadoutName)) continue;
			total += calculateLoadoutAlTotal(loadoutName);
		}
		return total;
	}

	private double calculateActivatedOccupiedAura() {
		double total = 0.0;
		for (String loadoutName : getStoredLoadoutNames()) {
			if (!isLoadoutActivated(loadoutName)) continue;
			total += calculateLoadoutOccupiedAura(loadoutName);
		}
		return total;
	}

	private List<GrantedRow> getGrantedRowDefinitions() {
		if (cachedGrantedRows == null || cachedGrantedRows.isEmpty()) {
			return collectGrantedRows();
		}
		return cachedGrantedRows;
	}

	private void appendActivatedLoadoutCodes(StringBuilder tempString) {
		if (tempString == null) return;
		boolean addedAny = false;
		for (String loadoutName : getStoredLoadoutNames()) {
			if (!isLoadoutActivated(loadoutName)) continue;
			String code = buildStatusCodeForLoadout(loadoutName);
			if (code == null || code.isBlank()) continue;
			tempString.append(" --+|Status Code: ").append(loadoutName)
					.append("[br]&nbsp;&nbsp;").append(code);
			addedAny = true;
		}
		if (!addedAny) {
			tempString.append(" --+|No activated loadouts to share as status codes.");
		}
	}

	private String buildStatusCodeForLoadout(String loadoutName) {
		Map<String, Double> totals = new LinkedHashMap<>();
		for (GrantedRow row : getGrantedRowDefinitions()) {
			if (row == null) continue;
			String attribute = row.statusAttribute();
			double ratio = row.statusRatio();
			if (attribute == null || attribute.isBlank() || Math.abs(ratio) < 0.0001) continue;
			int al = loadGrantedAlForLoadout(loadoutName, row.rowKey(), 0);
			if (al <= 0) continue;
			double effectiveAl = character == null ? al : character.getEffectiveTechniqueAl(row.affinity(), al);
			addStatusCodeContribution(totals, attribute, ratio * effectiveAl);
		}
		if (totals.isEmpty()) return "";

		StringBuilder code = new StringBuilder();
		code.append("NM").append(buildGrantedStatusName());
		boolean appendedAnyStatus = false;
		for (Map.Entry<String, Double> entry : totals.entrySet()) {
			double severity = entry.getValue() == null ? 0.0 : entry.getValue();
			if (Math.abs(severity) < 0.0001) continue;
			if (!appendedAnyStatus) {
				code.append('_');
				appendedAnyStatus = true;
			} else {
				code.append('_');
			}
			code.append(StatusCodeParser.getPreferredAttributeAlias(entry.getKey()))
					.append(formatPackedStatusSeverity(severity));
		}
		return code.toString();
	}

	private String buildGrantedStatusName() {
		String rawName = character != null && character.getIdentity() != null && character.getIdentity().getName() != null
				? character.getIdentity().getName()
				: "";
		String alphanumeric = rawName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
		if (alphanumeric.isBlank()) {
			alphanumeric = "CHR";
		}
		String prefix = alphanumeric.length() <= 3 ? alphanumeric : alphanumeric.substring(0, 3);
		return prefix + "GRANT";
	}

	private void addStatusCodeContribution(Map<String, Double> totals, String rawAttribute, double severity) {
		if (totals == null || rawAttribute == null || rawAttribute.isBlank() || Math.abs(severity) < 0.0001) return;
		String normalized = normalizeGrantedStatusAttribute(rawAttribute);
		if (normalized == null || normalized.isBlank()) return;
		switch (normalized) {
			case "MAXHP" -> mergeStatusSeverity(totals, "BASEHP", severity);
			case "HPMULTI" -> mergeStatusSeverity(totals, "MULTIHP", severity);
			case "MAXAURA" -> mergeStatusSeverity(totals, "BASEAURA", severity);
			case "AURAMULTI" -> mergeStatusSeverity(totals, "MULTIAURA", severity);
			case "REACT" -> mergeStatusSeverity(totals, "BASEREACT", severity);
			case "R1" -> mergeStatusSeverity(totals, "BASER1", severity);
			case "R2" -> mergeStatusSeverity(totals, "BASER2", severity);
			case "R3" -> mergeStatusSeverity(totals, "BASER3", severity);
			case "DMGMULTI" -> {
				mergeStatusSeverity(totals, "MBDMG", severity);
				mergeStatusSeverity(totals, "MTDMG", severity);
			}
			case "HEALMULTI" -> {
				mergeStatusSeverity(totals, "MBHEAL", severity);
				mergeStatusSeverity(totals, "MTHEAL", severity);
			}
			default -> mergeStatusSeverity(totals, "B" + normalized, severity);
		}
	}

	private void mergeStatusSeverity(Map<String, Double> totals, String attribute, double severity) {
		if (totals == null || attribute == null || attribute.isBlank() || Math.abs(severity) < 0.0001) return;
		totals.merge(attribute, severity, Double::sum);
	}

	private String normalizeGrantedStatusAttribute(String key) {
		if (key == null) return null;
		String upper = key.trim().toUpperCase();
		if (upper.isBlank()) return null;
		return switch (upper) {
			case "APPLY" -> "APP";
			case "IMPAIR" -> "IMP";
			case "RESPHY" -> "PHY";
			default -> {
				if (upper.startsWith("RESIST")) yield upper.substring("RESIST".length());
				if (upper.startsWith("RES") && upper.length() > 3) yield upper.substring(3);
				yield upper;
			}
		};
	}

	private String formatStatusSeverity(double severity) {
		double rounded = round2(severity);
		if (Math.abs(rounded - Math.rint(rounded)) < 0.0001) {
			int intValue = (int) Math.rint(rounded);
			return intValue > 0 ? "+" + intValue : Integer.toString(intValue);
		}
		String text = fmt(rounded);
		return rounded > 0 ? "+" + text : text;
	}

	private String formatPackedStatusSeverity(double severity) {
		double rounded = round2(severity);
		String sign = rounded < 0 ? "N" : "P";
		double absolute = Math.abs(rounded);
		if (Math.abs(absolute - Math.rint(absolute)) < 0.0001) {
			return sign + Integer.toString((int) Math.rint(absolute));
		}
		String text = fmt(absolute).replace(".", "D");
		return sign + text;
	}

	private String sanitizeStatusCodeText(String value) {
		if (value == null) return "";
		return value.replace("|", "/").trim();
	}

	private ArrayList<GrantedRow> collectGrantedRows() {
		ArrayList<GrantedRow> rows = new ArrayList<>();
		if (character == null || character.getTraining() == null) return rows;

		ArrayList<DataTraining> maintainedTechs = new ArrayList<>();
		for (DataTraining tech : character.getTraining().getAllTraining()) {
			if (tech == null || !"Maintained".equalsIgnoreCase(tech.getType())) continue;
			maintainedTechs.add(tech);
		}
		maintainedTechs.sort((a, b) -> {
			int idA = a.getId();
			int idB = b.getId();
			if (idA != idB) return Integer.compare(idA, idB);
			String nameA = a.getName() == null ? "" : a.getName();
			String nameB = b.getName() == null ? "" : b.getName();
			return nameA.compareToIgnoreCase(nameB);
		});

		for (DataTraining tech : maintainedTechs) {
			int maxRank = tech.getMaxRank(character);
			boolean hasProgress = tech.getRank() > 0 || tech.getExp() > 0;
			if (maxRank <= 0 && !hasProgress) continue;
			int baseAl = 0;
			boolean isStandardAffinity = "Standard".equalsIgnoreCase(tech.getAffinity()) && tech.getGrant() != null && !tech.getGrant().isEmpty();

			if (isStandardAffinity && dataQuery != null) {
				boolean addedAny = false;
				for (Integer gid : tech.getGrant()) {
					if (gid == null) continue;
					DataTechPerm perm = dataQuery.getTechPermById(gid);
					String attr = (perm != null && perm.getAttribute() != null) ? perm.getAttribute() : "None";
					String rowName = "Boost".equalsIgnoreCase(tech.getName()) ? "Boost (" + attr + ")" : tech.getName() + " (" + attr + ")";
					String rowKey = buildGrantedAlKey(tech, attr);
					int activeLevel = loadGrantedAl(rowKey, baseAl);
					double costPer = 0.0;
					if (perm != null) {
						if (perm.getCost() != 0) costPer = perm.getCost();
						else if (perm.getRatio() != 0) costPer = perm.getRatio();
					}
					rows.add(new GrantedRow(tech.getAffinity(), rowName, tech.getRank(), activeLevel, costPer, costPer * activeLevel,
							tech, rowKey, attr, perm != null ? perm.getRatio() : 0.0));
					addedAny = true;
				}
				if (addedAny) continue;
			}

			String rowKey = buildGrantedAlKey(tech, isStandardAffinity ? "None" : null);
			int activeLevel = loadGrantedAl(rowKey, baseAl);
			double costPer = resolveCostPer(tech);
			DataTechPerm firstPerm = resolveFirstPerm(tech);
			String attr = firstPerm != null ? firstPerm.getAttribute() : null;
			double ratio = firstPerm != null ? firstPerm.getRatio() : 0.0;
			rows.add(new GrantedRow(tech.getAffinity(), tech.getName(), tech.getRank(), activeLevel, costPer, costPer * activeLevel,
					tech, rowKey, attr, ratio));
		}
		return rows;
	}

	private ArrayList<GrantedRow> getGrantedRows() {
		String structureSignature = buildStructureSignature();
		if (!structureSignature.equals(cachedStructureSignature)) {
			cachedGrantedRows = collectGrantedRows();
			cachedStructureSignature = structureSignature;
			return cachedGrantedRows;
		}
		return refreshCachedGrantedRows();
	}

	private String buildStructureSignature() {
		if (character == null || character.getTraining() == null) return "";
		StringBuilder signature = new StringBuilder();
		for (DataTraining tech : character.getTraining().getAllTraining()) {
			if (tech == null || !"Maintained".equalsIgnoreCase(tech.getType())) continue;
			int maxRank = tech.getMaxRank(character);
			boolean hasProgress = tech.getRank() > 0 || tech.getExp() > 0;
			if (maxRank <= 0 && !hasProgress) continue;
			signature.append(tech.getId()).append('|')
					.append(tech.getName()).append('|')
					.append(tech.getAffinity()).append('|')
					.append(tech.getRank()).append('|');
			if (tech.getGrant() != null) {
				for (Integer gid : tech.getGrant()) {
					signature.append(gid).append(',');
				}
			}
			signature.append(';');
		}
		return signature.toString();
	}

	private ArrayList<GrantedRow> refreshCachedGrantedRows() {
		ArrayList<GrantedRow> refreshed = new ArrayList<>(cachedGrantedRows.size());
		for (GrantedRow row : cachedGrantedRows) {
			if (row == null) continue;
			int activeLevel = loadGrantedAl(row.rowKey(), 0);
			double occupiedCost = row.costPer() * activeLevel;
			refreshed.add(new GrantedRow(row.affinity(), row.name(), row.maxRank(), activeLevel, row.costPer(), occupiedCost,
					row.tech(), row.rowKey(), row.statusAttribute(), row.statusRatio()));
		}
		cachedGrantedRows = refreshed;
		return refreshed;
	}

	private double resolveCostPer(DataTraining tech) {
		if (tech == null || tech.getGrant() == null || dataQuery == null) return 0.0;
		for (Integer gid : tech.getGrant()) {
			if (gid == null) continue;
			DataTechPerm perm = dataQuery.getTechPermById(gid);
			if (perm == null) continue;
			if (perm.getCost() != 0) return perm.getCost();
			if (perm.getRatio() != 0) return perm.getRatio();
		}
		return 0.0;
	}

	private DataTechPerm resolveFirstPerm(DataTraining tech) {
		if (tech == null || tech.getGrant() == null || dataQuery == null) return null;
		for (Integer gid : tech.getGrant()) {
			if (gid == null) continue;
			DataTechPerm perm = dataQuery.getTechPermById(gid);
			if (perm != null) return perm;
		}
		return null;
	}

	private void ensureRowCapacity(int size) {
		while (mtName.size() < size) {
			JTextField affField = buildTextField("");
			JTextField nameField = buildTextField("");
			JFormattedTextField maxField = buildNumTextField(0);
			JFormattedTextField actField = buildNumTextField(0);
			JFormattedTextField costPerField = buildNumTextField(0.0);
			JFormattedTextField costField = buildNumTextField(0.0);

			styleMaxField(maxField);
			actField.setEditable(true);
			styleCostField(costPerField);
			styleOccField(costField);

			mtAffinity.add(affField);
			mtName.add(nameField);
			mtMax.add(maxField);
			mtActLevel.add(actField);
			mtCostPerAL.add(costPerField);
			mtCost.add(costField);
			mtTechRefs.add(null);
			mtRowKeys.add("");
			mtRowAffinities.add("");
		}
	}

	private void bindGrantedRow(int index, GrantedRow row) {
		JTextField affField = mtAffinity.get(index);
		if (!row.affinity().equals(affField.getText())) {
			affField.setText(row.affinity());
			styleAffinityField(affField, row.affinity());
		}
		affField.setVisible(!"None".equalsIgnoreCase(row.affinity()));

		if (!row.name().equals(mtName.get(index).getText())) {
			mtName.get(index).setText(row.name());
		}
		mtMax.get(index).setValue(row.maxRank());
		mtActLevel.get(index).setValue(row.activeLevel());
		mtCostPerAL.get(index).setValue(row.costPer());
		mtCost.get(index).setValue(row.occupiedCost());
		mtTechRefs.set(index, row.tech());
		mtRowKeys.set(index, row.rowKey());
		mtRowAffinities.set(index, row.affinity());

		mtName.get(index).setVisible(true);
		mtMax.get(index).setVisible(true);
		mtActLevel.get(index).setVisible(true);
		mtCostPerAL.get(index).setVisible(true);
		mtCost.get(index).setVisible(true);
	}

	private void hideUnusedRows(int usedCount) {
		for (int i = usedCount; i < mtName.size(); i++) {
			mtAffinity.get(i).setVisible(false);
			mtName.get(i).setVisible(false);
			mtMax.get(i).setVisible(false);
			mtActLevel.get(i).setVisible(false);
			mtCostPerAL.get(i).setVisible(false);
			mtCost.get(i).setVisible(false);
			mtTechRefs.set(i, null);
			mtRowKeys.set(i, "");
			mtRowAffinities.set(i, "");
		}
	}

	private void resetSectionMetadata() {
		mtSectionOrder.clear();
	}

	private void ensureSectionCapacity(int size) {
		while (mtSectionTitles.size() < size) {
			mtSectionTitles.add(buildLabel("", null));
			mtSectionAffinityL.add(buildLabel("", null));
			mtSectionNameL.add(buildLabel("", null));
			mtSectionMaxL.add(buildLabel("", null));
			mtSectionActLevelL.add(buildLabel("", null));
			mtSectionCostPerALL.add(buildLabel("", null));
			mtSectionCostL.add(buildLabel("", null));
		}
	}

	private void hideUnusedSections(int usedCount) {
		for (int i = usedCount; i < mtSectionTitles.size(); i++) {
			mtSectionTitles.get(i).setVisible(false);
			mtSectionAffinityL.get(i).setVisible(false);
			mtSectionNameL.get(i).setVisible(false);
			mtSectionMaxL.get(i).setVisible(false);
			mtSectionActLevelL.get(i).setVisible(false);
			mtSectionCostPerALL.get(i).setVisible(false);
			mtSectionCostL.get(i).setVisible(false);
		}
	}

	private void refreshAfterGrantedChange(boolean forceRebuild) {
		if (forceRebuild) {
			cachedStructureSignature = "";
		}
		synchronizeCharacterState();
		if (forceRebuild) {
			updateMaintained();
		} else {
			refreshGrantedValuesOnly();
		}
		if (sheetFrame != null) {
			sheetFrame.refreshTrainingPanel();
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshAllCharacterPanelHeaders();
		}
	}

	private void refreshGrantedValuesOnly() {
		ArrayList<GrantedRow> rows = refreshCachedGrantedRows();
		int limit = Math.min(rows.size(), mtActLevel.size());
		for (int i = 0; i < limit; i++) {
			GrantedRow row = rows.get(i);
			mtActLevel.get(i).setValue(row.activeLevel());
			mtCost.get(i).setValue(row.occupiedCost());
		}
		syncGrantOccupiedAuraFromOcc();
		refreshHPAuraOnly();
	}

	private String sanitizeVariantKey(String rawVariant) {
		StringBuilder safe = new StringBuilder(rawVariant.length());
		for (int i = 0; i < rawVariant.length(); i++) {
			char c = rawVariant.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') {
				safe.append(c);
			} else {
				safe.append('_');
			}
		}
		return safe.toString();
	}
	

	
}
