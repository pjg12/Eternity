console.log("PAGE BRIDGE LOADED");

window.addEventListener("message", (event) => {
    if (event.source !== window) return;
    if (!event.data || event.data.source !== "eternity-java-bridge") return;
    const payload = event.data.payload;
    if (!payload || !payload.type) return;
    if (payload.type === "send-chat") {
        sendToRoll20Chat(payload.text || "");
        return;
    }
    if (payload.type === "select-token") {
        selectRoll20Token(payload.id || "");
        return;
    }
    if (payload.type === "request-tokens") {
        if (eternityCampaignEnabled) {
            sendTokenSnapshot();
            watchTokenChanges();
            watchChatForAttacks();
        }
        return;
    }
    if (payload.type === "set-client-role") {
        setClientRole(payload.role || "");
        return;
    }
    if (payload.type === "notify-narrator-attack") {
        notifyNarratorAttack();
        return;
    }
    if (payload.type === "ack-narrator-attack") {
        ackNarratorAttack(payload.signalId || "");
        return;
    }
    if (payload.type === "initiate-combat") {
        initiateCombat();
        return;
    }
    if (payload.type === "end-combat") {
        endNarratorCombat();
        return;
    }
    if (payload.type === "new-round") {
        announceNewRound();
        return;
    }
    if (payload.type === "roll-initiative") {
        applyInitiativeRoll(payload);
        return;
    }
    if (payload.type === "set-aura1-radius") {
        setTokenAura1Radius(payload);
        return;
    }
    if (payload.type === "set-token-bars") {
        setTokenResourceBars(payload);
        return;
    }
});

function sendToRoll20Chat(text) {
    if (!text) return false;

    try {
        if (window.d20 && d20.textchat && typeof d20.textchat.doChatInput === "function") {
            d20.textchat.doChatInput(text);
            console.log("Sent chat via d20.textchat.doChatInput");
            return true;
        }
    } catch (error) {
        console.error("doChatInput failed", error);
    }

    try {
        if (window.$) {
            const $input = $("#textchat-input textarea");
            if ($input.length) {
                $input.val(text);
                const $form = $input.closest("form");
                if ($form.length) {
                    $form.trigger("submit");
                    console.log("Sent chat via jQuery form submit");
                    return true;
                }
                $input.trigger(jQuery.Event("keydown", { which: 13, keyCode: 13, key: "Enter" }));
                console.log("Sent chat via jQuery Enter key");
                return true;
            }
        }
    } catch (error) {
        console.error("jQuery chat send failed", error);
    }

    const textarea = document.querySelector("#textchat-input textarea")
        || document.querySelector("#textchat-input .ui-autocomplete-input");
    if (!textarea) {
        console.error("Roll20 chat input not found");
        return false;
    }

    const descriptor = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value");
    if (descriptor && descriptor.set) {
        descriptor.set.call(textarea, text);
    } else {
        textarea.value = text;
    }
    textarea.dispatchEvent(new Event("input", { bubbles: true }));
    textarea.dispatchEvent(new KeyboardEvent("keydown", {
        key: "Enter",
        code: "Enter",
        keyCode: 13,
        which: 13,
        bubbles: true
    }));
    console.log("Sent chat via textarea Enter key");
    return true;
}

function getCampaign() {
    return window.d20?.Campaign || window.Campaign || null;
}

function getCurrentPageId(campaign) {
    if (!campaign) return "";

    try {
        if (typeof campaign.activePage === "function") {
            const id = campaign.activePage();
            if (id) return id;
        }
    } catch (error) {
        console.error("activePage failed", error);
    }

    try {
        if (typeof campaign.get === "function") {
            const specific = campaign.get("playerspecificpages");
            const playerId = window.currentPlayer?.id;
            if (specific && playerId && specific[playerId]) {
                return specific[playerId];
            }
            const playerPage = campaign.get("playerpageid");
            if (playerPage) return playerPage;
        }
    } catch (error) {
        console.error("campaign page id failed", error);
    }

    try {
        const enginePage = window.d20?.engine?.currentPage
            || window.d20?.engine?.curPage
            || window.d20?.engine?.page;
        if (typeof enginePage === "function") {
            const value = enginePage();
            return value?.id || value || "";
        }
        if (enginePage?.id) return enginePage.id;
        if (typeof enginePage === "string") return enginePage;
    } catch (error) {
        console.error("engine page id failed", error);
    }

    return "";
}

function getPageGrid() {
    const campaign = getCampaign();
    const pages = campaign?.pages;
    const pageId = getCurrentPageId(campaign);
    const page = pages && typeof pages.get === "function" && pageId ? pages.get(pageId) : null;
    let snapping = 1;
    let scaleNumber = 5;
    let scaleUnits = "ft";
    try {
        if (page && typeof page.get === "function") {
            snapping = Number(page.get("snapping_increment"));
            scaleNumber = Number(page.get("scale_number"));
            scaleUnits = String(page.get("scale_units") || "ft");
        } else if (page?.attributes) {
            snapping = Number(page.attributes.snapping_increment);
            scaleNumber = Number(page.attributes.scale_number);
            scaleUnits = String(page.attributes.scale_units || "ft");
        }
    } catch (error) {
        console.error("page grid lookup failed", error);
    }
    if (!Number.isFinite(snapping) || snapping <= 0) snapping = 1;
    if (!Number.isFinite(scaleNumber) || scaleNumber <= 0) scaleNumber = 5;
    return {
        pixelsPerSquare: 70 * snapping,
        scaleNumber,
        scaleUnits
    };
}

