package websocket;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import websocket.events.BaseEvent;
import websocket.events.ChatEvent;
import websocket.events.ConnectedEvent;
import websocket.events.DebugEvent;
import websocket.events.IdEvent;
import websocket.events.InspectEvent;
import websocket.events.PageEvent;
import websocket.events.Roll20Token;
import websocket.events.RollEvent;
import websocket.events.TokenSnapshotEvent;
import websocket.events.WindowScanEvent;

public class Roll20WebSocketServer extends WebSocketServer {
    public interface ConnectionListener {
        void onRoll20Connected();
    }

    private static Roll20WebSocketServer sharedServer;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Roll20Token> tokensById = new HashMap<>();
    private final Set<ConnectionListener> connectionListeners = new HashSet<>();
    private Roll20Token selectedTarget;
    private boolean serviceConnected;

    private final DefaultListModel<Roll20Token> targetModel =
        new DefaultListModel<>();

    private final JList<Roll20Token> targetList =
        new JList<>(targetModel);

    public static synchronized Roll20WebSocketServer getSharedServer() {
        if (sharedServer == null) {
            sharedServer = new Roll20WebSocketServer(8080);
            sharedServer.start();
            System.out.println("Listening on ws://localhost:8080");
        }
        return sharedServer;
    }

    public Roll20WebSocketServer(int port) {
        super(new InetSocketAddress("localhost", port));
        initializeTargetList();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        serviceConnected = true;
        notifyConnectedListeners();
        System.out.println("Client connected.");
    }

    @Override
    public void onClose(WebSocket conn, int code,
                        String reason, boolean remote) {
        serviceConnected = false;
        System.out.println("Client disconnected.");
    }

    @Override
    public void onMessage(
        WebSocket conn,
        String message) {

        try {
            BaseEvent base =
                mapper.readValue(message, BaseEvent.class);

            switch (base.type) {
                case "connected" -> {
                    ConnectedEvent connected =
                        mapper.readValue(message, ConnectedEvent.class);

                    System.out.println(
                        "Connected from " +
                        connected.source);
                }

                case "debug" -> {
                    DebugEvent debug =
                        mapper.readValue(message, DebugEvent.class);

                    System.out.println(
                        "body " +
                        debug.bodyLength);
                }

                case "roll" -> {
                    RollEvent roll =
                        mapper.readValue(message, RollEvent.class);

                    System.out.println(
                        roll.player +
                        " rolled " +
                        roll.result);
                }

                case "chat" -> {
                    ChatEvent chat =
                        mapper.readValue(message, ChatEvent.class);

                    System.out.println(chat.text);
                }

                case "inspect" -> {
                    InspectEvent inspect =
                        mapper.readValue(message, InspectEvent.class);

                    System.out.println(
                        inspect.selector +
                        " has " +
                        inspect.count);
                }

                case "ids" -> {
                    IdEvent ids =
                        mapper.readValue(message, IdEvent.class);

                    System.out.println(
                        ids.type +
                        " : " +
                        ids.id);
                }

                case "window-scan" -> {
                    WindowScanEvent windowScan =
                        mapper.readValue(message, WindowScanEvent.class);

                    System.out.println(
                        windowScan.type +
                        " keys: " +
                        windowScan.count);
                }

                case "page" -> {
                    PageEvent page =
                        mapper.readValue(message, PageEvent.class);

                    System.out.println(
                        page.url +
                        " : " +
                        page.title);
                }

                case "token-snapshot" -> {
                    TokenSnapshotEvent event =
                        mapper.readValue(message, TokenSnapshotEvent.class);

                    tokensById.clear();

                    if (event.tokens != null) {
                        for (Roll20Token token : event.tokens) {
                            if (token != null && token.id != null) {
                                tokensById.put(token.id, token);
                            }
                        }
                    }

                    updateTargetList();
                }

                case "token-change" -> {
                    Roll20Token token =
                        mapper.readValue(message, Roll20Token.class);

                    if (token.id != null) {
                        tokensById.put(token.id, token);
                    }

                    updateTargetList();
                }

                default -> {
                    // Ignore unknown websocket events.
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started.");
    }

    public JList<Roll20Token> getTargetList() {
        return targetList;
    }

    public Roll20Token getSelectedTarget() {
        return selectedTarget;
    }

    public boolean isServiceConnected() {
        return serviceConnected;
    }

    public synchronized void addConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.add(listener);
        }
    }

    public synchronized void removeConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.remove(listener);
        }
    }

    private void initializeTargetList() {
        targetList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedTarget = targetList.getSelectedValue();

                if (selectedTarget != null) {
                    System.out.println("Target selected: " + selectedTarget.name);
                }
            }
        });
    }

    private void updateTargetList() {
        SwingUtilities.invokeLater(() -> {
            targetModel.clear();

            tokensById.values().stream()
                .filter(t -> "objects".equals(t.layer))
                .sorted(Comparator.comparing(t -> t.name == null ? "" : t.name))
                .forEach(targetModel::addElement);
        });
    }

    private void notifyConnectedListeners() {
        Set<ConnectionListener> snapshot;
        synchronized (this) {
            snapshot = new HashSet<>(connectionListeners);
        }
        SwingUtilities.invokeLater(() -> {
            for (ConnectionListener listener : snapshot) {
                listener.onRoll20Connected();
            }
        });
    }
}
