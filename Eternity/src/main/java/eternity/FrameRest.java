package eternity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

/**
 * Simple dialog to apply a short or long rest to the active character.
 */
public class FrameRest extends JFrame {
    private static final long serialVersionUID = 1L;

    private final FrameSheet sheetFrame;
    private StoreCharData character;

    private final JCheckBox shortRest = new JCheckBox("Short Rest", false);
    private final JCheckBox longRest  = new JCheckBox("Long Rest");
    private final JFormattedTextField hoursField;

    public FrameRest(FrameSheet sheetFrame) {
        super("Rest / Advance Time");
        this.sheetFrame = sheetFrame;
        setSize(360, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // numeric field for hours
        NumberFormatter nf = new NumberFormatter(java.text.NumberFormat.getIntegerInstance());
        nf.setMinimum(0);
        nf.setAllowsInvalid(false);
        hoursField = new JFormattedTextField(nf);
        hoursField.setColumns(4);
        hoursField.setMinimumSize(new java.awt.Dimension(100, hoursField.getPreferredSize().height));
        hoursField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hoursField.setValue(0);

        // center layout with GridBag for neat alignment
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        center.add(new JLabel("Rest?"), gc);

        gc.gridy = 1; gc.gridwidth = 1;
        center.add(shortRest, gc);
        gc.gridx = 1;
        center.add(longRest, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 1;
        center.add(new JLabel("Hours:"), gc);
        gc.gridx = 1;
        center.add(hoursField, gc);

        add(center, BorderLayout.CENTER);

        // enforce mutual exclusivity between checkboxes
        shortRest.addActionListener(e -> {
            if (shortRest.isSelected()) {
                longRest.setSelected(false);
            }
        });
        longRest.addActionListener(e -> {
            if (longRest.isSelected()) {
                shortRest.setSelected(false);
            }
        });

        // buttons
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applyRest());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.add(cancel);
        south.add(apply);
        add(south, BorderLayout.SOUTH);
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
    }

    private void applyRest() {
        if (character == null || character.getResources() == null) {
            setVisible(false);
            return;
        }

        var res = character.getResources();
        if (longRest.isSelected()) {
            res.setLostHP(0);
            res.setSpentAura(0);
            res.setSpentR1(0);
            res.setSpentR2(0);
            res.setSpentR3(0);
            res.setSpentReactions(0);
        } else if (shortRest.isSelected()) {


            res.setSpentR1(0);
            res.setSpentR2(0);
            res.setSpentR3(0);
            res.setSpentReactions(0);
        } 

        // Apply elapsed hours to campaign time if provided
        try {
            Object val = hoursField.getValue();
            if (val != null) {
                int hours = Integer.parseInt(val.toString());
                if (hours > 0 && character.getIdentity() != null) {
                    character.getIdentity().addCampaignTime(java.time.Duration.ofHours(hours));
                }
            }
        } catch (Exception ignored) {}

        if (sheetFrame != null) {
            sheetFrame.loadCharacter(character);
            sheetFrame.refreshImagePanel();
            sheetFrame.refreshMainPanel(); // ensure main stats (including HP) reflect rest effects
        }
        System.out.println(character.getIdentity().getCurrentCampaignDateTime());

        setVisible(false);
    }
}

