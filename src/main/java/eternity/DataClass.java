package eternity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Class Information
 */
public class DataClass {
    @JsonProperty private int ID;
    @JsonProperty private String name;
    @JsonProperty private String description;
    @JsonProperty private boolean baseClass;
    @JsonProperty private String primaryAtt;
    @JsonProperty private String secondaryAtt;
    @JsonProperty private String role;
    @JsonProperty private double hpScaling;
    @JsonProperty private double auraScaling;
    @JsonProperty private String armor;
    @JsonProperty private String profLabel;
    @JsonProperty private List<String> profAuto;
    @JsonProperty private List<String> profPick;
    @JsonProperty private int[] statScaling; 
    @JsonProperty private List<String> resourceNames;
    @JsonProperty private int abilBase;
    @JsonProperty private int abilOffset;

    // --- Constructors ---

    public DataClass() { this(-1, "", "", true, "", "", "", 0.0, 0.0, "", "", new ArrayList<>(), new ArrayList<>(), new int[]{0, 0, 0, 0, 0, 0}, new ArrayList<>(), -1, -1); }
    public DataClass(DataClass src) { this(src.ID, src.name, src.description, src.baseClass, src.primaryAtt, src.secondaryAtt, src.role, src.hpScaling, src.auraScaling, src.armor, src.profLabel, src.profAuto, src.profPick, src.statScaling, src.resourceNames, src.abilBase, src.abilOffset); }
    
    public DataClass(int ID, String name, String description, boolean baseClass, String primaryAtt, String secondaryAtt, String role, double hpScaling, double auraScaling, String armor, String profLabel, 
    		List<String> profAuto, List<String> profPick, int[] statScaling, List<String> resourceNames, int abilBase, int abilOffset) {
        this.ID = ID;
        this.name = safe(name);
        this.description = safe(description);
        this.baseClass = baseClass;
        this.primaryAtt = safe(primaryAtt);
        this.secondaryAtt = safe(secondaryAtt);
        this.role = safe(role);
        this.hpScaling = hpScaling;
        this.auraScaling = auraScaling;
        this.armor = safe(armor);
        this.profLabel = safe(profLabel);
        this.profAuto = new ArrayList<>(profAuto);
        this.profPick = new ArrayList<>(profPick);
        this.statScaling = (statScaling != null) ? statScaling.clone() : new int[]{0,0,0,0,0,0};
        this.resourceNames = new ArrayList<>(resourceNames);
        this.abilBase = abilBase;
        this.abilOffset = abilOffset;
    }

    // --- Getters & Setters ---

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    public boolean getBaseClass() { return baseClass; }
    public void setBaseClass(boolean baseClass) { this.baseClass = baseClass; }

    public String getPrimaryAtt() { return primaryAtt; }
    public void setPrimaryAtt(String primaryAtt) { this.primaryAtt = safe(primaryAtt); }

    public String getSecondaryAtt() { return secondaryAtt; }
    public void setSecondaryAtt(String secondaryAtt) { this.secondaryAtt = safe(secondaryAtt); }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = safe(role); }

    public double getHpScaling() { return hpScaling; }
    public void setHpScaling(double hpScaling) { this.hpScaling = hpScaling; }

    public double getAuraScaling() { return auraScaling; }
    public void setAuraScaling(double auraScaling) { this.auraScaling = auraScaling; }

    public String getArmor() { return armor; }
    public void setArmor(String armor) { this.armor = safe(armor); }

    public String getProfLabel() { return profLabel; }
    public void setProfLabel(String profLabel) { this.profLabel = safe(profLabel); }

    public List<String> getProfAuto() { return new ArrayList<>(profAuto); }
    public void setProfAuto(List<String> profAuto) { this.profAuto = (profAuto == null ? new ArrayList<>() : new ArrayList<>(profAuto)); }

    public List<String> getProfPick() { return new ArrayList<>(profPick); }
    public void setProfPick(List<String> profPick) { this.profPick = (profPick == null ? new ArrayList<>() : new ArrayList<>(profPick)); }

    public int[] getStatScaling() { return statScaling.clone(); }
    public void setStatScaling(int[] statScaling) { this.statScaling = (statScaling == null ? new int[]{0,0,0,0,0,0} : statScaling.clone()); }

    public List<String> getResourceNames() { return new ArrayList<>(resourceNames); }
    public void setResourceNames(List<String> resourceNames) { this.resourceNames = (resourceNames == null ? new ArrayList<>() : new ArrayList<>(resourceNames)); }

    public int getAbilBase() { return abilBase; }
    public void setAbilBase(int abilBase) { this.abilBase = abilBase; }
    
    public int getAbilOffset() { return abilOffset; }
    public void setAbilOffset(int abilOffset) { this.abilOffset = abilOffset; }

    // --- Helpers ---

    private static String safe(String s) { return s == null ? "" : s; }
    
    @Override
    public String toString() {
        return "DataClass {\n" + "  ID: " + ID + ",\n" + "  name: \"" + name + "\",\n" + "  description: \"" + description + "\",\n" +
            "  baseClass: " + baseClass + ",\n" + "  primaryAtt: \"" + primaryAtt + "\",\n" + "  secondaryAtt: \"" + secondaryAtt + "\",\n" +
            "  role: \"" + role + "\",\n" + "  hpScaling: " + hpScaling + ",\n" + "  auraScaling: " + auraScaling + ",\n" + "  armor: \"" + armor + "\",\n" +
            "  profLabel: \"" + profLabel + "\",\n" + "  profAuto: " + profAuto + ",\n" + "  profPick: " + profPick + ",\n" + "  statScaling: [" + statScaling[0] + ", " + statScaling[1] + ", " + statScaling[2] + ", " + statScaling[3] +  ", " + statScaling[4] + ", " + statScaling[5] + "],\n" +
            "  resourceNames: " + resourceNames + ",\n" + "  abilBase: " + abilBase + ",\n" + "  abilOffset: " + abilOffset + "\n" + "}";
    }
}