package attack;

import character.BasePlayer;

public class WindAttack implements Attackable {
    public static final int COOLDOWN = 2;
    @Override public void attack(BasePlayer enemy) {enemy.decreaseHp(1);};
    @Override public String getName()     { return "WIND"; }
    @Override public int    getCooldown() { return COOLDOWN; }
}
