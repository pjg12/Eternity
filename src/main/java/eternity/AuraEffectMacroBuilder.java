package eternity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eternity.DataTypes.DataAction;

/**
 * Builds ScriptCard effect summaries and pasteable status codes for
 * Mitigation / Support / Apply aura techniques from action modifierKey data.
 */
public final class AuraEffectMacroBuilder {
	private AuraEffectMacroBuilder() {
	}

	public static final class EffectMacroResult {
		public final String summaryText;
		public final String statusCode;
		public final List<String> summaryLines;

		private EffectMacroResult(String summaryText, String statusCode, List<String> summaryLines) {
			this.summaryText = summaryText == null ? "" : summaryText;
			this.statusCode = statusCode == null ? "" : statusCode;
			this.summaryLines = summaryLines == null ? List.of() : List.copyOf(summaryLines);
		}
	}

	public static EffectMacroResult build(DataAction action) {
		if (action == null) {
			return new EffectMacroResult("No effect.", "NAME:Effect_DESC:No effect.", List.of("No effect."));
		}
		if (action.getCharacter() == null) {
			// Evaluation still works for = formulas that only need AL on the action.
		}

		Map<String, Double> effectValues = new LinkedHashMap<>();
		int durationTurns = 0;
		List<DataAction.ModifierKey> modifiers = action.getModifierKey();
		if (modifiers != null) {
			for (DataAction.ModifierKey entry : modifiers) {
				if (entry == null || entry.getAttribute() == null || entry.getAttribute().isBlank()) continue;
				String attribute = entry.getAttribute().trim();
				double value = action.evaluateModifierEntryValue(entry);
				if ("duration".equalsIgnoreCase(attribute) || "dur".equalsIgnoreCase(attribute)) {
					durationTurns = Math.max(durationTurns, (int) Math.round(Math.max(0.0, value)));
					continue;
				}
				effectValues.merge(attribute.toUpperCase(Locale.ROOT), value, Double::sum);
			}
		}

		List<String> lines = new ArrayList<>();
		String category = action.getCategory() == null ? "Effect" : action.getCategory().trim();
		if (effectValues.isEmpty() && durationTurns <= 0) {
			lines.add(category + " technique with no encoded numeric modifiers.");
			String cleanseHint = cleanseHint(action.getName());
			if (cleanseHint != null) {
				lines.add(cleanseHint);
			}
		} else {
			for (Map.Entry<String, Double> entry : effectValues.entrySet()) {
				lines.add(formatEffectLine(entry.getKey(), entry.getValue(), action));
			}
			if (durationTurns > 0) {
				lines.add("Duration: " + durationTurns + (durationTurns == 1 ? " turn" : " turns") + ".");
			}
		}

		String summaryText = String.join("\n", lines);
		String statusCode = buildStatusCode(action, effectValues, durationTurns, lines);
		return new EffectMacroResult(summaryText, statusCode, lines);
	}

	private static String formatEffectLine(String attribute, double value, DataAction action) {
		String label = displayLabel(attribute);
		String amount = trimNumber(value);
		if ("SHIELD".equalsIgnoreCase(attribute)) {
			return "Shield: " + amount + " (Base HP x AL x SHD"
					+ (action.getName() != null
							&& action.getName().toLowerCase(Locale.ROOT).contains("(strong)") ? " x 3" : "")
					+ ").";
		}
		if ("MAXHP".equalsIgnoreCase(attribute) || "BASEHP".equalsIgnoreCase(attribute)) {
			return "Max HP effect: " + amount + ".";
		}
		if (attribute.toUpperCase(Locale.ROOT).startsWith("RES")) {
			return label + ": " + amount + ".";
		}
		return label + ": " + amount + ".";
	}

	private static String displayLabel(String attribute) {
		if (attribute == null) return "Effect";
		return switch (attribute.toUpperCase(Locale.ROOT)) {
			case "SHIELD" -> "Shield";
			case "MAXHP", "BASEHP" -> "Max HP";
			case "RESALL" -> "Resist All";
			case "RESSLASH" -> "Resist Slash";
			case "RESPIERCE" -> "Resist Pierce";
			case "RESBLUNT" -> "Resist Blunt";
			case "RESFROST" -> "Resist Frost";
			case "HPMULTI", "MULTIHP" -> "HP Multiplier";
			case "REGEN" -> "Regen (HoT)";
			case "MOVE" -> "Move";
			case "ATK" -> "Attack";
			case "CRUSH" -> "Crush";
			case "MAN" -> "Maneuver";
			case "SHD" -> "Shielding";
			case "DUR" -> "Duration Stat";
			case "THEAL" -> "Total Heal";
			case "BHEAL" -> "Base Heal";
			case "CRIT" -> "Crit";
			case "RANGE" -> "Range";
			case "MAXATK" -> "Max Attack";
			case "ROOT" -> "Root";
			case "HEAVY" -> "Heavy";
			case "INCAPACITATE" -> "Incapacitate";
			default -> attribute;
		};
	}

