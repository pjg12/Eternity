package eternity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import eternity.DataStatus;

public class DataStore {
	private List<DataColor> colorData;
	private List<DataLevel> levelData;
	private List<DataRace> raceData;
	private List<DataClass> classData;
	private List<DataDeity> deityData;
	private List<DataSkill> skillData;
	private List<DataSpecialty> specialtyData;
	private List<DataItemEquipment> itemEquipmentData;
	private List<DataTraining> trainingData;
	private List<DataTechPerm> techPermData;
	
	private final DataBuilder builder;
	
	public DataStore() {
        // Prefer local "data" directory; fall back to capitalized "Data"; finally default to working dir/data
        Path path = Paths.get("data");
        if (!path.toFile().exists()) {
            Path alt = Paths.get("Data");
            if (alt.toFile().exists()) {
                path = alt;
            } else {
                path = Paths.get(System.getProperty("user.dir")).resolve("data");
            }
        }

        this.builder = new DataBuilder(path);
        
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
            return (list != null ? list : new ArrayList<>());
        }
        catch (Exception e) {
            System.err.println("Failed to load " + filename + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
	
	// --- Getters & Setters ---

    public List<DataColor> getColorData() { return colorData; }
    public void setColorData(List<DataColor> colorData) { this.colorData = colorData; }

    public List<DataLevel> getLevelData() { return levelData; }
    public void setLevelData(List<DataLevel> levelData) { this.levelData = levelData; }

    public List<DataRace> getRaceData() { return raceData; }
    public void setRaceData(List<DataRace> raceData) { this.raceData = raceData; }

    public List<DataClass> getClassData() { return classData; }
    public void setClassData(List<DataClass> classData) { this.classData = classData; }

    public List<DataDeity> getDeityData() { return deityData; }
    public void setDeityData(List<DataDeity> deityData) { this.deityData = deityData; }

    public List<DataSkill> getSkillData() { return skillData; }
    public void setSkillData(List<DataSkill> skillData) { this.skillData = skillData; }

    public List<DataSpecialty> getSpecialtyData() { return specialtyData; }
    public void setSpecialtyData(List<DataSpecialty> specialtyData) { this.specialtyData = specialtyData; }

    public List<DataItemEquipment> getItemEquipmentData() { return itemEquipmentData; }
    public void setItemEquipmentData(List<DataItemEquipment> itemEquipmentData) { this.itemEquipmentData = itemEquipmentData; }

    public List<DataTraining> getTrainingData() { return trainingData; }
    public void setTrainingData(List<DataTraining> trainingData) { this.trainingData = trainingData; }

    public List<DataTechPerm> getTechPermData() { return techPermData; }
    public void setTechPermData(List<DataTechPerm> techPermData) { this.techPermData = techPermData; }

    /**
     * Injects permanent statuses into training entries based on their grant ids and techPermData.
     */
    private void applyTechPermsToTraining() {
        if (trainingData == null || techPermData == null) return;
        for (DataTraining t : trainingData) {
            if (t == null || t.getGrant() == null) continue;
            for (Integer gid : t.getGrant()) {
                if (gid == null) continue;
                DataTechPerm perm = techPermData.stream()
                        .filter(p -> p != null && p.getId() == gid)
                        .findFirst()
                        .orElse(null);
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
