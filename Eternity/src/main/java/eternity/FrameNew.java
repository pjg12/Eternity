package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * Character Creation Wizard
 */
public class FrameNew extends JFrame {

    // References
    private final FrameSheet sheetFrame;                
    private final StoreCharData character;

    // UI Constants
    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 520;
    private static final Font HEADER_FONT = new Font(null, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final int BUTTON_SPACING = 10;
    private static final int ICON_SPACING = 20;
    private static final EmptyBorder HEADER_BORDER = new EmptyBorder(12, 18, 4, 18);
    private static final EmptyBorder CENTER_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final Insets CENTER_INSETS = new Insets(10, 10, 10, 10);
    private static final EmptyBorder FOOTER_BORDER = new EmptyBorder(5, 10, 5, 10);
    private static final int ICON_SIZE = 100;
    private static final int STEP_COUNT = 6;
    private static final int BUTTONS_PER_ROW = 3;
    private static final int ROW_INCREMENT = 2;

    // UI Strings
    private static final String WINDOW_TITLE = "Character Builder";
    private static final String HEADER_TEXT = "Character Builder";
    private static final String BUTTON_RESET = "Reset";
    private static final String BUTTON_CANCEL = "Cancel / Load";
    private static final String BUTTON_FINALIZE = "Finalize";
    private static final String GM_CHECK_TEXT = "GM Mode";
    private static final String[] STEPS = { "Class", "Race", "Attributes", "Skills", "Specialties", "Affinity" };
    private static final String ARMOR_TYPE_LIGHT = "Light";
    private static final String ARMOR_TYPE_MEDIUM = "Medium";
    private static final String ARMOR_TYPE_HEAVY = "Heavy";
    private static final String ARMOR_TYPE_EXO = "Exo";
    private static final String[] STARTING_TECHS = {
            "Strength Training", "Dexterity Training", "Constitution Training", "Focus Training",
            "Control Training", "Capacity Training", "Knowledge Training", "Mechanical Training",
            "Perception Training", "Intuition Training", "Charisma Training", "Subtlety Training",
            "Skill Training", "Specialty Training", "Benefaction", "Emission", "Manifestation",
            "Potency", "Transmutation"
    };
    private static final String[] STARTING_TECHS_1 = {
            "Race Training", "Boost", "Harden", "Prism", "Shape", "Zone"
    };

    // Icons
    private static ImageIcon[] iconNormal;
    private static ImageIcon[] iconHover;
    private static ImageIcon[] iconDone;
    private static boolean iconsLoaded = false;

    // UI Elements
    private JPanel headerPanel, centerPanel, footerPanel;
    private JPanel gmPanel, buttonPanel;
    private JLabel headerL;
    private JButton[] stepButtons;
    private JLabel[] stepLabels;
    private JCheckBox gmCheck;
    private JButton resetButton, cancelButton, finalizeButton;

    // Frames
    private FrameNewClass classFrame;
    private FrameNewRace raceFrame;
    private FrameNewAttribute attributeFrame;
    private FrameNewSkills skillsFrame;
    private FrameNewSpecials specialsFrame;
    private FrameNewAura auraFrame;
    private FrameNewFinal finalFrame;
    private final List<String> starterWeaponSelections;

    // Step completion state
    private final boolean[] stepDone;

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------

    public FrameNew(FrameSheet sheetFrame, StoreCharData character) {
        super(WINDOW_TITLE);
        this.sheetFrame = sheetFrame;
        this.character = character;

        this.stepDone = new boolean[STEP_COUNT];
        this.starterWeaponSelections = new ArrayList<>();
        //ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);      
        SwingUtilities.invokeLater(this::loadIconsAsync);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(sheetFrame);
        setResizable(false);
        setLayout(new BorderLayout(BUTTON_SPACING, BUTTON_SPACING));

        buildUI();
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
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(CENTER_BORDER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = CENTER_INSETS;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;

        // Build button arrays
        stepButtons = new JButton[STEP_COUNT];
        stepLabels  = new JLabel[STEP_COUNT];

        int row = 0;
        for (int i = 0; i < STEP_COUNT; i++) {
            // Manage format
            gc.gridx = i % BUTTONS_PER_ROW;
            gc.gridy = row;

            // Build button
            JButton btn = new JButton(getNormalIcon(i));
            btn.setRolloverIcon(getHoverIcon(i));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
            btn.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));

            // Add Listener
            final int index = i;
            btn.addActionListener(e -> onStepPressed(index));

            // Add button
            stepButtons[i] = btn;
            centerPanel.add(btn, gc);

            // Build label
            gc.gridy = row + 1;
            JLabel lbl = new JLabel(STEPS[i], SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(ICON_SIZE, 20));
            lbl.setFont(LABEL_FONT);

            // Add label
            stepLabels[i] = lbl;
            centerPanel.add(lbl, gc);

            // Advance
            if (i % BUTTONS_PER_ROW == BUTTONS_PER_ROW - 1) {
                row += ROW_INCREMENT;
            }
        }

        // Add panel
        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildFooter() {
        // Build panels
        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(FOOTER_BORDER);
        gmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, BUTTON_SPACING, BUTTON_SPACING));
        gmPanel.setOpaque(false);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, ICON_SPACING, BUTTON_SPACING));

        // Build GM Mode button
        gmCheck = new JCheckBox(GM_CHECK_TEXT);
        gmCheck.setFocusable(false);
        gmCheck.setOpaque(false);
        gmCheck.addItemListener(e -> onGmCheckChanged());

        // Build buttons
        resetButton = new JButton(BUTTON_RESET);
        resetButton.addActionListener(e -> onResetPressed());
        cancelButton = new JButton(BUTTON_CANCEL);
        cancelButton.addActionListener(e -> onCancelPressed());
        finalizeButton = new JButton(BUTTON_FINALIZE);
        finalizeButton.addActionListener(e -> onFinalizePressed());
        finalizeButton.setVisible(false);

        // Add buttons
        gmPanel.add(gmCheck);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(finalizeButton);

        // Add panels
        footerPanel.add(gmPanel, BorderLayout.WEST);
        footerPanel.add(buttonPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // --------------------------------------------------------------------------
    // Icons
    // --------------------------------------------------------------------------
    private static synchronized void loadIcons() {
        if (iconsLoaded) return;

        // Build icon arrays
        iconNormal = new ImageIcon[STEP_COUNT];
        iconHover  = new ImageIcon[STEP_COUNT];
        iconDone   = new ImageIcon[STEP_COUNT];

        for (int i = 0; i < STEP_COUNT; i++) {
            // Load icons from images directory
            ImageIcon raw1 = loadIcon(STEPS[i], "1");
            ImageIcon raw2 = loadIcon(STEPS[i], "2");
            ImageIcon raw3 = loadIcon(STEPS[i], "3");

            // Add icons to icon arrays
            iconNormal[i] = scaleIcon(raw1, ICON_SIZE, ICON_SIZE);
            iconHover[i]  = scaleIcon(raw2, ICON_SIZE, ICON_SIZE);
            iconDone[i]   = scaleIcon(raw3, ICON_SIZE, ICON_SIZE);
        }
        iconsLoaded = true;
    }

    private static ImageIcon scaleIcon(ImageIcon src, int width, int height) { return new ImageIcon(src.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)); }
    private static ImageIcon loadIcon(String name, String variant) { return new ImageIcon("images/" + name + variant + ".png"); }

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
                applyRolloverIcons();
                updateFrame();
            }
        };
        worker.execute();
    }

    private static ImageIcon getNormalIcon(int stepIndex) { return iconsLoaded ? iconNormal[stepIndex] : null; }
    private static ImageIcon getHoverIcon(int stepIndex) { return iconsLoaded ? iconHover[stepIndex] : null; }
    private static ImageIcon getDoneIcon(int stepIndex) { return iconsLoaded ? iconDone[stepIndex] : null; }

    private void applyRolloverIcons() {
        if (stepButtons == null || !iconsLoaded) return;

        for (int i = 0; i < STEP_COUNT; i++) {
            stepButtons[i].setRolloverIcon(getHoverIcon(i));
        }
    }

    private void setStepIcon(int stepIndex, boolean done) {
        ImageIcon icon = done ? getDoneIcon(stepIndex) : getNormalIcon(stepIndex);
        if (icon != null && stepButtons[stepIndex].getIcon() != icon) {
            stepButtons[stepIndex].setIcon(icon);
        }
    }

    // ---------------------------------------------------------
    // Button Handlers
    // ---------------------------------------------------------
    private void onGmCheckChanged() {
        onResetPressed();
    }

    private void onResetPressed() {
        disposeSubFrames();
        clearSteps();
        starterWeaponSelections.clear();
        updateFrame();
    }

    private void onCancelPressed() {
        sheetFrame.onLoadPressed();
        dispose();
    }

    private void onFinalizePressed() {
        List<DataItemEquipment> grantedArmor = giveStarterArmor();
        giveStarterTechs();
        equipStarterLoadout(grantedArmor);
        if (finalFrame == null) finalFrame = new FrameNewFinal(sheetFrame, character, this);
        finalFrame.setVisible(true);
    }

    private void onStepPressed(int index) {
        switch (index) {
            case 0 -> {
                if (classFrame == null) classFrame = new FrameNewClass(sheetFrame, sheetFrame.getStoreRuleManager(), character, this, gmCheck != null && gmCheck.isSelected());
                classFrame.setVisible(true);
            }
            case 1 -> {
                if (raceFrame == null) raceFrame = new FrameNewRace(sheetFrame, sheetFrame.getStoreRuleManager(), character, this, gmCheck != null && gmCheck.isSelected());
                raceFrame.setVisible(true);
            }
            case 2 -> {
                if (attributeFrame == null) attributeFrame = new FrameNewAttribute(sheetFrame, character, this, gmCheck != null && gmCheck.isSelected());
                attributeFrame.setVisible(true);
            }
            case 3 -> {
                if (skillsFrame == null) skillsFrame = new FrameNewSkills(sheetFrame, sheetFrame.getStoreRuleManager(), character, this, gmCheck != null && gmCheck.isSelected());
                skillsFrame.setVisible(true);
            }
            case 4 -> {
                if (specialsFrame == null) specialsFrame = new FrameNewSpecials(sheetFrame, sheetFrame.getStoreRuleManager(), character, this, gmCheck != null && gmCheck.isSelected());
                specialsFrame.setVisible(true);
            }
            case 5 -> {
                if (auraFrame == null) auraFrame = new FrameNewAura(sheetFrame, sheetFrame.getStoreRuleManager(), character, this, gmCheck != null && gmCheck.isSelected());
                auraFrame.setVisible(true);
            }
        }
    }

    // --------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------

    private void disposeSubFrames() {
        if (classFrame != null) {
            classFrame.dispose();
            classFrame = null;
        }
        if (raceFrame != null && raceFrame.isDisplayable()) {
            raceFrame.dispose();
            raceFrame = null;
        }
        if (attributeFrame != null && attributeFrame.isDisplayable()) {
            attributeFrame.dispose();
            attributeFrame = null;
        }
        if (skillsFrame != null && skillsFrame.isDisplayable()) {
            skillsFrame.dispose();
            skillsFrame = null;
        }
        if (specialsFrame != null && specialsFrame.isDisplayable()) {
            specialsFrame.dispose();
            specialsFrame = null;
        }
        if (auraFrame != null && auraFrame.isDisplayable()) {
            auraFrame.dispose();
            auraFrame = null;
        }
        if (finalFrame != null && finalFrame.isDisplayable()) {
            finalFrame.dispose();
            finalFrame = null;
        }
    }

     private void saveCharacter() { StoreCharManager.saveCharacterNew(character); }

     public void finalConfirmed() {
        saveCharacter();
        if (sheetFrame != null && character != null) sheetFrame.loadCharacter(character); 
        dispose();
    }

    // --------------------------------------------------------------------------
    // Step Updaters
    // --------------------------------------------------------------------------

    private void updateFrame() {
        if (stepButtons == null || stepLabels == null) return;

        // First step always visible
        updateStepVisibility(0, true);

        // If a user clears progress for a selection, do not display options for other selections even if user has completed them.
        // dontShow will help track the earliest incomplete selection
        int dontShow = 0;
        // Update completed steps through stepDone[]
        for (int i = 0; i < STEP_COUNT; i++) {
            if ((i <= dontShow) && stepDone[i]) {
                dontShow = i+1;
                setStepIcon(i, true);
                updateStepVisibility(i+1, true);
            }
            else {
                setStepIcon(i, false);
                updateStepVisibility(i+1, false);
            }
        }
    }

    private void updateStepVisibility(int stepIndex, boolean visible) {
        if (stepIndex == STEP_COUNT) { 
            finalizeButton.setVisible(visible);
            return;
        }

        stepButtons[stepIndex].setVisible(visible);
        stepLabels[stepIndex].setVisible(visible);
    }

    private void clearSteps() { for (int i = 0; i < STEP_COUNT; i++) stepDone[i] = false; }

    /**
     * Marks the specified step as confirmed and updates the UI.
     */
    public void setStepConfirmed(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < STEP_COUNT && !stepDone[stepIndex]) {
            stepDone[stepIndex] = true;
            updateFrame();
        }
    }

    public void setStarterWeaponSelections(List<String> selections) {
        starterWeaponSelections.clear();
        if (selections == null) return;
        for (String selection : selections) {
            if (selection == null || selection.isBlank()) continue;
            starterWeaponSelections.add(selection);
        }
    }



   

