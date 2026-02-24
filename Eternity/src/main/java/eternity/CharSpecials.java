package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private DataSpecialty charRacial;                      // The single racial specialty
    @JsonProperty("classSpecialties")
    private final List<DataSpecialty> charClassSpecials;   // Specialties granted by class
    @JsonProperty("trainedSpecialties")
    private final List<DataSpecialty> charTrainedSpecials; // Specialties gained from training/resources

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharSpecials() {
        this.charSkills = new ArrayList<>();
        this.charClassSpecials = new ArrayList<>();
        this.charTrainedSpecials = new ArrayList<>();
        this.charRacial = null;
    }

    // ---------------------------------------------------------
    // Skills
    // ---------------------------------------------------------

    public List<DataSkill> getSkills() { return Collections.unmodifiableList(charSkills); }

    @JsonSetter("skills")
    public void setSkills(List<DataSkill> skills) {
        charSkills.clear();
        if (skills != null) charSkills.addAll(skills);
    }

    public void addSkill(DataSkill skill) {
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) return;

        DataSkill existing = getSkillByName(skill.getName());
        if (existing != null) {
            List<String> incomingChosen = skill.getChosenAttributes();
            if (incomingChosen != null) {
                for (String att : incomingChosen) {
                    if (att == null || att.isBlank()) continue;
                    boolean alreadyChosen = existing.getChosenAttributes().stream()
                            .anyMatch(a -> a != null && a.equalsIgnoreCase(att));
                    if (!alreadyChosen) {
                        existing.addChosenAttribute(att);
                    }
                }
            }
            return;
        }

        charSkills.add(skill);
    }

    public void removeSkill(DataSkill skill) {
        charSkills.remove(skill);
    }

    public DataSkill getSkillByName(String name) {
        for (DataSkill s : charSkills) {
            if (s.getName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    // ---------------------------------------------------------
    // Racial Specialty
    // ---------------------------------------------------------

    public DataSpecialty getRacialSpecialty() {
        return charRacial;
    }

    public void setRacialSpecialty(DataSpecialty racial) {
        this.charRacial = racial;
    }

    public boolean hasRacialSpecialty() {
        return charRacial != null;
    }

    // ---------------------------------------------------------
    // Class Specialties
    // ---------------------------------------------------------

    public List<DataSpecialty> getClassSpecialties() { return Collections.unmodifiableList(charClassSpecials); }

    @JsonSetter("classSpecialties")
    public void setClassSpecialties(List<DataSpecialty> specs) {
        charClassSpecials.clear();
        if (specs != null) charClassSpecials.addAll(specs);
    }

    public void addClassSpecialty(DataSpecialty spec) {
        if (spec != null && !charClassSpecials.contains(spec)) {
            charClassSpecials.add(spec);
        }
    }

    public void removeClassSpecialty(DataSpecialty spec) {
        charClassSpecials.remove(spec);
    }

    public DataSpecialty getClassSpecialtyByName(String name) {
        for (DataSpecialty s : charClassSpecials) {
            if (s.getName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    // ---------------------------------------------------------
    // Trained Specialties
    // ---------------------------------------------------------

    public List<DataSpecialty> getTrainedSpecialties() { return Collections.unmodifiableList(charTrainedSpecials); }

    @JsonSetter("trainedSpecialties")
    public void setTrainedSpecialties(List<DataSpecialty> specs) {
        charTrainedSpecials.clear();
        if (specs != null) charTrainedSpecials.addAll(specs);
    }

    public void addTrainedSpecialty(DataSpecialty spec) {
        if (spec != null && !charTrainedSpecials.contains(spec)) {
            charTrainedSpecials.add(spec);
        }
    }

    public void removeTrainedSpecialty(DataSpecialty spec) {
        charTrainedSpecials.remove(spec);
    }

    public DataSpecialty getTrainedSpecialtyByName(String name) {
        for (DataSpecialty s : charTrainedSpecials) {
            if (s.getName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    // ---------------------------------------------------------
    // Combined Access
    // ---------------------------------------------------------

    /** All specialties the character has, regardless of origin. */
    @JsonIgnore
    public List<DataSpecialty> getAllSpecialties() {
        ArrayList<DataSpecialty> all = new ArrayList<>();
        if (charRacial != null) all.add(charRacial);
        all.addAll(charClassSpecials);
        all.addAll(charTrainedSpecials);
        return all;
    }

    /** Search combined specialties by name. */
    public DataSpecialty findSpecialty(String name) {
        if (charRacial != null && charRacial.getName().equalsIgnoreCase(name))
            return charRacial;

        for (DataSpecialty d : charClassSpecials)
            if (d.getName().equalsIgnoreCase(name))
                return d;

        for (DataSpecialty d : charTrainedSpecials)
            if (d.getName().equalsIgnoreCase(name))
                return d;

        return null;
    }

    /** Search combined specialties by name. */
    public boolean hasSpecialty(String name) {
        if (charRacial != null && charRacial.getName().equalsIgnoreCase(name))
            return true;

        for (DataSpecialty d : charClassSpecials)
            if (d.getName().equalsIgnoreCase(name))
                return true;

        for (DataSpecialty d : charTrainedSpecials)
            if (d.getName().equalsIgnoreCase(name))
                return true;

        return false;
    }

    @JsonIgnore
    private CharData owner;

    @JsonIgnore
    public CharData getOwner() { return owner; }
    public void setOwner(CharData owner) { this.owner = owner; }
}
