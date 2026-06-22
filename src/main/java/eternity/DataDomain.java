package eternity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Domain Information
 */
public class DataDomain {
    private static final int BONUS_COUNT = 20;

    @JsonProperty private int id;
    @JsonProperty private String name;
    @JsonProperty private String condition;
    @JsonProperty private String[] bonus;

    // --- Constructors ---

    public DataDomain() { this(-1, "", "", new String[BONUS_COUNT]); }
    public DataDomain(DataDomain src) { this(src.id, src.name, src.condition, src.bonus); }

    public DataDomain(int id, String name, String condition, String[] bonus) {
        this.id = id;
        this.name = safe(name);
        this.condition = safe(condition);
        this.bonus = normalizeBonusArray(bonus);
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = safe(condition); }

    public String[] getBonus() { return bonus.clone(); }
    public void setBonus(String[] bonus) { this.bonus = normalizeBonusArray(bonus); }

    // --- Helpers ---

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String[] normalizeBonusArray(String[] values) {
        String[] normalized = new String[BONUS_COUNT];
        Arrays.fill(normalized, "");
        if (values == null) {
            return normalized;
        }
        int copyLength = Math.min(values.length, BONUS_COUNT);
        for (int i = 0; i < copyLength; i++) {
            normalized[i] = safe(values[i]);
        }
        return normalized;
    }

    @Override
    public String toString() {
        return "DataDomain{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", condition='" + condition + '\'' +
                ", bonus=" + Arrays.toString(bonus) +
                '}';
    }
}
