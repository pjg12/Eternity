package eternity;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * Frame holding existing training selector.
 */
class FrameTrainingExisting extends FrameTraining {
	private static final long serialVersionUID = 1L;
	private static final String NO_TECHNIQUES = "No techniques available";
	private final Map<String, List<DataTraining>> trainingsByCategory = new HashMap<>();
	private final Map<String, DataTraining> trainingByCategoryAndName = new HashMap<>();
	private final Map<String, List<String>> availableTechNamesByCategory = new HashMap<>();
	private String lastCategory = null;
	private String trainingCategorySignature = null;

	FrameTrainingExisting(FrameSheet sheetFrame, DataQuery dataQuery) {
		super(sheetFrame, dataQuery);
		headerL.setText("Select EXISTING Technique to Train");
		auraType.addActionListener(e -> updateExTechList());
		auraTech.addItemListener(e -> updateExTechInfo(e));
		buttons[1].addActionListener(e -> existingConfirm());
		buttons[4].setText("New");
		buttons[4].addActionListener(e -> {
			setVisible(false);
			sheetFrame.trainNewPressed();
		});
	}

	@Override
	public void updateCharacter(CharData character) {
		super.updateCharacter(character);
		trainingsByCategory.clear();
		trainingByCategoryAndName.clear();
		availableTechNamesByCategory.clear();
		lastCategory = null;
		trainingCategorySignature = null;
		updateExistingTraining();
	}

	public void updateExistingTraining() {
		matchAffinity();
		String signature = buildTrainingCategorySignature();
		if (!signature.equals(trainingCategorySignature)) {
			buildTypeBox();
			trainingCategorySignature = signature;
		}
		updateExTechList();
	}

	public void selectTechnique(String category, String techniqueName) {
		if (category == null || techniqueName == null) return;
		auraType.setSelectedItem(category);
		auraTech.setSelectedItem(techniqueName);
	}

	public void buildTypeBox() {
		if (character == null || character.getTraining() == null) return;
		auraType.removeAllItems();
		for (String type : AURATYPES) {
			if (!character.getTraining().getTrainingList(type).isEmpty()
					|| type.equals("Attribute") || type.equals("Misc") || type.equals("Affinity")
					|| type.equals("Fundamental") || type.equals("Standard") || type.equals("Crafting")) {
				auraType.addItem(type);
			}
		}
		if (auraType.getItemCount() > 0) {
			auraType.setSelectedIndex(0);
		}
	}

	void updateExTechList() {
		warn = false;
		if (character == null || character.getTraining() == null) return;
		String cat = (String) auraType.getSelectedItem();
		if (cat == null) return;
		if (cat.equals(lastCategory)) return;
		lastCategory = cat;

		auraTech.removeAllItems();
		for (String techName : getAvailableTechNames(cat)) {
			auraTech.addItem(techName);
		}

		if (auraTech.getItemCount() == 0) {
			auraTech.addItem(NO_TECHNIQUES);
			setTrainingFieldsVisible(false);
		} else {
			setTrainingFieldsVisible(true);
		}
	}

	void updateExTechInfo(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) return;
		if (character == null || character.getTraining() == null) return;
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		if (NO_TECHNIQUES.equalsIgnoreCase(name)) {
			setTrainingFieldsVisible(false);
			return;
		}

