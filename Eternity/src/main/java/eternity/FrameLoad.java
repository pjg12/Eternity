package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 * Simple character loader frame.
 *
 * Lists saved characters (newest first) and lets the user either load the
 * selected character or start a new one.
 */
public class FrameLoad extends JFrame {
    private static final long serialVersionUID = 1L;

    // Frame dimensions
    private static final int FRAME_WIDTH = 480;
    private static final int FRAME_HEIGHT = 360;
    private static final int LIST_WIDTH = 440;
    private static final int LIST_HEIGHT = 200;
    private static final int BORDER_PADDING = 12;
    private static final int LAYOUT_GAP = 10;

    // UI Strings
    private static final String TITLE = "Load Character";
    private static final String HEADER_TEXT = "Select a Character to Load";
    private static final String NEW_BUTTON = "New";
    private static final String ADD_BUTTON = "Add Character";
    private static final String LOAD_BUTTON = "Load";
    private static final String NO_SELECTION_MSG = "Please select a character to load.";
    private static final String NO_SELECTION_TITLE = "No Selection";
    private static final String LOAD_FAILED_MSG = "Failed to load character.";
    private static final String LOAD_FAILED_TITLE = "Error";
    private static final String ADD_PROMPT = "Enter character index:";
    private static final String ADD_TITLE = "Add Character";
    private static final String INVALID_INDEX_MSG = "Invalid index. Enter a positive integer.";
    private static final String INDEX_TOO_SMALL_MSG = "Index must be greater than 0.";
    private static final String FILE_NOT_FOUND_MSG = "Character file not found: ";
    private static final String BACKUP_DIR_FAILED_MSG = "Failed to create backup directory: ";
    private static final String BACKUP_FAILED_MSG = "Failed to create backups: ";
    private static final String LOAD_FAILED_AFTER_EXISTS_MSG = "Character file exists but could not be loaded.";
    private static final String ADD_SUCCESS_MSG = "Character %d %s. Missing backups created: %d.";
    private static final String ADD_SUCCESS_TITLE = "Add Character";

    // Date format
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final Comparator<StoreChar> UPDATED_DESC =
            Comparator.comparing(StoreChar::getUpdated, Comparator.nullsLast(Comparator.naturalOrder())).reversed();

    // Paths
    private static final String CHARACTER_DIR = "Characters";
    private static final String BACKUP_DIR = "Characters/Backup";

    // Lightweight container for extracting only identity metadata from JSON
    private static class CharMetadataOnly {
        public CharIdentity identity;
    }

