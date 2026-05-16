// CHECKED

package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
 * Lists saved characters (newest first) for loading.
 * Includes escape for new character.
 */
public class FrameLoad extends JFrame {
    
    // References
    private final FrameSheet sheetFrame;
    private ArrayList<StoreMetaChar> charStore;

    // UI Constants
    private static final int FRAME_WIDTH = 480;
    private static final int FRAME_HEIGHT = 360;
    private static final int LIST_WIDTH = 440;
    private static final int LIST_HEIGHT = 200;
    private static final int BORDER_PADDING = 12;
    private static final int LAYOUT_GAP = 10;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);

    // UI Strings
    private static final String WINDOW_TITLE = "Load Character";
    private static final String HEADER_TEXT = "Select a Character to Load";
    private static final String BUTTON_NEW = "New";
    private static final String BUTTON_LOAD = "Load";
    private static final String NO_SELECTION_MSG = "Please select a character to load.";
    private static final String NO_SELECTION_TITLE = "No Selection";
    private static final String LOAD_FAILED_MSG = "Failed to load character.";
    private static final String LOAD_FAILED_TITLE = "Error";

    // Date format
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    // UI Components
    private JPanel root, buttons;
    private JLabel headerL;
    private JScrollPane scrollPane;
    private JButton newBtn, loadBtn;

    // Load list elements
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
   
    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public FrameLoad(FrameSheet sheetFrame, ArrayList<StoreMetaChar> charStore) {
        this.sheetFrame = sheetFrame;
        this.charStore = charStore;
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        root = new JPanel(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
        root.setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
        
        // Header
        headerL = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        root.add(headerL, BorderLayout.NORTH);

        // Generate List
        refreshListModel();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!listModel.isEmpty()) list.setSelectedIndex(0);

        // Scroll Pane
        scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        root.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        newBtn = new JButton(BUTTON_NEW);
        loadBtn = new JButton(BUTTON_LOAD);
        newBtn.addActionListener(e -> onNewPressed());
        loadBtn.addActionListener(e -> onLoadPressed());

        buttons = new JPanel();
        buttons.add(newBtn);
        buttons.add(loadBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------
    
    private void onNewPressed() {
        sheetFrame.onNewPressed();
        dispose();
    }

    private void onLoadPressed() {
        int idx = list.getSelectedIndex();
        if (idx < 0 || idx >= charStore.size()) {
            JOptionPane.showMessageDialog(this, NO_SELECTION_MSG, NO_SELECTION_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        int index = charStore.get(idx).getIndex();
        StoreCharData character = StoreCharManager.loadCharacter(index);
        
        if (character != null) {
            sheetFrame.loadCharacter(character);
            dispose();
        } 
        else JOptionPane.showMessageDialog(this, LOAD_FAILED_MSG, LOAD_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private void refreshListModel() {
        listModel.clear();
        charStore = StoreMetaManager.getCharStore();

        for (StoreMetaChar store : StoreMetaManager.getCharStore()) {
            String updated = store.getUpdated() != null ? dateFormat.format(store.getUpdated()) : "unknown";
            String entry = String.format("%s  -  %s  -  L%s  -  %s",
                    store.getName(), store.getCampaign(), store.getLevel(), updated);
            listModel.addElement(entry);
        }
        if (!listModel.isEmpty() && list.getSelectedIndex() < 0) {
            list.setSelectedIndex(0);
        }
    }
}
