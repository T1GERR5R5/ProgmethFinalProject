package character;

/**
 * Cat character.
 * <p><b>Ability – AIM:</b> sets an internal flag that {@link game.Controller} reads
 * to calculate and lock the exact launch angle needed to hit the opponent's centre,
 * cancelling any active wind effect on the Cat. The turn does not end; the player
 * fires manually with SPACE.
 */
public class CatPlayer extends BasePlayer {

    /** {@code true} after {@link #ability()} is called and before {@link #resetAbilityEffect()}. */
    private boolean perfectAimReady;

    /** Creates a Cat with 10 max HP and 10 starting HP. */
    public CatPlayer() {
        super(10, 10);
    }

    @Override public String      getSpritePath()   { return "/images/cat.png"; }
    @Override public String      getName()         { return "Cat"; }
    @Override public String      getAbilityLabel() { return "AIM"; }
    @Override public AbilityType getAbilityType()  { return AbilityType.AIM; }
    @Override public BasePlayer  recreate()        { return new CatPlayer(); }

    /** Arms the perfect-aim flag so {@link game.Controller} can lock the angle. */
    @Override
    public void ability() {
        perfectAimReady = true;
    }

    /** @return {@code true} while perfect-aim is armed */
    @Override public boolean isPerfectAimReady()  { return perfectAimReady; }
    /** Clears the perfect-aim flag after the angle has been applied. */
    @Override public void    resetAbilityEffect() { perfectAimReady = false; }
}
