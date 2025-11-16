package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Specialty Information.
 */
public class DataSpecialty {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String type;
    @JsonProperty private String description;
    @JsonProperty private String source;
    @JsonProperty private boolean pick;
    
    // --- Constructors ---
    
    public DataSpecialty() { this(0, "", "", "", "", false); }
    public DataSpecialty(DataSpecialty src) { this(src.id, src.name, src.type, src.description, src.source, src.pick); }
    
    public DataSpecialty(int id, String name, String type, String description, String source, boolean pick) {
        this.id = id;
        this.name = safe(name);
        this.type = safe(type);
        this.description = safe(description);
        this.source = safe(source);
        this.pick = pick;
    }

    // --- Getters & Setters ---
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = safe(source); }

    public boolean getPick() { return pick; }
    public void setPick(boolean pick) { this.pick = pick; }

    // --- Helpers ---

    private static String safe(String s) { return s == null ? "" : s; }
    
    @Override
    public String toString() { return "DataSpecial {\n" + "  id: " + id + ",\n" + "  name: \"" + name + "\",\n" + "  type: \"" + type + "\",\n" +
        "  description: \"" + description + "\",\n" + "  source: \"" + source + "\",\n" + "  pick: " + pick + "\n" + "}"; }
}