function graphicsFromPage(page) {
    if (!page) return [];
    const graphics = page.thegraphics;
    if (!graphics) return [];
    if (Array.isArray(graphics.models)) return graphics.models;
    if (typeof graphics.toArray === "function") return graphics.toArray();
    if (typeof graphics.map === "function") {
        const mapped = [];
        graphics.map(g => mapped.push(g));
        return mapped;
    }
    if (Array.isArray(graphics)) return graphics;
    return [];
}

function collectFromCanvas() {
    const canvas = window.d20?.engine?.canvas || window.d20?.engine?.currentCanvas;
    if (!canvas) return [];
    let objects = [];
    try {
        if (typeof canvas.getObjects === "function") {
            objects = canvas.getObjects();
        } else if (Array.isArray(canvas._objects)) {
            objects = canvas._objects;
        }
    } catch (error) {
        console.error("canvas token collect failed", error);
        return [];
    }
    return objects
        .map(object => object.model || object._model || object)
        .filter(Boolean);
}

function collectGraphicModels() {
    const campaign = getCampaign();
    const models = [];
    const seen = new Set();

    const addAll = (list) => {
        (list || []).forEach(graphic => {
            if (!graphic) return;
            const id = graphic.id || graphic.attributes?.id;
            if (!id || seen.has(id)) return;
            seen.add(id);
            models.push(graphic);
        });
    };

    if (campaign) {
        const pages = campaign.pages;
        const pageId = getCurrentPageId(campaign);
        if (pages && typeof pages.get === "function" && pageId) {
            addAll(graphicsFromPage(pages.get(pageId)));
        }
        addAll(graphicsFromPage(window.d20?.engine?.page || campaign.engine?.page));
        if (models.length === 0 && Array.isArray(pages?.models)) {
            pages.models.forEach(page => addAll(graphicsFromPage(page)));
        }
    }

    if (models.length === 0) {
        addAll(collectFromCanvas());
    }

    return models;
}

function readAttr(model, key) {
    if (!model) return "";
    try {
        if (typeof model.get === "function") {
            const value = model.get(key);
            if (value != null && value !== "") return value;
        }
    } catch (error) {
        // Fall through to attributes.
    }
    const attributes = model.attributes || {};
    return attributes[key] != null ? attributes[key] : "";
}

function resolveRepresentedCharacter(model) {
    const represents = readAttr(model, "represents");
    if (!represents) return null;
    try {
        const characters = getCampaign()?.characters;
        return characters && typeof characters.get === "function"
            ? characters.get(represents)
            : null;
    } catch (error) {
        console.error("character lookup failed", error);
        return null;
    }
}

function resolveRepresentedCharacterName(model) {
    const character = resolveRepresentedCharacter(model);
    if (!character) return "";
    const characterName = readAttr(character, "name");
    return characterName ? String(characterName) : "";
}

function resolveControlledBy(model) {
    const tokenControlledBy = String(readAttr(model, "controlledby") || "").trim();
    if (tokenControlledBy) return tokenControlledBy;
    const character = resolveRepresentedCharacter(model);
    if (!character) return "";
    return String(readAttr(character, "controlledby") || "").trim();
}

function resolveTokenName(model) {
    const name = readAttr(model, "name");
    if (name) return String(name);
    return resolveRepresentedCharacterName(model);
}

function isControlledByMe(model) {
    const controlledby = resolveControlledBy(model);
    if (!controlledby) return false;
    if (controlledby === "all") return true;
    const playerId = window.currentPlayer?.id;
    if (!playerId) return false;
    return controlledby.split(",").map(part => part.trim()).includes(String(playerId));
}

function isCurrentlySelected(model) {
    if (!model) return false;
    const tokenId = String(model.id || readAttr(model, "id") || "");
    if (!tokenId) return false;
    try {
        const selected = window.d20?.engine?.selected;
        const list = typeof selected === "function" ? selected() : selected;
        if (!list) return false;
        const matches = (item) => {
            const candidate = item?.model || item?._model || item;
            if (!candidate) return false;
            const id = String(candidate.id || readAttr(candidate, "id") || "");
            return id === tokenId;
        };
        if (Array.isArray(list)) {
            return list.some(matches);
        }
        if (typeof list.forEach === "function") {
            let found = false;
            list.forEach(item => {
                if (matches(item)) found = true;
            });
            return found;
        }
    } catch (error) {
        console.error("selected token lookup failed", error);
    }
    return false;
}

function toNumber(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
}

function tokenPayload(model) {
    const characterName = resolveRepresentedCharacterName(model);
    return {
        id: String(model.id || readAttr(model, "id") || ""),
        name: resolveTokenName(model),
        characterName: characterName,
        layer: String(readAttr(model, "layer") || ""),
        left: toNumber(readAttr(model, "left")),
        top: toNumber(readAttr(model, "top")),
        width: toNumber(readAttr(model, "width")),
        height: toNumber(readAttr(model, "height")),
        represents: String(readAttr(model, "represents") || ""),
        controlledby: resolveControlledBy(model),
        controlledByMe: isControlledByMe(model),
        selected: isCurrentlySelected(model)
    };
}

