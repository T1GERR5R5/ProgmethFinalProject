package attack;

import character.BasePlayer;

public class FireAttack implements  Attackable{
    public static final int COOLDOWN = 3;
    @Override public void attack(BasePlayer enemy) {enemy.decreaseHp(2);};
    @Override public String getName()     { return "FIRE"; }
    @Override public int    getCooldown() { return COOLDOWN; }
}
