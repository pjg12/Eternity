package eternity;

import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Frame holding NEW training selector.
 */
class FrameTrainingNew extends FrameTraining {
	private static final long serialVersionUID = 1L;

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
		updateNewTraining();
	}

	public void updateNewTraining() {
		matchAffinity();
		buildTypeBox();
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
	}

	public void updateNewTechList() {
		warn = false;
		auraTech.removeAllItems();
		if (character == null || character.getTraining() == null) return;
		String cat = (String) auraType.getSelectedItem();
		if (cat == null) return;

		List<DataTraining> all = dataQuery.searchTraining(""); // returns all trainings
		for (DataTraining t : all) {
			if (t.getAffinity() != null && t.getAffinity().equalsIgnoreCase(cat)) {
				if (character.getTraining().getTrainingByName(t.getName()) == null && t.getMaxRank(character) > 0) {
					auraTech.addItem(t.getName());
				}
			}
		}

		if (auraTech.getItemCount() == 0) {
			auraTech.addItem("No techniques available");
			setTrainingFieldsVisible(false);
		} else {
			setTrainingFieldsVisible(true);
		}
	}

	void updateNewTechInfo(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) return;
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		if ("No techniques available".equalsIgnoreCase(name)) {
			setTrainingFieldsVisible(false);
			return;
		}
		List<DataTraining> all = dataQuery.searchTraining("");
		for (DataTraining t : all) {
			if (t.getName().equalsIgnoreCase(name) && t.getAffinity().equalsIgnoreCase(cat)) {
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
				return;
			}
		}
	}



	public void newConfirm() {
		if (character == null || character.getTraining() == null) return;
		double expGain = numFields[5].getValue() == null ? 0.0 : Double.parseDouble(numFields[5].getValue().toString());
		if (expGain <= 0) return;
		double hours = 0.0;
		try { hours = Double.parseDouble(String.valueOf(numFields[4].getValue())); } catch (Exception ignore) {}
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		List<DataTraining> all = dataQuery.searchTraining("");
		DataTraining template = all.stream()
				.filter(t -> t.getName().equalsIgnoreCase(name) && t.getAffinity().equalsIgnoreCase(cat))
				.findFirst().orElse(null);
		if (template == null) return;

		DataTraining tech = new DataTraining(template);
		double currentExp = tech.getExp();
		double nextAt = tech.getNextAt(character);
		int currentRank = tech.getRank();
		int maxRank = tech.getMaxRank(character);

		boolean willLevel = currentExp + expGain >= nextAt;
		if (willLevel) {
			if (!confirmLevelUpProgress(hours, expGain, currentExp, nextAt, currentRank, maxRank)) return;
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
		character.getTraining().addTraining(tech);
		advanceCampaignTime(hours);
		String keepName = (String) auraTech.getSelectedItem();
		updateNewTechList();
		if (keepName != null) {
			auraTech.setSelectedItem(keepName);
		}
		numFields[4].setValue(0.0);
		if (sheetFrame != null) sheetFrame.loadCharacter(character);
	}
}
