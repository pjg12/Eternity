package eternity;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

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

        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.setBounds(0, 0, 585, 965-230);

        // ==== Main Tab ====
        panelMain = new PanelCharMain(dataQuery, sheetFrame);
        tabbedPane.addTab("Main Stats", wrap(panelMain));

        // ==== Inventory Tab ====
        panelInventory = new PanelCharInventory(dataQuery, sheetFrame);
        tabbedPane.addTab("Inventory", wrap(panelInventory));

        // ==== Training Tab ====
        panelTraining = new PanelCharTraining(dataQuery, sheetFrame);
        tabbedPane.addTab("Training", wrap(panelTraining));

        panelMaintained = new PanelCharMaintained(dataQuery, sheetFrame);
        tabbedPane.addTab("Maintained", wrap(panelMaintained));
        
        // ==== List Tab ====
        panelList = new PanelCharList(dataQuery, sheetFrame);
        tabbedPane.addTab("Lists", wrap(panelList));

        // ==== Notes Tab ====
        panelNotes = new PanelCharNotes(dataQuery, sheetFrame);
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

    public void updateData() {
        if (character == null) return;

        character.updateAll();

        panelMain.updateCharacter(character);
        panelInventory.updateCharacter(character);
        panelTraining.updateCharacter(character);
        panelMaintained.updateCharacter(character);
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
