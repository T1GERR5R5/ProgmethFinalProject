package character;

/**
 * Dog character.
 * <p><b>Ability – HEAL:</b> restores 1 HP (capped at {@link #getMaxHp()})
 * and ends the turn immediately via {@link game.Controller#handleAbility()}.
 */
public class DogPlayer extends BasePlayer {

    /** Creates a Dog with 10 max HP and 10 starting HP. */
    public DogPlayer() {
        super(10, 10);
    }

    @Override public String      getSpritePath()   { return "/images/dog.png"; }
    @Override public String      getName()         { return "Dog"; }
    @Override public String      getAbilityLabel() { return "HEAL"; }
    @Override public AbilityType getAbilityType()  { return AbilityType.HEAL; }
    @Override public BasePlayer  recreate()        { return new DogPlayer(); }

    /** Heals 1 HP, capped at {@link #getMaxHp()}. */
    @Override
    public void ability() {
        setHp(Math.min(getHp() + 1, getMaxHp()));
    }
}
