package eternity;

import java.awt.Dimension;
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
	private JButton mtUpdateButton, mtMaxButton, mtOffButton;
	
	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharMaintained (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
			
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
		pageHeight = 120;
		maintainedTechsL.setBounds(5, pageHeight, 555, 20);
		pageHeight += 20;
		
		mtAffinityL.setBounds(5, pageHeight, 120, 20);
		mtNameL.setBounds(130, pageHeight, 210, 20);
		mtMaxL.setBounds(345, pageHeight, 50, 20);
		mtActLevelL.setBounds(400, pageHeight, 50, 20);
		mtCostPerALL.setBounds(455, pageHeight, 50, 20);
		mtCostL.setBounds(510, pageHeight, 50, 20);
		
		pageHeight += 20;
		
		
		for (int i = 0; i < mtName.size(); i++) {
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

		if (character != null && character.getTraining() != null) {
			for (DataTraining tech : character.getTraining().getAllTraining()) {
				if (tech == null) continue;
				if (!"Maintained".equalsIgnoreCase(tech.getType())) continue;
				if (tech.getRank() < 1) continue;

				// Affinity
				JTextField affField = buildTextField(tech.getAffinity());
				mtAffinity.add(affField);

				// Name
				JTextField nameField = buildTextField(tech.getName());
				mtName.add(nameField);

				// Max rank
				JFormattedTextField maxField = buildNumTextField(tech.getMaxRank(character));
				mtMax.add(maxField);

				// Active level (editable)
				JFormattedTextField actField = buildNumTextField(tech.getRank());
				actField.setEditable(true);
				mtActLevel.add(actField);

				// Cost placeholders (no cost data in model)
				mtCostPerAL.add(buildNumTextField(0));
				mtCost.add(buildNumTextField(0));
			}
		}



		resizeSheet();
	}

	@Override
	public void updateCharacter(CharData character) {
		super.updateCharacter(character);
		updateMaintained();
	}
	
	
	

	
	
	

	public void mtUpdate () {
		// Placeholder: maintained techniques not modeled in CharData yet.
		System.out.println("mtUpdate() not implemented with current data model.");
	    resizeSheet();
	}
	
	public void mtMax () {
		System.out.println("mtMax() not implemented with current data model.");
		mtUpdate();
	}
	
	public void mtOff () {
		System.out.println("mtOff() not implemented with current data model.");
		mtUpdate();
	}
	

	
}
