package eternity;

/**
 * A DataItemConsume represents a consumable inventory item.
 */
public class DataItemConsume extends DataItem {

    // --- Constructors ---

    public DataItemConsume() {
        super();
    }

    public DataItemConsume(DataItemConsume src) {
        super(src);
    }

    public DataItemConsume(DataItem src) {
        super(src);
    }

    public DataItemConsume(int did, int iid, String dname, String iname, String dnote, String inote, double quantity) {
        super(did, iid, dname, iname, dnote, inote, quantity);
    }
}
