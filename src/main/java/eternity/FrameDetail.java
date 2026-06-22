package eternity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Lightweight details editor for a character's identity.
 * Replaces the legacy FrameHelper-based implementation.
 */
public class FrameDetail extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String[] SUPPORTED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final int LABEL_COLUMN_WIDTH = 0;
    private static final int FIELD_COLUMN_WIDTH = 1;
    private static final int RIGHT_LABEL_COLUMN_WIDTH = 2;
    private static final int RIGHT_FIELD_COLUMN_WIDTH = 3;

    private final FrameSheet sheetFrame;
    private final StoreRuleManager dataQuery;
    private StoreCharData character;

    private final JTextField nameField = new JTextField(10);
    private final JTextField campaignField = new JTextField(10);
    private final JTextField nicknameField = new JTextField(10);
    private final JTextField raceField = new JTextField(8);
    private final JTextField classField = new JTextField(8);
    private final JComboBox<String> subclassBox = new JComboBox<>();
    private JLabel subclassLabel;
    private final JTextField genderField = new JTextField(8);
    private final JTextField heightField = new JTextField(8);
    private final JTextField weightField = new JTextField(8);
    private final JTextField eyesField = new JTextField(8);
    private final JTextField hairField = new JTextField(8);
    private final JTextArea physicalArea = new JTextArea(4, 10);
    private final JTextArea personalityArea = new JTextArea(4, 10);
    private String loadedClassName = null;

    public FrameDetail(FrameSheet sheetFrame, StoreRuleManager dataQuery) {
        super("Character Details");
        this.sheetFrame = sheetFrame;
        this.dataQuery = dataQuery;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        subclassBox.setPrototypeDisplayValue("XXXXXXXX");

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addTwoColumnRow(gc, row++, "Name", nameField, "Campaign", campaignField);
        addTwoColumnRow(gc, row++, "Nickname", nicknameField, "Race", raceField);

        subclassLabel = new JLabel("Subclass");
        addTwoColumnRow(gc, row++, "Class", classField, subclassLabel, subclassBox);

        addTwoColumnRow(gc, row++, "Gender", genderField, "Height", heightField);
        addTwoColumnRow(gc, row++, "Weight", weightField, "Eyes", eyesField);
        addCell(gc, row, LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, new JLabel("Hair"));
        addCell(gc, row++, FIELD_COLUMN_WIDTH, 1, 0, GridBagConstraints.HORIZONTAL, hairField);

        addTextAreaRow(gc, row++, "Physical", physicalArea, "Personality", personalityArea);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton image = new JButton("Image");
        image.addActionListener(e -> chooseImageFile());
        JButton save = new JButton("Save");
        save.addActionListener(e -> confirmDetails());

        JPanel footerPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 18, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        footerPanel.add(cancel);
        footerPanel.add(image);
        footerPanel.add(save);

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = RIGHT_FIELD_COLUMN_WIDTH + 1;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        add(footerPanel, gc);
    }

    private void addTwoColumnRow(GridBagConstraints gc, int row, String leftLabel, JComponent leftComp, String rightLabel, JComponent rightComp) {
        addCell(gc, row, LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, new JLabel(leftLabel));
        addCell(gc, row, FIELD_COLUMN_WIDTH, 1, 0, GridBagConstraints.HORIZONTAL, leftComp);
        if (rightLabel != null && rightComp != null) {
            addCell(gc, row, RIGHT_LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, new JLabel(rightLabel));
            addCell(gc, row, RIGHT_FIELD_COLUMN_WIDTH, 1, 0, GridBagConstraints.HORIZONTAL, rightComp);
        }
    }

    private void addTwoColumnRow(GridBagConstraints gc, int row, String leftLabel, JComponent leftComp, JLabel rightLabel, JComponent rightComp) {
        addCell(gc, row, LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, new JLabel(leftLabel));
        addCell(gc, row, FIELD_COLUMN_WIDTH, 1, 0, GridBagConstraints.HORIZONTAL, leftComp);
        addCell(gc, row, RIGHT_LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, rightLabel);
        addCell(gc, row, RIGHT_FIELD_COLUMN_WIDTH, 1, 0, GridBagConstraints.HORIZONTAL, rightComp);
    }

    private void addTextAreaRow(GridBagConstraints gc, int row, String leftLabel, JTextArea leftArea, String rightLabel, JTextArea rightArea) {
        addCell(gc, row, LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, new JLabel(leftLabel));
        addCell(gc, row, FIELD_COLUMN_WIDTH, 1, 1, GridBagConstraints.BOTH, new JScrollPane(leftArea));
        addCell(gc, row, RIGHT_LABEL_COLUMN_WIDTH, 0, 0, GridBagConstraints.NONE, new JLabel(rightLabel));
        addCell(gc, row, RIGHT_FIELD_COLUMN_WIDTH, 1, 1, GridBagConstraints.BOTH, new JScrollPane(rightArea));
    }

    private void addCell(GridBagConstraints gc, int row, int column, double weightx, double weighty, int fill, JComponent component) {
        gc.gridx = column;
        gc.gridy = row;
        gc.weightx = weightx;
        gc.weighty = weighty;
        gc.fill = fill;
        gc.anchor = GridBagConstraints.WEST;
        add(component, gc);
    }

    public void updateDetails(StoreCharData character) {
        this.character = character;
        if (character == null || character.getIdentity() == null) return;
        var id = character.getIdentity();
        nameField.setText(id.getName());
        campaignField.setText(id.getCampaign());
        nicknameField.setText(id.getNickname());
        raceField.setText(id.getRace());
        classField.setText(id.getCharClass());
        if (!java.util.Objects.equals(loadedClassName, id.getCharClass())) {
            refreshSubclassChoices(id.getCharClass(), id.getCharSubclass());
            loadedClassName = id.getCharClass();
        } else if (id.getCharSubclass() != null && !id.getCharSubclass().isBlank()) {
            subclassBox.setSelectedItem(id.getCharSubclass());
        } else {
            subclassBox.setSelectedIndex(0);
        }
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
        if (subclass != null && subclassBox.isVisible()) {
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
            character.syncIdentityDerivedState(dataQuery);
            character.syncLevelBaseResources(dataQuery);
            character.syncLevelCombatScalers(dataQuery);
            character.updateAll();
            //StoreMetaManager.saveCharacter(character);
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshInventoryPanel();
            sheetFrame.refreshImagePanel();
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

            if (sheetFrame != null) {
                sheetFrame.invalidateCharacterPortrait(characterIndex);
                sheetFrame.refreshImagePanel();
            }

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
