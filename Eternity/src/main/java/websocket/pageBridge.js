console.log("PAGE BRIDGE LOADED");

function watchTokenChanges() {
    const graphics =
        window.Campaign?.engine?.page?.thegraphics?.models || [];

    console.log("Registering token watchers:", graphics.length);

    graphics.forEach(g => {
        g.on("change", function(model) {
            const a = model.attributes || {};

            console.log("TOKEN CHANGED:", {
                id: a.id || model.id,
                name: a.name,
                left: a.left,
                top: a.top
            });

            window.postMessage({
                source: "roll20-page-bridge",
                payload: {
                    type: "token-change",
                    id: a.id || model.id || "",
                    name: a.name || "",
                    layer: a.layer || "",
                    left: a.left,
                    top: a.top,
                    width: a.width,
                    height: a.height,
                    represents: a.represents || "",
                    controlledby: a.controlledby || ""
                }
            }, "*");
        });
    });
}

function isRoll20Ready() {
    return (
        typeof window.Campaign !== "undefined" &&
        window.Campaign.engine &&
        window.Campaign.engine.page &&
        window.Campaign.engine.page.thegraphics &&
        window.Campaign.engine.page.thegraphics.models
    );
}

function sendTokenSnapshot() {
    console.log("TOKEN SNAPSHOT RUNNING");

    const graphics =
        window.Campaign?.engine?.page?.thegraphics?.models || [];

    console.log("Graphics found:", graphics.length);

    const tokens = graphics.map(g => {
        const a = g.attributes || {};

        return {
            id: a.id || g.id || "",
            name: a.name || "",
            layer: a.layer || "",
            left: a.left,
            top: a.top,
            width: a.width,
            height: a.height,
            represents: a.represents || "",
            controlledby: a.controlledby || ""
        };
    });

    window.postMessage({
        source: "roll20-page-bridge",
        payload: {
            type: "token-snapshot",
            count: tokens.length,
            tokens: tokens
        }
    }, "*");
}

function waitForRoll20(attempt = 0) {
    if (isRoll20Ready()) {
        console.log("Roll20 is ready in pageBridge.js");
        sendTokenSnapshot();
        watchTokenChanges();
        return;
    }

    if (attempt >= 120) {
        console.log("Roll20 not ready after 120 seconds");

        window.postMessage({
            source: "roll20-page-bridge",
            payload: {
                type: "error",
                message: "Roll20 Campaign not ready in pageBridge.js",
                campaignType: typeof window.Campaign,
                hasCampaign: typeof window.Campaign !== "undefined"
            }
        }, "*");

        return;
    }

    console.log("Waiting for Roll20 in pageBridge.js...", attempt);
    setTimeout(() => waitForRoll20(attempt + 1), 1000);
}

waitForRoll20();