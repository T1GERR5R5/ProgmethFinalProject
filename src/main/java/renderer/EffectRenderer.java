package renderer;

import game.Controller;
import game.Projectile;
import game.StatusManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class EffectRenderer {

    private static final double GROUND_Y = Projectile.GROUND_Y;

    private final GraphicsContext gc;
    private final Controller      controller;

    private final List<Particle> fireParticles = new ArrayList<>();
    private final List<Particle> iceParticles  = new ArrayList<>();
    private final List<Particle> windParticles = new ArrayList<>();

    public EffectRenderer(GraphicsContext gc, Controller controller) {
        this.gc = gc; this.controller = controller;
    }

    public void reset() {
        fireParticles.clear();
        iceParticles.clear();
        windParticles.clear();
    }

    public void draw() {
        drawStatusBadges();
        if (controller.getFrozenPlayer()     != 0) drawIceEffect();
        if (controller.getBurnTargetPlayer() != 0) drawFireGlow();
        if (controller.getWindTargetPlayer() != 0) drawWindOverlay();
        updateAndDrawParticles();
    }

    private void drawStatusBadges() {
        if (controller.getBurnTargetPlayer() != 0) {
            gc.setFill(Color.ORANGERED);
            gc.setFont(Font.font(13));
            gc.fillText("BURNING! (" + controller.getBurnTicksLeft() + ")",
                        charX(controller.getBurnTargetPlayer()) - 2, GROUND_Y - 9);
        }
        if (controller.getWindTargetPlayer() != 0) {
            gc.setFill(Color.color(0.2, 0.9, 0.5));
            gc.setFont(Font.font(13));
            gc.fillText("WIND! (" + controller.getWindTurnsLeft() + ")",
                        charX(controller.getWindTargetPlayer()) - 2, GROUND_Y + 3);
        }
    }

    private void drawFireGlow() {
        double cx    = charX(controller.getBurnTargetPlayer());
        double pulse = 0.35 + 0.2 * Math.sin(controller.getBurnFrameTimer() * 0.25);
        gc.setFill(Color.color(1.0, 0.35, 0.0, pulse));
        gc.fillOval(cx + 5, GROUND_Y + 60, 70, 18);
    }

    private void emitFireParticles() {
        if (controller.getBurnTargetPlayer() == 0) return;
        double cx = charX(controller.getBurnTargetPlayer());
        for (int i = 0; i < 4; i++) {
            fireParticles.add(new Particle(
                cx + 8 + Math.random() * 64,
                GROUND_Y + 15 + Math.random() * 50,
                (Math.random() - 0.5) * 1.5,
                -1.5 - Math.random() * 2.0,
                35 + Math.random() * 20
            ));
        }
    }

    private void drawIceEffect() {
        int    fp       = controller.getFrozenPlayer();
        double cx       = charX(fp) + 40;
        double cy       = GROUND_Y + 45;
        int    timer    = controller.getFrozenDisplayTimer();
        double progress = 1.0 - (timer / (double) StatusManager.FROZEN_DISPLAY_FRAMES);

        gc.setFill(Color.color(0.2, 0.6, 1.0, 0.2 + progress * 0.3));
        gc.fillRect(charX(fp) - 2, GROUND_Y + 3, 84, 84);

        gc.setStroke(Color.color(0.75, 0.93, 1.0, 0.85));
        gc.setLineWidth(1.5);
        double maxLen = 30 * progress;
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(i * 45);
            gc.strokeLine(cx, cy, cx + Math.cos(a) * maxLen, cy + Math.sin(a) * maxLen);
        }
        if (progress > 0.5) {
            gc.setStroke(Color.color(0.9, 1.0, 1.0, 0.95));
            gc.setLineWidth(2);
            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(i * 60);
                gc.strokeLine(cx, cy, cx + Math.cos(a) * 13, cy + Math.sin(a) * 13);
            }
        }
        gc.setFill(Color.color(0.55, 0.9, 1.0));
        gc.setFont(Font.font(13));
        gc.fillText("FROZEN!", charX(fp) + 12, GROUND_Y - 9);
        gc.fillText("Skip in " + (int) Math.ceil(timer / 60.0) + "s", charX(fp) + 8, GROUND_Y + 92);
    }

    private void emitIceParticles() {
        if (controller.getFrozenPlayer() == 0) return;
        double cx = charX(controller.getFrozenPlayer()) + 40;
        double cy = GROUND_Y + 45;
        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.0 + Math.random() * 2.5;
            iceParticles.add(new Particle(
                cx + Math.cos(angle) * 12,
                cy + Math.sin(angle) * 12,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed - 0.8,
                20 + Math.random() * 15
            ));
        }
    }

    private void drawWindOverlay() {
        int wp = controller.getWindTargetPlayer();
        gc.setFill(Color.color(0.15, 0.85, 0.45, 0.18));
        gc.fillRect(charX(wp) - 2, GROUND_Y + 3, 84, 84);
    }

    private void emitWindParticles() {
        int wp = controller.getWindTargetPlayer();
        if (wp == 0) return;
        double cx  = charX(wp) + 40;
        double dir = wp == 2 ? -1.0 : 1.0;
        for (int i = 0; i < 3; i++) {
            windParticles.add(new Particle(
                cx + dir * (10 + Math.random() * 30),
                GROUND_Y + 10 + Math.random() * 60,
                dir * (2.5 + Math.random() * 2.5),
                (Math.random() - 0.5) * 0.6,
                18 + Math.random() * 14
            ));
        }
    }

    private void updateAndDrawParticles() {
        emitFireParticles();
        emitIceParticles();
        emitWindParticles();

        for (Particle p : fireParticles) { p.x += p.vx; p.y += p.vy; p.vy += 0.05; p.life--; }
        fireParticles.removeIf(Particle::isDead);
        for (Particle p : fireParticles) {
            double t = p.lifeRatio(), r = 2 + t * 5;
            gc.setFill(t > 0.65 ? Color.color(1.0, 0.95, 0.0, t * 0.85)
                     : t > 0.35 ? Color.color(1.0, 0.50, 0.0, t * 0.85)
                                : Color.color(0.9, 0.10, 0.0, t * 0.85));
            gc.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }

        for (Particle p : iceParticles) { p.x += p.vx; p.y += p.vy; p.vy += 0.03; p.life--; }
        iceParticles.removeIf(Particle::isDead);
        for (Particle p : iceParticles) {
            double t = p.lifeRatio(), r = 1.5 + t * 3;
            gc.setFill(Color.color(0.65 + t * 0.35, 0.88 + t * 0.12, 1.0, t * 0.9));
            gc.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }

        for (Particle p : windParticles) { p.x += p.vx; p.y += p.vy; p.life--; }
        windParticles.removeIf(Particle::isDead);
        for (Particle p : windParticles) {
            double t = p.lifeRatio();
            gc.setFill(Color.color(0.2, 0.9, 0.5, t * 0.75));
            gc.fillOval(p.x - 6, p.y - 2, 12, 4);
        }
    }

    private double charX(int playerNum) {
        return (playerNum == 1 ? 100.0 : 620.0) + controller.getWindXOffset(playerNum);
    }

    private static final class Particle {
        double x, y, vx, vy, life;
        final double maxLife;

        Particle(double x, double y, double vx, double vy, double life) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life;
        }

        boolean isDead()    { return life <= 0; }
        double  lifeRatio() { return life / maxLife; }
    }
}
