package eternity;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;


/**
 * Tracks HP, Aura, Reactions, Class Resources, and special race resources
 * using the modern StatBlock architecture.
 */
public class CharResources {
    @JsonIgnore private StoreCharData owner;                                                // Reference to main character data

    @JsonProperty("baseHP") private ArrayList<DataStatus>[] baseHP;                   // List of base HP modifiers
    @JsonProperty("multiHP") private ArrayList<DataStatus>[] multiHP;                     // List of HP multipliers
    @JsonProperty("baseAura") private ArrayList<DataStatus>[] baseAura;               // List of base Aura modifiers
    @JsonProperty("multiAura") private ArrayList<DataStatus>[] multiAura;                 // List of Aura multipliers
    @JsonProperty("baseResource1") private ArrayList<DataStatus>[] baseResource1;     // List of base Class Resource 1 modifiers
    @JsonProperty("multiResource1") private ArrayList<DataStatus>[] multiResource1;       // List of Class Resource 1 multipliers
    @JsonProperty("baseResource2") private ArrayList<DataStatus>[] baseResource2;     // List of base Class Resource 2 modifiers
    @JsonProperty("multiResource2") private ArrayList<DataStatus>[] multiResource2;       // List of Class Resource 2 multipliers
    @JsonProperty("baseResource3") private ArrayList<DataStatus>[] baseResource3;     // List of base Class Resource 3 modifiers (subclass resource)
    @JsonProperty("multiResource3") private ArrayList<DataStatus>[] multiResource3;       // List of Class Resource 3 multipliers (subclass resource)
    @JsonProperty("baseAngelPoints") private ArrayList<DataStatus>[] baseAngelPoints; // List of base Angel Point modifiers
    @JsonProperty("multiAngelPoints") private ArrayList<DataStatus>[] multiAngelPoints;   // List of Angel Point multipliers
    @JsonProperty("baseReactions") private ArrayList<DataStatus>[] baseReactions;     // List of base Reaction modifiers
    @JsonProperty("multiReactions") private ArrayList<DataStatus>[] multiReactions;       // List of Reaction multipliers

    @JsonProperty private double lostHP;                                            // Count of missing HP
    @JsonProperty private double spentAura;                                         // Count of spent aura
    @JsonProperty private double spentR1;                                           // Count of spent Class Resource 1  
    @JsonProperty private double spentR2;                                           // Count of spent Class Resource 2         
    @JsonProperty private double spentR3;                                           // Count of spent Class Resource 3 (subclass resource)
    @JsonProperty private double spentAngelPoints;                                  // Count of spent Angel Points
    @JsonProperty private double spentReactions;                                    // Count of spent Reactions

    @JsonProperty private double mainOccupiedAura;                                  // Aura currently occupied by maintained abilities
    @JsonProperty private double grantOccupiedAura;                                     // Aura currently occupied by abilities granted to allies
    @JsonProperty private double shield;                                            // Shield counter: absorbs damage before HP is lost
    @JsonProperty private double stagger;                                           // Stagger counter: absorbs partial damage, dealing HP damage over time

