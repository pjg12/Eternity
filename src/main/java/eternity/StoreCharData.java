package eternity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * StoreCharData is the root of the character sheet object graph.
 */
public class StoreCharData {
    private static final String EQUIP_PASSIVE_PREFIX = "Equip Passive: ";
    private static final String SPECIALTY_PASSIVE_PREFIX = "Specialty Passive: ";
    private static final String TRAINING_STATUS_PREFIX = "Training Passive: ";
    private static final String SIZE_STATUS_PREFIX = "Size Modifier";
    private static final String DOMAIN_EMANATION_PREFIX = "Domain Emanation: ";
    private static final String[] STATUS_CATEGORY_ORDER = { "attribute", "defense", "resist", "combat", "secondary", "damage" };
    private static final String[] REMINDER_ATTRIBUTE_KEYS = { "REMINDER", "PANELREMINDER", "NOTE", "NOTES" };
    private static final String DIVINE_VOW_SPECIALTY = "Divine Vow";
    private static final String DIVINE_GRACE_SPECIALTY = "Divine Grace";
    private static final String DIVINE_ENLIGHTENMENT_SPECIALTY = "Divine Enlightenment";
    private static final String DIVINE_DEDICATION_SPECIALTY = "Divine Dedication";
    private static final String DIVINE_DEDICATION_FAITHFUL = "Faithful";
    private static final String DIVINE_DEDICATION_DAWNING = "Dawning";
    private static final String DIVINE_DEDICATION_DOMINANT = "Dominant";
    private static final String BALANCE_IN_ALL_THINGS_SPECIALTY = "Balance in All Things (Kenti)";
    private static final String MARTIAL_FOCUS_SPECIALTY = "Martial Focus";
    private static final String COMBAT_DISCIPLINE_SPECIALTY = "Combat Discipline";
    private static final String COMBAT_DISCIPLINE_I_DISPLAY = "Combat Discipline I";
    private static final String COMBAT_DISCIPLINE_II_SPECIALTY = "Combat Discipline II";
    private static final String COMBAT_SPECIALIST_SPECIALTY = "Combat Specialist";
    private static final String STANCE_SPECIALTY = "Stance";
    private static final String SNIPERS_DOMAIN_SPECIALTY = "Sniper's Domain";
    private static final String JAGGED_EDGES_SPECIALTY = "Jagged Edges";
    private static final String INDOMITABLE_PREDISPOSITION_SPECIALTY = "Indomitable Predisposition";
    private static final String ESCAPE_ARTISAN_SPECIALTY = "Escape Artisan";
    private static final String EVASION_SPECIALTY = "Evasion";
    private static final String ARMOR_DEPENDENCY_SPECIALTY = "Armor Dependency";
    private static final String AURA_PROFICIENCY_SPECIALTY = "Aura Proficiency";
    private static final String ASTRAL_PROFICIENCY_SPECIALTY = "Astral Proficiency";
    private static final String ASTRAL_PROFICIENCY_II_SPECIALTY = "Astral Proficiency II";
    private static final String EQUIPMENT_EVOCATION_SPECIALTY = "Equipment Evocation";
    private static final String BOND_SPECIALTY = "Bond";
    private static final String MODULAR_FORM_SPECIALTY = "Modular Form";
    private static final String MARTIAL_FOCUS_MOBILITY = "Mobility";
    private static final String MARTIAL_FOCUS_AVOIDANCE = "Avoidance";
    private static final String MARTIAL_FOCUS_HARM = "Harm";
    private static final String MARTIAL_FOCUS_MARTIAL_LEGACY = "Martial";
    private static final String MARTIAL_FOCUS_MOBILITY_REMINDER = "Any time you would move exactly 5ft, you may move an additional 5ft.";
    private static final String COMBAT_DISCIPLINE_MOBILITY_II_REMINDER = "You may split your movement into 2 seperate moves.";
    private static final String BOND_REMINDER = "Bond: bonded allies gain +20% direct-target range per level, ignore line of sight, reduce aura cost by 2% per level to a minimum of 1, and may receive maintained techniques without benefaction.";
    private static final String MODULAR_FORMS_PLACEHOLDER = "No Modular Forms Learned";
    private static final String HOLY_DOMAIN_SPECIALTY = "Holy Domain";
    private static final String DOMAIN_EMANATION_SPECIALTY = "Domain Emanation";
    private static final String DOMAIN_EMANATION_REMINDER_KEY = "Domain Emanation";
    private static final String MOLDING_REMINDER_KEY = "Molding";
    private static final String IRDON_RACE = "Irdon";
    private static final String UNARMED_PROWESS_SPECIALTY = "Unarmed Prowess";
    private static final int UNARMED_PROWESS_ITEM_IID = -5005;
    private static final String CLASS_LEVEL_RESOURCE1_STATUS = "ClassLevelR1";
    private static final String RACE_TRAINING_NAME = "Race Training";
    private static final String ANGEL_POINTS_RESOURCE_STATUS = "IrdonAngelPoints";
    private static final String ALTERI_RACE = "Alteri";
    private static final String ALTERI_SHAPESHIFT_LIST = "Shapeshift";
    private static final String MOLDS_LIST = "Molds";
    private static final String MOLD_CATEGORY_WEAPON = "Weapon";
    private static final String MOLD_CATEGORY_ARMOR = "Armor";
    private static final String FORMS_LIST = "Forms";
    private static final String MODULAR_FORMS_LIST = "Modular Forms";
    private static final String DOMAIN_EFFECTS_LIST = "Domain Effects";
    private static final String FAVORED_ENEMIES_LIST = "Favored Enemies";
    private static final String FAVORED_TERRAIN_LIST = "Favored Terrain";
    private static final String STANCE_LIST = "Stance";
    private static final String WEAPON_SPECIALIZATION_SPECIALTY = "Weapon Specialization";
    private static final String MARTIAL_FEATURE_PREFIX = "Martial Feature";
    private static final String MAINTAINED_ACTIVE_LOADOUT_KEY = "maintained.loadout.active";
    private static final String DEFAULT_MAINTAINED_LOADOUT = "Default";
    private static final String MAINTAINED_LOADOUT_PREFIX = "maintained.loadout.";
    private static final String MAINTAINED_ROW_PREFIX = "maintained.al.";
    private static final String VOW_STATUS_REMINDER_KEY = "Vow Status";
    private static final String VOW_STATUS_INTACT = "Intact";
    private static final String VOW_STATUS_BROKEN = "Broken";
    private static final String CURRENT_STANCE_REMINDER_KEY = "Current Stance";
    private static final String NO_ACTIVE_STANCE = "None";
    private static final String CURRENT_FORM_REMINDER_KEY = "Current Form:";
    private static final String MINION_FORM_LIST = "Minion Form";
    private static final String MINION_TYPE_LIST = "Minion Type";
    private static final String DEFAULT_MINION_FORM = "Generalized";
    private static final String DEFAULT_MINION_TYPE = "Melee";
    private static final String CLASS_SPECIALTY_CHOICE_PREFIX = "class.specialty.choice.";
    private static final String SHIFTER_CHESTPIECE_MOLD = "Shifter Chestpiece";
    private static final String SHIFTER_LEGGINGS_MOLD = "Shifter Leggings";
    private static final String MOLDING_MANIFEST_NOTE_PREFIX = "[MOLDING_MANIFEST]";
    private static final String SHIFTER_FORM_SWAP_STATUS_PREFIX = "Shifter Form Swap: ";
    private static final String SHIFTER_FORM_BONUS_STATUS_PREFIX = "Shifter Form Bonus: ";
    private static final String MOLDED_ITEM_PREFIX = "Molded ";
    private static final String SHUFFLE_SPECIALTY = "Shuffle";
    private static final String PRECISION_STANCE_NAME = "Precision";
    private static final String POWER_STANCE_NAME = "Power";
    private static final String PROTECTION_STANCE_NAME = "Protection";
    private static final String PUNISHMENT_STANCE_NAME = "Punishment";

    // ---------------------------------------------------------
    // Core subsystems
    // ---------------------------------------------------------

    private final CharIdentity identity;
    private final CharResources resources;
    private final CharAttributes attributes;
    private final CharSpecials specials;
    private final CharInventory inventory;
    private final CharTraining training;
    private final CharCombat combat;
    private List<List<DataList>> Lists;
    private Map<String, String> reminderSelections;
    @JsonProperty
    private boolean domainEmanationEnabled;
    private String panelReminder;
    private List<String> raceReminderLines;
    private List<String> classReminderLines;
    private List<String> otherSpecialtyReminderLines;
    private List<String> otherReminderLines;
    @JsonProperty
    private boolean enhancedEngineeringRankGranted;
    private String cachedClassSpecialtyKey;
    private List<DataSpecialty> cachedClassSpecialtyTemplates;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public StoreCharData() {
        this.identity = new CharIdentity();
        this.attributes = new CharAttributes();
        this.resources = new CharResources();
        this.specials = new CharSpecials();
        this.inventory = new CharInventory();
        this.training = new CharTraining();
        this.combat = new CharCombat();
        this.Lists = new ArrayList<>();
        this.reminderSelections = new LinkedHashMap<>();
        this.domainEmanationEnabled = false;
        this.panelReminder = "";
        this.raceReminderLines = new ArrayList<>();
        this.classReminderLines = new ArrayList<>();
        this.otherSpecialtyReminderLines = new ArrayList<>();
        this.otherReminderLines = new ArrayList<>();
        this.enhancedEngineeringRankGranted = false;
        this.cachedClassSpecialtyKey = "";
        this.cachedClassSpecialtyTemplates = List.of();
        this.identity.setOwner(this);
        this.attributes.setOwner(this);
        this.resources.setOwner(this);
        this.specials.setOwner(this);
        this.inventory.setOwner(this);
        this.training.setParent(this);
        this.combat.setOwner(this);
        syncDomainEmanationReminderSelection();
    }

    // ---------------------------------------------------------
    // Update Pipeline
    // ---------------------------------------------------------

    /**
     * Updates all subsystems in correct order.
     * Call whenever anything changes (level, gear, buffs, etc.)
     */
    public void updateAll() {
        this.identity.setOwner(this);
        this.attributes.setOwner(this);
        this.resources.setOwner(this);
        this.specials.setOwner(this);
        this.inventory.setOwner(this);
        this.training.setParent(this);
        this.combat.setOwner(this);
        syncDomainEmanationReminderSelection();
        setPanelReminder("");
        resetReminderBuckets();
        normalizeAlteriLists();
        restoreMaintainedTechniqueActiveLevelsFromSelections();

        try {
            syncEnhancedEngineeringTrainingBonus();
            if (training != null) {
                training.sortTrainingById();
            }

            refreshIdentityDerivedState();
            syncSpecialtyChoiceLists();
            syncShifterSpecialMoldsList();
        } catch (Exception e) {
            System.err.println("Failed to update tech caps / resources: " + e.getMessage());
        }

        refreshTrainingDerivedBonuses();
        refreshClassDomainEffects();
        syncUnarmedProwessWeapon();
        applyEquipmentPassiveBonuses();
        refreshSpecialtyPassiveBonuses();
        applyDomainEmanationToggleEffects();
        applyShifterFormAttributeSwap();
        if (attributes != null) {
            attributes.refreshLinkedAttributeStatuses();
        }
        this.combat.rebuildActions(this);
        logSpecialtyNames();
    }

    private void refreshClassDomainEffects() {
        if (training == null || identity == null) return;

        training.setDomains(List.of());
        training.clearDomainStatusEffects();
        training.clearDerivedAffinities();

        String className = identity.getCharClass();
        if (!"Cleric".equalsIgnoreCase(className) && !"Paladin".equalsIgnoreCase(className)) {
            return;
        }
        double bonusScale = "Paladin".equalsIgnoreCase(className) ? 0.5 : 1.0;
        if ("Cleric".equalsIgnoreCase(className) && hasDivineDedicationChoice(DIVINE_DEDICATION_DOMINANT)) {
            bonusScale *= 1.5;
        }

        String domainName = getStoredClassChoiceValue("Domain");
        if (domainName == null || domainName.isBlank()) {
            if ("Cleric".equalsIgnoreCase(className)) {
                syncClericDomainEffectsList(null, List.of());
            }
            return;
        }

        training.addDomain(domainName);
        if ("Cleric".equalsIgnoreCase(className)) {
            training.addDerivedAffinity(resolveClericDomainAffinity(domainName));
        }

        StoreRuleManager ruleManager = new StoreRuleManager();
        DataDomain domain = ruleManager.getDomainByName(domainName);
        if (domain == null) {
            if ("Cleric".equalsIgnoreCase(className)) {
                syncClericDomainEffectsList(null, List.of());
            }
            return;
        }

        int currentLevel = Math.max(1, getLevel());
        String[] bonuses = domain.getBonus();
        ArrayList<DataStatus> effects = new ArrayList<>();
        int maxIndex = Math.min(currentLevel, bonuses == null ? 0 : bonuses.length);
        for (int i = 0; i < maxIndex; i++) {
            DataStatus effect = buildDomainStatusEffect(domain, bonuses[i], i + 1, bonusScale);
            if (effect != null) {
                effects.add(effect);
            }
        }
        training.setDomainStatusEffects(effects);
        if ("Cleric".equalsIgnoreCase(className)) {
            syncClericDomainEffectsList(domain, effects);
        }
    }

    private void syncClericDomainEffectsList(DataDomain domain, List<DataStatus> effects) {
        if (identity == null || identity.getCharClass() == null || !identity.getCharClass().equalsIgnoreCase("Cleric")) {
            return;
        }
        List<DataList> group = findOrCreateListGroup(DOMAIN_EFFECTS_LIST);
        group.clear();
        if (domain == null || effects == null) {
            return;
        }

        for (DataStatus effect : effects) {
            if (effect == null) continue;
            String entryName = buildDomainEffectsListEntryName(effect);
            String entryDescription = effect.getDescription() == null ? "" : effect.getDescription().trim();
            if (entryName.isBlank() && entryDescription.isBlank()) continue;
            group.add(new DataList(DOMAIN_EFFECTS_LIST, entryName, entryDescription));
        }
    }

    private String buildDomainEffectsListEntryName(DataStatus effect) {
        if (effect == null) return "";
        String effectName = effect.getName() == null ? "" : effect.getName().trim();
        String levelToken = "";
        int suffixIndex = effectName.lastIndexOf("[L");
        if (suffixIndex >= 0 && effectName.endsWith("]")) {
            levelToken = effectName.substring(suffixIndex + 1, effectName.length() - 1).trim();
        }

        String description = effect.getDescription() == null ? "" : effect.getDescription().trim();
        String effectToken = "";
        String marker = ". Effect: ";
        int markerIndex = description.lastIndexOf(marker);
        if (markerIndex >= 0 && markerIndex + marker.length() < description.length()) {
            effectToken = description.substring(markerIndex + marker.length()).trim();
        }

        if (!levelToken.isBlank() && !effectToken.isBlank()) {
            return levelToken + ": " + effectToken;
        }
        if (!effectToken.isBlank()) {
            return effectToken;
        }
        return effectName;
    }

    private String resolveClericDomainAffinity(String domainName) {
        if (domainName == null || domainName.isBlank()) {
            return "Enhancement";
        }
        return isNamedAffinity(domainName) ? domainName.trim() : "Enhancement";
    }

    private boolean isNamedAffinity(String value) {
        if (value == null || value.isBlank()) return false;
        return switch (value.trim().toUpperCase()) {
            case "ENHANCEMENT", "BODY", "NATURE", "METAL", "EARTH", "WATER", "AIR", "FIRE",
                    "ELECTRICITY", "FORCE", "SOUND", "LIGHT", "DARKNESS", "POISON",
                    "PSIONIC", "ENERGY", "SPIRIT", "TIME" -> true;
            default -> false;
        };
    }

    private void syncUnarmedProwessWeapon() {
        if (inventory == null) return;

        DataItemWeapon currentAutoWeapon = findUnarmedProwessWeapon();
        if (currentAutoWeapon != null) {
            inventory.removeEquipment(currentAutoWeapon);
        }
    }

    private DataItemWeapon findUnarmedProwessWeapon() {
        if (inventory == null) return null;
        DataItemWeapon found = null;
        ArrayList<DataItemEquipment> duplicates = new ArrayList<>();
        for (DataItemEquipment item : inventory.getEquipment()) {
            if (!(item instanceof DataItemWeapon weapon)) continue;
            if (weapon.getIid() != UNARMED_PROWESS_ITEM_IID) continue;
            if (found == null) {
                found = weapon;
            } else {
                duplicates.add(weapon);
            }
        }
        for (DataItemEquipment duplicate : duplicates) {
            inventory.removeEquipment(duplicate);
        }
        return found;
    }

    public void syncLevelBaseResources(StoreRuleManager dq) {
        if (dq == null || identity == null || resources == null) return;
        DataLevel dataLevel = dq.getLevel(identity.getLevel());
        if (dataLevel == null) return;
        DataClass effectiveClass = resolveEffectiveClass(dq);
        double hpScaling = effectiveClass != null ? effectiveClass.getHpScaling() : 1.0;
        double auraScaling = effectiveClass != null ? effectiveClass.getAuraScaling() : 1.0;

        DataStatus hpStatus = new DataStatus();
        hpStatus.setName("Base");
        hpStatus.setAttribute("BASEHP");
        hpStatus.setDurationType("Passive");
        hpStatus.setSeverity(dataLevel.getBaseHP() * hpScaling);
        hpStatus.setAffinity("None");
        hpStatus.setDescription("Level and class-based base HP");
        resources.addStatus(hpStatus);

        DataStatus auraStatus = new DataStatus();
        auraStatus.setName("Base");
        auraStatus.setAttribute("BASEAURA");
        auraStatus.setDurationType("Passive");
        auraStatus.setSeverity(dataLevel.getBaseAura() * auraScaling);
        auraStatus.setAffinity("None");
        auraStatus.setDescription("Level and class-based base Aura");
        resources.addStatus(auraStatus);

        DataStatus reactionStatus = new DataStatus();
        reactionStatus.setName("Base");
        reactionStatus.setAttribute("BASEREACT");
        reactionStatus.setDurationType("Passive");
        reactionStatus.setSeverity(1.0);
        reactionStatus.setAffinity("None");
        reactionStatus.setDescription("Base reaction count");
        resources.addStatus(reactionStatus);
    }

    public void syncLevelCombatScalers(StoreRuleManager dq) {
        if (dq == null || identity == null || attributes == null) return;
        DataLevel dataLevel = dq.getLevel(identity.getLevel());
        if (dataLevel == null) return;

        DataClass effectiveClass = resolveEffectiveClass(dq);
        if (effectiveClass == null) return;

        int[] statScale = effectiveClass.getStatScaling();
        int[] levelScalers = dataLevel.getScalers();
        if (statScale == null || levelScalers == null) return;

        double fortSeverity = getTieredScalerSeverity(statScale, 0, levelScalers);
        double refSeverity = getTieredScalerSeverity(statScale, 1, levelScalers);
        double willSeverity = getTieredScalerSeverity(statScale, 2, levelScalers);
        double atkSeverity = getTieredScalerSeverity(statScale, 3, levelScalers);
        double appSeverity = getTieredScalerSeverity(statScale, 4, levelScalers, 3);
        double rangeSeverity = getTieredScalerSeverity(statScale, 5, levelScalers, 3);
        double baseDamageSeverity = parseLevelDamageValue(dataLevel.getDamage());

        setBasePassiveSeverity(attributes.getBDefense(), "BFORT", fortSeverity, "Class-based Fortitude base");
        removePassiveStatusByName(attributes.getBDefense(), "BFORT", "ClassLevelFort");
        setBasePassiveSeverity(attributes.getBDefense(), "BREF", refSeverity, "Class-based Reflex base");
        removePassiveStatusByName(attributes.getBDefense(), "BREF", "ClassLevelRef");
        setBasePassiveSeverity(attributes.getBDefense(), "BWILL", willSeverity, "Class-based Will base");
        removePassiveStatusByName(attributes.getBDefense(), "BWILL", "ClassLevelWill");
        setBasePassiveSeverity(attributes.getBCombat(), "BATK", atkSeverity, "Class-based Attack base");
        removePassiveStatusByName(attributes.getBCombat(), "BATK", "ClassLevelATK");
        setBasePassiveSeverity(attributes.getBCombat(), "BAPP", appSeverity, "Class-based Application base");
        removePassiveStatusByName(attributes.getBCombat(), "BAPP", "ClassLevelAPP");
        setBasePassiveSeverity(attributes.getBCombat(), "BRANGE", rangeSeverity, "Class-based Range base");
        removePassiveStatusByName(attributes.getBCombat(), "BRANGE", "ClassLevelRange");
        upsertLevelScalerStatus(attributes.getBDamage(), "BBDMG", "LevelBaseDamage", baseDamageSeverity, "Level-based base damage");
        upsertLevelScalerStatus(attributes.getBDamage(), "BBHEAL", "LevelBaseDamage", baseDamageSeverity, "Level-based base healing");
    }

