package character;

public class DogPlayer extends BasePlayer {

    public DogPlayer() {
        super(10, 10);
    }

    @Override public String      getSpritePath()   { return "/images/dog.png"; }
    @Override public String      getName()         { return "Dog"; }
    @Override public String      getAbilityLabel() { return "HEAL"; }
    @Override public AbilityType getAbilityType()  { return AbilityType.HEAL; }
    @Override public BasePlayer  recreate()        { return new DogPlayer(); }

    @Override
    public void ability() {
        setHp(Math.min(getHp() + 1, getMaxHp()));
    }
}
