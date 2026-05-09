package attack;

import character.BasePlayer;

public class IceAttack implements Attackable {
    public static final int COOLDOWN = 2;
    public void attack(BasePlayer enemy) {enemy.decreaseHp(1);};
    @Override public String getName()     { return "ICE"; }
    @Override public int    getCooldown() { return COOLDOWN; }
}
