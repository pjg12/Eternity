package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Generic Inventory Item Information.
 */
public class DataItem {
    @JsonProperty private int did;        // Default item ID
    @JsonProperty private int iid;        // Instance ID (unique per slot)
    @JsonProperty private String dname;   // Default name
    @JsonProperty private String iname;   // Slot-specific custom name
    @JsonProperty private String dnote;   // Default description
    @JsonProperty private String inote;   // Slot-specific custom description
    @JsonProperty private double quantity;
    
    // --- Constructors ---

    public DataItem() { this(-1, -1, "", "", "", "", 0.0); }
    public DataItem(DataItem src) { this(src.did, src.iid, src.dname, src.iname, src.dnote, src.inote, src.quantity); }
    
    public DataItem(int did, int iid, String dname, String iname, String dnote, String inote, double quantity) {
        this.did = did;
        this.iid = iid;
        this.dname = dname;
        this.iname = iname;
        this.dnote = dnote;
        this.inote = inote;
        this.quantity = quantity;
    }

    // --- Getters & Setters ---

    public int getDid() { return did; }
    public void setDid(int did) { this.did = did; }

    public int getIid() { return iid; }
    public void setIid(int iid) { this.iid = iid; }

    public String getDname() { return dname; }
    public void setDname(String dname) { this.dname = safe(dname); }

    public String getIname() { return iname; }
    public void setIname(String iname) { this.iname = safe(iname); }

    public String getDnote() { return dnote; }
    public void setDnote(String dnote) { this.dnote = safe(dnote); }

    public String getInote() { return inote; }
    public void setInote(String inote) { this.inote = safe(inote); }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    // --- Helpers ---
    
    public void addQuantity(double amount) { this.quantity += amount; }
    public void clampQuantity() { if (quantity < 0) quantity = 0; } // force to >= 0
    private static String safe(String s) { return s == null ? "" : s; }

    @Override
    public String toString() {
        return "DataItem {\n" + "  did: " + did + ",\n" + " iid: " + iid + ",\n" + "  dname: \"" + dname + "\",\n" + "  iname: \"" + iname + "\",\n" +
            "  dnote: \"" + dnote + "\",\n" + "  inote: \"" + inote + "\",\n" + "  quantity: " + quantity + "\n" + "}";
    }
}