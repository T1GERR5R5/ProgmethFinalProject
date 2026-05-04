package Charactor;

public class Player1 extends BasePlayer {
    public Player1() {
        super(10, 7, "/images/dog.png");
    }

    @Override public String getName() { return "Dog"; }

    @Override
    public void ability() {
        setHp(Math.min(getHp() + 1, getMaxHp()));
    }
}
