package attack;

import character.BasePlayer;

/**
 * Strategy interface for all attack types.
 * Each implementation defines immediate damage and provides metadata (name, cooldown)
 * used by {@link game.Controller} for selection and cooldown management.
 */
public interface Attackable {
    /**
     * Applies this attack's immediate effect to the target.
     * @param enemy the player receiving the attack
     */
    void   attack(BasePlayer enemy);

    /**
     * Returns the unique name identifier for this attack (e.g. {@code "FIRE"}).
     * @return attack name
     */
    String getName();

    /**
     * Returns the number of turns before this attack may be used again.
     * @return cooldown in turns; {@code 0} means no cooldown
     */
    int    getCooldown();
}
