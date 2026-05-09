package character;

import javafx.scene.image.Image;

public abstract class BasePlayer {

    private final int   maxHp;
    private       int   hp;
    private final Image sprite;

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

    public abstract void        ability();
    public abstract String      getName();
    public abstract String      getAbilityLabel();
    public abstract AbilityType getAbilityType();
    public abstract String      getSpritePath();
    public abstract BasePlayer  recreate();

    public boolean isPerfectAimReady()  { return false; }
    public void    resetAbilityEffect() {}

    public Image getSprite()            { return sprite; }
    public int   getHp()                { return hp; }
    public int   getMaxHp()             { return maxHp; }
    public void  setHp(int h)           { hp = h; }
    public void  decreaseHp(int damage) { hp -= damage; }
}