    public void refreshIdentityDerivedState() {
        if (identity == null) return;
        StoreRuleManager dq = new StoreRuleManager();
        syncIdentityDerivedState(dq);
        syncLevelBaseResources(dq);
        syncLevelCombatScalers(dq);
    }

    public void syncIdentityDerivedState(StoreRuleManager dq) {
        if (dq == null || identity == null) return;
        int level = identity.getLevel();
        DataLevel dataLevel = dq.getLevel(level);
        updateIdentityDerivedState(dq, level, dataLevel);
        applySizeSaveModifiers();
    }

    public void refreshTrainingDerivedBonuses() {
        clearTrainingDerivedBonuses();
        applyTrainingPermStatuses(new StoreRuleManager());
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

    private void updateIdentityDerivedState(StoreRuleManager dq, int level, DataLevel currentLevelData) {
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

        syncStoredClassSpecialtyChoicesFromCurrentSpecialties();
        specials.setClassSpecialties(buildResolvedClassSpecialties(dq, effectiveClass, level));

        applyClassResourceScaling(effectiveClass);
        applyIrdonAngelPointScaling();
        applyClassLevelScalers(baseClass, currentLevelData);
        applyClassAttributeBonuses(baseClass);
    }

    private DataClass resolveEffectiveClass(StoreRuleManager dq) {
        if (dq == null || identity == null) return null;

        String subclassName = identity.getCharSubclass();
        if (subclassName != null && !subclassName.isBlank() && !"***".equals(subclassName.trim()) && !"?".equals(subclassName.trim())) {
            DataClass subclass = dq.getClassByName(subclassName);
            if (subclass != null) return subclass;
        }

        String className = identity.getCharClass();
        if (className == null || className.isBlank() || "?".equals(className.trim())) return null;
        return dq.getClassByName(className);
    }


    private double getTieredScalerSeverity(int[] statScale, int statScaleIndex, int[] levelScalers) {
        if (statScale == null || levelScalers == null || statScaleIndex < 0 || statScaleIndex >= statScale.length) return 0.0;
        int scalerTier = statScale[statScaleIndex];
        if (scalerTier <= 0 || scalerTier > levelScalers.length) return 0.0;
        return levelScalers[scalerTier - 1];
    }

    private double getTieredScalerSeverity(int[] statScale, int statScaleIndex, int[] levelScalers, int fallbackIndex) {
        if (statScale != null && statScaleIndex >= 0 && statScaleIndex < statScale.length) {
            return getTieredScalerSeverity(statScale, statScaleIndex, levelScalers);
        }
        return getTieredScalerSeverity(statScale, fallbackIndex, levelScalers);
    }

    private double parseLevelDamageValue(String damageValue) {
        if (damageValue == null || damageValue.isBlank()) return 0.0;
        String trimmed = damageValue.trim().toLowerCase();
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException ignored) {
        }

        int dIndex = trimmed.indexOf('d');
        if (dIndex > 0) {
            String prefix = trimmed.substring(0, dIndex).trim();
            if (!prefix.isEmpty()) {
                try {
                    return Double.parseDouble(prefix);
                } catch (NumberFormatException ignored) {
                }
            }
            String suffix = trimmed.substring(dIndex + 1).trim();
            if (!suffix.isEmpty()) {
                try {
                    return Double.parseDouble(suffix);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0.0;
    }

    private void upsertLevelScalerStatus(ArrayList<DataStatus>[][] category, String blockAttribute, String statusName, double severity, String description) {
        if (category == null || blockAttribute == null || statusName == null) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus first = block[0].get(0);
            if (first == null || !blockAttribute.equalsIgnoreCase(first.getAttribute())) continue;

            ArrayList<DataStatus> passiveList = block[0];
            DataStatus existing = null;
            for (DataStatus status : passiveList) {
                if (status != null && statusName.equalsIgnoreCase(status.getName())) {
                    existing = status;
                    break;
                }
            }

            if (existing == null) {
                DataStatus added = new DataStatus();
                added.setName(statusName);
                added.setAttribute(blockAttribute);
                added.setDurationType("Passive");
                added.setSeverity(severity);
                added.setAffinity("None");
                added.setDescription(description);
                passiveList.add(added);
            } else {
                existing.setSeverity(severity);
                existing.setDescription(description);
                existing.setDurationType("Passive");
                existing.setAttribute(blockAttribute);
            }
            return;
        }
    }

    private void setBasePassiveSeverity(ArrayList<DataStatus>[][] category, String blockAttribute, double severity, String description) {
        if (category == null || blockAttribute == null) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus first = block[0].get(0);
            if (first == null || !blockAttribute.equalsIgnoreCase(first.getAttribute())) continue;

            DataStatus baseStatus = null;
            for (DataStatus status : block[0]) {
                if (status != null && "Base".equalsIgnoreCase(status.getName())) {
                    baseStatus = status;
                    break;
                }
            }
            if (baseStatus == null) {
                baseStatus = new DataStatus();
                baseStatus.setName("Base");
                baseStatus.setAttribute(blockAttribute);
                baseStatus.setDurationType("Passive");
                block[0].add(0, baseStatus);
            }
            baseStatus.setSeverity(severity);
            baseStatus.setDescription(description);
            baseStatus.setDurationType("Passive");
            baseStatus.setAttribute(blockAttribute);
            baseStatus.setAffinity("None");
            return;
        }
    }

    private void removePassiveStatusByName(ArrayList<DataStatus>[][] category, String blockAttribute, String statusName) {
        if (category == null || blockAttribute == null || statusName == null) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus first = block[0].get(0);
            if (first == null || !blockAttribute.equalsIgnoreCase(first.getAttribute())) continue;
            block[0].removeIf(status -> status != null && statusName.equalsIgnoreCase(status.getName()));
            return;
        }
    }

    private void upsertResourcePassiveStatus(ArrayList<DataStatus>[] block, String blockAttribute, String statusName, double severity, String description) {
        if (block == null || block.length == 0 || block[0] == null || statusName == null || blockAttribute == null) return;

        DataStatus existing = null;
        for (DataStatus status : block[0]) {
            if (status != null && statusName.equalsIgnoreCase(status.getName())) {
                existing = status;
                break;
            }
        }

        if (existing == null) {
            existing = new DataStatus();
            existing.setName(statusName);
            existing.setDurationType("Passive");
            block[0].add(existing);
        }

        existing.setAttribute(blockAttribute);
        existing.setSeverity(severity);
        existing.setDescription(description);
        existing.setDurationType("Passive");
        existing.setAffinity("None");
    }

    private void syncRaceDerivedState(StoreRuleManager dq, DataRace race) {
        if (race == null) return;
        if (race.getRacialID() >= 0) {
            DataSpecialty racialSpec = dq.getSpecialtyById(race.getRacialID());
            if (racialSpec != null) {
                if ("Felshify (Felsh Cat)".equalsIgnoreCase(racialSpec.getName())) {
                    racialSpec.setPick(true);
                    if (racialSpec.getRefName() == null || racialSpec.getRefName().isBlank()) {
                        racialSpec.setRefName("Form");
                    }
                }
                specials.setRacialSpecialty(new DataSpecialty(racialSpec));
            }
        }

        String size = identity.getSize();
        if (size == null || size.isBlank() || "?".equals(size.trim())) {
            String raceSize = race.getSize();
            identity.setSize((raceSize != null && !raceSize.isBlank()) ? raceSize : "Medium");
        }

        appendIrdonAngelReminder(race);

        if (attributes == null) return;
        DataStatus[] racialStatuses = race.getBaseDataStatus();
        if (racialStatuses == null) return;
        for (DataStatus racialStatus : racialStatuses) {
            if (racialStatus == null || racialStatus.getAttribute() == null) continue;
            if (isReminderStatus(racialStatus)) {
                appendRaceReminderStatus(racialStatus);
                continue;
            }
            attributes.removeStatusByStatus(racialStatus);
            attributes.addStatus(new DataStatus(racialStatus));
        }
    }

    private void appendIrdonAngelReminder(DataRace race) {
        if (race == null || race.getName() == null || !IRDON_RACE.equalsIgnoreCase(race.getName().trim())) return;
        if (identity == null) return;
        List<String> racePicks = identity.getCharRacePick();
        if (racePicks == null || racePicks.isEmpty()) return;
        String angelName = racePicks.get(racePicks.size() - 1);
        if (angelName == null || angelName.isBlank()) return;
        appendReminderLine(raceReminderLines, angelName.trim() + " is watching. Angel Points: "
                + getCurrentAngelPoints() + " / " + getMaxAngelPoints());
    }

    private List<DataSpecialty> buildClassSpecialtyTemplates(StoreRuleManager dq, DataClass dataClass, int classRank) {
        List<DataSpecialty> classSpecs = new ArrayList<>();
        int classFamily = resolveClassSpecialtyFamily(dataClass);
        int subclassSpecStart = resolveSubclassSpecStart(dataClass);
        if (classFamily <= 0) return classSpecs;

        for (int i = 1; i <= classRank; i++) {
            DataLevel levelData = dq.getLevel(i);
            if (levelData == null) continue;

            int generalCount = levelData.getClassGeneral();
            int specCount = levelData.getClassSpec();
            int levelBaseId = (classFamily * 1000) + (10 * i);
            for (int j = 0; j < generalCount; j++) {
                addClassSpecialty(classSpecs, dq, levelBaseId + j + 1);
            }
            if (i == 1 && classFamily == 2) {
                addClassSpecialty(classSpecs, dq, 2013);
            }
            if (i == 1 && classFamily == 4) {
                addClassSpecialtyByName(classSpecs, dq, COMBAT_DISCIPLINE_SPECIALTY);
            }
            if (i == 1 && classFamily == 10) {
                addClassSpecialtyByName(classSpecs, dq, "Armor Dependency");
                addClassSpecialtyByName(classSpecs, dq, "Patronage");
                addClassSpecialtyByName(classSpecs, dq, "Enhanced Engineering");
                addClassSpecialtyByName(classSpecs, dq, "Salvo");
            }
            if (i == 4 && classFamily == 4) {
                addClassSpecialtyByName(classSpecs, dq, "Sharp Senses");
                addClassSpecialtyByName(classSpecs, dq, "Still Mind");
                addClassSpecialtyByName(classSpecs, dq, "Combat Discipline II");
            }
            if (i == 5 && dataClass != null && "Brawler".equalsIgnoreCase(dataClass.getName())) {
                addClassSpecialtyByName(classSpecs, dq, "Tolerance");
                addClassSpecialtyByName(classSpecs, dq, "Clash");
                addClassSpecialtyByName(classSpecs, dq, "Shuffle");
            }
            for (int j = 0; j < specCount; j++) {
                addClassSpecialty(classSpecs, dq, levelBaseId + subclassSpecStart + j);
            }
        }
        return classSpecs;
    }

    private int resolveClassSpecialtyFamily(DataClass dataClass) {
        if (dataClass == null || dataClass.getID() <= 0) return -1;
        return ((dataClass.getID() - 1) / 3) + 1;
    }

    private int resolveSubclassSpecStart(DataClass dataClass) {
        if (dataClass == null || dataClass.getID() <= 0) return 1;
        return 1 + (((dataClass.getID() - 1) % 3) * 3);
    }

