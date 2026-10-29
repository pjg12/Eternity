package eternity;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import eternity.DataTypes.DataSkill;

public class FrameManStatus extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String[] RESOURCE_KEYS = {
            "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
            "BASER1", "MULTIR1", "BASER2", "MULTIR2", "BASER3", "MULTIR3",
            "BASEREACT", "MULTIREACT"
    };
    private static final String[] MANUAL_INSTANT_RESOURCE_KEYS = {
            "SPENTAURA"
    };

    private final FrameStatus statusFrame;
    private StoreCharData character;

    private JComboBox<String> attributeBox;
    private final Map<String, String> attributeKeyByLabel = new LinkedHashMap<>();
    private final List<String> allAttributeOptions = new ArrayList<>();
    private boolean filteringAttributeOptions;
    private JFormattedTextField severityField;

    public FrameManStatus(FrameStatus statusFrame, StoreCharData character) {
        super("Add Effect");
        this.statusFrame = statusFrame;
        this.character = character;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(520, 180);
        setLocationRelativeTo(statusFrame);
        setResizable(false);
        setLayout(new BorderLayout());

        buildUi();
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
        refreshAttributeOptions();
    }

    private void buildUi() {
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        attributeBox = new JComboBox<>(buildAttributeOptions());
        attributeBox.setEditable(true);
        installAttributeFilter();
        addRow(center, gbc, y++, "Attribute", attributeBox);

        severityField = new JFormattedTextField(NumberFormat.getNumberInstance());
        severityField.setValue(0.0);
        addRow(center, gbc, y++, "Severity", severityField);

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applyStatus());
        footer.add(cancel);
        footer.add(apply);
        add(footer, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, java.awt.Component field) {
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel(labelText + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.weightx = 0.0;
    }

    private String[] buildAttributeOptions() {
        attributeKeyByLabel.clear();
        List<String> values = new ArrayList<>();
        addPrefixed(values, CharAttributes.getAttributeKeys());
        addPrefixed(values, CharAttributes.getDefenseKeys());
        addPrefixed(values, CharAttributes.getDamageTypeKeys());
        addPrefixed(values, CharAttributes.getCombatKeys());
        addPrefixed(values, CharAttributes.getSecondaryKeys());
        addPrefixed(values, CharAttributes.getDamageKeys());
        for (String key : RESOURCE_KEYS) {
            addAttributeOption(values, key);
        }
        for (String key : MANUAL_INSTANT_RESOURCE_KEYS) {
            addAttributeOption(values, key);
        }
        addSkillOptions(values);
        addAttributeOption(values, StoreCharData.SIZE_ATTRIBUTE, "Size");
        allAttributeOptions.clear();
        allAttributeOptions.addAll(values);
        return values.toArray(String[]::new);
    }

    private void installAttributeFilter() {
        if (attributeBox == null) return;
        Component editorComponent = attributeBox.getEditor().getEditorComponent();
        if (!(editorComponent instanceof JTextField editorField)) return;
        editorField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleAttributeFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleAttributeFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleAttributeFilter();
            }

            private void scheduleAttributeFilter() {
                SwingUtilities.invokeLater(() -> filterAttributeOptions(editorField.getText()));
            }
        });
    }

    private void filterAttributeOptions(String typedText) {
        if (attributeBox == null || filteringAttributeOptions) return;
        filteringAttributeOptions = true;
        try {
            String typed = typedText == null ? "" : typedText;
            String filter = typed.trim().toLowerCase(Locale.ROOT);
            JTextField editorField = getAttributeEditorField();
            int caret = editorField == null ? typed.length() : editorField.getCaretPosition();

            attributeBox.hidePopup();
            attributeBox.removeAllItems();
            for (String option : allAttributeOptions) {
                if (attributeOptionMatches(option, filter)) {
                    attributeBox.addItem(option);
                }
            }

            attributeBox.getEditor().setItem(typed);
            if (editorField != null) {
                int maxCaret = editorField.getText() == null ? 0 : editorField.getText().length();
                editorField.setCaretPosition(Math.max(0, Math.min(caret, maxCaret)));
            }

            if (!filter.isEmpty() && attributeBox.getItemCount() > 0 && attributeBox.isShowing()) {
                attributeBox.showPopup();
            }
        } finally {
            filteringAttributeOptions = false;
        }
    }

    private boolean attributeOptionMatches(String option, String filterLower) {
        if (option == null) return false;
        if (filterLower == null || filterLower.isEmpty()) return true;
        if (option.toLowerCase(Locale.ROOT).contains(filterLower)) return true;
        String key = attributeKeyByLabel.get(option);
        return key != null && key.toLowerCase(Locale.ROOT).contains(filterLower);
    }

    private JTextField getAttributeEditorField() {
        if (attributeBox == null || attributeBox.getEditor() == null) return null;
        Component editorComponent = attributeBox.getEditor().getEditorComponent();
        return editorComponent instanceof JTextField field ? field : null;
    }

    private String getAttributeSelectionText() {
        if (attributeBox == null) return "";
        if (attributeBox.getEditor() != null) {
            Object editorItem = attributeBox.getEditor().getItem();
            if (editorItem != null && !editorItem.toString().isBlank()) {
                return editorItem.toString().trim();
            }
        }
        Object selected = attributeBox.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private void refreshAttributeOptions() {
        if (attributeBox == null) return;
        String previous = getAttributeSelectionText();
        buildAttributeOptions();
        filterAttributeOptions(previous);
        if (!previous.isBlank()) {
            attributeBox.getEditor().setItem(previous);
        }
    }

    private void addSkillOptions(List<String> values) {
        LinkedHashMap<String, String> skills = new LinkedHashMap<>();
        StoreRuleManager rules = new StoreRuleManager();
        List<DataSkill> catalog = rules.getSkillData();
        if (catalog != null) {
            for (DataSkill skill : catalog) {
                if (skill == null || skill.getName() == null || skill.getName().isBlank()) continue;
                String name = skill.getName().trim();
                skills.putIfAbsent(name, skillAttributeKey(name));
            }
        }
        if (character != null && character.getSpecials() != null && character.getSpecials().getSkills() != null) {
            for (DataSkill skill : character.getSpecials().getSkills()) {
                String display = CharSpecials.formatSkillDisplayName(skill);
                if (display.isBlank()) continue;
                skills.putIfAbsent(display, skillAttributeKey(display));
            }
        }
        ArrayList<String> names = new ArrayList<>(skills.keySet());
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        for (String name : names) {
            addAttributeOption(values, skills.get(name), "Skill: " + name);
        }
    }

    private void addPrefixed(List<String> values, String[] keys) {
        if (keys == null) return;
        for (String key : keys) {
            addAttributeOption(values, "B" + key);
            addAttributeOption(values, "M" + key);
        }
    }

    private void addAttributeOption(List<String> values, String key) {
        addAttributeOption(values, key, displayNameForAttributeKey(key));
    }

    private void addAttributeOption(List<String> values, String key, String label) {
        if (values == null || key == null || key.isBlank() || label == null || label.isBlank()) return;
        attributeKeyByLabel.put(label, key.toUpperCase(Locale.ROOT));
        values.add(label);
    }

    private String skillAttributeKey(String skillName) {
        return "BSKILL" + normalizeSkillToken(skillName);
    }

    private String normalizeSkillToken(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String resolveAttributeKey(String selected) {
        if (selected == null || selected.isBlank()) return "";
        String trimmed = selected.trim();
        String mapped = attributeKeyByLabel.get(trimmed);
        if (mapped != null) return mapped;
        for (Map.Entry<String, String> entry : attributeKeyByLabel.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed) || entry.getValue().equalsIgnoreCase(trimmed)) {
                return entry.getValue();
            }
        }
        String filterLower = trimmed.toLowerCase(Locale.ROOT);
        String uniqueKey = null;
        for (Map.Entry<String, String> entry : attributeKeyByLabel.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            boolean matches = entry.getKey().toLowerCase(Locale.ROOT).contains(filterLower)
                    || entry.getValue().toLowerCase(Locale.ROOT).contains(filterLower);
            if (!matches) continue;
            if (uniqueKey == null) {
                uniqueKey = entry.getValue();
            } else if (!uniqueKey.equals(entry.getValue())) {
                uniqueKey = null;
                break;
            }
        }
        if (uniqueKey != null) {
            return uniqueKey;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String displayNameForAttributeKey(String key) {
        if (key == null || key.isBlank()) return "";
        String normalized = key.trim().toUpperCase(Locale.ROOT);
        String resourceName = switch (normalized) {
            case "BASEHP" -> "Max HP";
            case "MULTIHP" -> "HP Multiplier";
            case "BASEAURA" -> "Max Aura";
            case "MULTIAURA" -> "Aura Multiplier";
            case "BASER1" -> "Resource 1";
            case "MULTIR1" -> "Resource 1 Multiplier";
            case "BASER2" -> "Resource 2";
            case "MULTIR2" -> "Resource 2 Multiplier";
            case "BASER3" -> "Resource 3";
            case "MULTIR3" -> "Resource 3 Multiplier";
            case "BASEREACT" -> "Reactions";
            case "MULTIREACT" -> "Reactions Multiplier";
            case "HP" -> "Current HP";
            case "AURA" -> "Current Aura";
            case "SPENTAURA" -> "Spent Aura";
            default -> null;
        };
        if (resourceName != null) return resourceName;
        if (normalized.startsWith("BSKILL") && normalized.length() > "BSKILL".length()) {
            return "Skill: " + normalized.substring("BSKILL".length());
        }
        if (normalized.startsWith("MSKILL") && normalized.length() > "MSKILL".length()) {
            return "Skill Multiplier: " + normalized.substring("MSKILL".length());
        }

        boolean multiplier = normalized.startsWith("M") && normalized.length() > 1;
        boolean base = normalized.startsWith("B") && normalized.length() > 1;
        String stem = (base || multiplier) ? normalized.substring(1) : normalized;
        String name = switch (stem) {
            case "STR" -> "Strength";
            case "DEX" -> "Dexterity";
            case "CON" -> "Constitution";
            case "FOC" -> "Focus";
            case "CTL" -> "Control";
            case "CAP" -> "Capacity";
            case "KNOW" -> "Knowledge";
            case "MECH" -> "Mechanical";
            case "PERC" -> "Perception";
            case "INT" -> "Intuition";
            case "CHA" -> "Charisma";
            case "SUB" -> "Subtlety";
            case "ARMOR" -> "Armor";
            case "DODGE" -> "Dodge";
            case "DEF" -> "Defense";
            case "FORT" -> "Fortitude";
            case "REF" -> "Reflex";
            case "WILL" -> "Will";
            case "AVOID" -> "Avoid";
            case "ALL" -> "Resist All";
            case "PHY" -> "Resist Physical";
            case "BLUNT" -> "Resist Blunt";
            case "PIERCE" -> "Resist Pierce";
            case "SLASH" -> "Resist Slash";
            case "FIRE" -> "Resist Fire";
            case "FROST" -> "Resist Frost";
            case "ELEC" -> "Resist Electricity";
            case "ENERGY" -> "Resist Energy";
            case "SONIC" -> "Resist Sonic";
            case "LIGHT" -> "Resist Light";
            case "TOXIC" -> "Resist Toxic";
            case "DARK" -> "Resist Dark";
            case "PSI" -> "Resist Psionic";
            case "SPIRIT" -> "Resist Spirit";
            case "TIME" -> "Resist Time";
            case "ATK" -> "Attack";
            case "APP" -> "Application";
            case "MOVE" -> "Move Speed";
            case "FLY" -> "Fly Speed";
            case "RANGE" -> "Range";
            case "INIT" -> "Initiative";
            case "MAN", "CMAN" -> "Combat Maneuvers";
            case "MAXATK" -> "Max Attacks";
            case "SUP" -> "Support";
            case "IMP" -> "Impairment";
            case "MAST" -> "Mastery";
            case "EXCL" -> "Exclusion Count";
            case "GRANT" -> "Grant";
            case "CRUSH" -> "Crush";
            case "BREAK" -> "Break";
            case "AREA" -> "Area Multiplier";
            case "POWER" -> "Power";
            case "DUR" -> "Duration";
            case "SHD" -> "Shielding";
            case "BDMG" -> "Base Damage";
            case "TDMG" -> "Total Damage";
            case "BHEAL" -> "Base Healing";
            case "THEAL" -> "Total Healing";
            case "CRIT" -> "Critical Increment";
            case "CRITDMG" -> "Critical Damage Multiplier";
            case "SIZE" -> "Size";
            case "RESDISP" -> "Displacement Resist";
            default -> stem;
        };
        return multiplier ? name + " Multiplier" : name;
    }

    private void applyStatus() {
        if (statusFrame == null) return;

        String attribute = resolveAttributeKey(getAttributeSelectionText());
        if (attribute.isBlank()) {
            JOptionPane.showMessageDialog(this, "Attribute is required.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusFrame.appendEffectToken(attribute, parseDouble(severityField));
        if (severityField != null) {
            severityField.setValue(0.0);
        }
        setVisible(false);
    }

    private double parseDouble(JFormattedTextField field) {
        if (field == null || field.getValue() == null) return 0.0;
        Object value = field.getValue();
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }
}
