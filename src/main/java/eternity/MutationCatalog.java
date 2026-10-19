package eternity;

import java.util.List;

/**
 * Presently available Body mutations. The list is not all-inclusive; a narrator may approve others.
 */
public final class MutationCatalog {
    public record MutationOption(String name, String description, int maxSelections, int alPerSelection) {}

    public static final String LIST_NAME = "Known Mutations";
    public static final String TECHNIQUE_NAME = "Evolved Mutation";

    private static final List<MutationOption> OPTIONS = List.of(
            new MutationOption(
                    "Additional Arm",
                    "May be selected a maximum of 4 times when activating Mutation. Arms can grasp items, provide a circumstantial STR bonus, and aid grappling, climbing, and similar tasks.",
                    4,
                    1),
            new MutationOption(
                    "Additional Leg",
                    "May be selected a maximum of 4 times when activating Mutation. Legs aid balance and provide bonus movement speed.",
                    4,
                    1),
            new MutationOption(
                    "Adjust size category",
                    "May be selected only 1 time when activating Mutation. Counts as 2 mutations when activating Mutation.",
                    1,
                    2),
            new MutationOption(
                    "Gain Natural Weapons",
                    "May be selected a maximum of 2 times when activating Mutation. Grants 1 weapon per mutation, such as a claw, bite, or tail. The weapon is considered wielded for additional attacks. If proficient with unarmed attacks, increases unarmed level dice.",
                    2,
                    1),
            new MutationOption(
                    "Additional Eyes",
                    "May be selected a maximum of 2 times when activating Mutation. Grants PERC bonuses of +1. Grants low-light vision with 1 application and darkvision with 2.",
                    2,
                    1),
            new MutationOption(
                    "Exoskeleton",
                    "May be selected a maximum of 3 times when activating Mutation. Grants +1 natural armor bonus to AC per application.",
                    3,
                    1),
            new MutationOption(
                    "Grow / Strengthen Wings",
                    "May be selected a maximum of 2 times when activating Mutation. Grants natural flight: half movement speed with 1 application and full movement speed with 2.",
                    2,
                    1));

    private MutationCatalog() {}

    public static List<MutationOption> options() {
        return OPTIONS;
    }

    public static String[] names() {
        String[] names = new String[OPTIONS.size()];
        for (int i = 0; i < OPTIONS.size(); i++) {
            names[i] = OPTIONS.get(i).name();
        }
        return names;
    }

    public static MutationOption find(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        for (MutationOption option : OPTIONS) {
            if (option.name().equalsIgnoreCase(trimmed)) return option;
        }
        return null;
    }

    public static String descriptionFor(String name) {
        MutationOption option = find(name);
        return option == null ? "" : option.description();
    }
}
