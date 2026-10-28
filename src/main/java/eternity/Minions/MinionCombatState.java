package eternity.Minions;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eternity.CharCombat;
import eternity.DataAction;
import eternity.DataStatus;

/**
 * Independent combat state for one summoned minion actor.
 * Owns action buckets and combat statuses; does not share the Leader CharCombat.
 */
public class MinionCombatState {
    @JsonProperty private String slotTitle;
    @JsonIgnore private final CharCombat combat;
    /** Encounter-scoped damage taken; not mitigated further when applied via Life Link. */
    @JsonIgnore private double lostHp;

    public MinionCombatState() {
        this("");
    }

    public MinionCombatState(String slotTitle) {
        this.slotTitle = slotTitle == null ? "" : slotTitle.trim();
        this.combat = new CharCombat();
        this.combat.clearActions();
        this.lostHp = 0.0;
    }

    public String getSlotTitle() { return slotTitle; }
    public void setSlotTitle(String slotTitle) { this.slotTitle = slotTitle == null ? "" : slotTitle.trim(); }

    @JsonIgnore
    public CharCombat getCombat() { return combat; }

    @JsonIgnore
    public double getLostHp() { return Math.max(0.0, lostHp); }

    public void setLostHp(double lostHp) {
        this.lostHp = Math.max(0.0, lostHp);
    }

    public void addLostHp(double amount) {
        if (amount <= 0.0) return;
        this.lostHp = Math.max(0.0, this.lostHp + amount);
    }

    @JsonIgnore
    public List<DataStatus> getCombatStatus() {
        return combat == null ? List.of() : combat.getCombatStatus();
    }

    public void startCombat() {
        if (combat != null) combat.startCombat();
    }

    public void endCombat() {
        if (combat == null) return;
        combat.endCombat();
        combat.clearActions();
        lostHp = 0.0;
    }

    public void clearActions() {
        if (combat != null) combat.clearActions();
    }

    public void addStatus(DataStatus status) {
        if (combat != null) combat.addStatus(status);
    }

    public void removeStatus(String name) {
        if (combat != null) combat.removeStatus(name);
    }

    public List<DataAction> getStandardActions() {
        return combat == null ? List.of() : combat.getStandardActions();
    }

    public List<DataAction> getMoveActions() {
        return combat == null ? List.of() : combat.getMoveActions();
    }

    public List<DataAction> getAuraActions() {
        return combat == null ? List.of() : combat.getAuraActions();
    }

    public List<DataAction> getFreeActions() {
        return combat == null ? List.of() : combat.getFreeActions();
    }

    public List<DataAction> getInterruptActions() {
        return combat == null ? List.of() : combat.getInterruptActions();
    }

    public void addAction(DataAction action) {
        if (combat == null || action == null) return;
        combat.routeAction(action);
    }

    public void clearActionBuckets() {
        if (combat != null) combat.clearActionBuckets();
    }

    /** Tick turn-scoped statuses; remove expired. */
    public List<DataStatus> advanceTurnStatuses() {
        ArrayList<DataStatus> expired = new ArrayList<>();
        if (combat == null) return expired;
        List<DataStatus> statuses = combat.getCombatStatus();
        for (int i = 0; i < statuses.size(); i++) {
            DataStatus status = statuses.get(i);
            if (status == null || status.getDurationType() == null) continue;
            if (!"Turn".equalsIgnoreCase(status.getDurationType())) continue;
            status.setDuration(status.getDuration() - 1);
            if (status.getDuration() <= 0) {
                expired.add(status);
                statuses.remove(i);
                i--;
            }
        }
        return expired;
    }
}
