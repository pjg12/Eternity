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
		// clear existing UI components for maintained rows
		for (int i = mtName.size() - 1; i >= 0; i--) {
			remove(mtName.get(i));
			remove(mtAffinity.get(i));
			remove(mtMax.get(i));
			remove(mtActLevel.get(i));
			remove(mtCostPerAL.get(i));
			remove(mtCost.get(i));
		}
		mtName = new ArrayList<>();
		mtAffinity = new ArrayList<>();
		mtMax = new ArrayList<>();
		mtActLevel = new ArrayList<>();
		mtCostPerAL = new ArrayList<>();
		mtCost = new ArrayList<>();
		mtTechRefs = new ArrayList<>();
		mtAttrKeys = new ArrayList<>();
		mtPermRatios = new ArrayList<>();
		mtRowAffinities = new ArrayList<>();
		for (JLabel l : mtSectionTitles) remove(l);
		for (JLabel l : mtSectionAffinityL) remove(l);
		for (JLabel l : mtSectionNameL) remove(l);
		for (JLabel l : mtSectionMaxL) remove(l);
		for (JLabel l : mtSectionActLevelL) remove(l);
		for (JLabel l : mtSectionCostPerALL) remove(l);
		for (JLabel l : mtSectionCostL) remove(l);
		mtSectionOrder = new ArrayList<>();
		mtSectionTitles = new ArrayList<>();
		mtSectionAffinityL = new ArrayList<>();
		mtSectionNameL = new ArrayList<>();
		mtSectionMaxL = new ArrayList<>();
		mtSectionActLevelL = new ArrayList<>();
		mtSectionCostPerALL = new ArrayList<>();
		mtSectionCostL = new ArrayList<>();

		if (character != null && character.getTraining() != null) {
			ArrayList<DataTraining> maintainedTechs = new ArrayList<>();
			for (DataTraining tech : character.getTraining().getAllTraining()) {
				if (tech == null) continue;
				if (!"Maintained".equalsIgnoreCase(tech.getType())) continue;
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
				if (tech == null) continue;
				int maxRank = tech.getMaxRank(character);
				boolean hasProgress = tech.getRank() > 0 || tech.getExp() > 0 || tech.getAl() > 0;
				if (maxRank <= 0 && !hasProgress) continue;
				int activeLevel = Math.max(0, tech.getAl());

				boolean isStandardAffinity = "Standard".equalsIgnoreCase(tech.getAffinity()) &&
						tech.getGrant() != null && !tech.getGrant().isEmpty();

				if (isStandardAffinity && dataQuery != null) {
					boolean addedAny = false;
					// One row per grant, e.g., "Boost (STR)", "Boost (DEX)", ...
					for (Integer gid : tech.getGrant()) {
						if (gid == null) continue;
						DataTechPerm perm = dataQuery.getTechPermById(gid);
						String attr = (perm != null && perm.getAttribute() != null) ? perm.getAttribute() : "None";
						String rowName;
						// Map TechPerm attribute to displayable short form for Boost rows
						if ("Boost".equalsIgnoreCase(tech.getName())) {
							if ("STR".equalsIgnoreCase(attr)) rowName = "Boost (STR)";
							else if ("DEX".equalsIgnoreCase(attr)) rowName = "Boost (DEX)";
							else if ("FOC".equalsIgnoreCase(attr)) rowName = "Boost (FOC)";
							else if ("CTL".equalsIgnoreCase(attr)) rowName = "Boost (CTL)";
							else rowName = "Boost (" + attr + ")";
						} else {
							rowName = tech.getName() + " (" + attr + ")";
						}

						JTextField affField = buildTextField(tech.getAffinity());
						styleAffinityField(affField, tech.getAffinity());
						mtAffinity.add(affField);

						JTextField nameField = buildTextField(rowName);
						mtName.add(nameField);

						JFormattedTextField maxField = buildNumTextField(tech.getRank());
						styleMaxField(maxField);
						mtMax.add(maxField);

					JFormattedTextField actField = buildNumTextField(activeLevel);
					actField.setEditable(true);
					mtActLevel.add(actField);
						mtTechRefs.add(tech);
						mtAttrKeys.add(attr == null ? null : attr.toUpperCase());
						mtPermRatios.add(perm != null ? perm.getRatio() : 0.0);
						mtRowAffinities.add(tech.getAffinity());

						double costPer = 0.0;
						if (perm != null) {
							if (perm.getCost() != 0) costPer = perm.getCost();
							else if (perm.getRatio() != 0) costPer = perm.getRatio();
						}
						JFormattedTextField costPerField = buildNumTextField(costPer);
						styleCostField(costPerField);
						mtCostPerAL.add(costPerField);

						double occ = costPer * activeLevel;
						JFormattedTextField costField = buildNumTextField(occ);
						styleOccField(costField);
						mtCost.add(costField);
						addedAny = true;
					}
					// Fallback: if no perm rows were added, show a single generic entry
					if (!addedAny) {
						JTextField affField = buildTextField(tech.getAffinity());
						styleAffinityField(affField, tech.getAffinity());
						mtAffinity.add(affField);

						JTextField nameField = buildTextField(tech.getName());
						mtName.add(nameField);

						JFormattedTextField maxField = buildNumTextField(tech.getRank());
						styleMaxField(maxField);
						mtMax.add(maxField);

						JFormattedTextField actField = buildNumTextField(activeLevel);
						actField.setEditable(true);
						mtActLevel.add(actField);
						mtTechRefs.add(tech);
						mtAttrKeys.add(null);
						mtPermRatios.add(0.0);
						mtRowAffinities.add(tech.getAffinity());

						JFormattedTextField costPerField = buildNumTextField(0);
						styleCostField(costPerField);
						mtCostPerAL.add(costPerField);
						JFormattedTextField costField = buildNumTextField(0);
						styleOccField(costField);
						mtCost.add(costField);
					}
				} else {
					// Existing single-row behavior for other maintained techniques
					JTextField affField = buildTextField(tech.getAffinity());
					styleAffinityField(affField, tech.getAffinity());
					mtAffinity.add(affField);

					JTextField nameField = buildTextField(tech.getName());
					mtName.add(nameField);

					JFormattedTextField maxField = buildNumTextField(tech.getRank());
					styleMaxField(maxField);
					mtMax.add(maxField);

						JFormattedTextField actField = buildNumTextField(activeLevel);
						actField.setEditable(true);
						mtActLevel.add(actField);
						mtTechRefs.add(tech);
						mtRowAffinities.add(tech.getAffinity());

					String mappedAttr = null;
					double mappedRatio = 0.0;
					if (tech.getGrant() != null && dataQuery != null) {
						for (Integer gid : tech.getGrant()) {
							if (gid == null) continue;
							DataTechPerm perm = dataQuery.getTechPermById(gid);
							if (perm != null) {
								mappedAttr = perm.getAttribute();
								mappedRatio = perm.getRatio();
								break;
							}
						}
					}
					mtAttrKeys.add(mappedAttr == null ? null : mappedAttr.toUpperCase());
					mtPermRatios.add(mappedRatio);

					double costPer = 0.0;
					if (tech.getGrant() != null && dataQuery != null) {
						for (Integer gid : tech.getGrant()) {
							if (gid == null) continue;
							DataTechPerm perm = dataQuery.getTechPermById(gid);
							if (perm != null) {
								if (perm.getCost() != 0) {
									costPer = perm.getCost();
								} else if (perm.getRatio() != 0) {
									costPer = perm.getRatio();
								}
								break;
							}
						}
					}
					JFormattedTextField costPerField = buildNumTextField(costPer);
					styleCostField(costPerField);
					mtCostPerAL.add(costPerField);
					JFormattedTextField costField = buildNumTextField(costPer * activeLevel);
					styleOccField(costField);
					mtCost.add(costField);
				}
			}
		}

		buildAffinitySections();
		syncMainOccupiedAuraFromOcc();
		updateHPAura();
		resizeSheet();
	}

	@Override
	public void updateCharacter(CharData character) {
		super.updateCharacter(character);
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
						if (!applyMaintainedToResource(normalized, sev)) {
							String category = resolveCategory(character.getAttributes(), normalized);
							if (category != null) {
								character.getAttributes().setStatusSeverity(category, normalized, "Maintained", sev);
							}
						}
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
			character.updateAll();
			syncMainOccupiedAuraFromOcc();
			if (sheetFrame != null) {
				sheetFrame.loadCharacter(character);
			} else {
				updateMaintained();
			}
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
			character.updateAll();
			syncMainOccupiedAuraFromOcc();
			if (sheetFrame != null) {
				sheetFrame.loadCharacter(character);
			} else {
				updateMaintained();
			}
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
			character.updateAll();
			syncMainOccupiedAuraFromOcc();
			if (sheetFrame != null) {
				sheetFrame.loadCharacter(character);
			} else {
				updateMaintained();
			}
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
		character.getResources().setOccupiedAura(Math.max(0.0, totalOcc));
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

	private boolean applyMaintainedToResource(String key, double severity) {
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
	}

	private void upsertStatusSeverity(StatBlock[] blocks, String name, String attr, double severity) {
		if (blocks == null || blocks.length == 0 || blocks[0] == null) return;
		StatBlock block = blocks[0];
		for (DataStatus s : block.getStatus()) {
			if (name.equals(s.getName())) {
				s.setSeverity(severity);
				return;
			}
		}
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
		for (DataStatus s : block.getMulti()) {
			if (name.equals(s.getName())) {
				s.setSeverity(severity);
				return;
			}
		}
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
		if (field == null || affinity == null || affinity.isBlank()) return;
		if ("Standard".equalsIgnoreCase(affinity)) return;
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
			JLabel title = buildLabel(safeAffinity + " Techniques");
			mtSectionTitles.add(title);
			mtSectionAffinityL.add(buildLabel("Affinity"));
			mtSectionNameL.add(buildLabel("Name"));
			mtSectionMaxL.add(buildLabel("Max"));
			mtSectionActLevelL.add(buildLabel("AL"));
			mtSectionCostPerALL.add(buildLabel("Cost"));
			mtSectionCostL.add(buildLabel("Occ"));
		}
	}
	

	
}



