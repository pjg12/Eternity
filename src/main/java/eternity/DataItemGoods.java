package eternity;

/**
 * A DataItemGoods represents a goods/trade-material inventory item.
 */
public class DataItemGoods extends DataItem {

    // --- Constructors ---

    public DataItemGoods() {
        super();
    }

    public DataItemGoods(DataItemGoods src) {
        super(src);
    }

    public DataItemGoods(DataItem src) {
        super(src);
    }

    public DataItemGoods(int did, int iid, String dname, String iname, String dnote, String inote, double quantity) {
        super(did, iid, dname, iname, dnote, inote, quantity);
    }
}
