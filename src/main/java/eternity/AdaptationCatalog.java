package eternity;

import java.util.List;

/**
 * Presently available Nature adaptations. The list is not all-inclusive; a narrator may approve others.
 */
public final class AdaptationCatalog {
    public record AdaptationOption(String name, String description, int maxSelections, int alPerSelection) {}

    public static final String LIST_NAME = "Known Adaptations";
    public static final String TECHNIQUE_NAME = "Feral Adaptation";

    private static final List<AdaptationOption> OPTIONS = List.of(
            new AdaptationOption(
                    "Additional Arm",
                    "May be selected a maximum of 4 times when activating Adaptation. Arms can grasp items, provide a circumstantial STR bonus, and aid grappling, climbing, and similar tasks.",
                    4,
                    1),
            new AdaptationOption(
                    "Additional Leg",
                    "May be selected a maximum of 4 times when activating Adaptation. Legs aid balance and provide bonus movement speed.",
                    4,
                    1),
            new AdaptationOption(
                    "Adjust size category",
                    "May be selected only 1 time when activating Adaptation. Counts as 2 mutations when activating Adaptation.",
                    1,
                    2),
            new AdaptationOption(
                    "Gain Natural Weapons",
                    "May be selected a maximum of 2 times when activating Adaptation. Grants 1 weapon per mutation, such as a claw, bite, or tail. The weapon is considered wielded for additional attacks. If proficient with unarmed attacks, increases unarmed level dice.",
                    2,
                    1),
            new AdaptationOption(
                    "Additional Eyes",
                    "May be selected a maximum of 2 times when activating Adaptation. Grants PERC bonuses of +1. Grants low-light vision with 1 application and darkvision with 2.",
                    2,
                    1),
            new AdaptationOption(
                    "Exoskeleton",
                    "May be selected a maximum of 3 times when activating Adaptation. Grants +1 natural armor bonus to AC per application.",
                    3,
                    1),
            new AdaptationOption(
                    "Grow / Strengthen Wings",
                    "May be selected a maximum of 2 times when activating Adaptation. Grants natural flight: half movement speed with 1 application and full movement speed with 2.",
                    2,
                    1));

    private AdaptationCatalog() {}

    public static List<AdaptationOption> options() {
        return OPTIONS;
    }

    public static String[] names() {
        String[] names = new String[OPTIONS.size()];
        for (int i = 0; i < OPTIONS.size(); i++) {
            names[i] = OPTIONS.get(i).name();
        }
        return names;
    }

    public static AdaptationOption find(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        for (AdaptationOption option : OPTIONS) {
            if (option.name().equalsIgnoreCase(trimmed)) return option;
        }
        return null;
    }

    public static String descriptionFor(String name) {
        AdaptationOption option = find(name);
        return option == null ? "" : option.description();
    }
}
