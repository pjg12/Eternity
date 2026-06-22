package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

public class FrameExp extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int FRAME_WIDTH = 460;
    private static final int FRAME_HEIGHT = 300;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 18);
    private static final Font SUBHEADER_FONT = new Font(null, Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 6, 18);
    private static final EmptyBorder CONTENT_BORDER = new EmptyBorder(8, 18, 8, 18);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 18, 12, 18);
    private static final Insets FIELD_INSETS = new Insets(4, 6, 4, 6);
    private static final String[] ATTRIBUTES = { "STR", "DEX", "CON", "FOC", "CAP", "CTL", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB" };
    private static final String[] SPECTYPES = { "Proficiency", "Martial", "Class" };

    private final FrameSheet sheetFrame;
    private final StoreRuleManager dataQuery;
    private StoreCharData character;
    private FrameLevel levelFrame;

    private boolean skillLevel;
    private boolean specLevel;
    private boolean levelChoiceListenersAttached;
    private String lastSkillAttribute;
    private String lastSpecialtyType;
    private final Map<String, String[]> skillOptionsByAttribute = new HashMap<>();
    private final Map<String, String[]> specialtyOptionsByType = new HashMap<>();

    private final JLabel headerL;
    private final JLabel subHeaderL;
    private final JLabel subHeader2L;
    private final JLabel expLabel;
    private final JLabel skillAttributeLabel;
    private final JLabel skillNameLabel;
    private final JLabel specialtyTypeLabel;
    private final JLabel specialtyNameLabel;

    private final JFormattedTextField expField;

    private final JButton cancelButton;
    private final JButton levelUpButton;
    private final JButton acceptButton;

    private final JComboBox<String> skillList;
    private final JComboBox<String> skillAttributes;
    private final JComboBox<String> specialsList;
    private final JComboBox<String> specialsType;

    FrameExp(FrameSheet sheetFrame, StoreRuleManager dataQuery) {
        super("Exp Up");
        this.sheetFrame = sheetFrame;
        this.dataQuery = dataQuery;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout());

        headerL = buildHeaderLabel(HEADER_FONT);
        subHeaderL = buildHeaderLabel(SUBHEADER_FONT);
        subHeader2L = buildHeaderLabel(SUBHEADER_FONT);

        expLabel = buildFieldLabel("Experience");
        skillAttributeLabel = buildFieldLabel("Attribute");
        skillNameLabel = buildFieldLabel("Skill");
        specialtyTypeLabel = buildFieldLabel("Type");
        specialtyNameLabel = buildFieldLabel("Specialty");

        NumberFormatter formatter = new NumberFormatter(java.text.NumberFormat.getIntegerInstance());
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0);

        expField = new JFormattedTextField(formatter);
        expField.setHorizontalAlignment(JTextField.CENTER);

        cancelButton = new JButton("Cancel");
        levelUpButton = new JButton("Level Up");
        acceptButton = new JButton("Accept");

        skillList = new JComboBox<>();
        skillAttributes = new JComboBox<>(ATTRIBUTES);
        specialsList = new JComboBox<>();
        specialsType = new JComboBox<>(SPECTYPES);

        buildUi();
        clearLevel();
    }

    /*
     * Sets the character to be modified
     */
    public void updateCharacter(StoreCharData character) {
        this.character = character;
    } //End of updateCharacter

    /*
     * Sets Add Exp Window
     */
    public void addXp() {
        clearLevel();

        headerL.setText("Enter Experience Value");
        headerL.setVisible(true);

        expLabel.setVisible(true);
        expField.setValue(0);
        expField.setVisible(true);

        attachButtonAction(cancelButton, e -> setVisible(false));
        attachButtonAction(levelUpButton, e -> levelUpPressed());
        attachButtonAction(acceptButton, e -> expPressed());

        cancelButton.setVisible(true);
        levelUpButton.setVisible(true);
        acceptButton.setVisible(true);
        refreshLayout();
    } //End of addXp

    /*
     * Clears the frame to its base state
     */
    public void clearLevel() {
        clearButton(cancelButton);
        clearButton(levelUpButton);
        clearButton(acceptButton);
        clearChoiceListeners();

        skillLevel = false;
        specLevel = false;
        lastSkillAttribute = null;
        lastSpecialtyType = null;

        headerL.setText("");
        subHeaderL.setText("");
        subHeader2L.setText("");
        headerL.setVisible(false);
        subHeaderL.setVisible(false);
        subHeader2L.setVisible(false);

        expField.setValue(0);
        expField.setVisible(false);
        expLabel.setVisible(false);

        skillAttributeLabel.setVisible(false);
        skillNameLabel.setVisible(false);
        specialtyTypeLabel.setVisible(false);
        specialtyNameLabel.setVisible(false);

        skillList.setVisible(false);
        skillAttributes.setVisible(false);
        specialsList.setVisible(false);
        specialsType.setVisible(false);

        refreshLayout();
    }

    public void expPressed() {
        if (character == null || character.getIdentity() == null) {
            setVisible(false);
            return;
        }

        CharIdentity id = character.getIdentity();
        double gain = parseExpField(expField);
        if (gain <= 0) {
            JOptionPane.showMessageDialog(this, "Enter a positive experience value.");
            return;
        }
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
                ? String.format("You are about to gain %.0f experience.%nThis will raise your level from %d to %d.%nProceed?", gain, startLevel, tempLevel)
                : String.format("You are about to gain %.0f experience.%nYou will remain level %d.%nProceed?", gain, startLevel);

        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Experience Gain",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        id.setLevel(tempLevel);
        id.setExp((float) tempExp);
        character.syncIdentityDerivedState(dataQuery);
        character.syncLevelBaseResources(dataQuery);
        character.syncLevelCombatScalers(dataQuery);
        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
            sheetFrame.refreshTrainingPanel();
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

        dispose();
    }

    private void levelUpPressed() {
        if (character == null || character.getIdentity() == null) {
            setVisible(false);
            return;
        }

        CharIdentity id = character.getIdentity();
        double expNeeded = Math.max(0.0, nextExpRequirement(id.getLevel()) - id.getExp());
        int wholeExpNeeded = (int) Math.ceil(expNeeded);
        expField.setValue(wholeExpNeeded);
        expPressed();
    }

    public void levelUp() {
        clearLevel();

        headerL.setText("You have leveled up!");
        subHeaderL.setText("Numeric values have increased immediately.");
        subHeader2L.setText("Increase your class training rank for class abilities.");
        headerL.setVisible(true);
        subHeaderL.setVisible(true);
        subHeader2L.setVisible(true);

        attachButtonAction(acceptButton, e -> levelUp2());
        acceptButton.setVisible(true);
        refreshLayout();
    }

    public void levelUp2() {
        clearLevel();

        int currentLevel = character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0;
        headerL.setText("Welcome to level " + (currentLevel + 1));
        headerL.setVisible(true);

        if (currentLevel % 3 == 0 || currentLevel == 19) {
            subHeader2L.setText("You have gained a new feature.");
            subHeader2L.setVisible(true);
            specialtyTypeLabel.setVisible(true);
            specialtyNameLabel.setVisible(true);
            specialsType.setVisible(true);
            specialsList.setVisible(true);
            specLevel = true;
        }
        if (currentLevel % 3 != 0 || currentLevel == 19) {
            subHeaderL.setText("You have gained a new skill.");
            subHeaderL.setVisible(true);
            skillAttributeLabel.setVisible(true);
            skillNameLabel.setVisible(true);
            skillAttributes.setVisible(true);
            skillList.setVisible(true);
            skillLevel = true;
        }

        attachButtonAction(acceptButton, e -> levelUpCon());
        acceptButton.setVisible(true);

        updateData();
        attachChoiceListeners();
        refreshLayout();
    }

    void updateData() {
        String selectedAttribute = (String) skillAttributes.getSelectedItem();
        if (skillLevel && !java.util.Objects.equals(lastSkillAttribute, selectedAttribute)) {
            lastSkillAttribute = selectedAttribute;
            skillList.removeAllItems();
            for (String skillName : getSkillOptions(selectedAttribute)) {
                skillList.addItem(skillName);
            }
        }
        String selectedType = (String) specialsType.getSelectedItem();
        if (specLevel && !java.util.Objects.equals(lastSpecialtyType, selectedType)) {
            lastSpecialtyType = selectedType;
            specialsList.removeAllItems();
            for (String specialtyName : getSpecialtyOptions(selectedType)) {
                specialsList.addItem(specialtyName);
            }
        }
    }

    public void levelUpCon() {
        if (skillLevel) {
            CharSpecials specials = character.getSpecials();
            DataSkill picked = dataQuery.getSkillByName((String) skillList.getSelectedItem());
            if (picked != null) {
                DataSkill copy = new DataSkill(picked);
                copy.addChosenAttribute((String) skillAttributes.getSelectedItem());
                specials.addSkill(copy);
            }
        }
        if (specLevel) {
            DataSpecialty picked = dataQuery.getSpecialtyByName((String) specialsList.getSelectedItem());
            if (picked != null) {
                DataSpecialty resolved = FrameSpecialsPicker.resolveSpecialtyChoice(this, dataQuery, character, picked);
                if (resolved == null) return;
                if (!FrameSpecial.applyResolvedSpecialtyGrant(this, dataQuery, character, resolved)) return;
            }
        }
        CharIdentity id = character.getIdentity();
        float newExp = (float) (id.getExp() - nextExpRequirement(id.getLevel()));
        id.setExp(Math.max(0f, newExp));
        id.setLevel(id.getLevel() + 1);
        character.syncIdentityDerivedState(dataQuery);
        character.syncLevelBaseResources(dataQuery);
        character.syncLevelCombatScalers(dataQuery);
        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
            sheetFrame.refreshTrainingPanel();
        }
        dispose();
    }

    private void attachChoiceListeners() {
        if (levelChoiceListenersAttached) return;
        ActionListener refresh = e -> updateData();
        skillAttributes.addActionListener(refresh);
        specialsType.addActionListener(refresh);
        levelChoiceListenersAttached = true;
    }

    private void clearChoiceListeners() {
        for (ActionListener listener : skillAttributes.getActionListeners()) {
            skillAttributes.removeActionListener(listener);
        }
        for (ActionListener listener : specialsType.getActionListeners()) {
            specialsType.removeActionListener(listener);
        }
        levelChoiceListenersAttached = false;
    }

    private String[] getSkillOptions(String attribute) {
        return skillOptionsByAttribute.computeIfAbsent(attribute, key -> {
            java.util.List<DataSkill> skills = dataQuery.getSkillsByAttribute(attribute);
            String[] names = new String[skills.size()];
            for (int i = 0; i < skills.size(); i++) {
                names[i] = skills.get(i).getName();
            }
            return names;
        });
    }

    private String[] getSpecialtyOptions(String type) {
        return specialtyOptionsByType.computeIfAbsent(type, key -> {
            java.util.List<DataSpecialty> specialties = dataQuery.getSpecialtiesByType(type).stream()
                    .filter(this::isAvailableLevelUpSpecialty)
                    .toList();
            String[] names = new String[specialties.size()];
            for (int i = 0; i < specialties.size(); i++) {
                names[i] = specialties.get(i).getName();
            }
            return names;
        });
    }

    private boolean isAvailableLevelUpSpecialty(DataSpecialty specialty) {
        if (specialty == null || specialty.getPrereq() < 0) return false;
        String name = specialty.getName();
        if (name == null || name.isBlank()) return false;
        if (character == null || character.getSpecials() == null) return true;
        if (!CharSpecials.isRepeatableSpecialty(specialty) && character.getSpecials().hasSpecialty(name)) return false;
        return !lacksRequiredSpecialty(specialty);
    }

    private boolean lacksRequiredSpecialty(DataSpecialty specialty) {
        if (specialty == null || character == null || character.getSpecials() == null) return false;
        int prereqId = specialty.getPrereq();
        if (prereqId <= 0) return false;

        DataSpecialty prereqSpecialty = dataQuery.getSpecialtyById(prereqId);
        if (prereqSpecialty == null) return false;

        String prereqName = prereqSpecialty.getName();
        return prereqName != null
                && !prereqName.isBlank()
                && !character.getSpecials().hasSpecialty(prereqName);
    }

    private double parseExpField(JFormattedTextField field) {
        Object value = field.getValue();
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = field.getText();
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ignore) {
            return 0.0;
        }
    }

    /** Simple placeholder: next level requires (level + 1) * 100 XP. */
    private int nextExpRequirement(int level) {
        if (level <= 0) return Integer.MAX_VALUE;
        return level * 1000;
    }

    private void buildUi() {
        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(HEADER_BORDER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 4, 0);

        gbc.gridy = 0;
        panel.add(headerL, gbc);

        gbc.gridy = 1;
        panel.add(subHeaderL, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(subHeader2L, gbc);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(CONTENT_BORDER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = FIELD_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        addWideRow(panel, gbc, 0, expLabel);
        addWideRow(panel, gbc, 1, expField);
        addTwoColumnRow(panel, gbc, 2, skillAttributeLabel, skillNameLabel);
        addTwoColumnRow(panel, gbc, 3, skillAttributes, skillList);
        addTwoColumnRow(panel, gbc, 4, specialtyTypeLabel, specialtyNameLabel);
        addTwoColumnRow(panel, gbc, 5, specialsType, specialsList);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBorder(FOOTER_BORDER);
        panel.add(cancelButton);
        panel.add(levelUpButton);
        panel.add(acceptButton);
        return panel;
    }

    private JLabel buildHeaderLabel(Font font) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(font);
        return label;
    }

    private JLabel buildFieldLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(LABEL_FONT);
        return label;
    }

    private void addWideRow(JPanel panel, GridBagConstraints gbc, int row, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
        gbc.gridwidth = 1;
    }

    private void addTwoColumnRow(JPanel panel, GridBagConstraints gbc, int row, java.awt.Component left, java.awt.Component right) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        panel.add(left, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(right, gbc);
    }

    private void attachButtonAction(JButton button, ActionListener listener) {
        clearButton(button);
        button.addActionListener(listener);
    }

    private void clearButton(JButton button) {
        for (ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
        button.setVisible(false);
    }

    private void refreshLayout() {
        revalidate();
        repaint();
    }
}
