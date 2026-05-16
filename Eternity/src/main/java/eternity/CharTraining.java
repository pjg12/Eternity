package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
    private StoreCharData parent;

    /** Natural elemental / cosmic affinities the character is born with. */
    @JsonProperty
    private final Set<String> naturalAffinities;

    /** Deity / cosmic domain associations (if used by your setting). */
    @JsonProperty
    private final Set<String> domains;

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
    @JsonIgnore
    private final Map<String, Set<Integer>> trainingIdsByCategory;
    @JsonIgnore
    private final Map<Integer, DataTraining> trainingById;
    @JsonIgnore
    private final Map<String, DataTraining> trainingByName;
    @JsonIgnore
    private final Map<String, List<DataTraining>> trainingViewsByCategory;
    @JsonIgnore
    private List<DataTraining> allTrainingCache;
    @JsonIgnore
    private boolean allTrainingDirty;
    @JsonIgnore
    private final Set<String> dirtyCategories;
    @JsonIgnore
    private final Set<String> sortedCategories;


    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharTraining() {
        this.naturalAffinities = new LinkedHashSet<>();
        this.domains = new LinkedHashSet<>();
        this.trainingByCategory = new HashMap<>();
        this.trainingIdsByCategory = new HashMap<>();
        this.trainingById = new HashMap<>();
        this.trainingByName = new HashMap<>();
        this.trainingViewsByCategory = new HashMap<>();
        this.allTrainingCache = List.of();
        this.allTrainingDirty = true;
        this.dirtyCategories = new HashSet<>();
        this.sortedCategories = new HashSet<>();

        this.isDeviant = false;
        this.classTrainingRank = 1;
    }


    // ---------------------------------------------------------
    // Affinities
    // ---------------------------------------------------------

    public List<String> getNaturalAffinities() {
        return List.copyOf(naturalAffinities);
    }

    public void setNaturalAffinities(List<String> affinities) {
        naturalAffinities.clear();
        if (affinities == null) return;
        for (String affinity : affinities) {
            addUniqueIgnoreCase(naturalAffinities, affinity);
        }
    }

    public void addNaturalAffinity(String affinity) {
        addUniqueIgnoreCase(naturalAffinities, affinity);
    }

    public void removeNaturalAffinity(String affinity) {
        if (affinity == null) return;
        naturalAffinities.removeIf(existing -> existing != null && existing.equalsIgnoreCase(affinity));
    }

    public boolean hasAffinity(String affinity) {
        if (affinity == null) return false;
        for (String existing : naturalAffinities) {
            if (existing != null && existing.equalsIgnoreCase(affinity)) return true;
        }
        return false;
    }


    // ---------------------------------------------------------
    // Domains
    // ---------------------------------------------------------

    public List<String> getDomains() {
        return List.copyOf(domains);
    }

    public void addDomain(String domain) {
        addUniqueIgnoreCase(domains, domain);
    }

    public void removeDomain(String domain) {
        if (domain == null) return;
        domains.removeIf(existing -> existing != null && existing.equalsIgnoreCase(domain));
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
        ensureTrainingState();
        DataTraining classTech = getTrainingById(23); // Class Training
        if (classTech != null && classTech.getRank() > 0) {
            classTrainingRank = classTech.getRank();
            return classTrainingRank;
        }
        if (classTrainingRank <= 0 && parent != null) {
            classTrainingRank = Math.max(1, parent.getLevel());
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
        //if (parent != null) dl = StoreMetaManager.getDataQuery().getLevel(parent.getLevel());
        if (dl != null) return Math.max(0, dl.getBaseTechs());
        return 0;
    }

    // ---------------------------------------------------------
    // Training Categories
    // ---------------------------------------------------------

    private List<DataTraining> getOrCreateCategory(String category) {
        String normalizedCategory = normalizeCategory(category);
        List<DataTraining> list = trainingByCategory.get(normalizedCategory);
        if (list != null) return list;
        List<DataTraining> created = new ArrayList<>();
        trainingByCategory.put(normalizedCategory, created);
        trainingViewsByCategory.put(normalizedCategory, Collections.unmodifiableList(created));
        trainingIdsByCategory.put(normalizedCategory, new HashSet<>());
        return created;
    }

    @JsonIgnore
    public Set<String> getTrainingCategories() {
        ensureTrainingState();
        return Collections.unmodifiableSet(trainingByCategory.keySet());
    }


    // ---------------------------------------------------------
    // Add / Remove / Retrieve Aura Techniques
    // ---------------------------------------------------------

    public List<DataTraining> getTrainingList(String category) {
        ensureTrainingState();
        String normalizedCategory = normalizeCategory(category);
        if (normalizedCategory == null) return Collections.emptyList();
        List<DataTraining> list = trainingByCategory.get(normalizedCategory);
        if (list == null) return Collections.emptyList();
        ensureCategorySorted(normalizedCategory, list);
        return trainingViewsByCategory.computeIfAbsent(normalizedCategory, ignored -> Collections.unmodifiableList(list));
    }

    public void addTraining(DataTraining tech) {
        ensureTrainingState();
        if (tech != null) {
            String category = normalizeCategory(tech.getAffinity());
            List<DataTraining> list = getOrCreateCategory(category);
            Set<Integer> categoryIds = trainingIdsByCategory.computeIfAbsent(category, ignored -> new HashSet<>());
            if (categoryIds.add(tech.getId())) {
                list.add(tech);
                indexTraining(tech);
                markCategoryDirty(category);
            }
        }
    }

    public void removeTraining(String category, DataTraining tech) {
        ensureTrainingState();
        String normalizedCategory = normalizeCategory(category);
        if (normalizedCategory != null && tech != null && trainingByCategory.containsKey(normalizedCategory)) {
            if (trainingByCategory.get(normalizedCategory).remove(tech)) {
                Set<Integer> categoryIds = trainingIdsByCategory.get(normalizedCategory);
                if (categoryIds != null) categoryIds.remove(tech.getId());
                removeTrainingIndexes(tech);
                markCategoryDirty(normalizedCategory);
            }
        }
    }


    // ---------------------------------------------------------
    // Lookup Helpers
    // ---------------------------------------------------------

    /** Search all categories for a training technique by its ID. */
    public DataTraining getTrainingById(int id) {
        ensureTrainingState();
        return trainingById.get(id);
    }

    /** Search all categories for a training technique by name. */
    public DataTraining getTrainingByName(String name) {
        ensureTrainingState();
        return trainingByName.get(normalizeName(name));
    }

    /** All aura techniques combined across every category. */
    @JsonIgnore
    public List<DataTraining> getAllTraining() {
        ensureTrainingState();
        if (!allTrainingDirty) {
            return allTrainingCache;
        }
        ArrayList<DataTraining> all = new ArrayList<>();
        for (var entry : trainingByCategory.entrySet()) {
            ensureCategorySorted(entry.getKey(), entry.getValue());
            all.addAll(entry.getValue());
        }
        allTrainingCache = Collections.unmodifiableList(all);
        allTrainingDirty = false;
        return allTrainingCache;
    }

    /**
     * Returns total ranks for all training categories, excluding any whose key matches
     * the provided skip list (case-insensitive).
     */
    @JsonIgnore
    public int getTotalRanksExcluding(List<String> skipCategories) {
        int total = 0;
        Set<String> normalizedSkips = new HashSet<>();
        if (skipCategories != null) {
            for (String category : skipCategories) {
                String normalized = normalizeName(category);
                if (normalized != null) normalizedSkips.add(normalized);
            }
        }
        for (var entry : trainingByCategory.entrySet()) {
            if (normalizedSkips.contains(normalizeName(entry.getKey()))) {
                continue;
            }
            for (DataTraining t : entry.getValue()) total += t.getRank();
        }
        return total;
    }

    /** Keeps each category list ordered by technique id for stable display/update behavior. */
    public void sortTrainingById() {
        ensureTrainingState();
        dirtyCategories.addAll(trainingByCategory.keySet());
        for (var entry : trainingByCategory.entrySet()) {
            ensureCategorySorted(entry.getKey(), entry.getValue());
        }
    }

    private void ensureTrainingState() {
        if (trainingByCategory.isEmpty()) {
            trainingViewsByCategory.clear();
            trainingIdsByCategory.clear();
            trainingById.clear();
            trainingByName.clear();
            allTrainingCache = List.of();
            allTrainingDirty = false;
            dirtyCategories.clear();
            sortedCategories.clear();
            return;
        }

        boolean needsRebuild = trainingById.isEmpty()
                || trainingViewsByCategory.size() != trainingByCategory.size()
                || trainingIdsByCategory.size() != trainingByCategory.size();
        if (!needsRebuild) {
            for (String category : trainingByCategory.keySet()) {
                String normalized = normalizeCategory(category);
                if (normalized == null || !normalized.equals(category)) {
                    needsRebuild = true;
                    break;
                }
            }
        }
        if (!needsRebuild) {
            return;
        }

        rebuildTrainingState();
    }

    private void rebuildTrainingState() {
        Map<String, List<DataTraining>> normalizedTraining = new HashMap<>();
        Map<String, Set<Integer>> normalizedIds = new HashMap<>();

        for (var entry : trainingByCategory.entrySet()) {
            String normalizedCategory = normalizeCategory(entry.getKey());
            if (normalizedCategory == null) continue;

            List<DataTraining> normalizedList = normalizedTraining.computeIfAbsent(normalizedCategory, ignored -> new ArrayList<>());
            Set<Integer> categoryIds = normalizedIds.computeIfAbsent(normalizedCategory, ignored -> new HashSet<>());
            List<DataTraining> sourceList = entry.getValue();
            if (sourceList == null) continue;

            for (DataTraining tech : sourceList) {
                if (tech == null) continue;
                if (categoryIds.add(tech.getId())) {
                    normalizedList.add(tech);
                }
            }
        }

        trainingByCategory.clear();
        trainingViewsByCategory.clear();
        trainingIdsByCategory.clear();
        trainingById.clear();
        trainingByName.clear();
        dirtyCategories.clear();
        sortedCategories.clear();

        for (var entry : normalizedTraining.entrySet()) {
            String category = entry.getKey();
            List<DataTraining> list = new ArrayList<>(entry.getValue());
            trainingByCategory.put(category, list);
            trainingViewsByCategory.put(category, Collections.unmodifiableList(list));
            trainingIdsByCategory.put(category, new HashSet<>(normalizedIds.getOrDefault(category, Set.of())));
            dirtyCategories.add(category);
            for (DataTraining tech : list) {
                indexTraining(tech);
            }
        }

        allTrainingDirty = true;
    }

    private void ensureCategorySorted(String category, List<DataTraining> list) {
        if (category == null || list == null) return;
        if (!dirtyCategories.contains(category) && sortedCategories.contains(category)) return;
        Comparator<DataTraining> byId = Comparator
                .comparingInt((DataTraining t) -> t == null ? Integer.MAX_VALUE : t.getId())
                .thenComparing(t -> t == null || t.getName() == null ? "" : t.getName(), String.CASE_INSENSITIVE_ORDER);
        list.sort(byId);
        dirtyCategories.remove(category);
        sortedCategories.add(category);
    }

    private void indexTraining(DataTraining tech) {
        if (tech == null) return;
        trainingById.put(tech.getId(), tech);
        String normalizedName = normalizeName(tech.getName());
        if (normalizedName != null) {
            trainingByName.put(normalizedName, tech);
        }
        allTrainingDirty = true;
    }

    private void removeTrainingIndexes(DataTraining tech) {
        if (tech == null) return;
        trainingById.remove(tech.getId());
        String normalizedName = normalizeName(tech.getName());
        if (normalizedName != null) {
            trainingByName.remove(normalizedName);
        }
        allTrainingDirty = true;
    }

    private void markCategoryDirty(String category) {
        if (category != null) {
            dirtyCategories.add(category);
            sortedCategories.remove(category);
        }
        allTrainingDirty = true;
    }

    private String normalizeCategory(String category) {
        return normalizeName(category);
    }

    private void addUniqueIgnoreCase(Set<String> values, String value) {
        String normalized = normalizeName(value);
        if (normalized == null || values == null) return;
        for (String existing : values) {
            if (existing != null && existing.equalsIgnoreCase(value)) {
                return;
            }
        }
        values.add(value);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase(Locale.ROOT);
    }

    

    @JsonIgnore
    public StoreCharData getParent() { return parent; }
    public void setParent(StoreCharData parent) { this.parent = parent; }
}

