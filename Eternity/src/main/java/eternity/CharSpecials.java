package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracks all character skills, racial specialties,
 * class specialties, and trained specialties.
 */
public class CharSpecials {

    // ---------------------------------------------------------
    // Fields
    // ---------------------------------------------------------

    private final List<DataSkill> charSkills;            // All skills the character has
    private DataSpecialty charRacial;                    // The single racial specialty
    private final List<DataSpecialty> charClassSpecials; // Specialties granted by class
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

    public List<DataSkill> getSkills() {
        return Collections.unmodifiableList(charSkills);
    }

    public void addSkill(DataSkill skill) {
        if (skill != null && !charSkills.contains(skill)) {
            charSkills.add(skill);
        }
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

    public List<DataSpecialty> getClassSpecialties() {
        return Collections.unmodifiableList(charClassSpecials);
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

    public List<DataSpecialty> getTrainedSpecialties() {
        return Collections.unmodifiableList(charTrainedSpecials);
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
}