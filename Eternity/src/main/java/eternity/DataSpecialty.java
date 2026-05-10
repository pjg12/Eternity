package eternity;

import java.util.ArrayList;
import java.util.List;

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
    @JsonProperty private int prereq;
    @JsonProperty private boolean pick;
    @JsonProperty private List<DataStatus> permStatus;
    
    // --- Constructors ---
    
    public DataSpecialty() { this(0, "", "", "", "", "", 0, false, null); }
    public DataSpecialty(DataSpecialty src) { this(src.id, src.name, src.refName, src.category, src.description, src.type, src.prereq, src.pick, src.permStatus); }
    
    public DataSpecialty(int id, String name, String category, String description, String type, boolean pick) {
        this(id, name, "", category, description, type, 0, pick, null);
    }

    public DataSpecialty(int id, String name, String refName, String category, String description, String type, boolean pick) {
        this(id, name, refName, category, description, type, 0, pick, null);
    }

    public DataSpecialty(int id, String name, String refName, String category, String description, String type, int prereq, boolean pick) {
        this(id, name, refName, category, description, type, prereq, pick, null);
    }

    public DataSpecialty(int id, String name, String refName, String category, String description, String type, int prereq, boolean pick, List<DataStatus> permStatus) {
        this.id = id;
        this.name = safe(name);
        this.refName = safe(refName);
        this.category = safe(category);
        this.description = safe(description);
        this.type = safe(type);
        this.prereq = prereq;
        this.pick = pick;
        this.permStatus = copyStatuses(permStatus);
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

    public int getPrereq() { return prereq; }
    public void setPrereq(int prereq) { this.prereq = prereq; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    @JsonIgnore
    public String getSource() { return type; }
    @JsonIgnore
    public void setSource(String source) { this.type = safe(source); }

    public boolean getPick() { return pick; }
    public void setPick(boolean pick) { this.pick = pick; }

    @JsonIgnore
    public List<DataStatus> getPermStatus() { return copyStatuses(permStatus); }

    public void setPermStatus(List<DataStatus> permStatus) { this.permStatus = copyStatuses(permStatus); }

    public void addPermStatus(DataStatus status) {
        if (status == null) return;
        if (this.permStatus == null) this.permStatus = new ArrayList<>();
        this.permStatus.add(new DataStatus(status));
    }

    // --- Helpers ---

    private static String safe(String s) { return s == null ? "" : s; }

    private static List<DataStatus> copyStatuses(List<DataStatus> statuses) {
        ArrayList<DataStatus> copies = new ArrayList<>();
        if (statuses == null) return copies;
        for (DataStatus status : statuses) {
            if (status != null) copies.add(new DataStatus(status));
        }
        return copies;
    }
    
    @Override
    public String toString() { return "DataSpecial {\n" + "  id: " + id + ",\n" + "  name: \"" + name + "\",\n" + "  refName: \"" + refName + "\",\n" + "  category: \"" + category + "\",\n" +
        "  description: \"" + description + "\",\n" + "  type: \"" + type + "\",\n" + "  prereq: " + prereq + ",\n" + "  pick: " + pick + "\n" + "}"; }
}
