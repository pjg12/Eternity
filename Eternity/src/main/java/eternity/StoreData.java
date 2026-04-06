package eternity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eternity.DataStatus;

public class StoreData {
    private static final Path DATA_PATH = resolveDataPath();

	private final List<DataColor> colorData;
	private final List<DataLevel> levelData;
	private final List<DataRace> raceData;
	private final List<DataClass> classData;
	private final List<DataDeity> deityData;
	private final List<DataSkill> skillData;
	private final List<DataSpecialty> specialtyData;
	private final List<DataItemEquipment> itemEquipmentData;
	private final List<DataTraining> trainingData;
	private final List<DataTechPerm> techPermData;
	
	private final DataBuilder builder;
	
	public StoreData() {
        this.builder = new DataBuilder(DATA_PATH);
        
        // Load all JSON data files
        colorData          = safeLoad("colordata.json",        DataColor[].class);
        levelData          = safeLoad("leveldata.json",        DataLevel[].class);
        raceData           = safeLoad("racedata.json",         DataRace[].class);
        classData          = safeLoad("classdata.json",        DataClass[].class);
        deityData          = safeLoad("deitydata.json",        DataDeity[].class);
        skillData          = safeLoad("skilldata.json",        DataSkill[].class);
        specialtyData      = safeLoad("specialtydata.json",    DataSpecialty[].class);
        itemEquipmentData  = safeLoad("itemequipdata.json",    DataItemEquipment[].class);
        techPermData       = safeLoad("techpermdata.json",     DataTechPerm[].class);
        trainingData       = safeLoad("trainingdata.json",     DataTraining[].class);
        applyTechPermsToTraining();
    }
	
	private <T> List<T> safeLoad(String filename, Class<T[]> type) {
        try {
            List<T> list = builder.loadList(filename, type);
            if (list == null || list.isEmpty()) {
                return List.of();
            }
            return Collections.unmodifiableList(list);
        }
        catch (Exception e) {
            System.err.println("Failed to load " + filename + ": " + e.getMessage());
            return List.of();
        }
    }

    private static Path resolveDataPath() {
        Path path = Paths.get("data");
        if (Files.exists(path)) {
            return path;
        }

        Path alt = Paths.get("Data");
        if (Files.exists(alt)) {
            return alt;
        }

        return Paths.get(System.getProperty("user.dir")).resolve("data");
    }

	// --- Getters ---

    public List<DataColor> getColorData() { return colorData; }

    public List<DataLevel> getLevelData() { return levelData; }

    public List<DataRace> getRaceData() { return raceData; }

    public List<DataClass> getClassData() { return classData; }

    public List<DataDeity> getDeityData() { return deityData; }

    public List<DataSkill> getSkillData() { return skillData; }

    public List<DataSpecialty> getSpecialtyData() { return specialtyData; }

    public List<DataItemEquipment> getItemEquipmentData() { return itemEquipmentData; }

    public List<DataTraining> getTrainingData() { return trainingData; }

    public List<DataTechPerm> getTechPermData() { return techPermData; }

    /**
     * Injects permanent statuses into training entries based on their grant ids and techPermData.
     */
    private void applyTechPermsToTraining() {
        if (trainingData == null || techPermData == null) return;
        Map<Integer, DataTechPerm> techPermsById = new HashMap<>(Math.max(16, techPermData.size()));
        for (DataTechPerm perm : techPermData) {
            if (perm != null) {
                techPermsById.put(perm.getId(), perm);
            }
        }

        for (DataTraining t : trainingData) {
            if (t == null || t.getGrant() == null) continue;
            for (Integer gid : t.getGrant()) {
                if (gid == null) continue;
                DataTechPerm perm = techPermsById.get(gid);
                if (perm == null) continue;
                DataStatus ds = new DataStatus();
                ds.setName("TechPerm " + gid);
                ds.setAttribute(perm.getAttribute());
                ds.setDurationType("Permanent");
                ds.setSeverity(perm.getRatio());
                ds.setAffinity("None");
                ds.setDescription("Tech permission grant");
                t.addPermStatus(ds);
            }
        }
    }
}
