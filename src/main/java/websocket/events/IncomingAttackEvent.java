package websocket.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingAttackEvent {

    public String type;
    public String targetId;
    public String targetName;
    public String vsDefense;
    public Double attackValue;
    public Double damageAmount;
    public String damageType;
    public Double crush;
    public Double criticalDamage;
    public String messageId;

}
