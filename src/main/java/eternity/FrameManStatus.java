package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FrameManStatus extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String[] EXCLUDED_AFFINITIES = {
            "Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting"
    };
    private static final String[] EXCLUDED_DURATION_TYPES = { "Passive", "Temporary" };

    private static final String[] DURATION_TYPES = { "Passive", "Maintained", "Temporary", "Turn", "Round", "Cycle", "Next Attack" };
    private static final String[] RESOURCE_KEYS = {
            "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
            "BASER1", "MULTIR1", "BASER2", "MULTIR2", "BASER3", "MULTIR3",
            "BASEREACT", "MULTIREACT"
    };

    private final FrameCombat combatFrame;
    private final FrameSheet sheetFrame;
    private StoreCharData character;

    private JTextField nameField;
    private JComboBox<String> affinityBox;
    private JTextField descriptionField;
    private JComboBox<String> attributeBox;
    private JFormattedTextField severityField;
    private JComboBox<String> durationTypeBox;
    private JFormattedTextField durationField;

    public FrameManStatus(FrameCombat combatFrame, StoreCharData character) {
        this(combatFrame, null, character);
    }

    public FrameManStatus(FrameSheet sheetFrame, StoreCharData character) {
        this(null, sheetFrame, character);
    }

    private FrameManStatus(FrameCombat combatFrame, FrameSheet sheetFrame, StoreCharData character) {
        super("Apply Status");
        this.combatFrame = combatFrame;
        this.sheetFrame = sheetFrame;
        this.character = character;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(520, 360);
        setLocationRelativeTo(combatFrame != null ? combatFrame : sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout());

        buildUi();
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
    }

    private void buildUi() {
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addRow(center, gbc, y++, "Name", nameField = new JTextField("Combat Status"));
        affinityBox = new JComboBox<>(buildAffinityOptions());
        affinityBox.setSelectedItem("None");
        addRow(center, gbc, y++, "Affinity", affinityBox);
        addRow(center, gbc, y++, "Description", descriptionField = new JTextField(""));

        attributeBox = new JComboBox<>(buildAttributeOptions());
        attributeBox.setEditable(true);
        addRow(center, gbc, y++, "Attribute", attributeBox);

        severityField = new JFormattedTextField(NumberFormat.getNumberInstance());
        severityField.setValue(0.0);
        addRow(center, gbc, y++, "Severity", severityField);

        durationTypeBox = new JComboBox<>(buildDurationTypeOptions());
        durationTypeBox.addActionListener(e -> updateDurationFieldState());
        addRow(center, gbc, y++, "Duration Type", durationTypeBox);

        durationField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        durationField.setValue(1);
        addRow(center, gbc, y++, "Duration", durationField);

        JLabel note = new JLabel("Timed statuses use the Temporary stat bucket and are removed on Turn / Round / Cycle / Next Attack expiry.", SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        center.add(note, gbc);
        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applyStatus());
        footer.add(cancel);
        footer.add(apply);
        add(footer, BorderLayout.SOUTH);

        updateDurationFieldState();
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
        List<String> values = new ArrayList<>();
        addPrefixed(values, "B", CharAttributes.getAttributeKeys());
        addPrefixed(values, "M", CharAttributes.getAttributeKeys());
        addPrefixed(values, "B", CharAttributes.getDefenseKeys());
        addPrefixed(values, "M", CharAttributes.getDefenseKeys());
        addPrefixed(values, "B", CharAttributes.getDamageTypeKeys());
        addPrefixed(values, "M", CharAttributes.getDamageTypeKeys());
        addPrefixed(values, "B", CharAttributes.getCombatKeys());
        addPrefixed(values, "M", CharAttributes.getCombatKeys());
        addPrefixed(values, "B", CharAttributes.getSecondaryKeys());
        addPrefixed(values, "M", CharAttributes.getSecondaryKeys());
        addPrefixed(values, "B", CharAttributes.getDamageKeys());
        addPrefixed(values, "M", CharAttributes.getDamageKeys());
        for (String key : RESOURCE_KEYS) {
            values.add(key);
        }
        return values.toArray(String[]::new);
    }

    private String[] buildAffinityOptions() {
        List<String> values = new ArrayList<>();
        for (String affinity : FrameTrainingExp.AURA_TYPES) {
            if (affinity == null || affinity.isBlank()) continue;
            if (isExcludedAffinity(affinity)) continue;
            values.add(affinity);
        }
        return values.toArray(String[]::new);
    }

    private String[] buildDurationTypeOptions() {
        List<String> values = new ArrayList<>();
        for (String durationType : DURATION_TYPES) {
            if (durationType == null || durationType.isBlank()) continue;
            if (isExcludedDurationType(durationType)) continue;
            values.add(durationType);
        }
        return values.toArray(String[]::new);
    }

    private boolean isExcludedAffinity(String affinity) {
        if (affinity == null) return false;
        String normalized = affinity.trim().toUpperCase(Locale.ROOT);
        for (String excluded : EXCLUDED_AFFINITIES) {
            if (excluded != null && normalized.equals(excluded.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcludedDurationType(String durationType) {
        if (durationType == null) return false;
        String normalized = durationType.trim().toUpperCase(Locale.ROOT);
        for (String excluded : EXCLUDED_DURATION_TYPES) {
            if (excluded != null && normalized.equals(excluded.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void addPrefixed(List<String> values, String prefix, String[] keys) {
        for (String key : keys) {
            values.add(prefix + key);
        }
    }

    private void updateDurationFieldState() {
        String durationType = getSelectedDurationType();
        boolean timed = isTimedDuration(durationType);
        durationField.setEditable(timed);
        durationField.setEnabled(timed);
        if (!timed) {
            durationField.setValue(0);
        } else if (parseInteger(durationField) <= 0) {
            durationField.setValue(1);
        }
    }

    private void applyStatus() {
        if (character == null) return;

        String name = safeText(nameField);
        String attribute = safeSelected(attributeBox).toUpperCase();
        String durationType = getSelectedDurationType();
        if (name.isBlank() || attribute.isBlank()) {
            JOptionPane.showMessageDialog(this, "Name and Attribute are required.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DataStatus status = new DataStatus();
        status.setName(name);
        status.setAffinity(safeSelected(affinityBox));
        status.setDescription(safeText(descriptionField));
        status.setAttribute(attribute);
        status.setSeverity(parseDouble(severityField));
        status.setDurationType(durationType);
        status.setDuration(isTimedDuration(durationType) ? Math.max(1, parseInteger(durationField)) : 0);

        if (!applyBuiltStatus(status)) {
            JOptionPane.showMessageDialog(this, "Unable to apply that status to the character.", "Status Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (severityField != null) severityField.setValue(0.0);
        if (durationField != null && isTimedDuration(durationType)) durationField.setValue(1);
        setVisible(false);
    }

    private boolean isTimedDuration(String durationType) {
        return "Turn".equalsIgnoreCase(durationType)
                || "Round".equalsIgnoreCase(durationType)
                || "Cycle".equalsIgnoreCase(durationType)
                || "Next Attack".equalsIgnoreCase(durationType);
    }

    private String getSelectedDurationType() {
        return safeSelected(durationTypeBox);
    }

    private String safeText(JTextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String safeSelected(JComboBox<String> box) {
        Object value = box == null ? null : box.getSelectedItem();
        return value == null ? "" : value.toString().trim();
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

    private int parseInteger(JFormattedTextField field) {
        if (field == null || field.getValue() == null) return 0;
        Object value = field.getValue();
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private boolean applyBuiltStatus(DataStatus status) {
        if (status == null || character == null || status.getAttribute() == null || status.getAttribute().isBlank()) {
            return false;
        }
        if (combatFrame != null) {
            return combatFrame.applyBuiltStatus(status);
        }

        DataStatus applied = new DataStatus(status);
        String originalDurationType = applied.getDurationType();
        boolean tokenStatus = isStunAttribute(applied.getAttribute()) || isHeavyAttribute(applied.getAttribute());
        if (isTimedDuration(originalDurationType)) {
            applied.setDurationType("Temporary");
        }

        boolean appliedOk = applyStatusToCharacter(applied);
        if (!appliedOk) {
            return false;
        }

        if (isTimedDuration(originalDurationType) && !tokenStatus && character.getCombat() != null) {
            character.getCombat().addStatus(new DataStatus(status));
        }

        if (sheetFrame != null) {
            character.updateAll();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }
        return true;
    }

    private boolean applyStatusToCharacter(DataStatus status) {
        if (status == null || character == null || status.getAttribute() == null) return false;
        String attribute = status.getAttribute().toUpperCase();
        if (isInstantResourceDeltaAttribute(attribute)) {
            return applyInstantResourceDelta(status);
        }
        if (isStunAttribute(attribute)) {
            if (character.getCombat() == null) return false;
            character.getCombat().adjustStunTokens((int)Math.round(status.getSeverity()));
            return true;
        }
        if (isHeavyAttribute(attribute)) {
            if (character.getCombat() == null) return false;
            character.getCombat().adjustHeavyTokens((int)Math.round(status.getSeverity()));
            return true;
        }
        try {
            if (isResourceAttribute(attribute)) {
                character.getResources().addStatus(status);
            } else {
                character.getAttributes().addStatus(status);
            }
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean isResourceAttribute(String attribute) {
        if (attribute == null) return false;
        for (String key : RESOURCE_KEYS) {
            if (key.equalsIgnoreCase(attribute)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStunAttribute(String attribute) {
        return attribute != null && "STUN".equalsIgnoreCase(attribute.trim());
    }

    private boolean isHeavyAttribute(String attribute) {
        return attribute != null && "HEAVY".equalsIgnoreCase(attribute.trim());
    }

    private boolean isInstantResourceDeltaAttribute(String attribute) {
        return attribute != null
                && ("HP".equalsIgnoreCase(attribute.trim()) || "AURA".equalsIgnoreCase(attribute.trim()));
    }

    private boolean applyInstantResourceDelta(DataStatus status) {
        if (status == null || character == null || character.getResources() == null || status.getAttribute() == null) {
            return false;
        }
        String attribute = status.getAttribute().trim();
        double severity = status.getSeverity();
        if ("HP".equalsIgnoreCase(attribute)) {
            double maxHp = Math.max(0.0, character.getResources().calcMaxHP());
            double newLostHp = clamp(character.getResources().getLostHP() - severity, 0.0, maxHp);
            character.getResources().setLostHP(newLostHp);
            return true;
        }
        if ("AURA".equalsIgnoreCase(attribute)) {
            double maxAura = Math.max(0.0, character.getResources().calcMaxAura());
            double newSpentAura = clamp(character.getResources().getSpentAura() - severity, 0.0, maxAura);
            character.getResources().setSpentAura(newSpentAura);
            return true;
        }
        return false;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
