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

    private final DataQuery dataQuery;
    private CharData character;
    private final FrameSheet sheetFrame;

    private final JTabbedPane tabbedPane;

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

    public PanelChar(DataQuery dataQuery, FrameSheet sheetFrame) {
        this.dataQuery = dataQuery;
        this.sheetFrame = sheetFrame;
        setLayout(null);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.setBounds(0, 0, 585, 965-230);

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

        panelMain.updateCharacter(character);
        panelInventory.updateCharacter(character);
        panelTraining.updateCharacter(character);
        panelMaintained.updateCharacter(character);
        panelGranted.updateCharacter(character);
        panelList.updateCharacter(character);
        panelNotes.updateCharacter(character);

        // Optional:
        // panelBattle.updateCharacter(character);


    }

    public void updateCharacter(CharData character) {
        this.character = character;
        updateData();
    }

    /** Persist equip dropdown selections back into the character inventory. */
    public void saveEquipmentSelections() {
        panelInventory.applyEquipSelections();
    }
}



