package eternity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Deity Information
 */
public class DataDeity {
	@JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String description;
    @JsonProperty private List<String> virtues;
    @JsonProperty private List<String> domains;
    
    // --- Constructors ---

    public DataDeity() { this(-1, "", "", new ArrayList<>(), new ArrayList<>()); }
    public DataDeity(DataDeity src) { this(src.id, src.name, src.description, src.virtues, src.domains); }
    
    public DataDeity(int id, String name, String description, List<String> virtues, List<String> domains) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.virtues = new ArrayList<String>(virtues);
        this.domains = new ArrayList<String>(domains);
    }
    
    // --- Getters & Setters ---
    
    public int getID() { return id; }
    public void setID(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = (name != null ? name : ""); }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = (description != null ? description : ""); }

    public List<String> getVirtues() { return new ArrayList<>(virtues); }
    public void setVirtues(List<String> virtues) {
        if (virtues == null)
            this.virtues = new ArrayList<>();
        else
            this.virtues = new ArrayList<>(virtues);
    }

    public List<String> getDomains() { return new ArrayList<>(domains); }
    public void setDomains(List<String> domains) {
        if (domains == null)
            this.domains = new ArrayList<>();
        else
            this.domains = new ArrayList<>(domains);
    }

    // --- Helpers ---
    
    public void addVirtue(String virtue) { if (virtue != null && !virtue.isBlank()) virtues.add(virtue); }
    public void addDomain(String domain) { if (domain != null && !domain.isBlank()) domains.add(domain); }

    @Override
    public String toString() {
        return "DataDeity{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' + ", virtues=" + virtues +
                ", domains=" + domains + '}';
    }
}