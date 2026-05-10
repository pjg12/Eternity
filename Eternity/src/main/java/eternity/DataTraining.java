package eternity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Aura Tech Training Information
 */
@JsonIgnoreProperties({ "nextAt", "statusScaleLevel" })
public class DataTraining {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String type;
    @JsonProperty private String affinity;
    @JsonProperty private String description;
    @JsonProperty private double levelCoef;
    @JsonProperty private double levelMod;
    @JsonProperty private int prereq;
    @JsonProperty private List<Integer> grant;
    @JsonProperty private int rank;
    @JsonProperty private int al;
    @JsonProperty private double exp;
    @JsonProperty private List<DataStatus> permStatus;
    
    // --- Constructors ---

    public DataTraining() { this(-1, "", "", "None", "", -1.0, 0.0, 0, new ArrayList<>(), 0, 0, 0.0, new ArrayList<>()); }
    public DataTraining(DataTraining src) { this(src.id, src.name, src.type, src.affinity, src.description, src.levelCoef, src.levelMod, src.prereq, src.grant, src.rank, src.al, src.exp, src.permStatus); }
    
    public DataTraining(int id, String name, String type, String affinity, String description, double levelCoef, double levelMod, int prereq, List<Integer> grant, int rank, double exp, List<DataStatus> permStatus) {
        this(id, name, type, affinity, description, levelCoef, levelMod, prereq, grant, rank, 0, exp, permStatus);
    }

    public DataTraining(int id, String name, String type, String affinity, String description, double levelCoef, double levelMod, int prereq, List<Integer> grant, int rank, int al, double exp, List<DataStatus> permStatus) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.affinity = affinity;
        this.description = description;
        this.levelCoef = levelCoef;
        this.levelMod = levelMod;
        this.prereq = prereq;
        this.grant = grant == null ? new ArrayList<>() : new ArrayList<>(grant);
        this.rank = rank;
        this.al = al;
        this.exp = exp;
        this.permStatus = permStatus == null ? new ArrayList<>() : new ArrayList<>(permStatus);
    }

    // --- Getters & Setters ---
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public String getAffinity() { return affinity; }
    public void setAffinity(String affinity) { this.affinity = safe(affinity).isEmpty() ? "None" : affinity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    public double getLevelCoef() { return levelCoef; }
    public void setLevelCoef(double levelCoef) { this.levelCoef = levelCoef; }

    public double getLevelMod() { return levelMod; }
    public void setLevelMod(double levelMod) { this.levelMod = levelMod; }

    public int getPrereq() { return prereq; }
    public void setPrereq(int prereq) { this.prereq = prereq; }

    public List<Integer> getGrant() { return new ArrayList<>(grant); }
    public void setGrant(List<Integer> grant) { this.grant = grant == null ? new ArrayList<>() : new ArrayList<>(grant); }
    public void addGrant(int grantId) {
        if (this.grant == null) this.grant = new ArrayList<>();
        if (!this.grant.contains(grantId)) this.grant.add(grantId);
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public int getAl() { return al; }
    public void setAl(int al) { this.al = al; }

    /**
     * Multiplier used when scaling a technique's DataStatus severity.
     * Maintained techniques scale from active level (AL); all others scale from rank.
     */
    @JsonIgnore
    public int getStatusScaleLevel() {
        if ("Maintained".equalsIgnoreCase(type)) return Math.max(0, al);
        return Math.max(0, rank);
    }

    /** Scales a base severity value by this technique's current status scale level. */
    public double scaleStatusSeverity(double baseSeverity) {
        return baseSeverity * getStatusScaleLevel();
    }

    public double getExp() { return exp; }
    public void setExp(double exp) { this.exp = exp; }

    @JsonIgnore // do not persist permStatus; it is rebuilt from master data on load
    public List<DataStatus> getPermStatus() { return new ArrayList<>(permStatus); }

    @JsonProperty // still allow incoming data (ignored on save) to avoid deserialization errors
    public void setPermStatus(List<DataStatus> permStatus) { this.permStatus = permStatus == null ? new ArrayList<>() : new ArrayList<>(permStatus); }
    public void addPermStatus(DataStatus status) {
        if (status == null) return;
        if (this.permStatus == null) this.permStatus = new ArrayList<>();
        this.permStatus.add(status);
    }
    
    // --- Helpers ---
    
    /// Computes the maximum achievable rank based on character level, affinity, and prerequisites.
    public int getMaxRank(StoreCharData character) {
        if (character == null || character.getIdentity() == null || character.getTraining() == null) return 0;

        int level = character.getIdentity().getLevel();
        double max = (level - levelMod) * levelCoef;
        if (max < 0) max = 0;
        
        // Natural affinity bonus (+1 rank)
        if (character.getTraining().getNaturalAffinities().contains(affinity)) {
            max++;
        }
        
        // Prereq cap
        if (prereq != -1) {
            DataTraining req = character.getTraining().getTrainingById(prereq);
            if (req != null) max = Math.min(max, req.getRank()); 
        }
        return (int) max;
    }
    
    //Returns a human-readable string explaining *what* is now the limiting factor: - "Level", - Prereq Skill name
    public String getPrereqCap(StoreCharData character) {
        if (character == null || character.getTraining() == null) return "Level";

        double cap = getMaxRank(character);

        if (prereq != -1) {
            DataTraining prereqTech = character.getTraining().getTrainingById(prereq);
            if (prereqTech != null && prereqTech.getRank() < cap) {
                return prereqTech.getName();
            }
        }
        return "Level";
    }
    
    /** Computes XP required for next rank. */
    public int getNextAt(StoreCharData character) {
        int value = getRank() * 4 + 10;

        // Spirit / Time penalty
        if ("Spirit".equalsIgnoreCase(affinity) || "Time".equalsIgnoreCase(affinity)) value = (int)(value * 1.5); 

        // Natural affinity bonus (half cost)
        if (character != null && character.getTraining() != null &&
                character.getTraining().getNaturalAffinities().contains(affinity)) {
            value /= 2;
        }
        return value;
    }
    
    private static String safe(String s) { return s == null ? "" : s; }

    public void updateStatus() {

    }

    @Override
    public String toString() { return "DataTraining {\n" + "  id: " + id + ",\n" + "  name: \"" + name + "\",\n" + "  type: \"" + type + "\",\n" +
        "  affinity: \"" + affinity + "\",\n" + "  description: \"" + description + "\",\n" + "  levelCoef: " + levelCoef + ",\n" + "  levelMod: " + levelMod + ",\n" +
        "  prereq: " + prereq + ",\n" + "  grant: " + grant + ",\n" + "  rank: " + rank + ",\n" + "  al: " + al + ",\n" + "  exp: " + exp + "\n" + "}"; }
}

