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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

/**
 * Character Creation Wizard
 */
public class FrameNew extends JFrame {
    private final FrameSheet sheetFrame;
    private final CharData character;

    private static final int ICON_SIZE = 100;
    private static final int STEP_COUNT = 6;
    private static final String[] STEPS = { "Class", "Race", "Attributes", "Skills", "Specialties", "Affinity" };

    // UI Constants
    private static final float HEADER_FONT_SIZE = 22f;
    private static final float LABEL_FONT_SIZE = 14f;
    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 520;
    private static final int HEADER_HEIGHT = 60;
    private static final int CENTER_HEIGHT = 380;
    private static final int FOOTER_HEIGHT = 60;
    private static final int BUTTON_SPACING = 10;
    private static final int ICON_SPACING = 20;
    private static final Insets HEADER_INSETS = new Insets(12, 18, 4, 18);
    private static final Insets CENTER_INSETS = new Insets(10, 10, 10, 10);
    private static final Insets FOOTER_INSETS = new Insets(5, 10, 5, 10);

    // UI Strings
    private static final String TITLE = "Character Builder";
    private static final String CANCEL_BUTTON_TEXT = "Cancel / Load";
    private static final String FINALIZE_BUTTON_TEXT = "Finalize";
    private static final String GM_CHECK_TEXT = "GM Mode";
    private static final String HEADER_TEXT = "Character Builder";
    private static final String STEP_PREFIX = "Step ";
    private static final String CONFIRMATION_TITLE = "Confirmation";
    private static final String CONFIRMATION_MESSAGE = "Are you sure you want to finalize this character?";
    private static final String SAVE_ERROR_TITLE = "Save Error";
    private static final String SAVE_ERROR_MESSAGE = "Failed to save character.";
    private static final String SUCCESS_TITLE = "Success";
    private static final String SUCCESS_MESSAGE = "Character created successfully!";
    private static final String RACE_TRAINING_NAME = "Race Training";
    private static final String ARMOR_TYPE_LIGHT = "Light";
    private static final String ARMOR_TYPE_MEDIUM = "Medium";
    private static final String ARMOR_TYPE_HEAVY = "Heavy";
    private static final String ARMOR_TYPE_EXO = "Exo";

    // Training constants from CharDataManager
    private static final int[][] TRAINING_RANGES = {{1,12}, {21,24}, {31,49}, {51,55}, {61,65}};
    private static final int[] TRAINING_SINGLES = {101, 141, 181};

    // DEF stat constants
    private static final int DEF_STAT_INDEX = 2; // DEF is the third entry in DEFENSE array {ARMOR, DODGE, DEF, FORT, REF, WILL}

    // Grid layout constants
    private static final int BUTTONS_PER_ROW = 3;
    private static final int ROW_INCREMENT = 2;
    /**
     * Initializes base training techniques for a new character using predefined ranges and singles.
     * Handles special cases for certain training IDs (e.g., rank initialization).
     */
    private void initializeBaseTraining() {
        if (character == null || character.getTraining() == null) return;

        DataQuery dq = CharDataManager.getDataQuery();
        if (dq == null) return;

        CharTraining training = character.getTraining();

        // Handle range-based training
        for (int rangeIndex = 0; rangeIndex < TRAINING_RANGES.length; rangeIndex++) {
            int[] range = TRAINING_RANGES[rangeIndex];
            for (int trainingId = range[0]; trainingId <= range[1]; trainingId++) {
                addTrainingIfMissing(training, dq, trainingId, rangeIndex);
            }
        }

        // Handle single training IDs
        for (int trainingId : TRAINING_SINGLES) {
            addTrainingIfMissing(training, dq, trainingId, -1);
        }
    }