//////////////////////////////////////////////////////////////////////////////














    







    /**
     * Grants the configured starter techniques to the new character.
     * Techniques are copied from rule data and initialized at their configured starting ranks.
     */
    private void giveStarterTechs() {
        if (character == null || character.getTraining() == null || sheetFrame == null) return;

        StoreRuleManager ruleManager = sheetFrame.getStoreRuleManager();
        if (ruleManager == null) return;

        CharTraining training = character.getTraining();
        addStarterTechs(training, ruleManager, STARTING_TECHS, 0);
        addStarterTechs(training, ruleManager, STARTING_TECHS_1, 1);
        addNaturalAffinityStarterTechs(training, ruleManager);
    }

    private void addStarterTechs(CharTraining training, StoreRuleManager ruleManager, String[] techNames, int startingRank) {
        if (training == null || ruleManager == null || techNames == null) return;
        for (String techName : techNames) {
            if (techName == null || techName.isBlank()) continue;

            DataTraining template = findTrainingTemplateByName(ruleManager, techName);
            if (template == null) continue;
            if (training.getTrainingById(template.getId()) != null) continue;

            DataTraining tech = new DataTraining(template);
            tech.setRank(Math.max(0, startingRank));
            tech.setExp(0.0);
            tech.setAl(0);
            training.addTraining(tech);
        }
    }

    private DataTraining findTrainingTemplateByName(StoreRuleManager ruleManager, String techName) {
        if (ruleManager == null || techName == null || techName.isBlank()) return null;
        for (DataTraining tech : ruleManager.getTrainingData()) {
            if (tech == null || tech.getName() == null) continue;
            if (tech.getName().equalsIgnoreCase(techName)) return tech;
        }
        return null;
    }

    private void addNaturalAffinityStarterTechs(CharTraining training, StoreRuleManager ruleManager) {
        if (training == null || ruleManager == null) return;
        for (String affinity : training.getNaturalAffinities()) {
            if (affinity == null || affinity.isBlank()) continue;
            for (DataTraining template : ruleManager.getTrainingData()) {
                if (!isMatchingAuraAffinityTechnique(template, affinity)) continue;
                if (training.getTrainingById(template.getId()) != null) continue;

                DataTraining tech = new DataTraining(template);
                tech.setRank(2);
                tech.setExp(0.0);
                tech.setAl(0);
                training.addTraining(tech);
            }
        }
    }

    private boolean isMatchingAuraAffinityTechnique(DataTraining tech, String affinity) {
        if (tech == null || affinity == null) return false;
        String name = tech.getName();
        String techAffinity = tech.getAffinity();
        if (name == null || techAffinity == null) return false;
        return name.trim().toLowerCase().startsWith("aura affinity")
                && techAffinity.equalsIgnoreCase(affinity);
    }

    /**
     * Grants tier 0 chest and leg armor that match the class armor type (Light/Medium/Heavy).
     * Items are only added if found in data and not already present.
     */
    private List<DataItemEquipment> giveStarterArmor() {
        ArrayList<DataItemEquipment> granted = new ArrayList<>();
        if (character == null || sheetFrame == null || character.getIdentity() == null) return granted;
        DataClass charClass = sheetFrame.getStoreRuleManager().getClassByName(character.getIdentity().getCharClass());
        if (charClass == null) return granted;
        
        CharInventory inv = character.getInventory();
        if (inv == null) return granted;
   
        String armorType = charClass.getArmor();
        if (armorType == null || armorType.isBlank()) return granted;
        armorType = armorType.trim();
    
        addStarterPieces(inv, armorType, granted);
        return granted;
    }

    private void addStarterPieces(CharInventory inv, String armorType, List<DataItemEquipment> granted) {
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
            DataItemEquipment base = sheetFrame.getStoreRuleManager().getItemByDid(id+i+1);
            if (base == null) return;
            
            // O(1) duplicate check using Sets
            if (existingIds.contains(base.getDid())) continue;
            String baseName = base.getDname();
            if (baseName != null && existingNames.contains(baseName.toLowerCase())) continue;
            
            DataItemEquipment added = new DataItemEquipment(base);
            inv.addEquipment(added);
            if (granted != null) {
                granted.add(added);
            }
            existingIds.add(base.getDid());
            if (baseName != null) {
                existingNames.add(baseName.toLowerCase());
            }
        }
    }

    private void equipStarterLoadout(List<DataItemEquipment> grantedArmor) {
        if (character == null) return;
        CharInventory inventory = character.getInventory();
        if (inventory == null) return;

        Set<String> starterWeapons = new LinkedHashSet<>();
        for (String selection : starterWeaponSelections) {
            if (selection != null && !selection.isBlank()) {
                starterWeapons.add(selection.trim().toLowerCase());
            }
        }

        for (DataItemWeapon weapon : inventory.getWeapons()) {
            if (weapon != null && isStarterWeapon(weapon, starterWeapons)) {
                weapon.setEquipped(true);
            }
        }
        for (DataItemEquipment item : inventory.getEquipment()) {
            if (item != null && isGrantedArmor(item, grantedArmor)) {
                item.setEquipped(true);
            }
        }
    }

    private boolean isStarterWeapon(DataItemWeapon item, Set<String> starterWeapons) {
        if (item == null || starterWeapons == null || starterWeapons.isEmpty()) return false;
        String displayName = item.getDname();
        String inventoryName = item.getIname();
        return matchesSelection(displayName, starterWeapons) || matchesSelection(inventoryName, starterWeapons);
    }

    private boolean matchesSelection(String itemName, Set<String> starterWeapons) {
        if (itemName == null || itemName.isBlank()) return false;
        return starterWeapons.contains(itemName.trim().toLowerCase());
    }

    private boolean isGrantedArmor(DataItemEquipment item, List<DataItemEquipment> grantedArmor) {
        if (item == null || grantedArmor == null || grantedArmor.isEmpty()) return false;
        for (DataItemEquipment granted : grantedArmor) {
            if (granted == item) return true;
        }
        return false;
    }
}

