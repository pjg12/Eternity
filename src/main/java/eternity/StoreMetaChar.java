// CHECKED

package eternity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lightweight character summary used by the load screen
 */
public class StoreMetaChar {
    @JsonProperty private int index;
    @JsonProperty private String name;
    @JsonProperty private String campaign;
    @JsonProperty private String race;
    @JsonProperty private String charClass;
    @JsonProperty private int level;
    @JsonProperty private Timestamp updated;

    // --- Constructors ---

    public StoreMetaChar() { this(0, "", "", "", "", 1, new Timestamp(System.currentTimeMillis())); }

    public StoreMetaChar(int index, String name, String campaign, String race, String charClass, int level, Timestamp updated) {
        this.index = index;
        this.name = name;
        this.campaign = campaign;
        this.race = race;
        this.charClass = charClass;
        this.level = level;
        this.updated = updated;
    }

    // --- Getters / Setters ---

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }
    
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
    
    public String getCharClass() { return charClass; }
    public void setCharClass(String charClass) { this.charClass = charClass; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public Timestamp getUpdated() { return updated; }
    public void setUpdated(Timestamp updated) { this.updated = updated; }
    public void setUpdated() { this.updated = new Timestamp(System.currentTimeMillis()); }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StoreMetaChar other = (StoreMetaChar) obj;
        return index == other.index;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(index);
    }

    @Override
    public String toString() {
        return "StoreMetaChar {\n" + "  index: " + index + ",\n" + "  name: \"" + name + "\",\n" + "  campaign: \"" + campaign + "\",\n" +
            "  race: \"" + race + "\",\n" + "  charClass: \"" + charClass + "\",\n" + "  level: " + level + ",\n" + "  updated: " +
            (updated != null ? updated.toString() : "null") + ",\n" + "}";
    }
}
