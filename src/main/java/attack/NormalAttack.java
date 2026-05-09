package attack;

import character.BasePlayer;

public class NormalAttack implements Attackable {
    @Override public void   attack(BasePlayer enemy) { enemy.decreaseHp(1); }
    @Override public String getName()                { return "NORMAL"; }
    @Override public int    getCooldown()            { return 0; }
}
