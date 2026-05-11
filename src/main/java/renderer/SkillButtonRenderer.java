package renderer;

import game.Controller;
import game.Projectile;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Draws the five skill-selection circles for both players at the top of the canvas.
 *
 * <p>Each player has five buttons in order: Normal, Fire, Ice, Wind, and their unique
 * special Ability. A button is drawn:
 * <ul>
 *   <li>Greyed out with a cooldown counter when on cooldown.</li>
 *   <li>At 38 % alpha when it is not the player's turn, the projectile is flying, or the
 *       player is frozen.</li>
 *   <li>With a white highlight ring when the attack is currently selected.</li>
 * </ul>
 * Hit-testing is performed externally by
 * {@link application.HandleInput} using the public position constants.
 */
public class SkillButtonRenderer {

    /** Y centre of all skill buttons in pixels. */
    public static final double   BTN_Y    = 55;
    /** Radius of each skill button circle in pixels. */
    public static final double   BTN_R    = 14;
    /** X centres of P1's five buttons, left to right. */
    public static final double[] P1_BTN_X = {69, 107, 145, 183, 221};
    /** X centres of P2's five buttons, left to right. */
    public static final double[] P2_BTN_X = {579, 617, 655, 693, 731};

    private static final Color[] BASE_COLORS = {
        Color.GRAY,                        // Normal
        Color.ORANGERED,                   // Fire
        Color.CYAN,                        // Ice
        Color.GREEN,                       // Wind
        Color.WHITE                        // Ability (overridden per player)
    };

    private final GraphicsContext gc;
    private final Controller      controller;

    /**
     * @param gc         canvas context to draw on
     * @param controller game-logic hub used to query cooldowns, selected attack, and turn state
     */
    public SkillButtonRenderer(GraphicsContext gc, Controller controller) {
        this.gc = gc;
        this.controller = controller;
    }

    /** Draws both players' button rows. */
    public void draw() {
        drawPlayerButtons(P1_BTN_X, true);
        drawPlayerButtons(P2_BTN_X, false);
    }

    /**
     * Draws one player's row of five skill buttons.
     * @param buttonXCoordinates the X centre positions for this player's buttons
     * @param isP1               {@code true} for Player 1's row
     */
    private void drawPlayerButtons(double[] buttonXCoordinates, boolean isP1) {
        boolean myTurn = controller.isPlayer1Turn() == isP1;
        boolean canAct = myTurn
                      && controller.getProjectile().getState() == Projectile.State.IDLE
                      && !controller.isCurrentPlayerFrozen();
        String selName = controller.getSelectedAttackName();

        int[] cooldowns = {
            0,
            controller.getFireCooldown(isP1),
            controller.getIceCooldown(isP1),
            controller.getWindCooldown(isP1),
            controller.getAbilityCooldown(isP1)
        };
        String abilityLabel = controller.getAbilityLabel(isP1);
        String[] labels = {"NRM", "FIRE", "ICE", "WIND", abilityLabel};
        gc.setGlobalAlpha(canAct ? 1.0 : 0.38);

        for (int i = 0; i < buttonXCoordinates.length; i++) {
            double  xCoordinate = buttonXCoordinates[i];
            boolean onCooldown = cooldowns[i] > 0;
            boolean sel  = labels[i].equals(selName) && myTurn;

            Color fill;
            if (onCooldown) {
                fill = Color.color(0.25, 0.25, 0.25);
            } else {
                fill = BASE_COLORS[i];
            }
            gc.setFill(fill);

            gc.fillOval(xCoordinate - BTN_R, BTN_Y - BTN_R, BTN_R * 2, BTN_R * 2);

            if (sel) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2.5);
                gc.strokeOval(xCoordinate - BTN_R - 2, BTN_Y - BTN_R - 2, (BTN_R + 2) * 2, (BTN_R + 2) * 2);
            }
            gc.setFill(onCooldown ? Color.color(0.6, 0.6, 0.6) : Color.BLACK);
            gc.setFont(Font.font(7));
            gc.fillText(labels[i], xCoordinate - 8, BTN_Y + 3);

            if (onCooldown) {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(11));
                gc.fillText(String.valueOf(cooldowns[i]), xCoordinate - 3.5, BTN_Y + 16);
            }
        }
        gc.setGlobalAlpha(1.0);
    }
}
