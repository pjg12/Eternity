package eternity;

import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PanelCharMaintained extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
	//maintained techniques
	private JLabel maintainedTechsL, mtNameL, mtMaxL, mtActLevelL, mtCostPerALL, mtCostL, mtAffinityL;
	private ArrayList<JTextField> mtName, mtAffinity;
	private ArrayList<JFormattedTextField> mtMax, mtActLevel, mtCostPerAL, mtCost;
	private ArrayList<DataTraining> mtTechRefs;
	private ArrayList<String> mtAttrKeys;
	private ArrayList<Double> mtPermRatios;
	private ArrayList<String> mtRowAffinities;
	private ArrayList<String> mtSectionOrder;
	private ArrayList<JLabel> mtSectionTitles, mtSectionAffinityL, mtSectionNameL, mtSectionMaxL, mtSectionActLevelL, mtSectionCostPerALL, mtSectionCostL;
	private JButton mtUpdateButton, mtMaxButton, mtOffButton;
	private ArrayList<MaintainedRow> cachedMaintainedRows;
	private String cachedStructureSignature;

	private record MaintainedRow(String affinity, String name, int maxRank, int activeLevel, double costPer, double occupiedCost,
			DataTraining tech, String attrKey, double permRatio, String normalizedKey, String resolvedCategory) {}
	
	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharMaintained (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(235, 225, 248));
			
		/**************
		* ***********		Maintained
		*/// ***********			
		maintainedTechsL = buildLabel("Maintained Techniques");
		mtNameL = buildLabel("Name");
		mtMaxL = buildLabel("Max");
		mtActLevelL = buildLabel("AL");
		mtCostPerALL = buildLabel("Cost");
		mtCostL = buildLabel("Occ");
		mtAffinityL = buildLabel("Affinity");

		mtName = new ArrayList<JTextField>();
		mtAffinity = new ArrayList<JTextField>();
		mtMax = new ArrayList<JFormattedTextField>();
		mtActLevel = new ArrayList<JFormattedTextField>();
		mtCostPerAL = new ArrayList<JFormattedTextField>();
		mtCost = new ArrayList<JFormattedTextField>();
		mtTechRefs = new ArrayList<DataTraining>();
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
		ArrayList<MaintainedRow> rows = getMaintainedRows();
		resetSectionMetadata();
		ensureRowCapacity(rows.size());
		hideUnusedRows(rows.size());
		for (int i = 0; i < rows.size(); i++) {
			bindMaintainedRow(i, rows.get(i));
		}
		buildAffinitySections();
		hideUnusedSections(mtSectionOrder.size());
		syncMainOccupiedAuraFromOcc();
		refreshHPAuraOnly();
		resizeSheet();
	}

	@Override
	public void updateCharacter(CharData character) {
		this.character = character;
		refreshBaseState();
		updateMaintained();
	}
	
	
	

	
	
	

	public void mtUpdate () {
		if (character != null && mtActLevel != null && mtTechRefs != null) {
			for (int i = 0; i < mtActLevel.size() && i < mtTechRefs.size(); i++) {
				DataTraining tech = mtTechRefs.get(i);
				if (tech == null) continue;
				try {
					int newAl = ((Number) mtActLevel.get(i).getValue()).intValue();
					tech.setAl(Math.max(0, newAl));
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
			syncMainOccupiedAuraFromOcc();
			refreshAfterMaintainedChange(false);
		}
	    resizeSheet();
	}
	
	public void mtMax () {
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
			syncMainOccupiedAuraFromOcc();
			refreshAfterMaintainedChange(false);
		}
		resizeSheet();
	}
	
	public void mtOff () {
		// Set all active levels to zero and recompute occupied cost display
		for (int i = 0; i < mtActLevel.size(); i++) {
			JFormattedTextField alField = mtActLevel.get(i);
			alField.setValue(0);
			if (mtTechRefs != null && i < mtTechRefs.size() && mtTechRefs.get(i) != null) {
				mtTechRefs.get(i).setAl(0);
			}
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
			syncMainOccupiedAuraFromOcc();
			refreshAfterMaintainedChange(false);
		}
		resizeSheet();
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
	
	private String resolveCategory(CharAttributes attrs, String key) {
		if (attrs == null || key == null) return null;
		String norm = key.toUpperCase();
		String[] cats = { "attribute", "defense", "resist", "combat", "secondary", "damage" };
		for (String c : cats) {
			if (attrs.getBlock(c, norm) != null) return c;
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
		block.addStatus(ds);
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
		block.addMulti(ds);
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
					String rowName = "Boost".equalsIgnoreCase(tech.getName()) ? "Boost (" + attr + ")" : tech.getName() + " (" + attr + ")";
					double costPer = resolveCostPer(perm);
					String normalizedKey = normalizeAttrKey(attr);
					String resolvedCategory = resolveCategory(character != null ? character.getAttributes() : null, normalizedKey);
					rows.add(new MaintainedRow(tech.getAffinity(), rowName, tech.getRank(), activeLevel, costPer, costPer * activeLevel,
							tech, attr == null ? null : attr.toUpperCase(), perm != null ? perm.getRatio() : 0.0, normalizedKey, resolvedCategory));
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
			rows.add(new MaintainedRow(tech.getAffinity(), tech.getName(), tech.getRank(), activeLevel, costPer, costPer * activeLevel,
					tech, mappedAttr == null ? null : mappedAttr.toUpperCase(), mappedRatio, normalizedKey, resolvedCategory));
		}
		return rows;
	}

	private ArrayList<MaintainedRow> refreshCachedMaintainedRows() {
		ArrayList<MaintainedRow> refreshed = new ArrayList<>(cachedMaintainedRows.size());
		for (MaintainedRow row : cachedMaintainedRows) {
			if (row == null) continue;
			DataTraining tech = row.tech();
			int activeLevel = tech == null ? 0 : Math.max(0, tech.getAl());
			double occupiedCost = row.costPer() * activeLevel;
			refreshed.add(new MaintainedRow(row.affinity(), row.name(), row.maxRank(), activeLevel, row.costPer(), occupiedCost,
					row.tech(), row.attrKey(), row.permRatio(), row.normalizedKey(), row.resolvedCategory()));
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
			styleCostField(costPerField);
			styleOccField(costField);

			mtAffinity.add(affField);
			mtName.add(nameField);
			mtMax.add(maxField);
			mtActLevel.add(actField);
			mtCostPerAL.add(costPerField);
			mtCost.add(costField);
			mtTechRefs.add(null);
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
		mtAttrKeys.set(index, row.attrKey());
		mtPermRatios.set(index, row.permRatio());
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
			mtSectionTitles.add(buildLabel(""));
			mtSectionAffinityL.add(buildLabel(""));
			mtSectionNameL.add(buildLabel(""));
			mtSectionMaxL.add(buildLabel(""));
			mtSectionActLevelL.add(buildLabel(""));
			mtSectionCostPerALL.add(buildLabel(""));
			mtSectionCostL.add(buildLabel(""));
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
		refreshHPAuraOnly();
		if (forceRebuild) {
			updateMaintained();
		} else {
			refreshMaintainedValuesOnly();
		}
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
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
		}
		syncMainOccupiedAuraFromOcc();
		refreshHPAuraOnly();
	}

	
}



