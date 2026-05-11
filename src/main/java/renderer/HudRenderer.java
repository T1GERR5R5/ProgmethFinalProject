package renderer;

import game.Controller;
import character.BasePlayer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Draws the heads-up display (HUD) overlay on the game canvas.
 *
 * <p>The HUD consists of:
 * <ul>
 *   <li>Two rounded HP-bar panels (one per player) at the top of the screen.</li>
 *   <li>A centred "VS" text label.</li>
 *   <li>A turn indicator below "VS" that shows whose turn it is or the FROZEN state.</li>
 * </ul>
 * HP bars shrink and change colour (green → yellow → red) as HP decreases.
 * P2's bar is right-aligned and fills from right to left.
 */
public class HudRenderer {

    private static final double PANEL_W    = 268;
    private static final double PANEL_H    = 36;
    private static final double P2_PANEL_X = 800 - 12 - PANEL_W;
    private static final double P1_PANEL_X = 12;
    private static final double BAR_INN_W  = 258;

    private final GraphicsContext gc;
    private final Controller      controller;
    private final BasePlayer      p1;
    private final BasePlayer      p2;

    /**
     * @param gc         canvas context to draw on
     * @param controller game-logic hub used to query turn and freeze state
     * @param p1         Player 1's character
     * @param p2         Player 2's character
     */
    public HudRenderer(GraphicsContext gc, Controller controller, BasePlayer p1, BasePlayer p2) {
        this.gc = gc;
        this.controller = controller;
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Draws the full HUD for the current frame: panel backgrounds, player labels,
     * VS text, HP bars, and the turn indicator.
     */
    public void draw() {
        gc.setFill(Color.color(0.07, 0.07, 0.12, 0.86));
        gc.fillRoundRect(P1_PANEL_X, 4, PANEL_W, PANEL_H, 10, 10);
        gc.fillRoundRect(P2_PANEL_X, 4, PANEL_W, PANEL_H, 10, 10);
        gc.setStroke(Color.color(0.46, 0.48, 0.58, 0.88));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(P1_PANEL_X, 4, PANEL_W, PANEL_H, 10, 10);
        gc.strokeRoundRect(P2_PANEL_X, 4, PANEL_W, PANEL_H, 10, 10);

        gc.setFont(Font.font(11));
        gc.setFill(Color.color(1.0, 0.25, 0.25));
        gc.fillText("P1", 19, 17);
        gc.setFill(Color.color(0.88, 0.88, 0.92));
        gc.fillText(" " + p1.getName(), 33, 17);

        gc.setFill(Color.color(0.35, 0.55, 1.0));
        gc.fillText("P2", P2_PANEL_X + 7, 17);
        gc.setFill(Color.color(0.88, 0.88, 0.92));
        gc.fillText(" " + p2.getName(), P2_PANEL_X + 21, 17);

        gc.setFill(Color.color(0.08, 0.08, 0.10));
        gc.fillRoundRect(P1_PANEL_X + 4, 21, BAR_INN_W, 16, 5, 5);
        gc.fillRoundRect(P2_PANEL_X + 4, 21, BAR_INN_W, 16, 5, 5);

        gc.setFont(Font.font(21));
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillText("VS", 394, 36);
        gc.setFill(Color.WHITE);
        gc.fillText("VS", 393, 35);

        updateHpBars();
        drawTurnIndicator();
    }

    /** Renders the current HP bars for both players and their numeric HP labels. */
    private void updateHpBars() {
        double P1HpRatio = Math.max(0, p1.getHp() / (double) p1.getMaxHp());
        gc.setFill(hpColor(P1HpRatio));
        if (P1HpRatio > 0) gc.fillRoundRect(P1_PANEL_X + 4, 22, BAR_INN_W * P1HpRatio, 14, 4, 4);

        double P2HpRatio  = Math.max(0, p2.getHp() / (double) p2.getMaxHp());
        gc.setFill(hpColor(P2HpRatio));
        if (P2HpRatio > 0) gc.fillRoundRect(P2_PANEL_X + 4 + BAR_INN_W *(1 - P2HpRatio), 22, BAR_INN_W * P2HpRatio, 14, 4, 4);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.fillText(p1.getHp() + " HP", P1_PANEL_X + 7, 34);
        gc.fillText(p2.getHp() + " HP", P2_PANEL_X + 7, 34);
    }

    /** Draws the turn label below "VS"; shows FROZEN state in cyan when applicable. */
    private void drawTurnIndicator() {
        boolean frozen = controller.isCurrentPlayerFrozen();
        String label = frozen
                ? "[ P" + controller.getFrozenPlayer() + " - FROZEN ]"
                : controller.isPlayer1Turn() ? "[ P1 TURN ]" : "[ P2 TURN ]";
        Color col = frozen ? Color.CYAN : Color.WHITE;
        gc.setFont(Font.font(14));
        gc.setFill(Color.color(0, 0, 0, 0.65));
        gc.fillText(label, 368, 77);
        gc.setFill(col);
        gc.fillText(label, 369, 76);
    }

    /**
     * Maps an HP ratio to a bar colour: green above 50 %, yellow above 25 %, red otherwise.
     * @param ratio current HP / max HP in [0, 1]
     */
    private static Color hpColor(double ratio) {
        if (ratio > 0.5)  return Color.rgb(55, 200, 65);
        if (ratio > 0.25) return Color.rgb(215, 175, 0);
        return Color.rgb(215, 45, 45);
    }
}
