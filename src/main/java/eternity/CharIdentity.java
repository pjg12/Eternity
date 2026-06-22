package eternity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic identity and progression data for a character.
 */
public class CharIdentity implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonIgnore
    private StoreCharData owner;

    // --- Fields ---
    @JsonProperty private int index;
    @JsonProperty private String name;
    @JsonProperty private String nickname;
    @JsonProperty private String campaign;
    @JsonProperty private LocalDateTime campaignStartDate; 
    @JsonProperty private Duration campaignElapsedTime;
    @JsonProperty private LocalDate birthday;
    @JsonProperty private boolean birthdayManual;
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
    @JsonProperty private String notes;
    @JsonProperty private Timestamp updated;
    @JsonProperty private Timestamp createdAt;
    @JsonProperty private Timestamp lastLevelUp;
    @JsonProperty private Duration timeSinceLastLevel = Duration.ZERO;

    // --- Constructors ---
    public CharIdentity() {
        this.index = 0;
        this.name = "";
        this.nickname = "";
        this.campaign = "";
        this.campaignStartDate = LocalDateTime.of(65, 1, 1, 8, 0);
        this.campaignElapsedTime = Duration.ZERO;
        this.birthday = this.campaignStartDate.toLocalDate().minusYears(18); // default age 18
        this.birthdayManual = false;
        this.race = "?";
        this.charRacePick = new ArrayList<>();
        this.charClass = "?";
        this.charSubclass = "?";
        this.charClassPick = new ArrayList<>();
        this.level = 1;
        this.exp = 0f;
        this.gender = "";
        this.size = "?";
        this.height = "";
        this.weight = "";
        this.eyes = "";
        this.hair = "";
        this.physical = "";
        this.personality = "";
        this.notes = "";
        long now = System.currentTimeMillis();
        this.updated = new Timestamp(now);
        this.createdAt = new Timestamp(now);
        this.lastLevelUp = new Timestamp(now);
        this.timeSinceLastLevel = Duration.ZERO;
    }

    public CharIdentity(CharIdentity other) {
        this.index = other.index;
        this.name = other.name;
        this.nickname = other.nickname;
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
        this.notes = other.notes;
        this.updated = other.updated;
        this.createdAt = other.createdAt;
        this.lastLevelUp = other.lastLevelUp;
        this.timeSinceLastLevel = other.timeSinceLastLevel;
        this.birthdayManual = other.birthdayManual;
    }

    // --- Getters / Setters ---
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getName() { return name; }
    public void setName(String name) { this.name = safeString(name); }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = safeString(nickname); }

    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = safeString(campaign); }
    
    public LocalDateTime  getCampaignStartDate() { return campaignStartDate; }
    public void setCampaignStartDate(LocalDateTime  campaignStartDate) { this.campaignStartDate = campaignStartDate; }
    
    public Duration getCampaignElapsedTime() { return campaignElapsedTime; }
    public void setCampaignElapsedTime(Duration campaignElapsedTime) { this.campaignElapsedTime = (campaignElapsedTime == null) ? Duration.ZERO : campaignElapsedTime; }
    
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
        this.birthdayManual = true;
    }
    @JsonIgnore
    public boolean isBirthdayManual() { return birthdayManual; }
    public void randomBirthday(int age) {
        int birthYear = getYearByAge(age);
        this.birthday = CharIdentity.randomDayOfYear(birthYear);
        this.birthdayManual = true;
    }

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

    @JsonIgnore
    public int getNextAt() { return (this.level * 1000); }

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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = safeString(notes); }
    
    public Timestamp getUpdated() { return updated; }
    public void setUpdated(Timestamp updated) { this.updated = updated; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastLevelUp() { return lastLevelUp; }
    public void setLastLevelUp(Timestamp lastLevelUp) { this.lastLevelUp = lastLevelUp; }

    public Duration getTimeSinceLastLevel() { return timeSinceLastLevel == null ? Duration.ZERO : timeSinceLastLevel; }
    public void setTimeSinceLastLevel(Duration timeSinceLastLevel) { this.timeSinceLastLevel = timeSinceLastLevel == null ? Duration.ZERO : timeSinceLastLevel; }
    
    @JsonIgnore
    public StoreCharData getOwner() { return owner; }
    public void setOwner(StoreCharData owner) { this.owner = owner; }

    // --- Helpers ---
    private String safeString(String value) { return (value == null) ? "" : value.trim(); }
    private List<String> safeList(List<String> list) { return (list == null) ? new ArrayList<>() : new ArrayList<>(list); }

    /**
     * Returns a random date within the specified year.
     * Uses current year when none is provided.
     */
    public static LocalDate randomDayOfYear() {
        return randomDayOfYear(LocalDate.now().getYear());
    }

    public static LocalDate randomDayOfYear(int year) {
        int lengthOfYear = LocalDate.ofYearDay(year, 1).lengthOfYear();
        int dayOfYear = ThreadLocalRandom.current().nextInt(lengthOfYear) + 1; // 1..length
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    // --- Derived Utility ---

    /**
     * Returns the current in-campaign date and time.
     * campaignStartDate + campaignElapsedTime
     */
    @JsonIgnore // derived value; exclude from serialization
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
    @JsonIgnore // derived value; exclude from serialization
    public int getAge() {
        if (birthday == null) return -1;
        LocalDateTime currentCampaignDateTime = getCurrentCampaignDateTime();
        if (currentCampaignDateTime == null) return -1;

        LocalDate currentDate = currentCampaignDateTime.toLocalDate();
        return java.time.Period.between(birthday, currentDate).getYears();
    }

    /**
     * Given an age in years, returns the in-campaign calendar year that is
     * {@code age} years before the current campaign year.
     *
     * Returns -1 when the campaign clock is not set.
     */
    public int getYearByAge(int age) {
        LocalDateTime currentCampaignDateTime = getCurrentCampaignDateTime();
        if (currentCampaignDateTime == null) {
            return -1;
        }
        int currentYear = currentCampaignDateTime.getYear();
        return currentYear - Math.max(0, age);
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