function findGraphicModel(tokenId) {
    if (!tokenId) return null;
    return collectGraphicModels().find(graphic =>
        graphic.id === tokenId || readAttr(graphic, "id") === tokenId) || null;
}

function writeGraphicAttrs(model, attrs) {
    if (!model || !attrs) return false;
    try {
        if (typeof model.save === "function") {
            model.save(attrs);
            return true;
        }
    } catch (error) {
        console.error("graphic.save failed", error);
    }
    try {
        if (typeof model.set === "function") {
            Object.keys(attrs).forEach(key => model.set(key, attrs[key]));
            return true;
        }
    } catch (error) {
        console.error("graphic.set failed", error);
    }
    return false;
}

function formatAuraRadius(value) {
    const number = Number(value);
    if (!Number.isFinite(number) || number < 0) return "";
    const rounded = Math.round(number * 10) / 10;
    if (Math.abs(rounded - Math.round(rounded)) <= 0.0001) {
        return String(Math.round(rounded));
    }
    return String(rounded);
}

function toNonNegativeNumber(value) {
    const number = Number(value);
    if (!Number.isFinite(number) || number < 0) return 0;
    return Math.round(number * 10) / 10;
}

function setTokenResourceBars(payload) {
    const tokenId = resolveInitiativeTokenId(
        String((payload && payload.tokenId) || ""),
        String((payload && payload.characterName) || ""),
        String((payload && payload.nickname) || ""));
    const model = findGraphicModel(tokenId);
    if (!model) {
        console.error("Could not find token for resource bars", tokenId);
        return false;
    }
    const attrs = {
        bar1_value: toNonNegativeNumber(payload && payload.hpValue),
        bar1_max: toNonNegativeNumber(payload && payload.hpMax),
        bar2_value: toNonNegativeNumber(payload && payload.auraValue),
        bar2_max: toNonNegativeNumber(payload && payload.auraMax)
    };
    const updated = writeGraphicAttrs(model, attrs);
    console.log("Set token bars", tokenId, attrs, updated);
    return updated;
}

function setTokenAura1Radius(payload) {
    const radiusText = formatAuraRadius(payload && payload.radius);
    if (!radiusText) {
        console.error("Aura 1 radius missing");
        return false;
    }
    const tokenId = resolveInitiativeTokenId(
        String((payload && payload.tokenId) || ""),
        String((payload && payload.characterName) || ""),
        String((payload && payload.nickname) || ""));
    const model = findGraphicModel(tokenId);
    if (!model) {
        console.error("Could not find token for Aura 1 radius", tokenId);
        return false;
    }
    const updated = writeGraphicAttrs(model, { aura1_radius: radiusText });
    console.log("Set Aura 1 radius", radiusText, "on", tokenId, updated);
    return updated;
}

function selectRoll20Token(tokenId) {
    if (!tokenId) return false;

    try {
        const model = findGraphicModel(tokenId);
        if (!model) {
            console.error("Roll20 token not found:", tokenId);
            return false;
        }

        if (window.d20 && d20.engine) {
            if (typeof d20.engine.select === "function") {
                d20.engine.select(model);
            }
            const left = toNumber(readAttr(model, "left"));
            const top = toNumber(readAttr(model, "top"));
            if (typeof d20.engine.ping === "function") {
                d20.engine.ping(left, top);
            }
        }
        console.log("Selected Roll20 token:", tokenId);
        return true;
    } catch (error) {
        console.error("selectRoll20Token failed", error);
        return false;
    }
}

function getPlayerPageId(campaign) {
    if (!campaign || typeof campaign.get !== "function") return "";
    try {
        const playerPage = campaign.get("playerpageid");
        return playerPage ? String(playerPage) : "";
    } catch (error) {
        console.error("player page id failed", error);
        return "";
    }
}

function isPlayerVisibleToken(model) {
    if (!model) return false;
    const id = String(model.id || readAttr(model, "id") || "");
    if (!id) return false;
    const layer = String(readAttr(model, "layer") || "").trim().toLowerCase();
    if (layer && layer !== "objects") return false;
    const drawing = readAttr(model, "isdrawing");
    if (drawing === true || drawing === "true" || drawing === 1 || drawing === "1") return false;
    const cardId = readAttr(model, "cardid");
    if (cardId) return false;
    return true;
}

function collectPlayerVisibleTokens() {
    const campaign = getCampaign();
    const pageId = getPlayerPageId(campaign);
    let models = [];
    if (pageId && campaign?.pages && typeof campaign.pages.get === "function") {
        models = graphicsFromPage(campaign.pages.get(pageId));
    }
    if (!models.length) {
        models = collectGraphicModels();
    }
    return models.filter(isPlayerVisibleToken);
}

function writeCampaignAttrs(attrs) {
    const campaign = getCampaign();
    if (!campaign || !attrs) return false;
    try {
        if (typeof campaign.save === "function") {
            campaign.save(attrs);
            return true;
        }
    } catch (error) {
        console.error("campaign.save failed", error);
    }
    try {
        if (typeof campaign.set === "function") {
            Object.keys(attrs).forEach(key => campaign.set(key, attrs[key]));
            return true;
        }
    } catch (error) {
        console.error("campaign.set failed", error);
    }
    return false;
}

