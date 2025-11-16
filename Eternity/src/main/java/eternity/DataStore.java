package eternity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
	
	private final DataBuilder builder;
	
	public DataStore() {
		Path path = Paths.get("c:\\Eternity\\Eternity\\data\\");
        this.builder = new DataBuilder(path);
        
        // Load all JSON data files
        colorData          = safeLoad("colordata.json",        DataColor[].class);
        levelData          = safeLoad("leveldata.json",        DataLevel[].class);
        raceData           = safeLoad("racedata.json",         DataRace[].class);
        classData          = safeLoad("classdata.json",        DataClass[].class);
        deityData          = safeLoad("deitydata.json",        DataDeity[].class);
        skillData          = safeLoad("skilldata.json",        DataSkill[].class);
        specialtyData      = safeLoad("specialtydata.json",    DataSpecialty[].class);
        itemEquipmentData  = safeLoad("itemequipment.json",    DataItemEquipment[].class);
        trainingData       = safeLoad("trainingdata.json",     DataTraining[].class);
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
}