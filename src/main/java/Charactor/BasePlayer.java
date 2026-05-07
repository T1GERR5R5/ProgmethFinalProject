package Charactor;

import javafx.scene.image.Image;

public abstract class BasePlayer {

    private int   maxHp;
    private int   hp;
    private Image sprite;
    private boolean perfectAimReady = false;

    protected BasePlayer(int maxHp, int startHp) {
        this.maxHp = maxHp;
        this.hp    = startHp;
        try {
            this.sprite = new Image(getClass().getResourceAsStream(getSpritePath()));
        } catch (Exception e) {
            System.out.println("Sprite not found: " + getSpritePath());
        }
    }

    public abstract void   ability();
    public abstract String getName();
    public abstract String getAbilityLabel();
    public abstract String getSpritePath();

    public boolean isPerfectAimReady()  { return perfectAimReady; }
    public void    resetAbilityEffect() {}

    public Image getSprite()            { return sprite; }
    public int   getHp()                { return hp; }
    public int   getMaxHp()             { return maxHp; }
    public void  setHp(int h)           { hp = h; }
    public void  decreaseHp(int damage) { hp -= damage; }
}
