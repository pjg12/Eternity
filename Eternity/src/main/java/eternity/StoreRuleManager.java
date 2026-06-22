// CHECKED

package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreRuleManager {
    private static final StoreRuleData RULE_DATA = new StoreRuleData();
    private static final QueryIndex QUERY_INDEX = new QueryIndex(RULE_DATA);

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    
    public StoreRuleManager() { /* No Code Needed */ }

    // ---------------------------------------------------------
    // Utility helpers
    // ---------------------------------------------------------

    private static String normalizeKey(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static boolean containsNormalized(String text, String normalizedSearch) {
        return text != null && normalizedSearch != null && text.contains(normalizedSearch);
    }

    // ---------------------------------------------------------
    // COLOR SEARCH
    // ---------------------------------------------------------

    public DataColor getColorByTitle(String name) {
        return QUERY_INDEX.colorsByTitle.get(normalizeKey(name));
    }

    public List<DataColor> searchColorsByTitle(String namePart) {
        return searchByEntries(QUERY_INDEX.colorTitleSearch, normalizeKey(namePart), QUERY_INDEX.colorSearchCache);
    }

    // ---------------------------------------------------------
    // LEVEL SEARCH
    // ---------------------------------------------------------

    public DataLevel getLevel(int level) {
        return QUERY_INDEX.levelsByLevel.get(level);
    }

    // ---------------------------------------------------------
    // RACE SEARCH
    // ---------------------------------------------------------

    public DataRace getRaceById(int id) {
        return QUERY_INDEX.racesById.get(id);
    }

    public DataRace getRaceByName(String name) {
        return QUERY_INDEX.racesByName.get(normalizeKey(name));
    }

    public List<DataRace> getRaceData() {
        return RULE_DATA.getRaceData();
    }

    public List<DataRace> searchRaceByName(String namePart) {
        return searchByEntries(QUERY_INDEX.raceNameSearch, normalizeKey(namePart), QUERY_INDEX.raceSearchCache);
    }

    // ---------------------------------------------------------
    // CLASS SEARCH
    // ---------------------------------------------------------

    public DataClass getClassById(int id) {
        return QUERY_INDEX.classesById.get(id);
    }

    public DataClass getClassByName(String name) {
        return QUERY_INDEX.classesByName.get(normalizeKey(name));
    }

    public List<DataClass> searchClassByName(String namePart) {
        return searchByEntries(QUERY_INDEX.classNameSearch, normalizeKey(namePart), QUERY_INDEX.classSearchCache);
    }

    // ---------------------------------------------------------
    // DEITY SEARCH
    // ---------------------------------------------------------

    public DataDeity getDeityById(int id) {
        return QUERY_INDEX.deitiesById.get(id);
    }

    public DataDeity getDeityByName(String name) {
        return QUERY_INDEX.deitiesByName.get(normalizeKey(name));
    }

    public List<DataDeity> searchDeities(String namePart) {
        return searchByEntries(QUERY_INDEX.deityNameSearch, normalizeKey(namePart), QUERY_INDEX.deitySearchCache);
    }

    // ---------------------------------------------------------
    // DOMAIN SEARCH
    // ---------------------------------------------------------

    public DataDomain getDomainById(int id) {
        return QUERY_INDEX.domainsById.get(id);
    }

    public DataDomain getDomainByName(String name) {
        return QUERY_INDEX.domainsByName.get(normalizeKey(name));
    }

    public List<DataDomain> getDomainData() {
        return RULE_DATA.getDomainData();
    }

    public List<DataTechnical> getTechnicalData() {
        return RULE_DATA.getTechnicalData();
    }

    public DataTechnical getTechnicalByName(String name) {
        if (name == null || name.isBlank()) return null;
        String normalizedName = normalizeKey(name);
        for (DataTechnical technical : RULE_DATA.getTechnicalData()) {
            if (technical == null || technical.getName() == null) continue;
            if (normalizedName.equals(normalizeKey(technical.getName()))) return technical;
        }
        for (DataTechnical technical : RULE_DATA.getTechnicalData()) {
            if (technical == null || technical.getName() == null) continue;
            String candidate = normalizeKey(technical.getName());
            if (candidate != null && isBoundedEditDistance(normalizedName, candidate, 1)) {
                return technical;
            }
        }
        return null;
    }

    private boolean isBoundedEditDistance(String left, String right, int maxDistance) {
        if (left == null || right == null) return false;
        int leftLength = left.length();
        int rightLength = right.length();
        if (Math.abs(leftLength - rightLength) > maxDistance) return false;

        int[] previous = new int[rightLength + 1];
        int[] current = new int[rightLength + 1];
        for (int j = 0; j <= rightLength; j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= leftLength; i++) {
            current[0] = i;
            int rowMin = current[0];
            for (int j = 1; j <= rightLength; j++) {
                int substitutionCost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + substitutionCost);
                rowMin = Math.min(rowMin, current[j]);
            }
            if (rowMin > maxDistance) return false;

            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[rightLength] <= maxDistance;
    }

    public List<DataVow> getVowData() {
        return RULE_DATA.getVowData();
    }

    public DataVow getVowById(int id) {
        for (DataVow vow : RULE_DATA.getVowData()) {
            if (vow != null && vow.getId() == id) return vow;
        }
        return null;
    }

    public DataVow getVowByName(String name) {
        if (name == null || name.isBlank()) return null;
        String normalizedName = normalizeKey(name);
        for (DataVow vow : RULE_DATA.getVowData()) {
            if (vow == null || vow.getName() == null) continue;
            if (normalizedName.equals(normalizeKey(vow.getName()))) return vow;
        }
        return null;
    }

    public List<DataDomain> searchDomains(String namePart) {
        return searchByEntries(QUERY_INDEX.domainNameSearch, normalizeKey(namePart), QUERY_INDEX.domainSearchCache);
    }

    // ---------------------------------------------------------
    // SKILL SEARCH
    // ---------------------------------------------------------

    public DataSkill getSkillById(int id) {
        return QUERY_INDEX.skillsById.get(id);
    }

    public DataSkill getSkillByName(String name) {
        return QUERY_INDEX.skillsByName.get(normalizeKey(name));
    }

    public List<DataSkill> searchSkills(String namePart) {
        return searchByEntries(QUERY_INDEX.skillNameSearch, normalizeKey(namePart), QUERY_INDEX.skillSearchCache);
    }

    public List<DataSkill> getSkillsByAttribute(String attribute) {
        if (attribute == null || attribute.equals("***")) return List.of();
        String normalizedAttribute = normalizeKey(attribute);
        List<DataSkill> directSkills = QUERY_INDEX.skillsByAttribute.get(normalizedAttribute);
        List<DataSkill> allSkills = QUERY_INDEX.skillsByAttribute.get("all");
        if ((allSkills == null || allSkills.isEmpty())) {
            return directSkills != null ? directSkills : List.of();
        }
        if (directSkills == null || directSkills.isEmpty()) {
            return allSkills;
        }

        LinkedHashSet<DataSkill> merged = new LinkedHashSet<>(directSkills);
        merged.addAll(allSkills);
        return List.copyOf(merged);
    }

    // ---------------------------------------------------------
    // SPECIALTY SEARCH
    // ---------------------------------------------------------

    public DataSpecialty getSpecialtyById(int id) {
        return QUERY_INDEX.specialtiesById.get(id);
    }

    public List<DataSpecialty> getAllSpecialty() {
        return RULE_DATA.getSpecialtyData();
    }

    public List<DataSpecialty> getSpecialtiesByType(String type) {
        if (type == null || type.equals("***")) return RULE_DATA.getSpecialtyData();
        return QUERY_INDEX.specialtiesByType.getOrDefault(normalizeKey(type), List.of());
    }

    public List<DataSpecialty> searchSpecialties(String namePart) {
        return searchByEntries(QUERY_INDEX.specialtyNameSearch, normalizeKey(namePart), QUERY_INDEX.specialtySearchCache);
    }

    public DataSpecialty getSpecialtyByName(String name) {
        return QUERY_INDEX.specialtiesByName.get(normalizeKey(name));
    }

    // ---------------------------------------------------------
    // TECH PERMISSION SEARCH
    // ---------------------------------------------------------

    public List<DataTechPerm> getTechPermData() {
        return RULE_DATA.getTechPermData();
    }

    public DataTechPerm getTechPermById(int id) {
        return QUERY_INDEX.techPermsById.get(id);
    }

    // ---------------------------------------------------------
    // EQUIPMENT SEARCH
    // ---------------------------------------------------------

    public DataItemEquipment getItemByDid(int did) {
        return QUERY_INDEX.itemsByDid.get(did);
    }

    public List<DataItemEquipment> getItemEquipmentData() {
        return RULE_DATA.getItemEquipmentData();
    }

    public List<DataItemEquipment> searchItems(String namePart) {
        return searchByEntries(QUERY_INDEX.itemNameSearch, normalizeKey(namePart), QUERY_INDEX.itemSearchCache);
    }

    public DataItemEquipment getItemByName(String name) {
        if (name == null) return null;
        return QUERY_INDEX.itemsByName.get(normalizeKey(name));
    }

    public DataItemWeapon getWeaponByDid(int did) {
        return QUERY_INDEX.weaponsByDid.get(did);
    }

    public List<DataItemWeapon> getItemWeaponData() {
        return RULE_DATA.getItemWeaponData();
    }

    public List<DataItemWeapon> searchWeapons(String namePart) {
        return searchByEntries(QUERY_INDEX.weaponNameSearch, normalizeKey(namePart), QUERY_INDEX.weaponSearchCache);
    }

    public DataItemWeapon getWeaponByName(String name) {
        if (name == null) return null;
        return QUERY_INDEX.weaponsByName.get(normalizeKey(name));
    }

    // ---------------------------------------------------------
    // TRAINING SEARCH
    // ---------------------------------------------------------

    public DataTraining getTrainingById(int id) {
        return QUERY_INDEX.trainingById.get(id);
    }

    public List<DataTraining> getTrainingData() {
        return RULE_DATA.getTrainingData();
    }

    public List<DataTraining> searchTraining(String namePart) {
        return searchByEntries(QUERY_INDEX.trainingNameSearch, normalizeKey(namePart), QUERY_INDEX.trainingSearchCache);
    }

    // ---------------------------------------------------------
    // ACTION SEARCH
    // ---------------------------------------------------------

    public List<DataAction> getActionData() {
        return RULE_DATA.getActionData();
    }

    public DataAction getActionById(int id) {
        return QUERY_INDEX.actionsById.get(id);
    }

    public DataAction getActionByName(String name) {
        return QUERY_INDEX.actionsByName.get(normalizeKey(name));
    }

    public List<DataAction> searchActions(String namePart) {
        return searchByEntries(QUERY_INDEX.actionNameSearch, normalizeKey(namePart), QUERY_INDEX.actionSearchCache);
    }

    public List<DataAction> getActionsBySource(String source) {
        if (source == null || source.equals("***")) return RULE_DATA.getActionData();
        return QUERY_INDEX.actionsBySource.getOrDefault(normalizeKey(source), List.of());
    }

    private static <T> List<T> searchByEntries(List<SearchEntry<T>> entries, String normalizedSearch, Map<String, List<T>> cache) {
        if (normalizedSearch == null || normalizedSearch.isBlank()) return List.of();
        List<T> cached = cache.get(normalizedSearch);
        if (cached != null) {
            return cached;
        }
        List<T> matches = new ArrayList<>(Math.min(entries.size(), 16));
        for (SearchEntry<T> entry : entries) {
            if (containsNormalized(entry.searchText(), normalizedSearch)) {
                matches.add(entry.value());
            }
        }
        List<T> result = matches.isEmpty() ? List.of() : Collections.unmodifiableList(matches);
        cache.put(normalizedSearch, result);
        return result;
    }

    private record SearchEntry<T>(String searchText, T value) {}

    private static final class QueryIndex {
        private final Map<String, DataColor> colorsByTitle;
        private final Map<Integer, DataLevel> levelsByLevel;
        private final Map<Integer, DataRace> racesById;
        private final Map<String, DataRace> racesByName;
        private final List<SearchEntry<DataRace>> raceNameSearch;
        private final Map<Integer, DataClass> classesById;
        private final Map<String, DataClass> classesByName;
        private final List<SearchEntry<DataClass>> classNameSearch;
        private final Map<Integer, DataDeity> deitiesById;
        private final Map<String, DataDeity> deitiesByName;
        private final List<SearchEntry<DataDeity>> deityNameSearch;
        private final Map<Integer, DataDomain> domainsById;
        private final Map<String, DataDomain> domainsByName;
        private final List<SearchEntry<DataDomain>> domainNameSearch;
        private final Map<Integer, DataSkill> skillsById;
        private final Map<String, DataSkill> skillsByName;
        private final Map<String, List<DataSkill>> skillsByAttribute;
        private final List<SearchEntry<DataSkill>> skillNameSearch;
        private final Map<Integer, DataSpecialty> specialtiesById;
        private final Map<String, DataSpecialty> specialtiesByName;
        private final Map<String, List<DataSpecialty>> specialtiesByType;
        private final List<SearchEntry<DataSpecialty>> specialtyNameSearch;
        private final Map<Integer, DataTechPerm> techPermsById;
        private final Map<Integer, DataItemEquipment> itemsByDid;
        private final Map<String, DataItemEquipment> itemsByName;
        private final List<SearchEntry<DataItemEquipment>> itemNameSearch;
        private final Map<Integer, DataItemWeapon> weaponsByDid;
        private final Map<String, DataItemWeapon> weaponsByName;
        private final List<SearchEntry<DataItemWeapon>> weaponNameSearch;
        private final Map<Integer, DataTraining> trainingById;
        private final List<SearchEntry<DataTraining>> trainingNameSearch;
        private final Map<Integer, DataAction> actionsById;
        private final Map<String, DataAction> actionsByName;
        private final Map<String, List<DataAction>> actionsBySource;
        private final List<SearchEntry<DataAction>> actionNameSearch;
        private final List<SearchEntry<DataColor>> colorTitleSearch;
        private final Map<String, List<DataColor>> colorSearchCache;
        private final Map<String, List<DataRace>> raceSearchCache;
        private final Map<String, List<DataClass>> classSearchCache;
        private final Map<String, List<DataDeity>> deitySearchCache;
        private final Map<String, List<DataDomain>> domainSearchCache;
        private final Map<String, List<DataSkill>> skillSearchCache;
        private final Map<String, List<DataSpecialty>> specialtySearchCache;
        private final Map<String, List<DataItemEquipment>> itemSearchCache;
        private final Map<String, List<DataItemWeapon>> weaponSearchCache;
        private final Map<String, List<DataTraining>> trainingSearchCache;
        private final Map<String, List<DataAction>> actionSearchCache;

        private QueryIndex(StoreRuleData store) {
            List<DataColor> colorData = store.getColorData();
            List<DataLevel> levelData = store.getLevelData();
            List<DataRace> raceData = store.getRaceData();
            List<DataClass> classData = store.getClassData();
            List<DataDeity> deityData = store.getDeityData();
            List<DataDomain> domainData = store.getDomainData();
            List<DataSkill> skillData = store.getSkillData();
            List<DataSpecialty> specialtyData = store.getSpecialtyData();
            List<DataTechPerm> techPermData = store.getTechPermData();
            List<DataItemEquipment> itemEquipmentData = store.getItemEquipmentData();
            List<DataItemWeapon> itemWeaponData = store.getItemWeaponData();
            List<DataTraining> trainingData = store.getTrainingData();
            List<DataAction> actionData = store.getActionData();

            colorsByTitle = new HashMap<>(Math.max(16, colorData.size() * 2));
            colorTitleSearch = new ArrayList<>(colorData.size());
            colorSearchCache = new ConcurrentHashMap<>();
            for (DataColor color : colorData) {
                if (color != null && color.getTitle() != null) {
                    String normalizedTitle = normalizeKey(color.getTitle());
                    colorsByTitle.putIfAbsent(normalizedTitle, color);
                    colorTitleSearch.add(new SearchEntry<>(normalizedTitle, color));
                }
            }

            levelsByLevel = new HashMap<>(Math.max(16, levelData.size() * 2));
            for (DataLevel level : levelData) {
                if (level != null) {
                    levelsByLevel.putIfAbsent(level.getLevel(), level);
                }
            }

            racesById = new HashMap<>(Math.max(16, raceData.size() * 2));
            racesByName = new HashMap<>(Math.max(16, raceData.size() * 2));
            raceNameSearch = new ArrayList<>(raceData.size());
            raceSearchCache = new ConcurrentHashMap<>();
            for (DataRace race : raceData) {
                if (race == null) continue;
                racesById.putIfAbsent(race.getID(), race);
                if (race.getName() != null) {
                    String normalizedName = normalizeKey(race.getName());
                    racesByName.putIfAbsent(normalizedName, race);
                    raceNameSearch.add(new SearchEntry<>(normalizedName, race));
                }
            }

            classesById = new HashMap<>(Math.max(16, classData.size() * 2));
            classesByName = new HashMap<>(Math.max(16, classData.size() * 2));
            classNameSearch = new ArrayList<>(classData.size());
            classSearchCache = new ConcurrentHashMap<>();
            for (DataClass dataClass : classData) {
                if (dataClass == null) continue;
                classesById.putIfAbsent(dataClass.getID(), dataClass);
                if (dataClass.getName() != null) {
                    String normalizedName = normalizeKey(dataClass.getName());
                    classesByName.putIfAbsent(normalizedName, dataClass);
                    classNameSearch.add(new SearchEntry<>(normalizedName, dataClass));
                }
            }

            deitiesById = new HashMap<>(Math.max(16, deityData.size() * 2));
            deitiesByName = new HashMap<>(Math.max(16, deityData.size() * 2));
            deityNameSearch = new ArrayList<>(deityData.size());
            deitySearchCache = new ConcurrentHashMap<>();
            for (DataDeity deity : deityData) {
                if (deity == null) continue;
                deitiesById.putIfAbsent(deity.getID(), deity);
                if (deity.getName() != null) {
                    String normalizedName = normalizeKey(deity.getName());
                    deitiesByName.putIfAbsent(normalizedName, deity);
                    deityNameSearch.add(new SearchEntry<>(normalizedName, deity));
                }
            }

            domainsById = new HashMap<>(Math.max(16, domainData.size() * 2));
            domainsByName = new HashMap<>(Math.max(16, domainData.size() * 2));
            domainNameSearch = new ArrayList<>(domainData.size());
            domainSearchCache = new ConcurrentHashMap<>();
            for (DataDomain domain : domainData) {
                if (domain == null) continue;
                domainsById.putIfAbsent(domain.getId(), domain);
                if (domain.getName() != null) {
                    String normalizedName = normalizeKey(domain.getName());
                    domainsByName.putIfAbsent(normalizedName, domain);
                    domainNameSearch.add(new SearchEntry<>(normalizedName, domain));
                }
            }

            skillsById = new HashMap<>(Math.max(16, skillData.size() * 2));
            skillsByName = new HashMap<>(Math.max(16, skillData.size() * 2));
            skillsByAttribute = new HashMap<>(Math.max(16, skillData.size() * 2));
            skillNameSearch = new ArrayList<>(skillData.size());
            skillSearchCache = new ConcurrentHashMap<>();
            for (DataSkill skill : skillData) {
                if (skill == null) continue;
                skillsById.putIfAbsent(skill.getID(), skill);
                if (skill.getName() != null) {
                    String normalizedName = normalizeKey(skill.getName());
                    skillsByName.putIfAbsent(normalizedName, skill);
                    skillNameSearch.add(new SearchEntry<>(normalizedName, skill));
                }
                if (skill.getAvailAttributes() != null) {
                    for (String attribute : skill.getAvailAttributes()) {
                        String normalizedAttribute = normalizeKey(attribute);
                        if (normalizedAttribute == null) continue;
                        skillsByAttribute.computeIfAbsent(normalizedAttribute, ignored -> new ArrayList<>()).add(skill);
                    }
                }
            }
            skillsByAttribute.replaceAll((ignored, skills) -> Collections.unmodifiableList(skills));

            specialtiesById = new HashMap<>(Math.max(16, specialtyData.size() * 2));
            specialtiesByName = new HashMap<>(Math.max(16, specialtyData.size() * 2));
            specialtiesByType = new HashMap<>(Math.max(16, specialtyData.size() * 2));
            specialtyNameSearch = new ArrayList<>(specialtyData.size());
            specialtySearchCache = new ConcurrentHashMap<>();
            for (DataSpecialty specialty : specialtyData) {
                if (specialty == null) continue;
                specialtiesById.putIfAbsent(specialty.getId(), specialty);
                if (specialty.getName() != null) {
                    String normalizedName = normalizeKey(specialty.getName());
                    specialtiesByName.putIfAbsent(normalizedName, specialty);
                    specialtyNameSearch.add(new SearchEntry<>(normalizedName, specialty));
                }
                if (specialty.getCategory() != null) {
                    String key = normalizeKey(specialty.getCategory());
                    specialtiesByType.computeIfAbsent(key, ignored -> new java.util.ArrayList<>()).add(specialty);
                }
            }
            specialtiesByType.replaceAll((ignored, specialties) -> Collections.unmodifiableList(specialties));

            techPermsById = new HashMap<>(Math.max(16, techPermData.size() * 2));
            for (DataTechPerm techPerm : techPermData) {
                if (techPerm != null) {
                    techPermsById.putIfAbsent(techPerm.getId(), techPerm);
                }
            }

            itemsByDid = new HashMap<>(Math.max(16, itemEquipmentData.size() * 2));
            itemsByName = new HashMap<>(Math.max(16, itemEquipmentData.size() * 3));
            itemNameSearch = new ArrayList<>(itemEquipmentData.size());
            itemSearchCache = new ConcurrentHashMap<>();
            for (DataItemEquipment item : itemEquipmentData) {
                if (item == null) continue;
                itemsByDid.putIfAbsent(item.getDid(), item);
                String normalizedDisplayName = normalizeKey(item.getDname());
                String normalizedInternalName = normalizeKey(item.getIname());
                if (item.getIname() != null) {
                    itemsByName.putIfAbsent(normalizedInternalName, item);
                }
                if (item.getDname() != null) {
                    itemsByName.putIfAbsent(normalizedDisplayName, item);
                }
                String combinedSearch = normalizedDisplayName == null
                        ? normalizedInternalName
                        : (normalizedInternalName == null ? normalizedDisplayName : normalizedDisplayName + "\n" + normalizedInternalName);
                if (combinedSearch != null) {
                    itemNameSearch.add(new SearchEntry<>(combinedSearch, item));
                }
            }

            weaponsByDid = new HashMap<>(Math.max(16, itemWeaponData.size() * 2));
            weaponsByName = new HashMap<>(Math.max(16, itemWeaponData.size() * 3));
            weaponNameSearch = new ArrayList<>(itemWeaponData.size());
            weaponSearchCache = new ConcurrentHashMap<>();
            for (DataItemWeapon weapon : itemWeaponData) {
                if (weapon == null) continue;
                weaponsByDid.putIfAbsent(weapon.getDid(), weapon);
                String normalizedDisplayName = normalizeKey(weapon.getDname());
                String normalizedInternalName = normalizeKey(weapon.getIname());
                if (weapon.getIname() != null) {
                    weaponsByName.putIfAbsent(normalizedInternalName, weapon);
                }
                if (weapon.getDname() != null) {
                    weaponsByName.putIfAbsent(normalizedDisplayName, weapon);
                }
                String combinedSearch = normalizedDisplayName == null
                        ? normalizedInternalName
                        : (normalizedInternalName == null ? normalizedDisplayName : normalizedDisplayName + "\n" + normalizedInternalName);
                if (combinedSearch != null) {
                    weaponNameSearch.add(new SearchEntry<>(combinedSearch, weapon));
                }
            }

            trainingById = new HashMap<>(Math.max(16, trainingData.size() * 2));
            trainingNameSearch = new ArrayList<>(trainingData.size());
            trainingSearchCache = new ConcurrentHashMap<>();
            for (DataTraining training : trainingData) {
                if (training != null) {
                    trainingById.putIfAbsent(training.getId(), training);
                    String normalizedName = normalizeKey(training.getName());
                    if (normalizedName != null) {
                        trainingNameSearch.add(new SearchEntry<>(normalizedName, training));
                    }
                }
            }

            actionsById = new HashMap<>(Math.max(16, actionData.size() * 2));
            actionsByName = new HashMap<>(Math.max(16, actionData.size() * 2));
            actionsBySource = new HashMap<>(Math.max(16, actionData.size() * 2));
            actionNameSearch = new ArrayList<>(actionData.size());
            actionSearchCache = new ConcurrentHashMap<>();
            for (DataAction action : actionData) {
                if (action == null) continue;
                actionsById.putIfAbsent(action.getId(), action);
                if (action.getName() != null) {
                    String normalizedName = normalizeKey(action.getName());
                    actionsByName.putIfAbsent(normalizedName, action);
                    actionNameSearch.add(new SearchEntry<>(normalizedName, action));
                }
                if (action.getSource() != null) {
                    String key = normalizeKey(action.getSource());
                    actionsBySource.computeIfAbsent(key, ignored -> new ArrayList<>()).add(action);
                }
            }
            actionsBySource.replaceAll((ignored, actions) -> Collections.unmodifiableList(actions));
        }
    }
}
