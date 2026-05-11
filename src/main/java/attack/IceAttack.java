package attack;

import character.BasePlayer;

/**
 * Ice attack that deals 1 HP of immediate damage and freezes the target for one turn.
 * The freeze (turn-skip) is managed by {@link game.StatusManager}.
 * Cooldown: {@link #COOLDOWN} turns.
 */
public class IceAttack implements Attackable {
    /** Turns before this attack is available again. */
    public static final int COOLDOWN = 2;

    /** @param enemy the target; loses 1 HP immediately (freeze applied via StatusManager) */
    public void attack(BasePlayer enemy) { enemy.decreaseHp(1); }
    /** @return {@code "ICE"} */
    @Override public String getName()     { return "ICE"; }
    /** @return {@link #COOLDOWN} */
    @Override public int    getCooldown() { return COOLDOWN; }
}
