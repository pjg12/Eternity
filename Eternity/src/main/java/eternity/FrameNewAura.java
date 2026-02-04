package eternity;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Affinity and starter weapon selection.
 */
public class FrameNewAura extends JFrame {
    private static final long serialVersionUID = 1L;

    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;

    private static final String[] AURATYPE = {
            "***", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity",
            "Force", "Sound", "Light", "Darkness", "Poison", "Psionic", "Energy", "Spirit", "Time"
    };

    private JComboBox<String> auraPick;
    private final ArrayList<JComboBox<String>> weaponPick = new ArrayList<>();

    public FrameNewAura(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent) {
        super("Affinity & Starter Weapons");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(540, 280);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildHeader();
        buildLabels();
        buildPickers();
        buildButtons();
    }

    private void buildHeader() {
        JLabel headerL = new JLabel("Aura & Starting Weapons Select", SwingConstants.CENTER);
        headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 20f));
        headerL.setBounds(20, 15, 500, 24);
        add(headerL);
    }

    private void buildLabels() {
        JLabel affinityLabel = new JLabel("Natural Affinity");
        affinityLabel.setBounds(25, 60, 140, 20);
        add(affinityLabel);

        JLabel weaponLabel = new JLabel("Starter Weapons");
        weaponLabel.setBounds(225, 60, 200, 20);
        add(weaponLabel);
    }

    private void buildPickers() {
        auraPick = new JComboBox<>(AURATYPE);
        auraPick.setBounds(25, 100, 160, 22);
        add(auraPick);

        List<String> profs = character.getInventory().getWeaponProficiencies();
        boolean hasProfs = !profs.isEmpty();

        for (int i = 0; i < 2; i++) {
            JComboBox<String> box = new JComboBox<>();
            box.addItem("***");
            for (String p : profs) box.addItem(p);
            box.setBounds(225, 100 + 60 * i, 250, 22);
            box.setEnabled(hasProfs);
            weaponPick.add(box);
            add(box);
        }
    }

    private void buildButtons() {
        JButton back = new JButton("Back");
        back.setBounds(150, 200, 100, 28);
        back.addActionListener(e -> dispose());
        add(back);

        JButton confirm = new JButton("Confirm");
        confirm.setBounds(290, 200, 120, 28);
        confirm.addActionListener(e -> auraConfirm());
        add(confirm);
    }

    private void auraConfirm() {
        String affinity = (String) auraPick.getSelectedItem();
        if (affinity == null || "***".equals(affinity)) {
            JOptionPane.showMessageDialog(this, "Select a Natural Affinity to proceed.");
            return;
        }

        // Validate weapon picks only if profs exist
        boolean requireWeapons = weaponPick.stream().anyMatch(JComboBox::isEnabled);
        if (requireWeapons) {
            for (JComboBox<String> wp : weaponPick) {
                String val = (String) wp.getSelectedItem();
                if (val == null || "***".equals(val)) {
                    JOptionPane.showMessageDialog(this, "Select 2 starter weapons to proceed.");
                    return;
                }
            }
        }

        character.getTraining().addNaturalAffinity(affinity);

        if (requireWeapons) {
            for (JComboBox<String> wp : weaponPick) {
                String weaponName = (String) wp.getSelectedItem();
                DataItemEquipment item = dataQuery.getItemByName(weaponName);
                if (item != null) {
                    character.getInventory().addEquipment(new DataItemEquipment(item));
                }
            }
        }

        parent.auraConfirmed();
        dispose();
    }
}
