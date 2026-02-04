package eternity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
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
    private CharData character;

    private final JTextField nameField = new JTextField(20);
    private final JTextField campaignField = new JTextField(20);
    private final JTextField nicknameField = new JTextField(20);
    private final JTextField raceField = new JTextField(15);
    private final JTextField classField = new JTextField(15);
    private final JTextField genderField = new JTextField(10);
    private final JTextField heightField = new JTextField(10);
    private final JTextField weightField = new JTextField(10);
    private final JTextField eyesField = new JTextField(10);
    private final JTextField hairField = new JTextField(10);
    private final JTextArea physicalArea = new JTextArea(4, 20);
    private final JTextArea personalityArea = new JTextArea(4, 20);

    public FrameDetail(FrameSheet sheetFrame) {
        super("Character Details");
        this.sheetFrame = sheetFrame;
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
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        add(field, gc);
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
}
