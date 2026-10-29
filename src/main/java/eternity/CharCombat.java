package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eternity.DataTypes.DataAction;
import eternity.DataTypes.DataSpecialty;
import eternity.DataTypes.DataStatus;
import eternity.DataTypes.DataTraining;

/**
 * Lightweight combat subsystem for a character.
 * Tracks encounter state and temporary combat statuses (round/turn scoped).
 */
public class CharCombat {
    private static final int BASE_ACTION_ID_MAX_EXCLUSIVE = 200;
    private static final String MOVE_ACTION_TYPE = "Move";
    private static final String STANDARD_ACTION_TYPE = "Standard";
    private static final String STANDARD_ACTION_SOURCE = "Standard";
    private static final String COMBAT_MANEUVER_ACTION_SOURCE = "Combat Maneuver";
    private static final String CRUSADER_SEAL_SPECIALTY = "Crusader Seal";
    private static final String CRUSADER_SEAL_ACTION = "Crusader Seal";
    private static final String UNLEASH_CRUSADER_SEAL_ACTION = "Unleash Crusader Seal";
    private static final String CRUSADER_SEAL_MARKER_ATTRIBUTE = "CRUSADERSEAL";
    private static final String STEALTH_STRIKE_SPECIALTY = "Stealth Strike";
    private static final String STILL_MIND_SPECIALTY = "Still Mind";
    private static final String CLASH_SPECIALTY = "Clash";
    private static final String HOLY_BEACON_SPECIALTY = "Holy Beacon";
    private static final String EMBED_SPECIALTY = "Embed";
    private static final String SALVO_SPECIALTY = "Salvo";
    private static final String CHARGE_UP_SPECIALTY = "Charge-Up";
    private static final String DRONES_SPECIALTY = "Drones";
    private static final String POSITIONING_JETS_SPECIALTY = "Positioning Jets";
    private static final String OVERCHARGE_SPECIALTY = "Overcharge";
    private static final String DEPLOY_DRONE_ACTION = "Deploy Drone";
    private static final String RECALL_DRONE_ACTION = "Recall Drone";
    private static final String COUNTERSTRIKE_SPECIALTY = "Counterstrike";
    private static final String ARCANE_MISSILES_SPECIALTY = "Arcane Missiles";
    private static final String MIGHTY_KICK_SPECIALTY = "Mighty Kick";
    private static final String MIGHTY_KICK_ATTACK_ACTION = "Mighty Kick (Attack)";
    private static final String MIGHTY_KICK_SPELL_ACTION = "Mighty Kick (Spell)";
    private static final String ANGER_STRIKE_ACTION = "Anger Strike";
    private static final String CALM_STRIKE_ACTION = "Calm Strike";
    private static final String ORDER_STRIKE_ACTION = "Order Strike";
    private static final String CHAOS_STRIKE_ACTION = "Chaos Strike";
    private static final String FLASH_MOB_ACTION = "Flash Mob";
    private static final String SHIVER_ACTION = "Shiver";
    private static final String INSPIRE_ACTION = "Inspire";
    private static final String INSPIRE_I_SPECIALTY = "Inspire I";
    private static final String INSPIRE_II_SPECIALTY = "Inspire II";
    private static final String GAMBIT_SPECIALTY = "Gambit";
    private static final String GAMBIT_MINION_ACTION = "Gambit (Minion)";
    private static final String GAMBIT_LEADER_ACTION = "Gambit (Leader)";
    private static final String[] ADAPTIVE_TECHNIQUE_KEYS = {"DEF", "DODGE", "FORT", "REF", "WILL"};
    private static final int STILL_MIND_ACTION_ID = -7001;
    private static final int CLASH_ACTION_ID = -7002;
    private static final int HOLY_BEACON_ACTION_ID = -7003;
    private static final int EMBED_ACTION_ID = -7004;
    private static final int SALVO_ACTION_ID = -7005;
    private static final int CHARGE_UP_ACTION_ID = -7010;
    private static final int DEPLOY_DRONE_ACTION_ID = -7011;
    private static final int RECALL_DRONE_ACTION_ID = -7012;
    private static final int POSITIONING_JETS_ACTION_ID = -7013;
    private static final int OVERCHARGE_ACTION_ID = -7014;
    private static final int COUNTERSTRIKE_ACTION_ID = -7006;
    private static final int ARCANE_MISSILES_ACTION_ID = -7007;
    private static final int MIGHTY_KICK_ATTACK_ACTION_ID = -7008;
    private static final int MIGHTY_KICK_SPELL_ACTION_ID = -7009;


    @JsonIgnore
    private StoreCharData owner;

    @JsonProperty
    private boolean inCombat;

    @JsonProperty
    private int round;

    @JsonProperty
    private int initiative;

    @JsonProperty
    private boolean showInitiativePopup = true;

    @JsonProperty
    private boolean showStillMindPopup = true;

    @JsonProperty
    private double critDamage = 2.0; // damage multiplier on crits

    @JsonProperty
    private boolean invade;

    @JsonProperty
    private int reach;

    @JsonProperty
    private int stunTokens;

    @JsonProperty
    private int heavyTokens;

    @JsonProperty
    private int incapacitateTokens;

    @JsonProperty
    private int rootTokens;

    @JsonProperty
    private double damageDealtThisTurn;

    @JsonProperty
    private double shufflePool;
    @JsonProperty
    private double streamValue;
    @JsonProperty
    private double auraSpentThisTurn;
    @JsonProperty
    private int breakerComboAttacksThisTurn;
    @JsonProperty
    private Map<String, Integer> adaptiveTechniqueBonuses;
    @JsonProperty
    private boolean overchargeActiveThisTurn;
    @JsonProperty
    private boolean temperanceAbilityUsedThisTurn;
    @JsonProperty
    private boolean tacticsAbilityUsedThisTurn;
    @JsonProperty
    private boolean flashMobPendingRefund;
    @JsonProperty
    private int classResource3Overflow;

    @JsonProperty
    private final List<DataStatus> combatStatus;

    @JsonIgnore
    private final List<DataAction> standardActions;
    @JsonIgnore
    private final List<DataAction> moveActions;
    @JsonIgnore
    private final List<DataAction> auraActions;
    @JsonIgnore
    private final List<DataAction> freeActions;
    @JsonIgnore
    private final List<DataAction> interruptActions;
    @JsonIgnore
    private final DataAction standardAttackAction;
    @JsonIgnore
    private final DataAction standardMoveAction;
    private final DataAction standardSpellAction;
    @JsonIgnore
    private final List<DataAction> defaultBaseActions;

