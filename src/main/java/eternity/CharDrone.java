package eternity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CharDrone {
    public static final String TYPE_ATTACK = "Attack";
    public static final String TYPE_DEFENSE = "Defense";
    public static final String STATE_ACTIVE = "Active";
    public static final String STATE_RETURNED = "Returned";

    private String type = TYPE_ATTACK;
    private String target = "Yourself";
    private String state = STATE_ACTIVE;
    private boolean canRedeploy;

    public CharDrone() {}

    public CharDrone(String type, String target) {
        this.type = type;
        this.target = target;
        this.state = STATE_ACTIVE;
        this.canRedeploy = false;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public boolean isCanRedeploy() { return canRedeploy; }
    public void setCanRedeploy(boolean canRedeploy) { this.canRedeploy = canRedeploy; }

    public boolean isActive() {
        return STATE_ACTIVE.equalsIgnoreCase(state);
    }

    public boolean isReturned() {
        return STATE_RETURNED.equalsIgnoreCase(state);
    }

    public boolean isAttack() {
        return TYPE_ATTACK.equalsIgnoreCase(type);
    }

    public boolean isDefense() {
        return TYPE_DEFENSE.equalsIgnoreCase(type);
    }
}
