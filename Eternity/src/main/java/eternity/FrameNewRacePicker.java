package eternity;

import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Data-driven race picker with String keys, modeled after FrameNewClassPicker.
 */
public class FrameNewRacePicker extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String EMPTY_OPTION = "***";
    private static volatile String[] sortedRaceNamesCache;
    private final DataQuery dataQuery;
    private final CharData character;
    private final FrameNewRace parent;
    private final DataRace selectedRace;

    private final Map<String, JComboBox<String>> fields = new LinkedHashMap<>();
    private Map<String, ChoiceConfig> choiceModel;

    private JButton clearButton;
    private JButton acceptButton;
    private JLabel headerLabel;

    public FrameNewRacePicker(FrameSheet sheetFrame,
                              DataQuery dataQuery,
                              CharData character,
                              DataRace selectedRace,
                              FrameNewRace parent) {

        super("Race Options");

        this.dataQuery = dataQuery;
        this.character = character;
        this.selectedRace = selectedRace;
        this.parent = parent;

        setSize(640, 420);
        setLayout(null);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildHeader();
        buildButtons();

        this.choiceModel = buildChoiceModel(selectedRace);
        renderChoices();

        setVisible(true);
    }

    private void buildHeader() {
        headerLabel = new JLabel(selectedRace.getName() + " Options", SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 22f));
        headerLabel.setBounds(10, 10, 600, 40);
        add(headerLabel);
    }

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

        List<DataRace> allRaces = dataQuery.searchRaceByName("");
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
