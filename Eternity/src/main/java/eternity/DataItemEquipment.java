package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DataItemEquipment represents an item that can be equipped.
 */
public class DataItemEquipment extends DataItem {
    @JsonProperty private String slot;        // Where the item can be equipped
    @JsonProperty private int tier;           // Relative power tier
    @JsonProperty private String category;    // Armor/weapon/accessory class
    @JsonProperty private String type;        // Weapon/armor subtype
    @JsonProperty private String bonusAtt;    // Attribute name to modify
    @JsonProperty private double bonusAmount; // How much attribute is modified
    @JsonProperty private int levelReq;       // Required character level to equip
    @JsonProperty private long value;         // Sell/buy value
    @JsonProperty private boolean equipped;   // Is the item currently equipped?

    @JsonProperty private int ench;   // Enchantment
    @JsonProperty private int gem;    // Gem slot
    @JsonProperty private int store;   // Tech Storage
    @JsonProperty private int oil;    // Oil modifier
    @JsonProperty private int mod;    // Modification
    @JsonProperty private int aug;    // Augment
    
    // --- Constructors ---

    public DataItemEquipment() { this(-1, -1, "", "", "", "", 0.0, "", 0, "", "", "", 0.0, 1, 0L, false, 0, 0, 0, 0, 0, 0); }
    public DataItemEquipment(DataItemEquipment src) { this(src.getDid(), src.getIid(), src.getDname(), src.getIname(), src.getDnote(), src.getInote(), src.getQuantity(), src.slot, src.tier, src.category, src.type, src.bonusAtt, src.bonusAmount, 
    		src.levelReq, src.value, src.equipped, src.ench, src.gem, src.store, src.oil, src.mod, src.aug);
    }
    
    public DataItemEquipment(int did, int iid, String dname, String iname, String dnote, String inote, double quantity, String slot, int tier, String category, String type, String bonusAtt, double bonusAmount,
    		int levelReq, long value, boolean equipped, int ench, int gem, int store, int oil, int mod, int aug) {
    	super(did, iid, dname, iname, dnote, inote, quantity);
    	this.slot = safe(slot);
        this.tier = tier;
        this.category = safe(category);
        this.type = safe(type);
        this.bonusAtt = safe(bonusAtt);
        this.bonusAmount = bonusAmount;
        this.levelReq = levelReq;
        this.value = value;
        this.equipped = equipped;
        this.ench = ench;
        this.gem = gem;
        this.store = store;
        this.oil = oil;
        this.mod = mod;
        this.aug = aug;
    }

    // --- Getters & Setters ---

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = safe(slot); }

    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = safe(category); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public String getBonusAtt() { return bonusAtt; }
    public void setBonusAtt(String bonusAtt) { this.bonusAtt = safe(bonusAtt); }

    public double getBonusAmount() { return bonusAmount; }
    public void setBonusAmount(double bonusAmount) { this.bonusAmount = bonusAmount; }

    public int getLevelReq() { return levelReq; }
    public void setLevelReq(int levelReq) { this.levelReq = levelReq; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }

    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean equipped) { this.equipped = equipped; }

    public int getEnch() { return ench; }
    public void setEnch(int ench) { this.ench = ench; }

    public int getGem() { return gem; }
    public void setGem(int gem) { this.gem = gem; }

    public int getStore() { return store; }
    public void setStore(int store) { this.store = store; }

    public int getOil() { return oil; }
    public void setOil(int oil) { this.oil = oil; }

    public int getMod() { return mod; }
    public void setMod(int mod) { this.mod = mod; }

    public int getAug() { return aug; }
    public void setAug(int aug) { this.aug = aug; }

    // --- Helpers ---

    private static String safe(String s) { return s == null ? "" : s; }
    
    @Override
    public String toString() {
        return "DataItemEquipment {\n" + "  dname: \"" + getDname() + "\",\n" + "  iname: \"" + getIname() + "\",\n" + "  slot: \"" + slot + "\",\n" +
            "  category: \"" + category + "\",\n" + "  type: \"" + type + "\",\n" + "  tier: " + tier + ",\n" + "  bonusAtt: \"" + bonusAtt + "\",\n" +
            "  bonusAmount: " + bonusAmount + ",\n" + "  levelReq: " + levelReq + ",\n" + "  value: " + value + ",\n" + "  equipped: " + equipped + ",\n" +
            "  ench: " + ench + ",\n" + "  gem: " + gem + ",\n" + "  store: " + store + ",\n" + "  oil: " + oil + ",\n" + "  mod: " + mod + ",\n" +
            "  aug: " + aug + "\n" + "}";
    }
}