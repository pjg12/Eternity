package eternity;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Manages a character's inventory, currency, equipment, and proficiencies.
 */
public class CharInventory {

    @JsonProperty private double credits;
    @JsonProperty private String armor;            // current equipped armor name
    @JsonProperty private List<String> weaponProf;
    @JsonProperty private final List<DataItem> equipment;     // weapons, armor, accessories
    @JsonProperty private final List<DataItem> consumables;   // potions, repair kits
    @JsonProperty private final List<DataItem> goods;         // crafting materials, trade goods
    @JsonProperty private final List<DataItem> items;         // misc quest items

    // ---------------------------------------------------------
    //  Constructor
    // ---------------------------------------------------------

    public CharInventory() {
        this.credits = 0.0;
        this.armor = "";
        this.weaponProf = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.consumables = new ArrayList<>();
        this.goods = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    // ---------------------------------------------------------
    //  Currency Handling
    // ---------------------------------------------------------

    public double getCredits() { return credits; }
    public void addCredits(double amount) { credits = Math.max(0, credits + amount); }

    public boolean spendCredits(double amount) {
        if (credits >= amount) {
            credits -= amount;
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------
    //  Armor Handling
    // ---------------------------------------------------------

    public String getArmor() { return armor; }
    public void setArmor(String armorName) { this.armor = armorName != null ? armorName : ""; }

    // ---------------------------------------------------------
    //  Weapon Proficiencies
    // ---------------------------------------------------------

    public List<String> getWeaponProficiencies() { return Collections.unmodifiableList(weaponProf); }
    public void setWeaponProficiencies(List<String> weaponProf) { this.weaponProf = weaponProf; }
    public void addWeaponProficiency(String prof) { if (prof != null && !prof.isEmpty() && !weaponProf.contains(prof)) weaponProf.add(prof); }
    public void removeWeaponProficiency(String prof) { weaponProf.remove(prof); }
    public boolean hasWeaponProficiency(String prof) { return weaponProf.contains(prof); }

    // ---------------------------------------------------------
    //  Equipment Handling
    // ---------------------------------------------------------

    public List<DataItem> getEquipment() { return Collections.unmodifiableList(equipment); }
    public void addEquipment(DataItem item) { if (item != null) equipment.add(item); }
    public void removeEquipment(DataItem item) { equipment.remove(item); }

    public DataItem findEquipmentByName(String name) {
        for (DataItem item : equipment) {
            if (item.getIname().equalsIgnoreCase(name) ||
                item.getDname().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    // ---------------------------------------------------------
    //  Consumables (Auto-stacking)
    // ---------------------------------------------------------

    public List<DataItem> getConsumables() { return Collections.unmodifiableList(consumables); }
    
    public void addConsumable(DataItem item) {
        if (item == null) return;

        // Merge stack if same ID
        for (DataItem c : consumables) {
            if (c.getDid() == item.getDid()) {
                c.setQuantity(c.getQuantity() + item.getQuantity());
                return;
            }
        }
        consumables.add(item);
    }

    public void removeConsumable(DataItem item) { consumables.remove(item); }

    // ---------------------------------------------------------
    //  Trade Goods (Auto-stacking)
    // ---------------------------------------------------------

    public List<DataItem> getGoods() { return Collections.unmodifiableList(goods); }

    public void addGoods(DataItem item) {
        if (item == null) return;

        for (DataItem g : goods) {
            if (g.getDid() == item.getDid()) {
                g.setQuantity(g.getQuantity() + item.getQuantity());
                return;
            }
        }
        goods.add(item);
    }

    public void removeGoods(DataItem item) { goods.remove(item); }

    // ---------------------------------------------------------
    //  Misc Items (Quest/Key Items)
    // ---------------------------------------------------------

    public List<DataItem> getItems() { return Collections.unmodifiableList(items); }
    public void addItem(DataItem item) { if (item != null) items.add(item); }
    public void removeItem(DataItem item) { items.remove(item); }

    public DataItem findItemByName(String name) {
        for (DataItem item : items) {
            if (item.getIname().equalsIgnoreCase(name) ||
                item.getDname().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    // ---------------------------------------------------------
    //  Bulk Utility
    // ---------------------------------------------------------

    public void clearInventory() {
        equipment.clear();
        consumables.clear();
        goods.clear();
        items.clear();
    }

    public int totalItemCount() {
        return equipment.size() +
               consumables.size() +
               goods.size() +
               items.size();
    }
}