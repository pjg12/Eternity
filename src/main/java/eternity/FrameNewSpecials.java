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
    private static final String DIVINE_VOW_SPECIALTY = "Divine Vow";
    private static final String STANCE_SPECIALTY = "Stance";

    private final StoreRuleManager dataQuery;
    private final StoreCharData character;
    private final FrameNew parent;
    private final boolean gmMode;

    private static final String[] SPECTYPES = {"***", "Martial", "Skill", "Class"};
    private static final String EMPTY_OPTION = "***";

    private final JComboBox<String>[] specialType = new JComboBox[2];
    private final JComboBox<String>[] specialPick = new JComboBox[2];
    private final JComboBox<String>[] specialSubtype = new JComboBox[2];
    private final String[] lastSelectedType = new String[2];
    private final Map<String, String[]> specialtyOptionsByType = new HashMap<>();
    private final String[] vowOptions;
    private JLabel subtypeLabel;

    public FrameNewSpecials(FrameSheet sheetFrame, StoreRuleManager dataQuery, StoreCharData character, FrameNew parent, boolean gmMode) {
        super("Specialty Select");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;
        this.vowOptions = buildVowOptions();

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(560, 280);
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
        typeLabel.setBounds(20, 60, 120, 20);
        add(typeLabel);

        JLabel specLabel = new JLabel("Specialty");
        specLabel.setBounds(155, 60, 190, 20);
        add(specLabel);

        subtypeLabel = new JLabel("Vow");
        subtypeLabel.setBounds(360, 60, 150, 20);
        subtypeLabel.setVisible(false);
        add(subtypeLabel);
    }

    private void buildPickers() {
        for (int i = 0; i < 2; i++) {
            int idx = i;

            JComboBox<String> typeBox = new JComboBox<>(SPECTYPES);
            typeBox.setBounds(20, 100 + 50 * i, 120, 20);
            typeBox.addActionListener(e -> updateSpecialPick(idx));
            specialType[i] = typeBox;
            add(typeBox);

            JComboBox<String> specBox = new JComboBox<>();
            specBox.addItem(EMPTY_OPTION);
            specBox.setBounds(155, 100 + 50 * i, 190, 20);
            specBox.addActionListener(e -> updateSubtypePick(idx));
            specialPick[i] = specBox;
            add(specBox);

            JComboBox<String> subtypeBox = new JComboBox<>();
            subtypeBox.addItem(EMPTY_OPTION);
            subtypeBox.setBounds(360, 100 + 50 * i, 150, 20);
            subtypeBox.setVisible(false);
            specialSubtype[i] = subtypeBox;
            add(subtypeBox);
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
        updateSubtypePick(k);
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
            if (isDivineVowSelection(i)) {
                String subtype = (String) specialSubtype[i].getSelectedItem();
                if (subtype == null || EMPTY_OPTION.equals(subtype)) {
                    JOptionPane.showMessageDialog(this, "Select a vow for Divine Vow.");
                    return;
                }
            }
        }

        ArrayList<DataSpecialty> picks = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            DataSpecialty resolved = resolveSelectedSpecialty(i);
            if (resolved == null) {
                return;
            }
            picks.add(resolved);
        }

        ArrayList<DataSkill> grantedSkills = new ArrayList<>();
        for (DataSpecialty specialty : picks) {
            int grantedSkillCount = FrameSpecial.resolveGrantedSkillCount(dataQuery, specialty);
            List<DataSkill> specialtySkills = FrameSkill.promptForTrainingSkills(this, dataQuery, character, grantedSkillCount, grantedSkills);
            if (specialtySkills == null) {
                return;
            }
            grantedSkills.addAll(specialtySkills);
        }

        for (DataSpecialty specialty : picks) {
            character.getSpecials().addTrainedSpecialty(specialty);
        }
        for (DataSkill grantedSkill : grantedSkills) {
            character.getSpecials().addSkill(grantedSkill);
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

    private DataSpecialty resolveSelectedSpecialty(int row) {
        String specName = (String) specialPick[row].getSelectedItem();
        DataSpecialty base = dataQuery.getSpecialtyByName(specName);
        if (base == null) return null;

        if (isDivineVowSelection(row)) {
            String vowName = (String) specialSubtype[row].getSelectedItem();
            if (vowName != null && !vowName.isBlank() && !EMPTY_OPTION.equals(vowName)) {
                DataSpecialty resolved = new DataSpecialty(base);
                resolved.setRefName(vowName);
                return resolved;
            }
        }

        return FrameSpecialsPicker.resolveSpecialtyChoice(this, dataQuery, character, base);
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
        if (specialty == null || specialty.getPrereq() < 0 || isCurrentClassSpecialty(specialty)) return false;
        String name = specialty.getName();
        if (name == null || name.isBlank()) return false;
        if (STANCE_SPECIALTY.equalsIgnoreCase(name)
                && FrameSpecialsPicker.getAvailableStanceOptions(character).isEmpty()) {
            return false;
        }
        String excludedCreationSpecialty = getExcludedCreationSpecialtyName();
        if (excludedCreationSpecialty != null && excludedCreationSpecialty.equalsIgnoreCase(name)) return false;
        if (lacksRequiredSpecialty(specialty)) return false;
        return character == null
                || character.getSpecials() == null
                || CharSpecials.isRepeatableSpecialty(specialty)
                || !character.getSpecials().hasSpecialty(name);
    }

    private boolean lacksRequiredSpecialty(DataSpecialty specialty) {
        if (specialty == null || character == null || character.getSpecials() == null) return false;
        int prereqId = specialty.getPrereq();
        if (prereqId <= 0) return false;

        DataSpecialty prereqSpecialty = dataQuery.getSpecialtyById(prereqId);
        if (prereqSpecialty == null) return false;

        String prereqName = prereqSpecialty.getName();
        return prereqName != null
                && !prereqName.isBlank()
                && !character.getSpecials().hasSpecialty(prereqName);
    }

    private String getExcludedCreationSpecialtyName() {
        if (character == null || character.getIdentity() == null) return null;
        if (!"Warrior".equalsIgnoreCase(character.getIdentity().getCharClass())) return null;

        List<String> classPicks = character.getIdentity().getCharClassPick();
        if (classPicks == null || classPicks.isEmpty()) return null;

        String warriorSpecialty = classPicks.get(0);
        if (warriorSpecialty == null || warriorSpecialty.isBlank()) return null;
        return warriorSpecialty;
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

    private void updateSubtypePick(int row) {
        JComboBox<String> subtypeBox = specialSubtype[row];
        if (subtypeBox == null) return;

        String previousSubtype = (String) subtypeBox.getSelectedItem();
        subtypeBox.removeAllItems();
        subtypeBox.addItem(EMPTY_OPTION);

        boolean showVowSubtype = isDivineVowSelection(row) && vowOptions.length > 0;
        if (showVowSubtype) {
            for (String option : vowOptions) {
                if (option != null && !option.isBlank() && !EMPTY_OPTION.equals(option)) {
                    subtypeBox.addItem(option);
                }
            }
            subtypeBox.setVisible(true);
            if (previousSubtype != null) {
                subtypeBox.setSelectedItem(previousSubtype);
            }
        } else {
            subtypeBox.setVisible(false);
        }

        refreshSubtypeLabelVisibility();
    }

    private void refreshSubtypeLabelVisibility() {
        if (subtypeLabel == null) return;
        boolean showLabel = false;
        for (JComboBox<String> subtypeBox : specialSubtype) {
            if (subtypeBox != null && subtypeBox.isVisible()) {
                showLabel = true;
                break;
            }
        }
        subtypeLabel.setVisible(showLabel);
    }

    private boolean isDivineVowSelection(int row) {
        if (row < 0 || row >= specialPick.length) return false;
        String specialtyName = (String) specialPick[row].getSelectedItem();
        return DIVINE_VOW_SPECIALTY.equalsIgnoreCase(specialtyName);
    }

    private String[] buildVowOptions() {
        List<String> names = new ArrayList<>();
        for (DataVow vow : dataQuery.getVowData()) {
            if (vow == null || vow.getName() == null || vow.getName().isBlank()) continue;
            names.add(vow.getName().trim());
        }
        return names.toArray(new String[0]);
    }
}
