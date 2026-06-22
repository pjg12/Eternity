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
	private StoreCharData character;
	
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
	public StoreCharData getCharacter() {
		return character;
	}

	@JsonIgnore
	public void setCharacter(StoreCharData character) {
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
		if (isSupportedAttackType(atkType)) {
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
		if (character != null && character.getCombat() != null && usesStandardAttackValues()) {
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
			applyModifierKey(entry);
		}

		applyRacialCombatManeuverBonuses();
	}

	public boolean hasModifierAttribute(String attribute) {
		if (attribute == null || attribute.isBlank() || modifierKey == null) return false;
		String normalized = attribute.trim().toUpperCase();
		for (ModifierKey entry : modifierKey) {
			if (entry == null || entry.getAttribute() == null) continue;
			if (normalized.equalsIgnoreCase(entry.getAttribute().trim())) {
				return true;
			}
		}
		return false;
	}

	public double evaluateModifierAttributeValue(String attribute) {
		if (attribute == null || attribute.isBlank() || modifierKey == null) return 0.0;
		String normalized = attribute.trim().toUpperCase();
		double value = 0.0;
		for (ModifierKey entry : modifierKey) {
			if (entry == null || entry.getAttribute() == null) continue;
			if (!normalized.equalsIgnoreCase(entry.getAttribute().trim())) continue;
			String normalizedOperator = entry.getOperator() == null ? "+" : entry.getOperator().trim().toUpperCase();
			char mathOperator = resolveMathOperator(normalizedOperator);
			double operand = resolveOperand(normalizedOperator, mathOperator);
			value = applyDoubleOperator(value, mathOperator, operand);
		}
		return value;
	}

	private boolean usesStandardAttackValues() {
		return isAttackLayoutType(atkType);
	}

	private boolean isSupportedAttackType(String value) {
		return isAttackLayoutType(value)
				|| "FORT".equalsIgnoreCase(value)
				|| "REF".equalsIgnoreCase(value)
				|| "WILL".equalsIgnoreCase(value)
				|| "SPELL".equalsIgnoreCase(value)
				|| ATKTYPE_OTHER.equalsIgnoreCase(value);
	}

	private boolean isAttackLayoutType(String value) {
		return "AC".equalsIgnoreCase(value)
				|| "DODGE".equalsIgnoreCase(value)
				|| "ARMOR".equalsIgnoreCase(value);
	}

	private void applyRacialCombatManeuverBonuses() {
		if (!isChargeCombatManeuver() || !hasRacialSpecialty(JOUSTING_CHARGE_SPECIALTY)) return;
		int raceTrainingRank = getRaceTrainingRank();
		atk += raceTrainingRank / 2;
		bdmg += raceTrainingRank;
		tdmg += raceTrainingRank;
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

	private void applyModifierKey(ModifierKey entry) {
		if (entry == null) return;
		applyModifierToField(entry.getAttribute(), entry.getOperator());
	}

	private void applyModifierToField(String attribute, String operatorExpression) {
		if (attribute == null || attribute.isBlank()) return;
		String normalizedAttribute = attribute.trim().toUpperCase();
		String normalizedOperator = operatorExpression == null ? "+" : operatorExpression.trim().toUpperCase();
		char mathOperator = resolveMathOperator(normalizedOperator);
		double operand = resolveOperand(normalizedOperator, mathOperator);

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
		String normalized = operator.trim();
		char first = normalized.charAt(0);
		return switch (first) {
			case '=', '*', '/', '-', '+' -> first;
			default -> '+';
		};
	}

	private double resolveOperand(String operatorExpression, char mathOperator) {
		if (operatorExpression == null || operatorExpression.isBlank()) {
			return defaultOperand(mathOperator);
		}

		String normalized = operatorExpression.trim();
		String operandExpression = stripLeadingOperator(normalized, mathOperator);
		if (operandExpression.isBlank()) {
			return defaultOperand(mathOperator);
		}
		try {
			return evaluateModifierExpression(operandExpression);
		} catch (IllegalArgumentException ignored) {
			return defaultOperand(mathOperator);
		}
	}

	private String stripLeadingOperator(String operatorExpression, char mathOperator) {
		if (operatorExpression == null) return "";
		String trimmed = operatorExpression.trim();
		if (!trimmed.isEmpty() && trimmed.charAt(0) == mathOperator
				&& (mathOperator == '+' || mathOperator == '-' || mathOperator == '*'
				|| mathOperator == '/' || mathOperator == '=')) {
			return trimmed.substring(1).trim();
		}
		return trimmed;
	}

	private double evaluateModifierExpression(String expression) {
		return new ModifierExpressionParser(expression).parse();
	}

	private double resolveExpressionIdentifier(String token) {
		if (token == null || token.isBlank()) return 0.0;
		String normalized = token.trim().toUpperCase();
		return switch (normalized) {
			case "AL" -> character == null ? al : character.getEffectiveTechniqueAl(this);
			case "CL" -> getClassLevel();
			case "CMAN" -> getEffectiveCombatManeuverValue();
			case "ATK" -> atk;
			case "BDMG" -> bdmg;
			case "TDMG" -> tdmg;
			case "DMGMULTI" -> dmgMulti;
			case "RANGE" -> character != null && character.getAttributes() != null
					? character.getAttributes().calcStatusValue("RANGE")
					: 0.0;
			case "RANGED" -> ranged;
			case "APP" -> character != null && character.getAttributes() != null
					? character.getAttributes().calcStatusValue("APP")
					: 0.0;
			default -> resolveCharacterStatValue(normalized);
		};
	}

	private double getEffectiveCombatManeuverValue() {
		if (character == null) return 0.0;
		return Math.max(0.0, character.getEffectiveCombatManeuverValue(name));
	}

	private double getClassLevel() {
		if (character == null || character.getIdentity() == null) return 0.0;
		return Math.max(0, character.getIdentity().getLevel());
	}

	private double resolveCharacterStatValue(String key) {
		if (character == null) return 0.0;
		if (character.getAttributes() != null && isKnownAttributeKey(key)) {
			return character.getAttributes().calcStatusValue(key);
		}
		double resourceValue = character.calcResourceValue(key);
		return resourceValue >= 0.0 ? resourceValue : 0.0;
	}

	private boolean isKnownAttributeKey(String key) {
		return containsIgnoreCase(CharAttributes.getAttributeKeys(), key)
				|| containsIgnoreCase(CharAttributes.getDefenseKeys(), key)
				|| containsIgnoreCase(CharAttributes.getDamageTypeKeys(), key)
				|| containsIgnoreCase(CharAttributes.getCombatKeys(), key)
				|| containsIgnoreCase(CharAttributes.getSecondaryKeys(), key)
				|| containsIgnoreCase(CharAttributes.getDamageKeys(), key);
	}

	private boolean containsIgnoreCase(String[] values, String target) {
		if (values == null || target == null) return false;
		for (String value : values) {
			if (value != null && value.equalsIgnoreCase(target)) {
				return true;
			}
		}
		return false;
	}

	private double defaultOperand(char mathOperator) {
		return switch (mathOperator) {
			case '*', '/' -> 1.0;
			default -> 0.0;
		};
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

	private final class ModifierExpressionParser {
		private final String expression;
		private int index = 0;

		private ModifierExpressionParser(String expression) {
			this.expression = expression == null ? "" : expression;
		}

		private double parse() {
			double value = parseExpression();
			skipWhitespace();
			while (index < expression.length() && expression.charAt(index) == ')') {
				index++;
				skipWhitespace();
			}
			if (index < expression.length()) {
				throw new IllegalArgumentException("Unexpected token in modifier expression");
			}
			return value;
		}

		private double parseExpression() {
			double value = parseTerm();
			while (true) {
				skipWhitespace();
				if (match('+')) {
					value += parseTerm();
				} else if (match('-')) {
					value -= parseTerm();
				} else {
					return value;
				}
			}
		}

		private double parseTerm() {
			double value = parseFactor();
			while (true) {
				skipWhitespace();
				if (match('*')) {
					value *= parseFactor();
				} else if (match('/')) {
					double divisor = parseFactor();
					if (divisor != 0.0) {
						value /= divisor;
					}
				} else {
					return value;
				}
			}
		}

		private double parseFactor() {
			skipWhitespace();
			if (match('+')) return parseFactor();
			if (match('-')) return -parseFactor();
			if (match('(')) {
				double value = parseExpression();
				if (!match(')')) {
					throw new IllegalArgumentException("Unclosed modifier expression group");
				}
				return value;
			}
			if (index >= expression.length()) {
				throw new IllegalArgumentException("Unexpected end of modifier expression");
			}
			char current = expression.charAt(index);
			if (Character.isDigit(current) || current == '.') {
				return parseNumber();
			}
			if (Character.isLetter(current)) {
				return parseIdentifier();
			}
			throw new IllegalArgumentException("Unsupported modifier expression token");
		}

		private double parseNumber() {
			int start = index;
			while (index < expression.length()) {
				char current = expression.charAt(index);
				if (!Character.isDigit(current) && current != '.') break;
				index++;
			}
			return Double.parseDouble(expression.substring(start, index));
		}

		private double parseIdentifier() {
			int start = index;
			while (index < expression.length()) {
				char current = expression.charAt(index);
				if (!Character.isLetterOrDigit(current) && current != '_') break;
				index++;
			}
			String identifier = expression.substring(start, index);
			skipWhitespace();
			if (match('(')) {
				double argument = parseExpression();
				if (!match(')')) {
					throw new IllegalArgumentException("Unclosed modifier function");
				}
				return applyFunction(identifier, argument);
			}
			return resolveExpressionIdentifier(identifier);
		}

		private double applyFunction(String identifier, double argument) {
			if (identifier == null || identifier.isBlank()) {
				throw new IllegalArgumentException("Missing modifier function");
			}
			if ("RT".equalsIgnoreCase(identifier.trim())) {
				return Math.sqrt(Math.max(0.0, argument));
			}
			throw new IllegalArgumentException("Unsupported modifier function");
		}

		private boolean match(char expected) {
			skipWhitespace();
			if (index < expression.length() && expression.charAt(index) == expected) {
				index++;
				return true;
			}
			return false;
		}

		private void skipWhitespace() {
			while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
				index++;
			}
		}
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

		public ModifierKey() {
			this("", "+");
		}

		public ModifierKey(String attribute, String operator) {
			this.attribute = attribute;
			this.operator = operator;
		}

		public ModifierKey(ModifierKey other) {
			this.attribute = other == null ? null : other.attribute;
			this.operator = other == null ? "+" : other.operator;
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
	}
	
	
	
}
