package eternity;

import java.util.Arrays;

/**
 * Stores Level Information
 */
public class DataLevel {
	
	private int level;
	private int baseHP;
	private int baseAura;
	private int baseTechs;
	private int[] scalers;
	private String damage;
	private int skills;
	private int specialties;
	private int classGeneral;
	private int classSpec;
	
	public DataLevel() { this(1, 0, 0, 0, new int[]{0, 0, 0}, "1d6", 0, 0, 0, 0); }
	public DataLevel(DataLevel src) { this(src.level, src.baseHP, src.baseAura, src.baseTechs, src.scalers != null ? src.scalers.clone() : new int[]{0,0,0}, src.damage, src.skills, src.specialties, src.classGeneral, src.classSpec); }
	
	public DataLevel(int level, int baseHP, int baseAura, int baseTechs, int[] scalers, String damage, int skills, int specialties, int classGeneral, int classSpec) {
		this.level = level;
        this.baseHP = baseHP;
        this.baseAura = baseAura;
        this.baseTechs = baseTechs;
        if (scalers == null || scalers.length == 0)
            this.scalers = new int[]{0, 0, 0};
        else
            this.scalers = scalers.clone();
        this.damage = damage;
        this.skills = skills;
        this.specialties = specialties;
        this.classGeneral = classGeneral;
        this.classSpec = classSpec;
    }

	// --- Getters & Setters ---
	
	public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(0, level); }

    public int getBaseHP() { return baseHP; }
    public void setBaseHP(int baseHP) { this.baseHP = Math.max(0, baseHP); }

    public int getBaseAura() { return baseAura; }
    public void setBaseAura(int baseAura) { this.baseAura = Math.max(0, baseAura); }

    public int getBaseTechs() { return baseTechs; }
    public void setBaseTechs(int baseTechs) { this.baseTechs = Math.max(0, baseTechs); }

    public int[] getScalers() { return scalers.clone(); }
    public void setScalers(int[] scalers) {
        if (scalers == null || scalers.length == 0)
            this.scalers = new int[]{0,0,0};
        else
            this.scalers = scalers.clone();
    }

    public String getDamage() { return damage; }
    public void setDamage(String damage) { this.damage = damage; }

    public int getSkills() { return skills; }
    public void setSkills(int skills) { this.skills = Math.max(0, skills); }

    public int getSpecialties() { return specialties; }
    public void setSpecialties(int specialties) { this.specialties = Math.max(0, specialties); }

    public int getClassGeneral() { return classGeneral; }
    public void setClassGeneral(int classGeneral) { this.classGeneral = Math.max(0, classGeneral); }

    public int getClassSpec() { return classSpec; }
    public void setClassSpec(int classSpec) { this.classSpec = Math.max(0, classSpec); }

    // --- Helpers ---

    @Override
    public String toString() {
        return "DataLevel{" +
                "level=" + level +
                ", baseHP=" + baseHP +
                ", baseAura=" + baseAura +
                ", baseTechs=" + baseTechs +
                ", scalers=" + Arrays.toString(scalers) +
                ", damage=" + damage +
                ", skills=" + skills +
                ", specialties=" + specialties +
                ", classGeneral=" + classGeneral +
                ", classSpec=" + classSpec +
                '}';
    }
}
