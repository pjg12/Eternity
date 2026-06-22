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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    /** Derived affinities granted by class/domain rules and rebuilt during updateAll(). */
    @JsonIgnore
    private final Set<String> derivedAffinities;

    /** Deity / cosmic domain associations (if used by your setting). */
    @JsonProperty
    private final Set<String> domains;

    /** Domain-derived effects currently available to the character. */
    @JsonProperty
    private final List<DataStatus> domainStatusEffects;

    /** Whether the character is Deviant (special case mutation). */
    @JsonProperty
    private boolean isDeviant;

    /** Legacy field retained for save compatibility; class progression now follows character level directly. */
    @JsonProperty
    private int classTrainingRank = 1;

    /** Unspent or tracked aggregate training XP for the character. */
    @JsonProperty
    private double trainingXp;

    /** Per-aura-type training XP totals, aligned to the tracked aura type list. */
    @JsonProperty
    private final List<Double> trainingXpByAuraType;

    /**
     * Training techniques organized by category.
     * 
     * Key: category string (e.g., "Attribute", "Affinity", "Spirit", "Metal", etc.)
     * Val: List of aura techniques trained under that category.
     */
    @JsonIgnore
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PersistedTrainingRecord {
        @JsonProperty
        private int id;
        @JsonProperty
        private int rank;

        public PersistedTrainingRecord() { }

        public PersistedTrainingRecord(DataTraining tech) {
            this.id = tech == null ? 0 : tech.getId();
            this.rank = tech == null ? 0 : tech.getRank();
        }

        public int getId() { return id; }
        public int getRank() { return rank; }
    }


    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharTraining() {
        this.naturalAffinities = new LinkedHashSet<>();
        this.derivedAffinities = new LinkedHashSet<>();
        this.domains = new LinkedHashSet<>();
        this.domainStatusEffects = new ArrayList<>();
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
        this.trainingXp = 0.0;
        this.trainingXpByAuraType = new ArrayList<>();
        initializeTrainingXpByAuraType();
    }


    // ---------------------------------------------------------
    // Affinities
    // ---------------------------------------------------------

    public List<String> getNaturalAffinities() {
        return List.copyOf(naturalAffinities);
    }

    @JsonIgnore
    public List<String> getDomainAffinities() {
        return List.copyOf(derivedAffinities);
    }

    @JsonIgnore
    public List<String> getAllAffinities() {
        LinkedHashSet<String> combined = new LinkedHashSet<>(naturalAffinities);
        combined.addAll(derivedAffinities);
        return List.copyOf(combined);
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

    public boolean hasNaturalAffinity(String affinity) {
        return containsIgnoreCase(naturalAffinities, affinity);
    }

    public boolean hasDomainAffinity(String affinity) {
        return containsIgnoreCase(derivedAffinities, affinity);
    }

    public boolean hasAffinity(String affinity) {
        return hasNaturalAffinity(affinity) || hasDomainAffinity(affinity);
    }

    public void clearDerivedAffinities() {
        derivedAffinities.clear();
    }

    public void addDerivedAffinity(String affinity) {
        addUniqueIgnoreCase(derivedAffinities, affinity);
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

    public void setDomains(List<String> domains) {
        this.domains.clear();
        if (domains == null) return;
        for (String domain : domains) {
            addUniqueIgnoreCase(this.domains, domain);
        }
    }

    public List<DataStatus> getDomainStatusEffects() {
        ArrayList<DataStatus> copies = new ArrayList<>(domainStatusEffects.size());
        for (DataStatus status : domainStatusEffects) {
            if (status != null) copies.add(new DataStatus(status));
        }
        return Collections.unmodifiableList(copies);
    }

    public void setDomainStatusEffects(List<DataStatus> statuses) {
        domainStatusEffects.clear();
        if (statuses == null) return;
        for (DataStatus status : statuses) {
            if (status != null) {
                domainStatusEffects.add(new DataStatus(status));
            }
        }
    }

    public void clearDomainStatusEffects() {
        domainStatusEffects.clear();
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
     * Class progression now follows character level directly.
     * The legacy Class Training technique is ignored.
     */
    public int getClassTrainingRank() {
        if (parent != null) {
            return Math.max(1, parent.getLevel());
        }
        return Math.max(1, classTrainingRank);
    }

    public void setClassTrainingRank(int classTrainingRank) {
        this.classTrainingRank = Math.max(1, classTrainingRank);
    }

    public double getTrainingXp() {
        return trainingXp;
    }

    public void setTrainingXp(double trainingXp) {
        this.trainingXp = Math.max(0.0, trainingXp);
    }

    public List<Double> getTrainingXpByAuraType() {
        ensureTrainingXpByAuraTypeSize();
        return Collections.unmodifiableList(trainingXpByAuraType);
    }

    public void setTrainingXpByAuraType(List<Double> values) {
        trainingXpByAuraType.clear();
        if (values != null) {
            if (values.size() == 19) {
                migrateLegacyAuraTypeXp(values);
            } else {
                for (Double value : values) {
                    trainingXpByAuraType.add(value == null ? 0.0 : Math.max(0.0, value));
                }
            }
        }
        ensureTrainingXpByAuraTypeSize();
    }

    public double getTrainingXpByAuraType(int index) {
        ensureTrainingXpByAuraTypeSize();
        if (index < 0 || index >= trainingXpByAuraType.size()) return 0.0;
        Double value = trainingXpByAuraType.get(index);
        return value == null ? 0.0 : value;
    }

    public void setTrainingXpByAuraType(int index, double value) {
        ensureTrainingXpByAuraTypeSize();
        if (index < 0 || index >= trainingXpByAuraType.size()) return;
        trainingXpByAuraType.set(index, Math.max(0.0, value));
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

    private void initializeTrainingXpByAuraType() {
        for (int i = 0; i < getTrackedAuraTypeCount(); i++) {
            trainingXpByAuraType.add(0.0);
        }
    }

    private void ensureTrainingXpByAuraTypeSize() {
        while (trainingXpByAuraType.size() < getTrackedAuraTypeCount()) {
            trainingXpByAuraType.add(0.0);
        }
        while (trainingXpByAuraType.size() > getTrackedAuraTypeCount()) {
            trainingXpByAuraType.remove(trainingXpByAuraType.size() - 1);
        }
    }

    private int getTrackedAuraTypeCount() {
        return Math.max(0, FrameTrainingExp.AURA_TYPES.length - 1);
    }

    private void migrateLegacyAuraTypeXp(List<Double> legacyValues) {
        for (int i = 0; i < 6; i++) {
            trainingXpByAuraType.add(0.0);
        }
        for (Double value : legacyValues) {
            trainingXpByAuraType.add(value == null ? 0.0 : Math.max(0.0, value));
        }
    }

    @JsonIgnore
    public Set<String> getTrainingCategories() {
        ensureTrainingState();
        return Collections.unmodifiableSet(trainingByCategory.keySet());
    }

    @JsonProperty("trainingByCategory")
    private Map<String, List<PersistedTrainingRecord>> getPersistedTrainingByCategory() {
        ensureTrainingState();
        Map<String, List<PersistedTrainingRecord>> persisted = new HashMap<>();
        for (var entry : trainingByCategory.entrySet()) {
            String category = entry.getKey();
            List<DataTraining> source = entry.getValue();
            if (category == null || source == null) continue;
            ArrayList<PersistedTrainingRecord> records = new ArrayList<>();
            for (DataTraining tech : source) {
                if (tech == null || isDeprecatedTraining(tech)) continue;
                records.add(new PersistedTrainingRecord(tech));
            }
            persisted.put(category, records);
        }
        return persisted;
    }

    @JsonProperty("trainingByCategory")
    private void setPersistedTrainingByCategory(Map<String, List<PersistedTrainingRecord>> persistedTrainingByCategory) {
        clearTrainingState();
        if (persistedTrainingByCategory == null || persistedTrainingByCategory.isEmpty()) return;

        StoreRuleManager ruleManager = new StoreRuleManager();
        for (var entry : persistedTrainingByCategory.entrySet()) {
            List<PersistedTrainingRecord> records = entry.getValue();
            if (records == null) continue;
            for (PersistedTrainingRecord record : records) {
                if (record == null || record.getId() <= 0) continue;
                DataTraining template = ruleManager.getTrainingById(record.getId());
                if (template == null) continue;
                DataTraining rebuilt = new DataTraining(template);
                rebuilt.setRank(Math.max(0, record.getRank()));
                rebuilt.setAl(0);
                addTraining(rebuilt);
            }
        }
        sortTrainingById();
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
            if (isDeprecatedTraining(tech)) return;
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
                if (isDeprecatedTraining(tech)) continue;
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

    private void clearTrainingState() {
        trainingByCategory.clear();
        trainingViewsByCategory.clear();
        trainingIdsByCategory.clear();
        trainingById.clear();
        trainingByName.clear();
        allTrainingCache = List.of();
        allTrainingDirty = false;
        dirtyCategories.clear();
        sortedCategories.clear();
    }

    private boolean isDeprecatedTraining(DataTraining tech) {
        if (tech == null) return false;
        if (tech.getId() == 23) return true;
        String name = tech.getName();
        return name != null && name.equalsIgnoreCase("Class Training");
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
        if (containsIgnoreCase(values, value)) return;
        values.add(value);
    }

    private boolean containsIgnoreCase(Set<String> values, String value) {
        if (values == null || value == null) return false;
        for (String existing : values) {
            if (existing != null && existing.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase(Locale.ROOT);
    }

    

    @JsonIgnore
    public StoreCharData getParent() { return parent; }
    public void setParent(StoreCharData parent) { this.parent = parent; }
}
