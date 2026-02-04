package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tracks character affinities, domains, and all learned / trainable Aura Techniques.
 */
public class CharTraining {

    /** The parent character data object. */
    @JsonIgnore
    private CharData parent;

    /** Natural elemental / cosmic affinities the character is born with. */
    @JsonProperty
    private final List<String> naturalAffinities;

    /** Deity / cosmic domain associations (if used by your setting). */
    @JsonProperty
    private final List<String> domains;

    /** Whether the character is Deviant (special case mutation). */
    @JsonProperty
    private boolean isDeviant;

    /** Rank used for class-based training progression (may differ from character level). */
    @JsonProperty
    private int classTrainingRank = 1;

    /**
     * Training techniques organized by category.
     * 
     * Key: category string (e.g., "Attribute", "Affinity", "Spirit", "Metal", etc.)
     * Val: List of aura techniques trained under that category.
     */
    @JsonProperty
    private final Map<String, List<DataTraining>> trainingByCategory;


    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharTraining() {
        this.naturalAffinities = new ArrayList<>();
        this.domains = new ArrayList<>();
        this.trainingByCategory = new HashMap<>();

        this.isDeviant = false;
        this.classTrainingRank = 1;
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
    // Class training rank
    // ---------------------------------------------------------

    /**
     * Returns the current class training rank. Falls back to the parent's level if unset.
     */
    public int getClassTrainingRank() {
        if (classTrainingRank <= 0 && parent != null) {
            return Math.max(1, parent.getLevel());
        }
        return Math.max(1, classTrainingRank);
    }

    public void setClassTrainingRank(int classTrainingRank) {
        this.classTrainingRank = Math.max(1, classTrainingRank);
    }


    // ---------------------------------------------------------
    // Max Tech Counts
    // ---------------------------------------------------------

    /**
     * Computes base max techs for a given level using DataLevel.getBaseTechs.
     * Returns 0 if level data is unavailable.
     */
    @JsonIgnore
    public int getBaseMaxTechs() {
        DataLevel dl = null;
        if (parent != null) dl = new DataQuery().getLevel(parent.getLevel());
        if (dl != null) return Math.max(0, dl.getBaseTechs());
        return 0;
    }

    // ---------------------------------------------------------
    // Training Categories
    // ---------------------------------------------------------

    private List<DataTraining> getOrCreateCategory(String category) {
        return trainingByCategory.computeIfAbsent(category, k -> new ArrayList<>());
    }

    @JsonIgnore
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

    public void addTraining(DataTraining tech) {
        if (tech != null) {
            List<DataTraining> list = getOrCreateCategory(tech.getAffinity());
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

    /** All aura techniques combined across every category. */
    @JsonIgnore
    public List<DataTraining> getAllTraining() {
        ArrayList<DataTraining> all = new ArrayList<>();
        for (var list : trainingByCategory.values()) all.addAll(list);
        return all;
    }

    /**
     * Returns total ranks for all training categories, excluding any whose key matches
     * the provided skip list (case-insensitive).
     */
    @JsonIgnore
    public int getTotalRanksExcluding(List<String> skipCategories) {
        int total = 0;
        for (var entry : trainingByCategory.entrySet()) {
            String cat = entry.getKey();
            if (skipCategories != null && skipCategories.stream().anyMatch(s -> s.equalsIgnoreCase(cat))) {
                continue;
            }
            for (DataTraining t : entry.getValue()) total += t.getRank();
        }
        return total;
    }

    

    @JsonIgnore
    public CharData getParent() { return parent; }
    public void setParent(CharData parent) { this.parent = parent; }
}
