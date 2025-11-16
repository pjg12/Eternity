package eternity;

import javax.swing.*;
import java.awt.*;

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
    private JButton exitBtn;

    public FrameFirst(FrameSheet sheetFrame) {
        this.sheetFrame = sheetFrame;

        setTitle("Eternity TTRPG Helper");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

        subHeaderL = new JLabel("No Characters Found. Create a New Character?");
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
        exitBtn = new JButton("Exit");

        Dimension btnSize = new Dimension(150, 30);
        newBtn.setPreferredSize(btnSize);
        exitBtn.setPreferredSize(btnSize);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(newBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(exitBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        root.add(buttonPanel);
        root.add(Box.createVerticalGlue());

        // Add root UI to frame
        add(root);

        // Listeners
        newBtn.addActionListener(e -> onNewPressed());
        exitBtn.addActionListener(e -> onExitPressed());
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

    private void onExitPressed() {
        SwingUtilities.invokeLater(() -> {
            sheetFrame.dispose();
            dispose();
        });
    }
}