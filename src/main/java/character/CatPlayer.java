package character;

public class CatPlayer extends BasePlayer {

    private boolean perfectAimReady;

    public CatPlayer() {
        super(10, 10);
    }

    @Override public String      getSpritePath()   { return "/images/cat.png"; }
    @Override public String      getName()         { return "Cat"; }
    @Override public String      getAbilityLabel() { return "AIM"; }
    @Override public AbilityType getAbilityType()  { return AbilityType.AIM; }
    @Override public BasePlayer  recreate()        { return new CatPlayer(); }

    @Override
    public void ability() {
        perfectAimReady = true;
    }

    @Override public boolean isPerfectAimReady()  { return perfectAimReady; }
    @Override public void    resetAbilityEffect() { perfectAimReady = false; }
}
