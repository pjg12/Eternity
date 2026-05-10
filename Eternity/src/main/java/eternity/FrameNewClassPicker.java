package eternity;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Fully data-driven class picker with simple String keys instead of ClassChoice objects.
 */
public class FrameNewClassPicker extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String EMPTY_OPTION = "***";
    private final StoreRuleManager dataQuery;
    private final StoreCharData character;
    private final FrameNewClass parent;
    private final DataClass selectedClass;

    // Field UI elements (label → combobox)
    private final Map<String, JComboBox<String>> fields = new LinkedHashMap<>();
    private final Map<String, String[]> deityDomains = new HashMap<>();
    private final Map<String, String[]> archerSelections = new HashMap<>();

    // Model: label → choice configuration
    private Map<String, ChoiceConfig> choiceModel;

    // Buttons
    private JButton clearButton;
    private JButton acceptButton;
    private JLabel headerLabel;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public FrameNewClassPicker(StoreRuleManager dataQuery,
                               StoreCharData character, DataClass selectedClass,
                               FrameNewClass parent) {

        super("Class Options");

        this.dataQuery = dataQuery;
        this.character = character;
        this.selectedClass = selectedClass;
        this.parent = parent;

        setSize(640, 420);
        setLayout(null);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildHeader();
        buildButtons();

        // Build simplified model
        this.choiceModel = buildChoiceModel(selectedClass);

        // Render auto-generated UI
        renderChoices();

        setVisible(true);
    }

    // -------------------------------------------------------------------------
    //  Header + Buttons
    // -------------------------------------------------------------------------
    private void buildHeader() {
        headerLabel = new JLabel(selectedClass.getName() + " Options", SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 22f));
        headerLabel.setBounds(10, 10, 600, 40);
        add(headerLabel);
    }

    private void buildButtons() {
        clearButton = new JButton("Clear");
        clearButton.setBounds(120, 330, 150, 28);
        clearButton.addActionListener(e -> clearAndClose());
        add(clearButton);

        acceptButton = new JButton("Accept");
        acceptButton.setBounds(350, 330, 150, 28);
        acceptButton.addActionListener(e -> acceptChoices());
        add(acceptButton);
    }

    // -------------------------------------------------------------------------
    //  Choice Model (String → config)
    // -------------------------------------------------------------------------

    private Map<String, ChoiceConfig> buildChoiceModel(DataClass cls) {

        Map<String, ChoiceConfig> map = new LinkedHashMap<>();
        String name = cls.getName();

        switch (name) {

            case "Paladin" -> {
                map.put("Deity", cfg(ChoiceType.DEITY));
                map.put("Vow", cfgStatic(VOWOPTIONS));
                map.put("Domain", cfg(ChoiceType.DOMAIN_DEPENDENT));
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.AURA));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Cleric" -> {
                map.put("Deity", cfg(ChoiceType.DEITY));
                map.put("Domain", cfg(ChoiceType.DOMAIN_DEPENDENT));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Warrior" -> {
                map.put("Specialty", cfgSpecial("Martial"));
                map.put("Combat Action", cfgStatic(COMBAT_ACTIONS));
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.RANGED));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Rogue" -> {
                map.put("Weapon Proficiency 1", cfgWeapon(WeaponPools.LIGHT_RANGED));
                map.put("Weapon Proficiency 2", cfgWeapon(WeaponPools.LIGHT_AURA));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Monk" -> {
                map.put("Discipline", cfgStatic(DISCIPLINES));
                map.put("Weapon Proficiency 1", cfgWeapon(WeaponPools.RANGED));
                map.put("Weapon Proficiency 2", cfgWeapon(WeaponPools.AURA));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Archer" -> {
                map.put("Favor Type", cfgStatic(FAVOR_TYPES));
                map.put("Favored Selection", cfg(ChoiceType.FAVOR_DEPENDENT));
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.MELEE));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Leader" -> {
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.AURA));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Caster" -> {
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.MELEE));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Shifter" -> {
                map.put("Melee Affinity", cfgStatic(SHIFTER_MELEE_ATTS));
                map.put("Ranged Affinity", cfgStatic(SHIFTER_RANGED_ATTS));
                map.put("Weapon Mold 1", cfgWeapon(WeaponPools.MELEE));
                map.put("Weapon Mold 2", cfgWeapon(WeaponPools.SHIFTER_MOLD_2));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Pilot" -> {
                map.put("Primary Attribute", cfgStatic(PRIM_ATTS));
                map.put("Subclass", cfgSubclass(cls));
            }
        }

        return map;
    }

    // -------------------------------------------------------------------------
    //  Simple factories for ChoiceConfig
    // -------------------------------------------------------------------------
    private ChoiceConfig cfg(ChoiceType type) {
        return new ChoiceConfig(type);
    }

    private ChoiceConfig cfgStatic(String[] vals) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.STATIC);
        c.options = vals;
        return c;
    }

    private ChoiceConfig cfgSpecial(String filter) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.SPECIAL_LIST);
        c.options = dataQuery.getSpecialtiesByType(filter)
                .stream()
                .map(DataSpecialty::getName)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        return c;
    }

    private ChoiceConfig cfgWeapon(String[] pool) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.WEAPON_PICK_1);
        c.options = pool;
        return c;
    }

    private ChoiceConfig cfgSubclass(DataClass cls) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.SUBCLASS);
        int classID = cls.getID();
        DataClass sub1 = dataQuery.getClassById(classID + 1);
        DataClass sub2 = dataQuery.getClassById(classID + 2);
        c.options = new String[] {
                sub1 != null ? sub1.getName() : EMPTY_OPTION,
                sub2 != null ? sub2.getName() : EMPTY_OPTION
        };
        return c;
    }

    // -------------------------------------------------------------------------
    //  UI Rendering
    // -------------------------------------------------------------------------

    private void renderChoices() {
        int y = 70;
        boolean paladinLayout = selectedClass != null && "Paladin".equalsIgnoreCase(selectedClass.getName());
        boolean shifterLayout = selectedClass != null && "Shifter".equalsIgnoreCase(selectedClass.getName());

        for (String label : choiceModel.keySet()) {

            ChoiceConfig cfg = choiceModel.get(label);
            int x = 25;
            int currentY = y;
            boolean staysOnCurrentRow = false;

            if (paladinLayout && "Subclass".equals(label)) {
                x = 325;
                currentY = 70;
                staysOnCurrentRow = true;
            } else if (shifterLayout && "Ranged Affinity".equals(label)) {
                x = 325;
                currentY = 70;
                staysOnCurrentRow = true;
            }

            JLabel lbl = new JLabel(label);
            lbl.setBounds(x, currentY, 250, 20);
            add(lbl);

            JComboBox<String> box = new JComboBox<>();
            box.setBounds(x, currentY + 25, 260, 22);
            add(box);

            fields.put(label, box);

            initComboBox(label, cfg, box);

            if (!staysOnCurrentRow) {
                y += 65;
            }
        }
    }

    // -------------------------------------------------------------------------
    //  ComboBox Initialization
    // -------------------------------------------------------------------------

    private void initComboBox(String label, ChoiceConfig cfg, JComboBox<String> box) {

        box.addItem(EMPTY_OPTION);

        switch (cfg.type) {

            case STATIC, SPECIAL_LIST, SUBCLASS, WEAPON_PICK_1, WEAPON_PICK_2 -> addOptions(box, cfg.options);

            case DEITY -> addOptions(box, DEITY_OPTIONS_WITHOUT_EMPTY);

            case DOMAIN_DEPENDENT -> {
                JComboBox<String> deityBox = fields.get("Deity");
                if (deityBox != null) {
                    deityBox.addActionListener(e -> updateDomainBox());
                }
            }

            case FAVOR_DEPENDENT -> {
                JComboBox<String> favorTypeBox = fields.get("Favor Type");
                if (favorTypeBox != null) {
                    favorTypeBox.addActionListener(e -> updateFavoredSelectionBox());
                }
            }
        }
    }

    private void updateDomainBox() {

        JComboBox<String> deity = fields.get("Deity");
        JComboBox<String> domain = fields.get("Domain");

        if (domain == null) return;

        domain.removeAllItems();
        domain.addItem(EMPTY_OPTION);

        String deityName = deity != null ? (String) deity.getSelectedItem() : null;
        addOptions(domain, deityDomains.computeIfAbsent(deityName, this::resolveDomainsForDeity));
    }

    private void updateFavoredSelectionBox() {

        JComboBox<String> favorType = fields.get("Favor Type");
        JComboBox<String> favoredSelection = fields.get("Favored Selection");

        if (favoredSelection == null) return;

        favoredSelection.removeAllItems();
        favoredSelection.addItem(EMPTY_OPTION);

        String favorTypeName = favorType != null ? (String) favorType.getSelectedItem() : null;
        addOptions(favoredSelection,
                archerSelections.computeIfAbsent(favorTypeName, this::resolveArcherSelectionsForFavorType));
    }

    // -------------------------------------------------------------------------
    //  Accept Logic
    // -------------------------------------------------------------------------

    private void acceptChoices() {

        int profCount = (int) choiceModel.values().stream().filter(cfg -> cfg.type.isProficiency()).count();
        List<String> classChoices = new ArrayList<>(choiceModel.size() - profCount);
        List<String> profs = new ArrayList<>(profCount + 1);

        for (String label : choiceModel.keySet()) {

            ChoiceConfig cfg = choiceModel.get(label);
            JComboBox<String> box = fields.get(label);

            String value = (String) box.getSelectedItem();

            if (value == null || value.equals(EMPTY_OPTION)) {
                JOptionPane.showMessageDialog(this,
                        "Please complete all fields.");
                return;
            }

            if (cfg.type.isProficiency()) {
                profs.add(value);
            } else {
                classChoices.add(value);
            }
        }

        // Cleric deity bonus
        if (selectedClass.getName().equals("Cleric")) {
            String deity = classChoices.get(0);
            int idx = DEITY_OPTIONS.indexOf(deity);
            if (idx >= 0) profs.add(DEITY_WEAPONS.get(idx));
        }

        character.getIdentity().setCharClassPick(classChoices);

        List<String> finalProfs = new ArrayList<>(selectedClass.getProfAuto());
        finalProfs.addAll(profs);

        character.getInventory().setWeaponProficiencies(finalProfs);

        parent.classChoicesConfirmed();
        dispose();
    }

    private void clearAndClose() {
        dispose();
    }

    private void addOptions(JComboBox<String> box, String[] options) {
        if (options == null) return;
        for (String option : options) {
            if (option != null && !EMPTY_OPTION.equals(option)) {
                box.addItem(option);
            }
        }
    }

    private String[] resolveDomainsForDeity(String deityName) {
        if (deityName == null || EMPTY_OPTION.equals(deityName)) {
            return new String[0];
        }
        DataDeity deity = dataQuery.getDeityByName(deityName);
        if (deity == null) {
            return new String[0];
        }
        List<String> resolved = new ArrayList<>();
        for (String entry : deity.getDomains()) {
            if (entry == null || entry.isBlank()) continue;
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

    private String[] resolveArcherSelectionsForFavorType(String favorTypeName) {
        if (favorTypeName == null || EMPTY_OPTION.equals(favorTypeName)) {
            return new String[0];
        }
        if ("Enemy".equalsIgnoreCase(favorTypeName)) {
            return ARCHER_ENEMY;
        }
        if ("Terrain".equalsIgnoreCase(favorTypeName)) {
            return ARCHER_TERRAIN;
        }
        return new String[0];
    }

    // -------------------------------------------------------------------------
    //  ChoiceType Enum + Config Struct
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
    private static final List<String> DEITY_OPTIONS = List.of(
            "***","Creation","Honor","Justice","Courage","Progress",
            "Providence","Grace","Hope","Mercy","*Custom"
    );
    private static final String[] DEITY_OPTIONS_WITHOUT_EMPTY = DEITY_OPTIONS.stream()
            .filter(option -> !EMPTY_OPTION.equals(option))
            .toArray(String[]::new);

    private static final List<String> DEITY_WEAPONS = List.of(
            "***","Sword","Whip","Axe","Fist","Polearm",
            "Greatsword","Bow","Dagger","Battleaxe","*Custom"
    );

    private static final String[] VOWOPTIONS = {
            "***","Honesty","Law","Justice","Faith","Courage","Wisdom","Charity","Hope",
            "Mercy","Forgiveness","Humility","Gratitude","Patience","Duty","*Undecided"
    };

    private static final String[] COMBAT_ACTIONS = {
            "***","Bull Rush","Shove","Overrun","Grapple","Trip",
            "Disarm","Sunder","Charge","Feint"
    };

    private static final String[] DISCIPLINES = {"***", "Mobility", "Avoidance", "Martial"};

    private static final String[] FAVOR_TYPES = {"***", "Enemy", "Terrain"};

    private static final String[] ARCHER_ENEMY = {"***","Animals (Land)", "Animalas (Sea)", "Animals (Air)", "Constructs (Mechanical)", "Constructs (Organic)", "Dragons", "Elementals", "Fey", "Outsiders", "Plants", "Undead", "Wardens", "Ardians..."};

    private static final String[] ARCHER_TERRAIN = {"***","Plains","Forest","Mountains","Hills","Swamp","Underground","Urban","Coastal","Arctic"};

    private static final String[] AURA_TYPES = {
            "***","Reinforcement","Body","Force","Metal","Fire","Water","Air","Earth",
            "Electricity","Energy","Sound","Light","Nature",
            "Poison","Darkness","Psionic","Spirit","Time"
    };

    private static final String[] PRIM_ATTS = {"STR","DEX","FOC","CTL"};
    private static final String[] SHIFTER_MELEE_ATTS = {"STR","DEX"};
    private static final String[] SHIFTER_RANGED_ATTS = {"FOC","CTL"};

    // Weapons
    private static class WeaponPools {

        static final String[] MELEE = {
                "Greatsword","Battleaxe","Warhammer","Polearm",
                "Sword","Axe","Mace","Shield",
                "Blade","Dagger","Knuckle","Whip"
        };

        static final String[] RANGED = {
                "Bow","Crossbow","Rifle","Cannon",
                "Thrown","Sling","Handbow","Pistol"
        };

        static final String[] AURA = {
                "Staff","Tome","Relic","Symbol",
                "Ring","Orb","Wand","Talisman"
        };

        static final String[] LIGHT_RANGED = {"Thrown","Sling","Handbow","Pistol"};
        static final String[] LIGHT_AURA    = {"Ring","Orb","Wand","Talisman"};
        static final String[] SHIFTER_MOLD_2 = {
                "Bow","Crossbow","Rifle","Cannon",
                "Thrown","Sling","Handbow","Pistol",
                "Staff","Tome","Relic","Symbol",
                "Ring","Orb","Wand","Talisman"
        };
    }
}

