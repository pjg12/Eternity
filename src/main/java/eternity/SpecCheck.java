package eternity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpecCheck {
    private List<DataSpecialty> characterSpecialties;
    private CharInventory inventory;
    private Consumer<String> panelReminderSetter;

    /** Stores a direct reference to the specialties list for subsequent checks. */
    public void setCharacterSpecialtiesReference(List<DataSpecialty> specialties) {
        this.characterSpecialties = specialties;
    }

    /** Stores inventory reference used by proficiency checks. */
    public void setInventoryReference(CharInventory inventory) {
        this.inventory = inventory;
    }

    /** Callback used by reminder checks to update the current panel reminder text. */
    public void setPanelReminderSetter(Consumer<String> panelReminderSetter) {
        this.panelReminderSetter = panelReminderSetter;
    }

    /** Runs checks against all specialties currently referenced by this checker. */
    public void runChecks() {
        if (characterSpecialties == null) return;
        for (DataSpecialty special : characterSpecialties) {
            checkSpec(special);
        }
    }

    public void checkSpec(DataSpecialty special) {
        if (special == null) return;
        String type = special.getType();
        if (type == null) return;
        if (type.equalsIgnoreCase("proficiency")) profCheck(special);
        else if (type.equalsIgnoreCase("reminder")) remindCheck(special);
    }

    private void profCheck(DataSpecialty special) {
        if (inventory == null) return;
        String refName = special.getRefName();
        if (refName == null || refName.isBlank()) return;
        boolean hasRef = inventory.getWeaponProficiencies().stream()
                .anyMatch(p -> p != null && p.equalsIgnoreCase(refName));
        if (!hasRef) {
            inventory.addWeaponProficiency(refName);
        }
    }

    private void remindCheck(DataSpecialty special) {
        if (panelReminderSetter == null) return;
        String refName = special.getRefName();
        String specialName = special.getName() == null ? "" : special.getName();
        if ("Domain Emanation".equalsIgnoreCase(specialName)) {
            panelReminderSetter.accept("Domain Emanation::On|Off");
            return;
        }
        if ("Nimble Feet".equalsIgnoreCase(specialName)) {
            panelReminderSetter.accept("Negate All Displacement Damage");
            return;
        }
        if (specialName.toLowerCase().startsWith("fighting form")) {
            String fightingFormReminder = (refName != null && !refName.isBlank()) ? refName.trim() : specialName;
            if (!fightingFormReminder.isBlank()) {
                panelReminderSetter.accept(fightingFormReminder);
            }
            return;
        }
        String label;
        if (special.getName() != null
                && special.getName().equalsIgnoreCase(CharSpecials.SKILL_DEDICATION_SPECIALTY)
                && refName != null && !refName.isBlank()) {
            label = special.getName() + " (" + refName + ")";
        } else {
            label = (refName != null && !refName.isBlank()) ? special.getName() + ": " + refName : special.getName();
        }
        String description = special.getDescription();
        boolean suppressDescriptionLine = "Shapeshifting (Alteri)".equalsIgnoreCase(specialName)
                || "Felshify (Felsh Cat)".equalsIgnoreCase(specialName);

        if (special.getPick()) {
            if (label == null || label.isBlank()) return;
            List<String> options;
            if (specialName.toLowerCase().contains("felshify")) {
                options = new ArrayList<>();
                options.add("Cat");
                options.add("Felsh");
            } else {
                options = extractOptions(refName);
                if (options.isEmpty()) {
                    options = extractOptions(special.getDescription());
                }
            }
            if (options.isEmpty()) {
                options = new ArrayList<>();
                options.add("Empty");
            }
            panelReminderSetter.accept(label + "::" + String.join("|", options));
        }
        if (!suppressDescriptionLine && description != null && !description.isBlank()) {
            panelReminderSetter.accept(description.trim());
        } else if (!special.getPick() && label != null && !label.isBlank()) {
            panelReminderSetter.accept(label);
        }
    }

    private List<String> extractOptions(String raw) {
        ArrayList<String> options = new ArrayList<>();
        if (raw == null || raw.isBlank()) return options;
        String[] parts = raw.contains("|") ? raw.split("\\|") : raw.split(",");
        if (parts.length <= 1) return options;
        for (String p : parts) {
            if (p == null) continue;
            String trimmed = p.trim();
            if (!trimmed.isBlank()) options.add(trimmed);
        }
        return options;
    }

}
