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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Affinity and starter weapon selection.
 */
public class FrameNewAura extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int FRAME_WIDTH = 540;
    private static final int FRAME_HEIGHT = 320;

    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNew parent;
    private final boolean gmMode;
    private final boolean casterSelected;

    private static final String[] AURATYPE = {
            "***", "Enhancement", "Body", "Nature", "Metal", "Earth", "Water", "Air", "Fire", "Electricity",
            "Force", "Sound", "Light", "Darkness", "Poison", "Psionic", "Energy", "Spirit", "Time"
    };
    private static final String EMPTY_OPTION = "***";

    private JComboBox<String> auraPick;
    private JComboBox<String> bonusAuraPick;
    private final JComboBox<String>[] weaponPick = new JComboBox[2];
    private final Map<String, List<String>> starterWeaponsByProfile = new HashMap<>();
    private boolean updatingAffinityChoices;

    public FrameNewAura(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, FrameNew parent, boolean gmMode) {
        super("Affinity & Starter Weapons");
        this.dataQuery = dataQuery;
        this.character = character;
        this.parent = parent;
        this.gmMode = gmMode;
        this.casterSelected = isCasterSelected();

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
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

        if (casterSelected) {
            JLabel bonusAffinityLabel = new JLabel("Bonus Affinity");
            bonusAffinityLabel.setBounds(25, 125, 140, 20);
            add(bonusAffinityLabel);
        }

        JLabel weaponLabel = new JLabel("Starter Weapons");
        weaponLabel.setBounds(225, 60, 200, 20);
        add(weaponLabel);
    }

    private void buildPickers() {
        auraPick = new JComboBox<>(AURATYPE);
        auraPick.setBounds(25, 90, 160, 22);
        add(auraPick);

        if (casterSelected) {
            bonusAuraPick = new JComboBox<>(AURATYPE);
            bonusAuraPick.setBounds(25, 155, 160, 22);
            add(bonusAuraPick);

            auraPick.addActionListener(e -> refreshAffinityPickers());
            bonusAuraPick.addActionListener(e -> refreshAffinityPickers());
            refreshAffinityPickers();
        }

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
        List<String> starterWeapons = getStarterWeaponOptions(profs);
        boolean hasProfs = !starterWeapons.isEmpty();

        for (int i = 0; i < 2; i++) {
            JComboBox<String> box = new JComboBox<>();
            box.addItem(EMPTY_OPTION);
            for (String weapon : starterWeapons) box.addItem(weapon);
            box.setBounds(225, 100 + 60 * i, 250, 22);
            box.setEnabled(hasProfs);
            weaponPick[i] = box;
            add(box);
        }
    }

    private void buildButtons() {
        JButton back = new JButton("Back");
        back.setBounds(150, 240, 100, 28);
        back.addActionListener(e -> dispose());
        add(back);

        JButton confirm = new JButton("Confirm");
        confirm.setBounds(290, 240, 120, 28);
        confirm.addActionListener(e -> auraConfirm());
        add(confirm);
    }

    private void auraConfirm() {
        if (gmMode) {
            randomizeAffinitySelections();
            // auto-pick first non "***" weapon options if available
            for (JComboBox<String> wp : weaponPick) {
                if (wp.isEnabled() && wp.getItemCount() > 1) {
                    wp.setSelectedIndex(1);
                }
            }
        }

        String affinity = (String) auraPick.getSelectedItem();
        if (affinity == null || EMPTY_OPTION.equals(affinity)) {
            JOptionPane.showMessageDialog(this, "Select a Natural Affinity to proceed.");
            return;
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
            if (wp.isEnabled()) {
                requireWeapons = true;
                break;
            }
        }
        if (requireWeapons) {
            for (JComboBox<String> wp : weaponPick) {
                String val = (String) wp.getSelectedItem();
                if (val == null || EMPTY_OPTION.equals(val)) {
                    JOptionPane.showMessageDialog(this, "Select 2 starter weapons to proceed.");
                    return;
                }
            }
        }

        ArrayList<String> selectedAffinities = new ArrayList<>();
        selectedAffinities.add(affinity);
        if (casterSelected && bonusAuraPick != null && bonusAffinity != null && !EMPTY_OPTION.equals(bonusAffinity)) {
            selectedAffinities.add(bonusAffinity);
        }
        character.getTraining().setNaturalAffinities(selectedAffinities);

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

    private boolean isCasterSelected() {
        if (character == null || character.getIdentity() == null) return false;
        String cls = character.getIdentity().getCharClass();
        return cls != null && cls.equalsIgnoreCase("Caster");
    }

    private void randomizeAffinitySelections() {
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

        List<DataItemEquipment> all = dataQuery.getItemEquipmentData();
        for (DataItemEquipment item : all) {
            if (item == null || item.getTier() != 0) continue;
            String category = item.getCategory() == null ? "" : item.getCategory();
            if (!category.equalsIgnoreCase("Melee") &&
                    !category.equalsIgnoreCase("Ranged") &&
                    !category.equalsIgnoreCase("Aura")) {
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

    private boolean matchesProficiency(DataItemEquipment item, String prof) {
        if (item == null || prof == null) return false;
        String p = prof.trim();
        if (p.isBlank()) return false;

        String category = item.getCategory() == null ? "" : item.getCategory();
        String slot = item.getSlot() == null ? "" : item.getSlot();
        String type = item.getType() == null ? "" : item.getType();
        String name = item.getDname() == null ? "" : item.getDname();

        if ("Any".equalsIgnoreCase(p)) return true;
        if ("Melee".equalsIgnoreCase(p) && "Melee".equalsIgnoreCase(category)) return true;
        if ("Ranged".equalsIgnoreCase(p) && "Ranged".equalsIgnoreCase(category)) return true;
        if ("Aura".equalsIgnoreCase(p) && "Aura".equalsIgnoreCase(category)) return true;
        if ("Light".equalsIgnoreCase(p) && slot.toLowerCase().contains("light")) return true;
        if ("Heavy".equalsIgnoreCase(p) && slot.toLowerCase().contains("heavy")) return true;
        if (p.equalsIgnoreCase(type)) return true;
        return p.equalsIgnoreCase(name);
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
