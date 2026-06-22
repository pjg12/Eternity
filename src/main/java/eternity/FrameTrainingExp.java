package eternity;

import java.awt.CardLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

public class FrameTrainingExp extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String STANDARD_CARD = "standard";
    private static final String AD_HOC_CARD = "adHoc";

    public static final String[] AURA_TYPES = {"None", "Attribute", "Misc", "Affinity", "Fundamental", "Standard", "Crafting", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity", "Energy", "Force", "Light", "Darkness", "Poison", "Sound", "Psionic", "Spirit", "Time", "Deviant"};

    private final FrameSheet sheetFrame;
    private final StoreRuleManager ruleManager;
    private StoreCharData character;

    private final JLabel headerL;
    private final JLabel currentTotalL;
    private final JLabel sourceL;
    private final ButtonGroup sourceGroup;
    private final JRadioButton selfSource;
    private final JRadioButton referenceSource;
    private final JRadioButton masterSource;
    private final JRadioButton adHocSource;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final JFormattedTextField standardTotalGain;
    private final JComboBox<String> standardAuraType;
    private final JFormattedTextField standardAuraGain;
    private final JFormattedTextField adHocTotalGain;
    private final JComboBox<String> adHocAuraType;
    private final JFormattedTextField adHocAuraGain;

    FrameTrainingExp(FrameSheet sheetFrame, StoreRuleManager ruleManager) {
        super("Training XP");
        this.sheetFrame = sheetFrame;
        this.ruleManager = ruleManager;

        setLayout(null);
        setSize(460, 340);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        headerL = new JLabel("Gain Training XP", SwingConstants.CENTER);
        headerL.setBounds(20, 16, 400, 24);
        add(headerL);

        currentTotalL = new JLabel("", SwingConstants.CENTER);
        currentTotalL.setBounds(20, 46, 400, 20);
        add(currentTotalL);

        sourceL = new JLabel("Source", SwingConstants.LEFT);
        sourceL.setBounds(45, 78, 150, 20);
        add(sourceL);

        sourceGroup = new ButtonGroup();

        selfSource = buildSourceButton("Self", 205, 78);
        referenceSource = buildSourceButton("Reference", 285, 78);
        masterSource = buildSourceButton("Master", 205, 104);
        adHocSource = buildSourceButton("Ad hoc", 285, 104);
        selfSource.setSelected(true);

        standardTotalGain = buildIntegerField();
        standardAuraType = new JComboBox<>(AURA_TYPES);
        standardAuraGain = buildDecimalField();
        standardAuraGain.setEditable(false);
        standardAuraGain.setFocusable(false);
        adHocTotalGain = buildDecimalField();
        adHocAuraType = new JComboBox<>(AURA_TYPES);
        adHocAuraGain = buildDecimalField();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setLayout(cardLayout);
        cardPanel.setBounds(20, 136, 400, 170);
        add(cardPanel);

        cardPanel.add(buildXpCard(standardTotalGain, standardAuraType, standardAuraGain, true, "Training Hours"), STANDARD_CARD);
        cardPanel.add(buildXpCard(adHocTotalGain, adHocAuraType, adHocAuraGain, false), AD_HOC_CARD);

        selfSource.addActionListener(e -> showSourceCard());
        referenceSource.addActionListener(e -> showSourceCard());
        masterSource.addActionListener(e -> showSourceCard());
        adHocSource.addActionListener(e -> showSourceCard());
        standardTotalGain.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStandardAuraGain();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStandardAuraGain();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStandardAuraGain();
            }
        });
        showSourceCard();
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
        refreshFields();
    }

    private void refreshFields() {
        double currentTotal = 0.0;
        if (character != null && character.getTraining() != null) {
            currentTotal = character.getTraining().getTrainingXp();
        }
        currentTotalL.setText("Current Training XP: " + fmt(currentTotal));
        resetCardFields(standardTotalGain, standardAuraType, standardAuraGain);
        resetCardFields(adHocTotalGain, adHocAuraType, adHocAuraGain);
        selfSource.setSelected(true);
        updateStandardAuraGain();
        showSourceCard();
    }

    private void applyTrainingXp() {
        if (character == null || character.getTraining() == null) {
            setVisible(false);
            return;
        }

        JFormattedTextField activeTotalGain;
        JComboBox<String> activeAuraType;
        JFormattedTextField activeAuraGain;
        if (isAdHocSelected()) {
            activeTotalGain = adHocTotalGain;
            activeAuraType = adHocAuraType;
            activeAuraGain = adHocAuraGain;
        } else {
            activeTotalGain = standardTotalGain;
            activeAuraType = standardAuraType;
            activeAuraGain = standardAuraGain;
        }

        double totalGainValue = parseField(activeTotalGain);
        double auraGainValue = parseField(activeAuraGain);
        double appliedAuraGainValue = applyAuraTrainingXpBonus(auraGainValue);
        int auraIndex = activeAuraType.getSelectedIndex();
        boolean hasAuraType = auraIndex > 0;
        boolean isUntypedAuraGain = auraIndex == 0;

        if (totalGainValue <= 0.0 && auraGainValue <= 0.0) {
            JOptionPane.showMessageDialog(this, "Enter a positive training value.");
            return;
        }
        if (auraGainValue > 0.0 && !hasAuraType && !isUntypedAuraGain) {
            JOptionPane.showMessageDialog(this, "Select an aura type when applying aura training XP.");
            return;
        }

        CharTraining training = character.getTraining();
        if (appliedAuraGainValue > 0.0 && isUntypedAuraGain) {
            training.setTrainingXp(training.getTrainingXp() + appliedAuraGainValue);
        } else if (appliedAuraGainValue > 0.0 && hasAuraType) {
            int auraTypeIndex = auraIndex - 1;
            double currentAuraXp = training.getTrainingXpByAuraType(auraTypeIndex);
            training.setTrainingXpByAuraType(auraTypeIndex, currentAuraXp + appliedAuraGainValue);
        }

        character.updateAll();
        if (sheetFrame != null) {
            sheetFrame.refreshTrainingPanel();
            sheetFrame.refreshMainPanel();
        }
        setVisible(false);
    }

    private JPanel buildXpCard(JFormattedTextField totalGainField, JComboBox<String> auraTypeBox,
            JFormattedTextField auraGainField, boolean showTotalGain) {
        return buildXpCard(totalGainField, auraTypeBox, auraGainField, showTotalGain, "Training XP Gain");
    }

    private JPanel buildXpCard(JFormattedTextField totalGainField, JComboBox<String> auraTypeBox,
            JFormattedTextField auraGainField, boolean showTotalGain, String totalLabel) {
        JPanel panel = new JPanel(null);

        int auraY = 46;
        int auraGainY = 86;
        int buttonY = 134;
        if (showTotalGain) {
            JLabel totalGainL = new JLabel(totalLabel, SwingConstants.LEFT);
            totalGainL.setBounds(25, 6, 150, 20);
            panel.add(totalGainL);

            totalGainField.setBounds(235, 6, 120, 20);
            panel.add(totalGainField);
        } else {
            auraY = 18;
            auraGainY = 58;
            buttonY = 106;
        }

        JLabel auraTypeL = new JLabel("Aura Type", SwingConstants.LEFT);
        auraTypeL.setBounds(25, auraY, 150, 20);
        panel.add(auraTypeL);

        auraTypeBox.setBounds(185, auraY, 170, 22);
        panel.add(auraTypeBox);

        JLabel auraGainL = new JLabel("Aura XP Gain", SwingConstants.LEFT);
        auraGainL.setBounds(25, auraGainY, 150, 20);
        panel.add(auraGainL);

        auraGainField.setBounds(235, auraGainY, 120, 20);
        panel.add(auraGainField);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(70, buttonY, 120, 24);
        cancelButton.addActionListener(e -> setVisible(false));
        panel.add(cancelButton);

        JButton acceptButton = new JButton("Accept");
        acceptButton.setBounds(220, buttonY, 120, 24);
        acceptButton.addActionListener(e -> applyTrainingXp());
        panel.add(acceptButton);

        return panel;
    }

    private JFormattedTextField buildDecimalField() {
        NumberFormat format = new DecimalFormat("0.0#");
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0);
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setValue(0.0);
        return field;
    }

    private JFormattedTextField buildIntegerField() {
        NumberFormat format = new DecimalFormat("0");
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0);
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setValue(0);
        return field;
    }

    private JRadioButton buildSourceButton(String text, int x, int y) {
        JRadioButton button = new JRadioButton(text);
        button.setBounds(x, y, 90, 20);
        button.setOpaque(false);
        sourceGroup.add(button);
        add(button);
        return button;
    }

    private void resetCardFields(JFormattedTextField totalGainField, JComboBox<String> auraTypeBox,
            JFormattedTextField auraGainField) {
        if (totalGainField == standardTotalGain) totalGainField.setValue(0);
        else totalGainField.setValue(0.0);
        auraTypeBox.setSelectedIndex(0);
        auraGainField.setValue(0.0);
    }

    private void showSourceCard() {
        if (isAdHocSelected()) {
            cardLayout.show(cardPanel, AD_HOC_CARD);
            return;
        }
        updateStandardAuraGain();
        cardLayout.show(cardPanel, STANDARD_CARD);
    }

    private boolean isAdHocSelected() {
        return adHocSource.isSelected();
    }

    private void updateStandardAuraGain() {
        int hours = parseLiveIntegerField(standardTotalGain);
        double multiplier = 2.0;
        if (masterSource.isSelected()) multiplier = 4.0;
        else if (referenceSource.isSelected()) multiplier = 3.0;
        standardAuraGain.setValue(hours * multiplier * getAuraTrainingXpGainMultiplier());
    }

    private double applyAuraTrainingXpBonus(double auraGainValue) {
        return auraGainValue * getAuraTrainingXpGainMultiplier();
    }

    private double getAuraTrainingXpGainMultiplier() {
        return character == null ? 1.0 : character.getAuraTrainingXpGainMultiplier();
    }

    private int parseLiveIntegerField(JFormattedTextField field) {
        if (field == null) return 0;
        String text = field.getText();
        if (text == null) return 0;
        text = text.trim();
        if (text.isEmpty()) return 0;
        try {
            return Math.max(0, Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private double parseField(JFormattedTextField field) {
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

    private String fmt(double value) {
        return String.format("%.2f", value);
    }
}