		DataTraining t = getTraining(cat, name);
		if (t != null) {
			numFields[0].setValue(t.getMaxRank(character));
			numFields[1].setValue(t.getRank());
			numFields[2].setValue(t.getExp());
			numFields[3].setValue(t.getNextAt(character));
			boolean capped = t.getMaxRank(character) == t.getRank();
			labels[12].setVisible(capped);
			labels[13].setVisible(capped);
			if (capped) {
				labels[13].setText("<html><center>" + t.getPrereqCap(character) + "<br>Capped.");
			} else {
				labels[13].setText("");
			}
			updateTrainXp();
		}
	}

	public void existingConfirm() {
		if (character == null || character.getTraining() == null) return;
		double expGain = parseNumericField(numFields[5]);
		if (expGain <= 0) return;
		Double parsedHours = parseTrainingHours();
		double hours = parsedHours == null ? 0.0 : parsedHours;
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		DataTraining tech = getTraining(cat, name);
		if (tech == null) return;
		if (tech.getMaxRank(character) == tech.getRank()) {
			JOptionPane.showMessageDialog(this, "Technique is at Max Rank.");
			return;
		}
		double currentExp = tech.getExp();
		double nextAt = tech.getNextAt(character);
		int currentRank = tech.getRank();
		int maxRank = tech.getMaxRank(character);

		boolean willLevel = currentExp + expGain >= nextAt;
		if (willLevel) {
			if (!confirmLevelUpProgress(tech, hours, expGain, currentExp, nextAt, currentRank, maxRank)) return;
		} else {
			if (!confirmPartialProgress(hours, expGain, currentExp, nextAt)) return;
		}
		tech.setExp(tech.getExp() + expGain);
		if (tech.getExp() >= tech.getNextAt(character)) {
			tech.setExp(tech.getExp() - tech.getNextAt(character));
			tech.setRank(tech.getRank() + 1);
			JOptionPane.showMessageDialog(this, "Technique has leveled up to Rank " + tech.getRank() + ".");
			if (tech.getMaxRank(character) == tech.getRank()) tech.setExp(0.0);
		}
		int newRank = tech.getRank();
		if (shouldAdvanceTime()) {
			advanceCampaignTime(hours);
		}
		String keepName = (String) auraTech.getSelectedItem();
		availableTechNamesByCategory.clear();
		lastCategory = null;
		updateExTechList();
		if (keepName != null) {
			auraTech.setSelectedItem(keepName);
		}
		numFields[4].setValue(0.0);
		if (newRank > currentRank) {
			maybeGrantSkillFromTraining(tech, currentRank, newRank);
			maybeGrantSpecialtyFromTraining(tech, currentRank, newRank);
		}
		if (character != null) character.updateAll();
		if (sheetFrame != null) {
			sheetFrame.refreshTrainingPanel();
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
		}
	}

	private List<DataTraining> getTrainingsForCategory(String category) {
		return trainingsByCategory.computeIfAbsent(normalizeKey(category), key -> {
			ArrayList<DataTraining> list = new ArrayList<>();
			for (DataTraining training : character.getTraining().getTrainingList(category)) {
				if (training == null) continue;
				list.add(training);
				trainingByCategoryAndName.put(buildCategoryKey(category, training.getName()), training);
			}
			return list;
		});
	}

	private List<String> getAvailableTechNames(String category) {
		return availableTechNamesByCategory.computeIfAbsent(normalizeKey(category), key -> {
			ArrayList<String> names = new ArrayList<>();
			for (DataTraining training : getTrainingsForCategory(category)) {
				if (training != null && training.getName() != null) {
					names.add(training.getName());
				}
			}
			return names;
		});
	}

	private DataTraining getTraining(String category, String name) {
		String key = buildCategoryKey(category, name);
		DataTraining cached = trainingByCategoryAndName.get(key);
		if (cached != null) {
			return cached;
		}
		getTrainingsForCategory(category);
		return trainingByCategoryAndName.get(key);
	}

	private String buildCategoryKey(String category, String name) {
		String left = normalizeKey(category);
		String right = normalizeKey(name);
		return left + "|" + right;
	}

	private String normalizeKey(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	private String buildTrainingCategorySignature() {
		if (character == null || character.getTraining() == null) return "";
		StringBuilder signature = new StringBuilder();
		for (String type : AURATYPES) {
			signature.append(type).append('=')
					.append(character.getTraining().getTrainingList(type).size())
					.append(';');
		}
		return signature.toString();
	}

	private double parseNumericField(javax.swing.JFormattedTextField field) {
		Object value = field.getValue();
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		String text = field.getText();
		if (text == null || text.isBlank()) {
			return 0.0;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException ignore) {
			return 0.0;
		}
	}
}
