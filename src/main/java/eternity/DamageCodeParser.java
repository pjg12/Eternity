package eternity;

import java.util.Locale;

/**
 * Parser/formatter for shorthand incoming damage codes.
 *
 * Format:
 * DMG_TYPE:<TYPE>_AMT:<AMOUNT>_CRUSH:<CRUSH>[_SMITE:1]
 */
public final class DamageCodeParser {
    private DamageCodeParser() {
    }

    public static DamageCodeParseResult parse(String input) {
        DamageCodeParseResult result = new DamageCodeParseResult();
        if (input == null || input.isBlank()) {
            result.error = "Damage code is empty.";
            return result;
        }

        String candidate = extractCandidate(input);
        if (candidate.isBlank()) {
            result.error = "Damage code is empty.";
            return result;
        }

        String[] tokens = candidate.split("_");
        if (tokens.length < 3 || !"DMG".equalsIgnoreCase(tokens[0].trim())) {
            result.error = "Damage code must begin with DMG_.";
            return result;
        }

        String damageType = null;
        Double amount = null;
        Double crush = 0.0;
        boolean smite = false;
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i] == null ? "" : tokens[i].trim();
            if (token.isBlank()) continue;
            String[] pair = token.split(":", 2);
            if (pair.length != 2) {
                result.error = "Invalid damage code token: " + token;
                return result;
            }
            String key = pair[0].trim().toUpperCase(Locale.ROOT);
            String value = pair[1].trim();
            switch (key) {
                case "TYPE" -> damageType = value.toUpperCase(Locale.ROOT);
                case "AMT", "AMOUNT" -> {
                    try {
                        amount = Double.parseDouble(value);
                    } catch (NumberFormatException ex) {
                        result.error = "Invalid damage amount.";
                        return result;
                    }
                }
                case "CRUSH" -> {
                    try {
                        crush = Double.parseDouble(value);
                    } catch (NumberFormatException ex) {
                        result.error = "Invalid crush amount.";
                        return result;
                    }
                }
                case "SMITE" -> smite = !"0".equals(value);
                default -> {
                    result.error = "Unsupported damage code token: " + key;
                    return result;
                }
            }
        }

        if (damageType == null || damageType.isBlank()) {
            result.error = "Damage type is required.";
            return result;
        }
        if (amount == null || amount < 0.0) {
            result.error = "Damage amount must be 0 or greater.";
            return result;
        }

        result.code = new DamageCode(damageType, amount, Math.max(0.0, crush == null ? 0.0 : crush), smite);
        return result;
    }

    public static String build(String damageType, double amount, double crush) {
        return build(damageType, amount, crush, false);
    }

    public static String build(String damageType, double amount, double crush, boolean smite) {
        String normalizedType = damageType == null ? "" : damageType.trim().toUpperCase(Locale.ROOT);
        return "DMG_TYPE:" + normalizedType
                + "_AMT:" + formatNumber(amount)
                + "_CRUSH:" + formatNumber(crush)
                + (smite ? "_SMITE:1" : "");
    }

    private static String extractCandidate(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isBlank()) return "";
        String upper = trimmed.toUpperCase(Locale.ROOT);
        int start = upper.indexOf("DMG_");
        if (start >= 0) {
            trimmed = trimmed.substring(start);
        }
        int whitespace = trimmed.indexOf(' ');
        if (whitespace >= 0) {
            trimmed = trimmed.substring(0, whitespace);
        }
        while (!trimmed.isBlank()) {
            char last = trimmed.charAt(trimmed.length() - 1);
            if (Character.isLetterOrDigit(last) || last == '_' || last == ':' || last == '.' || last == '-') {
                break;
            }
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.trim();
    }

    private static String formatNumber(double value) {
        double rounded = Math.round(value * 1000.0) / 1000.0;
        if (Math.abs(rounded - Math.rint(rounded)) <= 0.0001) {
            return Integer.toString((int) Math.round(rounded));
        }
        String text = String.format(Locale.ROOT, "%.3f", rounded);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static final class DamageCode {
        private final String damageType;
        private final double amount;
        private final double crush;
        private final boolean smite;

        private DamageCode(String damageType, double amount, double crush, boolean smite) {
            this.damageType = damageType;
            this.amount = amount;
            this.crush = crush;
            this.smite = smite;
        }

        public String getDamageType() { return damageType; }
        public double getAmount() { return amount; }
        public double getCrush() { return crush; }
        public boolean isSmite() { return smite; }
    }

    public static final class DamageCodeParseResult {
        private DamageCode code;
        private String error;

        public boolean isValid() { return code != null && error == null; }
        public DamageCode getCode() { return code; }
        public String getError() { return error == null ? "" : error; }
    }
}
