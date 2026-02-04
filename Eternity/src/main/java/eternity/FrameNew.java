package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

/**
 * Character Creation Wizard
 */
public class FrameNew extends JFrame {
    private final DataQuery dataQuery;
    private final FrameSheet sheetFrame;
    private final CharData character;

    private static final int ICON_SIZE = 100;
    private static final int STEP_COUNT = 6;
    private static final String[] STEPS = { "Class", "Race", "Attributes", "Skills", "Specialties", "Affinity" };

    // Icons
    private ImageIcon[] iconNormal;
    private ImageIcon[] iconHover;
    private ImageIcon[] iconDone;

    // UI Elements
    private JLabel headerLabel;
    private JButton[] stepButtons;
    private JLabel[] stepLabels;
    private JButton cancelButton;
    private JButton finalizeButton;
    private FrameNewFinal finalFrame;

    // Step completion state
    private final boolean[] stepDone;

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    public FrameNew(FrameSheet sheetFrame, DataQuery dataQuery, CharData character) {
        this(sheetFrame, dataQuery, character, -1);
    }

    /**
     * Creates a new character builder. When {@code age} is non-negative, the
     * character's birthday is randomized to a date that makes them that many
     * years old in the current campaign year.
     */
    public FrameNew(FrameSheet sheetFrame, DataQuery dataQuery, CharData character, int age) {
        super("Character Builder");
        this.sheetFrame = sheetFrame;
        this.dataQuery = dataQuery;
        this.character = character;
        this.stepDone = new boolean[STEP_COUNT];

        if (age >= 0 && this.character != null && this.character.getIdentity() != null) {
            this.character.getIdentity().randomBirthday(age);
        }

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);      

        loadIcons();
        initWindow();
        buildHeader();
        buildCenter();
        buildFooter();

