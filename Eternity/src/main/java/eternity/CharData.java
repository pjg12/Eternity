package eternity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Main character container, orchestrating all character subsystems.
 * 
 * Holds:
 *  - Identity data (name, level, race, class, etc.)
 *  - Attribute system (StatBlocks for all stats)
 *  - Resources (HP, Aura, class resources, reactions)
 *  - Skills & Specialties
 *  - Inventory
 *  - Training (Aura techniques, affinities, domains)
 *  - Combat (derived values)
 *
 * CharData is the root of the character sheet object graph.
 */
public class CharData {

    // ---------------------------------------------------------
    // Core subsystems
    // ---------------------------------------------------------

    private final CharIdentity identity;
    private final CharAttributes attributes;
    private final CharResources resources;
    private final CharSpecials specials;
    private final CharInventory inventory;
    private final CharTraining training;
    private final CharCombat combat;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharData() {
        this.identity = new CharIdentity();
        this.attributes = new CharAttributes();
        this.resources = new CharResources();
        this.specials = new CharSpecials();
        this.inventory = new CharInventory();
        this.training = new CharTraining();
        this.combat = new CharCombat();
    }

    // ---------------------------------------------------------
    // Update Pipeline
    // ---------------------------------------------------------

    /**
     * Updates all subsystems in correct order.
     * Call whenever anything changes (level, gear, buffs, etc.)
     */
    public void updateAll() {
        // Update max techs based on level data
        try {
            DataQuery dq = new DataQuery();
            int lvl = identity != null ? identity.getLevel() : 1;
            DataLevel dataLevel = dq.getLevel(lvl);

            // Push base resource caps from level data into the live stat blocks
            if (resources != null && dataLevel != null) {
                resources.setBaseMaxHP(dataLevel.getBaseHP());
                resources.setBaseMaxAura(dataLevel.getBaseAura());

                // Clamp current resource deltas so they never exceed the new maxima
                resources.setLostHP(Math.min(resources.getLostHP(), resources.getMaxHP()));
                resources.setSpentAura(Math.min(resources.getSpentAura(), resources.getMaxAura()));
                resources.setOccupiedAura(Math.min(resources.getOccupiedAura(), resources.getMaxAura()));
            }

            // Sync racial specialty from race data
            if (identity != null && specials != null) {
                DataRace race = dq.getRaceByName(identity.getRace());
                if (race != null && race.getRacialID() >= 0) {
                    DataSpecialty racialSpec = dq.getSpecialtyById(race.getRacialID());
                    if (racialSpec != null) {
                        specials.setRacialSpecialty(new DataSpecialty(racialSpec));
                    }
                    // Apply racial base attribute statuses
                    if (attributes != null) {
                        DataStatus[] racialStatuses = race.getBaseDataStatus();
                        if (racialStatuses != null) {
                            for (DataStatus rs : racialStatuses) {
                                if (rs == null || rs.getAttribute() == null) continue;
                                attributes.removeStatus("attribute", rs.getAttribute(), rs.getName());
                                attributes.addStatus("attribute", rs.getAttribute(), rs);
                            }
                        }
                    }
                }

                // Sync class specialty based on class's abilBase plus 10 * training rank
                DataClass dataClass = dq.getClassByName(identity.getCharClass());
                int classRank = training != null ? training.getClassTrainingRank() : lvl;
                if (dataClass != null) {
                    List<DataSpecialty> classSpecs = new ArrayList<>();
                    for (int i = 1; i <= classRank; i++) {
                        DataLevel levelData = dq.getLevel(i);
                        int generalCount = levelData != null ? levelData.getClassGeneral() : 0;
                        int specCount = levelData != null ? levelData.getClassSpec() : 0;
                        for (int j = 0; j < generalCount; j++) {
                            int specId = dataClass.getAbilBase() + (10 * i) + j;
                            DataSpecialty baseSpec = dq.getSpecialtyById(specId);
                            if (baseSpec != null) classSpecs.add(new DataSpecialty(baseSpec)); // defensive copy
                        }
                        for (int j = 0; j < specCount; j++) {
                            int specId = dataClass.getAbilOffset() + (10 * i) + j + dataClass.getAbilOffset();
                            DataSpecialty baseSpec = dq.getSpecialtyById(specId);
                            if (baseSpec != null) classSpecs.add(new DataSpecialty(baseSpec)); // defensive copy
                        }
                    }
                    specials.setClassSpecialties(classSpecs);

                    // Primary attribute bonus to ATK: 0.5 * primary attribute mod
                    if (attributes != null && dataClass.getPrimaryAtt() != null) {
                        String prim = dataClass.getPrimaryAtt().toUpperCase();
                        int attVal = attributes.getAttribute(prim);
                        int attMod = attVal - 10;
                        double sev = 0.5 * attMod;
                        DataStatus atkBonus = new DataStatus();
                        atkBonus.setName("ClassPrimaryATK");
                        atkBonus.setAttribute("ATK");
                        atkBonus.setDurationType("Permanent");
                        atkBonus.setSeverity(sev);
                        atkBonus.setAffinity("None");
                        atkBonus.setDescription("Primary attribute bonus to ATK");
                        attributes.removeStatus("combat", "ATK", "ClassPrimaryATK");
                        attributes.addStatus("combat", "ATK", atkBonus);

                        // Additional FOC-based ATK bonus: 0.5 * FOC mod
                        int focModOnly = attributes.getAttribute("FOC") - 10;
                        double focAtkSev = 0.5 * focModOnly;
                        DataStatus focAtkBonus = new DataStatus();
                        focAtkBonus.setName("FocusATKBonus");
                        focAtkBonus.setAttribute("ATK");
                        focAtkBonus.setDurationType("Permanent");
                        focAtkBonus.setSeverity(focAtkSev);
                        focAtkBonus.setAffinity("None");
                        focAtkBonus.setDescription("Focus-based ATK bonus");
                        attributes.removeStatus("combat", "ATK", "FocusATKBonus");
                        attributes.addStatus("combat", "ATK", focAtkBonus);

                        // Additional FOC-based APP bonus: 0.5 * FOC mod
                        DataStatus focAppBonus = new DataStatus();
                        focAppBonus.setName("FocusAPPBonus");
                        focAppBonus.setAttribute("APP");
                        focAppBonus.setDurationType("Permanent");
                        focAppBonus.setSeverity(focAtkSev);
                        focAppBonus.setAffinity("None");
                        focAppBonus.setDescription("Focus-based APP bonus");
                        attributes.removeStatus("combat", "APP", "FocusAPPBonus");
                        attributes.addStatus("combat", "APP", focAppBonus);

                        DataStatus infBonus = new DataStatus();
                        infBonus.setName("ClassPrimaryAPP");
                        infBonus.setAttribute("APP");
                        infBonus.setDurationType("Permanent");
                        infBonus.setSeverity(sev);
                        infBonus.setAffinity("None");
                        infBonus.setDescription("Primary attribute bonus to APP");
                        attributes.removeStatus("combat", "APP", "ClassPrimaryAPP");
                        attributes.addStatus("combat", "APP", infBonus);

                        DataStatus tdmBonus = new DataStatus();
                        tdmBonus.setName("ClassPrimaryTDMG");
                        tdmBonus.setAttribute("TDMG");
                        tdmBonus.setDurationType("Permanent");
                        tdmBonus.setSeverity(sev);
                        tdmBonus.setAffinity("None");
                        tdmBonus.setDescription("Primary attribute bonus to Total Damage");
                        attributes.removeStatus("damage", "TDMG", "ClassPrimaryTDMG");
                        attributes.addStatus("damage", "TDMG", tdmBonus);

                        // Additional STR-based TDMG bonus: 0.5 * STR mod
                        int strModOnly = attributes.getAttribute("STR") - 10;
                        double strTdmSev = 0.5 * strModOnly;
                        DataStatus strTdmBonus = new DataStatus();
                        strTdmBonus.setName("StrTDMGBonus");
                        strTdmBonus.setAttribute("TDMG");
                        strTdmBonus.setDurationType("Permanent");
                        strTdmBonus.setSeverity(strTdmSev);
                        strTdmBonus.setAffinity("None");
                        strTdmBonus.setDescription("Strength-based Total Damage bonus");
                        attributes.removeStatus("damage", "TDMG", "StrTDMGBonus");
                        attributes.addStatus("damage", "TDMG", strTdmBonus);

                        DataStatus thealBonus = new DataStatus();
                        thealBonus.setName("ClassPrimaryTHEAL");
                        thealBonus.setAttribute("THEAL");
                        thealBonus.setDurationType("Permanent");
                        thealBonus.setSeverity(sev);
                        thealBonus.setAffinity("None");
                        thealBonus.setDescription("Primary attribute bonus to Total Healing");
                        attributes.removeStatus("damage", "THEAL", "ClassPrimaryTHEAL");
                        attributes.addStatus("damage", "THEAL", thealBonus);

                        // Fortitude bonus: 0.75 * (STR mod + CON mod)
                        int strMod = attributes.getAttribute("STR") - 10;
                        int conMod = attributes.getAttribute("CON") - 10;
                        double fortSev = 0.75 * (strMod + conMod);
                        DataStatus fortBonus = new DataStatus();
                        fortBonus.setName("ClassFortBonus");
                        fortBonus.setAttribute("FORT");
                        fortBonus.setDurationType("Permanent");
                        fortBonus.setSeverity(fortSev);
                        fortBonus.setAffinity("None");
                        fortBonus.setDescription("Strength + Constitution derived Fortitude");
                        attributes.removeStatus("defense", "FORT", "ClassFortBonus");
                        attributes.addStatus("defense", "FORT", fortBonus);

                        // Reflex bonus: 0.75 * (DEX mod + FOC mod)
                        int dexMod = attributes.getAttribute("DEX") - 10;
                        int focMod = attributes.getAttribute("FOC") - 10;
                        double refSev = 0.75 * (dexMod + focMod);
                        DataStatus refBonus = new DataStatus();
                        refBonus.setName("ClassRefBonus");
                        refBonus.setAttribute("REF");
                        refBonus.setDurationType("Permanent");
                        refBonus.setSeverity(refSev);
                        refBonus.setAffinity("None");
                        refBonus.setDescription("Dexterity + Focus derived Reflex");
                        attributes.removeStatus("defense", "REF", "ClassRefBonus");
                        attributes.addStatus("defense", "REF", refBonus);

                        // Will bonus: 0.75 * (CTL mod + CAP mod)
                        int ctlMod = attributes.getAttribute("CTL") - 10;
                        int capMod = attributes.getAttribute("CAP") - 10;
                        double willSev = 0.75 * (ctlMod + capMod);
                        DataStatus willBonus = new DataStatus();
                        willBonus.setName("ClassWillBonus");
                        willBonus.setAttribute("WILL");
                        willBonus.setDurationType("Permanent");
                        willBonus.setSeverity(willSev);
                        willBonus.setAffinity("None");
                        willBonus.setDescription("Control + Capacity derived Will");
                        attributes.removeStatus("defense", "WILL", "ClassWillBonus");
                        attributes.addStatus("defense", "WILL", willBonus);

                        // Resist All bonus: STR mod
                        strModOnly = attributes.getAttribute("STR") - 10;
                        DataStatus resBonus = new DataStatus();
                        resBonus.setName("ClassResAllBonus");
                        resBonus.setAttribute("ALL");
                        resBonus.setDurationType("Permanent");
                        resBonus.setSeverity(strModOnly);
                        resBonus.setAffinity("None");
                        resBonus.setDescription("Strength derived Resist All");
                        attributes.removeStatus("resist", "ALL", "ClassResAllBonus");
                        attributes.addStatus("resist", "ALL", resBonus);

                        // Dodge bonus: 0.5 * DEX mod
                        int dexModOnly = attributes.getAttribute("DEX") - 10;
                        double dodgeSev = 0.5 * dexModOnly;
                        DataStatus dodgeBonus = new DataStatus();
                        dodgeBonus.setName("DexDodgeBonus");
                        dodgeBonus.setAttribute("DODGE");
                        dodgeBonus.setDurationType("Permanent");
                        dodgeBonus.setSeverity(dodgeSev);
                        dodgeBonus.setAffinity("None");
                        dodgeBonus.setDescription("Dexterity-based Dodge bonus");
                        attributes.removeStatus("defense", "DODGE", "DexDodgeBonus");
                        attributes.addStatus("defense", "DODGE", dodgeBonus);

                        // HP multiplier bonus: 0.05 * CON mod applied as multi on maxHP
                        int conModOnly = attributes.getAttribute("CON") - 10;
                        double hpMultiSev = 0.05 * conModOnly;
                        StatBlock[] hpBlocks = resources.getMaxHPBlocks();
                        if (hpBlocks != null && hpBlocks.length > 0 && hpBlocks[0] != null) {
                            hpBlocks[0].removeMulti("ConHPMulti");
                            DataStatus hpMulti = new DataStatus();
                            hpMulti.setName("ConHPMulti");
                            hpMulti.setAttribute("HPMULTI");
                            hpMulti.setDurationType("Permanent");
                            hpMulti.setSeverity(hpMultiSev);
                            hpMulti.setAffinity("None");
                            hpMulti.setDescription("Constitution-based HP multiplier");
                            hpBlocks[0].addMulti(hpMulti);
                        }

                        // Aura multiplier bonus: 0.05 * CAP mod applied as multi on maxAura
                        int capModOnly = attributes.getAttribute("CAP") - 10;
                        double auraMultiSev = 0.05 * capModOnly;
                        StatBlock[] auraBlocks = resources.getMaxAuraBlocks();
                        if (auraBlocks != null && auraBlocks.length > 0 && auraBlocks[0] != null) {
                            auraBlocks[0].removeMulti("CapAuraMulti");
                            DataStatus auraMulti = new DataStatus();
                            auraMulti.setName("CapAuraMulti");
                            auraMulti.setAttribute("AURAMULTI");
                            auraMulti.setDurationType("Permanent");
                            auraMulti.setSeverity(auraMultiSev);
                            auraMulti.setAffinity("None");
                            auraMulti.setDescription("Capacity-based Aura multiplier");
                            auraBlocks[0].addMulti(auraMulti);
                        }

                        // Total damage bonus: 0.5 * CTL mod
                        int ctlModOnly = attributes.getAttribute("CTL") - 10;
                        double bdmSev = 0.5 * ctlModOnly;
                        DataStatus bdmBonus = new DataStatus();
                        bdmBonus.setName("CtlTDMGBonus");
                        bdmBonus.setAttribute("TDMG");
                        bdmBonus.setDurationType("Permanent");
                        bdmBonus.setSeverity(bdmSev);
                        bdmBonus.setAffinity("None");
                        bdmBonus.setDescription("Control-based Total Damage bonus");
                        attributes.removeStatus("damage", "TDMG", "CtlTDMGBonus");
                        attributes.addStatus("damage", "TDMG", bdmBonus);

                        // Total healing bonus: 0.5 * CTL mod
                        double thealSev = 0.5 * ctlModOnly;
                        DataStatus ctlThealBonus = new DataStatus();
                        ctlThealBonus.setName("CtlTHEALBonus");
                        ctlThealBonus.setAttribute("THEAL");
                        ctlThealBonus.setDurationType("Permanent");
                        ctlThealBonus.setSeverity(thealSev);
                        ctlThealBonus.setAffinity("None");
                        ctlThealBonus.setDescription("Control-based Total Healing bonus");
                        attributes.removeStatus("damage", "THEAL", "CtlTHEALBonus");
                        attributes.addStatus("damage", "THEAL", ctlThealBonus);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to update tech caps / resources: " + e.getMessage());
        }

        this.identity.setOwner(this);
        this.attributes.setOwner(this);
        this.resources.setOwner(this);
        this.specials.setOwner(this);
        this.inventory.setOwner(this);
        this.training.setParent(this);
        this.combat.setOwner(this);
    }

    /**
     * Runs initialization logic for a freshly created character.
     */
    public void initializeNewCharacter() {

    }

    // ---------------------------------------------------------
    // Getters for subsystems
    // ---------------------------------------------------------

    public CharIdentity getIdentity()    { return identity; }
    public CharAttributes getAttributes() { return attributes; }
    public CharResources getResources()  { return resources; }
    public CharSpecials getSpecials()    { return specials; }
    public CharInventory getInventory()  { return inventory; }
    public CharTraining getTraining()    { return training; }
    public CharCombat getCombat()        { return combat; }

    // ---------------------------------------------------------
    // Utility
    // ---------------------------------------------------------

    /** Returns the character's display name. */
    @JsonIgnore // derived value; exclude from serialization
    public String getName() {
        return identity.getName();
    }

    /** Convenience: HP % as 0–1 value. */
    @JsonIgnore // derived value; exclude from serialization
    public double getHealthPercent() {
    	return 0.0;
    }

    /** Convenience: character level. */
    @JsonIgnore // derived value; exclude from serialization
    public int getLevel() {
        return identity.getLevel();
    }

}
