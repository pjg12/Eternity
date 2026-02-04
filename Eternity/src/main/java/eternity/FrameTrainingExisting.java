package eternity;

import java.awt.event.ItemEvent;
import java.util.Objects;

import javax.swing.JOptionPane;

/**
 * Frame holding existing training selector.
 */
class FrameTrainingExisting extends FrameTraining {
	private static final long serialVersionUID = 1L;

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
		updateExistingTraining();
	}

	public void updateExistingTraining() {
		matchAffinity();
		buildTypeBox();
		updateExTechList();
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
		auraTech.removeAllItems();
		if (character == null || character.getTraining() == null) return;
		String cat = (String) auraType.getSelectedItem();
		if (cat == null) return;
		for (DataTraining t : character.getTraining().getTrainingList(cat)) {
			auraTech.addItem(t.getName());
		}

		if (auraTech.getItemCount() == 0) {
			auraTech.addItem("No techniques available");
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
		if ("No techniques available".equalsIgnoreCase(name)) {
			setTrainingFieldsVisible(false);
			return;
		}

		for (DataTraining t : character.getTraining().getTrainingList(cat)) {
			if (t.getName().equalsIgnoreCase(name)) {
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
				return;
			}
		}
	}

	public void existingConfirm() {
		if (character == null || character.getTraining() == null) return;
		double expGain = numFields[5].getValue() == null ? 0.0 : Double.parseDouble(numFields[5].getValue().toString());
		if (expGain <= 0) return;
		double hours = 0.0;
		try { hours = Double.parseDouble(String.valueOf(numFields[4].getValue())); } catch (Exception ignore) {}
		String cat = (String) auraType.getSelectedItem();
		String name = (String) auraTech.getSelectedItem();
		if (cat == null || name == null) return;
		DataTraining tech = character.getTraining().getTrainingByName(name);
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
		advanceCampaignTime(hours);
		String keepName = (String) auraTech.getSelectedItem();
		updateExTechList();
		if (keepName != null) {
			auraTech.setSelectedItem(keepName);
		}
		numFields[4].setValue(0.0);
		if (sheetFrame != null) sheetFrame.loadCharacter(character);
	}
}