    public CharCombat() {
        this.inCombat = false;
        this.round = 0;
        this.initiative = 0;
        this.invade = false;
        this.reach = 0;
        this.stunTokens = 0;
        this.heavyTokens = 0;
        this.incapacitateTokens = 0;
        this.rootTokens = 0;
        this.damageDealtThisTurn = 0.0;
        this.shufflePool = 0.0;
        this.streamValue = 0.0;
        this.auraSpentThisTurn = 0.0;
        this.breakerComboAttacksThisTurn = 0;
        this.adaptiveTechniqueBonuses = new LinkedHashMap<>();
        this.overchargeActiveThisTurn = false;
        this.temperanceAbilityUsedThisTurn = false;
        this.tacticsAbilityUsedThisTurn = false;
        this.flashMobPendingRefund = false;
        this.classResource3Overflow = 0;
        this.combatStatus = new ArrayList<>();
        this.standardActions = new ArrayList<>();
        this.moveActions = new ArrayList<>();
        this.auraActions = new ArrayList<>();
        this.freeActions = new ArrayList<>();
        this.interruptActions = new ArrayList<>();
        this.defaultBaseActions = new ArrayList<>();

        StoreRuleManager dataQuery = new StoreRuleManager();

        // Baseline action every character has
        this.standardAttackAction = generateStandardAttack(dataQuery, true);
        this.standardMoveAction = generateStandardMoveAction(dataQuery);
        this.standardSpellAction = generateStandardAttack(dataQuery, false);
        refreshBaselineActionTemplates(dataQuery);
        clearActions();
    }

    /* Owner plumbing */
    public void setOwner(StoreCharData owner) { this.owner = owner; }
    @JsonIgnore public StoreCharData getOwner() { return owner; }

    /* State flags */
    public boolean isInCombat() { return inCombat; }
    public void startCombat() {
        inCombat = true;
        round = Math.max(round, 1);
        damageDealtThisTurn = 0.0;
        breakerComboAttacksThisTurn = 0;
        streamValue = 0.0;
        auraSpentThisTurn = 0.0;
        clearAdaptiveTechniqueBonuses();
        overchargeActiveThisTurn = false;
        temperanceAbilityUsedThisTurn = false;
        tacticsAbilityUsedThisTurn = false;
        flashMobPendingRefund = false;
        classResource3Overflow = 0;
    }
    public void endCombat() {
        inCombat = false;
        round = 0;
        stunTokens = 0;
        heavyTokens = 0;
        incapacitateTokens = 0;
        rootTokens = 0;
        damageDealtThisTurn = 0.0;
        shufflePool = 0.0;
        streamValue = 0.0;
        auraSpentThisTurn = 0.0;
        breakerComboAttacksThisTurn = 0;
        clearAdaptiveTechniqueBonuses();
        overchargeActiveThisTurn = false;
        temperanceAbilityUsedThisTurn = false;
        tacticsAbilityUsedThisTurn = false;
        flashMobPendingRefund = false;
        classResource3Overflow = 0;
        combatStatus.clear();
    }

    /* Rounds / initiative */
    public int getRound() { return round; }
    public void nextRound() { if (inCombat) round++; }
    public int getInitiative() { return initiative; }
    public void setInitiative(int initiative) { this.initiative = initiative; }

    public boolean isShowInitiativePopup() { return showInitiativePopup; }
    public void setShowInitiativePopup(boolean showInitiativePopup) { this.showInitiativePopup = showInitiativePopup; }
    public boolean isShowStillMindPopup() { return showStillMindPopup; }
    public void setShowStillMindPopup(boolean showStillMindPopup) { this.showStillMindPopup = showStillMindPopup; }

