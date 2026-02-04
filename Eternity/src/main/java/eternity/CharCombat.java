package eternity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lightweight combat subsystem for a character.
 * Tracks encounter state and temporary combat statuses (round/turn scoped).
 */
public class CharCombat {

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

    public CharCombat() {
        this.inCombat = false;
        this.round = 0;
        this.initiative = 0;
        this.combatStatus = new ArrayList<>();
        this.standardActions = new ArrayList<>();
        this.moveActions = new ArrayList<>();
        this.auraActions = new ArrayList<>();
        this.freeActions = new ArrayList<>();
        this.interruptActions = new ArrayList<>();

        // Baseline action every character has
        DataAction baseAttack = new DataAction();
        baseAttack.setName("Standard Attack");
        baseAttack.setType("Standard");
        baseAttack.setAffinity("None");
        baseAttack.setRanged(0);
        baseAttack.setActionType("Standard");
        this.standardActions.add(baseAttack);

        // Baseline combat maneuver: Grapple
        DataAction grapple = new DataAction();
        grapple.setName("Grapple");
        grapple.setType("Combat Maneuver"); // matches UI category
        grapple.setAffinity("None");
        grapple.setRanged(0); // melee
        grapple.setActionType("Standard");
        this.standardActions.add(grapple);
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

    /* Status handling */
    public List<DataStatus> getCombatStatus() { return combatStatus; }

    public List<DataAction> getStandardActions() { return generateActions(standardActions); }
    public List<DataAction> getMoveActions() { return generateActions(moveActions); }
    public List<DataAction> getAuraActions() { return generateActions(auraActions); }
    public List<DataAction> getFreeActions() { return generateActions(freeActions); }
    public List<DataAction> getInterruptActions() { return generateActions(interruptActions); }

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

    /** Updates the baseline Standard Attack range (used by UI weapon selection). */
    public void updateStandardAttackRange(int range) {
        for (DataAction action : standardActions) {
            if (action != null && "Standard Attack".equalsIgnoreCase(action.getName())) {
                action.setRanged(range);
            }
        }
    }

    /** Returns freshly generated action instances for the given bucket. */
    private List<DataAction> generateActions(List<DataAction> bucket) {
        List<DataAction> generated = new ArrayList<>();
        if (bucket == null) return generated;
        for (DataAction action : bucket) {
            generated.add(new DataAction(action));
        }
        return generated;
    }
}
