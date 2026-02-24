package eternity;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class PanelCharNotes extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
private ArrayList<JLabel> titles ;
private ArrayList<ArrayList<JTextField>> lists;
private javax.swing.JButton saveButton;
private javax.swing.JTextArea notesArea;
private javax.swing.JScrollPane notesScroll;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharNotes (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		
		titles = new ArrayList<JLabel>();
		lists = new ArrayList<ArrayList<JTextField>>();

		saveButton = buildButton("Save Notes");
        saveButton.addActionListener(e -> saveNotes());
		notesArea = new javax.swing.JTextArea();
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesScroll = new javax.swing.JScrollPane(notesArea);
		notesScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(notesScroll);
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
		if (character != null && character.getIdentity() != null) {
			notesArea.setText(character.getIdentity().getNotes());
		} else {
			notesArea.setText("");
		}
		resizeSheet();
	}
	public void updateList() {
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
		// Intentional no-content state for Notes tab
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

		// place save button below inherited header space
		saveButton.setBounds(5, pageHeight, 120, 22);
		saveButton.setVisible(true);
		for (var al : saveButton.getActionListeners()) {
			saveButton.removeActionListener(al);
		}
		saveButton.addActionListener(e -> saveNotes());
        pageHeight += 25;
		
		// Notes area takes majority of remaining space
		notesScroll.setBounds(5, pageHeight, 553, 555);
		notesScroll.setVisible(true);
		pageHeight += 555;
		
		this.setPreferredSize(new Dimension(580, pageHeight));
	}

	private void saveNotes() {
		if (character != null && character.getIdentity() != null) {
			character.getIdentity().setNotes(notesArea.getText());
			CharacterDataManager.saveCharacter(character); // full character save
			if (sheetFrame != null) {
				sheetFrame.loadCharacter(character);
				sheetFrame.refreshMainPanel();
			}
		}
	}
}
