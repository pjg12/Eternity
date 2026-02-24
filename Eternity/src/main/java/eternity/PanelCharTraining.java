package eternity;

import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import eternity.CharTraining;
import eternity.DataColor;
import eternity.DataTraining;

/*
 * 		TRAINING PANEL
 */
public class PanelCharTraining extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
	private JLabel naturalAffinityL;
	private ArrayList<JTextField> natAffinity;
	
	//aura training
	private JButton trainNewButton, trainExistingButton;
	private JLabel auraTrainingL, atMaxTechL, atCurTechL, atRemTechL;
	private JFormattedTextField atMaxTech, atCurTech, atRemTech;
	
	private ArrayList<JLabel> atAffinityL, atNameL, atMaxRankL, atCurRankL, atExpL, atNextAtL;
	private ArrayList<ArrayList<JTextField>> atName, atAffinity;
	private ArrayList<ArrayList<JFormattedTextField>> atMaxRank, atCurRank, atExp, atNextAt;
	private ArrayList<ArrayList<String>> atTypeGroup;
	private ArrayList<ArrayList<Boolean>> atAuraAffinityRow;
	private ArrayList<JButton> atSectionToggleB;
	private Map<String, Boolean> atSectionCollapsed;
	
	private ArrayList<ArrayList<String>> charLists;
	private ArrayList<String> listTitles;
	private Color defaultToggleBack, defaultToggleFore;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharTraining (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(222, 232, 244));
		
		naturalAffinityL = buildLabel("Natural Affinity");
		natAffinity = new ArrayList<JTextField>();
		
		atMaxTechL = buildLabel("Max Techs");
		atCurTechL = buildLabel("Current Techs");
		atRemTechL = buildLabel("Remain Techs");
		atMaxTech = buildNumTextField(0); 
		atMaxTech.setEditable(false);
		atCurTech = buildNumTextField(0.0); 
		atRemTech = buildNumTextField(0.0);
		
		trainNewButton = buildButton("Train New");
		trainNewButton.addActionListener (e -> sheetFrame.trainNewPressed());
		trainNewButton.setToolTipText("Open a dialog to select a new technique to train."); 
		
		trainExistingButton = buildButton("Train Existing");
		trainExistingButton.addActionListener (e -> sheetFrame.trainExistingPressed());
		trainExistingButton.setToolTipText("Open a dialog to select a technique to improve."); 
			
		atAffinityL = new ArrayList<JLabel>();
		atNameL = new ArrayList<JLabel>();
		atMaxRankL = new ArrayList<JLabel>();
		atCurRankL = new ArrayList<JLabel>();
		atExpL = new ArrayList<JLabel>();
		atNextAtL = new ArrayList<JLabel>();
		
		atAffinity = new ArrayList<ArrayList<JTextField>>();
		atName = new ArrayList<ArrayList<JTextField>>();
		atMaxRank = new ArrayList<ArrayList<JFormattedTextField>>();
		atCurRank = new ArrayList<ArrayList<JFormattedTextField>>();
		atExp = new ArrayList<ArrayList<JFormattedTextField>>();
		atNextAt = new ArrayList<ArrayList<JFormattedTextField>>();
		atTypeGroup = new ArrayList<ArrayList<String>>();
		atAuraAffinityRow = new ArrayList<ArrayList<Boolean>>();
		atSectionToggleB = new ArrayList<JButton>();
		atSectionCollapsed = new HashMap<String, Boolean>();
		defaultToggleBack = UIManager.getColor("Button.background");
		defaultToggleFore = UIManager.getColor("Button.foreground");
		if (defaultToggleBack == null) defaultToggleBack = new Color(240, 240, 240);
		if (defaultToggleFore == null) defaultToggleFore = Color.BLACK;

		for (int i = 0; i < TRAINING.length; i++) {
			final int sectionIndex = i;
			atAffinityL.add(buildLabel("Affinity:"));
			atNameL.add(buildLabel("Name:"));
			atMaxRankL.add(buildLabel("Max:"));
			atCurRankL.add(buildLabel("Cur:"));
			atExpL.add(buildLabel("Exp:"));
			atNextAtL.add(buildLabel("Next:"));
			JButton toggleButton = buildButton("[-]");
			toggleButton.addActionListener(e -> toggleSection(sectionIndex));
			atSectionToggleB.add(toggleButton);

			atName.add(new ArrayList<JTextField>());
			atAffinity.add(new ArrayList<JTextField>());
			atMaxRank.add(new ArrayList<JFormattedTextField>());
			atCurRank.add(new ArrayList<JFormattedTextField>());
			atExp.add(new ArrayList<JFormattedTextField>());
			atNextAt.add(new ArrayList<JFormattedTextField>());
			atTypeGroup.add(new ArrayList<String>());
			atAuraAffinityRow.add(new ArrayList<Boolean>());
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
	}  /*--------------
		END UPDATEALL
		--------------*/
	
	/*
	 * 		UPDATE TRAINING
	 */
	public void updateTraining() {
		CharTraining training = character.getTraining();
		if (training == null) return;

		// ensure underlying character data is up to date (including max techs)
		character.updateAll();

		// compute max techs from the character's current level
		int maxTechs = 0;
		if (character.getIdentity() != null) {
			DataLevel levelData = dataQuery.getLevel(character.getIdentity().getLevel());
			if (levelData != null) maxTechs = Math.max(0, levelData.getBaseTechs());
		}

		// update straight values
		atMaxTech.setValue(maxTechs);
		// Exclude specific generic categories from "current techs" and apply natural-affinity weighting.
		double current = calculateWeightedCurrentTechs(training);
		atCurTech.setValue(round2(current));
		atRemTech.setValue(round2(Math.max(0.0, maxTechs - current)));
		
		// remove all
		for (int i = natAffinity.size() -1; i >= 0; i--) {
			natAffinity.get(i).setVisible(false);
			remove(natAffinity.get(i));
			natAffinity.remove(i);
		}
		
		for (int i = TRAINING.length - 1; i >= 0; i--) {
			for (int j = atAffinity.get(i).size() -1; j >= 0; j--) {
				atAffinity.get(i).get(j).setVisible(false);
				atName.get(i).get(j).setVisible(false);
				atMaxRank.get(i).get(j).setVisible(false);
				atCurRank.get(i).get(j).setVisible(false);
				atExp.get(i).get(j).setVisible(false);
				atNextAt.get(i).get(j).setVisible(false);
				
				remove(atAffinity.get(i).get(j));
				remove(atName.get(i).get(j));
				remove(atMaxRank.get(i).get(j));
				remove(atCurRank.get(i).get(j));
				remove(atExp.get(i).get(j));
				remove(atNextAt.get(i).get(j));
			}
		}

		atAffinity = new ArrayList<ArrayList<JTextField>>();
		atName = new ArrayList<ArrayList<JTextField>>();
		atMaxRank = new ArrayList<ArrayList<JFormattedTextField>>();
		atCurRank = new ArrayList<ArrayList<JFormattedTextField>>();
		atExp = new ArrayList<ArrayList<JFormattedTextField>>();
		atNextAt = new ArrayList<ArrayList<JFormattedTextField>>();
		atTypeGroup = new ArrayList<ArrayList<String>>();
		atAuraAffinityRow = new ArrayList<ArrayList<Boolean>>();

		for (int i = 0; i < TRAINING.length; i++) {
			atName.add(new ArrayList<JTextField>());
			atAffinity.add(new ArrayList<JTextField>());
			atMaxRank.add(new ArrayList<JFormattedTextField>());
			atCurRank.add(new ArrayList<JFormattedTextField>());
			atExp.add(new ArrayList<JFormattedTextField>());
			atNextAt.add(new ArrayList<JFormattedTextField>());
			atTypeGroup.add(new ArrayList<String>());
			atAuraAffinityRow.add(new ArrayList<Boolean>());
		}
		
		// add all
		ArrayList<String> tempList = new ArrayList<>(training.getNaturalAffinities());
		for (int i = 0; i < tempList.size(); i++) {
			JTextField tempField = buildTextField(tempList.get(i));
			natAffinity.add(tempField);
			DataColor color = dataQuery.getColorByTitle(tempList.get(i));
			if (color != null) {
				tempField.setBackground(color.getBackColor());
				tempField.setForeground(color.getForeColor());
			}
		}
		
		// Build per-category display rows
		for (int row = 0; row < TRAINING.length; row++) {
			String cat = TRAINING[row];
			ArrayList<DataTraining> list = new ArrayList<>(training.getTrainingList(cat));
			list.sort(Comparator
					.comparingInt((DataTraining t) -> isAuraAffinityTech(t) ? 0 : 1)
					.thenComparingInt((DataTraining t) -> getTypeOrder(getTypeGroup(t == null ? null : t.getType())))
					.thenComparingInt(t -> t == null ? Integer.MAX_VALUE : t.getId())
					.thenComparing(t -> t == null || t.getName() == null ? "" : t.getName(), String.CASE_INSENSITIVE_ORDER));
			for (DataTraining tech : list) {
				if (tech == null) continue;
				String typeGroup = getTypeGroup(tech.getType());
				boolean auraAffinity = isAuraAffinityTech(tech);

				// Affinity column (if available)
				if (tech.getAffinity() != null && !tech.getAffinity().isBlank()) {
					JTextField tempField = buildTextField(tech.getAffinity());
					atAffinity.get(row).add(tempField);
					DataColor color = dataQuery.getColorByTitle(tech.getAffinity());
					if (color != null) {
						tempField.setBackground(color.getBackColor());
						tempField.setForeground(color.getForeColor());
					}
				}

				// Name
				String displayName = tech.getName();
				if (displayName != null && displayName.equalsIgnoreCase("Race Training")) {
					DataSpecialty racial = character.getSpecials() != null ? character.getSpecials().getRacialSpecialty() : null;
					if (racial != null && racial.getName() != null && !racial.getName().isBlank()) {
						displayName = racial.getName();
					}
				}
				atName.get(row).add(buildTextField(displayName));
				atTypeGroup.get(row).add(typeGroup);
				atAuraAffinityRow.get(row).add(auraAffinity);

				// Ranks / exp
				int maxRank = tech.getMaxRank(character);
				JFormattedTextField maxField = buildNumTextField(maxRank);
				JFormattedTextField curField = buildNumTextField(tech.getRank());

				if (maxRank == tech.getRank()) {
					maxField.setForeground(Color.RED);
					curField.setForeground(Color.RED);
				}

				atMaxRank.get(row).add(maxField);
				atCurRank.get(row).add(curField);
				atExp.get(row).add(buildNumTextField(tech.getExp()));
				int nextAtVal = tech.getNextAt(character);
				atNextAt.get(row).add(buildNumTextField(nextAtVal));

				// If this is an Attribute training, push a passive status into character attributes
				if ("Attribute".equalsIgnoreCase(cat) && character.getAttributes() != null) {
					String attKey = tech.getAffinity() != null ? tech.getAffinity().toUpperCase() : null;
					if (attKey != null && !attKey.isBlank()) {
						String statusName = "Attribute Training: " + tech.getName();
						DataStatus ds = new DataStatus();
						ds.setName(statusName);
						ds.setAttribute(attKey);
						ds.setDurationType("Permanent");
						ds.setSeverity(tech.getRank());
						ds.setAffinity("None");
						ds.setDescription("Attribute training rank bonus");
						character.getAttributes().removeStatus("attribute", attKey, statusName);
						character.getAttributes().addStatus("attribute", attKey, ds);
					}
				}
			}
		}
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

		for (int i = 0; i < TRAINING.length; i++) {
			atAffinityL.get(i).setVisible(false);
			atNameL.get(i).setVisible(false);
			atMaxRankL.get(i).setVisible(false);
			atCurRankL.get(i).setVisible(false);
			atExpL.get(i).setVisible(false);
			atNextAtL.get(i).setVisible(false);
			atSectionToggleB.get(i).setVisible(false);
			for (int j = 0; j < atName.get(i).size(); j++) {
				if (!atAffinity.get(i).isEmpty()) atAffinity.get(i).get(j).setVisible(false);
				atName.get(i).get(j).setVisible(false);
				atMaxRank.get(i).get(j).setVisible(false);
				atCurRank.get(i).get(j).setVisible(false);
				atExp.get(i).get(j).setVisible(false);
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

		atMaxTechL.setBounds(40,pageHeight,140,19);
		atCurTechL.setBounds(220,pageHeight,140,19);
		atRemTechL.setBounds(400,pageHeight,140,19);
		pageHeight += 20;
		
		atMaxTech.setBounds(40,pageHeight,140,19);
		atCurTech.setBounds(220,pageHeight,140,19);
		atRemTech.setBounds(400,pageHeight,140,19);
		pageHeight += 30;
		
		trainNewButton.setBounds(40,pageHeight,200,19);
		trainExistingButton.setBounds(340,pageHeight,200,19);
		pageHeight += 30;
		
		for (int i = 0; i < TRAINING.length; i++) {
			if (!atName.get(i).isEmpty()) {
				boolean collapsed = isSectionCollapsed(TRAINING[i]);
				String title = (i < TRAININGTITLE.length ? TRAININGTITLE[i] : TRAINING[i]) + " (" + atName.get(i).size() + ")";
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

				atExpL.get(i).setVisible(true);
				atExpL.get(i).setBounds(455, pageHeight, 50, 20);

				atNextAtL.get(i).setVisible(true);
				atNextAtL.get(i).setBounds(510, pageHeight, 50, 20);
				pageHeight += 20;

				for (int j = 0; j < atName.get(i).size(); j++) {
					String currGroup = (i < atTypeGroup.size() && j < atTypeGroup.get(i).size())
							? atTypeGroup.get(i).get(j)
							: "Active";
					boolean currAuraAffinity = (i < atAuraAffinityRow.size() && j < atAuraAffinityRow.get(i).size())
							? Boolean.TRUE.equals(atAuraAffinityRow.get(i).get(j))
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
						atAffinity.get(i).get(j).setVisible(true);
						atAffinity.get(i).get(j).setBounds(5, pageHeight, 120, 20);
					}
					
					atName.get(i).get(j).setVisible(true);
					atName.get(i).get(j).setBounds(130, pageHeight, 210, 20);
				
					atMaxRank.get(i).get(j).setVisible(true);
					atMaxRank.get(i).get(j).setBounds(345, pageHeight, 50, 20);
				
					atCurRank.get(i).get(j).setVisible(true);
					atCurRank.get(i).get(j).setBounds(400, pageHeight, 50, 20);
				
					atExp.get(i).get(j).setVisible(true);
					atExp.get(i).get(j).setBounds(455, pageHeight, 50, 20);
				
					atNextAt.get(i).get(j).setVisible(true);
					atNextAt.get(i).get(j).setBounds(510, pageHeight, 50, 20);
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
		List<String> excluded = List.of("Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting");
		List<String> natural = training.getNaturalAffinities();
		double total = 0.0;

		for (String category : training.getTrainingCategories()) {
			boolean excludedCat = excluded.stream().anyMatch(s -> s.equalsIgnoreCase(category));
			if (excludedCat) continue;

			for (DataTraining tech : training.getTrainingList(category)) {
				if (tech == null) continue;
				boolean naturalMatch = natural.stream().anyMatch(n ->
						(n != null && category != null && n.equalsIgnoreCase(category)) ||
						(n != null && tech.getAffinity() != null && n.equalsIgnoreCase(tech.getAffinity())));
				boolean spiritOrTime = ("Spirit".equalsIgnoreCase(category) || "Time".equalsIgnoreCase(category)) ||
						("Spirit".equalsIgnoreCase(tech.getAffinity()) || "Time".equalsIgnoreCase(tech.getAffinity()));
				double multiplier = 1.0;
				if (naturalMatch) multiplier *= 0.5;
				if (spiritOrTime) multiplier *= 2.0;
				total += tech.getRank() * multiplier;
			}
		}
		return total;
	}

	private boolean isAuraAffinityTech(DataTraining tech) {
		if (tech == null || tech.getName() == null) return false;
		return tech.getName().trim().toLowerCase().startsWith("aura affinity");
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

