package eternity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Specialty Information.
 */
public class DataSpecialty {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String refName;
    @JsonProperty private String category;
    @JsonProperty private String description;
    @JsonProperty private String type;
    @JsonProperty private boolean pick;
    
    // --- Constructors ---
    
    public DataSpecialty() { this(0, "", "", "", "", "", false); }
    public DataSpecialty(DataSpecialty src) { this(src.id, src.name, src.refName, src.category, src.description, src.type, src.pick); }
    
    public DataSpecialty(int id, String name, String category, String description, String type, boolean pick) {
        this(id, name, "", category, description, type, pick);
    }

    public DataSpecialty(int id, String name, String refName, String category, String description, String type, boolean pick) {
        this.id = id;
        this.name = safe(name);
        this.refName = safe(refName);
        this.category = safe(category);
        this.description = safe(description);
        this.type = safe(type);
        this.pick = pick;
    }

    // --- Getters & Setters ---
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getRefName() { return refName; }
    public void setRefName(String refName) { this.refName = safe(refName); }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = safe(category); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    @JsonIgnore
    public String getSource() { return type; }
    @JsonIgnore
    public void setSource(String source) { this.type = safe(source); }

    public boolean getPick() { return pick; }
    public void setPick(boolean pick) { this.pick = pick; }

    // --- Helpers ---

    private static String safe(String s) { return s == null ? "" : s; }
    
    @Override
    public String toString() { return "DataSpecial {\n" + "  id: " + id + ",\n" + "  name: \"" + name + "\",\n" + "  refName: \"" + refName + "\",\n" + "  category: \"" + category + "\",\n" +
        "  description: \"" + description + "\",\n" + "  type: \"" + type + "\",\n" + "  pick: " + pick + "\n" + "}"; }
}
