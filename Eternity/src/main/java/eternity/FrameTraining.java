package eternity;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;



public class FrameTraining extends JFrame {
    public static final String CARD_NEW = "new";
    public static final String CARD_EXISTING = "existing";
    private static final String FILTER_ALL = "All";
    private static final String NO_TECHNIQUES = "No techniques available";

	// References
    private final FrameSheet sheetFrame;
    private final StoreRuleManager ruleManager;
    private StoreCharData character;

	// UI Constants
	private static final int FRAME_WIDTH = 560;
    private static final int FRAME_HEIGHT = 380;
	private static final int BORDER_SPACING = 10;
	private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
	private static final Font SUBHEADER_FONT = new Font(null, Font.BOLD, 17);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
	private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder SUBHEADER_BORDER = new EmptyBorder(2, 18, 4, 18);
    private static final EmptyBorder LEFT_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 10, 2, 10);
	private static final Insets GB_INSETS = new Insets(2, 10, 2, 10);
	private static final int[] GB_COLUMN_WIDTHS = new int[] { 120, 120, 120, 120 };

	// UI Strings
    private static final String WINDOW_TITLE = "Technique Training";
	private static final String HEADER_TEXT = "Technique Training";
	private static final String NEW_HEADER_TEXT = "New Technique";
	private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_CONFIRM = "Confirm";
	private static final String AURA_TYPE = "Affinity";
    private static final String AURA_CATEGORY = "Tech Type";
    private static final String AURA_TECHNIQUE = "Aura Technique";
	private static final String MAX_RANK = "Max Rank:";
	private static final String CUR_RANK = "Current Rank:";
	private static final String CUR_XP = "Current XP:";
	private static final String NEXT_XP = "Next Rank At:";
	private final String[] TRAINING = {"Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Darkness", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};
	
	// UI Elements
    private JPanel headerPanel, centerHolderPanel, footerPanel;
    private JPanel newCenterPanel, existingCenterPanel;
    private CardLayout centerCardLayout;
	private JLabel headerL;
    private String activeCard = CARD_NEW;
    private TrainingCardControls newControls;
    private TrainingCardControls existingControls;
    private final List<DataTraining> newTechniques = new ArrayList<>();
    private final List<DataTraining> existingTechniques = new ArrayList<>();

	public boolean warn, isNew;
	public JRadioButton self, source, teacher;
	public ButtonGroup sourceGroup;
	public JCheckBox useTimeCheck;
	private JButton confirmButton;
    private JButton cancelButton;
    private JButton swapLayoutButton;

	// ---------------------------------------------------
    // Constructor
    // ---------------------------------------------------
    public FrameTraining(FrameSheet sheetFrame, StoreRuleManager ruleManager, StoreCharData character) {
		super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        this.ruleManager = ruleManager;
        this.character = character;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout(BORDER_SPACING, BORDER_SPACING));

        buildUI();
	}

	// ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        buildHeader();
        buildCenterPanel();
        buildFooter();
    }

	private void buildHeader() {
        // Build panel
        headerPanel = new JPanel();

        // Build header
        headerL = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setBorder(HEADER_BORDER);

        // Add elements
        headerPanel.add(headerL);
        add(headerPanel, BorderLayout.NORTH);
    }

	private void buildCenterPanel() {
        centerCardLayout = new CardLayout();
        centerHolderPanel = new JPanel(centerCardLayout);
        newControls = new TrainingCardControls();
        existingControls = new TrainingCardControls();
        newCenterPanel = buildTrainingCard(NEW_HEADER_TEXT, newControls, false);
        existingCenterPanel = buildTrainingCard("Existing Technique", existingControls, true);
        centerHolderPanel.add(newCenterPanel, CARD_NEW);
        centerHolderPanel.add(existingCenterPanel, CARD_EXISTING);
        add(centerHolderPanel, BorderLayout.CENTER);
        showCard(CARD_NEW);
    }

    private JPanel buildTrainingCard(String subHeaderText, TrainingCardControls controls, boolean existingCard) {
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = GB_COLUMN_WIDTHS;
        JPanel panel = new JPanel(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = GB_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Setup Variables
        int tileIndex = 0;
        int y = 0;
        int x = 0;
        int width = 4;
		gridHelper(gbc, y, x, width);

		JLabel sHeaderL = new JLabel(subHeaderText, SwingConstants.CENTER);
        sHeaderL.setFont(SUBHEADER_FONT);
        sHeaderL.setBorder(SUBHEADER_BORDER);
		panel.add(sHeaderL, gbc);

		y++;
		width = 1;
		gridHelper(gbc, y, x, width);
		JLabel typeL = buildLabel(AURA_TYPE);
        panel.add(typeL, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		JLabel catL = buildLabel(AURA_CATEGORY);
        panel.add(catL, gbc);

		x++;
		width++;
		gridHelper(gbc, y, x, width);
		JLabel techL = buildLabel(AURA_TECHNIQUE);
        panel.add(techL, gbc);

		y++;
		x = 0;
		width = 1;
		gridHelper(gbc, y, x, width);
		controls.affinityBox = buildComboBox();
		panel.add(controls.affinityBox, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.typeBox = buildComboBox();
		panel.add(controls.typeBox, gbc);

		x++;
		width = 2;
		gridHelper(gbc, y, x, width);
		controls.techniqueBox = buildComboBox();
		panel.add(controls.techniqueBox, gbc);

		y++;
		x = 0;
		width = 1;
		gridHelper(gbc, y, x, width);
		JLabel maxRankL = buildLabel(MAX_RANK);
		panel.add(maxRankL, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		JLabel curRankL = buildLabel(CUR_RANK);
		panel.add(curRankL, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		JLabel curXpL = buildLabel(CUR_XP);
		panel.add(curXpL, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		JLabel nextXpL = buildLabel(NEXT_XP);
		panel.add(nextXpL, gbc);

		y++;
		x = 0;
		gridHelper(gbc, y, x, width);
		controls.maxRankField = buildNumField();
		panel.add(controls.maxRankField, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.curRankField = buildNumField();
		panel.add(controls.curRankField, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.curXpField = buildValueField();
		panel.add(controls.curXpField, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.nextXpField = buildNumField();
		panel.add(controls.nextXpField, gbc);

		y++;
		x = 0;
		gridHelper(gbc, y, x, width);
		JLabel genXpL = buildLabel("Aura XP:");
		panel.add(genXpL, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		JLabel specXpL = buildLabel("Typed XP:");
		panel.add(specXpL, gbc);

		x+=2;
		gridHelper(gbc, y, x, width);
		JLabel useXpL = buildLabel("XP to Use:");
		panel.add(useXpL, gbc);

		y++;
		x = 0;
		gridHelper(gbc, y, x, width);
		controls.auraXpField = buildValueField();
		panel.add(controls.auraXpField, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.typedXpField = buildValueField();
		panel.add(controls.typedXpField, gbc);

		x+=2;
		gridHelper(gbc, y, x, width);
		controls.useXpField = buildValueField();
		controls.useXpField.setEditable(true);
		panel.add(controls.useXpField, gbc);

		y++;
		x = 0;
		width = 4;
		gridHelper(gbc, y, x, width);
		/* error field */
		/* panel.add(controls.techniqueBox, gbc); */

        if (existingCard) {
            controls.affinityBox.addActionListener(e -> populateTechniqueBox(existingControls, existingTechniques));
            controls.typeBox.addActionListener(e -> populateTechniqueBox(existingControls, existingTechniques));
            controls.techniqueBox.addActionListener(e -> updateTechniqueDetails(existingControls, existingTechniques, false));
        } else {
            controls.affinityBox.addActionListener(e -> populateTechniqueBox(newControls, newTechniques));
            controls.typeBox.addActionListener(e -> populateTechniqueBox(newControls, newTechniques));
            controls.techniqueBox.addActionListener(e -> updateTechniqueDetails(newControls, newTechniques, true));
        }
        return panel;
    }

	private void buildFooter() {
        // Build panel
        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(FOOTER_BORDER);

        // Build cancel button
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        JPanel pan = new JPanel();
        pan.add(cancelButton);
        footerPanel.add(pan, BorderLayout.WEST);

        swapLayoutButton = new JButton("Existing");
        swapLayoutButton.addActionListener(e -> onSwapLayoutPressed());
        pan = new JPanel();
        pan.add(swapLayoutButton);
        footerPanel.add(pan, BorderLayout.CENTER);

        // Build confirm button
        confirmButton = new JButton(BUTTON_CONFIRM);
        confirmButton.addActionListener(e -> onConfirmPressed());
        pan = new JPanel();
        pan.add(confirmButton);
        footerPanel.add(pan, BorderLayout.EAST);

        // Add panels
        add(footerPanel, BorderLayout.SOUTH);
        updateFooterButtons();
    }

	private JLabel buildLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

	private JComboBox<String> buildComboBox() {
        // Add empty choice
        JComboBox<String> box = new JComboBox<>();
        return box;
    }

	private JFormattedTextField buildNumField() {
        JFormattedTextField numF = new JFormattedTextField(NumberFormat.getIntegerInstance());
        numF.setFont(LABEL_FONT);
        numF.setHorizontalAlignment(SwingConstants.CENTER);
        numF.setAlignmentX(Component.CENTER_ALIGNMENT);
        numF.setEditable(false);
        return numF;
    }

    private JFormattedTextField buildValueField() {
        JFormattedTextField numF = new JFormattedTextField(NumberFormat.getNumberInstance());
        numF.setFont(LABEL_FONT);
        numF.setHorizontalAlignment(SwingConstants.CENTER);
        numF.setAlignmentX(Component.CENTER_ALIGNMENT);
        numF.setEditable(false);
        return numF;
    }

	// ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------

    private void onCancelPressed() {
        dispose();
    }

    public void onConfirmPressed() {
        if (CARD_NEW.equals(activeCard)) {
            confirmNewTechnique();
        } else if (CARD_EXISTING.equals(activeCard)) {
            confirmExistingTechnique();
        }
    }

    private void onSwapLayoutPressed() {
        if (CARD_EXISTING.equals(activeCard)) showCard(CARD_NEW);
        else showCard(CARD_EXISTING);
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
        if (CARD_EXISTING.equals(activeCard)) refreshExistingTechniqueChoices();
        else refreshNewTechniqueChoices();
    }

    public void showCard(String cardName) {
        if (!CARD_EXISTING.equals(cardName)) {
            activeCard = CARD_NEW;
        } else {
            activeCard = CARD_EXISTING;
        }
        if (centerCardLayout != null && centerHolderPanel != null) {
            centerCardLayout.show(centerHolderPanel, activeCard);
        }
        if (CARD_EXISTING.equals(activeCard)) refreshExistingTechniqueChoices();
        else refreshNewTechniqueChoices();
        updateFooterButtons();
    }

    public String getActiveCard() {
        return activeCard;
    }

    private void updateFooterButtons() {
        if (swapLayoutButton == null) return;
        if (CARD_EXISTING.equals(activeCard)) swapLayoutButton.setText("New");
        else swapLayoutButton.setText("Existing");
    }

    private void refreshExistingTechniqueChoices() {
        if (existingControls == null) return;

        existingTechniques.clear();
        if (character != null && character.getTraining() != null) {
            for (DataTraining tech : character.getTraining().getAllTraining()) {
                if (tech == null) continue;
                existingTechniques.add(tech);
            }
        }
        populateFilterBox(existingControls.affinityBox, existingTechniques, true);
        populateFilterBox(existingControls.typeBox, existingTechniques, false);
        populateTechniqueBox(existingControls, existingTechniques);
    }

    private void refreshNewTechniqueChoices() {
        if (newControls == null) return;

        newTechniques.clear();
        if (character != null && character.getTraining() != null && ruleManager != null) {
            for (DataTraining tech : ruleManager.getTrainingData()) {
                if (tech == null) continue;
                if (isDeprecatedTraining(tech)) continue;
                if (character.getTraining().getTrainingById(tech.getId()) != null) continue;
                if (tech.getMaxRank(character) < 1) continue;
                newTechniques.add(new DataTraining(tech));
            }
        }
        populateFilterBox(newControls.affinityBox, newTechniques, true);
        populateFilterBox(newControls.typeBox, newTechniques, false);
        populateTechniqueBox(newControls, newTechniques);
    }

    private void populateFilterBox(JComboBox<String> box, List<DataTraining> techniques, boolean affinityFilter) {
        if (box == null) return;
        Object selected = box.getSelectedItem();
        box.removeAllItems();
        box.addItem(FILTER_ALL);

        Set<String> values = new LinkedHashSet<>();
        for (DataTraining tech : techniques) {
            String value = affinityFilter ? tech.getAffinity() : tech.getType();
            value = safeLabel(value);
            if (!value.isEmpty()) values.add(value);
        }
        if (affinityFilter) {
            for (String canonical : TRAINING) {
                if (containsIgnoreCase(values, canonical)) {
                    box.addItem(canonical);
                }
            }
            for (String value : values) {
                if (!containsComboItem(box, value)) {
                    box.addItem(value);
                }
            }
        } else {
            for (String value : values) {
                box.addItem(value);
            }
        }

        if (selected != null && containsComboItem(box, selected.toString())) {
            box.setSelectedItem(selected);
        } else {
            box.setSelectedItem(FILTER_ALL);
        }
    }

    private void populateTechniqueBox(TrainingCardControls controls, List<DataTraining> techniques) {
        if (controls == null || controls.techniqueBox == null) return;
        JComboBox<String> affinityBox = controls.affinityBox;
        JComboBox<String> typeBox = controls.typeBox;
        JComboBox<String> techniqueBox = controls.techniqueBox;
        String selectedAffinity = affinityBox == null || affinityBox.getSelectedItem() == null
                ? FILTER_ALL
                : affinityBox.getSelectedItem().toString();
        String selectedType = typeBox == null || typeBox.getSelectedItem() == null
                ? FILTER_ALL
                : typeBox.getSelectedItem().toString();

        techniqueBox.removeAllItems();
        for (DataTraining tech : techniques) {
            if (!matchesFilter(selectedAffinity, tech.getAffinity())) continue;
            if (!matchesFilter(selectedType, tech.getType())) continue;
            techniqueBox.addItem(tech.getName());
        }

        if (techniqueBox.getItemCount() == 0) {
            techniqueBox.addItem(NO_TECHNIQUES);
            clearTechniqueDetails(controls);
        } else {
            techniqueBox.setSelectedIndex(0);
        }
        updateTechniqueDetails(controls, techniques, controls == newControls);
    }

    private void updateTechniqueDetails(TrainingCardControls controls, List<DataTraining> techniques, boolean newView) {
        if (controls == null || controls.techniqueBox == null) return;
        Object selected = controls.techniqueBox.getSelectedItem();
        if (selected == null || NO_TECHNIQUES.equals(selected.toString())) {
            clearTechniqueDetails(controls);
            return;
        }

        DataTraining tech = findTechniqueByName(techniques, selected.toString());
        if (tech == null) {
            clearTechniqueDetails(controls);
            return;
        }

        DataTraining displayTech = tech;
        if (newView) {
            displayTech = new DataTraining(tech);
            displayTech.setRank(0);
            displayTech.setExp(0.0);
            displayTech.setAl(0);
        }

        controls.maxRankField.setValue(displayTech.getMaxRank(character));
        controls.curRankField.setValue(displayTech.getRank());
        controls.curXpField.setValue(displayTech.getExp());
        controls.nextXpField.setValue(displayTech.getNextAt(character));
        updateTrainingXpFields(controls, displayTech);
    }

    private DataTraining findTechniqueByName(List<DataTraining> techniques, String name) {
        for (DataTraining tech : techniques) {
            if (tech == null || tech.getName() == null) continue;
            if (tech.getName().equalsIgnoreCase(name)) return tech;
        }
        return null;
    }

    private void clearTechniqueDetails(TrainingCardControls controls) {
        if (controls == null) return;
        if (controls.maxRankField != null) controls.maxRankField.setValue(0);
        if (controls.curRankField != null) controls.curRankField.setValue(0);
        if (controls.curXpField != null) controls.curXpField.setValue(0.0);
        if (controls.nextXpField != null) controls.nextXpField.setValue(0);
        if (controls.auraXpField != null) controls.auraXpField.setValue(getCharacterTrainingXp());
        if (controls.typedXpField != null) controls.typedXpField.setValue(0.0);
    }

    private void updateTrainingXpFields(TrainingCardControls controls, DataTraining tech) {
        if (controls == null) return;
        if (controls.auraXpField != null) {
            controls.auraXpField.setValue(getCharacterTrainingXp());
        }
        if (controls.typedXpField != null) {
            controls.typedXpField.setValue(getTypedTrainingXp(tech == null ? null : tech.getAffinity()));
        }
    }

    private double getCharacterTrainingXp() {
        if (character == null || character.getTraining() == null) return 0.0;
        return character.getTraining().getTrainingXp();
    }

    private double getTypedTrainingXp(String affinity) {
        if (character == null || character.getTraining() == null || affinity == null || affinity.isBlank()) return 0.0;
        int index = findAuraTypeIndex(affinity);
        if (index < 0) return 0.0;
        return character.getTraining().getTrainingXpByAuraType(index);
    }

    private int findAuraTypeIndex(String affinity) {
        for (int i = 1; i < FrameTrainingExp.AURA_TYPES.length; i++) {
            String auraType = FrameTrainingExp.AURA_TYPES[i];
            if (auraType != null && auraType.equalsIgnoreCase(affinity)) {
                return i - 1;
            }
        }
        return -1;
    }

    private boolean matchesFilter(String selectedFilter, String value) {
        if (selectedFilter == null || FILTER_ALL.equalsIgnoreCase(selectedFilter)) return true;
        return selectedFilter.equalsIgnoreCase(safeLabel(value));
    }

    private boolean containsComboItem(JComboBox<String> box, String value) {
        for (int i = 0; i < box.getItemCount(); i++) {
            String item = box.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private boolean containsIgnoreCase(Set<String> values, String target) {
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    private String safeLabel(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isDeprecatedTraining(DataTraining tech) {
        if (tech == null) return false;
        if (tech.getId() == 23) return true;
        String name = tech.getName();
        return name != null && name.equalsIgnoreCase("Class Training");
    }

    private void confirmNewTechnique() {
        if (character == null || character.getTraining() == null || newControls == null || newControls.techniqueBox == null) return;

        Object selected = newControls.techniqueBox.getSelectedItem();
        if (selected == null || NO_TECHNIQUES.equals(selected.toString())) return;

        DataTraining template = findTechniqueByName(newTechniques, selected.toString());
        if (template == null) return;
        if (character.getTraining().getTrainingById(template.getId()) != null) return;

        DataTraining added = new DataTraining(template);
        added.setRank(0);
        added.setExp(0.0);
        added.setAl(0);

        double expToUse = parseNumericField(newControls.useXpField);
        double availableAuraXp = getCharacterTrainingXp();
        double availableTypedXp = getTypedTrainingXp(template.getAffinity());
        double totalAvailableXp = availableAuraXp + availableTypedXp;
        if (expToUse > totalAvailableXp) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Insufficient XP is available for that amount.",
                    "Insufficient XP",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[] {"Use available XP", "Cancel Training"},
                    "Use available XP");
            if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {
                dispose();
                return;
            }
            expToUse = totalAvailableXp;
        }

        double spendableXp = Math.min(expToUse, getMaxSpendableXp(added));
        if (newControls.useXpField != null) newControls.useXpField.setValue(spendableXp);
        consumeTrainingXp(template.getAffinity(), availableAuraXp, availableTypedXp, spendableXp);

        added.setExp(spendableXp);
        applyTrainingExpProgression(added);

        character.getTraining().addTraining(added);
        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshTrainingPanel();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }

        refreshExistingTechniqueChoices();
        refreshNewTechniqueChoices();
        if (newControls.useXpField != null) newControls.useXpField.setValue(0.0);
    }

    private void confirmExistingTechnique() {
        if (character == null || character.getTraining() == null || existingControls == null || existingControls.techniqueBox == null) return;

        Object selected = existingControls.techniqueBox.getSelectedItem();
        if (selected == null || NO_TECHNIQUES.equals(selected.toString())) return;

        DataTraining existing = findTechniqueByName(existingTechniques, selected.toString());
        if (existing == null) return;

        double expToUse = parseNumericField(existingControls.useXpField);
        double availableAuraXp = getCharacterTrainingXp();
        double availableTypedXp = getTypedTrainingXp(existing.getAffinity());
        double totalAvailableXp = availableAuraXp + availableTypedXp;
        if (expToUse > totalAvailableXp) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Insufficient XP is available for that amount.",
                    "Insufficient XP",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[] {"Use available XP", "Cancel Training"},
                    "Use available XP");
            if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {
                dispose();
                return;
            }
            expToUse = totalAvailableXp;
        }

        double spendableXp = Math.min(expToUse, getMaxSpendableXp(existing));
        if (existingControls.useXpField != null) existingControls.useXpField.setValue(spendableXp);
        consumeTrainingXp(existing.getAffinity(), availableAuraXp, availableTypedXp, spendableXp);

        existing.setExp(existing.getExp() + spendableXp);
        applyTrainingExpProgression(existing);

        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshTrainingPanel();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }

        refreshExistingTechniqueChoices();
        refreshNewTechniqueChoices();
        if (existingControls.useXpField != null) existingControls.useXpField.setValue(0.0);
    }

    private void applyTrainingExpProgression(DataTraining tech) {
        if (tech == null || character == null) return;
        while (tech.getRank() < tech.getMaxRank(character) && tech.getExp() >= tech.getNextAt(character)) {
            tech.setExp(tech.getExp() - tech.getNextAt(character));
            tech.setRank(tech.getRank() + 1);
        }
        if (tech.getRank() >= tech.getMaxRank(character)) {
            tech.setExp(0.0);
        }
    }

    private double getMaxSpendableXp(DataTraining tech) {
        if (tech == null || character == null) return 0.0;
        DataTraining preview = new DataTraining(tech);
        double spendable = 0.0;
        while (preview.getRank() < preview.getMaxRank(character)) {
            int nextAt = preview.getNextAt(character);
            spendable += Math.max(0, nextAt - Math.max(0.0, preview.getExp()));
            preview.setRank(preview.getRank() + 1);
            preview.setExp(0.0);
        }
        return spendable;
    }

    private double parseNumericField(JFormattedTextField field) {
        if (field == null || field.getValue() == null) return 0.0;
        Object value = field.getValue();
        if (value instanceof Number number) {
            return Math.max(0.0, number.doubleValue());
        }
        try {
            return Math.max(0.0, Double.parseDouble(value.toString()));
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private void consumeTrainingXp(String affinity, double availableAuraXp, double availableTypedXp, double amountToConsume) {
        if (character == null || character.getTraining() == null || amountToConsume <= 0.0) return;
        CharTraining training = character.getTraining();

        double remaining = amountToConsume;
        double typedUsed = Math.min(availableTypedXp, remaining);
        remaining -= typedUsed;
        double auraUsed = Math.min(availableAuraXp, remaining);

        int typedIndex = findAuraTypeIndex(affinity);
        if (typedIndex >= 0) {
            training.setTrainingXpByAuraType(typedIndex, Math.max(0.0, availableTypedXp - typedUsed));
        }
        training.setTrainingXp(Math.max(0.0, availableAuraXp - auraUsed));
    }

    private static final class TrainingCardControls {
        private JComboBox<String> affinityBox;
        private JComboBox<String> typeBox;
        private JComboBox<String> techniqueBox;
        private JFormattedTextField maxRankField;
        private JFormattedTextField curRankField;
        private JFormattedTextField curXpField;
        private JFormattedTextField nextXpField;
        private JFormattedTextField auraXpField;
        private JFormattedTextField typedXpField;
        private JFormattedTextField useXpField;
    }

	private void gridHelper (GridBagConstraints gbc, int y, int x, int width) {
        gbc.gridwidth = width;
        gbc.gridy = y;
        gbc.gridx = x;
    }


}


/**
 * Lightweight replacement for the legacy FrameHelper-based training dialogs.
 * It compiles against the current data model (StoreCharData, CharTraining, StoreRuleManager).
 */

/*	private static final long serialVersionUID = 1L;


	

	protected final JLabel headerL = new JLabel("", SwingConstants.CENTER);
	protected final JLabel[] labels = new JLabel[14];
	protected final JFormattedTextField[] numFields = new JFormattedTextField[6];
	protected final JButton[] buttons = new JButton[5];
	private final SimpleDocListener trainingHoursDocListener = new SimpleDocListener(this::updateTrainXp);

	public final String[] AURATYPES = {"Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Force", "Sound", "Light", "Darkness", "Poison", "Psionic", "Energy", "Spirit", "Time", "Deviant"};

	FrameTraining(FrameSheet sheetFrame, StoreRuleManager dataQuery) {


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

		
	}

	/*
	 * UPDATE CHARACTER
	 */
	/*public void updateCharacter(StoreCharData character) {
		this.character = character;
	}

	/*
	 * MATCH NATURAL AFFINITY
	 */
	/*public void matchAffinity() {
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
	/*public void updateTrainXp() {
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
	/*protected boolean confirmPartialProgress(double hours, double expGain, double currentExp, double nextAt) {
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
	/*protected boolean confirmLevelUpProgress(DataTraining tech, double hours, double expGain, double currentExp, double nextAt, int currentRank, int maxRank) {
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
	/*protected void maybeGrantSkillFromTraining(DataTraining tech, int oldRank, int newRank) {
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
	/*protected void maybeGrantSpecialtyFromTraining(DataTraining tech, int oldRank, int newRank) {
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
}*/

