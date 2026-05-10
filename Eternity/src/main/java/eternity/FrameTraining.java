package eternity;

import java.awt.Color;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

/**
 * Lightweight replacement for the legacy FrameHelper-based training dialogs.
 * It compiles against the current data model (CharData, CharTraining, DataQuery).
 */
public class FrameTraining extends JFrame {
	private static final long serialVersionUID = 1L;

	protected final FrameSheet sheetFrame;
	protected final DataQuery dataQuery;
	protected CharData character;

	public JComboBox<String> auraType, auraTech;
	public boolean warn, isNew;
	public JRadioButton self, source, teacher;
	public ButtonGroup sourceGroup;
	public JCheckBox useTimeCheck;

	protected final JLabel headerL = new JLabel("", SwingConstants.CENTER);
	protected final JLabel[] labels = new JLabel[14];
	protected final JFormattedTextField[] numFields = new JFormattedTextField[6];
	protected final JButton[] buttons = new JButton[5];
	private final SimpleDocListener trainingHoursDocListener = new SimpleDocListener(this::updateTrainXp);

	public final String[] AURATYPES = {"Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Force", "Sound", "Light", "Darkness", "Poison", "Psionic", "Energy", "Spirit", "Time", "Deviant"};

	FrameTraining(FrameSheet sheetFrame, DataQuery dataQuery) {
		super("Training");
		this.sheetFrame = sheetFrame;
		this.dataQuery = dataQuery;
		setLayout(null);
		setSize(560, 380);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		add(headerL);
		headerL.setBounds(40, 20, 480, 24);

		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel("", SwingConstants.CENTER);
			add(labels[i]);
		}

