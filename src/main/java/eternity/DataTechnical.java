package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Technical Information
 */
public class DataTechnical {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String category;
    @JsonProperty private String save;
    @JsonProperty private String effect;
    @JsonProperty private String description;
    @JsonProperty private String numDescription;
    @JsonProperty private String number;
    @JsonProperty private String duration;

    // --- Constructors ---

    public DataTechnical() { this(-1, "", "", "", "", "", "", "", ""); }
    public DataTechnical(DataTechnical src) {
        this(src.id, src.name, src.category, src.save, src.effect, src.description, src.numDescription, src.number, src.duration);
    }

    public DataTechnical(int id, String name, String category, String save, String effect, String description, String numDescription, String number, String duration) {
        this.id = id;
        this.name = safe(name);
        this.category = safe(category);
        this.save = safe(save);
        this.effect = safe(effect);
        this.description = safe(description);
        this.numDescription = safe(numDescription);
        this.number = safe(number);
        this.duration = safe(duration);
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = safe(category); }

    public String getSave() { return save; }
    public void setSave(String save) { this.save = safe(save); }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = safe(effect); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    public String getNumDescription() { return numDescription; }
    public void setNumDescription(String numDescription) { this.numDescription = safe(numDescription); }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = safe(number); }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = safe(duration); }

    // --- Helpers ---

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String toString() {
        return "DataTechnical{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", save='" + save + '\'' +
                ", effect='" + effect + '\'' +
                ", description='" + description + '\'' +
                ", numDescription='" + numDescription + '\'' +
                ", number='" + number + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
