package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parsed shorthand effect definition prior to target application.
 */
public class DecodedEffect {
    private String originalCode;
    private EffectTargetSide targetSide;
    private EffectTargetShape targetShape;
    private final List<DataStatus> statuses = new ArrayList<>();

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public EffectTargetSide getTargetSide() {
        return targetSide;
    }

    public void setTargetSide(EffectTargetSide targetSide) {
        this.targetSide = targetSide;
    }

    public EffectTargetShape getTargetShape() {
        return targetShape;
    }

    public void setTargetShape(EffectTargetShape targetShape) {
        this.targetShape = targetShape;
    }

    public List<DataStatus> getStatuses() {
        return Collections.unmodifiableList(statuses);
    }

    public void addStatus(DataStatus status) {
        if (status != null) {
            statuses.add(status);
        }
    }

    public boolean hasStatuses() {
        return !statuses.isEmpty();
    }
}
