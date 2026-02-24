package eternity;

import java.util.ArrayList;
import java.sql.Timestamp;
import javax.swing.Timer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FrameSheet extends JFrame {
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 1000;
    private static final int AUTO_SAVE_INTERVAL_MS = 5 * 60 * 1000;

    private final DataQuery dataQuery;
    private final ArrayList<CharStore> charStore;
    private CharData character;
    private FrameNew newFrame;
    private FrameLoad loadFrame;
    private FrameExp levelFrame;
    private FrameTrainingNew trainingNewFrame;
    private FrameTrainingExisting trainingExistingFrame;
    private FrameInventoryAdd addInventoryFrame;
    private FrameInventoryRemove removeInventoryFrame;

    // === UI Components ===
    private PanelImage characterImage;
    private PanelChar charPanel;
    private final Timer autoSaveTimer;

    public FrameSheet(ArrayList<CharStore> charStore) {
        dataQuery = new DataQuery();
    	this.charStore = charStore;
        autoSaveTimer = new Timer(AUTO_SAVE_INTERVAL_MS, e -> runAutoSave());
        autoSaveTimer.setRepeats(true);

        setupFrame();
        initPanels();
        setInteractive(false);
        autoSaveTimer.start();
    }

    private void setupFrame() {
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("No Character Loaded");
        setVisible(true);
    }

    /**
     * Initializes core panels.
     */
    private void initPanels() {
        characterImage = new PanelImage(this, character);
        characterImage.setBounds(0, 0, FRAME_WIDTH, 230);
        add(characterImage);

        charPanel = new PanelChar(dataQuery, this);
        charPanel.setBounds(0, 230, FRAME_WIDTH, FRAME_HEIGHT - 230);
        add(charPanel);
    }

    public void loadCharacter(CharData character) {
        this.character = character;
        if (this.character != null) {
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

    /** Refreshes the main stats panel (PanelCharMain) after out-of-band changes such as rests. */
    public void refreshMainPanel() {
        if (charPanel != null && character != null) {
            charPanel.updateCharacter(character);
        }
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
            character = new CharData();
            newFrame = new FrameNew(this, dataQuery, character);
            newFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            
        }
        newFrame.setVisible(true);
    }

    public void onLoadPressed() {
        if (loadFrame == null) {
            loadFrame = new FrameLoad(this, charStore);
            loadFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        loadFrame.setVisible(true);
    }

    /** Saves the currently loaded character to disk and updates the store metadata. */
    public void onSavePressed() {
        if (character == null) {
            JOptionPane.showMessageDialog(this, "No character loaded to save.", "Save Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Persist any equipment dropdown selections before saving
        if (charPanel != null) {
            charPanel.saveEquipmentSelections();
        }

        character.updateAll();
        boolean ok = CharacterDataManager.saveCharacterManual(character);

        if (ok) {
            updateCharStoreEntry();
            JOptionPane.showMessageDialog(this, "Character saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save character.", "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Placeholder hooks for panel actions
    public void expPressed() {
        if (character == null) return;
        if (levelFrame == null) {
            levelFrame = new FrameExp(this, dataQuery);
        }
        levelFrame.updateCharacter(character);
        levelFrame.addXp();
        levelFrame.setVisible(true);
    }
    public void restPressed() {
        if (character == null) return;
        restPressed(character);
    }
    public void editPressed() {
        if (character == null) return;
        FrameDetail detail = new FrameDetail(this, dataQuery);
        detail.updateDetails(character);
        detail.setVisible(true);
    }
    public void restPressed(CharData character) {
        FrameRest rest = new FrameRest(this);
        rest.updateCharacter(character);
        rest.setVisible(true);
    }
    public void trainNewPressed() {
        if (character == null) return;
        if (trainingNewFrame == null) {
            trainingNewFrame = new FrameTrainingNew(this, dataQuery);
        }
        trainingNewFrame.updateCharacter(character);
        trainingNewFrame.setVisible(true);
    }
    public void trainExistingPressed() {
        trainExistingPressed(null, null);
    }
    public void trainExistingPressed(String category, String techniqueName) {
        if (character == null) return;
        if (trainingExistingFrame == null) {
            trainingExistingFrame = new FrameTrainingExisting(this, dataQuery);
        }
        trainingExistingFrame.updateCharacter(character);
        if (category != null && techniqueName != null) {
            trainingExistingFrame.selectTechnique(category, techniqueName);
        }
        trainingExistingFrame.setVisible(true);
    }

    /**
     * Placeholder hooks invoked by PanelCharInventory buttons.
     * Keeps the UI from throwing NPEs until full editors are added.
     */
    public void equipCharacter() {
        if (character != null) {
            // Push current equip dropdown selections back into the character
            if (charPanel != null) {
                charPanel.saveEquipmentSelections();
            }
            // Recompute derived stats/resources after equipment changes, then persist.
            character.updateAll();
            CharacterDataManager.saveCharacter(character);
            JOptionPane.showMessageDialog(this, "Character updated from equipment and saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No character loaded to save.", "Save Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void inventoryCharacter() {
        if (character == null) {
            JOptionPane.showMessageDialog(this, "No character loaded.", "Add Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (addInventoryFrame == null) {
            addInventoryFrame = new FrameInventoryAdd(this, dataQuery);
            addInventoryFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        addInventoryFrame.updateCharacter(character);
        addInventoryFrame.setVisible(true);
    }

    public void removeInventoryCharacter() {
        if (character == null) {
            JOptionPane.showMessageDialog(this, "No character loaded.", "Remove Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (removeInventoryFrame == null) {
            removeInventoryFrame = new FrameInventoryRemove(this, dataQuery);
            removeInventoryFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        removeInventoryFrame.updateCharacter(character);
        removeInventoryFrame.setVisible(true);
    }

    /**
     * Refreshes/creates the CharStore entry for the current character and writes the charStore.json file.
     */
    private void updateCharStoreEntry() {
        if (charStore == null || character == null || character.getIdentity() == null) return;

        CharIdentity id = character.getIdentity();
        int idx = id.getIndex();

        CharStore updated = new CharStore(
            idx,
            id.getName(),
            id.getCampaign(),
            id.getRace(),
            id.getCharClass(),
            id.getLevel(),
            new Timestamp(System.currentTimeMillis())
        );

        boolean replaced = false;
        for (int i = 0; i < charStore.size(); i++) {
            if (charStore.get(i).getIndex() == idx) {
                charStore.set(i, updated);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            charStore.add(updated);
        }

        CharacterDataManager.saveCharStore(charStore);
    }

    private void runAutoSave() {
        if (character == null) return;
        CharData snapshot = character;
        Thread saver = new Thread(() -> CharacterDataManager.saveCharacterAuto(snapshot), "character-auto-save");
        saver.setDaemon(true);
        saver.start();
    }
}
