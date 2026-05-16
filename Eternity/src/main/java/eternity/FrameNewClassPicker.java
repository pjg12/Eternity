// CHECKED

package eternity;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Allows the user to make class-based choices during character creation
 */
public class FrameNewClassPicker extends JFrame {

    // References
    private final StoreRuleManager ruleManager;
    private final StoreCharData character;
    private final FrameNewClass parent;
    private final DataClass selectedClass;
    private final boolean gmMode;

    // UI Constants
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder CENTER_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final int[] GB_COLUMN_WIDTHS = new int[] { 250, 250 };
    private static final Insets CENTER_INSETS = new Insets(10, 10, 10, 10);
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 420;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int BUTTON_SPACING = 10;

    // UI Strings
    private static final String WINDOW_TITLE = " Options";
    private static final String HEADER_TEXT = " Customization";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_CONFIRM = "Confirm";
    private static final String EMPTY_OPTION = "***";

    // UI Elements
    private JPanel headerPanel, centerPanel, footerPanel;
    private JLabel headerL;
    private JLabel[] optionLabels;
    private JComboBox<String>[] optionBoxes;
    private JButton cancelButton, confirmButton;

    // Maps
    private final Map<String, ChoiceConfig> classChoicesMap;
    private final Map<String, JComboBox<String>> fields = new LinkedHashMap<>();
    private final Map<String, String[]> deityDomains = new HashMap<>();
    private final Map<String, String[]> archerSelections = new HashMap<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public FrameNewClassPicker(StoreRuleManager ruleManager, StoreCharData character, DataClass selectedClass, FrameNewClass parent, boolean gmMode) {
        super(WINDOW_TITLE);
        this.ruleManager = ruleManager;
        this.character = character;
        this.selectedClass = selectedClass;
        this.parent = parent;
        this.gmMode = gmMode;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(BUTTON_SPACING, BUTTON_SPACING));

        this.classChoicesMap = makeChoiceMap(selectedClass);
        this.optionLabels = new JLabel[classChoicesMap.keySet().size()];
        this.optionBoxes = (JComboBox<String>[]) new JComboBox[classChoicesMap.size()];

        buildUI();

