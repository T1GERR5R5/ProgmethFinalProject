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

    // ── Constants ────────────────────────────────────────────────────────────
    private static final double GROUND_Y      = Projectile.GROUND_Y;
    private static final double P1_BASE_X     = 100.0;
    private static final double P2_BASE_X     = 620.0;
    private static final int    FIRE_EMIT     = 4;
    private static final int    ICE_EMIT      = 3;
    private static final int    WIND_EMIT     = 3;

    // ── Fields ───────────────────────────────────────────────────────────────
    private final GraphicsContext gc;
    private final Controller      controller;

    private final List<Particle> fireParticles = new ArrayList<>();
    private final List<Particle> iceParticles  = new ArrayList<>();
    private final List<Particle> windParticles = new ArrayList<>();

    // ── Constructor ──────────────────────────────────────────────────────────
    public EffectRenderer(GraphicsContext gc, Controller controller) {
        this.gc         = gc;
        this.controller = controller;
    }

    // ── Public API ───────────────────────────────────────────────────────────
    public void reset() {
        fireParticles.clear();
        iceParticles.clear();
        windParticles.clear();
    }

    public void draw() {
        if (controller.getFrozenPlayer()     != 0) drawIceEffect();
        if (controller.getBurnTargetPlayer() != 0) drawFireGlow();
        if (controller.getWindTargetPlayer() != 0) drawWindOverlay();
        updateAndDrawParticles();
    }

    // ── Fire ─────────────────────────────────────────────────────────────────
    private void drawFireGlow() {
        double cx = charX(controller.getBurnTargetPlayer());
        gc.setFill(Color.color(1.0, 0.35, 0.0, 0.5));
        gc.fillOval(cx + 5, GROUND_Y + 60, 70, 18);
    }

    private void emitFireParticles() {
        if (controller.getBurnTargetPlayer() == 0) return;
        double cx = charX(controller.getBurnTargetPlayer());

        for (int i = 0; i < FIRE_EMIT; i++) {
            fireParticles.add(new Particle(
                    cx + 8 + Math.random() * 64,
                    GROUND_Y + 15 + Math.random() * 50,
                    (Math.random() - 0.5) * 1.5,
                    -1.5 - Math.random() * 2.0,
                    35 + Math.random() * 20
            ));
        }
    }

    private void drawFireParticles() {
        for (Particle p : fireParticles) {
            double t = p.lifeRatio();
            double r = 2 + t * 5;
            gc.setFill(fireColor(t));
            gc.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }
    }

    private Color fireColor(double t) {
        if (t > 0.65) return Color.color(1.0, 0.95, 0.0, t * 0.85);
        if (t > 0.35) return Color.color(1.0, 0.50, 0.0, t * 0.85);
        return             Color.color(0.9, 0.10, 0.0, t * 0.85);
    }

    // ── Ice ──────────────────────────────────────────────────────────────────
    private void drawIceEffect() {
        int    fp       = controller.getFrozenPlayer();
        double cx       = charX(fp) + 40;
        double cy       = GROUND_Y + 45;
        int    timer    = controller.getFrozenDisplayTimer();
        double progress = 1.0 - (timer / (double) StatusManager.FROZEN_DISPLAY_FRAMES);

        // overlay
        gc.setFill(Color.color(0.2, 0.6, 1.0, 0.2 + progress * 0.3));
        gc.fillRect(charX(fp) - 2, GROUND_Y + 3, 84, 84);

        // outer spikes (8 directions)
        gc.setStroke(Color.color(0.75, 0.93, 1.0, 0.85));
        gc.setLineWidth(1.5);
        drawIceSpikes(cx, cy, 8, 45, 30 * progress);

        // inner spikes (6 directions) — only in later half
        if (progress > 0.5) {
            gc.setStroke(Color.color(0.9, 1.0, 1.0, 0.95));
            gc.setLineWidth(2);
            drawIceSpikes(cx, cy, 6, 60, 13);
        }

        gc.setFill(Color.color(0.55, 0.9, 1.0));
        gc.setFont(Font.font(13));
    }

    private void drawIceSpikes(double cx, double cy, int count, int stepDeg, double len) {
        for (int i = 0; i < count; i++) {
            double a = Math.toRadians(i * stepDeg);
            gc.strokeLine(cx, cy, cx + Math.cos(a) * len, cy + Math.sin(a) * len);
        }
    }

    private void emitIceParticles() {
        if (controller.getFrozenPlayer() == 0) return;
        double cx = charX(controller.getFrozenPlayer()) + 40;
        double cy = GROUND_Y + 45;

        for (int i = 0; i < ICE_EMIT; i++) {
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

    private void drawIceParticles() {
        for (Particle p : iceParticles) {
            double t = p.lifeRatio();
            double r = 1.5 + t * 3;
            gc.setFill(Color.color(0.65 + t * 0.35, 0.88 + t * 0.12, 1.0, t * 0.9));
            gc.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }
    }

    // ── Wind ─────────────────────────────────────────────────────────────────
    private void drawWindOverlay() {
        int wp = controller.getWindTargetPlayer();
        gc.setFill(Color.color(0.15, 0.85, 0.45, 0.18));
        gc.fillRect(charX(wp) - 2, GROUND_Y + 3, 84, 84);
    }

    private void emitWindParticles() {
        int wp = controller.getWindTargetPlayer();
        if (wp == 0) return;
        double cx  = charX(wp) + 40;
        double dir = (wp == 2) ? -1.0 : 1.0;

        for (int i = 0; i < WIND_EMIT; i++) {
            windParticles.add(new Particle(
                    cx + dir * (10 + Math.random() * 30),
                    GROUND_Y + 10 + Math.random() * 60,
                    dir * (2.5 + Math.random() * 2.5),
                    (Math.random() - 0.5) * 0.6,
                    18 + Math.random() * 14
            ));
        }
    }

    private void drawWindParticles() {
        for (Particle p : windParticles) {
            double t = p.lifeRatio();
            gc.setFill(Color.color(0.2, 0.9, 0.5, t * 0.75));
            gc.fillOval(p.x - 6, p.y - 2, 12, 4);
        }
    }

    // ── Particle Update ──────────────────────────────────────────────────────
    private void updateAndDrawParticles() {
        emitFireParticles();
        emitIceParticles();
        emitWindParticles();

        updateParticles(fireParticles, 0.05);
        updateParticles(iceParticles,  0.03);
        updateParticles(windParticles, 0.00);

        drawFireParticles();
        drawIceParticles();
        drawWindParticles();
    }

    private void updateParticles(List<Particle> particles, double gravity) {
        for (Particle p : particles) {
            p.x  += p.vx;
            p.y  += p.vy;
            p.vy += gravity;
            p.life--;
        }
        particles.removeIf(Particle::isDead);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private double charX(int playerNum) {
        double base = (playerNum == 1) ? P1_BASE_X : P2_BASE_X;
        return base + controller.getWindXOffset(playerNum);
    }

    // ── Particle ─────────────────────────────────────────────────────────────
    private static final class Particle {
        double x, y, vx, vy, life;
        final double maxLife;

        Particle(double x, double y, double vx, double vy, double life) {
            this.x       = x;
            this.y       = y;
            this.vx      = vx;
            this.vy      = vy;
            this.life    = life;
            this.maxLife = life;
        }

        boolean isDead()    { return life <= 0; }
        double  lifeRatio() { return life / maxLife; }
    }
}