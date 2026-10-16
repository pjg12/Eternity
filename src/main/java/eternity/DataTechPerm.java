package eternity;

public class DataTechPerm {
    private int id;
    private String attribute;
    private double ratio;
    private int cost;


    // --- Constructors ---

    public DataTechPerm() { this(0, "None", 0.0, 0); }
    public DataTechPerm(DataTechPerm src) { this(src.id, src.attribute, src.ratio, src.cost); }

    public DataTechPerm(int id, String attribute, double ratio, int cost) {
        this.id = id;
        this.attribute = attribute;
        this.ratio = ratio;
        this.cost = cost;
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public double getRatio() { return ratio; }
    public void setRatio(double ratio) { this.ratio = ratio; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    
}
