package eternity;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

/**
 * Attribute selection flow (core + character attributes) using a simple point-buy.
 * Simplified replacement for the old FrameHelper-based implementation.
 */
public class FrameNewAttribute extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private static final Integer[] ATTVALUES = {8, 9, 10, 11, 12, 13, 14, 15};
    private static final String[] ATTRIBUTES = {"STR", "DEX", "CON", "FOC", "CAP", "CTL", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB"};

    private final ArrayList<JComboBox<Integer>> attributeAnswers = new ArrayList<>();
    private final int[] coreAtts = new int[6];
    private final int[] charAtts = new int[6];

    private final JLabel[] labels = new JLabel[9];
    private final JFormattedTextField[] numFields = new JFormattedTextField[7];
    private final JButton[] buttons = new JButton[2];
    private JLabel headerL;

    private int remainder;
    private boolean warn;
    private boolean corePhase = true;

    public FrameNewAttribute(FrameSheet sheetFrame, CharData character, FrameNew parent, boolean gmMode) {
        super("Attributes");
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        initDefaults();
        buildLayout();

        setSize(560, 360);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initDefaults() {
        for (int i = 0; i < 6; i++) {
            coreAtts[i] = 10;
            charAtts[i] = 10;
        }
    }

    private void buildLayout() {
        setLayout(null);

        headerL = new JLabel("Attributes", SwingConstants.CENTER);
        headerL.setBounds(20, 15, 520, 24);
        headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 18f));
        add(headerL);

        // Labels and fields
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("");
            labels[i].setBounds(40, 80 + 35 * i, 140, 20);
            labels[i].setVisible(false);
            add(labels[i]);
        }

        for (int i = 0; i < numFields.length; i++) {
            numFields[i] = new JFormattedTextField(NumberFormat.getIntegerInstance());
            numFields[i].setBounds(265, 80 + 35 * i, 100, 20);
            numFields[i].setEditable(false);
            numFields[i].setVisible(false);
            add(numFields[i]);
        }

        // Combo boxes (reused for both phases)
        for (int i = 0; i < 6; i++) {
            JComboBox<Integer> box = new JComboBox<>(ATTVALUES);
            box.setBounds(160, 80 + 35 * i, 80, 20);
            box.addActionListener(e -> updateRemainder());
            attributeAnswers.add(box);
            add(box);
        }

        // Buttons
        buttons[0] = new JButton("Back");
        buttons[0].setBounds(140, 280, 100, 28);
        add(buttons[0]);

        buttons[1] = new JButton("Next >>>");
        buttons[1].setBounds(320, 280, 120, 28);
        add(buttons[1]);

        // Static label headers
        labels[6].setBounds(160, 60, 80, 20);
        labels[6].setText("Value");
        labels[6].setVisible(true);

        labels[7].setBounds(265, 60, 100, 20);
        labels[7].setText("Cost");
        labels[7].setVisible(true);

        labels[8].setBounds(380, 60, 140, 20);
        labels[8].setText("Remaining Points");
        labels[8].setVisible(true);

        numFields[6].setBounds(380, 85, 140, 20);
        numFields[6].setEditable(false);
        numFields[6].setVisible(true);

        // Visual border around the form area
        JLabel border = new JLabel();
        border.setBounds(20, 50, 520, 210);
        border.setBorder(BorderFactory.createLineBorder(getForeground()));
        add(border);

        showCorePhase();
    }

    // ---------------------------------------------------------
    // Phases
    // ---------------------------------------------------------
    private void showCorePhase() {
        corePhase = true;
        headerL.setText("Determine Core Attribute values:");
        setLabels(new String[]{"Strength", "Dexterity", "Constitution", "Focus", "Control", "Capacity"});
        setTooltips(List.of(
                "Strength increases Melee Total Damage.",
                "Dexterity increases Dodge.",
                "Constitution increases Maximum Hit Points.",
                "Focus increases Attack.",
                "Control increases Total Healing and Range.",
                "Capacity increases Maximum Aura."
        ));

        for (int i = 0; i < 6; i++) {
            attributeAnswers.get(i).setSelectedItem(coreAtts[i]);
            attributeAnswers.get(i).setVisible(true);
            numFields[i].setVisible(true);
        }

        buttons[0].setText("Back");
        resetButtonListeners();
        buttons[0].addActionListener(e -> closeFrame());
        buttons[1].addActionListener(e -> coreAttConfirm());

        updateRemainder();
    }

    private void showCharacterPhase() {
        corePhase = false;
        headerL.setText("Determine Character Attribute values:");
        setLabels(new String[]{"Knowledge", "Mechanical", "Perception", "Intuition", "Charisma", "Subtlety"});
        setTooltips(List.of("", "", "", "", "", ""));

        for (int i = 0; i < 6; i++) {
            attributeAnswers.get(i).setSelectedItem(charAtts[i]);
            attributeAnswers.get(i).setVisible(true);
            numFields[i].setVisible(true);
        }

        buttons[0].setText("Back");
        resetButtonListeners();
        buttons[0].addActionListener(e -> characterCharAttBack());
        buttons[1].addActionListener(e -> charAttConfirm());

        updateRemainder();
    }

    private void setLabels(String[] texts) {
        for (int i = 0; i < 6; i++) {
            labels[i].setText(texts[i]);
            labels[i].setVisible(true);
        }
    }

    private void setTooltips(List<String> tooltips) {
        for (int i = 0; i < tooltips.size(); i++) {
            labels[i].setToolTipText(tooltips.get(i));
        }
    }

    private void resetButtonListeners() {
        for (ActionListener l : buttons[0].getActionListeners()) buttons[0].removeActionListener(l);
        for (ActionListener l : buttons[1].getActionListeners()) buttons[1].removeActionListener(l);
    }

    // ---------------------------------------------------------
    // Logic
    // ---------------------------------------------------------
    private void updateRemainder() {
        remainder = 0;
        for (int i = 0; i < 6; i++) {
            Object sel = attributeAnswers.get(i).getSelectedItem();
            if (!(sel instanceof Integer)) continue;

            int tempInt = (Integer) sel;
            int attMod = tempInt - 10;
            int attVariant = Math.abs(attMod) + 1;
            attVariant = (attVariant * attMod) / 2;

            numFields[i].setValue(attVariant);
            remainder += attVariant;
        }

        remainder = 25 - remainder;
        numFields[6].setValue(remainder);
        warn = false;
    }

    private void coreAttConfirm() {
        if (gmMode) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < 6; i++) {
                coreAtts[i] = rng.nextInt(10, 16);
            }
            for (int i = 0; i < 6; i++) {
                charAtts[i] = rng.nextInt(10, 16);
            }
            applyAttributesToCharacter();
            parent.attConfirmed();
            dispose();
            return;
        }

        if (!validateSpend()) return;

        for (int i = 0; i < 6; i++) {
            coreAtts[i] = (Integer) attributeAnswers.get(i).getSelectedItem();
        }

        showCharacterPhase();
    }

    private void characterCharAttBack() {
        for (int i = 0; i < 6; i++) {
            charAtts[i] = (Integer) attributeAnswers.get(i).getSelectedItem();
        }
        showCorePhase();
    }

    private void charAttConfirm() {
        if (!validateSpend()) return;

        for (int i = 0; i < 6; i++) {
            charAtts[i] = (Integer) attributeAnswers.get(i).getSelectedItem();
        }

        applyAttributesToCharacter();
        parent.attConfirmed();
        dispose();
    }

    private boolean validateSpend() {
        if (remainder < 0) {
            JOptionPane.showMessageDialog(this,
                    "You do not have enough attribute points for this selection.\nPlease lower your overall attribute selection.");
            return false;
        } else if (remainder > 0 && !warn) {
            JOptionPane.showMessageDialog(this,
                    "You have not spent all of your attribute points.\nIf you are satisfied with your selection, press confirm again.");
            warn = true;
            return false;
        }
        return true;
    }

    private void applyAttributesToCharacter() {
        // Remove any previous "CharCreation" modifiers, then apply new ones.
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            String key = ATTRIBUTES[i];
            int value = (i < 6) ? coreAtts[i] : charAtts[i - 6];
            character.getAttributes().setStatusSeverity("attribute", key, "Passive", value);
        }
        character.getAttributes().setStatusSeverity("combat", "APP", "Passive", 10);
        character.getAttributes().setStatusSeverity("combat", "MOVE", "Passive", 25);
        character.getAttributes().setStatusSeverity("combat", "RANGE", "Passive", 15);
        character.getAttributes().setStatusSeverity("combat", "INIT", "Passive", 10);
        character.getAttributes().setStatusSeverity("secondary", "MAXATK", "Passive", 1);
    }

    private void closeFrame() {
        dispose();
    }
}