        if (gmMode) {
            applyGmSelectionsAndConfirm();
        }
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        buildHeader();
        buildCenter();
        buildFooter();
    }

    private void buildHeader() {
        // Build panel
        headerPanel = new JPanel(new BorderLayout());

        // Build header
        headerL = new JLabel(selectedClass.getName() + HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setBorder(HEADER_BORDER);

        // Add elements
        headerPanel.add(headerL, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void buildCenter() {
        // Build panel
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = GB_COLUMN_WIDTHS;
        centerPanel = new JPanel(layout);
        centerPanel.setBorder(CENTER_BORDER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = CENTER_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Setup Variables
        int tileIndex = 0;
        int y;
        int x;
        int width;

        for (String label : classChoicesMap.keySet()) {
            // Setup Grid
            y = tileIndex / 2;
            x = tileIndex % 2;
            width = 1;
            gridHelper(gbc, y, x, width);

            // Build panel
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
            choicePanel.setBorder(CENTER_BORDER);

            // Build Label
            JLabel lbl = buildLabel(label);
            choicePanel.add(lbl);
            optionLabels[tileIndex] = lbl;

            // Build Choice
            ChoiceConfig choice = classChoicesMap.get(label);
            JComboBox<String> choiceBox = buildComboBox(choice);
            choicePanel.add(choiceBox);
            optionBoxes[tileIndex] = choiceBox;
            
            centerPanel.add(choicePanel, gbc);
            fields.put(label, choiceBox);
            tileIndex++;
        }
        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildFooter() {
        // Build panel
        footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));

        // Build buttons
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        confirmButton = new JButton(BUTTON_CONFIRM);
        confirmButton.addActionListener(e -> onConfirmPressed());

        // Add buttons
        footerPanel.add(cancelButton);
        footerPanel.add(confirmButton);

        // Add panels
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JComboBox<String> buildComboBox(ChoiceConfig cfg) {
        // Add empty choice
        JComboBox<String> box = new JComboBox<>();
        box.addItem(EMPTY_OPTION);

        // Add choices
        switch (cfg.type) {
            case STATIC, SPECIAL_LIST, SUBCLASS, WEAPON_PICK_1, WEAPON_PICK_2 -> addOptions(box, cfg.options);
            case DEITY -> addOptions(box, DEITY_OPTIONS_WITHOUT_EMPTY);
            case DOMAIN_DEPENDENT -> {
                JComboBox<String> deityBox = fields.get("Deity");
                if (deityBox != null) deityBox.addActionListener(e -> updateDomainBox());
                else                 System.out.println("HI");
            }
            case FAVOR_DEPENDENT -> {
                JComboBox<String> favorTypeBox = fields.get("Favor Type");
                if (favorTypeBox != null) favorTypeBox.addActionListener(e -> updateFavoredBox());
            }
        }
        return box;
    }

    private JLabel buildLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------

    private void onCancelPressed() {
        dispose();
    }

    public void onConfirmPressed() {
        int profCount = (int) classChoicesMap.values().stream().filter(cfg -> cfg.type.isProficiency()).count();
        List<String> classChoices = new ArrayList<>(classChoicesMap.size() - profCount);
        List<String> profs = new ArrayList<>(profCount + 1);

        for (String label : classChoicesMap.keySet()) {
            // Get choice
            ChoiceConfig choice = classChoicesMap.get(label);
            String value = (String) fields.get(label).getSelectedItem();

            if (value == null || value.equals(EMPTY_OPTION)) {
                JOptionPane.showMessageDialog(this, "Please complete all fields.");
                return;
            }

            if (choice.type.isProficiency()) profs.add(value);
            else if (choice.type != ChoiceType.SUBCLASS) classChoices.add(value);
        }

        // Cleric deity bonus
        if (selectedClass.getName().equals("Cleric")) {
            String deity = classChoices.get(0);
            int idx = DEITY_OPTIONS.indexOf(deity);
            if (idx >= 0) profs.add(DEITY_WEAPONS.get(idx));
        }

        // Finalize proficiencies
        List<String> finalProfs = new ArrayList<>(selectedClass.getProfAuto());
        finalProfs.addAll(profs);

        // Add choices to character
        JComboBox<String> subclassBox = fields.get("Subclass");
        if (subclassBox != null) {
            String subclass = (String) subclassBox.getSelectedItem();
            if (subclass != null && !EMPTY_OPTION.equals(subclass)) {
                character.getIdentity().setCharSubclass(subclass);
            }
        }
        character.getIdentity().setCharClassPick(classChoices);
        character.getInventory().setWeaponProficiencies(finalProfs);

        // Pass control back and close
        parent.classChoicesConfirmed();
        dispose();
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private void gridHelper (GridBagConstraints gbc, int y, int x, int width) {
        gbc.gridwidth = width;
        gbc.gridy = y;
        gbc.gridx = x;
    }

    private Map<String, ChoiceConfig> makeChoiceMap(DataClass cls) {
        // Generate new map
        Map<String, ChoiceConfig> map = new LinkedHashMap<>();
        String name = cls.getName();

        // Input options based on class name
        switch (name) {
            case "Paladin" -> {
                map.put("Deity", makeChoice(ChoiceType.DEITY));
                map.put("Vow", makeChoiceFixed(VOWOPTIONS));
                map.put("Domain", makeChoice(ChoiceType.DOMAIN_DEPENDENT));
                map.put("Weapon Proficiency", makeChoiceWeapon(WeaponPools.AURA));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Cleric" -> {
                map.put("Deity", makeChoice(ChoiceType.DEITY));
                map.put("Domain", makeChoice(ChoiceType.DOMAIN_DEPENDENT));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Warrior" -> {
                map.put("Specialty", makeChoiceFiltered("Martial"));
                map.put("Weapon Proficiency", makeChoiceWeapon(WeaponPools.RANGED));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Rogue" -> {
                map.put("Weapon Proficiency 1", makeChoiceWeapon(WeaponPools.LIGHT_RANGED));
                map.put("Weapon Proficiency 2", makeChoiceWeapon(WeaponPools.LIGHT_AURA));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Monk" -> {
                map.put("Discipline", makeChoiceFixed(DISCIPLINES));
                map.put("Weapon Proficiency 1", makeChoiceWeapon(WeaponPools.RANGED));
                map.put("Weapon Proficiency 2", makeChoiceWeapon(WeaponPools.AURA));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Archer" -> {
                map.put("Favor Type", makeChoiceFixed(FAVOR_TYPES));
                map.put("Favored Selection", makeChoice(ChoiceType.FAVOR_DEPENDENT));
                map.put("Weapon Proficiency", makeChoiceWeapon(WeaponPools.MELEE));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Leader" -> {
                map.put("Weapon Proficiency", makeChoiceWeapon(WeaponPools.AURA));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Caster" -> {
                map.put("Weapon Proficiency", makeChoiceWeapon(WeaponPools.MELEE));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Shifter" -> {
                map.put("Melee Affinity", makeChoiceFixed(SHIFTER_MELEE_ATTS));
                map.put("Ranged Affinity", makeChoiceFixed(SHIFTER_RANGED_ATTS));
                map.put("Weapon Mold 1", makeChoiceWeapon(WeaponPools.MELEE));
                map.put("Weapon Mold 2", makeChoiceWeapon(WeaponPools.SHIFTER_MOLD_2));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
            case "Pilot" -> {
                map.put("Primary Attribute", makeChoiceFixed(PRIM_ATTS));
                map.put("Subclass", makeChoiceSubclass(cls));
            }
        }
        return map;
    }

    private ChoiceConfig makeChoice(ChoiceType type) {
        return new ChoiceConfig(type);
    }

    private ChoiceConfig makeChoiceFixed(String[] vals) {
        ChoiceConfig choice = new ChoiceConfig(ChoiceType.STATIC);
        choice.options = vals;
        return choice;
    }

    private ChoiceConfig makeChoiceFiltered(String filter) {
        ChoiceConfig choice = new ChoiceConfig(ChoiceType.SPECIAL_LIST);
        List<DataSpecialty> options = ruleManager.getSpecialtiesByType(filter);
        choice.options = options == null ? new String[0]
                : options.stream()
                        .filter(spec -> spec != null && spec.getPrereq() == 0)
                        .filter(spec -> !isCurrentClassSpecialty(spec))
                        .map(DataSpecialty::getName)
                        .filter(Objects::nonNull)
                        .toArray(String[]::new);
        return choice;
    }

    private boolean isCurrentClassSpecialty(DataSpecialty specialty) {
        if (specialty == null || selectedClass == null) return false;
        int family = resolveClassSpecialtyFamily(selectedClass);
        if (family <= 0) return false;
        int specialtyId = specialty.getId();
        int familyStart = family * 1000;
        return specialtyId >= familyStart && specialtyId < familyStart + 1000;
    }

    private int resolveClassSpecialtyFamily(DataClass dataClass) {
        if (dataClass == null || dataClass.getID() <= 0) return -1;
        return ((dataClass.getID() - 1) / 3) + 1;
    }

    private ChoiceConfig makeChoiceWeapon(String[] pool) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.WEAPON_PICK_1);
        c.options = pool;
        return c;
    }

    private ChoiceConfig makeChoiceSubclass(DataClass cls) {
        ChoiceConfig choice = new ChoiceConfig(ChoiceType.SUBCLASS);
        int classID = cls.getID();
        DataClass sub1 = ruleManager.getClassById(classID + 1);
        DataClass sub2 = ruleManager.getClassById(classID + 2);
        choice.options = new String[] {
                sub1 != null ? sub1.getName() : EMPTY_OPTION,
                sub2 != null ? sub2.getName() : EMPTY_OPTION
        };
        return choice;
    }

    private void addOptions(JComboBox<String> box, String[] options) {
        if (options == null) return;
        for (String option : options) {
            if (option != null && !EMPTY_OPTION.equals(option)) {
                box.addItem(option);
            }
        }
    }

    private void updateDomainBox() {
        // Get Combobox references
        JComboBox<String> deity = fields.get("Deity");
        JComboBox<String> domain = fields.get("Domain");
        if (domain == null) return;

        
        // Clear domain box
        domain.removeAllItems();
        domain.addItem(EMPTY_OPTION);

        // Get deity
        String deityName = deity != null ? (String) deity.getSelectedItem() : null;

        // Update domain box per deity
        addOptions(domain, deityDomains.computeIfAbsent(deityName, this::findDomainsByDeity));
    }

    private void updateFavoredBox() {
        // Get Combobox references
        JComboBox<String> favorType = fields.get("Favor Type");
        JComboBox<String> favoredSelection = fields.get("Favored Selection");
        if (favoredSelection == null) return;

        // Clear favored box
        favoredSelection.removeAllItems();
        favoredSelection.addItem(EMPTY_OPTION);

        // Get favored type
        String favorTypeName = favorType != null ? (String) favorType.getSelectedItem() : null;

        // Update favored box per favored type
        addOptions(favoredSelection, archerSelections.computeIfAbsent(favorTypeName, this::findFavoredByType));
    }

    private void applyGmSelectionsAndConfirm() {
        for (String label : classChoicesMap.keySet()) {
            JComboBox<String> box = fields.get(label);
            if (box == null || box.getItemCount() <= 1) continue;
            box.setSelectedIndex(randomChoiceIndex(box));
        }
        onConfirmPressed();
    }

    private int randomChoiceIndex(JComboBox<String> box) {
        int nonEmptyOptions = box.getItemCount() - 1;
        if (nonEmptyOptions <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(nonEmptyOptions) + 1;
    }

    private String[] findDomainsByDeity(String deityName) {
        if (deityName == null || EMPTY_OPTION.equals(deityName)) return new String[0];

        // Get Deity entry
        DataDeity deity = ruleManager.getDeityByName(deityName);
        if (deity == null) return new String[0];
        
        // Generate domain list
        List<String> resolved = new ArrayList<>();
        for (String entry : deity.getDomains()) {
            if (entry == null || entry.isBlank()) continue;

            // Split by colon
            String[] split = entry.split(":");
            for (String domain : split) {
                if (domain != null) {
                    String trimmed = domain.trim();
                    if (!trimmed.isBlank()) {
                        resolved.add(trimmed);
                    }
                }
            }
        }
        return resolved.toArray(new String[0]);
    }

    private String[] findFavoredByType(String favorTypeName) {
        if (favorTypeName == null || EMPTY_OPTION.equals(favorTypeName)) return new String[0];
        if ("Enemy".equalsIgnoreCase(favorTypeName)) return ARCHER_ENEMY;
        if ("Terrain".equalsIgnoreCase(favorTypeName)) return ARCHER_TERRAIN;
        return new String[0];
    }

    // -------------------------------------------------------------------------
    //  ChoiceType
    // -------------------------------------------------------------------------

    private enum ChoiceType {
        STATIC,
        DEITY,
        DOMAIN_DEPENDENT,
        FAVOR_DEPENDENT,
        SPECIAL_LIST,
        SUBCLASS,
        WEAPON_PICK_1,
        WEAPON_PICK_2;

        public boolean isProficiency() {
            return this == WEAPON_PICK_1 || this == WEAPON_PICK_2;
        }
    }

    private static class ChoiceConfig {
        ChoiceType type;
        String[] options;

        ChoiceConfig(ChoiceType type) {
            this.type = type;
        }
    }

    // -------------------------------------------------------------------------
    //  Static Option Lists
    // -------------------------------------------------------------------------

    private static final List<String> DEITY_OPTIONS = List.of("***","Creation","Honor","Justice","Courage","Progress","Providence","Grace","Hope","Mercy","*Custom");
    private static final String[] DEITY_OPTIONS_WITHOUT_EMPTY = DEITY_OPTIONS.stream().filter(option -> !EMPTY_OPTION.equals(option)).toArray(String[]::new);
    private static final List<String> DEITY_WEAPONS = List.of("***","Sword","Whip","Axe","Fist","Polearm","Greatsword","Bow","Dagger","Battleaxe","*Custom");
    private static final String[] VOWOPTIONS = {"***","Honesty","Law","Justice","Faith","Courage","Wisdom","Charity","Hope","Mercy","Forgiveness","Humility","Gratitude","Patience","Duty","*Undecided"};
    private static final String[] COMBAT_ACTIONS = {"***","Bull Rush","Shove","Overrun","Grapple","Trip","Disarm","Sunder","Charge","Feint"};
    private static final String[] DISCIPLINES = {"***", "Mobility", "Avoidance", "Martial"};
    private static final String[] FAVOR_TYPES = {"***", "Enemy", "Terrain"};
    private static final String[] ARCHER_ENEMY = {"***","Animals (Land)", "Animalas (Sea)", "Animals (Air)", "Constructs (Mechanical)", "Constructs (Organic)", "Dragons", "Elementals", "Fey", "Outsiders", "Plants", "Undead", "Wardens", "Ardians..."};
    private static final String[] ARCHER_TERRAIN = {"***","Plains","Forest","Mountains","Hills","Swamp","Underground","Urban","Coastal","Arctic"};
    private static final String[] AURA_TYPES = {"***","Reinforcement","Body","Force","Metal","Fire","Water","Air","Earth","Electricity","Energy","Sound","Light","Nature","Poison","Darkness","Psionic","Spirit","Time"};
    private static final String[] PRIM_ATTS = {"STR","DEX","FOC","CTL"};
    private static final String[] SHIFTER_MELEE_ATTS = {"STR","DEX"};
    private static final String[] SHIFTER_RANGED_ATTS = {"FOC","CTL"};

    // Weapons
    private static class WeaponPools {
        static final String[] MELEE = {"Greatsword","Battleaxe","Warhammer","Polearm","Sword","Axe","Mace","Shield","Blade","Dagger","Knuckle","Whip"};
        static final String[] RANGED = {"Bow","Crossbow","Rifle","Cannon","Thrown","Sling","Handbow","Pistol"};
        static final String[] AURA = {"Staff","Tome","Relic","Symbol","Ring","Orb","Wand","Talisman"};
        static final String[] LIGHT_RANGED = {"Thrown","Sling","Handbow","Pistol"};
        static final String[] LIGHT_AURA    = {"Ring","Orb","Wand","Talisman"};
        static final String[] SHIFTER_MOLD_2 = {"Bow","Crossbow","Rifle","Cannon","Thrown","Sling","Handbow","Pistol","Staff","Tome","Relic","Symbol","Ring","Orb","Wand","Talisman"};
    }
}
