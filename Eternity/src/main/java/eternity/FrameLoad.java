package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;

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

    private final FrameSheet sheetFrame;
    private final ArrayList<CharStore> charStore;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);

    public FrameLoad(FrameSheet sheetFrame, ArrayList<CharStore> charStore) {
        this.sheetFrame = sheetFrame;
        this.charStore = charStore;
        setTitle("Load Character");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel("Select a Character to Load", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(18f));
        root.add(header, BorderLayout.NORTH);

        refreshListModel();

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!listModel.isEmpty()) list.setSelectedIndex(0);
        JScrollPane scroller = new JScrollPane(list);
        scroller.setPreferredSize(new Dimension(440, 200));
        root.add(scroller, BorderLayout.CENTER);

        JButton newBtn = new JButton("New");
        JButton addBtn = new JButton("Add Character");
        JButton loadBtn = new JButton("Load");

        newBtn.addActionListener(e -> {
            sheetFrame.onNewPressed();
            dispose();
        });

        addBtn.addActionListener(e -> addCharacterByIndex());

        loadBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0 || idx >= charStore.size()) {
                JOptionPane.showMessageDialog(this, "Please select a character to load.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CharStore selected = charStore.get(idx);
            CharData character = CharacterDataManager.loadCharacter(selected.getIndex());
            if (character != null) {
                sheetFrame.loadCharacter(character);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load character.", "Error", JOptionPane.ERROR_MESSAGE);
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
        charStore.sort(Comparator.comparing(CharStore::getUpdated, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        for (CharStore store : charStore) {
            String updated = store.getUpdated() != null ? fmt.format(store.getUpdated()) : "unknown";
            String entry = String.format("%s  -  %s  -  L%s  -  %s",
                    store.getName(), store.getCampaign(), store.getLevel(), updated);
            listModel.addElement(entry);
        }
        if (!listModel.isEmpty() && list.getSelectedIndex() < 0) {
            list.setSelectedIndex(0);
        }
    }

    private void addCharacterByIndex() {
        String raw = JOptionPane.showInputDialog(this, "Enter character index:", "Add Character", JOptionPane.QUESTION_MESSAGE);
        if (raw == null) return;

        int idx;
        try {
            idx = Integer.parseInt(raw.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid index. Enter a positive integer.", "Add Character", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Index must be greater than 0.", "Add Character", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File mainFile = new File("Characters", idx + ".json");
        if (!mainFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Character file not found: " + mainFile.getPath(),
                    "Add Character",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int backupsCreated = 0;
        try {
            File backupDir = new File("Characters", "Backup");
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                JOptionPane.showMessageDialog(this,
                        "Failed to create backup directory: " + backupDir.getPath(),
                        "Add Character",
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to create backups: " + e.getMessage(),
                    "Add Character",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        CharData character = CharacterDataManager.loadCharacter(idx);
        if (character == null || character.getIdentity() == null) {
            JOptionPane.showMessageDialog(this,
                    "Character file exists but could not be loaded.",
                    "Add Character",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        CharIdentity id = character.getIdentity();
        CharStore updated = new CharStore(
                idx,
                id.getName(),
                id.getCampaign(),
                id.getRace(),
                id.getCharClass(),
                id.getLevel(),
                id.getUpdated() != null ? id.getUpdated() : new java.sql.Timestamp(mainFile.lastModified())
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
        refreshListModel();
        for (int i = 0; i < charStore.size(); i++) {
            if (charStore.get(i).getIndex() == idx) {
                list.setSelectedIndex(i);
                break;
            }
        }

        String action = replaced ? "updated" : "added";
        JOptionPane.showMessageDialog(this,
                "Character " + idx + " " + action + ". Missing backups created: " + backupsCreated + ".",
                "Add Character",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