function openTurnOrderWidget(pageId) {
    const campaign = getCampaign();
    const trackerPage = pageId || getPlayerPageId(campaign) || getCurrentPageId(campaign);
    if (trackerPage) {
        writeCampaignAttrs({ initiativepage: trackerPage });
    }

    try {
        const win = window.d20?.Campaign?.initiativewindow;
        if (win && typeof win.open === "function") {
            win.open();
            return true;
        }
        if (win && typeof win.show === "function") {
            win.show();
            return true;
        }
    } catch (error) {
        console.error("initiativewindow open failed", error);
    }

    try {
        if (window.$) {
            const $win = $("#initiativewindow");
            if ($win.length && typeof $win.dialog === "function") {
                if ($win.dialog("isOpen") !== true) {
                    $win.dialog("open");
                }
                return true;
            }
        }
    } catch (error) {
        console.error("initiative dialog open failed", error);
    }

    try {
        const button = document.querySelector("#initiativewindow_toggle")
            || document.querySelector("#startturns")
            || document.querySelector("[data-action='toggle-initiative']");
        if (button) {
            button.click();
            return true;
        }
    } catch (error) {
        console.error("initiative button click failed", error);
    }
    return false;
}

function initiateCombat() {
    const campaign = getCampaign();
    const pageId = getPlayerPageId(campaign) || getCurrentPageId(campaign);
    const entries = collectPlayerVisibleTokens().map(model => ({
        id: String(model.id || readAttr(model, "id") || ""),
        pr: 0,
        custom: "",
        _pageid: pageId || ""
    })).filter(entry => entry.id);

    const campaignUpdate = { turnorder: JSON.stringify(entries) };
    if (pageId) {
        campaignUpdate.initiativepage = pageId;
    }
    writeCampaignAttrs(campaignUpdate);
    openTurnOrderWidget(pageId);
    publishCombatSignal("combat-start");
    sendToRoll20Chat("/desc Combat has begun.");
    console.log("Initiated combat with", entries.length, "player-visible tokens");
    return entries.length;
}

function endNarratorCombat() {
    writeCampaignAttrs({
        turnorder: "[]",
        initiativepage: false
    });
    try {
        if (window.$) {
            const $win = $("#initiativewindow");
            if ($win.length && typeof $win.dialog === "function" && $win.dialog("isOpen") === true) {
                $win.dialog("close");
            }
        }
    } catch (error) {
        console.error("initiative dialog close failed", error);
    }
    publishCombatSignal("combat-end");
    sendToRoll20Chat("/desc Combat has ended.");
    console.log("Ended narrator combat");
}

function announceNewRound() {
    publishCombatSignal("combat-new-round");
    sendToRoll20Chat("/desc A new round has begun.");
    console.log("Announced new combat round");
}

function applyInitiativeRoll(payload) {
    let chat = String((payload && payload.chat) || "");
    if (!chat) {
        console.error("Initiative roll missing ScriptCard macro");
        return false;
    }
    const tokenId = resolveInitiativeTokenId(
        String((payload && payload.tokenId) || ""),
        String((payload && payload.characterName) || ""),
        String((payload && payload.nickname) || ""));
    if (tokenId) {
        selectRoll20Token(tokenId);
        chat = chat.replace(/@\{selected\|token_id\}/gi, tokenId);
        const current = getTurnOrderValue(tokenId);
        chat = chat.replace(/--=InitTotal\|\[\$SkillCheck\]/i, `--=InitTotal|[$SkillCheck] + ${current}`);
        chat = chat.replace(/@\{tracker\|[^}]+\}(?:\s*&\{noerror\})?/gi, String(current));
    }
    sendToRoll20Chat(chat);
    console.log("Sent Initiative Roll macro for", tokenId || (payload && payload.characterName) || "unknown token");
    return true;
}

function getTurnOrderValue(tokenId) {
    if (!tokenId) return 0;
    const entry = readTurnOrder().find(item => item && String(item.id) === String(tokenId));
    const current = Number(entry && entry.pr);
    return Number.isFinite(current) ? current : 0;
}

function readTurnOrder() {
    const campaign = getCampaign();
    if (!campaign || typeof campaign.get !== "function") return [];
    try {
        const raw = campaign.get("turnorder");
        if (!raw) return [];
        const parsed = typeof raw === "string" ? JSON.parse(raw) : raw;
        return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
        console.error("turnorder parse failed", error);
        return [];
    }
}

function namesMatch(left, right) {
    const a = String(left || "").trim().toLowerCase();
    const b = String(right || "").trim().toLowerCase();
    return !!a && !!b && a === b;
}

function resolveInitiativeTokenId(tokenId, characterName, nickname) {
    const requested = String(tokenId || "");
    if (requested) return requested;
    const aliases = [characterName, nickname].filter(value => String(value || "").trim());
    const models = collectPlayerVisibleTokens();
    for (const model of models) {
        if (aliases.some(alias =>
                namesMatch(alias, resolveTokenName(model))
                || namesMatch(alias, resolveRepresentedCharacterName(model)))) {
            return String(model.id || readAttr(model, "id") || "");
        }
    }
    const mine = models.filter(isControlledByMe);
    if (mine.length === 1) {
        return String(mine[0].id || readAttr(mine[0], "id") || "");
    }
    const selected = models.find(isCurrentlySelected);
    if (selected) {
        return String(selected.id || readAttr(selected, "id") || "");
    }
    return "";
}

