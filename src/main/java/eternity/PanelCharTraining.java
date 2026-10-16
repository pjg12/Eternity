package eternity;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

/*
 * 		TRAINING PANEL
 */
public class PanelCharTraining extends PanelCharBase {
	private static final long serialVersionUID = 1L;
    private static final String RACE_TRAINING_NAME = "Race Training";
    private static final String ALTERI_RACIAL_SPECIALTY = "Shapeshifting (Alteri)";
    private static final String ALTERI_SHAPESHIFT_LIST = "Shapeshift";
    private static final String MOLDS_LIST = "Molds";
    private static final String KNOWN_CHANNELS_LIST = "Known Channels";
    private static final String KNOWN_ARRAYS_LIST = "Known Arrays";
    private static final String KNOWN_ATTUNEMENTS_LIST = "Known Attunements";
    private static final String KNOWN_METALS_LIST = "Known Metals";
    private static final String MENTAL_NETWORK_TECHNIQUE = "Mental Network";
	private static final String AURA_RUNOLOGY_TECHNIQUE = "Aura Runology";
    private static final String ETHEREAL_ATTUNEMENT_TECHNIQUE = "Ethereal Attunement";
    private static final String METAL_MANIFESTATION_TECHNIQUE = "Metal Manifestation";

	private record TrainingRow(String affinity, String displayName, int maxRank, int rank, int nextAt, String typeGroup, boolean auraAffinity, boolean listEntry) {}
	private record TrainingRowModel(DataTraining tech, DataTraining template, String affinity, String displayName, String typeGroup, boolean auraAffinity) {}
	private record RemovableTechniqueOption(String label, String category, DataTraining tech, String listName) {}
	
	private JLabel naturalAffinityL;
	private ArrayList<JTextField> natAffinity;
	
	//aura training
	private JButton gainTrainingXpButton, trainNewButton, trainExistingButton;
    private JCheckBox disableCostCheckBox;
    private JButton removeTechniqueButton, maxRanksButton;
	private JLabel auraTrainingL, atTrainingXpL, atMaxTechL, atCurTechL, atRemTechL;
	private JFormattedTextField atTrainingXp, atMaxTech, atCurTech, atRemTech;
	
	private ArrayList<JLabel> atAffinityL, atNameL, atMaxRankL, atCurRankL, atNextAtL;
	private ArrayList<ArrayList<JTextField>> atName, atAffinity;
	private ArrayList<ArrayList<JFormattedTextField>> atMaxRank, atCurRank, atNextAt;
	private ArrayList<ArrayList<String>> atTypeGroup;
	private ArrayList<ArrayList<Boolean>> atAuraAffinityRow;
	private ArrayList<ArrayList<Boolean>> atListEntryRow;
	private ArrayList<JButton> atSectionToggleB;
	private Map<String, Boolean> atSectionCollapsed;
	
	private ArrayList<ArrayList<String>> charLists;
	private ArrayList<String> listTitles;
	private Color defaultToggleBack, defaultToggleFore;
	private ArrayList<ArrayList<TrainingRowModel>> cachedTrainingRows;
	private String cachedTrainingStructureSignature;
    private boolean suppressDisableCostEvents;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharTraining (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(222, 232, 244));
		
		naturalAffinityL = buildLabel("Natural Affinity", null);
		natAffinity = new ArrayList<JTextField>();
		
		atTrainingXpL = buildLabel("Training XP", null);
		atMaxTechL = buildLabel("Max Techs", null);
		atCurTechL = buildLabel("Current Techs", null);
		atRemTechL = buildLabel("Remain Techs", null);
		atTrainingXp = buildNumTextField(0.0);
		atTrainingXp.setEditable(false);
		atMaxTech = buildNumTextField(0); 
		atMaxTech.setEditable(false);
		atCurTech = buildNumTextField(0.0); 
		atRemTech = buildNumTextField(0.0);
		
		gainTrainingXpButton = buildButton("Gain Training Xp");
		gainTrainingXpButton.addActionListener (e -> sheetFrame.gainTrainingXpPressed());
		gainTrainingXpButton.setToolTipText("Open a dialog to add training XP to the character.");

		trainNewButton = buildButton("Train New");
		trainNewButton.addActionListener (e -> sheetFrame.trainNewPressed());
		trainNewButton.setToolTipText("Open a dialog to select a new technique to train."); 
		
		trainExistingButton = buildButton("Train Existing");
		trainExistingButton.addActionListener (e -> sheetFrame.trainExistingPressed());
		trainExistingButton.setToolTipText("Open a dialog to select a technique to improve."); 

        disableCostCheckBox = new JCheckBox("Disable Cost");
        disableCostCheckBox.setOpaque(false);
        disableCostCheckBox.setVisible(false);
        disableCostCheckBox.addActionListener(e -> {
            if (suppressDisableCostEvents || sheetFrame == null) return;
            sheetFrame.setTrainingCostDisabled(disableCostCheckBox.isSelected());
        });
        add(disableCostCheckBox);

        removeTechniqueButton = buildButton("Remove Technique");
        removeTechniqueButton.setVisible(false);
        removeTechniqueButton.addActionListener(e -> promptRemoveTechnique());
        removeTechniqueButton.setToolTipText("Remove a learned technique manually while GM mode is active.");

