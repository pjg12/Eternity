package eternity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps a weapon to the class specialty granted by Pilot Hand Abilities.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataExo {
    @JsonProperty private int id;
    @JsonProperty private String weapon;
    @JsonProperty private String type;
    @JsonProperty private int specialty;

    public DataExo() {
        this(0, "", "", 0);
    }

    public DataExo(DataExo src) {
        this(src == null ? 0 : src.id,
                src == null ? "" : src.weapon,
                src == null ? "" : src.type,
                src == null ? 0 : src.specialty);
    }

    public DataExo(int id, String weapon, String type, int specialty) {
        this.id = id;
        this.weapon = safe(weapon);
        this.type = safe(type);
        this.specialty = specialty;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWeapon() { return weapon; }
    public void setWeapon(String weapon) { this.weapon = safe(weapon); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public int getSpecialty() { return specialty; }
    public void setSpecialty(int specialty) { this.specialty = specialty; }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
