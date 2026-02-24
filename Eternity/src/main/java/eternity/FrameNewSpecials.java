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
 * Specialty selection window: pick 2 specialties by type.
 */
public class FrameNewSpecials extends JFrame {
    private static final long serialVersionUID = 1L;

    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private static final String[] SPECTYPES = {"***", "Proficiency", "Martial", "Class"};

    private final ArrayList<JComboBox<String>> specialType = new ArrayList<>();
    private final ArrayList<JComboBox<String>> specialPick = new ArrayList<>();

    public FrameNewSpecials(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent, boolean gmMode) {
        super("Specialty Select");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(520, 280);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildHeader();
        buildLabels();
        buildPickers();
        buildButtons();
    }

    private void buildHeader() {
        JLabel headerL = new JLabel("Specialty Select", SwingConstants.CENTER);
        headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 20f));
        headerL.setBounds(20, 15, 480, 24);
        add(headerL);
    }

    private void buildLabels() {
        JLabel typeLabel = new JLabel("Type");
        typeLabel.setBounds(25, 60, 125, 20);
        add(typeLabel);

        JLabel specLabel = new JLabel("Specialty");
        specLabel.setBounds(225, 60, 250, 20);
        add(specLabel);
    }

    private void buildPickers() {
        for (int i = 0; i < 2; i++) {
            int idx = i;

            JComboBox<String> typeBox = new JComboBox<>(SPECTYPES);
            typeBox.setBounds(25, 100 + 50 * i, 125, 20);
            typeBox.addActionListener(e -> updateSpecialPick(idx));
            specialType.add(typeBox);
            add(typeBox);

            JComboBox<String> specBox = new JComboBox<>();
            specBox.addItem("***");
            specBox.setBounds(225, 100 + 50 * i, 250, 20);
            specialPick.add(specBox);
            add(specBox);
        }
    }

    private void buildButtons() {
        JButton back = new JButton("Back");
        back.setBounds(140, 200, 100, 28);
        back.addActionListener(e -> dispose());
        add(back);

        JButton confirm = new JButton("Confirm");
        confirm.setBounds(280, 200, 120, 28);
        confirm.addActionListener(e -> specialsConfirm());
        add(confirm);
    }

    private void updateSpecialPick(int k) {
        JComboBox<String> typeBox = specialType.get(k);
        JComboBox<String> specBox = specialPick.get(k);

        String selectedType = (String) typeBox.getSelectedItem();
        if (selectedType == null) selectedType = "***";

        String otherPick = null;
        for (int i = 0; i < specialPick.size(); i++) {
            if (i == k) continue;
            String val = (String) specialPick.get(i).getSelectedItem();
            if (val != null && !"***".equals(val)) otherPick = val;
        }

        specBox.removeAllItems();
        specBox.addItem("***");

        List<DataSpecialty> options = dataQuery.getSpecialtiesByType(selectedType);
        for (DataSpecialty d : options) {
            if (otherPick != null && d.getName().equalsIgnoreCase(otherPick)) continue;
            specBox.addItem(d.getName());
        }
    }

    private void specialsConfirm() {
        if (gmMode) {
            specialType.get(0).setSelectedItem("Martial");
            specialType.get(1).setSelectedItem("Martial");
            updateSpecialPick(0);
            updateSpecialPick(1);
            specialPick.get(0).setSelectedItem("Specialization (Blade)");
            specialPick.get(1).setSelectedItem("Specialization (Sword)");
        }

        for (int i = 0; i < 2; i++) {
            String type = (String) specialType.get(i).getSelectedItem();
            String spec = (String) specialPick.get(i).getSelectedItem();
            if (type == null || "***".equals(type) || spec == null || "***".equals(spec)) {
                JOptionPane.showMessageDialog(this, "Select a type and specialty for both choices.");
                return;
            }
        }

        for (int i = 0; i < 2; i++) {
            String specName = (String) specialPick.get(i).getSelectedItem();
            DataSpecialty base = dataQuery.getSpecialtyByName(specName);
            if (base == null) continue;
            DataSpecialty copy = new DataSpecialty(base);
            character.getSpecials().addTrainedSpecialty(copy);
        }

        parent.specialsConfirmed();
        dispose();
    }
}
