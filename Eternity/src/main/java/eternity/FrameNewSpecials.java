package eternity;

import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

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
            if (!grantRandomGmSpecialties()) {
                JOptionPane.showMessageDialog(this, "No specialties are available for GM random selection.");
                return;
            }
            parent.setStepConfirmed(4);
            dispose();
            return;
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

        parent.setStepConfirmed(4);
        dispose();
    }

    private boolean grantRandomGmSpecialties() {
        List<DataSpecialty> available = collectAvailableSpecialties();
        if (available.isEmpty()) return false;

        int picksToGrant = Math.min(2, available.size());
        for (int i = 0; i < picksToGrant; i++) {
            int pickIndex = ThreadLocalRandom.current().nextInt(available.size());
            DataSpecialty selected = available.remove(pickIndex);
            character.getSpecials().addTrainedSpecialty(new DataSpecialty(selected));
        }
        return picksToGrant > 0;
    }

    private List<DataSpecialty> collectAvailableSpecialties() {
        Map<String, DataSpecialty> uniqueByName = new LinkedHashMap<>();
        for (String type : SPECTYPES) {
            if (type == null || EMPTY_OPTION.equals(type)) continue;
            List<DataSpecialty> options = dataQuery.getSpecialtiesByType(type);
            if (options == null) continue;
            for (DataSpecialty spec : options) {
                if (!isAvailableSpecialty(spec)) continue;
                String name = spec.getName();
                if (name == null || name.isBlank()) continue;
                uniqueByName.putIfAbsent(name.toLowerCase(), spec);
            }
        }
        return new ArrayList<>(uniqueByName.values());
    }

    private boolean isAvailableSpecialty(DataSpecialty specialty) {
        if (specialty == null || specialty.getPrereq() != 0 || isCurrentClassSpecialty(specialty)) return false;
        String name = specialty.getName();
        if (name == null || name.isBlank()) return false;
        return character == null || character.getSpecials() == null || !character.getSpecials().hasSpecialty(name);
    }

    private String[] getSpecialtyOptions(String type) {
        return specialtyOptionsByType.computeIfAbsent(type, this::buildSpecialtyOptions);
    }

    private String[] buildSpecialtyOptions(String type) {
        List<DataSpecialty> options = dataQuery.getSpecialtiesByType(type);
        if (options == null || options.isEmpty()) {
            return new String[0];
        }
        options = options.stream()
                .filter(this::isAvailableSpecialty)
                .toList();
        String[] names = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            names[i] = options.get(i).getName();
        }
        return names;
    }

    private boolean isCurrentClassSpecialty(DataSpecialty specialty) {
        if (specialty == null || character == null || character.getIdentity() == null) return false;
        DataClass cls = resolveCurrentClass();
        if (cls == null) return false;
        int family = resolveClassSpecialtyFamily(cls);
        if (family <= 0) return false;
        int specialtyId = specialty.getId();
        int familyStart = family * 1000;
        return specialtyId >= familyStart && specialtyId < familyStart + 1000;
    }

    private DataClass resolveCurrentClass() {
        String subclass = character.getIdentity().getCharSubclass();
        DataClass cls = dataQuery.getClassByName(subclass);
        if (cls != null) return cls;
        return dataQuery.getClassByName(character.getIdentity().getCharClass());
    }

    private int resolveClassSpecialtyFamily(DataClass dataClass) {
        if (dataClass == null || dataClass.getID() <= 0) return -1;
        return ((dataClass.getID() - 1) / 3) + 1;
    }

    private boolean isSpecialStillAllowed(String previousSpecial, String otherPick) {
        return previousSpecial != null
                && !EMPTY_OPTION.equals(previousSpecial)
                && (otherPick == null || !previousSpecial.equalsIgnoreCase(otherPick));
    }
}
