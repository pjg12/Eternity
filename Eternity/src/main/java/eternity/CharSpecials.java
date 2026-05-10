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

    @JsonProperty("skills")
    private final List<DataSkill> charSkills;              // All skills the character has
    @JsonIgnore
    private final Map<String, DataSkill> skillsByName;
    private DataSpecialty charRacial;                      // The single racial specialty
    @JsonProperty("classSpecialties")
    private final List<DataSpecialty> charClassSpecials;   // Specialties granted by class
    @JsonIgnore
    private final Map<String, DataSpecialty> classSpecialtiesByName;
    @JsonProperty("trainedSpecialties")
    private final List<DataSpecialty> charTrainedSpecials; // Specialties gained from training/resources
    @JsonIgnore
    private final Map<String, DataSpecialty> trainedSpecialtiesByName;
    @JsonIgnore
    private final List<DataSkill> skillsView;
    @JsonIgnore
    private final List<DataSpecialty> classSpecialtiesView;
    @JsonIgnore
    private final List<DataSpecialty> trainedSpecialtiesView;
    @JsonIgnore
    private List<DataSpecialty> allSpecialtiesCache;
    @JsonIgnore
    private boolean allSpecialtiesDirty;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharSpecials() {
        this.charSkills = new ArrayList<>();
        this.skillsByName = new LinkedHashMap<>();
        this.charClassSpecials = new ArrayList<>();
        this.classSpecialtiesByName = new LinkedHashMap<>();
        this.charTrainedSpecials = new ArrayList<>();
        this.trainedSpecialtiesByName = new LinkedHashMap<>();
        this.skillsView = Collections.unmodifiableList(charSkills);
        this.classSpecialtiesView = Collections.unmodifiableList(charClassSpecials);
        this.trainedSpecialtiesView = Collections.unmodifiableList(charTrainedSpecials);
        this.allSpecialtiesCache = List.of();
        this.allSpecialtiesDirty = true;
        this.charRacial = null;
    }

    // ---------------------------------------------------------
    // Skills
    // ---------------------------------------------------------

    public List<DataSkill> getSkills() { return skillsView; }

    @JsonSetter("skills")
    public void setSkills(List<DataSkill> skills) {
        charSkills.clear();
        skillsByName.clear();
        addSkillsBulk(skills);
    }

    public void addSkill(DataSkill skill) {
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) return;

        DataSkill existing = getSkillByName(skill.getName());
        if (existing != null) {
            List<String> incomingChosen = skill.getChosenAttributes();
            if (incomingChosen != null) {
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
                    }
                }
            }
            return;
        }

        charSkills.add(skill);
        skillsByName.put(normalizeName(skill.getName()), skill);
        markAllSpecialtiesDirty();
    }

    public void removeSkill(DataSkill skill) {
        if (charSkills.remove(skill) && skill != null) {
            skillsByName.remove(normalizeName(skill.getName()));
            markAllSpecialtiesDirty();
        }
    }

    public DataSkill getSkillByName(String name) {
        return skillsByName.get(normalizeName(name));
    }

    // ---------------------------------------------------------
    // Racial Specialty
    // ---------------------------------------------------------

    public DataSpecialty getRacialSpecialty() {
        return charRacial;
    }

    public void setRacialSpecialty(DataSpecialty racial) {
        this.charRacial = racial;
        markAllSpecialtiesDirty();
        notifyOwnerSpecialtiesChanged();
    }

    public boolean hasRacialSpecialty() {
        return charRacial != null;
    }

    // ---------------------------------------------------------
    // Class Specialties
    // ---------------------------------------------------------

    public List<DataSpecialty> getClassSpecialties() { return classSpecialtiesView; }

    @JsonSetter("classSpecialties")
    public void setClassSpecialties(List<DataSpecialty> specs) {
        charClassSpecials.clear();
        classSpecialtiesByName.clear();
        addClassSpecialtiesBulk(specs);
        markAllSpecialtiesDirty();
        notifyOwnerSpecialtiesChanged();
    }

    public void addClassSpecialty(DataSpecialty spec) {
        if (spec != null) {
            String normalizedName = normalizeName(spec.getName());
            if (normalizedName == null || classSpecialtiesByName.containsKey(normalizedName)) return;
            charClassSpecials.add(spec);
            classSpecialtiesByName.put(normalizedName, spec);
            markAllSpecialtiesDirty();
            notifyOwnerSpecialtiesChanged();
        }
    }

    public void removeClassSpecialty(DataSpecialty spec) {
        if (charClassSpecials.remove(spec) && spec != null) {
            classSpecialtiesByName.remove(normalizeName(spec.getName()));
            markAllSpecialtiesDirty();
            notifyOwnerSpecialtiesChanged();
        }
    }

    public DataSpecialty getClassSpecialtyByName(String name) {
        return classSpecialtiesByName.get(normalizeName(name));
    }

    // ---------------------------------------------------------
    // Trained Specialties
    // ---------------------------------------------------------

    public List<DataSpecialty> getTrainedSpecialties() { return trainedSpecialtiesView; }

    @JsonSetter("trainedSpecialties")
    public void setTrainedSpecialties(List<DataSpecialty> specs) {
        charTrainedSpecials.clear();
        trainedSpecialtiesByName.clear();
        addTrainedSpecialtiesBulk(specs);
        markAllSpecialtiesDirty();
        notifyOwnerSpecialtiesChanged();
    }

    public void addTrainedSpecialty(DataSpecialty spec) {
        if (spec != null) {
            String normalizedName = normalizeName(spec.getName());
            if (normalizedName == null || trainedSpecialtiesByName.containsKey(normalizedName)) return;
            charTrainedSpecials.add(spec);
            trainedSpecialtiesByName.put(normalizedName, spec);
            markAllSpecialtiesDirty();
            notifyOwnerSpecialtiesChanged();
        }
    }

    public void removeTrainedSpecialty(DataSpecialty spec) {
        if (charTrainedSpecials.remove(spec) && spec != null) {
            trainedSpecialtiesByName.remove(normalizeName(spec.getName()));
            markAllSpecialtiesDirty();
            notifyOwnerSpecialtiesChanged();
        }
    }

    public DataSpecialty getTrainedSpecialtyByName(String name) {
        return trainedSpecialtiesByName.get(normalizeName(name));
    }

    // ---------------------------------------------------------
    // Combined Access
    // ---------------------------------------------------------

    /** All specialties the character has, regardless of origin. */
    @JsonIgnore
    public List<DataSpecialty> getAllSpecialties() {
        if (!allSpecialtiesDirty) {
            return allSpecialtiesCache;
        }

        ArrayList<DataSpecialty> all = new ArrayList<>(charClassSpecials.size() + charTrainedSpecials.size() + (charRacial != null ? 1 : 0));
        if (charRacial != null) all.add(charRacial);
        all.addAll(charClassSpecials);
        all.addAll(charTrainedSpecials);
        allSpecialtiesCache = Collections.unmodifiableList(all);
        allSpecialtiesDirty = false;
        return allSpecialtiesCache;
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

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase(Locale.ROOT);
    }

    private void addSkillsBulk(List<DataSkill> skills) {
        if (skills == null) return;
        for (DataSkill skill : skills) {
            if (skill == null || skill.getName() == null || skill.getName().isBlank()) continue;
            String normalizedName = normalizeName(skill.getName());
            DataSkill existing = skillsByName.get(normalizedName);
            if (existing != null) {
                mergeChosenAttributes(existing, skill);
                continue;
            }
            charSkills.add(skill);
            skillsByName.put(normalizedName, skill);
        }
    }

    private void mergeChosenAttributes(DataSkill existing, DataSkill incoming) {
        if (existing == null || incoming == null) return;
        List<String> incomingChosen = incoming.getChosenAttributes();
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
            }
        }
    }

    private void addClassSpecialtiesBulk(List<DataSpecialty> specs) {
        if (specs == null) return;
        for (DataSpecialty spec : specs) {
            if (spec == null) continue;
            String normalizedName = normalizeName(spec.getName());
            if (normalizedName == null || classSpecialtiesByName.containsKey(normalizedName)) continue;
            charClassSpecials.add(spec);
            classSpecialtiesByName.put(normalizedName, spec);
        }
    }

    private void addTrainedSpecialtiesBulk(List<DataSpecialty> specs) {
        if (specs == null) return;
        for (DataSpecialty spec : specs) {
            if (spec == null) continue;
            String normalizedName = normalizeName(spec.getName());
            if (normalizedName == null || trainedSpecialtiesByName.containsKey(normalizedName)) continue;
            charTrainedSpecials.add(spec);
            trainedSpecialtiesByName.put(normalizedName, spec);
        }
    }

    private void markAllSpecialtiesDirty() {
        allSpecialtiesDirty = true;
    }

    private void notifyOwnerSpecialtiesChanged() {
        if (owner != null) {
            owner.refreshSpecialtyPassiveBonuses();
        }
    }

    /** Search combined specialties by name. */
    public boolean hasSpecialty(String name) {
        return findSpecialty(name) != null;
    }

    @JsonIgnore
    private CharData owner;

    @JsonIgnore
    public CharData getOwner() { return owner; }
    public void setOwner(CharData owner) { this.owner = owner; }
}
