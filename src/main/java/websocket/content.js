const socket = new WebSocket("ws://localhost:8080");

function sendMessage(data) {
    console.log("Sending to Java:", data);

    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(data));
    } else {
        console.log("Socket not open:", socket.readyState);
    }
}

socket.onopen = () => {
    console.log("WebSocket connected from content.js");
    injectPageScript();
};

function injectPageScript() {
    console.log("Injecting pageBridge.js...");

    const script = document.createElement("script");
    script.src = chrome.runtime.getURL("pageBridge.js");

    script.onload = () => {
        console.log("pageBridge.js loaded into page");
        script.remove();
    };

    script.onerror = (error) => {
        console.error("Failed to load pageBridge.js", error);
    };

    document.documentElement.appendChild(script);
}

window.addEventListener("message", (event) => {
    console.log("content.js received window message:", event.data);

    if (event.source !== window) return;
    if (!event.data || event.data.source !== "roll20-page-bridge") return;

    sendMessage(event.data.payload);
});