	private static String buildStatusCode(DataAction action, Map<String, Double> effectValues,
			int durationTurns, List<String> summaryLines) {
		String name = action.getName() == null || action.getName().isBlank() ? "Effect" : action.getName();
		StringBuilder code = new StringBuilder();
		code.append("NAME:").append(sanitize(name));

		boolean emittedAttribute = false;
		for (Map.Entry<String, Double> entry : effectValues.entrySet()) {
			String statusAttr = toStatusAttribute(entry.getKey());
			if (statusAttr == null || statusAttr.isBlank()) continue;
			code.append("_").append(statusAttr).append(":").append(formatSeverity(entry.getValue()));
			emittedAttribute = true;
		}

		if (durationTurns > 0) {
			code.append("_DUR:Turn:").append(durationTurns);
		}

		String desc = summaryLines.isEmpty()
				? (action.getCategory() == null ? "Apply technique effect." : "Apply " + action.getCategory() + " effect.")
				: String.join(" ", summaryLines);
		// StatusCodeParser requires at least one ATTRIBUTE:SEVERITY token.
		if (!emittedAttribute) {
			code.append("_HOTHP:0");
		}
		code.append("_DESC:").append(sanitize(desc));
		return code.toString();
	}

	/**
	 * Maps action modifier attributes to StatusCodeParser-accepted keys.
	 */
	static String toStatusAttribute(String modifierAttribute) {
		if (modifierAttribute == null || modifierAttribute.isBlank()) return null;
		String normalized = modifierAttribute.trim().toUpperCase(Locale.ROOT);
		return switch (normalized) {
			case "SHIELD" -> "SHIELD";
			case "MAXHP", "BASEHP" -> "BASEHP";
			case "RESALL" -> "BALL";
			case "RESSLASH" -> "BSLASH";
			case "RESPIERCE" -> "BPIERCE";
			case "RESBLUNT" -> "BBLUNT";
			case "RESFROST" -> "BFROST";
			case "HPMULTI", "MULTIHP" -> "MULTIHP";
			case "REGEN" -> "HOTHP";
			case "MOVE" -> "BMOVE";
			case "ATK" -> "BATK";
			case "CRUSH" -> "BCRUSH";
			case "MAN" -> "BMAN";
			case "SHD" -> "BSHD";
			case "DUR", "DURATION" -> null; // handled via DUR:Turn
			case "THEAL" -> "BTHEAL";
			case "BHEAL" -> "BBHEAL";
			case "CRIT" -> "BCRIT";
			case "RANGE" -> "BRANGE";
			case "MAXATK" -> "BMAXATK";
			case "ROOT" -> "ROOT";
			case "HEAVY" -> "HEAVY";
			case "INCAPACITATE" -> "INCAP";
			case "FLY" -> "BFLY";
			// Complex packs: leave numeric tokens out; DESC carries intent.
			case "DRUGDUR", "DRUGPURGE", "DRUGSEV", "SAVEDRUGDUR", "SAVEDRUGSEV",
					"ADAPTDUR", "ADAPTPURGE", "ADAPTSEV", "SAVEADAPTDUR", "SAVEADAPTSEV",
					"BLESSDUR", "BLESSPURGE", "BLESSSEV", "SAVEBLESSDUR", "SAVEBLESSSEV",
					"DISEASESAVE", "DISEASESAVES", "POISONSAVE", "POISONSAVES",
					"PSYCHOSISSAVE", "PSYCHOSISSAVES" -> null;
			default -> {
				// Prefer B-prefixed combat/stat keys when the bare token is unsupported.
				if (normalized.startsWith("B") || normalized.startsWith("M")) {
					yield normalized;
				}
				yield null;
			}
		};
	}

	private static String cleanseHint(String name) {
		if (name == null || name.isBlank()) return null;
		String lower = name.toLowerCase(Locale.ROOT);
		if (lower.contains("antidote")) return "Cleanse or bolster saves against Poison effects.";
		if (lower.contains("cure")) return "Cleanse or bolster saves against Disease effects.";
		if (lower.contains("cleanse")) return "Cleanse or bolster saves against Curse effects.";
		if (lower.contains("therapy")) return "Cleanse or bolster saves against Psychosis effects.";
		return null;
	}

	private static String sanitize(String value) {
		if (value == null) return "";
		return value.replace("|", "/").replace("_", " ").trim();
	}

	private static String formatSeverity(double value) {
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return Long.toString(Math.round(value));
		}
		return trimNumber(value);
	}

	private static String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return Long.toString(Math.round(value));
		}
		String text = String.format(Locale.ROOT, "%.2f", value);
		if (text.indexOf('.') >= 0) {
			text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
		}
		return text;
	}
}
