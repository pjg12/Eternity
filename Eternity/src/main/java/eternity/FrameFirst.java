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
import javax.swing.SwingUtilities;

/**
 * Welcome screen for the Eternity TTRPG Helper.
 * Standalone version with modern UI layout and no FrameHelper dependency.
 */
public class FrameFirst extends JFrame {
    private static final long serialVersionUID = 1L;

    private final FrameSheet sheetFrame;

    private JLabel headerL;
    private JLabel subHeaderL;
    private JButton newBtn;
    private JButton loadBtn;

    public FrameFirst(FrameSheet sheetFrame) {
        this.sheetFrame = sheetFrame;

        setTitle("Eternity TTRPG Helper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 200);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        setVisible(true);
    }

    /**
     * Builds a modern, centered UI layout (BoxLayout-based).
     */
    private void buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        // ------------------------
        // Header
        // ------------------------
        headerL = new JLabel("Welcome to the Eternity TTRPG Helper");
        headerL.setFont(headerL.getFont().deriveFont(Font.BOLD, 20f));
        headerL.setAlignmentX(Component.CENTER_ALIGNMENT);

        subHeaderL = new JLabel("Create a New Character or Load an Existing Character?");
        subHeaderL.setFont(subHeaderL.getFont().deriveFont(15f));
        subHeaderL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // spacing
        root.add(headerL);
        root.add(Box.createVerticalStrut(10));
        root.add(subHeaderL);
        root.add(Box.createVerticalStrut(35));

        // ------------------------
        // Buttons
        // ------------------------
        newBtn = new JButton("New");
        loadBtn = new JButton("Load");

        Dimension btnSize = new Dimension(150, 30);
        newBtn.setPreferredSize(btnSize);
        loadBtn.setPreferredSize(btnSize);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(newBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(loadBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        root.add(buttonPanel);
        root.add(Box.createVerticalGlue());

        // Add root UI to frame
        add(root);

        // Listeners
        newBtn.addActionListener(e -> onNewPressed());
        loadBtn.addActionListener(e -> onLoadPressed());
    }

    // -------------------------------
    // Button Handlers
    // -------------------------------
    private void onNewPressed() {
        SwingUtilities.invokeLater(() -> {
            sheetFrame.onNewPressed();
            dispose();
        });
    }

    private void onLoadPressed() {
        SwingUtilities.invokeLater(() -> {
            sheetFrame.onLoadPressed();
            dispose();
        });
    }
}
