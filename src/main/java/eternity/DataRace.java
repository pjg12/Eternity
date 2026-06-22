package eternity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Racial Information
 */
public class DataRace {
    @JsonProperty private int ID;
    @JsonProperty private String name;
    @JsonProperty private String namett;
    @JsonProperty private String homeworld;
    @JsonProperty private String homeworldtt;
    @JsonProperty private String affiliation;
    @JsonProperty private String affiliationtt;
    @JsonProperty private String physical;
    @JsonProperty private String physicaltt;
    @JsonProperty private String personality;
    @JsonProperty private String personalitytt;
    @JsonProperty private String baseStatusDesc;
    @JsonProperty private String baseStatusDesctt;
    @JsonProperty private int[] baseStatus;              
    @JsonProperty private String scalingStatusDesc;
    @JsonProperty private String scalingStatusDesctt;
    @JsonProperty private double scalingStatus;
    @JsonProperty private String racialDesc;
    @JsonProperty private String racialDesctt;
    @JsonProperty private int racialID;
    @JsonProperty private String description;
    @JsonProperty private String size;
    @JsonProperty private boolean racePick;
    @JsonProperty private String raceReminder;

    // --- Constructors ---
    
    public DataRace() { this(-1, "", "", "", "", "", "", "", "", "", "", "", "", new int[] {-1, -1, -1}, "", "", -1, "", "", -1, "", "", false, ""); }
    public DataRace(DataRace src) { this(src.ID, src.name, src.namett, src.homeworld, src.homeworldtt, src.affiliation, src.affiliationtt, src.physical, src.physicaltt, src.personality, src.personalitytt, 
    		src.baseStatusDesc, src.baseStatusDesctt, src.baseStatus, src.scalingStatusDesc, src.scalingStatusDesctt, src.scalingStatus, src.racialDesc, src.racialDesctt, src.racialID, 
    		src.description, src.size, src.racePick, src.raceReminder); }
    
    public DataRace(int ID, String name, String namett, String homeworld, String homeworldtt, String affiliation, String affiliationtt, String physical, String physicaltt, String personality, String personalitytt, 
    	String baseStatusDesc, String baseStatusDesctt, int[] baseStatus, String scalingStatusDesc, String scalingStatusDesctt, double scalingStatus, String racialDesc, String racialDesctt, int racialID, 
    	String description, String size, boolean racePick, String raceReminder) {
    	this.ID = ID;
        this.name = name;
        this.namett = namett;
        this.homeworld = homeworld;
        this.homeworldtt = homeworldtt;
        this.affiliation = affiliation;
        this.affiliationtt = affiliationtt;
        this.physical = physical;
        this.physicaltt = physicaltt;
        this.personality = personality;
        this.personalitytt = personalitytt;
        this.baseStatusDesc = baseStatusDesc;
        this.baseStatusDesctt = baseStatusDesctt;
        this.baseStatus = baseStatus != null ? baseStatus.clone() : new int[]{-1, -1, -1};
        this.scalingStatusDesc = scalingStatusDesc;
        this.scalingStatusDesctt = scalingStatusDesctt;
        this.scalingStatus = scalingStatus;
        this.racialDesc = racialDesc;
        this.racialDesctt = racialDesctt;
        this.racialID = racialID;
        this.description = description;
        this.size = safe(size);
        this.racePick = racePick;
        this.raceReminder = raceReminder;
    }
    
    // --- Getters & Setters ---
    
    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getNamett() { return namett; }
    public void setNamett(String namett) { this.namett = safe(namett); }

    public String getHomeworld() { return homeworld; }
    public void setHomeworld(String homeworld) { this.homeworld = safe(homeworld); }

    public String getHomeworldtt() { return homeworldtt; }
    public void setHomeworldtt(String homeworldtt) { this.homeworldtt = safe(homeworldtt); }

    public String getAffiliation() { return affiliation; }
    public void setAffiliation(String affiliation) { this.affiliation = safe(affiliation); }

    public String getAffiliationtt() { return affiliationtt; }
    public void setAffiliationtt(String affiliationtt) { this.affiliationtt = safe(affiliationtt); }

    public String getPhysical() { return physical; }
    public void setPhysical(String physical) { this.physical = safe(physical); }

