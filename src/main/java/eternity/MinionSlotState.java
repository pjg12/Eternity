package eternity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persisted state for one Leader minion slot.
 */
public class MinionSlotState {
    @JsonProperty private String slotTitle;
    @JsonProperty private boolean summoned;
    @JsonProperty private String form;
    @JsonProperty private String type;
    @JsonProperty private boolean flashMobCopy;
    @JsonProperty private String flashMobSourceSlot;

    public MinionSlotState() {
        this("", false, "", "");
    }

    public MinionSlotState(String slotTitle) {
        this(slotTitle, false, "", "");
    }

    public MinionSlotState(String slotTitle, boolean summoned, String form, String type) {
        this(slotTitle, summoned, form, type, false, "");
    }

    public MinionSlotState(String slotTitle, boolean summoned, String form, String type,
                           boolean flashMobCopy, String flashMobSourceSlot) {
        this.slotTitle = safe(slotTitle);
        this.summoned = summoned;
        this.form = safe(form);
        this.type = safe(type);
        this.flashMobCopy = flashMobCopy;
        this.flashMobSourceSlot = safe(flashMobSourceSlot);
    }

    public MinionSlotState(MinionSlotState src) {
        this(src == null ? "" : src.slotTitle,
                src != null && src.summoned,
                src == null ? "" : src.form,
                src == null ? "" : src.type,
                src != null && src.flashMobCopy,
                src == null ? "" : src.flashMobSourceSlot);
    }

    public String getSlotTitle() { return slotTitle; }
    public void setSlotTitle(String slotTitle) { this.slotTitle = safe(slotTitle); }

    public boolean isSummoned() { return summoned; }
    public void setSummoned(boolean summoned) { this.summoned = summoned; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = safe(form); }

    public String getType() { return type; }
    public void setType(String type) { this.type = safe(type); }

    public boolean isFlashMobCopy() { return flashMobCopy; }
    public void setFlashMobCopy(boolean flashMobCopy) { this.flashMobCopy = flashMobCopy; }

    public String getFlashMobSourceSlot() { return flashMobSourceSlot; }
    public void setFlashMobSourceSlot(String flashMobSourceSlot) {
        this.flashMobSourceSlot = safe(flashMobSourceSlot);
    }

    @JsonIgnore
    public String buildDisplayName() {
        StringBuilder label = new StringBuilder(slotTitle == null ? "" : slotTitle.trim());
        String formValue = form == null ? "" : form.trim();
        String typeValue = type == null ? "" : type.trim();
        if (!formValue.isBlank() || !typeValue.isBlank()) {
            label.append(" (");
            if (!formValue.isBlank()) {
                label.append(formValue);
            }
            if (!formValue.isBlank() && !typeValue.isBlank()) {
                label.append(" / ");
            }
            if (!typeValue.isBlank()) {
                label.append(typeValue);
            }
            label.append(")");
        }
        if (flashMobCopy) {
            label.append(" [Copy]");
        }
        return label.toString();
    }

    @JsonIgnore
    public int getInitiativeOffset() {
        if (flashMobCopy && flashMobSourceSlot != null && !flashMobSourceSlot.isBlank()) {
            return switch (flashMobSourceSlot.trim()) {
                case "Minor 1" -> -4;
                case "Minor 2" -> -8;
                case "Minor 3" -> -2;
                case "Medium 1" -> -3;
                case "Medium 2" -> -7;
                case "Major" -> -6;
                default -> 0;
            };
        }
        return switch (slotTitle == null ? "" : slotTitle.trim()) {
            case "Minor 1" -> -4;
            case "Minor 2" -> -8;
            case "Minor 3" -> -2;
            case "Medium 1" -> -3;
            case "Medium 2" -> -7;
            case "Major" -> -6;
            default -> 0;
        };
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
