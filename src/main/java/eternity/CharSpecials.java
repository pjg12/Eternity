package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Tracks all character skills, racial specialties,
 * class specialties, and trained specialties.
 */
public class CharSpecials {
    public static final String SKILL_DEDICATION_SPECIALTY = "Skill Dedication";
    public static final String STANCE_SPECIALTY = "Stance";
    private static final String GRANT_SPEC_TYPE = "grantSpec";
    private static final String CRUSHING_BLOW_SPECIALTY = "Crushing Blow";
    private static final String CRUSHING_BLOW_TRIUMPH_SPECIALTY = "Crushing Blow (Bend)";
    private static final String CRUSHING_BLOW_BREAK_SPECIALTY = "Crushing Blow (Break)";
    private static final String JUDGMENT_SPECIALTY = "Judgment";
    private static final String JUDGMENT_INNOCENT_SPECIALTY = "Judgment (Innocent)";
    private static final String JUDGMENT_GUILTY_SPECIALTY = "Judgment (Guilty)";
    private static final String SHIV_SPECIALTY = "Shiv";
    private static final String SHIV_OVER_SPECIALTY = "Shiv (Over)";
    private static final String SHIV_UNDER_SPECIALTY = "Shiv (Under)";
    private static final String RESONANT_STRIKE_SPECIALTY = "Resonant Strike";
    private static final String RESONANT_STRIKE_EBB_SPECIALTY = "Resonant Strike (Ebb)";
    private static final String RESONANT_STRIKE_FLOW_SPECIALTY = "Resonant Strike (Flow)";
    private static final String MISDIRECTION_SPECIALTY = "Misdirection";
    private static final String MISDIRECTION_TOSS_SPECIALTY = "Misdirection (Toss)";
    private static final String MISDIRECTION_TURN_SPECIALTY = "Misdirection (Turn)";
    private static final String COMMAND_SPECIALTY = "Command";
    private static final String COMMAND_SAFE_SPECIALTY = "Command (Safe)";
    private static final String COMMAND_SOUND_SPECIALTY = "Command (Sound)";
    private static final String CHASTISE_SPECIALTY = "Chastise";
    private static final String CHASTISE_NIGHT_SPECIALTY = "Chastise (Night)";
    private static final String CHASTISE_DAY_SPECIALTY = "Chastise (Day)";
    private static final String EFFLUX_SPECIALTY = "Efflux";
    private static final String EFFLUX_RISE_SPECIALTY = "Efflux (Rise)";
    private static final String EFFLUX_FALL_SPECIALTY = "Efflux (Fall)";
    private static final String PULSE_SPECIALTY = "Pulse";
    private static final String PULSE_SLOW_SPECIALTY = "Pulse (Slow)";
    private static final String PULSE_STEADY_SPECIALTY = "Pulse (Steady)";
    private static final String IONIZE_SPECIALTY = "Ionize";
    private static final String IONIZE_SUPPLY_SPECIALTY = "Ionize (Supply)";
    private static final String IONIZE_DEMAND_SPECIALTY = "Ionize (Demand)";

    // ---------------------------------------------------------
    // Fields
    // ---------------------------------------------------------
    @JsonIgnore private StoreCharData owner;

    @JsonProperty("skills") private final List<DataSkill> charSkills;              // All skills the character has
    @JsonIgnore private final Map<String, DataSkill> skillsByName;

    @JsonIgnore private DataSpecialty charRacial;                      // The single racial specialty

    @JsonIgnore private final List<DataSpecialty> charClassSpecials;   // Specialties granted by class
    @JsonIgnore private final Map<String, DataSpecialty> classSpecialtiesByName;

    @JsonProperty("trainedSpecialties") private final List<DataSpecialty> charTrainedSpecials; // Specialties gained from training/resources
    @JsonIgnore private final Map<String, DataSpecialty> trainedSpecialtiesByName;

    @JsonIgnore private List<DataSpecialty> allSpecialtiesCache;

