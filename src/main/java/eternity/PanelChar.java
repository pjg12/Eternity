package eternity;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;
import java.util.List;

/**
 * Main character sheet panel. Contains tabs for stats, inventory, training, etc.
 */
public class PanelChar extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String TAB_TITLE_MAIN = "Main Stats";
    private static final String TAB_TITLE_INVENTORY = "Inventory";
    private static final String TAB_TITLE_TRAINING = "Training";
    private static final String TAB_TITLE_MAINTAINED = "Maintained";
    private static final String TAB_TITLE_GRANTED = "Granted";
    private static final String TAB_TITLE_LISTS = "Lists";
    private static final String TAB_TITLE_MINION = "Minion";
    private static final String TAB_TITLE_NOTES = "Notes";

    private final StoreRuleManager dataQuery;
    private StoreCharData character;
    private final FrameSheet sheetFrame;

    private final JTabbedPane tabbedPane;
    private final boolean[] dirtyTabs;

    private static final int TAB_MAIN = 0;
    private static final int TAB_INVENTORY = 1;
    private static final int TAB_TRAINING = 2;
    private static final int TAB_MAINTAINED = 3;
    private static final int TAB_GRANTED = 4;
    private static final int TAB_LISTS = 5;
    private static final int TAB_MINION = 6;
    private static final int TAB_NOTES = 7;

    // Panels
    private final PanelCharMain panelMain;
    private final PanelCharInventory panelInventory;
    private final PanelCharTraining panelTraining;
    private final PanelCharList panelList;
    private final PanelCharMaintained panelMaintained;
    private final PanelCharGranted panelGranted;
    private final PanelCharMinion panelMinion;
    private final PanelCharNotes panelNotes;
    private final JScrollPane minionTabScroll;

    // Optional future panels
    // private final PanelCharBattle panelBattle;
    // 

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public PanelChar(StoreRuleManager dataQuery, FrameSheet sheetFrame) {
        this.dataQuery = dataQuery;
        this.sheetFrame = sheetFrame;
        setLayout(null);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.setBounds(0, 0, 585, 965-230);
        dirtyTabs = new boolean[8];

        // ==== Main Tab ====
        panelMain = new PanelCharMain(dataQuery, sheetFrame);
        panelMain.setTabTitle(TAB_TITLE_MAIN);
        tabbedPane.addTab(TAB_TITLE_MAIN, wrap(panelMain));

        // ==== Inventory Tab ====
        panelInventory = new PanelCharInventory(dataQuery, sheetFrame);
        panelInventory.setTabTitle(TAB_TITLE_INVENTORY);
        tabbedPane.addTab(TAB_TITLE_INVENTORY, wrap(panelInventory));

        // ==== Training Tab ====
        panelTraining = new PanelCharTraining(dataQuery, sheetFrame);
        panelTraining.setTabTitle(TAB_TITLE_TRAINING);
        tabbedPane.addTab(TAB_TITLE_TRAINING, wrap(panelTraining));

        panelMaintained = new PanelCharMaintained(dataQuery, sheetFrame);
        panelMaintained.setTabTitle(TAB_TITLE_MAINTAINED);
        tabbedPane.addTab(TAB_TITLE_MAINTAINED, wrap(panelMaintained));
        
        panelGranted = new PanelCharGranted(dataQuery, sheetFrame);
        panelGranted.setTabTitle(TAB_TITLE_GRANTED);
        tabbedPane.addTab(TAB_TITLE_GRANTED, wrap(panelGranted));
        
        // ==== List Tab ====
        panelList = new PanelCharList(dataQuery, sheetFrame);
        panelList.setTabTitle(TAB_TITLE_LISTS);
        tabbedPane.addTab(TAB_TITLE_LISTS, wrap(panelList));

        panelMinion = new PanelCharMinion(dataQuery, sheetFrame);
        panelMinion.setTabTitle(TAB_TITLE_MINION);
        minionTabScroll = wrap(panelMinion);

        // ==== Notes Tab ====
        panelNotes = new PanelCharNotes(dataQuery, sheetFrame);
        panelNotes.setTabTitle(TAB_TITLE_NOTES);
        tabbedPane.addTab(TAB_TITLE_NOTES, wrap(panelNotes));

        tabbedPane.addChangeListener(e -> refreshSelectedTabIfNeeded());

        

        // ==== Optional Panels ====
        /*
        panelBattle = new PanelCharBattle(dataStore, sheetFrame);
        tabbedPane.addTab("Battle Stats", wrap(panelBattle));

        
        */
    }

    // ---------------------------------------------------------
    // Utility: Wrap panel in a standard scroll pane
    // ---------------------------------------------------------

    private JScrollPane wrap(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
        return scroll;
    }

    // ---------------------------------------------------------
    // Character Data Updates
    // ---------------------------------------------------------

    /** Enables or disables all tabs and their child controls. */
    public void setInteractive(boolean enabled) {
        tabbedPane.setEnabled(enabled);
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, enabled);
        }
        setEnabledDeep(panelMain, enabled);
        setEnabledDeep(panelInventory, enabled);
        setEnabledDeep(panelTraining, enabled);
        setEnabledDeep(panelMaintained, enabled);
        setEnabledDeep(panelGranted, enabled);
        setEnabledDeep(panelList, enabled);
        setEnabledDeep(panelMinion, enabled);
        setEnabledDeep(panelNotes, enabled);
        // Re-lock inventory checkboxes so they stay non-interactive even when panels are enabled
        if (panelInventory != null) {
            panelInventory.enforceReadOnlyChecks();
        }
    }

    private void setEnabledDeep(java.awt.Component comp, boolean enabled) {
        comp.setEnabled(enabled);
        if (comp instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                setEnabledDeep(child, enabled);
            }
        }
    }

    public void updateData() {
        if (character == null) return;

        character.syncIdentityDerivedState(dataQuery);
        character.syncLevelBaseResources(dataQuery);
        character.syncLevelCombatScalers(dataQuery);
        character.updateAll();
        syncSpecialTabs();
        markAllTabsDirty();
        refreshSelectedTabIfNeeded();
    }

    public void updateCharacter(StoreCharData character) {
        this.character = character;
        updateData();
    }

    public void refreshMainOnly() {
        if (character == null) return;
        panelMain.updateCharacter(character);
        dirtyTabs[TAB_MAIN] = false;
    }

    public void refreshInventoryOnly() {
        if (character == null) return;
        panelInventory.updateCharacter(character);
        dirtyTabs[TAB_INVENTORY] = false;
    }

    public void refreshTrainingOnly() {
        if (character == null) return;
        panelTraining.updateCharacter(character);
        dirtyTabs[TAB_TRAINING] = false;
    }

    public void refreshMaintainedOnly() {
        if (character == null) return;
        panelMaintained.updateCharacter(character);
        dirtyTabs[TAB_MAINTAINED] = false;
    }

    public void refreshGrantedOnly() {
        if (character == null) return;
        panelGranted.updateCharacter(character);
        dirtyTabs[TAB_GRANTED] = false;
    }

    public void refreshMinionOnly() {
        if (character == null) return;
        panelMinion.updateCharacter(character);
        dirtyTabs[TAB_MINION] = false;
    }

    public void refreshAllPrimaryHeaders() {
        if (character == null) return;
        panelMain.refreshHeaderState(character);
        panelInventory.refreshHeaderState(character);
        panelTraining.refreshHeaderState(character);
        panelMaintained.refreshHeaderState(character);
        panelGranted.refreshHeaderState(character);
        panelList.refreshHeaderState(character);
        panelMinion.refreshHeaderState(character);
        panelNotes.refreshHeaderState(character);
    }

    /** Persist equip dropdown selections back into the character inventory. */
    public void saveEquipmentSelections() {
        panelInventory.applyEquipSelections();
    }

    private void markAllTabsDirty() {
        for (int i = 0; i < dirtyTabs.length; i++) {
            dirtyTabs[i] = true;
        }
    }

    private void refreshSelectedTabIfNeeded() {
        if (character == null) return;
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        int tabSlot = resolveDirtyTabSlot(selectedIndex);
        if (tabSlot < 0 || tabSlot >= dirtyTabs.length || !dirtyTabs[tabSlot]) return;
        refreshTab(selectedIndex, tabSlot);
    }

    private void refreshTab(int selectedIndex, int tabSlot) {
        String title = tabbedPane.getTitleAt(selectedIndex);
        switch (title) {
            case TAB_TITLE_MAIN -> panelMain.updateCharacter(character);
            case TAB_TITLE_INVENTORY -> panelInventory.updateCharacter(character);
            case TAB_TITLE_TRAINING -> panelTraining.updateCharacter(character);
            case TAB_TITLE_MAINTAINED -> panelMaintained.updateCharacter(character);
            case TAB_TITLE_GRANTED -> panelGranted.updateCharacter(character);
            case TAB_TITLE_LISTS -> panelList.updateCharacter(character);
            case TAB_TITLE_MINION -> panelMinion.updateCharacter(character);
            case TAB_TITLE_NOTES -> panelNotes.updateCharacter(character);
            default -> {
                return;
            }
        }
        dirtyTabs[tabSlot] = false;
    }

    private int resolveDirtyTabSlot(int selectedIndex) {
        String title = tabbedPane.getTitleAt(selectedIndex);
        return switch (title) {
            case TAB_TITLE_MAIN -> TAB_MAIN;
            case TAB_TITLE_INVENTORY -> TAB_INVENTORY;
            case TAB_TITLE_TRAINING -> TAB_TRAINING;
            case TAB_TITLE_MAINTAINED -> TAB_MAINTAINED;
            case TAB_TITLE_GRANTED -> TAB_GRANTED;
            case TAB_TITLE_LISTS -> TAB_LISTS;
            case TAB_TITLE_MINION -> TAB_MINION;
            case TAB_TITLE_NOTES -> TAB_NOTES;
            default -> -1;
        };
    }

    private void syncSpecialTabs() {
        boolean shouldShowMinionTab = isLeaderCharacter();
        int currentMinionIndex = tabbedPane.indexOfTab(TAB_TITLE_MINION);
        if (shouldShowMinionTab && currentMinionIndex < 0) {
            int notesIndex = tabbedPane.indexOfTab(TAB_TITLE_NOTES);
            int insertIndex = notesIndex >= 0 ? notesIndex : tabbedPane.getTabCount();
            tabbedPane.insertTab(TAB_TITLE_MINION, null, minionTabScroll, null, insertIndex);
            dirtyTabs[TAB_MINION] = true;
            return;
        }
        if (!shouldShowMinionTab && currentMinionIndex >= 0) {
            tabbedPane.removeTabAt(currentMinionIndex);
            dirtyTabs[TAB_MINION] = false;
        }
    }

    private boolean isLeaderCharacter() {
        if (character == null || character.getIdentity() == null) return false;
        String charClass = character.getIdentity().getCharClass();
        return charClass != null && charClass.equalsIgnoreCase("Leader");
    }

    public List<PanelCharMinion.MinionInitiativeInfo> getSummonedMinionInitiativeInfo() {
        return panelMinion == null ? List.of() : panelMinion.getSummonedMinionInitiativeInfo();
    }
}




