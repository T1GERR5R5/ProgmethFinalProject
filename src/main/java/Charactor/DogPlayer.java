package Charactor;

public class DogPlayer extends BasePlayer {

    public DogPlayer() {
        super(10, 10);
    }

    @Override public String getSpritePath()   { return "/images/dog.png"; }
    @Override public String getName()         { return "Dog"; }
    @Override public String getAbilityLabel() { return "HEAL"; }

    @Override
    public void ability() {
        setHp(Math.min(getHp() + 1, getMaxHp()));
    }
}