    public CharResources() {
        this.baseHP       = initStatus("BASEHP");
        this.multiHP       = initStatus("MULTIHP");
        this.baseAura     = initStatus("BASEAURA");
        this.multiAura     = initStatus("MULTIAURA");
        this.baseResource1 = initStatus("BASER1");
        this.multiResource1 = initStatus("MULTIR1");
        this.baseResource2 = initStatus("BASER2");
        this.multiResource2 = initStatus("MULTIR2");
        this.baseResource3 = initStatus("BASER3");
        this.multiResource3 = initStatus("MULTIR3");
        this.baseAngelPoints = initStatus("BASEANGEL");
        this.multiAngelPoints = initStatus("MULTIANGEL");
        this.baseReactions = initStatus("BASEREACT");
        this.multiReactions = initStatus("MULTIREACT");

        this.lostHP = 0.0;
        this.spentAura = 0.0;
        this.spentR1 = 0.0;
        this.spentR2 = 0.0;
        this.spentR3 = 0.0;
        this.spentAngelPoints = 0.0;
        this.spentReactions = 0.0;

        this.mainOccupiedAura = 0.0;
        this.grantOccupiedAura = 0.0;
        this.shield = 0.0;
        this.stagger = 0.0;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<DataStatus>[] initStatus(String attributeKey) {
        ArrayList<DataStatus>[] list = (ArrayList<DataStatus>[]) new ArrayList<?>[3];
        String[] labels = { "Passive", "Maintained", "Temporary" };
        for (int i = 0; i < 3; i++) {
            list[i] = new ArrayList<>();
            DataStatus base = new DataStatus();
            base.setName("Base");
            base.setAttribute(attributeKey);
            base.setSeverity(0);
            base.setDurationType(labels[i]);
            list[i].add(base);
        }
        return list;
    }

    // ---------------------------------------------------------
    //   COMPUTED MAX VALUES
    // ---------------------------------------------------------

    @JsonIgnore
    public double calcValue(ArrayList<DataStatus> list) {
        double result = 0.0;
        for (DataStatus status : list) {
            if (status != null) result += status.getSeverity();
        }
        return Math.max(0.0, result);
    }

    @JsonIgnore
    public int calcMaxValue(ArrayList<DataStatus>[] base, ArrayList<DataStatus>[] multi) {
        double baseValue = 0.0;
        double multiValue = 1.0;
        for (int i = 0; i < 3; i++) {
            baseValue += calcValue(base[i]);
            multiValue += calcValue(multi[i]);
        }
        return (int)Math.max(0, baseValue * multiValue);
    }

    @JsonIgnore public int calcMaxHP() { return calcMaxValue(baseHP, multiHP); }
    @JsonIgnore public int calcMaxAura() { return calcMaxValue(baseAura, multiAura); }
    @JsonIgnore public int calcMaxResource1() { return calcMaxValue(baseResource1, multiResource1); }
    @JsonIgnore public int calcMaxResource2() { return calcMaxValue(baseResource2, multiResource2); }
    @JsonIgnore public int calcMaxResource3() { return calcMaxValue(baseResource3, multiResource3); }
    @JsonIgnore public int calcMaxAngelPoints() { return calcMaxValue(baseAngelPoints, multiAngelPoints); }
    @JsonIgnore public int calcMaxReactions() { return calcMaxValue(baseReactions, multiReactions); }

    // ---------------------------------------------------------
    //   COMPUTED CURRENT VALUES
    // ---------------------------------------------------------

    @JsonIgnore public int calcCurrentHP() { return (int)(calcMaxHP() - lostHP); }
    @JsonIgnore public int calcCurrentAura() { return (int)(calcMaxAura() - spentAura - calcOccupiedAura()); }
    @JsonIgnore public int calcCurrentResource1() { return (int)(calcMaxResource1() - spentR1); }
    @JsonIgnore public int calcCurrentResource2() { return (int)(calcMaxResource2() - spentR2); }
    @JsonIgnore public int calcCurrentResource3() { return (int)(calcMaxResource3() - spentR3); }
    @JsonIgnore public int calcCurrentAngelPoints() { return (int)(calcMaxAngelPoints() - spentAngelPoints); }
    @JsonIgnore public int calcCurrentReactions() { return (int)(calcMaxReactions() - spentReactions); }

    public double calcOccupiedAura() { return mainOccupiedAura + grantOccupiedAura; }

    // ---------------------------------------------------------
    //   RESOURCE OPERATIONS
    // ---------------------------------------------------------

    /*public void damage(double amount) { lostHP = Math.min(calcMaxHP(), lostHP + amount); }
    public void heal(double amount) { lostHP = Math.max(0, lostHP - amount); }
    
    public void spendAura(double amount) { spentAura = Math.min(calcMaxAura(), spentAura + amount); }
    public void restoreAura(double amount) { spentAura = Math.max(0, spentAura - amount); }
    public void occupyAura(double amount) { mainOccupiedAura += amount; }
    public void freeAura(double amount) { mainOccupiedAura = Math.max(0, mainOccupiedAura - amount); }

    public void occupyAuraGrant(double amount) { grantOccupiedAura += amount; }
    public void freeAuraGrant(double amount) { grantOccupiedAura = Math.max(0, grantOccupiedAura - amount); }

    public void addShield(double amount) { shield += amount; }
    public void removeShield(double amount) { shield = Math.max(0, shield - amount); }

    public void spendReaction() { spentReactions = Math.min(calcMaxReactions(), spentReactions + 1); }
    public void resetReactions() { spentReactions = 0; }*/

    // ---------------------------------------------------------
    //   GETTERS & SETTERS
    // ---------------------------------------------------------

    public ArrayList<DataStatus>[] getBaseHP() { return baseHP != null ? baseHP : initStatus("BASEHP"); }
    public ArrayList<DataStatus>[] getMultiHP() { return multiHP != null ? multiHP : initStatus("MULTIHP"); }
    public ArrayList<DataStatus>[] getBaseAura() { return baseAura != null ? baseAura : initStatus("BASEAURA"); }
    public ArrayList<DataStatus>[] getMultiAura() { return multiAura != null ? multiAura : initStatus("MULTIAURA"); }
    public ArrayList<DataStatus>[] getBaseResource1() { return baseResource1 != null ? baseResource1 : initStatus("BASER1"); }
    public ArrayList<DataStatus>[] getMultiResource1() { return multiResource1 != null ? multiResource1 : initStatus("MULTIR1"); }
    public ArrayList<DataStatus>[] getBaseResource2() { return baseResource2 != null ? baseResource2 : initStatus("BASER2"); }
    public ArrayList<DataStatus>[] getMultiResource2() { return multiResource2 != null ? multiResource2 : initStatus("MULTIR2"); }
    public ArrayList<DataStatus>[] getBaseResource3() { return baseResource3 != null ? baseResource3 : initStatus("BASER3"); }
    public ArrayList<DataStatus>[] getMultiResource3() { return multiResource3 != null ? multiResource3 : initStatus("MULTIR3"); }
    public ArrayList<DataStatus>[] getBaseAngelPoints() { return baseAngelPoints != null ? baseAngelPoints : initStatus("BASEANGEL"); }
    public ArrayList<DataStatus>[] getMultiAngelPoints() { return multiAngelPoints != null ? multiAngelPoints : initStatus("MULTIANGEL"); }
    public ArrayList<DataStatus>[] getBaseReactions() { return baseReactions != null ? baseReactions : initStatus("BASEREACT"); }
    public ArrayList<DataStatus>[] getMultiReactions() { return multiReactions != null ? multiReactions : initStatus("MULTIREACT"); }
    
    @JsonSetter("baseHP") public void setBaseHP(ArrayList<DataStatus>[] list) { this.baseHP = list != null ? list : initStatus("BASEHP"); }
    @JsonSetter("multiHP") public void setMultiHP(ArrayList<DataStatus>[] list) { this.multiHP = list != null ? list : initStatus("MULTIHP"); }
    @JsonSetter("baseAura") public void setBaseAura(ArrayList<DataStatus>[] list) { this.baseAura = list != null ? list : initStatus("BASEAURA"); }
    @JsonSetter("multiAura") public void setMultiAura(ArrayList<DataStatus>[] list) { this.multiAura = list != null ? list : initStatus("MULTIAURA"); }
    @JsonSetter("baseResource1") public void setBaseResource1(ArrayList<DataStatus>[] list) { this.baseResource1 = list != null ? list : initStatus("BASER1"); }
    @JsonSetter("multiResource1") public void setMultiResource1(ArrayList<DataStatus>[] list) { this.multiResource1 = list != null ? list : initStatus("MULTIR1"); }
    @JsonSetter("baseResource2") public void setBaseResource2(ArrayList<DataStatus>[] list) { this.baseResource2 = list != null ? list : initStatus("BASER2"); }
    @JsonSetter("multiResource2") public void setMultiResource2(ArrayList<DataStatus>[] list) { this.multiResource2 = list != null ? list : initStatus("MULTIR2"); }
    @JsonSetter("baseResource3") public void setBaseResource3(ArrayList<DataStatus>[] list) { this.baseResource3 = list != null ? list : initStatus("BASER3"); }
    @JsonSetter("multiResource3") public void setMultiResource3(ArrayList<DataStatus>[] list) { this.multiResource3 = list != null ? list : initStatus("MULTIR3"); }
    @JsonSetter("baseAngelPoints") public void setBaseAngelPoints(ArrayList<DataStatus>[] list) { this.baseAngelPoints = list != null ? list : initStatus("BASEANGEL"); }
    @JsonSetter("multiAngelPoints") public void setMultiAngelPoints(ArrayList<DataStatus>[] list) { this.multiAngelPoints = list != null ? list : initStatus("MULTIANGEL"); }
    @JsonSetter("baseReactions") public void setBaseReactions(ArrayList<DataStatus>[] list) { this.baseReactions = list != null ? list : initStatus("BASEREACT"); }
    @JsonSetter("multiReactions") public void setMultiReactions(ArrayList<DataStatus>[] list) { this.multiReactions = list != null ? list : initStatus("MULTIREACT"); }
    
    public double getLostHP() { return lostHP; }
    public void setLostHP(double lostHP) { this.lostHP = lostHP; }
    public double getSpentAura() { return spentAura; }
    public void setSpentAura(double spentAura) { this.spentAura = spentAura; }

    public double getMainOccupiedAura() { return mainOccupiedAura; }
    public void setMainOccupiedAura(double occupiedAura) { this.mainOccupiedAura = Math.max(0, occupiedAura); }
    public double getGrantOccupiedAura() { return grantOccupiedAura; }
    public void setGrantOccupiedAura(double v) { this.grantOccupiedAura = Math.max(0, v); }

    public double getShield() { return shield; }
    public void setShield(double shield) { this.shield = shield; }
    public double getStagger() { return stagger; }
    public void setStagger(double stagger) { this.stagger = stagger; }

    public double getSpentR1() { return spentR1; }
    public double getSpentR2() { return spentR2; }
    public double getSpentR3() { return spentR3; }
    public double getSpentAngelPoints() { return spentAngelPoints; }
    public void setSpentR1(double spent) { spentR1 = spent; }
    public void setSpentR2(double spent) { spentR2 = spent; }
    public void setSpentR3(double spent) { spentR3 = spent; }
    public void setSpentAngelPoints(double spent) { spentAngelPoints = spent; }

    public double getSpentReactions() { return spentReactions; }
    public void setSpentReactions(double spentReactions) { this.spentReactions = spentReactions; }

    @JsonIgnore public StoreCharData getOwner() { return owner; }
    @JsonIgnore public void setOwner(StoreCharData owner) { this.owner = owner; }

    // ---------------------------------------------------------
    //   HELPERS
    // ---------------------------------------------------------

    public void addStatus (DataStatus status) {
        if (status == null || status.getAttribute() == null) return;
        ArrayList<DataStatus> list = findStatusArray(findStatusBlock(status.getAttribute().toUpperCase()), status.getDurationType().toUpperCase());
        DataStatus existing = findStatus(list, status.getName());
        if (existing != null) existing.setSeverity(status.getSeverity());
            // TODO better comparison logic for statuses of the same name? For now, just take the highest severity if a duplicate is added.
        else list.add(status); 
    }

    public void removeStatusByStatus(DataStatus status) {
        if (status == null || status.getName() == null || status.getAttribute() == null) return;
        ArrayList<DataStatus>[] block = findStatusBlock(status.getAttribute().toUpperCase());
        if (block == null) return;
        String name = status.getName();
        for (int i = 0; i < block.length; i++) {
            DataStatus existing = findStatus(block[i], name);
            if (existing != null) {
                block[i].remove(existing);
            }
        }
    }

    private ArrayList<DataStatus>[] findStatusBlock (String key) {
        return switch (key) {
            case "BASEHP" -> baseHP;
            case "MULTIHP" -> multiHP;
            case "BASEAURA" -> baseAura;
            case "MULTIAURA" -> multiAura;
            case "BASER1" -> baseResource1;
            case "MULTIR1" -> multiResource1;
            case "BASER2" -> baseResource2;
            case "MULTIR2" -> multiResource2;
            case "BASER3" -> baseResource3;
            case "MULTIR3" -> multiResource3;
            case "BASEANGEL" -> baseAngelPoints;
            case "MULTIANGEL" -> multiAngelPoints;
            case "BASEREACT" -> baseReactions;
            case "MULTIREACT" -> multiReactions;
            default -> null;
        };
    }

    private ArrayList<DataStatus> findStatusArray (ArrayList<DataStatus>[] block, String key) {
        return switch (key) {
            case "PASSIVE" -> block[0];
            case "MAINTAINED" -> block[1];
            case "TEMPORARY" -> block[2];
            default -> null;
        };
    }

    private DataStatus findStatus(ArrayList<DataStatus> list, String name) {
        for (DataStatus status : list) {
            if (status != null && name.equalsIgnoreCase(status.getName())) {
                return status;
            }
        }
        return null;
    }

}

