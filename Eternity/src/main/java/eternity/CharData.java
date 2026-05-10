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
    private static final String SPECIALTY_PASSIVE_PREFIX = "Specialty Passive: ";
    private static final boolean ENABLE_SPECIALTY_CHECKS = false;
    private static final String[] STATUS_CATEGORY_ORDER = { "attribute", "defense", "resist", "combat", "secondary", "damage" };

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
    private String cachedClassSpecialtyKey;
    private List<DataSpecialty> cachedClassSpecialtyTemplates;

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
        this.cachedClassSpecialtyKey = "";
        this.cachedClassSpecialtyTemplates = List.of();
    }

    // ---------------------------------------------------------
    // Update Pipeline
    // ---------------------------------------------------------

    /**
     * Updates all subsystems in correct order.
     * Call whenever anything changes (level, gear, buffs, etc.)
     */
    public void updateAll() {
        try {
            if (training != null) {
                training.sortTrainingById();
            }

            refreshIdentityDerivedState();
        } catch (Exception e) {
            System.err.println("Failed to update tech caps / resources: " + e.getMessage());
        }

        refreshTrainingDerivedBonuses();
        applyEquipmentPassiveBonuses();
        refreshSpecialtyPassiveBonuses();

        this.identity.setOwner(this);
        this.attributes.setOwner(this);
        this.resources.setOwner(this);
        this.specials.setOwner(this);
        this.inventory.setOwner(this);
        this.training.setParent(this);
        this.combat.setOwner(this);
        this.combat.rebuildActions(this);
        if (ENABLE_SPECIALTY_CHECKS) {
            logSpecialtyNames();
        }
    }

    public void refreshIdentityDerivedState() {
        DataQuery dq = CharDataManager.getDataQuery();
        int lvl = identity != null ? identity.getLevel() : 1;
        DataLevel dataLevel = dq.getLevel(lvl);
        updateResourceCaps(dataLevel);
        updateIdentityDerivedState(dq, lvl, dataLevel);
        applySizeSaveModifiers();
    }

    public void refreshTrainingDerivedBonuses() {
        applyTrainingPermStatuses(CharDataManager.getDataQuery());
    }

    private void updateResourceCaps(DataLevel dataLevel) {
        if (resources == null || dataLevel == null) return;
        
        DataStatus status = new DataStatus();
        status.setName("base");
        status.setAttribute("BASEHP");
        status.setSeverity(dataLevel.getBaseHP());
        resources.addStatus(status);

        status.setAttribute("BASEAURA");
        status.setSeverity(dataLevel.getBaseAura());
        resources.addStatus(status);
    }

    private void updateIdentityDerivedState(DataQuery dq, int level, DataLevel currentLevelData) {
        if (identity == null || specials == null) return;

        DataRace race = dq.getRaceByName(identity.getRace());
        syncRaceDerivedState(dq, race);
        ensureLevelSpecialties(dq, level);

        DataClass baseClass = dq.getClassByName(identity.getCharClass());
        if (baseClass == null) return;

        DataClass effectiveClass = dq.getClassByName(identity.getCharSubclass());
        if (effectiveClass == null) {
            effectiveClass = baseClass;
        }

        int classRank = training != null ? training.getClassTrainingRank() : level;
        specials.setClassSpecialties(getCachedClassSpecialties(dq, effectiveClass, classRank));

        applyClassResourceScaling(effectiveClass);
        applyClassLevelScalers(baseClass, currentLevelData);
        applyClassAttributeBonuses(baseClass);
    }

    private void syncRaceDerivedState(DataQuery dq, DataRace race) {
        if (race == null) return;
        if (race.getRacialID() >= 0) {
            DataSpecialty racialSpec = dq.getSpecialtyById(race.getRacialID());
            if (racialSpec != null) {
                specials.setRacialSpecialty(new DataSpecialty(racialSpec));
            }
        }

        String size = identity.getSize();
        if (size == null || size.isBlank() || "?".equals(size.trim())) {
            String raceSize = race.getSize();
            identity.setSize((raceSize != null && !raceSize.isBlank()) ? raceSize : "Medium");
        }

        if (attributes == null) return;
        DataStatus[] racialStatuses = race.getBaseDataStatus();
        if (racialStatuses == null) return;
        for (DataStatus racialStatus : racialStatuses) {
            if (racialStatus == null || racialStatus.getAttribute() == null) continue;
            attributes.removeStatus("attribute", racialStatus.getAttribute(), racialStatus.getName());
            attributes.addStatus("attribute", racialStatus.getAttribute(), racialStatus);
        }
    }

    private List<DataSpecialty> buildClassSpecialtyTemplates(DataQuery dq, DataClass dataClass, int classRank) {
        List<DataSpecialty> classSpecs = new ArrayList<>();
        for (int i = 1; i <= classRank; i++) {
            DataLevel levelData = dq.getLevel(i);
            if (levelData == null) continue;

            int generalCount = levelData.getClassGeneral();
            int specCount = levelData.getClassSpec();
            for (int j = 0; j < generalCount; j++) {
                addClassSpecialty(classSpecs, dq, dataClass.getAbilBase() + (10 * i) + j);
            }
            for (int j = 0; j < specCount; j++) {
                addClassSpecialty(classSpecs, dq, dataClass.getAbilBase() + (10 * i) + j + dataClass.getAbilOffset());
            }
        }
        return classSpecs;
    }

    private List<DataSpecialty> getCachedClassSpecialties(DataQuery dq, DataClass dataClass, int classRank) {
        String className = dataClass.getName() == null ? "" : dataClass.getName();
        String cacheKey = className + "|" + classRank;
        if (!cacheKey.equals(cachedClassSpecialtyKey)) {
            cachedClassSpecialtyTemplates = buildClassSpecialtyTemplates(dq, dataClass, classRank);
            cachedClassSpecialtyKey = cacheKey;
        }

        List<DataSpecialty> copies = new ArrayList<>(cachedClassSpecialtyTemplates.size());
        for (DataSpecialty specialty : cachedClassSpecialtyTemplates) {
            copies.add(new DataSpecialty(specialty));
        }
        return copies;
    }

    private void addClassSpecialty(List<DataSpecialty> classSpecs, DataQuery dq, int specialtyId) {
        DataSpecialty baseSpec = dq.getSpecialtyById(specialtyId);
        if (baseSpec != null) {
            classSpecs.add(new DataSpecialty(baseSpec));
        }
    }

    private void applyClassResourceScaling(DataClass effectiveClass) {
        if (effectiveClass == null || resources == null) return;

        StatBlock[] hpBlocks = resources.getMaxHPBlocks();
        if (hpBlocks != null && hpBlocks.length > 0 && hpBlocks[0] != null) {
            hpBlocks[0].removeMulti("ClassHpScaling");
            DataStatus hpScale = new DataStatus();
            hpScale.setName("ClassHpScaling");
            hpScale.setAttribute("HPMULTI");
            hpScale.setDurationType("Permanent");
            hpScale.setSeverity(effectiveClass.getHpScaling());
            hpScale.setAffinity("None");
            hpScale.setDescription("HP multiplier from class scaling");
            hpBlocks[0].addMulti(hpScale);
        }

        StatBlock[] auraBlocks = resources.getMaxAuraBlocks();
        if (auraBlocks != null && auraBlocks.length > 0 && auraBlocks[0] != null) {
            auraBlocks[0].removeMulti("ClassAuraScaling");
            DataStatus auraScale = new DataStatus();
            auraScale.setName("ClassAuraScaling");
            auraScale.setAttribute("AURAMULTI");
            auraScale.setDurationType("Permanent");
            auraScale.setSeverity(effectiveClass.getAuraScaling());
            auraScale.setAffinity("None");
            auraScale.setDescription("Aura multiplier from class scaling");
            auraBlocks[0].addMulti(auraScale);
        }
    }

    private void applyClassLevelScalers(DataClass dataClass, DataLevel currentLevelData) {
        if (attributes == null || dataClass == null || currentLevelData == null) return;
        int[] statScale = dataClass.getStatScaling();
        int[] levelScalers = currentLevelData.getScalers();
        if (statScale == null || statScale.length < 4 || levelScalers == null || levelScalers.length == 0) return;

        applyTieredLevelScaler(attributes, "defense", "FORT", "ClassLevelFort", statScale[0], levelScalers);
        applyTieredLevelScaler(attributes, "defense", "REF", "ClassLevelRef", statScale[1], levelScalers);
        applyTieredLevelScaler(attributes, "defense", "WILL", "ClassLevelWill", statScale[2], levelScalers);
        applyTieredLevelScaler(attributes, "combat", "ATK", "ClassLevelATK", statScale[3], levelScalers);
    }

    private void applyClassAttributeBonuses(DataClass dataClass) {
        if (attributes == null || dataClass == null || dataClass.getPrimaryAtt() == null) return;

        String primaryAttribute = dataClass.getPrimaryAtt().toUpperCase();
        int primaryMod = attributes.getAttribute(primaryAttribute) - 10;
        int primaryValue = attributes.getAttribute(primaryAttribute);
        int focusMod = attributes.getAttribute("FOC") - 10;
        int strengthMod = attributes.getAttribute("STR") - 10;
        int constitutionMod = attributes.getAttribute("CON") - 10;
        int dexterityMod = attributes.getAttribute("DEX") - 10;
        int controlMod = attributes.getAttribute("CTL") - 10;
        int capacityMod = attributes.getAttribute("CAP") - 10;
        int strengthValue = attributes.getAttribute("STR");
        int constitutionValue = attributes.getAttribute("CON");
        int dexterityValue = attributes.getAttribute("DEX");
        int focusValue = attributes.getAttribute("FOC");
        int controlValue = attributes.getAttribute("CTL");
        int capacityValue = attributes.getAttribute("CAP");

        double primaryAtkSeverity = 0.5 * primaryValue;
        double primarySeverity = 0.5 * primaryMod;
        double focusAtkSeverity = 0.5 * focusValue;
        double focusAppSeverity = 0.5 * focusMod;
        double strengthDamageSeverity = 0.5 * strengthMod;
        double fortitudeSeverity = 0.5 * (strengthValue + constitutionValue);
        double reflexSeverity = 0.5 * (dexterityValue + focusValue);
        double willSeverity = 0.5 * (controlValue + capacityValue);
        double dodgeSeverity = 0.5 * dexterityValue;
        double hpMultiSeverity = 0.05 * constitutionMod;
        double auraMultiSeverity = 0.05 * capacityMod;
        double controlSeverity = 0.5 * controlMod;

        addPermanentAttributeStatus("combat", "ATK", "ClassPrimaryATK", primaryAtkSeverity, "Primary attribute bonus to ATK");
        addPermanentAttributeStatus("combat", "ATK", "FocusATKBonus", focusAtkSeverity, "Focus-based ATK bonus");
        addPermanentAttributeStatus("combat", "APP", "FocusAPPBonus", focusAppSeverity, "Focus-based APP bonus");
        addPermanentAttributeStatus("combat", "APP", "ClassPrimaryAPP", primarySeverity, "Primary attribute bonus to APP");
        addPermanentAttributeStatus("damage", "TDMG", "ClassPrimaryTDMG", primarySeverity, "Primary attribute bonus to Total Damage");
        addPermanentAttributeStatus("damage", "TDMG", "StrTDMGBonus", strengthDamageSeverity, "Strength-based Total Damage bonus");
        addPermanentAttributeStatus("damage", "THEAL", "ClassPrimaryTHEAL", primarySeverity, "Primary attribute bonus to Total Healing");
        addPermanentAttributeStatus("defense", "FORT", "Attribute Bonus", fortitudeSeverity, "Strength + Constitution derived Fortitude");
        addPermanentAttributeStatus("defense", "REF", "Attribute Bonus", reflexSeverity, "Dexterity + Focus derived Reflex");
        addPermanentAttributeStatus("defense", "WILL", "Attribute Bonus", willSeverity, "Control + Capacity derived Will");
        addPermanentAttributeStatus("resist", "ALL", "Attribute Bonus", strengthMod, "Strength derived Resist All");
        addPermanentAttributeStatus("defense", "DODGE", "DexDodgeBonus", dodgeSeverity, "Dexterity-based Dodge bonus");
        addPermanentResourceMulti(resources != null ? resources.getMaxHPBlocks() : null, "ConHPMulti", "HPMULTI", hpMultiSeverity, "Constitution-based HP multiplier");
        addPermanentResourceMulti(resources != null ? resources.getMaxAuraBlocks() : null, "CapAuraMulti", "AURAMULTI", auraMultiSeverity, "Capacity-based Aura multiplier");
        addPermanentAttributeStatus("damage", "TDMG", "CtlTDMGBonus", controlSeverity, "Control-based Total Damage bonus");
        addPermanentAttributeStatus("damage", "THEAL", "CtlTHEALBonus", controlSeverity, "Control-based Total Healing bonus");
    }

    /**
     * Ensures level specialties are present for all levels up to current level (max 20).
     * This backfills missed grants when a character levels up.
     */
    private void ensureLevelSpecialties(DataQuery dq, int level) {
        if (dq == null || specials == null) return;
        int cappedLevel = Math.max(0, Math.min(level, 20));
        for (int id = 1; id <= cappedLevel; id++) {
            DataSpecialty base = dq.getSpecialtyById(id);
            if (base == null || base.getName() == null || base.getName().isBlank()) continue;
            if (specials.hasSpecialty(base.getName())) continue;
            specials.addTrainedSpecialty(new DataSpecialty(base));
        }
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

    /** Rebuilds passive specialty statuses from the currently owned specialties. */
    public void refreshSpecialtyPassiveBonuses() {
        applySpecialtyPassiveStatuses(CharDataManager.getDataQuery());
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

    private void applySpecialtyPassiveStatuses(DataQuery dq) {
        clearSpecialtyPassiveBonuses();
        if (dq == null || specials == null) return;

        List<DataSpecialty> all = specials.getAllSpecialties();
        if (all == null || all.isEmpty()) return;

        for (DataSpecialty specialty : all) {
            if (specialty == null || !"Passive".equalsIgnoreCase(specialty.getType())) continue;
            List<DataStatus> perms = collectSpecialtyPermStatuses(dq, specialty);
            if (perms.isEmpty()) continue;
            for (DataStatus permStatus : perms) {
                applySpecialtyPermStatus(specialty, permStatus);
            }
        }
    }

    private void clearSpecialtyPassiveBonuses() {
        if (attributes != null) {
            clearPrefixedStatuses(attributes.getAttributes(), SPECIALTY_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getDefense(), SPECIALTY_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getResist(), SPECIALTY_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getCombat(), SPECIALTY_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getSecondary(), SPECIALTY_PASSIVE_PREFIX);
            clearPrefixedStatuses(attributes.getDamage(), SPECIALTY_PASSIVE_PREFIX);
        }
        if (resources != null) {
            clearPrefixedStatuses(resources.getMaxHPBlocks(), SPECIALTY_PASSIVE_PREFIX);
            clearPrefixedStatuses(resources.getMaxAuraBlocks(), SPECIALTY_PASSIVE_PREFIX);
        }
    }

    private List<DataStatus> collectSpecialtyPermStatuses(DataQuery dq, DataSpecialty specialty) {
        ArrayList<DataStatus> copies = new ArrayList<>();
        if (specialty == null) return copies;

        if (dq != null && specialty.getId() > 0) {
            DataSpecialty base = dq.getSpecialtyById(specialty.getId());
            if (base != null) {
                for (DataStatus permStatus : base.getPermStatus()) {
                    if (permStatus != null) copies.add(new DataStatus(permStatus));
                }
            }
        }

        for (DataStatus permStatus : specialty.getPermStatus()) {
            if (permStatus != null) copies.add(new DataStatus(permStatus));
        }

        if (copies.isEmpty()) {
            copies.addAll(buildLegacyPassiveSpecialtyStatuses(specialty));
        }

        return copies;
    }

    private List<DataStatus> buildLegacyPassiveSpecialtyStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> legacyStatuses = new ArrayList<>();
        if (specialty == null || specialty.getName() == null) return legacyStatuses;

        // TODO: add explicit legacy mappings for passive specialties whose effects are still
        // only documented in description text instead of encoded as permStatus payloads.
        return legacyStatuses;
    }

    private void applySpecialtyPermStatus(DataSpecialty specialty, DataStatus permStatus) {
        if (specialty == null || permStatus == null || permStatus.getAttribute() == null) return;

        String attr = permStatus.getAttribute().toUpperCase();
        String normalizedAttr = normalizeAttrKey(attr);
        String baseName = permStatus.getName() != null && !permStatus.getName().isBlank()
                ? permStatus.getName()
                : specialty.getName();
        String uniqueName = SPECIALTY_PASSIVE_PREFIX + baseName + " (S" + specialty.getId() + ")";

        if ("MAXHP".equals(attr) || "BASEHP".equals(attr)) {
            addSpecialtyResourceStatus(resources != null ? resources.getMaxHPBlocks() : null, uniqueName, "HP", permStatus.getSeverity(), false, permStatus.getDescription());
            return;
        }
        if ("HPMULTI".equals(attr)) {
            addSpecialtyResourceStatus(resources != null ? resources.getMaxHPBlocks() : null, uniqueName, "HPMULTI", permStatus.getSeverity(), true, permStatus.getDescription());
            return;
        }
        if ("MAXAURA".equals(attr) || "BASEAURA".equals(attr)) {
            addSpecialtyResourceStatus(resources != null ? resources.getMaxAuraBlocks() : null, uniqueName, "AURA", permStatus.getSeverity(), false, permStatus.getDescription());
            return;
        }
        if ("AURAMULTI".equals(attr)) {
            addSpecialtyResourceStatus(resources != null ? resources.getMaxAuraBlocks() : null, uniqueName, "AURAMULTI", permStatus.getSeverity(), true, permStatus.getDescription());
            return;
        }

        String category = resolveCategory(normalizedAttr);
        if (category == null) return;

        DataStatus copy = new DataStatus(permStatus);
        copy.setName(uniqueName);
        copy.setAttribute(normalizedAttr);
        copy.setDurationType("Permanent");
        attributes.removeStatus(category, normalizedAttr, uniqueName);
        attributes.addStatus(category, normalizedAttr, copy);
    }

    private void addSpecialtyResourceStatus(StatBlock[] blocks, String uniqueName, String attribute, double severity, boolean multi, String description) {
        if (blocks == null || blocks.length == 0 || blocks[0] == null) return;
        DataStatus copy = new DataStatus();
        copy.setName(uniqueName);
        copy.setAttribute(attribute);
        copy.setDurationType("Permanent");
        copy.setSeverity(severity);
        copy.setAffinity("None");
        copy.setDescription(description == null || description.isBlank() ? "Specialty passive bonus" : description);
        if (multi) {
            blocks[0].removeMulti(uniqueName);
            blocks[0].addMulti(copy);
        } else {
            blocks[0].removeStatus(uniqueName);
            blocks[0].addStatus(copy);
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

    private void addPermanentAttributeStatus(String category, String key, String statusName, double severity, String description) {
        if (attributes == null || category == null || key == null || statusName == null) return;
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription(description);
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

    private void addPermanentResourceMulti(StatBlock[] blocks, String statusName, String key, double severity, String description) {
        if (blocks == null || blocks.length == 0 || blocks[0] == null || statusName == null || key == null) return;
        blocks[0].removeMulti(statusName);
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription(description);
        blocks[0].addMulti(ds);
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
    private void applyTrainingPermStatuses(DataQuery dq) {
        if (training == null || attributes == null) return;
        List<DataTraining> all = training.getAllTraining();
        if (all == null) return;

        for (DataTraining tech : all) {
            if (tech == null || tech.getRank() <= 0 || !tech.getType().equals("Passive")) continue;
            List<DataStatus> perms = collectTrainingPermStatuses(dq, tech);
            if (perms.isEmpty()) continue;
            for (DataStatus ps : perms) {
                applyTrainingPermStatus(tech, ps);
            }
        }
    }

    private List<DataStatus> collectTrainingPermStatuses(DataQuery dq, DataTraining tech) {
        ArrayList<DataStatus> copies = new ArrayList<>();
        if (tech == null) return copies;

        if (tech.getId() > 0) {
            DataTraining base = dq.getTrainingById(tech.getId());
            if (base != null) {
                List<DataStatus> perms = base.getPermStatus();
                if (perms != null) {
                    for (DataStatus ps : perms) {
                        if (ps != null) copies.add(new DataStatus(ps));
                    }
                }
            }
        }

        if (tech.getGrant() != null) {
            for (Integer gid : tech.getGrant()) {
                if (gid == null || gid <= 0) continue;
                DataTechPerm perm = dq.getTechPermById(gid);
                if (perm == null) continue;
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

        return copies;
    }

    private void applyTrainingPermStatus(DataTraining tech, DataStatus permStatus) {
        if (tech == null || permStatus == null || permStatus.getAttribute() == null) return;
        String attr = permStatus.getAttribute().toUpperCase();
        String statusName = permStatus.getName() != null ? permStatus.getName() : ("Training " + tech.getName());
        String uniqueName = statusName + " (T" + tech.getId() + ")";
        double severity = tech.scaleStatusSeverity(permStatus.getSeverity());

        if ("MAXHP".equals(attr)) {
            addTrainingResourceStatus(resources != null ? resources.getMaxHPBlocks() : null, uniqueName, "HP", severity, false);
            return;
        }
        if ("HPMULTI".equals(attr)) {
            addTrainingResourceStatus(resources != null ? resources.getMaxHPBlocks() : null, uniqueName, "HPMULTI", severity, true);
            return;
        }
        if ("MAXAURA".equals(attr)) {
            addTrainingResourceStatus(resources != null ? resources.getMaxAuraBlocks() : null, uniqueName, "AURA", severity, false);
            return;
        }
        if ("AURAMULTI".equals(attr)) {
            addTrainingResourceStatus(resources != null ? resources.getMaxAuraBlocks() : null, uniqueName, "AURAMULTI", severity, true);
            return;
        }

        String category = resolveCategory(attr);
        if (category == null) return;
        DataStatus copy = new DataStatus(permStatus);
        copy.setName(uniqueName);
        copy.setAttribute(attr);
        copy.setDurationType("Permanent");
        copy.setSeverity(severity);
        attributes.removeStatus(category, attr, uniqueName);
        attributes.addStatus(category, attr, copy);
    }

    private void addTrainingResourceStatus(StatBlock[] blocks, String uniqueName, String attribute, double severity, boolean multi) {
        if (blocks == null || blocks.length == 0 || blocks[0] == null) return;
        DataStatus copy = new DataStatus();
        copy.setName(uniqueName);
        copy.setAttribute(attribute);
        copy.setDurationType("Permanent");
        copy.setSeverity(severity);
        copy.setAffinity("None");
        if (multi) {
            blocks[0].removeMulti(uniqueName);
            blocks[0].addMulti(copy);
        } else {
            blocks[0].removeStatus(uniqueName);
            blocks[0].addStatus(copy);
        }
    }

    /** Heuristically resolve which category an attribute key belongs to. */
    private String resolveCategory(String key) {
        if (key == null) return null;
        // Normalize aliases
        String norm = normalizeAttrKey(key);
        for (String c : STATUS_CATEGORY_ORDER) {
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

    /** Applies a 1-based class scaling tier to the given block/key. */
    private void applyTieredLevelScaler(CharAttributes attrs, String blockType, String key, String statusName, int scalerTier, int[] levelScalers) {
        if (attrs == null || levelScalers == null || scalerTier <= 0 || scalerTier > levelScalers.length) return;
        applyLevelScaler(attrs, blockType, key, statusName, scalerTier - 1, levelScalers);
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


    /////////////////////////////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////////////////////////
    /// //////////////////////////////////////////////////////////////////////
    /// 
    
    public double calcResourceValue(String key) {
        if (resources == null || key == null) return -1.0;
        return switch (key.toUpperCase()) {
            case "MAXHP" -> resources.calcMaxHP();
            case "MAXAURA" -> resources.calcMaxAura();
            case "MAXR1" -> resources.calcMaxResource1();
            case "MAXR2" -> resources.calcMaxResource2();
            case "MAXR3" -> resources.calcMaxResource3();
            case "MAXREACT" -> resources.calcMaxReactions();
            case "CURHP" -> resources.calcCurrentHP();
            case "CURAURA" -> resources.calcCurrentAura();
            case "CURR1" -> resources.calcCurrentResource1();
            case "CURR2" -> resources.calcCurrentResource2();
            case "CURR3" -> resources.calcCurrentResource3();
            case "CURREACT" -> resources.calcCurrentReactions();
            case "OCCAURA" -> resources.calcOccupiedAura();
            case "MAINOCC" -> resources.getMainOccupiedAura();
            case "GRANTOCC" -> resources.getGrantOccupiedAura();
            default -> -1;
        };
    }

        public void setResourceValue(String key, double value) {
        if (resources == null || key == null) return;
        switch (key.toUpperCase()) {
            case "LOSTHP" -> resources.setLostHP(value);
            case "SPENTAURA" -> resources.setSpentAura(value);
            case "SPENTR1" -> resources.setSpentR1(value);
            case "SPENTR2" -> resources.setSpentR2(value);
            case "SPENTR3" -> resources.setSpentR3(value);
            case "SPENTREACT" -> resources.setSpentReactions(value);
            case "MAINOCC" -> resources.setMainOccupiedAura(value);
            case "GRANTOCC" -> resources.setGrantOccupiedAura(value);
            case "SHIELD" -> resources.setShield(value);
            case "STAGGER" -> resources.setStagger(value);
        };
    }
}