function publishCombatSignal(type) {
    const id = Date.now() + "-" + Math.random().toString(36).slice(2, 8);
    lastHandledSignalId = id;
    updateBridgeState(bridge => {
        bridge.signal = { type: type, id: id };
        return bridge;
    });
    return id;
}

let watchingTokens = false;
let snapshotRetryTimer = null;

function watchTokenChanges() {
    const graphics = collectGraphicModels();
    console.log("Registering token watchers:", graphics.length);

    graphics.forEach(graphic => {
        if (!graphic || typeof graphic.on !== "function" || graphic._eternityTokenWatch) return;
        graphic._eternityTokenWatch = true;
        graphic.on("change", function (model) {
            const payload = tokenPayload(model || graphic);
            console.log("TOKEN CHANGED:", {
                id: payload.id,
                name: payload.name,
                left: payload.left,
                top: payload.top
            });
            window.postMessage({
                source: "roll20-page-bridge",
                payload: Object.assign({ type: "token-change" }, payload)
            }, "*");
        });
    });

    if (watchingTokens) return;
    watchingTokens = true;

    const campaign = getCampaign();
    const pages = campaign?.pages;
    const pageId = getCurrentPageId(campaign);
    const page = pages && typeof pages.get === "function" && pageId ? pages.get(pageId) : null;
    const collection = page?.thegraphics;
    if (collection && typeof collection.on === "function") {
        collection.on("add", function () {
            sendTokenSnapshot();
            watchTokenChanges();
        });
        collection.on("remove", function () {
            sendTokenSnapshot();
        });
    }
    if (campaign && typeof campaign.on === "function") {
        campaign.on("change:playerpageid", function () {
            watchingTokens = false;
            sendTokenSnapshot();
            watchTokenChanges();
        });
    }
}

function isRoll20Ready() {
    return collectGraphicModels().length > 0
        || !!getCampaign()?.pages
        || !!(window.d20 && d20.textchat);
}

function sendTokenSnapshot() {
    console.log("TOKEN SNAPSHOT RUNNING");

    const tokens = collectGraphicModels().map(tokenPayload).filter(token => token.id);
    const grid = getPageGrid();
    console.log("Graphics found:", tokens.length, "grid:", grid);

    window.postMessage({
        source: "roll20-page-bridge",
        payload: {
            type: "token-snapshot",
            count: tokens.length,
            pixelsPerSquare: grid.pixelsPerSquare,
            scaleNumber: grid.scaleNumber,
            scaleUnits: grid.scaleUnits,
            tokens: tokens
        }
    }, "*");

    if (tokens.length === 0 && !snapshotRetryTimer) {
        let attempts = 0;
        snapshotRetryTimer = setInterval(() => {
            attempts += 1;
            const retryTokens = collectGraphicModels().map(tokenPayload).filter(token => token.id);
            if (retryTokens.length > 0 || attempts >= 15) {
                clearInterval(snapshotRetryTimer);
                snapshotRetryTimer = null;
                if (retryTokens.length > 0) {
                    sendTokenSnapshot();
                    watchTokenChanges();
                }
            }
        }, 1000);
    }
}

const ETERNITY_HANDOUT_NAME = "eternity";
let eternityCampaignEnabled = false;
let watchingHandouts = false;

function collectHandouts() {
    const handouts = getCampaign()?.handouts;
    if (!handouts) return [];
    if (Array.isArray(handouts.models)) return handouts.models;
    if (typeof handouts.toArray === "function") return handouts.toArray();
    if (typeof handouts.map === "function") {
        const mapped = [];
        handouts.map(handout => mapped.push(handout));
        return mapped;
    }
    return [];
}

function hasEternityHandout() {
    return collectHandouts().some(handout => {
        const name = String(readAttr(handout, "name") || "").trim().toLowerCase();
        return name === ETERNITY_HANDOUT_NAME;
    });
}

const BRIDGE_MARKER = "<!--eternity-bridge:";
const BRIDGE_END = "-->";
const NARRATOR_ONLINE_MS = 45000;
let clientRole = "";
let narratorHeartbeatTimer = null;
let watchingBridgeSignals = false;
let lastHandledSignalId = "";
let pendingNarratorAckId = "";

function postToJava(payload) {
    window.postMessage({
        source: "roll20-page-bridge",
        payload: payload
    }, "*");
}

function findEternityHandout() {
    return collectHandouts().find(handout => {
        const name = String(readAttr(handout, "name") || "").trim().toLowerCase();
        return name === ETERNITY_HANDOUT_NAME;
    }) || null;
}

