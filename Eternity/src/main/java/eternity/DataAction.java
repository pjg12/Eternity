package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataAction {
	private static final String ATKTYPE_OTHER = "OTHER";
	private static final String RACE_TRAINING_NAME = "Race Training";
	private static final String JOUSTING_CHARGE_SPECIALTY = "Jousting Charge";

	@JsonIgnore
	private CharData character;
	
	private int id;
	private String name;
	private String category;
	private String source;
	private String affinity;
	private String atkType;
	private int atk;
	private int bdmg;
	private int tdmg;
	private double dmgMulti;
	private int al;
	private int ranged;
	private String actionType;
	private String weapon;
	private List<CostPair> costs = new ArrayList<>();
	private List<ModifierKey> modifierKey = new ArrayList<>();
	
	public DataAction () {
		this.id = 0;
		this.name = "";
		this.category = "";
		this.source = "";
		this.affinity = "";
		this.atkType = ATKTYPE_OTHER;
		this.atk = 0;
		this.bdmg = 0;
		this.tdmg = 0;
		this.dmgMulti = 0.0;
		this.al = 0;
		this.ranged = 0;
		this.actionType = "Standard";
		this.weapon = "";
		this.costs = new ArrayList<>();
		this.modifierKey = new ArrayList<>();
	}

	DataAction (DataAction newAction) {
		this.character = newAction.getCharacter();
		this.id = newAction.getId();
		this.name = newAction.getName();
		this.category = newAction.getCategory();
		this.source = newAction.getSource();
		this.affinity = newAction.getAffinity();
		this.atkType = newAction.getAtkType();
		this.atk = newAction.getAtk();
		this.bdmg = newAction.getBdmg();
		this.tdmg = newAction.getTdmg();
		this.dmgMulti = newAction.getDmgMulti();
		this.al = newAction.getAl();
		this.ranged = newAction.getRanged();
		this.actionType = newAction.getActionType();
		this.weapon = newAction.getWeapon();
		setCosts(newAction.getCosts());
		setModifierKey(newAction.getModifierKey());
	}

	@JsonIgnore
	public CharData getCharacter() {
		return character;
	}

	@JsonIgnore
	public void setCharacter(CharData character) {
		this.character = character;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAffinity() {
		return affinity;
	}

	public void setAffinity(String affinity) {
		this.affinity = affinity;
	}

	public String getAtkType() {
		return atkType;
	}

	public void setAtkType(String atkType) {
		if ("AC".equalsIgnoreCase(atkType)
				|| "FORT".equalsIgnoreCase(atkType)
				|| "REF".equalsIgnoreCase(atkType)
				|| "WILL".equalsIgnoreCase(atkType)
				|| "SPELL".equalsIgnoreCase(atkType)
				|| ATKTYPE_OTHER.equalsIgnoreCase(atkType)) {
			this.atkType = atkType.toUpperCase();
			return;
		}
		this.atkType = ATKTYPE_OTHER;
	}

	public int getRanged() {
		return ranged;
	}

	public int getAtk() {
		return atk;
	}

	public void setAtk(int atk) {
		this.atk = atk;
	}

	public int getBdmg() {
		return bdmg;
	}

	public void setBdmg(int bdmg) {
		this.bdmg = bdmg;
	}

	public int getTdmg() {
		return tdmg;
	}

	public void setTdmg(int tdmg) {
		this.tdmg = tdmg;
	}

	public double getDmgMulti() {
		return dmgMulti;
	}

	public void setDmgMulti(double dmgMulti) {
		this.dmgMulti = dmgMulti;
	}

	public int getAl() {
		return al;
	}

	public void setAl(int al) {
		this.al = al;
	}

	public void setRanged(int ranged) {
		this.ranged = ranged;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getWeapon() {
		return weapon;
	}

	public void setWeapon(String weapon) {
		this.weapon = weapon;
	}

	public List<CostPair> getCosts() {
		return Collections.unmodifiableList(costs);
	}

	public void setCosts(List<CostPair> costs) {
		this.costs = new ArrayList<>();
		if (costs == null) {
			return;
		}
		for (CostPair cost : costs) {
			if (cost != null) {
				this.costs.add(new CostPair(cost));
			}
		}
	}

	public List<ModifierKey> getModifierKey() {
		return Collections.unmodifiableList(modifierKey);
	}

	public void setModifierKey(List<ModifierKey> modifierKey) {
		this.modifierKey = new ArrayList<>();
		if (modifierKey == null) {
			return;
		}
		for (ModifierKey entry : modifierKey) {
			if (entry != null) {
				this.modifierKey.add(new ModifierKey(entry));
			}
		}
	}

	public void update() {
		DataAction sourceAction = null;
		if (character != null && character.getCombat() != null && "AC".equalsIgnoreCase(atkType)) {
			sourceAction = character.getCombat().getStandardAttackAction();
		} else if (character != null && character.getCombat() != null && !ATKTYPE_OTHER.equalsIgnoreCase(atkType)) {
			sourceAction = character.getCombat().getStandardSpellAction();
		}

		if (sourceAction != null && sourceAction != this) {
			atk = sourceAction.getAtk();
			bdmg = sourceAction.getBdmg();
			tdmg = sourceAction.getTdmg();
			dmgMulti = sourceAction.getDmgMulti();
		}

		for (ModifierKey entry : modifierKey) {
			if (entry == null) continue;
			String operator = entry.getOperator();
			if (operator != null && operator.toUpperCase().contains("AL")) {
				applyModifierKeyWithAl(entry);
			} else {
				applyModifierKey(entry);
			}
		}

		applyRacialCombatManeuverBonuses();
	}

	private void applyRacialCombatManeuverBonuses() {
		if (!isChargeCombatManeuver() || !hasRacialSpecialty(JOUSTING_CHARGE_SPECIALTY)) return;
		atk += getRaceTrainingRank();
	}

	private boolean isChargeCombatManeuver() {
		return "Combat Maneuver".equalsIgnoreCase(source)
				&& "Charge".equalsIgnoreCase(name);
	}

	private boolean hasRacialSpecialty(String specialtyNamePrefix) {
		if (character == null || character.getSpecials() == null || specialtyNamePrefix == null) return false;
		DataSpecialty racial = character.getSpecials().getRacialSpecialty();
		if (racial == null || racial.getName() == null) return false;
		return racial.getName().regionMatches(true, 0, specialtyNamePrefix, 0, specialtyNamePrefix.length());
	}

	private int getRaceTrainingRank() {
		if (character == null || character.getTraining() == null) return 0;
		DataTraining raceTraining = character.getTraining().getTrainingByName(RACE_TRAINING_NAME);
		return raceTraining == null ? 0 : Math.max(0, raceTraining.getRank());
	}

	private void applyModifierKeyWithAl(ModifierKey entry) {
		if (entry == null) return;
		entry.setModifier(entry.getModifier() * al);
		applyModifierKey(entry);
	}

	private void applyModifierKey(ModifierKey entry) {
		if (entry == null) return;
		String operator = entry.getOperator().replaceFirst("AL", "").trim();
		if (operator != null && operator.toUpperCase().matches(".*[a-z].*")) { 
			applyModifierKeyWithOther(operator, entry);
		} else {
		applyModifierToField(entry.getAttribute(), entry.getOperator(), entry.getModifier());
		}
	}

	private void applyModifierKeyWithOther(String operator, ModifierKey entry) {
		
	}

	private void applyModifierToField(String attribute, String operator, double operand) {
		if (attribute == null || attribute.isBlank()) return;
		String normalizedAttribute = attribute.trim().toUpperCase();
		String normalizedOperator = operator == null ? "+" : operator.trim().toUpperCase();
		char mathOperator = resolveMathOperator(normalizedOperator);

		switch (normalizedAttribute) {
			case "ATK" -> atk = applyIntOperator(atk, mathOperator, operand);
			case "BDMG" -> bdmg = applyIntOperator(bdmg, mathOperator, operand);
			case "TDMG" -> tdmg = applyIntOperator(tdmg, mathOperator, operand);
			case "DMGMULTI" -> dmgMulti = applyDoubleOperator(dmgMulti, mathOperator, operand);
			case "AL" -> al = applyIntOperator(al, mathOperator, operand);
			case "RANGED", "RANGE" -> ranged = applyIntOperator(ranged, mathOperator, operand);
			default -> {
				// unsupported modifier target
			}
		}
	}

	private char resolveMathOperator(String operator) {
		if (operator == null || operator.isBlank()) return '+';
		if (operator.indexOf('=') >= 0) return '=';
		if (operator.indexOf('*') >= 0) return '*';
		if (operator.indexOf('/') >= 0) return '/';
		if (operator.indexOf('-') >= 0) return '-';
		return '+';
	}

	private int applyIntOperator(int currentValue, char operator, double operand) {
		double result = applyDoubleOperator(currentValue, operator, operand);
		return (int)Math.round(result);
	}

	private double applyDoubleOperator(double currentValue, char operator, double operand) {
		return switch (operator) {
			case '=' -> operand;
			case '-' -> currentValue - operand;
			case '*' -> currentValue * operand;
			case '/' -> operand == 0.0 ? currentValue : currentValue / operand;
			default -> currentValue + operand;
		};
	}

	public static class CostPair {
		private String type;
		private double value;

		public CostPair() {
			this("", 0.0);
		}

		public CostPair(String type, double value) {
			this.type = type;
			this.value = value;
		}

		public CostPair(CostPair other) {
			this.type = other == null ? null : other.type;
			this.value = other == null ? 0 : other.value;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}
	}

	public static class ModifierKey {
		private String attribute;
		private String operator;
		private double modifier;

		public ModifierKey() {
			this("", "+", 0.0);
		}

		public ModifierKey(String attribute, String operator, double modifier) {
			this.attribute = attribute;
			this.operator = operator;
			this.modifier = modifier;
		}

		public ModifierKey(ModifierKey other) {
			this.attribute = other == null ? null : other.attribute;
			this.operator = other == null ? "+" : other.operator;
			this.modifier = other == null ? 0.0 : other.modifier;
		}

		public String getAttribute() {
			return attribute;
		}

		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}

		public String getOperator() {
			return operator;
		}

		public void setOperator(String operator) {
			this.operator = operator;
		}

		public double getModifier() {
			return modifier;
		}

		public void setModifier(double modifier) {
			this.modifier = modifier;
		}
	}
	
	
	
}
