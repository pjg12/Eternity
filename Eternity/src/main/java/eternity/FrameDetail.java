package eternity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Lightweight details editor for a character's identity.
 * Replaces the legacy FrameHelper-based implementation.
 */
public class FrameDetail extends JFrame {
    private static final long serialVersionUID = 1L;

    private final FrameSheet sheetFrame;
    private final DataQuery dataQuery;
    private CharData character;

    private final JTextField nameField = new JTextField(20);
    private final JTextField campaignField = new JTextField(20);
    private final JTextField nicknameField = new JTextField(20);
    private final JTextField raceField = new JTextField(15);
    private final JTextField classField = new JTextField(15);
    private final JComboBox<String> subclassBox = new JComboBox<>();
    private JLabel subclassLabel;
    private final JTextField genderField = new JTextField(10);
    private final JTextField heightField = new JTextField(10);
    private final JTextField weightField = new JTextField(10);
    private final JTextField eyesField = new JTextField(10);
    private final JTextField hairField = new JTextField(10);
    private final JTextArea physicalArea = new JTextArea(4, 20);
    private final JTextArea personalityArea = new JTextArea(4, 20);

    public FrameDetail(FrameSheet sheetFrame, DataQuery dataQuery) {
        super("Character Details");
        this.sheetFrame = sheetFrame;
        this.dataQuery = dataQuery;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int row = 0;

        addRow(gc, row++, "Name", nameField);
        addRow(gc, row++, "Campaign", campaignField);
        addRow(gc, row++, "Nickname", nicknameField);
        addRow(gc, row++, "Race", raceField);
        addRow(gc, row++, "Class", classField);

        // Subclass row (needs label reference for visibility toggling)
        subclassLabel = new JLabel("Subclass");
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        add(subclassLabel, gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        add(subclassBox, gc);
        row++;

        addRow(gc, row++, "Gender", genderField);
        addRow(gc, row++, "Height", heightField);
        addRow(gc, row++, "Weight", weightField);
        addRow(gc, row++, "Eyes", eyesField);
        addRow(gc, row++, "Hair", hairField);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        add(new JLabel("Physical"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(physicalArea), gc);
        row++;

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        add(new JLabel("Personality"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(personalityArea), gc);
        row++;

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton save = new JButton("Save");
        save.addActionListener(e -> confirmDetails());

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        add(cancel, gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.EAST;
        add(save, gc);
    }

    private void addRow(GridBagConstraints gc, int row, String label, JTextField field) {
        addRow(gc, row, label, (JComponent) field);
    }

    private void addRow(GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        add(comp, gc);
    }

    public void updateDetails(CharData character) {
        this.character = character;
        if (character == null || character.getIdentity() == null) return;
        var id = character.getIdentity();
        nameField.setText(id.getName());
        campaignField.setText(id.getCampaign());
        nicknameField.setText(id.getNickname());
        raceField.setText(id.getRace());
        classField.setText(id.getCharClass());
        refreshSubclassChoices(id.getCharClass(), id.getCharSubclass());
        toggleSubclassVisibility(id.getLevel() < 5);
        genderField.setText(id.getGender());
        heightField.setText(id.getHeight());
        weightField.setText(id.getWeight());
        eyesField.setText(id.getEyes());
        hairField.setText(id.getHair());
        physicalArea.setText(id.getPhysical());
        personalityArea.setText(id.getPersonality());
    }

    private void confirmDetails() {
        if (character == null || character.getIdentity() == null) {
            setVisible(false);
            return;
        }
        var id = character.getIdentity();
        id.setName(nameField.getText());
        id.setCampaign(campaignField.getText());
        id.setNickname(nicknameField.getText());
        id.setRace(raceField.getText());
        id.setCharClass(classField.getText());
        Object subclass = subclassBox.getSelectedItem();
        if (subclass != null) {
            id.setCharSubclass(subclass.toString());
        }
        id.setGender(genderField.getText());
        id.setHeight(heightField.getText());
        id.setWeight(weightField.getText());
        id.setEyes(eyesField.getText());
        id.setHair(hairField.getText());
        id.setPhysical(physicalArea.getText());
        id.setPersonality(personalityArea.getText());

        if (sheetFrame != null) {
            sheetFrame.loadCharacter(character);
            CharacterDataManager.saveCharacter(character);
        }
        setVisible(false);
    }

    private void toggleSubclassVisibility(boolean visible) {
        subclassLabel.setVisible(visible);
        subclassBox.setVisible(visible);
    }

    private void refreshSubclassChoices(String className, String currentSubclass) {
        subclassBox.removeAllItems();
        subclassBox.addItem("***");

        if (dataQuery != null && className != null && !className.isBlank()) {
            DataClass base = dataQuery.getClassByName(className);
            if (base != null) {
                int id = base.getID();
                DataClass s1 = dataQuery.getClassById(id + 1);
                DataClass s2 = dataQuery.getClassById(id + 2);
                if (s1 != null) subclassBox.addItem(s1.getName());
                if (s2 != null) subclassBox.addItem(s2.getName());
            }
        }

        if (currentSubclass != null && !currentSubclass.isBlank()) {
            boolean exists = false;
            for (int i = 0; i < subclassBox.getItemCount(); i++) {
                if (currentSubclass.equals(subclassBox.getItemAt(i))) {
                    exists = true;
                    break;
                }
            }
            if (!exists) subclassBox.addItem(currentSubclass);
            subclassBox.setSelectedItem(currentSubclass);
        } else {
            subclassBox.setSelectedIndex(0);
        }
    }
}