function parseBridgeState(notes) {
    const text = String(notes || "");
    const start = text.indexOf(BRIDGE_MARKER);
    if (start < 0) {
        return { narratorOnline: 0, signal: null };
    }
    const end = text.indexOf(BRIDGE_END, start);
    if (end < 0) {
        return { narratorOnline: 0, signal: null };
    }
    try {
        const parsed = JSON.parse(text.slice(start + BRIDGE_MARKER.length, end));
        return {
            narratorOnline: Number(parsed && parsed.narratorOnline) || 0,
            signal: parsed && parsed.signal ? parsed.signal : null
        };
    } catch (error) {
        return { narratorOnline: 0, signal: null };
    }
}

function applyBridgeState(notes, bridge) {
    const text = String(notes || "");
    const block = BRIDGE_MARKER + JSON.stringify(bridge || { narratorOnline: 0, signal: null }) + BRIDGE_END;
    const start = text.indexOf(BRIDGE_MARKER);
    if (start < 0) {
        return text + (text && !text.endsWith("\n") ? "\n" : "") + block;
    }
    const end = text.indexOf(BRIDGE_END, start);
    if (end < 0) {
        return text + block;
    }
    return text.slice(0, start) + block + text.slice(end + BRIDGE_END.length);
}

function readHandoutNotes(handout, callback) {
    if (!handout) {
        callback("");
        return;
    }
    try {
        if (typeof handout.get === "function") {
            handout.get("notes", notes => callback(String(notes || "")));
            return;
        }
    } catch (error) {
        console.error("handout notes read failed", error);
    }
    callback(String(readAttr(handout, "notes") || ""));
}

function writeHandoutNotes(handout, notes) {
    if (!handout) return;
    try {
        if (typeof handout.save === "function") {
            handout.save({ notes: notes });
            return;
        }
        if (typeof handout.set === "function") {
            handout.set("notes", notes);
        }
    } catch (error) {
        console.error("handout notes write failed", error);
    }
}

function updateBridgeState(mutator, callback) {
    const handout = findEternityHandout();
    if (!handout) {
        if (callback) callback(null);
        return;
    }
    readHandoutNotes(handout, notes => {
        const bridge = parseBridgeState(notes);
        const next = mutator(bridge) || bridge;
        writeHandoutNotes(handout, applyBridgeState(notes, next));
        if (callback) callback(next);
    });
}

function isNarratorOnline(bridge) {
    return !!bridge && (Date.now() - Number(bridge.narratorOnline || 0) <= NARRATOR_ONLINE_MS);
}

function setClientRole(role) {
    clientRole = String(role || "").trim().toLowerCase();
    if (clientRole === "narrator") {
        publishNarratorPresence();
        startNarratorHeartbeat();
        watchBridgeSignals();
        return;
    }
    stopNarratorHeartbeat();
    if (clientRole === "player") {
        watchBridgeSignals();
        return;
    }
    updateBridgeState(bridge => {
        bridge.narratorOnline = 0;
        return bridge;
    });
}

function publishNarratorPresence() {
    updateBridgeState(bridge => {
        bridge.narratorOnline = Date.now();
        return bridge;
    });
}

function startNarratorHeartbeat() {
    if (narratorHeartbeatTimer) return;
    narratorHeartbeatTimer = setInterval(() => {
        if (clientRole === "narrator") {
            publishNarratorPresence();
        }
    }, 15000);
}

function stopNarratorHeartbeat() {
    if (!narratorHeartbeatTimer) return;
    clearInterval(narratorHeartbeatTimer);
    narratorHeartbeatTimer = null;
}

function watchBridgeSignals() {
    if (watchingBridgeSignals) return;
    watchingBridgeSignals = true;
    const handout = findEternityHandout();
    if (handout) {
        readHandoutNotes(handout, notes => {
            const signal = parseBridgeState(notes).signal;
            if (signal && signal.id) {
                lastHandledSignalId = signal.id;
            }
        });
    }
    const poll = () => {
        if (clientRole !== "narrator" && clientRole !== "player") return;
        const currentHandout = findEternityHandout();
        if (!currentHandout) return;
        readHandoutNotes(currentHandout, notes => {
            const signal = parseBridgeState(notes).signal;
            if (!signal || !signal.id || signal.id === lastHandledSignalId) return;
            if (signal.type === "player-attacking" && !signal.ack && clientRole === "narrator") {
                lastHandledSignalId = signal.id;
                postToJava({ type: "player-attacking", signalId: signal.id });
                return;
            }
            if (clientRole === "player"
                    && (signal.type === "combat-start"
                        || signal.type === "combat-end"
                        || signal.type === "combat-new-round")) {
                lastHandledSignalId = signal.id;
                postToJava({ type: signal.type, signalId: signal.id });
            }
        });
    };
    if (handout && typeof handout.on === "function") {
        handout.on("change:notes", poll);
    }
    setInterval(poll, 400);
}

function notifyNarratorAttack() {
    const id = Date.now() + "-" + Math.random().toString(36).slice(2, 8);
    if (clientRole === "narrator") {
        postToJava({ type: "player-attacking", signalId: id });
        return;
    }
    const sent = sendToRoll20Chat("/w gm [eternity-narrator-prompt] " + id);
    if (!sent) {
        postToJava({ type: "narrator-absent" });
        return;
    }
    pendingNarratorAckId = id;
    waitForNarratorChatAck(id);
}

