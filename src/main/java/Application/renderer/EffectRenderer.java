package Application.renderer;

import Application.Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class EffectRenderer {

    private final GraphicsContext gc;
    private final Controller      controller;

    // Particle format: [x, y, vx, vy, life, maxLife]
    private final List<double[]> fireParticles = new ArrayList<>();
    private final List<double[]> iceParticles  = new ArrayList<>();
    private final List<double[]> windParticles = new ArrayList<>();

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
        if (controller.getFrozenPlayer()    != 0) drawIceEffect();
        if (controller.getBurnTargetPlayer() != 0) drawFireGlow();
        if (controller.getWindTargetPlayer() != 0) drawWindOverlay();
        updateAndDrawParticles();
    }

    // ── Status badges ─────────────────────────────────────────────────────────

    private void drawStatusBadges() {
        if (controller.getBurnTargetPlayer() != 0) {
            gc.setFill(Color.ORANGERED);
            gc.setFont(Font.font(13));
            gc.fillText("BURNING! (" + controller.getBurnTicksLeft() + ")",
                        charX(controller.getBurnTargetPlayer()) - 2, 226);
        }
        if (controller.getWindTargetPlayer() != 0) {
            gc.setFill(Color.color(0.2, 0.9, 0.5));
            gc.setFont(Font.font(13));
            gc.fillText("WIND! (" + controller.getWindTurnsLeft() + ")",
                        charX(controller.getWindTargetPlayer()) - 2, 238);
        }
    }

    // ── Fire ──────────────────────────────────────────────────────────────────

    private void drawFireGlow() {
        double cx = charX(controller.getBurnTargetPlayer());
        double pulse = 0.35 + 0.2 * Math.sin(controller.getBurnFrameTimer() * 0.25);
        gc.setFill(Color.color(1.0, 0.35, 0.0, pulse));
        gc.fillOval(cx + 5, 295, 70, 18);
    }

    private void emitFireParticles() {
        if (controller.getBurnTargetPlayer() == 0) return;
        double cx = charX(controller.getBurnTargetPlayer());
        for (int i = 0; i < 4; i++) {
            double life = 35 + Math.random() * 20;
            fireParticles.add(new double[]{
                cx + 8 + Math.random() * 64, 255 + Math.random() * 50,
                (Math.random() - 0.5) * 1.5, -1.5 - Math.random() * 2.0, life, life
            });
        }
    }

    // ── Ice ───────────────────────────────────────────────────────────────────

    private void drawIceEffect() {
        int    fp       = controller.getFrozenPlayer();
        double cx       = charX(fp) + 40;
        double cy       = 280;
        int    timer    = controller.getFrozenDisplayTimer();
        double progress = 1.0 - (timer / (double) Controller.FROZEN_DISPLAY_FRAMES);

        gc.setFill(Color.color(0.2, 0.6, 1.0, 0.2 + progress * 0.3));
        gc.fillRect(charX(fp) - 2, 238, 84, 84);

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
        gc.fillText("FROZEN!", charX(fp) + 12, 226);
        gc.fillText("Skip in " + (int) Math.ceil(timer / 60.0) + "s", charX(fp) + 8, 362);
    }

    private void emitIceParticles() {
        if (controller.getFrozenPlayer() == 0) return;
        double cx = charX(controller.getFrozenPlayer()) + 40, cy = 280;
        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.0 + Math.random() * 2.5;
            double life  = 20 + Math.random() * 15;
            iceParticles.add(new double[]{
                cx + Math.cos(angle) * 12, cy + Math.sin(angle) * 12,
                Math.cos(angle) * speed, Math.sin(angle) * speed - 0.8, life, life
            });
        }
    }

    // ── Wind ──────────────────────────────────────────────────────────────────

    private void drawWindOverlay() {
        int wp = controller.getWindTargetPlayer();
        gc.setFill(Color.color(0.15, 0.85, 0.45, 0.18));
        gc.fillRect(charX(wp) - 2, 238, 84, 84);
    }

    private void emitWindParticles() {
        int wp = controller.getWindTargetPlayer();
        if (wp == 0) return;
        double cx = charX(wp) + 40;
        double dir = wp == 2 ? -1.0 : 1.0;
        for (int i = 0; i < 3; i++) {
            double startX = cx + dir * (10 + Math.random() * 30);
            double startY = 245 + Math.random() * 70;
            double speed  = 2.5 + Math.random() * 2.5;
            double life   = 18 + Math.random() * 14;
            windParticles.add(new double[]{
                startX, startY, dir * speed, (Math.random() - 0.5) * 0.6, life, life
            });
        }
    }

    // ── Particle update + draw ────────────────────────────────────────────────

    private void updateAndDrawParticles() {
        emitFireParticles();
        emitIceParticles();
        emitWindParticles();

        for (double[] p : fireParticles) { p[0] += p[2]; p[1] += p[3]; p[3] += 0.05; p[4]--; }
        fireParticles.removeIf(p -> p[4] <= 0);
        for (double[] p : fireParticles) {
            double t = p[4] / p[5], r = 2 + t * 5;
            gc.setFill(t > 0.65 ? Color.color(1.0, 0.95, 0.0, t * 0.85)
                     : t > 0.35 ? Color.color(1.0, 0.50, 0.0, t * 0.85)
                                : Color.color(0.9, 0.10, 0.0, t * 0.85));
            gc.fillOval(p[0] - r, p[1] - r, r * 2, r * 2);
        }

        for (double[] p : iceParticles) { p[0] += p[2]; p[1] += p[3]; p[3] += 0.03; p[4]--; }
        iceParticles.removeIf(p -> p[4] <= 0);
        for (double[] p : iceParticles) {
            double t = p[4] / p[5], r = 1.5 + t * 3;
            gc.setFill(Color.color(0.65 + t * 0.35, 0.88 + t * 0.12, 1.0, t * 0.9));
            gc.fillOval(p[0] - r, p[1] - r, r * 2, r * 2);
        }

        for (double[] p : windParticles) { p[0] += p[2]; p[1] += p[3]; p[4]--; }
        windParticles.removeIf(p -> p[4] <= 0);
        for (double[] p : windParticles) {
            double t = p[4] / p[5];
            gc.setFill(Color.color(0.2, 0.9, 0.5, t * 0.75));
            gc.fillOval(p[0] - 6, p[1] - 2, 12, 4);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private double charX(int playerNum) {
        return (playerNum == 1 ? 100.0 : 620.0) + controller.getWindXOffset(playerNum);
    }
}
