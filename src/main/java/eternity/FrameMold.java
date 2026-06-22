package eternity;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

/**
 * Helper frame for selecting molds to activate from a character's Molds list.
 */
public class FrameMold extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String MOLDS_LIST = "Molds";
    private static final String EMPTY_OPTION = "";
    private static final String CATEGORY_ALL = "All";
    private static final String CATEGORY_WEAPON = "Weapon";
    private static final String CATEGORY_ARMOR = "Armor";
    private static final String CATEGORY_ITEM = "Item";
    private static final String MOLDING_MANIFEST_NOTE_PREFIX = "[MOLDING_MANIFEST]";
    private static final String MOLDING_WEAPON_BONUS_KEY = "TDMG";
    private static final String MOLDING_ARMOR_BONUS_KEY = "ARMOR";
    private static final int SHIFTER_SPECIAL_MOLD_DID = -9001;
    private static final int ACTIVE_WEAPON_SLOT_COUNT = 4;
    private static final int FRAME_WIDTH = 420;
    private static final int FRAME_HEIGHT = 360;
    private static final int ROW_WIDTH = 360;
    private static final int HEADER_ROW_HEIGHT = 28;
    private static final int BODY_ROW_HEIGHT = 34;

    private final FrameSheet sheetFrame;
    private StoreCharData character;
    private boolean confirmed;
    private boolean suppressRowEvents;
    private ArrayList<MoldOption> availableMolds = new ArrayList<>();
    private final ArrayList<MoldRow> moldRows = new ArrayList<>();
    private int totalActiveMoldingLevels;
    private int highestActiveMoldingLevel;

    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel rowsPanel;
    private JButton confirmButton;

    FrameMold(FrameSheet sheetFrame, StoreCharData character) {
        super("Mold Selection");
        this.sheetFrame = sheetFrame;
        this.character = character;
        this.confirmed = false;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        buildUi();
        refreshMoldChoices();
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
        this.confirmed = false;
        refreshMoldChoices();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<String> getSelectedMolds() {
        ArrayList<String> selected = new ArrayList<>();
        for (MoldRow row : moldRows) {
            if (row == null || row.moldBox() == null) continue;
            Object selectedItem = row.moldBox().getSelectedItem();
            if (selectedItem == null) continue;
            String moldName = selectedItem.toString().trim();
            if (!moldName.isBlank()) {
                selected.add(moldName);
            }
        }
        return selected;
    }

    public void setSelectedMolds(List<String> moldNames) {
        ArrayList<MoldRowState> states = new ArrayList<>();
        if (moldNames != null) {
            for (String moldName : moldNames) {
                String trimmed = moldName == null ? "" : moldName.trim();
                if (trimmed.isBlank()) continue;
                MoldOption option = findMoldOption(trimmed);
                String category = option == null || option.category() == null || option.category().isBlank() ? CATEGORY_ALL : option.category();
                states.add(new MoldRowState(category, trimmed, 0));
            }
        }
        rebuildRows(states);
        updateConfirmButton();
    }

    private void buildUi() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(12, 12, 0, 12));

        headerLabel = new JLabel("Select Molds To Activate", SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setBorder(new EmptyBorder(0, 12, 0, 12));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(BODY_ROW_HEIGHT);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> confirmSelection());
        footer.add(cancelButton);
        footer.add(confirmButton);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshMoldChoices() {
        ArrayList<MoldRowState> previousStates = captureRowStates();
        availableMolds = collectMoldEntries();
        totalActiveMoldingLevels = getTotalActiveMoldingLevels();
        highestActiveMoldingLevel = getHighestActiveMoldingLevel();
        rebuildRows(previousStates);

        refreshAvailableLevelsStatus();
        updateConfirmButton();
    }

    private int getTotalActiveMoldingLevels() {
        if (character == null || character.getTraining() == null) return 0;
        int total = 0;
        for (DataTraining tech : character.getTraining().getAllTraining()) {
            if (tech == null || !tech.isMoldingTechnique()) continue;
            total += Math.max(0, tech.getAl());
        }
        return total;
    }

    private ArrayList<MoldOption> collectMoldEntries() {
        ArrayList<MoldOption> molds = new ArrayList<>();
        if (character == null || character.getLists() == null) return molds;

        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        for (List<DataList> group : character.getLists()) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!MOLDS_LIST.equalsIgnoreCase(entry.getList().trim())) continue;
                String name = entry.getName().trim();
                if (!name.isBlank()) {
                    String key = name.toLowerCase() + "|" + parseMoldCategory(entry.getDescription()).toLowerCase();
                    if (deduped.add(key)) {
                        molds.add(new MoldOption(name, parseMoldCategory(entry.getDescription())));
                    }
                }
            }
        }
        return molds;
    }

    private void updateConfirmButton() {
        if (confirmButton == null) return;
        confirmButton.setEnabled(!availableMolds.isEmpty() && !getSelectedMolds().isEmpty());
    }

    private void confirmSelection() {
        if (character == null || character.getInventory() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No character inventory is available.",
                    "Mold Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (availableMolds.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No entries are available in the Molds list.",
                    "Mold Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (getSelectedMolds().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select at least one mold.",
                    "Mold Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        applySelectedMoldsToInventory();
        if (sheetFrame != null) {
            sheetFrame.loadCharacter(character);
        } else {
            character.updateAll();
        }
        confirmed = true;
        dispose();
    }

    private int getHighestActiveMoldingLevel() {
        if (character == null || character.getTraining() == null) return 0;
        int highest = 0;
        for (DataTraining tech : character.getTraining().getAllTraining()) {
            if (tech == null || !tech.isMoldingTechnique()) continue;
            highest = Math.max(highest, Math.max(0, tech.getAl()));
        }
        return highest;
    }

    private void rebuildRows(List<MoldRowState> rawStates) {
        rowsPanel.removeAll();
        moldRows.clear();
        rowsPanel.add(buildHeaderRow());

        ArrayList<MoldRowState> states = normalizeRowStates(rawStates);
        for (int i = 0; i < states.size(); i++) {
            MoldRow row = createMoldRow(states.get(i));
            moldRows.add(row);
            rowsPanel.add(row.panel());
        }
        refreshAllAlChoices();
        refreshAvailableLevelsStatus();
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private JPanel buildHeaderRow() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configureRowPanel(header, HEADER_ROW_HEIGHT);
        header.add(buildColumnLabel("Type", 90));
        header.add(buildColumnLabel("Mold", 180));
        header.add(buildColumnLabel("AL", 80));
        return header;
    }

    private JLabel buildColumnLabel(String text, int width) {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(width, 20));
        return label;
    }

    private MoldRow createMoldRow(MoldRowState state) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configureRowPanel(panel, BODY_ROW_HEIGHT);
        JComboBox<String> typeBox = new JComboBox<>(new String[] { EMPTY_OPTION, CATEGORY_ALL, CATEGORY_WEAPON, CATEGORY_ARMOR, CATEGORY_ITEM });
        typeBox.setPreferredSize(new Dimension(90, 24));
        JComboBox<String> moldBox = new JComboBox<>();
        moldBox.setPreferredSize(new Dimension(180, 24));
        JComboBox<Integer> alBox = new JComboBox<>();
        alBox.setPreferredSize(new Dimension(80, 24));

        MoldRow row = new MoldRow(panel, typeBox, moldBox, alBox);
        panel.add(typeBox);
        panel.add(moldBox);
        panel.add(alBox);

        typeBox.addActionListener(e -> {
            if (suppressRowEvents) return;
            refreshMoldOptions(row);
            rebalanceRows();
            refreshAvailableLevelsStatus();
            updateConfirmButton();
        });
        moldBox.addActionListener(e -> {
            if (suppressRowEvents) return;
            refreshAvailableLevelsStatus();
            updateConfirmButton();
        });
        alBox.addActionListener(e -> {
            if (suppressRowEvents) return;
            refreshAllAlChoices();
            refreshAvailableLevelsStatus();
            updateConfirmButton();
        });

        suppressRowEvents = true;
        try {
            String type = state == null ? EMPTY_OPTION : safe(state.type());
            typeBox.setSelectedItem(type.isBlank() ? EMPTY_OPTION : type);
            refreshMoldOptions(row);
            String mold = state == null ? EMPTY_OPTION : safe(state.mold());
            moldBox.setSelectedItem(mold.isBlank() ? EMPTY_OPTION : mold);
        } finally {
            suppressRowEvents = false;
        }
        refreshMoldOptions(row);
        int preferredAl = state == null ? 0 : Math.max(0, state.al());
        populateAlChoices(alBox, Math.max(totalActiveMoldingLevels, highestActiveMoldingLevel), preferredAl);
        return row;
    }

    private void configureRowPanel(JPanel panel, int rowHeight) {
        if (panel == null) return;
        Dimension size = new Dimension(ROW_WIDTH, rowHeight);
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panel.setPreferredSize(size);
        panel.setMinimumSize(size);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
    }

    private void refreshMoldOptions(MoldRow row) {
        if (row == null || row.moldBox() == null || row.typeBox() == null) return;
        String currentSelection = row.moldBox().getSelectedItem() == null ? "" : row.moldBox().getSelectedItem().toString();
        String categoryFilter = row.typeBox().getSelectedItem() == null ? EMPTY_OPTION : row.typeBox().getSelectedItem().toString();
        row.moldBox().removeAllItems();
        row.moldBox().addItem(EMPTY_OPTION);
        if (categoryFilter.isBlank()) {
            row.moldBox().setSelectedItem(EMPTY_OPTION);
            return;
        }
        for (MoldOption option : availableMolds) {
            if (option == null) continue;
            if (!matchesCategoryFilter(option, categoryFilter)) continue;
            row.moldBox().addItem(option.name());
        }
        if (!currentSelection.isBlank()) {
            row.moldBox().setSelectedItem(currentSelection);
        }
        if (row.moldBox().getSelectedIndex() < 0 && row.moldBox().getItemCount() > 0) {
            row.moldBox().setSelectedIndex(0);
        }
    }

    private boolean matchesCategoryFilter(MoldOption option, String categoryFilter) {
        if (option == null) return false;
        if (categoryFilter == null || categoryFilter.isBlank() || CATEGORY_ALL.equalsIgnoreCase(categoryFilter)) return true;
        return categoryFilter.equalsIgnoreCase(option.category());
    }

    private void refreshAllAlChoices() {
        suppressRowEvents = true;
        try {
            int assignedLevels = getAssignedLevels();
            int availableLevels = Math.max(0, totalActiveMoldingLevels - assignedLevels);
            for (MoldRow row : moldRows) {
                int currentAl = row.alBox().getSelectedItem() instanceof Integer ? (Integer) row.alBox().getSelectedItem() : 0;
                int maxAl = Math.min(highestActiveMoldingLevel, availableLevels + currentAl);
                populateAlChoices(row.alBox(), maxAl, currentAl);
            }
        } finally {
            suppressRowEvents = false;
        }
    }

    private void populateAlChoices(JComboBox<Integer> alBox, int maxAl, int preferredValue) {
        if (alBox == null) return;
        alBox.removeAllItems();
        for (int i = 0; i <= Math.max(0, maxAl); i++) {
            alBox.addItem(i);
        }
        int selected = preferredValue >= 0 && preferredValue <= Math.max(0, maxAl) ? preferredValue : 0;
        alBox.setSelectedItem(selected);
    }

    private void refreshAvailableLevelsStatus() {
        if (statusLabel == null) return;
        if (availableMolds.isEmpty()) {
            statusLabel.setText("No entries are available in the Molds list.");
            return;
        }
        statusLabel.setText("Available Levels: " + Math.max(0, totalActiveMoldingLevels - getAssignedLevels()));
    }

    private int getAssignedLevels() {
        int assigned = 0;
        for (MoldRow row : moldRows) {
            if (row == null || row.alBox() == null) continue;
            if (row.alBox().getSelectedItem() instanceof Integer selected) {
                assigned += selected;
            }
        }
        return assigned;
    }

    private ArrayList<MoldRowState> captureRowStates() {
        ArrayList<MoldRowState> states = new ArrayList<>();
        for (MoldRow row : moldRows) {
            if (row == null) continue;
            String type = row.typeBox().getSelectedItem() == null ? EMPTY_OPTION : row.typeBox().getSelectedItem().toString();
            String mold = row.moldBox().getSelectedItem() == null ? EMPTY_OPTION : row.moldBox().getSelectedItem().toString();
            int al = row.alBox().getSelectedItem() instanceof Integer ? (Integer) row.alBox().getSelectedItem() : 0;
            states.add(new MoldRowState(type, mold, al));
        }
        return states;
    }

    private ArrayList<MoldRowState> normalizeRowStates(List<MoldRowState> rawStates) {
        ArrayList<MoldRowState> states = new ArrayList<>();
        if (rawStates != null) {
            for (MoldRowState state : rawStates) {
                if (state == null) continue;
                String type = safe(state.type());
                String mold = safe(state.mold());
                int al = Math.max(0, state.al());
                if (type.isBlank()) continue;
                states.add(new MoldRowState(type, mold, al));
            }
        }
        states.add(new MoldRowState(EMPTY_OPTION, EMPTY_OPTION, 0));
        return states;
    }

    private void rebalanceRows() {
        if (suppressRowEvents) return;
        ArrayList<MoldRowState> states = captureRowStates();
        rebuildRows(states);
    }

    private void applySelectedMoldsToInventory() {
        clearExistingMoldingManifestItems();
        for (MoldRowState state : captureRowStates()) {
            if (state == null) continue;
            String moldName = safe(state.mold());
            int al = Math.max(0, state.al());
            if (moldName.isBlank()) continue;
            manifestMold(moldName, al);
        }
    }

    private void manifestMold(String moldName, int al) {
        if (moldName == null || moldName.isBlank() || character == null || character.getInventory() == null) return;
        if (al < 0) return;
        int defaultTier = Math.max(0, al - 1);
        if (manifestShifterSpecialMold(moldName, defaultTier, al)) {
            return;
        }

        MoldEntryMetadata metadata = resolveMoldEntryMetadata(moldName);
        boolean equipmentEvocation = character.hasEquipmentEvocationSpecialty();
        int weaponTier = equipmentEvocation ? Math.max(0, al) : defaultTier;
        DataItemWeapon weaponTemplate = resolveWeaponTemplate(moldName, metadata, weaponTier);
        if (weaponTemplate != null) {
            if (al == 0 && !equipmentEvocation) {
                return;
            }
            DataItemWeapon weapon = new DataItemWeapon(weaponTemplate);
            weapon.setIname("Molded " + moldName);
            weapon.setInote(buildMoldingManifestNote(MOLDING_WEAPON_BONUS_KEY, al, al));
            weapon.setTier(weaponTier);
            weapon.setEquipped(hasAvailableWeaponSlot());
            weapon.setQuantity(1.0);
            character.getInventory().addWeapon(weapon);
            return;
        }

        if (al <= 0) {
            return;
        }

        DataItemEquipment armorTemplate = resolveArmorTemplate(moldName, metadata, defaultTier);
        if (armorTemplate != null) {
            DataItemEquipment armor = new DataItemEquipment(armorTemplate);
            armor.setIname("Molded " + moldName);
            armor.setInote(buildMoldingManifestNote(MOLDING_ARMOR_BONUS_KEY, al * 0.5, al));
            armor.setTier(defaultTier);
            armor.setEquipped(assignArmorSlotIfAvailable(armor));
            armor.setQuantity(1.0);
            character.getInventory().addEquipment(armor);
            return;
        }

        DataItem item = new DataItem();
        item.setDid(-1);
        item.setIid(-1);
        item.setDname("Molded " + moldName);
        item.setInote(buildMoldingManifestNote("", 0.0, al));
        item.setQuantity(1.0);
        character.getInventory().addItem(item);
    }

    private boolean manifestShifterSpecialMold(String moldName, int targetTier, int al) {
        if (!StoreCharData.isShifterSpecialMoldName(moldName)) return false;
        String slot = StoreCharData.resolveShifterSpecialMoldSlot(moldName);
        if (slot == null || slot.isBlank()) return false;
        DataItemEquipment armor = new DataItemEquipment();
        String displayName = "Molded " + moldName;
        armor.setDid(SHIFTER_SPECIAL_MOLD_DID);
        armor.setIid(-1);
        armor.setDname(displayName);
        armor.setIname(displayName);
        armor.setSlot(slot);
        armor.setTier(targetTier);
        armor.setCategory("Armor");
        armor.setType("Shifter");
        armor.setBonusAtt("ARMOR");
        armor.setBonusAmount(0.0);
        armor.setLevelReq(1);
        armor.setValue(0L);
        armor.setInote(buildMoldingManifestNote(MOLDING_ARMOR_BONUS_KEY, al * 0.5, al));
        armor.setEquipped(assignArmorSlotIfAvailable(armor));
        armor.setQuantity(1.0);
        character.getInventory().addEquipment(armor);
        return true;
    }

    private boolean hasAvailableWeaponSlot() {
        if (character == null || character.getInventory() == null) return false;
        int equippedWeapons = 0;
        for (DataItemWeapon weapon : character.getInventory().getWeapons()) {
            if (weapon == null || !weapon.isEquipped()) continue;
            if (!isWeaponEquipment(weapon)) continue;
            equippedWeapons++;
            if (equippedWeapons >= ACTIVE_WEAPON_SLOT_COUNT) {
                return false;
            }
        }
        return true;
    }

    private boolean assignArmorSlotIfAvailable(DataItemEquipment armor) {
        if (armor == null || character == null || character.getInventory() == null) return false;
        boolean equipmentEvocation = character.hasEquipmentEvocationSpecialty();
        if (isMatrixEquipment(armor)) {
            String preferredSlot = safe(armor.getSlot());
            if (!preferredSlot.isBlank() && isArmorSlotAvailable(preferredSlot)) {
                return !equipmentEvocation;
            }
            for (String slotName : getMatrixArmorSlotNames()) {
                if (isArmorSlotAvailable(slotName)) {
                    armor.setSlot(slotName);
                    return !equipmentEvocation;
                }
            }
            return false;
        }

        String slotName = safe(armor.getSlot());
        if (slotName.isBlank()) return false;
        if (equipmentEvocation) return false;
        return isArmorSlotAvailable(slotName);
    }

    private boolean isArmorSlotAvailable(String slotName) {
        if (slotName == null || slotName.isBlank() || character == null || character.getInventory() == null) return false;
        for (DataItemEquipment item : character.getInventory().getEquipment()) {
            if (item == null || !item.isEquipped()) continue;
            if (isWeaponEquipment(item)) continue;
            if (isMatrixEquipment(item)) {
                if (slotName.equalsIgnoreCase(safe(item.getSlot()))) {
                    return false;
                }
                continue;
            }
            if (slotName.equalsIgnoreCase(safe(item.getSlot()))) {
                return false;
            }
        }
        return true;
    }

    private boolean isWeaponEquipment(DataItemEquipment item) {
        if (item == null) return false;
        String slot = item.getSlot() == null ? "" : item.getSlot().toLowerCase();
        String category = item.getCategory() == null ? "" : item.getCategory().toLowerCase();
        String type = item.getType() == null ? "" : item.getType().toLowerCase();
        if (category.contains("matrix")) return false;
        return slot.contains("weapon")
                || slot.contains("hand")
                || category.contains("weapon")
                || category.contains("melee")
                || category.contains("ranged")
                || category.contains("aura")
                || type.contains("bow")
                || type.contains("crossbow")
                || type.contains("gun")
                || type.contains("rifle")
                || type.contains("pistol")
                || type.contains("sword")
                || type.contains("axe")
                || type.contains("spear")
                || type.contains("dagger")
                || type.contains("staff")
                || type.contains("mace")
                || type.contains("hammer");
    }

    private boolean isMatrixEquipment(DataItemEquipment item) {
        return item != null && item.getCategory() != null && "Matrix".equalsIgnoreCase(item.getCategory().trim());
    }

    private String[] getMatrixArmorSlotNames() {
        return new String[] { "Head", "Shoulders", "Chest", "Waist", "Legs", "Feet", "Hands" };
    }

    private MoldEntryMetadata resolveMoldEntryMetadata(String moldName) {
        if (character == null || character.getLists() == null || moldName == null || moldName.isBlank()) return null;
        for (List<DataList> group : character.getLists()) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!MOLDS_LIST.equalsIgnoreCase(entry.getList().trim())) continue;
                if (!moldName.equalsIgnoreCase(entry.getName().trim())) continue;
                return parseMoldEntryMetadata(entry.getDescription());
            }
        }
        return null;
    }

    private MoldEntryMetadata parseMoldEntryMetadata(String description) {
        if (description == null || description.isBlank()) return null;
        String category = "";
        String type = "";
        String slot = "";
        for (String part : description.split("\\|")) {
            if (part == null) continue;
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "CATEGORY=", 0, "CATEGORY=".length())) {
                category = trimmed.substring("CATEGORY=".length()).trim();
            } else if (trimmed.regionMatches(true, 0, "TYPE=", 0, "TYPE=".length())) {
                type = trimmed.substring("TYPE=".length()).trim();
            } else if (trimmed.regionMatches(true, 0, "SLOT=", 0, "SLOT=".length())) {
                slot = trimmed.substring("SLOT=".length()).trim();
            }
        }
        if (category.isBlank() && type.isBlank() && slot.isBlank()) return null;
        return new MoldEntryMetadata(category, type, slot);
    }

    private DataItemWeapon resolveWeaponTemplate(String moldName, MoldEntryMetadata metadata, int targetTier) {
        StoreRuleManager ruleManager = sheetFrame == null ? new StoreRuleManager() : sheetFrame.getStoreRuleManager();
        if (metadata != null) {
            if (!CATEGORY_WEAPON.equalsIgnoreCase(metadata.category())) return null;
            if (!metadata.type().isBlank()) {
                return pickBestWeaponTemplate(ruleManager.getItemWeaponData(), metadata.type(), targetTier);
            }
        }
        return pickBestWeaponTemplate(ruleManager.getItemWeaponData(), moldName, targetTier);
    }

    private DataItemEquipment resolveArmorTemplate(String moldName, MoldEntryMetadata metadata, int targetTier) {
        StoreRuleManager ruleManager = sheetFrame == null ? new StoreRuleManager() : sheetFrame.getStoreRuleManager();
        if (metadata != null) {
            if (!CATEGORY_ARMOR.equalsIgnoreCase(metadata.category())) return null;
            if (!metadata.type().isBlank() && !metadata.slot().isBlank()) {
                return pickBestArmorTemplate(ruleManager.getItemEquipmentData(), metadata.type(), metadata.slot(), targetTier);
            }
        }
        return pickBestArmorTemplate(ruleManager.getItemEquipmentData(), moldName, targetTier);
    }

    private DataItemWeapon pickBestWeaponTemplate(List<DataItemWeapon> candidates, String moldName, int targetTier) {
        if (candidates == null || moldName == null || moldName.isBlank()) return null;
        DataItemWeapon bestExactTier = null;
        DataItemWeapon bestFallback = null;
        for (DataItemWeapon candidate : candidates) {
            if (candidate == null) continue;
            if (!matchesMoldName(candidate.getDname(), candidate.getType(), moldName)) continue;
            if (candidate.getTier() == targetTier && bestExactTier == null) {
                bestExactTier = candidate;
            }
            if (bestFallback == null || Math.abs(candidate.getTier() - targetTier) < Math.abs(bestFallback.getTier() - targetTier)) {
                bestFallback = candidate;
            }
        }
        return bestExactTier != null ? bestExactTier : bestFallback;
    }

    private DataItemEquipment pickBestArmorTemplate(List<DataItemEquipment> candidates, String moldName, int targetTier) {
        if (candidates == null || moldName == null || moldName.isBlank()) return null;
        DataItemEquipment bestExactTier = null;
        DataItemEquipment bestFallback = null;
        for (DataItemEquipment candidate : candidates) {
            if (candidate == null) continue;
            if (!CATEGORY_ARMOR.equalsIgnoreCase(candidate.getCategory())) continue;
            if (!matchesMoldName(candidate.getDname(), candidate.getType(), moldName)) continue;
            if (candidate.getTier() == targetTier && bestExactTier == null) {
                bestExactTier = candidate;
            }
            if (bestFallback == null || Math.abs(candidate.getTier() - targetTier) < Math.abs(bestFallback.getTier() - targetTier)) {
                bestFallback = candidate;
            }
        }
        return bestExactTier != null ? bestExactTier : bestFallback;
    }

    private DataItemEquipment pickBestArmorTemplate(List<DataItemEquipment> candidates, String armorType, String armorSlot, int targetTier) {
        if (candidates == null || armorType == null || armorType.isBlank() || armorSlot == null || armorSlot.isBlank()) return null;
        DataItemEquipment bestExactTier = null;
        DataItemEquipment bestFallback = null;
        for (DataItemEquipment candidate : candidates) {
            if (candidate == null) continue;
            if (!CATEGORY_ARMOR.equalsIgnoreCase(candidate.getCategory())) continue;
            if (candidate.getType() == null || !armorType.equalsIgnoreCase(candidate.getType().trim())) continue;
            if (candidate.getSlot() == null || !armorSlot.equalsIgnoreCase(candidate.getSlot().trim())) continue;
            if (candidate.getTier() == targetTier && bestExactTier == null) {
                bestExactTier = candidate;
            }
            if (bestFallback == null || Math.abs(candidate.getTier() - targetTier) < Math.abs(bestFallback.getTier() - targetTier)) {
                bestFallback = candidate;
            }
        }
        return bestExactTier != null ? bestExactTier : bestFallback;
    }

    private boolean matchesMoldName(String dname, String type, String moldName) {
        String target = moldName == null ? "" : moldName.trim();
        if (target.isBlank()) return false;
        if (dname != null && dname.trim().equalsIgnoreCase(target)) return true;
        return type != null && type.trim().equalsIgnoreCase(target);
    }

    private String buildMoldingManifestNote(String bonusKey, double bonusAmount, int al) {
        return MOLDING_MANIFEST_NOTE_PREFIX + "|AL=" + al + "|BONUS=" + (bonusKey == null ? "" : bonusKey) + "|AMOUNT=" + bonusAmount;
    }

    private void clearExistingMoldingManifestItems() {
        if (character == null || character.getInventory() == null) return;
        CharInventory inventory = character.getInventory();
        ArrayList<DataItemEquipment> equipmentToRemove = new ArrayList<>();
        for (DataItemEquipment item : inventory.getEquipment()) {
            if (item != null && isMoldingManifestItem(item.getInote())) {
                equipmentToRemove.add(item);
            }
        }
        for (DataItemEquipment item : equipmentToRemove) {
            inventory.removeEquipment(item);
        }

        ArrayList<DataItem> itemsToRemove = new ArrayList<>();
        for (DataItem item : inventory.getItems()) {
            if (item != null && isMoldingManifestItem(item.getInote())) {
                itemsToRemove.add(item);
            }
        }
        for (DataItem item : itemsToRemove) {
            inventory.removeItem(item);
        }
    }

    private boolean isMoldingManifestItem(String note) {
        return note != null && note.startsWith(MOLDING_MANIFEST_NOTE_PREFIX);
    }

    private String parseMoldCategory(String description) {
        if (description == null || description.isBlank()) return CATEGORY_ALL;
        for (String part : description.split("\\|")) {
            if (part == null) continue;
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "CATEGORY=", 0, "CATEGORY=".length())) {
                String value = trimmed.substring("CATEGORY=".length()).trim();
                if (!value.isBlank()) return value;
            }
        }
        return CATEGORY_ALL;
    }

    private MoldOption findMoldOption(String moldName) {
        if (moldName == null || moldName.isBlank()) return null;
        for (MoldOption option : availableMolds) {
            if (option != null && moldName.equalsIgnoreCase(option.name())) {
                return option;
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record MoldOption(String name, String category) {}
    private record MoldEntryMetadata(String category, String type, String slot) {}
    private record MoldRow(JPanel panel, JComboBox<String> typeBox, JComboBox<String> moldBox, JComboBox<Integer> alBox) {}
    private record MoldRowState(String type, String mold, int al) {}
}
