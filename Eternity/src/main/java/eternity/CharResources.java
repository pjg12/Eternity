package eternity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tracks HP, Aura, Reactions, and Class Resources
 * using the modern StatBlock architecture.
 */
public class CharResources {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PASSIVE_STATUS = "Passive";

    @JsonIgnore
    private CharData owner;

    @JsonProperty("maxHP") private StatBlock[] maxHP;
    @JsonProperty("maxAura") private StatBlock[] maxAura;
    @JsonProperty("maxResource1") private StatBlock[] maxResource1;
    @JsonProperty("maxResource2") private StatBlock[] maxResource2;
    @JsonProperty("maxResource3") private StatBlock[] maxResource3;
    @JsonProperty("reactions") private StatBlock[] reactions;

    @JsonProperty private double lostHP;         // missing HP
    @JsonProperty private double spentAura;      // spent aura capacity
    @JsonProperty private double mainOccupiedAura;    // aura reserved by abilities (main)
    @JsonProperty private double grantOccupiedAura;   // aura reserved by granted abilities
    @JsonProperty private double shield;         // temporary shielding
    @JsonProperty private double stagger;        // stagger meter
    @JsonProperty private double spentR1;
    @JsonProperty private double spentR2;
    @JsonProperty private double spentR3;
    @JsonProperty private double spentReactions;

    public CharResources() {
        this.maxHP       = initSingle("HP");
        this.maxAura     = initSingle("AURA");
        this.maxResource1 = initSingle("RESOURCE1");
        this.maxResource2 = initSingle("RESOURCE2");
        this.maxResource3 = initSingle("RESOURCE3");
        this.reactions    = initSingle("REACTION");

        this.lostHP = 0;
        this.spentAura = 0;
        this.mainOccupiedAura = 0;
        this.grantOccupiedAura = 0;
        this.shield = 0;
        this.stagger = 0;
        this.spentR1 = 0;
        this.spentR2 = 0;
        this.spentR3 = 0;
        this.spentReactions = 0;
    }

    private StatBlock[] initSingle(String attributeKey) {
        StatBlock[] arr = new StatBlock[1];
        StatBlock block = new StatBlock();
        arr[0] = block;

        seedBaseStatuses(block, attributeKey);

        return arr;
    }

    // ---------------------------------------------------------
    //   DESERIALIZATION SAFETY (accept legacy numeric values)
    // ---------------------------------------------------------
    @JsonSetter("maxHP") private void setMaxHP(JsonNode node) { this.maxHP = coerceStatBlocks(node, "HP"); }
    @JsonSetter("maxAura") private void setMaxAura(JsonNode node) { this.maxAura = coerceStatBlocks(node, "AURA"); }
    @JsonSetter("maxResource1") private void setMaxResource1(JsonNode node) { this.maxResource1 = coerceStatBlocks(node, "RESOURCE1"); }
    @JsonSetter("maxResource2") private void setMaxResource2(JsonNode node) { this.maxResource2 = coerceStatBlocks(node, "RESOURCE2"); }
    @JsonSetter("maxResource3") private void setMaxResource3(JsonNode node) { this.maxResource3 = coerceStatBlocks(node, "RESOURCE3"); }
    @JsonSetter("reactions") private void setReactions(JsonNode node) { this.reactions = coerceStatBlocks(node, "REACTION"); }

    private StatBlock[] coerceStatBlocks(JsonNode node, String attributeKey) {
        if (node == null || node.isNull()) return initSingle(attributeKey);

        // Legacy format: numeric value instead of StatBlock array
        if (node.isNumber()) {
            StatBlock[] arr = initSingle(attributeKey);
            DataStatus passive = findPassiveStatus(arr[0]);
            if (passive != null) {
                passive.setSeverity(node.doubleValue());
            }
            return arr;
        }

        try {
            StatBlock[] parsed = MAPPER.convertValue(node, StatBlock[].class);
            return (parsed != null && parsed.length > 0) ? parsed : initSingle(attributeKey);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse StatBlock array, using defaults: " + e.getMessage());
            return initSingle(attributeKey);
        }
    }

    // ---------------------------------------------------------
    //   COMPUTED VALUES
    // ---------------------------------------------------------

    @JsonIgnore // derived value; exclude from serialization
    public int getMaxHP() {
        double base = maxHP[0].computeValueNoBase();
        return (int)Math.max(0, base);
    }
    @JsonIgnore // derived value; exclude from serialization
    public int getMaxAura() {
        double base = maxAura[0].computeValueNoBase();
        return (int)Math.max(0, base);
    }

    @JsonIgnore // derived value; exclude from serialization
    public int getMaxResource1() { return maxResource1[0].computeValue(); }
    @JsonIgnore // derived value; exclude from serialization
    public int getMaxResource2() { return maxResource2[0].computeValue(); }
    @JsonIgnore // derived value; exclude from serialization
    public int getMaxResource3() { return maxResource3[0].computeValue(); }

    @JsonIgnore // derived value; exclude from serialization
    public int getMaxReactions() { return reactions[0].computeValue(); }

    // ---------------------------------------------------------
    //   CURRENT VALUES
    // ---------------------------------------------------------

    @JsonIgnore // derived value; exclude from serialization
    public int getCurrentHP() { return (int)(getMaxHP() - lostHP); }
    @JsonIgnore // derived value; exclude from serialization
    public int getCurrentAura() { return (int)(getMaxAura() - spentAura - getOccupiedAura()); }
    @JsonIgnore // derived value; exclude from serialization
    public int getCurrentReactions() { return (int)(getMaxReactions() - spentReactions); }

    // ---------------------------------------------------------
    //   RESOURCE OPERATIONS
    // ---------------------------------------------------------