        maxRanksButton = buildButton("Max");
        maxRanksButton.setVisible(false);
        maxRanksButton.addActionListener(e -> maxExistingTechniqueRanks());
        maxRanksButton.setToolTipText("Raise all known training techniques to their current maximum rank.");
			
		atAffinityL = new ArrayList<JLabel>();
		atNameL = new ArrayList<JLabel>();
		atMaxRankL = new ArrayList<JLabel>();
		atCurRankL = new ArrayList<JLabel>();
		atNextAtL = new ArrayList<JLabel>();
		
		atAffinity = new ArrayList<ArrayList<JTextField>>();
		atName = new ArrayList<ArrayList<JTextField>>();
		atMaxRank = new ArrayList<ArrayList<JFormattedTextField>>();
		atCurRank = new ArrayList<ArrayList<JFormattedTextField>>();
		atNextAt = new ArrayList<ArrayList<JFormattedTextField>>();
		atTypeGroup = new ArrayList<ArrayList<String>>();
		atAuraAffinityRow = new ArrayList<ArrayList<Boolean>>();
		atListEntryRow = new ArrayList<ArrayList<Boolean>>();
		atSectionToggleB = new ArrayList<JButton>();
		atSectionCollapsed = new HashMap<String, Boolean>();
		defaultToggleBack = UIManager.getColor("Button.background");
		defaultToggleFore = UIManager.getColor("Button.foreground");
		if (defaultToggleBack == null) defaultToggleBack = new Color(240, 240, 240);
		if (defaultToggleFore == null) defaultToggleFore = Color.BLACK;
		cachedTrainingRows = new ArrayList<ArrayList<TrainingRowModel>>();
		cachedTrainingStructureSignature = "";

