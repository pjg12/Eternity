package eternity;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Prompts for any additional choice text a specialty requires before it is granted.
 */
public class FrameSpecialsPicker extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final String EMPTY_OPTION = "***";
    private static final String COMBAT_SPECIALIST_SPECIALTY = "Combat Specialist";
    private static final String COMBAT_MANEUVER_SOURCE = "Combat Maneuver";
    private static final String MARTIAL_FOCUS_SPECIALTY = "Martial Focus";
    private static final String COMBAT_DISCIPLINE_SPECIALTY = "Combat Discipline";
    private static final String COMBAT_DISCIPLINE_I_DISPLAY = "Combat Discipline I";
    private static final String COMBAT_DISCIPLINE_II_SPECIALTY = "Combat Discipline II";
    private static final String STANCE_SPECIALTY = "Stance";
    private static final String DIVINE_DEDICATION_SPECIALTY = "Divine Dedication";
    private static final String WEAPON_SPECIALIZATION_SPECIALTY = "Weapon Specialization";
    private static final String MARTIAL_FEATURE_PREFIX = "Martial Feature";
    private static final String SPECIALIZATION_PREFIX = "Specialization (";
    private static final String[] MARTIAL_FOCUS_OPTIONS = { "Mobility", "Avoidance", "Harm" };
    private static final String[] COMBAT_DISCIPLINE_OPTIONS = { "Mobility", "Avoidance", "Martial" };
    private static final String[] DIVINE_DEDICATION_OPTIONS = { "Faithful", "Dawning", "Dominant" };
    private static final String[] MELEE_WEAPON_TYPES = {"Greatsword", "Battleaxe", "Warhammer", "Polearm", "Sword", "Axe", "Mace", "Shield", "Blade", "Dagger", "Fist", "Whip"};
    private static final String[] RANGED_WEAPON_TYPES = {"Bow", "Crossbow", "Rifle", "Cannon", "Thrown", "Sling", "Handbow", "Pistol"};
    private static final String[] AURA_WEAPON_TYPES = {"Staff", "Tome", "Relic", "Symbol", "Ring", "Orb", "Wand", "Talisman"};
    private static final String[][] STANCE_OPTIONS = {
            {"Precision", "3"},
            {"Power", "3"},
            {"Protection", "3"},
            {"Punishment", "3"}
    };
    private static final int FRAME_WIDTH = 560;
    private static final int FRAME_HEIGHT = 340;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 8, 18);
    private static final EmptyBorder CENTER_BORDER = new EmptyBorder(10, 12, 10, 12);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 12, 10, 12);
    private static final Insets FIELD_INSETS = new Insets(8, 8, 8, 8);

    private final DataSpecialty specialty;
    private final List<ChoiceField> choiceFields;
    private final Map<ChoiceField, JComboBox<String>> comboFields = new LinkedHashMap<>();
    private final Map<ChoiceField, JTextField> textFields = new LinkedHashMap<>();

    private DataSpecialty resolvedSpecialty;

    private FrameSpecialsPicker(Window parent, StoreRuleManager ruleManager, StoreCharData character, DataSpecialty specialty) {
        super(parent, specialty == null ? "Specialty Choice" : specialty.getName(), ModalityType.APPLICATION_MODAL);
        this.specialty = specialty == null ? null : new DataSpecialty(specialty);
        this.choiceFields = buildChoiceFields(ruleManager, character, this.specialty);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        buildUi();
    }

    public static DataSpecialty resolveSpecialtyChoice(Component parent, StoreRuleManager ruleManager, StoreCharData character, DataSpecialty specialty) {
        if (specialty == null) return null;

        DataSpecialty copy = new DataSpecialty(specialty);
        if (!requiresChoice(copy)) {
            return copy;
        }
        if (STANCE_SPECIALTY.equalsIgnoreCase(copy.getName()) && getAvailableStanceOptions(character).isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No stance options are currently available.");
            return null;
        }
        if (isMartialFeaturePlaceholder(copy) && buildAvailableMartialFeatureOptions(ruleManager, character).isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No martial feature options are currently available.");
            return null;
        }
        if (isWeaponSpecializationSpecialty(copy)) {
            List<String> weaponOptions = buildAvailableWeaponSpecializationOptions(ruleManager, character);
            if (!characterHasWeaponSpecialization(character) && weaponOptions.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No proficient weapon types are currently available for specialization.");
                return null;
            }
            if (characterHasWeaponSpecialization(character)
                    && buildAvailableMartialFeatureOptions(ruleManager, character).isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No martial feature options are currently available.");
                return null;
            }
        }
        if (CharSpecials.SKILL_DEDICATION_SPECIALTY.equalsIgnoreCase(copy.getName())
                && getSkillDedicationOptions(character).isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Skill Dedication requires a known skill that is not already dedicated.");
            return null;
        }

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        FrameSpecialsPicker picker = new FrameSpecialsPicker(owner, ruleManager, character, copy);
        picker.setVisible(true);
        return picker.resolvedSpecialty;
    }

    public static boolean requiresChoice(DataSpecialty specialty) {
        return specialty != null && (specialty.getPick() || isMartialFocusSpecialty(specialty) || isCombatDisciplineSpecialty(specialty));
    }

    static String[] getMartialFocusOptions() {
        return MARTIAL_FOCUS_OPTIONS.clone();
    }

    private static boolean isMartialFocusSpecialty(DataSpecialty specialty) {
        return specialty != null
                && specialty.getName() != null
                && MARTIAL_FOCUS_SPECIALTY.equalsIgnoreCase(specialty.getName().trim());
    }

    private static boolean isCombatDisciplineSpecialty(DataSpecialty specialty) {
        if (specialty == null || specialty.getName() == null) return false;
        String name = specialty.getName().trim();
        return COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(name)
                || COMBAT_DISCIPLINE_II_SPECIALTY.equalsIgnoreCase(name);
    }

    private void buildUi() {
        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(HEADER_BORDER);

        JLabel header = new JLabel("Complete Specialty Choice", SwingConstants.CENTER);
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        String description = specialty == null ? "" : specialty.getDescription();
        if (description != null && !description.isBlank()) {
            JTextArea area = new JTextArea(description.trim());
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setOpaque(false);
            area.setFocusable(false);
            area.setFont(LABEL_FONT);
            panel.add(area, BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(CENTER_BORDER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = FIELD_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        for (int i = 0; i < choiceFields.size(); i++) {
            ChoiceField field = choiceFields.get(i);

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.35;
            panel.add(buildLabel(field.label), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;
            panel.add(buildInput(field), gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = choiceFields.size();
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panel.setBorder(FOOTER_BORDER);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        panel.add(cancel);

        JButton confirm = new JButton("Confirm");
        confirm.addActionListener(e -> onConfirmPressed());
        panel.add(confirm);

        return panel;
    }

    private JLabel buildLabel(String text) {
        JLabel label = new JLabel(text == null ? "Choice" : text);
        label.setFont(LABEL_FONT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private Component buildInput(ChoiceField field) {
        if (field.options != null && field.options.length > 0) {
            JComboBox<String> box = new JComboBox<>();
            box.addItem(EMPTY_OPTION);
            for (String option : field.options) {
                if (option == null || option.isBlank()) continue;
                box.addItem(option);
            }
            if (field.initialValue != null && !field.initialValue.isBlank()) {
                box.setSelectedItem(field.initialValue);
            }
            comboFields.put(field, box);
            return box;
        }

        JTextField textField = new JTextField(field.initialValue == null ? "" : field.initialValue);
        textFields.put(field, textField);
        return textField;
    }

    private void onConfirmPressed() {
        if (specialty == null) {
            dispose();
            return;
        }
        if (isMartialFeaturePlaceholder(specialty)) {
            String selectedFeature = resolveSingleChoiceValue();
            if (selectedFeature == null) return;
            resolvedSpecialty = new DataSpecialty(specialty);
            resolvedSpecialty.setRefName(selectedFeature);
            dispose();
            return;
        }
        if (isWeaponSpecializationSpecialty(specialty)) {
            String selectedValue = resolveSingleChoiceValue();
            if (selectedValue == null) return;
            resolvedSpecialty = new DataSpecialty(specialty);
            if (!choiceFields.isEmpty() && "Feature".equalsIgnoreCase(choiceFields.get(0).label)) {
                resolvedSpecialty.setRefName(selectedValue);
            } else {
                resolvedSpecialty.setRefName(buildWeaponSpecializationName(selectedValue));
            }
            dispose();
            return;
        }

        List<String> values = new ArrayList<>(choiceFields.size());
        for (ChoiceField field : choiceFields) {
            String value = resolveValue(field);
            if (value == null || value.isBlank() || EMPTY_OPTION.equals(value)) {
                JOptionPane.showMessageDialog(this, "Please complete all specialty choice fields.");
                return;
            }
            if (CharSpecials.isRepeatableSpecialty(specialty)) {
                values.add(value.trim());
            } else if (choiceFields.size() == 1 && (field.label == null || field.label.isBlank() || "Choice".equals(field.label))) {
                values.add(value.trim());
            } else {
                values.add(field.label + ": " + value.trim());
            }
        }

        resolvedSpecialty = new DataSpecialty(specialty);
        if (CharSpecials.isRepeatableSpecialty(resolvedSpecialty) && !values.isEmpty()) {
            resolvedSpecialty.setRefName(values.get(0).trim());
        } else {
            resolvedSpecialty.setRefName(String.join(" | ", values));
        }
        dispose();
    }

    private String resolveValue(ChoiceField field) {
        JComboBox<String> combo = comboFields.get(field);
        if (combo != null) {
            Object selected = combo.getSelectedItem();
            return selected == null ? "" : selected.toString();
        }

        JTextField textField = textFields.get(field);
        return textField == null ? "" : textField.getText();
    }

    private List<ChoiceField> buildChoiceFields(StoreRuleManager ruleManager, StoreCharData character, DataSpecialty specialty) {
        ArrayList<ChoiceField> fields = new ArrayList<>();
        if (specialty == null) return fields;

        String name = specialty.getName() == null ? "" : specialty.getName();
        String refName = specialty.getRefName();
        List<String> refOptions = extractOptions(refName);
        if (!refOptions.isEmpty()) {
            fields.add(ChoiceField.combo("Choice", refOptions, null));
            return fields;
        }

        List<String> descriptionOptions = extractOptions(specialty.getDescription());
        if (!descriptionOptions.isEmpty()) {
            fields.add(ChoiceField.combo("Choice", descriptionOptions, null));
            return fields;
        }

        if ("Evan".equalsIgnoreCase(name)) {
            fields.add(ChoiceField.combo("Alignment", List.of("Hero", "Villain"), null));
            return fields;
        }
        if ("Divine Vow".equalsIgnoreCase(name) && ruleManager != null) {
            List<String> options = new ArrayList<>();
            for (DataVow vow : ruleManager.getVowData()) {
                if (vow != null && vow.getName() != null && !vow.getName().isBlank()) {
                    options.add(vow.getName());
                }
            }
            if (!options.isEmpty()) {
                fields.add(ChoiceField.combo("Vow", options, specialty.getRefName()));
                return fields;
            }
        }
        if (DIVINE_DEDICATION_SPECIALTY.equalsIgnoreCase(name)) {
            fields.add(ChoiceField.combo("Choice", List.of(DIVINE_DEDICATION_OPTIONS), extractSingleChoiceValue(specialty.getRefName())));
            return fields;
        }
        if (COMBAT_SPECIALIST_SPECIALTY.equalsIgnoreCase(name) && ruleManager != null) {
            List<String> options = new ArrayList<>();
            for (DataAction action : ruleManager.getActionsBySource(COMBAT_MANEUVER_SOURCE)) {
                if (action == null || action.getName() == null || action.getName().isBlank()) continue;
                String actionName = action.getName().trim();
                if (!options.contains(actionName)) {
                    options.add(actionName);
                }
            }
            if (!options.isEmpty()) {
                fields.add(ChoiceField.combo("Choice", options, specialty.getRefName()));
                return fields;
            }
        }
        if (MARTIAL_FOCUS_SPECIALTY.equalsIgnoreCase(name) || COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(name)) {
            fields.add(ChoiceField.combo("Focus", List.of(MARTIAL_FOCUS_OPTIONS), normalizeMartialFocusValue(specialty.getRefName())));
            return fields;
        }
        if (COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(name) || COMBAT_DISCIPLINE_II_SPECIALTY.equalsIgnoreCase(name)) {
            fields.add(ChoiceField.combo("Discipline", List.of(COMBAT_DISCIPLINE_OPTIONS), normalizeCombatDisciplineValue(specialty.getRefName())));
            return fields;
        }
        if (STANCE_SPECIALTY.equalsIgnoreCase(name)) {
            List<String> options = buildAvailableStanceOptions(character);
            if (!options.isEmpty()) {
                fields.add(ChoiceField.combo("Stance", options, specialty.getRefName()));
                return fields;
            }
        }
        if (isMartialFeaturePlaceholder(specialty) && ruleManager != null) {
            List<String> options = buildAvailableMartialFeatureOptions(ruleManager, character);
            if (!options.isEmpty()) {
                fields.add(ChoiceField.combo("Feature", options, specialty.getRefName()));
                return fields;
            }
        }
        if (isWeaponSpecializationSpecialty(specialty) && ruleManager != null) {
            if (characterHasWeaponSpecialization(character)) {
                List<String> options = buildAvailableMartialFeatureOptions(ruleManager, character);
                if (!options.isEmpty()) {
                    fields.add(ChoiceField.combo("Feature", options, specialty.getRefName()));
                    return fields;
                }
            } else {
                List<String> options = buildAvailableWeaponSpecializationOptions(ruleManager, character);
                String initialValue = extractWeaponTypeFromSpecializationName(specialty.getRefName());
                fields.add(ChoiceField.combo("Weapon", options, initialValue));
                return fields;
            }
        }
        if (CharSpecials.isRepeatableSpecialtyName(name)) {
            List<String> options = getSkillDedicationOptions(character);
            fields.add(ChoiceField.combo("Skill", options, null));
            return fields;
        }
        if (name.toLowerCase().contains("felshify")) {
            fields.add(ChoiceField.combo("Form", List.of("Cat", "Felsh"), null));
            return fields;
        }
        if (name.toLowerCase().contains("shapeshifting")) {
            List<String> raceNames = new ArrayList<>();
            if (ruleManager != null) {
                for (DataRace race : ruleManager.getRaceData()) {
                    if (race != null && race.getName() != null && !race.getName().isBlank()) {
                        raceNames.add(race.getName());
                    }
                }
            }
            if (!raceNames.isEmpty()) {
                fields.add(ChoiceField.combo("Shift", raceNames, null));
                return fields;
            }
        }
        if ("Versatility (Gaian)".equalsIgnoreCase(name)) {
            fields.add(ChoiceField.text("Skill", ""));
            fields.add(ChoiceField.text("Feature", ""));
            return fields;
        }
        if ("Tails of the Fox (Kitsune)".equalsIgnoreCase(name)) {
            fields.add(ChoiceField.text("Spirit", ""));
            return fields;
        }

        String initialValue = refName == null ? "" : refName;
        fields.add(ChoiceField.text("Choice", initialValue));
        return fields;
    }

    private String resolveSingleChoiceValue() {
        if (choiceFields.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No specialty choice options are available.");
            return null;
        }
        String value = resolveValue(choiceFields.get(0));
        if (value == null || value.isBlank() || EMPTY_OPTION.equals(value)) {
            JOptionPane.showMessageDialog(this, "Please complete all specialty choice fields.");
            return null;
        }
        return value.trim();
    }

    private static boolean isMartialFeaturePlaceholder(DataSpecialty specialty) {
        if (specialty == null || specialty.getName() == null) return false;
        return specialty.getName().trim().regionMatches(true, 0, MARTIAL_FEATURE_PREFIX, 0, MARTIAL_FEATURE_PREFIX.length());
    }

    private static boolean isWeaponSpecializationSpecialty(DataSpecialty specialty) {
        return specialty != null
                && specialty.getName() != null
                && WEAPON_SPECIALIZATION_SPECIALTY.equalsIgnoreCase(specialty.getName().trim());
    }

    private static boolean characterHasWeaponSpecialization(StoreCharData character) {
        if (character == null || character.getSpecials() == null) return false;
        for (DataSpecialty specialty : character.getSpecials().getAllSpecialties()) {
            if (specialty == null || specialty.getName() == null) continue;
            String name = specialty.getName().trim();
            if (name.regionMatches(true, 0, SPECIALIZATION_PREFIX, 0, SPECIALIZATION_PREFIX.length())
                    && name.endsWith(")")) {
                return true;
            }
        }
        return false;
    }

    private static List<String> buildAvailableMartialFeatureOptions(StoreRuleManager ruleManager, StoreCharData character) {
        ArrayList<String> options = new ArrayList<>();
        if (ruleManager == null) return options;
        int level = character == null ? Integer.MAX_VALUE : Math.max(0, character.getLevel());
        for (DataSpecialty option : ruleManager.getSpecialtiesByType("Martial")) {
            if (option == null || option.getName() == null || option.getName().isBlank()) continue;
            if (option.getPrereq() < 0 || option.getPrereq() > level) continue;
            if (character != null && character.getSpecials() != null
                    && !CharSpecials.isRepeatableSpecialty(option)
                    && character.getSpecials().hasSpecialty(option.getName())) {
                continue;
            }
            options.add(option.getName().trim());
        }
        return options;
    }

    private static List<String> buildAvailableWeaponSpecializationOptions(StoreRuleManager ruleManager, StoreCharData character) {
        ArrayList<String> options = new ArrayList<>();
        if (ruleManager == null || character == null || character.getInventory() == null) return options;
        for (DataSpecialty option : ruleManager.getSpecialtiesByType("Martial")) {
            if (option == null || option.getName() == null || option.getName().isBlank()) continue;
            String weaponType = extractWeaponTypeFromSpecializationName(option.getName());
            if (weaponType == null || weaponType.isBlank()) continue;
            if (!isProficientWithWeaponType(character, weaponType)) continue;
            options.add(weaponType);
        }
        return options;
    }

    private static boolean isProficientWithWeaponType(StoreCharData character, String weaponType) {
        if (character == null || character.getInventory() == null || weaponType == null || weaponType.isBlank()) return false;
        for (String proficiency : character.getInventory().getWeaponProficiencies()) {
            if (proficiency == null || proficiency.isBlank()) continue;
            String trimmed = proficiency.trim();
            if ("Any".equalsIgnoreCase(trimmed) || trimmed.equalsIgnoreCase(weaponType)) {
                return true;
            }
            if ("Melee".equalsIgnoreCase(trimmed) && containsIgnoreCase(MELEE_WEAPON_TYPES, weaponType)) return true;
            if ("Ranged".equalsIgnoreCase(trimmed) && containsIgnoreCase(RANGED_WEAPON_TYPES, weaponType)) return true;
            if ("Aura".equalsIgnoreCase(trimmed) && containsIgnoreCase(AURA_WEAPON_TYPES, weaponType)) return true;
        }
        return false;
    }

    private static boolean containsIgnoreCase(String[] values, String candidate) {
        if (values == null || candidate == null) return false;
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(candidate.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String buildWeaponSpecializationName(String weaponType) {
        if (weaponType == null || weaponType.isBlank()) return "";
        return SPECIALIZATION_PREFIX + weaponType.trim() + ")";
    }

    private static String extractWeaponTypeFromSpecializationName(String specialtyName) {
        if (specialtyName == null || specialtyName.isBlank()) return null;
        String trimmed = specialtyName.trim();
        if (!trimmed.regionMatches(true, 0, SPECIALIZATION_PREFIX, 0, SPECIALIZATION_PREFIX.length())
                || !trimmed.endsWith(")")) {
            return null;
        }
        return trimmed.substring(SPECIALIZATION_PREFIX.length(), trimmed.length() - 1).trim();
    }

    private String normalizeMartialFocusValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if ("Martial".equalsIgnoreCase(trimmed)) return "Harm";
        return trimmed;
    }

    private String normalizeCombatDisciplineValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isBlank()) return trimmed;
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < trimmed.length()) {
            trimmed = trimmed.substring(colonIndex + 1).trim();
        }
        if ("Harm".equalsIgnoreCase(trimmed)) return "Martial";
        return trimmed;
    }

    private String extractSingleChoiceValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isBlank()) return trimmed;
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < trimmed.length()) {
            return trimmed.substring(colonIndex + 1).trim();
        }
        return trimmed;
    }

    private List<String> buildAvailableStanceOptions(StoreCharData character) {
        return getAvailableStanceOptions(character);
    }

    public static List<String> getAvailableStanceOptions(StoreCharData character) {
        ArrayList<String> options = new ArrayList<>();
        int level = character == null ? 0 : Math.max(0, character.getLevel());
        for (String[] option : STANCE_OPTIONS) {
            if (option == null || option.length < 2) continue;
            String name = option[0] == null ? "" : option[0].trim();
            if (name.isBlank()) continue;
            int requiredLevel;
            try {
                requiredLevel = Integer.parseInt(option[1]);
            } catch (NumberFormatException ignored) {
                continue;
            }
            int currentRank = character == null ? 0 : character.getStanceRank(name);
            if (level >= requiredLevel && currentRank < 3) {
                options.add(name);
            }
        }
        return options;
    }

    private List<String> extractOptions(String raw) {
        ArrayList<String> options = new ArrayList<>();
        if (raw == null || raw.isBlank()) return options;

        String[] parts = raw.contains("|") ? raw.split("\\|") : raw.split(",");
        if (parts.length <= 1) return options;
        for (String part : parts) {
            if (part == null) continue;
            String trimmed = part.trim();
            if (!trimmed.isBlank()) options.add(trimmed);
        }
        return options;
    }

    private static List<String> getSkillDedicationOptions(StoreCharData character) {
        ArrayList<String> options = new ArrayList<>();
        if (character == null || character.getSpecials() == null) return options;
        CharSpecials specials = character.getSpecials();
        List<DataSkill> skills = specials.getSkills();
        if (skills == null) return options;
        for (DataSkill skill : skills) {
            String display = CharSpecials.formatSkillDisplayName(skill);
            if (display.isBlank()) continue;
            if (specials.getSkillDedicationBonusForDisplayName(display, 1) > 0) continue;
            options.add(display);
        }
        return options;
    }

    private static final class ChoiceField {
        private final String label;
        private final String[] options;
        private final String initialValue;

        private ChoiceField(String label, String[] options, String initialValue) {
            this.label = label;
            this.options = options;
            this.initialValue = initialValue;
        }

        private static ChoiceField combo(String label, List<String> options, String initialValue) {
            return new ChoiceField(label, options == null ? new String[0] : options.toArray(new String[0]), initialValue);
        }

        private static ChoiceField text(String label, String initialValue) {
            return new ChoiceField(label, null, initialValue);
        }
    }
}