    public String getPhysicaltt() { return physicaltt; }
    public void setPhysicaltt(String physicaltt) { this.physicaltt = safe(physicaltt); }

    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = safe(personality); }

    public String getPersonalitytt() { return personalitytt; }
    public void setPersonalitytt(String personalitytt) { this.personalitytt = safe(personalitytt); }

    public String getBaseStatusDesc() { return baseStatusDesc; }
    public void setBaseStatusDesc(String baseStatusDesc) { this.baseStatusDesc = safe(baseStatusDesc); }

    public String getBaseStatusDesctt() { return baseStatusDesctt; }
    public void setBaseStatusDesctt(String baseStatusDesctt) { this.baseStatusDesctt = safe(baseStatusDesctt); }

    public int[] getBaseStatus() { return baseStatus.clone(); }
    public void setBaseStatus(int[] baseStatus) {
        if (baseStatus == null || baseStatus.length == 0)
            this.baseStatus = new int[]{-1, -1, -1};
        else
            this.baseStatus = baseStatus.clone();
    }
    public DataStatus[] getBaseDataStatus() {
        if (baseStatus == null || baseStatus.length == 0) return new DataStatus[0];

        java.util.LinkedHashMap<String, Double> totals = new java.util.LinkedHashMap<>();
        int lastIndex = -1;
        for (int i = 0; i < baseStatus.length; i++) {
            if (baseStatus[i] == -1) break;
            lastIndex = i;
        }
        if (lastIndex < 0) return new DataStatus[0];

        for (int i = 0; i <= lastIndex; i++) {
            String attribute = mapBaseStatusCode(baseStatus[i]);
            if (attribute == null) continue;
            double delta = (i == lastIndex && lastIndex > 0) ? -1.0 : 1.0;
            totals.merge(attribute, delta, Double::sum);
        }

        DataStatus[] statuses = new DataStatus[totals.size()];
        int index = 0;
        for (java.util.Map.Entry<String, Double> entry : totals.entrySet()) {
            DataStatus status = new DataStatus();
            status.setName("Base Racial");
            status.setAffinity("None");
            status.setDescription("Racial base status");
            status.setAttribute(entry.getKey());
            status.setSeverity(entry.getValue());
            status.setDurationType("Passive");
            status.setDuration(-1);
            statuses[index++] = status;
        }
        return statuses;
    }

    private String mapBaseStatusCode(int code) {
        return switch (code) {
            case 1 -> "BSTR";
            case 2 -> "BDEX";
            case 3 -> "BCON";
            case 4 -> "BFOC";
            case 5 -> "BCTL";
            case 6 -> "BCAP";
            default -> null;
        };
    }

    public String getScalingStatusDesc() { return scalingStatusDesc; }
    public void setScalingStatusDesc(String scalingStatusDesc) { this.scalingStatusDesc = safe(scalingStatusDesc); }

    public String getScalingStatusDesctt() { return scalingStatusDesctt; }
    public void setScalingStatusDesctt(String scalingStatusDesctt) { this.scalingStatusDesctt = safe(scalingStatusDesctt); }

    public double getScalingStatus() { return scalingStatus; }
    public void setScalingStatus(double scalingStatus) { this.scalingStatus = scalingStatus; }

    public String getRacialDesc() { return racialDesc; }
    public void setRacialDesc(String racialDesc) { this.racialDesc = safe(racialDesc); }

    public String getRacialDesctt() { return racialDesctt; }
    public void setRacialDesctt(String racialDesctt) { this.racialDesctt = safe(racialDesctt); }

    public int getRacialID() { return racialID; }
    public void setRacialID(int racialID) { this.racialID = racialID; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = safe(size); }

    public boolean getRacePick() { return racePick; }
    public void setRacePick(boolean racePick) { this.racePick = racePick; }

    public String getRaceReminder() { return raceReminder; }
    public void setRaceReminder(String raceReminder) { this.raceReminder = safe(raceReminder); }

    // --- Helpers ---
    
    private static String safe(String val) { return val == null ? "" : val; }

    @Override
    public String toString() {
        return "DataRace{" + "ID=" + ID + ", name='" + name + '\'' + ", homeworld='" + homeworld + '\'' + ", affiliation='" + affiliation + '\'' +
            ", baseStatus=" + Arrays.toString(baseStatus) + ", scalingStatus=" + scalingStatus + ", size='" + size + '\'' + ", racialID=" + racialID + '}';
    }
}
