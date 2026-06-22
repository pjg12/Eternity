package eternity;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Helper frame for resolving Technical effects.
 */
public class FrameTechnical extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String TECHNICAL_REMINDER_MARKER = "[[TECH]]";
    private static final String CRUSHING_BLOW_BEND_TECHNICAL = "Crushing Blow (Bend)";
    private static final String CRUSHING_BLOW_BREAK_TECHNICAL = "Crushing Blow (Break)";
    private static final String COMMAND_SAFE_TECHNICAL = "Command (Safe)";
    private static final String COMMAND_SOUND_TECHNICAL = "Command (Sound)";
    private static final String CHASTISE_NIGHT_TECHNICAL = "Chastise (Night)";
    private static final String CHASTISE_DAY_TECHNICAL = "Chastise (Day)";
    private static final String EFFLUX_RISE_TECHNICAL = "Efflux (Rise)";
    private static final String EFFLUX_FALL_TECHNICAL = "Efflux (Fall)";
    private static final String IONIZE_SUPPLY_TECHNICAL = "Ionize (Supply)";
    private static final String MISDIRECTION_TURN_TECHNICAL = "Misdirection (Turn)";
    private static final String PULSE_SLOW_MELEE_TECHNICAL = "Pulse (Slow) Melee";
    private static final String PULSE_SLOW_RANGED_TECHNICAL = "Pulse (Slow) Ranged";
    private static final String PULSE_STEADY_MELEE_TECHNICAL = "Pulse (Steady) Melee";
    private static final String PULSE_STEADY_RANGED_TECHNICAL = "Pulse (Steady) Ranged";
    private static final String RESONANT_STRIKE_EBB_TECHNICAL = "Resonant Strike (Ebb)";
    private static final String SHIV_OVER_TECHNICAL = "Shiv (Over)";
    private static final String SHIV_UNDER_TECHNICAL = "Shiv (Under)";
    private static final String[] RESOURCE_KEYS = {
            "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
            "BASER1", "MULTIR1", "BASER2", "MULTIR2", "BASER3", "MULTIR3",
            "BASEREACT", "MULTIREACT", "BASEANGEL", "MULTIANGEL"
    };

    private static final int FRAME_WIDTH = 520;
    private static final int FRAME_HEIGHT = 360;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 18);
    private static final Font SUBHEADER_FONT = new Font(null, Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 6, 18);
    private static final EmptyBorder CONTENT_BORDER = new EmptyBorder(8, 18, 8, 18);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(0, 18, 12, 18);
    private static final Insets FIELD_INSETS = new Insets(4, 6, 4, 6);

    private final StoreCharData character;
    private final DataTechnical technical;
    private final Runnable confirmAction;
    private final Runnable cancelAction;

    private final JLabel headerLabel;
    private final JLabel subHeaderLabel;
    private final JLabel saveValueLabel;
    private final JLabel numberValueLabel;
    private final JLabel durationValueLabel;
    private final JLabel techPointsValueLabel;
    private final JTextArea descriptionArea;

    private final JButton confirmButton;
    private final JButton shareButton;

    private int techPoints;
    private boolean promptSequenceShown;
    private boolean confirmed;

    public FrameTechnical(JFrame parent, StoreCharData character, DataTechnical technical) {
        this(parent, character, technical, null, null);
    }

    public FrameTechnical(JFrame parent, StoreCharData character, DataTechnical technical, Runnable confirmAction, Runnable cancelAction) {
        super("Technical Helper");
        this.character = character;
        this.technical = technical == null ? new DataTechnical() : new DataTechnical(technical);
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
        this.techPoints = 0;
        this.promptSequenceShown = false;
        this.confirmed = false;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        headerLabel = buildHeaderLabel(HEADER_FONT);
        subHeaderLabel = buildHeaderLabel(SUBHEADER_FONT);
        saveValueLabel = buildValueLabel();
        numberValueLabel = buildValueLabel();
        durationValueLabel = buildValueLabel();
        techPointsValueLabel = buildValueLabel();

        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        confirmButton = new JButton("Confirm");
        shareButton = new JButton("Share");

        buildUi();
        loadTechnicalData();
        bindButtons();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && !promptSequenceShown) {
            promptSequenceShown = true;
            runPromptSequence();
        }
    }

    public int getTechPoints() {
        return techPoints;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public DataTechnical getTechnical() {
        return new DataTechnical(technical);
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
        panel.add(headerLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(subHeaderLabel, gbc);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(CONTENT_BORDER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = FIELD_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, gbc, 0, "Save", saveValueLabel);
        addRow(panel, gbc, 1, "Number", numberValueLabel);
        addRow(panel, gbc, 2, "Duration", durationValueLabel);
        addRow(panel, gbc, 3, "Tech Points", techPointsValueLabel);

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBorder(FOOTER_BORDER);
        panel.add(shareButton);
        panel.add(confirmButton);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buildFieldLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(valueLabel, gbc);
    }

    private void loadTechnicalData() {
        headerLabel.setText(safeDisplay(technical.getName(), "Technical"));
        subHeaderLabel.setText("Resolve the active technical effect.");
        saveValueLabel.setText(safeDisplay(technical.getSave(), "None"));
        shareButton.setVisible(isStatusTechnical());
        refreshComputedValues();
    }

    private void bindButtons() {
        confirmButton.addActionListener(e -> {
            confirmed = true;
            applyTechnicalEffect();
            if (confirmAction != null) {
                confirmAction.run();
            }
            dispose();
        });
        shareButton.addActionListener(e -> copyStatusMacroToClipboard());
    }

    private void runPromptSequence() {
        if (promptForSuccessfulAttack()) {
            techPoints++;
        }

        if (requiresSavePrompt() && !promptForTargetMadeSave()) {
            techPoints++;
        }

        refreshComputedValues();
    }

    private boolean promptForSuccessfulAttack() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Was the attack successful?",
                "Technical Check",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean requiresSavePrompt() {
        String save = technical.getSave();
        return save != null && !save.isBlank() && !"none".equalsIgnoreCase(save.trim());
    }

    private boolean promptForTargetMadeSave() {
        String message = "The primary target must make a " + technical.getSave().trim() + " save.\n"
                + "Your APPLY value is " + trimNumber(getApplyValue()) + ".\n\n"
                + "Did the target make its save?";
        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Technical Save",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private double getApplyValue() {
        if (character == null || character.getAttributes() == null) return 0.0;
        return Math.max(0.0, character.getAttributes().calcStatusValue("APP"));
    }

    private void refreshComputedValues() {
        updateTechPointsLabel();
        updateNumberLabel();
        updateDurationLabel();
        updateDetailsText();
    }

    private void updateTechPointsLabel() {
        techPointsValueLabel.setText(Integer.toString(techPoints));
    }

    private void updateNumberLabel() {
        String formula = technical.getNumber();
        if (formula == null || formula.isBlank()) {
            numberValueLabel.setText("-");
            return;
        }
        try {
            numberValueLabel.setText(trimNumber(resolveDisplayedNumberValue(evaluateFormula(formula))));
        } catch (IllegalArgumentException ignored) {
            numberValueLabel.setText(formula.trim());
        }
    }

    private void updateDurationLabel() {
        if (isPulseSlowMeleeTechnical() || isPulseSteadyTechnical()) {
            durationValueLabel.setText("Instant");
            return;
        }
        if (isPulseSlowRangedTechnical()) {
            int durationTurns = getPulseSlowRangedDurationTurns();
            durationValueLabel.setText(durationTurns + (durationTurns == 1 ? " round" : " rounds"));
            return;
        }
        String formula = technical.getDuration();
        if (formula == null || formula.isBlank()) {
            durationValueLabel.setText("-");
            return;
        }
        try {
            durationValueLabel.setText(trimNumber(evaluateDurationValue()) + " turns");
        } catch (IllegalArgumentException ignored) {
            durationValueLabel.setText(formula.trim() + " turns");
        }
    }

    private void applyTechnicalEffect() {
        if (character == null || technical == null) return;
        if (isSelfTechnical()) {
            applySelfTechnicalEffect();
            return;
        }
        if (character.getCombat() == null || !isReminderTechnical()) return;

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return;

        DataStatus reminderStatus = new DataStatus();
        reminderStatus.setName(safeDisplay(technical.getName(), "Technical"));
        reminderStatus.setAffinity("None");
        reminderStatus.setAttribute("REMINDER");
        reminderStatus.setSeverity(0.0);
        reminderStatus.setDurationType("TURN");
        reminderStatus.setDuration(durationTurns);
        reminderStatus.setDescription(buildResolvedReminderDescription());
        character.getCombat().addStatus(reminderStatus);
    }

    private boolean isReminderTechnical() {
        String effect = technical.getEffect();
        return effect != null && "reminder".equalsIgnoreCase(effect.trim());
    }

    private boolean isSelfTechnical() {
        String effect = technical.getEffect();
        return effect != null && "self".equalsIgnoreCase(effect.trim());
    }

    private boolean isStatusTechnical() {
        String effect = technical.getEffect();
        return effect != null && "status".equalsIgnoreCase(effect.trim());
    }

    private void applySelfTechnicalEffect() {
        if (CRUSHING_BLOW_BEND_TECHNICAL.equalsIgnoreCase(safeDisplay(technical.getName(), ""))) {
            applySelfStatus("BALL", buildResolvedNumberValue(), evaluateDurationTurns(), buildSelfTechnicalDescription());
        }
    }

    private double buildResolvedNumberValue() {
        String formula = technical.getNumber();
        if (formula == null || formula.isBlank()) return 0.0;
        try {
            return resolveDisplayedNumberValue(evaluateFormula(formula));
        } catch (IllegalArgumentException ignored) {
            return 0.0;
        }
    }

    private String buildSelfTechnicalDescription() {
        String details = technical.getNumDescription();
        if (details == null || details.isBlank()) {
            details = technical.getDescription();
        }
        return resolveTechnicalDescriptionText(safeDisplay(details, "No description available."));
    }

    private void applySelfStatus(String attribute, double severity, int durationTurns, String description) {
        if (attribute == null || attribute.isBlank() || Math.abs(severity) <= 0.0001 || durationTurns <= 0) return;

        DataStatus tracked = new DataStatus();
        tracked.setName(safeDisplay(technical.getName(), "Technical"));
        tracked.setAffinity("None");
        tracked.setAttribute(attribute.toUpperCase());
        tracked.setSeverity(severity);
        tracked.setDescription(description);
        tracked.setDurationType("Turn");
        tracked.setDuration(durationTurns);

        DataStatus applied = new DataStatus(tracked);
        applied.setDurationType("Temporary");

        if (!applyStatusToCharacter(applied)) return;

        if (character.getCombat() != null) {
            character.getCombat().addStatus(tracked);
        }
        character.updateAll();
    }

    private boolean applyStatusToCharacter(DataStatus status) {
        if (status == null || character == null || status.getAttribute() == null) return false;
        String attribute = status.getAttribute().toUpperCase();
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

    private String buildResolvedReminderDescription() {
        String details = technical.getNumDescription();
        if (details == null || details.isBlank()) {
            details = technical.getDescription();
        }
        String display = safeDisplay(details, "No description available.");
        String resolved = resolveTechnicalDescriptionText(display);
        String iconMarker = resolveReminderIconMarker();
        String technicalMarker = TECHNICAL_REMINDER_MARKER + " ";
        if (!iconMarker.isBlank()) {
            return technicalMarker + iconMarker + " " + resolved;
        }
        return technicalMarker + resolved;
    }

    private String resolveReminderIconMarker() {
        String technicalName = technical.getName();
        if (technicalName == null || technicalName.isBlank()) return "";
        if ("Judgment (Innocent)".equalsIgnoreCase(technicalName.trim())) {
            return "[[ICON:innocent.jpg]]";
        }
        if ("Judgment (Guilty)".equalsIgnoreCase(technicalName.trim())
                || "Judgment (Guity)".equalsIgnoreCase(technicalName.trim())) {
            return "[[ICON:guilty.jpg]]";
        }
        return "";
    }

    private double evaluateDurationValue() {
        String formula = technical.getDuration();
        if (formula == null || formula.isBlank()) return 0.0;
        return evaluateFormula(formula);
    }

    private double resolveDisplayedNumberValue(double rawValue) {
        if (isShivOverTechnical()) {
            return Math.ceil(rawValue);
        }
        return rawValue;
    }

    private boolean isShivOverTechnical() {
        String technicalName = technical == null ? null : technical.getName();
        return technicalName != null && SHIV_OVER_TECHNICAL.equalsIgnoreCase(technicalName.trim());
    }

    private void copyStatusMacroToClipboard() {
        String macro = buildStatusMacro();
        if (macro.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Status-code sharing is not yet defined for this technical.",
                    "Share Unavailable",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringSelection stringSelection = new StringSelection(macro);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(
                this,
                "Technical status macro copied to the clipboard.",
                "Share Ready",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String buildStatusMacro() {
        String statusCode = buildTechnicalStatusCode();
        if (statusCode.isBlank()) return "";

        String technicalName = sanitizeMacroText(safeDisplay(technical.getName(), "Technical"));
        String charName = "Character";
        if (character != null && character.getIdentity() != null && character.getIdentity().getName() != null
                && !character.getIdentity().getName().isBlank()) {
            charName = sanitizeMacroText(character.getIdentity().getName());
        }

        StringBuilder macro = new StringBuilder();
        macro.append("!scriptcard {{ --#title|").append(charName)
                .append(" --#leftSub|Technical Effect")
                .append(" --+|Technical: ").append(technicalName)
                .append(" --+|Status Code:[br]&nbsp;&nbsp;").append(statusCode)
                .append(" }}");
        return macro.toString();
    }

    private String buildTechnicalStatusCode() {
        if (!isStatusTechnical() || technical == null || technical.getName() == null) return "";
        String technicalName = technical.getName().trim();
        if (CRUSHING_BLOW_BREAK_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildCrushingBlowBreakStatusCode();
        }
        if (COMMAND_SAFE_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildCommandSafeStatusCode();
        }
        if (COMMAND_SOUND_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildCommandSoundStatusCode();
        }
        if (CHASTISE_NIGHT_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildChastiseNightStatusCode();
        }
        if (CHASTISE_DAY_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildChastiseDayStatusCode();
        }
        if (EFFLUX_RISE_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildEffluxRiseStatusCode();
        }
        if (EFFLUX_FALL_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildEffluxFallStatusCode();
        }
        if (IONIZE_SUPPLY_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildIonizeSupplyStatusCode();
        }
        if (MISDIRECTION_TURN_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildMisdirectionTurnStatusCode();
        }
        if (PULSE_SLOW_MELEE_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildPulseSlowMeleeStatusCode();
        }
        if (PULSE_SLOW_RANGED_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildPulseSlowRangedStatusCode();
        }
        if (PULSE_STEADY_MELEE_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildPulseSteadyStatusCode(PULSE_STEADY_MELEE_TECHNICAL, true);
        }
        if (PULSE_STEADY_RANGED_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildPulseSteadyStatusCode(PULSE_STEADY_RANGED_TECHNICAL, false);
        }
        if (RESONANT_STRIKE_EBB_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildResonantStrikeEbbStatusCode();
        }
        if (SHIV_OVER_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildShivOverStatusCode();
        }
        if (SHIV_UNDER_TECHNICAL.equalsIgnoreCase(technicalName)) {
            return buildShivUnderStatusCode();
        }
        return "";
    }

    private String buildCommandSafeStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(COMMAND_SAFE_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_DF:").append(formatStatusSeverityValue(numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Increase the target's DEF by " + trimNumber(numberValue) + "."));
        return code.toString();
    }

    private String buildCommandSoundStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(COMMAND_SOUND_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_HOTHP:").append(formatStatusSeverityValue(numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Heal the target for " + trimNumber(numberValue) + " at the start of each turn."));
        return code.toString();
    }

    private String buildChastiseNightStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(CHASTISE_NIGHT_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_TAKEN:").append(formatStatusSeverityValue(numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Increase final damage dealt to the target by " + trimNumber(numberValue) + " after damage reduction."));
        return code.toString();
    }

    private String buildChastiseDayStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int repeatCount = Math.max(1, techPoints);

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(CHASTISE_DAY_TECHNICAL));
        code.append("_DUR:Turn:").append(repeatCount);
        code.append("_HOTHP:").append(formatStatusSeverityValue(numberValue));
        code.append("_HOTSHIELD:").append(formatStatusSeverityValue(numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("At the start of each turn for " + repeatCount
                        + " turns, heal the target for " + trimNumber(numberValue)
                        + " and grant " + trimNumber(numberValue) + " shield."));
        return code.toString();
    }

    private String buildEffluxRiseStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(EFFLUX_RISE_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_TD:").append(formatStatusSeverityValue(numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Increase the target's TDMG by " + trimNumber(numberValue) + "."));
        return code.toString();
    }

    private String buildEffluxFallStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(EFFLUX_FALL_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_TD:").append(formatStatusSeverityValue(-numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Reduce the target's TDMG by " + trimNumber(numberValue) + "."));
        return code.toString();
    }

    private String buildIonizeSupplyStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(IONIZE_SUPPLY_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_BALL:").append(formatStatusSeverityValue(numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Increase the target's Resist All by " + trimNumber(numberValue) + "."));
        return code.toString();
    }

    private String buildCrushingBlowBreakStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        double severity = -numberValue;
        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(CRUSHING_BLOW_BREAK_TECHNICAL));

        int durationTurns = evaluateDurationTurns();
        if (durationTurns > 0) {
            code.append("_DUR:Turn:").append(durationTurns);
        }

        code.append("_DG:").append(formatStatusSeverityValue(severity));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Reduce the target's Dodge by " + trimNumber(numberValue) + "."));
        return code.toString();
    }

    private String buildShivOverStatusCode() {
        String formula = technical.getNumber();
        if (formula == null || formula.isBlank()) return "";

        double numberValue;
        try {
            numberValue = resolveDisplayedNumberValue(evaluateFormula(formula));
        } catch (IllegalArgumentException ignored) {
            return "";
        }

        int stunTokens = Math.max(0, (int)Math.ceil(numberValue));
        if (stunTokens <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(SHIV_OVER_TECHNICAL));
        code.append("_STUN:").append(stunTokens);
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Apply " + stunTokens + " stun tokens to the target."));
        return code.toString();
    }

    private String buildMisdirectionTurnStatusCode() {
        int durationTurns = evaluateDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(MISDIRECTION_TURN_TECHNICAL));
        code.append("_DUR:Turn:").append(durationTurns);
        code.append("_FLANK:1");
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Grant flanking bonuses against all targets."));
        return code.toString();
    }

    private String buildPulseSteadyStatusCode(String technicalName, boolean melee) {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int repeatCount = Math.max(0, techPoints);
        if (repeatCount <= 0) return "";
        double totalHealing = numberValue * repeatCount;

        StringBuilder code = new StringBuilder();
        code.append("ALLY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(technicalName));
        code.append("_HP:").append(formatStatusSeverityValue(totalHealing));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText((melee ? "Heal a target ally within melee range for " : "Heal a target ally for ")
                        + trimNumber(numberValue) + " HP, repeated " + repeatCount + " times."));
        return code.toString();
    }

    private String buildPulseSlowMeleeStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int incapacitateTokens = Math.max(0, (int)Math.ceil(numberValue));
        if (incapacitateTokens <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(PULSE_SLOW_MELEE_TECHNICAL));
        code.append("_INCAP:").append(incapacitateTokens);
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Apply " + incapacitateTokens + " incapacitate tokens to the target."));
        return code.toString();
    }

    private String buildPulseSlowRangedStatusCode() {
        double numberValue = buildResolvedNumberValue();
        if (Math.abs(numberValue) <= 0.0001) return "";

        int durationTurns = getPulseSlowRangedDurationTurns();
        if (durationTurns <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(PULSE_SLOW_RANGED_TECHNICAL));
        code.append("_DUR:Round:").append(durationTurns);
        code.append("_MV:").append(formatStatusSeverityValue(-numberValue));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Reduce the target's movement speed by " + trimNumber(numberValue)
                        + " until end of round" + (durationTurns > 1 ? ", extended by 1 additional round." : ".")));
        return code.toString();
    }

    private String buildResonantStrikeEbbStatusCode() {
        String formula = technical.getNumber();
        if (formula == null || formula.isBlank()) return "";

        double numberValue;
        try {
            numberValue = resolveDisplayedNumberValue(evaluateFormula(formula));
        } catch (IllegalArgumentException ignored) {
            return "";
        }

        int heavyTokens = Math.max(0, (int)Math.ceil(numberValue));
        if (heavyTokens <= 0) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(RESONANT_STRIKE_EBB_TECHNICAL));
        code.append("_HEAVY:").append(heavyTokens);
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Apply " + heavyTokens + " heavy tokens to the target."));
        return code.toString();
    }

    private String buildShivUnderStatusCode() {
        String formula = technical.getNumber();
        if (formula == null || formula.isBlank()) return "";

        double numberValue;
        try {
            numberValue = resolveDisplayedNumberValue(evaluateFormula(formula));
        } catch (IllegalArgumentException ignored) {
            return "";
        }

        double severity = -numberValue;
        if (Math.abs(severity) <= 0.0001) return "";

        StringBuilder code = new StringBuilder();
        code.append("ENEMY_SINGLE");
        code.append("_NAME:").append(sanitizeStatusCodeText(SHIV_UNDER_TECHNICAL));

        int durationTurns = evaluateDurationTurns();
        if (durationTurns > 0) {
            code.append("_DUR:Turn:").append(durationTurns);
        }

        code.append("_AT:").append(formatStatusSeverityValue(severity));
        code.append("_DESC:")
                .append(sanitizeStatusCodeText("Apply a debuff that reduces the target's ATK by " + trimNumber(numberValue) + "."));
        return code.toString();
    }

    private String formatStatusSeverityValue(double severity) {
        double rounded = Math.round(severity * 1000.0) / 1000.0;
        if (Math.abs(rounded - Math.rint(rounded)) <= 0.0001) {
            return Integer.toString((int) Math.round(rounded));
        }
        String text = String.format(java.util.Locale.ROOT, "%.3f", rounded);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private String sanitizeStatusCodeText(String value) {
        if (value == null) return "";
        return value.replace("|", "/").replace("_", " ").trim();
    }

    private String sanitizeMacroText(String value) {
        if (value == null) return "";
        return value.replace("|", "/").trim();
    }

    private int evaluateDurationTurns() {
        if (isPulseSlowMeleeTechnical() || isPulseSteadyTechnical()) {
            return 0;
        }
        if (isPulseSlowRangedTechnical()) {
            return getPulseSlowRangedDurationTurns();
        }
        double durationValue;
        try {
            durationValue = evaluateDurationValue();
        } catch (IllegalArgumentException ignored) {
            return 0;
        }
        return Math.max(0, (int) Math.ceil(durationValue));
    }

    private boolean isPulseSlowMeleeTechnical() {
        String technicalName = technical == null ? null : technical.getName();
        return technicalName != null && PULSE_SLOW_MELEE_TECHNICAL.equalsIgnoreCase(technicalName.trim());
    }

    private boolean isPulseSlowRangedTechnical() {
        String technicalName = technical == null ? null : technical.getName();
        return technicalName != null && PULSE_SLOW_RANGED_TECHNICAL.equalsIgnoreCase(technicalName.trim());
    }

    private boolean isPulseSteadyTechnical() {
        String technicalName = technical == null ? null : technical.getName();
        return technicalName != null
                && (PULSE_STEADY_MELEE_TECHNICAL.equalsIgnoreCase(technicalName.trim())
                || PULSE_STEADY_RANGED_TECHNICAL.equalsIgnoreCase(technicalName.trim()));
    }

    private int getPulseSlowRangedDurationTurns() {
        return techPoints > 0 ? 2 : 1;
    }

    private void updateDetailsText() {
        String details = technical.getNumDescription();
        if (details == null || details.isBlank()) {
            details = technical.getDescription();
        }
        String display = safeDisplay(details, "No description available.");
        descriptionArea.setText(resolveTechnicalDescriptionText(display));
        descriptionArea.setCaretPosition(0);
    }

    private String resolveTechnicalDescriptionText(String text) {
        if (text == null) return "";
        return text.replace("@", numberValueLabel.getText())
                .replace("<TP>", Integer.toString(techPoints));
    }

    private double evaluateFormula(String expression) {
        return new FormulaParser(expression).parse();
    }

    private double resolveFormulaIdentifier(String token) {
        if (token == null || token.isBlank()) return 0.0;
        String normalized = token.trim().toUpperCase();
        return switch (normalized) {
            case "PRIM" -> resolvePrimaryAttributeValue();
            case "SEC" -> resolveSecondaryAttributeValue();
            case "CL" -> getCharacterLevel();
            case "TP" -> techPoints;
            case "MAXHP" -> resolveMaxHpValue();
            case "MAXAURA" -> resolveMaxAuraValue();
            default -> resolveAttributeValue(normalized);
        };
    }

    private double resolvePrimaryAttributeValue() {
        String primaryKey = resolvePrimaryAttributeKey();
        return resolveAttributeValue(primaryKey);
    }

    private double resolveSecondaryAttributeValue() {
        String secondaryKey = resolveSecondaryAttributeKey();
        return resolveAttributeValue(secondaryKey);
    }

    private String resolvePrimaryAttributeKey() {
        DataClass effectiveClass = resolveEffectiveClassForPrimary();
        if (effectiveClass == null || effectiveClass.getPrimaryAtt() == null || effectiveClass.getPrimaryAtt().isBlank()) {
            return "";
        }
        return effectiveClass.getPrimaryAtt().trim().toUpperCase();
    }

    private String resolveSecondaryAttributeKey() {
        if (character == null || character.getIdentity() == null) return "";
        if (getCharacterLevel() < 5) return "";

        String subclassName = character.getIdentity().getCharSubclass();
        if (subclassName == null || subclassName.isBlank() || "?".equals(subclassName.trim()) || "***".equals(subclassName.trim())) {
            return "";
        }

        StoreRuleManager ruleManager = new StoreRuleManager();
        DataClass subclass = ruleManager.getClassByName(subclassName);
        if (subclass == null || subclass.getSecondaryAtt() == null || subclass.getSecondaryAtt().isBlank()) {
            return "";
        }
        return subclass.getSecondaryAtt().trim().toUpperCase();
    }

    private DataClass resolveEffectiveClassForPrimary() {
        if (character == null || character.getIdentity() == null) return null;
        StoreRuleManager ruleManager = new StoreRuleManager();

        String subclassName = character.getIdentity().getCharSubclass();
        if (subclassName != null && !subclassName.isBlank()
                && !"?".equals(subclassName.trim())
                && !"***".equals(subclassName.trim())) {
            DataClass subclass = ruleManager.getClassByName(subclassName);
            if (subclass != null) return subclass;
        }

        String className = character.getIdentity().getCharClass();
        if (className == null || className.isBlank() || "?".equals(className.trim())) return null;
        return ruleManager.getClassByName(className);
    }

    private double resolveAttributeValue(String attributeKey) {
        if (attributeKey == null || attributeKey.isBlank() || character == null || character.getAttributes() == null) {
            return 0.0;
        }
        return Math.max(0.0, character.getAttributes().calcStatusValue(attributeKey));
    }

    private double resolveMaxHpValue() {
        if (character == null || character.getResources() == null) return 0.0;
        return Math.max(0.0, character.getResources().calcMaxHP());
    }

    private double resolveMaxAuraValue() {
        if (character == null || character.getResources() == null) return 0.0;
        return Math.max(0.0, character.getResources().calcMaxAura());
    }

    private int getCharacterLevel() {
        if (character == null || character.getIdentity() == null) return 0;
        return Math.max(0, character.getIdentity().getLevel());
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

    private JLabel buildValueLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(LABEL_FONT);
        return label;
    }

    private String safeDisplay(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String trimNumber(double value) {
        if (Math.abs(value - Math.rint(value)) <= 0.0001) {
            return Integer.toString((int) Math.round(value));
        }
        String text = String.format(java.util.Locale.ROOT, "%.3f", value);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private final class FormulaParser {
        private final String expression;
        private int index = 0;

        private FormulaParser(String expression) {
            this.expression = expression == null ? "" : expression;
        }

        private double parse() {
            double value = parseExpression();
            skipWhitespace();
            if (index < expression.length()) {
                throw new IllegalArgumentException("Unexpected token in technical formula");
            }
            return value;
        }

        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    double divisor = parseFactor();
                    if (Math.abs(divisor) > 0.0000001) {
                        value /= divisor;
                    }
                } else {
                    return value;
                }
            }
        }

        private double parseFactor() {
            skipWhitespace();
            if (match('+')) return parseFactor();
            if (match('-')) return -parseFactor();
            if (match('(')) {
                double value = parseExpression();
                if (!match(')')) {
                    throw new IllegalArgumentException("Unclosed technical formula group");
                }
                return value;
            }
            if (index >= expression.length()) {
                throw new IllegalArgumentException("Unexpected end of technical formula");
            }

            char current = expression.charAt(index);
            if (Character.isDigit(current) || current == '.') {
                return parseNumber();
            }
            if (Character.isLetter(current)) {
                return parseIdentifier();
            }
            throw new IllegalArgumentException("Unsupported technical formula token");
        }

        private double parseNumber() {
            int start = index;
            while (index < expression.length()) {
                char current = expression.charAt(index);
                if (!Character.isDigit(current) && current != '.') break;
                index++;
            }
            return Double.parseDouble(expression.substring(start, index));
        }

        private double parseIdentifier() {
            int start = index;
            while (index < expression.length()) {
                char current = expression.charAt(index);
                if (!Character.isLetterOrDigit(current) && current != '_') break;
                index++;
            }
            return resolveFormulaIdentifier(expression.substring(start, index));
        }

        private boolean match(char expected) {
            skipWhitespace();
            if (index < expression.length() && expression.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
                index++;
            }
        }
    }
}
