package eternity;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class NarratorFrame extends JFrame {

    private static final int FRAME_WIDTH = 320;
    private static final int FRAME_HEIGHT = 190;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);

    private final JButton loadCharacterButton;
    private final JButton generateNpcButton;

    public NarratorFrame() {
        super("Narrator Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Narrator Mode", SwingConstants.CENTER);
        header.setFont(HEADER_FONT);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subHeader = new JLabel("Open and manage character sheets.", SwingConstants.CENTER);
        subHeader.setFont(LABEL_FONT);
        subHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        loadCharacterButton = new JButton("Load Character");
        loadCharacterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadCharacterButton.addActionListener(e -> onLoadCharacterPressed());

        generateNpcButton = new JButton("Generate NPC");
        generateNpcButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        generateNpcButton.addActionListener(e -> onGenerateNpcPressed());

        root.add(header);
        root.add(Box.createVerticalStrut(10));
        root.add(subHeader);
        root.add(Box.createVerticalStrut(20));
        root.add(loadCharacterButton);
        root.add(Box.createVerticalStrut(12));
        root.add(generateNpcButton);
        root.add(Box.createVerticalGlue());

        setContentPane(root);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void onLoadCharacterPressed() {
        StoreMetaManager.loadCharStore();
        FrameSheet narratorSheet = FrameSheet.createNarratorSheet();
        narratorSheet.setVisible(false);

        FrameLoad loadFrame = new FrameLoad(narratorSheet, StoreMetaManager.getCharStore());
        loadFrame.setAllowNewCharacter(false);
        loadFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!narratorSheet.hasActiveCharacter()) {
                    narratorSheet.dispose();
                }
            }
        });
        loadFrame.setVisible(true);
    }

    private void onGenerateNpcPressed() {
        FrameSheet narratorSheet = FrameSheet.createNarratorSheet();
        narratorSheet.onNewPressed();
    }
}
