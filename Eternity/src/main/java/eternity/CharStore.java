package eternity;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lightweight character summary used by the load screen
 */
public class CharStore {
    @JsonProperty private int index;
    @JsonProperty private String name;
    @JsonProperty private String campaign;
    @JsonProperty private String race;
    @JsonProperty private String charClass;
    @JsonProperty private int level;
    @JsonProperty private Timestamp updated;
    @JsonProperty private String reference;

    // --- Constructors ---

    public CharStore() { this(0, "", "", "", "", 1, new Timestamp(System.currentTimeMillis()), ""); }
    //public CharStore(CharData character) { this( character.getIdentity().getIndex(), character.getIdentity().getName(), character.getIdentity().getCampaign(),
    //    character.getIdentity().getRace(), character.getIdentity().getClass, character.getIdentity().getLevel, character.getIdentity().getUpdated()); }

    public CharStore(int index, String name, String campaign, String race, String charClass, int level, Timestamp updated) {
        this(index, name, campaign, race, charClass, level, updated, "");
    }

    public CharStore(int index, String name, String campaign, String race, String charClass, int level, Timestamp updated, String reference) {
    	this.index = index;
        this.name = name;
        this.campaign = campaign;
        this.race = race;
        this.charClass = charClass;
        this.level = level;
        this.updated = updated;
        this.reference = reference == null ? "" : reference;
    }

    // --- Getters / Setters ---

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    
    public String getName() { return name; }

    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }
    
    public String getRace() { return race; }
    
    public String getCharClass() { return charClass; }
    
    public int getLevel() { return level; }
    
    public Timestamp getUpdated() { return updated; }
    public void update() { this.updated = new Timestamp(System.currentTimeMillis()); }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference == null ? "" : reference; }
    
    @Override
    public String toString() {
        return "CharStore {\n" + "  index: " + index + ",\n" + "  name: \"" + name + "\",\n" + "  campaign: \"" + campaign + "\",\n" +
            "  race: \"" + race + "\",\n" + "  charClass: \"" + charClass + "\",\n" + "  level: " + level + ",\n" + "  updated: " +
            (updated != null ? updated.toString() : "null") + ",\n" + "  reference: \"" + reference + "\"\n" + "}";
    }
}
