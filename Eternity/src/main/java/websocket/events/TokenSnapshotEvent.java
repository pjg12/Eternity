package websocket.events;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenSnapshotEvent {
    public String type;
    public int count;
    public List<Roll20Token> tokens;
}
