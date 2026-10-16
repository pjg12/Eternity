package eternity;

/**
 * A DataItemMisc represents a miscellaneous inventory item.
 */
public class DataItemMisc extends DataItem {

    // --- Constructors ---

    public DataItemMisc() {
        super();
    }

    public DataItemMisc(DataItemMisc src) {
        super(src);
    }

    public DataItemMisc(DataItem src) {
        super(src);
    }

    public DataItemMisc(int did, int iid, String dname, String iname, String dnote, String inote, double quantity) {
        super(did, iid, dname, iname, dnote, inote, quantity);
    }
}
