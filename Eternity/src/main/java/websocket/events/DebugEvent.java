package websocket.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DebugEvent {
    public String type;
    public int bodyLength;
}
