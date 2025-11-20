package eternity;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataQuery {

    private final DataStore store;

    public DataQuery() {
        this.store = new DataStore();
    }

    // ---------------------------------------------------------
    // Utility helpers
    // ---------------------------------------------------------

    private boolean eq(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private boolean contains(String text, String search) {
        if (text == null || search == null) return false;
        return text.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    // ---------------------------------------------------------
    // COLOR SEARCH
    // ---------------------------------------------------------

    public DataColor getColorByTitle(String name) {
        return store.getColorData().stream()
            .filter(c -> eq(c.getTitle(), name))
            .findFirst()
            .orElse(null);
    }

    public List<DataColor> searchColorsByTitle(String namePart) {
        return store.getColorData().stream()
            .filter(c -> contains(c.getTitle(), namePart))
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // LEVEL SEARCH
    // ---------------------------------------------------------

    public DataLevel getLevel(int level) {
        return store.getLevelData().stream()
            .filter(l -> l.getLevel() == level)
            .findFirst()
            .orElse(null);
    }

    // ---------------------------------------------------------
    // RACE SEARCH
    // ---------------------------------------------------------

    public DataRace getRaceById(int id) {
        return store.getRaceData().stream()
            .filter(r -> r.getID() == id)
            .findFirst()
            .orElse(null);
    }

    public DataRace getRaceByName(String name) {
        return store.getRaceData().stream()
            .filter(r -> eq(r.getName(), name))
            .findFirst()
            .orElse(null);
    }

    public List<DataRace> searchRaceByName(String namePart) {
        return store.getRaceData().stream()
            .filter(r -> contains(r.getName(), namePart))
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // CLASS SEARCH
    // ---------------------------------------------------------

    public DataClass getClassById(int id) {
        return store.getClassData().stream()
            .filter(c -> c.getID() == id)
            .findFirst()
            .orElse(null);
    }

    public DataClass getClassByName(String name) {
        return store.getClassData().stream()
            .filter(c -> eq(c.getName(), name))
            .findFirst()
            .orElse(null);
    }

    public List<DataClass> searchClassByName(String namePart) {
        return store.getClassData().stream()
            .filter(c -> contains(c.getName(), namePart))
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // DEITY SEARCH
    // ---------------------------------------------------------

    public DataDeity getDeityById(int id) {
        return store.getDeityData().stream()
            .filter(d -> d.getID() == id)
            .findFirst()
            .orElse(null);
    }

    public DataDeity getDeityByName(String name) {
        return store.getDeityData().stream()
            .filter(d -> eq(d.getName(), name))
            .findFirst()
            .orElse(null);
    }

    public List<DataDeity> searchDeities(String namePart) {
        return store.getDeityData().stream()
            .filter(d -> contains(d.getName(), namePart))
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // SKILL SEARCH
    // ---------------------------------------------------------

    public DataSkill getSkillById(int id) {
        return store.getSkillData().stream()
            .filter(s -> s.getID() == id)
            .findFirst()
            .orElse(null);
    }

    public List<DataSkill> searchSkills(String namePart) {
        return store.getSkillData().stream()
            .filter(s -> contains(s.getName(), namePart))
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // SPECIALTY SEARCH
    // ---------------------------------------------------------

    public DataSpecialty getSpecialtyById(int id) {
        return store.getSpecialtyData().stream()
            .filter(s -> s.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public List<DataSpecialty> getAllSpecialty() {
        return store.getSpecialtyData();
    }

    public List<DataSpecialty> getSpecialtiesByType(String type) {
        return store.getSpecialtyData().stream()
            .filter(s -> eq(s.getType(), type))
            .collect(Collectors.toList());
    }

    public List<DataSpecialty> searchSpecialties(String namePart) {
        return store.getSpecialtyData().stream()
            .filter(s -> contains(s.getName(), namePart))
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // EQUIPMENT SEARCH
    // ---------------------------------------------------------

    public DataItemEquipment getItemByIid(int iid) {
        return store.getItemEquipmentData().stream()
            .filter(i -> i.getIid() == iid)
            .findFirst()
            .orElse(null);
    }

    public List<DataItemEquipment> searchItems(String namePart) {
        return store.getItemEquipmentData().stream()
            .filter(i ->
                contains(i.getDname(), namePart) ||
                contains(i.getIname(), namePart)
            )
            .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // TRAINING SEARCH
    // ---------------------------------------------------------

    public DataTraining getTrainingById(int id) {
        return store.getTrainingData().stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public List<DataTraining> searchTraining(String namePart) {
        return store.getTrainingData().stream()
            .filter(t -> contains(t.getName(), namePart))
            .collect(Collectors.toList());
    }
}