function waitForNarratorAck(handout, signalId) {
    let finished = false;
    const finish = () => {
        if (finished) return;
        finished = true;
        postToJava({ type: "narrator-notified" });
    };
    const timer = setTimeout(finish, 20000);
    const check = () => {
        if (finished) return;
        readHandoutNotes(handout, notes => {
            if (finished) return;
            const signal = parseBridgeState(notes).signal;
            if (signal && signal.id === signalId && signal.ack) {
                clearTimeout(timer);
                finish();
            }
        });
    };
    if (handout && typeof handout.on === "function") {
        handout.on("change:notes", check);
    }
    const poll = setInterval(() => {
        if (finished) {
            clearInterval(poll);
            return;
        }
        check();
    }, 250);
}

function ackNarratorAttack(signalId) {
    if (clientRole === "narrator") {
        postToJava({ type: "narrator-notified" });
    }
    if (!signalId) return;
    sendToRoll20Chat("/w gm [eternity-narrator-ack] " + signalId);
    updateBridgeState(bridge => {
        if (bridge.signal && bridge.signal.id === signalId) {
            bridge.signal.ack = true;
        }
        return bridge;
    });
}

function reportEternityCampaign(enabled) {
    const next = !!enabled;
    if (eternityCampaignEnabled === next) {
        return;
    }
    eternityCampaignEnabled = next;
    console.log("Eternity campaign handout:", eternityCampaignEnabled);
    window.postMessage({
        source: "roll20-page-bridge",
        payload: {
            type: "eternity-campaign",
            enabled: eternityCampaignEnabled
        }
    }, "*");
    if (eternityCampaignEnabled) {
        sendTokenSnapshot();
        watchTokenChanges();
        watchChatForAttacks();
        if (clientRole === "narrator") {
            publishNarratorPresence();
            startNarratorHeartbeat();
            watchBridgeSignals();
        } else if (clientRole === "player") {
            watchBridgeSignals();
        }
    }
}

function watchHandoutChanges() {
    if (watchingHandouts) return;
    const handouts = getCampaign()?.handouts;
    if (!handouts || typeof handouts.on !== "function") return;
    watchingHandouts = true;

    const refresh = () => reportEternityCampaign(hasEternityHandout());
    handouts.on("add", function (handout) {
        if (handout && typeof handout.on === "function" && !handout._eternityNameWatch) {
            handout._eternityNameWatch = true;
            handout.on("change:name", refresh);
        }
        refresh();
    });
    handouts.on("remove", refresh);
    handouts.on("change:name", refresh);
    collectHandouts().forEach(handout => {
        if (handout && typeof handout.on === "function" && !handout._eternityNameWatch) {
            handout._eternityNameWatch = true;
            handout.on("change:name", refresh);
        }
    });
}

let watchingChat = false;
const reportedAttackMessageIds = new Set();
const pendingAttackMessages = new WeakSet();
const ATTACK_RESPONSE_DELAY_MS = 500;

function findChatRoot() {
    return document.getElementById("textchat")
        || document.querySelector("#textchat")
        || document.querySelector("#textchat-input")?.parentElement
        || null;
}

function messageKey(message) {
    if (!message) return "";
    return message.getAttribute?.("data-messageid")
        || message.id
        || "";
}

function parseEternityAttack(message) {
    if (!message) return null;
    const html = message.innerHTML || "";
    const text = message.textContent || "";
    const isAttackCard = /ATK Roll:|DMG Roll:/.test(text)
        || (/\bAttack:/.test(text) && /\bDamage:/.test(text))
        || /\bApplication:/.test(text);
    if (!isAttackCard) return null;
    const targetName = parseAttackTargetName(text);
    if (!targetName) return null;
    const vsDefense = parseAttackVs(text);
    const attackValue = parseAttackValue(text);
    if (/^(AC|DODGE|ARMOR)$/i.test(vsDefense) && (attackValue == null || Number.isNaN(attackValue))) {
        return null;
    }
    return {
        targetId: "",
        targetName: targetName,
        vsDefense: vsDefense,
        attackValue: attackValue,
        damageAmount: parseDamageValue(text),
        damageType: parseDamageType(text),
        crush: parseCrushValue(text),
        criticalDamage: parseCriticalDamage(text)
    };
}

function parseAttackValue(text) {
    const match = String(text || "").match(/(?:^|[\n\r]|[\s>])Attack:\s*([0-9]+(?:\.[0-9]+)?)/);
    return match ? Number(match[1]) : null;
}

function parseDamageValue(text) {
    const match = String(text || "").match(/(?:^|[\n\r]|[\s>])Damage:\s*([0-9]+(?:\.[0-9]+)?)/);
    return match ? Number(match[1]) : null;
}

function parseDamageType(text) {
    const match = String(text || "").match(/(?:^|[\n\r]|[\s>])Damage Type:\s*([A-Z][A-Z0-9]*)/i);
    return match ? String(match[1] || "").trim().toUpperCase() : "";
}

function parseCrushValue(text) {
    const match = String(text || "").match(/(?:^|[\n\r]|[\s>])Crush:\s*([0-9]+(?:\.[0-9]+)?)/);
    return match ? Number(match[1]) : null;
}

function parseCriticalDamage(text) {
    const match = String(text || "").match(/(?:^|[\n\r]|[\s>])Critical Damage:\s*(-?[0-9]+(?:\.[0-9]+)?)/i);
    return match ? Number(match[1]) : null;
}

