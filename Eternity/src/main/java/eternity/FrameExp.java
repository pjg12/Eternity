package eternity;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

public class FrameExp extends JFrame {
	private static final long serialVersionUID = 1L;

	private final FrameSheet sheetFrame;
	private final DataQuery dataQuery;
	private CharData character;
	private FrameLevel levelFrame;

	private boolean skillLevel, specLevel;

	private final JComboBox<String> skillList;
	private final JComboBox<String> skillAttributes;
	private final JComboBox<String> specialsList;
	private final JComboBox<String> specialsType;

	private final JLabel headerL;
	private final JLabel subHeaderL;
	private final JLabel subHeader2L;
	private final JLabel[] labels;
	private final JFormattedTextField[] numFields;
	private final JButton[] buttons;

	private final String[] ATTRIBUTES = {"STR","DEX","CON","FOC","CAP","CTL","KNOW","MECH","PERC","INT","CHA","SUB"};
	private final String[] SPECTYPES = {"Proficiency","Martial","Class"};

	FrameExp (FrameSheet sheetFrame, DataQuery dataQuery) {
		super("Exp Up");
		this.sheetFrame = sheetFrame;
		this.dataQuery = dataQuery;

		setLayout(null);
		setSize(500, 340);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		headerL = new JLabel("", SwingConstants.CENTER);
		headerL.setBounds(30, 16, 440, 24);
		add(headerL);

		subHeaderL = new JLabel("", SwingConstants.CENTER);
		subHeaderL.setBounds(30, 44, 440, 20);
		add(subHeaderL);

		subHeader2L = new JLabel("", SwingConstants.CENTER);
		subHeader2L.setBounds(30, 66, 440, 20);
		add(subHeader2L);

		labels = new JLabel[4];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel("", SwingConstants.LEFT);
			add(labels[i]);
		}

		numFields = new JFormattedTextField[2];
		NumberFormatter nf = new NumberFormatter(java.text.NumberFormat.getIntegerInstance());
		nf.setAllowsInvalid(false);
		nf.setMinimum(0);
		numFields[0] = new JFormattedTextField(nf);
		numFields[0].setHorizontalAlignment(JTextField.CENTER);
		add(numFields[0]);
		numFields[1] = new JFormattedTextField(nf); // unused placeholder
		add(numFields[1]);

