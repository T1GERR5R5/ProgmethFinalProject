package renderer;

import game.Controller;
import game.Projectile;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Draws the projectile in its two active states: AIMING and FLYING.
 *
 * <ul>
 *   <li><b>AIMING</b> — renders a white aim arrow extending from the shooter's launch
 *       position in the current angle direction.</li>
 *   <li><b>FLYING</b> — renders the orange-red projectile ball with a fading trail
 *       of translucent circles that grows in radius toward the head.</li>
 * </ul>
 * The renderer reads all necessary state from the shared {@link Projectile} instance
 * obtained via {@link Controller#getProjectile()}.
 */
public class ProjectileRenderer {

    private final GraphicsContext gc;
    private final Controller      controller;

    /**
     * @param gc         canvas context to draw on
     * @param controller game-logic hub used to obtain the projectile instance
     */
    public ProjectileRenderer(GraphicsContext gc, Controller controller) {
        this.gc = gc;
        this.controller = controller;
    }

    /**
     * Draws the projectile for the current frame.
     * Does nothing when the projectile is in {@link Projectile.State#IDLE}.
     */
    public void draw() {
        Projectile proj = controller.getProjectile();
        switch (proj.getState()) {
            case AIMING -> drawAiming(proj);
            case FLYING -> drawFlying(proj);
            default     -> {}
        }
    }

    /** Computes the aim-arrow direction from the current angle and delegates to {@link #drawAimArrow}. */
    private void drawAiming(Projectile proj) {
        boolean p1Turn = proj.isPlayer1Turn();
        double sx   = (p1Turn ? Projectile.P1_START_X : Projectile.P2_START_X) + proj.getLaunchXOffset();
        double sy   = Projectile.GROUND_Y+25;
        double rad  = Math.toRadians(proj.getAngle());
        double dirX = p1Turn ? Math.cos(rad) : -Math.cos(rad);
        double dirY = -Math.sin(rad);
        drawAimArrow(sx, sy, dirX, dirY);
    }

    /**
     * Draws a 55-pixel line from the launch point with a circle at the tip.
     * @param sx   start X (launch position)
     * @param sy   start Y (launch position)
     * @param dirX normalised X direction
     * @param dirY normalised Y direction
     */
    private void drawAimArrow(double sx, double sy, double dirX, double dirY) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(sx, sy, sx + dirX * 55, sy + dirY * 55);
        gc.setFill(Color.WHITE);
        gc.fillOval(sx + dirX * 55 - 4, sy + dirY * 55 - 4, 8, 8);
    }

    /**
     * Draws the trail of fading circles followed by the main 18px orange-red ball.
     * Trail circles increase in radius and opacity toward the current position.
     * @param proj the projectile whose trail and position are rendered
     */
    private void drawFlying(Projectile proj) {
        List<double[]> trail = proj.getTrail();
        for (int i = 0; i < trail.size(); i++) {
            double a = (i + 1.0) / (trail.size() + 1.0);
            double r = 3 + a * 4;
            gc.setFill(Color.color(1.0, 0.4, 0.0, a * 0.65));
            gc.fillOval(trail.get(i)[0] - r, trail.get(i)[1] - r, r * 2, r * 2);
        }
        gc.setFill(Color.ORANGERED);
        gc.fillOval(proj.getX() - 9, proj.getY() - 9, 18, 18);
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1.5);
        gc.strokeOval(proj.getX() - 9, proj.getY() - 9, 18, 18);
    }
}
