package eternity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic attribute data for a character.
 */
@JsonIgnoreProperties({
    "dataStore", "matchedLevel", "matchedRace",
    "matchedClass", "charRacial", "classSpecials"
})
public class CharAttributes {

    private static final String[] ATTRIBUTES = { "STR","DEX","CON","FOC","CTL","CAP","KNOW","MECH","PERC","INT","CHA","SUB" };
    private static final String[] DEFENSE = { "ARMOR", "DODGE", "DEF", "FORT", "REF", "WILL" };
    private static final String[] DMGTYPE = { "ALL", "PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME" };
    private static final String[] COMBAT = { "ATK", "MATK", "RATK", "DC", "MOVE", "FLY", "RANGE", "INIT" };
    private static final String[] SECONDARY = { "SUP", "IMP", "BEN", "EXCL", "MAXATK" };
    private static final String[] DAMAGE = { "BDMG", "BMDMG", "BRDMG", "TDMG", "TMDMG", "TRDMG", "BHEAL", "THEAL" };

    //  GENERIC STAT ARRAYS

    @JsonProperty("attributes") private StatBlock[] attributes;
    @JsonProperty("defense") private StatBlock[] defense;
    @JsonProperty("resist") private StatBlock[] resist;
    @JsonProperty("combat") private StatBlock[] combat;
    @JsonProperty("secondary") private StatBlock[] secondary;
    @JsonProperty("damage") private StatBlock[] damage;

    public CharAttributes() {
    	this.attributes = initCategory(ATTRIBUTES);
    	this.defense    = initCategory(DEFENSE);
    	this.resist     = initCategory(DMGTYPE);
    	this.combat     = initCategory(COMBAT);
    	this.secondary  = initCategory(SECONDARY);
        this.damage     = initCategory(DAMAGE);
    }

    // ---------------------------------------------------------
    //  CATEGORY INITIALIZATION
    // ---------------------------------------------------------

    private StatBlock[] initCategory(String[] categoryKeys) {
        StatBlock[] arr = new StatBlock[categoryKeys.length];

        for (int i = 0; i < arr.length; i++) {
            StatBlock block = new StatBlock();
            arr[i] = block;

            // BASE
            DataStatus base = new DataStatus();
            base.setName("Base");
            base.setAttribute(categoryKeys[i]);
            base.setDurationType("Permanent");
            block.addStatus(base);

            // ATTRIBUTE (if applicable)
            DataStatus attr = new DataStatus();
            attr.setName("Attribute");
            attr.setAttribute(categoryKeys[i]);
            attr.setDurationType("Permanent");
            block.addStatus(attr);
        }

        return arr;
    }

    // ---------------------------------------------------------
    //  UTILITY: INDEX MAPPING
    // ---------------------------------------------------------

    private int idx(String[] group, String key) {
        for (int i = 0; i < group.length; i++) if (group[i].equals(key)) return i; 
        return -1;
    }

    // ---------------------------------------------------------
    //  PUBLIC API: STATUS MODIFICATION
    // ---------------------------------------------------------

    public void addStatus(String category, String key, DataStatus status) {
        StatBlock block = getBlock(category, key);
        if (block != null) block.addStatus(status);
    }

    public void addMulti(String category, String key, DataStatus status) {
        StatBlock block = getBlock(category, key);
        if (block != null) block.addMulti(status);
    }

    public void removeStatus(String category, String key, String name) {
        StatBlock block = getBlock(category, key);
        if (block != null) block.removeStatus(name);
    }

    public void removeMulti(String category, String key, String name) {
        StatBlock block = getBlock(category, key);
        if (block != null) block.removeMulti(name);
    }

    // ---------------------------------------------------------
    //  PUBLIC API: VALUE RETRIEVAL
    // ---------------------------------------------------------

    public int getValue(String category, String key) {
        StatBlock block = getBlock(category, key);
        return block == null ? 0 : block.computeValue();
    }

    // ---------------------------------------------------------
    //  CATEGORY ACCESS
    // ---------------------------------------------------------

    private StatBlock getBlock(String category, String key) {
        switch (category.toLowerCase()) {
            case "attribute": return attributes[idx(ATTRIBUTES, key)];
            case "defense":   return defense[idx(DEFENSE, key)];
            case "resist":    return resist[idx(DMGTYPE, key)];
            case "combat":    return combat[idx(COMBAT, key)];
            case "secondary": return secondary[idx(SECONDARY, key)];
            case "damage":    return damage[idx(DAMAGE, key)];
            default: return null;
        }
    }

    // ---------------------------------------------------------
    //  DIRECT CATEGORY GETTERS
    // ---------------------------------------------------------

    public int getAttribute(String key) { return getValue("attribute", key); }
    public int getDefense(String key)   { return getValue("defense", key); }
    public int getResist(String key)    { return getValue("resist", key); }
    public int getCombat(String key)    { return getValue("combat", key); }
    public int getSecondary(String key) { return getValue("secondary", key); }
    public int getDamage(String key)    { return getValue("damage", key); }

    // ---------------------------------------------------------
    //  SERIALIZATION HELPERS
    // ---------------------------------------------------------

    @JsonProperty("attributes") public StatBlock[] getAttributes() { return attributes; }
    @JsonProperty("defense") public StatBlock[] getDefense() { return defense; }
    @JsonProperty("resist") public StatBlock[] getResist() { return resist; }
    @JsonProperty("combat") public StatBlock[] getCombat() { return combat; }
    @JsonProperty("secondary") public StatBlock[] getSecondary() { return secondary; }
    @JsonProperty("damage") public StatBlock[] getDamage() { return damage; }
}