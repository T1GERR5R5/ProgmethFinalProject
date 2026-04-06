package Charactor;

import AttackLogic.Attackable;

public class BasePlayer {
    private final int maxHp = 10;
    private int Hp = 7;

    public void decreaseHp(int damage) {
        this.Hp -= damage;
    }

    public int getHp() {
        return Hp;
    }
    public int getMaxHp() {
        return maxHp;
    }

    public void setHp(int hp) {
        Hp = hp;
    }
}
