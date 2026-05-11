package attack;

import character.BasePlayer;

/**
 * Standard attack that deals exactly 1 HP of immediate damage. No cooldown.
 */
public class NormalAttack implements Attackable {
    /** @param enemy the target; loses 1 HP immediately */
    @Override public void   attack(BasePlayer enemy) { enemy.decreaseHp(1); }
    /** @return {@code "NORMAL"} */
    @Override public String getName()                { return "NORMAL"; }
    /** @return {@code 0} — no cooldown */
    @Override public int    getCooldown()            { return 0; }
}