    @JsonIgnore private boolean skillsDirty;
    @JsonIgnore private boolean specialtiesDirty;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharSpecials() {
        this.charSkills = new ArrayList<>();
        this.skillsByName = new LinkedHashMap<>();
        this.skillsDirty = true;
        this.specialtiesDirty = true;

        this.charRacial = null;

        this.charClassSpecials = new ArrayList<>();
        this.classSpecialtiesByName = new LinkedHashMap<>();

        this.charTrainedSpecials = new ArrayList<>();
        this.trainedSpecialtiesByName = new LinkedHashMap<>();
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase(Locale.ROOT);
    }

    private DataSpecialty resolveRuleSpecialty(DataSpecialty specialty) {
        if (specialty == null) return null;
        StoreRuleManager ruleManager = new StoreRuleManager();
        if (specialty.getId() > 0) {
            DataSpecialty byId = ruleManager.getSpecialtyById(specialty.getId());
            if (byId != null) return byId;
        }
        if (specialty.getName() != null && !specialty.getName().isBlank()) {
            return ruleManager.getSpecialtyByName(specialty.getName());
        }
        return null;
    }

    private boolean isGrantSpecSpecialty(DataSpecialty specialty) {
        if (specialty == null) return false;
        DataSpecialty base = resolveRuleSpecialty(specialty);
        if (base != null && base.getType() != null && !base.getType().isBlank()) {
            return GRANT_SPEC_TYPE.equalsIgnoreCase(base.getType());
        }
        return specialty.getType() != null && GRANT_SPEC_TYPE.equalsIgnoreCase(specialty.getType());
    }

    public boolean shouldHideFromSpecialtyList(DataSpecialty specialty) {
        if (isUnifiedGrantSpecialty(specialty)) {
            return false;
        }
        return isGrantSpecSpecialty(specialty) && !resolveGrantedSubtypeSpecialties(specialty).isEmpty();
    }

    private boolean isUnifiedGrantSpecialty(DataSpecialty specialty) {
        if (specialty == null || specialty.getName() == null) return false;
        String name = specialty.getName().trim();
        return CRUSHING_BLOW_SPECIALTY.equalsIgnoreCase(name)
                || JUDGMENT_SPECIALTY.equalsIgnoreCase(name)
                || SHIV_SPECIALTY.equalsIgnoreCase(name)
                || RESONANT_STRIKE_SPECIALTY.equalsIgnoreCase(name)
                || MISDIRECTION_SPECIALTY.equalsIgnoreCase(name)
                || COMMAND_SPECIALTY.equalsIgnoreCase(name)
                || CHASTISE_SPECIALTY.equalsIgnoreCase(name)
                || EFFLUX_SPECIALTY.equalsIgnoreCase(name)
                || PULSE_SPECIALTY.equalsIgnoreCase(name)
                || IONIZE_SPECIALTY.equalsIgnoreCase(name);
    }

    private List<DataSpecialty> resolveGrantedSubtypeSpecialties(DataSpecialty specialty) {
        ArrayList<DataSpecialty> granted = new ArrayList<>();
        StoreRuleManager ruleManager = new StoreRuleManager();
        DataSpecialty ruleSpecialty = resolveRuleSpecialty(specialty);

        if (hasExplicitGrantStatuses(ruleSpecialty, specialty)) {
            return granted;
        }

        if (ruleSpecialty != null) {
            for (DataStatus status : ruleSpecialty.getPermStatus()) {
                if (status == null || status.getAttribute() == null || status.getAttribute().isBlank()) continue;
                try {
                    int specialtyId = Integer.parseInt(status.getAttribute().trim());
                    DataSpecialty grantedById = ruleManager.getSpecialtyById(specialtyId);
                    if (grantedById != null && grantedById.getName() != null && !containsSpecialtyName(granted, grantedById.getName())) {
                        granted.add(new DataSpecialty(grantedById));
                    }
                } catch (NumberFormatException ignored) {
                    // Not a specialty-id grant reference.
                }
            }
        }

        if (!isGrantSpecSpecialty(specialty) || specialty.getName() == null || specialty.getName().isBlank()) {
            return granted;
        }

        String prefix = specialty.getName().trim() + " (";
        for (DataSpecialty candidate : ruleManager.getAllSpecialty()) {
            if (candidate == null || candidate.getName() == null) continue;
            String candidateName = candidate.getName().trim();
            if (!candidateName.startsWith(prefix) || !candidateName.endsWith(")")) continue;
            if (candidateName.equalsIgnoreCase(specialty.getName().trim())) continue;
            if (!containsSpecialtyName(granted, candidateName)) {
                granted.add(new DataSpecialty(candidate));
            }
        }
        return granted;
    }

