package attack;

import character.BasePlayer;

/**
 * Wind attack that deals 1 HP of immediate damage and causes the target to oscillate
 * horizontally for {@link game.StatusManager#WIND_TURNS} turn(s), making them harder to hit.
 * The oscillation is managed by {@link game.StatusManager}.
 * Cooldown: {@link #COOLDOWN} turns.
 */
public class WindAttack implements Attackable {
    /** Turns before this attack is available again. */
    public static final int COOLDOWN = 2;

    /** @param enemy the target; loses 1 HP immediately (oscillation applied via StatusManager) */
    @Override public void attack(BasePlayer enemy) { enemy.decreaseHp(1); }
    /** @return {@code "WIND"} */
    @Override public String getName()     { return "WIND"; }
    /** @return {@link #COOLDOWN} */
    @Override public int    getCooldown() { return COOLDOWN; }
}
