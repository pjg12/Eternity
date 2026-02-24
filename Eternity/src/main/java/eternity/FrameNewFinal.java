package eternity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import java.awt.Font;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Final character detail entry.
 */
public class FrameNewFinal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CharData character;
    private final FrameNew parent;

    private JTextField nameField;
    private JTextField nicknameField;
    private JTextField campaignField;
    private JTextField raceField;
    private JTextField classField;
    private JTextField genderField;
    private JTextField heightField;
    private JTextField weightField;
    private JTextField eyesField;
    private JTextField hairField;
    private JTextArea physicalArea;
    private JTextArea personalityArea;
    private JTextField campaignStartField;

    public FrameNewFinal(FrameSheet sheetFrame, CharData character, FrameNew parent) {
        super("Finalize Character");
        this.character = character;
        this.parent = parent;

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(560, 400);
        setLocationRelativeTo(sheetFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildHeader();
        buildFields();
        buildButtons();

        populateFromCharacter();
    }

    private void buildHeader() {
        JLabel header = new JLabel("Enter Character Details", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        header.setBounds(20, 10, 520, 30);
        add(header);
    }

    private void buildFields() {
        int y = 60;

        nameField = addLabeledField("Name", 10, y, 140, 20);
        nicknameField = addLabeledField("Nickname", 190, y, 140, 20);
        campaignField = addLabeledField("Campaign", 370, y, 140, 20);
        raceField = addLabeledField("Race", 10, y + 40, 140, 20);
        raceField.setEditable(false);

        y += 40;
        classField = addLabeledField("Class", 190, y, 140, 20);
        classField.setEditable(false);
        genderField = addLabeledField("Gender", 370, y, 120, 20);
        heightField = addLabeledField("Height", 10, y + 40, 80, 20);
        weightField = addLabeledField("Weight", 110, y + 40, 80, 20);

        y += 40;
        eyesField = addLabeledField("Eyes", 200, y + 40, 120, 20);
        hairField = addLabeledField("Hair", 340, y + 40, 120, 20);

        // Campaign start date (ISO-8601 local date)
        campaignStartField = addLabeledField("Campaign Start (YYYY-MM-DD)", 400, 20, 140, 20);
        campaignStartField.setToolTipText("Enter the in-campaign start date; defaults to today if left blank.");

        y += 50;
        JLabel physLabel = new JLabel("Physical");
        physLabel.setBounds(25, y - 20, 200, 20);
        add(physLabel);

        physicalArea = new JTextArea();
        JScrollPane physPane = new JScrollPane(physicalArea);
        physPane.setBounds(25, y, 225, 90);
        physPane.setBorder(BorderFactory.createLineBorder(getForeground()));
        add(physPane);

        JLabel persLabel = new JLabel("Personality");
        persLabel.setBounds(300, y - 20, 200, 20);
        add(persLabel);

        personalityArea = new JTextArea();
        JScrollPane persPane = new JScrollPane(personalityArea);
        persPane.setBounds(300, y, 225, 90);
        persPane.setBorder(BorderFactory.createLineBorder(getForeground()));
        add(persPane);
    }

    private JTextField addLabeledField(String label, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y, w, 20);
        add(lbl);

        JTextField field = new JTextField();
        field.setBounds(x, y + 20, w, h);
        add(field);

        return field;
    }

    private void buildButtons() {
        JButton back = new JButton("Back");
        back.setBounds(140, 320, 120, 26);
        back.addActionListener(e -> dispose());
        add(back);

        JButton accept = new JButton("Accept");
        accept.setBounds(300, 320, 120, 26);
        accept.addActionListener(e -> confirmFinalize());
        add(accept);
    }

    private void populateFromCharacter() {
        var id = character.getIdentity();
        nameField.setText(id.getName());
        nicknameField.setText(id.getNickname());
        campaignField.setText(id.getCampaign());
        raceField.setText(id.getRace());
        classField.setText(id.getCharClass());
        genderField.setText(id.getGender());
        heightField.setText(id.getHeight());
        weightField.setText(id.getWeight());
        eyesField.setText(id.getEyes());
        hairField.setText(id.getHair());
        physicalArea.setText(id.getPhysical());
        personalityArea.setText(id.getPersonality());
        if (id.getCampaignStartDate() != null) {
            campaignStartField.setText(id.getCampaignStartDate().toLocalDate().toString());
        }
    }

    private void confirmFinalize() {
        var id = character.getIdentity();
        String enteredName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (enteredName.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Character name is required.", "Missing Name", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        id.setName(enteredName);
        id.setNickname(nicknameField.getText());
        id.setCampaign(campaignField.getText());
        id.setGender(genderField.getText());
        id.setHeight(heightField.getText());
        id.setWeight(weightField.getText());
        id.setEyes(eyesField.getText());
        id.setHair(hairField.getText());
        id.setPhysical(physicalArea.getText());
        id.setPersonality(personalityArea.getText());

        // parse campaign start date (optional)
        LocalDateTime campaignStart = id.getCampaignStartDate();
        try {
            String cs = campaignStartField.getText();
            if (cs != null && !cs.isBlank()) {
                campaignStart = LocalDate.parse(cs).atStartOfDay();
                id.setCampaignStartDate(campaignStart);
            }
        } catch (Exception ignored) {}
        if (campaignStart == null) {
            campaignStart = LocalDate.now().atStartOfDay();
            id.setCampaignStartDate(campaignStart);
        }

        // Set a random birthday (day/month) while preserving intended age/year if not explicitly chosen
        if (id.getBirthday() == null || !id.isBirthdayManual()) {
            int year = (id.getBirthday() != null)
                    ? id.getBirthday().getYear()           // keep existing birth year to preserve age
                    : campaignStart.toLocalDate().getYear(); // fallback: campaign year
            LocalDate birthday = CharIdentity.randomDayOfYear(year);
            id.setBirthday(birthday);

            // Ensure computed age aligns with the intended year difference
            var currentCampaignDateTime = id.getCurrentCampaignDateTime();
            if (currentCampaignDateTime != null) {
                LocalDate currentDate = currentCampaignDateTime.toLocalDate();
                int targetAge = Math.max(0, currentDate.getYear() - birthday.getYear());
                int actualAge = java.time.Period.between(birthday, currentDate).getYears();
                if (actualAge < targetAge) {
                    // Birthday is later in the year; shift back one year to match intended age
                    birthday = birthday.minusYears(1);
                    id.setBirthday(birthday);
                }
            }
        }

        long now = System.currentTimeMillis();
        if (id.getCreatedAt() == null) {
            id.setCreatedAt(new Timestamp(now));
        }
        if (id.getLastLevelUp() == null) {
            id.setLastLevelUp(new Timestamp(now));
        }
        id.setUpdated(new Timestamp(now));

        dispose();
        if (parent != null) parent.finalConfirmed();
    }
}