    public double getCritDamage() { return critDamage; }
    public void setCritDamage(double critDamage) { this.critDamage = critDamage; }
    public boolean isInvade() { return invade; }
    public void setInvade(boolean invade) { this.invade = invade; }
    public int getReach() { return reach; }
    public void setReach(int reach) { this.reach = Math.max(0, reach); }
    public int getStunTokens() { return stunTokens; }
    public void setStunTokens(int stunTokens) { this.stunTokens = Math.max(0, stunTokens); }
    public void adjustStunTokens(int delta) { setStunTokens(this.stunTokens + delta); }
    public boolean consumeStunToken() {
        if (stunTokens <= 0) return false;
        stunTokens--;
        return true;
    }
    public int getHeavyTokens() { return heavyTokens; }
    public void setHeavyTokens(int heavyTokens) { this.heavyTokens = Math.max(0, heavyTokens); }
    public void adjustHeavyTokens(int delta) { setHeavyTokens(this.heavyTokens + delta); }
    public boolean consumeHeavyToken() {
        if (heavyTokens <= 0) return false;
        heavyTokens--;
        return true;
    }
    public int getIncapacitateTokens() { return incapacitateTokens; }
    public void setIncapacitateTokens(int incapacitateTokens) { this.incapacitateTokens = Math.max(0, incapacitateTokens); }
    public void adjustIncapacitateTokens(int delta) { setIncapacitateTokens(this.incapacitateTokens + delta); }
    public boolean consumeIncapacitateToken() {
        if (incapacitateTokens <= 0) return false;
        incapacitateTokens--;
        return true;
    }
    public int getRootTokens() { return rootTokens; }
    public void setRootTokens(int rootTokens) { this.rootTokens = Math.max(0, rootTokens); }
    public void adjustRootTokens(int delta) { setRootTokens(this.rootTokens + delta); }
    public double getDamageDealtThisTurn() { return damageDealtThisTurn; }
    public void setDamageDealtThisTurn(double damageDealtThisTurn) { this.damageDealtThisTurn = Math.max(0.0, damageDealtThisTurn); }
    public void addDamageDealtThisTurn(double damageDealt) {
        if (damageDealt <= 0.0) return;
        setDamageDealtThisTurn(this.damageDealtThisTurn + damageDealt);
    }
    public void resetDamageDealtThisTurn() { this.damageDealtThisTurn = 0.0; }
    public double getShufflePool() { return Math.max(0.0, shufflePool); }
    public void setShufflePool(double shufflePool) { this.shufflePool = Math.max(0.0, shufflePool); }
    public void adjustShufflePool(double delta) { setShufflePool(this.shufflePool + delta); }
    public double getStreamValue() { return Math.max(0.0, streamValue); }
    public void setStreamValue(double streamValue) { this.streamValue = Math.max(0.0, streamValue); }
    public void adjustStreamValue(double delta) { setStreamValue(this.streamValue + delta); }
    public double getAuraSpentThisTurn() { return Math.max(0.0, auraSpentThisTurn); }
    public void setAuraSpentThisTurn(double auraSpentThisTurn) { this.auraSpentThisTurn = Math.max(0.0, auraSpentThisTurn); }
    public void adjustAuraSpentThisTurn(double delta) { setAuraSpentThisTurn(this.auraSpentThisTurn + delta); }
    public void resetAuraSpentThisTurn() { this.auraSpentThisTurn = 0.0; }
    public int getBreakerComboAttacksThisTurn() { return Math.max(0, breakerComboAttacksThisTurn); }
    public void setBreakerComboAttacksThisTurn(int breakerComboAttacksThisTurn) {
        this.breakerComboAttacksThisTurn = Math.max(0, breakerComboAttacksThisTurn);
    }
    public void incrementBreakerComboAttacksThisTurn() {
        setBreakerComboAttacksThisTurn(this.breakerComboAttacksThisTurn + 1);
    }
    public void resetBreakerComboAttacksThisTurn() { this.breakerComboAttacksThisTurn = 0; }
    public Map<String, Integer> getAdaptiveTechniqueBonuses() {
        ensureAdaptiveTechniqueBonusMap();
        return adaptiveTechniqueBonuses;
    }
    public void setAdaptiveTechniqueBonuses(Map<String, Integer> adaptiveTechniqueBonuses) {
        ensureAdaptiveTechniqueBonusMap();
        this.adaptiveTechniqueBonuses.clear();
        if (adaptiveTechniqueBonuses == null) return;
        for (Map.Entry<String, Integer> entry : adaptiveTechniqueBonuses.entrySet()) {
            if (entry == null) continue;
            String normalizedKey = normalizeAdaptiveTechniqueKey(entry.getKey());
            if (normalizedKey == null) continue;
            int value = entry.getValue() == null ? 0 : Math.max(0, entry.getValue());
            if (value > 0) {
                this.adaptiveTechniqueBonuses.put(normalizedKey, value);
            }
        }
    }
    public int getAdaptiveTechniqueBonus(String attribute) {
        ensureAdaptiveTechniqueBonusMap();
        String normalizedKey = normalizeAdaptiveTechniqueKey(attribute);
        if (normalizedKey == null) return 0;
        return Math.max(0, adaptiveTechniqueBonuses.getOrDefault(normalizedKey, 0));
    }
    public void adjustAdaptiveTechniqueBonus(String attribute, int delta) {
        if (delta == 0) return;
        ensureAdaptiveTechniqueBonusMap();
        String normalizedKey = normalizeAdaptiveTechniqueKey(attribute);
        if (normalizedKey == null) return;
        int updatedValue = Math.max(0, getAdaptiveTechniqueBonus(normalizedKey) + delta);
        if (updatedValue <= 0) {
            adaptiveTechniqueBonuses.remove(normalizedKey);
            return;
        }
        adaptiveTechniqueBonuses.put(normalizedKey, updatedValue);
    }
    public void clearAdaptiveTechniqueBonuses() {
        ensureAdaptiveTechniqueBonusMap();
        adaptiveTechniqueBonuses.clear();
    }
    public boolean isOverchargeActiveThisTurn() { return overchargeActiveThisTurn; }
    public void setOverchargeActiveThisTurn(boolean overchargeActiveThisTurn) {
        this.overchargeActiveThisTurn = overchargeActiveThisTurn;
    }
    public boolean isTemperanceAbilityUsedThisTurn() { return temperanceAbilityUsedThisTurn; }
    public void setTemperanceAbilityUsedThisTurn(boolean temperanceAbilityUsedThisTurn) {
        this.temperanceAbilityUsedThisTurn = temperanceAbilityUsedThisTurn;
    }
    public boolean isTacticsAbilityUsedThisTurn() { return tacticsAbilityUsedThisTurn; }
    public void setTacticsAbilityUsedThisTurn(boolean tacticsAbilityUsedThisTurn) {
        this.tacticsAbilityUsedThisTurn = tacticsAbilityUsedThisTurn;
    }
    public boolean isFlashMobPendingRefund() { return flashMobPendingRefund; }
    public void setFlashMobPendingRefund(boolean flashMobPendingRefund) {
        this.flashMobPendingRefund = flashMobPendingRefund;
    }
    public int getClassResource3Overflow() { return Math.max(0, classResource3Overflow); }
    public void setClassResource3Overflow(int classResource3Overflow) {
        this.classResource3Overflow = Math.max(0, classResource3Overflow);
    }
    public void adjustClassResource3Overflow(int delta) {
        setClassResource3Overflow(getClassResource3Overflow() + delta);
    }

    /* Status handling */
    public List<DataStatus> getCombatStatus() { return combatStatus; }

    public List<DataAction> getStandardActions() { return getActionView(standardActions); }
    public List<DataAction> getMoveActions() { return getActionView(moveActions); }
    public List<DataAction> getAuraActions() { return getActionView(auraActions); }
    public List<DataAction> getFreeActions() { return getActionView(freeActions); }
    public List<DataAction> getInterruptActions() { return getActionView(interruptActions); }
    public DataAction getStandardAttackAction() { return standardAttackAction; }
    public DataAction getStandardSpellAction() { return standardSpellAction; }

    public void addStandardAction(DataAction action) { addAction(action, standardActions); }
    public void addMoveAction(DataAction action) { addAction(action, moveActions); }
    public void addAuraAction(DataAction action) { addAction(action, auraActions); }
    public void addFreeAction(DataAction action) { addAction(action, freeActions); }
    public void addInterruptAction(DataAction action) {
        ensureInterruptReactionCost(action);
        addAction(action, interruptActions);
    }

    public void clearActions() {
        clearActionBuckets();
        ensureStandardAttackExists();
        ensureStandardSpellExists();
        ensureStandardMoveExists();
        ensureDefaultBaseActionsExist();
    }

    /** Clears combat action buckets without reinstalling leader baselines. */
    public void clearActionBuckets() {
        standardActions.clear();
        moveActions.clear();
        auraActions.clear();
        freeActions.clear();
        interruptActions.clear();
    }

    /** Routes a copy of the action into the matching combat bucket. */
    public void routeAction(DataAction action) {
        if (action == null) return;
        routeActionByType(new DataAction(action));
    }

    /**
     * Rebuilds runtime combat action buckets from the character's current derived state.
     * These actions are not persisted; they are regenerated whenever the character updates.
     */
    public void rebuildActions(StoreCharData character) {
        if (character != null) {
            this.owner = character;
        }

        StoreRuleManager dataQuery = new StoreRuleManager();
        refreshBaselineActionTemplates(dataQuery);
        clearActions();
        ensureStandardAttackExists();
        ensureStandardSpellExists();
        ensureStandardMoveExists();
        ensureDefaultBaseActionsExist();
        syncBaselineActionsFromCharacter();

        if (owner == null || owner.getTraining() == null) {
            return;
        }
        ArrayList<DataTraining> trainedTechniques = new ArrayList<>();
        for (DataTraining tech : owner.getTraining().getAllTraining()) {
            if (!hasTrainedActionTechnique(tech, dataQuery)) continue;
            trainedTechniques.add(tech);
        }

        trainedTechniques.sort((a, b) -> {
            int idCompare = Integer.compare(a.getId(), b.getId());
            if (idCompare != 0) return idCompare;
            String nameA = a.getName() == null ? "" : a.getName();
            String nameB = b.getName() == null ? "" : b.getName();
            return nameA.compareToIgnoreCase(nameB);
        });

        for (DataTraining tech : trainedTechniques) {
            addTrainingActionFromData(tech, dataQuery);
        }

        addSpecialtyActionsFromData(dataQuery);
    }