    public void damage(double amount) { lostHP = Math.min(getMaxHP(), lostHP + amount); }
    public void heal(double amount) { lostHP = Math.max(0, lostHP - amount); }
    
    public void spendAura(double amount) { spentAura = Math.min(getMaxAura(), spentAura + amount); }
    public void restoreAura(double amount) { spentAura = Math.max(0, spentAura - amount); }
    public void occupyAura(double amount) { mainOccupiedAura += amount; }
    public void freeAura(double amount) { mainOccupiedAura = Math.max(0, mainOccupiedAura - amount); }

    public void occupyAuraGrant(double amount) { grantOccupiedAura += amount; }
    public void freeAuraGrant(double amount) { grantOccupiedAura = Math.max(0, grantOccupiedAura - amount); }

    public void addShield(double amount) { shield += amount; }
    public void removeShield(double amount) { shield = Math.max(0, shield - amount); }

    public void spendReaction() { spentReactions = Math.min(getMaxReactions(), spentReactions + 1); }
    public void resetReactions() { spentReactions = 0; }

    // ---------------------------------------------------------
    //   GETTERS & SETTERS
    // ---------------------------------------------------------

    public double getLostHP() { return lostHP; }
    public void setLostHP(double lostHP) { this.lostHP = lostHP; }

    public double getSpentAura() { return spentAura; }
    public void setSpentAura(double spentAura) { this.spentAura = spentAura; }

    public double getOccupiedAura() { return mainOccupiedAura + grantOccupiedAura; }
    public void setOccupiedAura(double occupiedAura) { this.mainOccupiedAura = Math.max(0, occupiedAura); }
    public double getMainOccupiedAura() { return mainOccupiedAura; }
    public double getGrantOccupiedAura() { return grantOccupiedAura; }
    public void setGrantOccupiedAura(double v) { this.grantOccupiedAura = Math.max(0, v); }

    public double getShield() { return shield; }
    public void setShield(double shield) { this.shield = shield; }

    public double getStagger() { return stagger; }
    public void setStagger(double stagger) { this.stagger = stagger; }

    public double getSpentR1() { return spentR1; }
    public double getSpentR2() { return spentR2; }
    public double getSpentR3() { return spentR3; }

    public void setSpentR1(double v) { spentR1 = v; }
    public void setSpentR2(double v) { spentR2 = v; }
    public void setSpentR3(double v) { spentR3 = v; }

    public double getSpentReactions() { return spentReactions; }
    public void setSpentReactions(double spentReactions) { this.spentReactions = spentReactions; }

    @JsonIgnore // derived value; exclude from serialization
    public StatBlock[] getMaxHPBlocks() { return maxHP; }
    @JsonIgnore // derived value; exclude from serialization
    public StatBlock[] getMaxAuraBlocks() { return maxAura; }
    @JsonIgnore // derived value; exclude from serialization
    public StatBlock[] getMaxResource1Blocks() { return maxResource1; }
    @JsonIgnore // derived value; exclude from serialization
    public StatBlock[] getMaxResource2Blocks() { return maxResource2; }
    @JsonIgnore // derived value; exclude from serialization
    public StatBlock[] getMaxResource3Blocks() { return maxResource3; }
    @JsonIgnore // derived value; exclude from serialization
    public StatBlock[] getReactionBlocks() { return reactions; }

    @JsonIgnore
    public CharData getOwner() { return owner; }
    public void setOwner(CharData owner) { this.owner = owner; }

    // ---------------------------------------------------------
    //   BASE VALUE HELPERS
    // ---------------------------------------------------------

    /**
     * Updates the "Passive" status entry for Max HP with the supplied value.
     * If the status does not exist (e.g., older saved characters), it will be created.
     */
    public void setBaseMaxHP(double value) {
        setBaseStatusValue(maxHP, value, "HP");
    }

    /**
     * Updates the "Passive" status entry for Max Aura with the supplied value.
     */
    public void setBaseMaxAura(double value) {
        setBaseStatusValue(maxAura, value, "AURA");
    }

    /**
     * Shared helper to upsert the Passive status severity on the first StatBlock.
     */
    private void setBaseStatusValue(StatBlock[] blocks, double value, String attrKey) {
        if (blocks == null || blocks.length == 0) return;
        StatBlock block = blocks[0];
        DataStatus baseStatus = findOrCreatePassiveStatus(block, attrKey);
        baseStatus.setSeverity(Math.max(0, value));
    }

    private DataStatus findPassiveStatus(StatBlock block) {
        if (block == null) return null;
        for (DataStatus status : block.getStatus()) {
            if (status != null && PASSIVE_STATUS.equalsIgnoreCase(status.getName())) {
                return status;
            }
        }
        return null;
    }

    private DataStatus findOrCreatePassiveStatus(StatBlock block, String attrKey) {
        DataStatus baseStatus = findPassiveStatus(block);
        if (baseStatus != null) {
            return baseStatus;
        }

        DataStatus created = new DataStatus();
        created.setName(PASSIVE_STATUS);
        created.setAttribute(attrKey);
        created.setDurationType("Permanent");
        block.addStatus(created);
        return created;
    }

    /** Seeds a StatBlock with Passive/Maintained/Temporary base entries. */
    private void seedBaseStatuses(StatBlock block, String key) {
        if (block == null) return;
        String[] labels = { "Passive", "Maintained", "Temporary" };
        for (String label : labels) {
            DataStatus base = new DataStatus();
            base.setName(label);
            base.setAttribute(key);
            base.setDurationType("Permanent");
            // For resources, default Passive to 10 (legacy), others to 0
            base.setSeverity("Passive".equals(label) ? 10 : 0);
            block.addStatus(base);
        }
    }
}
