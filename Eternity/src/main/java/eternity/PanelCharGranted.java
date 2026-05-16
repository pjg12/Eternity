package eternity;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PanelCharGranted extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
	// granted techniques (maintained-style presentation)
	private JLabel grantedTechsL, mtNameL, mtMaxL, mtActLevelL, mtCostPerALL, mtCostL, mtAffinityL;
	private ArrayList<JTextField> mtName, mtAffinity;
	private ArrayList<JFormattedTextField> mtMax, mtActLevel, mtCostPerAL, mtCost;
	private ArrayList<DataTraining> mtTechRefs;
	private ArrayList<String> mtRowKeys;
	private ArrayList<String> mtRowAffinities;
	private ArrayList<String> mtSectionOrder;
	private ArrayList<JLabel> mtSectionTitles, mtSectionAffinityL, mtSectionNameL, mtSectionMaxL, mtSectionActLevelL, mtSectionCostPerALL, mtSectionCostL;
	private JButton mtUpdateButton, mtMaxButton, mtOffButton;
	private ArrayList<GrantedRow> cachedGrantedRows;
	private String cachedStructureSignature;

	private record GrantedRow(String affinity, String name, int maxRank, int activeLevel, double costPer, double occupiedCost,
			DataTraining tech, String rowKey) {}
	
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
		grantedTechsL.setVisible(false);
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
		refreshHPAuraOnly();

		resizeSheet();
	}

	@Override
	public void updateCharacter(StoreCharData character) {
		this.character = character;
		refreshBaseState();
		updateMaintained();
	}
	
	
	

	



	public void mtUpdate () {
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
	
	public void mtMax () {
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
		if (character == null || character.getResources() == null || mtCost == null) return;
		double totalOcc = 0.0;
		for (JFormattedTextField occField : mtCost) {
			if (occField == null) continue;
			try {
				Number value = (Number) occField.getValue();
				if (value != null) totalOcc += value.doubleValue();
			} catch (Exception ignored) {
				// ignore malformed fields
			}
		}
		character.getResources().setGrantOccupiedAura(Math.max(0.0, totalOcc));
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

	private String buildGrantedAlKey(DataTraining tech, String variant) {
		if (tech == null) return "granted.al.unknown";
		String rawVariant = (variant == null || variant.isBlank()) ? "base" : variant.trim().toLowerCase();
		return "granted.al." + tech.getId() + "." + sanitizeVariantKey(rawVariant);
	}

	private int loadGrantedAl(String rowKey, int fallback) {
		if (character == null || rowKey == null || rowKey.isBlank()) return Math.max(0, fallback);
		String raw = character.getReminderSelection(rowKey);
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
		character.setReminderSelection(rowKey, Integer.toString(Math.max(0, al)));
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
			boolean hasProgress = tech.getRank() > 0 || tech.getExp() > 0 || tech.getAl() > 0;
			if (maxRank <= 0 && !hasProgress) continue;
			int baseAl = Math.max(0, tech.getAl());
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
					rows.add(new GrantedRow(tech.getAffinity(), rowName, tech.getRank(), activeLevel, costPer, costPer * activeLevel, tech, rowKey));
					addedAny = true;
				}
				if (addedAny) continue;
			}

			String rowKey = buildGrantedAlKey(tech, isStandardAffinity ? "None" : null);
			int activeLevel = loadGrantedAl(rowKey, baseAl);
			double costPer = resolveCostPer(tech);
			rows.add(new GrantedRow(tech.getAffinity(), tech.getName(), tech.getRank(), activeLevel, costPer, costPer * activeLevel, tech, rowKey));
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

	private ArrayList<GrantedRow> refreshCachedGrantedRows() {
		ArrayList<GrantedRow> refreshed = new ArrayList<>(cachedGrantedRows.size());
		for (GrantedRow row : cachedGrantedRows) {
			if (row == null) continue;
			DataTraining tech = row.tech();
			int fallbackAl = tech == null ? 0 : Math.max(0, tech.getAl());
			int activeLevel = loadGrantedAl(row.rowKey(), fallbackAl);
			double occupiedCost = row.costPer() * activeLevel;
			refreshed.add(new GrantedRow(row.affinity(), row.name(), row.maxRank(), activeLevel, row.costPer(), occupiedCost, row.tech(), row.rowKey()));
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
		refreshHPAuraOnly();
		if (forceRebuild) {
			updateMaintained();
		} else {
			refreshGrantedValuesOnly();
		}
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
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


