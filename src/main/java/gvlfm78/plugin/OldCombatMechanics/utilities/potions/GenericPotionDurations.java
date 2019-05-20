package gvlfm78.plugin.OldCombatMechanics.utilities.potions;

/**
 * Used to hold information on duration of base, II & extended versions of potion.
 * <p>
 * All durations are in seconds.
 */
public class GenericPotionDurations {

    private final int base, II, extended;

    public GenericPotionDurations(int base, int II, int extended) {
        this.base = base;
        this.II = II;
        this.extended = extended;
    }

    /**
     * Returns the base potion duration in seconds.
     *
     * @return the base potion duration in seconds.
     */
    public int getBaseTime() {
        return base;
    }

    /**
     * Returns the duration in seconds for the amplified potion.
     *
     * @return the duration in seconds for the amplified potion.
     */
    public int getIITime() {
        return II;
    }

    /**
     * Returns the duration in seconds for the extended potion.
     *
     * @return the duration in seconds for the extended potion
     */
    public int getExtendedTime() {
        return extended;
    }
}
