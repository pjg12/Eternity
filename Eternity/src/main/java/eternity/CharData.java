package eternity;

/**
 * Main character container, orchestrating all character subsystems.
 * 
 * Holds:
 *  - Identity data (name, level, race, class, etc.)
 *  - Attribute system (StatBlocks for all stats)
 *  - Resources (HP, Aura, class resources, reactions)
 *  - Skills & Specialties
 *  - Inventory
 *  - Training (Aura techniques, affinities, domains)
 *  - Combat (derived values)
 *
 * CharData is the root of the character sheet object graph.
 */
public class CharData {

    // ---------------------------------------------------------
    // Core subsystems
    // ---------------------------------------------------------

    private final CharIdentity identity;
    private final CharAttributes attributes;
    private final CharResources resources;
    private final CharSpecials specials;
    private final CharInventory inventory;
    private final CharTraining training;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public CharData() {
        this.identity = new CharIdentity();
        this.attributes = new CharAttributes();
        this.resources = new CharResources();
        this.specials = new CharSpecials();
        this.inventory = new CharInventory();
        this.training = new CharTraining();
    }

    // ---------------------------------------------------------
    // Update Pipeline
    // ---------------------------------------------------------

    /**
     * Updates all subsystems in correct order.
     * Call whenever anything changes (level, gear, buffs, etc.)
     */
    public void updateAll() {

    }

    /**
     * Runs initialization logic for a freshly created character.
     */
    public void initializeNewCharacter() {

    }

    // ---------------------------------------------------------
    // Getters for subsystems
    // ---------------------------------------------------------

    public CharIdentity getIdentity()    { return identity; }
    public CharAttributes getAttributes() { return attributes; }
    public CharResources getResources()  { return resources; }
    public CharSpecials getSpecials()    { return specials; }
    public CharInventory getInventory()  { return inventory; }
    public CharTraining getTraining()    { return training; }

    // ---------------------------------------------------------
    // Utility
    // ---------------------------------------------------------

    /** Returns the character's display name. */
    public String getName() {
        return identity.getName();
    }

    /** Convenience: HP % as 0–1 value. */
    public double getHealthPercent() {
    	return 0.0;
    }

}