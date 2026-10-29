package eternity.Minions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import eternity.CharAttributes;
import eternity.CharCombat;
import eternity.StoreCharData;
import eternity.StoreRuleManager;
import eternity.DataTypes.DataAction;
import eternity.DataTypes.DataClass;
import eternity.DataTypes.DataLevel;
import eternity.DataTypes.DataStatus;

/**
 * Leader minion slots and runtime combat actors.
 * Slot form/type/summoned flags are persisted; combat states are encounter-only.
 */
public class CharMinions {
    private static final String[] MINION_ATTRIBUTE_KEYS = {
            "STR", "DEX", "CON", "FOC", "CTL", "CAP", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB"
    };
    private static final String TRAINING_STATUS_PREFIX = "Training Passive: ";
    private static final String MAINTAINED_STATUS_PREFIX = "Panel Maintained: ";
    private static final String[] LEADER_TECHNIQUE_STATUS_PREFIXES = {
            TRAINING_STATUS_PREFIX, MAINTAINED_STATUS_PREFIX
    };

    @JsonIgnore private StoreCharData owner;
    @JsonProperty("minionSlots") private final List<MinionSlotState> slots;
    @JsonIgnore private final List<MinionSlotState> ephemeralSlots;
    @JsonIgnore private final Map<String, MinionCombatState> combatBySlot;

    public CharMinions() {
        this.slots = new ArrayList<>();
        this.ephemeralSlots = new ArrayList<>();
        this.combatBySlot = new LinkedHashMap<>();
    }

    public void setOwner(StoreCharData owner) {
        this.owner = owner;
    }

    @JsonProperty("minionSlots")
    public List<MinionSlotState> getSlots() {
        return slots;
    }

    @JsonSetter("minionSlots")
    public void setSlots(List<MinionSlotState> incoming) {
        slots.clear();
        combatBySlot.clear();
        if (incoming == null) return;
        for (MinionSlotState slot : incoming) {
            if (slot == null || slot.getSlotTitle() == null || slot.getSlotTitle().isBlank()) continue;
            slots.add(new MinionSlotState(slot));
        }
    }

    public MinionSlotState getOrCreateSlot(String slotTitle) {
        String normalized = normalizeSlotTitle(slotTitle);
        if (normalized.isBlank()) return null;
        for (MinionSlotState slot : slots) {
            if (slot != null && normalized.equalsIgnoreCase(slot.getSlotTitle())) {
                return slot;
            }
        }
        MinionSlotState created = new MinionSlotState(normalized);
        slots.add(created);
        return created;
    }

    public MinionSlotState findSlot(String slotTitle) {
        String normalized = normalizeSlotTitle(slotTitle);
        if (normalized.isBlank()) return null;
        for (MinionSlotState slot : slots) {
            if (slot != null && normalized.equalsIgnoreCase(slot.getSlotTitle())) {
                return slot;
            }
        }
        for (MinionSlotState slot : ephemeralSlots) {
            if (slot != null && normalized.equalsIgnoreCase(slot.getSlotTitle())) {
                return slot;
            }
        }
        return null;
    }

    public void syncUnlockedSlots(List<String> unlockedTitles) {
        LinkedHashMap<String, MinionSlotState> existing = new LinkedHashMap<>();
        for (MinionSlotState slot : slots) {
            if (slot == null || slot.getSlotTitle() == null || slot.getSlotTitle().isBlank()) continue;
            existing.put(slot.getSlotTitle().trim().toLowerCase(Locale.ROOT), slot);
        }
        slots.clear();
        if (unlockedTitles == null) {
            combatBySlot.clear();
            return;
        }
        for (String title : unlockedTitles) {
            String normalized = normalizeSlotTitle(title);
            if (normalized.isBlank()) continue;
            MinionSlotState prior = existing.get(normalized.toLowerCase(Locale.ROOT));
            if (prior != null) {
                prior.setSlotTitle(normalized);
                slots.add(prior);
            } else {
                slots.add(new MinionSlotState(normalized));
            }
        }
        combatBySlot.keySet().removeIf(key -> {
            for (MinionSlotState slot : slots) {
                if (slot != null && key.equalsIgnoreCase(slot.getSlotTitle()) && slot.isSummoned()) {
                    return false;
                }
            }
            return true;
        });
    }

