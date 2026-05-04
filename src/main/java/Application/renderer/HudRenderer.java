package Application.renderer;

import Application.Controller;
import Application.Projectile;
import Charactor.Player1;
import Charactor.Player2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HudRenderer {

    private static final double BAR_W = 250, BAR_H = 20;

    private final GraphicsContext gc;
    private final Controller      controller;
    private final Player1         p1;
    private final Player2         p2;

    public HudRenderer(GraphicsContext gc, Controller controller, Player1 p1, Player2 p2) {
        this.gc = gc; this.controller = controller;
        this.p1 = p1; this.p2 = p2;
    }

    public void draw() {
        drawHpBars();
        drawTurnIndicator();
        drawHint();
        drawAttackBadge();
    }

    private void drawHpBars() {
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(20, 20, (p1.getHp() / (double) p1.getMaxHp()) * BAR_W, BAR_H);
        double p2w = (p2.getHp() / (double) p2.getMaxHp()) * BAR_W;
        gc.fillRect(800 - 20 - p2w, 20, p2w, BAR_H);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.fillText("P1  " + p1.getHp() + "/" + p1.getMaxHp(), 22, 52);
        gc.fillText("P2  " + p2.getHp() + "/" + p2.getMaxHp(), 800 - 100, 52);
    }

    private void drawTurnIndicator() {
        gc.setFont(Font.font(15));
        if (isFrozenNow()) {
            gc.setFill(Color.CYAN);
            gc.fillText("[ P" + controller.getFrozenPlayer() + " - FROZEN ]", 325, 52);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillText(controller.isPlayer1Turn() ? "[ P1 TURN ]" : "[ P2 TURN ]", 350, 52);
        }
    }

    private void drawHint() {
        String selName = controller.getSelectedAttackName();
        Projectile.State state = controller.getProjectile().getState();
        gc.setFont(Font.font(11));
        if (isFrozenNow()) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Turn is being skipped...", 320, 88);
        } else if (state == Projectile.State.FLYING) {
            gc.setFill(Color.YELLOW);
            gc.fillText("[ " + selName + " ] in flight...", 330, 88);
        } else if (!selName.isEmpty()) {
            gc.setFill(attackColor(selName));
            gc.fillText("[ " + selName + " ] selected  →  SPACE to aim", 260, 88);
        } else {
            gc.setFill(Color.color(1, 1, 1, 0.5));
            gc.fillText("Click a skill button below", 305, 88);
        }
    }

    private void drawAttackBadge() {
        String selName = controller.getSelectedAttackName();
        Projectile.State state = controller.getProjectile().getState();
        if (!selName.isEmpty() && state != Projectile.State.FLYING) {
            double badgeX = controller.isPlayer1Turn() ? 104 : 618;
            gc.setFill(attackColor(selName));
            gc.setFont(Font.font(13));
            gc.fillText("[ " + selName + " ]", badgeX, 226);
        }
    }

    private boolean isFrozenNow() {
        return controller.getFrozenPlayer() != 0 &&
               controller.getFrozenPlayer() == (controller.isPlayer1Turn() ? 1 : 2);
    }

    static Color attackColor(String name) {
        return switch (name) {
            case "FIRE" -> Color.ORANGERED;
            case "ICE"  -> Color.CYAN;
            case "WIND" -> Color.color(0.2, 0.9, 0.5);
            default     -> Color.WHITE;
        };
    }
}