    private void syncBaselineActionsFromCharacter() {
        if (owner == null || owner.getAttributes() == null) return;
        CharAttributes attrs = owner.getAttributes();

        standardAttackAction.setAtk(roundDerivedCombatStat(attrs, "ATK"));
        standardAttackAction.setBdmg(roundDerivedCombatStat(attrs, "BDMG"));
        standardAttackAction.setTdmg(roundDerivedCombatStat(attrs, "TDMG"));

        standardSpellAction.setAtk(roundDerivedCombatStat(attrs, "APP"));
        standardSpellAction.setBdmg(roundDerivedCombatStat(attrs, "BDMG"));
        standardSpellAction.setTdmg(roundDerivedCombatStat(attrs, "TDMG"));
    }

    private int roundDerivedCombatStat(CharAttributes attrs, String key) {
        if (attrs == null || key == null) return 0;
        return (int) Math.round(Math.max(0.0, attrs.calcStatusValue(key)));
    }

    public void addStatus(DataStatus status) {
        if (status == null) return;
        // Avoid sharing mutable objects between characters
        DataStatus clone = new DataStatus(status);
        combatStatus.add(clone);
    }

    public void removeStatus(String name) {
        if (name == null) return;
        combatStatus.removeIf(s -> name.equalsIgnoreCase(s.getName()));
    }

    public boolean hasCombatStatusAttribute(String attribute) {
        if (attribute == null || attribute.isBlank()) return false;
        for (DataStatus status : combatStatus) {
            if (status == null || status.getAttribute() == null) continue;
            if (attribute.equalsIgnoreCase(status.getAttribute().trim())) {
                return true;
            }
        }
        return false;
    }

    /** Decrement durations for statuses that tick each turn; remove expired. */
    public void tickTurn() {
        tickByType("Turn");
    }

    /** Decrement durations for statuses that tick each round; remove expired. */
    public void tickRound() {
        tickByType("Round");
    }

    /** Decrement durations for statuses that tick each cycle; remove expired. */
    public void tickCycle() {
        tickByType("Cycle");
    }

    private void tickByType(String type) {
        Iterator<DataStatus> it = combatStatus.iterator();
        while (it.hasNext()) {
            DataStatus s = it.next();
            if (s.getDurationType() == null) continue;
            if (s.getDurationType().equalsIgnoreCase(type)) {
                s.setDuration(s.getDuration() - 1);
                if (s.getDuration() <= 0) {
                    it.remove();
                }
            }
        }
    }

    private void addAction(DataAction action, List<DataAction> bucket) {
        if (action == null || bucket == null) return;
        // Defensive copy to avoid shared mutations
        bucket.add(new DataAction(action));
    }

    private boolean hasTrainedActionTechnique(DataTraining tech, StoreRuleManager dataQuery) {
        if (tech == null || dataQuery == null) return false;
        if (tech.getRank() < 1) return false;
        if ("Active".equalsIgnoreCase(tech.getType())) {
            return !getTrainingActionTemplates(tech, dataQuery).isEmpty();
        }
        return "Maintained".equalsIgnoreCase(tech.getType())
                && tech.getAl() > 0
                && !getTrainingActionTemplates(tech, dataQuery).isEmpty();
    }

    private void addTrainingActionFromData(DataTraining tech, StoreRuleManager dataQuery) {
        if (tech == null || dataQuery == null) return;
        List<DataAction> actionTemplates = getTrainingActionTemplates(tech, dataQuery);
        if (actionTemplates.isEmpty()) return;

        for (DataAction actionTemplate : actionTemplates) {
            if (actionTemplate == null) continue;
            DataAction action = new DataAction(actionTemplate);
            action.setCharacter(owner);
            routeActionByType(action);
            DataAction interruptVariant = buildDualInterruptVariant(actionTemplate);
            if (interruptVariant != null) {
                interruptVariant.setCharacter(owner);
                routeActionByType(interruptVariant);
            }
        }
    }

    private void addSpecialtyActionsFromData(StoreRuleManager dataQuery) {
        if (owner == null || owner.getSpecials() == null || dataQuery == null) return;
        for (DataSpecialty specialty : owner.getSpecials().getAllSpecialties()) {
            if (specialty == null) continue;
            for (DataAction actionTemplate : getSpecialtyActionTemplates(specialty, dataQuery)) {
                if (actionTemplate == null || hasBaseAction(actionTemplate)) continue;
                // Gambit (Minion) belongs on summoned minion actors only.
                if (GAMBIT_MINION_ACTION.equalsIgnoreCase(actionTemplate.getName())) continue;
                DataAction action = new DataAction(actionTemplate);
                action.setCharacter(owner);
                routeActionByType(action);
                DataAction interruptVariant = buildDualInterruptVariant(actionTemplate);
                if (interruptVariant != null) {
                    interruptVariant.setCharacter(owner);
                    routeActionByType(interruptVariant);
                }
            }
        }
    }