		buttons = new JButton[2];
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton("");
			add(buttons[i]);
		}

		skillList = new JComboBox<>();
		add(skillList);
		skillAttributes = new JComboBox<>(ATTRIBUTES);
		add(skillAttributes);
		
		specialsList = new JComboBox<>();
		add(specialsList);
		specialsType = new JComboBox<>(SPECTYPES);
		add(specialsType);

		clearLevel();
	}
	
	/*
	 * Sets the character to be modified
	 */
	public void updateCharacter(CharData character) {
		this.character = character;
	} //End of updateCharacter
	
	/*
	 * Sets Add Exp Window
	 */
	public void addXp() {
		clearLevel();
		
		/*
		 * Set Headers
		*/
		headerL.setText("Enter Experience Value");
		headerL.setVisible(true);
		
		/*
		 * Set General Buttons
		 */
		buttons[0].setText("Cancel");
		buttons[0].addActionListener(e -> this.setVisible(false));
		buttons[0].setBounds(60, 280, 120, 20);
		buttons[0].setVisible(true);
		buttons[1].setText("Accept");
		buttons[1].addActionListener(e -> expPressed());
		buttons[1].setBounds(320, 280, 120, 20);
		buttons[1].setVisible(true);
		
		/*
		 * Set General Label
		 */
		labels[0].setText("Experience");
		labels[0].setHorizontalAlignment(SwingConstants.CENTER);
		labels[0].setBounds(200, 160, 150, 20); // centered above the experience field
		labels[0].setVisible(true);
		
		/*
		 * Set Exp number field
		 */
		numFields[0].setValue(0);
		numFields[0].setBounds(225, 200, 100, 20);
		numFields[0].setVisible(true);
	} //End of addXp
	
	/*
	 * Sets Add Exp Window
	 */
	public void clearLevel() {
		// remove lingering listeners to avoid stacking actions
		for (JButton b : buttons) {
			for (var al : b.getActionListeners()) b.removeActionListener(al);
			b.setVisible(false);
		}
		headerL.setText("");
		subHeaderL.setText("");
		subHeader2L.setText("");
		headerL.setVisible(false);
		subHeaderL.setVisible(false);
		subHeader2L.setVisible(false);

		for (JLabel l : labels) {
			l.setText("");
			l.setVisible(false);
		}
		for (JFormattedTextField f : numFields) {
			f.setValue(0);
			f.setVisible(false);
		}

		skillList.setVisible(false);
		skillAttributes.setVisible(false);
		specialsList.setVisible(false);
		specialsType.setVisible(false);
	}
	
	public void expPressed() {
		if (character == null || character.getIdentity() == null) {
			this.setVisible(false);
			return;
		}

		CharIdentity id = character.getIdentity();
		double gain = ((Number)numFields[0].getValue()).doubleValue();
		double currentExp = id.getExp();
		int startLevel = id.getLevel();

		// simulate leveling to show accurate preview
		double tempExp = currentExp + gain;
		int tempLevel = startLevel;
		double req = nextExpRequirement(tempLevel);
		while (tempExp >= req) {
			tempExp -= req;
			tempLevel++;
			req = nextExpRequirement(tempLevel);
		}

		boolean leveled = tempLevel > startLevel;
		String message = leveled
				? String.format("You are about to gain %.0f experience.\nThis will raise your level from %d to %d.\nProceed?", gain, startLevel, tempLevel)
				: String.format("You are about to gain %.0f experience.\nYou will remain level %d.\nProceed?", gain, startLevel);

		int choice = JOptionPane.showConfirmDialog(
				this,
				message,
				"Confirm Experience Gain",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		// commit changes using the simulated results
		id.setLevel(tempLevel);
		id.setExp((float)tempExp);
		character.updateAll();

		if (sheetFrame != null) {
			sheetFrame.loadCharacter(character);
			sheetFrame.refreshMainPanel();
		}

		if (leveled) {
			if (levelFrame == null) {
				levelFrame = new FrameLevel(sheetFrame, dataQuery);
				levelFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			}
			levelFrame.updateCharacter(character);
			levelFrame.setSkipLevelIncrement(true);
			levelFrame.setLevelContext(startLevel + 1);
			levelFrame.levelUp();
			levelFrame.setVisible(true);
		}

		this.dispose();
	}
	
	public void levelUp() {
		clearLevel();
		
		/*
		 * Set Headers
		*/
		headerL.setText("You have leveled up!");
		headerL.setVisible(true);
		
		subHeaderL.setText("Numeric values have increased immediately.");
		subHeaderL.setVisible(true);
		
		subHeader2L.setText("Increase your class training rank for class abilities.");
		subHeader2L.setVisible(true);
		
		/*
		 * Set General Buttons
		 */
		buttons[1].setText("Accept");
		buttons[1].addActionListener(e -> levelUp2());
		buttons[1].setBounds(320, 280, 120, 20);
		buttons[1].setVisible(true);
	}
	
	public void levelUp2() {
		clearLevel();
		
		/*
		 * Set Headers
		*/
		int currentLevel = character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0;
		headerL.setText("Welcome to level " + (currentLevel + 1));
		headerL.setVisible(true);
		
		if (currentLevel % 3 == 0 || currentLevel == 19) {
			subHeader2L.setText("You have gained a new feature.");
			subHeader2L.setVisible(true);
			labels[2].setBounds(60, 240, 120, 20);
			labels[2].setText("Type");
			labels[2].setVisible(true);
			specialsType.setVisible(true);
			specialsType.setBounds(60, 265, 120, 20);
			labels[3].setBounds(210, 240, 280, 20);
			labels[3].setText("Specialty");
			labels[3].setVisible(true);
			specialsList.setVisible(true);
			specialsList.setBounds(210, 265, 280, 20);
			specLevel = true;
		}
		if (currentLevel % 3 != 0 || currentLevel == 19) {
			subHeaderL.setText("You have gained a new skill.");
			subHeaderL.setVisible(true);
			labels[0].setBounds(60, 115, 120, 20);	
			labels[0].setText("Attribute");
			labels[0].setVisible(true);
			skillAttributes.setVisible(true);
			skillAttributes.setBounds(60, 140, 120, 20);
			
			labels[1].setBounds(210, 115, 280, 20);
			labels[1].setText("Skill");
			labels[1].setVisible(true);
			skillList.setVisible(true);
			skillList.setBounds(210, 140, 280, 20);
			skillLevel = true;
		}
		
		/*
		 * Set General Buttons
		 */
		buttons[1].setText("Accept");
		buttons[1].addActionListener(e -> levelUpCon());
		buttons[1].setBounds(320, 280, 120, 20);
		buttons[1].setVisible(true);

		updateData();
		
		skillAttributes.addActionListener(e -> updateData());
		specialsType.addActionListener(e -> updateData());
	}
	
	void updateData() {
		skillList.removeAllItems();
		String tempString = (String)skillAttributes.getSelectedItem();
		ArrayList<DataSkill> tempList = new ArrayList<>(dataQuery.getSkillsByAttribute(tempString));
		for (DataSkill dataSkill : tempList) {
			skillList.addItem(dataSkill.getName());
		}
		specialsList.removeAllItems();
		ArrayList<DataSpecialty> tempSpecs = null;
		tempString = (String)specialsType.getSelectedItem();
		tempSpecs = new ArrayList<>(dataQuery.getSpecialtiesByType(tempString));
		for (DataSpecialty spec : tempSpecs) {
			specialsList.addItem(spec.getName());
		}		
	}

	public void levelUpCon() {
		if (skillLevel) {
			CharSpecials specials = character.getSpecials();
			DataSkill picked = dataQuery.getSkillByName((String)skillList.getSelectedItem());
			if (picked != null) {
				DataSkill copy = new DataSkill(picked);
				copy.addChosenAttribute((String)skillAttributes.getSelectedItem());
				specials.addSkill(copy);
			}
		}
		if (specLevel) {
			DataSpecialty picked = dataQuery.getSpecialtyByName((String)specialsList.getSelectedItem());
			if (picked != null) {
				character.getSpecials().addTrainedSpecialty(new DataSpecialty(picked));
			}
		}
		CharIdentity id = character.getIdentity();
		float newExp = (float)(id.getExp() - nextExpRequirement(id.getLevel()));
		id.setExp(Math.max(0f, newExp));
		id.setLevel(id.getLevel() + 1);
		character.updateAll();
		
		if (sheetFrame != null) {
			sheetFrame.loadCharacter(character);
			sheetFrame.refreshMainPanel(); // ensure main stats refresh after level-up
		}
		this.dispose();
	}

	/** Simple placeholder: next level requires (level + 1) * 100 XP. */
	private int nextExpRequirement(int level) {
		if (level <= 0) return Integer.MAX_VALUE;
		return level * 1000;
	}
	
	
	

}