    public void updateSlot(String slotTitle, boolean summoned, String form, String type) {
        MinionSlotState slot = getOrCreateSlot(slotTitle);
        if (slot == null) return;
        boolean wasSummoned = slot.isSummoned();
        slot.setSummoned(summoned);
        slot.setForm(form);
        slot.setType(type);
        if (!summoned) {
            removeCombat(slot.getSlotTitle());
        } else if (!wasSummoned && owner != null && owner.getCombat() != null && owner.getCombat().isInCombat()) {
            ensureCombat(slot.getSlotTitle());
            rebuildMinionActions(slot.getSlotTitle());
        } else if (summoned) {
            rebuildMinionActions(slot.getSlotTitle());
        }
    }

    @JsonIgnore
    public List<MinionSlotState> getSummonedSlots() {
        ArrayList<MinionSlotState> summoned = new ArrayList<>();
        for (MinionSlotState slot : slots) {
            if (slot != null && slot.isSummoned()) {
                summoned.add(slot);
            }
        }
        for (MinionSlotState slot : ephemeralSlots) {
            if (slot != null && slot.isSummoned()) {
                summoned.add(slot);
            }
        }
        return summoned;
    }

    @JsonIgnore
    public List<SummonedMinionActor> getSummonedMinionActors() {
        ArrayList<SummonedMinionActor> actors = new ArrayList<>();
        for (MinionSlotState slot : getSummonedSlots()) {
            if (slot == null) continue;
            CharAttributes attrs = buildMinionAttributes(slot);
            double attackBonus = attrs == null ? 0.0 : Math.max(0.0, attrs.calcStatusValue("ATK"));
            actors.add(new SummonedMinionActor(
                    slot.getSlotTitle(),
                    slot.buildDisplayName(),
                    attackBonus,
                    slot.getForm(),
                    slot.getType(),
                    slot.getInitiativeOffset()));
        }
        return actors;
    }

    public MinionCombatState ensureCombat(String slotTitle) {
        String normalized = normalizeSlotTitle(slotTitle);
        if (normalized.isBlank()) return null;
        MinionSlotState slot = findSlot(normalized);
        if (slot == null || !slot.isSummoned()) return null;
        MinionCombatState existing = combatBySlot.get(normalized.toLowerCase(Locale.ROOT));
        if (existing != null) return existing;
        MinionCombatState created = new MinionCombatState(normalized);
        if (owner != null && owner.getCombat() != null && owner.getCombat().isInCombat()) {
            created.startCombat();
        }
        combatBySlot.put(normalized.toLowerCase(Locale.ROOT), created);
        return created;
    }

    public MinionCombatState getCombat(String slotTitle) {
        String normalized = normalizeSlotTitle(slotTitle);
        if (normalized.isBlank()) return null;
        return combatBySlot.get(normalized.toLowerCase(Locale.ROOT));
    }

    public void removeCombat(String slotTitle) {
        String normalized = normalizeSlotTitle(slotTitle);
        if (normalized.isBlank()) return;
        MinionCombatState state = combatBySlot.remove(normalized.toLowerCase(Locale.ROOT));
        if (state != null) state.endCombat();
    }

    public void startCombatForSummoned() {
        for (MinionSlotState slot : getSummonedSlots()) {
            if (slot == null) continue;
            MinionCombatState combat = ensureCombat(slot.getSlotTitle());
            if (combat != null) {
                combat.startCombat();
                rebuildMinionActions(slot.getSlotTitle());
            }
        }
    }

    public void endCombatForAll() {
        for (MinionCombatState combat : combatBySlot.values()) {
            if (combat != null) combat.endCombat();
        }
        combatBySlot.clear();
        removeFlashMobCopies();
    }

    public void rebuildAllSummonedActions() {
        for (MinionSlotState slot : getSummonedSlots()) {
            if (slot != null) rebuildMinionActions(slot.getSlotTitle());
        }
    }