		for (int i = 0; i < TRAINING.length; i++) {
			final int sectionIndex = i;
			atAffinityL.add(buildLabel("Affinity:", null));
			atNameL.add(buildLabel("Name:", null));
			atMaxRankL.add(buildLabel("Max:", null));
			atCurRankL.add(buildLabel("Cur:", null));
			atNextAtL.add(buildLabel("Next:", null));
			JButton toggleButton = buildButton("[-]");
			toggleButton.addActionListener(e -> toggleSection(sectionIndex));
			atSectionToggleB.add(toggleButton);

			atName.add(new ArrayList<JTextField>());
			atAffinity.add(new ArrayList<JTextField>());
			atMaxRank.add(new ArrayList<JFormattedTextField>());
			atCurRank.add(new ArrayList<JFormattedTextField>());
			atNextAt.add(new ArrayList<JFormattedTextField>());
			atTypeGroup.add(new ArrayList<String>());
			atAuraAffinityRow.add(new ArrayList<Boolean>());
			atListEntryRow.add(new ArrayList<Boolean>());
		}
	}  /*--------------
		END DEFAULTCONSTRUCTOR
		--------------*/
	
	/*
	 * 		UPDATE ALL
	 */
	public void updateAll() {
		updateTraining();
		resizeSheet();
		revalidate();
		repaint();
	}  /*--------------
		END UPDATEALL
		--------------*/
	
	/*
	 * 		UPDATE TRAINING
	 */
	public boolean updateTraining() {
		CharTraining training = character.getTraining();
		if (training == null) return false;

        suppressDisableCostEvents = true;
        boolean gmMode = sheetFrame != null && sheetFrame.isGmModeActive();
        disableCostCheckBox.setVisible(gmMode);
        disableCostCheckBox.setSelected(gmMode && sheetFrame.isTrainingCostDisabled());
        suppressDisableCostEvents = false;
        removeTechniqueButton.setVisible(gmMode);
        removeTechniqueButton.setEnabled(gmMode && hasRemovableTechniques(training));
        maxRanksButton.setVisible(gmMode);
        maxRanksButton.setEnabled(gmMode && hasRaisableTechniques(training));

		// compute max techs from the character's current level
		int maxTechs = 0;
		if (character.getIdentity() != null) {
			DataLevel levelData = dataQuery.getLevel(character.getIdentity().getLevel());
			if (levelData != null) maxTechs = Math.max(0, levelData.getBaseTechs());
		}
		if (character.hasAuraProficiencySpecialty()) {
			maxTechs = (int) (maxTechs * character.getAuraProficiencyBonusMultiplier());
		}

		// update straight values
		atTrainingXp.setValue(round2(training.getTrainingXp()));
		atMaxTech.setValue(maxTechs);
		// Exclude specific generic categories from "current techs" and apply natural-affinity weighting.
		double current = calculateWeightedCurrentTechs(training);
		atCurTech.setValue(round2(current));
		atRemTech.setValue(round2(Math.max(0.0, maxTechs - current)));

		String structureSignature = buildTrainingStructureSignature(training);
		boolean structureChanged = !structureSignature.equals(cachedTrainingStructureSignature);
		if (structureChanged) {
			cachedTrainingRows = collectTrainingRows(training);
			cachedTrainingStructureSignature = structureSignature;
		}
		
		// add all
		ArrayList<String> tempList = new ArrayList<>(training.getNaturalAffinities());
		ensureNaturalAffinityCapacity(tempList.size());
		for (int i = 0; i < tempList.size(); i++) {
			JTextField tempField = natAffinity.get(i);
			tempField.setText(tempList.get(i));
			tempField.setVisible(true);
			tempField.setBackground(Color.WHITE);
			tempField.setForeground(Color.BLACK);
			DataColor color = dataQuery.getColorByTitle(tempList.get(i));
			if (color != null) {
				tempField.setBackground(color.getBackColor());
				tempField.setForeground(color.getForeColor());
			}
		}
		hideUnusedNaturalAffinityRows(tempList.size());
		
		// Build per-category display rows
		for (int row = 0; row < TRAINING.length; row++) {
			ArrayList<TrainingRow> rows = materializeTrainingRows(row);
			ensureTrainingRowCapacity(row, rows.size());
			for (int i = 0; i < rows.size(); i++) {
				bindTrainingRow(row, i, rows.get(i));
			}
			hideUnusedTrainingRows(row, rows.size());
		}
		return structureChanged;
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

        if (maxRanksButton.isVisible()) {
            maxRanksButton.setBounds(480, pageHeight - 25, 80, 20);
        } else {
            maxRanksButton.setVisible(false);
        }

        if (disableCostCheckBox.isVisible()) {
            disableCostCheckBox.setBounds(5, pageHeight, 140, 20);
            removeTechniqueButton.setBounds(150, pageHeight, 150, 20);
            pageHeight += 25;
        } else {
            removeTechniqueButton.setVisible(false);
        }

		for (int i = 0; i < TRAINING.length; i++) {
			atAffinityL.get(i).setVisible(false);
			atNameL.get(i).setVisible(false);
			atMaxRankL.get(i).setVisible(false);
			atCurRankL.get(i).setVisible(false);
			atNextAtL.get(i).setVisible(false);
			atSectionToggleB.get(i).setVisible(false);
			for (int j = 0; j < atName.get(i).size(); j++) {
				if (!atAffinity.get(i).isEmpty()) atAffinity.get(i).get(j).setVisible(false);
				atName.get(i).get(j).setVisible(false);
				atMaxRank.get(i).get(j).setVisible(false);
				atCurRank.get(i).get(j).setVisible(false);
				atNextAt.get(i).get(j).setVisible(false);
			}
		}
		
		naturalAffinityL.setBounds(5,pageHeight,570,20);
		pageHeight += 25;

		int tempInt = natAffinity.size();
		int tempInt2 = 200;
		int tempInt3 = 0;
		if (tempInt >= 3) {
			tempInt2 = 570 - 5*(tempInt+1);
			tempInt2 /= tempInt;
		}
		else {
			tempInt3 = (570 - tempInt*200) / 2;
		}

		for (int i = 0; i < tempInt; i++) {
			natAffinity.get(i).setBounds(5 + (5+tempInt2)*i + tempInt3,pageHeight,tempInt2,20);
		}
		pageHeight += 25;

		atTrainingXpL.setBounds(5,pageHeight,130,19);
		atMaxTechL.setBounds(145,pageHeight,130,19);
		atCurTechL.setBounds(285,pageHeight,130,19);
		atRemTechL.setBounds(425,pageHeight,130,19);
		pageHeight += 20;
		
		atTrainingXp.setBounds(5,pageHeight,130,19);
		atMaxTech.setBounds(145,pageHeight,130,19);
		atCurTech.setBounds(285,pageHeight,130,19);
		atRemTech.setBounds(425,pageHeight,130,19);
		pageHeight += 30;
		
		gainTrainingXpButton.setBounds(5,pageHeight,175,19);
		trainNewButton.setBounds(195,pageHeight,175,19);
		trainExistingButton.setBounds(385,pageHeight,175,19);
		pageHeight += 30;
		
		for (int i = 0; i < TRAINING.length; i++) {
			ArrayList<TrainingRow> rows = materializeTrainingRows(i);
			if (!rows.isEmpty()) {
				boolean collapsed = isSectionCollapsed(TRAINING[i]);
				String title = (i < TRAININGTITLE.length ? TRAININGTITLE[i] : TRAINING[i]) + " (" + rows.size() + ")";
				atSectionToggleB.get(i).setText((collapsed ? "[+] " : "[-] ") + title);
				styleSectionToggleButton(atSectionToggleB.get(i), TRAINING[i]);
				atSectionToggleB.get(i).setVisible(true);
				atSectionToggleB.get(i).setBounds(5, pageHeight, 555, 20);
				pageHeight += 25;

				if (collapsed) {
					pageHeight += 5;
					continue;
				}

				if (!atAffinity.get(i).isEmpty()) {
					atAffinityL.get(i).setVisible(true);
					atAffinityL.get(i).setBounds(5, pageHeight, 120, 20);
				}

				atNameL.get(i).setVisible(true);
				atNameL.get(i).setBounds(130, pageHeight, 210, 20);
						
				atMaxRankL.get(i).setVisible(true);
				atMaxRankL.get(i).setBounds(345, pageHeight, 50, 20);

				atCurRankL.get(i).setVisible(true);
				atCurRankL.get(i).setBounds(400, pageHeight, 50, 20);

				atNextAtL.get(i).setVisible(true);
				atNextAtL.get(i).setBounds(455, pageHeight, 100, 20);
				pageHeight += 20;

				for (int j = 0; j < rows.size(); j++) {
					String currGroup = (i < atTypeGroup.size() && j < atTypeGroup.get(i).size())
							? atTypeGroup.get(i).get(j)
							: "Active";
					boolean currAuraAffinity = (i < atAuraAffinityRow.size() && j < atAuraAffinityRow.get(i).size())
							? Boolean.TRUE.equals(atAuraAffinityRow.get(i).get(j))
							: false;
					boolean currListEntry = (i < atListEntryRow.size() && j < atListEntryRow.get(i).size())
							? Boolean.TRUE.equals(atListEntryRow.get(i).get(j))
							: false;
					if (j > 0) {
						String prevGroup = (i < atTypeGroup.size() && (j - 1) < atTypeGroup.get(i).size())
								? atTypeGroup.get(i).get(j - 1)
								: currGroup;
						boolean prevAuraAffinity = (i < atAuraAffinityRow.size() && (j - 1) < atAuraAffinityRow.get(i).size())
								? Boolean.TRUE.equals(atAuraAffinityRow.get(i).get(j - 1))
								: false;
						// Dedicated spacer after Aura Affinity rows before non-Aura rows.
						if (prevAuraAffinity && !currAuraAffinity) pageHeight += 5;
						else if (!prevAuraAffinity && !currAuraAffinity && !prevGroup.equals(currGroup)) pageHeight += 5;
					}

					if (!atAffinity.get(i).isEmpty()) {
						atAffinity.get(i).get(j).setVisible(!currListEntry);
						atAffinity.get(i).get(j).setBounds(5, pageHeight, 120, 20);
					}
					
					atName.get(i).get(j).setVisible(true);
					atName.get(i).get(j).setBounds(130, pageHeight, 210, 20);
				
					atMaxRank.get(i).get(j).setVisible(true);
					atMaxRank.get(i).get(j).setBounds(345, pageHeight, 50, 20);
				
					atCurRank.get(i).get(j).setVisible(true);
					atCurRank.get(i).get(j).setBounds(400, pageHeight, 50, 20);

					atNextAt.get(i).get(j).setVisible(true);
					atNextAt.get(i).get(j).setBounds(455, pageHeight, 100, 20);
					pageHeight += 20;
					if (i == 0 && j == 5) pageHeight += 8;
				}
				pageHeight += 5;
		
			}
		}
		
		
		
		/*
		 * Set Window Size
		 */	
		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}

	private void styleSectionToggleButton(JButton button, String sectionKey) {
		if (button == null) return;
		DataColor color = (dataQuery != null && sectionKey != null) ? dataQuery.getColorByTitle(sectionKey) : null;
		if (color != null) {
			button.setBackground(color.getBackColor());
			button.setForeground(color.getForeColor());
		} else {
			button.setBackground(defaultToggleBack);
			button.setForeground(defaultToggleFore);
		}
		button.setOpaque(true);
		button.setContentAreaFilled(true);
		button.setBorderPainted(true);
	}

	private void toggleSection(int sectionIndex) {
		if (sectionIndex < 0 || sectionIndex >= TRAINING.length) return;
		String key = TRAINING[sectionIndex];
		boolean collapsed = isSectionCollapsed(key);
		setSectionCollapsed(key, !collapsed);
		resizeSheet();
		revalidate();
		repaint();
	}

	private boolean isSectionCollapsed(String sectionKey) {
		if (sectionKey == null || sectionKey.isBlank()) return false;
		if (atSectionCollapsed.containsKey(sectionKey)) {
			return Boolean.TRUE.equals(atSectionCollapsed.get(sectionKey));
		}
		boolean collapsed = false;
		if (character != null) {
			String stored = character.getReminderSelection("training.collapse." + sectionKey);
			if (stored != null && !stored.isBlank()) {
				collapsed = Boolean.parseBoolean(stored.trim());
			}
		}
		atSectionCollapsed.put(sectionKey, collapsed);
		return collapsed;
	}

	private void setSectionCollapsed(String sectionKey, boolean collapsed) {
		if (sectionKey == null || sectionKey.isBlank()) return;
		atSectionCollapsed.put(sectionKey, collapsed);
		if (character != null) {
			character.setReminderSelection("training.collapse." + sectionKey, Boolean.toString(collapsed));
		}
	}

	private String getTypeGroup(String rawType) {
		if (rawType != null) {
			if ("Maintained".equalsIgnoreCase(rawType)) return "Maintained";
			if ("Passive".equalsIgnoreCase(rawType)) return "Passive";
		}
		return "Active";
	}

	private int getTypeOrder(String typeGroup) {
		if ("Maintained".equalsIgnoreCase(typeGroup)) return 1;
		if ("Passive".equalsIgnoreCase(typeGroup)) return 2;
		return 0; // Active first
	}

	private double calculateWeightedCurrentTechs(CharTraining training) {
		if (training == null) return 0.0;
		Set<String> excluded = Set.of("attribute", "misc", "affinity", "fundamental", "standard", "crafting");
		Set<String> natural = normalizeAffinitySet(training.getNaturalAffinities());
		Set<String> domain = normalizeAffinitySet(training.getDomainAffinities());
		double total = 0.0;

		for (String category : training.getTrainingCategories()) {
			if (category == null) continue;
			if (excluded.contains(category.toLowerCase())) continue;

			for (DataTraining tech : training.getTrainingList(category)) {
				if (tech == null) continue;
				String normalizedCategory = category.toLowerCase();
				String normalizedAffinity = tech.getAffinity() == null ? "" : tech.getAffinity().toLowerCase();
				boolean naturalMatch = natural.contains(normalizedCategory) || natural.contains(normalizedAffinity);
				boolean domainMatch = domain.contains(normalizedCategory) || domain.contains(normalizedAffinity);
				boolean spiritOrTime = ("Spirit".equalsIgnoreCase(category) || "Time".equalsIgnoreCase(category)) ||
						("Spirit".equalsIgnoreCase(tech.getAffinity()) || "Time".equalsIgnoreCase(tech.getAffinity()));
				double multiplier = 1.0;
				if (naturalMatch) multiplier *= 0.5;
				else if (domainMatch) multiplier *= 0.75;
				if (spiritOrTime) multiplier *= 1.5;
				total += tech.getRank() * multiplier;
			}
		}
		return total;
	}

	private Set<String> normalizeAffinitySet(Iterable<String> affinities) {
		Set<String> normalized = new HashSet<>();
		if (affinities == null) return normalized;
		for (String affinity : affinities) {
			if (affinity != null) {
				normalized.add(affinity.toLowerCase());
			}
		}
		return normalized;
	}

	private boolean isAuraAffinityTech(DataTraining tech) {
		if (tech == null || tech.getName() == null) return false;
		return tech.getName().trim().toLowerCase().startsWith("aura affinity");
	}

	private String buildTrainingStructureSignature(CharTraining training) {
		if (training == null) return "";
		StringBuilder signature = new StringBuilder();
		if (character != null && character.getIdentity() != null) {
			signature.append("lvl=").append(character.getIdentity().getLevel()).append(';');
		}
		for (String affinity : training.getNaturalAffinities()) {
			signature.append("nat=").append(affinity).append(';');
		}
		for (String category : TRAINING) {
			signature.append('[').append(category).append(']');
			ArrayList<DataTraining> list = new ArrayList<>(training.getTrainingList(category));
			list.sort(Comparator
					.comparingInt((DataTraining t) -> isAuraAffinityTech(t) ? 0 : 1)
					.thenComparingInt((DataTraining t) -> getTypeOrder(getTypeGroup(t == null ? null : t.getType())))
					.thenComparingInt(t -> t == null ? Integer.MAX_VALUE : t.getId())
					.thenComparing(t -> t == null || t.getName() == null ? "" : t.getName(), String.CASE_INSENSITIVE_ORDER));
			for (DataTraining tech : list) {
				if (tech == null) continue;
				DataTraining template = resolveTrainingTemplate(tech);
				signature.append(tech.getId()).append('|')
						.append(tech.getName()).append('|')
						.append(tech.getType()).append('|')
						.append(tech.getAffinity()).append('|')
						.append(resolveAssociatedListName(template, tech)).append('|')
						.append(resolveListMaxPerRank(template, tech)).append('|')
						.append(resolveListMaxBase(template, tech)).append(';');
			}
		}
		return signature.toString();
	}

	private ArrayList<ArrayList<TrainingRowModel>> collectTrainingRows(CharTraining training) {
		ArrayList<ArrayList<TrainingRowModel>> rowsByCategory = new ArrayList<>();
		for (String category : TRAINING) {
			ArrayList<DataTraining> list = new ArrayList<>(training.getTrainingList(category));
			list.sort(Comparator
					.comparingInt((DataTraining t) -> isAuraAffinityTech(t) ? 0 : 1)
					.thenComparingInt((DataTraining t) -> getTypeOrder(getTypeGroup(t == null ? null : t.getType())))
					.thenComparingInt(t -> t == null ? Integer.MAX_VALUE : t.getId())
					.thenComparing(t -> t == null || t.getName() == null ? "" : t.getName(), String.CASE_INSENSITIVE_ORDER));
			ArrayList<TrainingRowModel> categoryRows = new ArrayList<>();
			for (DataTraining tech : list) {
				if (tech == null) continue;
				DataTraining template = resolveTrainingTemplate(tech);
				categoryRows.add(new TrainingRowModel(
						tech,
						template,
						tech.getAffinity(),
						resolveTrainingDisplayName(tech),
						getTypeGroup(tech.getType()),
						isAuraAffinityTech(tech)));
			}
			rowsByCategory.add(categoryRows);
		}
		return rowsByCategory;
	}

	private ArrayList<TrainingRow> materializeTrainingRows(int categoryIndex) {
		ArrayList<TrainingRow> rows = new ArrayList<>();
		if (categoryIndex < 0 || categoryIndex >= cachedTrainingRows.size()) return rows;
		for (TrainingRowModel model : cachedTrainingRows.get(categoryIndex)) {
			DataTraining tech = model.tech();
			if (tech == null) continue;
			rows.add(new TrainingRow(
					model.affinity(),
					model.displayName(),
					tech.getMaxRank(character),
					tech.getRank(),
					tech.getNextAt(character),
					model.typeGroup(),
					model.auraAffinity(),
					false));
			String listName = resolveAssociatedListName(model.template(), tech);
			if (!listName.isBlank()) {
				rows.add(new TrainingRow(
						"",
						resolveListTrainingEntryDisplayName(listName),
						resolveListMaxMembers(model.template(), tech),
						countListMembers(listName),
						10,
						model.typeGroup(),
						false,
						true));
			}
		}
		return rows;
	}

	private String resolveTrainingDisplayName(DataTraining tech) {
		if (tech == null) return "";
		String displayName = tech.getName();
		if (displayName != null && displayName.equalsIgnoreCase("Race Training")) {
			DataSpecialty racial = character != null && character.getSpecials() != null ? character.getSpecials().getRacialSpecialty() : null;
			if (racial != null && racial.getName() != null && !racial.getName().isBlank()) {
				displayName = racial.getName();
			}
		}
		return displayName;
	}

	private DataTraining resolveTrainingTemplate(DataTraining tech) {
		if (tech == null || dataQuery == null || tech.getId() <= 0) return tech;
		DataTraining template = dataQuery.getTrainingById(tech.getId());
		return template != null ? template : tech;
	}

	private String resolveAssociatedListName(DataTraining template, DataTraining tech) {
		if (isAlteriRaceTraining(tech)) return ALTERI_SHAPESHIFT_LIST;
		if (isMoldingTechnique(template) || isMoldingTechnique(tech)) return MOLDS_LIST;
		if (isMentalNetworkTechnique(template) || isMentalNetworkTechnique(tech)) return KNOWN_CHANNELS_LIST;
		if (isAuraRunologyTechnique(template) || isAuraRunologyTechnique(tech)) return KNOWN_ARRAYS_LIST;
		if (isEtherealAttunementTechnique(template) || isEtherealAttunementTechnique(tech)) return KNOWN_ATTUNEMENTS_LIST;
		if (isMetalManifestationTechnique(template) || isMetalManifestationTechnique(tech)) return KNOWN_METALS_LIST;
		String fromTemplate = template != null ? template.getListName() : "";
		if (fromTemplate != null && !fromTemplate.isBlank()) return fromTemplate.trim();
		String fromTech = tech != null ? tech.getListName() : "";
		return fromTech == null ? "" : fromTech.trim();
	}

	private String resolveListTrainingEntryDisplayName(String listName) {
		if (KNOWN_ATTUNEMENTS_LIST.equalsIgnoreCase(listName == null ? "" : listName.trim())) {
			return "New Attunement";
		}
		if (KNOWN_METALS_LIST.equalsIgnoreCase(listName == null ? "" : listName.trim())) {
			return "New Metal";
		}
		return "New " + listName;
	}

	private int resolveListMaxPerRank(DataTraining template, DataTraining tech) {
		if (isMentalNetworkTechnique(template) || isMentalNetworkTechnique(tech)) return 1;
		if (isAuraRunologyTechnique(template) || isAuraRunologyTechnique(tech)) return 4;
		if (isEtherealAttunementTechnique(template) || isEtherealAttunementTechnique(tech)) return 1;
		if (isMetalManifestationTechnique(template) || isMetalManifestationTechnique(tech)) return 1;
		if (template != null && template.hasAssociatedList()) return template.getEffectiveListMaxPerRank();
		if (tech != null && tech.hasAssociatedList()) return tech.getEffectiveListMaxPerRank();
		return 2;
	}

	private int resolveListMaxBase(DataTraining template, DataTraining tech) {
		if (isMentalNetworkTechnique(template) || isMentalNetworkTechnique(tech)) return 0;
		if (isAuraRunologyTechnique(template) || isAuraRunologyTechnique(tech)) return 0;
		if (isEtherealAttunementTechnique(template) || isEtherealAttunementTechnique(tech)) return 0;
		if (isMetalManifestationTechnique(template) || isMetalManifestationTechnique(tech)) return 0;
		if (template != null && template.hasAssociatedList()) return template.getEffectiveListMaxBase();
		if (tech != null && tech.hasAssociatedList()) return tech.getEffectiveListMaxBase();
		return 1;
	}

	private int resolveListMaxMembers(DataTraining template, DataTraining tech) {
		int coreSkillRank = tech == null ? 0 : Math.max(0, tech.getRank());
		return Math.max(0, resolveListMaxPerRank(template, tech) * coreSkillRank + resolveListMaxBase(template, tech));
	}

	private int countListMembers(String listName) {
		if (listName == null || listName.isBlank() || character == null || character.getLists() == null) return 0;
		int count = 0;
		for (List<DataList> group : character.getLists()) {
			if (group == null) continue;
			for (DataList entry : group) {
				if (entry == null || entry.getList() == null || entry.getName() == null) continue;
				if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
				if (entry.getName().trim().isBlank()) continue;
				count++;
			}
		}
		return count;
	}

	private boolean isAlteriRaceTraining(DataTraining tech) {
		if (tech == null || tech.getName() == null || !RACE_TRAINING_NAME.equalsIgnoreCase(tech.getName().trim())) return false;
		if (character == null || character.getSpecials() == null) return false;
		DataSpecialty racial = character.getSpecials().getRacialSpecialty();
		if (racial == null || racial.getName() == null) return false;
		return ALTERI_RACIAL_SPECIALTY.equalsIgnoreCase(racial.getName().trim());
	}

	private boolean isMoldingTechnique(DataTraining tech) {
		return tech != null && tech.isMoldingTechnique();
	}

	private boolean isMentalNetworkTechnique(DataTraining tech) {
		return tech != null
				&& tech.getName() != null
				&& MENTAL_NETWORK_TECHNIQUE.equalsIgnoreCase(tech.getName().trim());
	}

	private boolean isAuraRunologyTechnique(DataTraining tech) {
		return tech != null
				&& tech.getName() != null
				&& AURA_RUNOLOGY_TECHNIQUE.equalsIgnoreCase(tech.getName().trim());
	}

	private boolean isEtherealAttunementTechnique(DataTraining tech) {
		return tech != null
				&& tech.getName() != null
				&& ETHEREAL_ATTUNEMENT_TECHNIQUE.equalsIgnoreCase(tech.getName().trim());
	}

	private boolean isMetalManifestationTechnique(DataTraining tech) {
		return tech != null
				&& tech.getName() != null
				&& METAL_MANIFESTATION_TECHNIQUE.equalsIgnoreCase(tech.getName().trim());
	}

	private void ensureNaturalAffinityCapacity(int size) {
		while (natAffinity.size() < size) {
			natAffinity.add(buildTextField(""));
		}
	}

	private void hideUnusedNaturalAffinityRows(int usedCount) {
		for (int i = usedCount; i < natAffinity.size(); i++) {
			natAffinity.get(i).setVisible(false);
		}
	}

	private void ensureTrainingRowCapacity(int categoryIndex, int size) {
		while (atName.get(categoryIndex).size() < size) {
			atAffinity.get(categoryIndex).add(buildTextField(""));
			atName.get(categoryIndex).add(buildTextField(""));
			atMaxRank.get(categoryIndex).add(buildNumTextField(0));
			atCurRank.get(categoryIndex).add(buildNumTextField(0));
			atNextAt.get(categoryIndex).add(buildNumTextField(0));
			atTypeGroup.get(categoryIndex).add("Active");
			atAuraAffinityRow.get(categoryIndex).add(Boolean.FALSE);
			atListEntryRow.get(categoryIndex).add(Boolean.FALSE);
		}
	}

	private void bindTrainingRow(int categoryIndex, int rowIndex, TrainingRow row) {
		JTextField affinityField = atAffinity.get(categoryIndex).get(rowIndex);
		String affinity = row.affinity();
		affinityField.setText(affinity == null ? "" : affinity);
		affinityField.setVisible(!row.listEntry() && affinity != null && !affinity.isBlank());
		affinityField.setBackground(Color.WHITE);
		affinityField.setForeground(Color.BLACK);
		if (affinity != null && !affinity.isBlank()) {
			DataColor color = dataQuery.getColorByTitle(affinity);
			if (color != null) {
				affinityField.setBackground(color.getBackColor());
				affinityField.setForeground(color.getForeColor());
			}
		}

		atName.get(categoryIndex).get(rowIndex).setText(row.displayName());
		atName.get(categoryIndex).get(rowIndex).setVisible(true);

		JFormattedTextField maxField = atMaxRank.get(categoryIndex).get(rowIndex);
		JFormattedTextField curField = atCurRank.get(categoryIndex).get(rowIndex);
		maxField.setValue(row.maxRank());
		curField.setValue(row.rank());
		maxField.setForeground(row.maxRank() == row.rank() ? Color.RED : Color.BLACK);
		curField.setForeground(row.maxRank() == row.rank() ? Color.RED : Color.BLACK);
		maxField.setVisible(true);
		curField.setVisible(true);

		atNextAt.get(categoryIndex).get(rowIndex).setValue(row.nextAt());
		atNextAt.get(categoryIndex).get(rowIndex).setVisible(true);

		atTypeGroup.get(categoryIndex).set(rowIndex, row.typeGroup());
		atAuraAffinityRow.get(categoryIndex).set(rowIndex, row.auraAffinity());
		atListEntryRow.get(categoryIndex).set(rowIndex, row.listEntry());
	}

	private void hideUnusedTrainingRows(int categoryIndex, int usedCount) {
		for (int i = usedCount; i < atName.get(categoryIndex).size(); i++) {
			atAffinity.get(categoryIndex).get(i).setVisible(false);
			atName.get(categoryIndex).get(i).setVisible(false);
			atMaxRank.get(categoryIndex).get(i).setVisible(false);
			atCurRank.get(categoryIndex).get(i).setVisible(false);
			atNextAt.get(categoryIndex).get(i).setVisible(false);
			atTypeGroup.get(categoryIndex).set(i, "Active");
			atAuraAffinityRow.get(categoryIndex).set(i, Boolean.FALSE);
			atListEntryRow.get(categoryIndex).set(i, Boolean.FALSE);
		}
	}

    private boolean hasRemovableTechniques(CharTraining training) {
        if (training == null) return false;
        for (String category : TRAINING) {
            if (!training.getTrainingList(category).isEmpty()) return true;
        }
        return false;
    }

    private boolean hasRaisableTechniques(CharTraining training) {
        if (training == null) return false;
        for (DataTraining tech : training.getAllTraining()) {
            if (canRaiseTechniqueRank(tech)) return true;
        }
        return false;
    }

    private boolean canRaiseTechniqueRank(DataTraining tech) {
        if (tech == null || tech.isListEntry()) return false;
        return tech.getRank() < Math.max(0, tech.getMaxRank(character));
    }

    private void maxExistingTechniqueRanks() {
        if (sheetFrame == null || !sheetFrame.isGmModeActive() || character == null) return;
        CharTraining training = character.getTraining();
        if (training == null) return;

        boolean raisedAny = false;
        boolean raisedThisPass;
        int safety = 0;
        do {
            raisedThisPass = false;
            for (DataTraining tech : training.getAllTraining()) {
                if (!canRaiseTechniqueRank(tech)) continue;
                int oldRank = tech.getRank();
                int newRank = Math.max(0, tech.getMaxRank(character));
                if (!FrameTraining.promptForGaianVersatilityRankGains(this, dataQuery, character, tech, oldRank, newRank)) {
                    continue;
                }
                tech.setRank(newRank);
                raisedThisPass = true;
                raisedAny = true;
            }
            safety++;
        } while (raisedThisPass && safety < 64);

        if (!raisedAny) return;

        sheetFrame.refreshTrainingPanel();
        sheetFrame.refreshMainPanel();
    }

    private void promptRemoveTechnique() {
        if (sheetFrame == null || !sheetFrame.isGmModeActive() || character == null) return;
        CharTraining training = character.getTraining();
        if (training == null) return;

        ArrayList<RemovableTechniqueOption> options = collectRemovableTechniqueOptions(training);
        if (options.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This character has no techniques to remove.", "No Techniques", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LinkedHashMap<String, RemovableTechniqueOption> optionsByLabel = new LinkedHashMap<>();
        for (RemovableTechniqueOption option : options) {
            optionsByLabel.put(option.label(), option);
        }

        JComboBox<String> techniqueBox = new JComboBox<>(optionsByLabel.keySet().toArray(new String[0]));
        int choice = JOptionPane.showConfirmDialog(
                this,
                techniqueBox,
                "Remove Technique",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String selectedLabel = (String) techniqueBox.getSelectedItem();
        RemovableTechniqueOption selected = optionsByLabel.get(selectedLabel);
        if (selected == null || selected.tech() == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove " + selected.tech().getName() + " and all " + Math.max(0, selected.tech().getRank()) + " current ranks from the character?",
                "Confirm Remove Technique",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        training.removeTraining(selected.category(), selected.tech());
        removeAssociatedListGroup(selected.listName());
        character.updateAll();
        sheetFrame.loadCharacter(character, sheetFrame.isGmModeActive());
    }

    private ArrayList<RemovableTechniqueOption> collectRemovableTechniqueOptions(CharTraining training) {
        ArrayList<RemovableTechniqueOption> options = new ArrayList<>();
        for (String category : TRAINING) {
            for (DataTraining tech : training.getTrainingList(category)) {
                if (tech == null || tech.getName() == null || tech.getName().isBlank()) continue;
                String label = buildTechniqueRemovalLabel(tech, category, options);
                String listName = resolveAssociatedListName(resolveTrainingTemplate(tech), tech);
                options.add(new RemovableTechniqueOption(label, category, tech, listName));
            }
        }
        return options;
    }

    private String buildTechniqueRemovalLabel(DataTraining tech, String category, List<RemovableTechniqueOption> existing) {
        String baseLabel = tech.getName() + " [" + category + "] Rank " + Math.max(0, tech.getRank());
        String label = baseLabel;
        int suffix = 2;
        while (containsTechniqueRemovalLabel(existing, label)) {
            label = baseLabel + " #" + suffix;
            suffix++;
        }
        return label;
    }

    private boolean containsTechniqueRemovalLabel(List<RemovableTechniqueOption> options, String label) {
        for (RemovableTechniqueOption option : options) {
            if (option != null && label.equals(option.label())) return true;
        }
        return false;
    }

    private void removeAssociatedListGroup(String listName) {
        if (listName == null || listName.isBlank() || character == null || character.getLists() == null) return;
        character.getLists().removeIf(group -> {
            if (group == null || group.isEmpty()) return false;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null) continue;
                if (listName.equalsIgnoreCase(entry.getList().trim())) {
                    return true;
                }
            }
            return false;
        });
    }
	
	/*
	 * 
	 * UPDATERS
	 * 
	 */
	
	

	
	
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*public void resizeTrainTech(ArrayList<DataATTraining> trainList, int index) {
		for (int i = 0; i < trainList.size(); i++) {
			atAffinityL.get(i).setVisible(false);
			atNameL.get(i).setVisible(false);
			atMaxRankL.get(i).setVisible(false);
			atCurRankL.get(i).setVisible(false);
			atExpL.get(i).setVisible(false);
			atNextAtL.get(i).setVisible(false);
			
			
		}
	}*/
	
	
	
}