    private List<DataAction> getTrainingActionTemplates(DataTraining tech, StoreRuleManager dataQuery) {
        ArrayList<DataAction> actionTemplates = new ArrayList<>();
        if (tech == null || dataQuery == null) return actionTemplates;

        DataTraining ruleTemplate = resolveTrainingRuleTemplate(tech, dataQuery);
        LinkedHashSet<Integer> actionIds = new LinkedHashSet<>();
        collectGrantedActionIds(actionIds, ruleTemplate);
        collectGrantedActionIds(actionIds, tech);

        boolean maintained = "Maintained".equalsIgnoreCase(tech.getType());
        for (Integer actionId : actionIds) {
            if (actionId == null || actionId <= 0) continue;
            if (maintained && dataQuery.getTechPermById(actionId) != null) continue;
            DataAction actionTemplate = dataQuery.getActionById(actionId);
            if (actionTemplate != null) {
                actionTemplates.add(actionTemplate);
            }
        }

        if (!actionTemplates.isEmpty()) {
            return actionTemplates;
        }

        addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(tech.getName()));
        if (ruleTemplate != null) {
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionById(ruleTemplate.getId()));
        }
        addActionTemplateIfPresent(actionTemplates, dataQuery.getActionById(tech.getId()));
        return actionTemplates;
    }

    private List<DataAction> getSpecialtyActionTemplates(DataSpecialty specialty, StoreRuleManager dataQuery) {
        ArrayList<DataAction> actionTemplates = new ArrayList<>();
        if (specialty == null || dataQuery == null) return actionTemplates;

        if (CRUSADER_SEAL_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            DataAction sealAction = hasCombatStatusAttribute(CRUSADER_SEAL_MARKER_ATTRIBUTE)
                    ? dataQuery.getActionByName(UNLEASH_CRUSADER_SEAL_ACTION)
                    : dataQuery.getActionByName(CRUSADER_SEAL_ACTION);
            addActionTemplateIfPresent(actionTemplates, sealAction);
            return actionTemplates;
        }
        if (STEALTH_STRIKE_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(STEALTH_STRIKE_SPECIALTY));
            return actionTemplates;
        }
        if (STILL_MIND_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildStillMindActionTemplate());
            return actionTemplates;
        }
        if (CLASH_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildClashActionTemplate());
            return actionTemplates;
        }
        if (HOLY_BEACON_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildHolyBeaconActionTemplate());
            return actionTemplates;
        }
        if (EMBED_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildEmbedActionTemplate());
            return actionTemplates;
        }
        if (SALVO_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildSalvoActionTemplate());
            return actionTemplates;
        }
        if (CHARGE_UP_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildChargeUpActionTemplate());
            return actionTemplates;
        }
        if (DRONES_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildDeployDroneActionTemplate());
            actionTemplates.add(buildRecallDroneActionTemplate());
            return actionTemplates;
        }
        if (POSITIONING_JETS_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildPositioningJetsActionTemplate());
            return actionTemplates;
        }
        if (OVERCHARGE_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildOverchargeActionTemplate());
            return actionTemplates;
        }
        if (COUNTERSTRIKE_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildCounterstrikeActionTemplate());
            return actionTemplates;
        }
        if (ARCANE_MISSILES_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildArcaneMissilesActionTemplate());
            return actionTemplates;
        }
        if (MIGHTY_KICK_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            actionTemplates.add(buildMightyKickAttackActionTemplate());
            actionTemplates.add(buildMightyKickSpellActionTemplate());
            return actionTemplates;
        }
        if (isGambitSpecialtyName(specialty.getName())) {
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(GAMBIT_LEADER_ACTION));
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(GAMBIT_MINION_ACTION));
            return actionTemplates;
        }

        DataSpecialty ruleTemplate = resolveSpecialtyRuleTemplate(specialty, dataQuery);
        collectGrantedActionTemplates(actionTemplates, specialty, dataQuery);
        collectGrantedActionTemplates(actionTemplates, ruleTemplate, dataQuery);
        collectGrantedSubtypeActionTemplates(actionTemplates, specialty, dataQuery);
        collectGrantedSubtypeActionTemplates(actionTemplates, ruleTemplate, dataQuery);

        addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(specialty.getName()));
        if (ruleTemplate != null) {
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(ruleTemplate.getName()));
        }
        if (isInspireSpecialtyName(specialty.getName())
                || (ruleTemplate != null && isInspireSpecialtyName(ruleTemplate.getName()))) {
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(INSPIRE_ACTION));
        }

        return actionTemplates;
    }

    private static boolean isInspireSpecialtyName(String name) {
        if (name == null || name.isBlank()) return false;
        String trimmed = name.trim();
        return INSPIRE_ACTION.equalsIgnoreCase(trimmed)
                || INSPIRE_I_SPECIALTY.equalsIgnoreCase(trimmed)
                || INSPIRE_II_SPECIALTY.equalsIgnoreCase(trimmed);
    }

    private static boolean isGambitSpecialtyName(String name) {
        if (name == null || name.isBlank()) return false;
        String trimmed = name.trim();
        return GAMBIT_SPECIALTY.equalsIgnoreCase(trimmed)
                || GAMBIT_MINION_ACTION.equalsIgnoreCase(trimmed)
                || GAMBIT_LEADER_ACTION.equalsIgnoreCase(trimmed);
    }

    private void collectGrantedSubtypeActionTemplates(List<DataAction> actionTemplates, DataSpecialty specialty, StoreRuleManager dataQuery) {
        if (actionTemplates == null || specialty == null || dataQuery == null || owner == null || owner.getSpecials() == null) return;
        for (DataSpecialty granted : owner.getSpecials().getGrantedSubtypeSpecialties(specialty)) {
            if (granted == null || granted.getName() == null || granted.getName().isBlank()) continue;
            // Gambit (Minion) is granted to summoned minions, not the leader.
            if (GAMBIT_MINION_ACTION.equalsIgnoreCase(granted.getName())) continue;
            addActionTemplateIfPresent(actionTemplates, dataQuery.getActionByName(granted.getName()));
            collectGrantedActionTemplates(actionTemplates, granted, dataQuery);
        }
    }

    private DataSpecialty resolveSpecialtyRuleTemplate(DataSpecialty specialty, StoreRuleManager dataQuery) {
        if (specialty == null || dataQuery == null) return null;
        if (specialty.getId() > 0) {
            DataSpecialty byId = dataQuery.getSpecialtyById(specialty.getId());
            if (byId != null) return byId;
        }
        String specialtyName = specialty.getName();
        if (specialtyName == null || specialtyName.isBlank()) return null;
        return dataQuery.getSpecialtyByName(specialtyName);
    }

    private void collectGrantedActionTemplates(List<DataAction> actionTemplates, DataSpecialty specialty, StoreRuleManager dataQuery) {
        if (actionTemplates == null || specialty == null || dataQuery == null) return;
        for (DataStatus status : specialty.getPermStatus()) {
            if (status == null || status.getAttribute() == null || status.getAttribute().isBlank()) continue;
            try {
                int actionId = Integer.parseInt(status.getAttribute().trim());
                if (actionId <= 0) continue;
                addActionTemplateIfPresent(actionTemplates, dataQuery.getActionById(actionId));
            } catch (NumberFormatException ignored) {
                // Not an action-id grant reference.
            }
        }
    }

    private DataTraining resolveTrainingRuleTemplate(DataTraining tech, StoreRuleManager dataQuery) {
        if (tech == null || dataQuery == null) return null;

        String techName = tech.getName();
        if (techName != null && !techName.isBlank()) {
            for (DataTraining candidate : dataQuery.getTrainingData()) {
                if (candidate != null && techName.equalsIgnoreCase(candidate.getName())) {
                    return candidate;
                }
            }
        }

        if (tech.getId() > 0) {
            return dataQuery.getTrainingById(tech.getId());
        }
        return null;
    }

    private void collectGrantedActionIds(LinkedHashSet<Integer> actionIds, DataTraining tech) {
        if (actionIds == null || tech == null) return;
        for (Integer grantId : tech.getGrant()) {
            if (grantId != null && grantId > 0) {
                actionIds.add(grantId);
            }
        }
    }

    private void addActionTemplateIfPresent(List<DataAction> actionTemplates, DataAction actionTemplate) {
        if (actionTemplates == null || actionTemplate == null) return;
        for (DataAction existing : actionTemplates) {
            if (existing != null && existing.getId() == actionTemplate.getId()) {
                return;
            }
        }
        actionTemplates.add(actionTemplate);
    }

    private DataAction buildStillMindActionTemplate() {
        DataAction action = new DataAction();
        action.setId(STILL_MIND_ACTION_ID);
        action.setName(STILL_MIND_SPECIALTY);
        action.setCategory("Other");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Interrupt");
        action.setCosts(List.of(new DataAction.CostPair("None", 0.0)));
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildClashActionTemplate() {
        DataAction action = new DataAction();
        action.setId(CLASH_ACTION_ID);
        action.setName(CLASH_SPECIALTY);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Interrupt");
        action.setCosts(List.of(
                new DataAction.CostPair("R2", 1.0),
                new DataAction.CostPair("REACT", 1.0)));
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildHolyBeaconActionTemplate() {
        DataAction action = new DataAction();
        action.setId(HOLY_BEACON_ACTION_ID);
        action.setName(HOLY_BEACON_SPECIALTY);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("Divine");
        action.setAtkType("OTHER");
        action.setActionType("Interrupt");
        action.setRanged(1);
        action.setCosts(List.of(new DataAction.CostPair("R1", 1.0)));
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildEmbedActionTemplate() {
        DataAction action = new DataAction();
        action.setId(EMBED_ACTION_ID);
        action.setName(EMBED_SPECIALTY);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Interrupt");
        action.setCosts(List.of(new DataAction.CostPair("R2", 1.0)));
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildSalvoActionTemplate() {
        DataAction action = new DataAction();
        action.setId(SALVO_ACTION_ID);
        action.setName(SALVO_SPECIALTY);
        action.setCategory("Attack");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Standard");
        action.setCosts(List.of());
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildChargeUpActionTemplate() {
        DataAction action = new DataAction();
        action.setId(CHARGE_UP_ACTION_ID);
        action.setName(CHARGE_UP_SPECIALTY);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Standard");
        action.setCosts(List.of());
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildDeployDroneActionTemplate() {
        DataAction action = new DataAction();
        action.setId(DEPLOY_DRONE_ACTION_ID);
        action.setName(DEPLOY_DRONE_ACTION);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Aura");
        action.setRanged(1);
        action.setCosts(List.of());
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildPositioningJetsActionTemplate() {
        DataAction action = new DataAction();
        action.setId(POSITIONING_JETS_ACTION_ID);
        action.setName(POSITIONING_JETS_SPECIALTY);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Interrupt");
        action.setCosts(List.of(new DataAction.CostPair("REACT", 1.0)));
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildOverchargeActionTemplate() {
        DataAction action = new DataAction();
        action.setId(OVERCHARGE_ACTION_ID);
        action.setName(OVERCHARGE_SPECIALTY);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Aura");
        action.setCosts(List.of());
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildRecallDroneActionTemplate() {
        DataAction action = new DataAction();
        action.setId(RECALL_DRONE_ACTION_ID);
        action.setName(RECALL_DRONE_ACTION);
        action.setCategory("Inform");
        action.setSource("Class");
        action.setAffinity("None");
        action.setAtkType("OTHER");
        action.setActionType("Free");
        action.setRanged(1);
        action.setCosts(List.of());
        action.setModifierKey(List.of());
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildCounterstrikeActionTemplate() {
        DataAction template = standardAttackAction == null ? new DataAction() : new DataAction(standardAttackAction);
        template.setId(COUNTERSTRIKE_ACTION_ID);
        template.setName(COUNTERSTRIKE_SPECIALTY);
        template.setCategory("Attack");
        template.setSource("Class");
        template.setActionType("Interrupt");
        template.setCosts(List.of(new DataAction.CostPair("REACT", 1.0)));
        template.setCharacter(owner);
        return template;
    }

    private DataAction buildArcaneMissilesActionTemplate() {
        DataAction template = standardSpellAction == null ? new DataAction() : new DataAction(standardSpellAction);
        template.setId(ARCANE_MISSILES_ACTION_ID);
        template.setName(ARCANE_MISSILES_SPECIALTY);
        template.setCategory("Attack");
        template.setSource("Class");
        template.setAffinity("Variable");
        template.setRanged(1);
        template.setActionType("Standard");

        int classLevel = owner != null && owner.getIdentity() != null
                ? Math.max(0, owner.getIdentity().getLevel())
                : 0;
        ArrayList<DataAction.CostPair> costs = new ArrayList<>();
        costs.add(new DataAction.CostPair("R1", 1.0));
        costs.add(new DataAction.CostPair("Aura", classLevel * classLevel));
        template.setCosts(costs);

        ArrayList<DataAction.ModifierKey> modifierKeys = new ArrayList<>();
        if (template.getModifierKey() != null) {
            for (DataAction.ModifierKey modifier : template.getModifierKey()) {
                if (modifier != null) {
                    modifierKeys.add(new DataAction.ModifierKey(modifier));
                }
            }
        }
        modifierKeys.add(new DataAction.ModifierKey("DMGMULTI", "+" + Double.toString(classLevel * 0.1)));
        template.setModifierKey(modifierKeys);
        template.setCharacter(owner);
        return template;
    }

    private DataAction buildMightyKickAttackActionTemplate() {
        DataAction template = standardAttackAction == null ? new DataAction() : new DataAction(standardAttackAction);
        template.setId(MIGHTY_KICK_ATTACK_ACTION_ID);
        template.setName(MIGHTY_KICK_ATTACK_ACTION);
        template.setCategory("Attack");
        template.setSource("Class");
        template.setAffinity("None");
        template.setAtkType("AC");
        template.setActionType(STANDARD_ACTION_TYPE);
        template.setCosts(List.of());
        template.setModifierKey(List.of());
        template.setCharacter(owner);
        return template;
    }

    private DataAction buildMightyKickSpellActionTemplate() {
        DataAction template = standardSpellAction == null ? new DataAction() : new DataAction(standardSpellAction);
        template.setId(MIGHTY_KICK_SPELL_ACTION_ID);
        template.setName(MIGHTY_KICK_SPELL_ACTION);
        template.setCategory("Attack");
        template.setSource("Class");
        template.setAffinity("None");
        template.setAtkType("FORT");
        template.setActionType(STANDARD_ACTION_TYPE);
        template.setCosts(List.of());
        template.setModifierKey(List.of());
        if (standardAttackAction != null) {
            template.setBdmg(standardAttackAction.getBdmg());
            template.setTdmg(standardAttackAction.getTdmg());
            template.setDmgMulti(standardAttackAction.getDmgMulti());
            template.setRanged(standardAttackAction.getRanged());
        }
        template.setCharacter(owner);
        return template;
    }

    private DataAction buildDualInterruptVariant(DataAction actionTemplate) {
        if (!isTemperanceAbilityAction(actionTemplate)
                && !isFlashMobAbilityAction(actionTemplate)
                && !isShiverAbilityAction(actionTemplate)
                && !isFreeOrInterruptMitigationAction(actionTemplate)) {
            return null;
        }
        // Avoid cloning an action that is already Interrupt-only.
        if (actionTemplate != null && "Interrupt".equalsIgnoreCase(actionTemplate.getActionType())) {
            return null;
        }
        DataAction interruptAction = new DataAction(actionTemplate);
        interruptAction.setActionType("Interrupt");
        return interruptAction;
    }

    /** Every interrupt spends 1 Reaction. Existing reaction costs are left as written. */
    private void ensureInterruptReactionCost(DataAction action) {
        if (action == null) return;
        List<DataAction.CostPair> costs = new ArrayList<>();
        for (DataAction.CostPair cost : action.getCosts()) {
            if (cost != null && cost.getType() != null) {
                String type = cost.getType().trim();
                if (type.equalsIgnoreCase("Reaction")
                        || type.equalsIgnoreCase("Reactions")
                        || type.equalsIgnoreCase("React")) {
                    return;
                }
            }
            if (cost != null) costs.add(new DataAction.CostPair(cost));
        }
        costs.add(new DataAction.CostPair("Reaction", 1.0));
        action.setCosts(costs);
    }

    /** Enhanced Shield/Shell (and affinity copies) are Free actions that may also interrupt. */
    private boolean isFreeOrInterruptMitigationAction(DataAction action) {
        if (action == null || action.getName() == null) return false;
        if (!"Mitigation".equalsIgnoreCase(action.getCategory())) return false;
        if (!"Free".equalsIgnoreCase(action.getActionType())) return false;
        String name = action.getName().toLowerCase(java.util.Locale.ROOT);
        return name.contains("shield") || name.contains("shell");
    }

    private boolean isFlashMobAbilityAction(DataAction action) {
        return action != null
                && action.getName() != null
                && FLASH_MOB_ACTION.equalsIgnoreCase(action.getName().trim());
    }

    private boolean isShiverAbilityAction(DataAction action) {
        return action != null
                && action.getName() != null
                && SHIVER_ACTION.equalsIgnoreCase(action.getName().trim());
    }

    private void routeActionByType(DataAction action) {
        if (action == null) return;
        String type = action.getActionType();
        if ("Move".equalsIgnoreCase(type)) {
            addMoveAction(action);
            return;
        }
        if ("Aura".equalsIgnoreCase(type)) {
            addAuraAction(action);
            return;
        }
        if ("Free".equalsIgnoreCase(type)) {
            addFreeAction(action);
            return;
        }
        if ("Interrupt".equalsIgnoreCase(type)) {
            addInterruptAction(action);
            return;
        }
        if ("Full".equalsIgnoreCase(type)) {
            addStandardAction(action);
            return;
        }
        addStandardAction(action);
    }

    private void refreshBaselineActionTemplates(StoreRuleManager dataQuery) {
        applyActionTemplate(standardAttackAction, generateStandardAttack(dataQuery, true));
        applyActionTemplate(standardMoveAction, generateStandardMoveAction(dataQuery));
        applyActionTemplate(standardSpellAction, generateStandardAttack(dataQuery, false));

        defaultBaseActions.clear();
        if (dataQuery == null) {
            return;
        }
        for (DataAction template : dataQuery.getActionData()) {
            if (template == null) continue;
            if (!isDefaultBaseActionTemplate(template)) continue;
            if (isCoreBaselineAction(template)) continue;
            DataAction action = new DataAction(template);
            action.setCharacter(owner);
            defaultBaseActions.add(action);
        }
    }

    private boolean isDefaultBaseActionTemplate(DataAction template) {
        if (template == null) return false;
        if (template.getId() <= 0 || template.getId() >= BASE_ACTION_ID_MAX_EXCLUSIVE) return false;
        String source = template.getSource();
        return STANDARD_ACTION_SOURCE.equalsIgnoreCase(source)
                || COMBAT_MANEUVER_ACTION_SOURCE.equalsIgnoreCase(source);
    }

    private boolean isCoreBaselineAction(DataAction action) {
        if (action == null) return false;
        int actionId = action.getId();
        return actionId == standardAttackAction.getId()
                || actionId == standardMoveAction.getId()
                || actionId == standardSpellAction.getId();
    }

    /** Builds the baseline Standard Attack action. */
    public DataAction generateStandardAttack(StoreRuleManager dataQuery, boolean attack) {
        DataAction baseAttack = attack
                ? buildActionByNameAndSource(dataQuery, "Standard Attack", "Standard")
                : buildRuleActionByAlias(dataQuery, new String[] {"Standard Spell", "Standard Cast"}, "Standard");
        if (baseAttack == null) {
            baseAttack = new DataAction();
            baseAttack.setName(attack ? "Standard Attack" : "Standard Spell");
            baseAttack.setAffinity("None");
            baseAttack.setActionType(STANDARD_ACTION_TYPE);
            if (attack) {
                baseAttack.setCategory("Attack");
                baseAttack.setSource("Standard");
                baseAttack.setAtkType("AC");
            } else {
                baseAttack.setCategory("Attack");
                baseAttack.setSource("Standard");
                baseAttack.setAtkType("Spell");
            }
        }
        baseAttack.setCharacter(owner);
        return baseAttack;
    }

    /** Builds the baseline Move action. */
    public DataAction generateStandardMoveAction(StoreRuleManager dataQuery) {
        DataAction moveAction = buildActionByNameAndSource(dataQuery, "Move", "Standard");
        if (moveAction == null) {
            moveAction = new DataAction();
            moveAction.setName("Move");
            moveAction.setCategory("");
            moveAction.setSource("Standard");
            moveAction.setAffinity("None");
            moveAction.setAtkType("Other");
            moveAction.setRanged(0);
            moveAction.setActionType(MOVE_ACTION_TYPE);
            moveAction.setWeapon(null);
            moveAction.setCosts(List.of(new DataAction.CostPair("None", 0.0)));
            moveAction.setModifierKey(List.of());
        }
        moveAction.setCharacter(owner);
        return moveAction;
    }

    /** Builds a baseline Combat Maneuver action. */
    public DataAction generateCombatManeuverAction(StoreRuleManager dataQuery, String name) {
        String actionName = name == null || name.isBlank() ? "Combat Maneuver" : name;
        DataAction maneuver = buildActionByNameAndSource(dataQuery, actionName, "Combat Maneuver");
        if (maneuver == null) {
            maneuver = new DataAction();
            maneuver.setName(actionName);
            maneuver.setSource("Combat Maneuver");
            maneuver.setAffinity("None");
            maneuver.setAtkType("AC");
            maneuver.setRanged(0);
            maneuver.setActionType(isChargeManeuver(name) ? MOVE_ACTION_TYPE : STANDARD_ACTION_TYPE);
        }
        if (isChargeManeuver(actionName)) {
            maneuver.setActionType(MOVE_ACTION_TYPE);
            maneuver.setCosts(List.of(new DataAction.CostPair("None", 0.0)));
        }
        maneuver.setCharacter(owner);
        return maneuver;
    }

    private DataAction buildRuleActionByName(StoreRuleManager dataQuery, String name) {
        if (dataQuery == null || name == null || name.isBlank()) return null;
        DataAction template = dataQuery.getActionByName(name);
        if (template == null) return null;
        DataAction action = new DataAction(template);
        action.setCharacter(owner);
        return action;
    }

    private DataAction buildRuleActionByAlias(StoreRuleManager dataQuery, String[] names, String source) {
        if (names == null) return null;
        for (String name : names) {
            DataAction action = buildActionByNameAndSource(dataQuery, name, source);
            if (action != null) {
                return action;
            }
        }
        for (String name : names) {
            DataAction action = buildRuleActionByName(dataQuery, name);
            if (action != null) {
                return action;
            }
        }
        return null;
    }

    private DataAction buildActionByNameAndSource(StoreRuleManager dataQuery, String name, String source) {
        if (dataQuery == null || name == null || name.isBlank()) return null;
        List<DataAction> sourcedActions = dataQuery.getActionsBySource(source);
        if (sourcedActions != null) {
            for (DataAction template : sourcedActions) {
                if (template != null && template.getName() != null && template.getName().equalsIgnoreCase(name)) {
                    DataAction action = new DataAction(template);
                    action.setCharacter(owner);
                    return action;
                }
            }
        }

        DataAction action = buildRuleActionByName(dataQuery, name);
        if (action != null && source != null && action.getSource() != null && !action.getSource().equalsIgnoreCase(source)) {
            return null;
        }
        return action;
    }

    private void applyActionTemplate(DataAction target, DataAction template) {
        if (target == null || template == null) return;
        target.setCharacter(owner);
        target.setId(template.getId());
        target.setName(template.getName());
        target.setCategory(template.getCategory());
        target.setSource(template.getSource());
        target.setAffinity(template.getAffinity());
        target.setAtkType(template.getAtkType());
        target.setAtk(template.getAtk());
        target.setBdmg(template.getBdmg());
        target.setTdmg(template.getTdmg());
        target.setDmgMulti(template.getDmgMulti());
        target.setAl(template.getAl());
        target.setRanged(template.getRanged());
        target.setActionType(template.getActionType());
        target.setWeapon(template.getWeapon());
        target.setCosts(template.getCosts());
        target.setModifierKey(template.getModifierKey());
    }

    /** Ensures the baseline Standard Attack action exists exactly once in the standard-action list. */
    public void ensureStandardAttackExists() {
        standardAttackAction.setCharacter(owner);
        for (DataAction action : standardActions) {
            if (action == standardAttackAction) {
                return;
            }
            if (action != null && "Standard Attack".equalsIgnoreCase(action.getName())) {
                return;
            }
        }
        standardActions.add(standardAttackAction);
    }

    /** Ensures the baseline Standard Spell action exists exactly once in the standard-action list. */
    public void ensureStandardSpellExists() {
        standardSpellAction.setCharacter(owner);
        for (DataAction action : standardActions) {
            if (action == standardSpellAction) {
                return;
            }
            if (action != null && action.getName() != null
                    && ("Standard Spell".equalsIgnoreCase(action.getName())
                    || "Standard Cast".equalsIgnoreCase(action.getName()))) {
                return;
            }
        }
        standardActions.add(standardSpellAction);
    }

    /** Ensures the baseline Move action exists exactly once in the move-action list. */
    public void ensureStandardMoveExists() {
        standardMoveAction.setCharacter(owner);
        for (DataAction action : moveActions) {
            if (action == standardMoveAction) {
                return;
            }
            if (action != null
                    && "Move".equalsIgnoreCase(action.getName())
                    && "Standard".equalsIgnoreCase(action.getSource())
                    && "Move".equalsIgnoreCase(action.getActionType())) {
                return;
            }
        }
        moveActions.add(standardMoveAction);
    }

    /** Ensures all id<200 rule actions exist exactly once in the runtime action buckets. */
    public void ensureDefaultBaseActionsExist() {
        for (DataAction action : defaultBaseActions) {
            if (action == null) continue;
            action.setCharacter(owner);
            if (!hasBaseAction(action)) {
                routeActionByType(action);
            }
        }
    }

    private boolean hasBaseAction(DataAction template) {
        return containsMatchingBaseAction(standardActions, template)
                || containsMatchingBaseAction(moveActions, template)
                || containsMatchingBaseAction(auraActions, template)
                || containsMatchingBaseAction(freeActions, template)
                || containsMatchingBaseAction(interruptActions, template);
    }

    private boolean containsMatchingBaseAction(List<DataAction> bucket, DataAction template) {
        if (bucket == null || template == null) return false;
        for (DataAction action : bucket) {
            if (action == template) {
                return true;
            }
            if (action == null) continue;
            if (action.getId() > 0 && template.getId() > 0 && action.getId() == template.getId()) {
                return true;
            }
            if (action.getName() != null && template.getName() != null
                    && action.getName().equalsIgnoreCase(template.getName())
                    && sameIgnoreCase(action.getSource(), template.getSource())
                    && sameIgnoreCase(action.getActionType(), template.getActionType())) {
                return true;
            }
        }
        return false;
    }

    private boolean sameIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.equalsIgnoreCase(right);
    }

    private boolean isTemperanceAbilityAction(DataAction action) {
        if (action == null || action.getName() == null) return false;
        String actionName = action.getName().trim();
        return ANGER_STRIKE_ACTION.equalsIgnoreCase(actionName)
                || CALM_STRIKE_ACTION.equalsIgnoreCase(actionName)
                || ORDER_STRIKE_ACTION.equalsIgnoreCase(actionName)
                || CHAOS_STRIKE_ACTION.equalsIgnoreCase(actionName);
    }

    private void ensureAdaptiveTechniqueBonusMap() {
        if (adaptiveTechniqueBonuses == null) {
            adaptiveTechniqueBonuses = new LinkedHashMap<>();
        }
    }

    private String normalizeAdaptiveTechniqueKey(String attribute) {
        if (attribute == null || attribute.isBlank()) return null;
        String normalized = attribute.trim().toUpperCase(Locale.ROOT);
        for (String allowedKey : ADAPTIVE_TECHNIQUE_KEYS) {
            if (allowedKey.equalsIgnoreCase(normalized)) {
                return allowedKey;
            }
        }
        return null;
    }

    private boolean isChargeManeuver(String name) {
        return name != null && "Charge".equalsIgnoreCase(name.trim());
    }

    /** Updates the baseline Standard Attack range (used by UI weapon selection). */
    public void updateStandardAttackRange(int range) {
        standardAttackAction.setRanged(range);
    }

    /** Returns a read-only view of the current action bucket. */
    private List<DataAction> getActionView(List<DataAction> bucket) {
        if (bucket == null) return List.of();
        return Collections.unmodifiableList(bucket);
    }
}
