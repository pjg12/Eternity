package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parsed shorthand effect definition prior to application.
 */
public class DecodedEffect {
    private String originalCode;
    private String title = "";
    private String affinity = "";
    private String durationType = "";
    private int duration;
    private boolean durationSpecified;
    private final List<DataStatus> statuses = new ArrayList<>();

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity == null ? "" : affinity;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType == null ? "" : durationType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isDurationSpecified() {
        return durationSpecified;
    }

    public void setDurationSpecified(boolean durationSpecified) {
        this.durationSpecified = durationSpecified;
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
