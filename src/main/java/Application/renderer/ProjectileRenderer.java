package Application.renderer;

import Application.Controller;
import Application.Projectile;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class ProjectileRenderer {

    private final GraphicsContext gc;
    private final Controller      controller;

    public ProjectileRenderer(GraphicsContext gc, Controller controller) {
        this.gc = gc; this.controller = controller;
    }

    public void draw() {
        Projectile proj = controller.getProjectile();
        switch (proj.getState()) {
            case AIMING -> drawAiming(proj);
            case FLYING -> drawFlying(proj);
            default     -> {}
        }
    }

    // ── Aiming ────────────────────────────────────────────────────────────────

    private void drawAiming(Projectile proj) {
        boolean p1Turn = proj.isPlayer1Turn();
        double sx  = (p1Turn ? Projectile.P1_START_X : Projectile.P2_START_X) + proj.getLaunchXOffset();
        double sy  = p1Turn ? Projectile.P1_START_Y : Projectile.P2_START_Y;
        double rad = Math.toRadians(proj.getAngle());
        double dirX = p1Turn ? Math.cos(rad) : -Math.cos(rad);
        double dirY = -Math.sin(rad);

        drawAimArrow(sx, sy, dirX, dirY);
        drawTrajectoryDots(proj, p1Turn);
        drawTargetBox(p1Turn);
        drawAngleLabel(proj, p1Turn);
    }

    private void drawAimArrow(double sx, double sy, double dirX, double dirY) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(sx, sy, sx + dirX * 55, sy + dirY * 55);
        gc.setFill(Color.WHITE);
        gc.fillOval(sx + dirX * 55 - 4, sy + dirY * 55 - 4, 8, 8);
    }

    private void drawTrajectoryDots(Projectile proj, boolean p1Turn) {
        double[][] pts = proj.getTrajectoryPoints(22);
        for (int i = 0; i < pts.length; i++) {
            double px = pts[i][0], py = pts[i][1];
            if (px < 0 || px > 800 || py > 308) break;
            double alpha = 0.9 - (i / 22.0) * 0.55;
            double r     = 5   - (i / 22.0) * 2;
            gc.setFill(isInTargetBox(px, py, p1Turn)
                    ? Color.color(0.1, 1.0, 0.1, alpha)
                    : Color.color(1.0, 0.55, 0.0, alpha));
            gc.fillOval(px - r, py - r, r * 2, r * 2);
        }
    }

    private void drawTargetBox(boolean p1Turn) {
        double off = p1Turn ? controller.getWindXOffset(2) : controller.getWindXOffset(1);
        gc.setStroke(Color.color(1, 1, 0, 0.55));
        gc.setLineWidth(2);
        if (p1Turn)
            gc.strokeRect(Projectile.P2_BOX_X1 + off, Projectile.BOX_Y1,
                          Projectile.P2_BOX_X2 - Projectile.P2_BOX_X1,
                          Projectile.BOX_Y2    - Projectile.BOX_Y1);
        else
            gc.strokeRect(Projectile.P1_BOX_X1 + off, Projectile.BOX_Y1,
                          Projectile.P1_BOX_X2 - Projectile.P1_BOX_X1,
                          Projectile.BOX_Y2    - Projectile.BOX_Y1);
    }

    private void drawAngleLabel(Projectile proj, boolean p1Turn) {
        double labelX = (p1Turn ? 105 : 535) + proj.getLaunchXOffset();
        gc.setFont(Font.font(13));
        if (proj.isAngleLocked()) {
            gc.setFill(Color.color(1.0, 0.9, 0.3));
            gc.fillText(String.format("[ LOCKED ] %.0f°", proj.getAngle()), labelX, 236);
            gc.fillText("SPACE to fire! (Perfect aim)", labelX - 20, 358);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillText(String.format("Angle: %.0f°", proj.getAngle()), labelX, 236);
            gc.fillText("SPACE to lock & fire!", labelX, 358);
        }
    }

    // ── Flying ────────────────────────────────────────────────────────────────

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

    // ── Helper ────────────────────────────────────────────────────────────────

    private boolean isInTargetBox(double px, double py, boolean p1Turn) {
        double off = p1Turn ? controller.getWindXOffset(2) : controller.getWindXOffset(1);
        if (p1Turn)
            return px >= Projectile.P2_BOX_X1 + off && px <= Projectile.P2_BOX_X2 + off
                && py >= Projectile.BOX_Y1 && py <= Projectile.BOX_Y2;
        else
            return px >= Projectile.P1_BOX_X1 + off && px <= Projectile.P1_BOX_X2 + off
                && py >= Projectile.BOX_Y1 && py <= Projectile.BOX_Y2;
    }
}
