package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import eternity.DataTypes.DataStatus;

public class FrameStatus extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.##");
    private static final String[] RESOURCE_KEYS = {
            "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
            "BASER1", "MULTIR1", "BASER2", "MULTIR2", "BASER3", "MULTIR3",
            "BASEREACT", "MULTIREACT"
    };
    private static final String[] EXCLUDED_AFFINITIES = {
            "Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting"
    };
    private static final String[] DURATION_TYPES = {
            "Passive", "Maintained", "Temporary", "Turn", "Round", "Cycle", "Next Attack"
    };
    private static final String[] EXCLUDED_DURATION_TYPES = { "Passive", "Temporary" };
    private static final String TRAINING_STATUS_PREFIX = "Training Passive: ";
    private static final String MAINTAINED_STATUS_PREFIX = "Panel Maintained: ";

    private final FrameCombat combatFrame;
    private final FrameSheet sheetFrame;
    private StoreCharData character;

    private JTextArea codeArea;
    private JTextArea previewArea;
    private JTextField titleField;
    private JComboBox<String> affinityBox;
    private JComboBox<String> durationTypeBox;
    private JFormattedTextField durationField;
    private JLabel statusCountValueLabel;
    private StatusCodeParseResult lastParseResult;
    private FrameManStatus manualStatusFrame;
    private boolean updatingFromCode;

    public FrameStatus(FrameCombat combatFrame, StoreCharData character) {
        this(combatFrame, null, character);
    }

    public FrameStatus(FrameSheet sheetFrame, StoreCharData character) {
        this(null, sheetFrame, character);
    }

    private FrameStatus(FrameCombat combatFrame, FrameSheet sheetFrame, StoreCharData character) {
        super("Apply Status Code");
        this.combatFrame = combatFrame;
        this.sheetFrame = sheetFrame;
        this.character = character;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(620, 500);
        setLocationRelativeTo(combatFrame != null ? combatFrame : sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout());

        buildUi();
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
    }

    public String getEffectTitle() {
        return titleField == null || titleField.getText() == null ? "" : titleField.getText().trim();
    }

    public String getEffectAffinity() {
        if (affinityBox == null || affinityBox.getSelectedItem() == null) {
            return "None";
        }
        String affinity = affinityBox.getSelectedItem().toString().trim();
        return affinity.isBlank() ? "None" : affinity;
    }

    public String getEffectDurationType() {
        if (durationTypeBox == null || durationTypeBox.getSelectedItem() == null) {
            return "Maintained";
        }
        String durationType = durationTypeBox.getSelectedItem().toString().trim();
        return durationType.isBlank() ? "Maintained" : durationType;
    }

    public int getEffectDuration() {
        if (!isTimedDuration(getEffectDurationType())) {
            return 0;
        }
        return Math.max(1, parseInteger(durationField));
    }

    public void appendEffectToken(String attribute, double severity) {
        if (codeArea == null || attribute == null || attribute.isBlank()) {
            return;
        }
        String alias = StatusCodeParser.getPreferredAttributeAlias(attribute);
        if (alias == null || alias.isBlank()) {
            alias = attribute.trim().toUpperCase(Locale.ROOT);
        }
        String token = alias + ":" + formatSignedNumber(severity);
        String current = codeArea.getText() == null ? "" : codeArea.getText().trim();
        if (current.isBlank()) {
            codeArea.setText(token);
        } else if (current.endsWith("_") || current.endsWith("|")) {
            codeArea.setText(current + token);
        } else {
            codeArea.setText(current + "_" + token);
        }
        codeArea.setCaretPosition(codeArea.getText().length());
    }

    private void buildUi() {
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        JLabel header = new JLabel("Status Shorthand Preview / Apply", SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = y++;
        gbc.gridwidth = 2;
        center.add(header, gbc);

        codeArea = new JTextArea(4, 40);
        codeArea.setLineWrap(true);
        codeArea.setWrapStyleWord(true);
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onCodeChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onCodeChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onCodeChanged();
            }
        });
        JScrollPane codePane = new JScrollPane(codeArea);
        addRow(center, gbc, y++, "Code", codePane);

        titleField = new JTextField();
        addRow(center, gbc, y++, "Title", titleField);
        affinityBox = new JComboBox<>(buildAffinityOptions());
        affinityBox.setSelectedItem("None");
        addRow(center, gbc, y++, "Affinity", affinityBox);
        durationTypeBox = new JComboBox<>(buildDurationTypeOptions());
        durationTypeBox.setSelectedItem("Maintained");
        durationTypeBox.addActionListener(e -> updateDurationFieldState());
        addRow(center, gbc, y++, "Duration Type", durationTypeBox);
        durationField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        durationField.setValue(1);
        addRow(center, gbc, y++, "Duration", durationField);
        statusCountValueLabel = new JLabel("0");
        addRow(center, gbc, y++, "Statuses", statusCountValueLabel);

        previewArea = new JTextArea(12, 40);
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        JScrollPane previewPane = new JScrollPane(previewArea);
        addRow(center, gbc, y++, "Preview", previewPane);

        JLabel note = new JLabel(
                "<html>Format: <code>BATK:+2_BDODGE:+1_DUR:Turn:1_NAME:Guarded</code><br>"
                        + "Short form: <code>AT+2_DG+1</code> defaults to Maintained.<br>"
                        + "Packed form: <code>NMABCGRANT_ATP2DGN1</code> uses a compact name token plus alphanumeric status tokens.<br>"
                        + "Apply affects the currently loaded character.</html>",
                SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        center.add(note, gbc);

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton applied = new JButton("View Applied");
        applied.addActionListener(e -> showAppliedStatuses());
        JButton remove = new JButton("Remove Effect");
        remove.addActionListener(e -> showRemoveEffectDialog());
        JButton manual = new JButton("Add Effect");
        manual.addActionListener(e -> openManualStatusFrame());
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applyStatusCode());
        leftFooter.add(cancel);
        leftFooter.add(applied);
        rightFooter.add(remove);
        rightFooter.add(manual);
        rightFooter.add(apply);
        footer.add(leftFooter, BorderLayout.WEST);
        footer.add(rightFooter, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        updateDurationFieldState();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, java.awt.Component field) {
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(labelText + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = field instanceof JScrollPane ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        if (field instanceof JScrollPane) {
            gbc.weighty = "Code".equals(labelText) ? 0.2 : 1.0;
        } else {
            gbc.weighty = 0.0;
        }
        panel.add(field, gbc);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
    }

    private void onCodeChanged() {
        if (updatingFromCode) return;
        previewStatusCode(true);
    }

    private void previewStatusCode() {
        previewStatusCode(false);
    }

    private void previewStatusCode(boolean updateFieldsFromCode) {
        StatusCodeParseResult parseResult = parseCurrentCode();
        lastParseResult = parseResult;
        if (parseResult == null || !parseResult.isSuccess()) {
            showParseErrors(parseResult);
            return;
        }

        DecodedEffect effect = parseResult.getEffect();
        if (updateFieldsFromCode) {
            updateTitleFromEffect(effect);
            updateAffinityFromEffect(effect);
            updateDurationFromEffect(effect);
        }
        applyAffinityOverride(effect);
        applyDurationOverride(effect);
        statusCountValueLabel.setText(Integer.toString(effect.getStatuses().size()));
        previewArea.setText(buildPreviewText(effect));
        previewArea.setCaretPosition(0);
    }

    private void updateTitleFromEffect(DecodedEffect effect) {
        if (titleField == null) return;
        String title = effect == null || effect.getTitle() == null ? "" : effect.getTitle().trim();
        if (title.isBlank()) return;
        updatingFromCode = true;
        try {
            titleField.setText(title);
        } finally {
            updatingFromCode = false;
        }
    }

    private void updateAffinityFromEffect(DecodedEffect effect) {
        if (affinityBox == null || effect == null) return;
        String affinity = effect.getAffinity() == null ? "" : effect.getAffinity().trim();
        if (affinity.isBlank()) return;
        updatingFromCode = true;
        try {
            affinityBox.setSelectedItem(affinity);
            if (affinityBox.getSelectedItem() == null
                    || !affinity.equalsIgnoreCase(affinityBox.getSelectedItem().toString())) {
                affinityBox.addItem(affinity);
                affinityBox.setSelectedItem(affinity);
            }
        } finally {
            updatingFromCode = false;
        }
    }

    private void applyStatusCode() {
        if (character == null) return;

        StatusCodeParseResult parseResult = parseCurrentCode();
        lastParseResult = parseResult;
        if (parseResult == null || !parseResult.isSuccess()) {
            showParseErrors(parseResult);
            return;
        }

        DecodedEffect effect = parseResult.getEffect();
        applyTitleOverride(effect);
        applyAffinityOverride(effect);
        applyDurationOverride(effect);
        removeObsoleteGrantStatuses(effect);
        int appliedCount = 0;
        for (DataStatus status : effect.getStatuses()) {
            if (!applyBuiltStatus(new DataStatus(status))) {
                JOptionPane.showMessageDialog(this,
                        "Unable to apply decoded status " + status.getName() + ".",
                        "Status Failed",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            appliedCount++;
        }

        previewStatusCode();
        JOptionPane.showMessageDialog(this,
                "Applied " + appliedCount + " decoded status" + (appliedCount == 1 ? "" : "es") + " to the current character.",
                "Statuses Applied",
                JOptionPane.INFORMATION_MESSAGE);
        setVisible(false);
    }

    private void applyTitleOverride(DecodedEffect effect) {
        if (effect == null || titleField == null) return;
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isBlank()) return;

        effect.setTitle(title);
        for (DataStatus status : effect.getStatuses()) {
            if (status == null) continue;
            String attribute = status.getAttribute() == null ? "" : status.getAttribute().trim();
            if (attribute.isBlank()) {
                status.setName(title);
            } else {
                status.setName(title + " [" + attribute + "]");
            }
        }
    }

    private void applyAffinityOverride(DecodedEffect effect) {
        if (effect == null) return;
        String affinity = getEffectAffinity();
        effect.setAffinity(affinity);
        for (DataStatus status : effect.getStatuses()) {
            if (status != null) {
                status.setAffinity(affinity);
            }
        }
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

    private void updateDurationFromEffect(DecodedEffect effect) {
        if (durationTypeBox == null || effect == null || !effect.isDurationSpecified()) {
            return;
        }
        String durationType = effect.getDurationType() == null ? "" : effect.getDurationType().trim();
        if (durationType.isBlank()) return;
        updatingFromCode = true;
        try {
            durationTypeBox.setSelectedItem(durationType);
            if (durationTypeBox.getSelectedItem() == null
                    || !durationType.equalsIgnoreCase(durationTypeBox.getSelectedItem().toString())) {
                durationTypeBox.addItem(durationType);
                durationTypeBox.setSelectedItem(durationType);
            }
            if (durationField != null) {
                if (isTimedDuration(durationType)) {
                    durationField.setValue(Math.max(1, effect.getDuration()));
                } else {
                    durationField.setValue(0);
                }
            }
            updateDurationFieldState();
        } finally {
            updatingFromCode = false;
        }
    }

    private void applyDurationOverride(DecodedEffect effect) {
        if (effect == null) return;
        String durationType = getEffectDurationType();
        int duration = getEffectDuration();
        effect.setDurationType(durationType);
        effect.setDuration(duration);
        for (DataStatus status : effect.getStatuses()) {
            if (status == null || status.getAttribute() == null) continue;
            if (isInstantResourceDeltaAttribute(status.getAttribute())) {
                status.setDurationType("Instant");
                status.setDuration(0);
                continue;
            }
            status.setDurationType(durationType);
            status.setDuration(duration);
        }
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

    private void updateDurationFieldState() {
        if (durationField == null) return;
        String durationType = getEffectDurationType();
        boolean timed = isTimedDuration(durationType);
        durationField.setEditable(timed);
        durationField.setEnabled(timed);
        if (!timed) {
            durationField.setValue(0);
        } else if (parseInteger(durationField) <= 0) {
            durationField.setValue(1);
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

    private StatusCodeParseResult parseCurrentCode() {
        resetPreviewIfCodeBlank();
        String code = codeArea == null ? "" : codeArea.getText();
        return StatusCodeParser.parse(code);
    }

    private void resetPreviewIfCodeBlank() {
        if (codeArea == null || codeArea.getText() == null || codeArea.getText().isBlank()) {
            updatingFromCode = true;
            try {
                if (titleField != null) {
                    titleField.setText("");
                }
            } finally {
                updatingFromCode = false;
            }
            statusCountValueLabel.setText("0");
            previewArea.setText("");
        }
    }

    private void showParseErrors(StatusCodeParseResult parseResult) {
        statusCountValueLabel.setText("0");
        if (codeArea == null || codeArea.getText() == null || codeArea.getText().isBlank()) {
            updatingFromCode = true;
            try {
                if (titleField != null) {
                    titleField.setText("");
                }
            } finally {
                updatingFromCode = false;
            }
            previewArea.setText("");
            return;
        }
        if (parseResult == null || parseResult.getErrors().isEmpty()) {
            previewArea.setText("Unable to parse status code.");
            return;
        }

        StringBuilder errorText = new StringBuilder("Parse errors:");
        for (String error : parseResult.getErrors()) {
            errorText.append(System.lineSeparator()).append("- ").append(error);
        }
        previewArea.setText(errorText.toString());
        previewArea.setCaretPosition(0);
    }

    private String buildPreviewText(DecodedEffect effect) {
        if (effect == null || !effect.hasStatuses()) {
            return "No statuses decoded.";
        }

        StringBuilder text = new StringBuilder();
        text.append("Original Code: ").append(effect.getOriginalCode()).append(System.lineSeparator());
        String title = getEffectTitle();
        if (title.isBlank()) {
            title = effect.getTitle() == null ? "" : effect.getTitle().trim();
        }
        if (!title.isBlank()) {
            text.append("Title: ").append(title).append(System.lineSeparator());
        }
        text.append("Affinity: ").append(getEffectAffinity()).append(System.lineSeparator());
        text.append("Duration: ").append(getEffectDurationType());
        if (getEffectDuration() > 0) {
            text.append(" (").append(getEffectDuration()).append(")");
        }
        text.append(System.lineSeparator());
        text.append(System.lineSeparator());

        Map<String, List<DataStatus>> grantStatusesByFamily = groupGrantStatusesByFamily(effect.getStatuses());
        if (!grantStatusesByFamily.isEmpty()) {
            appendGrantPreview(text, grantStatusesByFamily);
        }

        List<DataStatus> standardStatuses = collectNonGrantStatuses(effect.getStatuses());
        if (!standardStatuses.isEmpty()) {
            if (text.charAt(text.length() - 1) != System.lineSeparator().charAt(0)) {
                text.append(System.lineSeparator());
            }
            appendStandardPreview(text, standardStatuses);
        }

        return text.toString().trim();
    }

    private void appendGrantPreview(StringBuilder text, Map<String, List<DataStatus>> grantStatusesByFamily) {
        text.append("Grant Replacement Preview").append(System.lineSeparator());
        text.append(System.lineSeparator());

        int index = 1;
        for (Map.Entry<String, List<DataStatus>> entry : grantStatusesByFamily.entrySet()) {
            String grantFamily = entry.getKey();
            List<DataStatus> incomingStatuses = entry.getValue();
            List<DataStatus> existingStatuses = collectActiveStatusesByGrantFamily(grantFamily);
            Map<String, Double> existingTotals = sumStatusSeverityByAttribute(existingStatuses);
            Map<String, Double> incomingTotals = sumStatusSeverityByAttribute(incomingStatuses);
            Set<String> attributes = new LinkedHashSet<>();
            attributes.addAll(existingTotals.keySet());
            attributes.addAll(incomingTotals.keySet());

            text.append(index++).append(". ").append(grantFamily).append(System.lineSeparator());
            text.append("Existing active statuses with this name are replaced by the incoming code.")
                    .append(System.lineSeparator());
            if (attributes.isEmpty()) {
                text.append("No matching active or incoming statuses.").append(System.lineSeparator());
            } else {
                for (String attribute : attributes) {
                    double existing = existingTotals.getOrDefault(attribute, 0.0);
                    double incoming = incomingTotals.getOrDefault(attribute, 0.0);
                    double difference = incoming - existing;
                    text.append(attribute)
                            .append(": ")
                            .append(formatNumber(existing))
                            .append(" -> ")
                            .append(formatNumber(incoming))
                            .append(" (")
                            .append(formatSignedNumber(difference))
                            .append(")")
                            .append(System.lineSeparator());
                }
            }
            text.append(System.lineSeparator());
        }
    }

    private void openManualStatusFrame() {
        if (manualStatusFrame == null) {
            manualStatusFrame = new FrameManStatus(this, character);
        } else {
            manualStatusFrame.updateCharacter(character);
        }
        manualStatusFrame.setVisible(true);
    }

    private void showRemoveEffectDialog() {
        DefaultListModel<AppliedStatusGroup> listModel = new DefaultListModel<>();
        Map<String, List<DataStatus>> grouped = groupAppliedStatuses(collectAppliedStatuses());
        for (Map.Entry<String, List<DataStatus>> entry : grouped.entrySet()) {
            listModel.addElement(new AppliedStatusGroup(entry.getKey(), entry.getValue()));
        }

        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No applied status effects are available to remove.",
                    "Remove Effect",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<AppliedStatusGroup> statusList = new JList<>(listModel);
        statusList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        statusList.setSelectedIndex(0);
        JScrollPane listPane = new JScrollPane(statusList);
        listPane.setPreferredSize(new Dimension(320, 220));

        JDialog dialog = new JDialog(this, "Remove Effect", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(0, 8));
        dialog.add(new JLabel("Select a status effect to remove:"), BorderLayout.NORTH);
        dialog.add(listPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(event -> dialog.dispose());
        JButton delete = new JButton("Delete");
        delete.addActionListener(event -> {
            AppliedStatusGroup selected = statusList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Select a status effect to delete.",
                        "Remove Effect",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            deleteAppliedStatusGroup(selected);
            dialog.dispose();
        });
        footer.add(cancel);
        footer.add(delete);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAppliedStatuses() {
        DefaultListModel<AppliedStatusGroup> listModel = new DefaultListModel<>();
        JList<AppliedStatusGroup> statusList = new JList<>(listModel);
        statusList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        Runnable reload = () -> {
            int selectedIndex = statusList.getSelectedIndex();
            listModel.clear();
            Map<String, List<DataStatus>> grouped = groupAppliedStatuses(collectAppliedStatuses());
            for (Map.Entry<String, List<DataStatus>> entry : grouped.entrySet()) {
                listModel.addElement(new AppliedStatusGroup(entry.getKey(), entry.getValue()));
            }
            if (listModel.isEmpty()) {
                detailsArea.setText("No statuses have been applied by status codes or manually.");
                return;
            }
            if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
                statusList.setSelectedIndex(selectedIndex);
            } else {
                statusList.setSelectedIndex(0);
            }
        };

        statusList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) return;
            AppliedStatusGroup selected = statusList.getSelectedValue();
            detailsArea.setText(selected == null
                    ? "No statuses have been applied by status codes or manually."
                    : buildAppliedStatusText(selected.statuses));
            detailsArea.setCaretPosition(0);
        });

        JScrollPane listPane = new JScrollPane(statusList);
        listPane.setPreferredSize(new Dimension(180, 360));
        JScrollPane detailsPane = new JScrollPane(detailsArea);
        detailsPane.setPreferredSize(new Dimension(340, 360));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPane, detailsPane);
        split.setResizeWeight(0.35);

        JDialog dialog = new JDialog(this, "Applied Statuses", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(0, 8));
        dialog.add(split, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(event -> dialog.dispose());
        footer.add(close);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);
        reload.run();
        dialog.setVisible(true);
    }

    private Map<String, List<DataStatus>> groupAppliedStatuses(List<DataStatus> statuses) {
        LinkedHashMap<String, List<DataStatus>> grouped = new LinkedHashMap<>();
        if (statuses == null) return grouped;
        for (DataStatus status : statuses) {
            if (status == null) continue;
            String groupName = extractStatusBaseName(status.getName());
            if (groupName.isBlank()) {
                groupName = status.getName() == null ? "Unnamed" : status.getName().trim();
            }
            grouped.computeIfAbsent(groupName, ignored -> new ArrayList<>()).add(status);
        }
        return grouped;
    }

    private void deleteAppliedStatusGroup(AppliedStatusGroup group) {
        if (group == null || group.name == null || group.name.isBlank()) return;

        for (DataStatus status : collectActiveCharacterStatuses()) {
            if (!isAppliedStatus(status)) continue;
            if (!group.name.equalsIgnoreCase(extractStatusBaseName(status.getName()))) continue;
            removeActiveStatus(status);
        }

        CharCombat combat = resolveCombat();
        if (combat != null && combat.getCombatStatus() != null) {
            List<DataStatus> tracked = new ArrayList<>(combat.getCombatStatus());
            for (DataStatus status : tracked) {
                if (status == null) continue;
                if (!group.name.equalsIgnoreCase(extractStatusBaseName(status.getName()))) continue;
                reverseTokenStatus(status);
            }
            combat.getCombatStatus().removeIf(status ->
                    status != null && group.name.equalsIgnoreCase(extractStatusBaseName(status.getName())));
        }

        if (combatFrame != null) {
            combatFrame.removeBattleStatusByBaseName(group.name);
        }

        refreshAfterAppliedStatusChange();
    }

    private void reverseTokenStatus(DataStatus status) {
        if (status == null || status.getAttribute() == null) return;
        CharCombat combat = resolveCombat();
        if (combat == null) return;
        String attribute = status.getAttribute().toUpperCase();
        int amount = (int) Math.round(status.getSeverity());
        if (isStunAttribute(attribute)) {
            combat.adjustStunTokens(-amount);
        } else if (isHeavyAttribute(attribute)) {
            combat.adjustHeavyTokens(-amount);
        } else if (isIncapacitateAttribute(attribute)) {
            combat.adjustIncapacitateTokens(-amount);
        } else if (isRootAttribute(attribute)) {
            combat.adjustRootTokens(-amount);
        }
    }

    private void refreshAfterAppliedStatusChange() {
        if (character != null) {
            character.updateAll();
        }
        if (combatFrame != null) {
            combatFrame.refreshAfterStatusChange();
        } else if (sheetFrame != null) {
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }
    }

    private List<DataStatus> collectAppliedStatuses() {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        for (DataStatus status : collectActiveCharacterStatuses()) {
            if (!isAppliedStatus(status)) continue;
            statuses.add(status);
        }

        CharCombat combat = resolveCombat();
        if (combat != null && combat.getCombatStatus() != null) {
            for (DataStatus tracked : combat.getCombatStatus()) {
                if (!isAppliedStatus(tracked)) continue;
                DataStatus existing = findMatchingAppliedStatus(statuses, tracked);
                if (existing != null) {
                    existing.setDurationType(tracked.getDurationType());
                    existing.setDuration(tracked.getDuration());
                } else if (isCombatMarkerAttribute(tracked.getAttribute())) {
                    statuses.add(new DataStatus(tracked));
                }
            }
        }
        return statuses;
    }

    private boolean isAppliedStatus(DataStatus status) {
        if (status == null || status.getName() == null || status.getName().isBlank()) return false;
        String name = status.getName().trim();
        if ("Base".equalsIgnoreCase(name)) return false;
        if (name.regionMatches(true, 0, TRAINING_STATUS_PREFIX, 0, TRAINING_STATUS_PREFIX.length())) return false;
        if (name.regionMatches(true, 0, MAINTAINED_STATUS_PREFIX, 0, MAINTAINED_STATUS_PREFIX.length())) return false;
        String duration = status.getDurationType();
        if (duration != null
                && ("Passive".equalsIgnoreCase(duration)
                || "Permanent".equalsIgnoreCase(duration)
                || "Domain".equalsIgnoreCase(duration)
                || "Combat".equalsIgnoreCase(duration))) {
            return false;
        }
        return true;
    }

    private DataStatus findMatchingAppliedStatus(List<DataStatus> statuses, DataStatus candidate) {
        if (statuses == null || candidate == null) return null;
        String name = normalizeStatusName(candidate.getName());
        String attribute = candidate.getAttribute() == null ? "" : candidate.getAttribute().trim();
        for (DataStatus status : statuses) {
            if (status == null) continue;
            String statusAttribute = status.getAttribute() == null ? "" : status.getAttribute().trim();
            if (name.equalsIgnoreCase(normalizeStatusName(status.getName()))
                    && attribute.equalsIgnoreCase(statusAttribute)) {
                return status;
            }
        }
        return null;
    }

    private String buildAppliedStatusText(List<DataStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "No statuses have been applied by status codes or manually.";
        }

        Map<String, List<DataStatus>> grouped = new LinkedHashMap<>();
        for (DataStatus status : statuses) {
            String groupName = extractStatusBaseName(status.getName());
            if (groupName.isBlank()) {
                groupName = status.getName().trim();
            }
            grouped.computeIfAbsent(groupName, ignored -> new ArrayList<>()).add(status);
        }

        StringBuilder text = new StringBuilder();
        int index = 1;
        for (Map.Entry<String, List<DataStatus>> entry : grouped.entrySet()) {
            text.append(index++).append(". ").append(entry.getKey()).append(System.lineSeparator());
            for (DataStatus status : entry.getValue()) {
                text.append("   ").append(formatAttributeDisplay(status.getAttribute()))
                        .append("  ").append(formatSignedNumber(status.getSeverity()))
                        .append("  ").append(status.getDurationType() == null ? "None" : status.getDurationType());
                if (status.getDuration() > 0) {
                    text.append(" (").append(status.getDuration()).append(")");
                }
                text.append(System.lineSeparator());
                if (status.getDescription() != null && !status.getDescription().isBlank()) {
                    text.append("   ").append(status.getDescription()).append(System.lineSeparator());
                }
            }
            text.append(System.lineSeparator());
        }
        return text.toString().trim();
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
        boolean reminderStatus = character.isReminderStatus(applied);
        boolean combatMarkerStatus = isCombatMarkerAttribute(applied.getAttribute());
        if (isTimedDuration(originalDurationType)) {
            applied.setDurationType("Temporary");
        }

        boolean appliedOk = combatMarkerStatus || applyStatusToCharacter(applied);
        if (!appliedOk) {
            return false;
        }

        if ((reminderStatus || combatMarkerStatus || isTimedDuration(originalDurationType)) && resolveCombat() != null) {
            resolveCombat().addStatus(new DataStatus(status));
        }

        if (sheetFrame != null) {
            character.updateAll();
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshImagePanel();
        }
        return true;
    }

    private CharCombat resolveCombat() {
        if (combatFrame != null && combatFrame.getActiveCombat() != null) {
            return combatFrame.getActiveCombat();
        }
        return character == null ? null : character.getCombat();
    }

    private boolean applyStatusToCharacter(DataStatus status) {
        if (status == null || character == null || status.getAttribute() == null) return false;
        String attribute = status.getAttribute().toUpperCase();
        if (isInstantResourceDeltaAttribute(attribute)) {
            return applyInstantResourceDelta(status);
        }
        CharCombat combat = resolveCombat();
        if (isStunAttribute(attribute)) {
            if (combat == null) return false;
            combat.adjustStunTokens((int)Math.round(status.getSeverity()));
            return true;
        }
        if (isHeavyAttribute(attribute)) {
            if (combat == null) return false;
            combat.adjustHeavyTokens((int)Math.round(status.getSeverity()));
            return true;
        }
        if (isIncapacitateAttribute(attribute)) {
            if (combat == null) return false;
            combat.adjustIncapacitateTokens((int)Math.round(status.getSeverity()));
            return true;
        }
        if (isRootAttribute(attribute)) {
            if (combat == null) return false;
            combat.adjustRootTokens((int)Math.round(status.getSeverity()));
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

    private boolean isTimedDuration(String durationType) {
        return durationType != null
                && ("TURN".equalsIgnoreCase(durationType)
                || "ROUND".equalsIgnoreCase(durationType)
                || "CYCLE".equalsIgnoreCase(durationType)
                || "NEXT ATTACK".equalsIgnoreCase(durationType));
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

    private boolean isIncapacitateAttribute(String attribute) {
        return attribute != null && "INCAP".equalsIgnoreCase(attribute.trim());
    }

    private boolean isRootAttribute(String attribute) {
        return attribute != null && "ROOT".equalsIgnoreCase(attribute.trim());
    }

    private boolean isCombatMarkerAttribute(String attribute) {
        if (attribute == null) return false;
        String normalized = attribute.trim();
        return "STEALTHSTRIKE".equalsIgnoreCase(normalized)
                || "FLANKING".equalsIgnoreCase(normalized)
                || "HOTHP".equalsIgnoreCase(normalized)
                || "HOTSHIELD".equalsIgnoreCase(normalized)
                || "DMGTAKEN".equalsIgnoreCase(normalized)
                || StoreCharData.isSizeAttribute(normalized);
    }

    private boolean isInstantResourceDeltaAttribute(String attribute) {
        if (attribute == null || attribute.isBlank()) return false;
        String normalized = attribute.trim();
        return "HP".equalsIgnoreCase(normalized)
                || "AURA".equalsIgnoreCase(normalized)
                || "SPENTAURA".equalsIgnoreCase(normalized)
                || "SHIELD".equalsIgnoreCase(normalized);
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
        if ("SPENTAURA".equalsIgnoreCase(attribute)) {
            double maxAura = Math.max(0.0, character.getResources().calcMaxAura());
            double previousSpent = Math.max(0.0, character.getResources().getSpentAura());
            double newSpentAura = clamp(previousSpent + severity, 0.0, maxAura);
            character.getResources().setSpentAura(newSpentAura);
            double spentDelta = newSpentAura - previousSpent;
            if (spentDelta > 0.0) {
                character.recordAuraSpentForCombatSpecialties(spentDelta);
            }
            return true;
        }
        if ("SHIELD".equalsIgnoreCase(attribute)) {
            double previous = Math.max(0.0, character.getResources().getShield());
            character.getResources().setShield(Math.max(0.0, previous + severity));
            return true;
        }
        return false;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void appendStandardPreview(StringBuilder text, List<DataStatus> statuses) {
        int index = 1;
        for (DataStatus status : statuses) {
            text.append(index++).append(". ").append(status.getName()).append(System.lineSeparator());
            text.append("Attribute: ").append(formatAttributeDisplay(status.getAttribute())).append(System.lineSeparator());
            text.append("Severity: ").append(formatNumber(status.getSeverity())).append(System.lineSeparator());
            text.append("Duration: ").append(status.getDurationType());
            if (status.getDuration() > 0) {
                text.append(" (").append(status.getDuration()).append(")");
            }
            text.append(System.lineSeparator());
            text.append("Affinity: ").append(status.getAffinity()).append(System.lineSeparator());
            text.append("Description: ").append(status.getDescription()).append(System.lineSeparator());
            text.append(System.lineSeparator());
        }
    }

    private List<DataStatus> collectNonGrantStatuses(List<DataStatus> statuses) {
        ArrayList<DataStatus> nonGrantStatuses = new ArrayList<>();
        if (statuses == null) return nonGrantStatuses;
        for (DataStatus status : statuses) {
            if (status == null || getGrantFamilyName(status.getName()) != null) continue;
            nonGrantStatuses.add(status);
        }
        return nonGrantStatuses;
    }

    private Map<String, List<DataStatus>> groupGrantStatusesByFamily(List<DataStatus> statuses) {
        LinkedHashMap<String, List<DataStatus>> grantStatuses = new LinkedHashMap<>();
        if (statuses == null) return grantStatuses;
        for (DataStatus status : statuses) {
            if (status == null) continue;
            String grantFamily = getGrantFamilyName(status.getName());
            if (grantFamily == null) continue;
            grantStatuses.computeIfAbsent(grantFamily, ignored -> new ArrayList<>()).add(status);
        }
        return grantStatuses;
    }

    private List<DataStatus> collectActiveStatusesByGrantFamily(String grantFamily) {
        ArrayList<DataStatus> matches = new ArrayList<>();
        if (character == null || grantFamily == null || grantFamily.isBlank()) return matches;

        for (DataStatus status : collectActiveCharacterStatuses()) {
            if (grantFamily.equalsIgnoreCase(getGrantFamilyName(status.getName()))) {
                matches.add(status);
            }
        }
        return matches;
    }

    private List<DataStatus> collectActiveCharacterStatuses() {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (character == null) return statuses;

        if (character.getAttributes() != null) {
            appendStatusMatrix(statuses, character.getAttributes().getBAttributes());
            appendStatusMatrix(statuses, character.getAttributes().getMAttributes());
            appendStatusMatrix(statuses, character.getAttributes().getBDefense());
            appendStatusMatrix(statuses, character.getAttributes().getMDefense());
            appendStatusMatrix(statuses, character.getAttributes().getBResist());
            appendStatusMatrix(statuses, character.getAttributes().getMResist());
            appendStatusMatrix(statuses, character.getAttributes().getBCombat());
            appendStatusMatrix(statuses, character.getAttributes().getMCombat());
            appendStatusMatrix(statuses, character.getAttributes().getBSecondary());
            appendStatusMatrix(statuses, character.getAttributes().getMSecondary());
            appendStatusMatrix(statuses, character.getAttributes().getBDamage());
            appendStatusMatrix(statuses, character.getAttributes().getMDamage());
            appendStatusBlock(statuses, character.getAttributes().getBSkill());
            appendStatusBlock(statuses, character.getAttributes().getMSkill());
        }

        if (character.getResources() != null) {
            appendStatusBlock(statuses, character.getResources().getBaseHP());
            appendStatusBlock(statuses, character.getResources().getMultiHP());
            appendStatusBlock(statuses, character.getResources().getBaseAura());
            appendStatusBlock(statuses, character.getResources().getMultiAura());
            appendStatusBlock(statuses, character.getResources().getBaseResource1());
            appendStatusBlock(statuses, character.getResources().getMultiResource1());
            appendStatusBlock(statuses, character.getResources().getBaseResource2());
            appendStatusBlock(statuses, character.getResources().getMultiResource2());
            appendStatusBlock(statuses, character.getResources().getBaseResource3());
            appendStatusBlock(statuses, character.getResources().getMultiResource3());
            appendStatusBlock(statuses, character.getResources().getBaseReactions());
            appendStatusBlock(statuses, character.getResources().getMultiReactions());
        }

        return statuses;
    }

    private void appendStatusMatrix(List<DataStatus> target, ArrayList<DataStatus>[][] matrix) {
        if (target == null || matrix == null) return;
        for (ArrayList<DataStatus>[] block : matrix) {
            appendStatusBlock(target, block);
        }
    }

    private void appendStatusBlock(List<DataStatus> target, ArrayList<DataStatus>[] block) {
        if (target == null || block == null) return;
        for (ArrayList<DataStatus> list : block) {
            if (list == null) continue;
            for (DataStatus status : list) {
                maybeAddStatusCopy(target, status);
            }
        }
    }

    private void maybeAddStatusCopy(List<DataStatus> target, DataStatus status) {
        if (target == null || status == null || status.getName() == null) return;
        if ("Base".equalsIgnoreCase(status.getName())) return;
        target.add(new DataStatus(status));
    }

    private Map<String, Double> sumStatusSeverityByAttribute(List<DataStatus> statuses) {
        LinkedHashMap<String, Double> totals = new LinkedHashMap<>();
        if (statuses == null) return totals;
        for (DataStatus status : statuses) {
            if (status == null || status.getAttribute() == null || status.getAttribute().isBlank()) continue;
            String attribute = status.getAttribute().toUpperCase();
            totals.merge(attribute, status.getSeverity(), Double::sum);
        }
        return totals;
    }

    private void removeObsoleteGrantStatuses(DecodedEffect effect) {
        if (character == null || effect == null || !effect.hasStatuses()) return;

        Map<String, Set<String>> incomingNamesByGrantFamily = new LinkedHashMap<>();
        for (DataStatus status : effect.getStatuses()) {
            if (status == null) continue;
            String grantFamily = getGrantFamilyName(status.getName());
            if (grantFamily == null) continue;
            incomingNamesByGrantFamily
                    .computeIfAbsent(grantFamily, ignored -> new LinkedHashSet<>())
                    .add(normalizeStatusName(status.getName()));
        }
        if (incomingNamesByGrantFamily.isEmpty()) return;

        for (DataStatus status : collectActiveCharacterStatuses()) {
            String grantFamily = getGrantFamilyName(status.getName());
            if (grantFamily == null) continue;
            Set<String> incomingNames = incomingNamesByGrantFamily.get(grantFamily);
            if (incomingNames == null || incomingNames.contains(normalizeStatusName(status.getName()))) continue;
            removeActiveStatus(status);
        }

        if (resolveCombat() != null) {
            resolveCombat().getCombatStatus().removeIf(status -> {
                String grantFamily = getGrantFamilyName(status == null ? null : status.getName());
                if (grantFamily == null) return false;
                Set<String> incomingNames = incomingNamesByGrantFamily.get(grantFamily);
                return incomingNames != null && !incomingNames.contains(normalizeStatusName(status.getName()));
            });
        }
    }

    private void removeActiveStatus(DataStatus status) {
        if (status == null || character == null || status.getAttribute() == null || status.getAttribute().isBlank()) return;
        String attribute = status.getAttribute().toUpperCase();
        if (isResourceAttribute(attribute)) {
            character.getResources().removeStatusByStatus(status);
        } else {
            character.getAttributes().removeStatusByStatus(status);
        }
    }

    private String getGrantFamilyName(String statusName) {
        String baseName = extractStatusBaseName(statusName);
        if (baseName.isBlank() || !baseName.toUpperCase().contains("GRANT")) {
            return null;
        }
        return baseName;
    }

    private String extractStatusBaseName(String statusName) {
        if (statusName == null) return "";
        String trimmed = statusName.trim();
        int bracketIndex = trimmed.indexOf('[');
        if (bracketIndex >= 0) {
            return trimmed.substring(0, bracketIndex).trim();
        }
        return trimmed;
    }

    private String normalizeStatusName(String statusName) {
        return statusName == null ? "" : statusName.trim();
    }

    private String formatAttributeDisplay(String attribute) {
        if (attribute == null || attribute.isBlank()) return "None";
        String normalized = attribute.trim().toUpperCase();
        if (normalized.startsWith("BSKILL") && normalized.length() > "BSKILL".length()) {
            return "Skill: " + normalized.substring("BSKILL".length());
        }
        if (normalized.startsWith("MSKILL") && normalized.length() > "MSKILL".length()) {
            return "Skill Multiplier: " + normalized.substring("MSKILL".length());
        }
        if (StoreCharData.isSizeAttribute(normalized)) {
            return "Size";
        }
        return switch (normalized) {
            case "HP" -> "Current HP";
            case "AURA" -> "Current Aura";
            case "SPENTAURA" -> "Spent Aura";
            default -> attribute;
        };
    }

    private String formatNumber(double value) {
        synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(value);
        }
    }

    private String formatSignedNumber(double value) {
        return (value >= 0 ? "+" : "") + formatNumber(value);
    }

    private static final class AppliedStatusGroup {
        private final String name;
        private final List<DataStatus> statuses;

        private AppliedStatusGroup(String name, List<DataStatus> statuses) {
            this.name = name;
            this.statuses = statuses;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
