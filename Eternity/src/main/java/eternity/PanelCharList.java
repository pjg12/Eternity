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
	private String cachedListSignature = "";

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
		boolean structureChanged = updateList();
		if (structureChanged) {
			resizeSheet();
		}
	}
	public boolean updateList() {
		List<List<DataList>> tempLists = character != null && character.getLists() != null ? character.getLists() : List.of();
		String signature = buildListSignature(tempLists);
		boolean structureChanged = !signature.equals(cachedListSignature);
		cachedListSignature = signature;

		int usedSections = 0;
		for (List<DataList> currList : tempLists) {
			if (currList == null || currList.isEmpty()) continue;

			ensureSectionCapacity(usedSections);
			String title = resolveListTitle(currList);
			JLabel label = titles.get(usedSections);
			label.setText(title);
			label.setVisible(true);

			ArrayList<JTextField> fields = lists.get(usedSections);
			ensureFieldCapacity(fields, currList.size());
			int usedFields = 0;
			for (DataList data : currList) {
				if (data == null) continue;
				JTextField field = fields.get(usedFields);
				field.setText(data.getName() == null ? "" : data.getName());
				field.setToolTipText(data.getDescription() != null && !data.getDescription().isBlank() ? data.getDescription() : null);
				field.setVisible(true);
				usedFields++;
			}
			hideUnusedFields(fields, usedFields);
			usedSections++;
		}

		hideUnusedSections(usedSections);
		return structureChanged;
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

		
		for (int i = 0; i < titles.size(); i++ ) {
			if (!titles.get(i).isVisible()) continue;
			titles.get(i).setBounds(5,pageHeight,570,20);
			pageHeight += 25;
		
			for (int j = 0; j < lists.get(i).size(); j++) {
				if (!lists.get(i).get(j).isVisible()) continue;
				if (j % 2 == 1)
					pageHeight -= 25;
					
				lists.get(i).get(j).setBounds(5 + 285*(j%2),pageHeight,280,20);
				pageHeight += 25;
			}
		}
		
		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}

	private String buildListSignature(List<List<DataList>> tempLists) {
		StringBuilder signature = new StringBuilder();
		for (List<DataList> currList : tempLists) {
			if (currList == null || currList.isEmpty()) continue;
			signature.append(currList.size()).append(':');
			for (DataList data : currList) {
				if (data == null) continue;
				signature.append(data.getList()).append('|').append(data.getName()).append(';');
			}
			signature.append('#');
		}
		return signature.toString();
	}

	private String resolveListTitle(List<DataList> currList) {
		for (DataList data : currList) {
			if (data != null && data.getList() != null && !data.getList().isBlank()) {
				return data.getList();
			}
		}
		return "List";
	}

	private void ensureSectionCapacity(int size) {
		while (titles.size() <= size) {
			titles.add(buildLabel(""));
			lists.add(new ArrayList<JTextField>());
		}
	}

	private void ensureFieldCapacity(ArrayList<JTextField> fields, int size) {
		while (fields.size() < size) {
			JTextField field = buildTextField("");
			field.setEditable(false);
			fields.add(field);
		}
	}

	private void hideUnusedFields(ArrayList<JTextField> fields, int usedCount) {
		for (int i = usedCount; i < fields.size(); i++) {
			fields.get(i).setVisible(false);
			fields.get(i).setToolTipText(null);
		}
	}

	private void hideUnusedSections(int usedSections) {
		for (int i = usedSections; i < titles.size(); i++) {
			titles.get(i).setVisible(false);
			hideUnusedFields(lists.get(i), 0);
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

	


	

	
}

