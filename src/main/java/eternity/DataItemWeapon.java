package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DataItemWeapon represents a weapon item.
 */
public class DataItemWeapon extends DataItemEquipment {
    @JsonProperty private String damage;      // Weapon damage expression
    @JsonProperty private String attack;      // Weapon attack expression

    public DataItemWeapon() {
        this(new DataItemEquipment());
    }

    public DataItemWeapon(DataItemWeapon src) {
        this((DataItemEquipment) src);
        if (src != null) {
            this.damage = safe(src.getDamage());
            this.attack = safe(src.getAttack());
        }
    }

    public DataItemWeapon(DataItemEquipment src) {
        super(resolveBaseWeapon(src));
        if (src instanceof DataItemWeapon weapon) {
            this.damage = safe(weapon.getDamage());
            this.attack = safe(weapon.getAttack());
        } else {
            DataItemWeapon template = resolveWeaponTemplate(src);
            this.damage = safe(template != null ? template.getDamage() : "");
            this.attack = safe(template != null ? template.getAttack() : "");
            if (src != null) {
                setIid(src.getIid());
                setIname(src.getIname());
                setDnote(src.getDnote());
                setInote(src.getInote());
                setQuantity(src.getQuantity());
                setEquipped(src.isEquipped());
                setEnch(src.getEnch());
                setGem(src.getGem());
                setStore(src.getStore());
                setOil(src.getOil());
                setMod(src.getMod());
                setAug(src.getAug());
            }
        }
    }

    public DataItemWeapon(int did, int iid, String dname, String iname, String dnote, String inote, double quantity,
            String slot, int tier, String category, String type, String bonusAtt, double bonusAmount, int levelReq,
            long value, boolean equipped, int ench, int gem, int store, int oil, int mod, int aug,
            String damage, String attack) {
        super(did, iid, dname, iname, dnote, inote, quantity, slot, tier, category, type, bonusAtt, bonusAmount,
                levelReq, value, equipped, ench, gem, store, oil, mod, aug);
        this.damage = safe(damage);
        this.attack = safe(attack);
    }

    public String getDamage() { return damage; }
    public void setDamage(String damage) { this.damage = safe(damage); }

    public String getAttack() { return attack; }
    public void setAttack(String attack) { this.attack = safe(attack); }

    private static String safe(String s) { return s == null ? "" : s; }

    private static DataItemEquipment resolveBaseWeapon(DataItemEquipment src) {
        if (src == null) return new DataItemEquipment();
        DataItemWeapon template = resolveWeaponTemplate(src);
        return template != null ? template : src;
    }

    private static DataItemWeapon resolveWeaponTemplate(DataItemEquipment src) {
        if (src == null) return null;
        try {
            return new StoreRuleManager().getWeaponByDid(src.getDid());
        } catch (Exception ignored) {
            return null;
        }
    }
}
