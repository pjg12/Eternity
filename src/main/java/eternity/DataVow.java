package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Vow Information
 */
public class DataVow {
    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String vowText;
    @JsonProperty private String vowEffect;
    @JsonProperty private String bonusType;
    @JsonProperty private String bonusEffect;

    // --- Constructors ---

    public DataVow() { this(-1, "", "", "", "", ""); }
    public DataVow(DataVow src) { this(src.id, src.name, src.vowText, src.vowEffect, src.bonusType, src.bonusEffect); }

    public DataVow(int id, String name, String vowText, String vowEffect, String bonusType, String bonusEffect) {
        this.id = id;
        this.name = safe(name);
        this.vowText = safe(vowText);
        this.vowEffect = safe(vowEffect);
        this.bonusType = safe(bonusType);
        this.bonusEffect = safe(bonusEffect);
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getVowText() { return vowText; }
    public void setVowText(String vowText) { this.vowText = safe(vowText); }

    public String getVowEffect() { return vowEffect; }
    public void setVowEffect(String vowEffect) { this.vowEffect = safe(vowEffect); }

    public String getBonusType() { return bonusType; }
    public void setBonusType(String bonusType) { this.bonusType = safe(bonusType); }

    public String getBonusEffect() { return bonusEffect; }
    public void setBonusEffect(String bonusEffect) { this.bonusEffect = safe(bonusEffect); }

    // --- Helpers ---

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String toString() {
        return "DataVow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", vowText='" + vowText + '\'' +
                ", vowEffect='" + vowEffect + '\'' +
                ", bonusType='" + bonusType + '\'' +
                ", bonusEffect='" + bonusEffect + '\'' +
                '}';
    }
}
