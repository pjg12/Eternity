package eternity;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Welcome screen for the Eternity TTRPG Helper.
 * Standalone version with modern UI layout and no FrameHelper dependency.
 */
public class FrameFirst extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int FRAME_WIDTH = 550;
    private static final int FRAME_HEIGHT = 220;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font SUBHEADER_FONT = new Font(null, Font.PLAIN, 15);
    private static final Font STATUS_FONT = new Font(null, Font.PLAIN, 13);
    
    // UI Dimensions
    private static final int PADDING_TOP_BOTTOM = 25;
    private static final int PADDING_LEFT_RIGHT = 20;
    private static final int SPACING_HEADER = 10;
    private static final int SPACING_STATUS = 10;
    private static final int SPACING_BEFORE_BUTTONS = 15;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_SPACING = 20;
    
    // UI Strings
    private static final String WINDOW_TITLE = "Eternity TTRPG Helper";
    private static final String HEADER_TEXT = "Welcome to the Eternity TTRPG Helper";
    private static final String SUBHEADER_TEXT = "Create a New Character or Load an Existing Character?";
    private static final String STATUS_LOADING = "Loading app data...";
    private static final String STATUS_READY = "Ready. Select New or Load.";
    private static final String STATUS_ERROR = "Unable to initialize application.";
    private static final String BUTTON_NEW = "New";
    private static final String BUTTON_LOAD = "Load";
    private static final String MSG_LOADING_BLOCKER = "Please wait until the app finishes loading.";
    private static final String DIALOG_LOADING = "Loading";
    private static final String ERROR_TITLE = "Startup Error";

    private FrameSheet sheetFrame;
    private ArrayList<StoreChar> charStore;

    private JLabel headerL;
    private JLabel subHeaderL;
    private JLabel statusL;
    private JButton newBtn;
    private JButton loadBtn;

    public FrameFirst() {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
    }

    public void attachStartupStore(ArrayList<StoreChar> store) {
        this.charStore = store;
        setLoadingState(false);
    }

    private void ensureSheetFrame() {
        if (sheetFrame != null) return;
        sheetFrame = new FrameSheet(new DataQuery(), charStore);
    }

    public void showLoadError(String message) {
        setErrorState();
        JOptionPane.showMessageDialog(this, message, "Startup Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setErrorState() {
        statusL.setText(STATUS_ERROR);
        newBtn.setEnabled(false);
        loadBtn.setEnabled(false);
    }

    private void setLoadingState(boolean loading) {
        statusL.setText(loading ? STATUS_LOADING : STATUS_READY);
        newBtn.setEnabled(!loading);
        loadBtn.setEnabled(!loading);
    }

    /**
     * Builds a modern, centered UI layout (BoxLayout-based).
     */
    private void buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT, PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT));

        // ------------------------
        // Header
        // ------------------------
        headerL = new JLabel(HEADER_TEXT);
        headerL.setFont(HEADER_FONT);
        headerL.setAlignmentX(Component.CENTER_ALIGNMENT);

        subHeaderL = new JLabel(SUBHEADER_TEXT);
        subHeaderL.setFont(SUBHEADER_FONT);
        subHeaderL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // spacing
        root.add(headerL);
        root.add(Box.createVerticalStrut(SPACING_HEADER));
        root.add(subHeaderL);
        root.add(Box.createVerticalStrut(SPACING_STATUS));

        statusL = new JLabel(STATUS_LOADING, SwingConstants.CENTER);
        statusL.setFont(STATUS_FONT);
        statusL.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(statusL);
        root.add(Box.createVerticalStrut(SPACING_BEFORE_BUTTONS));

        // ------------------------
        // Buttons
        // ------------------------
        newBtn = new JButton(BUTTON_NEW);
        loadBtn = new JButton(BUTTON_LOAD);

        Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        newBtn.setPreferredSize(btnSize);
        loadBtn.setPreferredSize(btnSize);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(newBtn);
        buttonPanel.add(Box.createHorizontalStrut(BUTTON_SPACING));
        buttonPanel.add(loadBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        root.add(buttonPanel);
        root.add(Box.createVerticalGlue());

        // Add root UI to frame
        add(root);

        // Listeners and initial state
        newBtn.addActionListener(e -> onNewPressed());
        loadBtn.addActionListener(e -> onLoadPressed());
        setLoadingState(true);
    }

    // -------------------------------
    // Button Handlers
    // -------------------------------
    
    private void showLoadingBlocker() {
        JOptionPane.showMessageDialog(this, MSG_LOADING_BLOCKER, DIALOG_LOADING, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void onNewPressed() {
        if (charStore == null) {
            showLoadingBlocker();
            return;
        }
        ensureSheetFrame();
        sheetFrame.onNewPressed();
        dispose();
    }

    private void onLoadPressed() {
        if (charStore == null) {
            showLoadingBlocker();
            return;
        }
        ensureSheetFrame();
        sheetFrame.onLoadPressed();
        dispose();
    }
}
