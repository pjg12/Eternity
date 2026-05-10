package eternity;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;

/**
 * Main character sheet panel. Contains tabs for stats, inventory, training, etc.
 */
public class PanelChar extends JPanel {

    private static final long serialVersionUID = 1L;

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
    private static final int TAB_NOTES = 6;

    // Panels
    private final PanelCharMain panelMain;
    private final PanelCharInventory panelInventory;
    private final PanelCharTraining panelTraining;
    private final PanelCharList panelList;
    private final PanelCharMaintained panelMaintained;
    private final PanelCharGranted panelGranted;
    private final PanelCharNotes panelNotes;

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
        dirtyTabs = new boolean[7];

        // ==== Main Tab ====
        panelMain = new PanelCharMain(dataQuery, sheetFrame);
        panelMain.setTabTitle("Main Stats");
        tabbedPane.addTab("Main Stats", wrap(panelMain));

        // ==== Inventory Tab ====
        panelInventory = new PanelCharInventory(dataQuery, sheetFrame);
        panelInventory.setTabTitle("Inventory");
        tabbedPane.addTab("Inventory", wrap(panelInventory));

        // ==== Training Tab ====
        panelTraining = new PanelCharTraining(dataQuery, sheetFrame);
        panelTraining.setTabTitle("Training");
        tabbedPane.addTab("Training", wrap(panelTraining));

        panelMaintained = new PanelCharMaintained(dataQuery, sheetFrame);
        panelMaintained.setTabTitle("Maintained");
        tabbedPane.addTab("Maintained", wrap(panelMaintained));
        
        panelGranted = new PanelCharGranted(dataQuery, sheetFrame);
        panelGranted.setTabTitle("Granted");
        tabbedPane.addTab("Granted", wrap(panelGranted));
        
        // ==== List Tab ====
        panelList = new PanelCharList(dataQuery, sheetFrame);
        panelList.setTabTitle("Lists");
        tabbedPane.addTab("Lists", wrap(panelList));

        // ==== Notes Tab ====
        panelNotes = new PanelCharNotes(dataQuery, sheetFrame);
        panelNotes.setTabTitle("Notes");
        tabbedPane.addTab("Notes", wrap(panelNotes));

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

        character.updateAll();
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
        if (selectedIndex < 0 || selectedIndex >= dirtyTabs.length || !dirtyTabs[selectedIndex]) {
            return;
        }
        refreshTab(selectedIndex);
    }

    private void refreshTab(int tabIndex) {
        switch (tabIndex) {
            case TAB_MAIN -> panelMain.updateCharacter(character);
            case TAB_INVENTORY -> panelInventory.updateCharacter(character);
            case TAB_TRAINING -> panelTraining.updateCharacter(character);
            case TAB_MAINTAINED -> panelMaintained.updateCharacter(character);
            case TAB_GRANTED -> panelGranted.updateCharacter(character);
            case TAB_LISTS -> panelList.updateCharacter(character);
            case TAB_NOTES -> panelNotes.updateCharacter(character);
            default -> { return; }
        }
        dirtyTabs[tabIndex] = false;
    }
}