    private List<DataSpecialty> getCachedClassSpecialties(StoreRuleManager dq, DataClass dataClass, int classRank) {
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

    private List<DataSpecialty> buildResolvedClassSpecialties(StoreRuleManager dq, DataClass dataClass, int classRank) {
        List<DataSpecialty> resolved = new ArrayList<>(getCachedClassSpecialties(dq, dataClass, classRank));
        mergeExistingClassSpecialtyChoices(resolved);
        applyStoredClassSpecialtyChoices(resolved);
        replaceClassFeatureChoicePlaceholders(resolved, dq);
        if (identity == null) return resolved;

        List<String> picks = identity.getCharClassPick();
        if (picks == null || picks.isEmpty()) return resolved;

        if ("Archer".equalsIgnoreCase(identity.getCharClass())) {
            applyArcherFavoredSelections(resolved, dq);
            return resolved;
        }

        List<String> choiceLabels = getStoredClassChoiceLabels(identity.getCharClass());
        boolean[] consumed = new boolean[picks.size()];
        for (int i = 0; i < picks.size() && i < choiceLabels.size(); i++) {
            String pick = picks.get(i);
            if (pick == null || pick.isBlank()) continue;
            String featureName = mapChoiceLabelToFeatureName(choiceLabels.get(i));
            if (featureName == null || featureName.isBlank()) continue;
            consumed[i] = applyClassChoiceToFeature(resolved, dq, featureName, pick);
        }

        for (int i = 0; i < picks.size(); i++) {
            if (consumed[i]) continue;
            String pick = picks.get(i);
            if (pick == null || pick.isBlank()) continue;
            DataSpecialty pickedSpecialty = dq.getSpecialtyByName(pick);
            if (pickedSpecialty != null) {
                resolved.add(new DataSpecialty(pickedSpecialty));
            }
        }
        return resolved;
    }

    private void replaceClassFeatureChoicePlaceholders(List<DataSpecialty> resolved, StoreRuleManager dq) {
        if (resolved == null || dq == null) return;
        for (int i = 0; i < resolved.size(); i++) {
            DataSpecialty specialty = resolved.get(i);
            if (specialty == null || specialty.getName() == null) continue;
            if (!isClassFeatureChoicePlaceholder(specialty)) continue;
            String chosenSpecialtyName = extractSpecialtyChoiceValue(specialty.getRefName());
            if (chosenSpecialtyName.isBlank()) continue;
            DataSpecialty chosenSpecialty = dq.getSpecialtyByName(chosenSpecialtyName);
            if (chosenSpecialty == null && WEAPON_SPECIALIZATION_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) {
                chosenSpecialty = dq.getSpecialtyByName("Specialization (" + chosenSpecialtyName + ")");
            }
            if (chosenSpecialty == null) continue;
            resolved.set(i, new DataSpecialty(chosenSpecialty));
        }
    }

    private boolean isClassFeatureChoicePlaceholder(DataSpecialty specialty) {
        if (specialty == null || specialty.getName() == null) return false;
        String specialtyName = specialty.getName().trim();
        return WEAPON_SPECIALIZATION_SPECIALTY.equalsIgnoreCase(specialtyName)
                || specialtyName.regionMatches(true, 0, MARTIAL_FEATURE_PREFIX, 0, MARTIAL_FEATURE_PREFIX.length());
    }

    private void syncStoredClassSpecialtyChoicesFromCurrentSpecialties() {
        if (specials == null) return;
        for (DataSpecialty specialty : specials.getClassSpecialties()) {
            if (specialty == null) continue;
            storeClassSpecialtyChoice(specialty);
        }
    }

    private void applyStoredClassSpecialtyChoices(List<DataSpecialty> resolved) {
        if (resolved == null) return;
        for (DataSpecialty specialty : resolved) {
            if (specialty == null) continue;
            if (specialty.getRefName() != null && !specialty.getRefName().isBlank()) continue;
            String storedChoice = getStoredClassSpecialtyChoice(specialty);
            if (storedChoice != null && !storedChoice.isBlank()) {
                specialty.setRefName(storedChoice);
            }
        }
    }

    private void storeClassSpecialtyChoice(DataSpecialty specialty) {
        if (specialty == null || specialty.getRefName() == null || specialty.getRefName().isBlank()) return;
        setReminderSelection(buildClassSpecialtyChoiceKey(specialty), specialty.getRefName().trim());
    }

    private String getStoredClassSpecialtyChoice(DataSpecialty specialty) {
        if (specialty == null) return "";
        String storedChoice = getReminderSelection(buildClassSpecialtyChoiceKey(specialty));
        if ((storedChoice == null || storedChoice.isBlank())
                && specialty.getName() != null
                && COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) {
            DataSpecialty alias = new DataSpecialty(specialty);
            alias.setName(MARTIAL_FOCUS_SPECIALTY);
            storedChoice = getReminderSelection(buildClassSpecialtyChoiceKey(alias));
        }
        return storedChoice;
    }

    private String buildClassSpecialtyChoiceKey(DataSpecialty specialty) {
        if (specialty == null) {
            return CLASS_SPECIALTY_CHOICE_PREFIX + "unknown";
        }
        if (specialty.getId() > 0) {
            return CLASS_SPECIALTY_CHOICE_PREFIX + specialty.getId();
        }
        String safeName = specialty.getName() == null ? "unknown" : specialty.getName().trim().toLowerCase(Locale.ROOT);
        safeName = safeName.replaceAll("[^a-z0-9]+", "_");
        if (safeName.isBlank()) safeName = "unknown";
        return CLASS_SPECIALTY_CHOICE_PREFIX + safeName;
    }

    private void mergeExistingClassSpecialtyChoices(List<DataSpecialty> resolved) {
        if (resolved == null || specials == null) return;
        for (DataSpecialty existing : specials.getClassSpecialties()) {
            if (existing == null) continue;
            String existingRef = existing.getRefName();
            if (existingRef == null || existingRef.isBlank()) continue;
            for (DataSpecialty current : resolved) {
                if (!matchesSpecialtyIdentity(existing, current)) continue;
                if (current.getRefName() == null || current.getRefName().isBlank()) {
                    current.setRefName(existingRef);
                }
                break;
            }
        }
    }

    private boolean matchesSpecialtyIdentity(DataSpecialty left, DataSpecialty right) {
        if (left == null || right == null) return false;
        if (left.getId() > 0 && right.getId() > 0 && left.getId() == right.getId()) {
            return true;
        }
        String leftName = left.getName();
        String rightName = right.getName();
        if (isCombatDisciplineAliasName(leftName) && isCombatDisciplineAliasName(rightName)) {
            return true;
        }
        return leftName != null && rightName != null && leftName.equalsIgnoreCase(rightName);
    }

    private List<String> getStoredClassChoiceLabels(String className) {
        if (className == null || className.isBlank()) return List.of();
        return switch (className) {
            case "Paladin" -> List.of("Deity", "Vow", "Domain");
            case "Cleric" -> List.of("Deity", "Domain");
            case "Warrior" -> List.of("Specialty", "Combat Action");
            case "Monk" -> List.of(COMBAT_DISCIPLINE_I_DISPLAY);
            case "Archer" -> List.of("Favored Enemy", "Favored Terrain");
            case "Shifter" -> List.of("Melee Affinity", "Ranged Affinity", "Weapon Mold 1", "Weapon Mold 2");
            case "Pilot" -> List.of("Primary Attribute", "Patron");
            default -> List.of();
        };
    }

    private String mapChoiceLabelToFeatureName(String choiceLabel) {
        if (choiceLabel == null || choiceLabel.isBlank()) return null;
        return switch (choiceLabel) {
            case "Deity" -> "Divine Attunement";
            case "Vow" -> "Divine Vow";
            case "Domain" -> "Holy Domain";
            case "Specialty" -> "Martial Feature";
            case "Combat Action" -> "Specialized Combatant";
            case "Discipline", "Martial Focus", COMBAT_DISCIPLINE_I_DISPLAY -> COMBAT_DISCIPLINE_SPECIALTY;
            case "Favored Enemy" -> "Favored Enemy";
            case "Favored Terrain" -> "Favored Terrain";
            case "Primary Attribute" -> "Primary Attribute";
            case "Melee Affinity" -> "Melee Affinity";
            case "Ranged Affinity" -> "Ranged Affinity";
            case "Weapon Mold 1" -> "Weapon Mold 1";
            case "Weapon Mold 2" -> "Weapon Mold 2";
            default -> null;
        };
    }

    private void applyArcherFavoredSelections(List<DataSpecialty> specialties, StoreRuleManager dq) {
        if (specialties == null || dq == null) return;
        int insertIndex = findSpecialtyIndex(specialties, "Favored Enemy and Terrain");
        if (insertIndex < 0) {
            insertIndex = specialties.size();
        }

        removeSpecialtyByName(specialties, "Favored Enemy");
        removeSpecialtyByName(specialties, "Favored Terrain");
        removeSpecialtyByName(specialties, "Favored Enemy and Terrain");

        DataSpecialty favoredEnemy = dq.getSpecialtyByName("Favored Enemy");
        if (favoredEnemy != null) {
            specialties.add(Math.min(insertIndex, specialties.size()), new DataSpecialty(favoredEnemy));
            insertIndex++;
        }

        DataSpecialty favoredTerrain = dq.getSpecialtyByName("Favored Terrain");
        if (favoredTerrain != null) {
            specialties.add(Math.min(insertIndex, specialties.size()), new DataSpecialty(favoredTerrain));
        }
    }

    private int findSpecialtyIndex(List<DataSpecialty> specialties, String specialtyName) {
        if (specialties == null || specialtyName == null || specialtyName.isBlank()) return -1;
        for (int i = 0; i < specialties.size(); i++) {
            DataSpecialty specialty = specialties.get(i);
            if (specialty != null && specialty.getName() != null && specialtyName.equalsIgnoreCase(specialty.getName().trim())) {
                return i;
            }
        }
        return -1;
    }

    private void removeSpecialtyByName(List<DataSpecialty> specialties, String specialtyName) {
        int index = findSpecialtyIndex(specialties, specialtyName);
        if (index >= 0) {
            specialties.remove(index);
        }
    }

    private String getStoredClassChoiceValue(String choiceLabel) {
        if (identity == null || choiceLabel == null || choiceLabel.isBlank()) return "";
        List<String> labels = getStoredClassChoiceLabels(identity.getCharClass());
        List<String> picks = identity.getCharClassPick();
        if (labels.isEmpty() || picks == null || picks.isEmpty()) return "";
        for (int i = 0; i < labels.size() && i < picks.size(); i++) {
            if (choiceLabel.equalsIgnoreCase(labels.get(i))) {
                String pick = picks.get(i);
                return pick == null ? "" : pick.trim();
            }
        }
        return "";
    }

    private boolean applyClassChoiceToFeature(List<DataSpecialty> specialties, StoreRuleManager dq, String featureName, String choiceValue) {
        if (specialties == null || featureName == null || featureName.isBlank() || choiceValue == null || choiceValue.isBlank()) return false;
        for (int i = 0; i < specialties.size(); i++) {
            DataSpecialty specialty = specialties.get(i);
            if (specialty == null || specialty.getName() == null) continue;
            if (!matchesFeatureName(featureName, specialty.getName())) continue;
            DataSpecialty pickedSpecialty = dq == null ? null : dq.getSpecialtyByName(choiceValue);
            if (pickedSpecialty != null) {
                specialties.set(i, new DataSpecialty(pickedSpecialty));
                return true;
            }
            if (specialty.getRefName() != null && !specialty.getRefName().isBlank()) continue;
            specialty.setRefName(choiceValue);
            return true;
        }
        return false;
    }

    private DataStatus buildDomainStatusEffect(DataDomain domain, String bonusText, int levelRequirement, double bonusScale) {
        if (domain == null || bonusText == null) return null;
        String trimmed = bonusText.trim();
        if (trimmed.isBlank()) return null;

        DataStatus status = new DataStatus();
        status.setName("Domain: " + domain.getName() + " [L" + levelRequirement + "]");
        status.setAffinity(domain.getName());
        status.setDurationType("Domain");
        status.setDuration(0);
        status.setDescription(buildDomainStatusDescription(domain, trimmed, levelRequirement));

        if ("Special".equalsIgnoreCase(trimmed)) {
            status.setAttribute("REMINDER");
            status.setSeverity(0.0);
            return status;
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length < 3) {
            status.setAttribute("REMINDER");
            status.setSeverity(0.0);
            return status;
        }

        String signToken = parts[0];
        String amountToken = parts[1];
        String attributeToken = parts[2];

        double severity = evaluateDomainBonusAmount(amountToken, getLevel());
        if (signToken.startsWith("-")) {
            severity *= -1.0;
        }
        severity *= bonusScale;

        String resolvedAttribute = resolveDomainStatusAttribute(attributeToken);
        if ("SPENTAURA".equalsIgnoreCase(resolvedAttribute)) {
            status.setAttribute("AURA");
            status.setSeverity(-severity);
        } else if ("LOSTHP".equalsIgnoreCase(resolvedAttribute)) {
            status.setAttribute("HP");
            status.setSeverity(-severity);
        } else {
            status.setAttribute(resolvedAttribute);
            status.setSeverity(severity);
        }
        return status;
    }

    private String buildDomainStatusDescription(DataDomain domain, String bonusText, int levelRequirement) {
        String condition = domain.getCondition() == null || domain.getCondition().isBlank() ? "-" : domain.getCondition().trim();
        return "Domain " + domain.getName()
                + " level " + levelRequirement
                + " effect. Condition: " + condition
                + ". Effect: " + bonusText;
    }

    private double evaluateDomainBonusAmount(String token, int level) {
        if (token == null || token.isBlank()) return 0.0;
        String normalized = token.trim().toUpperCase();
        if ("CL".equals(normalized)) {
            return level;
        }
        if (normalized.contains("*")) {
            String[] factors = normalized.split("\\*");
            double result = 1.0;
            for (String factor : factors) {
                if (factor == null || factor.isBlank()) continue;
                String piece = factor.trim();
                if ("CL".equals(piece)) {
                    result *= level;
                } else {
                    try {
                        result *= Double.parseDouble(piece);
                    } catch (NumberFormatException ignored) {
                        return 0.0;
                    }
                }
            }
            return result;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private String resolveDomainStatusAttribute(String attributeToken) {
        if (attributeToken == null || attributeToken.isBlank()) return "REMINDER";
        String normalized = attributeToken.trim().toUpperCase();
        if ("PRIM".equals(normalized)) {
            return resolveDomainPrimaryAttribute();
        }
        return normalized;
    }

    private String resolveDomainPrimaryAttribute() {
        if (identity == null) return "PRIM";
        StoreRuleManager ruleManager = new StoreRuleManager();

        String subclassName = identity.getCharSubclass();
        if (subclassName != null && !subclassName.isBlank() && !"?".equals(subclassName.trim()) && !"***".equals(subclassName.trim())) {
            DataClass subclass = ruleManager.getClassByName(subclassName);
            if (subclass != null && subclass.getPrimaryAtt() != null && !subclass.getPrimaryAtt().isBlank()) {
                return subclass.getPrimaryAtt().trim().toUpperCase();
            }
        }

        String className = identity.getCharClass();
        if (className == null || className.isBlank() || "?".equals(className.trim())) return "PRIM";
        DataClass baseClass = ruleManager.getClassByName(className);
        if (baseClass == null || baseClass.getPrimaryAtt() == null || baseClass.getPrimaryAtt().isBlank()) return "PRIM";
        return baseClass.getPrimaryAtt().trim().toUpperCase();
    }

    public double getCombatSpecialistBonusForManeuver(String maneuverName) {
        if (maneuverName == null || maneuverName.isBlank() || specials == null || attributes == null) return 0.0;
        DataSpecialty specialty = specials.findSpecialty(COMBAT_SPECIALIST_SPECIALTY);
        if (specialty == null) return 0.0;
        String chosenManeuver = extractSpecialtyChoiceValue(specialty.getRefName());
        if (chosenManeuver == null || chosenManeuver.isBlank() || !chosenManeuver.equalsIgnoreCase(maneuverName.trim())) {
            return 0.0;
        }
        String primaryAttribute = resolveDomainPrimaryAttribute();
        if (primaryAttribute == null || primaryAttribute.isBlank() || "PRIM".equalsIgnoreCase(primaryAttribute)) {
            return 0.0;
        }
        return Math.max(0.0, attributes.calcStatusValue(primaryAttribute));
    }

    public double getEffectiveCombatManeuverValue(String maneuverName) {
        double baseValue = attributes == null ? 0.0 : Math.max(0.0, attributes.calcStatusValue("CMAN"));
        return baseValue + getCombatSpecialistBonusForManeuver(maneuverName);
    }

    private void addClassSpecialty(List<DataSpecialty> classSpecs, StoreRuleManager dq, int specialtyId) {
        DataSpecialty baseSpec = dq.getSpecialtyById(specialtyId);
        if (baseSpec != null && classSpecs.stream().noneMatch(spec -> spec != null && spec.getId() == specialtyId)) {
            classSpecs.add(new DataSpecialty(baseSpec));
        }
    }

    private void addClassSpecialtyByName(List<DataSpecialty> classSpecs, StoreRuleManager dq, String specialtyName) {
        if (classSpecs == null || dq == null || specialtyName == null || specialtyName.isBlank()) return;
        DataSpecialty baseSpec = dq.getSpecialtyByName(specialtyName);
        if (baseSpec == null) return;
        boolean exists = classSpecs.stream().anyMatch(spec ->
                spec != null
                        && spec.getName() != null
                        && specialtyName.equalsIgnoreCase(spec.getName().trim()));
        if (!exists) {
            classSpecs.add(new DataSpecialty(baseSpec));
        }
    }

    private void applyClassResourceScaling(DataClass effectiveClass) {
        if (effectiveClass == null || resources == null) return;
        int level = identity != null ? Math.max(0, identity.getLevel()) : 0;
        double resource1Base = hasUnlockedResourceSpecialtyType("resource1") ? level : 0.0;
        upsertResourcePassiveStatus(
                resources.getBaseResource1(),
                "BASER1",
                CLASS_LEVEL_RESOURCE1_STATUS,
                resource1Base,
                "Level-based base value for Resource 1");
    }

    private void applyIrdonAngelPointScaling() {
        if (resources == null) return;
        double angelPointBase = hasAngelPoints() ? getRaceTrainingRank() : 0.0;
        upsertResourcePassiveStatus(
                resources.getBaseAngelPoints(),
                "BASEANGEL",
                ANGEL_POINTS_RESOURCE_STATUS,
                angelPointBase,
                "Irdon Angel Points");
    }

    private boolean hasUnlockedResourceSpecialtyType(String specialtyType) {
        if (specialtyType == null || specialtyType.isBlank() || specials == null) return false;
        StoreRuleManager dq = new StoreRuleManager();
        for (DataSpecialty specialty : specials.getAllSpecialties()) {
            if (specialty == null) continue;
            String liveType = null;
            if (specialty.getId() > 0) {
                DataSpecialty resolved = dq.getSpecialtyById(specialty.getId());
                if (resolved != null) liveType = resolved.getType();
            }
            String candidateType = liveType != null && !liveType.isBlank() ? liveType : specialty.getType();
            if (candidateType != null && specialtyType.equalsIgnoreCase(candidateType.trim())) return true;
        }
        return false;
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
        double primaryMod = attributes.calcStatusValue(primaryAttribute) - 10.0;
        /*int primaryValue = attributes.getAttribute(primaryAttribute);
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
        int capacityValue = attributes.getAttribute("CAP");*/

        /*double primaryAtkSeverity = 0.5 * primaryValue;
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
        double controlSeverity = 0.5 * controlMod;*/

        /*addPermanentAttributeStatus("combat", "ATK", "ClassPrimaryATK", primaryAtkSeverity, "Primary attribute bonus to ATK");
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
        addPermanentAttributeStatus("damage", "TDMG", "CtlTDMGBonus", controlSeverity, "Control-based Total Damage bonus");
        addPermanentAttributeStatus("damage", "THEAL", "CtlTHEALBonus", controlSeverity, "Control-based Total Healing bonus");
    */
        }

    /**
     * Ensures level specialties are present for all levels up to current level (max 20).
     * This backfills missed grants when a character levels up.
     */
    private void ensureLevelSpecialties(StoreRuleManager dq, int level) {
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
        if (Lists == null) {
            Lists = new ArrayList<>();
        }

        initializeAlteriShapeshiftList();
        initializeShifterFormsList();
        syncModularFormsList();
        initializeShifterMoldsList();
        syncShifterSpecialMoldsList();
        initializeLeaderMinionLists();
        initializeFavoredSpecialtyLists();
    }

    private void initializeAlteriShapeshiftList() {
        if (identity == null || !ALTERI_RACE.equalsIgnoreCase(identity.getRace())) return;

        String shapeshiftName = "";
        List<String> racePicks = identity.getCharRacePick();
        if (racePicks != null && !racePicks.isEmpty() && racePicks.get(0) != null) {
            shapeshiftName = racePicks.get(0);
        }

        ArrayList<DataList> alteriList = new ArrayList<>();
        alteriList.add(new DataList(ALTERI_SHAPESHIFT_LIST, ALTERI_RACE, ""));
        alteriList.add(new DataList(ALTERI_SHAPESHIFT_LIST, shapeshiftName, ""));

        for (int i = 0; i < Lists.size(); i++) {
            List<DataList> existingGroup = Lists.get(i);
            if (existingGroup == null || existingGroup.isEmpty()) continue;
            DataList first = existingGroup.get(0);
            if (first == null || first.getList() == null) continue;
            String listName = first.getList().trim();
            if ("Shapeshifts".equalsIgnoreCase(listName) || "Shapeshift".equalsIgnoreCase(listName)) {
                Lists.set(i, alteriList);
                return;
            }
        }

        Lists.add(alteriList);
    }

    private void initializeShifterMoldsList() {
        if (identity == null || identity.getCharClass() == null || !identity.getCharClass().equalsIgnoreCase("Shifter")) {
            return;
        }

        String firstMold = getStoredClassChoiceValue("Weapon Mold 1");
        String secondMold = getStoredClassChoiceValue("Weapon Mold 2");
        if ((firstMold == null || firstMold.isBlank()) && (secondMold == null || secondMold.isBlank())) {
            return;
        }

        List<DataList> moldsGroup = null;
        for (List<DataList> group : Lists) {
            if (group == null || group.isEmpty()) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null) continue;
                if (MOLDS_LIST.equalsIgnoreCase(entry.getList().trim())) {
                    moldsGroup = group;
                    break;
                }
            }
            if (moldsGroup != null) {
                break;
            }
        }

        if (moldsGroup == null) {
            moldsGroup = new ArrayList<>();
            Lists.add(moldsGroup);
        }

        addListEntryIfMissing(moldsGroup, MOLDS_LIST, firstMold, buildMoldEntryDescription(MOLD_CATEGORY_WEAPON, firstMold, ""));
        addListEntryIfMissing(moldsGroup, MOLDS_LIST, secondMold, buildMoldEntryDescription(MOLD_CATEGORY_WEAPON, secondMold, ""));
    }

    private void syncShifterSpecialMoldsList() {
        if (Lists == null) {
            Lists = new ArrayList<>();
        }
        if (!isShifterClass()) {
            removeShifterSpecialMoldsFromLists();
            return;
        }

        List<DataList> moldsGroup = findOrCreateListGroup(MOLDS_LIST);
        removeLockedShifterSpecialMolds(moldsGroup);
        int level = Math.max(0, getLevel());
        for (String moldName : getUnlockedShifterSpecialMolds(level)) {
            addListEntryIfMissing(moldsGroup, MOLDS_LIST, moldName,
                    buildMoldEntryDescription(MOLD_CATEGORY_ARMOR, "Shifter", resolveShifterSpecialMoldSlot(moldName)));
        }
    }

    private void removeShifterSpecialMoldsFromLists() {
        if (Lists == null) return;
        for (List<DataList> group : Lists) {
            if (group == null) continue;
            group.removeIf(entry -> entry != null
                    && MOLDS_LIST.equalsIgnoreCase(safeTrim(entry.getList()))
                    && isShifterSpecialMoldName(entry.getName()));
        }
    }

    private void removeLockedShifterSpecialMolds(List<DataList> moldsGroup) {
        if (moldsGroup == null) return;
        int level = Math.max(0, getLevel());
        moldsGroup.removeIf(entry -> entry != null
                && MOLDS_LIST.equalsIgnoreCase(safeTrim(entry.getList()))
                && isShifterSpecialMoldName(entry.getName())
                && resolveShifterSpecialMoldRequiredLevel(entry.getName()) > level);
    }

    private List<String> getUnlockedShifterSpecialMolds(int level) {
        ArrayList<String> molds = new ArrayList<>();
        if (level >= resolveShifterSpecialMoldRequiredLevel(SHIFTER_CHESTPIECE_MOLD)) {
            molds.add(SHIFTER_CHESTPIECE_MOLD);
        }
        if (level >= resolveShifterSpecialMoldRequiredLevel(SHIFTER_LEGGINGS_MOLD)) {
            molds.add(SHIFTER_LEGGINGS_MOLD);
        }
        return molds;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isShifterSpecialMoldName(String moldName) {
        return resolveShifterSpecialMoldSlot(moldName) != null;
    }

    public static String resolveShifterSpecialMoldSlot(String moldName) {
        if (moldName == null || moldName.isBlank()) return null;
        String normalized = moldName.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("shifter ")) return null;
        String slotToken = normalized.substring("shifter ".length()).trim();
        return switch (slotToken) {
            case "chest", "chestpiece" -> "Chest";
            case "legs", "leggings" -> "Legs";
            default -> null;
        };
    }

    public static int resolveShifterSpecialMoldRequiredLevel(String moldName) {
        String slot = resolveShifterSpecialMoldSlot(moldName);
        if (slot == null) return Integer.MAX_VALUE;
        return 1;
    }

    private void initializeShifterFormsList() {
        if (identity == null || identity.getCharClass() == null || !identity.getCharClass().equalsIgnoreCase("Shifter")) {
            return;
        }

        String meleeAffinity = getStoredClassChoiceValue("Melee Affinity");
        String rangedAffinity = getStoredClassChoiceValue("Ranged Affinity");
        if ((meleeAffinity == null || meleeAffinity.isBlank()) && (rangedAffinity == null || rangedAffinity.isBlank())) {
            return;
        }

        List<DataList> formsGroup = null;
        for (List<DataList> group : Lists) {
            if (group == null || group.isEmpty()) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null) continue;
                if (FORMS_LIST.equalsIgnoreCase(entry.getList().trim())) {
                    formsGroup = group;
                    break;
                }
            }
            if (formsGroup != null) {
                break;
            }
        }

        if (formsGroup == null) {
            formsGroup = new ArrayList<>();
            Lists.add(formsGroup);
        }

