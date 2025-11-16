package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic identity and progression data for a character.
 */
public class CharIdentity implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Fields ---
    @JsonProperty private int index;
    @JsonProperty private String name;
    @JsonProperty private String campaign;
    @JsonProperty private LocalDateTime campaignStartDate; 
    @JsonProperty private Duration campaignElapsedTime;
    @JsonProperty private LocalDate birthday;
    @JsonProperty private String race;
    @JsonProperty private List<String> charRacePick;
    @JsonProperty private String charClass;
    @JsonProperty private String charSubclass;
    @JsonProperty private List<String> charClassPick;
    @JsonProperty private int level;
    @JsonProperty private float exp;
    @JsonProperty private String gender;
    @JsonProperty private String size;
    @JsonProperty private String height;
    @JsonProperty private String weight;
    @JsonProperty private String eyes;
    @JsonProperty private String hair;
    @JsonProperty private String physical;
    @JsonProperty private String personality;
    @JsonProperty private Timestamp updated;

    // --- Constructors ---
    public CharIdentity() {
        this.index = 0;
        this.name = "";
        this.campaign = "";
        this.campaignStartDate = null;
        this.campaignElapsedTime = Duration.ZERO;
        this.birthday = null;
        this.race = "";
        this.charRacePick = new ArrayList<>();
        this.charClass = "";
        this.charSubclass = "";
        this.charClassPick = new ArrayList<>();
        this.level = 1;
        this.exp = 0f;
        this.gender = "";
        this.size = "";
        this.height = "";
        this.weight = "";
        this.eyes = "";
        this.hair = "";
        this.physical = "";
        this.personality = "";
        this.updated = new Timestamp(System.currentTimeMillis());
    }

    public CharIdentity(CharIdentity other) {
        this.index = other.index;
        this.name = other.name;
        this.campaign = other.campaign;
        this.campaignStartDate = other.campaignStartDate;
        this.campaignElapsedTime = other.campaignElapsedTime;
        this.birthday = other.birthday;
        this.race = other.race;
        this.charRacePick = new ArrayList<>(other.charRacePick);
        this.charClass = other.charClass;
        this.charSubclass = other.charSubclass;
        this.charClassPick = new ArrayList<>(other.charClassPick);
        this.level = other.level;
        this.exp = other.exp;
        this.gender = other.gender;
        this.size = other.size;
        this.height = other.height;
        this.weight = other.weight;
        this.eyes = other.eyes;
        this.hair = other.hair;
        this.physical = other.physical;
        this.personality = other.personality;
        this.updated = other.updated;
    }

    // --- Getters / Setters ---
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safeString(name); }

    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = safeString(campaign); }
    
    public LocalDateTime  getCampaignStartDate() { return campaignStartDate; }
    public void setCampaignStartDate(LocalDateTime  campaignStartDate) { this.campaignStartDate = campaignStartDate; }
    
    public Duration getCampaignElapsedTime() { return campaignElapsedTime; }
    public void setCampaignElapsedTime(Duration campaignElapsedTime) { this.campaignElapsedTime = (campaignElapsedTime == null) ? Duration.ZERO : campaignElapsedTime; }
    
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getRace() { return race; }
    public void setRace(String race) { this.race = safeString(race); }

    public List<String> getCharRacePick() { return Collections.unmodifiableList(charRacePick); }
    public void setCharRacePick(List<String> charRacePick) { this.charRacePick = safeList(charRacePick); }
    public void addRacePick(String pick) { this.charRacePick.add(safeString(pick)); }

    public String getCharClass() { return charClass; }
    public void setCharClass(String charClass) { this.charClass = safeString(charClass); }

    public String getCharSubclass() { return charSubclass; }
    public void setCharSubclass(String charSubclass) { this.charSubclass = safeString(charSubclass); }

    public List<String> getCharClassPick() { return Collections.unmodifiableList(charClassPick); }
    public void setCharClassPick(List<String> charClassPick) { this.charClassPick = safeList(charClassPick); }
    public void addClassPick(String pick) { this.charClassPick.add(safeString(pick)); }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }
    
    public float getExp() { return exp; }
    public void setExp(float exp) { this.exp = Math.max(0f, exp); }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = safeString(gender); }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = safeString(size); }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = safeString(height); }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = safeString(weight); }

    public String getEyes() { return eyes; }
    public void setEyes(String eyes) { this.eyes = safeString(eyes); }

    public String getHair() { return hair; }
    public void setHair(String hair) { this.hair = safeString(hair); }

    public String getPhysical() { return physical; }
    public void setPhysical(String physical) { this.physical = safeString(physical); }

    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = safeString(personality); }
    
    public Timestamp getUpdated() { return updated; }
    public void setUpdated(Timestamp updated) { this.updated = updated; }

    // --- Helpers ---
    private String safeString(String value) { return (value == null) ? "" : value.trim(); }
    private List<String> safeList(List<String> list) { return (list == null) ? new ArrayList<>() : new ArrayList<>(list); }

    // --- Derived Utility ---

    /**
     * Returns the current in-campaign date and time.
     * campaignStartDate + campaignElapsedTime
     */
    public LocalDateTime getCurrentCampaignDateTime() {
        if (campaignStartDate == null) return null;
        return campaignStartDate.plus(campaignElapsedTime);
    }
    
    /**
     * Adds the given amount of time to the campaign's elapsed time.
     * Null or negative durations are ignored.
     */
    public void addCampaignTime(Duration amount) {
        if (amount == null || amount.isNegative()) {
            return;
        }
        this.campaignElapsedTime = this.campaignElapsedTime.plus(amount);
    }
    
    /**
     * Calculates the character's age in years based on birthday
     * and the current in-campaign date and time.
     *
     * Returns -1 if birthday or campaign time is not set.
     */
    public int getAge() {
        if (birthday == null) return -1;
        LocalDateTime currentCampaignDateTime = getCurrentCampaignDateTime();
        if (currentCampaignDateTime == null) return -1;

        LocalDate currentDate = currentCampaignDateTime.toLocalDate();
        return java.time.Period.between(birthday, currentDate).getYears();
    }
    
    // --- Debug / Logging ---
    @Override
    public String toString() {
        return "CharIdentity{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", campaign='" + campaign + '\'' +
                ", race='" + race + '\'' +
                ", charClass='" + charClass + '\'' +
                ", charSubclass='" + charSubclass + '\'' +
                ", level=" + level +
                '}';
    }
}