package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lightweight combat subsystem for a character.
 * Tracks encounter state and temporary combat statuses (round/turn scoped).
 */
public class CharCombat {
    private static final String[] DEFAULT_COMBAT_MANEUVER_NAMES = {
            "Grapple", "Charge", "Rush", "Disarm", "Overrun", "Sunder", "Trip", "Feint", "Brace", "Protect"
    };
    private static final String MOVE_ACTION_TYPE = "Move";
    private static final String STANDARD_ACTION_TYPE = "Standard";


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
    private double critDamage = 2.0; // damage multiplier on crits

    @JsonProperty
    private boolean invade;

    @JsonProperty
    private int reach;

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
    private final List<DataAction> defaultCombatManeuverActions;

    public CharCombat() {
        this.inCombat = false;
        this.round = 0;
        this.initiative = 0;
        this.invade = false;
        this.reach = 0;
        this.combatStatus = new ArrayList<>();
        this.standardActions = new ArrayList<>();
        this.moveActions = new ArrayList<>();
        this.auraActions = new ArrayList<>();
        this.freeActions = new ArrayList<>();
        this.interruptActions = new ArrayList<>();
        this.defaultCombatManeuverActions = new ArrayList<>();

        // Baseline action every character has
        this.standardAttackAction = generateStandardAttack(true);
        this.standardMoveAction = generateStandardMoveAction();
        this.standardSpellAction = generateStandardAttack(false);
        this.standardActions.add(standardAttackAction);
        this.moveActions.add(standardMoveAction);

        for (String name : DEFAULT_COMBAT_MANEUVER_NAMES) {
            DataAction maneuver = generateCombatManeuverAction(name);
            defaultCombatManeuverActions.add(maneuver);
            routeActionByType(maneuver);
        }
    }

    /* Owner plumbing */
    public void setOwner(StoreCharData owner) { this.owner = owner; }
    @JsonIgnore public StoreCharData getOwner() { return owner; }

    /* State flags */
    public boolean isInCombat() { return inCombat; }
    public void startCombat() { inCombat = true; round = Math.max(round, 1); }
    public void endCombat() {
        inCombat = false;
        round = 0;
        combatStatus.clear();
    }

    /* Rounds / initiative */
    public int getRound() { return round; }
    public void nextRound() { if (inCombat) round++; }
    public int getInitiative() { return initiative; }
    public void setInitiative(int initiative) { this.initiative = initiative; }

    public boolean isShowInitiativePopup() { return showInitiativePopup; }
    public void setShowInitiativePopup(boolean showInitiativePopup) { this.showInitiativePopup = showInitiativePopup; }

    public double getCritDamage() { return critDamage; }
    public void setCritDamage(double critDamage) { this.critDamage = critDamage; }
    public boolean isInvade() { return invade; }
    public void setInvade(boolean invade) { this.invade = invade; }
    public int getReach() { return reach; }
    public void setReach(int reach) { this.reach = Math.max(0, reach); }

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
    public void addInterruptAction(DataAction action) { addAction(action, interruptActions); }

    public void clearActions() {
        standardActions.clear();
        moveActions.clear();
        auraActions.clear();
        freeActions.clear();
        interruptActions.clear();
        ensureStandardAttackExists();
        ensureStandardMoveExists();
        ensureDefaultCombatManeuversExist();
    }

    /**
     * Rebuilds runtime combat action buckets from the character's current derived state.
     * These actions are not persisted; they are regenerated whenever the character updates.
     */
    public void rebuildActions(StoreCharData character) {
        if (character != null) {
            this.owner = character;
        }

        clearActions();
        ensureStandardAttackExists();
        ensureStandardMoveExists();
        ensureDefaultCombatManeuversExist();
        syncBaselineActionsFromCharacter();

        if (owner == null || owner.getTraining() == null) {
            return;
        }

        StoreRuleManager dataQuery = new StoreRuleManager();
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
        if (!"Active".equalsIgnoreCase(tech.getType())) return false;
        return !getTrainingActionTemplates(tech, dataQuery).isEmpty();
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
        }
    }

    private List<DataAction> getTrainingActionTemplates(DataTraining tech, StoreRuleManager dataQuery) {
        ArrayList<DataAction> actionTemplates = new ArrayList<>();
        if (tech == null || dataQuery == null) return actionTemplates;

        DataTraining ruleTemplate = resolveTrainingRuleTemplate(tech, dataQuery);
        LinkedHashSet<Integer> actionIds = new LinkedHashSet<>();
        collectGrantedActionIds(actionIds, ruleTemplate);
        collectGrantedActionIds(actionIds, tech);

        for (Integer actionId : actionIds) {
            if (actionId == null || actionId <= 0) continue;
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
        addStandardAction(action);
    }

    /** Builds the baseline Standard Attack action. */
    public DataAction generateStandardAttack(boolean attack) {
        DataAction baseAttack = new DataAction();
        baseAttack.setCharacter(owner);
        baseAttack.setAffinity("None");
        baseAttack.setActionType(STANDARD_ACTION_TYPE);
        if (attack) {
            baseAttack.setName("Standard Attack");
            baseAttack.setCategory("Attack");
            baseAttack.setSource("Standard");
            baseAttack.setAtkType("AC");
        } else {
            baseAttack.setName("Standard Cast");
            baseAttack.setSource("Spell");
        }
        return baseAttack;
    }

    /** Builds the baseline Move action. */
    public DataAction generateStandardMoveAction() {
        DataAction moveAction = new DataAction();
        moveAction.setCharacter(owner);
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
        return moveAction;
    }

    /** Builds a baseline Combat Maneuver action. */
    public DataAction generateCombatManeuverAction(String name) {
        DataAction maneuver = new DataAction();
        maneuver.setCharacter(owner);
        maneuver.setName(name == null || name.isBlank() ? "Combat Maneuver" : name);
        maneuver.setSource("Combat Maneuver");
        maneuver.setAffinity("None");
        maneuver.setAtkType("AC");
        maneuver.setRanged(0);
        maneuver.setActionType(isChargeManeuver(name) ? MOVE_ACTION_TYPE : STANDARD_ACTION_TYPE);
        return maneuver;
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

    /** Ensures all baseline Combat Maneuver actions exist exactly once in the standard-action list. */
    public void ensureDefaultCombatManeuversExist() {
        for (DataAction maneuver : defaultCombatManeuverActions) {
            if (maneuver == null) continue;
            maneuver.setCharacter(owner);
            if (!hasCombatManeuverAction(maneuver)) {
                routeActionByType(maneuver);
            }
        }
    }

    private boolean hasCombatManeuverAction(DataAction maneuver) {
        return containsMatchingCombatManeuver(standardActions, maneuver)
                || containsMatchingCombatManeuver(moveActions, maneuver)
                || containsMatchingCombatManeuver(auraActions, maneuver)
                || containsMatchingCombatManeuver(freeActions, maneuver)
                || containsMatchingCombatManeuver(interruptActions, maneuver);
    }

    private boolean containsMatchingCombatManeuver(List<DataAction> bucket, DataAction maneuver) {
        if (bucket == null || maneuver == null) return false;
        for (DataAction action : bucket) {
            if (action == maneuver) {
                return true;
            }
            if (action != null && action.getName() != null
                    && action.getName().equalsIgnoreCase(maneuver.getName())
                    && "Combat Maneuver".equalsIgnoreCase(action.getSource())) {
                return true;
            }
        }
        return false;
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
