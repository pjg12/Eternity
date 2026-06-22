package eternity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Skill Information
 */
public class DataSkill {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String type;
    @JsonProperty private String description;
    @JsonProperty private List<String> availAttributes;
    @JsonProperty private List<String> chosenAttributes;
    @JsonProperty private String chosenSubtype;

    // --- Constructors ---

    public DataSkill() { this(-1, "", "", "", new ArrayList<>(), new ArrayList<>(), ""); }
    public DataSkill(DataSkill src) { this(src.id, src.name, src.type, src.description, src.availAttributes, src.chosenAttributes, src.chosenSubtype); }
    
    public DataSkill(int id, String name, String type, String description, List<String> availAttributes, List<String> chosenAttributes, String chosenSubtype) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.availAttributes = new ArrayList<>(availAttributes);
        this.chosenAttributes = new ArrayList<>(chosenAttributes);
        this.chosenSubtype = safe(chosenSubtype);
    }
    
    // --- Getters & Setters ---
    
    public int getID() { return id; }
    public void setID(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    public List<String> getAvailAttributes() { return new ArrayList<>(availAttributes); }
    public void setAvailAttributes(List<String> availAttributes) {
        if (availAttributes == null)
            this.availAttributes = new ArrayList<>();
        else
            this.availAttributes = new ArrayList<>(availAttributes);
    }

    public List<String> getChosenAttributes() { return new ArrayList<>(chosenAttributes); }
    public void setChosenAttributes(List<String> chosenAttributes) {
        if (chosenAttributes == null)
            this.chosenAttributes = new ArrayList<>();
        else
            this.chosenAttributes = new ArrayList<>(chosenAttributes);
    }

    public String getChosenSubtype() { return chosenSubtype; }
    public void setChosenSubtype(String chosenSubtype) { this.chosenSubtype = safe(chosenSubtype).trim(); }

    // --- Helpers ---
    
    public void addChosenAttribute(String att) {
        if (att == null || att.isBlank()) return;
        if (!chosenAttributes.contains(att)) chosenAttributes.add(att);
    }
    public void removeChosenAttribute(String att) { chosenAttributes.remove(att); }

    @JsonIgnore
    public boolean requiresSubtype() {
        if (availAttributes == null) return false;
        for (String attribute : availAttributes) {
            if (attribute != null && attribute.equalsIgnoreCase("ALL")) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    @Override
    public String toString() { return "DataSkill{" + "id=" + id + ", name='" + name + '\'' + ", type='" + type + '\'' + ", description='" + description + '\'' + ", chosenSubtype='" + chosenSubtype + '\'' + '}'; }
}
