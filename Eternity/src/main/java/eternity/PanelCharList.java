package eternity;

import java.awt.Color;
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
		setBackground(new Color(169, 169, 169));
		
		titles = new ArrayList<JLabel>();
		lists = new ArrayList<ArrayList<JTextField>>();
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
		List<List<DataList>> tempLists = new ArrayList<>();
		if (character != null && character.getLists() != null) {
			tempLists.addAll(character.getLists());
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
			List<DataList> currList = tempLists.get(i);
			if (currList == null || currList.isEmpty()) continue;

			String title = "List";
			for (DataList data : currList) {
				if (data != null && data.getList() != null && !data.getList().isBlank()) {
					title = data.getList();
					break;
				}
			}

			JLabel tempLabel = buildLabel(title);		//create title
			titles.add(tempLabel);
			
			lists.add(new ArrayList<JTextField>());
			for (DataList data : currList) {
				if (data == null) continue;
				String name = data.getName() == null ? "" : data.getName();
				JTextField tempField = buildTextField(name);
				tempField.setEditable(false);
				if (data.getDescription() != null && !data.getDescription().isBlank()) {
					tempField.setToolTipText(data.getDescription());
				}
				lists.get(i).add(tempField);
			}
		}	
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

		
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

