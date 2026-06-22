// CHECKED

package eternity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StoreRuleData {
    private static final String DATA_DIR = "Data";
    private static final Path DATA_PATH = AppPaths.dataDir();
    private static final ObjectMapper MAPPER = new ObjectMapper();

	private final List<DataColor> colorData;
	private final List<DataLevel> levelData;
	private final List<DataRace> raceData;
	private final List<DataClass> classData;
	private final List<DataDeity> deityData;
	private final List<DataDomain> domainData;
	private final List<DataTechnical> technicalData;
	private final List<DataVow> vowData;
	private final List<DataSkill> skillData;
	private final List<DataSpecialty> specialtyData;
	private final List<DataItemEquipment> itemEquipmentData;
	private final List<DataItemWeapon> itemWeaponData;
	private final List<DataTraining> trainingData;
	private final List<DataTechPerm> techPermData;
    private final List<DataAction> actionData;

	
	public StoreRuleData() {
        startUp();        
        
        // Load all JSON data files
        colorData          = safeLoad("colordata.json",        DataColor[].class);
        levelData          = safeLoad("leveldata.json",        DataLevel[].class);
        raceData           = safeLoad("racedata.json",         DataRace[].class);
        classData          = safeLoad("classdata.json",        DataClass[].class);
        deityData          = safeLoad("deitydata.json",        DataDeity[].class);
        domainData         = safeLoad("domaindata.json",       DataDomain[].class);
        technicalData      = safeLoad("technicaldata.json",    DataTechnical[].class);
        vowData            = safeLoad("vowdata.json",          DataVow[].class);
        skillData          = safeLoad("skilldata.json",        DataSkill[].class);
        specialtyData      = safeLoad("specialtydata.json",    DataSpecialty[].class);
        itemEquipmentData  = safeLoad("itemequipdata.json",    DataItemEquipment[].class);
        itemWeaponData     = safeLoad("itemweapondata.json",   DataItemWeapon[].class);
        techPermData       = safeLoad("techpermdata.json",     DataTechPerm[].class);
        trainingData       = safeLoad("trainingdata.json",     DataTraining[].class);
        actionData         = safeLoad("actiondata.json",       DataAction[].class);
        
        finishTrainingData();
    }

    private static void startUp() {
        try {
            if (!Files.exists(DATA_PATH)) Files.createDirectories(DATA_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create base data directory: " + DATA_DIR, e);
        }
    }
    
	
	private static <T> List<T> safeLoad(String filename, Class<T[]> type) {
        try {
            List<T> list = loadList(filename, type);
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

    /**
     * Load a JSON array from disk and return a list.
     * Example: loadList("racedata.json", DataRace[].class)
     */
    public static <T> List<T> loadList(String filename, Class<T[]> arrayType) {
        Path path = DATA_PATH.resolve(filename);
        if (!Files.exists(path)) return List.of(); // empty list if file missing
        try {
            T[] arr = MAPPER.readValue(path.toFile(), arrayType);
            if (arr == null) return List.of();
            return Collections.unmodifiableList(Arrays.asList(arr));
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + path.toString(), e);
        }
    }

    /**
     * Injects permanent statuses into training entries based on their grant ids and techPermData.
     */
    private void finishTrainingData() {
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

	// --- Getters ---

    public List<DataColor> getColorData() { return colorData; }
    public List<DataLevel> getLevelData() { return levelData; }
    public List<DataRace> getRaceData() { return raceData; }
    public List<DataClass> getClassData() { return classData; }
    public List<DataDeity> getDeityData() { return deityData; }
    public List<DataDomain> getDomainData() { return domainData; }
    public List<DataTechnical> getTechnicalData() { return technicalData; }
    public List<DataVow> getVowData() { return vowData; }
    public List<DataSkill> getSkillData() { return skillData; }
    public List<DataSpecialty> getSpecialtyData() { return specialtyData; }
    public List<DataItemEquipment> getItemEquipmentData() { return itemEquipmentData; }
    public List<DataItemWeapon> getItemWeaponData() { return itemWeaponData; }
    public List<DataTraining> getTrainingData() { return trainingData; }
    public List<DataTechPerm> getTechPermData() { return techPermData; }
    public List<DataAction> getActionData() { return actionData; }
}