    public void rebuildMinionActions(String slotTitle) {
        MinionSlotState slot = findSlot(slotTitle);
        if (slot == null || !slot.isSummoned()) return;
        MinionCombatState combatState = ensureCombat(slot.getSlotTitle());
        if (combatState == null || combatState.getCombat() == null) return;

        CharCombat combat = combatState.getCombat();
        combat.clearActionBuckets();

        StoreRuleManager dataQuery = new StoreRuleManager();
        CharAttributes attrs = buildMinionAttributes(slot);

        DataAction move = combat.generateStandardMoveAction(dataQuery);
        move.setCharacter(owner);
        combat.routeAction(move);

        String type = slot.getType() == null ? "" : slot.getType().trim();
        if ("Melee".equalsIgnoreCase(type) || type.isBlank()) {
            DataAction attack = combat.generateStandardAttack(dataQuery, true);
            syncActionCombatStats(attack, attrs, true);
            attack.setRanged(0);
            attack.setCharacter(owner);
            combat.routeAction(attack);
        } else if ("Ranged".equalsIgnoreCase(type)) {
            DataAction attack = combat.generateStandardAttack(dataQuery, true);
            syncActionCombatStats(attack, attrs, true);
            attack.setRanged((int) Math.round(Math.max(1.0, attrs.calcStatusValue("RANGE"))));
            attack.setCharacter(owner);
            combat.routeAction(attack);
        } else if ("Heal".equalsIgnoreCase(type)) {
            DataAction healCast = combat.generateStandardAttack(dataQuery, false);
            syncActionCombatStats(healCast, attrs, false);
            healCast.setName("Standard Heal");
            healCast.setCategory("Heal");
            healCast.setCharacter(owner);
            combat.routeAction(healCast);
        } else if ("Support".equalsIgnoreCase(type)) {
            // Support starts with Move only in v1 unless specialty grants add more.
        } else if ("Aura".equalsIgnoreCase(type)) {
            // Aura starts with Move only in v1; aura techniques come from granted actions.
        }

        for (DataAction granted : resolveMinionGrantedActions(slot)) {
            if (granted == null) continue;
            DataAction copy = new DataAction(granted);
            copy.setCharacter(owner);
            combat.routeAction(copy);
        }
    }

    /**
     * Specialty grant hook for Leader abilities that add to minion action lists.
     */
    public List<DataAction> resolveMinionGrantedActions(String minionType, String minionForm) {
        return resolveMinionGrantedActions(null);
    }

    public List<DataAction> resolveMinionGrantedActions(MinionSlotState slot) {
        ArrayList<DataAction> granted = new ArrayList<>();
        if (owner == null || owner.getSpecials() == null) return granted;
        boolean flashMobCopy = slot != null && slot.isFlashMobCopy();
        if (flashMobCopy) {
            // Copies cannot use class abilities unless Leader has a Support subclass (Gambit exception).
            if (ownerHasSupportSubclass() && ownerHasGambitSpecialty()) {
                DataAction gambitMinion = new StoreRuleManager().getActionByName("Gambit (Minion)");
                if (gambitMinion != null) {
                    granted.add(gambitMinion);
                }
            }
            return granted;
        }
        if (ownerHasGambitSpecialty()) {
            DataAction gambitMinion = new StoreRuleManager().getActionByName("Gambit (Minion)");
            if (gambitMinion != null) {
                granted.add(gambitMinion);
            }
        }
        return granted;
    }

    public String createFlashMobCopy(String sourceSlotTitle, boolean minionMoved, String adjacentTileNote) {
        MinionSlotState source = findSlot(sourceSlotTitle);
        if (source == null || !source.isSummoned() || source.isFlashMobCopy()) return "";
        String copyTitle = nextFlashMobCopyTitle(source.getSlotTitle());
        MinionSlotState copy = new MinionSlotState(
                copyTitle,
                true,
                source.getForm(),
                source.getType(),
                true,
                source.getSlotTitle());
        ephemeralSlots.add(copy);
        MinionCombatState combatState = ensureCombat(copyTitle);
        if (combatState != null) {
            // Cap HP at 1: set lostHp so current stays at 1 once max is forced.
            combatState.setLostHp(0.0);
        }
        rebuildMinionActions(copyTitle);
        return copyTitle;
    }

    public void removeFlashMobCopies() {
        ArrayList<String> titles = new ArrayList<>();
        for (MinionSlotState slot : ephemeralSlots) {
            if (slot != null && slot.getSlotTitle() != null) {
                titles.add(slot.getSlotTitle());
            }
        }
        ephemeralSlots.clear();
        for (String title : titles) {
            removeCombat(title);
        }
    }

