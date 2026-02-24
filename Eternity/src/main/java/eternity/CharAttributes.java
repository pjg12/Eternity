package eternity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
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
    @JsonIgnore
    private CharData owner;

    private static final String[] ATTRIBUTES = { "STR","DEX","CON","FOC","CTL","CAP","KNOW","MECH","PERC","INT","CHA","SUB" };
    private static final String[] DEFENSE = { "ARMOR", "DODGE", "DEF", "FORT", "REF", "WILL", "AVOID" };
    private static final String[] DMGTYPE = { "ALL", "PHY", "BLUNT", "PIERCE", "SLASH", "FIRE", "FROST", "ELEC", "ENERGY", "SONIC", "LIGHT", "TOXIC", "DARK", "PSI", "SPIRIT", "TIME" };
    private static final String[] COMBAT = { "ATK", "MATK", "RATK", "APP", "MOVE", "FLY", "RANGE", "INIT" };
    private static final String[] SECONDARY = { "SUP", "IMP", "BEN", "EXCL", "GRANT", "CRUSH", "AREA", "MAST", "CMAN", "MAXATK" };
    private static final String[] DAMAGE = { "BDMG", "BMDMG", "BRDMG", "TDMG", "TMDMG", "TRDMG", "BHEAL", "THEAL", "CRIT", "CRITDMG" };

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
            seedBaseStatuses(block, categoryKeys[i]);
        }

        return arr;
    }
    
    /**
     * Ensures a category array is at least as large as its key list. If the incoming
     * array is shorter (e.g., loading legacy data before new keys were added), the
     * missing slots are initialized with a base status for the corresponding key.
     */
    private StatBlock[] ensureCategorySize(StatBlock[] arr, String[] keys) {
        if (arr == null || arr.length >= keys.length) return arr;
        StatBlock[] expanded = new StatBlock[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if (i < arr.length && arr[i] != null) {
                expanded[i] = arr[i];
            } else {
                StatBlock block = new StatBlock();
                seedBaseStatuses(block, keys[i]);
                // apply key-specific defaults for newly added slots
                if ("MAXATK".equalsIgnoreCase(keys[i])) {
                    setPassiveSeverity(block, 1);
                } else if ("CRITDMG".equalsIgnoreCase(keys[i])) {
                    setPassiveSeverity(block, 2);
                } else if ("AREA".equalsIgnoreCase(keys[i])) {
                    setPassiveSeverity(block, 1);
                } else if ("GRANT".equalsIgnoreCase(keys[i])) {
                    setPassiveSeverity(block, 0);
                } else if ("MAST".equalsIgnoreCase(keys[i])) {
                    setPassiveSeverity(block, 0);
                } else if ("CMAN".equalsIgnoreCase(keys[i])) {
                    setPassiveSeverity(block, 0);
                }
                expanded[i] = block;
            }
        }
        return expanded;
    }

    // ---------------------------------------------------------
    //  UTILITY: INDEX MAPPING
    // ---------------------------------------------------------

    private int idx(String[] group, String key) {
        if (key == null) return -1;
        for (int i = 0; i < group.length; i++) if (group[i].equalsIgnoreCase(key)) return i; 
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

    /**
     * Sets the severity of an existing status entry (by name) on the given category/key.
     * Does nothing if the block or status is missing.
     */
    public void setStatusSeverity(String category, String key, String name, double severity) {
        StatBlock block = getBlock(category, key);
        if (block == null || name == null) return;
        for (DataStatus status : block.getStatus()) {
            if (name.equals(status.getName())) {
                status.setSeverity(severity);
                return;
            }
        }
    }

    /**
     * Sets the severity of an existing multi entry (by name) on the given category/key.
     * Does nothing if the block or multi-status is missing.
     */
    public void setMultiSeverity(String category, String key, String name, double severity) {
        StatBlock block = getBlock(category, key);
        if (block == null || name == null) return;
        for (DataStatus status : block.getMulti()) {
            if (name.equals(status.getName())) {
                status.setSeverity(severity);
                return;
            }
        }
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

    public StatBlock getBlock(String category, String key) {
        int i;
        switch (category.toLowerCase()) {
            case "attribute": i = idx(ATTRIBUTES, key); return i >= 0 ? attributes[i] : null;
            case "defense":
                defense = ensureCategorySize(defense, DEFENSE);
                i = idx(DEFENSE, key);
                return i >= 0 && i < defense.length ? defense[i] : null;
            case "resist":    i = idx(DMGTYPE, key);    return i >= 0 ? resist[i] : null;
            case "combat":    i = idx(COMBAT, key);     return i >= 0 ? combat[i] : null;
            case "secondary":
                secondary = ensureCategorySize(secondary, SECONDARY);
                i = idx(SECONDARY, key);
                return i >= 0 && i < secondary.length ? secondary[i] : null;
            case "damage":
                damage = ensureCategorySize(damage, DAMAGE);
                i = idx(DAMAGE, key);
                return i >= 0 && i < damage.length ? damage[i] : null;
            default: return null;
        }
    }

    /** Seeds a new StatBlock with Passive, Maintained, Temporary base statuses and key-specific defaults. */
    private void seedBaseStatuses(StatBlock block, String key) {
        if (block == null) return;
        String[] labels = { "Passive", "Maintained", "Temporary" };
        for (String label : labels) {
            DataStatus base = new DataStatus();
            base.setName(label);
            base.setAttribute(key);
            base.setDurationType("Permanent");
            boolean isAttributeKey = Arrays.stream(ATTRIBUTES).anyMatch(a -> a.equalsIgnoreCase(key));
            if (isAttributeKey) {
                base.setSeverity("Passive".equals(label) ? 10 : 0); // attribute bases start at 10
            } else if ("MAXATK".equalsIgnoreCase(key)) {
                base.setSeverity(1); // base Max Attacks starts at 1
            } else if ("CRITDMG".equalsIgnoreCase(key)) {
                base.setSeverity(2); // base Critical Damage multiplier
            } else if ("AREA".equalsIgnoreCase(key)) {
                base.setSeverity(1); // base Area starts at 1
            } else if ("GRANT".equalsIgnoreCase(key)) {
                base.setSeverity(0); // baseline Grant
            } else if ("MAST".equalsIgnoreCase(key)) {
                base.setSeverity(0); // baseline Mastery
            }
            block.addStatus(base);
        }
    }

    /** Convenience to tweak the Passive status severity for a newly seeded block. */
    private void setPassiveSeverity(StatBlock block, double severity) {
        if (block == null) return;
        for (DataStatus s : block.getStatus()) {
            if ("Passive".equalsIgnoreCase(s.getName())) {
                s.setSeverity(severity);
                return;
            }
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

    /** Returns a defensive copy of attribute keys. */
    public static String[] getAttributeKeys() { return ATTRIBUTES.clone(); }

    // ---------------------------------------------------------
    //  SERIALIZATION HELPERS
    // ---------------------------------------------------------

    @JsonProperty("attributes") public StatBlock[] getAttributes() { return attributes; }
    @JsonProperty("defense") public StatBlock[] getDefense() { return defense; }
    @JsonProperty("resist") public StatBlock[] getResist() { return resist; }
    @JsonProperty("combat") public StatBlock[] getCombat() { return combat; }
    @JsonProperty("secondary") public StatBlock[] getSecondary() { return secondary; }
    @JsonProperty("damage") public StatBlock[] getDamage() { return damage; }

    @JsonIgnore
    public CharData getOwner() { return owner; }
    public void setOwner(CharData owner) { this.owner = owner; }
}
