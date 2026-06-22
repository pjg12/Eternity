package websocket.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Roll20Token {
    public String id;
    public String name;
    public String layer;
    public double left;
    public double top;
    public double width;
    public double height;
    public String represents;
    public String controlledby;

    @Override
    public String toString() {
        return name == null || name.isBlank()
            ? id
            : name;
    }
}
