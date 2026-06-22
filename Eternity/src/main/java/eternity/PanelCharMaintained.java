package eternity;

import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class PanelCharMaintained extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_LOADOUT_NAME = "Default";
	private static final String LOADOUT_NAMES_KEY = "maintained.loadouts";
	private static final String ACTIVE_LOADOUT_KEY = "maintained.loadout.active";
	private static final String MOLDS_LIST = "Molds";
	private static final String MOLDING_MANIFEST_NOTE_PREFIX = "[MOLDING_MANIFEST]";
	private static final String MOLDING_WEAPON_BONUS_KEY = "TDMG";
	private static final String MOLDING_ARMOR_BONUS_KEY = "ARMOR";
	private static final int SHIFTER_SPECIAL_MOLD_DID = -9001;
	private static final String HARDEN_TECHNIQUE_NAME = "Harden";
	private static final String MAINTAINED_STATUS_PREFIX = "Panel Maintained: ";
	
	//maintained techniques
	private JLabel maintainedTechsL, mtNameL, mtMaxL, mtActLevelL, mtCostPerALL, mtCostL, mtAffinityL;
	private ArrayList<JTextField> mtName, mtAffinity;
	private ArrayList<JFormattedTextField> mtMax, mtActLevel, mtCostPerAL, mtCost;
	private ArrayList<DataTraining> mtTechRefs;
	private ArrayList<String> mtRowKeys;
	private ArrayList<String> mtAttrKeys;
	private ArrayList<Double> mtPermRatios;
	private ArrayList<String> mtRowAffinities;
	private ArrayList<String> mtSectionOrder;
	private ArrayList<JLabel> mtSectionTitles, mtSectionAffinityL, mtSectionNameL, mtSectionMaxL, mtSectionActLevelL, mtSectionCostPerALL, mtSectionCostL;
	private JLabel loadoutL;
	private JComboBox<String> loadoutBox;
	private JButton loadoutNewButton, loadoutSaveButton, mtUpdateButton, mtMaxButton, mtOffButton;
	private ArrayList<MaintainedRow> cachedMaintainedRows;
	private String cachedStructureSignature;
	private String currentLoadoutName;
	private boolean suppressLoadoutEvents;

	private record MaintainedRow(String affinity, String name, int maxRank, int activeLevel, double costPer, double occupiedCost,
			DataTraining tech, String attrKey, double permRatio, String normalizedKey, String resolvedCategory, String rowKey) {}
	
	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharMaintained (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(235, 225, 248));
			
		/**************
		* ***********		Maintained
		*/// ***********			
		maintainedTechsL = buildLabel("Maintained Techniques", null);
		mtNameL = buildLabel("Name", null);
		mtMaxL = buildLabel("Max", null);
		mtActLevelL = buildLabel("AL", null);
		mtCostPerALL = buildLabel("Cost", null);
		mtCostL = buildLabel("Occ", null);
		mtAffinityL = buildLabel("Affinity", null);

		mtName = new ArrayList<JTextField>();
		mtAffinity = new ArrayList<JTextField>();
		mtMax = new ArrayList<JFormattedTextField>();
		mtActLevel = new ArrayList<JFormattedTextField>();
		mtCostPerAL = new ArrayList<JFormattedTextField>();
		mtCost = new ArrayList<JFormattedTextField>();
		mtTechRefs = new ArrayList<DataTraining>();
		mtRowKeys = new ArrayList<String>();
		mtAttrKeys = new ArrayList<String>();
		mtPermRatios = new ArrayList<Double>();
		mtRowAffinities = new ArrayList<String>();
		mtSectionOrder = new ArrayList<String>();
		mtSectionTitles = new ArrayList<JLabel>();
		mtSectionAffinityL = new ArrayList<JLabel>();
		mtSectionNameL = new ArrayList<JLabel>();
		mtSectionMaxL = new ArrayList<JLabel>();
		mtSectionActLevelL = new ArrayList<JLabel>();
		mtSectionCostPerALL = new ArrayList<JLabel>();
		mtSectionCostL = new ArrayList<JLabel>();
		cachedMaintainedRows = new ArrayList<MaintainedRow>();
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
		mtUpdateButton = buildButton("Update");
		mtUpdateButton.addActionListener(e -> mtUpdate());
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
		maintainedTechsL.setVisible(false);
		loadoutL.setBounds(5, pageHeight, 60, 20);
		loadoutBox.setBounds(70, pageHeight, 180, 20);
		loadoutNewButton.setBounds(265, pageHeight, 80, 20);
		loadoutSaveButton.setBounds(355, pageHeight, 80, 20);
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

			if (!"None".equals(mtAffinity.get(i).getText())) {
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
		ArrayList<MaintainedRow> rows = getMaintainedRows();
		resetSectionMetadata();
		ensureRowCapacity(rows.size());
		hideUnusedRows(rows.size());
		for (int i = 0; i < rows.size(); i++) {
			bindMaintainedRow(i, rows.get(i));
		}
		applyMaintainedRowsToTraining(rows);
		buildAffinitySections();
		hideUnusedSections(mtSectionOrder.size());
		syncMainOccupiedAuraFromOcc();
		synchronizeCharacterState();
		applyDisplayedMaintainedStatuses(rows);
		refreshHPAuraOnly();
		resizeSheet();
	}

	@Override
	public void updateCharacter(StoreCharData character) {
		this.character = character;
		updateMaintained();
		refreshReminderOnly();
	}
	
	
	

	
	
	

	public void mtUpdate () {
		if (!canApplyProjectedMaintainedAura(calculateProjectedMaintainedOccupiedAuraFromFields())) {
			refreshMaintainedValuesOnly();
			refreshAllMaxFieldHighlights();
			resizeSheet();
			return;
		}
		if (character != null && mtActLevel != null && mtTechRefs != null) {
			for (int i = 0; i < mtActLevel.size() && i < mtTechRefs.size(); i++) {
				DataTraining tech = mtTechRefs.get(i);
				if (tech == null) continue;
				try {
					int newAl = ((Number) mtActLevel.get(i).getValue()).intValue();
					int safeAl = Math.max(0, newAl);
					tech.setAl(safeAl);
					saveMaintainedAl(i, safeAl);
				} catch (Exception ignored) {
					// leave AL unchanged on parse error
				}
				// Apply maintained-status scaling to the corresponding StatBlock
				if (character != null && character.getAttributes() != null &&
						mtAttrKeys != null && mtPermRatios != null &&
						i < mtAttrKeys.size() && i < mtPermRatios.size()) {
					String attrKey = mtAttrKeys.get(i);
					double ratio = mtPermRatios.get(i) != null ? mtPermRatios.get(i) : 0.0;
					if (attrKey != null && !attrKey.isBlank()) {
						double alVal = 0.0;
						try { alVal = ((Number) mtActLevel.get(i).getValue()).doubleValue(); } catch (Exception ignored) {}
						double sev = ratio * alVal;
						String normalized = normalizeAttrKey(attrKey);
						/*if (!applyMaintainedToResource(normalized, sev)) {
							String category = i < cachedMaintainedRows.size() && cachedMaintainedRows.get(i) != null
									? cachedMaintainedRows.get(i).resolvedCategory()
									: null;
							if (category != null) {
								character.getAttributes().setStatusSeverity(category, normalized, "Maintained", sev);
							}
						}*/
					}
				}
			}
			// Recompute occupied costs after AL changes
			for (int i = 0; i < mtCost.size() && i < mtCostPerAL.size() && i < mtActLevel.size(); i++) {
				try {
					double costPer = ((Number) mtCostPerAL.get(i).getValue()).doubleValue();
					double al = ((Number) mtActLevel.get(i).getValue()).doubleValue();
					mtCost.get(i).setValue(costPer * al);
				} catch (Exception ignored) {
					mtCost.get(i).setValue(0);
				}
			}
			syncTechniqueActiveLevelsFromDisplayedRows();
			syncMainOccupiedAuraFromOcc();
			refreshAfterMaintainedChange(false);
		}
		refreshAllMaxFieldHighlights();
	    resizeSheet();
		repaint();
	}
	
	public void mtMax () {
		double projectedOccupiedAura = calculateProjectedMaintainedOccupiedAuraFromMax();
		if (!canApplyProjectedMaintainedAura(projectedOccupiedAura)) {
			refreshMaintainedValuesOnly();
			refreshAllMaxFieldHighlights();
			resizeSheet();
			return;
		}
		// Set each Active Level to its Max value, persist to training data, and refresh costs.
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
			if (mtTechRefs != null && i < mtTechRefs.size() && mtTechRefs.get(i) != null) {
				mtTechRefs.get(i).setAl(newAl);
			}
			saveMaintainedAl(i, newAl);
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
			syncTechniqueActiveLevelsFromDisplayedRows();
			syncMainOccupiedAuraFromOcc();
			refreshAfterMaintainedChange(false);
		}
		refreshAllMaxFieldHighlights();
		resizeSheet();
		repaint();
	}
	
	public void mtOff () {
		// Set all active levels to zero and recompute occupied cost display
		for (int i = 0; i < mtActLevel.size(); i++) {
			JFormattedTextField alField = mtActLevel.get(i);
			alField.setValue(0);
			if (mtTechRefs != null && i < mtTechRefs.size() && mtTechRefs.get(i) != null) {
				mtTechRefs.get(i).setAl(0);
			}
			saveMaintainedAl(i, 0);
		}
		// Recalculate occupied cost columns based on cost per AL * new AL
		for (int i = 0; i < mtCost.size() && i < mtCostPerAL.size(); i++) {
			try {
				mtCost.get(i).setValue(0); // 0 AL so occ is 0
			} catch (Exception ignored) {
				mtCost.get(i).setValue(0);
			}
		}
		if (character != null) {
			syncTechniqueActiveLevelsFromDisplayedRows();
			syncMainOccupiedAuraFromOcc();
			refreshAfterMaintainedChange(false);
		}
		refreshAllMaxFieldHighlights();
		resizeSheet();
		repaint();
	}

	private void syncMainOccupiedAuraFromOcc() {
		if (character == null || character.getResources() == null || mtCost == null) return;
		double totalOcc = 0.0;
		for (JFormattedTextField occField : mtCost) {
			if (occField == null) continue;
			try {
				Number value = (Number) occField.getValue();
				if (value != null) totalOcc += value.doubleValue();
			} catch (Exception ignored) {
				// Ignore malformed field values.
			}
		}
		character.setResourceValue("MAINOCC", totalOcc);
	}

	private double calculateProjectedMaintainedOccupiedAura(Map<String, Integer> effectiveAlByTechnique, boolean useMaxFieldAsFallback) {
		double totalOcc = 0.0;
		int limit = Math.min(Math.min(mtActLevel.size(), mtCostPerAL.size()), mtTechRefs.size());
		for (int i = 0; i < limit; i++) {
			DataTraining tech = mtTechRefs.get(i);
			double costPer = safeDoubleFieldValue(mtCostPerAL.get(i));
			int fallbackAl = useMaxFieldAsFallback ? Math.max(0, safeIntFieldValue(mtMax.get(i))) : Math.max(0, safeIntFieldValue(mtActLevel.get(i)));
			int al = tech == null ? fallbackAl : Math.max(0, effectiveAlByTechnique.getOrDefault(getMaintainedTechniqueKey(tech), fallbackAl));
			totalOcc += costPer * al;
		}
		return totalOcc;
	}

	private double calculateProjectedMaintainedOccupiedAuraFromFields() {
		double totalOcc = 0.0;
		int limit = Math.min(mtActLevel.size(), mtCostPerAL.size());
		for (int i = 0; i < limit; i++) {
			double costPer = safeDoubleFieldValue(mtCostPerAL.get(i));
			int al = Math.max(0, safeIntFieldValue(mtActLevel.get(i)));
			totalOcc += costPer * al;
		}
		return totalOcc;
	}

	private double calculateProjectedMaintainedOccupiedAuraFromMax() {
		double totalOcc = 0.0;
		int limit = Math.min(mtMax.size(), mtCostPerAL.size());
		for (int i = 0; i < limit; i++) {
			double costPer = safeDoubleFieldValue(mtCostPerAL.get(i));
			int al = Math.max(0, safeIntFieldValue(mtMax.get(i)));
			totalOcc += costPer * al;
		}
		return totalOcc;
	}

	private boolean canApplyProjectedMaintainedAura(double projectedMainOccupiedAura) {
		if (character == null || character.getResources() == null) return true;
		CharResources resources = character.getResources();
		double availableAfter = resources.calcMaxAura()
				- resources.getSpentAura()
				- resources.getGrantOccupiedAura()
				- Math.max(0.0, projectedMainOccupiedAura);
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

	private String getMaintainedTechniqueKey(DataTraining tech) {
		if (tech == null) return "maintained.unknown";
		if (tech.getId() > 0) return "maintained.tech." + tech.getId();
		String name = tech.getName() == null ? "" : tech.getName().trim().toLowerCase();
		String affinity = tech.getAffinity() == null ? "" : tech.getAffinity().trim().toLowerCase();
		return "maintained.tech." + name + "." + affinity;
	}
	
	private String resolveCategory(CharAttributes attrs, String key) {
		if (attrs == null || key == null) return null;
		String norm = key.toUpperCase();
		String[] cats = { "attribute", "defense", "resist", "combat", "secondary", "damage" };
		for (String c : cats) {
			//if (attrs.getBlock(c, norm) != null) return c;
		}
		return null;
	}

	private String normalizeAttrKey(String key) {
		if (key == null) return null;
		String upper = key.toUpperCase();
		if ("APPLY".equals(upper)) return "APP";
		if ("IMPAIR".equals(upper)) return "IMP";
		if ("RESPHY".equals(upper)) return "PHY";
		if (upper.startsWith("RESIST")) return upper.substring("RESIST".length());
		if (upper.startsWith("RES") && upper.length() > 3) return upper.substring(3);
		return upper;
	}

	/*private boolean applyMaintainedToResource(String key, double severity) {
		if (character == null || character.getResources() == null || key == null) return false;
		if ("MAXHP".equals(key)) {
			upsertStatusSeverity(character.getResources().getMaxHPBlocks(), "Maintained", "HP", severity);
			return true;
		}
		if ("MAXAURA".equals(key)) {
			upsertStatusSeverity(character.getResources().getMaxAuraBlocks(), "Maintained", "AURA", severity);
			return true;
		}
		if ("HPMULTI".equals(key)) {
			upsertMultiSeverity(character.getResources().getMaxHPBlocks(), "Maintained", "HPMULTI", severity);
			return true;
		}
		if ("AURAMULTI".equals(key)) {
			upsertMultiSeverity(character.getResources().getMaxAuraBlocks(), "Maintained", "AURAMULTI", severity);
			return true;
		}
		return false;
	}*/

	private void upsertStatusSeverity(StatBlock[] blocks, String name, String attr, double severity) {
		if (blocks == null || blocks.length == 0 || blocks[0] == null) return;
		StatBlock block = blocks[0];
		DataStatus ds = new DataStatus();
		ds.setName(name);
		ds.setAttribute(attr);
		ds.setDurationType("Permanent");
		ds.setSeverity(severity);
		ds.setAffinity("None");
		ds.setDescription("Maintained technique effect");
		//block.addStatus(ds);
	}

	private void upsertMultiSeverity(StatBlock[] blocks, String name, String attr, double severity) {
		if (blocks == null || blocks.length == 0 || blocks[0] == null) return;
		StatBlock block = blocks[0];
		DataStatus ds = new DataStatus();
		ds.setName(name);
		ds.setAttribute(attr);
		ds.setDurationType("Permanent");
		ds.setSeverity(severity);
		ds.setAffinity("None");
		ds.setDescription("Maintained technique effect");
		//block.addMulti(ds);
	}

	private void styleMaxField(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(false);
		field.setFocusable(false);
		field.setEnabled(false);
		field.setOpaque(true);
		field.setBackground(new Color(226, 236, 250));
		field.setForeground(new Color(22, 50, 87));
		field.setDisabledTextColor(new Color(22, 50, 87));
		field.setSelectedTextColor(new Color(22, 50, 87));
		field.setCaretColor(new Color(22, 50, 87));
	}

	private void styleMaxFieldMatched(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(false);
		field.setFocusable(false);
		field.setEnabled(false);
		field.setOpaque(true);
		field.setBackground(new Color(226, 236, 250));
		field.setForeground(new Color(0, 140, 255));
		field.setDisabledTextColor(new Color(0, 140, 255));
		field.setSelectedTextColor(new Color(0, 140, 255));
		field.setCaretColor(new Color(0, 140, 255));
	}

	private void styleActLevelField(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(true);
		field.setEnabled(true);
		field.setFocusable(true);
		field.setForeground(Color.BLACK);
		field.setDisabledTextColor(Color.BLACK);
		field.setSelectedTextColor(Color.BLACK);
		field.setCaretColor(Color.BLACK);
	}

	private void styleActLevelFieldMatched(JFormattedTextField field) {
		if (field == null) return;
		field.setEditable(true);
		field.setEnabled(true);
		field.setFocusable(true);
		field.setForeground(new Color(0, 140, 255));
		field.setDisabledTextColor(new Color(0, 140, 255));
		field.setSelectedTextColor(new Color(0, 140, 255));
		field.setCaretColor(new Color(0, 140, 255));
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
		refreshAfterMaintainedChange(false);
	}

	private void createLoadout() {
		if (character == null) return;
		String rawName = JOptionPane.showInputDialog(this, "New maintained loadout name:", "Create Loadout", JOptionPane.PLAIN_MESSAGE);
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

	private String buildMaintainedAlKey(DataTraining tech, String variant) {
		if (tech == null) return "maintained.al.unknown";
		String rawVariant = (variant == null || variant.isBlank()) ? "base" : variant.trim().toLowerCase();
		return "maintained.al." + tech.getId() + "." + sanitizeVariantKey(rawVariant);
	}

	private String buildLoadoutValueKey(String loadoutName, String rowKey) {
		String safeLoadout = sanitizeVariantKey(loadoutName == null ? DEFAULT_LOADOUT_NAME.toLowerCase() : loadoutName.trim().toLowerCase());
		return "maintained.loadout." + safeLoadout + "." + rowKey;
	}

	private int loadMaintainedAl(String rowKey, int fallback) {
		if (character == null || rowKey == null || rowKey.isBlank()) return Math.max(0, fallback);
		String raw = character.getReminderSelection(buildLoadoutValueKey(currentLoadoutName, rowKey));
		if (raw == null || raw.isBlank()) return Math.max(0, fallback);
		try {
			return Math.max(0, Integer.parseInt(raw.trim()));
		} catch (NumberFormatException ignored) {
			return Math.max(0, fallback);
		}
	}

	private void saveMaintainedAl(int rowIndex, int al) {
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

	private void applyMaintainedRowsToTraining(List<MaintainedRow> rows) {
		if (rows == null) return;
		Map<String, Integer> maxByTechnique = new LinkedHashMap<>();
		Map<String, DataTraining> techByKey = new LinkedHashMap<>();
		for (MaintainedRow row : rows) {
			if (row == null || row.tech() == null) continue;
			String key = getMaintainedTechniqueKey(row.tech());
			maxByTechnique.merge(key, Math.max(0, row.activeLevel()), Math::max);
			techByKey.putIfAbsent(key, row.tech());
		}
		for (Map.Entry<String, DataTraining> entry : techByKey.entrySet()) {
			DataTraining tech = entry.getValue();
			if (tech == null) continue;
			tech.setAl(Math.max(0, maxByTechnique.getOrDefault(entry.getKey(), 0)));
		}
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

	private ArrayList<MaintainedRow> getMaintainedRows() {
		String structureSignature = buildStructureSignature();
		if (!structureSignature.equals(cachedStructureSignature)) {
			cachedMaintainedRows = collectMaintainedRows();
			cachedStructureSignature = structureSignature;
			return cachedMaintainedRows;
		}
		return refreshCachedMaintainedRows();
	}

	private ArrayList<MaintainedRow> collectMaintainedRows() {
		ArrayList<MaintainedRow> rows = new ArrayList<>();
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
			boolean hasProgress = tech.getRank() > 0 || tech.getExp() > 0 || tech.getAl() > 0;
			if (maxRank <= 0 && !hasProgress) continue;
			int activeLevel = Math.max(0, tech.getAl());
			boolean isStandardAffinity = "Standard".equalsIgnoreCase(tech.getAffinity()) && tech.getGrant() != null && !tech.getGrant().isEmpty();

			if (isStandardAffinity && dataQuery != null) {
				boolean addedAny = false;
				for (Integer gid : tech.getGrant()) {
					if (gid == null) continue;
					DataTechPerm perm = dataQuery.getTechPermById(gid);
					String attr = (perm != null && perm.getAttribute() != null) ? perm.getAttribute() : "None";
					String rowName = shouldAppendMaintainedSubtype(tech)
							? tech.getName() + " (" + attr + ")"
							: tech.getName();
					double costPer = resolveCostPer(perm);
					String normalizedKey = normalizeAttrKey(attr);
					String resolvedCategory = resolveCategory(character != null ? character.getAttributes() : null, normalizedKey);
					String rowKey = buildMaintainedAlKey(tech, attr);
					int rowActiveLevel = loadMaintainedAl(rowKey, activeLevel);
					rows.add(new MaintainedRow(tech.getAffinity(), rowName, tech.getRank(), rowActiveLevel, costPer, costPer * rowActiveLevel,
							tech, attr == null ? null : attr.toUpperCase(), perm != null ? perm.getRatio() : 0.0, normalizedKey, resolvedCategory, rowKey));
					addedAny = true;
				}
				if (addedAny) continue;
			}

			DataTechPerm perm = resolveFirstPerm(tech);
			String mappedAttr = perm != null ? perm.getAttribute() : null;
			double mappedRatio = perm != null ? perm.getRatio() : 0.0;
			double costPer = resolveCostPer(perm);
			String normalizedKey = normalizeAttrKey(mappedAttr);
			String resolvedCategory = resolveCategory(character != null ? character.getAttributes() : null, normalizedKey);
			String rowKey = buildMaintainedAlKey(tech, mappedAttr);
			int rowActiveLevel = loadMaintainedAl(rowKey, activeLevel);
			rows.add(new MaintainedRow(tech.getAffinity(), tech.getName(), tech.getRank(), rowActiveLevel, costPer, costPer * rowActiveLevel,
					tech, mappedAttr == null ? null : mappedAttr.toUpperCase(), mappedRatio, normalizedKey, resolvedCategory, rowKey));
		}
		return rows;
	}

	private boolean shouldAppendMaintainedSubtype(DataTraining tech) {
		if (tech == null || tech.getName() == null) return false;
		return !HARDEN_TECHNIQUE_NAME.equalsIgnoreCase(tech.getName().trim());
	}

	private ArrayList<MaintainedRow> refreshCachedMaintainedRows() {
		ArrayList<MaintainedRow> refreshed = new ArrayList<>(cachedMaintainedRows.size());
		for (MaintainedRow row : cachedMaintainedRows) {
			if (row == null) continue;
			DataTraining tech = row.tech();
			int fallbackAl = tech == null ? 0 : Math.max(0, tech.getAl());
			int activeLevel = loadMaintainedAl(row.rowKey(), fallbackAl);
			double occupiedCost = row.costPer() * activeLevel;
			refreshed.add(new MaintainedRow(row.affinity(), row.name(), row.maxRank(), activeLevel, row.costPer(), occupiedCost,
					row.tech(), row.attrKey(), row.permRatio(), row.normalizedKey(), row.resolvedCategory(), row.rowKey()));
		}
		cachedMaintainedRows = refreshed;
		return refreshed;
	}

	private String buildStructureSignature() {
		if (character == null || character.getTraining() == null) return "";
		StringBuilder signature = new StringBuilder();
		for (DataTraining tech : character.getTraining().getAllTraining()) {
			if (tech == null || !"Maintained".equalsIgnoreCase(tech.getType())) continue;
			int maxRank = tech.getMaxRank(character);
			boolean hasProgress = tech.getRank() > 0 || tech.getExp() > 0 || tech.getAl() > 0;
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
			styleActLevelField(actField);
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
			mtAttrKeys.add(null);
			mtPermRatios.add(0.0);
			mtRowAffinities.add("");
		}
	}

	private void bindMaintainedRow(int index, MaintainedRow row) {
		JTextField affField = mtAffinity.get(index);
		if (!row.affinity().equals(affField.getText())) {
			affField.setText(row.affinity());
			styleAffinityField(affField, row.affinity());
		}
		affField.setVisible(!"None".equalsIgnoreCase(row.affinity()));

		mtName.get(index).setText(row.name());
		mtMax.get(index).setValue(row.maxRank());
		mtActLevel.get(index).setValue(row.activeLevel());
		mtCostPerAL.get(index).setValue(row.costPer());
		mtCost.get(index).setValue(row.occupiedCost());
		mtTechRefs.set(index, row.tech());
		mtRowKeys.set(index, row.rowKey());
		mtAttrKeys.set(index, row.attrKey());
		mtPermRatios.set(index, row.permRatio());
		mtRowAffinities.set(index, row.affinity());

		mtName.get(index).setVisible(true);
		mtMax.get(index).setVisible(true);
		mtActLevel.get(index).setVisible(true);
		mtCostPerAL.get(index).setVisible(true);
		mtCost.get(index).setVisible(true);
		refreshMaxFieldHighlight(index);
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
			mtAttrKeys.set(i, null);
			mtPermRatios.set(i, 0.0);
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

	private DataTechPerm resolveFirstPerm(DataTraining tech) {
		if (tech == null || tech.getGrant() == null || dataQuery == null) return null;
		for (Integer gid : tech.getGrant()) {
			if (gid == null) continue;
			DataTechPerm perm = dataQuery.getTechPermById(gid);
			if (perm != null) return perm;
		}
		return null;
	}

	private double resolveCostPer(DataTechPerm perm) {
		if (perm == null) return 0.0;
		if (perm.getCost() != 0) return perm.getCost();
		if (perm.getRatio() != 0) return perm.getRatio();
		return 0.0;
	}

	private void refreshAfterMaintainedChange(boolean forceRebuild) {
		if (forceRebuild) {
			cachedStructureSignature = "";
		}
		synchronizeCharacterState();
		ArrayList<MaintainedRow> rows = forceRebuild ? getMaintainedRows() : refreshCachedMaintainedRows();
		applyDisplayedMaintainedStatuses(rows);
		if (forceRebuild) {
			updateMaintained();
		} else {
			refreshMaintainedValuesOnly();
		}
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshTrainingPanel();
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshInventoryPanel();
			sheetFrame.refreshAllCharacterPanelHeaders();
		}
	}

	private Map<Integer, Integer> snapshotMoldingActiveLevels() {
		Map<Integer, Integer> levels = new LinkedHashMap<>();
		if (character == null || character.getTraining() == null) return levels;
		for (DataTraining tech : character.getTraining().getAllTraining()) {
			if (!isMoldingTechnique(tech)) continue;
			levels.put(tech.getId(), Math.max(0, tech.getAl()));
		}
		return levels;
	}

	private void applyMoldingArrangementIfNeeded(Map<Integer, Integer> previousMoldingLevels) {
		if (character == null || character.getInventory() == null) return;
		Map<Integer, Integer> currentMoldingLevels = snapshotMoldingActiveLevels();
		if (previousMoldingLevels != null && previousMoldingLevels.equals(currentMoldingLevels)) return;

		clearExistingMoldingManifestItems();

		int totalActiveMoldingAl = 0;
		int highestMoldingRank = 0;
		for (DataTraining tech : character.getTraining().getAllTraining()) {
			if (!isMoldingTechnique(tech)) continue;
			totalActiveMoldingAl += Math.max(0, tech.getAl());
			highestMoldingRank = Math.max(highestMoldingRank, Math.max(0, tech.getRank()));
		}
		if (totalActiveMoldingAl <= 0 || highestMoldingRank <= 0) return;

		ArrayList<String> moldNames = getListEntries(MOLDS_LIST);
		if (moldNames.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No entries are available in the Molds list.", "Molding", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int remainingAl = totalActiveMoldingAl;
		while (remainingAl > 0) {
			ArrayList<String> availableMolds = getAvailableMoldingChoices(moldNames);
			MoldingManifestChoice choice = promptForMoldingManifestChoice(availableMolds, remainingAl, highestMoldingRank);
			if (choice == null) {
				int confirm = JOptionPane.showConfirmDialog(
						this,
						"End molding arrangement with " + remainingAl + " AL unspent?",
						"End Molding Arrangement",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (confirm == JOptionPane.YES_OPTION) {
					break;
				}
				continue;
			}
			manifestMold(choice.moldName(), choice.al());
			remainingAl -= choice.al();
		}
	}

	private boolean isMoldingTechnique(DataTraining tech) {
		if (tech == null || tech.getName() == null) return false;
		return tech.getName().trim().toLowerCase().endsWith(" molding");
	}

	private ArrayList<String> getListEntries(String listName) {
		ArrayList<String> entries = new ArrayList<>();
		if (character == null || character.getLists() == null || listName == null || listName.isBlank()) return entries;
		LinkedHashSet<String> deduped = new LinkedHashSet<>();
		for (List<DataList> group : character.getLists()) {
			if (group == null) continue;
			for (DataList entry : group) {
				if (entry == null || entry.getList() == null || entry.getName() == null) continue;
				if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
				String name = entry.getName().trim();
				if (!name.isBlank()) deduped.add(name);
			}
		}
		entries.addAll(deduped);
		return entries;
	}

	private ArrayList<String> getAvailableMoldingChoices(List<String> moldNames) {
		ArrayList<String> available = new ArrayList<>();
		if (moldNames == null) return available;
		int level = character == null ? 0 : Math.max(0, character.getLevel());
		for (String moldName : moldNames) {
			if (moldName == null || moldName.isBlank()) continue;
			if (StoreCharData.isShifterSpecialMoldName(moldName)) {
				if (resolveShifterSpecialMoldAlCap(level) < 1) continue;
				if (hasManifestedShifterSpecialMold(moldName)) continue;
			}
			available.add(moldName);
		}
		return available;
	}

	private MoldingManifestChoice promptForMoldingManifestChoice(List<String> moldNames, int remainingAl, int highestMoldingRank) {
		if (moldNames == null || moldNames.isEmpty()) return null;
		JComboBox<String> moldBox = new JComboBox<>(moldNames.toArray(new String[0]));
		JComboBox<Integer> alBox = new JComboBox<>();
		populateMoldingAlChoices(alBox, getMaxAllowedMoldingAl(moldBox.getSelectedItem(), remainingAl, highestMoldingRank));
		moldBox.addActionListener(e -> populateMoldingAlChoices(alBox, getMaxAllowedMoldingAl(moldBox.getSelectedItem(), remainingAl, highestMoldingRank)));

		JPanel panel = new JPanel();
		panel.add(new JLabel("Mold"));
		panel.add(moldBox);
		panel.add(new JLabel("AL"));
		panel.add(alBox);

		int result = JOptionPane.showOptionDialog(
				this,
				panel,
				"Arrange Molding (" + remainingAl + " AL remaining)",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				new Object[] {"Manifest", "Finish"},
				"Manifest");
		if (result != 0) return null;

		String moldName = moldBox.getSelectedItem() == null ? "" : moldBox.getSelectedItem().toString().trim();
		if (moldName.isBlank()) {
			JOptionPane.showMessageDialog(this, "Select a mold entry.", "Molding", JOptionPane.WARNING_MESSAGE);
			return promptForMoldingManifestChoice(moldNames, remainingAl, highestMoldingRank);
		}
		int maxAllowedAl = getMaxAllowedMoldingAl(moldName, remainingAl, highestMoldingRank);
		if (maxAllowedAl <= 0) {
			JOptionPane.showMessageDialog(this, "That mold cannot currently be manifested.", "Molding", JOptionPane.WARNING_MESSAGE);
			return promptForMoldingManifestChoice(moldNames, remainingAl, highestMoldingRank);
		}

		Object alSelection = alBox.getSelectedItem();
		int al = alSelection instanceof Integer ? (Integer) alSelection : 0;
		if (al <= 0 || al > maxAllowedAl) {
			JOptionPane.showMessageDialog(this, "AL must be between 1 and " + maxAllowedAl + ".", "Molding", JOptionPane.WARNING_MESSAGE);
			return promptForMoldingManifestChoice(moldNames, remainingAl, highestMoldingRank);
		}

		return new MoldingManifestChoice(moldName, al);
	}

	private void populateMoldingAlChoices(JComboBox<Integer> alBox, int maxAllowedAl) {
		alBox.removeAllItems();
		for (int i = 1; i <= maxAllowedAl; i++) {
			alBox.addItem(i);
		}
		if (maxAllowedAl > 0) {
			alBox.setSelectedItem(maxAllowedAl);
		}
	}

	private int getMaxAllowedMoldingAl(Object moldSelection, int remainingAl, int highestMoldingRank) {
		int maxAllowedAl = Math.min(remainingAl, highestMoldingRank);
		if (maxAllowedAl <= 0) return 0;
		String moldName = moldSelection == null ? "" : moldSelection.toString().trim();
		if (!StoreCharData.isShifterSpecialMoldName(moldName)) {
			return maxAllowedAl;
		}
		int level = character == null ? 0 : Math.max(0, character.getLevel());
		return Math.min(maxAllowedAl, resolveShifterSpecialMoldAlCap(level));
	}

	private int resolveShifterSpecialMoldAlCap(int level) {
		return (int) Math.floor(level * 0.5);
	}

	private void manifestMold(String moldName, int al) {
		if (moldName == null || moldName.isBlank() || al <= 0 || character == null || character.getInventory() == null) return;
		int targetTier = Math.max(0, al - 1);
		if (manifestShifterSpecialMold(moldName, targetTier, al)) {
			return;
		}

		MoldEntryMetadata metadata = resolveMoldEntryMetadata(moldName);
		DataItemWeapon weaponTemplate = resolveWeaponTemplate(moldName, metadata, targetTier);
		if (weaponTemplate != null) {
			DataItemWeapon weapon = new DataItemWeapon(weaponTemplate);
			weapon.setIname("Molded " + moldName);
			weapon.setInote(buildMoldingManifestNote(MOLDING_WEAPON_BONUS_KEY, al, al));
			weapon.setTier(targetTier);
			weapon.setEquipped(false);
			weapon.setQuantity(1.0);
			character.getInventory().addWeapon(weapon);
			return;
		}

		DataItemEquipment armorTemplate = resolveArmorTemplate(moldName, metadata, targetTier);
		if (armorTemplate != null) {
			DataItemEquipment armor = new DataItemEquipment(armorTemplate);
			armor.setIname("Molded " + moldName);
			armor.setInote(buildMoldingManifestNote(MOLDING_ARMOR_BONUS_KEY, al * 0.5, al));
			armor.setTier(targetTier);
			armor.setEquipped(false);
			armor.setQuantity(1.0);
			character.getInventory().addEquipment(armor);
			return;
		}

		DataItem item = new DataItem();
		item.setDid(-1);
		item.setIid(-1);
		item.setDname("Molded " + moldName);
		item.setInote(buildMoldingManifestNote("", 0.0, al));
		item.setQuantity(1.0);
		character.getInventory().addItem(item);
	}

	private boolean manifestShifterSpecialMold(String moldName, int targetTier, int al) {
		if (!StoreCharData.isShifterSpecialMoldName(moldName)) return false;
		String slot = StoreCharData.resolveShifterSpecialMoldSlot(moldName);
		if (slot == null || slot.isBlank()) return false;
		DataItemEquipment armor = new DataItemEquipment();
		String displayName = "Molded " + moldName;
		armor.setDid(SHIFTER_SPECIAL_MOLD_DID);
		armor.setIid(-1);
		armor.setDname(displayName);
		armor.setIname(displayName);
		armor.setSlot(slot);
		armor.setTier(targetTier);
		armor.setCategory("Armor");
		armor.setType("Shifter");
		armor.setBonusAtt("ARMOR");
		armor.setBonusAmount(0.0);
		armor.setLevelReq(1);
		armor.setValue(0L);
		armor.setInote(buildMoldingManifestNote(MOLDING_ARMOR_BONUS_KEY, al * 0.5, al));
		armor.setEquipped(false);
		armor.setQuantity(1.0);
		character.getInventory().addEquipment(armor);
		return true;
	}

	private MoldEntryMetadata resolveMoldEntryMetadata(String moldName) {
		if (character == null || character.getLists() == null || moldName == null || moldName.isBlank()) return null;
		for (List<DataList> group : character.getLists()) {
			if (group == null) continue;
			for (DataList entry : group) {
				if (entry == null || entry.getList() == null || entry.getName() == null) continue;
				if (!MOLDS_LIST.equalsIgnoreCase(entry.getList().trim())) continue;
				if (!moldName.equalsIgnoreCase(entry.getName().trim())) continue;
				return parseMoldEntryMetadata(entry.getDescription());
			}
		}
		return null;
	}

	private MoldEntryMetadata parseMoldEntryMetadata(String description) {
		if (description == null || description.isBlank()) return null;
		String category = "";
		String type = "";
		String slot = "";
		for (String part : description.split("\\|")) {
			if (part == null) continue;
			String trimmed = part.trim();
			if (trimmed.regionMatches(true, 0, "CATEGORY=", 0, "CATEGORY=".length())) {
				category = trimmed.substring("CATEGORY=".length()).trim();
			} else if (trimmed.regionMatches(true, 0, "TYPE=", 0, "TYPE=".length())) {
				type = trimmed.substring("TYPE=".length()).trim();
			} else if (trimmed.regionMatches(true, 0, "SLOT=", 0, "SLOT=".length())) {
				slot = trimmed.substring("SLOT=".length()).trim();
			}
		}
		if (category.isBlank() && type.isBlank() && slot.isBlank()) return null;
		return new MoldEntryMetadata(category, type, slot);
	}

	private boolean hasManifestedShifterSpecialMold(String moldName) {
		if (character == null || character.getInventory() == null || moldName == null || moldName.isBlank()) return false;
		String displayName = "Molded " + moldName.trim();
		for (DataItemEquipment item : character.getInventory().getEquipment()) {
			if (item == null) continue;
			if (!isMoldingManifestItem(item.getInote())) continue;
			String dname = item.getDname() == null ? "" : item.getDname().trim();
			String iname = item.getIname() == null ? "" : item.getIname().trim();
			if (displayName.equalsIgnoreCase(dname) || displayName.equalsIgnoreCase(iname)) {
				return true;
			}
		}
		return false;
	}

	private DataItemWeapon resolveWeaponTemplate(String moldName, MoldEntryMetadata metadata, int targetTier) {
		if (metadata != null) {
			if (!"Weapon".equalsIgnoreCase(metadata.category())) return null;
			if (!metadata.type().isBlank()) {
				return pickBestWeaponTemplate(dataQuery == null ? List.of() : dataQuery.getItemWeaponData(), metadata.type(), targetTier);
			}
		}
		List<DataItemWeapon> candidates = dataQuery == null ? List.of() : dataQuery.getItemWeaponData();
		return pickBestWeaponTemplate(candidates, moldName, targetTier);
	}

	private DataItemEquipment resolveArmorTemplate(String moldName, MoldEntryMetadata metadata, int targetTier) {
		if (metadata != null) {
			if (!"Armor".equalsIgnoreCase(metadata.category())) return null;
			if (!metadata.type().isBlank() && !metadata.slot().isBlank()) {
				return pickBestArmorTemplate(dataQuery == null ? List.of() : dataQuery.getItemEquipmentData(),
						metadata.type(), metadata.slot(), targetTier);
			}
		}
		List<DataItemEquipment> candidates = dataQuery == null ? List.of() : dataQuery.getItemEquipmentData();
		return pickBestArmorTemplate(candidates, moldName, targetTier);
	}

	private DataItemWeapon pickBestWeaponTemplate(List<DataItemWeapon> candidates, String moldName, int targetTier) {
		if (candidates == null || moldName == null || moldName.isBlank()) return null;
		DataItemWeapon bestExactTier = null;
		DataItemWeapon bestFallback = null;
		for (DataItemWeapon candidate : candidates) {
			if (candidate == null) continue;
			if (!matchesMoldName(candidate.getDname(), candidate.getType(), moldName)) continue;
			if (candidate.getTier() == targetTier && bestExactTier == null) {
				bestExactTier = candidate;
			}
			if (bestFallback == null || Math.abs(candidate.getTier() - targetTier) < Math.abs(bestFallback.getTier() - targetTier)) {
				bestFallback = candidate;
			}
		}
		return bestExactTier != null ? bestExactTier : bestFallback;
	}

	private DataItemEquipment pickBestArmorTemplate(List<DataItemEquipment> candidates, String moldName, int targetTier) {
		if (candidates == null || moldName == null || moldName.isBlank()) return null;
		DataItemEquipment bestExactTier = null;
		DataItemEquipment bestFallback = null;
		for (DataItemEquipment candidate : candidates) {
			if (candidate == null) continue;
			if (!"Armor".equalsIgnoreCase(candidate.getCategory())) continue;
			if (!matchesMoldName(candidate.getDname(), candidate.getType(), moldName)) continue;
			if (candidate.getTier() == targetTier && bestExactTier == null) {
				bestExactTier = candidate;
			}
			if (bestFallback == null || Math.abs(candidate.getTier() - targetTier) < Math.abs(bestFallback.getTier() - targetTier)) {
				bestFallback = candidate;
			}
		}
		return bestExactTier != null ? bestExactTier : bestFallback;
	}

	private DataItemEquipment pickBestArmorTemplate(List<DataItemEquipment> candidates, String armorType, String armorSlot, int targetTier) {
		if (candidates == null || armorType == null || armorType.isBlank() || armorSlot == null || armorSlot.isBlank()) return null;
		DataItemEquipment bestExactTier = null;
		DataItemEquipment bestFallback = null;
		for (DataItemEquipment candidate : candidates) {
			if (candidate == null) continue;
			if (!"Armor".equalsIgnoreCase(candidate.getCategory())) continue;
			if (candidate.getType() == null || !armorType.equalsIgnoreCase(candidate.getType().trim())) continue;
			if (candidate.getSlot() == null || !armorSlot.equalsIgnoreCase(candidate.getSlot().trim())) continue;
			if (candidate.getTier() == targetTier && bestExactTier == null) {
				bestExactTier = candidate;
			}
			if (bestFallback == null || Math.abs(candidate.getTier() - targetTier) < Math.abs(bestFallback.getTier() - targetTier)) {
				bestFallback = candidate;
			}
		}
		return bestExactTier != null ? bestExactTier : bestFallback;
	}

	private boolean matchesMoldName(String dname, String type, String moldName) {
		String target = moldName == null ? "" : moldName.trim();
		if (target.isBlank()) return false;
		if (dname != null && dname.trim().equalsIgnoreCase(target)) return true;
		return type != null && type.trim().equalsIgnoreCase(target);
	}

	private record MoldEntryMetadata(String category, String type, String slot) {}

	private String buildMoldingManifestNote(String bonusKey, double bonusAmount, int al) {
		return MOLDING_MANIFEST_NOTE_PREFIX + "|AL=" + al + "|BONUS=" + (bonusKey == null ? "" : bonusKey) + "|AMOUNT=" + bonusAmount;
	}

	private void clearExistingMoldingManifestItems() {
		if (character == null || character.getInventory() == null) return;
		CharInventory inventory = character.getInventory();
		ArrayList<DataItemEquipment> equipmentToRemove = new ArrayList<>();
		for (DataItemEquipment item : inventory.getEquipment()) {
			if (item != null && isMoldingManifestItem(item.getInote())) equipmentToRemove.add(item);
		}
		for (DataItemEquipment item : equipmentToRemove) {
			inventory.removeEquipment(item);
		}

		ArrayList<DataItem> itemsToRemove = new ArrayList<>();
		for (DataItem item : inventory.getItems()) {
			if (item != null && isMoldingManifestItem(item.getInote())) itemsToRemove.add(item);
		}
		for (DataItem item : itemsToRemove) {
			inventory.removeItem(item);
		}
	}

	private boolean isMoldingManifestItem(String note) {
		return note != null && note.startsWith(MOLDING_MANIFEST_NOTE_PREFIX);
	}

	private record MoldingManifestChoice(String moldName, int al) {}

	private void syncTechniqueActiveLevelsFromDisplayedRows() {
		if (mtTechRefs == null || mtActLevel == null) return;
		Map<String, Integer> maxByTechnique = new LinkedHashMap<>();
		Map<String, DataTraining> techByKey = new LinkedHashMap<>();
		int limit = Math.min(mtTechRefs.size(), mtActLevel.size());
		for (int i = 0; i < limit; i++) {
			DataTraining tech = mtTechRefs.get(i);
			if (tech == null) continue;
			String key = getMaintainedTechniqueKey(tech);
			int value = Math.max(0, safeIntFieldValue(mtActLevel.get(i)));
			maxByTechnique.merge(key, value, Math::max);
			techByKey.putIfAbsent(key, tech);
		}
		for (Map.Entry<String, DataTraining> entry : techByKey.entrySet()) {
			DataTraining tech = entry.getValue();
			if (tech == null) continue;
			tech.setAl(Math.max(0, maxByTechnique.getOrDefault(entry.getKey(), 0)));
		}
	}

	private void applyDisplayedMaintainedStatuses(List<MaintainedRow> rows) {
		clearDisplayedMaintainedStatuses();
		if (character == null || rows == null) return;
		for (MaintainedRow row : rows) {
			if (row == null || row.activeLevel() <= 0 || row.attrKey() == null || row.attrKey().isBlank()) continue;
			String attr = normalizeAttrKey(row.attrKey());
			if (attr == null || attr.isBlank()) continue;
			double severity = row.permRatio() * character.getEffectiveTechniqueAl(row.affinity(), row.activeLevel());
			if (Math.abs(severity) < 0.0001) continue;
			String statusName = MAINTAINED_STATUS_PREFIX + row.rowKey();
			String description = row.name() + " maintained effect";
			if (applyMaintainedResourceStatus(statusName, attr, severity, description)) continue;
			if (applyMaintainedMultiplierAliases(statusName, attr, severity, description)) continue;
			addMaintainedAttributeStatus(statusName, "B" + attr, severity, description);
		}
		if (character.getAttributes() != null) {
			character.getAttributes().refreshLinkedAttributeStatuses();
		}
	}

	private void clearDisplayedMaintainedStatuses() {
		if (character == null) return;
		if (character.getAttributes() != null) {
			clearStatusPrefix(character.getAttributes().getBAttributes(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMAttributes(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getBDefense(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMDefense(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getBResist(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMResist(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getBCombat(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMCombat(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getBSecondary(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMSecondary(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getBDamage(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMDamage(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getBSkill(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getAttributes().getMSkill(), MAINTAINED_STATUS_PREFIX);
		}
		if (character.getResources() != null) {
			clearStatusPrefix(character.getResources().getBaseHP(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiHP(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getBaseAura(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiAura(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getBaseResource1(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiResource1(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getBaseResource2(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiResource2(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getBaseResource3(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiResource3(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getBaseAngelPoints(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiAngelPoints(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getBaseReactions(), MAINTAINED_STATUS_PREFIX);
			clearStatusPrefix(character.getResources().getMultiReactions(), MAINTAINED_STATUS_PREFIX);
		}
	}

	private void clearStatusPrefix(ArrayList<DataStatus>[][] category, String prefix) {
		if (category == null || prefix == null) return;
		for (ArrayList<DataStatus>[] block : category) {
			clearStatusPrefix(block, prefix);
		}
	}

	private void clearStatusPrefix(ArrayList<DataStatus>[] blocks, String prefix) {
		if (blocks == null || prefix == null) return;
		for (ArrayList<DataStatus> statuses : blocks) {
			if (statuses == null) continue;
			statuses.removeIf(status -> status != null
					&& status.getName() != null
					&& status.getName().startsWith(prefix));
		}
	}

	private boolean applyMaintainedResourceStatus(String uniqueName, String attribute, double severity, String description) {
		if (character == null || character.getResources() == null) return false;
		String resourceAttribute = switch (attribute) {
			case "MAXHP" -> "BASEHP";
			case "HPMULTI" -> "MULTIHP";
			case "MAXAURA" -> "BASEAURA";
			case "AURAMULTI" -> "MULTIAURA";
			case "REACT" -> "BASEREACT";
			case "R1" -> "BASER1";
			case "R2" -> "BASER2";
			case "R3" -> "BASER3";
			default -> null;
		};
		if (resourceAttribute == null) return false;
		DataStatus copy = new DataStatus();
		copy.setName(uniqueName);
		copy.setAttribute(resourceAttribute);
		copy.setDurationType("Maintained");
		copy.setSeverity(severity);
		copy.setAffinity("None");
		copy.setDescription(description);
		character.getResources().addStatus(copy);
		return true;
	}

	private boolean applyMaintainedMultiplierAliases(String uniqueName, String attribute, double severity, String description) {
		if ("DMGMULTI".equals(attribute)) {
			addMaintainedAttributeStatus(uniqueName + " (BDMG)", "MBDMG", severity, description);
			addMaintainedAttributeStatus(uniqueName + " (TDMG)", "MTDMG", severity, description);
			return true;
		}
		if ("HEALMULTI".equals(attribute)) {
			addMaintainedAttributeStatus(uniqueName + " (BHEAL)", "MBHEAL", severity, description);
			addMaintainedAttributeStatus(uniqueName + " (THEAL)", "MTHEAL", severity, description);
			return true;
		}
		return false;
	}

	private void addMaintainedAttributeStatus(String uniqueName, String attribute, double severity, String description) {
		if (character == null || character.getAttributes() == null || attribute == null || attribute.isBlank()) return;
		DataStatus copy = new DataStatus();
		copy.setName(uniqueName);
		copy.setAttribute(attribute);
		copy.setDurationType("Maintained");
		copy.setSeverity(severity);
		copy.setAffinity("None");
		copy.setDescription(description);
		character.getAttributes().addStatus(copy);
	}

	private void refreshMaintainedValuesOnly() {
		ArrayList<MaintainedRow> rows = refreshCachedMaintainedRows();
		int limit = Math.min(rows.size(), mtActLevel.size());
		for (int i = 0; i < limit; i++) {
			MaintainedRow row = rows.get(i);
			mtActLevel.get(i).setValue(row.activeLevel());
			mtCost.get(i).setValue(row.occupiedCost());
			mtAttrKeys.set(i, row.attrKey());
			mtPermRatios.set(i, row.permRatio());
			mtTechRefs.set(i, row.tech());
			refreshMaxFieldHighlight(i);
		}
		syncMainOccupiedAuraFromOcc();
		refreshHPAuraOnly();
	}

	private void refreshAllMaxFieldHighlights() {
		int limit = Math.min(mtMax.size(), mtActLevel.size());
		for (int i = 0; i < limit; i++) {
			refreshMaxFieldHighlight(i);
		}
	}

	private void refreshMaxFieldHighlight(int index) {
		if (index < 0 || index >= mtMax.size() || index >= mtActLevel.size()) return;
		JFormattedTextField maxField = mtMax.get(index);
		JFormattedTextField actField = mtActLevel.get(index);
		int maxValue = safeIntFieldValue(maxField);
		int activeLevel = safeIntFieldValue(actField);
		if (maxValue > 0 && maxValue == activeLevel) {
			styleMaxFieldMatched(maxField);
			styleActLevelFieldMatched(actField);
		} else {
			styleMaxField(maxField);
			styleActLevelField(actField);
		}
		maxField.revalidate();
		maxField.repaint();
		actField.revalidate();
		actField.repaint();
	}

	
}
