package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Attribute Modification Information
 */
public class DataStatus {
    @JsonProperty private String name;
    @JsonProperty private String affinity;
    @JsonProperty private String description;
    @JsonProperty private String attribute;
    @JsonProperty private double severity;
    @JsonProperty private String durationType;
    @JsonProperty private int duration;

    // --- Constructors ---

    public DataStatus() { this("Base", "None", "None", "None", 0.0, "Permanent", 0); }
    public DataStatus(DataStatus src) { this(src.name, src.affinity, src.description, src.attribute, src.severity, src.durationType, src.duration); }

    public DataStatus(String name, String affinity, String description, String attribute, double severity, String durationType, int duration) {
        this.name = name;
        this.affinity = affinity;
        this.description = description;
        this.attribute = attribute;
        this.severity = severity;
        this.durationType = durationType;
        this.duration = duration;
    }

    // --- Getters & Setters ---
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAffinity() { return affinity; }
    public void setAffinity(String affinity) { this.affinity = affinity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public double getSeverity() { return severity; }
    public void setSeverity(double severity) { this.severity = severity; }

    public String getDurationType() { return durationType; }
    public void setDurationType(String durationType) { this.durationType = durationType; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    // --- Helpers ---
    
    @Override
    public String toString() { return "DataStatus{" + "name='" + name + '\'' + ", affinity='" + affinity + '\'' + ", description='" + description + '\'' +
        ", attribute='" + attribute + '\'' + ", severity=" + severity + ", durationType='" + durationType + '\'' + ", duration=" + duration + '}'; }
}