package eternity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import websocket.Roll20WebSocketServer;
import websocket.events.Roll20Token;

public class NarratorFrame extends JFrame {

    private static final int FRAME_WIDTH = 320;
    private static final int CONNECTED_FRAME_WIDTH = 560;
    private static final int FRAME_HEIGHT = 320;
    private static final int TOKEN_PANEL_WIDTH = 220;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);

    private static final Set<NarratorFrame> OPEN_FRAMES = Collections.synchronizedSet(new HashSet<>());

    private final JButton loadCharacterButton;
    private final JButton generateNpcButton;
    private final JButton initiateCombatButton;
    private final JButton newRoundButton;
    private final JPanel tokenPanel;
    private final JList<Roll20Token> tokenList;
    private Roll20WebSocketServer roll20Server;
    private Roll20WebSocketServer.ConnectionListener connectionListener;
    private boolean combatActive;

    public NarratorFrame() {
        super("Narrator Mode");
        OPEN_FRAMES.add(this);
        registerNarratorClient();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setAutoRequestFocus(false);

        JPanel buttonColumn = new JPanel();
        buttonColumn.setLayout(new BoxLayout(buttonColumn, BoxLayout.Y_AXIS));
        buttonColumn.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        initiateCombatButton = new JButton("Initiate Combat");
        initiateCombatButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        initiateCombatButton.setOpaque(true);
        initiateCombatButton.setContentAreaFilled(true);
        initiateCombatButton.addActionListener(e -> onCombatTogglePressed());

        newRoundButton = new JButton("New Round");
        newRoundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newRoundButton.setVisible(false);
        newRoundButton.addActionListener(e -> onNewRoundPressed());

        buttonColumn.add(header);
        buttonColumn.add(Box.createVerticalStrut(10));
        buttonColumn.add(subHeader);
        buttonColumn.add(Box.createVerticalStrut(20));
        buttonColumn.add(loadCharacterButton);
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(generateNpcButton);
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(initiateCombatButton);
        buttonColumn.add(Box.createVerticalStrut(12));
        buttonColumn.add(newRoundButton);
        buttonColumn.add(Box.createVerticalGlue());

        tokenList = new JList<>();
        tokenList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tokenList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (component instanceof JLabel label && value instanceof Roll20Token token) {
                    label.setText(formatTokenLabel(token));
                }
                return component;
            }
        });
        tokenList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Roll20Token selected = tokenList.getSelectedValue();
            if (selected == null || selected.id == null || selected.id.isBlank() || roll20Server == null) {
                return;
            }
            roll20Server.sendSelectToken(selected.id);
        });

        JLabel tokenHeader = new JLabel("Page Tokens");
        tokenHeader.setFont(LABEL_FONT.deriveFont(Font.BOLD));

        JScrollPane tokenPane = new JScrollPane(tokenList);
        tokenPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tokenPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        tokenPanel = new JPanel(new BorderLayout(0, 8));
        tokenPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
        tokenPanel.setPreferredSize(new Dimension(TOKEN_PANEL_WIDTH, FRAME_HEIGHT));
        tokenPanel.add(tokenHeader, BorderLayout.NORTH);
        tokenPanel.add(tokenPane, BorderLayout.CENTER);
        tokenPanel.setVisible(false);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buttonColumn, BorderLayout.CENTER);
        root.add(tokenPanel, BorderLayout.EAST);

        setContentPane(root);
        attachRoll20TokenList();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setAutoRequestFocus(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public static boolean hasOpenFrame() {
        return !OPEN_FRAMES.isEmpty();
    }

    public static void promptPlayerAttacking() {
        JOptionPane pane = new JOptionPane(
                "A player is attacking.",
                JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(null, "Narrator");
        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
    }

    private static void registerNarratorClient() {
        try {
            Roll20WebSocketServer.getSharedServer().setClientRole("narrator");
        } catch (Exception ex) {
            System.out.println("Could not register narrator with Roll20: " + ex.getMessage());
        }
    }

    private void attachRoll20TokenList() {
        try {
            roll20Server = Roll20WebSocketServer.getSharedServer();
            tokenList.setModel(roll20Server.getTargetModel());
            connectionListener = new Roll20WebSocketServer.ConnectionListener() {
                @Override
                public void onRoll20Connected() {
                    refreshTokenPanelVisibility();
                }

                @Override
                public void onRoll20Disconnected() {
                    refreshTokenPanelVisibility();
                }
            };
            roll20Server.addConnectionListener(connectionListener);
            refreshTokenPanelVisibility();
        } catch (Exception ex) {
            System.out.println("Could not attach narrator token list: " + ex.getMessage());
        }
    }

    private void refreshTokenPanelVisibility() {
        Runnable update = () -> {
            boolean connected = roll20Server != null && roll20Server.isServiceConnected();
            tokenPanel.setVisible(connected);
            if (connected) {
                roll20Server.requestTokenSnapshot();
            } else {
                tokenList.clearSelection();
            }
            setSize(connected ? CONNECTED_FRAME_WIDTH : FRAME_WIDTH, FRAME_HEIGHT);
            revalidate();
            repaint();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private static String formatTokenLabel(Roll20Token token) {
        if (token == null) {
            return "";
        }
        if (token.name != null && !token.name.isBlank()) {
            return token.name;
        }
        if (token.characterName != null && !token.characterName.isBlank()) {
            return token.characterName;
        }
        return token.id == null || token.id.isBlank() ? "Unnamed" : token.id;
    }

    @Override
    public void dispose() {
        if (roll20Server != null && connectionListener != null) {
            roll20Server.removeConnectionListener(connectionListener);
            connectionListener = null;
        }
        OPEN_FRAMES.remove(this);
        super.dispose();
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

    private void onCombatTogglePressed() {
        if (combatActive) {
            sendCombatCommand(false);
        } else {
            sendCombatCommand(true);
        }
    }

    private void onNewRoundPressed() {
        try {
            boolean sent = Roll20WebSocketServer.getSharedServer().announceNewRound();
            if (!sent) {
                showRoll20Disconnected("New Round");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not announce a new round: " + ex.getMessage(),
                    "New Round",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void sendCombatCommand(boolean starting) {
        try {
            Roll20WebSocketServer server = Roll20WebSocketServer.getSharedServer();
            boolean sent = starting ? server.initiateCombat() : server.endCombat();
            if (!sent) {
                showRoll20Disconnected(starting ? "Initiate Combat" : "End Combat");
                return;
            }
            setCombatActive(starting);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not update combat in Roll20: " + ex.getMessage(),
                    starting ? "Initiate Combat" : "End Combat",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setCombatActive(boolean active) {
        combatActive = active;
        if (active) {
            initiateCombatButton.setText("End Combat");
            initiateCombatButton.setBackground(Color.RED);
            initiateCombatButton.setForeground(Color.WHITE);
            newRoundButton.setVisible(true);
        } else {
            initiateCombatButton.setText("Initiate Combat");
            initiateCombatButton.setBackground(null);
            initiateCombatButton.setForeground(null);
            newRoundButton.setVisible(false);
        }
        revalidate();
        repaint();
    }

    private void showRoll20Disconnected(String title) {
        JOptionPane.showMessageDialog(this,
                "Roll20 is not connected. Open the campaign with the Eternity extension loaded.",
                title,
                JOptionPane.WARNING_MESSAGE);
    }
}