		NumberFormatter nf = new NumberFormatter(NumberFormat.getNumberInstance());
		nf = createNullableDoubleFormatter();
		for (int i = 0; i < numFields.length; i++) {
			numFields[i] = new JFormattedTextField(nf);
			numFields[i].setFocusLostBehavior(JFormattedTextField.PERSIST);
			numFields[i].setHorizontalAlignment(JFormattedTextField.CENTER);
			add(numFields[i]);
		}
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton("");
			add(buttons[i]);
		}

		auraType = new JComboBox<>(AURATYPES);
		add(auraType);
		auraTech = new JComboBox<>();
		add(auraTech);
		useTimeCheck = new JCheckBox("Time", true);
		add(useTimeCheck);

		setupBaseLayout();
	}

	private void setupBaseLayout() {
		headerL.setText("Select Technique to Train");
		headerL.setVisible(true);

		buttons[0].setText("Cancel");
		buttons[0].addActionListener(e -> trainCancel());
		buttons[0].setBounds(80, 320, 120, 20);
		buttons[0].setVisible(true);

		buttons[1].setText("Accept");
		buttons[1].setBounds(350, 320, 120, 20);
		buttons[1].setVisible(true);

		labels[4].setText("Aura Type");
		labels[4].setBounds(25, 110, 150, 20);
		labels[4].setVisible(true);

		labels[5].setText("Technique");
		labels[5].setBounds(200, 110, 300, 20);
		labels[5].setVisible(true);

		auraType.setBounds(25, 140, 150, 20);
		auraType.setVisible(true);

		auraTech.setBounds(200, 140, 300, 20);
		auraTech.setVisible(true);

		labels[6].setText("Max Rank");
		labels[6].setBounds(25, 170, 100, 20);
		labels[6].setVisible(true);

		labels[7].setText("Current Rank");
		labels[7].setBounds(150, 170, 100, 20);
		labels[7].setVisible(true);

		labels[8].setText("Current Exp");
		labels[8].setBounds(275, 170, 100, 20);
		labels[8].setVisible(true);

		labels[9].setText("Rank Up At");
		labels[9].setBounds(400, 170, 100, 20);
		labels[9].setVisible(true);

		numFields[0].setBounds(25, 200, 100, 20);
		numFields[1].setBounds(150, 200, 100, 20);
		numFields[2].setBounds(275, 200, 100, 20);
		numFields[3].setBounds(400, 200, 100, 20);
		for (int i = 0; i <= 3; i++) {
			numFields[i].setEditable(false);
			numFields[i].setVisible(true);
		}

		labels[10].setText("<html><center>Training Length<br>(hrs)");
		labels[10].setBounds(25, 225, 120, 40);
		labels[10].setVisible(true);

		numFields[4].setBounds(25, 285, 120, 20);
		numFields[4].setVisible(true);
		numFields[4].getDocument().addDocumentListener(trainingHoursDocListener);

		// buttons[2] unused; hide legacy arrow control
		buttons[2].setVisible(false);

		// button[4]: swap between New and Existing dialogs (wired in subclasses)
		buttons[4].setBounds(240, 320, 100, 20);
		buttons[4].setVisible(true);

		labels[11].setText("<html><center>Training<br>Exp");
		labels[11].setBounds(245, 230, 80, 40);
		labels[11].setVisible(true);

		numFields[5].setBounds(245, 285, 80, 20);
		numFields[5].setVisible(true);
		numFields[5].setEditable(false);

		labels[12].setText("At Max:");
		labels[12].setForeground(Color.RED);
		labels[12].setBounds(305, 230, 235, 20);
		labels[12].setVisible(false);

		labels[13].setText("Error");
		labels[13].setForeground(Color.RED);
		labels[13].setBounds(305, 250, 235, 70);
		labels[13].setVisible(false);

		self = new JRadioButton("Self");
		source = new JRadioButton("Source");
		teacher = new JRadioButton("Teacher");
		add(self); add(source); add(teacher);
		self.setBounds(160, 220, 100, 20);
		source.setBounds(160, 240, 100, 20);
		teacher.setBounds(160, 260, 100, 20);
		self.setVisible(true); source.setVisible(true); teacher.setVisible(true);

		sourceGroup = new ButtonGroup();
		sourceGroup.add(self); sourceGroup.add(source); sourceGroup.add(teacher);
		self.setSelected(true);

		wireTrainXpTrigger(self);
		wireTrainXpTrigger(source);
		wireTrainXpTrigger(teacher);

		useTimeCheck.setBounds(25, 255, 80, 20);
		useTimeCheck.setVisible(true);
		useTimeCheck.addActionListener(e -> refreshTimeModeUI());
		refreshTimeModeUI();
	}

	/*
	 * UPDATE CHARACTER
	 */
	public void updateCharacter(CharData character) {
		this.character = character;
	}

	/*
	 * MATCH NATURAL AFFINITY
	 */
	public void matchAffinity() {
		if (character == null || character.getTraining() == null) return;
		List<String> affinities = character.getTraining().getNaturalAffinities();
		labels[0].setText(affinities.size() > 1 ? "Natural Affinities" : "Natural Affinity");

		for (int i = 0; i < affinities.size() && i + 1 < labels.length; i++) {
			String aff = affinities.get(i);
			labels[i + 1].setText(aff);
			labels[i + 1].setVisible(true);
			labels[i + 1].setOpaque(true);
			DataColor color = dataQuery.getColorByTitle(aff);
			if (color != null) {
				labels[i + 1].setBackground(color.getBackColor());
				labels[i + 1].setForeground(color.getForeColor());
			}
			int tempInt = affinities.size();
			int tempInt2 = 200;
			int tempInt3 = 0;
			if (tempInt >= 3) {
				tempInt2 = 550 - 5 * (tempInt + 1);
				tempInt2 /= tempInt;
			} else {
				tempInt3 = (550 - tempInt * 200) / 2;
			}
			labels[i + 1].setBounds(5 + (5 + tempInt2) * i + tempInt3, 75, tempInt2, 20);
		}
	}

	/*
	 * UPDATE TRAINING EXPERIENCE
	 */
	public void updateTrainXp() {
		if (useTimeCheck != null && !useTimeCheck.isSelected()) {
			return; // manual EXP entry mode
		}
		warn = false;
		double tempDub = 0;
		Double hrs = parseTrainingHours();
		if (hrs != null) {
			if (self.isSelected()) tempDub = 2 * hrs;
			else if (source.isSelected()) tempDub = 3 * hrs;
			else if (teacher.isSelected()) tempDub = 4 * hrs;
		}
		numFields[5].setValue(tempDub);
	}

	protected boolean shouldAdvanceTime() {
		return useTimeCheck != null && useTimeCheck.isSelected();
	}

	private void refreshTimeModeUI() {
		boolean useTime = shouldAdvanceTime();
		labels[10].setVisible(useTime);
		numFields[4].setVisible(useTime);
		numFields[5].setEditable(!useTime);
		if (useTime) {
			updateTrainXp();
		}
	}

	private void wireTrainXpTrigger(AbstractButton button) {
		button.addActionListener(e -> updateTrainXp());
	}

	/*private void attachTrainingHoursListeners() {
		if (numFields[4] == null) return;
		if (trainingHoursDocument != null) {
			trainingHoursDocument.removeDocumentListener(trainingHoursDocListener);
		}
		numFields[4].removePropertyChangeListener("value", trainingHoursValueListener);
		trainingHoursDocument = numFields[4].getDocument();
		if (trainingHoursDocument != null) {
			trainingHoursDocument.addDocumentListener(trainingHoursDocListener);
		}
		numFields[4].addPropertyChangeListener("value", trainingHoursValueListener);
	}*/

	/**
	 * Confirms with the user when the added XP will not reach the next rank.
	 * Returns true if the user wants to proceed, false to cancel.
	 */
	protected boolean confirmPartialProgress(double hours, double expGain, double currentExp, double nextAt) {
		if (expGain <= 0) return false;
		if (currentExp + expGain >= nextAt) return true; // will level; no prompt

		double remaining = Math.max(0, nextAt - currentExp - expGain);
		String message = String.format(
				"<html>You are about to train for %.2f hours.<br>"
				+ "Training EXP gained: %.2f<br>"
				+ "EXP still required to rank up: %.2f<br><br>"
				+ "Apply this training?</html>",
				hours, expGain, remaining);
		int choice = JOptionPane.showConfirmDialog(this, message, "Confirm Training", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		return choice == JOptionPane.YES_OPTION;
	}

	/**
	 * Confirms with the user when the added XP will level the technique.
	 * Returns true if the user wants to proceed, false to cancel.
	 */
	protected boolean confirmLevelUpProgress(DataTraining tech, double hours, double expGain, double currentExp, double nextAt, int currentRank, int maxRank) {
		if (expGain <= 0) return false;
		double overflow = Math.max(0, currentExp + expGain);
		int newRank = currentRank;
		boolean capped = newRank >= maxRank;
		boolean squish = false;

		double rate = 2.0;
		if (source.isSelected()) rate = 3.0;
		else if (teacher.isSelected()) rate = 4.0;

		// Check for multiple level-ups
		//double tempExp = overflow;
		while (overflow > 0) {
			DataTraining preview = tech == null ? new DataTraining() : new DataTraining(tech);
			preview.setRank(newRank);
			preview.setExp(0.0);
			double nextThreshold = preview.getNextAt(character);
			if (overflow >= nextThreshold) {
				newRank++;
				overflow -= nextThreshold;
				if (newRank >= maxRank) {
					capped = true;
					break;
				}
			} else {
				break;
			}
		}

		while (overflow >= rate) {
			squish = true;
			overflow -= rate;
			hours -= 1.0;
			expGain -= rate;
		}

		String message = String.format(
				"<html>You are about to reach Rank %d%s.<br>"
				+ "%s"
				+ "Time spent: %.2f hours<br>"
				+ "Training EXP gained: %.2f<br>"
				+ "EXP applied toward next rank: %.2f<br><br>"
				+ "Apply this training?</html>",
				newRank, capped ? " (max)" : "", squish ? "<i>Due to overlevel limits, time has been reduced.</i><br>" : "", hours, expGain, overflow);
		int choice = JOptionPane.showConfirmDialog(this, message, "Confirm Rank Up", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		return choice == JOptionPane.YES_OPTION;
	}

	public void trainCancel() {
		setVisible(false);
	}

	// Simple doc listener adapter
	private static class SimpleDocListener implements javax.swing.event.DocumentListener {
		private final Runnable r;
		SimpleDocListener(Runnable r) { this.r = r; }
		public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
		public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
		public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
	}

		public void setTrainingFieldsVisible(boolean visible) {
		// Rank / XP boxes
		for (int i = 6; i <= 11; i++) {
			if (labels[i] != null) labels[i].setVisible(visible);
		}
		for (int i = 0; i <= 5; i++) {
			if (numFields[i] != null) numFields[i].setVisible(visible);
		}
		// Divider arrow (deprecated) remains hidden
		if (buttons[2] != null) buttons[2].setVisible(false);
		// Training source radios
		if (self != null) self.setVisible(visible);
		if (source != null) source.setVisible(visible);
		if (teacher != null) teacher.setVisible(visible);
		if (useTimeCheck != null) useTimeCheck.setVisible(visible);
		// Accept/Cancel buttons stay visible; swap button remains as-is
		if (buttons[0] != null) buttons[0].setVisible(true);
		if (buttons[1] != null) buttons[1].setVisible(true);
		if (buttons[4] != null) buttons[4].setVisible(true);
		// Clear warning labels when hiding
		if (!visible) {
			labels[12].setVisible(false);
			labels[13].setVisible(false);
		}
		if (visible) {
			refreshTimeModeUI();
		}
	}

	public void advanceCampaignTime(double hours) {
		if (character == null || character.getIdentity() == null) return;
		if (hours <= 0) return;
		long minutes = Math.round(hours * 60.0);
		if (minutes <= 0) return;
		character.getIdentity().addCampaignTime(java.time.Duration.ofMinutes(minutes));
	}

	/**
	 * When Skill Training gains a rank, prompt the user to add a new skill.
	 */
	protected void maybeGrantSkillFromTraining(DataTraining tech, int oldRank, int newRank) {
		if (tech == null || character == null || character.getSpecials() == null || dataQuery == null) return;
		if (newRank <= oldRank) return;
		String name = tech.getName() != null ? tech.getName().toLowerCase() : "";
		boolean isSkillTraining = name.contains("skill training") || "Skill".equalsIgnoreCase(tech.getAffinity());
		if (!isSkillTraining) return;
		FrameSkill.promptForTrainingSkill(this, dataQuery, character);
	}

	/**
	 * When Specialty/Feature Training gains a rank, prompt the user to add a new specialty.
	 */
	protected void maybeGrantSpecialtyFromTraining(DataTraining tech, int oldRank, int newRank) {
		if (tech == null || character == null || character.getSpecials() == null || dataQuery == null) return;
		if (newRank <= oldRank) return;
		String name = tech.getName() != null ? tech.getName().toLowerCase() : "";
		boolean isSpecialtyTraining = name.contains("specialty training")
				|| name.contains("feature training")
				|| "Specialty".equalsIgnoreCase(tech.getAffinity())
				|| "Feature".equalsIgnoreCase(tech.getAffinity());
		if (!isSpecialtyTraining) return;
		FrameSpecial.promptForTrainingSpecialty(this, dataQuery, character);
	}

	private NumberFormatter createNullableDoubleFormatter() {
		NumberFormatter nf = new NumberFormatter(NumberFormat.getNumberInstance()) {
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
		nf.setMinimum(0.0);
		return nf;
	}

	protected Double parseTrainingHours() {
		Object value = numFields[4].getValue();
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		String text = numFields[4].getText();
		if (text == null || text.isBlank()) {
			return null;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException ignore) {
			return null;
		}
	}
}
