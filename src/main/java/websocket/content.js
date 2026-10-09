let socket = null;
let reconnectTimer = null;
let eternityCampaign = false;
let pageBridgeInjected = false;

function sendMessage(data) {
    console.log("Sending to Java:", data);

    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(data));
    } else {
        console.log("Socket not open:", socket ? socket.readyState : "none");
    }
}

function requestTokenSnapshotFromPage() {
    window.postMessage({
        source: "eternity-java-bridge",
        payload: { type: "request-tokens" }
    }, "*");
}

function scheduleReconnect() {
    if (!eternityCampaign || reconnectTimer || (socket && socket.readyState === WebSocket.OPEN)) {
        return;
    }
    reconnectTimer = setTimeout(() => {
        reconnectTimer = null;
        connectToClient();
    }, 1000);
}

function connectToClient() {
    if (!eternityCampaign) {
        return;
    }
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
        return;
    }

    try {
        socket = new WebSocket("ws://localhost:8080");
    } catch (error) {
        console.log("WebSocket connect failed:", error);
        scheduleReconnect();
        return;
    }

    socket.onopen = () => {
        console.log("WebSocket connected from content.js");
        requestTokenSnapshotFromPage();
    };

    socket.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            if (!data || !data.type) return;
            window.postMessage({
                source: "eternity-java-bridge",
                payload: data
            }, "*");
        } catch (error) {
            console.error("Failed to handle message from Java:", error);
        }
    };

    socket.onclose = () => {
        socket = null;
        scheduleReconnect();
    };

    socket.onerror = () => {
        // onclose follows and schedules the next retry.
    };
}

function setEternityCampaign(enabled) {
    const next = !!enabled;
    if (eternityCampaign === next) {
        return;
    }
    eternityCampaign = next;
    console.log("Eternity campaign enabled:", eternityCampaign);
    if (eternityCampaign) {
        connectToClient();
        return;
    }
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
    if (socket) {
        const closing = socket;
        socket = null;
        closing.close();
    }
}

function injectPageScript() {
    if (pageBridgeInjected) {
        return;
    }
    pageBridgeInjected = true;
    console.log("Injecting pageBridge.js...");

    const script = document.createElement("script");
    script.src = chrome.runtime.getURL("pageBridge.js") + "?t=" + Date.now();

    script.onload = () => {
        console.log("pageBridge.js loaded into page");
        script.remove();
    };

    script.onerror = (error) => {
        console.error("Failed to load pageBridge.js", error);
        pageBridgeInjected = false;
    };

    document.documentElement.appendChild(script);
}

window.addEventListener("message", (event) => {
    if (event.source !== window) return;
    if (!event.data || event.data.source !== "roll20-page-bridge") return;
    const payload = event.data.payload;
    if (!payload || !payload.type) return;

    if (payload.type === "eternity-campaign") {
        setEternityCampaign(payload.enabled);
        return;
    }

    sendMessage(payload);
});

injectPageScript();
