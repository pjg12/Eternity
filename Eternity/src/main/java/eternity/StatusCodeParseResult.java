package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result wrapper for shorthand status parsing.
 */
public class StatusCodeParseResult {
    private DecodedEffect effect;
    private final List<String> errors = new ArrayList<>();

    public DecodedEffect getEffect() {
        return effect;
    }

    public void setEffect(DecodedEffect effect) {
        this.effect = effect;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void addError(String error) {
        if (error != null && !error.isBlank()) {
            errors.add(error);
        }
    }

    public boolean isSuccess() {
        return effect != null && errors.isEmpty();
    }
}
