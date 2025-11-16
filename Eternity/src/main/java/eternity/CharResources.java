package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tracks HP, Aura, Reactions, and Class Resources
 * using the modern StatBlock architecture.
 */
public class CharResources {

    @JsonProperty("maxHP") private StatBlock[] maxHP;
    @JsonProperty("maxAura") private StatBlock[] maxAura;
    @JsonProperty("maxResource1") private StatBlock[] maxResource1;
    @JsonProperty("maxResource2") private StatBlock[] maxResource2;
    @JsonProperty("maxResource3") private StatBlock[] maxResource3;
    @JsonProperty("reactions") private StatBlock[] reactions;

    @JsonProperty private double lostHP;         // missing HP
    @JsonProperty private double spentAura;      // spent aura capacity
    @JsonProperty private double occupiedAura;   // aura reserved by abilities
    @JsonProperty private double shield;         // temporary shielding
    @JsonProperty private double stagger;        // stagger meter
    @JsonProperty private double spentR1;
    @JsonProperty private double spentR2;
    @JsonProperty private double spentR3;
    @JsonProperty private double spentReactions;

    public CharResources() {
        this.maxHP       = initSingle();
        this.maxAura     = initSingle();
        this.maxResource1 = initSingle();
        this.maxResource2 = initSingle();
        this.maxResource3 = initSingle();
        this.reactions    = initSingle();

        this.lostHP = 0;
        this.spentAura = 0;
        this.occupiedAura = 0;
        this.shield = 0;
        this.stagger = 0;
        this.spentR1 = 0;
        this.spentR2 = 0;
        this.spentR3 = 0;
        this.spentReactions = 0;
    }

    private StatBlock[] initSingle() {
        StatBlock[] arr = new StatBlock[1];
        StatBlock block = new StatBlock();
        arr[0] = block;

        DataStatus base = new DataStatus();
        base.setName("Base");
        base.setAttribute("RESOURCE");
        base.setDurationType("Permanent");
        block.addStatus(base);

        DataStatus attr = new DataStatus();
        attr.setName("Attribute");
        attr.setAttribute("RESOURCE");
        attr.setDurationType("Permanent");
        block.addStatus(attr);

        return arr;
    }

    // ---------------------------------------------------------
    //   COMPUTED VALUES
    // ---------------------------------------------------------

    public int getMaxHP() { return maxHP[0].computeValue(); }
    public int getMaxAura() { return maxAura[0].computeValue(); }

    public int getMaxResource1() { return maxResource1[0].computeValue(); }
    public int getMaxResource2() { return maxResource2[0].computeValue(); }
    public int getMaxResource3() { return maxResource3[0].computeValue(); }

    public int getMaxReactions() { return reactions[0].computeValue(); }

    // ---------------------------------------------------------
    //   CURRENT VALUES
    // ---------------------------------------------------------

    public int getCurrentHP() { return (int)(getMaxHP() - lostHP); }
    public int getCurrentAura() { return (int)(getMaxAura() - spentAura - occupiedAura); }
    public int getCurrentReactions() { return (int)(getMaxReactions() - spentReactions); }

    // ---------------------------------------------------------
    //   RESOURCE OPERATIONS
    // ---------------------------------------------------------

    public void damage(double amount) { lostHP = Math.min(getMaxHP(), lostHP + amount); }
    public void heal(double amount) { lostHP = Math.max(0, lostHP - amount); }
    
    public void spendAura(double amount) { spentAura = Math.min(getMaxAura(), spentAura + amount); }
    public void restoreAura(double amount) { spentAura = Math.max(0, spentAura - amount); }
    public void occupyAura(double amount) { occupiedAura += amount; }
    public void freeAura(double amount) { occupiedAura = Math.max(0, occupiedAura - amount); }

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

    public double getOccupiedAura() { return occupiedAura; }
    public void setOccupiedAura(double occupiedAura) { this.occupiedAura = occupiedAura; }

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

    public StatBlock[] getMaxHPBlocks() { return maxHP; }
    public StatBlock[] getMaxAuraBlocks() { return maxAura; }
    public StatBlock[] getMaxResource1Blocks() { return maxResource1; }
    public StatBlock[] getMaxResource2Blocks() { return maxResource2; }
    public StatBlock[] getMaxResource3Blocks() { return maxResource3; }
    public StatBlock[] getReactionBlocks() { return reactions; }
}