        if (meleeAffinity != null && !meleeAffinity.isBlank()) {
            addListEntryIfMissing(formsGroup, FORMS_LIST, "Heavy (" + meleeAffinity.trim() + ")");
        }
        if (rangedAffinity != null && !rangedAffinity.isBlank()) {
            addListEntryIfMissing(formsGroup, FORMS_LIST, "Light (" + rangedAffinity.trim() + ")");
        }
        ensureShifterCurrentFormSelection(formsGroup);
    }

    private void syncModularFormsList() {
        if (Lists == null) {
            Lists = new ArrayList<>();
        }
        if (!isShifterClass() || specials == null || !specials.hasSpecialty(MODULAR_FORM_SPECIALTY)) {
            removeListGroup(MODULAR_FORMS_LIST);
            return;
        }

        List<DataList> modularFormsGroup = findOrCreateListGroup(MODULAR_FORMS_LIST);
        boolean hasRealEntries = false;
        for (DataList entry : modularFormsGroup) {
            if (entry == null || entry.getName() == null) continue;
            String entryName = entry.getName().trim();
            if (entryName.isBlank() || MODULAR_FORMS_PLACEHOLDER.equalsIgnoreCase(entryName)) continue;
            hasRealEntries = true;
            break;
        }
        final boolean keepOnlyRealEntries = hasRealEntries;
        modularFormsGroup.removeIf(entry -> entry != null
                && entry.getName() != null
                && MODULAR_FORMS_PLACEHOLDER.equalsIgnoreCase(entry.getName().trim())
                && keepOnlyRealEntries);
        if (!hasRealEntries) {
            addListEntryIfMissing(modularFormsGroup, MODULAR_FORMS_LIST, MODULAR_FORMS_PLACEHOLDER);
        }
    }

    private void ensureShifterCurrentFormSelection(List<DataList> formsGroup) {
        if (formsGroup == null || reminderSelections == null) return;
        String existingSelection = getReminderSelection(CURRENT_FORM_REMINDER_KEY);
        if (existingSelection != null && !existingSelection.isBlank()) return;
        for (DataList entry : formsGroup) {
            if (entry == null || entry.getName() == null) continue;
            String formName = entry.getName().trim();
            if (formName.isBlank()) continue;
            setReminderSelection(CURRENT_FORM_REMINDER_KEY, formName);
            return;
        }
    }

    private void initializeLeaderMinionLists() {
        if (identity == null || identity.getCharClass() == null || !identity.getCharClass().equalsIgnoreCase("Leader")) {
            return;
        }

        List<DataList> minionFormGroup = findOrCreateListGroup(MINION_FORM_LIST);
        addListEntryIfMissing(minionFormGroup, MINION_FORM_LIST, DEFAULT_MINION_FORM);

        List<DataList> minionTypeGroup = findOrCreateListGroup(MINION_TYPE_LIST);
        addListEntryIfMissing(minionTypeGroup, MINION_TYPE_LIST, DEFAULT_MINION_TYPE);
    }

    private void initializeFavoredSpecialtyLists() {
        if (Lists == null) {
            Lists = new ArrayList<>();
        }
        if (specials == null) return;

        if (specials.hasSpecialty("Favored Enemy")) {
            List<DataList> favoredEnemies = findOrCreateListGroup(FAVORED_ENEMIES_LIST);
            String initialEnemy = resolveInitialArcherFavoredEnemyChoice();
            if (initialEnemy != null && !initialEnemy.isBlank()) {
                addListEntryIfMissing(favoredEnemies, FAVORED_ENEMIES_LIST, initialEnemy);
            }
            migrateFavoredSpecialtyRefNames("Favored Enemy", FAVORED_ENEMIES_LIST);
        }

        if (specials.hasSpecialty("Favored Terrain")) {
            List<DataList> favoredTerrain = findOrCreateListGroup(FAVORED_TERRAIN_LIST);
            String initialTerrain = resolveInitialArcherFavoredTerrainChoice();
            if (initialTerrain != null && !initialTerrain.isBlank()) {
                addListEntryIfMissing(favoredTerrain, FAVORED_TERRAIN_LIST, initialTerrain);
            }
            migrateFavoredSpecialtyRefNames("Favored Terrain", FAVORED_TERRAIN_LIST);
        }
    }

    public void syncSpecialtyChoiceLists() {
        initializeFavoredSpecialtyLists();
        initializeStanceSpecialtyList();
        syncModularFormsList();
    }

    private void initializeStanceSpecialtyList() {
        if (Lists == null) {
            Lists = new ArrayList<>();
        }
        List<DataList> stanceGroup = findOrCreateListGroup(STANCE_LIST);
        stanceGroup.clear();
        if (specials == null || !specials.hasSpecialty(STANCE_SPECIALTY)) return;

        Map<String, Integer> stanceRanks = new LinkedHashMap<>();
        collectStanceRanks(specials.getRacialSpecialty(), stanceRanks);
        for (DataSpecialty specialty : specials.getClassSpecialties()) {
            collectStanceRanks(specialty, stanceRanks);
        }
        for (DataSpecialty specialty : specials.getTrainedSpecialties()) {
            collectStanceRanks(specialty, stanceRanks);
        }

        for (Map.Entry<String, Integer> entry : stanceRanks.entrySet()) {
            String stanceName = entry.getKey();
            if (stanceName == null || stanceName.isBlank()) continue;
            int rank = Math.max(1, Math.min(3, entry.getValue() == null ? 1 : entry.getValue()));
            stanceGroup.add(new DataList(STANCE_LIST, stanceName.trim(), "Rank " + rank));
        }
        ensureCurrentStanceSelection(stanceGroup);
    }

    private void ensureCurrentStanceSelection(List<DataList> stanceGroup) {
        if (stanceGroup == null || reminderSelections == null) return;
        String existingSelection = getReminderSelection(CURRENT_STANCE_REMINDER_KEY);
        if (existingSelection != null && !existingSelection.isBlank()) {
            if (NO_ACTIVE_STANCE.equalsIgnoreCase(existingSelection.trim())) {
                return;
            }
            for (DataList entry : stanceGroup) {
                if (entry == null || entry.getName() == null) continue;
                if (existingSelection.equalsIgnoreCase(entry.getName().trim())) {
                    return;
                }
            }
        }
        setReminderSelection(CURRENT_STANCE_REMINDER_KEY, NO_ACTIVE_STANCE);
    }

    private void collectStanceRanks(DataSpecialty specialty, Map<String, Integer> stanceRanks) {
        if (specialty == null || specialty.getName() == null || stanceRanks == null) return;
        if (!STANCE_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) return;
        String stanceName = extractSpecialtyChoiceValue(specialty.getRefName());
        if (stanceName == null || stanceName.isBlank()) return;
        stanceRanks.merge(stanceName.trim(), 1, Integer::sum);
    }

    public int getStanceRank(String stanceName) {
        if (stanceName == null || stanceName.isBlank() || Lists == null) return 0;
        for (List<DataList> group : Lists) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!STANCE_LIST.equalsIgnoreCase(entry.getList().trim())) continue;
                if (!stanceName.equalsIgnoreCase(entry.getName().trim())) continue;
                return parseRankValue(entry.getDescription());
            }
        }
        return 0;
    }

    private int parseRankValue(String description) {
        if (description == null || description.isBlank()) return 0;
        String trimmed = description.trim();
        if (trimmed.regionMatches(true, 0, "Rank ", 0, 5)) {
            try {
                return Math.max(0, Integer.parseInt(trimmed.substring(5).trim()));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private String resolveInitialArcherFavoredEnemyChoice() {
        if (identity == null || identity.getCharClass() == null || !identity.getCharClass().equalsIgnoreCase("Archer")) {
            return "";
        }
        List<String> picks = identity.getCharClassPick();
        if (picks == null || picks.isEmpty()) return "";
        String firstPick = picks.get(0) == null ? "" : picks.get(0).trim();
        String secondPick = picks.size() > 1 && picks.get(1) != null ? picks.get(1).trim() : "";
        if (!firstPick.isBlank() && !"Terrain".equalsIgnoreCase(firstPick)) {
            return "Enemy".equalsIgnoreCase(firstPick) ? secondPick : firstPick;
        }
        return "";
    }

    private String resolveInitialArcherFavoredTerrainChoice() {
        if (identity == null || identity.getCharClass() == null || !identity.getCharClass().equalsIgnoreCase("Archer")) {
            return "";
        }
        List<String> picks = identity.getCharClassPick();
        if (picks == null || picks.size() < 2) return "";
        String firstPick = picks.get(0) == null ? "" : picks.get(0).trim();
        String secondPick = picks.get(1) == null ? "" : picks.get(1).trim();
        if ("Terrain".equalsIgnoreCase(firstPick)) {
            return secondPick;
        }
        if (!secondPick.isBlank() && !"Enemy".equalsIgnoreCase(firstPick) && !"Terrain".equalsIgnoreCase(firstPick)) {
            return secondPick;
        }
        return "";
    }

    private void migrateFavoredSpecialtyRefNames(String specialtyName, String listName) {
        if (specialtyName == null || specialtyName.isBlank() || listName == null || listName.isBlank() || specials == null) return;
        List<DataList> group = findOrCreateListGroup(listName);
        migrateFavoredSpecialtyRefName(specials.getRacialSpecialty(), specialtyName, group, listName);
        for (DataSpecialty specialty : specials.getClassSpecialties()) {
            migrateFavoredSpecialtyRefName(specialty, specialtyName, group, listName);
        }
        for (DataSpecialty specialty : specials.getTrainedSpecialties()) {
            migrateFavoredSpecialtyRefName(specialty, specialtyName, group, listName);
        }
    }

    private void migrateFavoredSpecialtyRefName(DataSpecialty specialty, String specialtyName, List<DataList> group, String listName) {
        if (specialty == null || specialty.getName() == null || !specialtyName.equalsIgnoreCase(specialty.getName().trim())) return;
        String refName = specialty.getRefName();
        if (refName != null && !refName.isBlank()) {
            addListEntryIfMissing(group, listName, refName.trim());
            specialty.setRefName("");
        }
    }

    private List<DataList> findOrCreateListGroup(String listName) {
        if (Lists == null) {
            Lists = new ArrayList<>();
        }
        if (listName == null || listName.isBlank()) {
            return new ArrayList<>();
        }
        for (List<DataList> group : Lists) {
            if (group == null || group.isEmpty()) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null) continue;
                if (listName.equalsIgnoreCase(entry.getList().trim())) {
                    return group;
                }
            }
        }

        List<DataList> group = new ArrayList<>();
        Lists.add(group);
        return group;
    }

    public static String buildMoldEntryDescription(String category, String type, String slot) {
        return "CATEGORY=" + safeStatic(category)
                + "|TYPE=" + safeStatic(type)
                + "|SLOT=" + safeStatic(slot);
    }

    private static String safeStatic(String value) {
        return value == null ? "" : value.trim();
    }

    private void addListEntryIfMissing(List<DataList> group, String listName, String entryName) {
        addListEntryIfMissing(group, listName, entryName, "");
    }

    private void addListEntryIfMissing(List<DataList> group, String listName, String entryName, String description) {
        if (group == null || listName == null || listName.isBlank() || entryName == null || entryName.isBlank()) return;
        for (DataList existing : group) {
            if (existing == null) continue;
            String existingList = existing.getList();
            String existingName = existing.getName();
            if (existingList != null && existingName != null
                    && listName.equalsIgnoreCase(existingList.trim())
                    && entryName.equalsIgnoreCase(existingName.trim())) {
                if ((existing.getDescription() == null || existing.getDescription().isBlank())
                        && description != null && !description.isBlank()) {
                    existing.setDescription(description);
                }
                return;
            }
        }
        group.add(new DataList(listName, entryName, safeTrim(description)));
    }

    private void removeListGroup(String listName) {
        if (Lists == null || listName == null || listName.isBlank()) return;
        Lists.removeIf(group -> {
            if (group == null || group.isEmpty()) return false;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null) continue;
                if (listName.equalsIgnoreCase(entry.getList().trim())) {
                    return true;
                }
            }
            return false;
        });
    }

    private int countNamedListEntries(String listName, String... ignoredNames) {
        if (Lists == null || listName == null || listName.isBlank()) return 0;
        ArrayList<String> ignored = new ArrayList<>();
        if (ignoredNames != null) {
            for (String ignoredName : ignoredNames) {
                if (ignoredName != null && !ignoredName.isBlank()) {
                    ignored.add(ignoredName.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        int count = 0;
        for (List<DataList> group : Lists) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
                String entryName = entry.getName().trim();
                if (entryName.isBlank()) continue;
                if (ignored.contains(entryName.toLowerCase(Locale.ROOT))) continue;
                count++;
            }
        }
        return count;
    }

    private void applyShifterFormAttributeSwap() {
        if (attributes == null) return;
        clearShifterFormAttributeSwapStatuses();
        clearShifterFormBonusStatuses();
        if (!isShifterClass()) return;

        String meleeAttribute = normalizeShifterAttributeKey(getStoredClassChoiceValue("Melee Affinity"));
        String rangedAttribute = normalizeShifterAttributeKey(getStoredClassChoiceValue("Ranged Affinity"));
        String selectedFormName = resolveSelectedShifterFormName();
        String selectedFormAttribute = resolveSelectedShifterFormAttribute();
        if (meleeAttribute == null || rangedAttribute == null || selectedFormAttribute == null) return;
        if (!selectedFormAttribute.equalsIgnoreCase(meleeAttribute)
                && !selectedFormAttribute.equalsIgnoreCase(rangedAttribute)) {
            return;
        }

        double meleeValue = attributes.calcStatusValue(meleeAttribute);
        double rangedValue = attributes.calcStatusValue(rangedAttribute);
        double highValue = Math.max(meleeValue, rangedValue);
        double lowValue = Math.min(meleeValue, rangedValue);

        if (selectedFormAttribute.equalsIgnoreCase(meleeAttribute)) {
            applyShifterFormSwapSeverity(meleeAttribute, highValue - meleeValue);
            applyShifterFormSwapSeverity(rangedAttribute, lowValue - rangedValue);
        } else {
            applyShifterFormSwapSeverity(rangedAttribute, highValue - rangedValue);
            applyShifterFormSwapSeverity(meleeAttribute, lowValue - meleeValue);
        }
        applyShifterFormBonuses(selectedFormName);
    }

    private void clearShifterFormAttributeSwapStatuses() {
        String[] attributeKeys = CharAttributes.getAttributeKeys();
        for (String attributeKey : attributeKeys) {
            if (attributeKey == null || attributeKey.isBlank()) continue;
            attributes.removeStatus(SHIFTER_FORM_SWAP_STATUS_PREFIX + attributeKey, "BATTRIBUTES");
        }
    }

    private void clearShifterFormBonusStatuses() {
        String[] combatKeys = { "ATK", "MOVE", "APP", "RANGE" };
        for (String combatKey : combatKeys) {
            attributes.removeStatus(SHIFTER_FORM_BONUS_STATUS_PREFIX + combatKey, "BCOMBAT");
        }
    }

    private void applyShifterFormSwapSeverity(String attributeKey, double severity) {
        if (attributeKey == null || attributeKey.isBlank()) return;
        DataStatus status = new DataStatus();
        status.setName(SHIFTER_FORM_SWAP_STATUS_PREFIX + attributeKey);
        status.setAttribute("B" + attributeKey.toUpperCase(Locale.ROOT));
        status.setDurationType("Passive");
        status.setSeverity(severity);
        status.setAffinity("None");
        status.setDescription("Derived from current Shifter form");
        attributes.addStatus(status);
    }

    private void applyShifterFormBonuses(String selectedFormName) {
        if (selectedFormName == null || selectedFormName.isBlank() || identity == null) return;
        int level = Math.max(0, identity.getLevel());
        String normalizedForm = selectedFormName.trim().toLowerCase(Locale.ROOT);
        if (normalizedForm.startsWith("heavy")) {
            addShifterFormCombatBonus("ATK", level, "Heavy form attack bonus");
            addShifterFormCombatBonus("MOVE", level * 2.5, "Heavy form movement bonus");
            return;
        }
        if (normalizedForm.startsWith("light")) {
            addShifterFormCombatBonus("APP", level, "Light form apply bonus");
            addShifterFormCombatBonus("RANGE", level * 2.5, "Light form range bonus");
        }
    }

    private void addShifterFormCombatBonus(String attributeKey, double severity, String description) {
        if (attributeKey == null || attributeKey.isBlank() || severity == 0.0) return;
        DataStatus status = new DataStatus();
        status.setName(SHIFTER_FORM_BONUS_STATUS_PREFIX + attributeKey);
        status.setAttribute("B" + attributeKey.toUpperCase(Locale.ROOT));
        status.setDurationType("Passive");
        status.setSeverity(severity);
        status.setAffinity("None");
        status.setDescription(description);
        attributes.addStatus(status);
    }

    private boolean isShifterClass() {
        return identity != null
                && identity.getCharClass() != null
                && identity.getCharClass().equalsIgnoreCase("Shifter");
    }

    private String resolveSelectedShifterFormAttribute() {
        String selectedForm = resolveSelectedShifterFormName();
        if (selectedForm == null || selectedForm.isBlank()) return null;

        int openParen = selectedForm.lastIndexOf('(');
        int closeParen = selectedForm.lastIndexOf(')');
        if (openParen < 0 || closeParen <= openParen + 1) return null;
        return normalizeShifterAttributeKey(selectedForm.substring(openParen + 1, closeParen));
    }

    private String resolveSelectedShifterFormName() {
        String selectedForm = getReminderSelection(CURRENT_FORM_REMINDER_KEY);
        if (selectedForm == null || selectedForm.isBlank()) {
            selectedForm = getFirstListEntryName(FORMS_LIST);
            if (selectedForm != null && !selectedForm.isBlank()) {
                setReminderSelection(CURRENT_FORM_REMINDER_KEY, selectedForm);
            }
        }
        return selectedForm;
    }

    private String getFirstListEntryName(String listName) {
        if (Lists == null || listName == null || listName.isBlank()) return null;
        for (List<DataList> group : Lists) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
                String entryName = entry.getName().trim();
                if (!entryName.isBlank()) return entryName;
            }
        }
        return null;
    }

    private String normalizeShifterAttributeKey(String attributeKey) {
        if (attributeKey == null || attributeKey.isBlank()) return null;
        String normalized = attributeKey.trim().toUpperCase(Locale.ROOT);
        for (String key : CharAttributes.getAttributeKeys()) {
            if (key.equalsIgnoreCase(normalized)) return key;
        }
        return null;
    }

    private void normalizeAlteriLists() {
        if (identity == null || !ALTERI_RACE.equalsIgnoreCase(identity.getRace()) || Lists == null) return;
        for (List<DataList> group : Lists) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null) continue;
                if ("Shapeshifts".equalsIgnoreCase(entry.getList().trim())
                        || ALTERI_SHAPESHIFT_LIST.equalsIgnoreCase(entry.getList().trim())) {
                    entry.setList(ALTERI_SHAPESHIFT_LIST);
                }
            }
        }
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

    @JsonIgnore
    public boolean hasAuraProficiencySpecialty() {
        return getAuraProficiencySpecialtyRank() > 0;
    }

    @JsonIgnore
    public int getAuraProficiencySpecialtyRank() {
        if (specials == null) return 0;
        int rank = 0;
        if (specials.hasSpecialty(AURA_PROFICIENCY_SPECIALTY)) {
            rank++;
        }
        if (specials.hasSpecialty(ASTRAL_PROFICIENCY_SPECIALTY)) {
            rank++;
        }
        if (specials.hasSpecialty(ASTRAL_PROFICIENCY_II_SPECIALTY)) {
            rank++;
        }
        return rank;
    }

    @JsonIgnore
    public double getAuraProficiencyBonusMultiplier() {
        return 1.0 + (0.05 * getAuraProficiencySpecialtyRank());
    }

    @JsonIgnore
    public double getAuraTrainingXpGainMultiplier() {
        return getAuraProficiencyBonusMultiplier();
    }

    @JsonIgnore
    public int getAuraTypeSynergy(String affinity) {
        if (training == null) return 0;
        String normalizedAffinity = normalizeAuraAffinityKey(affinity);
        if (normalizedAffinity == null) return 0;

        int total = 0;
        for (DataTraining tech : training.getAllTraining()) {
            if (tech == null || tech.isListEntry()) continue;
            String techAffinity = normalizeAuraAffinityKey(tech.getAffinity());
            if (techAffinity == null || !normalizedAffinity.equalsIgnoreCase(techAffinity)) continue;
            total += Math.max(0, tech.getRank());
        }
        return Math.max(0, total);
    }

    @JsonIgnore
    public double getEffectiveTechniqueAl(String affinity, int rawAl) {
        double safeAl = Math.max(0, rawAl);
        if (safeAl <= 0.0) return 0.0;
        int synergy = getAuraTypeSynergy(affinity);
        return safeAl * (1.0 + (0.001 * synergy));
    }

    @JsonIgnore
    public double getEffectiveTechniqueAl(DataTraining tech) {
        if (tech == null) return 0.0;
        return getEffectiveTechniqueAl(tech.getAffinity(), tech.getAl());
    }

    @JsonIgnore
    public double getEffectiveTechniqueAl(DataAction action) {
        if (action == null) return 0.0;
        String affinity = action.getAffinity();
        if ((affinity == null || affinity.isBlank() || "None".equalsIgnoreCase(affinity.trim()))
                && training != null) {
            DataTraining byId = training.getTrainingById(action.getId());
            if (byId != null) {
                affinity = byId.getAffinity();
            } else {
                DataTraining byName = training.getTrainingByName(action.getName());
                if (byName != null) {
                    affinity = byName.getAffinity();
                }
            }
        }
        return getEffectiveTechniqueAl(affinity, action.getAl());
    }

    private String normalizeAuraAffinityKey(String affinity) {
        if (affinity == null || affinity.isBlank()) return null;
        String normalized = affinity.trim().toUpperCase(Locale.ROOT);
        return "NONE".equals(normalized) ? null : normalized;
    }

    @JsonIgnore
    public boolean hasEquipmentEvocationSpecialty() {
        return specials != null && specials.hasSpecialty(EQUIPMENT_EVOCATION_SPECIALTY);
    }

    @JsonIgnore
    public boolean hasEnhancedEngineeringSpecialty() {
        return specials != null && specials.hasSpecialty("Enhanced Engineering");
    }

    /** Rebuilds equipment passive statuses from currently equipped inventory items. */
    public void refreshEquipmentPassiveBonuses() {
        applyEquipmentPassiveBonuses();
    }

    private void syncEnhancedEngineeringTrainingBonus() {
        if (enhancedEngineeringRankGranted || !hasEnhancedEngineeringSpecialty() || training == null) {
            return;
        }

        DataTraining auraEngineering = training.getTrainingByName("Aura Engineering");
        if (auraEngineering == null) {
            DataTraining template = findRuleTrainingByName("Aura Engineering");
            if (template != null) {
                auraEngineering = new DataTraining(template);
                auraEngineering.setRank(Math.max(0, auraEngineering.getRank()));
                auraEngineering.setExp(Math.max(0.0, auraEngineering.getExp()));
                training.addTraining(auraEngineering);
            }
        }
        if (auraEngineering == null) {
            return;
        }

        auraEngineering.setRank(Math.max(0, auraEngineering.getRank()) + 1);
        enhancedEngineeringRankGranted = true;
    }

    private DataTraining findRuleTrainingByName(String trainingName) {
        if (trainingName == null || trainingName.isBlank()) return null;
        StoreRuleManager ruleManager = new StoreRuleManager();
        for (DataTraining template : ruleManager.getTrainingData()) {
            if (template == null || template.getName() == null) continue;
            if (trainingName.equalsIgnoreCase(template.getName().trim())) {
                return template;
            }
        }
        return null;
    }

    /** Rebuilds passive specialty statuses from the currently owned specialties. */
    public void refreshSpecialtyPassiveBonuses() {
        applySpecialtyPassiveStatuses(new StoreRuleManager());
    }

    private void applyDomainEmanationToggleEffects() {
        if (attributes == null) return;
        clearStatusPrefix(attributes.getMDamage(), DOMAIN_EMANATION_PREFIX);
        if (!isDomainEmanationEnabled() || !hasPaladinDomainEmanationAccess()) {
            return;
        }

        addDomainEmanationStatus("MBDMG", -0.5, "Domain Emanation halves the paladin's base damage multiplier while active.");
        addDomainEmanationStatus("MTDMG", -0.5, "Domain Emanation halves the paladin's technique damage multiplier while active.");
        addDomainEmanationStatus("MBHEAL", -0.5, "Domain Emanation halves the paladin's base healing multiplier while active.");
        addDomainEmanationStatus("MTHEAL", -0.5, "Domain Emanation halves the paladin's technique healing multiplier while active.");
    }

    private void addDomainEmanationStatus(String attribute, double severity, String description) {
        if (attributes == null || attribute == null || attribute.isBlank()) return;
        DataStatus status = new DataStatus();
        status.setName(DOMAIN_EMANATION_PREFIX + attribute);
        status.setAttribute(attribute);
        status.setDurationType("Passive");
        status.setSeverity(severity);
        status.setAffinity("None");
        status.setDescription(description);
        attributes.addStatus(status);
    }

    private void syncDomainEmanationReminderSelection() {
        if (reminderSelections == null) {
            reminderSelections = new LinkedHashMap<>();
        }
        reminderSelections.put(DOMAIN_EMANATION_REMINDER_KEY, domainEmanationEnabled ? "On" : "Off");
    }

    private void restoreMaintainedTechniqueActiveLevelsFromSelections() {
        if (training == null || reminderSelections == null || reminderSelections.isEmpty()) return;

        String activeLoadout = getReminderSelection(MAINTAINED_ACTIVE_LOADOUT_KEY);
        String normalizedLoadout = sanitizeMaintainedSelectionSegment(
                activeLoadout == null || activeLoadout.isBlank() ? DEFAULT_MAINTAINED_LOADOUT.toLowerCase(Locale.ROOT)
                        : activeLoadout.trim().toLowerCase(Locale.ROOT));
        String keyPrefix = MAINTAINED_LOADOUT_PREFIX + normalizedLoadout + "." + MAINTAINED_ROW_PREFIX;

        Map<Integer, Integer> maxActiveLevelsByTechnique = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : reminderSelections.entrySet()) {
            if (entry == null) continue;
            String key = entry.getKey();
            if (key == null || !key.startsWith(keyPrefix)) continue;

            String remainder = key.substring(keyPrefix.length());
            int separatorIndex = remainder.indexOf('.');
            if (separatorIndex <= 0) continue;

            int trainingId;
            try {
                trainingId = Integer.parseInt(remainder.substring(0, separatorIndex));
            } catch (NumberFormatException ignored) {
                continue;
            }

            int activeLevel = 0;
            try {
                String rawValue = entry.getValue();
                activeLevel = rawValue == null ? 0 : Math.max(0, Integer.parseInt(rawValue.trim()));
            } catch (NumberFormatException ignored) {
                activeLevel = 0;
            }
            maxActiveLevelsByTechnique.merge(trainingId, activeLevel, Math::max);
        }

        if (maxActiveLevelsByTechnique.isEmpty()) return;
        for (DataTraining tech : training.getAllTraining()) {
            if (tech == null || !"Maintained".equalsIgnoreCase(tech.getType())) continue;
            Integer restoredAl = maxActiveLevelsByTechnique.get(tech.getId());
            if (restoredAl != null) {
                tech.setAl(Math.max(0, restoredAl));
            }
        }
    }

    private String sanitizeMaintainedSelectionSegment(String raw) {
        if (raw == null) return "";
        StringBuilder safe = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') {
                safe.append(c);
            } else {
                safe.append('_');
            }
        }
        return safe.toString();
    }

    public void setLists(List<List<DataList>> lists) { this.Lists = (lists == null) ? new ArrayList<>() : lists; }
    public Map<String, String> getReminderSelections() { return reminderSelections; }
    public void setReminderSelections(Map<String, String> reminderSelections) {
        this.reminderSelections = (reminderSelections == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(reminderSelections);
        String selection = getReminderSelection(DOMAIN_EMANATION_REMINDER_KEY);
        if (selection != null && !selection.isBlank()) {
            this.domainEmanationEnabled = "On".equalsIgnoreCase(selection);
        } else {
            syncDomainEmanationReminderSelection();
        }
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
        if (DOMAIN_EMANATION_REMINDER_KEY.equalsIgnoreCase(key)) {
            domainEmanationEnabled = "On".equalsIgnoreCase(value);
        }
    }
    public boolean isDomainEmanationEnabled() {
        return domainEmanationEnabled;
    }
    public void setDomainEmanationEnabled(boolean domainEmanationEnabled) {
        this.domainEmanationEnabled = domainEmanationEnabled;
        syncDomainEmanationReminderSelection();
    }
    @JsonIgnore
    public boolean hasPaladinDomainEmanationAccess() {
        if (identity == null || specials == null) return false;
        if (!"Paladin".equalsIgnoreCase(identity.getCharClass())) return false;
        return specials.hasSpecialty(HOLY_DOMAIN_SPECIALTY) || specials.hasSpecialty(DOMAIN_EMANATION_SPECIALTY);
    }
    public String getPanelReminder()     { return panelReminder; }
    public void setPanelReminder(String panelReminder) { this.panelReminder = panelReminder == null ? "" : panelReminder; }
    private void resetReminderBuckets() {
        raceReminderLines = new ArrayList<>();
        classReminderLines = new ArrayList<>();
        otherSpecialtyReminderLines = new ArrayList<>();
        otherReminderLines = new ArrayList<>();
    }

    private void flushReminderBuckets() {
        appendReminderBucket(raceReminderLines);
        appendReminderBucket(classReminderLines);
        appendReminderBucket(otherSpecialtyReminderLines);
        appendReminderBucket(otherReminderLines);
    }

    private void appendReminderBucket(List<String> bucket) {
        if (bucket == null) return;
        for (String line : bucket) {
            appendPanelReminderLine(line);
        }
    }

    private void appendReminderLine(List<String> bucket, String reminderLine) {
        if (bucket == null) return;
        String normalizedReminder = normalizeReminderText(reminderLine);
        if (normalizedReminder.isBlank()) return;
        String[] reminderLines = normalizedReminder.split("\n");
        for (String rawLine : reminderLines) {
            if (rawLine == null) continue;
            String line = rawLine.trim();
            if (!line.isBlank()) {
                bucket.add(line);
            }
        }
    }

    public void appendPanelReminderLine(String reminderLine) {
        String normalizedReminder = normalizeReminderText(reminderLine);
        if (normalizedReminder.isBlank()) return;
        String[] reminderLines = normalizedReminder.split("\n");
        for (String rawLine : reminderLines) {
            if (rawLine == null) continue;
            String line = rawLine.trim();
            if (line.isBlank()) continue;
            if (panelReminder == null || panelReminder.isBlank()) {
                panelReminder = line;
            } else {
                panelReminder += "\n" + line;
            }
        }
    }

    public static String normalizeReminderText(String reminderText) {
        if (reminderText == null || reminderText.isBlank()) {
            return "";
        }
        return reminderText
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?is)<style[^>]*>.*?</style>", "")
                .replaceAll("(?i)</?(html|center|div)>", "")
                .trim();
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
        boolean equipmentEvocation = hasEquipmentEvocationSpecialty();

        int idx = 0;
        for (DataItemEquipment item : equippedItems) {
            if (item == null || !item.isEquipped()) continue;
            String itemName = (item.getIname() != null && !item.getIname().isBlank())
                    ? item.getIname().trim()
                    : ((item.getDname() == null || item.getDname().isBlank()) ? "Item" : item.getDname().trim());
            String statusName = EQUIP_PASSIVE_PREFIX + itemName + " #" + idx;
            idx++;

            String bonusAtt = item.getBonusAtt();
            boolean suppressArmorBonus = equipmentEvocation && isEquipmentEvocationArmorItem(item);
            if (isShifterSpecialArmorItem(item)) {
                double severity = resolveShifterSpecialArmorBonus(item);
                if (!suppressArmorBonus && severity != 0.0) {
                    applyEquipmentBonusStatus("ARMOR", severity, statusName);
                }
            } else if (bonusAtt != null && !bonusAtt.isBlank() && !"NONE".equalsIgnoreCase(bonusAtt.trim())) {
                double severity = item.getBonusAmount();
                String normalizedBonus = bonusAtt.trim().toUpperCase();
                if (!suppressArmorBonus || !"ARMOR".equalsIgnoreCase(normalizedBonus)) {
                    applyEquipmentBonusStatus(normalizedBonus, severity, statusName);
                }
            }
            applyMoldingManifestEquipmentBonus(item, statusName + " (Molding)", !suppressArmorBonus);
        }

        if (equipmentEvocation) {
            applyEquipmentEvocationArmorBonuses();
        }
    }

    private void applyEquipmentEvocationArmorBonuses() {
        if (inventory == null) return;
        Map<String, Double> bestArmorBySlot = new LinkedHashMap<>();
        for (DataItemEquipment item : inventory.getEquipment()) {
            if (item == null || !isEquipmentEvocationArmorItem(item)) continue;
            String slot = resolveEquipmentEvocationArmorSlot(item);
            if (slot.isBlank()) continue;
            double severity = resolveEquipmentEvocationArmorSeverity(item);
            if (severity <= 0.0) continue;
            double existing = bestArmorBySlot.getOrDefault(slot, Double.NEGATIVE_INFINITY);
            if (severity > existing) {
                bestArmorBySlot.put(slot, severity);
            }
        }
        for (Map.Entry<String, Double> entry : bestArmorBySlot.entrySet()) {
            if (entry == null || entry.getValue() == null || entry.getValue() <= 0.0) continue;
            applyEquipmentBonusStatus("ARMOR", entry.getValue(),
                    EQUIP_PASSIVE_PREFIX + "Equipment Evocation " + entry.getKey());
        }
    }

    private boolean isEquipmentEvocationArmorItem(DataItemEquipment item) {
        if (item == null) return false;
        if (isShifterSpecialArmorItem(item)) return true;
        String category = safeTrim(item.getCategory());
        return "Armor".equalsIgnoreCase(category) || "Matrix".equalsIgnoreCase(category);
    }

    private String resolveEquipmentEvocationArmorSlot(DataItemEquipment item) {
        if (item == null) return "";
        String shifterSlot = resolveShifterSpecialArmorSlot(item);
        if (shifterSlot != null && !shifterSlot.isBlank()) {
            return shifterSlot;
        }
        return safeTrim(item.getSlot());
    }

    private double resolveEquipmentEvocationArmorSeverity(DataItemEquipment item) {
        if (item == null) return 0.0;
        double total = 0.0;
        if (isShifterSpecialArmorItem(item)) {
            total += resolveShifterSpecialArmorBonus(item);
        } else if ("ARMOR".equalsIgnoreCase(safeTrim(item.getBonusAtt()))) {
            total += item.getBonusAmount();
        }
        MoldingEquipmentBonus moldingBonus = parseMoldingManifestBonus(item.getInote());
        if (moldingBonus != null && "ARMOR".equalsIgnoreCase(moldingBonus.bonusKey())) {
            total += moldingBonus.amount();
        }
        return total;
    }

    private boolean isShifterSpecialArmorItem(DataItemEquipment item) {
        if (item == null) return false;
        return resolveShifterSpecialArmorSlot(item) != null;
    }

    private double resolveShifterSpecialArmorBonus(DataItemEquipment item) {
        if (item == null) return 0.0;
        String slot = resolveShifterSpecialArmorSlot(item);
        String armorType = resolveCurrentShifterArmorType();
        if (slot == null || armorType == null) return 0.0;

        int targetTier = Math.max(0, item.getTier());
        StoreRuleManager ruleManager = new StoreRuleManager();
        double bestExact = Double.NaN;
        double bestFallback = Double.NaN;
        int bestFallbackDistance = Integer.MAX_VALUE;
        for (DataItemEquipment template : ruleManager.getItemEquipmentData()) {
            if (template == null) continue;
            if (!"Armor".equalsIgnoreCase(safeTrim(template.getCategory()))) continue;
            if (!slot.equalsIgnoreCase(safeTrim(template.getSlot()))) continue;
            if (!armorType.equalsIgnoreCase(safeTrim(template.getType()))) continue;
            if (!"ARMOR".equalsIgnoreCase(safeTrim(template.getBonusAtt()))) continue;

            double bonusAmount = template.getBonusAmount();
            int distance = Math.abs(template.getTier() - targetTier);
            if (distance == 0) {
                if (Double.isNaN(bestExact) || bonusAmount > bestExact) {
                    bestExact = bonusAmount;
                }
            } else if (distance < bestFallbackDistance || (distance == bestFallbackDistance && (Double.isNaN(bestFallback) || bonusAmount > bestFallback))) {
                bestFallback = bonusAmount;
                bestFallbackDistance = distance;
            }
        }
        return !Double.isNaN(bestExact) ? bestExact : (Double.isNaN(bestFallback) ? 0.0 : bestFallback);
    }

    private String resolveCurrentShifterArmorType() {
        String selectedForm = resolveSelectedShifterFormName();
        if (selectedForm == null || selectedForm.isBlank()) return null;
        String normalized = selectedForm.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("heavy")) return "Heavy";
        if (normalized.startsWith("light")) return "Light";
        return null;
    }

    private String resolveShifterSpecialArmorSlot(DataItemEquipment item) {
        if (item == null) return null;
        String itemName = !safeTrim(item.getIname()).isBlank() ? item.getIname() : item.getDname();
        String moldName = normalizeShifterSpecialArmorName(itemName);
        if (!isShifterSpecialMoldName(moldName)) return null;
        return resolveShifterSpecialMoldSlot(moldName);
    }

    private String normalizeShifterSpecialArmorName(String name) {
        String trimmed = safeTrim(name);
        if (trimmed.regionMatches(true, 0, MOLDED_ITEM_PREFIX, 0, MOLDED_ITEM_PREFIX.length())) {
            return trimmed.substring(MOLDED_ITEM_PREFIX.length()).trim();
        }
        return trimmed;
    }

    /** Clears previously applied equipment passive statuses so updateAll can re-apply from current equip state. */
    private void clearEquipmentPassiveBonuses() {
        if (attributes != null) {
            clearStatusPrefix(attributes.getBAttributes(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMAttributes(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBDefense(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMDefense(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBResist(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMResist(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBCombat(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMCombat(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBSecondary(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMSecondary(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBDamage(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMDamage(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBSkill(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMSkill(), EQUIP_PASSIVE_PREFIX);
        }
        if (resources != null) {
            clearStatusPrefix(resources.getBaseHP(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiHP(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseAura(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiAura(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseResource1(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiResource1(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseResource2(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiResource2(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseResource3(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiResource3(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseAngelPoints(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiAngelPoints(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseReactions(), EQUIP_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiReactions(), EQUIP_PASSIVE_PREFIX);
        }
    }

    private void clearPrefixedStatuses(StatBlock[] blocks, String prefix) {
        if (blocks == null || prefix == null) return;
        for (StatBlock block : blocks) {
            if (block == null) continue;
            /*block.getStatus().removeIf(s -> s != null && s.getName() != null && s.getName().startsWith(prefix));
            block.getMulti().removeIf(s -> s != null && s.getName() != null && s.getName().startsWith(prefix));*/
        }
    }

    private void applySpecialtyPassiveStatuses(StoreRuleManager dq) {
        clearSpecialtyPassiveBonuses();
        if (dq == null || specials == null) return;

        List<DataSpecialty> all = specials.getAllSpecialties();
        if (all == null || all.isEmpty()) return;

        for (DataSpecialty specialty : all) {
            if (specialty == null || !isPassiveSpecialty(dq, specialty)) continue;
            List<DataStatus> perms = collectSpecialtyPermStatuses(dq, specialty);
            if (perms.isEmpty()) continue;
            for (DataStatus permStatus : perms) {
                applySpecialtyPermStatus(dq, specialty, permStatus);
            }
        }
    }

    private boolean isPassiveSpecialty(StoreRuleManager dq, DataSpecialty specialty) {
        if (specialty == null) return false;
        if (hasLegacyDerivedPassiveSpecialty(specialty)) return true;
        if (dq != null) {
            DataSpecialty base = null;
            if (specialty.getId() > 0) {
                base = dq.getSpecialtyById(specialty.getId());
            }
            if (base == null && specialty.getName() != null && !specialty.getName().isBlank()) {
                base = dq.getSpecialtyByName(specialty.getName());
            }
            if (base != null && base.getType() != null && !base.getType().isBlank()) {
                return "Passive".equalsIgnoreCase(base.getType());
            }
        }
        return "Passive".equalsIgnoreCase(specialty.getType());
    }

    private boolean hasLegacyDerivedPassiveSpecialty(DataSpecialty specialty) {
        return specialty != null
                && specialty.getName() != null
                && (DIVINE_GRACE_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())
                || DIVINE_ENLIGHTENMENT_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())
                || DIVINE_DEDICATION_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())
                || MARTIAL_FOCUS_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())
                || BALANCE_IN_ALL_THINGS_SPECIALTY.equalsIgnoreCase(specialty.getName().trim()));
    }

    private void clearSpecialtyPassiveBonuses() {
        if (attributes != null) {
            clearStatusPrefix(attributes.getBAttributes(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMAttributes(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBDefense(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMDefense(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBResist(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMResist(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBCombat(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMCombat(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBSecondary(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMSecondary(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBDamage(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMDamage(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getBSkill(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(attributes.getMSkill(), SPECIALTY_PASSIVE_PREFIX);
        }
        if (resources != null) {
            clearStatusPrefix(resources.getBaseHP(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiHP(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseAura(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiAura(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseResource1(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiResource1(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseResource2(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiResource2(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseResource3(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiResource3(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseAngelPoints(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiAngelPoints(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getBaseReactions(), SPECIALTY_PASSIVE_PREFIX);
            clearStatusPrefix(resources.getMultiReactions(), SPECIALTY_PASSIVE_PREFIX);
        }
    }

    private List<DataStatus> collectSpecialtyPermStatuses(StoreRuleManager dq, DataSpecialty specialty) {
        ArrayList<DataStatus> copies = new ArrayList<>();
        if (specialty == null) return copies;

        boolean foundRuleStatuses = false;
        if (dq != null && specialty.getId() > 0) {
            DataSpecialty base = dq.getSpecialtyById(specialty.getId());
            if (base != null) {
                for (DataStatus permStatus : base.getPermStatus()) {
                    if (permStatus != null) {
                        copies.add(new DataStatus(permStatus));
                        foundRuleStatuses = true;
                    }
                }
            }
        }

        if (!foundRuleStatuses) {
            for (DataStatus permStatus : specialty.getPermStatus()) {
                if (permStatus != null) copies.add(new DataStatus(permStatus));
            }
        }

        if (copies.isEmpty() && dq != null && specialty.getRefName() != null && !specialty.getRefName().isBlank()) {
            DataSpecialty referenced = dq.getSpecialtyByName(specialty.getRefName().trim());
            if (referenced != null) {
                for (DataStatus permStatus : referenced.getPermStatus()) {
                    if (permStatus != null) copies.add(new DataStatus(permStatus));
                }
            }
        }

        if (copies.isEmpty()) {
            copies.addAll(buildLegacyPassiveSpecialtyStatuses(specialty));
        }

        return copies;
    }

    private List<DataStatus> buildLegacyPassiveSpecialtyStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> legacyStatuses = new ArrayList<>();
        if (specialty == null || specialty.getName() == null) return legacyStatuses;

        if (DIVINE_VOW_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildDivineVowStatuses(specialty));
        } else if (DIVINE_GRACE_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildDivineGraceStatuses(specialty));
        } else if (DIVINE_ENLIGHTENMENT_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildDivineEnlightenmentStatuses(specialty));
        } else if (DIVINE_DEDICATION_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildDivineDedicationStatuses(specialty));
        } else if (BALANCE_IN_ALL_THINGS_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildBalanceInAllThingsStatuses(specialty));
        } else if (MARTIAL_FOCUS_SPECIALTY.equalsIgnoreCase(specialty.getName())
                || COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildCombatDisciplineOneStatuses(specialty));
        } else if (COMBAT_DISCIPLINE_II_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildCombatDisciplineTwoStatuses(specialty));
        } else if (STANCE_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildStanceStatuses(specialty));
        } else if (SNIPERS_DOMAIN_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildSnipersDomainStatuses(specialty));
        } else if (JAGGED_EDGES_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildJaggedEdgesStatuses(specialty));
        } else if (INDOMITABLE_PREDISPOSITION_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildIndomitablePredispositionStatuses(specialty));
        } else if (ESCAPE_ARTISAN_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildEscapeArtisanStatuses(specialty));
        } else if (EVASION_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildEvasionStatuses(specialty));
        } else if (ARMOR_DEPENDENCY_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildArmorDependencyStatuses(specialty));
        } else if (BOND_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildBondStatuses(specialty));
        } else if (MODULAR_FORM_SPECIALTY.equalsIgnoreCase(specialty.getName())) {
            legacyStatuses.addAll(buildModularFormStatuses(specialty));
        }

        // TODO: add explicit legacy mappings for passive specialties whose effects are still
        // only documented in description text instead of encoded as permStatus payloads.
        return legacyStatuses;
    }

    private List<DataStatus> buildDivineVowStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null) return statuses;

        DataVow vow = resolveSelectedVow(specialty);
        if (vow == null) return statuses;

        String vowEffect = vow.getVowEffect();
        if (vowEffect != null && !vowEffect.isBlank()) {
            DataStatus reminder = new DataStatus();
            reminder.setName("Divine Vow: " + vow.getName());
            reminder.setAttribute("REMINDER");
            reminder.setDurationType("Passive");
            reminder.setSeverity(0.0);
            reminder.setAffinity("None");
            reminder.setDescription(vowEffect.trim());
            statuses.add(reminder);
        }

        return statuses;
    }

    private List<DataStatus> buildDivineGraceStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        if (isDivineGraceSuppressed()) {
            statuses.add(buildLegacyReminderStatus(specialty, VOW_STATUS_REMINDER_KEY + "::" + VOW_STATUS_BROKEN + "|" + VOW_STATUS_INTACT));
            return statuses;
        }

        double severity = Math.max(0, identity.getLevel());
        statuses.add(buildLegacyPassiveStatus(specialty, "REF", severity, "Divine Grace level-based Reflex bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "FORT", severity, "Divine Grace level-based Fortitude bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "WILL", severity, "Divine Grace level-based Will bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "DEF", severity, "Divine Grace level-based Defense bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "ATK", severity, "Divine Grace level-based Attack bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "APPLY", severity, "Divine Grace level-based Apply bonus"));
        return statuses;
    }

    private List<DataStatus> buildDivineEnlightenmentStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null) return statuses;

        DataDeity deity = resolveSelectedDeity();
        if (deity == null) return statuses;

        addDivineEnlightenmentAttributeStatus(statuses, specialty, deity.getCoreAtt(), "deity core paragon");
        addDivineEnlightenmentAttributeStatus(statuses, specialty, deity.getCharAtt(), "deity character paragon");
        return statuses;
    }

    private List<DataStatus> buildDivineDedicationStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null) return statuses;

        String choice = extractSpecialtyChoiceValue(specialty.getRefName());
        if (DIVINE_DEDICATION_FAITHFUL.equalsIgnoreCase(choice)) {
            statuses.add(buildLegacyPassiveStatus(specialty, "MULTIR1", 1.0,
                    "Divine Dedication (Faithful) doubles Faith."));
        }

        return statuses;
    }

    private void addDivineEnlightenmentAttributeStatus(List<DataStatus> statuses, DataSpecialty specialty,
            String attributeKey, String descriptionSuffix) {
        if (statuses == null || specialty == null) return;
        String normalizedAttribute = normalizeCharacterAttributeKey(attributeKey);
        if (normalizedAttribute == null) return;
        statuses.add(buildLegacyPassiveStatus(specialty, normalizedAttribute, 1.0,
                "Divine Enlightenment bonus to " + descriptionSuffix + " attribute"));
    }

    private DataDeity resolveSelectedDeity() {
        if (identity == null || identity.getCharClassPick() == null || identity.getCharClassPick().isEmpty()) return null;
        String deityName = identity.getCharClassPick().get(0);
        if (deityName == null || deityName.isBlank()) return null;
        StoreRuleManager dq = new StoreRuleManager();
        return dq.getDeityByName(deityName.trim());
    }

    private String normalizeCharacterAttributeKey(String attributeKey) {
        if (attributeKey == null || attributeKey.isBlank()) return null;
        String normalized = attributeKey.trim().toUpperCase(Locale.ROOT);
        for (String key : CharAttributes.getAttributeKeys()) {
            if (key.equalsIgnoreCase(normalized)) return key;
        }
        return null;
    }

    @JsonIgnore
    public boolean hasDivineDedicationChoice(String choiceName) {
        if (choiceName == null || choiceName.isBlank() || specials == null) return false;
        for (DataSpecialty specialty : specials.getAllSpecialties()) {
            if (specialty == null || specialty.getName() == null) continue;
            if (!DIVINE_DEDICATION_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) continue;
            String storedChoice = extractSpecialtyChoiceValue(specialty.getRefName());
            if (choiceName.equalsIgnoreCase(storedChoice)) {
                return true;
            }
        }
        return false;
    }

    private List<DataStatus> buildBalanceInAllThingsStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null || attributes == null) return statuses;

        String primaryAttribute = resolveDomainPrimaryAttribute();
        if (primaryAttribute == null || primaryAttribute.isBlank() || "PRIM".equalsIgnoreCase(primaryAttribute)) {
            return statuses;
        }

        String[] attributeKeys = CharAttributes.getAttributeKeys();
        ArrayList<String> nonPrimaryAttributes = new ArrayList<>();
        double total = 0.0;
        for (String attributeKey : attributeKeys) {
            if (attributeKey == null || attributeKey.isBlank()) continue;
            if (primaryAttribute.equalsIgnoreCase(attributeKey.trim())) continue;
            nonPrimaryAttributes.add(attributeKey.trim().toUpperCase(Locale.ROOT));
            total += attributes.calcStatusValue(attributeKey);
        }

        if (nonPrimaryAttributes.size() != 11) {
            return statuses;
        }

        double average = total / nonPrimaryAttributes.size();
        ArrayList<String> failedAttributes = new ArrayList<>();
        for (String attributeKey : nonPrimaryAttributes) {
            double value = attributes.calcStatusValue(attributeKey);
            if (Math.abs(value - average) > 1.5) {
                failedAttributes.add(attributeKey);
            }
        }
        if (!failedAttributes.isEmpty()) {
            statuses.add(buildLegacyReminderStatus(specialty,
                    "Kenti: Out of Balance (" + String.join(", ", failedAttributes) + ")"));
            return statuses;
        }

        statuses.add(buildLegacyReminderStatus(specialty, "Kenti: In Balance"));
        double severity = Math.max(0, identity.getLevel()) / 2.0;
        for (String attributeKey : nonPrimaryAttributes) {
            statuses.add(buildLegacyPassiveStatus(specialty, attributeKey, severity,
                    "Balance in All Things level-based non-primary attribute bonus"));
        }
        return statuses;
    }

    private List<DataStatus> buildCombatDisciplineOneStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        String focus = extractSpecialtyChoiceValue(specialty.getRefName());
        if (focus.isBlank()) return statuses;

        double severity = Math.max(0, identity.getLevel());
        if (MARTIAL_FOCUS_MOBILITY.equalsIgnoreCase(focus)) {
            statuses.add(buildLegacyPassiveStatus(specialty, "MOVE", severity * 2.5, "Combat Discipline I (Mobility) level-based movement bonus"));
            statuses.add(buildLegacyReminderStatus(specialty, MARTIAL_FOCUS_MOBILITY_REMINDER));
        } else if (MARTIAL_FOCUS_AVOIDANCE.equalsIgnoreCase(focus)) {
            statuses.add(buildLegacyPassiveStatus(specialty, "REF", severity, "Combat Discipline I (Avoidance) level-based Reflex bonus"));
        } else if (MARTIAL_FOCUS_HARM.equalsIgnoreCase(focus) || MARTIAL_FOCUS_MARTIAL_LEGACY.equalsIgnoreCase(focus)) {
            statuses.add(buildLegacyPassiveStatus(specialty, "TDMG", Math.pow(severity, 1.5), "Combat Discipline I (Harm) level-based total damage bonus"));
        }

        return statuses;
    }

    private List<DataStatus> buildCombatDisciplineTwoStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        String focus = normalizeCombatDisciplineChoice(extractSpecialtyChoiceValue(specialty.getRefName()));
        if (focus.isBlank() || !isMatchingCombatDisciplineUpgrade(focus)) {
            return statuses;
        }

        double classLevel = Math.max(0, identity.getLevel());
        if (MARTIAL_FOCUS_MOBILITY.equalsIgnoreCase(focus)) {
            statuses.add(buildLegacyReminderStatus(specialty, COMBAT_DISCIPLINE_MOBILITY_II_REMINDER));
        } else if (MARTIAL_FOCUS_AVOIDANCE.equalsIgnoreCase(focus)) {
            statuses.add(buildLegacyPassiveStatus(specialty, "DEF", classLevel / 2.0,
                    "Combat Discipline II (Avoidance) class-level defense bonus"));
        } else if (MARTIAL_FOCUS_MARTIAL_LEGACY.equalsIgnoreCase(focus)) {
            statuses.add(buildLegacyPassiveStatus(specialty, "BDMG", classLevel,
                    "Combat Discipline II (Martial) class-level base damage bonus"));
        }

        return statuses;
    }

    private List<DataStatus> buildCombatDisciplineStatuses(DataSpecialty specialty) {
        return buildCombatDisciplineOneStatuses(specialty);
    }

    private List<DataStatus> buildSnipersDomainStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        double severity = Math.max(0, identity.getLevel()) * 2.5;
        statuses.add(buildLegacyPassiveStatus(specialty, "RANGE", severity, "Sniper's Domain level-based range bonus"));
        return statuses;
    }

    private List<DataStatus> buildJaggedEdgesStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || attributes == null) return statuses;

        double armorValue = Math.max(0.0, attributes.calcStatusValue("ARMOR"));
        statuses.add(buildLegacyPassiveStatus(specialty, "TDMG", armorValue / 2.0,
                "Jagged Edges armor-based total damage bonus"));
        return statuses;
    }

    private List<DataStatus> buildIndomitablePredispositionStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        double severity = Math.max(0, identity.getLevel());
        statuses.add(buildLegacyReminderStatus(specialty,
                "Indomitable Predisposition: +" + formatPassiveReminderNumber(severity)
                        + " to saves that would cause you to act against your principles or deity."));
        return statuses;
    }

    private List<DataStatus> buildBondStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null) return statuses;
        statuses.add(buildLegacyReminderStatus(specialty, BOND_REMINDER));
        return statuses;
    }

    private List<DataStatus> buildModularFormStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        int capacity = Math.max(0, identity.getLevel() / 4);
        int knownForms = countNamedListEntries(MODULAR_FORMS_LIST, MODULAR_FORMS_PLACEHOLDER);
        statuses.add(buildLegacyReminderStatus(specialty,
                "Modular Forms: " + knownForms + " / " + capacity
                        + ". Learning a new form requires 4 hours of training. You may abandon a form as a full round action to free space."));
        return statuses;
    }

    private List<DataStatus> buildEscapeArtisanStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null || attributes == null) return statuses;

        double dexValue = Math.max(0.0, attributes.calcStatusValue("DEX"));
        double severity = Math.min(Math.max(0, identity.getLevel()), dexValue);
        statuses.add(buildLegacyReminderStatus(specialty,
                "Escape Artisan: +" + formatPassiveReminderNumber(severity)
                        + " to saves against hard crowd control and direct INIT reduction."));
        return statuses;
    }

    private List<DataStatus> buildEvasionStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null) return statuses;
        statuses.add(buildLegacyReminderStatus(specialty,
                "Evasion: When you succeed on a save that would reduce damage, that damage is negated instead."));
        return statuses;
    }

    private List<DataStatus> buildStanceStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || attributes == null) return statuses;

        String stanceName = extractSpecialtyChoiceValue(specialty.getRefName());
        if (stanceName.isBlank() || !isPrimaryStanceSpecialtyInstance(specialty, stanceName)) {
            return statuses;
        }
        String primaryAttribute = resolveDomainPrimaryAttribute();
        if (primaryAttribute == null || primaryAttribute.isBlank() || "PRIM".equalsIgnoreCase(primaryAttribute)) {
            return statuses;
        }

        double primValue = Math.max(0.0, attributes.calcStatusValue(primaryAttribute));
        double rankMultiplier = getStanceRankMultiplier(stanceName);
        if (rankMultiplier <= 0.0) {
            return statuses;
        }
        if (PRECISION_STANCE_NAME.equalsIgnoreCase(stanceName)) {
            boolean active = isCurrentStanceSelected(PRECISION_STANCE_NAME);
            double ratio = active ? 0.25 : 0.10;
            statuses.add(buildLegacyPassiveStatus(specialty, "ATK", primValue * ratio * rankMultiplier,
                    active ? "Precision Stance active attack bonus" : "Precision Stance inactive attack bonus"));
        } else if (POWER_STANCE_NAME.equalsIgnoreCase(stanceName)) {
            boolean active = isCurrentStanceSelected(POWER_STANCE_NAME);
            double ratio = active ? 0.50 : 0.20;
            statuses.add(buildLegacyPassiveStatus(specialty, "BDMG", primValue * ratio * rankMultiplier,
                    active ? "Power Stance active base damage bonus" : "Power Stance inactive base damage bonus"));
        } else if (PROTECTION_STANCE_NAME.equalsIgnoreCase(stanceName)) {
            boolean active = isCurrentStanceSelected(PROTECTION_STANCE_NAME);
            double ratio = active ? 0.25 : 0.10;
            statuses.add(buildLegacyPassiveStatus(specialty, "DEF", primValue * ratio * rankMultiplier,
                    active ? "Protection Stance active defense bonus" : "Protection Stance inactive defense bonus"));
        } else if (PUNISHMENT_STANCE_NAME.equalsIgnoreCase(stanceName)) {
            boolean active = isCurrentStanceSelected(PUNISHMENT_STANCE_NAME);
            double ratio = active ? 0.25 : 0.10;
            statuses.add(buildLegacyPassiveStatus(specialty, "APPLY", primValue * ratio * rankMultiplier,
                    active ? "Punishment Stance active apply bonus" : "Punishment Stance inactive apply bonus"));
        }
        return statuses;
    }

    private List<DataStatus> buildArmorDependencyStatuses(DataSpecialty specialty) {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (specialty == null || identity == null) return statuses;

        double level = Math.max(0, identity.getLevel());
        double attributeBonus = level;
        double attackApplyBonus = 0.5 * level * level;
        double saveDamageBonus = level * level;
        double resistAllBonus = 2.0 * level * level;
        double subResistBonus = 0.5 * level * level;

        for (String attributeKey : CharAttributes.getAttributeKeys()) {
            statuses.add(buildLegacyPassiveStatus(specialty, attributeKey, attributeBonus,
                    "Armor Dependency level-based attribute bonus"));
        }

        statuses.add(buildLegacyPassiveStatus(specialty, "ATK", attackApplyBonus,
                "Armor Dependency level-based attack bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "APPLY", attackApplyBonus,
                "Armor Dependency level-based apply bonus"));

        statuses.add(buildLegacyPassiveStatus(specialty, "BDMG", saveDamageBonus,
                "Armor Dependency level-based base damage bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "FORT", saveDamageBonus,
                "Armor Dependency level-based Fortitude bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "REF", saveDamageBonus,
                "Armor Dependency level-based Reflex bonus"));
        statuses.add(buildLegacyPassiveStatus(specialty, "WILL", saveDamageBonus,
                "Armor Dependency level-based Will bonus"));

        statuses.add(buildLegacyPassiveStatus(specialty, "ALL", resistAllBonus,
                "Armor Dependency level-based Resist All bonus"));
        for (String resistKey : CharAttributes.getDamageTypeKeys()) {
            if ("ALL".equalsIgnoreCase(resistKey)) continue;
            statuses.add(buildLegacyPassiveStatus(specialty, resistKey, subResistBonus,
                    "Armor Dependency level-based specific resistance bonus"));
        }

        return statuses;
    }

    private String extractSpecialtyChoiceValue(String refName) {
        if (refName == null) return "";
        String trimmed = refName.trim();
        if (trimmed.isBlank()) return "";
        if (trimmed.contains("|")) return trimmed;

        int colonIndex = trimmed.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < trimmed.length()) {
            String suffix = trimmed.substring(colonIndex + 1).trim();
            if (!suffix.isBlank()) {
                return suffix;
            }
        }
        return trimmed;
    }

    private String normalizeCombatDisciplineChoice(String focus) {
        if (focus == null) return "";
        String trimmed = focus.trim();
        if (trimmed.isBlank()) return "";
        if (MARTIAL_FOCUS_HARM.equalsIgnoreCase(trimmed)) {
            return MARTIAL_FOCUS_MARTIAL_LEGACY;
        }
        return trimmed;
    }

    private boolean isMatchingCombatDisciplineUpgrade(String chosenFocus) {
        if (chosenFocus == null || chosenFocus.isBlank() || specials == null) return false;
        for (DataSpecialty specialty : specials.getAllSpecialties()) {
            if (specialty == null || specialty.getName() == null) continue;
            if (!COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) continue;
            String baseFocus = normalizeCombatDisciplineChoice(extractSpecialtyChoiceValue(specialty.getRefName()));
            return !baseFocus.isBlank() && baseFocus.equalsIgnoreCase(chosenFocus.trim());
        }
        return false;
    }

    private boolean matchesFeatureName(String featureName, String specialtyName) {
        if (featureName == null || specialtyName == null) return false;
        if (featureName.equalsIgnoreCase(specialtyName)) return true;
        return isCombatDisciplineAliasName(featureName) && isCombatDisciplineAliasName(specialtyName);
    }

    private boolean isCombatDisciplineAliasName(String name) {
        if (name == null || name.isBlank()) return false;
        String trimmed = name.trim();
        return MARTIAL_FOCUS_SPECIALTY.equalsIgnoreCase(trimmed)
                || COMBAT_DISCIPLINE_SPECIALTY.equalsIgnoreCase(trimmed)
                || COMBAT_DISCIPLINE_I_DISPLAY.equalsIgnoreCase(trimmed);
    }

    private boolean isCurrentStanceSelected(String stanceName) {
        if (stanceName == null || stanceName.isBlank()) return false;
        String selectedStance = resolveSelectedStanceName();
        return selectedStance != null && selectedStance.equalsIgnoreCase(stanceName.trim());
    }

    private boolean isDivineGraceSuppressed() {
        String vowStatus = getReminderSelection(VOW_STATUS_REMINDER_KEY);
        if (vowStatus == null || vowStatus.isBlank()) {
            setReminderSelection(VOW_STATUS_REMINDER_KEY, VOW_STATUS_INTACT);
            return false;
        }
        return VOW_STATUS_BROKEN.equalsIgnoreCase(vowStatus.trim());
    }

    private boolean isPrimaryStanceSpecialtyInstance(DataSpecialty specialty, String stanceName) {
        if (specialty == null || stanceName == null || stanceName.isBlank() || specials == null) return false;
        for (DataSpecialty candidate : specials.getAllSpecialties()) {
            if (candidate == null || candidate.getName() == null) continue;
            if (!STANCE_SPECIALTY.equalsIgnoreCase(candidate.getName().trim())) continue;
            String candidateStance = extractSpecialtyChoiceValue(candidate.getRefName());
            if (!stanceName.equalsIgnoreCase(candidateStance)) continue;
            return candidate == specialty;
        }
        return false;
    }

    private double getStanceRankMultiplier(String stanceName) {
        int rank = Math.max(0, Math.min(3, getStanceRank(stanceName)));
        return switch (rank) {
            case 1 -> 1.0;
            case 2 -> 2.5;
            case 3 -> 5.0;
            default -> 0.0;
        };
    }

    private String resolveSelectedStanceName() {
        String selectedStance = getReminderSelection(CURRENT_STANCE_REMINDER_KEY);
        if (selectedStance != null && NO_ACTIVE_STANCE.equalsIgnoreCase(selectedStance.trim())) {
            return "";
        }
        if (selectedStance != null && !selectedStance.isBlank() && getStanceRank(selectedStance) > 0) {
            return selectedStance.trim();
        }
        setReminderSelection(CURRENT_STANCE_REMINDER_KEY, NO_ACTIVE_STANCE);
        return "";
    }

    private DataStatus buildLegacyPassiveStatus(DataSpecialty specialty, String attribute, double severity, String description) {
        DataStatus status = new DataStatus();
        status.setName(specialty.getName());
        status.setAttribute(attribute);
        status.setDurationType("Passive");
        status.setSeverity(severity);
        status.setAffinity("None");
        status.setDescription(description);
        return status;
    }

    private DataStatus buildLegacyReminderStatus(DataSpecialty specialty, String description) {
        DataStatus status = new DataStatus();
        status.setName(specialty == null ? "" : specialty.getName());
        status.setAttribute("REMINDER");
        status.setDurationType("Passive");
        status.setSeverity(0.0);
        status.setAffinity("None");
        status.setDescription(description);
        return status;
    }

    private String formatPassiveReminderNumber(double value) {
        if (Math.abs(value - Math.rint(value)) <= 0.0001) {
            return Integer.toString((int)Math.round(value));
        }
        return String.format(Locale.ROOT, "%.3f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private DataVow resolveSelectedVow(DataSpecialty specialty) {
        if (specialty == null) return null;
        String vowName = specialty.getRefName();
        if (vowName == null || vowName.isBlank()) return null;
        return new StoreRuleManager().getVowByName(vowName.trim());
    }

    private void applySpecialtyPermStatus(StoreRuleManager dq, DataSpecialty specialty, DataStatus permStatus) {
        if (specialty == null || permStatus == null || permStatus.getAttribute() == null) return;
        if (isReminderStatus(permStatus)) {
            appendSpecialtyReminderStatus(specialty, permStatus);
            return;
        }

        String attr = permStatus.getAttribute().toUpperCase();
        String baseName = permStatus.getName() != null && !permStatus.getName().isBlank()
                ? permStatus.getName()
                : specialty.getName();
        String uniqueName = SPECIALTY_PASSIVE_PREFIX + baseName + " (S" + specialty.getId() + ")";
        String description = permStatus.getDescription() == null || permStatus.getDescription().isBlank()
                ? "Specialty passive bonus"
                : permStatus.getDescription();
        if (applySpecialtyResourceStatus(dq, uniqueName, attr, permStatus.getSeverity(), description)) {
            return;
        }
        if (applySpecialtyMultiplierAliases(uniqueName, attr, permStatus.getSeverity(), description)) {
            return;
        }

        String normalizedAttr = normalizeAttrKey(attr);
        String category = resolveCategory(normalizedAttr);
        if (category == null) return;

        DataStatus copy = new DataStatus(permStatus);
        copy.setName(uniqueName);
        copy.setAttribute("B" + normalizedAttr);
        copy.setDurationType("Passive");
        copy.setSeverity(permStatus.getSeverity());
        copy.setAffinity("None");
        copy.setDescription(description);
        attributes.addStatus(copy);
    }

    private boolean applySpecialtyResourceStatus(StoreRuleManager dq, String uniqueName, String attribute, double severity, String description) {
        String normalizedAttribute = attribute == null ? "" : attribute.trim().toUpperCase();
        String resourceAttribute = switch (normalizedAttribute) {
            case "BASEHPM", "MAXHP" -> "BASEHP";
            case "HPMULTI" -> "MULTIHP";
            case "BASEAURAM", "MAXAURA" -> "BASEAURA";
            case "AURAMULTI" -> "MULTIAURA";
            case "REACT" -> "BASEREACT";
            case "R1" -> "BASER1";
            case "R2" -> "BASER2";
            case "R3" -> "BASER3";
            case "MULTIR1" -> "MULTIR1";
            case "MULTIR2" -> "MULTIR2";
            case "MULTIR3" -> "MULTIR3";
            case "MULTIANGEL" -> "MULTIANGEL";
            case "MULTIREACT" -> "MULTIREACT";
            default -> null;
        };
        if (resourceAttribute == null) return false;

        double computedSeverity = severity;
        if ("BASEHPM".equals(normalizedAttribute)) {
            if (dq == null || identity == null) return false;
            DataLevel dataLevel = dq.getLevel(identity.getLevel());
            if (dataLevel == null) return false;
            computedSeverity = dataLevel.getBaseHP() * severity;
        } else if ("BASEAURAM".equals(normalizedAttribute)) {
            if (dq == null || identity == null) return false;
            DataLevel dataLevel = dq.getLevel(identity.getLevel());
            if (dataLevel == null) return false;
            computedSeverity = dataLevel.getBaseAura() * severity;
        }

        DataStatus copy = new DataStatus();
        copy.setName(uniqueName);
        copy.setAttribute(resourceAttribute);
        copy.setDurationType("Passive");
        copy.setSeverity(computedSeverity);
        copy.setAffinity("None");
        copy.setDescription(description == null || description.isBlank() ? "Specialty passive bonus" : description);
        resources.addStatus(copy);
        return true;
    }

    private boolean applySpecialtyMultiplierAliases(String uniqueName, String attribute, double severity, String description) {
        if ("DMGMULTI".equalsIgnoreCase(attribute)) {
            addTrainingAttributeStatus(uniqueName + " (BDMG)", "MBDMG", severity, "Passive", description);
            addTrainingAttributeStatus(uniqueName + " (TDMG)", "MTDMG", severity, "Passive", description);
            return true;
        }
        if ("HEALMULTI".equalsIgnoreCase(attribute)) {
            addTrainingAttributeStatus(uniqueName + " (BHEAL)", "MBHEAL", severity, "Passive", description);
            addTrainingAttributeStatus(uniqueName + " (THEAL)", "MTHEAL", severity, "Passive", description);
            return true;
        }
        return false;
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
        ds.setAttribute("B" + key.toUpperCase());
        ds.setDurationType("Passive");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription("Equipment passive bonus");
        attributes.addStatus(ds);
    }

    private void applyMoldingManifestEquipmentBonus(DataItemEquipment item, String statusName) {
        applyMoldingManifestEquipmentBonus(item, statusName, true);
    }

    private void applyMoldingManifestEquipmentBonus(DataItemEquipment item, String statusName, boolean includeArmor) {
        if (item == null || statusName == null) return;
        MoldingEquipmentBonus bonus = parseMoldingManifestBonus(item.getInote());
        if (bonus == null || bonus.bonusKey().isBlank() || bonus.amount() == 0.0) return;
        if (!includeArmor && "ARMOR".equalsIgnoreCase(bonus.bonusKey())) return;
        applyEquipmentBonusStatus(bonus.bonusKey(), bonus.amount(), statusName);
    }

    private MoldingEquipmentBonus parseMoldingManifestBonus(String note) {
        if (note == null || !note.startsWith(MOLDING_MANIFEST_NOTE_PREFIX)) return null;
        String[] parts = note.split("\\|");
        String bonusKey = "";
        double amount = 0.0;
        for (String part : parts) {
            if (part == null) continue;
            String trimmed = part.trim();
            if (trimmed.startsWith("BONUS=")) {
                bonusKey = trimmed.substring("BONUS=".length()).trim().toUpperCase();
            } else if (trimmed.startsWith("AMOUNT=")) {
                try {
                    amount = Double.parseDouble(trimmed.substring("AMOUNT=".length()).trim());
                } catch (NumberFormatException ignored) {
                    amount = 0.0;
                }
            }
        }
        return new MoldingEquipmentBonus(bonusKey, amount);
    }

    private record MoldingEquipmentBonus(String bonusKey, double amount) {}

    private void addPermanentAttributeStatus(String category, String key, String statusName, double severity, String description) {
        if (attributes == null || category == null || key == null || statusName == null) return;
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription(description);

    }

    private void addEquipmentResourceStatus(StatBlock[] blocks, String key, double severity, String statusName) {
        if (blocks == null || blocks.length == 0 || blocks[0] == null || statusName == null) return;
        //blocks[0].removeStatus(statusName);
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription("Equipment passive bonus");
        //blocks[0].addStatus(ds);
    }

    private void addPermanentResourceMulti(StatBlock[] blocks, String statusName, String key, double severity, String description) {
        if (blocks == null || blocks.length == 0 || blocks[0] == null || statusName == null || key == null) return;
        //blocks[0].removeMulti(statusName);
        DataStatus ds = new DataStatus();
        ds.setName(statusName);
        ds.setAttribute(key);
        ds.setDurationType("Permanent");
        ds.setSeverity(severity);
        ds.setAffinity("None");
        ds.setDescription(description);
        //blocks[0].addMulti(ds);
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
    private void applyTrainingPermStatuses(StoreRuleManager dq) {
        if (dq == null || training == null || attributes == null || resources == null) return;
        List<DataTraining> all = training.getAllTraining();
        if (all == null) return;

        for (DataTraining tech : all) {
            if (tech == null) continue;
            boolean passiveTech = "Passive".equalsIgnoreCase(tech.getType());
            boolean maintainedTech = "Maintained".equalsIgnoreCase(tech.getType());
            if (!passiveTech && !maintainedTech) continue;
            if (maintainedTech) continue;
            if (tech.getStatusScaleLevel() <= 0) continue;
            List<DataStatus> perms = collectTrainingPermStatuses(dq, tech);
            if (perms.isEmpty()) continue;
            for (DataStatus ps : perms) {
                applyTrainingPermStatus(tech, ps);
            }
        }
    }

    private List<DataStatus> collectTrainingPermStatuses(StoreRuleManager dq, DataTraining tech) {
        ArrayList<DataStatus> copies = new ArrayList<>();
        if (tech == null) return copies;

        boolean foundRuleStatuses = false;
        if (tech.getId() > 0) {
            DataTraining base = dq.getTrainingById(tech.getId());
            if (base != null) {
                List<DataStatus> perms = base.getPermStatus();
                if (perms != null) {
                    for (DataStatus ps : perms) {
                        if (ps != null) {
                            copies.add(new DataStatus(ps));
                            foundRuleStatuses = true;
                        }
                    }
                }
            }
        }

        if (!foundRuleStatuses) {
            List<DataStatus> localPerms = tech.getPermStatus();
            if (localPerms != null) {
                for (DataStatus ps : localPerms) {
                    if (ps != null) {
                        copies.add(new DataStatus(ps));
                        foundRuleStatuses = true;
                    }
                }
            }
        }

        if (!foundRuleStatuses && tech.getGrant() != null) {
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
        if (isReminderStatus(permStatus)) {
            appendOtherReminderStatus(permStatus);
            return;
        }
        String attr = normalizeAttrKey(permStatus.getAttribute());
        if (attr == null || attr.isBlank()) return;
        String statusName = permStatus.getName() != null ? permStatus.getName() : tech.getName();
        String uniqueName = TRAINING_STATUS_PREFIX + statusName + " [" + attr + "] (T" + tech.getId() + ")";
        double severity = tech.scaleStatusSeverity(this, permStatus.getSeverity());
        String durationType = "Maintained".equalsIgnoreCase(tech.getType()) ? "Maintained" : "Passive";
        String description = permStatus.getDescription() == null || permStatus.getDescription().isBlank()
                ? "Training passive bonus"
                : permStatus.getDescription();
        if (applyTrainingResourceStatus(uniqueName, attr, severity, durationType, description)) {
            return;
        }
        if (applyTrainingMultiplierAliases(uniqueName, attr, severity, durationType, description)) {
            return;
        }

        String category = resolveCategory(attr);
        if (category == null) return;
        DataStatus copy = new DataStatus(permStatus);
        copy.setName(uniqueName);
        copy.setAttribute("B" + attr);
        copy.setDurationType(durationType);
        copy.setSeverity(severity);
        copy.setAffinity("None");
        copy.setDescription(description);
        attributes.addStatus(copy);
    }

    private boolean applyTrainingResourceStatus(String uniqueName, String attribute, double severity, String durationType, String description) {
        String resourceAttribute = switch (attribute) {
            case "MAXHP" -> "BASEHP";
            case "HPMULTI" -> "MULTIHP";
            case "MAXAURA" -> "BASEAURA";
            case "AURAMULTI" -> "MULTIAURA";
            case "REACT" -> "BASEREACT";
            case "R1" -> "BASER1";
            case "R2" -> "BASER2";
            case "R3" -> "BASER3";
            default -> null;
        };
        if (resourceAttribute == null) return false;

        DataStatus copy = new DataStatus();
        copy.setName(uniqueName);
        copy.setAttribute(resourceAttribute);
        copy.setDurationType(durationType);
        copy.setSeverity(severity);
        copy.setAffinity("None");
        copy.setDescription(description);
        resources.addStatus(copy);
        return true;
    }

    private boolean applyTrainingMultiplierAliases(String uniqueName, String attribute, double severity, String durationType, String description) {
        if ("DMGMULTI".equals(attribute)) {
            addTrainingAttributeStatus(uniqueName + " (BDMG)", "MBDMG", severity, durationType, description);
            addTrainingAttributeStatus(uniqueName + " (TDMG)", "MTDMG", severity, durationType, description);
            return true;
        }
        if ("HEALMULTI".equals(attribute)) {
            addTrainingAttributeStatus(uniqueName + " (BHEAL)", "MBHEAL", severity, durationType, description);
            addTrainingAttributeStatus(uniqueName + " (THEAL)", "MTHEAL", severity, durationType, description);
            return true;
        }
        return false;
    }

    private void addTrainingAttributeStatus(String uniqueName, String attribute, double severity, String durationType, String description) {
        if (attributes == null || attribute == null || attribute.isBlank()) return;
        DataStatus copy = new DataStatus();
        copy.setName(uniqueName);
        copy.setAttribute(attribute);
        copy.setDurationType(durationType);
        copy.setSeverity(severity);
        copy.setAffinity("None");
        copy.setDescription(description);
        attributes.addStatus(copy);
    }

    /** Heuristically resolve which category an attribute key belongs to. */
    private String resolveCategory(String key) {
        if (key == null) return null;
        // Normalize aliases
        String norm = normalizeAttrKey(key);
        if (containsKey(CharAttributes.getAttributeKeys(), norm)) return "attribute";
        if (containsKey(CharAttributes.getDefenseKeys(), norm)) return "defense";
        if (containsKey(CharAttributes.getDamageTypeKeys(), norm)) return "resist";
        if (containsKey(CharAttributes.getCombatKeys(), norm)) return "combat";
        if (containsKey(CharAttributes.getSecondaryKeys(), norm)) return "secondary";
        if (containsKey(CharAttributes.getDamageKeys(), norm)) return "damage";
        if (norm.toUpperCase(Locale.ROOT).startsWith("SKILL") && norm.length() > "SKILL".length()) return "skill";
        return null;
    }

    /** Maps known aliases (e.g., RESPHY -> PHY) to existing stat keys. */
    private String normalizeAttrKey(String key) {
        if (key == null) return null;
        String upper = key.toUpperCase();
        if ("APPLY".equals(upper)) return "APP";
        if ("IMPAIR".equals(upper)) return "IMP";
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

    private boolean containsKey(String[] keys, String target) {
        if (keys == null || target == null) return false;
        for (String key : keys) {
            if (target.equalsIgnoreCase(key)) return true;
        }
        return false;
    }

    private void clearTrainingDerivedBonuses() {
        if (attributes != null) {
            clearStatusPrefix(attributes.getBAttributes(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMAttributes(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getBDefense(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMDefense(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getBResist(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMResist(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getBCombat(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMCombat(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getBSecondary(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMSecondary(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getBDamage(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMDamage(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getBSkill(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(attributes.getMSkill(), TRAINING_STATUS_PREFIX);
        }
        if (resources != null) {
            clearStatusPrefix(resources.getBaseHP(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiHP(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getBaseAura(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiAura(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getBaseResource1(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiResource1(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getBaseResource2(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiResource2(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getBaseResource3(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiResource3(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getBaseAngelPoints(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiAngelPoints(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getBaseReactions(), TRAINING_STATUS_PREFIX);
            clearStatusPrefix(resources.getMultiReactions(), TRAINING_STATUS_PREFIX);
        }
    }

    private void clearStatusPrefix(ArrayList<DataStatus>[][] category, String prefix) {
        if (category == null) return;
        for (ArrayList<DataStatus>[] block : category) {
            clearStatusPrefix(block, prefix);
        }
    }

    private void clearStatusPrefix(ArrayList<DataStatus>[] block, String prefix) {
        if (block == null || prefix == null || prefix.isBlank()) return;
        for (ArrayList<DataStatus> statuses : block) {
            if (statuses == null) continue;
            statuses.removeIf(status -> status != null && status.getName() != null && status.getName().startsWith(prefix));
        }
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

    @JsonIgnore
    public boolean isIrdonRace() {
        return identity != null
                && identity.getRace() != null
                && IRDON_RACE.equalsIgnoreCase(identity.getRace().trim());
    }

    @JsonIgnore
    public boolean hasAngelPoints() {
        return isIrdonRace();
    }

    @JsonIgnore
    public boolean hasShareableDomainStatusEffects() {
        if (training == null) return false;
        for (DataStatus status : training.getDomainStatusEffects()) {
            if (isShareableDomainStatus(status)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public int getRaceTrainingRank() {
        if (training == null) return 0;
        DataTraining raceTraining = training.getTrainingByName(RACE_TRAINING_NAME);
        return raceTraining == null ? 0 : Math.max(0, raceTraining.getRank());
    }

    @JsonIgnore
    public int getCurrentAngelPoints() {
        return resources == null ? 0 : Math.max(0, resources.calcCurrentAngelPoints());
    }

    public boolean hasSpecialty(String specialtyName) {
        return specials != null && specialtyName != null && specials.hasSpecialty(specialtyName);
    }

    public double applyIncomingHealthDamage(double amount) {
        double safeAmount = Math.max(0.0, amount);
        if (safeAmount <= 0.0 || resources == null) return 0.0;

        double immediateDamage = safeAmount;
        if (combat != null && combat.isInCombat() && hasSpecialty(SHUFFLE_SPECIALTY)) {
            immediateDamage = safeAmount * 0.5;
            combat.adjustShufflePool(safeAmount - immediateDamage);
        }

        resources.setLostHP(resources.getLostHP() + immediateDamage);
        return immediateDamage;
    }

    public double applyShuffleTurnStartDamage() {
        if (combat == null || resources == null) return 0.0;
        double currentPool = combat.getShufflePool();
        if (currentPool <= 0.0) return 0.0;

        double damage = currentPool * 0.5;
        combat.setShufflePool(currentPool - damage);
        resources.setLostHP(resources.getLostHP() + damage);
        return damage;
    }

    public double flushShufflePoolDamage() {
        if (combat == null || resources == null) return 0.0;
        double currentPool = combat.getShufflePool();
        if (currentPool <= 0.0) return 0.0;

        combat.setShufflePool(0.0);
        resources.setLostHP(resources.getLostHP() + currentPool);
        return currentPool;
    }

    @JsonIgnore
    public int getMaxAngelPoints() {
        return resources == null ? 0 : Math.max(0, resources.calcMaxAngelPoints());
    }

    @JsonIgnore
    public String buildDomainStatusMacro() {
        DataColor raceColor = null;
        if (identity != null && identity.getRace() != null) {
            StoreRuleManager ruleManager = new StoreRuleManager();
            raceColor = ruleManager.getColorByTitle(identity.getRace());
        }
        if (raceColor == null) {
            raceColor = new DataColor("Default", 0, 0, 0, 255, 255, 255);
        }

        String colorString1 = toHexColor(raceColor.getBackRed(), raceColor.getBackGreen(), raceColor.getBackBlue());
        String colorString2 = toHexColor(raceColor.getForeRed(), raceColor.getForeGreen(), raceColor.getForeBlue());
        String charName = identity != null && identity.getName() != null ? identity.getName() : "Character";
        String domainName = training != null && !training.getDomains().isEmpty() ? training.getDomains().get(0) : "";

        StringBuilder tempString = new StringBuilder();
        tempString.append("!scriptcard {{ --#titleCardBackground|").append(colorString1)
                .append(" --#titleFontFace|Arial --#titleFontSize|2em --#titleFontColor|").append(colorString1)
                .append(" --#titleCardBottomBorder|4px solid #000000; --#title|").append(charName)
                .append(" --#subtitleFontFace|Tahoma --#subtitleFontSize|1.2em --#subtitleFontColor|").append(colorString2)
                .append(" --#leftSub|Domain Emanation")
                .append(" --#LineHeight|1.5em --#rollHilightLineHeight|1.5em --#evenRowBackground|").append(colorString1)
                .append(" --#evenRowFontColor|").append(colorString2)
                .append(" --#oddRowBackground|").append(colorString2)
                .append(" --#oddRowFontColor|").append(colorString1)
                .append(" --#bodyFontFace|Helvetica --#bodyFontSize|16px --#outputtagprefix|&nbsp;&nbsp;");

        if (!domainName.isBlank()) {
            tempString.append(" --+|Domain: ").append(sanitizeStatusCodeText(domainName));
        }
        String statusCode = buildDomainStatusCode();
        if (statusCode.isBlank()) {
            tempString.append(" --+|No shareable domain bonus statuses are active.");
        } else {
            tempString.append(" --+|Status Code:[br]&nbsp;&nbsp;").append(statusCode);
        }
        tempString.append(" }}");
        return tempString.toString();
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

        upsertLevelScalerStatus(attributes.getBDefense(), "BREF",
                SIZE_STATUS_PREFIX + " (REF)", refSev, "Size-based Reflex modifier");
        upsertLevelScalerStatus(attributes.getBDefense(), "BFORT",
                SIZE_STATUS_PREFIX + " (FORT)", fortSev, "Size-based Fortitude modifier");
        upsertLevelScalerStatus(attributes.getBDefense(), "BDODGE",
                SIZE_STATUS_PREFIX + " (DODGE)", dodgeSev, "Size-based Dodge modifier");
    }

    /** Writes each current specialty name to the terminal for update-time inspection. */
    private void logSpecialtyNames() {
        if (specials == null) {
            flushReminderBuckets();
            return;
        }
        SpecCheck specCheck = new SpecCheck();
        specCheck.setInventoryReference(inventory);
        collectSpecialtySpecCheckReminders(specCheck, specials.getRacialSpecialty(), raceReminderLines);
        for (DataSpecialty specialty : specials.getClassSpecialties()) {
            collectSpecialtySpecCheckReminders(specCheck, specialty, classReminderLines);
        }
        for (DataSpecialty specialty : specials.getTrainedSpecialties()) {
            collectSpecialtySpecCheckReminders(specCheck, specialty, otherSpecialtyReminderLines);
        }
        appendCombatStatusReminders();
        appendMoldingReminderLine(otherReminderLines);
        flushReminderBuckets();
    }

    private void collectSpecialtySpecCheckReminders(SpecCheck specCheck, DataSpecialty specialty, List<String> bucket) {
        if (specCheck == null || specialty == null || bucket == null) return;
        if (shouldEmitDomainEmanationReminder(specialty)) {
            appendDomainEmanationReminderLine(bucket);
            return;
        }
        if (shouldEmitStanceReminder(specialty)) {
            appendStanceReminderLine(bucket);
            return;
        }
        specCheck.setPanelReminderSetter(line -> appendReminderLine(bucket, line));
        specCheck.checkSpec(specialty);
    }

    private boolean shouldEmitDomainEmanationReminder(DataSpecialty specialty) {
        if (specialty == null || specialty.getName() == null || !hasPaladinDomainEmanationAccess()) return false;
        String specialtyName = specialty.getName().trim();
        return HOLY_DOMAIN_SPECIALTY.equalsIgnoreCase(specialtyName)
                || DOMAIN_EMANATION_SPECIALTY.equalsIgnoreCase(specialtyName);
    }

    private void appendDomainEmanationReminderLine(List<String> bucket) {
        if (bucket == null) return;
        String reminderLine = DOMAIN_EMANATION_REMINDER_KEY + "::On|Off";
        if (!bucket.contains(reminderLine)) {
            bucket.add(reminderLine);
        }
    }

    private boolean shouldEmitStanceReminder(DataSpecialty specialty) {
        if (specialty == null || specialty.getName() == null || !STANCE_SPECIALTY.equalsIgnoreCase(specialty.getName().trim())) {
            return false;
        }
        return !getKnownStanceNames().isEmpty();
    }

    private void appendStanceReminderLine(List<String> bucket) {
        if (bucket == null) return;
        ArrayList<String> stanceNames = getKnownStanceNames();
        if (stanceNames.isEmpty()) return;
        stanceNames.add(0, NO_ACTIVE_STANCE);
        String reminderLine = CURRENT_STANCE_REMINDER_KEY + "::" + String.join("|", stanceNames);
        if (!bucket.contains(reminderLine)) {
            bucket.add(reminderLine);
        }
    }

    private void appendCombatStatusReminders() {
        if (combat == null || combat.getCombatStatus() == null) return;
        for (DataStatus status : combat.getCombatStatus()) {
            appendOtherReminderStatus(status);
        }
        if (combat.getStunTokens() > 0) {
            appendReminderLine(otherReminderLines, "Stunned: " + combat.getStunTokens());
        }
        if (combat.getRootTokens() > 0) {
            appendReminderLine(otherReminderLines, "Rooted: unable to move on your turn. (" + combat.getRootTokens() + ")");
        }
        if (combat.getIncapacitateTokens() > 0) {
            appendReminderLine(otherReminderLines, "Incapacitated: turn is skipped. (" + combat.getIncapacitateTokens() + ")");
        }
    }

    private void appendMoldingReminderLine(List<String> bucket) {
        if (bucket == null || !hasActiveMoldingTechnique()) return;
        appendReminderLine(bucket, MOLDING_REMINDER_KEY + ":");
    }

    public boolean hasActiveMoldingTechnique() {
        if (training == null) return false;
        for (DataTraining tech : training.getAllTraining()) {
            if (tech == null || tech.getName() == null) continue;
            if (!"Maintained".equalsIgnoreCase(tech.getType())) continue;
            if (!tech.getName().trim().toLowerCase(Locale.ROOT).endsWith(" molding")) continue;
            if (tech.getAl() > 0) return true;
        }
        return false;
    }

    public boolean isReminderStatus(DataStatus status) {
        return status != null && isReminderAttribute(status.getAttribute());
    }

    private boolean isReminderAttribute(String attribute) {
        if (attribute == null || attribute.isBlank()) return false;
        String normalized = attribute.trim().toUpperCase();
        for (String key : REMINDER_ATTRIBUTE_KEYS) {
            if (key.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    private void appendRaceReminderStatus(DataStatus status) {
        appendReminderToBucket(raceReminderLines, status);
    }

    private void appendSpecialtyReminderStatus(DataSpecialty specialty, DataStatus status) {
        if (specialty != null && specials != null) {
            DataSpecialty racialSpecialty = specials.getRacialSpecialty();
            if (racialSpecialty != null && racialSpecialty == specialty) {
                appendReminderToBucket(raceReminderLines, status);
                return;
            }
            if (specials.getClassSpecialties().contains(specialty)) {
                appendReminderToBucket(classReminderLines, status);
                return;
            }
        }
        appendReminderToBucket(otherSpecialtyReminderLines, status);
    }

    private void appendOtherReminderStatus(DataStatus status) {
        appendReminderToBucket(otherReminderLines, status);
    }

    private void appendReminderToBucket(List<String> bucket, DataStatus status) {
        if (!isReminderStatus(status) || bucket == null) return;
        String reminderText = buildReminderTextFromStatus(status);
        appendReminderLine(bucket, reminderText);
    }

    private void appendReminderStatus(DataStatus status) {
        appendOtherReminderStatus(status);
    }

    private String buildReminderTextFromStatus(DataStatus status) {
        if (status == null) return "";
        if (status.getDescription() != null && !status.getDescription().isBlank()) {
            return status.getDescription().trim();
        }
        if (status.getName() != null && !status.getName().isBlank()) {
            return status.getName().trim();
        }
        return "";
    }

    private ArrayList<String> getKnownStanceNames() {
        ArrayList<String> stanceNames = new ArrayList<>();
        if (Lists == null) return stanceNames;
        for (List<DataList> group : Lists) {
            if (group == null) continue;
            for (DataList entry : group) {
                if (entry == null || entry.getList() == null || entry.getName() == null) continue;
                if (!STANCE_LIST.equalsIgnoreCase(entry.getList().trim())) continue;
                String stanceName = entry.getName().trim();
                if (stanceName.isBlank()) continue;
                boolean alreadyAdded = false;
                for (String existing : stanceNames) {
                    if (existing.equalsIgnoreCase(stanceName)) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    stanceNames.add(stanceName);
                }
            }
        }
        return stanceNames;
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
            case "MAXANGEL" -> resources.calcMaxAngelPoints();
            case "MAXREACT" -> resources.calcMaxReactions();
            case "CURHP" -> resources.calcCurrentHP();
            case "CURAURA" -> resources.calcCurrentAura();
            case "CURR1" -> resources.calcCurrentResource1();
            case "CURR2" -> resources.calcCurrentResource2();
            case "CURR3" -> resources.calcCurrentResource3();
            case "CURANGEL" -> resources.calcCurrentAngelPoints();
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
            case "SPENTANGEL" -> resources.setSpentAngelPoints(value);
            case "SPENTREACT" -> resources.setSpentReactions(value);
            case "MAINOCC" -> resources.setMainOccupiedAura(value);
            case "GRANTOCC" -> resources.setGrantOccupiedAura(value);
            case "SHIELD" -> resources.setShield(value);
            case "STAGGER" -> resources.setStagger(value);
        };
    }

    public void checkSkillSpecialChanges() {
        specials.checkChanges();
    }

    public double getSkillSpecialBonusForDisplayName(String displayName) {
        return getSkillDedicationBonusForDisplayName(displayName)
                + getDivineVowSkillBonusForDisplayName(displayName)
                + getStatusSkillBonusForDisplayName(displayName);
    }

    @JsonIgnore
    public double getUniversalSkillBonus() {
        if (attributes == null) return 0.0;
        return attributes.calcStatusValue("INT") * 0.5;
    }

    public double getSkillDedicationBonusForDisplayName(String displayName) {
        if (specials == null || displayName == null || displayName.isBlank()) return 0.0;
        return specials.getSkillDedicationBonusForDisplayName(displayName, getLevel());
    }

    public double getDivineVowSkillBonusForDisplayName(String displayName) {
        if (specials == null || displayName == null || displayName.isBlank()) return 0.0;
        List<DataSpecialty> allSpecialties = specials.getAllSpecialties();
        if (allSpecialties == null || allSpecialties.isEmpty()) return 0.0;

        double total = 0.0;
        for (DataSpecialty specialty : allSpecialties) {
            if (specialty == null || specialty.getName() == null) continue;
            if (!DIVINE_VOW_SPECIALTY.equalsIgnoreCase(specialty.getName())) continue;
            DataVow vow = resolveSelectedVow(specialty);
            if (vow == null) continue;
            total += resolveVowSkillBonus(vow, displayName);
        }
        return total;
    }

    public double getStatusSkillBonusForDisplayName(String displayName) {
        if (attributes == null || displayName == null || displayName.isBlank()) return 0.0;
        double total = 0.0;
        for (DataStatus status : collectAllAttributeStatuses()) {
            if (status == null) continue;
            String targetSkill = extractSkillStatusTarget(status);
            if (targetSkill.isBlank()) continue;
            if (!skillDisplayMatches(displayName, targetSkill)) continue;
            total += status.getSeverity();
        }
        return total;
    }

    private String extractSkillStatusTarget(DataStatus status) {
        if (status == null) return "";
        String statusName = safeTrim(status.getName());
        if (statusName.regionMatches(true, 0, "SKILL", 0, "SKILL".length())) {
            return statusName.substring("SKILL".length()).trim();
        }
        String attribute = safeTrim(status.getAttribute()).toUpperCase(Locale.ROOT);
        if ((attribute.startsWith("B") || attribute.startsWith("M")) && attribute.length() > 1) {
            attribute = attribute.substring(1);
        }
        if (attribute.regionMatches(true, 0, "SKILL", 0, "SKILL".length())) {
            return attribute.substring("SKILL".length()).trim();
        }
        return "";
    }

    private List<DataStatus> collectAllAttributeStatuses() {
        ArrayList<DataStatus> statuses = new ArrayList<>();
        if (attributes == null) return statuses;
        addStatuses(statuses, attributes.getBAttributes());
        addStatuses(statuses, attributes.getMAttributes());
        addStatuses(statuses, attributes.getBDefense());
        addStatuses(statuses, attributes.getMDefense());
        addStatuses(statuses, attributes.getBResist());
        addStatuses(statuses, attributes.getMResist());
        addStatuses(statuses, attributes.getBCombat());
        addStatuses(statuses, attributes.getMCombat());
        addStatuses(statuses, attributes.getBSecondary());
        addStatuses(statuses, attributes.getMSecondary());
        addStatuses(statuses, attributes.getBDamage());
        addStatuses(statuses, attributes.getMDamage());
        addStatuses(statuses, attributes.getBSkill());
        addStatuses(statuses, attributes.getMSkill());
        return statuses;
    }

    private void addStatuses(List<DataStatus> target, ArrayList<DataStatus>[][] category) {
        if (target == null || category == null) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null) continue;
            for (ArrayList<DataStatus> bucket : block) {
                if (bucket == null || bucket.isEmpty()) continue;
                target.addAll(bucket);
            }
        }
    }

    private void addStatuses(List<DataStatus> target, ArrayList<DataStatus>[] block) {
        if (target == null || block == null) return;
        for (ArrayList<DataStatus> bucket : block) {
            if (bucket == null || bucket.isEmpty()) continue;
            target.addAll(bucket);
        }
    }

    private double resolveVowSkillBonus(DataVow vow, String displayName) {
        if (vow == null || displayName == null || displayName.isBlank()) return 0.0;
        if (!"passive".equalsIgnoreCase(vow.getBonusType())) return 0.0;
        String effect = vow.getBonusEffect();
        if (effect == null || effect.isBlank()) return 0.0;

        int colonIndex = effect.indexOf(':');
        if (colonIndex <= 0 || colonIndex >= effect.length() - 1) return 0.0;

        String targetToken = effect.substring(0, colonIndex).trim();
        String expression = effect.substring(colonIndex + 1).trim();
        if (!targetToken.regionMatches(true, 0, "Skill", 0, "Skill".length())) return 0.0;

        String targetSkill = targetToken.substring("Skill".length()).trim();
        if (!skillDisplayMatches(displayName, targetSkill)) return 0.0;

        return evaluateVowSkillBonusExpression(expression);
    }

    private boolean skillDisplayMatches(String displayName, String targetSkill) {
        if (displayName == null || targetSkill == null) return false;
        String trimmedDisplay = displayName.trim();
        String trimmedTarget = targetSkill.trim();
        if (trimmedDisplay.equalsIgnoreCase(trimmedTarget)) return true;
        if (normalizeSkillToken(trimmedDisplay).equalsIgnoreCase(normalizeSkillToken(trimmedTarget))) return true;
        int subtypeStart = trimmedDisplay.indexOf(" (");
        if (subtypeStart > 0) {
            String baseDisplay = trimmedDisplay.substring(0, subtypeStart).trim();
            return baseDisplay.equalsIgnoreCase(trimmedTarget)
                    || normalizeSkillToken(baseDisplay).equalsIgnoreCase(normalizeSkillToken(trimmedTarget));
        }
        return false;
    }

    private String normalizeSkillToken(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll("[^A-Za-z0-9]", "");
    }

    private double evaluateVowSkillBonusExpression(String expression) {
        if (expression == null || expression.isBlank()) return 0.0;
        String normalized = expression.replace(" ", "");
        if (normalized.isBlank()) return 0.0;
        if (normalized.charAt(0) != '+' && normalized.charAt(0) != '-') {
            normalized = "+" + normalized;
        }

        double total = 0.0;
        int index = 0;
        while (index < normalized.length()) {
            char signChar = normalized.charAt(index);
            if (signChar != '+' && signChar != '-') {
                return 0.0;
            }
            index++;
            int start = index;
            while (index < normalized.length()) {
                char ch = normalized.charAt(index);
                if (ch == '+' || ch == '-') break;
                index++;
            }
            String term = normalized.substring(start, index);
            if (term.isBlank()) continue;
            double termValue = evaluateVowSkillBonusTerm(term);
            total += (signChar == '-' ? -termValue : termValue);
        }
        return total;
    }

    private double evaluateVowSkillBonusTerm(String term) {
        if (term == null || term.isBlank()) return 0.0;
        String[] factors = term.split("\\*");
        double value = 1.0;
        boolean resolvedFactor = false;
        for (String factor : factors) {
            if (factor == null || factor.isBlank()) continue;
            value *= resolveVowSkillBonusToken(factor);
            resolvedFactor = true;
        }
        return resolvedFactor ? value : 0.0;
    }

    private double resolveVowSkillBonusToken(String token) {
        if (token == null || token.isBlank()) return 0.0;
        String normalized = token.trim().toUpperCase();
        if ("PRIM".equals(normalized)) {
            return resolveRawAttributeBonus(resolveDomainPrimaryAttribute());
        }
        if (containsKey(CharAttributes.getAttributeKeys(), normalized)) {
            return resolveRawAttributeBonus(normalized);
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private double resolveRawAttributeBonus(String attributeKey) {
        if (attributes == null || attributeKey == null || attributeKey.isBlank()) return 0.0;
        return attributes.calcStatusValue(attributeKey);
    }

    private boolean isShareableDomainStatus(DataStatus status) {
        if (status == null) return false;
        String attribute = status.getAttribute();
        if (attribute == null || attribute.isBlank()) return false;
        if ("REMINDER".equalsIgnoreCase(attribute)) return false;
        return Math.abs(status.getSeverity()) >= 0.0001;
    }

    private String buildDomainStatusCode() {
        if (training == null) return "";
        Map<String, Double> totals = new LinkedHashMap<>();
        for (DataStatus status : training.getDomainStatusEffects()) {
            if (!isShareableDomainStatus(status)) continue;
            addStatusCodeContribution(totals, status.getAttribute(), status.getSeverity());
        }
        if (totals.isEmpty()) return "";

        StringBuilder code = new StringBuilder();
        code.append("NM").append(buildDomainStatusCodeName());
        String durationToken = buildDomainStatusDurationToken();
        if (!durationToken.isBlank()) {
            code.append('_').append(durationToken);
        }
        boolean appendedAnyStatus = false;
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            double severity = entry.getValue() == null ? 0.0 : entry.getValue();
            if (Math.abs(severity) < 0.0001) continue;
            if (appendedAnyStatus) {
                code.append('_');
            } else {
                code.append('_');
                appendedAnyStatus = true;
            }
            code.append(StatusCodeParser.getPreferredAttributeAlias(entry.getKey()))
                    .append(formatPackedStatusSeverity(severity));
        }
        return code.toString();
    }

    private String buildDomainStatusCodeName() {
        return "DOM";
    }

    private String buildDomainStatusDurationToken() {
        if (identity == null) return "";
        String className = identity.getCharClass();
        if (className == null || className.isBlank()) return "";
        if ("Paladin".equalsIgnoreCase(className)) {
            return "DUR:Turn:1";
        }
        if ("Cleric".equalsIgnoreCase(className)) {
            return "DUR:Turn:" + Math.max(1, getLevel());
        }
        return "";
    }

    private void addStatusCodeContribution(Map<String, Double> totals, String rawAttribute, double severity) {
        if (totals == null || rawAttribute == null || rawAttribute.isBlank() || Math.abs(severity) < 0.0001) return;
        String normalized = normalizeStatusCodeAttribute(rawAttribute);
        if (normalized == null || normalized.isBlank()) return;
        switch (normalized) {
            case "HP" -> mergeStatusSeverity(totals, "HP", severity);
            case "AURA" -> mergeStatusSeverity(totals, "AURA", severity);
            case "AC" -> mergeStatusSeverity(totals, "BDEF", severity);
            case "MAXHP" -> mergeStatusSeverity(totals, "BASEHP", severity);
            case "HPMULTI" -> mergeStatusSeverity(totals, "MULTIHP", severity);
            case "MAXAURA" -> mergeStatusSeverity(totals, "BASEAURA", severity);
            case "AURAMULTI" -> mergeStatusSeverity(totals, "MULTIAURA", severity);
            case "REACT" -> mergeStatusSeverity(totals, "BASEREACT", severity);
            case "R1" -> mergeStatusSeverity(totals, "BASER1", severity);
            case "R2" -> mergeStatusSeverity(totals, "BASER2", severity);
            case "R3" -> mergeStatusSeverity(totals, "BASER3", severity);
            case "DMGMULTI" -> {
                mergeStatusSeverity(totals, "MBDMG", severity);
                mergeStatusSeverity(totals, "MTDMG", severity);
            }
            case "HEALMULTI" -> {
                mergeStatusSeverity(totals, "MBHEAL", severity);
                mergeStatusSeverity(totals, "MTHEAL", severity);
            }
            default -> {
                String outputAttribute = normalized.startsWith("B") || normalized.startsWith("M")
                        ? normalized
                        : "B" + normalized;
                if (isSupportedStatusCodeAttribute(outputAttribute)) {
                    mergeStatusSeverity(totals, outputAttribute, severity);
                }
            }
        }
    }

    private void mergeStatusSeverity(Map<String, Double> totals, String attribute, double severity) {
        if (totals == null || attribute == null || attribute.isBlank() || Math.abs(severity) < 0.0001) return;
        totals.merge(attribute, severity, Double::sum);
    }

    private String normalizeStatusCodeAttribute(String key) {
        if (key == null) return null;
        String upper = key.trim().toUpperCase(Locale.ROOT);
        if (upper.isBlank()) return null;
        return switch (upper) {
            case "SPENTAURA", "LOSTHP", "KBRES" -> null;
            case "APPLY" -> "APP";
            case "IMPAIR" -> "IMP";
            case "RESPHY" -> "PHY";
            default -> {
                if (upper.startsWith("RESIST")) yield upper.substring("RESIST".length());
                if (upper.startsWith("RES") && upper.length() > 3) yield upper.substring(3);
                yield upper;
            }
        };
    }

    private boolean isSupportedStatusCodeAttribute(String attribute) {
        if (attribute == null || attribute.isBlank()) return false;
        String upper = attribute.trim().toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
                    "BASEREACT", "BASER1", "BASER2", "BASER3",
                    "BSTR", "BDEX", "BCON", "BFOC", "BCTL", "BCAP", "BKNOW", "BMECH", "BPERC", "BINT", "BCHA", "BSUB",
                    "BARMOR", "BDODGE", "BDEF", "BFORT", "BREF", "BWILL", "BAVOID",
                    "BATK", "BAPP", "BMOVE", "BFLY", "BRANGE", "BINIT", "BCMAN", "BMAXATK",
                    "BSUP", "BIMP", "BMAST", "BEXCL", "BGRANT", "BCRUSH", "BBREAK", "BAREA", "BPOWER",
                    "BBDMG", "BTDMG", "BBHEAL", "BTHEAL",
                    "MBDMG", "MTDMG", "MBHEAL", "MTHEAL",
                    "BALL", "BPHY", "BBLUNT", "BPIERCE", "BSLASH", "BFIRE", "BFROST", "BELEC", "BENERGY", "BSONIC",
                    "BLIGHT", "BTOXIC", "BDARK", "BPSI", "BSPIRIT", "BTIME" -> true;
            default -> (upper.startsWith("BSKILL") || upper.startsWith("MSKILL")) && upper.length() > "BSKILL".length();
        };
    }

    private String formatPackedStatusSeverity(double severity) {
        double rounded = round2(severity);
        String sign = rounded < 0 ? "N" : "P";
        double absolute = Math.abs(rounded);
        if (Math.abs(absolute - Math.rint(absolute)) < 0.0001) {
            return sign + Integer.toString((int) Math.rint(absolute));
        }
        String text = String.format(Locale.ROOT, "%.2f", absolute)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "")
                .replace(".", "D");
        return sign + text;
    }

    private String sanitizeStatusCodeText(String value) {
        if (value == null) return "";
        return value.replace("|", "/").trim();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String toHexColor(int red, int green, int blue) {
        return String.format(Locale.ROOT, "#%02x%02x%02x", red, green, blue);
    }

    public void refreshSkills() {
        if (specials != null) {
            
        }
    }

    public void refreshSpecialties() {
        if (specials != null) {
            
        }
    }
}

