package eternity;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Final character detail entry.
 */
public class FrameNewFinal extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String[] SUPPORTED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};

    private final StoreCharData character;
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

    public FrameNewFinal(FrameSheet sheetFrame, StoreCharData character, FrameNew parent) {
        super("Finalize Character");
        this.character = character;
        this.parent = parent;

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setLayout(null);
        setSize(560, 430);
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
        int contentLeft = 20;
        int contentWidth = 520;
        int fourColGap = 15;
        int threeColGap = 15;
        int twoColGap = 20;
        int fourColWidth = 118;
        int threeColWidth = 163;
        int twoColWidth = 250;
        int row1Y = 55;
        int row2Y = 115;
        int row3Y = 175;
        int row4LabelY = 235;
        int row4FieldY = 255;

        int col1X = contentLeft;
        int col2X = col1X + fourColWidth + fourColGap;
        int col3X = col2X + fourColWidth + fourColGap;
        int col4X = col3X + fourColWidth + fourColGap;

        nameField = addLabeledField("Name", col1X, row1Y, fourColWidth, 20);
        nicknameField = addLabeledField("Nickname", col2X, row1Y, fourColWidth, 20);
        campaignField = addLabeledField("Campaign", col3X, row1Y, fourColWidth, 20);
        campaignStartField = addLabeledField("Campaign Start", col4X, row1Y, fourColWidth, 20);
        campaignStartField.setToolTipText("Enter the in-campaign start date; defaults to today if left blank.");

        col1X = contentLeft;
        col2X = col1X + threeColWidth + threeColGap;
        col3X = col2X + threeColWidth + threeColGap;

        raceField = addLabeledField("Race", col1X, row2Y, threeColWidth, 20);
        raceField.setEditable(false);
        classField = addLabeledField("Class", col2X, row2Y, threeColWidth, 20);
        classField.setEditable(false);
        genderField = addLabeledField("Gender", col3X, row2Y, threeColWidth, 20);

        col1X = contentLeft;
        col2X = col1X + fourColWidth + fourColGap;
        col3X = col2X + fourColWidth + fourColGap;
        col4X = col3X + fourColWidth + fourColGap;

        heightField = addLabeledField("Height", col1X, row3Y, fourColWidth, 20);
        weightField = addLabeledField("Weight", col2X, row3Y, fourColWidth, 20);
        eyesField = addLabeledField("Eyes", col3X, row3Y, fourColWidth, 20);
        hairField = addLabeledField("Hair", col4X, row3Y, fourColWidth, 20);

        JLabel physLabel = new JLabel("Physical");
        physLabel.setBounds(contentLeft, row4LabelY, twoColWidth, 20);
        add(physLabel);

        physicalArea = new JTextArea();
        JScrollPane physPane = new JScrollPane(physicalArea);
        physPane.setBounds(contentLeft, row4FieldY, twoColWidth, 90);
        physPane.setBorder(BorderFactory.createLineBorder(getForeground()));
        add(physPane);

        int rightPaneX = contentLeft + twoColWidth + twoColGap;
        JLabel persLabel = new JLabel("Personality");
        persLabel.setBounds(rightPaneX, row4LabelY, twoColWidth, 20);
        add(persLabel);

        personalityArea = new JTextArea();
        JScrollPane persPane = new JScrollPane(personalityArea);
        persPane.setBounds(rightPaneX, row4FieldY, twoColWidth, 90);
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
        back.setBounds(70, 360, 120, 26);
        back.addActionListener(e -> dispose());
        add(back);

        JButton image = new JButton("Image");
        image.setBounds(220, 360, 120, 26);
        image.addActionListener(e -> chooseImageFile());
        add(image);

        JButton accept = new JButton("Accept");
        accept.setBounds(370, 360, 120, 26);
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

    private void chooseImageFile() {
        if (character == null || character.getIdentity() == null) return;
        int characterIndex = character.getIdentity().getIndex();
        if (characterIndex < 1) {
            JOptionPane.showMessageDialog(this,
                    "Character index is not available yet, so the image cannot be imported.",
                    "Image Import Unavailable",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Character Image");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)",
                SUPPORTED_IMAGE_EXTENSIONS));
        chooser.setAcceptAllFileFilterUsed(true);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null || !selectedFile.isFile()) {
            JOptionPane.showMessageDialog(this,
                    "The selected file is not valid.",
                    "Invalid Image",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String extension = resolveExtension(selectedFile.getName());
            if (extension == null) {
                throw new IllegalArgumentException("Unsupported image type.");
            }

            BufferedImage preview = ImageIO.read(selectedFile);
            if (preview == null) {
                throw new IllegalArgumentException("The selected file could not be read as an image.");
            }

            Path imagesDir = AppPaths.imagesDir();
            Files.createDirectories(imagesDir);
            deleteExistingPortraits(imagesDir, characterIndex);

            Path targetPath = imagesDir.resolve(characterIndex + "." + extension);
            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            JOptionPane.showMessageDialog(this,
                    "Image imported for this character.",
                    "Image Imported",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to import the selected image.\n" + ex.getMessage(),
                    "Image Import Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String resolveExtension(String fileName) {
        if (fileName == null) return null;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) return null;
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        for (String candidate : SUPPORTED_IMAGE_EXTENSIONS) {
            if (candidate.equalsIgnoreCase(extension)) {
                return extension;
            }
        }
        return null;
    }

    private void deleteExistingPortraits(Path imagesDir, int characterIndex) {
        for (String extension : SUPPORTED_IMAGE_EXTENSIONS) {
            try {
                Files.deleteIfExists(imagesDir.resolve(characterIndex + "." + extension));
            } catch (Exception ignored) {
                // Best effort; import can still proceed with overwrite on the chosen extension.
            }
        }
    }
}

