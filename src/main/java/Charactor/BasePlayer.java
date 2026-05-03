package Charactor;

import AttackLogic.Attackable;

import java.awt.*;
import javafx.scene.image.Image;

public class BasePlayer {
    private final int maxHp = 10;
    private int Hp = 7;
    private Image sprite;

    public void setSprite(String fileName) {
        // Path จะชี้ไปที่ src/main/resources/images/fileName
        String path = "/images/" + fileName;
        try {
            this.sprite = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Not found: " + path);
        }
    }

    public Image getSprite() {
        return sprite;
    }

    public void decreaseHp(int damage) {
        this.Hp -= damage;
    }

    public int getHp() {
        return Hp;
    }
    public int getMaxHp() {
        return maxHp;
    }

    public void setHp(int hp) {
        Hp = hp;
    }
}
