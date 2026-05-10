// CHECKED

package eternity;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Basic attribute data for a character.
 */

public class CharAttributes {
    @JsonIgnore private StoreCharData owner;                                             // Reference to main character data

    private static final String[] ATTRIBUTES = { "STR","DEX","CON","FOC","CTL","CAP","KNOW","MECH","PERC","INT","CHA","SUB" };
    private static final String[] DEFENSE = { "ARMOR","DODGE","DEF","FORT","REF","WILL","AVOID" };
    private static final String[] DMGTYPE = { "ALL","PHY","BLUNT","PIERCE","SLASH","FIRE","FROST","ELEC","ENERGY","SONIC","LIGHT","TOXIC","DARK","PSI","SPIRIT","TIME" };
    private static final String[] COMBAT = { "ATK","APP","MOVE","FLY","RANGE","INIT","CMAN","MAXATK" };
    private static final String[] SECONDARY = { "SUP","IMP","MAST","EXCL","GRANT","CRUSH","AREA" };
    private static final String[] DAMAGE = { "BDMG", "TDMG", "BHEAL", "THEAL", "CRIT", "CRITDMG" };

    //  GENERIC STAT ARRAYS

    @JsonProperty("bAttributes") private ArrayList<DataStatus>[][] bAttributes;     // List of base attribute modifiers
    @JsonProperty("mAttributes") private ArrayList<DataStatus>[][] mAttributes;         // List of attribute multipliers
    @JsonProperty("bDefense") private ArrayList<DataStatus>[][] bDefense;           // List of base defense modifiers
    @JsonProperty("mDefense") private ArrayList<DataStatus>[][] mDefense;               // List of defense multipliers
    @JsonProperty("bResist") private ArrayList<DataStatus>[][] bResist;             // List of base resist modifiers
    @JsonProperty("mResist") private ArrayList<DataStatus>[][] mResist;                 // List of resist multipliers
    @JsonProperty("bCombat") private ArrayList<DataStatus>[][] bCombat;             // List of base combat modifiers
    @JsonProperty("mCombat") private ArrayList<DataStatus>[][] mCombat;                 // List of combat multipliers
    @JsonProperty("bSecondary") private ArrayList<DataStatus>[][] bSecondary;       // List of base secondary modifiers
    @JsonProperty("mSecondary") private ArrayList<DataStatus>[][] mSecondary;           // List of secondary multipliers
    @JsonProperty("bDamage") private ArrayList<DataStatus>[][] bDamage;             // List of base damage modifiers
    @JsonProperty("mDamage") private ArrayList<DataStatus>[][] mDamage;                 // List of damage multipliers

    public CharAttributes() {
    	this.bAttributes = initCategory(ATTRIBUTES, true);
        this.mAttributes = initCategory(ATTRIBUTES, false);
    	this.bDefense    = initCategory(DEFENSE, true);
        this.mDefense    = initCategory(DEFENSE, false);
    	this.bResist     = initCategory(DMGTYPE, true);
        this.mResist     = initCategory(DMGTYPE, false);
    	this.bCombat     = initCategory(COMBAT, true);
        this.mCombat     = initCategory(COMBAT, false);
    	this.bSecondary  = initCategory(SECONDARY, true);
        this.mSecondary  = initCategory(SECONDARY, false);
        this.bDamage     = initCategory(DAMAGE, true);
        this.mDamage     = initCategory(DAMAGE, false);
    }

    // ---------------------------------------------------------
    //  CATEGORY INITIALIZATION
    // ---------------------------------------------------------

