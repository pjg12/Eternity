package eternity;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

/**
 * Affinity and starter weapon selection.
 */
public class FrameNewAura extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int FRAME_WIDTH = 540;
    private static final int FRAME_HEIGHT = 320;
    private static final int PILOT_FRAME_HEIGHT = 400;

    private final StoreRuleManager dataQuery;
    private final StoreCharData character;
    private final FrameNew parent;
    private final boolean gmMode;
    private final boolean casterSelected;
    private final boolean shifterSelected;
    private final boolean pilotSelected;

    private static final String[] AURATYPE = {
            "***", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity",
            "Force", "Sound", "Light", "Darkness", "Poison", "Psionic", "Energy", "Spirit", "Time"
    };
    private static final String EMPTY_OPTION = "***";

    private JComboBox<String> auraPick;
    private JComboBox<String> bonusAuraPick;
    private final JComboBox<String>[] weaponPick;
    private final Map<String, List<String>> starterWeaponsByProfile = new HashMap<>();
    private final Map<String, String> starterMatrixTypesByName = new HashMap<>();
    private boolean updatingAffinityChoices;

    public FrameNewAura(FrameSheet sheetFrame, StoreRuleManager dataQuery, StoreCharData character, FrameNew parent, boolean gmMode) {
        super("Affinity & Starter Weapons");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;
        this.casterSelected = isCasterSelected();
        this.shifterSelected = isShifterSelected();
        this.pilotSelected = isPilotSelected();
        this.weaponPick = new JComboBox[getStarterWeaponPickCount()];

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(FRAME_WIDTH, pilotSelected ? PILOT_FRAME_HEIGHT : FRAME_HEIGHT);
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
        if (!pilotSelected) {
            JLabel affinityLabel = new JLabel("Natural Affinity");
            affinityLabel.setBounds(25, 60, 140, 20);
            add(affinityLabel);
        }

        if (casterSelected) {
            JLabel bonusAffinityLabel = new JLabel("Bonus Affinity");
            bonusAffinityLabel.setBounds(25, 125, 140, 20);
            add(bonusAffinityLabel);
        }

        JLabel weaponLabel = new JLabel(shifterSelected ? "Starter Matrices" : "Starter Weapons");
        weaponLabel.setBounds(225, 60, 200, 20);
        add(weaponLabel);
    }

    private void buildPickers() {
        if (!pilotSelected) {
            auraPick = new JComboBox<>(AURATYPE);
            auraPick.setBounds(25, 90, 160, 22);
            add(auraPick);
        }

        if (casterSelected) {
            bonusAuraPick = new JComboBox<>(AURATYPE);
            bonusAuraPick.setBounds(25, 155, 160, 22);
            add(bonusAuraPick);

            auraPick.addActionListener(e -> refreshAffinityPickers());
            bonusAuraPick.addActionListener(e -> refreshAffinityPickers());
            refreshAffinityPickers();
        }

        List<String> starterWeapons;
        boolean hasProfs;
        if (shifterSelected) {
            starterWeapons = getStarterMatrixOptions();
            hasProfs = !starterWeapons.isEmpty();
        } else {
            List<String> profs = character.getInventory().getWeaponProficiencies();
            if ((profs == null || profs.isEmpty()) && dataQuery != null && character != null && character.getIdentity() != null) {
                String cls = character.getIdentity().getCharClass();
                if (cls != null && !cls.isBlank()) {
                    DataClass dc = dataQuery.getClassByName(cls);
                    if (dc != null && dc.getProfAuto() != null && !dc.getProfAuto().isEmpty()) {
                        profs = new ArrayList<>(dc.getProfAuto());
                        character.getInventory().setWeaponProficiencies(profs); // hydrate inventory so downstream stays consistent
                    }
                }
            }
            starterWeapons = getStarterWeaponOptions(profs);
            hasProfs = !starterWeapons.isEmpty();
        }

        for (int i = 0; i < weaponPick.length; i++) {
            JComboBox<String> box = new JComboBox<>();
            box.addItem(EMPTY_OPTION);
            for (String weapon : starterWeapons) box.addItem(weapon);
            box.setBounds(225, 100 + 40 * i, 250, 22);
            box.setEnabled(hasProfs);
            weaponPick[i] = box;
            add(box);
        }
    }

    private void buildButtons() {
        JButton back = new JButton("Back");
        int buttonY = pilotSelected ? 320 : 240;
        back.setBounds(150, buttonY, 100, 28);
        back.addActionListener(e -> dispose());
        add(back);

        JButton confirm = new JButton("Confirm");
        confirm.setBounds(290, buttonY, 120, 28);
        confirm.addActionListener(e -> auraConfirm());
        add(confirm);
    }

    private void auraConfirm() {
        if (gmMode) {
            randomizeAffinitySelections();
            randomizeWeaponSelections();
        }

        String affinity = EMPTY_OPTION;
        if (!pilotSelected) {
            affinity = (String) auraPick.getSelectedItem();
            if (affinity == null || EMPTY_OPTION.equals(affinity)) {
                JOptionPane.showMessageDialog(this, "Select a Natural Affinity to proceed.");
                return;
            }
        }

        String bonusAffinity = EMPTY_OPTION;
        if (casterSelected && bonusAuraPick != null) {
            bonusAffinity = (String) bonusAuraPick.getSelectedItem();
            if (bonusAffinity == null || EMPTY_OPTION.equals(bonusAffinity)) {
                JOptionPane.showMessageDialog(this, "Select a Bonus Affinity to proceed.");
                return;
            }
            if (bonusAffinity.equalsIgnoreCase(affinity)) {
                JOptionPane.showMessageDialog(this, "Natural Affinity and Bonus Affinity must be different.");
                return;
            }
        }

        // Validate weapon picks only if profs exist
        boolean requireWeapons = false;
        for (JComboBox<String> wp : weaponPick) {
            if (wp != null && wp.isEnabled()) {
                requireWeapons = true;
                break;
            }
        }
        if (requireWeapons) {
            for (JComboBox<String> wp : weaponPick) {
                if (wp == null) continue;
                String val = (String) wp.getSelectedItem();
                if (val == null || EMPTY_OPTION.equals(val)) {
                    JOptionPane.showMessageDialog(this, shifterSelected
                            ? "Select 2 starter matrices to proceed."
                            : "Select " + weaponPick.length + " starter weapons to proceed.");
                    return;
                }
            }
            if (shifterSelected) {
                String firstMatrix = (String) weaponPick[0].getSelectedItem();
                String secondMatrix = (String) weaponPick[1].getSelectedItem();
                String firstType = getStarterMatrixType(firstMatrix);
                String secondType = getStarterMatrixType(secondMatrix);
                if (firstType.isBlank() || secondType.isBlank() || firstType.equalsIgnoreCase(secondType)) {
                    JOptionPane.showMessageDialog(this, "Select 2 starter matrices of differing types.");
                    return;
                }
            }
        }

        ArrayList<String> selectedAffinities = new ArrayList<>();
        if (!pilotSelected && affinity != null && !EMPTY_OPTION.equals(affinity)) {
            selectedAffinities.add(affinity);
        }
        if (casterSelected && bonusAuraPick != null && bonusAffinity != null && !EMPTY_OPTION.equals(bonusAffinity)) {
            selectedAffinities.add(bonusAffinity);
        }
        character.getTraining().setNaturalAffinities(selectedAffinities);

        if (requireWeapons) {
            ArrayList<String> selectedWeapons = new ArrayList<>();
            for (JComboBox<String> wp : weaponPick) {
                if (wp == null) continue;
                String weaponName = (String) wp.getSelectedItem();
                if (weaponName == null || EMPTY_OPTION.equals(weaponName)) continue;
                selectedWeapons.add(weaponName);
                DataItemWeapon item = dataQuery.getWeaponByName(weaponName);
                if (item != null) {
                    DataItemWeapon granted = new DataItemWeapon(item);
                    granted.setEquipped(true);
                    character.getInventory().addWeapon(granted);
                }
            }
            parent.setStarterWeaponSelections(selectedWeapons);
        } else {
            parent.setStarterWeaponSelections(List.of());
        }

        parent.setStepConfirmed(5);
        dispose();
    }

    private boolean isCasterSelected() {
        if (character == null || character.getIdentity() == null) return false;
        String cls = character.getIdentity().getCharClass();
        return cls != null && cls.equalsIgnoreCase("Caster");
    }

    private boolean isShifterSelected() {
        if (character == null || character.getIdentity() == null) return false;
        String cls = character.getIdentity().getCharClass();
        return cls != null && cls.equalsIgnoreCase("Shifter");
    }

    private boolean isPilotSelected() {
        if (character == null || character.getIdentity() == null) return false;
        String cls = character.getIdentity().getCharClass();
        return cls != null && cls.equalsIgnoreCase("Pilot");
    }

    private int getStarterWeaponPickCount() {
        return pilotSelected ? 4 : 2;
    }

    private void randomizeAffinitySelections() {
        if (pilotSelected || auraPick == null) return;
        int naturalIndex = ThreadLocalRandom.current().nextInt(1, AURATYPE.length);
        auraPick.setSelectedItem(AURATYPE[naturalIndex]);
        if (casterSelected && bonusAuraPick != null) {
            refreshAffinityPickers();
            ArrayList<String> options = new ArrayList<>();
            for (int i = 1; i < bonusAuraPick.getItemCount(); i++) {
                String item = bonusAuraPick.getItemAt(i);
                if (item != null && !EMPTY_OPTION.equals(item)) {
                    options.add(item);
                }
            }
            if (!options.isEmpty()) {
                String bonus = options.get(ThreadLocalRandom.current().nextInt(options.size()));
                bonusAuraPick.setSelectedItem(bonus);
            }
        }
    }

    private void randomizeWeaponSelections() {
        if (shifterSelected) {
            randomizeMatrixSelections();
            return;
        }
        ArrayList<String> options = new ArrayList<>();
        JComboBox<String> sourceBox = weaponPick[0];
        if (sourceBox != null && sourceBox.isEnabled()) {
            for (int i = 1; i < sourceBox.getItemCount(); i++) {
                String item = sourceBox.getItemAt(i);
                if (item != null && !EMPTY_OPTION.equals(item) && !options.contains(item)) {
                    options.add(item);
                }
            }
        }
        if (options.isEmpty()) return;

        ArrayList<String> remaining = new ArrayList<>(options);
        for (int i = 0; i < weaponPick.length; i++) {
            JComboBox<String> box = weaponPick[i];
            if (box == null || !box.isEnabled()) continue;
            if (remaining.isEmpty()) {
                remaining = new ArrayList<>(options);
            }
            String selection = remaining.get(ThreadLocalRandom.current().nextInt(remaining.size()));
            box.setSelectedItem(selection);
            if (options.size() > 1) {
                remaining.removeIf(option -> option.equalsIgnoreCase(selection));
            }
        }
    }

    private void randomizeMatrixSelections() {
        ArrayList<String> options = new ArrayList<>();
        if (weaponPick[0] != null && weaponPick[0].isEnabled()) {
            for (int i = 1; i < weaponPick[0].getItemCount(); i++) {
                String item = weaponPick[0].getItemAt(i);
                if (item != null && !EMPTY_OPTION.equals(item) && !options.contains(item)) {
                    options.add(item);
                }
            }
        }
        if (options.isEmpty()) return;

        String firstPick = options.get(ThreadLocalRandom.current().nextInt(options.size()));
        String firstType = getStarterMatrixType(firstPick);
        weaponPick[0].setSelectedItem(firstPick);

        ArrayList<String> remaining = new ArrayList<>();
        for (String option : options) {
            String optionType = getStarterMatrixType(option);
            if (!option.equalsIgnoreCase(firstPick) && !optionType.equalsIgnoreCase(firstType)) {
                remaining.add(option);
            }
        }
        if (!remaining.isEmpty() && weaponPick[1] != null && weaponPick[1].isEnabled()) {
            String secondPick = remaining.get(ThreadLocalRandom.current().nextInt(remaining.size()));
            weaponPick[1].setSelectedItem(secondPick);
        }
    }

    private void refreshAffinityPickers() {
        if (!casterSelected || bonusAuraPick == null || updatingAffinityChoices) return;

        updatingAffinityChoices = true;
        try {
            String naturalSelection = getSelectedOrEmpty(auraPick);
            String bonusSelection = getSelectedOrEmpty(bonusAuraPick);

            rebuildAffinityPicker(auraPick, naturalSelection, bonusSelection);
            rebuildAffinityPicker(bonusAuraPick, bonusSelection, naturalSelection);
        } finally {
            updatingAffinityChoices = false;
        }
    }

    private void rebuildAffinityPicker(JComboBox<String> box, String currentSelection, String blockedSelection) {
        box.removeAllItems();
        box.addItem(EMPTY_OPTION);

        for (String affinity : AURATYPE) {
            if (EMPTY_OPTION.equals(affinity)) continue;
            if (!EMPTY_OPTION.equals(blockedSelection) && affinity.equalsIgnoreCase(blockedSelection)) continue;
            box.addItem(affinity);
        }

        if (currentSelection != null && !currentSelection.isBlank() && containsOption(box, currentSelection)) {
            box.setSelectedItem(currentSelection);
        } else {
            box.setSelectedItem(EMPTY_OPTION);
        }
    }

    private String getSelectedOrEmpty(JComboBox<String> box) {
        if (box == null) return EMPTY_OPTION;
        String selected = (String) box.getSelectedItem();
        return selected == null ? EMPTY_OPTION : selected;
    }

    private boolean containsOption(JComboBox<String> box, String value) {
        for (int i = 0; i < box.getItemCount(); i++) {
            String item = box.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getStarterWeaponOptions(List<String> profs) {
        if (profs == null || profs.isEmpty() || dataQuery == null) return new ArrayList<>();
        String cacheKey = buildProfileKey(profs);
        List<String> cached = starterWeaponsByProfile.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Set<String> names = new LinkedHashSet<>();

        List<DataItemWeapon> all = dataQuery.getItemWeaponData();
        for (DataItemWeapon item : all) {
            if (item == null || item.getTier() != 0) continue;
            String category = item.getCategory() == null ? "" : item.getCategory();
            if (!category.equalsIgnoreCase("Melee") &&
                    !category.equalsIgnoreCase("Ranged") &&
                    !category.equalsIgnoreCase("Aura") &&
                    !category.toLowerCase().startsWith("exo")) {
                continue;
            }

            boolean match = false;
            for (String prof : profs) {
                if (matchesProficiency(item, prof)) {
                    match = true;
                    break;
                }
            }
            if (match) names.add(item.getDname());
        }

        ArrayList<String> out = new ArrayList<>(names);
        out.sort(Comparator.naturalOrder());
        starterWeaponsByProfile.put(cacheKey, out);
        return out;
    }

    private List<String> getStarterMatrixOptions() {
        starterMatrixTypesByName.clear();
        if (dataQuery == null) return new ArrayList<>();

        Set<String> names = new LinkedHashSet<>();
        for (DataItemWeapon item : dataQuery.getItemWeaponData()) {
            if (item == null || item.getTier() != 0) continue;
            if (!"Matrix".equalsIgnoreCase(item.getCategory())) continue;
            String name = item.getDname();
            if (name == null || name.isBlank()) continue;
            names.add(name);
            starterMatrixTypesByName.put(name, item.getType() == null ? "" : item.getType().trim());
        }

        ArrayList<String> out = new ArrayList<>(names);
        out.sort(Comparator.naturalOrder());
        return out;
    }

    private String getStarterMatrixType(String matrixName) {
        if (matrixName == null || matrixName.isBlank()) return "";
        String type = starterMatrixTypesByName.get(matrixName);
        return type == null ? "" : type.trim();
    }

    private boolean matchesProficiency(DataItemWeapon item, String prof) {
        if (item == null || prof == null) return false;
        String p = prof.trim();
        if (p.isBlank()) return false;

        String category = item.getCategory() == null ? "" : item.getCategory();
        String slot = item.getSlot() == null ? "" : item.getSlot();
        String type = item.getType() == null ? "" : item.getType();
        String name = item.getDname() == null ? "" : item.getDname();
        String normalizedProf = normalizeWeaponFamily(p);
        String normalizedType = normalizeWeaponFamily(type);
        String normalizedName = normalizeWeaponFamily(name);

        if ("Any".equalsIgnoreCase(p)) return true;
        if ("Melee".equalsIgnoreCase(p) && "Melee".equalsIgnoreCase(category)) return true;
        if ("Ranged".equalsIgnoreCase(p) && "Ranged".equalsIgnoreCase(category)) return true;
        if ("Aura".equalsIgnoreCase(p) && "Aura".equalsIgnoreCase(category)) return true;
        if ("Exo".equalsIgnoreCase(p) && category.toLowerCase().startsWith("exo")) return true;
        if ("Light".equalsIgnoreCase(p) && slot.toLowerCase().contains("light")) return true;
        if ("Heavy".equalsIgnoreCase(p) && slot.toLowerCase().contains("heavy")) return true;
        if (p.equalsIgnoreCase(type)) return true;
        if (p.equalsIgnoreCase(name)) return true;
        if (!normalizedProf.isBlank() && normalizedProf.equalsIgnoreCase(normalizedType)) return true;
        if (!normalizedProf.isBlank() && normalizedProf.equalsIgnoreCase(normalizedName)) return true;
        return normalizedType.contains(normalizedProf) || normalizedName.contains(normalizedProf);
    }

    private String normalizeWeaponFamily(String value) {
        if (value == null) return "";
        String normalized = value.trim().toLowerCase();
        if (normalized.isBlank()) return "";
        if (normalized.contains("axe")) return "axe";
        if (normalized.contains("sword")) return "sword";
        if (normalized.contains("bow")) return "bow";
        if (normalized.contains("crossbow")) return "crossbow";
        if (normalized.contains("fist") || normalized.contains("knuckle")) return "fist";
        if (normalized.contains("polearm")) return "polearm";
        if (normalized.contains("whip")) return "whip";
        if (normalized.contains("thrown")) return "thrown";
        if (normalized.contains("dagger")) return "dagger";
        if (normalized.contains("mace")) return "mace";
        if (normalized.contains("shield")) return "shield";
        if (normalized.contains("staff")) return "staff";
        if (normalized.contains("tome")) return "tome";
        if (normalized.contains("relic")) return "relic";
        if (normalized.contains("symbol")) return "symbol";
        if (normalized.contains("ring")) return "ring";
        if (normalized.contains("orb")) return "orb";
        if (normalized.contains("wand")) return "wand";
        if (normalized.contains("talisman")) return "talisman";
        if (normalized.contains("rifle")) return "rifle";
        if (normalized.contains("cannon")) return "cannon";
        if (normalized.contains("sling")) return "sling";
        if (normalized.contains("handbow")) return "handbow";
        if (normalized.contains("pistol")) return "pistol";
        if (normalized.contains("blade")) return "blade";
        return normalized;
    }

    private String buildProfileKey(List<String> profs) {
        ArrayList<String> normalized = new ArrayList<>(profs.size());
        for (String prof : profs) {
            if (prof != null && !prof.isBlank()) {
                normalized.add(prof.trim().toLowerCase());
            }
        }
        String[] parts = normalized.toArray(new String[0]);
        Arrays.sort(parts);
        return String.join("|", parts);
    }
}
