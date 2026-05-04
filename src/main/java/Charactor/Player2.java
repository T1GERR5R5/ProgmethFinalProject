package Charactor;

public class Player2 extends BasePlayer {
    private boolean perfectAimReady = false;

    public Player2() {
        super(10, 7, "/images/cat.png");
    }

    @Override public String getName() { return "Cat"; }

    @Override
    public void ability() { perfectAimReady = true; }

    @Override public boolean isPerfectAimReady()  { return perfectAimReady; }
    @Override public void    resetAbilityEffect() { perfectAimReady = false; }
}
