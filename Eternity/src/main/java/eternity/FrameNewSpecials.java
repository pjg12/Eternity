package eternity;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialty selection window: pick 2 specialties by type.
 */
public class FrameNewSpecials extends JFrame {
    private static final long serialVersionUID = 1L;

    private final StoreRuleManager dataQuery;
    private final StoreCharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private static final String[] SPECTYPES = {"***", "Proficiency", "Martial", "Class"};
    private static final String EMPTY_OPTION = "***";

    private final JComboBox<String>[] specialType = new JComboBox[2];
    private final JComboBox<String>[] specialPick = new JComboBox[2];
    private final String[] lastSelectedType = new String[2];
    private final Map<String, String[]> specialtyOptionsByType = new HashMap<>();

    public FrameNewSpecials(FrameSheet sheetFrame, StoreRuleManager dataQuery, StoreCharData character, FrameNew parent, boolean gmMode) {
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
            specialType[i] = typeBox;
            add(typeBox);

            JComboBox<String> specBox = new JComboBox<>();
            specBox.addItem(EMPTY_OPTION);
            specBox.setBounds(225, 100 + 50 * i, 250, 20);
            specialPick[i] = specBox;
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
        JComboBox<String> typeBox = specialType[k];
        JComboBox<String> specBox = specialPick[k];

        String selectedType = (String) typeBox.getSelectedItem();
        if (selectedType == null) selectedType = EMPTY_OPTION;
        String previousSpecial = (String) specBox.getSelectedItem();

        String otherPick = null;
        for (int i = 0; i < specialPick.length; i++) {
            if (i == k) continue;
            String val = (String) specialPick[i].getSelectedItem();
            if (val != null && !EMPTY_OPTION.equals(val)) otherPick = val;
        }

        if (selectedType.equals(lastSelectedType[k]) && isSpecialStillAllowed(previousSpecial, otherPick)) {
            return;
        }
        lastSelectedType[k] = selectedType;

        specBox.removeAllItems();
        specBox.addItem(EMPTY_OPTION);

        for (String option : getSpecialtyOptions(selectedType)) {
            if (otherPick != null && option.equalsIgnoreCase(otherPick)) continue;
            specBox.addItem(option);
        }

        if (isSpecialStillAllowed(previousSpecial, otherPick)) {
            specBox.setSelectedItem(previousSpecial);
        }
    }

    private void specialsConfirm() {
        if (gmMode) {
            specialType[0].setSelectedItem("Martial");
            specialType[1].setSelectedItem("Martial");
            updateSpecialPick(0);
            updateSpecialPick(1);
            specialPick[0].setSelectedItem("Specialization (Blade)");
            specialPick[1].setSelectedItem("Specialization (Sword)");
        }

        for (int i = 0; i < 2; i++) {
            String type = (String) specialType[i].getSelectedItem();
            String spec = (String) specialPick[i].getSelectedItem();
            if (type == null || EMPTY_OPTION.equals(type) || spec == null || EMPTY_OPTION.equals(spec)) {
                JOptionPane.showMessageDialog(this, "Select a type and specialty for both choices.");
                return;
            }
        }

        for (int i = 0; i < 2; i++) {
            String specName = (String) specialPick[i].getSelectedItem();
            DataSpecialty base = dataQuery.getSpecialtyByName(specName);
            if (base == null) continue;
            DataSpecialty copy = new DataSpecialty(base);
            character.getSpecials().addTrainedSpecialty(copy);
        }

        parent.specialsConfirmed();
        dispose();
    }

    private String[] getSpecialtyOptions(String type) {
        return specialtyOptionsByType.computeIfAbsent(type, this::buildSpecialtyOptions);
    }

    private String[] buildSpecialtyOptions(String type) {
        List<DataSpecialty> options = dataQuery.getSpecialtiesByType(type);
        if (options == null || options.isEmpty()) {
            return new String[0];
        }
        String[] names = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            names[i] = options.get(i).getName();
        }
        return names;
    }

    private boolean isSpecialStillAllowed(String previousSpecial, String otherPick) {
        return previousSpecial != null
                && !EMPTY_OPTION.equals(previousSpecial)
                && (otherPick == null || !previousSpecial.equalsIgnoreCase(otherPick));
    }
}

