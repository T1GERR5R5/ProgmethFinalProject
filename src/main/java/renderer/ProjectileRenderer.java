package renderer;

import game.Controller;
import game.Projectile;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class ProjectileRenderer {

    private final GraphicsContext gc;
    private final Controller      controller;

    public ProjectileRenderer(GraphicsContext gc, Controller controller) {
        this.gc = gc;
        this.controller = controller;
    }

    public void draw() {
        Projectile proj = controller.getProjectile();
        switch (proj.getState()) {
            case AIMING -> drawAiming(proj);
            case FLYING -> drawFlying(proj);
            default     -> {}
        }
    }

    private void drawAiming(Projectile proj) {
        boolean p1Turn = proj.isPlayer1Turn();
        double sx   = (p1Turn ? Projectile.P1_START_X : Projectile.P2_START_X) + proj.getLaunchXOffset();
        double sy   = Projectile.GROUND_Y+25;
        double rad  = Math.toRadians(proj.getAngle());
        double dirX = p1Turn ? Math.cos(rad) : -Math.cos(rad);
        double dirY = -Math.sin(rad);
        drawAimArrow(sx, sy, dirX, dirY);
    }

    private void drawAimArrow(double sx, double sy, double dirX, double dirY) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(sx, sy, sx + dirX * 55, sy + dirY * 55);
        gc.setFill(Color.WHITE);
        gc.fillOval(sx + dirX * 55 - 4, sy + dirY * 55 - 4, 8, 8);
    }

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
