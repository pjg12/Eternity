// CHECKED

package eternity;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Welcome screen for the Eternity TTRPG Helper.
 */
public class FrameFirst extends JFrame {

    // References
    private final FrameSheet sheetFrame;
    
    // UI Constants
    private static final int FRAME_WIDTH = 550;
    private static final int FRAME_HEIGHT = 220;
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
    
    // UI Strings
    private static final String WINDOW_TITLE = "Eternity TTRPG Helper";
    private static final String HEADER_TEXT = "Welcome to the Eternity TTRPG Helper";
    private static final String SUBHEADER_TEXT = "Create a New Character or Load an Existing Character?";
    private static final String STATUS_LOADING = " ";
    private static final String BUTTON_NEW = "New";
    private static final String BUTTON_LOAD = "Load";

    // UI Components
    private JPanel root;
    private JLabel headerL;
    private JLabel subHeaderL;
    private JLabel statusL;
    private JButton newBtn;
    private JButton loadBtn;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public FrameFirst(FrameSheet sheetFrame) {
        super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        buildUI();
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT, PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT));

        // Header
        headerL = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subheader
        subHeaderL = new JLabel(SUBHEADER_TEXT);
        subHeaderL.setFont(SUBHEADER_FONT);
        subHeaderL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Spacer
        root.add(headerL);
        root.add(Box.createVerticalStrut(SPACING_HEADER));
        root.add(subHeaderL);
        root.add(Box.createVerticalStrut(SPACING_STATUS));

        // Status
        statusL = new JLabel(STATUS_LOADING, SwingConstants.CENTER);
        statusL.setFont(LABEL_FONT);
        statusL.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(statusL);
        root.add(Box.createVerticalStrut(SPACING_BEFORE_BUTTONS));

        // Button Setup
        Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);

        // New Button
        newBtn = new JButton(BUTTON_NEW);
        newBtn.setPreferredSize(btnSize);

        // Load Button
        loadBtn = new JButton(BUTTON_LOAD);
        loadBtn.setPreferredSize(btnSize);

        // Add Buttons
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(newBtn);
        buttonPanel.add(Box.createHorizontalStrut(BUTTON_SPACING));
        buttonPanel.add(loadBtn);
        buttonPanel.add(Box.createHorizontalGlue());
        root.add(buttonPanel);
        root.add(Box.createVerticalGlue());

        // Add root to frame
        add(root);

        // Listeners
        newBtn.addActionListener(e -> onNewPressed());
        loadBtn.addActionListener(e -> onLoadPressed());
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------
    
    private void onNewPressed() {
        sheetFrame.onNewPressed();
        dispose();
    }

    private void onLoadPressed() {
        sheetFrame.onLoadPressed();
        dispose();
    }
}