    private String nextFlashMobCopyTitle(String sourceTitle) {
        String base = (sourceTitle == null || sourceTitle.isBlank() ? "Minion" : sourceTitle.trim()) + " Copy";
        String candidate = base;
        int index = 2;
        while (findSlot(candidate) != null) {
            candidate = base + " " + index;
            index++;
        }
        return candidate;
    }

    private boolean ownerHasSupportSubclass() {
        DataClass effectiveClass = resolveEffectiveClass();
        return effectiveClass != null
                && effectiveClass.getRole() != null
                && "Support".equalsIgnoreCase(effectiveClass.getRole().trim());
    }

    private boolean ownerHasGambitSpecialty() {
        if (owner == null || owner.getSpecials() == null) return false;
        return owner.getSpecials().hasSpecialty("Gambit")
                || owner.getSpecials().hasSpecialty("Gambit (Minion)")
                || owner.getSpecials().hasSpecialty("Gambit (Leader)");
    }

    public CharAttributes buildMinionAttributes(MinionSlotState slot) {
        CharAttributes minionAttributes = new CharAttributes();
        if (slot == null || owner == null) return minionAttributes;
        double power = resolveMinionPowerValue(slot);
        for (String attributeKey : MINION_ATTRIBUTE_KEYS) {
            setBaseAttributeValue(minionAttributes, attributeKey, power);
        }
        applyLeaderClassValues(minionAttributes);
        applyLeaderTechniqueBonuses(minionAttributes);
        String primaryAttribute = resolveMinionPrimaryAttributeKey(minionAttributes, slot.getType());
        minionAttributes.setPrimaryAttributeOverride(primaryAttribute);
        minionAttributes.refreshLinkedAttributeStatuses();
        applyFormBonuses(minionAttributes, power, slot.getForm());
        double typePrimaryValue = primaryAttribute == null
                ? power
                : Math.max(0.0, minionAttributes.calcStatusValue(primaryAttribute));
        applyTypeBonuses(minionAttributes, typePrimaryValue, power, slot.getType());
        applyMinionRange(minionAttributes, slot.getType());
        minionAttributes.refreshLinkedAttributeStatuses();
        return minionAttributes;
    }

    public int calcMaxHp(String slotTitle) {
        return calcMaxHp(findSlot(slotTitle));
    }

    public int calcMaxHp(MinionSlotState slot) {
        if (slot == null || owner == null) return 0;
        if (slot.isFlashMobCopy()) {
            return 1;
        }
        StoreRuleManager dataQuery = new StoreRuleManager();
        DataLevel levelData = dataQuery.getLevel(Math.max(1, owner.getLevel()));
        DataClass effectiveClass = resolveEffectiveClass();
        if (levelData == null || effectiveClass == null) return 0;
        double baseHp = Math.max(0.0, levelData.getBaseHP() * effectiveClass.getHpScaling())
                + halfLeaderTechniqueResourceBonus("BASEHP");
        double power = resolveMinionPowerValue(slot);
        double meleeConBonus = isMeleeMinionType(slot.getType())
                ? Math.round((power * 0.25) * 10.0) / 10.0
                : 0.0;
        double conValue = Math.max(0.0, power + halfLeaderTechniqueAttributeBonus("BCON") + meleeConBonus);
        double hpMultiplier = 1.0 + (Math.round(conValue * 0.5) / 10.0)
                + halfLeaderTechniqueResourceBonus("MULTIHP");
        return (int) Math.max(0.0, baseHp * hpMultiplier * resolveFormHpScale(slot));
    }

    public int getCurrentHp(String slotTitle) {
        int maxHp = calcMaxHp(slotTitle);
        MinionCombatState combatState = getCombat(slotTitle);
        double lost = combatState == null ? 0.0 : combatState.getLostHp();
        return (int) Math.max(0.0, maxHp - lost);
    }

    /** Apply raw post-mitigation damage to a minion with no further mitigation. */
    public double applyUnmitigatedDamage(String slotTitle, double amount) {
        double safeAmount = Math.max(0.0, amount);
        if (safeAmount <= 0.0) return 0.0;
        MinionCombatState combatState = ensureCombat(slotTitle);
        if (combatState == null) return 0.0;
        combatState.addLostHp(safeAmount);
        return safeAmount;
    }

