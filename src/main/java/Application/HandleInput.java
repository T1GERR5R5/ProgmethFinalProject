package Application;

import Application.renderer.SkillButtonRenderer;
import AttackLogic.*;
import javafx.scene.Scene;

public class HandleInput {
    private final Scene      scene;
    private final Controller controller;

    public HandleInput(Scene scene, Controller controller) {
        this.scene      = scene;
        this.controller = controller;
    }

    public void process() {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE -> controller.handleSpacebar();
                // Keyboard shortcuts still work as fallback
                case Q, U  -> controller.handleAbility();
                case A     -> controller.selectAttack(new NormalAttack());
                case S     -> controller.selectAttack(new FireAttack());
                case D     -> controller.selectAttack(new IceAttack());
                case F     -> controller.selectAttack(new WindAttack());
                case J     -> controller.selectAttack(new NormalAttack());
                case K     -> controller.selectAttack(new FireAttack());
                case L     -> controller.selectAttack(new IceAttack());
                case SEMICOLON -> controller.selectAttack(new WindAttack());
                default    -> {}
            }
        });

        scene.setOnMouseClicked(event -> handleClick(event.getX(), event.getY()));
    }

    private void handleClick(double mx, double my) {
        // Only process clicks near the button row
        if (Math.abs(my - SkillButtonRenderer.BTN_Y) > SkillButtonRenderer.BTN_R + 6) return;

        double[] xs = controller.isPlayer1Turn() ? SkillButtonRenderer.P1_BTN_X : SkillButtonRenderer.P2_BTN_X;
        for (int i = 0; i < xs.length; i++) {
            double dx = mx - xs[i];
            double dy = my - SkillButtonRenderer.BTN_Y;
            if (dx * dx + dy * dy <= SkillButtonRenderer.BTN_R * SkillButtonRenderer.BTN_R) {
                switch (i) {
                    case 0 -> controller.selectAttack(new NormalAttack());
                    case 1 -> controller.selectAttack(new FireAttack());
                    case 2 -> controller.selectAttack(new IceAttack());
                    case 3 -> controller.selectAttack(new WindAttack());
                    case 4 -> controller.handleAbility();
                }
                return;
            }
        }
    }
}
