package websocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import websocket.events.IncomingAttackEvent;
import websocket.events.InspectEvent;
import websocket.events.PageEvent;
import websocket.events.PlayerAttackingEvent;
import websocket.events.Roll20Token;
import websocket.events.RollEvent;
import websocket.events.TokenSnapshotEvent;
import websocket.events.WindowScanEvent;

public class Roll20WebSocketServer extends WebSocketServer {
    public interface ConnectionListener {
        void onRoll20Connected();

        default void onRoll20Disconnected() {
        }
    }

    public interface DefenseReplyHandler {
        String buildDefenseReply(int openCharacterKey, IncomingAttackEvent attack);
    }

    private static Roll20WebSocketServer sharedServer;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Roll20Token> tokensById = new HashMap<>();
    private final Set<ConnectionListener> connectionListeners = new HashSet<>();
    private DefenseReplyHandler defenseReplyHandler;
    private String clientRole;
    private CountDownLatch narratorNotifyLatch;
    private final Object narratorNotifyLock = new Object();
    private Roll20Token selectedTarget;
    private boolean serviceConnected;
    private String loadedCharacterName;
    private String loadedCharacterNickname;
    private String loadedCharacterTokenId;
    private double pixelsPerSquare = 70.0;
    private double feetPerSquare = 5.0;
    private final Set<String> announcedIncomingAttacks = new LinkedHashSet<>();
    private String lastAnnouncedAttackTarget;
    private long lastAnnouncedAttackAtMs;
    private final Map<Integer, OpenCharacter> openCharacters = new LinkedHashMap<>();

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
        requestTokenSnapshot();
        sendClientRole();
    }

    @Override
    public void onClose(WebSocket conn, int code,
                        String reason, boolean remote) {
        serviceConnected = false;
        notifyDisconnectedListeners();
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

                case "incoming-attack" -> {
                    IncomingAttackEvent attack =
                        mapper.readValue(message, IncomingAttackEvent.class);
                    handleIncomingAttack(attack);
                }

                case "player-attacking" -> {
                    PlayerAttackingEvent attacking =
                        mapper.readValue(message, PlayerAttackingEvent.class);
                    handlePlayerAttacking(attacking);
                }

                case "narrator-absent", "narrator-notified" -> completeNarratorNotify();

                case "combat-start", "combat-end", "combat-new-round" ->
                    handleNarratorCombatSignal(base.type);

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
                            if (token != null && token.id != null && !token.id.isBlank()) {
                                tokensById.put(token.id, token);
                            }
                        }
                    }

                    if (event.pixelsPerSquare != null && event.pixelsPerSquare > 0) {
                        pixelsPerSquare = event.pixelsPerSquare;
                    }
                    if (event.scaleNumber != null && event.scaleNumber > 0 && isFeetScale(event.scaleUnits)) {
                        feetPerSquare = event.scaleNumber;
                    }
                    System.out.println("Token snapshot received: " + tokensById.size()
                        + " (" + pixelsPerSquare + " px/square)");
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

    public DefaultListModel<Roll20Token> getTargetModel() {
        return targetModel;
    }

    public Roll20Token getSelectedTarget() {
        return selectedTarget;
    }

    public void setLoadedCharacter(String name, String nickname) {
        loadedCharacterName = name;
        loadedCharacterNickname = nickname;
        updateTargetList();
    }

    public void registerOpenCharacter(int key, String name, String nickname) {
        synchronized (openCharacters) {
            openCharacters.put(key, new OpenCharacter(name, nickname));
        }
        System.out.println("Registered open character: " + name);
        updateTargetList();
    }

    public void unregisterOpenCharacter(int key) {
        synchronized (openCharacters) {
            openCharacters.remove(key);
        }
        updateTargetList();
    }

    public boolean isLoadedCharacterToken(Roll20Token token) {
        return token != null
            && token.id != null
            && !token.id.isBlank()
            && token.id.equals(loadedCharacterTokenId);
    }

    public Roll20Token getLoadedCharacterToken() {
        if (loadedCharacterTokenId == null || loadedCharacterTokenId.isBlank()) {
            return null;
        }
        return tokensById.get(loadedCharacterTokenId);
    }

    public boolean isAdjacentToLoadedCharacter(Roll20Token token) {
        return areAdjacent(getLoadedCharacterToken(), token, pixelsPerSquare);
    }

    public boolean isWithinRangeOfLoadedCharacter(Roll20Token token, double minFeet, double maxFeet) {
        double distance = getDistanceFeetFromLoadedCharacter(token);
        if (Double.isNaN(distance)) {
            return false;
        }
        return distance + 0.01 >= minFeet && distance - 0.01 <= maxFeet;
    }

    public double getDistanceFeetFromLoadedCharacter(Roll20Token token) {
        return getDistanceFeet(getLoadedCharacterToken(), token, pixelsPerSquare, feetPerSquare);
    }

    public boolean isChargePathClearToLoadedCharacter(Roll20Token token) {
        return isChargePathClear(getLoadedCharacterToken(), token);
    }

    public boolean isChargePathClear(Roll20Token from, Roll20Token to) {
        if (from == null || to == null || from.id == null || from.id.equals(to.id)) {
            return false;
        }
        if (from.left == null || from.top == null || to.left == null || to.top == null) {
            return false;
        }
        double square = pixelsPerSquare > 0 ? pixelsPerSquare : 70.0;
        Set<Long> start = occupiedSquares(from, square);
        Set<Long> end = occupiedSquares(to, square);
        int[] fromCell = centerCell(from, square);
        int[] toCell = centerCell(to, square);
        Set<Long> between = supercoverSquares(fromCell[0], fromCell[1], toCell[0], toCell[1]);
        between.removeAll(start);
        between.removeAll(end);
        if (between.isEmpty()) {
            return true;
        }
        for (Roll20Token token : tokensById.values()) {
            if (token == null || token.id == null || token.id.equals(from.id) || token.id.equals(to.id)) {
                continue;
            }
            if (!isTargetableToken(token) || token.left == null || token.top == null) {
                continue;
            }
            for (Long cell : occupiedSquares(token, square)) {
                if (between.contains(cell)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double getDistanceFeet(Roll20Token first, Roll20Token second, double pixelsPerSquare, double feetPerSquare) {
        if (first == null || second == null || first.id == null || first.id.equals(second.id)) {
            return Double.NaN;
        }
        if (first.left == null || first.top == null || second.left == null || second.top == null) {
            return Double.NaN;
        }
        double square = pixelsPerSquare > 0 ? pixelsPerSquare : 70.0;
        double firstWidth = tokenSpan(first.width, square);
        double firstHeight = tokenSpan(first.height, square);
        double secondWidth = tokenSpan(second.width, square);
        double secondHeight = tokenSpan(second.height, square);

        double firstLeft = first.left - firstWidth / 2.0;
        double firstRight = first.left + firstWidth / 2.0;
        double firstTop = first.top - firstHeight / 2.0;
        double firstBottom = first.top + firstHeight / 2.0;
        double secondLeft = second.left - secondWidth / 2.0;
        double secondRight = second.left + secondWidth / 2.0;
        double secondTop = second.top - secondHeight / 2.0;
        double secondBottom = second.top + secondHeight / 2.0;

        double gapX = Math.max(0.0, Math.max(secondLeft - firstRight, firstLeft - secondRight));
        double gapY = Math.max(0.0, Math.max(secondTop - firstBottom, firstTop - secondBottom));
        double squares = Math.max(gapX, gapY) / square;
        double scale = feetPerSquare > 0 ? feetPerSquare : 5.0;
        return squares * scale;
    }

    private static boolean isFeetScale(String scaleUnits) {
        if (scaleUnits == null || scaleUnits.isBlank()) {
            return true;
        }
        String units = scaleUnits.trim().toLowerCase();
        return units.startsWith("ft") || units.equals("feet");
    }

    public static boolean areAdjacent(Roll20Token first, Roll20Token second, double pixelsPerSquare) {
        if (first == null || second == null || first.id == null || first.id.equals(second.id)) {
            return false;
        }
        if (first.left == null || first.top == null || second.left == null || second.top == null) {
            return false;
        }
        double square = pixelsPerSquare > 0 ? pixelsPerSquare : 70.0;
        double firstWidth = tokenSpan(first.width, square);
        double firstHeight = tokenSpan(first.height, square);
        double secondWidth = tokenSpan(second.width, square);
        double secondHeight = tokenSpan(second.height, square);

        double firstLeft = first.left - firstWidth / 2.0;
        double firstRight = first.left + firstWidth / 2.0;
        double firstTop = first.top - firstHeight / 2.0;
        double firstBottom = first.top + firstHeight / 2.0;
        double secondLeft = second.left - secondWidth / 2.0;
        double secondRight = second.left + secondWidth / 2.0;
        double secondTop = second.top - secondHeight / 2.0;
        double secondBottom = second.top + secondHeight / 2.0;

        double gapX = Math.max(0.0, Math.max(secondLeft - firstRight, firstLeft - secondRight));
        double gapY = Math.max(0.0, Math.max(secondTop - firstBottom, firstTop - secondBottom));
        return Math.max(gapX, gapY) <= square * 0.35;
    }

    private static double tokenSpan(Double pixels, double square) {
        return pixels != null && pixels > 0 ? pixels : square;
    }

    private static int[] centerCell(Roll20Token token, double square) {
        return new int[] {
            (int) Math.floor(token.left / square),
            (int) Math.floor(token.top / square)
        };
    }

    private static Set<Long> occupiedSquares(Roll20Token token, double square) {
        Set<Long> cells = new HashSet<>();
        double width = tokenSpan(token.width, square);
        double height = tokenSpan(token.height, square);
        double inset = square * 0.05;
        double left = token.left - width / 2.0;
        double right = token.left + width / 2.0;
        double top = token.top - height / 2.0;
        double bottom = token.top + height / 2.0;
        int minCol = (int) Math.floor((left + inset) / square);
        int maxCol = (int) Math.floor((right - inset) / square);
        int minRow = (int) Math.floor((top + inset) / square);
        int maxRow = (int) Math.floor((bottom - inset) / square);
        if (maxCol < minCol) {
            maxCol = minCol;
        }
        if (maxRow < minRow) {
            maxRow = minRow;
        }
        for (int col = minCol; col <= maxCol; col++) {
            for (int row = minRow; row <= maxRow; row++) {
                cells.add(packCell(col, row));
            }
        }
        return cells;
    }

    private static Set<Long> supercoverSquares(int x0, int y0, int x1, int y1) {
        Set<Long> cells = new LinkedHashSet<>();
        int nx = Math.abs(x1 - x0);
        int ny = Math.abs(y1 - y0);
        int signX = Integer.signum(x1 - x0);
        int signY = Integer.signum(y1 - y0);
        int px = x0;
        int py = y0;
        cells.add(packCell(px, py));
        for (int ix = 0, iy = 0; ix < nx || iy < ny; ) {
            long decision = (1L + 2L * ix) * ny - (1L + 2L * iy) * nx;
            if (decision == 0) {
                px += signX;
                py += signY;
                ix++;
                iy++;
            } else if (decision < 0) {
                px += signX;
                ix++;
            } else {
                py += signY;
                iy++;
            }
            cells.add(packCell(px, py));
        }
        return cells;
    }

    private static long packCell(int col, int row) {
        return ((long) col << 32) ^ (row & 0xffffffffL);
    }

    public void setSelectedTarget(Roll20Token token) {
        selectedTarget = token;
        if (token == null) {
            targetList.clearSelection();
            return;
        }
        for (int i = 0; i < targetModel.size(); i++) {
            Roll20Token candidate = targetModel.get(i);
            if (candidate != null && token.id != null && token.id.equals(candidate.id)) {
                targetList.setSelectedIndex(i);
                selectedTarget = candidate;
                return;
            }
        }
    }

    public boolean initiateCombat() {
        boolean sent = sendBridgeMessage("initiate-combat", null);
        if (sent) {
            applyLocalCombatSignal("combat-start");
        }
        return sent;
    }

    public boolean endCombat() {
        boolean sent = sendBridgeMessage("end-combat", null);
        if (sent) {
            applyLocalCombatSignal("combat-end");
        }
        return sent;
    }

    public boolean announceNewRound() {
        boolean sent = sendBridgeMessage("new-round", null);
        if (sent) {
            applyLocalCombatSignal("combat-new-round");
        }
        return sent;
    }

    private void applyLocalCombatSignal(String type) {
        SwingUtilities.invokeLater(() -> eternity.FrameSheet.applyNarratorCombatSignal(type));
    }

    public boolean sendInitiativeRoll(String tokenId, String characterName, String nickname, String macro) {
        if (!serviceConnected || macro == null || macro.isBlank()) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "roll-initiative");
            if (tokenId != null && !tokenId.isBlank()) {
                payload.put("tokenId", tokenId);
            }
            if (characterName != null && !characterName.isBlank()) {
                payload.put("characterName", characterName);
            }
            if (nickname != null && !nickname.isBlank()) {
                payload.put("nickname", nickname);
            }
            payload.put("chat", macro);
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean setTokenAura1Radius(String tokenId, String characterName, String nickname, double radius) {
        if (!serviceConnected || radius < 0) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "set-aura1-radius");
            if (tokenId != null && !tokenId.isBlank()) {
                payload.put("tokenId", tokenId);
            }
            if (characterName != null && !characterName.isBlank()) {
                payload.put("characterName", characterName);
            }
            if (nickname != null && !nickname.isBlank()) {
                payload.put("nickname", nickname);
            }
            payload.put("radius", Double.toString(radius));
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean setTokenResourceBars(String tokenId, String characterName, String nickname,
            double hpValue, double hpMax, double auraValue, double auraMax) {
        if (!serviceConnected) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "set-token-bars");
            if (tokenId != null && !tokenId.isBlank()) {
                payload.put("tokenId", tokenId);
            }
            if (characterName != null && !characterName.isBlank()) {
                payload.put("characterName", characterName);
            }
            if (nickname != null && !nickname.isBlank()) {
                payload.put("nickname", nickname);
            }
            payload.put("hpValue", Double.toString(Math.max(0.0, hpValue)));
            payload.put("hpMax", Double.toString(Math.max(0.0, hpMax)));
            payload.put("auraValue", Double.toString(Math.max(0.0, auraValue)));
            payload.put("auraMax", Double.toString(Math.max(0.0, auraMax)));
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean sendSelectToken(String tokenId) {
        if (tokenId == null || tokenId.isBlank() || !serviceConnected) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "select-token");
            payload.put("id", tokenId);
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isServiceConnected() {
        return serviceConnected;
    }

    public boolean requestTokenSnapshot() {
        if (!serviceConnected) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "request-tokens");
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isLoadedCharacterAttackTarget(String targetId, String targetName) {
        if (matchesOpenCharacter(targetId, targetName, loadedCharacterName, loadedCharacterNickname, loadedCharacterTokenId)) {
            return true;
        }
        synchronized (openCharacters) {
            for (OpenCharacter open : openCharacters.values()) {
                if (matchesOpenCharacter(targetId, targetName, open.name, open.nickname, open.tokenId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setDefenseReplyHandler(DefenseReplyHandler handler) {
        this.defenseReplyHandler = handler;
    }

    public void setClientRole(String role) {
        this.clientRole = role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
        sendClientRole();
    }

    public void notifyNarratorOfPlayerAttack() {
        if (promptLocalNarrator()) {
            return;
        }
        if (!serviceConnected) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        synchronized (narratorNotifyLock) {
            narratorNotifyLatch = latch;
        }
        if (!sendBridgeMessage("notify-narrator-attack", null)) {
            completeNarratorNotify();
            return;
        }
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            synchronized (narratorNotifyLock) {
                if (narratorNotifyLatch == latch) {
                    narratorNotifyLatch = null;
                }
            }
        }
    }

    private boolean promptLocalNarrator() {
        if (!eternity.NarratorFrame.hasOpenFrame() && !eternity.FrameSheet.hasOpenNarratorSheet()) {
            return false;
        }
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                eternity.NarratorFrame.promptPlayerAttacking();
            } else {
                SwingUtilities.invokeAndWait(eternity.NarratorFrame::promptPlayerAttacking);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void handlePlayerAttacking(PlayerAttackingEvent attacking) {
        String signalId = attacking == null ? "" : attacking.signalId;
        SwingUtilities.invokeLater(() -> {
            eternity.NarratorFrame.promptPlayerAttacking();
            sendAckNarratorAttack(signalId);
        });
    }

    private void handleNarratorCombatSignal(String type) {
        SwingUtilities.invokeLater(() -> eternity.FrameSheet.applyNarratorCombatSignal(type));
    }

    private void completeNarratorNotify() {
        CountDownLatch latch;
        synchronized (narratorNotifyLock) {
            latch = narratorNotifyLatch;
            narratorNotifyLatch = null;
        }
        if (latch != null) {
            latch.countDown();
        }
    }

    private void sendClientRole() {
        if (clientRole == null || clientRole.isBlank()) {
            return;
        }
        sendBridgeMessage("set-client-role", clientRole);
    }

    private void sendAckNarratorAttack(String signalId) {
        if (signalId == null || signalId.isBlank()) {
            return;
        }
        if (!serviceConnected) {
            return;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "ack-narrator-attack");
            payload.put("signalId", signalId);
            broadcast(mapper.writeValueAsString(payload));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean sendBridgeMessage(String type, String role) {
        if (!serviceConnected) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", type);
            if (role != null && !role.isBlank()) {
                payload.put("role", role);
            }
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void announceIfLoadedCharacterAttacked(String targetId, String targetName, String vsDefense, String messageId) {
        IncomingAttackEvent attack = new IncomingAttackEvent();
        attack.type = "incoming-attack";
        attack.targetId = targetId;
        attack.targetName = targetName;
        attack.vsDefense = vsDefense;
        attack.attackValue = null;
        attack.messageId = messageId;
        handleIncomingAttack(attack);
    }

    private boolean matchesOpenCharacter(String targetId, String targetName,
            String characterName, String nickname, String tokenId) {
        if (targetId != null && !targetId.isBlank() && tokenId != null && targetId.equals(tokenId)) {
            return true;
        }
        String normalized = normalizeName(targetName);
        if (normalized.isEmpty()) {
            return false;
        }
        if (containsAlias(buildCharacterAliases(characterName, nickname), normalized)) {
            return true;
        }
        Roll20Token token = tokenId == null || tokenId.isBlank() ? null : tokensById.get(tokenId);
        if (token == null) {
            return false;
        }
        LinkedHashSet<String> tokenAliases = new LinkedHashSet<>();
        addNameAlias(tokenAliases, token.characterName);
        addNameAlias(tokenAliases, token.name);
        addDashPrefixAlias(tokenAliases, token.characterName);
        addDashPrefixAlias(tokenAliases, token.name);
        return containsAlias(tokenAliases, normalized);
    }

    private void handleIncomingAttack(IncomingAttackEvent attack) {
        if (attack == null) {
            return;
        }
        String targetId = attack.targetId == null ? "" : attack.targetId.trim();
        String targetName = attack.targetName == null ? "" : attack.targetName.trim();
        if (targetId.isEmpty() && targetName.isEmpty()) {
            return;
        }
        String messageKey = attack.messageId == null ? "" : attack.messageId.trim();
        String targetKey = !targetId.isEmpty()
                ? "id:" + targetId
                : "name:" + normalizeName(targetName);
        synchronized (announcedIncomingAttacks) {
            if (!messageKey.isEmpty() && announcedIncomingAttacks.contains(messageKey)) {
                return;
            }
            if (!isLoadedCharacterAttackTarget(targetId, targetName)) {
                System.out.println("Incoming attack ignored (not loaded character): "
                    + targetName + " (" + targetId + ")");
                return;
            }
            long now = System.currentTimeMillis();
            if (targetKey.equals(lastAnnouncedAttackTarget) && now - lastAnnouncedAttackAtMs < 3000L) {
                if (!messageKey.isEmpty()) {
                    announcedIncomingAttacks.add(messageKey);
                }
                return;
            }
            if (!messageKey.isEmpty()) {
                announcedIncomingAttacks.add(messageKey);
                while (announcedIncomingAttacks.size() > 32) {
                    String first = announcedIncomingAttacks.iterator().next();
                    announcedIncomingAttacks.remove(first);
                }
            }
            lastAnnouncedAttackTarget = targetKey;
            lastAnnouncedAttackAtMs = now;
        }
        int openCharacterKey = findMatchingOpenCharacterKey(targetId, targetName);
        String vsDefense = attack.vsDefense == null ? "" : attack.vsDefense.trim();
        System.out.println("Loaded character is the attack target: " + targetName + " vs " + vsDefense);
        String macro = defenseReplyHandler == null
                ? null
                : defenseReplyHandler.buildDefenseReply(openCharacterKey, attack);
        if (macro != null && !macro.isBlank()) {
            sendChatMessage(macro);
        }
    }

    private int findMatchingOpenCharacterKey(String targetId, String targetName) {
        synchronized (openCharacters) {
            for (Map.Entry<Integer, OpenCharacter> entry : openCharacters.entrySet()) {
                OpenCharacter open = entry.getValue();
                if (matchesOpenCharacter(targetId, targetName, open.name, open.nickname, open.tokenId)) {
                    return entry.getKey();
                }
            }
        }
        return 0;
    }

    public boolean sendChatMessage(String text) {
        if (text == null || text.isBlank() || !serviceConnected) {
            return false;
        }
        Collection<WebSocket> connections = getConnections();
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", "send-chat");
            payload.put("text", text);
            broadcast(mapper.writeValueAsString(payload));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
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
            String selectedId = selectedTarget == null ? null : selectedTarget.id;
            targetModel.clear();
            markLoadedCharacterToken();

            tokensById.values().stream()
                .filter(Roll20WebSocketServer::isTargetableToken)
                .sorted(Comparator
                    .comparing((Roll20Token token) -> !token.loadedCharacter)
                    .thenComparing(token -> token.name == null ? "" : token.name, String.CASE_INSENSITIVE_ORDER))
                .forEach(targetModel::addElement);

            if (selectedId == null) {
                selectedTarget = null;
                return;
            }
            for (int i = 0; i < targetModel.size(); i++) {
                Roll20Token candidate = targetModel.get(i);
                if (candidate != null && selectedId.equals(candidate.id)) {
                    selectedTarget = candidate;
                    targetList.setSelectedIndex(i);
                    return;
                }
            }
            selectedTarget = null;
        });
    }

    private void markLoadedCharacterToken() {
        loadedCharacterTokenId = null;
        for (Roll20Token token : tokensById.values()) {
            if (token != null) {
                token.loadedCharacter = false;
            }
        }
        Roll20Token match = resolveLoadedCharacterToken();
        if (match != null) {
            match.loadedCharacter = true;
            loadedCharacterTokenId = match.id;
            System.out.println("Loaded character token: " + match.name + " (" + match.id + ")");
        }
        refreshOpenCharacterTokens();
    }

    private void refreshOpenCharacterTokens() {
        synchronized (openCharacters) {
            for (OpenCharacter open : openCharacters.values()) {
                Roll20Token match = resolveCharacterToken(open.name, open.nickname, false);
                open.tokenId = match == null ? null : match.id;
            }
        }
    }

    public String resolveTokenId(String name, String nickname) {
        Roll20Token match = resolveCharacterToken(name, nickname, false);
        return match == null ? null : match.id;
    }

    private Roll20Token resolveLoadedCharacterToken() {
        return resolveCharacterToken(loadedCharacterName, loadedCharacterNickname, true);
    }

    private Roll20Token resolveCharacterToken(String name, String nickname, boolean allowOnlyControlledFallback) {
        List<String> aliases = buildCharacterAliases(name, nickname);
        Roll20Token best = null;
        int bestScore = 0;
        int controlledCount = 0;
        Roll20Token onlyControlled = null;

        for (Roll20Token token : tokensById.values()) {
            if (!isTargetableToken(token)) {
                continue;
            }
            if (Boolean.TRUE.equals(token.controlledByMe)) {
                controlledCount++;
                onlyControlled = token;
            }
            int score = scoreLoadedCharacterMatch(token, aliases);
            if (score > bestScore) {
                bestScore = score;
                best = token;
            }
        }
        if (bestScore > 0) {
            return best;
        }
        return allowOnlyControlledFallback && controlledCount == 1 ? onlyControlled : null;
    }

    private List<String> buildLoadedCharacterAliases() {
        return buildCharacterAliases(loadedCharacterName, loadedCharacterNickname);
    }

    private static List<String> buildCharacterAliases(String name, String nickname) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        addNameAlias(aliases, name);
        addNameAlias(aliases, nickname);
        addDashPrefixAlias(aliases, name);
        return new ArrayList<>(aliases);
    }

    private static int scoreLoadedCharacterMatch(Roll20Token token, List<String> aliases) {
        if (aliases == null || aliases.isEmpty() || token == null) {
            return 0;
        }
        LinkedHashSet<String> tokenAliases = new LinkedHashSet<>();
        addNameAlias(tokenAliases, token.characterName);
        addNameAlias(tokenAliases, token.name);
        addDashPrefixAlias(tokenAliases, token.characterName);
        addDashPrefixAlias(tokenAliases, token.name);

        int score = 0;
        if (containsAlias(aliases, normalizeName(token.characterName))) {
            score = 100;
        } else if (containsAlias(aliases, normalizeName(token.name))) {
            score = 80;
        } else if (overlapsAlias(aliases, tokenAliases)) {
            score = 60;
        } else {
            return 0;
        }
        if (Boolean.TRUE.equals(token.controlledByMe)) {
            score += 20;
        }
        if (Boolean.TRUE.equals(token.selected)) {
            score += 10;
        }
        return score;
    }

    private static void addNameAlias(Set<String> aliases, String raw) {
        String normalized = normalizeName(raw);
        if (!normalized.isEmpty()) {
            aliases.add(normalized);
        }
    }

    private static void addDashPrefixAlias(Set<String> aliases, String raw) {
        String normalized = normalizeName(raw);
        int separator = normalized.indexOf(" - ");
        if (separator > 0) {
            addNameAlias(aliases, normalized.substring(0, separator));
        }
    }

    private static boolean containsAlias(Collection<String> aliases, String candidate) {
        return candidate != null && !candidate.isEmpty() && aliases.contains(candidate);
    }

    private static boolean overlapsAlias(Collection<String> left, Collection<String> right) {
        for (String alias : left) {
            if (right.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static boolean isTargetableToken(Roll20Token token) {
        if (token == null || token.id == null || token.id.isBlank()) {
            return false;
        }
        String layer = token.layer == null ? "" : token.layer.trim().toLowerCase();
        if (layer.equals("map") || layer.equals("walls") || layer.equals("foreground") || layer.equals("weather")) {
            return false;
        }
        if (layer.equals("objects") || layer.equals("gmlayer") || layer.isEmpty()) {
            return true;
        }
        return (token.name != null && !token.name.isBlank())
            || (token.represents != null && !token.represents.isBlank());
    }

    private void notifyConnectedListeners() {
        notifyConnectionListeners(true);
    }

    private void notifyDisconnectedListeners() {
        notifyConnectionListeners(false);
    }

    private void notifyConnectionListeners(boolean connected) {
        Set<ConnectionListener> snapshot;
        synchronized (this) {
            snapshot = new HashSet<>(connectionListeners);
        }
        SwingUtilities.invokeLater(() -> {
            for (ConnectionListener listener : snapshot) {
                if (connected) {
                    listener.onRoll20Connected();
                } else {
                    listener.onRoll20Disconnected();
                }
            }
        });
    }

    private static final class OpenCharacter {
        private final String name;
        private final String nickname;
        private String tokenId;

        private OpenCharacter(String name, String nickname) {
            this.name = name;
            this.nickname = nickname;
        }
    }
}
