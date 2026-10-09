package websocket.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Roll20Token {
    public String id;
    public String name;
    public String characterName;
    public String layer;
    public Double left;
    public Double top;
    public Double width;
    public Double height;
    public String represents;
    public String controlledby;
    public Boolean controlledByMe;
    public Boolean selected;

    @JsonIgnore
    public transient boolean loadedCharacter;

    @JsonIgnore
    public boolean isAlly() {
        return controlledby != null && !controlledby.isBlank();
    }

    @JsonIgnore
    public boolean isEnemy() {
        return !isAlly();
    }

    @Override
    public String toString() {
        return name == null || name.isBlank()
            ? id
            : name;
    }
}
