package eternity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String EQUIP_PASSIVE_PREFIX = "Equip Passive: ";

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
    private List<List<DataList>> Lists;
    private Map<String, String> reminderSelections;
    private String panelReminder;

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
        this.Lists = new ArrayList<>();
        this.reminderSelections = new LinkedHashMap<>();
        this.panelReminder = "";
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
            if (training != null) {
                training.sortTrainingById();
            }
            DataQuery dq = new DataQuery();
            int lvl = identity != null ? identity.getLevel() : 1;
            DataLevel dataLevel = dq.getLevel(lvl);

            // Push base resource caps from level data into the live stat blocks
            if (resources != null && dataLevel != null) {
                resources.setBaseMaxHP(dataLevel.getBaseHP());
                resources.setBaseMaxAura(dataLevel.getBaseAura());

                // Clamp current spend/loss deltas so they never exceed the new maxima.
                // Occupied aura is intentionally not clamped here because over-occupation
                // (negative available aura) is a valid and user-visible state.
                resources.setLostHP(Math.min(resources.getLostHP(), resources.getMaxHP()));
                resources.setSpentAura(Math.min(resources.getSpentAura(), resources.getMaxAura()));
            }

            // Sync racial specialty from race data
            if (identity != null && specials != null) {
                DataRace race = dq.getRaceByName(identity.getRace());
                if (race != null && race.getRacialID() >= 0) {
                    DataSpecialty racialSpec = dq.getSpecialtyById(race.getRacialID());
                    if (racialSpec != null) {
                        specials.setRacialSpecialty(new DataSpecialty(racialSpec));
                    }
                    // If size is unset or placeholder, adopt race size
                    String sz = identity.getSize();
                    if (sz == null || sz.isBlank() || "?".equals(sz.trim())) {
                        String raceSize = race.getSize();
                        if (raceSize != null && !raceSize.isBlank()) {
                            identity.setSize(raceSize);
                        } else {
                            identity.setSize("Medium"); // fallback default
                        }
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

                    // Class HP scaling multiplier
                    StatBlock[] hpBlocks = resources != null ? resources.getMaxHPBlocks() : null;
                    DataClass hpClass = dq.getClassByName(identity.getCharSubclass());
                    if (hpClass == null) hpClass = dataClass;
                    if (hpBlocks != null && hpBlocks.length > 0 && hpBlocks[0] != null && hpClass != null) {
                        hpBlocks[0].removeMulti("ClassHpScaling");
                        DataStatus hpScale = new DataStatus();
                        hpScale.setName("ClassHpScaling");
                        hpScale.setAttribute("HPMULTI");
                        hpScale.setDurationType("Permanent");
                        hpScale.setSeverity(hpClass.getHpScaling());
                        hpScale.setAffinity("None");
                        hpScale.setDescription("HP multiplier from class scaling");
                        hpBlocks[0].addMulti(hpScale);
                    }

                    // Class Aura scaling multiplier
                    StatBlock[] auraBlocks = resources != null ? resources.getMaxAuraBlocks() : null;
                    DataClass auraClass = dq.getClassByName(identity.getCharSubclass());
                    if (auraClass == null) auraClass = dataClass;
                    if (auraBlocks != null && auraBlocks.length > 0 && auraBlocks[0] != null && auraClass != null) {
                        auraBlocks[0].removeMulti("ClassAuraScaling");
                        DataStatus auraScale = new DataStatus();
                        auraScale.setName("ClassAuraScaling");
                        auraScale.setAttribute("AURAMULTI");
                        auraScale.setDurationType("Permanent");
                        auraScale.setSeverity(auraClass.getAuraScaling());
                        auraScale.setAffinity("None");
                        auraScale.setDescription("Aura multiplier from class scaling");
                        auraBlocks[0].addMulti(auraScale);
                    }

                    // Class level scalers -> FORT / REF / WILL / ATK
                    int[] statScale = dataClass.getStatScaling();
                    int[] levelScalers = dataLevel != null ? dataLevel.getScalers() : null;
                    if (attributes != null && statScale != null && statScale.length >= 4 && levelScalers != null && levelScalers.length > 0) {
                        applyLevelScaler(attributes, "defense", "FORT", "ClassLevelFort", statScale[0], levelScalers);
                        applyLevelScaler(attributes, "defense", "REF",  "ClassLevelRef",  statScale[1], levelScalers);
                        applyLevelScaler(attributes, "defense", "WILL", "ClassLevelWill", statScale[2], levelScalers);
                        applyLevelScaler(attributes, "combat",  "ATK",  "ClassLevelATK",  statScale[3], levelScalers);
                    }

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
                        fortBonus.setName("Attribute Bonus");
                        fortBonus.setAttribute("FORT");
                        fortBonus.setDurationType("Permanent");
                        fortBonus.setSeverity(fortSev);
                        fortBonus.setAffinity("None");
                        fortBonus.setDescription("Strength + Constitution derived Fortitude");
                        attributes.removeStatus("defense", "FORT", "Attribute Bonus");
                        attributes.addStatus("defense", "FORT", fortBonus);

                        // Reflex bonus: 0.75 * (DEX mod + FOC mod)
                        int dexMod = attributes.getAttribute("DEX") - 10;
                        int focMod = attributes.getAttribute("FOC") - 10;
                        double refSev = 0.75 * (dexMod + focMod);
                        DataStatus refBonus = new DataStatus();
                        refBonus.setName("Attribute Bonus");
                        refBonus.setAttribute("REF");
                        refBonus.setDurationType("Permanent");
                        refBonus.setSeverity(refSev);
                        refBonus.setAffinity("None");
                        refBonus.setDescription("Dexterity + Focus derived Reflex");
                        attributes.removeStatus("defense", "REF", "Attribute Bonus");
                        attributes.addStatus("defense", "REF", refBonus);

                        // Will bonus: 0.75 * (CTL mod + CAP mod)
                        int ctlMod = attributes.getAttribute("CTL") - 10;
                        int capMod = attributes.getAttribute("CAP") - 10;
                        double willSev = 0.75 * (ctlMod + capMod);
                        DataStatus willBonus = new DataStatus();
                        willBonus.setName("Attribute Bonus");
                        willBonus.setAttribute("WILL");
                        willBonus.setDurationType("Permanent");
                        willBonus.setSeverity(willSev);
                        willBonus.setAffinity("None");
                        willBonus.setDescription("Control + Capacity derived Will");
                        attributes.removeStatus("defense", "WILL", "Attribute Bonus");
                        attributes.addStatus("defense", "WILL", willBonus);

                        // Resist All bonus: STR mod
                        strModOnly = attributes.getAttribute("STR") - 10;
                        DataStatus resBonus = new DataStatus();
                        resBonus.setName("Attribute Bonus");
                        resBonus.setAttribute("ALL");
                        resBonus.setDurationType("Permanent");
                        resBonus.setSeverity(strModOnly);
                        resBonus.setAffinity("None");
                        resBonus.setDescription("Strength derived Resist All");
                        attributes.removeStatus("resist", "ALL", "Attribute Bonus");
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
                        hpBlocks = resources.getMaxHPBlocks();
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
                        auraBlocks = resources.getMaxAuraBlocks();
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

            // Apply size-derived save modifiers (kept as dedicated statuses so updates replace cleanly)
            applySizeSaveModifiers();
        } catch (Exception e) {
            System.err.println("Failed to update tech caps / resources: " + e.getMessage());
        }

        // Apply Attribute Training bonuses as passive statuses
        //applyAttributeTrainingBonuses();
        syncTrainingPermStatusesFromStore();
        applyTrainingPermStatuses();
        applyEquipmentPassiveBonuses();
        clearTrainingPermStatusCache();

        this.identity.setOwner(this);
        this.attributes.setOwner(this);
        this.resources.setOwner(this);
        this.specials.setOwner(this);
        this.inventory.setOwner(this);
        this.training.setParent(this);
        this.combat.setOwner(this);
        logSpecialtyNames();
    }

    /**
     * Runs initialization logic for a freshly created character.
     */
    public void initializeNewCharacter() {
        if (identity == null) return;
        if (!"Alteri".equalsIgnoreCase(identity.getRace())) return;
        if (Lists == null) {
            Lists = new ArrayList<>();
        }
        if (!Lists.isEmpty()) return;

        String shapeshiftName = "";
        List<String> racePicks = identity.getCharRacePick();
        if (racePicks != null && !racePicks.isEmpty() && racePicks.get(0) != null) {
            shapeshiftName = racePicks.get(0);
        }

        ArrayList<DataList> alteriList = new ArrayList<>();
        alteriList.add(new DataList("Shapeshift", shapeshiftName, ""));
        Lists.add(alteriList);
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
    public List<List<DataList>> getLists() { return Lists; }

    /** Rebuilds equipment passive statuses from currently equipped inventory items. */
    public void refreshEquipmentPassiveBonuses() {
        applyEquipmentPassiveBonuses();
    }

    public void setLists(List<List<DataList>> lists) { this.Lists = (lists == null) ? new ArrayList<>() : lists; }
    public Map<String, String> getReminderSelections() { return reminderSelections; }
    public void setReminderSelections(Map<String, String> reminderSelections) {
        this.reminderSelections = (reminderSelections == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(reminderSelections);
    }
    public String getReminderSelection(String key) {
        if (key == null || reminderSelections == null) return null;
        return reminderSelections.get(key);
    }
    public void setReminderSelection(String key, String value) {
        if (key == null || key.isBlank()) return;
        if (reminderSelections == null) {
            reminderSelections = new LinkedHashMap<>();
        }
        reminderSelections.put(key, value == null ? "" : value);
    }
    public String getPanelReminder()     { return panelReminder; }
    public void setPanelReminder(String panelReminder) { this.panelReminder = panelReminder == null ? "" : panelReminder; }
    public void appendPanelReminderLine(String reminderLine) {
        if (reminderLine == null || reminderLine.isBlank()) return;
        if (panelReminder == null || panelReminder.isBlank()) {
            panelReminder = reminderLine;
        } else {
            panelReminder += "\n" + reminderLine;
        }
    }

    // ---------------------------------------------------------
    // Utility
    // ---------------------------------------------------------

    /** Applies currently equipped item bonuses as passive statuses on character stats/resources. */
    private void applyEquipmentPassiveBonuses() {
        if (inventory == null) return;
        clearEquipmentPassiveBonuses();

        List<DataItemEquipment> equippedItems = inventory.getEquipment();
        if (equippedItems == null || equippedItems.isEmpty()) return;

        int idx = 0;
        for (DataItemEquipment item : equippedItems) {
            if (item == null || !item.isEquipped()) continue;
            String bonusAtt = item.getBonusAtt();
            if (bonusAtt == null || bonusAtt.isBlank() || "NONE".equalsIgnoreCase(bonusAtt.trim())) continue;

            double severity = item.getBonusAmount();
            String itemName = (item.getDname() == null || item.getDname().isBlank()) ? "Item" : item.getDname().trim();
            String statusName = EQUIP_PASSIVE_PREFIX + itemName + " #" + idx;
            idx++;
            applyEquipmentBonusStatus(bonusAtt.trim().toUpperCase(), severity, statusName);
        }
    }

    /** Clears previously applied equipment passive statuses so updateAll can re-apply from current equip state. */
    private void clearEquipmentPassiveBonuses() {
        if (attributes != null) {
            clearPrefixedStatuses(attributes.getAttributes(), EQUIP_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getDefense(), EQUIP_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getResist(), EQUIP_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getCombat(), EQUIP_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getSecondary(), EQUIP_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getDamage(), EQUIP_PASSIVE_PREFIX);
        }
        if (resources != null) {
            clearPrefixedStatuses(resources.getMaxHPBlocks(), EQUIP_PASSIVE_PREFIX);
            clearPrefixedStatuses(resources.getMaxAuraBlocks(), EQUIP_PASSIVE_PREFIX);
        }
    }

    private void clearPrefixedStatuses(StatBlock[] blocks, String prefix) {
        if (blocks == null || prefix == null) return;
        for (StatBlock block : blocks) {
            if (block == null) continue;
            block.getStatus().removeIf(s -> s != null && s.getName() != null && s.getName().startsWith(prefix));
            block.getMulti().removeIf(s -> s != null && s.getName() != null && s.getName().startsWith(prefix));
        }
    }

    /** Maps equipment bonus keys to live stat blocks and applies passive severity. */
    private void applyEquipmentBonusStatus(String rawKey, double severity, String statusName) {
        if (rawKey == null || rawKey.isBlank()) return;

        // Known combined/alias keys from item data
        if ("TDMG-THEAL".equals(rawKey)) {
            addEquipmentAttributeStatus("damage", "TDMG", severity, statusName + " (TDMG)");
            addEquipmentAttributeStatus("damage", "THEAL", severity, statusName + " (THEAL)");
            return;
        }
        if ("SAVES".equals(rawKey)) {
            addEquipmentAttributeStatus("defense", "FORT", severity, statusName + " (FORT)");
            addEquipmentAttributeStatus("defense", "REF", severity, statusName + " (REF)");
            addEquipmentAttributeStatus("defense", "WILL", severity, statusName + " (WILL)");
            return;
        }
        if ("MAXHP".equals(rawKey)) {
            addEquipmentResourceStatus(resources != null ? resources.getMaxHPBlocks() : null, "HP", severity, statusName);
            return;
        }
        if ("MAXAURA".equals(rawKey)) {
            addEquipmentResourceStatus(resources != null ? resources.getMaxAuraBlocks() : null, "AURA", severity, statusName);
            return;
        }

        String mapped = switch (rawKey) {
            case "RESISTALL", "CRES" -> "ALL";
            case "DC" -> "APP";
            case "BMDG" -> "BMDMG";
            default -> rawKey;
        };

        String norm = normalizeAttrKey(mapped);
        String category = resolveCategory(norm);
        if (category == null) return;
        addEquipmentAttributeStatus(category, norm, severity, statusName);
    }

    private void addEquipmentAttributeStatus(String category, String key, double severity, String statusName) {
        if (attributes == null || category == null || key == null || statusName == null) return;
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription("Equipment passive bonus");
        attributes.removeStatus(category, key, statusName);
        attributes.addStatus(category, key, ds);
    }

    private void addEquipmentResourceStatus(StatBlock[] blocks, String key, double severity, String statusName) {
        if (blocks == null || blocks.length == 0 || blocks[0] == null || statusName == null) return;
        blocks[0].removeStatus(statusName);
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription("Equipment passive bonus");
        blocks[0].addStatus(ds);
    }

    /**
     * Pushes Attribute training ranks into attribute StatBlocks as passive statuses.
     */
    /*private void applyAttributeTrainingBonuses() {
        if (training == null || attributes == null) return;
        List<DataTraining> list = training.getTrainingList("Attribute");
        if (list == null) return;

        for (DataTraining tech : list) {
            if (tech == null) continue;
            String attKey = deriveAttributeKey(tech);
            if (attKey == null || attKey.isBlank()) continue;
            String statusName = "Attribute Training: " + tech.getName();
            attributes.removeStatus("attribute", attKey, statusName);
            if (tech.getRank() > 0) {
                DataStatus ds = new DataStatus();
                ds.setName(statusName);
                ds.setAttribute(attKey);
                ds.setDurationType("Permanent");
                ds.setSeverity(tech.getRank());
                ds.setAffinity("None");
                ds.setDescription("Attribute training rank bonus");
                attributes.addStatus("attribute", attKey, ds);
            }
        }
    }*/

    private String deriveAttributeKey(DataTraining tech) {
        if (tech == null) return null;
        // Prefer explicit affinity if it matches an attribute key
        String aff = tech.getAffinity();
        if (aff != null) {
            String upper = aff.toUpperCase();
        for (String k : CharAttributes.getAttributeKeys()) {
            if (k.equalsIgnoreCase(upper)) return k;
        }
        }
        // Fallback: parse attribute name from training title prefix (e.g., "Knowledge Training")
        String name = tech.getName();
        if (name != null && name.toLowerCase().endsWith("training")) {
            String[] parts = name.split("\\s+");
            if (parts.length >= 1) {
                String prefix = parts[0].toLowerCase();
                return switch (prefix) {
                    case "strength" -> "STR";
                    case "dexterity" -> "DEX";
                    case "constitution" -> "CON";
                    case "focus" -> "FOC";
                    case "control" -> "CTL";
                    case "capacity" -> "CAP";
                    case "knowledge" -> "KNOW";
                    case "mechanical", "mechanics" -> "MECH";
                    case "perception" -> "PERC";
                    case "intuition" -> "INT";
                    case "charisma" -> "CHA";
                    case "subtlety" -> "SUB";
                    default -> null;
                };
            }
        }
        return null;
    }

    /** Applies Fundamental Benefaction bonus as a passive Grant status equal to its rank. */
    /*private void applyFundamentalBenefactionBonus() {
        if (training == null || attributes == null) return;
        List<DataTraining> list = training.getTrainingList("Fundamental");
        if (list == null) return;
        String statusName = "Fundamental Benefaction";
        String key = "GRANT"; // use dedicated Grant secondary
        attributes.removeStatus("secondary", key, statusName);
        for (DataTraining tech : list) {
            if (tech == null || tech.getRank() <= 0) continue;
            String name = tech.getName() != null ? tech.getName().toLowerCase() : "";
            if (!name.contains("benefaction")) continue;
            double rank = tech.getRank();
            DataStatus ds = new DataStatus();
            ds.setName(statusName);
            ds.setAttribute(key);
            ds.setDurationType("Permanent");
            ds.setSeverity(rank);
            ds.setAffinity("None");
            ds.setDescription("Fundamental Benefaction bonus");
            attributes.addStatus("secondary", key, ds);
            // Also apply to Base Heal
            String healKey = "BHEAL";
            attributes.removeStatus("damage", healKey, statusName);
            DataStatus heal = new DataStatus();
            heal.setName(statusName);
            heal.setAttribute(healKey);
            heal.setDurationType("Permanent");
            heal.setSeverity(rank);
            heal.setAffinity("None");
            heal.setDescription("Fundamental Benefaction healing bonus");
            attributes.addStatus("damage", healKey, heal);
            break; // only one benefaction applied
        }
    }

    /**
     * Applies permStatus grants from training data, scaled by rank, into live attributes.
     */
    private void applyTrainingPermStatuses() {
        if (training == null || attributes == null) return;
        List<DataTraining> all = training.getAllTraining();
        if (all == null) return;

        for (DataTraining tech : all) {
            if (tech == null || tech.getRank() <= 0 || !tech.getType().equals("Passive")) continue;
            List<DataStatus> perms = tech.getPermStatus();
            if (perms == null || perms.isEmpty()) continue;
            for (DataStatus ps : perms) {
                if (ps == null || ps.getAttribute() == null) continue;
                String attr = ps.getAttribute().toUpperCase();
                String category = resolveCategory(attr);
                String statusName = ps.getName() != null ? ps.getName() : ("Training " + tech.getName());
                String uniqueName = statusName + " (T" + tech.getId() + ")";

                // Special-case routing for resource-based keys
                if ("MAXHP".equals(attr)) {
                    StatBlock[] blocks = resources != null ? resources.getMaxHPBlocks() : null;
                    if (blocks != null && blocks.length > 0 && blocks[0] != null) {
                        blocks[0].removeStatus(uniqueName);
                        DataStatus copy = new DataStatus(ps);
                        copy.setName(uniqueName);
                        copy.setAttribute("HP");
                        copy.setDurationType("Permanent");
                        copy.setSeverity(tech.scaleStatusSeverity(ps.getSeverity()));
                        blocks[0].addStatus(copy);
                    }
                    continue;
                }
                if ("HPMULTI".equals(attr)) {
                    StatBlock[] blocks = resources != null ? resources.getMaxHPBlocks() : null;
                    if (blocks != null && blocks.length > 0 && blocks[0] != null) {
                        blocks[0].removeMulti(uniqueName);
                        DataStatus copy = new DataStatus(ps);
                        copy.setName(uniqueName);
                        copy.setAttribute("HPMULTI");
                        copy.setDurationType("Permanent");
                        copy.setSeverity(tech.scaleStatusSeverity(ps.getSeverity()));
                        blocks[0].addMulti(copy);
                    }
                    continue;
                }
                if ("MAXAURA".equals(attr)) {
                    StatBlock[] blocks = resources != null ? resources.getMaxAuraBlocks() : null;
                    if (blocks != null && blocks.length > 0 && blocks[0] != null) {
                        blocks[0].removeStatus(uniqueName);
                        DataStatus copy = new DataStatus(ps);
                        copy.setName(uniqueName);
                        copy.setAttribute("AURA");
                        copy.setDurationType("Permanent");
                        copy.setSeverity(tech.scaleStatusSeverity(ps.getSeverity()));
                        blocks[0].addStatus(copy);
                    }
                    continue;
                }
                if ("AURAMULTI".equals(attr)) {
                    StatBlock[] blocks = resources != null ? resources.getMaxAuraBlocks() : null;
                    if (blocks != null && blocks.length > 0 && blocks[0] != null) {
                        blocks[0].removeMulti(uniqueName);
                        DataStatus copy = new DataStatus(ps);
                        copy.setName(uniqueName);
                        copy.setAttribute("AURAMULTI");
                        copy.setDurationType("Permanent");
                        copy.setSeverity(tech.scaleStatusSeverity(ps.getSeverity()));
                        blocks[0].addMulti(copy);
                    }
                    continue;
                }

                if (category == null) continue;
                attributes.removeStatus(category, attr, uniqueName);
                DataStatus copy = new DataStatus(ps);
                copy.setName(uniqueName);
                copy.setAttribute(attr);
                copy.setDurationType("Permanent");
                copy.setSeverity(tech.scaleStatusSeverity(ps.getSeverity()));
                attributes.addStatus(category, attr, copy);
            }
        }
    }

    /**
     * Refreshes each trained technique's permStatus list from the master data store,
     * ensuring passive effects are up to date before being applied to attributes.
     */
    private void syncTrainingPermStatusesFromStore() {
        if (training == null) return;
        List<DataTraining> all = training.getAllTraining();
        if (all == null || all.isEmpty()) return;

        DataQuery dq = new DataQuery();
        for (DataTraining tech : all) {
            if (tech == null || tech.getId() <= 0) continue;
            DataTraining base = dq.getTrainingById(tech.getId());
            if (base == null) continue;
            List<DataStatus> perms = base.getPermStatus();
            ArrayList<DataStatus> copies = new ArrayList<>();

            // 1) Copy canonical perm statuses from data store (if any)
            if (perms != null && !perms.isEmpty()) {
                for (DataStatus ps : perms) {
                    if (ps != null) copies.add(new DataStatus(ps));
                }
            }

            // 2) Build perm statuses directly from grant ids so late-added techpermdata entries are picked up
            if (tech.getGrant() != null) {
                for (Integer gid : tech.getGrant()) {
                    if (gid == null || gid <= 0) continue;
                    DataTechPerm perm = dq.getTechPermById(gid);
                    if (perm == null) {
                        continue;
                    }
                    DataStatus ds = new DataStatus();
                    ds.setName("TechPerm " + gid);
                    ds.setAttribute(perm.getAttribute());
                    ds.setDurationType("Permanent");
                    ds.setSeverity(perm.getRatio());
                    ds.setAffinity("None");
                    ds.setDescription("Tech permission grant");
                    copies.add(ds);
                }
            }

            if (!copies.isEmpty()) {
                tech.setPermStatus(copies);
            }
        }
    }

    /**
     * Clears cached permStatus lists so they are not persisted in character save files.
     */
    private void clearTrainingPermStatusCache() {
        if (training == null) return;
        List<DataTraining> all = training.getAllTraining();
        if (all == null) return;
        for (DataTraining tech : all) {
            if (tech != null) {
                tech.setPermStatus(new ArrayList<>());
            }
        }
    }

    /** Heuristically resolve which category an attribute key belongs to. */
    private String resolveCategory(String key) {
        if (key == null) return null;
        // Normalize aliases
        String norm = normalizeAttrKey(key);
        String[] cats = { "attribute", "defense", "resist", "combat", "secondary", "damage" };
        for (String c : cats) {
            if (attributes.getBlock(c, norm) != null) return c;
        }
        return null;
    }

    /** Maps known aliases (e.g., RESPHY -> PHY) to existing stat keys. */
    private String normalizeAttrKey(String key) {
        if (key == null) return null;
        String upper = key.toUpperCase();
        if ("RESPHY".equals(upper)) return "PHY";
        // Strip common "RES" or "RESIST" prefixes so resist tech attributes map correctly
        if (upper.startsWith("RESIST")) {
            return upper.substring("RESIST".length());
        }
        if (upper.startsWith("RES") && upper.length() > 3) {
            return upper.substring(3);
        }
        return upper;
    }

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

    /** Applies a level-scaler-derived permanent status to the given block/key. */
    private void applyLevelScaler(CharAttributes attrs, String blockType, String key, String statusName, int scalerIndex, int[] levelScalers) {
        if (attrs == null || levelScalers == null || scalerIndex < 0 || scalerIndex >= levelScalers.length) return;
        double sev = levelScalers[scalerIndex];
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(sev);
        ds.setAffinity("None");
        ds.setDescription("Class level scaling");
        attrs.removeStatus(blockType, key, statusName);
        attrs.addStatus(blockType, key, ds);
    }

    /** Applies permanent size-based save modifiers to Reflex and Fortitude. */
    private void applySizeSaveModifiers() {
        if (attributes == null || identity == null) return;

        String size = identity.getSize();
        double refSev = 0;
        double fortSev = 0;
        double dodgeSev = 0;

        if (size != null) {
            switch (size.trim().toUpperCase()) {
                case "TINY":
                    refSev = 4;
                    fortSev = -4;
                    dodgeSev = 2;
                    break;
                case "SMALL":
                    refSev = 2;
                    fortSev = -2;
                    dodgeSev = 1;
                    break;
                case "LARGE":
                    refSev = -2;
                    fortSev = 2;
                    dodgeSev = -1;
                    break;
                case "MEDIUM":
                default:
                    refSev = 0;
                    fortSev = 0;
                    dodgeSev = 0;
                    break;
            }
        }

        if (combat != null) {
            String normalized = size == null ? "" : size.trim().toUpperCase();
            combat.setInvade("TINY".equals(normalized));
            combat.setReach("LARGE".equals(normalized) ? 5 : 0);
        }

        DataStatus refSizeBonus = new DataStatus();
        refSizeBonus.setName("Size Modifier");
        refSizeBonus.setAttribute("REF");
        refSizeBonus.setDurationType("Permanent");
        refSizeBonus.setSeverity(refSev);
        refSizeBonus.setAffinity("None");
        refSizeBonus.setDescription("Size-based Reflex modifier");
        attributes.removeStatus("defense", "REF", "Size Modifier");
        attributes.addStatus("defense", "REF", refSizeBonus);

        DataStatus fortSizeBonus = new DataStatus();
        fortSizeBonus.setName("Size Modifier");
        fortSizeBonus.setAttribute("FORT");
        fortSizeBonus.setDurationType("Permanent");
        fortSizeBonus.setSeverity(fortSev);
        fortSizeBonus.setAffinity("None");
        fortSizeBonus.setDescription("Size-based Fortitude modifier");
        attributes.removeStatus("defense", "FORT", "Size Modifier");
        attributes.addStatus("defense", "FORT", fortSizeBonus);

        DataStatus dodgeSizeBonus = new DataStatus();
        dodgeSizeBonus.setName("Size Modifier");
        dodgeSizeBonus.setAttribute("DODGE");
        dodgeSizeBonus.setDurationType("Permanent");
        dodgeSizeBonus.setSeverity(dodgeSev);
        dodgeSizeBonus.setAffinity("None");
        dodgeSizeBonus.setDescription("Size-based Dodge modifier");
        attributes.removeStatus("defense", "DODGE", "Size Modifier");
        attributes.addStatus("defense", "DODGE", dodgeSizeBonus);
    }

    /** Writes each current specialty name to the terminal for update-time inspection. */
    private void logSpecialtyNames() {
        if (specials == null) return;
        List<DataSpecialty> all = specials.getAllSpecialties();
        if (all == null) return;
        setPanelReminder("");
        SpecCheck specCheck = new SpecCheck();
        specCheck.setCharacterSpecialtiesReference(all);
        specCheck.setInventoryReference(inventory);
        specCheck.setPanelReminderSetter(this::appendPanelReminderLine);
        specCheck.runChecks();
    }

}
