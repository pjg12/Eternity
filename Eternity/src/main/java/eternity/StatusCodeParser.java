package eternity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Strict parser for shorthand effect codes.
 *
 * Format:
 * TARGET_SIDE_TARGET_SHAPE_ATTRIBUTE:SEVERITY_..._DUR:TYPE:DURATION_NAME:Label_AFF:Affinity_DESC:Text
 * Short format defaults omitted TARGET_SIDE to ALLY, TARGET_SHAPE to SINGLE, and duration to Maintained.
 * Status tokens may be written as ATTRIBUTE:SEVERITY or compact ATTRIBUTE+SEVERITY / ATTRIBUTE-SEVERITY.
 *
 * Example:
 * ALLY_SINGLE_BATK:+2_BDODGE:+1_DUR:Turn:1_NAME:Guarded
 * AT+2_DG+1
 */
public final class StatusCodeParser {
    private static final String[] RESOURCE_KEYS = {
            "BASEHP", "MULTIHP", "BASEAURA", "MULTIAURA",
            "BASER1", "MULTIR1", "BASER2", "MULTIR2", "BASER3", "MULTIR3",
            "BASEREACT", "MULTIREACT"
    };
    private static final String[] INSTANT_RESOURCE_KEYS = {"HP", "AURA"};

    private static final Set<String> VALID_ATTRIBUTES = buildValidAttributes();

    private StatusCodeParser() {
    }

    public static StatusCodeParseResult parse(String code) {
        StatusCodeParseResult result = new StatusCodeParseResult();
        if (code == null || code.isBlank()) {
            result.addError("Status code is empty.");
            return result;
        }

        String[] tokens = code.trim().split("[|_]");
        if (tokens.length < 1) {
            result.addError("Status code must include at least one status entry.");
            return result;
        }

        int tokenIndex = 0;
        EffectTargetSide targetSide = EffectTargetSide.ALLY;
        EffectTargetShape targetShape = EffectTargetShape.SINGLE;

        EffectTargetSide parsedSide = tryParseTargetSide(tokens[tokenIndex]);
        if (parsedSide != null) {
            targetSide = parsedSide;
            tokenIndex++;
        }
        if (tokenIndex < tokens.length) {
            EffectTargetShape parsedShape = tryParseTargetShape(tokens[tokenIndex]);
            if (parsedShape != null) {
                targetShape = parsedShape;
                tokenIndex++;
            }
        }

        String effectName = "Decoded Effect";
        String affinity = "None";
        String description = "Decoded from shorthand effect code.";
        String durationType = "Maintained";
        int duration = 0;
        boolean timedDuration = false;
        List<StatusSpec> statusSpecs = new ArrayList<>();

        for (int i = tokenIndex; i < tokens.length; i++) {
            String token = tokens[i] == null ? "" : tokens[i].trim();
            if (token.isBlank()) {
                continue;
            }

            String upper = token.toUpperCase(Locale.ROOT);
            if (upper.startsWith("NM") && token.indexOf(':') < 0) {
                effectName = token.substring(2).trim();
                if (effectName.isBlank()) {
                    result.addError("NM token cannot be empty.");
                }
                continue;
            }
            if (upper.startsWith("NAME:")) {
                effectName = token.substring(5).trim();
                if (effectName.isBlank()) {
                    result.addError("NAME token cannot be empty.");
                }
                continue;
            }
            if (upper.startsWith("AFF:")) {
                affinity = token.substring(4).trim();
                if (affinity.isBlank()) {
                    result.addError("AFF token cannot be empty.");
                }
                continue;
            }
            if (upper.startsWith("DESC:")) {
                description = token.substring(5).trim();
                if (description.isBlank()) {
                    result.addError("DESC token cannot be empty.");
                }
                continue;
            }
            if (upper.startsWith("DUR:")) {
                DurationSpec spec = parseDurationToken(token, result);
                if (spec != null) {
                    durationType = spec.durationType;
                    duration = spec.duration;
                    timedDuration = spec.timed;
                }
                continue;
            }

            List<StatusSpec> parsedSpecs = parseStatusTokens(token, result);
            if (parsedSpecs != null) {
                statusSpecs.addAll(parsedSpecs);
            }
        }

        if (statusSpecs.isEmpty()) {
            result.addError("At least one ATTRIBUTE:SEVERITY entry is required.");
        }
        if (!result.getErrors().isEmpty()) {
            return result;
        }

        DecodedEffect effect = new DecodedEffect();
        effect.setOriginalCode(code);
        effect.setTargetSide(targetSide);
        effect.setTargetShape(targetShape);

        for (StatusSpec spec : statusSpecs) {
            DataStatus status = new DataStatus();
            status.setName(effectName + " [" + spec.attribute + "]");
            status.setAffinity(affinity);
            status.setDescription(description);
            status.setAttribute(spec.attribute);
            status.setSeverity(spec.severity);
            if (isInstantResourceDeltaAttribute(spec.attribute)) {
                status.setDurationType("Instant");
                status.setDuration(0);
            } else {
                status.setDurationType(durationType);
                status.setDuration(timedDuration ? duration : 0);
            }
            effect.addStatus(status);
        }

        result.setEffect(effect);
        return result;
    }