    public double resolveMinionPowerValue(String slotTitle) {
        return resolveMinionPowerValue(findSlot(slotTitle));
    }

    public double resolveMinionPowerValue(MinionSlotState slot) {
        return scalePowerForSlot(resolveLeaderPowerValue(), slot);
    }

    public String resolveMinionPrimaryAttributeKey(MinionSlotState slot) {
        if (slot == null || owner == null) return null;
        CharAttributes probe = new CharAttributes();
        double power = resolveMinionPowerValue(slot);
        for (String attributeKey : MINION_ATTRIBUTE_KEYS) {
            setBaseAttributeValue(probe, attributeKey, power);
        }
        applyLeaderTechniqueBonuses(probe);
        return resolveMinionPrimaryAttributeKey(probe, slot.getType());
    }

    private String resolveMinionPrimaryAttributeKey(CharAttributes attributes, String type) {
        if (attributes == null) return null;
        String normalized = type == null ? "" : type.trim();
        if (normalized.isBlank() || "Melee".equalsIgnoreCase(normalized)) {
            return higherAttributeKey(attributes, "STR", "DEX");
        }
        if ("Ranged".equalsIgnoreCase(normalized)) {
            return higherAttributeKey(attributes, "FOC", "CTL");
        }
        return null;
    }

    private static String higherAttributeKey(CharAttributes attributes, String firstKey, String secondKey) {
        double firstValue = Math.max(0.0, attributes.calcStatusValue(firstKey));
        double secondValue = Math.max(0.0, attributes.calcStatusValue(secondKey));
        return secondValue > firstValue ? secondKey : firstKey;
    }

    private double resolveLeaderPowerValue() {
        if (owner == null || owner.getAttributes() == null) return 0.0;
        DataClass effectiveClass = resolveEffectiveClass();
        if (effectiveClass == null || effectiveClass.getPrimaryAtt() == null || effectiveClass.getPrimaryAtt().isBlank()) {
            return 0.0;
        }
        double primaryValue = Math.max(0.0, owner.getAttributes().calcStatusValue(effectiveClass.getPrimaryAtt().trim().toUpperCase()));
        double levelValue = owner.getIdentity() == null ? 0.0 : Math.max(1, owner.getIdentity().getLevel());
        return Math.round(((primaryValue * 0.5) + levelValue) * 10.0) / 10.0;
    }

    private double scalePowerForSlot(double power, MinionSlotState slot) {
        String title = resolvePowerScaleTitle(slot);
        if (title.startsWith("minor")) {
            return Math.round((power / 3.0) * 10.0) / 10.0;
        }
        if (title.startsWith("medium")) {
            return Math.round((power / 2.0) * 10.0) / 10.0;
        }
        return power;
    }

    private double resolveFormHpScale(MinionSlotState slot) {
        String form = slot == null || slot.getForm() == null ? "" : slot.getForm().trim();
        if ("Generalized".equalsIgnoreCase(form) || "Generalize".equalsIgnoreCase(form)) {
            return 0.7;
        }
        if ("Damaging".equalsIgnoreCase(form)) {
            return 0.4;
        }
        return 1.0;
    }

    private String resolvePowerScaleTitle(MinionSlotState slot) {
        if (slot == null) return "";
        if (slot.isFlashMobCopy() && slot.getFlashMobSourceSlot() != null && !slot.getFlashMobSourceSlot().isBlank()) {
            return slot.getFlashMobSourceSlot().trim().toLowerCase(Locale.ROOT);
        }
        return slot.getSlotTitle() == null ? "" : slot.getSlotTitle().trim().toLowerCase(Locale.ROOT);
    }

    private DataClass resolveEffectiveClass() {
        if (owner == null || owner.getIdentity() == null) return null;
        StoreRuleManager dataQuery = new StoreRuleManager();
        String subclassName = owner.getIdentity().getCharSubclass();
        if (subclassName != null && !subclassName.isBlank() && !"***".equals(subclassName.trim()) && !"?".equals(subclassName.trim())) {
            DataClass subclass = dataQuery.getClassByName(subclassName);
            if (subclass != null) return subclass;
        }
        String className = owner.getIdentity().getCharClass();
        if (className == null || className.isBlank() || "?".equals(className.trim())) return null;
        return dataQuery.getClassByName(className);
    }

