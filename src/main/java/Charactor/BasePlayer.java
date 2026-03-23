package Charactor;

public class BasePlayer {
    private final int maxHp = 10;
    private int Hp = 7;

    public void decreaseHp(int damage) {
        this.Hp -= 1;
    }

    public int getHp() {
        return Hp;
    }
    public int getMaxHp() {
        return maxHp;
    }
}
