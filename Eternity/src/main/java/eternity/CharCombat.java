package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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


    @JsonIgnore
    private CharData owner;

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
        this.standardSpellAction = generateStandardAttack(false);
        this.standardActions.add(standardAttackAction);

        for (String name : DEFAULT_COMBAT_MANEUVER_NAMES) {
            DataAction maneuver = generateCombatManeuverAction(name);
            defaultCombatManeuverActions.add(maneuver);
            this.standardActions.add(maneuver);
        }
    }

    /* Owner plumbing */
    public void setOwner(CharData owner) { this.owner = owner; }
    @JsonIgnore public CharData getOwner() { return owner; }

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
        ensureDefaultCombatManeuversExist();
    }

    /**
     * Rebuilds runtime combat action buckets from the character's current derived state.
     * These actions are not persisted; they are regenerated whenever the character updates.
     */
    public void rebuildActions(CharData character) {
        if (character != null) {
            this.owner = character;
        }

        clearActions();
        ensureStandardAttackExists();
        ensureDefaultCombatManeuversExist();

        if (owner == null || owner.getTraining() == null) {
            return;
        }

        DataQuery dataQuery = CharDataManager.getDataQuery();
        ArrayList<DataTraining> trainedTechniques = new ArrayList<>();
        for (DataTraining tech : owner.getTraining().getAllTraining()) {
            if (!hasTrainedActionTechnique(tech)) continue;
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

    private boolean hasTrainedActionTechnique(DataTraining tech) {
        if (tech == null || tech.getId() <= 0) return false;
        return tech.getRank() >= 1;
    }

    private void addTrainingActionFromData(DataTraining tech, DataQuery dataQuery) {
        if (tech == null || dataQuery == null) return;
        DataAction actionTemplate = dataQuery.getActionById(tech.getId());
        if (actionTemplate == null) return;

        DataAction action = new DataAction(actionTemplate);
        action.setCharacter(owner);
        routeActionByType(action);
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
        baseAttack.setActionType("Standard");
        if (attack) {
            baseAttack.setName("Standard Attack");
            baseAttack.setCategory("Attack");
            baseAttack.setSource("Standard");
            baseAttack.setAtkType("AC");
        } else {
            baseAttack.setName("Standard Spell");
            baseAttack.setSource("Spell");
        }
        return baseAttack;
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
        maneuver.setActionType("Standard");
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

    /** Ensures all baseline Combat Maneuver actions exist exactly once in the standard-action list. */
    public void ensureDefaultCombatManeuversExist() {
        for (DataAction maneuver : defaultCombatManeuverActions) {
            if (maneuver == null) continue;
            maneuver.setCharacter(owner);
            boolean present = false;
            for (DataAction action : standardActions) {
                if (action == maneuver) {
                    present = true;
                    break;
                }
                if (action != null && action.getName() != null
                        && action.getName().equalsIgnoreCase(maneuver.getName())
                        && "Combat Maneuver".equalsIgnoreCase(action.getSource())) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                standardActions.add(maneuver);
            }
        }
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
