package eternity;

import java.sql.Timestamp;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

public class FrameLevel extends JFrame {
	private static final long serialVersionUID = 1L;

private final FrameSheet sheetFrame;
private final DataQuery dataQuery;
private CharData character;
private boolean skipLevelIncrement = false;
private Integer levelContext = null; // optional override for display/logic
private Integer subclassReminderLevel = null;

	private final FrameSkill frameSkill;
	private final FrameSpecial frameSpecial;

	private final JLabel headerL;
	private final JLabel subHeaderL;
	private final JLabel subHeader2L;
	private final JLabel[] labels;
	private final JFormattedTextField[] numFields;
	private final JButton[] buttons;

	FrameLevel (FrameSheet sheetFrame, DataQuery dataQuery) {
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
		NumberFormatter nf = createNullableIntegerFormatter();
		numFields[0] = new JFormattedTextField(nf);
		numFields[0].setFocusLostBehavior(JFormattedTextField.PERSIST);
		numFields[0].setHorizontalAlignment(JTextField.CENTER);
		add(numFields[0]);
		numFields[1] = new JFormattedTextField(nf); // unused placeholder
		numFields[1].setFocusLostBehavior(JFormattedTextField.PERSIST);
		add(numFields[1]);

		buttons = new JButton[2];
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton("");
			add(buttons[i]);
		}

		frameSkill = new FrameSkill(dataQuery);
		frameSkill.attachToFrame(this);
		frameSpecial = new FrameSpecial(dataQuery);
		frameSpecial.attachToFrame(this);

		clearLevel();
	}
	
	/*
	 * Sets the character to be modified
	 */
	public void updateCharacter(CharData character) {
		this.character = character;
	} //End of updateCharacter
	
	/** Sets the level context used for display/logic; null to use character's current level. */
	public void setLevelContext(Integer levelContext) {
		this.levelContext = levelContext;
	}
	
	/** If true, levelUpCon will not change level/exp (used when level already applied). */
	public void setSkipLevelIncrement(boolean skip) {
		this.skipLevelIncrement = skip;
	}
	
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
		// reset state flags for a fresh session
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

		frameSkill.clear();
		frameSpecial.clear();
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
		int currentLevel = levelContext != null ? levelContext :
				(character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0);

		// Warn about subclass lock-in at level 5
		if (currentLevel == 5 && !java.util.Objects.equals(subclassReminderLevel, currentLevel)
				&& character != null && character.getIdentity() != null) {
			String subclass = character.getIdentity().getCharSubclass();
			String cls = character.getIdentity().getCharClass();
			String msg = "Upon reaching level 5 you will not be able to change subclass.\n"
					+ "Current class: " + (cls == null ? "?" : cls) + "\n"
					+ "Current subclass: " + (subclass == null ? "?" : subclass) + "\n\n"
					+ "Ensure the correct subclass is selected before proceeding.";
			javax.swing.JOptionPane.showMessageDialog(this, msg, "Subclass Reminder", javax.swing.JOptionPane.WARNING_MESSAGE);
			subclassReminderLevel = currentLevel;
		}

		headerL.setText("Welcome to level " + currentLevel);
		headerL.setVisible(true);
		
		if (currentLevel % 3 == 0 || currentLevel == 19) {
			subHeader2L.setText("You have gained a new feature.");
			subHeader2L.setVisible(true);
			frameSpecial.showSpecialtySelection(labels[2], labels[3]);
		}
		if (currentLevel % 3 != 0 || currentLevel == 19) {
			subHeaderL.setText("You have gained a new skill.");
			subHeaderL.setVisible(true);
			frameSkill.showSkillSelection(labels[0], labels[1]);
		}
		
		/*
		 * Set General Buttons
		 */
		buttons[1].setText("Accept");
		buttons[1].addActionListener(e -> levelUpCon());
		buttons[1].setBounds(320, 280, 120, 20);
		buttons[1].setVisible(true);
	}

	public void levelUpCon() {
		int contextLevel = levelContext != null ? levelContext :
				(character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0);
		boolean skipIncrementThisPass = skipLevelIncrement;

		frameSkill.applySelection(character);
		frameSpecial.applySelection(character);
		// Record level-up timing
		CharIdentity id = character.getIdentity();
		long now = System.currentTimeMillis();
		id.setLastLevelUp(new Timestamp(now));
		// Track in-game elapsed at the moment of level up so we can diff later
		id.setTimeSinceLastLevel(id.getCampaignElapsedTime());

		if (!skipIncrementThisPass) {
			float newExp = (float)(id.getExp() - nextExpRequirement(id.getLevel()));
			id.setExp(Math.max(0f, newExp));
			id.setLevel(id.getLevel() + 1);
		}
		character.updateAll();
		
		if (sheetFrame != null) {
			sheetFrame.refreshMainPanel();
			sheetFrame.refreshImagePanel();
			sheetFrame.refreshTrainingPanel();
		}

		int currentLevel = character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0;
		if (contextLevel < currentLevel) {
			skipLevelIncrement = skipIncrementThisPass;
			levelContext = contextLevel + 1;
			levelUp();
			setVisible(true);
			return;
		}

		// always reset skip flag after processing to avoid leaking across sessions
		skipLevelIncrement = false;
		levelContext = null;
		subclassReminderLevel = null;
		this.dispose();
	}

	/** Simple placeholder: next level requires (level + 1) * 100 XP. */
	private int nextExpRequirement(int level) {
		if (level <= 0) return Integer.MAX_VALUE;
		return level * 1000;
	}

	private NumberFormatter createNullableIntegerFormatter() {
		NumberFormatter nf = new NumberFormatter(java.text.NumberFormat.getIntegerInstance()) {
			private static final long serialVersionUID = 1L;
			@Override
			public Object stringToValue(String text) throws ParseException {
				if (text == null || text.trim().isEmpty()) return null;
				return super.stringToValue(text);
			}
			@Override
			public String valueToString(Object value) throws ParseException {
				if (value == null) return "";
				return super.valueToString(value);
			}
		};
		nf.setAllowsInvalid(true);
		nf.setCommitsOnValidEdit(true);
		nf.setMinimum(0);
		return nf;
	}
	
	
	

}
