package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class FrameStatus extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.##");
    private static final String[] RESOURCE_KEYS = {
            "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
            "BASER1", "MULTIR1", "BASER2", "MULTIR2", "BASER3", "MULTIR3",
            "BASEREACT", "MULTIREACT"
    };

    private final FrameCombat combatFrame;
    private final FrameSheet sheetFrame;
    private StoreCharData character;

    private JTextArea codeArea;
    private JTextArea previewArea;
    private JLabel targetSideValueLabel;
    private JLabel targetShapeValueLabel;
    private JLabel statusCountValueLabel;
    private StatusCodeParseResult lastParseResult;
    private FrameManStatus manualStatusFrame;

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
        JScrollPane codePane = new JScrollPane(codeArea);
        addRow(center, gbc, y++, "Code", codePane);

        targetSideValueLabel = new JLabel("Not parsed");
        addRow(center, gbc, y++, "Target Side", targetSideValueLabel);
        targetShapeValueLabel = new JLabel("Not parsed");
        addRow(center, gbc, y++, "Target Shape", targetShapeValueLabel);
        statusCountValueLabel = new JLabel("0");
        addRow(center, gbc, y++, "Statuses", statusCountValueLabel);

        previewArea = new JTextArea(12, 40);
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        JScrollPane previewPane = new JScrollPane(previewArea);
        addRow(center, gbc, y++, "Preview", previewPane);

        JLabel note = new JLabel(
                "<html>Format: <code>ALLY_SINGLE_BATK:+2_BDODGE:+1_DUR:Turn:1_NAME:Guarded</code><br>"
                        + "Short form: <code>AT+2_DG+1</code> defaults to Ally / Single / Maintained.<br>"
                        + "Packed form: <code>NMABCGRANT_ATP2DGN1</code> uses a compact name token plus alphanumeric status tokens.<br>"
                        + "Target side and shape are previewed here. Apply still affects the currently loaded character.</html>",
                SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        center.add(note, gbc);

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton manual = new JButton("Manual");
        manual.addActionListener(e -> openManualStatusFrame());
        JButton preview = new JButton("Preview");
        preview.addActionListener(e -> previewStatusCode());
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applyStatusCode());
        footer.add(cancel);
        footer.add(manual);
        footer.add(preview);
        footer.add(apply);
        add(footer, BorderLayout.SOUTH);
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

    private void previewStatusCode() {
        StatusCodeParseResult parseResult = parseCurrentCode();
        lastParseResult = parseResult;
        if (parseResult == null || !parseResult.isSuccess()) {
            showParseErrors(parseResult);
            return;
        }

        DecodedEffect effect = parseResult.getEffect();
        targetSideValueLabel.setText(effect.getTargetSide() == null ? "Unknown" : effect.getTargetSide().name());
        targetShapeValueLabel.setText(effect.getTargetShape() == null ? "Unknown" : effect.getTargetShape().name());
        statusCountValueLabel.setText(Integer.toString(effect.getStatuses().size()));
        previewArea.setText(buildPreviewText(effect));
        previewArea.setCaretPosition(0);
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

    private StatusCodeParseResult parseCurrentCode() {
        resetPreviewIfCodeBlank();
        String code = codeArea == null ? "" : codeArea.getText();
        return StatusCodeParser.parse(code);
    }

    private void resetPreviewIfCodeBlank() {
        if (codeArea == null || codeArea.getText() == null || codeArea.getText().isBlank()) {
            targetSideValueLabel.setText("Not parsed");
            targetShapeValueLabel.setText("Not parsed");
            statusCountValueLabel.setText("0");
            previewArea.setText("");
        }
    }

    private void showParseErrors(StatusCodeParseResult parseResult) {
        targetSideValueLabel.setText("Invalid");
        targetShapeValueLabel.setText("Invalid");
        statusCountValueLabel.setText("0");
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
        text.append("Target Side: ").append(effect.getTargetSide()).append(System.lineSeparator());
        text.append("Target Shape: ").append(effect.getTargetShape()).append(System.lineSeparator());
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
            manualStatusFrame = combatFrame != null
                    ? new FrameManStatus(combatFrame, character)
                    : new FrameManStatus(sheetFrame, character);
        } else {
            manualStatusFrame.updateCharacter(character);
        }
        manualStatusFrame.setVisible(true);
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

        if ((reminderStatus || combatMarkerStatus || isTimedDuration(originalDurationType)) && character.getCombat() != null) {
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
        if (isIncapacitateAttribute(attribute)) {
            if (character.getCombat() == null) return false;
            character.getCombat().adjustIncapacitateTokens((int)Math.round(status.getSeverity()));
            return true;
        }
        if (isRootAttribute(attribute)) {
            if (character.getCombat() == null) return false;
            character.getCombat().adjustRootTokens((int)Math.round(status.getSeverity()));
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
                || "DMGTAKEN".equalsIgnoreCase(normalized);
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

    private void appendStandardPreview(StringBuilder text, List<DataStatus> statuses) {
        int index = 1;
        for (DataStatus status : statuses) {
            text.append(index++).append(". ").append(status.getName()).append(System.lineSeparator());
            text.append("Attribute: ").append(status.getAttribute()).append(System.lineSeparator());
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

        if (character.getCombat() != null) {
            character.getCombat().getCombatStatus().removeIf(status -> {
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

    private String formatNumber(double value) {
        synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(value);
        }
    }

    private String formatSignedNumber(double value) {
        return (value >= 0 ? "+" : "") + formatNumber(value);
    }
}