    private final FrameSheet sheetFrame;
    private final ArrayList<StoreChar> charStore;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final ArrayList<StoreChar> displayStore = new ArrayList<>();
    private final Map<Integer, StoreChar> charStoreByIndex = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public FrameLoad(FrameSheet sheetFrame, ArrayList<StoreChar> charStore) {
        this.sheetFrame = sheetFrame;
        this.charStore = charStore;
        rebuildCharStoreIndex();
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
        root.setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));

        JLabel header = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(18f));
        root.add(header, BorderLayout.NORTH);

        refreshListModel();

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!listModel.isEmpty()) list.setSelectedIndex(0);
        JScrollPane scroller = new JScrollPane(list);
        scroller.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        root.add(scroller, BorderLayout.CENTER);

        JButton newBtn = new JButton(NEW_BUTTON);
        JButton addBtn = new JButton(ADD_BUTTON);
        JButton loadBtn = new JButton(LOAD_BUTTON);

        newBtn.addActionListener(e -> {
            sheetFrame.onNewPressed();
            dispose();
        });

        addBtn.addActionListener(e -> addCharacterByIndex());

        loadBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0 || idx >= displayStore.size()) {
                JOptionPane.showMessageDialog(this, NO_SELECTION_MSG, NO_SELECTION_TITLE, JOptionPane.WARNING_MESSAGE);
                return;
            }
            StoreChar selected = displayStore.get(idx);
            CharData character = CharDataManager.loadCharacter(selected.getIndex());
            if (character != null) {
                sheetFrame.loadCharacter(character);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, LOAD_FAILED_MSG, LOAD_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(newBtn);
        buttons.add(addBtn);
        buttons.add(loadBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void refreshListModel() {
        listModel.clear();
        displayStore.clear();
        displayStore.addAll(charStore);
        displayStore.sort(UPDATED_DESC);

        for (StoreChar store : displayStore) {
            String updated = store.getUpdated() != null ? dateFormat.format(store.getUpdated()) : "unknown";
            String entry = String.format("%s  -  %s  -  L%s  -  %s",
                    store.getName(), store.getCampaign(), store.getLevel(), updated);
            listModel.addElement(entry);
        }
        if (!listModel.isEmpty() && list.getSelectedIndex() < 0) {
            list.setSelectedIndex(0);
        }
    }

    private void rebuildCharStoreIndex() {
        charStoreByIndex.clear();
        for (StoreChar store : charStore) {
            charStoreByIndex.put(store.getIndex(), store);
        }
    }

    private int findDisplayIndex(int charIndex) {
        for (int i = 0; i < displayStore.size(); i++) {
            if (displayStore.get(i).getIndex() == charIndex) return i;
        }
        return -1;
    }

    /** Loads only the identity metadata from a character file to avoid expensive full deserialization. */
    private CharIdentity loadCharacterIdentity(int idx) {
        try {
            File f = new File(CHARACTER_DIR, idx + ".json");
            if (!f.exists()) return null;
            CharMetadataOnly metadata = CharDataManager.mapper.readValue(f, CharMetadataOnly.class);
            return metadata != null ? metadata.identity : null;
        } catch (IOException e) {
            return null;
        }
    }

    private void addCharacterByIndex() {
        String raw = JOptionPane.showInputDialog(this, ADD_PROMPT, ADD_TITLE, JOptionPane.QUESTION_MESSAGE);
        if (raw == null) return;

        int idx;
        try {
            idx = Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, INVALID_INDEX_MSG, ADD_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, INDEX_TOO_SMALL_MSG, ADD_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }

        File mainFile = new File(CHARACTER_DIR, idx + ".json");
        if (!mainFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    FILE_NOT_FOUND_MSG + mainFile.getPath(),
                    ADD_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int backupsCreated = 0;
        try {
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                JOptionPane.showMessageDialog(this,
                        BACKUP_DIR_FAILED_MSG + backupDir.getPath(),
                        ADD_TITLE,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            File backup1 = new File(backupDir, idx + "Backup1.json");
            File backup2 = new File(backupDir, idx + "Backup2.json");
            if (!backup1.exists()) {
                Files.copy(mainFile.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);
                backupsCreated++;
            }
            if (!backup2.exists()) {
                Files.copy(mainFile.toPath(), backup2.toPath(), StandardCopyOption.REPLACE_EXISTING);
                backupsCreated++;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    BACKUP_FAILED_MSG + e.getMessage(),
                    ADD_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Load only identity metadata instead of full CharData
        CharIdentity id = loadCharacterIdentity(idx);
        if (id == null) {
            JOptionPane.showMessageDialog(this,
                    LOAD_FAILED_AFTER_EXISTS_MSG,
                    ADD_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        StoreChar updated = new StoreChar(
                idx,
                id.getName(),
                id.getCampaign(),
                id.getRace(),
                id.getCharClass(),
                id.getLevel(),
                id.getUpdated() != null ? id.getUpdated() : new java.sql.Timestamp(mainFile.lastModified())
        );

        StoreChar existing = charStoreByIndex.get(idx);
        boolean replaced = existing != null;
        if (replaced) {
            int existingIndex = charStore.indexOf(existing);
            if (existingIndex >= 0) {
                charStore.set(existingIndex, updated);
            }
        } else {
            charStore.add(updated);
        }
        charStoreByIndex.put(idx, updated);

        CharDataManager.saveCharStore(charStore);
        refreshListModel();
        int newIndex = findDisplayIndex(idx);
        if (newIndex >= 0) {
            list.setSelectedIndex(newIndex);
        }

        String action = replaced ? "updated" : "added";
        JOptionPane.showMessageDialog(this,
                String.format(ADD_SUCCESS_MSG, idx, action, backupsCreated),
                ADD_SUCCESS_TITLE,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