function parseAttackVs(text) {
    const match = String(text || "").match(/(?:^|[\n\r]|[\s>])vs\s+([A-Z][A-Z0-9 /]*)/i);
    return match ? String(match[1] || "").trim() : "";
}

function parseAttackTargetName(text) {
    const line = text.match(/(?:^|[\n\r])\s*Target:\s+([^\n\r]+)/);
    const inline = text.match(/(?<!Enemy )Target:\s+(.+?)(?=\s*(?:ATK Roll:|DMG Roll:|Attack:|Application:|Additional Attacks:|$))/);
    const raw = (line && line[1]) || (inline && inline[1]) || "";
    return String(raw).replace(/\[eternity-attack[\s\S]*$/i, "").trim();
}

function looksLikeIncomingAttack(message) {
    const text = message && message.textContent ? message.textContent : "";
    if (!/Target:/.test(text)) return false;
    return /ATK Roll:|DMG Roll:/.test(text)
        || (/\bAttack:/.test(text) && /\bDamage:/.test(text))
        || /\bApplication:/.test(text)
        || /\bvs\s+/i.test(text);
}

function considerNarratorPromptMessage(message) {
    const text = message && message.textContent ? message.textContent : "";
    const prompt = text.match(/\[eternity-narrator-prompt\]\s*(\S+)/i);
    if (prompt && clientRole === "narrator") {
        const signalId = prompt[1];
        if (signalId && signalId !== lastHandledSignalId) {
            lastHandledSignalId = signalId;
            postToJava({ type: "player-attacking", signalId: signalId });
        }
        return true;
    }
    const ack = text.match(/\[eternity-narrator-ack\]\s*(\S+)/i);
    if (ack && pendingNarratorAckId && ack[1] === pendingNarratorAckId) {
        pendingNarratorAckId = "";
        postToJava({ type: "narrator-notified" });
        return true;
    }
    return false;
}

function waitForNarratorChatAck(signalId) {
    setTimeout(() => {
        if (pendingNarratorAckId === signalId) {
            pendingNarratorAckId = "";
            postToJava({ type: "narrator-notified" });
        }
    }, 20000);
}

function considerChatNode(node) {
    if (!node || node.nodeType !== 1) return;
    const message = (typeof node.closest === "function" && node.closest(".message")) || node;
    if (considerNarratorPromptMessage(message)) return;
    if (!looksLikeIncomingAttack(message) && !parseEternityAttack(message)) return;
    if (pendingAttackMessages.has(message)) return;

    const id = messageKey(message);
    if (id && reportedAttackMessageIds.has(id)) return;

    pendingAttackMessages.add(message);
    setTimeout(() => {
        reportIncomingAttack(message);
    }, ATTACK_RESPONSE_DELAY_MS);
}

function reportIncomingAttack(message) {
    const parsed = parseEternityAttack(message);
    if (!parsed || (!parsed.targetId && !parsed.targetName)) return;

    const id = messageKey(message);
    const key = id || (parsed.targetId + "|" + parsed.targetName + "|" + (message.textContent || "").slice(0, 80));
    if (reportedAttackMessageIds.has(key)) return;
    reportedAttackMessageIds.add(key);
    if (reportedAttackMessageIds.size > 64) {
        const first = reportedAttackMessageIds.values().next().value;
        reportedAttackMessageIds.delete(first);
    }

    console.log("Incoming Eternity attack:", parsed);
    window.postMessage({
        source: "roll20-page-bridge",
        payload: {
            type: "incoming-attack",
            targetId: parsed.targetId,
            targetName: parsed.targetName,
            vsDefense: parsed.vsDefense,
            attackValue: parsed.attackValue,
            damageAmount: parsed.damageAmount,
            damageType: parsed.damageType,
            crush: parsed.crush,
            criticalDamage: parsed.criticalDamage,
            messageId: key
        }
    }, "*");
}

function watchChatForAttacks() {
    if (watchingChat) return;
    const root = findChatRoot();
    if (!root) {
        setTimeout(watchChatForAttacks, 1000);
        return;
    }
    watchingChat = true;
    console.log("Watching Roll20 chat for incoming attacks");
    const observer = new MutationObserver((mutations) => {
        for (const mutation of mutations) {
            for (const node of mutation.addedNodes) {
                considerChatNode(node);
            }
            if (mutation.target && mutation.target.nodeType === 1) {
                considerChatNode(mutation.target);
            }
        }
    });
    observer.observe(root, { childList: true, subtree: true });
}

function waitForEternityCampaign(attempt = 0) {
    if (hasEternityHandout()) {
        watchHandoutChanges();
        reportEternityCampaign(true);
        return;
    }

    if (getCampaign()?.handouts || isRoll20Ready()) {
        watchHandoutChanges();
    }

    if (attempt >= 120) {
        console.log("No Eternity handout found; bridge will stay idle");
        reportEternityCampaign(false);
        return;
    }

    if (attempt === 0 || attempt % 10 === 0) {
        console.log("Looking for Eternity handout...", attempt);
    }
    setTimeout(() => waitForEternityCampaign(attempt + 1), 1000);
}

waitForEternityCampaign();
