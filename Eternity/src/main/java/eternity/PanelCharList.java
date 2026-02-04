package eternity;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class PanelCharList extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<JLabel> titles ;
	private ArrayList<ArrayList<JTextField>> lists;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharList (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		
		titles = new ArrayList<JLabel>();
		lists = new ArrayList<ArrayList<JTextField>>();
		lists.add(new ArrayList<JTextField>());
	} //END OF PARAMETERIZED CONSTRUCTOR
	
	/*
	 * 
	 * UPDATERS
	 * 
	 */
	/*
	 * updateMain - updates the main panel
	 */
	public void updateAll() {
		updateList();
		resizeSheet();
	}
	public void updateList() {
		List<List<String>> tempLists = new ArrayList<>();

		if (character != null) {
			// Natural affinities
			if (character.getTraining() != null && !character.getTraining().getNaturalAffinities().isEmpty()) {
				List<String> affinities = new ArrayList<>();
				affinities.add("Natural Affinities");
				affinities.addAll(character.getTraining().getNaturalAffinities());
				tempLists.add(affinities);
			}
			// Domains
			if (character.getTraining() != null && !character.getTraining().getDomains().isEmpty()) {
				List<String> domains = new ArrayList<>();
				domains.add("Domains");
				domains.addAll(character.getTraining().getDomains());
				tempLists.add(domains);
			}
			// Weapon proficiencies
			if (character.getInventory() != null && !character.getInventory().getWeaponProficiencies().isEmpty()) {
				List<String> prof = new ArrayList<>();
				prof.add("Weapon Proficiencies");
				prof.addAll(character.getInventory().getWeaponProficiencies());
				tempLists.add(prof);
			}
		}

		for (int i = titles.size() -1; i >= 0; i--) {			// remove all
			for (int j = lists.get(i).size() -1; j >= 0; j--) {
				lists.get(i).get(j).setVisible(false);
				remove(lists.get(i).get(j));
				lists.get(i).remove(j);
			}
			titles.get(i).setVisible(false);
			remove(titles.get(i));
			titles.remove(i);
		}
			
		titles = new ArrayList<JLabel>();
		lists = new ArrayList<ArrayList<JTextField>>();
		
		for (int i = 0; i < tempLists.size(); i++) {			// add all
			if (tempLists.get(i).isEmpty()) continue;
			JLabel tempLabel = buildLabel(tempLists.get(i).get(0));		//create title
			titles.add(tempLabel);
			
			lists.add(new ArrayList<JTextField>());
			for (int j = 1; j < tempLists.get(i).size(); j++) {
				JTextField tempField = buildTextField(tempLists.get(i).get(j));
				tempField.setEditable(false);
				lists.get(i).add(tempField);
			}
		}	
	}
	
	public void resizeSheet() {
		pageHeight = 120;

		
		for (int i = 0; i < titles.size(); i++ ) {
			titles.get(i).setBounds(5,pageHeight,570,20);
			pageHeight += 25;
		
			for (int j = 0; j < lists.get(i).size(); j++) {
				if (j % 2 == 1)
					pageHeight -= 25;
					
				lists.get(i).get(j).setBounds(5 + 285*(j%2),pageHeight,280,20);
				pageHeight += 25;
			}
		}
		
		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

	


	

	
}
