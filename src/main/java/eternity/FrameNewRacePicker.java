package eternity;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Data-driven race picker with String keys, modeled after FrameNewClassPicker.
 */
public class FrameNewRacePicker extends JFrame {
    private static final String IRDON_RACE = "Irdon";
    private static final String IRDON_ANGEL_NAME_LABEL = "Angel Name";
    private static final String DEFAULT_GM_IRDON_ANGEL_NAME = "Unnamed Angel";

    // References
    private final StoreRuleManager ruleManager;
    private final StoreCharData character;
    private final FrameNewRace parent;
    private final DataRace selectedRace;
    private final boolean gmMode;

    // UI Constants
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder CENTER_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final int[] GB_COLUMN_WIDTHS = new int[] { 250, 250 };
    private static final Insets CENTER_INSETS = new Insets(10, 10, 10, 10);
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 420;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int BUTTON_SPACING = 10;

    // UI Strings
    private static final String WINDOW_TITLE = " Options";
    private static final String HEADER_TEXT = " Customization";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_CONFIRM = "Confirm";
    private static final String EMPTY_OPTION = "***";
    private static final String[] RACE_OPTIONS = { EMPTY_OPTION,"Alteri","Aquata","Ardian","Azuri","Boxlor","Cetryu","Construct","Deckan","En","Evan","Felsh","Felsh Cat","Forven","Gaian","Irdon","Kenti","Kitsune", "Loben","Loritho","Nohmen","Nosfer","Oon","Poruuk","Quez","Raigon","Reven", "Skren","Theran","Vindis","Vyrek","Xid","Zyan" };
    
    // UI Elements
    private JPanel headerPanel, centerPanel, footerPanel;
    private JLabel headerL;
    private JLabel[] optionLabels;
    private JComboBox<String>[] optionBoxes;
    private JTextField irdonAngelNameField;
    private JButton cancelButton, confirmButton;

    // Maps
    private final Map<String, String[]> raceChoicesMap;
    private final Map<String, JComboBox<String>> fields = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public FrameNewRacePicker(StoreRuleManager ruleManager, StoreCharData character, DataRace selectedRace, FrameNewRace parent, boolean gmMode) {
        super(WINDOW_TITLE);
        this.ruleManager = ruleManager;
        this.character = character;
        this.selectedRace = selectedRace;
        this.parent = parent;
        this.gmMode = gmMode;

        this.raceChoicesMap = makeChoiceMap(selectedRace);
        if (raceChoicesMap != null) this.optionLabels = new JLabel[raceChoicesMap.keySet().size()];
        if (raceChoicesMap != null) this.optionBoxes = (JComboBox<String>[]) new JComboBox[raceChoicesMap.size()];

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(BUTTON_SPACING, BUTTON_SPACING));

        buildUI();

