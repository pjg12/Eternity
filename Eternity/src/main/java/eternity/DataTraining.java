package eternity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Aura Tech Training Information
 */
@JsonIgnoreProperties({ "nextAt" })
public class DataTraining {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String type;
    @JsonProperty private String affinity;
    @JsonProperty private String description;
    @JsonProperty private double levelCoef;
    @JsonProperty private double levelMod;
    @JsonProperty private int prereq;
    @JsonProperty private int grant;
    @JsonProperty private int rank;
    @JsonProperty private double exp;
    
    // --- Constructors ---

    public DataTraining() { this(-1, "", "", "None", "", -1.0, 0.0, 0, -1, 0, 0.0); }
    public DataTraining(DataTraining src) { this(src.id, src.name, src.type, src.affinity, src.description, src.levelCoef, src.levelMod, src.prereq, src.grant, src.rank, src.exp); }
    
    public DataTraining(int id, String name, String type, String affinity, String description, double levelCoef, double levelMod, int prereq, int grant, int rank, double exp) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.affinity = affinity;
        this.description = description;
        this.levelCoef = levelCoef;
        this.levelMod = levelMod;
        this.prereq = prereq;
        this.grant = grant;
        this.rank = rank;
        this.exp = exp;
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

    public int getGrant() { return grant; }
    public void setGrant(int grant) { this.grant = grant; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getExp() { return exp; }
    public void setExp(double exp) { this.exp = exp; }
    
    // --- Helpers ---
    
    /** Computes the maximum achievable rank based on character level, affinity, and prerequisites. */
    /*public int getMaxRank(CharData character) {
        // Base computation: (level - mod) * coef
        double max = (character.getCharLevel() - levelMod) * levelCoef;
        if (max < 0) max = 0;
        
        // Natural affinity bonus (+1 rank)
        if (character.getCharNaturalAffinities().contains(affinity)) {
            max++;
        }
        
        // Prereq cap
        if (prereq != -1) {
            DataTraining req = character.getTrainingTechById(prereq);
            if (req != null) max = Math.min(max, req.getRank()); 
        }
        return (int) max;
    }*/
    
    //Returns a human-readable string explaining *what* is now the limiting factor: - "Level", - Prereq Skill name
    /*public String getPrereqCap(CharData character) {
        String cap = "Level";
        // Compute level-based cap
        double floor = (levelMod > 1 ? Math.floor(levelMod) : 0.0);
        double fraction = (levelMod > 1 ? levelMod - floor : levelMod);
        if (fraction == 0.0 && levelMod > 1) {
            floor--;
            fraction = 1.0;
        }
        double lvlRank = character.getCharLevel() * fraction;
        if (character.getCharNaturalAffinities().contains(affinity)) lvlRank++;
        lvlRank -= floor;

        // Check prereq cap
        DataTraining prereqTech = character.getTrainingTechById(prereq);
        if (prereqTech != null) {
            double prereqRank = prereqTech.getRank();
            if (prereqRank < lvlRank) return prereqTech.getName();
            else return character.getDataStore().getTrainingData(prereq).getName();
        }
        return cap;
    }*/
    
    /** Computes XP required for next rank. */
    /*public int getNextAt(CharData character) {
        int value = getRank() * 4 + 10;

        // Spirit / Time penalty
        if ("Spirit".equals(affinity) || "Time".equals(affinity)) value = (int)(value * 1.5); 

        // Natural affinity bonus (half cost)
        if (character.getCharNaturalAffinities().contains(affinity)) value /= 2;
        return value;
    }*/
    
    private static String safe(String s) { return s == null ? "" : s; }

    @Override
    public String toString() { return "DataTraining {\n" + "  id: " + id + ",\n" + "  name: \"" + name + "\",\n" + "  type: \"" + type + "\",\n" +
        "  affinity: \"" + affinity + "\",\n" + "  description: \"" + description + "\",\n" + "  levelCoef: " + levelCoef + ",\n" + "  levelMod: " + levelMod + ",\n" +
        "  prereq: " + prereq + ",\n" + "  grant: " + grant + ",\n" + "  rank: " + rank + ",\n" + "  exp: " + exp + "\n" + "}"; }
}