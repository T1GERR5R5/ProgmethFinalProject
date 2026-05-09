package renderer;

import game.Controller;
import game.Projectile;
import character.AbilityType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SkillButtonRenderer {

    public static final double   BTN_Y    = 55;
    public static final double   BTN_R    = 14;
    public static final double[] P1_BTN_X = {69, 107, 145, 183, 221};
    public static final double[] P2_BTN_X = {579, 617, 655, 693, 731};

    private static final int ABILITY_IDX = 4;

    private static final Color[] BASE_COLORS = {
        Color.color(0.85, 0.85, 0.85),   // Normal
        Color.ORANGERED,                   // Fire
        Color.CYAN,                        // Ice
        Color.color(0.2, 0.9, 0.5),       // Wind
        Color.WHITE                        // Ability (overridden per player)
    };
    private static final String[] ATK_NAMES = {"NORMAL", "FIRE", "ICE", "WIND"};

    private final GraphicsContext gc;
    private final Controller      controller;

    public SkillButtonRenderer(GraphicsContext gc, Controller controller) {
        this.gc = gc; this.controller = controller;
    }

    public void draw() {
        drawPlayerButtons(P1_BTN_X, true);
        drawPlayerButtons(P2_BTN_X, false);
    }

    private void drawPlayerButtons(double[] xs, boolean isP1) {
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
        Color  abilityColor = controller.getAbilityType(isP1) == AbilityType.HEAL
                            ? Color.color(0.4, 1.0, 0.4)
                            : Color.color(1.0, 0.85, 0.2);
        String[] labels = {"NRM", "FIRE", "ICE", "WIND", abilityLabel};

        gc.setGlobalAlpha(canAct ? 1.0 : 0.38);

        for (int i = 0; i < xs.length; i++) {
            double  cx   = xs[i];
            boolean onCd = cooldowns[i] > 0;
            boolean sel  = i < ATK_NAMES.length && ATK_NAMES[i].equals(selName) && myTurn;

            Color fill = onCd ? Color.color(0.25, 0.25, 0.25)
                              : (i == ABILITY_IDX ? abilityColor : BASE_COLORS[i]);
            gc.setFill(fill);
            gc.fillOval(cx - BTN_R, BTN_Y - BTN_R, BTN_R * 2, BTN_R * 2);

            if (sel) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2.5);
                gc.strokeOval(cx - BTN_R - 2, BTN_Y - BTN_R - 2, (BTN_R + 2) * 2, (BTN_R + 2) * 2);
            }
            if (i == ABILITY_IDX && controller.getProjectile().isAngleLocked() && myTurn) {
                gc.setStroke(Color.color(1.0, 0.9, 0.3));
                gc.setLineWidth(2.5);
                gc.strokeOval(cx - BTN_R - 2, BTN_Y - BTN_R - 2, (BTN_R + 2) * 2, (BTN_R + 2) * 2);
            }

            gc.setFill(onCd ? Color.color(0.6, 0.6, 0.6) : Color.BLACK);
            gc.setFont(Font.font(7));
            gc.fillText(labels[i], cx - 8, BTN_Y + 3);

            if (onCd) {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(11));
                gc.fillText(String.valueOf(cooldowns[i]), cx - 3.5, BTN_Y + 16);
            }
        }

        gc.setGlobalAlpha(1.0);
    }
}