        if (gmMode) {
            applyGmSelectionsAndConfirm();
        }
    }

    // ---------------------------------------------------------
    // Build UI
    // ---------------------------------------------------------

    private void buildUI() {
        buildHeader();
        buildCenter();
        buildFooter();
    }

    private void buildHeader() {
        // Build panel
        headerPanel = new JPanel(new BorderLayout());

        // Build header
        headerL = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerL.setFont(HEADER_FONT);
        headerL.setBorder(HEADER_BORDER);

        // Add elements
        headerPanel.add(headerL, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void buildCenter() {
        // Build panel
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = GB_COLUMN_WIDTHS;
        centerPanel = new JPanel(layout);
        centerPanel.setBorder(CENTER_BORDER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = CENTER_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Setup Variables
        int tileIndex = 0;
        int y;
        int x;
        int width;

        if (requiresIrdonAngelName()) {
            gridHelper(gbc, 0, 0, 1);
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
            choicePanel.setBorder(CENTER_BORDER);

            JLabel lbl = buildLabel(IRDON_ANGEL_NAME_LABEL);
            choicePanel.add(lbl);

            irdonAngelNameField = new JTextField(getInitialIrdonAngelName());
            choicePanel.add(irdonAngelNameField);

            centerPanel.add(choicePanel, gbc);
        }

        if (raceChoicesMap == null || raceChoicesMap.isEmpty()) {
            add(centerPanel, BorderLayout.CENTER);
            return;
        }
 
        for (String label : raceChoicesMap.keySet()) {
            // Setup Grid
            y = tileIndex / 2;
            x = tileIndex % 2;
            width = 1;
            gridHelper(gbc, y, x, width);

            // Build panel
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
            choicePanel.setBorder(CENTER_BORDER);

            // Build Label
            JLabel lbl = buildLabel(label);
            choicePanel.add(lbl);
            optionLabels[tileIndex] = lbl;

            // Build Choice
            String[] choice = raceChoicesMap.get(label);
            JComboBox<String> choiceBox = buildComboBox(choice);
            choicePanel.add(choiceBox);
            optionBoxes[tileIndex] = choiceBox;

            centerPanel.add(choicePanel, gbc);
            fields.put(label, choiceBox);
            tileIndex++;
        }
        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildFooter() {
        // Build panel
        footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));

        // Build buttons
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        confirmButton = new JButton(BUTTON_CONFIRM);
        confirmButton.addActionListener(e -> onConfirmPressed());

        // Add buttons
        footerPanel.add(cancelButton);
        footerPanel.add(confirmButton);

        // Add panels
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JComboBox<String> buildComboBox(String[] choices) {
        // Add empty choice
        JComboBox<String> box = new JComboBox<>();
        box.addItem(EMPTY_OPTION);

        // Add choices
        if (choices == null) return null;
        for (String option : choices) {
            if (option != null && !EMPTY_OPTION.equals(option)) {
                box.addItem(option);
            }
        }
        return box;
    }

    private JLabel buildLabel(String s) {
        JLabel lbl = new JLabel(s);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------

    private void onCancelPressed() {
        dispose();
    }

    public void onConfirmPressed() {
        List<String> raceChoices = java.util.List.of();
        if ((selectedRace.getRacePick() && raceChoicesMap != null && !raceChoicesMap.isEmpty()) || requiresIrdonAngelName()) {
            raceChoices = new ArrayList<>(raceChoicesMap.size());
            if (raceChoicesMap != null) {
                for (String label : raceChoicesMap.keySet()) {
                    String choice = (String) fields.get(label).getSelectedItem();
                    if (choice == null || choice.equals(EMPTY_OPTION)) {
                        JOptionPane.showMessageDialog(this, "Please complete all fields.");
                        return;
                    }
                    raceChoices.add(choice);
                }
            }
            if (requiresIrdonAngelName()) {
                String angelName = resolveInlineIrdonAngelName();
                if (angelName == null) {
                    return;
                }
                raceChoices.add(angelName);
            }

            // Add choices to character
            character.getIdentity().setCharRacePick(raceChoices);
        }

        // Pass control back and close
        parent.onConfirmPressed(raceChoices);
        dispose();
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private void gridHelper (GridBagConstraints gbc, int y, int x, int width) {
        gbc.gridwidth = width;
        gbc.gridy = y;
        gbc.gridx = x;
    }

    private Map<String, String[]> makeChoiceMap(DataRace race) {
        // Generate new map
        Map<String, String[]> map = new LinkedHashMap<>();
        if (race == null || race.getName() == null) return map;
        String name = race.getName();

        // Input options based on class name
        switch (name) {
            case "Alteri" -> {
                map.put("Shapeshift", RACE_OPTIONS);
            }
            default -> { return map; }
        }
        return map;
    }

    private void applyGmSelectionsAndConfirm() {
        if (raceChoicesMap != null) {
            for (String label : raceChoicesMap.keySet()) {
                JComboBox<String> box = fields.get(label);
                if (box == null || box.getItemCount() <= 1) continue;
                box.setSelectedIndex(randomChoiceIndex(box));
            }
        }
        onConfirmPressed();
    }

    private int randomChoiceIndex(JComboBox<String> box) {
        int nonEmptyOptions = box.getItemCount() - 1;
        if (nonEmptyOptions <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(nonEmptyOptions) + 1;
    }

    private boolean requiresIrdonAngelName() {
        return selectedRace != null
                && selectedRace.getName() != null
                && IRDON_RACE.equalsIgnoreCase(selectedRace.getName().trim());
    }

    private String getInitialIrdonAngelName() {
        if (character != null && character.getIdentity() != null) {
            List<String> existing = character.getIdentity().getCharRacePick();
            if (existing != null && !existing.isEmpty() && existing.get(existing.size() - 1) != null) {
                return existing.get(existing.size() - 1).trim();
            }
        }
        return gmMode ? DEFAULT_GM_IRDON_ANGEL_NAME : "";
    }

    private String resolveInlineIrdonAngelName() {
        if (gmMode) {
            return DEFAULT_GM_IRDON_ANGEL_NAME;
        }
        String angelName = irdonAngelNameField == null ? "" : irdonAngelNameField.getText().trim();
        if (angelName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter an angel name.");
            return null;
        }
        return angelName;
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
/// ////////////////////////////////////////////////////////////////////////////////////////////
/// ////////////////////////////////////////////////////////////////////////////////////////////

/*

    private void buildButtons() {
        clearButton = new JButton("Clear");
        clearButton.setBounds(120, 330, 150, 28);
        clearButton.addActionListener(e -> clearAndClose());
        add(clearButton);

        acceptButton = new JButton("Accept");
        acceptButton.setBounds(350, 330, 150, 28);
        acceptButton.addActionListener(e -> acceptChoices());
        add(acceptButton);
    }

    private Map<String, ChoiceConfig> buildChoiceModel(DataRace race) {
        Map<String, ChoiceConfig> map = new LinkedHashMap<>();

        if (race == null || !race.getRacePick()) {
            return map;
        }

        String raceName = race.getName();

        switch (raceName) {
            case "Alteri" -> map.put("Shapeshift", cfgStatic(getShapeshiftRaceOptions(raceName)));
            default -> map.put("Selection", cfgStatic(DEFAULT_OPTIONS));
        }

        return map;
    }

    private ChoiceConfig cfgStatic(String[] vals) {
        ChoiceConfig c = new ChoiceConfig(ChoiceType.STATIC);
        c.options = vals;
        return c;
    }

    private String[] getShapeshiftRaceOptions(String currentRaceName) {
        String[] allNames = getSortedRaceNames();
        if (allNames.length == 0) return SHAPESHIFT_OPTIONS;

        ArrayList<String> names = new ArrayList<>(allNames.length);
        for (String name : allNames) {
            if (!name.equalsIgnoreCase(currentRaceName)) {
                names.add(name);
            }
        }
        return names.toArray(new String[0]);
    }

    private void renderChoices() {
        int y = 70;

        for (String label : choiceModel.keySet()) {
            ChoiceConfig cfg = choiceModel.get(label);

            JLabel lbl = new JLabel(label);
            lbl.setBounds(25, y, 250, 20);
            add(lbl);

            JComboBox<String> box = new JComboBox<>();
            box.setBounds(25, y + 25, 260, 22);
            add(box);

            fields.put(label, box);
            initComboBox(cfg, box);

            y += 65;
        }
    }

    private void initComboBox(ChoiceConfig cfg, JComboBox<String> box) {
        box.addItem(EMPTY_OPTION);

        if (cfg.type == ChoiceType.STATIC && cfg.options != null) {
            for (String option : cfg.options) {
                box.addItem(option);
            }
        }
    }

    private void acceptChoices() {
        ArrayList<String> raceChoices = new ArrayList<>(choiceModel.size());

        for (String label : choiceModel.keySet()) {
            JComboBox<String> box = fields.get(label);
            String value = box != null ? (String) box.getSelectedItem() : null;

            if (value == null || value.equals(EMPTY_OPTION)) {
                JOptionPane.showMessageDialog(this, "Please complete all fields.");
                return;
            }

            raceChoices.add(value);
        }

        character.getIdentity().setCharRacePick(raceChoices);
        parent.raceChoicesConfirmed(raceChoices);
        dispose();
    }

    private void clearAndClose() {
        dispose();
    }

    private enum ChoiceType {
        STATIC
    }

    private static class ChoiceConfig {
        ChoiceType type;
        String[] options;

        ChoiceConfig(ChoiceType type) {
            this.type = type;
        }
    }

    private String[] getSortedRaceNames() {
        String[] cached = sortedRaceNamesCache;
        if (cached != null) {
            return cached;
        }

        List<DataRace> allRaces = dataQuery.getRaceData();
        if (allRaces == null || allRaces.isEmpty()) {
            sortedRaceNamesCache = SHAPESHIFT_OPTIONS;
            return SHAPESHIFT_OPTIONS;
        }

        java.util.TreeSet<String> names = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (DataRace race : allRaces) {
            if (race == null) continue;
            String name = race.getName();
            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }

        String[] built = names.toArray(new String[0]);
        sortedRaceNamesCache = built;
        return built;
    }

    private static final String[] SHAPESHIFT_OPTIONS = {};

    private static final String[] DEFAULT_OPTIONS = {
            "Option 1",
            "Option 2",
            "Option 3"
    };
}

*/
