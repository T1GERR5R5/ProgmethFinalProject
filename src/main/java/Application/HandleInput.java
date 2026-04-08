package Application;

import AttackLogic.*;
import Charactor.Player1;
import Charactor.Player2;
import javafx.scene.Scene;

public class HandleInput {
    private Scene scene;
    private Controller controller;

    public HandleInput(Scene scene, Controller controller) {
        this.scene = scene;
        this.controller = controller;
    }

    public void process() {
        scene.setOnKeyPressed(event -> {
            Attackable attack = null;

            if (controller.isPlayer1Turn()) {
                switch (event.getCode()) {
                    case A -> attack = new NormalAttack();
                    case S -> attack = new FireAttack();
                    case D -> attack = new IceAttack();
                }
            } else {
                switch (event.getCode()) {
                    case J -> attack = new NormalAttack();
                    case K -> attack = new FireAttack();
                    case L -> attack = new IceAttack();
                }
            }

            // ส่งให้ Controller ตัดสินใจ
            controller.executeAttack(attack);
        });
    }
}