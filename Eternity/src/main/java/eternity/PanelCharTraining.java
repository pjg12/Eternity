package eternity;

import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

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
	
	private ArrayList<ArrayList<String>> charLists;
	private ArrayList<String> listTitles;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharTraining (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		
		naturalAffinityL = buildLabel("Natural Affinity");
		natAffinity = new ArrayList<JTextField>();
		
		atMaxTechL = buildLabel("Max Techs");
		atCurTechL = buildLabel("Current Techs");
		atRemTechL = buildLabel("Remain Techs");
		atMaxTech = buildNumTextField(0); 
		atMaxTech.setEditable(false);
		atCurTech = buildNumTextField(0); 
		atRemTech = buildNumTextField(0);
		
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

		for (int i = 0; i < 24; i++) {
			atAffinityL.add(buildLabel("Affinity:"));
			atNameL.add(buildLabel("Name:"));
			atMaxRankL.add(buildLabel("Max:"));
			atCurRankL.add(buildLabel("Cur:"));
			atExpL.add(buildLabel("Exp:"));
			atNextAtL.add(buildLabel("Next:"));

			atName.add(new ArrayList<JTextField>());
			atAffinity.add(new ArrayList<JTextField>());
			atMaxRank.add(new ArrayList<JFormattedTextField>());
			atCurRank.add(new ArrayList<JFormattedTextField>());
			atExp.add(new ArrayList<JFormattedTextField>());
			atNextAt.add(new ArrayList<JFormattedTextField>());
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
		// Exclude specific generic categories from "current techs"
		int current = training.getTotalRanksExcluding(List.of("Attribute","Misc","Affinity","Fundamental","Standard","Crafting"));
		atCurTech.setValue(current);
		atRemTech.setValue(Math.max(0, maxTechs - current));
		
		// remove all
		for (int i = natAffinity.size() -1; i >= 0; i--) {
			natAffinity.get(i).setVisible(false);
			remove(natAffinity.get(i));
			natAffinity.remove(i);
		}
		
		for (int i = 23; i >= 0; i--) {
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

		for (int i = 0; i < 24; i++) {
			atName.add(new ArrayList<JTextField>());
			atAffinity.add(new ArrayList<JTextField>());
			atMaxRank.add(new ArrayList<JFormattedTextField>());
			atCurRank.add(new ArrayList<JFormattedTextField>());
			atExp.add(new ArrayList<JFormattedTextField>());
			atNextAt.add(new ArrayList<JFormattedTextField>());
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
		
		// Flatten categories for display (up to 25 rows safeguard)
		int row = 0;
		for (String cat : TRAINING) {
			if (row >= 24) break;
			java.util.List<DataTraining> list = training.getTrainingList(cat);
			for (DataTraining tech : list) {
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
				atName.get(row).add(buildTextField(tech.getName()));

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
			}
			row++;
		}
	}
	
	public void resizeSheet() {
		pageHeight = 120;
		
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
		
		for (int i = 0; i < 24; i++) {
			if (!atName.get(i).isEmpty()) {
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
