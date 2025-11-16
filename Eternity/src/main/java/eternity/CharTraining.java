package eternity;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tracks character affinities, domains, and all learned / trainable Aura Techniques.
 */
public class CharTraining {

    // ---------------------------------------------------------
    // Core Character Training Data
    // ---------------------------------------------------------

    /** Natural elemental / cosmic affinities the character is born with. */
    @JsonProperty
    private final List<String> naturalAffinities;

    /** Deity / cosmic domain associations (if used by your setting). */
    @JsonProperty
    private final List<String> domains;

    /** Whether the character is Deviant (special case mutation). */
    @JsonProperty
    private boolean isDeviant;

    /** How many aura-techs the character can normally know. */
    @JsonProperty
    private int baseMaxTechs;

    /** Scaling multiplier applied to max tech count. */
    @JsonProperty
    private double maxTechMultiplier;

    /**
     * Training techniques organized by category.
     * 
     * Key: category string (e.g., "Attribute", "Affinity", "Spirit", "Metal", etc.)
     * Val: List of aura techniques trained under that category.
     */
    @JsonProperty("techByCategory")
    private final Map<String, List<DataTraining>> trainingByCategory;


    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharTraining() {
        this.naturalAffinities = new ArrayList<>();
        this.domains = new ArrayList<>();
        this.trainingByCategory = new HashMap<>();

        this.isDeviant = false;
        this.baseMaxTechs = 0;
        this.maxTechMultiplier = 1.0;
    }


    // ---------------------------------------------------------
    // Affinities
    // ---------------------------------------------------------

    public List<String> getNaturalAffinities() {
        return Collections.unmodifiableList(naturalAffinities);
    }

    public void addNaturalAffinity(String affinity) {
        if (affinity != null && !naturalAffinities.contains(affinity)) {
            naturalAffinities.add(affinity);
        }
    }

    public void removeNaturalAffinity(String affinity) {
        naturalAffinities.remove(affinity);
    }

    public boolean hasAffinity(String affinity) {
        return naturalAffinities.contains(affinity);
    }


    // ---------------------------------------------------------
    // Domains
    // ---------------------------------------------------------

    public List<String> getDomains() {
        return Collections.unmodifiableList(domains);
    }

    public void addDomain(String domain) {
        if (domain != null && !domains.contains(domain)) {
            domains.add(domain);
        }
    }

    public void removeDomain(String domain) {
        domains.remove(domain);
    }


    // ---------------------------------------------------------
    // Deviant Status
    // ---------------------------------------------------------

    public boolean isDeviant() {
        return isDeviant;
    }

    public void setDeviant(boolean deviant) {
        this.isDeviant = deviant;
    }


    // ---------------------------------------------------------
    // Max Tech Counts
    // ---------------------------------------------------------

    public int getBaseMaxTechs() {
        return baseMaxTechs;
    }

    public void setBaseMaxTechs(int baseMaxTechs) {
        this.baseMaxTechs = Math.max(0, baseMaxTechs);
    }

    public double getMaxTechMultiplier() {
        return maxTechMultiplier;
    }

    public void setMaxTechMultiplier(double multiplier) {
        this.maxTechMultiplier = multiplier <= 0 ? 1.0 : multiplier;
    }

    public int getFinalMaxTechs() {
        return (int) Math.floor(baseMaxTechs * maxTechMultiplier);
    }


    // ---------------------------------------------------------
    // Training Categories
    // ---------------------------------------------------------

    private List<DataTraining> getOrCreateCategory(String category) {
        return trainingByCategory.computeIfAbsent(category, k -> new ArrayList<>());
    }

    public Set<String> getTrainingCategories() {
        return Collections.unmodifiableSet(trainingByCategory.keySet());
    }


    // ---------------------------------------------------------
    // Add / Remove / Retrieve Aura Techniques
    // ---------------------------------------------------------

    public List<DataTraining> getTrainingList(String category) {
        return trainingByCategory.containsKey(category)
                ? Collections.unmodifiableList(trainingByCategory.get(category))
                : Collections.emptyList();
    }

    public void addTraining(String category, DataTraining tech) {
        if (category != null && tech != null) {
            List<DataTraining> list = getOrCreateCategory(category);
            if (!list.contains(tech)) {
                list.add(tech);
            }
        }
    }

    public void removeTraining(String category, DataTraining tech) {
        if (category != null && tech != null && trainingByCategory.containsKey(category)) {
            trainingByCategory.get(category).remove(tech);
        }
    }


    // ---------------------------------------------------------
    // Lookup Helpers
    // ---------------------------------------------------------

    /** Search all categories for a training technique by its ID. */
    public DataTraining getTrainingById(int id) {
        for (var list : trainingByCategory.values()) {
            for (var t : list) {
                if (t.getId() == id) return t;
            }
        }
        return null;
    }

    /** Search all categories for a training technique by name. */
    public DataTraining getTrainingByName(String name) {
        for (var list : trainingByCategory.values()) {
            for (var t : list) {
                if (t.getName().equalsIgnoreCase(name)) return t;
            }
        }
        return null;
    }

    /** All aura techniques combined. */
    public List<DataTraining> getAllTraining() {
        ArrayList<DataTraining> all = new ArrayList<>();
        for (var list : trainingByCategory.values()) all.addAll(list);
        return all;
    }
}