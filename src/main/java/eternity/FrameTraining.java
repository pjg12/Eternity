package eternity;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final String RACE_TRAINING_NAME = "Race Training";
    private static final String ALTERI_RACIAL_SPECIALTY = "Shapeshifting (Alteri)";
    private static final String ALTERI_SHAPESHIFT_LIST = "Shapeshift";
    private static final String MOLDS_LIST = "Molds";
    private static final String MOLD_CATEGORY_WEAPON = "Weapon";
    private static final String MOLD_CATEGORY_ARMOR = "Armor";
    private static final String MOLD_CATEGORY_ITEM = "Item";
    private static final String SKILL_TRAINING_NAME = "Skill Training";
    private static final String SPECIALTY_TRAINING_NAME = "Specialty Training";
    private static final Set<String> EXCLUDED_TECH_CATEGORIES = Set.of("attribute", "misc", "affinity", "fundamental", "standard", "crafting");

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
    private static final String BUTTON_CONFIRM = "Rank Up";
	private static final String AURA_TYPE = "Affinity";
    private static final String AURA_CATEGORY = "Tech Type";
	private static final String AURA_TECHNIQUE = "Aura Technique";
	private static final String MAX_RANK = "Max Rank:";
	private static final String CUR_RANK = "Current Rank:";
	private static final String NEXT_XP = "Next Rank Cost:";
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

    private record MoldEntrySelection(String entryName, String description) {}

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
		width = 2;
		gridHelper(gbc, y, x, width);
		JLabel nextXpL = buildLabel(NEXT_XP);
		panel.add(nextXpL, gbc);

		y++;
		x = 0;
		width = 1;
		gridHelper(gbc, y, x, width);
		controls.maxRankField = buildNumField();
		panel.add(controls.maxRankField, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.curRankField = buildNumField();
		panel.add(controls.curRankField, gbc);

		x++;
		width = 2;
		gridHelper(gbc, y, x, width);
		controls.nextXpField = buildNumField();
		panel.add(controls.nextXpField, gbc);

		y++;
		x = 0;
		width = 1;
		gridHelper(gbc, y, x, width);
		JLabel genXpL = buildLabel("Aura XP:");
		panel.add(genXpL, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		JLabel specXpL = buildLabel("Typed XP:");
		panel.add(specXpL, gbc);

		y++;
		x = 0;
		width = 1;
		gridHelper(gbc, y, x, width);
		controls.auraXpField = buildValueField();
		panel.add(controls.auraXpField, gbc);

		x++;
		gridHelper(gbc, y, x, width);
		controls.typedXpField = buildValueField();
		panel.add(controls.typedXpField, gbc);

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
        String selectedAffinity = getSelectedComboValue(existingControls == null ? null : existingControls.affinityBox);
        String selectedType = getSelectedComboValue(existingControls == null ? null : existingControls.typeBox);
        String selectedTechnique = getSelectedComboValue(existingControls == null ? null : existingControls.techniqueBox);
        if (existingControls == null) return;

        existingTechniques.clear();
        if (character != null && character.getTraining() != null) {
            for (DataTraining tech : character.getTraining().getAllTraining()) {
                if (tech == null) continue;
                if (canAdvanceExistingTechnique(tech)) {
                    existingTechniques.add(tech);
                }
                String listName = resolveAssociatedListName(tech);
                if (!listName.isBlank() && canAdvanceExistingListTechnique(tech, listName)) {
                    existingTechniques.add(buildListTrainingEntry(tech, listName));
                }
            }
        }
        existingTechniques.sort(Comparator
                .comparing((DataTraining t) -> safeLabel(t == null ? null : t.getAffinity()), String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt((DataTraining t) -> isAuraAffinityTech(t) ? 0 : 1)
                .thenComparingInt((DataTraining t) -> getTypeOrder(t == null ? null : t.getType()))
                .thenComparingInt(t -> t != null && t.isListEntry() ? 1 : 0)
                .thenComparing(t -> safeLabel(t == null ? null : t.getName()), String.CASE_INSENSITIVE_ORDER));
        populateFilterBox(existingControls.affinityBox, existingTechniques, true);
        populateFilterBox(existingControls.typeBox, existingTechniques, false);
        restoreSelection(existingControls, existingTechniques, selectedAffinity, selectedType, selectedTechnique);
    }

    private boolean canAdvanceExistingTechnique(DataTraining tech) {
        if (tech == null || character == null) return false;
        return tech.getRank() < tech.getMaxRank(character);
    }

    private boolean canAdvanceExistingListTechnique(DataTraining tech, String listName) {
        if (tech == null || listName == null || listName.isBlank()) return false;
        DataTraining listEntry = buildListTrainingEntry(tech, listName);
        return countListMembers(listName) < resolveListMaxMembers(listEntry);
    }

    private void refreshNewTechniqueChoices() {
        String selectedAffinity = getSelectedComboValue(newControls == null ? null : newControls.affinityBox);
        String selectedType = getSelectedComboValue(newControls == null ? null : newControls.typeBox);
        String selectedTechnique = getSelectedComboValue(newControls == null ? null : newControls.techniqueBox);
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
        restoreSelection(newControls, newTechniques, selectedAffinity, selectedType, selectedTechnique);
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

    private String getSelectedComboValue(JComboBox<String> box) {
        if (box == null || box.getSelectedItem() == null) return null;
        String value = box.getSelectedItem().toString();
        return value == null || value.isBlank() ? null : value;
    }

    private void restoreSelection(TrainingCardControls controls, List<DataTraining> techniques,
            String affinity, String type, String techniqueName) {
        if (controls == null) return;
        if (affinity != null && containsComboItem(controls.affinityBox, affinity)) {
            controls.affinityBox.setSelectedItem(affinity);
        }
        if (type != null && containsComboItem(controls.typeBox, type)) {
            controls.typeBox.setSelectedItem(type);
        }
        populateTechniqueBox(controls, techniques);
        if (techniqueName != null && containsComboItem(controls.techniqueBox, techniqueName)) {
            controls.techniqueBox.setSelectedItem(techniqueName);
            updateTechniqueDetails(controls, techniques, controls == newControls);
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
            displayTech.setAl(0);
        }
        if (!newView && tech.isListEntry()) {
            controls.maxRankField.setValue(resolveListMaxMembers(tech));
            controls.curRankField.setValue(countListMembers(tech.getListName()));
            controls.nextXpField.setValue(10);
        } else {
            controls.maxRankField.setValue(displayTech.getMaxRank(character));
            controls.curRankField.setValue(displayTech.getRank());
            controls.nextXpField.setValue(displayTech.getNextAt(character));
        }
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
        added.setAl(0);

        int xpCost = added.getNextAt(character);
        double availableAuraXp = getCharacterTrainingXp();
        double availableTypedXp = getTypedTrainingXp(template.getAffinity());
        double totalAvailableXp = availableAuraXp + availableTypedXp;
        if (totalAvailableXp + 0.0001 < xpCost) {
            JOptionPane.showMessageDialog(
                    this,
                    "Insufficient XP is available to reach the next rank.",
                    "Insufficient XP",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean createInitialMoldsList = shouldCreateInitialMoldsList(added);
        MoldEntrySelection firstMoldEntry = null;
        if (createInitialMoldsList) {
            firstMoldEntry = promptForMoldEntrySelection(List.of(), 1, 1);
            if (firstMoldEntry == null) return;
        }

        DataTraining preview = new DataTraining(added);
        int oldRank = preview.getRank();
        preview.setRank(oldRank + 1);
        if (wouldExceedMaxTechsWithNewTechnique(preview)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Learning that technique would make Current Techs greater than Max Techs.",
                    "Max Techs Exceeded",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<DataSkill> grantedSkills = promptForSkillsFromTrainingRankGain(preview, oldRank, preview.getRank());
        if (grantedSkills == null) return;
        List<FrameSpecial.SpecialtyGrant> grantedSpecialties = promptForSpecialtiesFromTrainingRankGain(preview, oldRank, preview.getRank(), grantedSkills);
        if (grantedSpecialties == null) return;

        consumeTrainingXp(template.getAffinity(), availableAuraXp, availableTypedXp, xpCost);
        added.setRank(preview.getRank());

        character.getTraining().addTraining(added);
        grantTrainingSkills(grantedSkills);
        grantTrainingSpecialties(grantedSpecialties);
        if (createInitialMoldsList) {
            ensureListWithFirstEntry(MOLDS_LIST, firstMoldEntry.entryName(), firstMoldEntry.description());
        }
        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshTrainingPanel();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }

        boolean atMaxRank = added.getRank() >= added.getMaxRank(character);
        refreshNewTechniqueChoices();
        refreshExistingTechniqueChoices();
        if (!atMaxRank) {
            showCard(CARD_EXISTING);
            restoreSelection(existingControls, existingTechniques, added.getAffinity(), added.getType(), added.getName());
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    added.getName() + " has reached its maximum rank.",
                    "Maximum Rank Reached",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean wouldExceedMaxTechsWithNewTechnique(DataTraining preview) {
        if (preview == null || character == null || character.getTraining() == null) return false;
        double projectedCurrent = calculateWeightedCurrentTechs(character.getTraining()) + calculateWeightedTechContribution(preview);
        return projectedCurrent > getMaxTechCapacity() + 0.0001;
    }

    private int getMaxTechCapacity() {
        if (character == null || character.getIdentity() == null || ruleManager == null) return 0;
        int maxTechs = 0;
        DataLevel levelData = ruleManager.getLevel(character.getIdentity().getLevel());
        if (levelData != null) {
            maxTechs = Math.max(0, levelData.getBaseTechs());
        }
        if (character.hasAuraProficiencySpecialty()) {
            maxTechs = (int) (maxTechs * character.getAuraProficiencyBonusMultiplier());
        }
        return maxTechs;
    }

    private double calculateWeightedCurrentTechs(CharTraining training) {
        if (training == null) return 0.0;
        Set<String> natural = normalizeAffinitySet(training.getNaturalAffinities());
        Set<String> domain = normalizeAffinitySet(training.getDomainAffinities());
        double total = 0.0;

        for (String category : training.getTrainingCategories()) {
            if (category == null) continue;
            if (EXCLUDED_TECH_CATEGORIES.contains(category.toLowerCase())) continue;

            for (DataTraining tech : training.getTrainingList(category)) {
                total += calculateWeightedTechContribution(category, tech, natural, domain);
            }
        }
        return total;
    }

    private double calculateWeightedTechContribution(DataTraining tech) {
        if (tech == null) return 0.0;
        Set<String> natural = normalizeAffinitySet(character != null && character.getTraining() != null
                ? character.getTraining().getNaturalAffinities()
                : List.of());
        Set<String> domain = normalizeAffinitySet(character != null && character.getTraining() != null
                ? character.getTraining().getDomainAffinities()
                : List.of());
        String category = safeLabel(tech.getAffinity());
        return calculateWeightedTechContribution(category, tech, natural, domain);
    }

    private double calculateWeightedTechContribution(String category, DataTraining tech, Set<String> natural, Set<String> domain) {
        if (tech == null) return 0.0;
        String normalizedCategory = category == null ? "" : category.toLowerCase();
        if (EXCLUDED_TECH_CATEGORIES.contains(normalizedCategory)) return 0.0;
        String normalizedAffinity = tech.getAffinity() == null ? "" : tech.getAffinity().toLowerCase();
        boolean naturalMatch = natural.contains(normalizedCategory) || natural.contains(normalizedAffinity);
        boolean domainMatch = domain.contains(normalizedCategory) || domain.contains(normalizedAffinity);
        boolean spiritOrTime = ("Spirit".equalsIgnoreCase(category) || "Time".equalsIgnoreCase(category))
                || ("Spirit".equalsIgnoreCase(tech.getAffinity()) || "Time".equalsIgnoreCase(tech.getAffinity()));
        double multiplier = 1.0;
        if (naturalMatch) multiplier *= 0.5;
        else if (domainMatch) multiplier *= 0.75;
        if (spiritOrTime) multiplier *= 1.5;
        return tech.getRank() * multiplier;
    }

    private Set<String> normalizeAffinitySet(Iterable<String> affinities) {
        Set<String> normalized = new LinkedHashSet<>();
        if (affinities == null) return normalized;
        for (String affinity : affinities) {
            if (affinity != null) {
                normalized.add(affinity.toLowerCase());
            }
        }
        return normalized;
    }

    private void confirmExistingTechnique() {
        if (character == null || character.getTraining() == null || existingControls == null || existingControls.techniqueBox == null) return;

        Object selected = existingControls.techniqueBox.getSelectedItem();
        if (selected == null || NO_TECHNIQUES.equals(selected.toString())) return;

        DataTraining existing = findTechniqueByName(existingTechniques, selected.toString());
        if (existing == null) return;
        if (existing.isListEntry()) {
            confirmExistingListTechnique(existing);
            return;
        }
        int xpCost = existing.getNextAt(character);
        double availableAuraXp = getCharacterTrainingXp();
        double availableTypedXp = getTypedTrainingXp(existing.getAffinity());
        double totalAvailableXp = availableAuraXp + availableTypedXp;
        if (totalAvailableXp + 0.0001 < xpCost) {
            JOptionPane.showMessageDialog(
                    this,
                    "Insufficient XP is available to reach the next rank.",
                    "Insufficient XP",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DataTraining preview = new DataTraining(existing);
        int oldRank = existing.getRank();
        preview.setRank(oldRank + 1);
        List<DataSkill> grantedSkills = promptForSkillsFromTrainingRankGain(preview, oldRank, preview.getRank());
        if (grantedSkills == null) return;
        List<FrameSpecial.SpecialtyGrant> grantedSpecialties = promptForSpecialtiesFromTrainingRankGain(preview, oldRank, preview.getRank(), grantedSkills);
        if (grantedSpecialties == null) return;

        consumeTrainingXp(existing.getAffinity(), availableAuraXp, availableTypedXp, xpCost);
        existing.setRank(preview.getRank());
        grantTrainingSkills(grantedSkills);
        grantTrainingSpecialties(grantedSpecialties);

        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshTrainingPanel();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }

        boolean atMaxRank = existing.getRank() >= existing.getMaxRank(character);
        refreshNewTechniqueChoices();
        refreshExistingTechniqueChoices();
        if (!atMaxRank) {
            restoreSelection(existingControls, existingTechniques, existing.getAffinity(), existing.getType(), existing.getName());
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    existing.getName() + " has reached its maximum rank.",
                    "Maximum Rank Reached",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void confirmExistingListTechnique(DataTraining listTech) {
        if (character == null || character.getTraining() == null || listTech == null || !listTech.isListEntry()) return;

        DataTraining parent = character.getTraining().getTrainingById(listTech.getListParentId());
        if (parent == null) return;

        int currentRank = countListMembers(listTech.getListName());
        int maxRank = resolveListMaxMembers(listTech);
        int remainingRanks = Math.max(0, maxRank - currentRank);
        if (remainingRanks <= 0) {
            JOptionPane.showMessageDialog(this, "No additional list entries can currently be learned.", "List Full", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double availableAuraXp = getCharacterTrainingXp();
        double availableTypedXp = getTypedTrainingXp(listTech.getAffinity());
        double totalAvailableXp = availableAuraXp + availableTypedXp;
        if (remainingRanks <= 0 || totalAvailableXp + 0.0001 < 10.0) {
            JOptionPane.showMessageDialog(this, "List training requires 10 XP per new list entry.", "Insufficient XP", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MoldEntrySelection chosenEntry = MOLDS_LIST.equalsIgnoreCase(safeLabel(listTech.getListName()))
                ? promptForMoldEntrySelection(List.of(), 1, 1)
                : null;
        if (MOLDS_LIST.equalsIgnoreCase(safeLabel(listTech.getListName()))) {
            if (chosenEntry == null) {
                return;
            }
        } else {
            String genericEntry = promptForSingleListEntry(listTech.getListName(), List.of(), 1, 1);
            if (genericEntry == null) {
                return;
            }
            chosenEntry = new MoldEntrySelection(genericEntry, "");
        }

        consumeTrainingXp(listTech.getAffinity(), availableAuraXp, availableTypedXp, 10.0);
        addListEntry(listTech.getListName(), chosenEntry.entryName(), chosenEntry.description());

        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshTrainingPanel();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }

        refreshExistingTechniqueChoices();
        refreshNewTechniqueChoices();
        restoreSelection(existingControls, existingTechniques, listTech.getAffinity(), listTech.getType(), listTech.getName());
    }

    private List<DataSkill> promptForSkillsFromTrainingRankGain(DataTraining tech, int oldRank, int newRank) {
        if (!isSkillTraining(tech) || newRank <= oldRank) {
            return List.of();
        }
        return FrameSkill.promptForTrainingSkills(this, ruleManager, character, newRank - oldRank);
    }

    private boolean isSkillTraining(DataTraining tech) {
        if (tech == null || tech.getName() == null) return false;
        return SKILL_TRAINING_NAME.equalsIgnoreCase(tech.getName().trim());
    }

    private void grantTrainingSkills(List<DataSkill> grantedSkills) {
        if (grantedSkills == null || character == null || character.getSpecials() == null) return;
        for (DataSkill skill : grantedSkills) {
            if (skill != null) {
                character.getSpecials().addSkill(skill);
            }
        }
    }

    private List<FrameSpecial.SpecialtyGrant> promptForSpecialtiesFromTrainingRankGain(DataTraining tech, int oldRank, int newRank, List<DataSkill> reservedSkills) {
        if (!isSpecialtyTraining(tech) || newRank <= oldRank) {
            return List.of();
        }
        return FrameSpecial.promptForTrainingSpecialtyGrants(this, ruleManager, character, newRank - oldRank, null, reservedSkills);
    }

    private boolean isSpecialtyTraining(DataTraining tech) {
        if (tech == null || tech.getName() == null) return false;
        return SPECIALTY_TRAINING_NAME.equalsIgnoreCase(tech.getName().trim());
    }

    private void grantTrainingSpecialties(List<FrameSpecial.SpecialtyGrant> grantedSpecialties) {
        if (grantedSpecialties == null || character == null) return;
        for (FrameSpecial.SpecialtyGrant grant : grantedSpecialties) {
            if (grant == null || grant.specialty() == null) continue;
            FrameSpecial.applyResolvedSpecialtyGrant(character, grant.specialty(), grant.grantedSkills());
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

    private DataTraining buildListTrainingEntry(DataTraining parent, String listName) {
        DataTraining listEntry = new DataTraining(parent);
        listEntry.setName("New " + listName);
        listEntry.setListName(listName);
        listEntry.setListEntry(true);
        listEntry.setListParentId(parent.getId());
        listEntry.setRank(countListMembers(listName));
        listEntry.setAl(0);
        return listEntry;
    }

    private String resolveAssociatedListName(DataTraining tech) {
        if (tech == null) return "";
        if (isAlteriRaceTraining(tech)) return ALTERI_SHAPESHIFT_LIST;
        String listName = tech.getListName();
        return listName == null ? "" : listName.trim();
    }

    private boolean isAlteriRaceTraining(DataTraining tech) {
        if (tech == null || tech.getName() == null || !RACE_TRAINING_NAME.equalsIgnoreCase(tech.getName().trim())) return false;
        if (character == null || character.getSpecials() == null) return false;
        DataSpecialty racial = character.getSpecials().getRacialSpecialty();
        if (racial == null || racial.getName() == null) return false;
        return ALTERI_RACIAL_SPECIALTY.equalsIgnoreCase(racial.getName().trim());
    }

    private boolean shouldCreateInitialMoldsList(DataTraining tech) {
        return isMoldingTechnique(tech) && !hasListNamed(MOLDS_LIST);
    }

    private boolean isMoldingTechnique(DataTraining tech) {
        if (tech == null || tech.getName() == null) return false;
        return tech.getName().trim().toLowerCase().endsWith(" molding");
    }

    private int resolveListMaxMembers(DataTraining listTech) {
        if (listTech == null || character == null || character.getTraining() == null) return 0;
        DataTraining parent = character.getTraining().getTrainingById(listTech.getListParentId());
        if (parent == null) return 0;
        return Math.max(0, parent.getRank() * parent.getEffectiveListMaxPerRank() + parent.getEffectiveListMaxBase());
    }

    private int countListMembers(String listName) {
        if (character == null || character.getLists() == null || listName == null || listName.isBlank()) return 0;
        int count = 0;
        for (List<DataList> group : character.getLists()) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
                if (entry.getName().trim().isBlank()) continue;
                count++;
            }
        }
        return count;
    }

    private ArrayList<String> promptForListEntries(String listName, int count) {
        ArrayList<String> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String selected = promptForSingleListEntry(listName, entries, i + 1, count);
            if (selected == null) return null;
            entries.add(selected);
        }
        return entries;
    }

    private String promptForSingleListEntry(String listName, List<String> pendingEntries, int index, int total) {
        while (true) {
            String chosen = resolveListSelectionInput(listName, index, total);
            if (chosen == null) return null;
            String trimmed = chosen.trim();
            if (trimmed.isBlank()) {
                JOptionPane.showMessageDialog(this, "A list entry name is required.", "Missing Entry", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (isRestrictedShifterMoldEntry(listName, trimmed)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Shifter slot molds are granted automatically by Shifter level and cannot be trained.",
                        "Restricted Mold",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (hasListEntry(listName, trimmed) || containsIgnoreCase(pendingEntries, trimmed)) {
                JOptionPane.showMessageDialog(this, "That list entry already exists.", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return trimmed;
        }
    }

    private boolean isRestrictedShifterMoldEntry(String listName, String entryName) {
        if (!MOLDS_LIST.equalsIgnoreCase(safeLabel(listName))) return false;
        return StoreCharData.isShifterSpecialMoldName(entryName);
    }

    private MoldEntrySelection promptForMoldEntrySelection(List<String> pendingEntries, int index, int total) {
        ArrayList<String> weaponTypes = collectWeaponTypeOptions();
        ArrayList<String> armorTypes = collectArmorTypeOptions();
        ArrayList<String> armorSlots = collectArmorSlotOptions();
        if (weaponTypes.isEmpty() && armorTypes.isEmpty() && armorSlots.isEmpty()) {
            String itemName = JOptionPane.showInputDialog(this, "Enter new mold name:", "Select Molds", JOptionPane.PLAIN_MESSAGE);
            if (itemName == null) return null;
            String trimmed = itemName.trim();
            if (trimmed.isBlank()) {
                JOptionPane.showMessageDialog(this, "A mold name is required.", "Missing Entry", JOptionPane.WARNING_MESSAGE);
                return promptForMoldEntrySelection(pendingEntries, index, total);
            }
            return new MoldEntrySelection(trimmed, buildMoldEntryDescription(MOLD_CATEGORY_ITEM, "", ""));
        }

        while (true) {
            String title = total > 1 ? "Select Molds (" + index + " of " + total + ")" : "Select Molds";
            JComboBox<String> categoryBox = new JComboBox<>(new String[] { MOLD_CATEGORY_WEAPON, MOLD_CATEGORY_ARMOR, MOLD_CATEGORY_ITEM });
            JComboBox<String> weaponBox = new JComboBox<>(weaponTypes.toArray(new String[0]));
            JComboBox<String> armorTypeBox = new JComboBox<>(armorTypes.toArray(new String[0]));
            JComboBox<String> armorSlotBox = new JComboBox<>(armorSlots.toArray(new String[0]));
            javax.swing.JTextField itemField = new javax.swing.JTextField(16);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 6, 4, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Category"), gbc);
            gbc.gridx = 1;
            panel.add(categoryBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Weapon Type"), gbc);
            gbc.gridx = 1;
            panel.add(weaponBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Armor Type"), gbc);
            gbc.gridx = 1;
            panel.add(armorTypeBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Armor Slot"), gbc);
            gbc.gridx = 1;
            panel.add(armorSlotBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(new JLabel("Item Name"), gbc);
            gbc.gridx = 1;
            panel.add(itemField, gbc);

            Runnable syncFields = () -> {
                String category = categoryBox.getSelectedItem() == null ? MOLD_CATEGORY_WEAPON : categoryBox.getSelectedItem().toString();
                boolean weapon = MOLD_CATEGORY_WEAPON.equalsIgnoreCase(category);
                boolean armor = MOLD_CATEGORY_ARMOR.equalsIgnoreCase(category);
                boolean item = MOLD_CATEGORY_ITEM.equalsIgnoreCase(category);
                weaponBox.setEnabled(weapon);
                armorTypeBox.setEnabled(armor);
                armorSlotBox.setEnabled(armor);
                itemField.setEnabled(item);
            };
            syncFields.run();
            ActionListener listener = e -> syncFields.run();
            categoryBox.addActionListener(listener);

            int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            categoryBox.removeActionListener(listener);
            if (result != JOptionPane.OK_OPTION) return null;

            String category = categoryBox.getSelectedItem() == null ? "" : categoryBox.getSelectedItem().toString().trim();
            MoldEntrySelection selection = buildMoldEntrySelection(category, weaponBox, armorTypeBox, armorSlotBox, itemField);
            if (selection == null) {
                continue;
            }
            String trimmed = selection.entryName().trim();
            if (isRestrictedShifterMoldEntry(MOLDS_LIST, trimmed)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Shifter slot molds are granted automatically by Shifter level and cannot be trained.",
                        "Restricted Mold",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (hasListEntry(MOLDS_LIST, trimmed) || containsIgnoreCase(pendingEntries, trimmed)) {
                JOptionPane.showMessageDialog(this, "That mold entry already exists.", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return selection;
        }
    }

    private MoldEntrySelection buildMoldEntrySelection(String category, JComboBox<String> weaponBox,
            JComboBox<String> armorTypeBox, JComboBox<String> armorSlotBox, javax.swing.JTextField itemField) {
        if (MOLD_CATEGORY_WEAPON.equalsIgnoreCase(category)) {
            Object selected = weaponBox.getSelectedItem();
            String weaponType = selected == null ? "" : selected.toString().trim();
            if (weaponType.isBlank()) {
                JOptionPane.showMessageDialog(this, "Select a weapon type.", "Missing Weapon", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return new MoldEntrySelection(weaponType, buildMoldEntryDescription(MOLD_CATEGORY_WEAPON, weaponType, ""));
        }
        if (MOLD_CATEGORY_ARMOR.equalsIgnoreCase(category)) {
            String armorType = armorTypeBox.getSelectedItem() == null ? "" : armorTypeBox.getSelectedItem().toString().trim();
            String armorSlot = armorSlotBox.getSelectedItem() == null ? "" : armorSlotBox.getSelectedItem().toString().trim();
            if (armorType.isBlank() || armorSlot.isBlank()) {
                JOptionPane.showMessageDialog(this, "Select both an armor type and armor slot.", "Missing Armor", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            String displayName = armorType + " " + armorSlot;
            return new MoldEntrySelection(displayName, buildMoldEntryDescription(MOLD_CATEGORY_ARMOR, armorType, armorSlot));
        }

        String itemName = itemField.getText() == null ? "" : itemField.getText().trim();
        if (itemName.isBlank()) {
            JOptionPane.showMessageDialog(this, "An item mold name is required.", "Missing Item", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new MoldEntrySelection(itemName, buildMoldEntryDescription(MOLD_CATEGORY_ITEM, "", ""));
    }

    private String buildMoldEntryDescription(String category, String type, String slot) {
        return "CATEGORY=" + safeLabel(category)
                + "|TYPE=" + safeLabel(type)
                + "|SLOT=" + safeLabel(slot);
    }

    private ArrayList<String> collectWeaponTypeOptions() {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        if (ruleManager == null) return new ArrayList<>(options);
        for (DataItemWeapon weapon : ruleManager.getItemWeaponData()) {
            if (weapon == null || weapon.getType() == null) continue;
            String type = weapon.getType().trim();
            if (!type.isBlank()) options.add(type);
        }
        ArrayList<String> sorted = new ArrayList<>(options);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    private ArrayList<String> collectArmorTypeOptions() {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        if (ruleManager == null) return new ArrayList<>(options);
        for (DataItemEquipment armor : ruleManager.getItemEquipmentData()) {
            if (armor == null || armor.getCategory() == null || armor.getType() == null) continue;
            if (!"Armor".equalsIgnoreCase(armor.getCategory().trim())) continue;
            String type = armor.getType().trim();
            if (type.isBlank() || "Shifter".equalsIgnoreCase(type)) continue;
            options.add(type);
        }
        ArrayList<String> sorted = new ArrayList<>(options);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    private ArrayList<String> collectArmorSlotOptions() {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        if (ruleManager == null) return new ArrayList<>(options);
        for (DataItemEquipment armor : ruleManager.getItemEquipmentData()) {
            if (armor == null || armor.getCategory() == null || armor.getSlot() == null) continue;
            if (!"Armor".equalsIgnoreCase(armor.getCategory().trim())) continue;
            String slot = armor.getSlot().trim();
            if (!slot.isBlank()) options.add(slot);
        }
        ArrayList<String> sorted = new ArrayList<>(options);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    private String resolveListSelectionInput(String listName, int index, int total) {
        String[] options = resolveListEntryOptions(listName);
        String title = total > 1 ? "Select " + listName + " (" + index + " of " + total + ")" : "Select " + listName;
        if (options.length > 0) {
            JComboBox<String> combo = new JComboBox<>(options);
            combo.setEditable(true);
            int result = JOptionPane.showConfirmDialog(this, combo, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return null;
            Object selected = combo.getEditor().getItem();
            if (selected == null) selected = combo.getSelectedItem();
            return selected == null ? null : selected.toString();
        }
        return JOptionPane.showInputDialog(this, "Enter new " + listName + ":", title, JOptionPane.PLAIN_MESSAGE);
    }

    private String[] resolveListEntryOptions(String listName) {
        if (!ALTERI_SHAPESHIFT_LIST.equalsIgnoreCase(listName) || ruleManager == null) return new String[0];
        ArrayList<String> names = new ArrayList<>();
        String currentRace = character != null && character.getIdentity() != null ? safeLabel(character.getIdentity().getRace()) : "";
        for (DataRace race : ruleManager.getRaceData()) {
            if (race == null || race.getName() == null) continue;
            String name = safeLabel(race.getName());
            if (name.isBlank() || name.equalsIgnoreCase(currentRace)) continue;
            names.add(name);
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names.toArray(new String[0]);
    }

    private boolean hasListEntry(String listName, String entryName) {
        if (character == null || character.getLists() == null || listName == null || entryName == null) return false;
        for (List<DataList> group : character.getLists()) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
                if (entryName.equalsIgnoreCase(entry.getName().trim())) return true;
            }
        }
        return false;
    }

    private boolean hasListNamed(String listName) {
        if (character == null || character.getLists() == null || listName == null || listName.isBlank()) return false;
        for (List<DataList> group : character.getLists()) {
            if (group == null || group.isEmpty()) continue;
            DataList first = group.get(0);
            if (first == null || first.getList() == null) continue;
            if (listName.equalsIgnoreCase(first.getList().trim())) return true;
        }
        return false;
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        if (values == null || target == null) return false;
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    private void addListEntry(String listName, String entryName, String description) {
        if (character == null || listName == null || entryName == null) return;
        List<List<DataList>> lists = character.getLists();
        if (lists == null) {
            lists = new ArrayList<>();
            character.setLists(lists);
        }
        for (List<DataList> group : lists) {
            if (group == null || group.isEmpty()) continue;
            DataList first = group.get(0);
            if (first == null || first.getList() == null) continue;
            if (!listName.equalsIgnoreCase(first.getList().trim())) continue;
            group.add(new DataList(listName, entryName, safeLabel(description)));
            return;
        }
        ArrayList<DataList> newGroup = new ArrayList<>();
        newGroup.add(new DataList(listName, entryName, safeLabel(description)));
        lists.add(newGroup);
    }

    private void ensureListWithFirstEntry(String listName, String entryName, String description) {
        if (listName == null || listName.isBlank() || entryName == null || entryName.isBlank()) return;
        if (hasListNamed(listName)) return;
        addListEntry(listName, entryName, description);
    }

    private boolean isAuraAffinityTech(DataTraining tech) {
        if (tech == null || tech.getName() == null) return false;
        return tech.getName().trim().toLowerCase().startsWith("aura affinity");
    }

    private int getTypeOrder(String rawType) {
        if ("Maintained".equalsIgnoreCase(rawType)) return 1;
        if ("Passive".equalsIgnoreCase(rawType)) return 2;
        return 0;
    }

    private static final class TrainingCardControls {
        private JComboBox<String> affinityBox;
        private JComboBox<String> typeBox;
        private JComboBox<String> techniqueBox;
        private JFormattedTextField maxRankField;
        private JFormattedTextField curRankField;
        private JFormattedTextField nextXpField;
        private JFormattedTextField auraXpField;
        private JFormattedTextField typedXpField;
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