    @SuppressWarnings("unchecked")
    private ArrayList<DataStatus>[][] initCategory(String[] categoryKeys, boolean isBase) {
        ArrayList<DataStatus>[][] list = (ArrayList<DataStatus>[][]) new ArrayList<?>[categoryKeys.length][3];
        String prefix = isBase ? "B" : "M";
        for (int i = 0; i < categoryKeys.length; i++) {
            list[i] = initStatus(prefix + categoryKeys[i]);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<DataStatus>[] initStatus(String attributeKey) {
        ArrayList<DataStatus>[] list = (ArrayList<DataStatus>[]) new ArrayList<?>[3];
        String[] labels = { "Passive", "Maintained", "Temporary" };
        for (int i = 0; i < 3; i++) {
            list[i] = new ArrayList<>();
            DataStatus base = new DataStatus();
            base.setName(labels[i]);
            base.setAttribute(attributeKey);
            base.setSeverity(0);
            base.setDurationType(labels[i]);
            list[i].add(base);
        }
        return list;
    }
    
    // ---------------------------------------------------------
    //   COMPUTED VALUES
    // ---------------------------------------------------------

    @JsonIgnore
    public double calcValue(ArrayList<DataStatus> list) {
        double result = 0.0;
        for (DataStatus status : list) {
            if (status != null) result += status.getSeverity();
        }
        return Math.max(0.0, result);
    }

    @JsonIgnore
    public int calcMaxValue(ArrayList<DataStatus>[] base, ArrayList<DataStatus>[] multi) {
        double baseValue = 0.0, multiValue = 0.0;
        for (int i = 0; i < 3; i++) {
            baseValue += calcValue(base[i]);
            multiValue += calcValue(base[i]);
        }
        return (int)Math.max(0, baseValue * multiValue);
    }

    @JsonIgnore public int calcStatusValue(String key) {
        ArrayList<DataStatus>[] base = findStatusBlock(findStatusCategory(findCatByAttribute("B" + key)), key);
        ArrayList<DataStatus>[] multi = findStatusBlock(findStatusCategory(findCatByAttribute("M" + key)), key);
        if (base == null || multi == null) return 0;
        return calcMaxValue(base, multi);
    }

    // ---------------------------------------------------------
    //   GETTERS & SETTERS
    // ---------------------------------------------------------
    public ArrayList<DataStatus>[][] getBAttributes() { return bAttributes; }
    public ArrayList<DataStatus>[][] getMAttributes() { return mAttributes; }
    public ArrayList<DataStatus>[][] getBDefense() { return bDefense; }
    public ArrayList<DataStatus>[][] getMDefense() { return mDefense; }
    public ArrayList<DataStatus>[][] getBResist() { return bResist; }
    public ArrayList<DataStatus>[][] getMResist() { return mResist; }
    public ArrayList<DataStatus>[][] getBCombat() { return bCombat; }
    public ArrayList<DataStatus>[][] getMCombat() { return mCombat; }
    public ArrayList<DataStatus>[][] getBSecondary() { return bSecondary; }
    public ArrayList<DataStatus>[][] getMSecondary() { return mSecondary; }
    public ArrayList<DataStatus>[][] getBDamage() { return bDamage; }
    public ArrayList<DataStatus>[][] getMDamage() { return mDamage; }

    @JsonSetter("bAttributes") public void setBAttributes(ArrayList<DataStatus>[][] list) { this.bAttributes = list; }
    @JsonSetter("mAttributes") public void setMAttributes(ArrayList<DataStatus>[][] list) { this.mAttributes = list; }
    @JsonSetter("bDefense") public void setBDefense(ArrayList<DataStatus>[][] list) { this.bDefense = list; }
    @JsonSetter("mDefense") public void setMDefense(ArrayList<DataStatus>[][] list) { this.mDefense = list; }
    @JsonSetter("bResist") public void setBResist(ArrayList<DataStatus>[][] list) { this.bResist = list; }
    @JsonSetter("mResist") public void setMResist(ArrayList<DataStatus>[][] list) { this.mResist = list; }
    @JsonSetter("bCombat") public void setBCombat(ArrayList<DataStatus>[][] list) { this.bCombat = list; }
    @JsonSetter("mCombat") public void setMCombat(ArrayList<DataStatus>[][] list) { this.mCombat = list; }
    @JsonSetter("bSecondary") public void setBSecondary(ArrayList<DataStatus>[][] list) { this.bSecondary = list; }
    @JsonSetter("mSecondary") public void setMSecondary(ArrayList<DataStatus>[][] list) { this.mSecondary = list; }
    @JsonSetter("bDamage") public void setBDamage(ArrayList<DataStatus>[][] list) { this.bDamage = list; }
    @JsonSetter("mDamage") public void setMDamage(ArrayList<DataStatus>[][] list) { this.mDamage = list; }

    @JsonIgnore public StoreCharData getOwner() { return owner; }
    @JsonIgnore public void setOwner(StoreCharData owner) { this.owner = owner; }

    public static String[] getAttributeKeys() { return ATTRIBUTES.clone(); }
    public static String[] getDefenseKeys() { return DEFENSE.clone(); }
    public static String[] getDamageTypeKeys() { return DMGTYPE.clone(); }
    public static String[] getCombatKeys() { return COMBAT.clone(); }
    public static String[] getSecondaryKeys() { return SECONDARY.clone(); }
    public static String[] getDamageKeys() { return DAMAGE.clone(); }

    // ---------------------------------------------------------
    //   HELPERS
    // ---------------------------------------------------------

    public void addStatus (DataStatus status) {
        if (status == null || status.getAttribute() == null) return;
        String att = status.getAttribute().toUpperCase();
        ArrayList<DataStatus> list = findStatusArray( findStatusBlock( findStatusCategory( findCatByAttribute(att)), att), status.getDurationType().toUpperCase());
        DataStatus existing = findStatus(list, status.getName());
        if (existing != null) existing.setSeverity(status.getSeverity());
            // TODO better comparison logic for statuses of the same name? For now, just take the highest severity if a duplicate is added.
        else list.add(status); 
    }

    public void removeStatus (String name, String category) {
        name = name.toUpperCase();
        category = category.toUpperCase();
        ArrayList<DataStatus>[][] catList = findStatusCategory(category);
        for (int i = 0; i < catList.length; i++) {
            for (int j = 0; j < 3; j++) {
                ArrayList<DataStatus> list = catList[i][j];
                DataStatus existing = findStatus(list, name);
                if (existing != null) list.remove(existing);
            }
        }
    }

    public void removeStatusByStatus (DataStatus status) {
        String name = status.getName().toUpperCase();
        String category = findCatByAttribute(status.getAttribute().toUpperCase()).toUpperCase();
        ArrayList<DataStatus>[][] catList = findStatusCategory(category);
        for (int i = 0; i < catList.length; i++) {
            for (int j = 0; j < 3; j++) {
                ArrayList<DataStatus> list = catList[i][j];
                DataStatus existing = findStatus(list, name);
                if (existing != null) list.remove(existing);
            }
        }
    }
    
    private ArrayList<DataStatus>[][] findStatusCategory (String key) {
        key = key.toUpperCase();
        return switch (key) {
            case "BATTRIBUTES" -> bAttributes;
            case "MATTRIBUTES" -> mAttributes;
            case "BDEFENSE" -> bDefense;
            case "MDEFENSE" -> mDefense;
            case "BRESIST" -> bResist;
            case "MRESIST" -> mResist;
            case "BCOMBAT" -> bCombat;
            case "MCOMBAT" -> mCombat;
            case "BSECONDARY" -> bSecondary;
            case "MSECONDARY" -> mSecondary;
            case "BDAMAGE" -> bDamage;
            case "MDAMAGE" -> mDamage;
            default -> null;
        };
    }

    private ArrayList<DataStatus>[] findStatusBlock (ArrayList<DataStatus>[][] category, String key) {
        if (category == null) return null;
        String cat;
        for (int i = 0; i < category.length; i++) {
            cat = category[i][0].get(0).getAttribute(); 
            if (cat.equalsIgnoreCase(key)) {
                return category[i];
            }
        }
        return null;
    }

    private ArrayList<DataStatus> findStatusArray (ArrayList<DataStatus>[] block, String key) {
        return switch (key) {
            case "PASSIVE" -> block[0];
            case "MAINTAINED" -> block[1];
            case "TEMPORARY" -> block[2];
            default -> null;
        };
    }

    private DataStatus findStatus(ArrayList<DataStatus> list, String name) {
        for (DataStatus status : list) {
            if (status != null && name.equalsIgnoreCase(status.getName())) {
                return status;
            }
        }
        return null;
    }

    private String findCatByAttribute(String attribute) {
        String cat = checkCatByKey(attribute);
        if (cat != null) return cat;
        throw new IllegalArgumentException("Invalid attribute key: " + attribute);
    }

    private String checkCatByKey(String key) {
        String subcat = key.substring(0, 1);
        key = key.substring(1).toUpperCase();
        for (String cat: ATTRIBUTES) {
            if ((cat).equals(key)) return subcat + "attributes";
        }
        for (String cat: DEFENSE) {
            if (cat.equals(key)) return subcat + "defense";
        }
        for (String cat: DMGTYPE) {
            if (cat.equals(key)) return subcat + "resist";
        }
        for (String cat: COMBAT) {
            if (cat.equals(key)) return subcat + "combat";
        }
        for (String cat: SECONDARY) {
            if (cat.equals(key)) return subcat + "secondary";
        }
        for (String cat: DAMAGE) {
            if (cat.equals(key)) return subcat + "damage";
        }
        return null;
    }
}

