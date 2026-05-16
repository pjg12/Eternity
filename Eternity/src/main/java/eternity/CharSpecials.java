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

    // ---------------------------------------------------------
    // Fields
    // ---------------------------------------------------------
    @JsonIgnore private StoreCharData owner;

    @JsonProperty("skills") private final List<DataSkill> charSkills;              // All skills the character has
    @JsonIgnore private final Map<String, DataSkill> skillsByName;

    @JsonIgnore private DataSpecialty charRacial;                      // The single racial specialty

    @JsonProperty("classSpecialties") private final List<DataSpecialty> charClassSpecials;   // Specialties granted by class
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

    public DataSkill getSkillByName(String name) { return skillsByName.get(normalizeName(name)); }

    public void addSkill(DataSkill skill) {
        // Validate skill
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) return;

        DataSkill existing = getSkillByName(skill.getName());
        if (existing == null) {
            charSkills.add(skill);
            skillsByName.put(normalizeName(skill.getName()), skill);
            skillsDirty = true;
            return;
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
            skillsByName.remove(normalizeName(skill.getName()));
            skillsDirty = true;
        }
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

    public List<DataSpecialty> getClassSpecialties() { return charClassSpecials; }

    public DataSpecialty getClassSpecialtyByName(String name) { return classSpecialtiesByName.get(normalizeName(name)); }

    @JsonSetter("classSpecialties")
    public void setClassSpecialties(List<DataSpecialty> specs) {
        charClassSpecials.clear();
        classSpecialtiesByName.clear();
        addClassSpecialtyList(specs);
        specialtiesDirty = true;
    }

    public void addClassSpecialty(DataSpecialty spec) {
        if (spec != null) {
            String normalizedName = normalizeName(spec.getName());
            if (normalizedName == null || classSpecialtiesByName.containsKey(normalizedName)) return;
            charClassSpecials.add(spec);
            classSpecialtiesByName.put(normalizedName, spec);
            specialtiesDirty = true;
        }
    }

    private void addClassSpecialtyList(List<DataSpecialty> specs) {
        if (specs == null) return;
        for (DataSpecialty spec : specs) {
            addClassSpecialty(spec);
        }
    }

    public void removeClassSpecialty(DataSpecialty spec) {
        if (charClassSpecials.remove(spec) && spec != null) {
            classSpecialtiesByName.remove(normalizeName(spec.getName()));
            specialtiesDirty = true;
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
        if (spec != null) {
            String normalizedName = normalizeName(spec.getName());
            if (normalizedName == null || trainedSpecialtiesByName.containsKey(normalizedName)) return;
            charTrainedSpecials.add(spec);
            trainedSpecialtiesByName.put(normalizedName, spec);
            specialtiesDirty = true;
        }
    }

    private void addTrainedSpecialtyList(List<DataSpecialty> specs) {
        if (specs == null) return;
        for (DataSpecialty spec : specs) {
            addTrainedSpecialty(spec);
        }
    }

    public void removeTrainedSpecialty(DataSpecialty spec) {
        if (charTrainedSpecials.remove(spec) && spec != null) {
            trainedSpecialtiesByName.remove(normalizeName(spec.getName()));
            specialtiesDirty = true;
        }
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
