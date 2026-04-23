package eternity;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 * Frame holding NEW training selector.
 */
class FrameTrainingNew extends FrameTraining {
	private static final long serialVersionUID = 1L;
	private static final String NO_TECHNIQUES = "No techniques available";
	private final Map<String, List<DataTraining>> trainingsByAffinity = new HashMap<>();
	private final Map<String, DataTraining> trainingByAffinityAndName = new HashMap<>();
	private final Map<String, List<String>> availableTechNamesByAffinity = new HashMap<>();
	private boolean typeBoxBuilt = false;
	private String lastAffinity = null;

	FrameTrainingNew(FrameSheet sheetFrame, DataQuery dataQuery) {
		super(sheetFrame, dataQuery);
		headerL.setText("Select NEW Technique to Train");
		auraType.addActionListener(e -> updateNewTechList());
		auraTech.addItemListener(e -> updateNewTechInfo(e));
		buttons[1].addActionListener(e -> newConfirm());
		buttons[4].setText("Existing");
		buttons[4].addActionListener(e -> {
			setVisible(false);
			sheetFrame.trainExistingPressed();
		});
	}

	@Override
	public void updateCharacter(CharData character) {
		super.updateCharacter(character);
		availableTechNamesByAffinity.clear();
		lastAffinity = null;
		updateNewTraining();
	}

	public void updateNewTraining() {
		matchAffinity();
		if (!typeBoxBuilt) {
			buildTypeBox();
		}
		updateNewTechList();
	}

	public void buildTypeBox() {
		auraType.removeAllItems();
		for (int i = 5; i < AURATYPES.length; i++) {
			auraType.addItem(AURATYPES[i]);
		}
		if (auraType.getItemCount() > 0) {
			auraType.setSelectedIndex(0);
		}
		typeBoxBuilt = true;
	}

	public void updateNewTechList() {
		warn = false;
		if (character == null || character.getTraining() == null) return;
		String cat = (String) auraType.getSelectedItem();
		if (cat == null) return;
		if (cat.equals(lastAffinity)) return;
		lastAffinity = cat;

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

	private boolean isPrerequisiteMet(DataTraining tech) {
		if (tech == null || character == null || character.getTraining() == null) return false;
		if (tech.getPrereq() == -1) return true;
		DataTraining req = character.getTraining().getTrainingById(tech.getPrereq());
		return req != null && req.getRank() > 0;
	}

	void updateNewTechInfo(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) return;
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		if (NO_TECHNIQUES.equalsIgnoreCase(name)) {
			setTrainingFieldsVisible(false);
			return;
		}
		DataTraining t = getTrainingTemplate(cat, name);
		if (t != null) {
			numFields[0].setValue(t.getMaxRank(character));
			numFields[1].setValue(0);
			numFields[2].setValue(0);
			// For new techniques, show the first rank-up threshold (rank 0 -> 1)
			DataTraining preview = new DataTraining(t);
			preview.setRank(0);
			preview.setExp(0.0);
			numFields[3].setValue(preview.getNextAt(character));
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



	public void newConfirm() {
		if (character == null || character.getTraining() == null) return;
		double expGain = numFields[5].getValue() == null ? 0.0 : Double.parseDouble(numFields[5].getValue().toString());
		if (expGain <= 0) return;
		double hours = parseTrainingHours() == null ? 0.0 : parseTrainingHours();
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		DataTraining template = getTrainingTemplate(cat, name);
		if (template == null) return;

		DataTraining tech = new DataTraining(template);
		// New techniques must always start at rank 0 with no accumulated EXP.
		tech.setRank(0);
		tech.setExp(0.0);
		tech.setAl(0);
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
		character.getTraining().addTraining(tech);
		if (shouldAdvanceTime()) {
			advanceCampaignTime(hours);
		}
		String keepName = (String) auraTech.getSelectedItem();
		availableTechNamesByAffinity.clear();
		lastAffinity = null;
		updateNewTechList();
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
			setVisible(false);
			sheetFrame.trainExistingPressed(cat, tech.getName());
		}
	}

	private List<DataTraining> getTrainingsForAffinity(String affinity) {
		return trainingsByAffinity.computeIfAbsent(normalizeKey(affinity), key -> {
			List<DataTraining> matches = new ArrayList<>();
			List<DataTraining> all = dataQuery.getTrainingData();
			for (DataTraining training : all) {
				if (training == null || training.getAffinity() == null) continue;
				if (training.getAffinity().equalsIgnoreCase(affinity)) {
					matches.add(training);
					trainingByAffinityAndName.put(buildTrainingKey(training.getAffinity(), training.getName()), training);
				}
			}
			return matches;
		});
	}

	private DataTraining getTrainingTemplate(String affinity, String name) {
		String key = buildTrainingKey(affinity, name);
		DataTraining cached = trainingByAffinityAndName.get(key);
		if (cached != null) {
			return cached;
		}
		getTrainingsForAffinity(affinity);
		return trainingByAffinityAndName.get(key);
	}

	private List<String> getAvailableTechNames(String affinity) {
		return availableTechNamesByAffinity.computeIfAbsent(normalizeKey(affinity), key -> {
			ArrayList<String> names = new ArrayList<>();
			Set<String> ownedNames = collectOwnedTrainingNames();
			for (DataTraining training : getTrainingsForAffinity(affinity)) {
				String trainingName = training.getName();
				if (trainingName == null) continue;
				if (ownedNames.contains(normalizeKey(trainingName))) continue;
				if (!isPrerequisiteMet(training)) continue;
				if (training.getMaxRank(character) <= 0) continue;
				names.add(trainingName);
			}
			return names;
		});
	}

	private Set<String> collectOwnedTrainingNames() {
		Set<String> owned = new HashSet<>();
		for (DataTraining training : character.getTraining().getAllTraining()) {
			if (training != null && training.getName() != null) {
				owned.add(normalizeKey(training.getName()));
			}
		}
		return owned;
	}

	private String buildTrainingKey(String affinity, String name) {
		String left = normalizeKey(affinity);
		String right = normalizeKey(name);
		return left + "|" + right;
	}

	private String normalizeKey(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}
}
