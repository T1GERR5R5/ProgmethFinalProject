package Application.renderer;

import Application.Controller;
import Application.Projectile;
import Charactor.BasePlayer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HudRenderer {

    private static final double PANEL_W    = 268;
    private static final double PANEL_H    = 36;
    private static final double P2_PANEL_X = 800 - 12 - PANEL_W;  // 520
    private static final double BAR_INN_W  = 258;

    private final GraphicsContext gc;
    private final Controller      controller;
    private final BasePlayer      p1;
    private final BasePlayer      p2;

    public HudRenderer(GraphicsContext gc, Controller controller, BasePlayer p1, BasePlayer p2) {
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
        // ── Panels ──────────────────────────────────────────────────────────
        gc.setFill(Color.color(0.07, 0.07, 0.12, 0.86));
        gc.fillRoundRect(12, 4, PANEL_W, PANEL_H, 10, 10);
        gc.fillRoundRect(P2_PANEL_X, 4, PANEL_W, PANEL_H, 10, 10);
        gc.setStroke(Color.color(0.46, 0.48, 0.58, 0.88));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(12, 4, PANEL_W, PANEL_H, 10, 10);
        gc.strokeRoundRect(P2_PANEL_X, 4, PANEL_W, PANEL_H, 10, 10);

        // ── Player name labels ───────────────────────────────────────────────
        gc.setFont(Font.font(11));
        gc.setFill(Color.color(1.0, 0.25, 0.25));
        gc.fillText("P1", 19, 17);
        gc.setFill(Color.color(0.88, 0.88, 0.92));
        gc.fillText(" " + p1.getName(), 33, 17);

        gc.setFill(Color.color(0.35, 0.55, 1.0));
        gc.fillText("P2", P2_PANEL_X + 7, 17);
        gc.setFill(Color.color(0.88, 0.88, 0.92));
        gc.fillText(" " + p2.getName(), P2_PANEL_X + 21, 17);

        // ── Bar tracks ──────────────────────────────────────────────────────
        gc.setFill(Color.color(0.08, 0.08, 0.10));
        gc.fillRoundRect(16, 21, BAR_INN_W, 16, 5, 5);
        gc.fillRoundRect(P2_PANEL_X + 4, 21, BAR_INN_W, 16, 5, 5);

        // ── P1 HP fill (left → right) ────────────────────────────────────────
        double p1r = Math.max(0, p1.getHp() / (double) p1.getMaxHp());
        gc.setFill(hpColor(p1r));
        if (p1r > 0) gc.fillRoundRect(17, 22, BAR_INN_W * p1r, 14, 4, 4);

        // ── P2 HP fill (right → left) ────────────────────────────────────────
        double p2r = Math.max(0, p2.getHp() / (double) p2.getMaxHp());
        double p2fw = BAR_INN_W * p2r;
        gc.setFill(hpColor(p2r));
        if (p2r > 0) gc.fillRoundRect(P2_PANEL_X + 4 + BAR_INN_W - p2fw, 22, p2fw, 14, 4, 4);

        // ── HP numbers ───────────────────────────────────────────────────────
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.fillText(p1.getHp() + " HP", 20, 34);
        gc.fillText(p2.getHp() + " HP", P2_PANEL_X + 7, 34);

        // ── VS. centre (shadow then fill) ────────────────────────────────────
        gc.setFont(Font.font(21));
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillText("VS.", 375, 36);
        gc.setFill(Color.WHITE);
        gc.fillText("VS.", 374, 35);
    }

    private void drawTurnIndicator() {
        String label = isFrozenNow()
                ? "[ P" + controller.getFrozenPlayer() + " - FROZEN ]"
                : controller.isPlayer1Turn() ? "[ P1 TURN ]" : "[ P2 TURN ]";
        Color col = isFrozenNow() ? Color.CYAN : Color.WHITE;
        gc.setFont(Font.font(14));
        gc.setFill(Color.color(0, 0, 0, 0.65));
        gc.fillText(label, 336, 77);
        gc.setFill(col);
        gc.fillText(label, 335, 76);
    }

    private void drawHint() {
        /*
        String selName = controller.getSelectedAttackName();
        Projectile.State state = controller.getProjectile().getState();
        String hint;
        Color  col;
        if (isFrozenNow()) {
            hint = "Turn is being skipped...";
            col  = Color.LIGHTYELLOW;
        } else if (state == Projectile.State.FLYING) {
            hint = "[ " + selName + " ] in flight...";
            col  = Color.YELLOW;
        } else if (!selName.isEmpty()) {
            hint = "[ " + selName + " ] selected  →  SPACE to aim";
            col  = attackColor(selName);
        } else {
            hint = "Click a skill button to select attack";
            col  = Color.color(1, 1, 1, 0.55);
        }
        gc.setFont(Font.font(11));
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillText(hint, 281, 91);
        gc.setFill(col);
        gc.fillText(hint, 280, 90);
        */
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

    private static Color hpColor(double ratio) {
        if (ratio > 0.5)  return Color.rgb(55, 200, 65);
        if (ratio > 0.25) return Color.rgb(215, 175, 0);
        return Color.rgb(215, 45, 45);
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