    private void applyLeaderTechniqueBonuses(CharAttributes minionAttributes) {
        if (minionAttributes == null || owner == null || owner.getAttributes() == null) return;
        for (DataStatus status : owner.getAttributes().collectStatusesByNamePrefixes(LEADER_TECHNIQUE_STATUS_PREFIXES)) {
            if (!isShareableLeaderTechniqueStatus(status)) continue;
            DataStatus copy = new DataStatus(status);
            copy.setSeverity(halfTechniqueSeverity(status.getSeverity()));
            if (Math.abs(copy.getSeverity()) < 0.0001) continue;
            minionAttributes.addStatus(copy);
        }
    }

    private double halfLeaderTechniqueAttributeBonus(String attribute) {
        if (owner == null || owner.getAttributes() == null) return 0.0;
        double total = 0.0;
        for (DataStatus status : owner.getAttributes().collectStatusesByNamePrefixes(LEADER_TECHNIQUE_STATUS_PREFIXES)) {
            if (!isShareableLeaderTechniqueStatus(status)) continue;
            if (attribute.equalsIgnoreCase(status.getAttribute())) {
                total += halfTechniqueSeverity(status.getSeverity());
            }
        }
        return total;
    }

    private double halfLeaderTechniqueResourceBonus(String attribute) {
        if (owner == null || owner.getResources() == null || attribute == null) return 0.0;
        ArrayList<DataStatus>[] block = switch (attribute.trim().toUpperCase(Locale.ROOT)) {
            case "BASEHP" -> owner.getResources().getBaseHP();
            case "MULTIHP" -> owner.getResources().getMultiHP();
            default -> null;
        };
        if (block == null) return 0.0;
        double total = 0.0;
        for (ArrayList<DataStatus> statuses : block) {
            if (statuses == null) continue;
            for (DataStatus status : statuses) {
                if (!isShareableLeaderTechniqueStatus(status)) continue;
                total += halfTechniqueSeverity(status.getSeverity());
            }
        }
        return total;
    }

    private static boolean isShareableLeaderTechniqueStatus(DataStatus status) {
        if (status == null || status.getName() == null) return false;
        if (!matchesLeaderTechniquePrefix(status.getName())) return false;
        String durationType = status.getDurationType();
        return "Passive".equalsIgnoreCase(durationType) || "Maintained".equalsIgnoreCase(durationType);
    }

    private static boolean matchesLeaderTechniquePrefix(String name) {
        if (name == null) return false;
        for (String prefix : LEADER_TECHNIQUE_STATUS_PREFIXES) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }

    private static double halfTechniqueSeverity(double severity) {
        return Math.round((severity * 0.5) * 10.0) / 10.0;
    }

    private void applyLeaderClassValues(CharAttributes minionAttributes) {
        if (minionAttributes == null || owner == null) return;
        DataClass effectiveClass = resolveEffectiveClass();
        if (effectiveClass == null) return;
        StoreRuleManager dataQuery = new StoreRuleManager();
        DataLevel levelData = dataQuery.getLevel(Math.max(1, owner.getLevel()));
        if (levelData == null) return;
        int[] statScale = effectiveClass.getStatScaling();
        int[] levelScalers = levelData.getScalers();
        if (statScale == null || levelScalers == null) return;

        setBaseCategoryValue(minionAttributes.getBDefense(), "BFORT",
                resolveTieredScalerSeverity(statScale, 0, levelScalers), "Class-based Fortitude base");
        setBaseCategoryValue(minionAttributes.getBDefense(), "BREF",
                resolveTieredScalerSeverity(statScale, 1, levelScalers), "Class-based Reflex base");
        setBaseCategoryValue(minionAttributes.getBDefense(), "BWILL",
                resolveTieredScalerSeverity(statScale, 2, levelScalers), "Class-based Will base");
        double attackValue = resolveTieredScalerSeverity(statScale, 3, levelScalers);
        setBaseCategoryValue(minionAttributes.getBCombat(), "BATK", attackValue, "Class-based Attack base");
        double applyValue = statScale.length > 4
                ? resolveTieredScalerSeverity(statScale, 4, levelScalers)
                : attackValue;
        setBaseCategoryValue(minionAttributes.getBCombat(), "BAPP", applyValue, "Class-based Application base");
    }