        setVisible(true);
        updateFrame();
    }

    // --------------------------------------------------------------------------
    // Window Setup
    // --------------------------------------------------------------------------
    private void initWindow() {
        setSize(500, 520);
        setMinimumSize(new Dimension(500, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }

    private void buildHeader() {
        headerLabel = new JLabel("Character Builder", SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 22f));
        headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    // --------------------------------------------------------------------------
    // Icons
    // --------------------------------------------------------------------------
    private void loadIcons() {
        iconNormal = new ImageIcon[STEP_COUNT];
        iconHover  = new ImageIcon[STEP_COUNT];
        iconDone   = new ImageIcon[STEP_COUNT];

        for (int i = 0; i < STEP_COUNT; i++) {
            ImageIcon raw1 = loadIcon(STEPS[i], "1");
            ImageIcon raw2 = loadIcon(STEPS[i], "2");
            ImageIcon raw3 = loadIcon(STEPS[i], "3");

            iconNormal[i] = scaleIcon(raw1, ICON_SIZE, ICON_SIZE);
            iconHover[i]  = scaleIcon(raw2, ICON_SIZE, ICON_SIZE);
            iconDone[i]   = scaleIcon(raw3, ICON_SIZE, ICON_SIZE);
        }
    }

    private ImageIcon scaleIcon(ImageIcon src, int width, int height) {
        Image img = src.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
    
    private ImageIcon loadIcon(String name, String variant) {
        return new ImageIcon("images/" + name + variant + ".png");
    }

    // --------------------------------------------------------------------------
    // Center Buttons + Labels (Modern GridBag)
    // --------------------------------------------------------------------------
    private void buildCenter() {

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gc = new GridBagConstraints();

        stepButtons = new JButton[STEP_COUNT];
        stepLabels  = new JLabel[STEP_COUNT];

        gc.insets = new Insets(12, 18, 4, 18);
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;

        int row = 0;

        for (int i = 0; i < STEP_COUNT; i++) {

            gc.gridx = i % 3;
            gc.gridy = row;

            // Button
            JButton btn = new JButton(iconNormal[i]);
            btn.setRolloverIcon(iconHover[i]);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));

            final int index = i;
            btn.addActionListener(e -> onStepSelected(index));

            stepButtons[i] = btn;
            centerPanel.add(btn, gc);

            // Label under button
            gc.gridy = row + 1;
            JLabel lbl = new JLabel(STEPS[i], SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(100, 20));
            lbl.setFont(lbl.getFont().deriveFont(14f));
            stepLabels[i] = lbl;
            centerPanel.add(lbl, gc);

            // Move down after each row of 3
            if (i % 3 == 2) {
                row += 2;
            }
        }

        add(centerPanel, BorderLayout.CENTER);
    }

    // --------------------------------------------------------------------------
    // Footer (Cancel + Finalize)
    // --------------------------------------------------------------------------
    private void buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        cancelButton = new JButton("Cancel / Load");
        cancelButton.addActionListener(e -> onCancelPressed());

        finalizeButton = new JButton("Finalize");
        finalizeButton.setVisible(false);
        finalizeButton.addActionListener(e -> onFinalizePressed());

        footer.add(cancelButton);
        footer.add(finalizeButton);

        add(footer, BorderLayout.SOUTH);
    }

    // --------------------------------------------------------------------------
    // Button Actions
    // --------------------------------------------------------------------------
    private void onCancelPressed() {
        //sheetFrame.onLoadPressed();
        dispose();
    }

    private void onFinalizePressed() {
        if (finalFrame == null) {
            finalFrame = new FrameNewFinal(
                sheetFrame,
                character,
                this
            );
        }
        finalFrame.setVisible(true);
    }

    private void onStepSelected(int index) {
    switch (index) {
        case 0 -> {
            // --- CLASS SELECTION WINDOW ---
            FrameNewClass classFrame =
                    new FrameNewClass(
                        sheetFrame,
                        dataQuery,   // DataStore access
                        character,               // active character
                        this                        // parent FrameNew
                    );

            classFrame.setVisible(true);
        }

        // --------------------------------------------------------------------
        // Other steps still use placeholder dialogs for now
        // --------------------------------------------------------------------
        case 1 -> {
            // --- RACE SELECTION WINDOW ---
            FrameNewRace raceFrame =
                    new FrameNewRace(
                        sheetFrame,
                        dataQuery,   // DataStore access
                        character,               // active character
                        this                        // parent FrameNew
                    );

            raceFrame.setVisible(true);
        }

        case 2 -> {
            FrameNewAttribute attFrame =
                    new FrameNewAttribute(
                        sheetFrame,
                        character,
                        this
                    );
            attFrame.setVisible(true);
        }

        case 3 -> {
            FrameNewSkills skillsFrame =
                    new FrameNewSkills(
                        sheetFrame,
                        dataQuery,
                        character,
                        this
                    );
            skillsFrame.setVisible(true);
        }

        case 4 -> {
            FrameNewSpecials specialsFrame =
                    new FrameNewSpecials(
                        sheetFrame,
                        dataQuery,
                        character,
                        this
                    );
            specialsFrame.setVisible(true);
        }

        case 5 -> {
            FrameNewAura auraFrame =
                    new FrameNewAura(
                        sheetFrame,
                        dataQuery,
                        character,
                        this
                    );
            auraFrame.setVisible(true);
        }
    }
}

    // --------------------------------------------------------------------------
    // Update Step Progress (unchanged from your logic)
    // --------------------------------------------------------------------------
    private void updateFrame() {
        boolean unlocked = stepDone[0];

        for (int i = 1; i < STEP_COUNT; i++) {
            if (unlocked) {
                stepButtons[i - 1].setIcon(iconDone[i - 1]);
                stepButtons[i].setVisible(true);
                stepLabels[i].setVisible(true);
                unlocked = stepDone[i];
            }
            else {
                stepButtons[i - 1].setIcon(iconNormal[i - 1]);
                stepButtons[i].setVisible(false);
                stepLabels[i].setVisible(false);
            }
        }

        // Final button appears only if all steps done
        finalizeButton.setVisible(unlocked);

        if (unlocked) {
            stepButtons[STEP_COUNT - 1].setIcon(iconDone[STEP_COUNT - 1]);
        }
    }

    public void classConfirmed()
    {
        stepDone[0] = true;
        updateFrame();
    }

    public void raceConfirmed()
    {
        stepDone[1] = true;
        updateFrame();
    }

    public void attConfirmed()
    {
        stepDone[2] = true;
        updateFrame();
    }

    public void skillsConfirmed()
    {
        stepDone[3] = true;
        updateFrame();
    }

    public void specialsConfirmed()
    {
        stepDone[4] = true;
        updateFrame();
    }

    public void auraConfirmed()
    {
        stepDone[5] = true;
        updateFrame();
    }

    public void finalConfirmed()
    {
        saveCharacterToDisk();
        if (sheetFrame != null && character != null) {
            sheetFrame.loadCharacter(character);
        }
        dispose();
    }

    private void saveCharacterToDisk() {
        try {
            ArrayList<CharStore> stores = CharacterDataManager.loadCharStore();
            CharIdentity id = character.getIdentity();

            int idx = id.getIndex();
            if (idx <= 0) {
                idx = CharacterDataManager.getNextFreeIndex(stores);
                id.setIndex(idx);
            }

            // Preload base training techniques (id 1..12 and 21..24) into character
            if (character.getTraining() != null && dataQuery != null) {
                for (int trainingId = 1; trainingId <= 12; trainingId++) {
                    DataTraining tech = dataQuery.getTrainingById(trainingId);
                    if (tech != null && character.getTraining().getTrainingById(trainingId) == null) {
                        // clone to avoid shared state
                        String category = (tech.getType() != null && !tech.getType().isBlank()) ? tech.getType() : "General";
                        DataTraining techClone = new DataTraining(tech);
                        techClone.setRank(0);
                        character.getTraining().addTraining(techClone);
                    }
                }
                for (int trainingId = 21; trainingId <= 24; trainingId++) {
                    DataTraining tech = dataQuery.getTrainingById(trainingId);
                    if (tech != null && character.getTraining().getTrainingById(trainingId) == null) {
                        String category = (tech.getAffinity() != null && !tech.getAffinity().isBlank()) ? tech.getAffinity() : "General";
                        DataTraining techClone = new DataTraining(tech);
                        if (trainingId == 23) techClone.setRank(1);
                        else techClone.setRank(0);
                        character.getTraining().addTraining(techClone);
                    }
                }
                for (int trainingId = 31; trainingId <= 49; trainingId++) {
                    DataTraining tech = dataQuery.getTrainingById(trainingId);
                    if (tech != null && character.getTraining().getTrainingById(trainingId) == null) {
                        String category = (tech.getAffinity() != null && !tech.getAffinity().isBlank()) ? tech.getAffinity() : "General";
                        DataTraining techClone = new DataTraining(tech);
                        for (String aff: character.getTraining().getNaturalAffinities()) {
                            if (techClone.getAffinity().compareTo(aff) == 0){
                                techClone.setRank(1);
                                break;
                            }
                            else techClone.setRank(0);
                        }                                               
                        character.getTraining().addTraining(techClone);
                    }
                }
                for (int trainingId = 51; trainingId <= 55; trainingId++) {
                    DataTraining tech = dataQuery.getTrainingById(trainingId);
                    if (tech != null && character.getTraining().getTrainingById(trainingId) == null) {
                        String category = (tech.getAffinity() != null && !tech.getAffinity().isBlank()) ? tech.getAffinity() : "General";
                        DataTraining techClone = new DataTraining(tech);
                        techClone.setRank(0);
                        character.getTraining().addTraining(techClone);
                    }
                }
                for (int trainingId = 61; trainingId <= 65; trainingId++) {
                    DataTraining tech = dataQuery.getTrainingById(trainingId);
                    if (tech != null && character.getTraining().getTrainingById(trainingId) == null) {
                        DataTraining techClone = new DataTraining(tech);
                        techClone.setRank(1);
                        character.getTraining().addTraining(techClone);
                    }
                }
                int[] miscIds = {101, 141, 181};
                for (int trainingId : miscIds) {
                    DataTraining tech = dataQuery.getTrainingById(trainingId);
                    if (tech != null && character.getTraining().getTrainingById(trainingId) == null) {
                        String category = (tech.getAffinity() != null && !tech.getAffinity().isBlank()) ? tech.getAffinity() : "General";
                        DataTraining techClone = new DataTraining(tech);
                        techClone.setRank(0);
                        character.getTraining().addTraining(techClone);
                }
                }
            }

            // Ensure DEF base severity (index 0 status) is set to 10 before saving
            CharAttributes attrs = character.getAttributes();
            if (attrs != null) {
                StatBlock[] defense = attrs.getDefense();
                int defIndex = 2; // DEF is the third entry in DEFENSE array {ARMOR, DODGE, DEF, FORT, REF, WILL}
                if (defense != null && defense.length > defIndex) {
                    var statuses = defense[defIndex].getStatus();
                    if (statuses != null && !statuses.isEmpty()) {
                        statuses.get(0).setSeverity(10);
                    }
                }
            }

            // Starter armor: grant tier 0 chest and legs that match the class armor type
            giveStarterArmor(id.getCharClass());

            CharStore updated = new CharStore(
                idx,
                id.getName(),
                id.getCampaign(),
                id.getRace(),
                id.getCharClass(),
                id.getLevel(),
                new Timestamp(System.currentTimeMillis())
            );

            boolean replaced = false;
            for (int i = 0; i < stores.size(); i++) {
                if (stores.get(i).getIndex() == idx) {
                    stores.set(i, updated);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                stores.add(updated);
            }

            CharacterDataManager.saveCharStore(stores);
            CharacterDataManager.saveCharacter(character);
        } catch (Exception e) {
            System.err.println("Failed to save character: " + e.getMessage());
        }
    }

    /**
     * Grants tier 0 chest and leg armor that match the class armor type (Light/Medium/Heavy).
     * Items are only added if found in data and not already present.
     */
    private void giveStarterArmor(String className) {
        if (dataQuery == null || character == null || className == null) return;
       
        DataClass dc = dataQuery.getClassByName(className);
        CharInventory inv = character.getInventory();
        if (dc == null || inv == null) return;
   
        String armorType = dc.getArmor();
        if (armorType == null || armorType.isBlank()) return;
        armorType = armorType.trim();
    
        addStarterPieces(inv, armorType);
    }

    private void addStarterPieces(CharInventory inv, String armorType) {
        int id = 0;
        if (null != armorType) switch (armorType) {
            case "Light":
                id += 1000;
                break;
            case "Medium":
                id += 2000;
                break;
            case "Heavy":
                id += 3000;
                break;
            case "Exo":
                id += 4000;
                break;
            default:
                break;
        }

        for (int i = 0; i < 2; i++) {
            DataItemEquipment base = dataQuery.getItemByDid(id+i+1);
            if (base == null) return;
            for (DataItem item : inv.getEquipment()) {
                if (item instanceof DataItemEquipment eq) {
                    if (eq.getDid() == base.getDid()) return;
                    String dname = eq.getDname();
                    if (dname != null && dname.equalsIgnoreCase(base.getDname())) return;
                }
            }
            inv.addEquipment(new DataItemEquipment(base));
        }
    }
}
