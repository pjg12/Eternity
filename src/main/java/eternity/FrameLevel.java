package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

public class FrameLevel extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String SHIFTER_COMPLEXITY_SPECIALTY = "Complexity I";
    private static final int FRAME_WIDTH = 470;
    private static final int FRAME_HEIGHT = 355;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 18);
    private static final Font SUBHEADER_FONT = new Font(null, Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 6, 18);
    private static final EmptyBorder CONTENT_BORDER = new EmptyBorder(8, 18, 8, 18);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 18, 12, 18);
    private static final Insets FIELD_INSETS = new Insets(4, 6, 4, 6);

    private final FrameSheet sheetFrame;
    private final StoreRuleManager dataQuery;
    private StoreCharData character;
    private boolean skipLevelIncrement = false;
    private Integer levelContext = null;
    private Integer subclassReminderLevel = null;
    private int pendingExtraSkillSelections = 0;

    private final FrameSkill frameSkill;
    private final FrameSpecial frameSpecial;

    private final JLabel headerL;
    private final JLabel subHeaderL;
    private final JLabel subHeader2L;
    private final JLabel expLabel;
    private final JLabel skillAttributeLabel;
    private final JLabel skillNameLabel;
    private final JLabel skillSubtypeLabel;
    private final JLabel specialtyTypeLabel;
    private final JLabel specialtyNameLabel;

    private final JFormattedTextField expField;

    private final JButton cancelButton;
    private final JButton acceptButton;

    FrameLevel(FrameSheet sheetFrame, StoreRuleManager dataQuery) {
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
        skillSubtypeLabel = buildFieldLabel("Subtype");
        specialtyTypeLabel = buildFieldLabel("Type");
        specialtyNameLabel = buildFieldLabel("Specialty");

        NumberFormatter nf = createNullableIntegerFormatter();
        expField = new JFormattedTextField(nf);
        expField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        expField.setHorizontalAlignment(JTextField.CENTER);

        cancelButton = new JButton("Cancel");
        acceptButton = new JButton("Accept");

        frameSkill = new FrameSkill(dataQuery);
        frameSpecial = new FrameSpecial(dataQuery);

        buildUi();
        clearLevel();
    }

    /*
     * Sets the character to be modified
     */
    public void updateCharacter(StoreCharData character) {
        this.character = character;
        frameSkill.setOwnedSkillFilter(character, true);
        frameSpecial.setOwnedSpecialtyFilter(character, true);
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

        headerL.setText("Enter Experience Value");
        headerL.setVisible(true);

        expLabel.setVisible(true);
        expField.setValue(0);
        expField.setVisible(true);

        attachButtonAction(cancelButton, e -> setVisible(false));
        cancelButton.setVisible(true);
        acceptButton.setVisible(true);
        refreshLayout();
    } //End of addXp

    /*
     * Clears the frame to its base state
     */
    public void clearLevel() {
        clearButton(cancelButton);
        clearButton(acceptButton);

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
        skillSubtypeLabel.setVisible(false);
        specialtyTypeLabel.setVisible(false);
        specialtyNameLabel.setVisible(false);

        frameSkill.clear();
        frameSpecial.clear();
        pendingExtraSkillSelections = 0;

        refreshLayout();
    }

    public void levelUp() {
        clearLevel();

        headerL.setText("You have leveled up!");
        headerL.setVisible(true);

        subHeaderL.setText("Numeric values have increased immediately.");
        subHeaderL.setVisible(true);

        attachButtonAction(acceptButton, e -> levelUp2());
        acceptButton.setVisible(true);
        refreshLayout();
    }

    public void levelUp2() {
        clearLevel();

        int currentLevel = levelContext != null ? levelContext
                : (character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0);

        if (currentLevel == 5
                && !java.util.Objects.equals(subclassReminderLevel, currentLevel)
                && character != null
                && character.getIdentity() != null
                && !confirmSubclassLockIn(currentLevel)) {
            return;
        }

        if (!resolvePendingClassSpecialtyChoices()) {
            return;
        }

        headerL.setText("Welcome to level " + currentLevel);
        headerL.setVisible(true);

        pendingExtraSkillSelections = getExtraSkillSelectionCount(currentLevel);

        if (currentLevel % 3 == 0 || currentLevel == 19) {
            subHeader2L.setText("You have gained a new feature.");
            subHeader2L.setVisible(true);
            frameSpecial.showSpecialtySelection(specialtyTypeLabel, specialtyNameLabel);
        }
        if (currentLevel % 3 != 0 || currentLevel == 19 || pendingExtraSkillSelections > 0) {
            subHeaderL.setText(buildSkillHeaderText(currentLevel));
            subHeaderL.setVisible(true);
            frameSkill.showSkillSelection(skillAttributeLabel, skillNameLabel, skillSubtypeLabel);
        }

        attachButtonAction(acceptButton, e -> levelUpCon());
        acceptButton.setVisible(true);
        refreshLayout();
    }

    private boolean resolvePendingClassSpecialtyChoices() {
        if (character == null || character.getSpecials() == null) return true;
        for (DataSpecialty specialty : character.getSpecials().getClassSpecialties()) {
            if (specialty == null || !FrameSpecialsPicker.requiresChoice(specialty)) continue;
            String refName = specialty.getRefName();
            if (refName != null && !refName.isBlank()) continue;

            DataSpecialty resolved = FrameSpecialsPicker.resolveSpecialtyChoice(this, dataQuery, character, specialty);
            if (resolved == null) {
                return false;
            }
            specialty.setRefName(resolved.getRefName());
            character.syncSpecialtyChoiceLists();
        }
        return true;
    }

    private boolean confirmSubclassLockIn(int currentLevel) {
        if (character == null || character.getIdentity() == null) {
            return true;
        }

        String subclass = character.getIdentity().getCharSubclass();
        String cls = character.getIdentity().getCharClass();
        String msg = "Upon reaching level 5 you will not be able to change subclass.\n"
                + "Current class: " + (cls == null ? "?" : cls) + "\n"
                + "Current subclass: " + (subclass == null ? "?" : subclass) + "\n\n"
                + "Select Confirm to continue or Cancel to revert to level 4.";
        int choice = javax.swing.JOptionPane.showConfirmDialog(
                this,
                msg,
                "Subclass Reminder",
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);
        if (choice == javax.swing.JOptionPane.OK_OPTION) {
            subclassReminderLevel = currentLevel;
            return true;
        }
        revertLevelUpTo(Math.max(1, currentLevel - 1));
        return false;
    }

    private void revertLevelUpTo(int targetLevel) {
        if (character == null || character.getIdentity() == null) {
            dispose();
            return;
        }

        CharIdentity id = character.getIdentity();
        int actualLevel = Math.max(1, id.getLevel());
        float restoredExp = id.getExp();
        for (int level = Math.max(1, targetLevel); level < actualLevel; level++) {
            restoredExp += nextExpRequirement(level);
        }

        id.setLevel(targetLevel);
        id.setExp(restoredExp);
        character.updateAll();

        if (sheetFrame != null) {
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
            sheetFrame.refreshTrainingPanel();
        }

        skipLevelIncrement = false;
        levelContext = null;
        subclassReminderLevel = null;
        dispose();
    }

    public void levelUpCon() {
        int contextLevel = levelContext != null ? levelContext
                : (character != null && character.getIdentity() != null ? character.getIdentity().getLevel() : 0);
        boolean skipIncrementThisPass = skipLevelIncrement;

        if (!frameSkill.applySelection(character)) return;
        if (!frameSpecial.applySelection(character, this)) return;

        if (pendingExtraSkillSelections > 0) {
            pendingExtraSkillSelections--;
        }

        CharIdentity id = character.getIdentity();
        long now = System.currentTimeMillis();
        id.setLastLevelUp(new Timestamp(now));
        id.setTimeSinceLastLevel(id.getCampaignElapsedTime());

        if (!skipIncrementThisPass) {
            float newExp = (float) (id.getExp() - nextExpRequirement(id.getLevel()));
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
        if (pendingExtraSkillSelections > 0) {
            skipLevelIncrement = skipIncrementThisPass;
            levelContext = contextLevel;
            levelUp2();
            setVisible(true);
            return;
        }

        if (contextLevel < currentLevel) {
            skipLevelIncrement = skipIncrementThisPass;
            levelContext = contextLevel + 1;
            levelUp();
            setVisible(true);
            return;
        }

        skipLevelIncrement = false;
        levelContext = null;
        subclassReminderLevel = null;
        dispose();
    }

    private String buildSkillHeaderText(int currentLevel) {
        int totalSkillSelections = (currentLevel % 3 != 0 || currentLevel == 19 ? 1 : 0) + pendingExtraSkillSelections;
        if (totalSkillSelections <= 1) {
            return "You have gained a new skill.";
        }
        return "You have gained " + totalSkillSelections + " new skills.";
    }

    private int getExtraSkillSelectionCount(int currentLevel) {
        if (!hasShifterComplexitySpecialty()) return 0;
        if (currentLevel < 3) return 0;
        return currentLevel % 3 == 0 ? 1 : 0;
    }

    private boolean hasShifterComplexitySpecialty() {
        return character != null
                && character.getSpecials() != null
                && character.getSpecials().hasSpecialty(SHIFTER_COMPLEXITY_SPECIALTY);
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
        addTwoColumnRow(panel, gbc, 3, frameSkill.getSkillAttributes(), frameSkill.getSkillList());
        addWideRow(panel, gbc, 4, skillSubtypeLabel);
        addWideRow(panel, gbc, 5, frameSkill.getSkillSubtype());
        addTwoColumnRow(panel, gbc, 6, specialtyTypeLabel, specialtyNameLabel);
        addTwoColumnRow(panel, gbc, 7, frameSpecial.getSpecialsType(), frameSpecial.getSpecialsList());

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBorder(FOOTER_BORDER);
        panel.add(cancelButton);
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
