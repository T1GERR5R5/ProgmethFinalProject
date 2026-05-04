package Charactor;

import javafx.scene.image.Image;

public abstract class BasePlayer {
    private int maxHp;
    private int hp;
    private Image sprite;

    public abstract String getName();
    public abstract void   ability();

    // Override in subclasses that need extra ability state (e.g. Cat's perfect aim)
    public boolean isPerfectAimReady()  { return false; }
    public void    resetAbilityEffect() {}

    protected BasePlayer(int maxHp, int startHp, String spritePath) {
        this.maxHp = maxHp;
        this.hp    = startHp;
        setSprite(spritePath);
    }

    private void setSprite(String path) {
        try {
            this.sprite = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Sprite not found: " + path);
        }
    }

    public Image getSprite()  { return sprite; }
    public int   getHp()      { return hp; }
    public int   getMaxHp()   { return maxHp; }
    public void  setHp(int h) { hp = h; }
    public void  decreaseHp(int damage) { hp -= damage; }
}