    public List<DataSpecialty> getGrantedSubtypeSpecialties(DataSpecialty specialty) {
        ArrayList<DataSpecialty> copies = new ArrayList<>();
        for (DataSpecialty granted : resolveGrantedSubtypeSpecialties(specialty)) {
            if (granted != null) {
                copies.add(new DataSpecialty(granted));
            }
        }
        return copies;
    }

    private boolean hasExplicitGrantStatuses(DataSpecialty ruleSpecialty, DataSpecialty specialty) {
        return hasPermStatusEntries(ruleSpecialty) || hasPermStatusEntries(specialty);
    }

    private boolean hasPermStatusEntries(DataSpecialty specialty) {
        return specialty != null
                && specialty.getPermStatus() != null
                && !specialty.getPermStatus().isEmpty();
    }

    private boolean containsSpecialtyName(List<DataSpecialty> specialties, String name) {
        if (specialties == null || name == null || name.isBlank()) return false;
        for (DataSpecialty specialty : specialties) {
            if (specialty != null && specialty.getName() != null && specialty.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeSkillKey(String name, String subtype) {
        String normalizedName = normalizeName(name);
        if (normalizedName == null) return null;
        if (subtype == null || subtype.isBlank()) return normalizedName;
        return normalizedName + "::" + subtype.trim().toLowerCase(Locale.ROOT);
    }

    public StoreCharData getOwner() { return owner; }
    public void setOwner(StoreCharData owner) { this.owner = owner; }

    // ---------------------------------------------------------
    // Skills
    // ---------------------------------------------------------

    public List<DataSkill> getSkills() { return charSkills; }
    @JsonSetter("skills") public void setSkills(List<DataSkill> skills) {
        charSkills.clear();
        skillsByName.clear();
        addSkillsList(skills);
    }

    public DataSkill getSkillByName(String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName == null) return null;

        DataSkill direct = skillsByName.get(normalizedName);
        if (direct != null) return direct;

        for (DataSkill skill : charSkills) {
            if (skill == null) continue;
            if (normalizedName.equals(normalizeName(skill.getName()))) return skill;
        }
        return null;
    }

    private DataSkill getSkillByKey(String name, String subtype) {
        return skillsByName.get(normalizeSkillKey(name, subtype));
    }

    public void addSkill(DataSkill skill) {
        // Validate skill
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) return;

        DataSkill existing = getSkillByKey(skill.getName(), skill.getChosenSubtype());
        if (existing == null) {
            charSkills.add(skill);
            skillsByName.put(normalizeSkillKey(skill.getName(), skill.getChosenSubtype()), skill);
            skillsDirty = true;
            return;
        }

        if (!canMergeSkill(existing, skill)) {
            charSkills.add(skill);
            skillsByName.put(normalizeSkillKey(skill.getName(), skill.getChosenSubtype()), skill);
            skillsDirty = true;
            return;
        }

        if (!existing.requiresSubtype()
                && (existing.getChosenSubtype() == null || existing.getChosenSubtype().isBlank())
                && skill.getChosenSubtype() != null && !skill.getChosenSubtype().isBlank()) {
            existing.setChosenSubtype(skill.getChosenSubtype());
            skillsDirty = true;
        }

        List<String> incomingChosen = skill.getChosenAttributes();
        if (incomingChosen == null) return;

        for (String att : incomingChosen) {
            if (att == null || att.isBlank()) continue;
            boolean alreadyChosen = false;
            for (String chosen : existing.getChosenAttributes()) {
                if (chosen != null && chosen.equalsIgnoreCase(att)) {
                    alreadyChosen = true;
                    break;
                }
            }
            if (!alreadyChosen) {
                if (hasChosenAttributeCap(existing)) {
                    continue;
                }
                existing.addChosenAttribute(att);
                skillsDirty = true;
            }
        }      
    }

    private void addSkillsList(List<DataSkill> skills) {
        if (skills == null) return;
        for (DataSkill skill : skills) {
            addSkill(skill);
        }
    }

    public void removeSkill(DataSkill skill) {
        if (charSkills.remove(skill) && skill != null) {
            skillsByName.remove(normalizeSkillKey(skill.getName(), skill.getChosenSubtype()));
            skillsDirty = true;
        }
    }

    private boolean canMergeSkill(DataSkill existing, DataSkill incoming) {
        if (existing == null || incoming == null) return false;
        if (existing.requiresSubtype() || incoming.requiresSubtype()) {
            return subtypeMatches(existing.getChosenSubtype(), incoming.getChosenSubtype());
        }
        return true;
    }

    private boolean subtypeMatches(String left, String right) {
        if (left == null || left.isBlank() || right == null || right.isBlank()) return false;
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean hasChosenAttributeCap(DataSkill skill) {
        if (skill == null || !skill.requiresSubtype()) return false;
        return skill.getChosenAttributes().size() >= 2;
    }

    // ---------------------------------------------------------
    // Racial Specialty
    // ---------------------------------------------------------

    public DataSpecialty getRacialSpecialty() { return charRacial; }

    public void setRacialSpecialty(DataSpecialty racial) {  
        this.charRacial = racial;
        specialtiesDirty = true;
    }

    public boolean hasRacialSpecialty() { return charRacial != null; }

    // ---------------------------------------------------------
    // Class Specialties
    // ---------------------------------------------------------

    @JsonIgnore
    public List<DataSpecialty> getClassSpecialties() { return charClassSpecials; }

    public DataSpecialty getClassSpecialtyByName(String name) { return classSpecialtiesByName.get(normalizeName(name)); }

    @JsonSetter("classSpecialties")
    public void setClassSpecialties(List<DataSpecialty> specs) {
        charClassSpecials.clear();
        classSpecialtiesByName.clear();
        addClassSpecialtyList(specs);
        normalizeLegacyClassSpecialties();
        specialtiesDirty = true;
    }

    public void addClassSpecialty(DataSpecialty spec) {
        if (spec == null) return;
        addClassSpecialtyInternal(spec);
        for (DataSpecialty granted : resolveGrantedSubtypeSpecialties(spec)) {
            addClassSpecialtyInternal(granted);
        }
    }

    private void addClassSpecialtyInternal(DataSpecialty spec) {
        if (spec == null) return;
        String normalizedName = normalizeName(spec.getName());
        if (normalizedName == null || classSpecialtiesByName.containsKey(normalizedName)) return;
        charClassSpecials.add(spec);
        classSpecialtiesByName.put(normalizedName, spec);
        specialtiesDirty = true;
    }

    private void addClassSpecialtyList(List<DataSpecialty> specs) {
        if (specs == null) return;
        for (DataSpecialty spec : specs) {
            addClassSpecialty(spec);
        }
    }

    private void normalizeLegacyClassSpecialties() {
        normalizeLegacySplitClassSpecialty(
                CRUSHING_BLOW_SPECIALTY,
                CRUSHING_BLOW_TRIUMPH_SPECIALTY,
                CRUSHING_BLOW_BREAK_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                JUDGMENT_SPECIALTY,
                JUDGMENT_INNOCENT_SPECIALTY,
                JUDGMENT_GUILTY_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                SHIV_SPECIALTY,
                SHIV_OVER_SPECIALTY,
                SHIV_UNDER_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                RESONANT_STRIKE_SPECIALTY,
                RESONANT_STRIKE_EBB_SPECIALTY,
                RESONANT_STRIKE_FLOW_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                MISDIRECTION_SPECIALTY,
                MISDIRECTION_TOSS_SPECIALTY,
                MISDIRECTION_TURN_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                COMMAND_SPECIALTY,
                COMMAND_SAFE_SPECIALTY,
                COMMAND_SOUND_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                CHASTISE_SPECIALTY,
                CHASTISE_NIGHT_SPECIALTY,
                CHASTISE_DAY_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                EFFLUX_SPECIALTY,
                EFFLUX_RISE_SPECIALTY,
                EFFLUX_FALL_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                PULSE_SPECIALTY,
                PULSE_SLOW_SPECIALTY,
                PULSE_STEADY_SPECIALTY);
        normalizeLegacySplitClassSpecialty(
                IONIZE_SPECIALTY,
                IONIZE_SUPPLY_SPECIALTY,
                IONIZE_DEMAND_SPECIALTY);
        rebuildClassSpecialtiesByName();
    }

    private void normalizeLegacySplitClassSpecialty(String unifiedName, String legacyVariantOne, String legacyVariantTwo) {
        boolean hasUnified = false;
        boolean foundLegacyVariant = false;

        for (DataSpecialty specialty : charClassSpecials) {
            if (specialty == null || specialty.getName() == null) continue;
            if (unifiedName.equalsIgnoreCase(specialty.getName())) {
                hasUnified = true;
            }
            if (legacyVariantOne.equalsIgnoreCase(specialty.getName())
                    || legacyVariantTwo.equalsIgnoreCase(specialty.getName())) {
                foundLegacyVariant = true;
            }
        }

        if (!foundLegacyVariant) return;

        charClassSpecials.removeIf(spec -> spec != null
                && spec.getName() != null
                && (legacyVariantOne.equalsIgnoreCase(spec.getName())
                || legacyVariantTwo.equalsIgnoreCase(spec.getName())));

        if (hasUnified) return;

        StoreRuleManager ruleManager = new StoreRuleManager();
        DataSpecialty unifiedSpecialty = ruleManager.getSpecialtyByName(unifiedName);
        if (unifiedSpecialty != null) {
            addClassSpecialtyInternal(new DataSpecialty(unifiedSpecialty));
        }
    }

    public void removeClassSpecialty(DataSpecialty spec) {
        if (charClassSpecials.remove(spec) && spec != null) {
            classSpecialtiesByName.remove(normalizeName(spec.getName()));
            specialtiesDirty = true;
        }
    }

    private void rebuildClassSpecialtiesByName() {
        classSpecialtiesByName.clear();
        for (DataSpecialty spec : charClassSpecials) {
            if (spec == null || spec.getName() == null || spec.getName().isBlank()) continue;
            classSpecialtiesByName.putIfAbsent(normalizeName(spec.getName()), spec);
        }
    }

    // ---------------------------------------------------------
    // Trained Specialties
    // ---------------------------------------------------------

    public List<DataSpecialty> getTrainedSpecialties() { return charTrainedSpecials; }

    public DataSpecialty getTrainedSpecialtyByName(String name) { return trainedSpecialtiesByName.get(normalizeName(name)); }

    @JsonSetter("trainedSpecialties")
    public void setTrainedSpecialties(List<DataSpecialty> specs) {
        charTrainedSpecials.clear();
        trainedSpecialtiesByName.clear();
        addTrainedSpecialtyList(specs);
        specialtiesDirty = true;
    }

    public void addTrainedSpecialty(DataSpecialty spec) {
        if (spec == null) return;
        addTrainedSpecialtyInternal(spec);
        for (DataSpecialty granted : resolveGrantedSubtypeSpecialties(spec)) {
            addTrainedSpecialtyInternal(granted);
        }
    }

    private void addTrainedSpecialtyInternal(DataSpecialty spec) {
        if (spec == null) return;
        String normalizedName = normalizeName(spec.getName());
        if (normalizedName == null) return;
        if (isRepeatableSpecialty(spec)) {
            if (!allowsDuplicateRepeatableSpecialtyInstances(spec)
                    && containsRepeatableSpecialty(spec.getName(), spec.getRefName())) return;
            charTrainedSpecials.add(spec);
            trainedSpecialtiesByName.putIfAbsent(normalizedName, spec);
            specialtiesDirty = true;
            return;
        }
        if (trainedSpecialtiesByName.containsKey(normalizedName)) return;
        charTrainedSpecials.add(spec);
        trainedSpecialtiesByName.put(normalizedName, spec);
        specialtiesDirty = true;
    }

    private void addTrainedSpecialtyList(List<DataSpecialty> specs) {
        if (specs == null) return;
        for (DataSpecialty spec : specs) {
            addTrainedSpecialty(spec);
        }
    }

    public void removeTrainedSpecialty(DataSpecialty spec) {
        if (charTrainedSpecials.remove(spec) && spec != null) {
            rebuildTrainedSpecialtiesByName();
            specialtiesDirty = true;
        }
    }

    private void rebuildTrainedSpecialtiesByName() {
        trainedSpecialtiesByName.clear();
        for (DataSpecialty spec : charTrainedSpecials) {
            if (spec == null || spec.getName() == null || spec.getName().isBlank()) continue;
            trainedSpecialtiesByName.putIfAbsent(normalizeName(spec.getName()), spec);
        }
    }

    private boolean containsRepeatableSpecialty(String name, String refName) {
        for (DataSpecialty spec : charTrainedSpecials) {
            if (spec == null || !matchesName(spec.getName(), name)) continue;
            if (matchesRefName(spec.getRefName(), refName)) return true;
        }
        return false;
    }

    private boolean allowsDuplicateRepeatableSpecialtyInstances(DataSpecialty specialty) {
        return specialty != null
                && specialty.getName() != null
                && STANCE_SPECIALTY.equalsIgnoreCase(specialty.getName().trim());
    }

    private boolean matchesName(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private boolean matchesRefName(String left, String right) {
        String normalizedLeft = normalizeName(left);
        String normalizedRight = normalizeName(right);
        if (normalizedLeft == null || normalizedRight == null) return normalizedLeft == normalizedRight;
        return normalizedLeft.equals(normalizedRight);
    }

    public static boolean isRepeatableSpecialty(DataSpecialty specialty) {
        return specialty != null && isRepeatableSpecialtyName(specialty.getName());
    }

    public static boolean isRepeatableSpecialtyName(String specialtyName) {
        return specialtyName != null
                && (specialtyName.equalsIgnoreCase(SKILL_DEDICATION_SPECIALTY)
                || specialtyName.equalsIgnoreCase(STANCE_SPECIALTY));
    }

    public static String formatSkillDisplayName(DataSkill skill) {
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) return "";
        String display = skill.getName();
        String subtype = skill.getChosenSubtype();
        if (subtype != null && !subtype.isBlank()) {
            display += " (" + subtype.trim() + ")";
        }
        return display;
    }

    public double getSkillDedicationBonus(DataSkill skill, int level) {
        return getSkillDedicationBonusForDisplayName(formatSkillDisplayName(skill), level);
    }

    public double getSkillDedicationBonusForDisplayName(String displayName, int level) {
        if (displayName == null || displayName.isBlank() || level <= 0) return 0.0;
        double total = 0.0;
        for (DataSpecialty specialty : charTrainedSpecials) {
            if (!isRepeatableSpecialty(specialty)) continue;
            if (specialty.getRefName() != null && specialty.getRefName().equalsIgnoreCase(displayName.trim())) {
                total += level;
            }
        }
        return total;
    }

    // ---------------------------------------------------------
    // Combined Access
    // ---------------------------------------------------------

    /** All specialties the character has, regardless of origin. */
    @JsonIgnore
    public List<DataSpecialty> getAllSpecialties() {
        if (specialtiesDirty) { rebuildSpecialtiesCache(); }
        return allSpecialtiesCache;
    }

    public void checkChanges() {
        if (owner == null) { return; }
        if (skillsDirty) {
            skillsDirty = false;
            owner.refreshSkills();
        }
        if (specialtiesDirty) {
            specialtiesDirty = false;
            owner.refreshSpecialties();
        }
    }

    public void rebuildSpecialtiesCache() {
        allSpecialtiesCache = new ArrayList<>();
        if (charRacial != null) allSpecialtiesCache.add(charRacial);
        allSpecialtiesCache.addAll(charClassSpecials);
        allSpecialtiesCache.addAll(charTrainedSpecials);
        allSpecialtiesCache = Collections.unmodifiableList(allSpecialtiesCache);
        specialtiesDirty = false;
    }

    /** Search combined specialties by name. */
    public DataSpecialty findSpecialty(String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName == null) return null;

        if (charRacial != null && normalizedName.equals(normalizeName(charRacial.getName())))
            return charRacial;

        DataSpecialty classSpec = classSpecialtiesByName.get(normalizedName);
        if (classSpec != null) return classSpec;

        return trainedSpecialtiesByName.get(normalizedName);
    }

    /** Search combined specialties by name. */
    public boolean hasSpecialty(String name) { return findSpecialty(name) != null; }
}
