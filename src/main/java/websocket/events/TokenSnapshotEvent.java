package websocket.events;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenSnapshotEvent {
    public String type;
    public int count;
    public Double pixelsPerSquare;
    public Double scaleNumber;
    public String scaleUnits;
    public List<Roll20Token> tokens;
}