    /**
     * Adds training technique if not already present, with special handling for certain ranges.
     * @param training The character's training component
     * @param dq The DataQuery instance
     * @param trainingId The training technique ID
     * @param rangeIndex The range index for special handling (-1 for singles)
     */
    private void addTrainingIfMissing(CharTraining training, DataQuery dq, int trainingId, int rangeIndex) {
        if (training.getTrainingById(trainingId) != null) return;

        DataTraining tech = dq.getTrainingById(trainingId);
        if (tech == null) return;

        DataTraining techClone = new DataTraining(tech);

        // Special handling based on range
        if (rangeIndex == 1 && trainingId == 23) { // Range 21-24, ID 23
            techClone.setRank(1);
        } else if (rangeIndex == 2) { // Range 31-49 (affinity-based)
            techClone.setRank(0);
            for (String aff : training.getNaturalAffinities()) {
                if (techClone.getAffinity() != null && techClone.getAffinity().equals(aff)) {
                    techClone.setRank(1);
                    break;
                }
            }
        } else if (rangeIndex == 4) { // Range 61-65
            techClone.setRank(1);
        } else {
            techClone.setRank(0);
        }

        training.addTraining(techClone);
    }

    // Cached icons (loaded once and shared across all instances)
    private static ImageIcon[] iconNormal;
    private static ImageIcon[] iconHover;
    private static ImageIcon[] iconDone;
    private static boolean iconsLoaded = false;

    // UI Elements
    private JLabel headerLabel;
    private JButton[] stepButtons;
    private JLabel[] stepLabels;
    private JButton cancelButton;
    private JButton finalizeButton;
    private FrameNewFinal finalFrame;
    private JCheckBox gmCheck;

    // Step completion state
    private final boolean[] stepDone;
    private final boolean[] stepVisible;

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    public FrameNew(FrameSheet sheetFrame, CharData character) {
        this(sheetFrame, character, -1);
    }

    /**
     * Creates a new character builder. When {@code age} is non-negative, the
     * character's birthday is randomized to a date that makes them that many
     * years old in the current campaign year.
     */
    public FrameNew(FrameSheet sheetFrame, CharData character, int age) {
        super(TITLE);
        this.sheetFrame = sheetFrame;
        this.character = character;
        this.stepDone = new boolean[STEP_COUNT];
        this.stepVisible = new boolean[STEP_COUNT];

        if (age >= 0 && this.character != null && this.character.getIdentity() != null) {
            this.character.getIdentity().randomBirthday(age);
        }

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);      

