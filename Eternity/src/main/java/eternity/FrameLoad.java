package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.SwingUtilities;

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

        // Sort newest first
        charStore.sort(Comparator.comparing(CharStore::getUpdated, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        for (CharStore store : charStore) {
            String updated = store.getUpdated() != null ? fmt.format(store.getUpdated()) : "unknown";
            String entry = String.format("%s  —  %s  —  L%s  —  %s", store.getName(), store.getCampaign(), store.getLevel(), updated);
            listModel.addElement(entry);
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!listModel.isEmpty()) list.setSelectedIndex(0);
        JScrollPane scroller = new JScrollPane(list);
        scroller.setPreferredSize(new Dimension(440, 200));
        root.add(scroller, BorderLayout.CENTER);

        // Buttons
        JButton newBtn = new JButton("New");
        JButton loadBtn = new JButton("Load");

        newBtn.addActionListener(e -> {
            sheetFrame.onNewPressed();
            dispose();
        });

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
        buttons.add(loadBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
