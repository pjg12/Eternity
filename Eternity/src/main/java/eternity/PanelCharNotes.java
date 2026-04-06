package eternity;

import java.awt.Dimension;

public class PanelCharNotes extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
private javax.swing.JButton saveButton;
private javax.swing.JTextArea notesArea;
private javax.swing.JScrollPane notesScroll;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharNotes (DataQuery dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);

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
		if (character != null && character.getIdentity() != null) {
			notesArea.setText(character.getIdentity().getNotes());
		} else {
			notesArea.setText("");
		}
		resizeSheet();
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

		// place save button below inherited header space
		saveButton.setBounds(5, pageHeight, 120, 22);
		saveButton.setVisible(true);
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
			CharDataManager.saveCharacter(character); // full character save
			if (sheetFrame != null) {
				sheetFrame.refreshMainPanel();
				sheetFrame.refreshImagePanel();
			}
		}
	}
}