        // Load icons asynchronously to avoid blocking UI creation
        SwingUtilities.invokeLater(this::loadIconsAsync);
        
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
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT - 40));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(BUTTON_SPACING, BUTTON_SPACING));
    }

    private void buildHeader() {
        headerLabel = new JLabel(HEADER_TEXT, SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, HEADER_FONT_SIZE));
        headerLabel.setBorder(new EmptyBorder(HEADER_INSETS.top, HEADER_INSETS.left, HEADER_INSETS.bottom, HEADER_INSETS.right));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    // --------------------------------------------------------------------------
    // Icons
    // --------------------------------------------------------------------------
    private static synchronized void loadIcons() {
        if (iconsLoaded) return;

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

        iconsLoaded = true;
    }

    private static ImageIcon scaleIcon(ImageIcon src, int width, int height) {
        Image img = src.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
    
    private static ImageIcon loadIcon(String name, String variant) {
        return new ImageIcon("images/" + name + variant + ".png");
    }

    /**
     * Loads icons asynchronously to avoid blocking UI creation.
     */
    private void loadIconsAsync() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadIcons();
                return null;
            }

            @Override
            protected void done() {
                applyLoadedIcons();
                updateFrame();
            }
        };
        worker.execute();
    }

    // --------------------------------------------------------------------------
    // Center Buttons + Labels (Modern GridBag)
    // --------------------------------------------------------------------------
    private void buildCenter() {

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(CENTER_INSETS.top, CENTER_INSETS.left, CENTER_INSETS.bottom, CENTER_INSETS.right));
        GridBagConstraints gc = new GridBagConstraints();

        stepButtons = new JButton[STEP_COUNT];
        stepLabels  = new JLabel[STEP_COUNT];

        gc.insets = CENTER_INSETS;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;

        int row = 0;

        for (int i = 0; i < STEP_COUNT; i++) {

            gc.gridx = i % BUTTONS_PER_ROW;
            gc.gridy = row;

            // Button
            JButton btn = new JButton(getNormalIcon(i));
            btn.setRolloverIcon(getHoverIcon(i));
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
            lbl.setPreferredSize(new Dimension(ICON_SIZE, 20));
            lbl.setFont(lbl.getFont().deriveFont(LABEL_FONT_SIZE));
            stepLabels[i] = lbl;
            centerPanel.add(lbl, gc);

            // Move down after each row of buttons
            if (i % BUTTONS_PER_ROW == BUTTONS_PER_ROW - 1) {
                row += ROW_INCREMENT;
            }
        }

        add(centerPanel, BorderLayout.CENTER);
    }

    // --------------------------------------------------------------------------
    // Footer (Cancel + Finalize)
    // --------------------------------------------------------------------------
    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());

        gmCheck = new JCheckBox(GM_CHECK_TEXT);
        gmCheck.setFocusable(false);
        gmCheck.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, BUTTON_SPACING, BUTTON_SPACING));
        leftPanel.setOpaque(false);
        leftPanel.add(gmCheck);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, ICON_SPACING, BUTTON_SPACING));

        cancelButton = new JButton(CANCEL_BUTTON_TEXT);
        cancelButton.addActionListener(e -> onCancelPressed());

        finalizeButton = new JButton(FINALIZE_BUTTON_TEXT);
        finalizeButton.setVisible(false);
        finalizeButton.addActionListener(e -> onFinalizePressed());

        buttonPanel.add(cancelButton);
        buttonPanel.add(finalizeButton);

        footer.add(leftPanel, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.CENTER);

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
                        CharDataManager.getDataQuery(),   // StoreData access
                        character,               // active character
                        this,                       // parent FrameNew
                        gmCheck != null && gmCheck.isSelected()
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
                        CharDataManager.getDataQuery(),   // StoreData access
                        character,               // active character
                        this,                       // parent FrameNew
                        gmCheck != null && gmCheck.isSelected()
                    );

            raceFrame.setVisible(true);
        }

        case 2 -> {
            FrameNewAttribute attFrame =
                    new FrameNewAttribute(
                        sheetFrame,
                        character,
                        this,
                        gmCheck != null && gmCheck.isSelected()
                    );
            attFrame.setVisible(true);
        }

        case 3 -> {
            FrameNewSkills skillsFrame =
                    new FrameNewSkills(
                        sheetFrame,
                        CharDataManager.getDataQuery(),
                        character,
                        this,
                        gmCheck != null && gmCheck.isSelected()
                    );
            skillsFrame.setVisible(true);
        }

        case 4 -> {
            FrameNewSpecials specialsFrame =
                    new FrameNewSpecials(
                        sheetFrame,
                        CharDataManager.getDataQuery(),
                        character,
                        this,
                        gmCheck != null && gmCheck.isSelected()
                    );
            specialsFrame.setVisible(true);
        }

        case 5 -> {
            FrameNewAura auraFrame =
                    new FrameNewAura(
                        sheetFrame,
                        CharDataManager.getDataQuery(),
                        character,
                        this,
                        gmCheck != null && gmCheck.isSelected()
                    );
            auraFrame.setVisible(true);
        }
    }
}

    // --------------------------------------------------------------------------
    // Update Step Progress (unchanged from your logic)
    // --------------------------------------------------------------------------
    private void updateFrame() {
        if (stepButtons == null || stepLabels == null) return;

        updateStepVisibility(0, true);
        boolean unlocked = stepDone[0];

        for (int i = 1; i < STEP_COUNT; i++) {
            if (unlocked) {
                setStepIcon(i - 1, true);
                updateStepVisibility(i, true);
                unlocked = stepDone[i];
            }
            else {
                setStepIcon(i - 1, false);
                updateStepVisibility(i, false);
            }
        }

        // Final button appears only if all steps done
        if (finalizeButton.isVisible() != unlocked) {
            finalizeButton.setVisible(unlocked);
        }

        setStepIcon(STEP_COUNT - 1, unlocked);
    }

    public void classConfirmed()
    {
        setStepConfirmed(0);
    }

    public void raceConfirmed()
    {
        setStepConfirmed(1);
    }

    public void attConfirmed()
    {
        setStepConfirmed(2);
    }

    public void skillsConfirmed()
    {
        setStepConfirmed(3);
    }

    public void specialsConfirmed()
    {
        setStepConfirmed(4);
    }

    public void auraConfirmed()
    {
        setStepConfirmed(5);
    }

    /**
     * Marks the specified step as confirmed and updates the UI.
     * @param stepIndex The index of the step to confirm (0-5)
     */
    private void setStepConfirmed(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < STEP_COUNT && !stepDone[stepIndex]) {
            stepDone[stepIndex] = true;
            updateFrame();
        }
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
            ArrayList<StoreChar> stores = CharDataManager.loadCharStore();
            CharIdentity id = character.getIdentity();
            boolean isNewCharacter = false;

            int idx = id.getIndex();
            if (idx <= 0) {
                character.initializeNewCharacter();
                idx = CharDataManager.getNextFreeIndex(stores);
                id.setIndex(idx);
                isNewCharacter = true;
            }

            // Preload base training techniques using consolidated method
            if (character.getTraining() != null) {
                initializeBaseTraining();
            }

            // New-character safeguard: Race Training should always begin at rank 1.
            if (isNewCharacter && character.getTraining() != null) {
                DataTraining raceTraining = character.getTraining().getTrainingByName(RACE_TRAINING_NAME);
                if (raceTraining != null) {
                    raceTraining.setRank(1);
                }
            }

            if (isNewCharacter) {
                grantInitialSpecialties();
            }

            // Ensure DEF base severity (index 0 status) is set to 10 before saving
            setDefBaseSeverity(character);

            // Starter armor: grant tier 0 chest and legs that match the class armor type
            giveStarterArmor(id.getCharClass());

            StoreChar updated = new StoreChar(
                idx,
                id.getName(),
                id.getCampaign(),
                id.getRace(),
                id.getCharClass(),
                id.getLevel(),
                new Timestamp(System.currentTimeMillis())
            );

            // Optimized StoreChar replacement using helper method
            replaceOrAddCharStore(stores, updated);

            // Persist character file first so StoreChar normalization can resolve snapshot metadata.
            boolean saved = CharDataManager.saveCharacter(character);
            if (saved) {
                CharDataManager.saveCharStore(stores);
            } else {
                System.err.println("Failed to persist character file; skipping StoreChar update.");
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to save character: " + e.getMessage());
        }
    }

    /**
     * Sets the DEF base severity to 10 for new characters.
     */
    private static void setDefBaseSeverity(CharData character) {
        CharAttributes attrs = character.getAttributes();
        if (attrs != null) {
            StatBlock[] defense = attrs.getDefense();
            if (defense != null && defense.length > DEF_STAT_INDEX) {
                var statuses = defense[DEF_STAT_INDEX].getStatus();
                if (statuses != null && !statuses.isEmpty()) {
                    statuses.get(0).setSeverity(10);
                }
            }
        }
    }

    /**
     * Replaces existing StoreChar with matching index or adds new one if not found.
     * More efficient than linear search for large store lists.
     */
    private static void replaceOrAddCharStore(ArrayList<StoreChar> stores, StoreChar updated) {
        int idx = updated.getIndex();
        for (int i = 0; i < stores.size(); i++) {
            if (stores.get(i).getIndex() == idx) {
                stores.set(i, updated);
                return;
            }
        }
        stores.add(updated);
    }

    private void grantInitialSpecialties() {
        if (character == null) return;
        CharSpecials specials = character.getSpecials();
        if (specials == null) return;

        DataQuery dq = CharDataManager.getDataQuery();
        if (dq == null) return;

        grantSpecialtyIfMissing(specials, dq.getSpecialtyByName("Level 1"));

        CharInventory inv = character.getInventory();
        if (inv == null) return;
        for (String prof : inv.getWeaponProficiencies()) {
            if (prof == null || prof.isBlank()) continue;

            DataSpecialty spec = null;
            for (DataSpecialty candidate : dq.getSpecialtiesByType("Proficiency")) {
                if (candidate == null) continue;
                String refName = candidate.getRefName();
                String name = candidate.getName();
                if ((refName != null && refName.equalsIgnoreCase(prof))
                        || (name != null && name.equalsIgnoreCase("Proficiency (" + prof + ")"))) {
                    spec = candidate;
                    break;
                }
            }
            grantSpecialtyIfMissing(specials, spec);
        }
    }

    private void grantSpecialtyIfMissing(CharSpecials specials, DataSpecialty spec) {
        if (specials == null || spec == null || spec.getName() == null || spec.getName().isBlank()) return;
        if (specials.hasSpecialty(spec.getName())) return;
        specials.addTrainedSpecialty(new DataSpecialty(spec));
    }

    /**
     * Grants tier 0 chest and leg armor that match the class armor type (Light/Medium/Heavy).
     * Items are only added if found in data and not already present.
     */
    private void giveStarterArmor(String className) {
        if (character == null || className == null) return;
       
        DataQuery dq = CharDataManager.getDataQuery();
        if (dq == null) return;

        DataClass dc = dq.getClassByName(className);
        CharInventory inv = character.getInventory();
        if (dc == null || inv == null) return;
   
        String armorType = dc.getArmor();
        if (armorType == null || armorType.isBlank()) return;
        armorType = armorType.trim();
    
        addStarterPieces(inv, dq, armorType);
    }

    private void addStarterPieces(CharInventory inv, DataQuery dq, String armorType) {
        int id = 0;
        if (null != armorType) switch (armorType) {
            case ARMOR_TYPE_LIGHT:
                id += 1000;
                break;
            case ARMOR_TYPE_MEDIUM:
                id += 2000;
                break;
            case ARMOR_TYPE_HEAVY:
                id += 3000;
                break;
            case ARMOR_TYPE_EXO:
                id += 4000;
                break;
            default:
                break;
        }

        // Use Set for O(1) duplicate checking instead of O(n²) nested loops
        java.util.Set<Integer> existingIds = new java.util.HashSet<>();
        java.util.Set<String> existingNames = new java.util.HashSet<>();
        for (DataItem item : inv.getEquipment()) {
            if (item instanceof DataItemEquipment eq) {
                existingIds.add(eq.getDid());
                String dname = eq.getDname();
                if (dname != null) {
                    existingNames.add(dname.toLowerCase());
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            DataItemEquipment base = dq.getItemByDid(id+i+1);
            if (base == null) return;
            
            // O(1) duplicate check using Sets
            if (existingIds.contains(base.getDid())) return;
            String baseName = base.getDname();
            if (baseName != null && existingNames.contains(baseName.toLowerCase())) return;
            
            inv.addEquipment(new DataItemEquipment(base));
        }
    }

    private void applyLoadedIcons() {
        if (stepButtons == null || !iconsLoaded) return;
        for (int i = 0; i < STEP_COUNT; i++) {
            stepButtons[i].setRolloverIcon(getHoverIcon(i));
        }
    }

    private void updateStepVisibility(int stepIndex, boolean visible) {
        if (stepVisible[stepIndex] == visible) return;
        stepVisible[stepIndex] = visible;
        stepButtons[stepIndex].setVisible(visible);
        stepLabels[stepIndex].setVisible(visible);
    }

    private void setStepIcon(int stepIndex, boolean done) {
        ImageIcon icon = done ? getDoneIcon(stepIndex) : getNormalIcon(stepIndex);
        if (icon != null && stepButtons[stepIndex].getIcon() != icon) {
            stepButtons[stepIndex].setIcon(icon);
        }
    }

    private static ImageIcon getNormalIcon(int stepIndex) {
        return iconsLoaded ? iconNormal[stepIndex] : null;
    }

    private static ImageIcon getHoverIcon(int stepIndex) {
        return iconsLoaded ? iconHover[stepIndex] : null;
    }

    private static ImageIcon getDoneIcon(int stepIndex) {
        return iconsLoaded ? iconDone[stepIndex] : null;
    }
}
