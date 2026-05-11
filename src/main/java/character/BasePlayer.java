package character;

import javafx.scene.image.Image;

/**
 * Abstract base class for all playable characters.
 * Manages HP and sprite loading. Subclasses must implement six abstract methods
 * to define their unique identity, ability, and factory method.
 *
 * <p><b>Ability contract:</b> if {@link #ability()} causes {@link #isPerfectAimReady()}
 * to return {@code true}, {@link game.Controller} will lock the projectile angle and
 * then call {@link #resetAbilityEffect()}.
 */
public abstract class BasePlayer {

    private final int   maxHp;
    private       int   hp;
    private final Image sprite;

    /**
     * Loads the sprite image and initialises HP.
     * @param maxHp   maximum HP
     * @param startHp starting HP
     */
    protected BasePlayer(int maxHp, int startHp) {
        this.maxHp = maxHp;
        this.hp    = startHp;
        Image tmp = null;
        try {
            tmp = new Image(getClass().getResourceAsStream(getSpritePath()));
        } catch (Exception e) {
            System.out.println("Sprite not found: " + getSpritePath());
        }
        this.sprite = tmp;
    }

    /** Activates this character's special ability. */
    public abstract void        ability();
    /** @return display name shown in the HUD (e.g. {@code "Dog"}) */
    public abstract String      getName();
    /** @return short label shown on the ability button (e.g. {@code "HEAL"}) */
    public abstract String      getAbilityLabel();
    /** @return the {@link AbilityType} for colour-coding the ability button */
    public abstract AbilityType getAbilityType();
    /** @return classpath resource path to the character's sprite image */
    public abstract String      getSpritePath();
    /** @return a fresh instance of this character with full HP (used for Rematch) */
    public abstract BasePlayer  recreate();

    /** @return {@code true} if Cat's perfect-aim is ready; {@code false} by default */
    public boolean isPerfectAimReady()  { return false; }
    /** Clears the perfect-aim flag after the angle has been applied. No-op by default. */
    public void    resetAbilityEffect() {}

    /** @return the loaded sprite {@link Image}, or {@code null} if loading failed */
    public Image getSprite()            { return sprite; }
    /** @return current HP */
    public int   getHp()                { return hp; }
    /** @return maximum HP */
    public int   getMaxHp()             { return maxHp; }
    /** @param h new HP value */
    public void  setHp(int h)           { hp = h; }
    /** @param damage HP amount to subtract */
    public void  decreaseHp(int damage) { hp -= damage; }
}
