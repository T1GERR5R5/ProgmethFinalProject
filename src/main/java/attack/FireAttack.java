package attack;

import character.BasePlayer;

/**
 * Fire attack that deals 2 HP of immediate damage on hit.
 * {@link game.StatusManager} additionally activates a burn-over-time effect
 * ({@link game.StatusManager#BURN_TICKS} ticks × 1 HP each).
 * Cooldown: {@link #COOLDOWN} turns.
 */
public class FireAttack implements Attackable {
    /** Turns before this attack is available again. */
    public static final int COOLDOWN = 3;

    /** @param enemy the target; loses 2 HP immediately (burn ticks follow via StatusManager) */
    @Override public void attack(BasePlayer enemy) { enemy.decreaseHp(2); }
    /** @return {@code "FIRE"} */
    @Override public String getName()     { return "FIRE"; }
    /** @return {@link #COOLDOWN} */
    @Override public int    getCooldown() { return COOLDOWN; }
}