    private static EffectTargetSide tryParseTargetSide(String token) {
        if (token == null) return null;
        String normalized = token.trim().toUpperCase(Locale.ROOT);
        try {
            return EffectTargetSide.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static EffectTargetShape tryParseTargetShape(String token) {
        if (token == null) return null;
        String normalized = token.trim().toUpperCase(Locale.ROOT);
        try {
            return EffectTargetShape.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static List<StatusSpec> parseStatusTokens(String token, StatusCodeParseResult result) {
        ArrayList<StatusSpec> specs = new ArrayList<>();
        if (token == null || token.isBlank()) {
            return specs;
        }
        if (hasExplicitSeveritySyntax(token)) {
            StatusSpec single = parseStatusToken(token, result);
            if (single != null) {
                specs.add(single);
            }
            return specs;
        }

        List<StatusSpec> packed = parsePackedStatusSequence(token, result);
        if (packed == null) {
            return null;
        }
        specs.addAll(packed);
        return specs;
    }

    private static boolean hasExplicitSeveritySyntax(String token) {
        if (token == null || token.isBlank()) return false;
        return token.indexOf(':') >= 0 || token.indexOf('+') >= 0 || token.indexOf('-') >= 0;
    }

    private static List<StatusSpec> parsePackedStatusSequence(String token, StatusCodeParseResult result) {
        ArrayList<StatusSpec> specs = new ArrayList<>();
        String normalized = token == null ? "" : token.trim().toUpperCase(Locale.ROOT);
        int index = 0;
        while (index < normalized.length()) {
            if (normalized.charAt(index) == '_') {
                index++;
                continue;
            }

            int aliasEnd = findPackedAliasEnd(normalized, index);
            if (aliasEnd < 0) {
                result.addError("Invalid packed status token: " + token);
                return null;
            }

            char signChar = normalized.charAt(aliasEnd);
            int valueStart = aliasEnd + 1;
            int valueEnd = valueStart;
            while (valueEnd < normalized.length()) {
                char c = normalized.charAt(valueEnd);
                if (Character.isDigit(c) || c == 'D') {
                    valueEnd++;
                    continue;
                }
                break;
            }
            if (valueEnd <= valueStart) {
                result.addError("Invalid packed status token: " + token);
                return null;
            }

            String alias = normalized.substring(index, aliasEnd);
            String rawValue = normalized.substring(valueStart, valueEnd).replace('D', '.');
            String signedValue = (signChar == 'N' ? "-" : "") + rawValue;
            StatusSpec single = parseStatusToken(alias + ":" + signedValue, result);
            if (single == null) {
                return null;
            }
            specs.add(single);
            index = valueEnd;
        }
        return specs;
    }

    private static int findPackedAliasEnd(String token, int start) {
        if (token == null || start < 0 || start >= token.length()) return -1;
        int maxEnd = Math.min(token.length() - 1, start + 6);
        for (int end = maxEnd; end > start; end--) {
            char signChar = token.charAt(end);
            if (signChar != 'P' && signChar != 'N') continue;
            String alias = token.substring(start, end);
            String attribute = canonicalizeAttributeToken(alias);
            if (attribute != null && VALID_ATTRIBUTES.contains(attribute)) {
                return end;
            }
        }
        return -1;
    }

    private static StatusSpec parseStatusToken(String token, StatusCodeParseResult result) {
        String attributeToken;
        String severityToken;
        String[] pair = token.split(":", 2);
        if (pair.length == 2) {
            attributeToken = pair[0].trim();
            severityToken = pair[1].trim();
        } else {
            int splitIndex = findSeveritySplitIndex(token);
            if (splitIndex <= 0 || splitIndex >= token.length()) {
                result.addError("Invalid status token: " + token);
                return null;
            }
            attributeToken = token.substring(0, splitIndex).trim();
            severityToken = token.substring(splitIndex).trim();
        }

        String attribute = canonicalizeAttributeToken(attributeToken);
        if (attribute == null || !VALID_ATTRIBUTES.contains(attribute)) {
            result.addError("Unsupported attribute key: " + attributeToken);
            return null;
        }

        double severity;
        try {
            severity = Double.parseDouble(severityToken);
        } catch (NumberFormatException ex) {
            result.addError("Invalid severity for token: " + token);
            return null;
        }

        return new StatusSpec(attribute, severity);
    }

    private static int findSeveritySplitIndex(String token) {
        if (token == null || token.isBlank()) return -1;
        for (int i = 1; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '+' || c == '-') {
                return i;
            }
        }
        return -1;
    }

    private static String canonicalizeAttributeToken(String token) {
        if (token == null || token.isBlank()) return null;
        String normalized = token.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "STUN" -> "STUN";
            case "HEAVY", "HVY" -> "HEAVY";
            case "INCAP", "INCAPACITATE" -> "INCAP";
            case "ROOT" -> "ROOT";
            case "FLANK", "FLANKING" -> "FLANKING";
            case "HOTHP", "REGENHP" -> "HOTHP";
            case "HOTSHIELD", "REGSHIELD" -> "HOTSHIELD";
            case "TAKEN", "DMGTKN", "DMGTAKEN" -> "DMGTAKEN";
            case "HP" -> "HP";
            case "HPM", "HPMULTI" -> "MULTIHP";
            case "AU", "AURA" -> "AURA";
            case "AUM", "AURAMULTI" -> "MULTIAURA";
            case "REA", "REACT" -> "BASEREACT";
            case "R1" -> "BASER1";
            case "R2" -> "BASER2";
            case "R3" -> "BASER3";
            case "STR", "DEX", "CON", "FOC", "CTL", "CAP", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB" -> "B" + normalized;
            case "AR", "ARM", "ARMOR" -> "BARMOR";
            case "DG", "DODGE" -> "BDODGE";
            case "DF", "DEF" -> "BDEF";
            case "FT", "FORT" -> "BFORT";
            case "RF", "REF" -> "BREF";
            case "WL", "WILL" -> "BWILL";
            case "AV", "AVOID" -> "BAVOID";
            case "AT", "ATK" -> "BATK";
            case "AP", "APP", "APPLY" -> "BAPP";
            case "MV", "MOVE" -> "BMOVE";
            case "FL", "FLY" -> "BFLY";
            case "RG", "RANGE" -> "BRANGE";
            case "IN", "INIT" -> "BINIT";
            case "CM", "CMAN" -> "BCMAN";
            case "MX", "MAXATK" -> "BMAXATK";
            case "PW", "POWER" -> "BPOWER";
            case "SU", "SUP" -> "BSUP";
            case "IM", "IMP", "IMPAIR" -> "BIMP";
            case "MS", "MAST" -> "BMAST";
            case "EX", "EXCL" -> "BEXCL";
            case "GR", "GRANT" -> "BGRANT";
            case "CR", "CRUSH" -> "BCRUSH";
            case "AE", "AREA" -> "BAREA";
            case "BD", "BDMG" -> "BBDMG";
            case "TD", "TDMG" -> "BTDMG";
            case "BH", "BHEAL" -> "BBHEAL";
            case "TH", "THEAL" -> "BTHEAL";
            case "BDM" -> "MBDMG";
            case "TDM" -> "MTDMG";
            case "BHM" -> "MBHEAL";
            case "THM" -> "MTHEAL";
            default -> normalized;
        };
    }

    public static String getPreferredAttributeAlias(String attribute) {
        if (attribute == null || attribute.isBlank()) return "";
        String normalized = attribute.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "STUN" -> "STUN";
            case "HEAVY" -> "HEAVY";
            case "INCAP" -> "INCAP";
            case "ROOT" -> "ROOT";
            case "FLANKING" -> "FLANK";
            case "HOTHP" -> "HOTHP";
            case "HOTSHIELD" -> "HOTSHIELD";
            case "DMGTAKEN" -> "TAKEN";
            case "HP" -> "HP";
            case "BASEHP" -> "HP";
            case "MULTIHP" -> "HPM";
            case "AURA" -> "AU";
            case "BASEAURA" -> "AU";
            case "MULTIAURA" -> "AUM";
            case "BASEREACT" -> "REA";
            case "BASER1" -> "R1";
            case "BASER2" -> "R2";
            case "BASER3" -> "R3";
            case "BSTR" -> "STR";
            case "BDEX" -> "DEX";
            case "BCON" -> "CON";
            case "BFOC" -> "FOC";
            case "BCTL" -> "CTL";
            case "BCAP" -> "CAP";
            case "BKNOW" -> "KNOW";
            case "BMECH" -> "MECH";
            case "BPERC" -> "PERC";
            case "BINT" -> "INT";
            case "BCHA" -> "CHA";
            case "BSUB" -> "SUB";
            case "BARMOR" -> "AR";
            case "BDODGE" -> "DG";
            case "BDEF" -> "DF";
            case "BFORT" -> "FT";
            case "BREF" -> "RF";
            case "BWILL" -> "WL";
            case "BAVOID" -> "AV";
            case "BATK" -> "AT";
            case "BAPP" -> "AP";
            case "BMOVE" -> "MV";
            case "BFLY" -> "FL";
            case "BRANGE" -> "RG";
            case "BINIT" -> "IN";
            case "BCMAN" -> "CM";
            case "BMAXATK" -> "MX";
            case "BPOWER" -> "PW";
            case "BSUP" -> "SU";
            case "BIMP" -> "IM";
            case "BMAST" -> "MS";
            case "BEXCL" -> "EX";
            case "BGRANT" -> "GR";
            case "BCRUSH" -> "CR";
            case "BAREA" -> "AE";
            case "BBDMG" -> "BD";
            case "BTDMG" -> "TD";
            case "BBHEAL" -> "BH";
            case "BTHEAL" -> "TH";
            case "MBDMG" -> "BDM";
            case "MTDMG" -> "TDM";
            case "MBHEAL" -> "BHM";
            case "MTHEAL" -> "THM";
            default -> normalized;
        };
    }

    private static DurationSpec parseDurationToken(String token, StatusCodeParseResult result) {
        String[] pair = token.split(":", 3);
        if (pair.length < 2) {
            result.addError("Invalid duration token: " + token);
            return null;
        }

        String durationType = normalizeDurationType(pair[1].trim());
        if (durationType == null) {
            result.addError("Unsupported duration type in token: " + token);
            return null;
        }

        boolean timed = isTimedDuration(durationType);
        int duration = 0;
        if (timed) {
            if (pair.length < 3) {
                result.addError("Timed durations require a duration count: " + token);
                return null;
            }
            try {
                duration = Integer.parseInt(pair[2].trim());
            } catch (NumberFormatException ex) {
                result.addError("Invalid duration count in token: " + token);
                return null;
            }
            if (duration < 1) {
                result.addError("Timed durations must be at least 1: " + token);
                return null;
            }
        }

        return new DurationSpec(durationType, duration, timed);
    }

    private static String normalizeDurationType(String token) {
        if (token == null || token.isBlank()) return null;
        String normalized = token.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        return switch (normalized) {
            case "PASSIVE" -> "Passive";
            case "MAINTAINED" -> "Maintained";
            case "TEMPORARY" -> "Temporary";
            case "TURN" -> "Turn";
            case "ROUND" -> "Round";
            case "CYCLE" -> "Cycle";
            case "NEXTATTACK" -> "Next Attack";
            default -> null;
        };
    }

    private static boolean isTimedDuration(String durationType) {
        return "Turn".equalsIgnoreCase(durationType)
                || "Round".equalsIgnoreCase(durationType)
                || "Cycle".equalsIgnoreCase(durationType)
                || "Next Attack".equalsIgnoreCase(durationType);
    }

    private static Set<String> buildValidAttributes() {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        addPrefixed(keys, "B", CharAttributes.getAttributeKeys());
        addPrefixed(keys, "M", CharAttributes.getAttributeKeys());
        addPrefixed(keys, "B", CharAttributes.getDefenseKeys());
        addPrefixed(keys, "M", CharAttributes.getDefenseKeys());
        addPrefixed(keys, "B", CharAttributes.getDamageTypeKeys());
        addPrefixed(keys, "M", CharAttributes.getDamageTypeKeys());
        addPrefixed(keys, "B", CharAttributes.getCombatKeys());
        addPrefixed(keys, "M", CharAttributes.getCombatKeys());
        addPrefixed(keys, "B", CharAttributes.getSecondaryKeys());
        addPrefixed(keys, "M", CharAttributes.getSecondaryKeys());
        addPrefixed(keys, "B", CharAttributes.getDamageKeys());
        addPrefixed(keys, "M", CharAttributes.getDamageKeys());
        for (String key : RESOURCE_KEYS) {
            keys.add(key);
        }
        for (String key : INSTANT_RESOURCE_KEYS) {
            keys.add(key);
        }
        keys.add("STUN");
        keys.add("HEAVY");
        keys.add("INCAP");
        keys.add("ROOT");
        keys.add("FLANKING");
        keys.add("HOTHP");
        keys.add("HOTSHIELD");
        keys.add("DMGTAKEN");
        return keys;
    }

    private static boolean isInstantResourceDeltaAttribute(String attribute) {
        if (attribute == null || attribute.isBlank()) return false;
        return "HP".equalsIgnoreCase(attribute.trim()) || "AURA".equalsIgnoreCase(attribute.trim());
    }

    private static void addPrefixed(Set<String> keys, String prefix, String[] values) {
        if (keys == null || prefix == null || values == null) return;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                keys.add(prefix + value.trim().toUpperCase(Locale.ROOT));
            }
        }
    }

    private static final class StatusSpec {
        private final String attribute;
        private final double severity;

        private StatusSpec(String attribute, double severity) {
            this.attribute = attribute;
            this.severity = severity;
        }
    }

    private static final class DurationSpec {
        private final String durationType;
        private final int duration;
        private final boolean timed;

        private DurationSpec(String durationType, int duration, boolean timed) {
            this.durationType = durationType;
            this.duration = duration;
            this.timed = timed;
        }
    }
}
