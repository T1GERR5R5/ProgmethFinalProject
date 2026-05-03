package Application;

import AttackLogic.*;
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
            // SPACE controls the projectile (aim / fire)
            switch (event.getCode()) {
                case SPACE -> { controller.handleSpacebar(); return; }
            }

            // Attack selection keys — choose type first, then SPACE to aim
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

            controller.selectAttack(attack); // stores selection, does NOT fire yet
        });
    }
}