    private void applyMinionRange(CharAttributes attributes, String type) {
        if (attributes == null) return;
        String normalized = type == null ? "" : type.trim();
        if (isMeleeMinionType(normalized)) {
            lockMinionRangeToZero(attributes);
            return;
        }
        if (isRangedRangeMinionType(normalized)) {
            setBaseCategoryValue(attributes.getBCombat(), "BRANGE", 20.0, "Generalized Range base");
        }
    }

    private void lockMinionRangeToZero(CharAttributes attributes) {
        clearNonBaseStatuses(attributes.getBCombat(), "BRANGE");
        clearNonBaseStatuses(attributes.getMCombat(), "MRANGE");
        setBaseCategoryValue(attributes.getBCombat(), "BRANGE", 0.0, "Melee minion range lock");
    }

    private static boolean isMeleeMinionType(String type) {
        return type == null || type.isBlank() || "Melee".equalsIgnoreCase(type);
    }

    private static boolean isRangedRangeMinionType(String type) {
        return "Ranged".equalsIgnoreCase(type)
                || "Support".equalsIgnoreCase(type)
                || "Aura".equalsIgnoreCase(type);
    }

    private void clearNonBaseStatuses(ArrayList<DataStatus>[][] category, String attribute) {
        if (category == null || attribute == null || attribute.isBlank()) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus first = block[0].get(0);
            if (first == null || !attribute.equalsIgnoreCase(first.getAttribute())) continue;
            for (ArrayList<DataStatus> statuses : block) {
                if (statuses == null) continue;
                statuses.removeIf(status -> status != null
                        && status.getName() != null
                        && !"Base".equalsIgnoreCase(status.getName()));
            }
            return;
        }
    }

    private static double resolveTieredScalerSeverity(int[] statScale, int statScaleIndex, int[] levelScalers) {
        if (statScale == null || levelScalers == null || statScaleIndex < 0 || statScaleIndex >= statScaleLength(statScale)) {
            return 0.0;
        }
        int scalerTier = statScale[statScaleIndex];
        if (scalerTier <= 0 || scalerTier > levelScalers.length) return 0.0;
        return levelScalers[scalerTier - 1];
    }

    private static int statScaleLength(int[] statScale) {
        return statScale == null ? 0 : statScale.length;
    }

    private void setBaseCategoryValue(ArrayList<DataStatus>[][] category, String attribute, double value, String description) {
        if (category == null || attribute == null || attribute.isBlank()) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus baseStatus = block[0].get(0);
            if (baseStatus == null || !attribute.equalsIgnoreCase(baseStatus.getAttribute())) continue;
            baseStatus.setSeverity(value);
            if (description != null && !description.isBlank()) {
                baseStatus.setDescription(description);
            }
            return;
        }
    }

    private void setBaseAttributeValue(CharAttributes attributes, String attributeKey, double value) {
        if (attributes == null || attributeKey == null || attributeKey.isBlank()) return;
        String normalized = "B" + attributeKey.trim().toUpperCase();
        for (ArrayList<DataStatus>[] block : attributes.getBAttributes()) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus baseStatus = block[0].get(0);
            if (baseStatus == null || !normalized.equalsIgnoreCase(baseStatus.getAttribute())) continue;
            baseStatus.setSeverity(value);
            baseStatus.setDescription("Derived from leader POWER");
            return;
        }
    }

    private void applyFormBonuses(CharAttributes attributes, double powerValue, String form) {
        if (attributes == null) return;
        String normalized = form == null ? "" : form.trim();
        if ("Generalized".equalsIgnoreCase(normalized) || "Generalize".equalsIgnoreCase(normalized)) {
            double halfPower = Math.round((powerValue * 0.5) * 10.0) / 10.0;
            addPassiveBonus(attributes.getBDefense(), "BARMOR", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBDefense(), "BFORT", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBDefense(), "BREF", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBDefense(), "BWILL", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBCombat(), "BATK", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBCombat(), "BAPP", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBDamage(), "BBDMG", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBDamage(), "BTDMG", halfPower, "Generalized Form");
            addPassiveBonus(attributes.getBResist(), "BALL", halfPower, "Generalized Form");
            return;
        }
        if ("Damaging".equalsIgnoreCase(normalized)) {
            addPassiveBonus(attributes.getBCombat(), "BATK", powerValue, "Damaging Form");
            addPassiveBonus(attributes.getBCombat(), "BAPP", powerValue, "Damaging Form");
            addPassiveBonus(attributes.getBDamage(), "BBDMG", powerValue, "Damaging Form");
            addPassiveBonus(attributes.getBDamage(), "BTDMG", powerValue, "Damaging Form");
            return;
        }
        if ("Durable".equalsIgnoreCase(normalized)) {
            addPassiveBonus(attributes.getBDefense(), "BARMOR", powerValue, "Durable Form");
            addPassiveBonus(attributes.getBDefense(), "BFORT", powerValue, "Durable Form");
            addPassiveBonus(attributes.getBDefense(), "BREF", powerValue, "Durable Form");
            addPassiveBonus(attributes.getBDefense(), "BWILL", powerValue, "Durable Form");
            addPassiveBonus(attributes.getBResist(), "BALL", powerValue, "Durable Form");
        }
    }

    private void applyTypeBonuses(CharAttributes attributes, double primaryAttributeValue, double powerValue, String type) {
        if (attributes == null) return;
        if (!isMeleeMinionType(type)) return;
        double severity = Math.round((primaryAttributeValue * 0.25) * 10.0) / 10.0;
        addPassiveBonus(attributes.getBDamage(), "BTDMG", severity, "Melee Type");
        addPassiveBonus(attributes.getBDamage(), "BBDMG", severity, "Melee Type");
        addPassiveBonus(attributes.getBCombat(), "BATK", severity, "Melee Type");
        addPassiveBonus(attributes.getBDamage(), "BTHEAL", severity, "Melee Type");
        addPassiveBonus(attributes.getBDamage(), "BBHEAL", severity, "Melee Type");
        addPassiveBonus(attributes.getBCombat(), "BAPP", severity, "Melee Type");
        double powerSeverity = Math.round((powerValue * 0.25) * 10.0) / 10.0;
        addPassiveBonus(attributes.getBAttributes(), "BCON", powerSeverity, "Melee Type");
        addPassiveBonus(attributes.getBResist(), "BALL", powerSeverity, "Melee Type");
    }

    private void addPassiveBonus(ArrayList<DataStatus>[][] category, String attribute, double severity, String sourceName) {
        if (category == null || attribute == null || sourceName == null) return;
        for (ArrayList<DataStatus>[] block : category) {
            if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
            DataStatus baseStatus = block[0].get(0);
            if (baseStatus == null || !attribute.equalsIgnoreCase(baseStatus.getAttribute())) continue;
            DataStatus bonusStatus = new DataStatus();
            bonusStatus.setName(sourceName);
            bonusStatus.setAttribute(attribute);
            bonusStatus.setAffinity("None");
            bonusStatus.setDescription("Derived from minion form");
            bonusStatus.setDurationType("Passive");
            bonusStatus.setSeverity(severity);
            block[0].add(bonusStatus);
            return;
        }
    }

    private void syncActionCombatStats(DataAction action, CharAttributes attrs, boolean weaponAttack) {
        if (action == null || attrs == null) return;
        if (weaponAttack) {
            action.setAtk((int) Math.round(Math.max(0.0, attrs.calcStatusValue("ATK"))));
        } else {
            action.setAtk((int) Math.round(Math.max(0.0, attrs.calcStatusValue("APP"))));
        }
        action.setBdmg((int) Math.round(Math.max(0.0, attrs.calcStatusValue("BDMG"))));
        action.setTdmg((int) Math.round(Math.max(0.0, attrs.calcStatusValue("TDMG"))));
    }

    private static String normalizeSlotTitle(String slotTitle) {
        return slotTitle == null ? "" : slotTitle.trim();
    }

    public static record SummonedMinionActor(
            String slotTitle,
            String displayName,
            double attackBonus,
            String form,
            String type,
            int initiativeOffset) {}
}
