package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Manages a character's inventory, currency, equipment, and proficiencies.
 */
public class CharInventory {
    @JsonIgnore
    private StoreCharData owner;

    @JsonProperty private double credits;
    @JsonProperty private String armor;            // current equipped armor name
    @JsonProperty private List<String> weaponProf;
    @JsonProperty private final List<DataItemEquipment> equipment;     // weapons, armor, accessories
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

    public List<String> getWeaponProficiencies() {
        List<String> flattened = new ArrayList<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for (String entry : weaponProf) {
            if (entry == null) continue;
            String[] parts = entry.split(":");
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty() && seen.add(trimmed)) {
                    flattened.add(trimmed);
                }
            }
        }
        return Collections.unmodifiableList(flattened);
    }
    public void setWeaponProficiencies(List<String> weaponProf) { this.weaponProf = weaponProf; }
    public void addWeaponProficiency(String prof) { if (prof != null && !prof.isEmpty() && !weaponProf.contains(prof)) weaponProf.add(prof); }
    public void removeWeaponProficiency(String prof) { weaponProf.remove(prof); }
    public boolean hasWeaponProficiency(String prof) { return weaponProf.contains(prof); }

    // ---------------------------------------------------------
    //  Equipment Handling
    // ---------------------------------------------------------

    public List<DataItemEquipment> getEquipment() { return Collections.unmodifiableList(equipment); }
    public void addEquipment(DataItemEquipment item) { if (item != null) equipment.add(item); }
    public void removeEquipment(DataItemEquipment item) { equipment.remove(item); }
    public void addWeapon(DataItemWeapon weapon) { if (weapon != null) equipment.add(weapon); }

    @JsonIgnore
    public List<DataItemWeapon> getWeapons() {
        ArrayList<DataItemWeapon> weapons = new ArrayList<>();
        for (DataItemEquipment item : equipment) {
            if (!isWeaponEntry(item)) continue;
            weapons.add(item instanceof DataItemWeapon weapon ? weapon : new DataItemWeapon(item));
        }
        return Collections.unmodifiableList(weapons);
    }

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

    @JsonIgnore
    public StoreCharData getOwner() { return owner; }
    public void setOwner(StoreCharData owner) { this.owner = owner; }

    private boolean isWeaponEntry(DataItemEquipment item) {
        if (item == null) return false;
        String slot = item.getSlot() == null ? "" : item.getSlot().toLowerCase();
        String category = item.getCategory() == null ? "" : item.getCategory().toLowerCase();
        String type = item.getType() == null ? "" : item.getType().toLowerCase();
        return slot.contains("weapon")
                || slot.contains("hand")
                || category.contains("weapon")
                || category.contains("melee")
                || category.contains("ranged")
                || category.contains("aura")
                || type.contains("bow")
                || type.contains("crossbow")
                || type.contains("gun")
                || type.contains("rifle")
                || type.contains("pistol")
                || type.contains("sword")
                || type.contains("axe")
                || type.contains("spear")
                || type.contains("dagger")
                || type.contains("staff")
                || type.contains("mace")
                || type.contains("hammer");
    }
}

