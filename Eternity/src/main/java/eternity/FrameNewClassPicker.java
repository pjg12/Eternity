package eternity;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Fully data-driven class picker with simple String keys instead of ClassChoice objects.
 */
public class FrameNewClassPicker extends JFrame {
    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNewClass parent;
    private final DataClass selectedClass;

    // Field UI elements (label → combobox)
    private final Map<String, JComboBox<String>> fields = new LinkedHashMap<>();

    // Model: label → choice configuration
    private Map<String, ChoiceConfig> choiceModel;

    // Buttons
    private JButton clearButton;
    private JButton acceptButton;
    private JLabel headerLabel;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public FrameNewClassPicker(DataQuery dataQuery,
                               CharData character, DataClass selectedClass,
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
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Cleric" -> {
                map.put("Deity", cfg(ChoiceType.DEITY));
                map.put("Domain", cfg(ChoiceType.DOMAIN_DEPENDENT));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Warrior" -> {
                map.put("Feature", cfgSpecial("Martial"));
                map.put("Combat Action", cfgStatic(COMBAT_ACTIONS));
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.MELEE));
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
                map.put("Favored Condition", cfgStatic(ARCHER_FAVORS));
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.MELEE));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Caster" -> {
                map.put("Bonus Affinity", cfgStatic(AURA_TYPES));
                map.put("Weapon Proficiency", cfgWeapon(WeaponPools.MELEE));
                map.put("Subclass", cfgSubclass(cls));
            }

            case "Shifter" -> {
                map.put("Primary Attribute", cfgStatic(PRIM_ATTS));
                map.put("Weapon Proficiency 1", cfgWeapon(WeaponPools.MELEE));
                map.put("Weapon Proficiency 2", cfgWeapon(WeaponPools.RANGED));
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
        c.staticOptions = vals;
        return c;
    }

    private ChoiceConfig cfgSpecial(String filter) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.SPECIAL_LIST);
        c.specialFilter = filter;
        return c;
    }

    private ChoiceConfig cfgWeapon(String[] pool) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.WEAPON_PICK_1);
        c.weaponPool = pool;
        return c;
    }

    private ChoiceConfig cfgSubclass(DataClass cls) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.SUBCLASS);
        c.subclassSource = cls;
        return c;
    }

    // -------------------------------------------------------------------------
    //  UI Rendering
    // -------------------------------------------------------------------------

    private void renderChoices() {
        int y = 70;

        for (String label : choiceModel.keySet()) {

            ChoiceConfig cfg = choiceModel.get(label);

            JLabel lbl = new JLabel(label);
            lbl.setBounds(25, y, 250, 20);
            add(lbl);

            JComboBox<String> box = new JComboBox<>();
            box.setBounds(25, y + 25, 260, 22);
            add(box);

            fields.put(label, box);

            initComboBox(label, cfg, box);

            y += 65;
        }
    }

    // -------------------------------------------------------------------------
    //  ComboBox Initialization
    // -------------------------------------------------------------------------

    private void initComboBox(String label, ChoiceConfig cfg, JComboBox<String> box) {

        box.addItem("***");

        switch (cfg.type) {

            case STATIC -> Arrays.stream(cfg.staticOptions).forEach(box::addItem);

            case DEITY -> DEITY_OPTIONS.forEach(box::addItem);

            case DOMAIN_DEPENDENT -> {
                JComboBox<String> deityBox = fields.get("Deity");
                if (deityBox != null)
                    deityBox.addActionListener(e -> updateDomainBox());
            }

            case SPECIAL_LIST -> {
                var list = dataQuery.getSpecialtiesByType(cfg.specialFilter);
                list.forEach(s -> box.addItem(s.getName()));
            }

            case SUBCLASS -> {
                List<String> subs = new ArrayList<>();
                int classID = cfg.subclassSource.getID();
                subs.add(dataQuery.getClassById(classID+1).getName());
                subs.add(dataQuery.getClassById(classID+2).getName());
                if (subs.size() >= 2) {
                    box.addItem(subs.get(0));
                    box.addItem(subs.get(1));
                }
                box.addItem("*Undecided");
            }

            case WEAPON_PICK_1, WEAPON_PICK_2 -> {
                Arrays.stream(cfg.weaponPool).forEach(box::addItem);
            }
        }
    }

    private void updateDomainBox() {

        JComboBox<String> deity = fields.get("Deity");
        JComboBox<String> domain = fields.get("Domain");

        if (domain == null) return;

        domain.removeAllItems();
        domain.addItem("***");

        DataDeity d = dataQuery.getDeityByName((String) deity.getSelectedItem());
        if (d != null)
            d.getDomains().forEach(domain::addItem);
    }

    // -------------------------------------------------------------------------
    //  Accept Logic
    // -------------------------------------------------------------------------

    private void acceptChoices() {

        List<String> classChoices = new ArrayList<>();
        List<String> profs = new ArrayList<>();

        for (String label : choiceModel.keySet()) {

            ChoiceConfig cfg = choiceModel.get(label);
            JComboBox<String> box = fields.get(label);

            String value = (String) box.getSelectedItem();

            if (value == null || value.equals("***")) {
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

    // -------------------------------------------------------------------------
    //  ChoiceType Enum + Config Struct
    // -------------------------------------------------------------------------
    private enum ChoiceType {
        STATIC,
        DEITY,
        DOMAIN_DEPENDENT,
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
        String[] staticOptions;
        String specialFilter;
        String[] weaponPool;
        DataClass subclassSource;

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

    private static final String[] ARCHER_FAVORS = {"***","Orcs","Humans","Forest"};

    private static final String[] AURA_TYPES = {
            "***","Reinforcement","Body","Force","Metal","Fire","Water","Air","Earth",
            "Electricity","Energy","Sound","Light","Nature",
            "Poison","Darkness","Psionic","Spirit","Time"
    };

    private static final String[] PRIM_ATTS = {"STR","DEX","FOC","CTL"};

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
    }
}