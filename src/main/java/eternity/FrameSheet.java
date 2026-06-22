package eternity;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FrameSheet extends JFrame {

    // References
    private final StoreRuleManager ruleManager;
    private final ArrayList<StoreMetaChar> metaStore;
    private final boolean promptToSaveOnClose;
    private final boolean exitProgramWhenLastSheetCloses;
    private StoreCharData character;
    private final StoreCharManager charManager;

    // UI Constants
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 1000;
    private static final int IMAGE_PANEL_HEIGHT = 230;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font SUBHEADER_FONT = new Font(null, Font.PLAIN, 17);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int PADDING_TOP_BOTTOM = 25;
    private static final int PADDING_LEFT_RIGHT = 20;
    private static final int SPACING_HEADER = 10;
    private static final int SPACING_STATUS = 10;
    private static final int SPACING_BEFORE_BUTTONS = 15;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_SPACING = 20;


    // Frame dimensions and layout
    
    //private static final int IMAGE_PANEL_Y = 0;
    //private static final int CHAR_PANEL_Y = IMAGE_PANEL_HEIGHT;
    
    // Timing
    private static final int AUTO_SAVE_INTERVAL_MS = 5 /* Minutes */ * 60 /* Seconds Per Min */ * 1000 /* MS Per Sec */;
    
    // UI Strings
    private static final String NO_CHAR_LOADED = "No Character Loaded";
    private static final String CHAR_SAVED_MSG = "Character saved.";
    private static final String SAVE_SUCCESS_TITLE = "Saved";
    private static final String SAVE_FAILED_MSG = "Failed to save character.";
    private static final String SAVE_FAILED_TITLE = "Save Failed";
    private static final String WARNING_TITLE = "Warning";
    private static final String CHAR_EQUIPPED_MSG = "Character updated from equipment and saved.";
    private static final String EXIT_SAVE_PROMPT = "Save the current character before closing?";
    private static final String EXIT_CONFIRM_TITLE = "Save Before Exit";
    private static final ScheduledExecutorService autoSaveExecutor = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "character-auto-save");
        t.setDaemon(true);
        return t;
    });
    private static final Set<FrameSheet> OPEN_SHEETS = Collections.synchronizedSet(new HashSet<>());

    
    private final Map<Integer, Integer> charStoreIndexByCharId;
    private final ScheduledFuture<?> autoSaveTask;
    private boolean shuttingDown;
    
    private FrameNew newFrame;
    private FrameLoad loadFrame;
    private FrameExp levelFrame;
    private FrameTraining trainingNewFrame;
    private FrameTrainingExp trainingXpFrame;
    private FrameTrainingExisting trainingExistingFrame;
    private FrameInventoryAdd addInventoryFrame;
    private FrameInventoryRemove removeInventoryFrame;
    private FrameStatus statusFrame;

    // === UI Components ===
    private PanelImage characterImage;
    private PanelChar charPanel;
    private FrameDetail detailFrame;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------
    
    public FrameSheet() {
        this(true, true);
    }

    public FrameSheet(boolean promptToSaveOnClose, boolean exitProgramWhenLastSheetCloses) {
        this.ruleManager = new StoreRuleManager();
        StoreMetaManager.loadCharStore();
        this.metaStore = StoreMetaManager.getCharStore();
        this.charStoreIndexByCharId = buildCharStoreIndex(metaStore);
        this.promptToSaveOnClose = promptToSaveOnClose;
        this.exitProgramWhenLastSheetCloses = exitProgramWhenLastSheetCloses;
        this.charManager = new StoreCharManager();
        OPEN_SHEETS.add(this);

        setupFrame();
        initPanels();
        
        // Schedule autosave using shared thread pool to avoid creating new threads
        autoSaveTask = autoSaveExecutor.scheduleAtFixedRate(
            this::runAutoSave,
            AUTO_SAVE_INTERVAL_MS,
            AUTO_SAVE_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }

    public static FrameSheet createNarratorSheet() {
        return new FrameSheet(false, false);
    }

    public StoreRuleManager getStoreRuleManager() { return ruleManager; }

    private void setupFrame() {
        setLayout(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle(NO_CHAR_LOADED);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
        setVisible(true);
        setInteractive(false);
    }

    /**
     * Initializes core panels.
     */
    private void initPanels() {
        characterImage = new PanelImage(this, character);
        characterImage.setBounds(0, 0, FRAME_WIDTH, IMAGE_PANEL_HEIGHT);
        add(characterImage);

        charPanel = new PanelChar(ruleManager, this);
        charPanel.setBounds(0, IMAGE_PANEL_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT - IMAGE_PANEL_HEIGHT);
        add(charPanel);
    }

    public void loadCharacter(StoreCharData character) {
        this.character = character;
        if (this.character != null) {
            this.character.syncIdentityDerivedState(ruleManager);
            this.character.syncLevelBaseResources(ruleManager);
            this.character.syncLevelCombatScalers(ruleManager);
            this.character.updateAll(); // ensure derived/attribute data is current
        }
        setTitle(character.getName());

        characterImage.updateCharacter(character);
        if (charPanel != null) {
            charPanel.updateCharacter(character);
            charPanel.setInteractive(true);
        }

        revalidate();
        repaint();
    }

    /** Refreshes the portrait/date panel using the current character. */
    public void refreshImagePanel() {
        if (characterImage != null && character != null) {
            characterImage.updateCharacter(character);
        }
    }

    public void invalidateCharacterPortrait(int characterIndex) {
        if (characterImage != null && characterIndex > 0) {
            characterImage.invalidatePortrait(characterIndex);
        }
    }

    /** Refreshes the main stats panel (PanelCharMain) after out-of-band changes such as rests. */
    public void refreshMainPanel() {
        if (charPanel != null && character != null) {
            character.updateAll();
            charPanel.refreshMainOnly();
            charPanel.refreshAllPrimaryHeaders();
        }
    }

    /** Refreshes the training tab without reloading the full sheet. */
    public void refreshTrainingPanel() {
        if (charPanel != null && character != null) {
            character.updateAll();
            charPanel.refreshTrainingOnly();
            charPanel.refreshMaintainedOnly();
            charPanel.refreshGrantedOnly();
            charPanel.refreshAllPrimaryHeaders();
        }
    }

    /** Refreshes the inventory tab so doll/equipment displays pick up identity changes. */
    public void refreshInventoryPanel() {
        if (charPanel != null && character != null) {
            character.updateAll();
            charPanel.refreshInventoryOnly();
            charPanel.refreshAllPrimaryHeaders();
        }
    }

    public void refreshAllCharacterPanelHeaders() {
        if (charPanel != null && character != null) {
            character.updateAll();
            charPanel.refreshAllPrimaryHeaders();
        }
    }

    public List<PanelCharMinion.MinionInitiativeInfo> getSummonedMinionInitiativeInfo() {
        return charPanel == null ? List.of() : charPanel.getSummonedMinionInitiativeInfo();
    }

    /** Enables or disables all interactive panels. */
    public void setInteractive(boolean enabled) {
        if (charPanel != null) {
            charPanel.setInteractive(enabled);
        }
    }

    // === UI EVENT HANDLERS ===

    public void onNewPressed() {
        if (newFrame == null) {
            character = new StoreCharData();
            character.getIdentity().setIndex(StoreMetaManager.getNextFreeIndex(metaStore));
            newFrame = new FrameNew(this, character);
        }
        newFrame.setVisible(true);
    }

    /*public void reopenNewBuilder() {
        character = new StoreCharData();
        newFrame = new FrameNew(this, character);
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setVisible(true);
    }*/

    public void onLoadPressed() {
        if (loadFrame == null) {
            loadFrame = new FrameLoad(this, metaStore);
            loadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        loadFrame.setOpenCombatHelperAfterLoad(false);
        loadFrame.setVisible(true);
    }

    public void onLoadPressedForPlayer() {
        if (loadFrame == null) {
            loadFrame = new FrameLoad(this, metaStore);
            loadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        loadFrame.setOpenCombatHelperAfterLoad(true);
        loadFrame.setVisible(true);
    }

    public boolean hasActiveCharacter() {
        return hasLoadedCharacter();
    }

    public FrameSheet createSiblingSheet() {
        return new FrameSheet(promptToSaveOnClose, exitProgramWhenLastSheetCloses);
    }

    public void openCombatHelper() {
        openCombatHelper(false);
    }

    public void openCombatHelper(boolean playerMode) {
        if (!requireCharacter() || characterImage == null) {
            return;
        }
        characterImage.openCombatHelper(playerMode);
    }

    public void enterPlayerMode() {
        if (!requireCharacter() || characterImage == null) {
            return;
        }
        characterImage.enterPlayerMode();
    }

    /** Saves the currently loaded character to disk and updates the store metadata. */
    public void onSavePressed() {
        if (!checkCharacterLoaded("No character loaded to save.")) return;
        saveCharacter(false, true, true);
    }
    
    /**
     * Consolidates the save workflow: persist equipment selections, update all derived data, save to disk,
     * update store metadata, and show feedback. skipEquipmentSave = true for equipment-only saves.
     */
    private boolean saveCharacter(boolean skipEquipmentSave, boolean rotateBackups, boolean showFeedback) {
        if (!skipEquipmentSave && charPanel != null) {
            charPanel.saveEquipmentSelections();
        }

        character.syncIdentityDerivedState(ruleManager);
        character.syncLevelBaseResources(ruleManager);
        character.syncLevelCombatScalers(ruleManager);
        character.updateAll();

        boolean ok;
        if (character.getIdentity() == null || character.getIdentity().getIndex() <= 0) {
            ok = StoreCharManager.saveCharacterNew(character);
        } else if (rotateBackups) {
            ok = StoreCharManager.saveCharacterManual(character);
        } else {
            ok = StoreCharManager.saveCharacterAuto(character);
        }

        if (ok) {
            updateCharStoreEntry();
            if (showFeedback) {
                JOptionPane.showMessageDialog(this, CHAR_SAVED_MSG, SAVE_SUCCESS_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (showFeedback) {
            JOptionPane.showMessageDialog(this, SAVE_FAILED_MSG, SAVE_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        return ok;
    }

    // Placeholder hooks for panel actions
    public void expPressed() {
        if (!requireCharacter()) return;
        if (levelFrame == null) {
            levelFrame = new FrameExp(this, ruleManager);
        }
        levelFrame.updateCharacter(character);
        levelFrame.addXp();
        levelFrame.setVisible(true);
    }
    public void restPressed() {
        if (!requireCharacter()) return;
        restPressed(character);
    }
    public void editPressed() {
        if (!requireCharacter()) return;
        if (detailFrame == null) {
            detailFrame = new FrameDetail(this, ruleManager);
            detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        detailFrame.updateDetails(character);
        detailFrame.setVisible(true);
    }
    public void statusPressed() {
        if (!requireCharacter()) return;
        if (statusFrame == null) {
            statusFrame = new FrameStatus(this, character);
            statusFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        statusFrame.updateCharacter(character);
        statusFrame.setVisible(true);
    }
    public void restPressed(StoreCharData character) {
        FrameRest rest = new FrameRest(this);
        rest.updateCharacter(character);
        rest.setVisible(true);
    }
    public void trainNewPressed() {
        if (!requireCharacter()) return;
        if (trainingNewFrame == null) {
            trainingNewFrame = new FrameTraining(this, ruleManager, character);
        }
        trainingNewFrame.updateCharacter(character);
        trainingNewFrame.showCard(FrameTraining.CARD_NEW);
        trainingNewFrame.setVisible(true);
    }
    public void gainTrainingXpPressed() {
        if (!requireCharacter()) return;
        if (trainingXpFrame == null) {
            trainingXpFrame = new FrameTrainingExp(this, ruleManager);
        }
        trainingXpFrame.updateCharacter(character);
        trainingXpFrame.setVisible(true);
    }
    public void trainExistingPressed() {
        trainExistingPressed(null, null);
    }
    public void trainExistingPressed(String category, String techniqueName) {
        if (!requireCharacter()) return;
        if (trainingNewFrame == null) {
            trainingNewFrame = new FrameTraining(this, ruleManager, character);
        }
        trainingNewFrame.updateCharacter(character);
        trainingNewFrame.showCard(FrameTraining.CARD_EXISTING);
        trainingNewFrame.setVisible(true);
    }

    /**
     * Placeholder hooks invoked by PanelCharInventory buttons.
     * Keeps the UI from throwing NPEs until full editors are added.
     */
    public void equipCharacter() {
        if (!checkCharacterLoaded("No character loaded to save.")) return;
        if (charPanel != null) {
            charPanel.saveEquipmentSelections();
        }
        character.syncIdentityDerivedState(ruleManager);
        character.syncLevelBaseResources(ruleManager);
        character.syncLevelCombatScalers(ruleManager);
        character.updateAll();
        //StoreMetaManager.saveCharacter(character);
        JOptionPane.showMessageDialog(this, CHAR_EQUIPPED_MSG, SAVE_SUCCESS_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    public void inventoryCharacter() {
        if (!checkCharacterLoaded("No character loaded.")) return;
        if (addInventoryFrame == null) {
            addInventoryFrame = new FrameInventoryAdd(this, ruleManager);
            addInventoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        addInventoryFrame.updateCharacter(character);
        addInventoryFrame.setVisible(true);
    }

    public void removeInventoryCharacter() {
        if (!checkCharacterLoaded("No character loaded.")) return;
        if (removeInventoryFrame == null) {
            removeInventoryFrame = new FrameInventoryRemove(this, ruleManager);
            removeInventoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        removeInventoryFrame.updateCharacter(character);
        removeInventoryFrame.setVisible(true);
    }

    private static Map<Integer, Integer> buildCharStoreIndex(ArrayList<StoreMetaChar> charStore) {
        Map<Integer, Integer> indexByCharId = new HashMap<>(Math.max(16, charStore.size() * 2));
        for (int i = 0; i < charStore.size(); i++) {
            indexByCharId.put(charStore.get(i).getIndex(), i);
        }
        return indexByCharId;
    }
    
    /**
     * Refreshes/creates the StoreMetaChar entry for the current character and writes the charStore.json file.
     */
    private void updateCharStoreEntry() {
        if (metaStore == null || character == null || character.getIdentity() == null) return;

        CharIdentity id = character.getIdentity();
        int idx = id.getIndex();

        StoreMetaChar updated = new StoreMetaChar(
            idx,
            id.getName(),
            id.getCampaign(),
            id.getRace(),
            id.getCharClass(),
            id.getLevel(),
            new Timestamp(System.currentTimeMillis())
        );

        Integer existingIndex = charStoreIndexByCharId.get(idx);
        if (existingIndex != null) {
            metaStore.set(existingIndex, updated);
        } else {
            existingIndex = metaStore.size();
            metaStore.add(updated);
        }
        charStoreIndexByCharId.put(idx, existingIndex);

        //StoreMetaManager.saveCharStore(charStore);
    }
    
    /** Checks if a character is loaded; shows warning dialog if not. Returns true if loaded. */
    private boolean checkCharacterLoaded(String warningMessage) {
        if (character != null) return true;
        JOptionPane.showMessageDialog(this, warningMessage, WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    /** Returns true if character is loaded, false otherwise (silent check for internal validation). */
    private boolean requireCharacter() {
        return character != null;
    }

    private void runAutoSave() {
        if (!requireCharacter()) return;
        //StoreMetaManager.saveCharacterAuto(character);
    }

    private void handleWindowClosing() {
        boolean shouldExitProgram = exitProgramWhenLastSheetCloses && getOpenSheetCount() <= 1;
        if (!promptToSaveOnClose || !hasLoadedCharacter()) {
            if (shouldExitProgram) {
                exitApplication();
            } else {
                closeCurrentSheet();
            }
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            EXIT_SAVE_PROMPT,
            EXIT_CONFIRM_TITLE,
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }
        if (choice == JOptionPane.YES_OPTION && !saveCharacter(false, true, false)) {
            JOptionPane.showMessageDialog(this, SAVE_FAILED_MSG, SAVE_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (shouldExitProgram) {
            exitApplication();
        } else {
            closeCurrentSheet();
        }
    }

    private void exitApplication() {
        if (shuttingDown) return;
        shuttingDown = true;

        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window != null) {
                window.dispose();
            }
        }
        System.exit(0);
    }

    private void closeCurrentSheet() {
        if (shuttingDown) return;
        shuttingDown = true;
        disposeOwnedWindows();
        dispose();
    }

    private void disposeOwnedWindows() {
        safeDispose(newFrame);
        safeDispose(loadFrame);
        safeDispose(levelFrame);
        safeDispose(trainingNewFrame);
        safeDispose(trainingXpFrame);
        safeDispose(trainingExistingFrame);
        safeDispose(addInventoryFrame);
        safeDispose(removeInventoryFrame);
        safeDispose(statusFrame);
        safeDispose(detailFrame);
        if (characterImage != null) {
            characterImage.disposeOwnedWindows();
        }
    }

    private void safeDispose(Window window) {
        if (window != null) {
            window.dispose();
        }
    }

    private static int getOpenSheetCount() {
        synchronized (OPEN_SHEETS) {
            return OPEN_SHEETS.size();
        }
    }

    @Override
    public void dispose() {
        if (autoSaveTask != null) autoSaveTask.cancel(false);
        OPEN_SHEETS.remove(this);
        super.dispose();
    }

    private boolean hasLoadedCharacter() {
        return character != null
                && character.getIdentity() != null
                && character.getIdentity().getIndex() > 0;
    }




    //////////////////////////////////////////////////////////////////////////////////////////////
    /// 
    public StoreCharManager getCharManager() { return charManager; }
}

