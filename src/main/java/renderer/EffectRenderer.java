package renderer;

import game.Controller;
import game.Projectile;
import game.StatusManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws particle-based visual effects for the three status effects: Burn, Freeze, and Wind.
 *
 * <p>Each status effect has:
 * <ul>
 *   <li>An overlay highlight drawn over the affected character's sprite.</li>
 *   <li>A pool of short-lived {@link Particle} instances emitted and updated every frame.</li>
 * </ul>
 * Called each frame by {@link GameRenderer#render()}.
 */
public class EffectRenderer {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final double GROUND_Y      = Projectile.GROUND_Y;
    private static final double P1_BASE_X     = 100.0;
    private static final double P2_BASE_X     = 620.0;
    /** Particles emitted per frame while burning. */
    private static final int    FIRE_EMIT     = 4;
    /** Particles emitted per frame while frozen. */
    private static final int    ICE_EMIT      = 3;
    /** Particles emitted per frame while under wind. */
    private static final int    WIND_EMIT     = 3;

    // ── Fields ───────────────────────────────────────────────────────────────
    private final GraphicsContext gc;
    private final Controller      controller;

    private final List<Particle> fireParticles = new ArrayList<>();
    private final List<Particle> iceParticles  = new ArrayList<>();
    private final List<Particle> windParticles = new ArrayList<>();

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * @param gc         canvas context to draw on
     * @param controller game-logic hub used to query active status effects
     */
    public EffectRenderer(GraphicsContext gc, Controller controller) {
        this.gc         = gc;
        this.controller = controller;
    }

    // ── Public API ───────────────────────────────────────────────────────────
    /** Clears all live particles — called when a new match starts. */
    public void reset() {
        fireParticles.clear();
        iceParticles.clear();
        windParticles.clear();
    }

    /**
     * Draws all active status-effect overlays and particles for one frame.
     * Only draws an effect if its corresponding player number is non-zero.
     */
    public void draw() {
        if (controller.getFrozenPlayer()     != 0) drawIceEffect();
        if (controller.getBurnTargetPlayer() != 0) drawFireGlow();
        if (controller.getWindTargetPlayer() != 0) drawWindOverlay();
        updateAndDrawParticles();
    }

    // ── Fire ─────────────────────────────────────────────────────────────────
    /** Draws an orange glow ellipse at the feet of the burning player. */
    private void drawFireGlow() {
        double cx = charX(controller.getBurnTargetPlayer());
        gc.setFill(Color.color(1.0, 0.35, 0.0, 0.5));
        gc.fillOval(cx + 5, GROUND_Y + 60, 70, 18);
    }

    /** Emits new fire particles originating from the burning character's body. */
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

    /** Renders each live fire particle as a colour-shifting circle. */
    private void drawFireParticles() {
        for (Particle p : fireParticles) {
            double t = p.lifeRatio();
            double r = 2 + t * 5;
            gc.setFill(fireColor(t));
            gc.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }
    }

    /**
     * Maps a fire-particle life ratio to a yellow → orange → red colour gradient.
     * @param t life ratio in [0, 1]; higher = fresher
     */
    private Color fireColor(double t) {
        if (t > 0.65) return Color.color(1.0, 0.95, 0.0, t * 0.85);
        if (t > 0.35) return Color.color(1.0, 0.50, 0.0, t * 0.85);
        return             Color.color(0.9, 0.10, 0.0, t * 0.85);
    }

    // ── Ice ──────────────────────────────────────────────────────────────────
    /** Draws the blue freeze overlay and radiating spike lines on the frozen player. */
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

    /**
     * Draws {@code count} evenly spaced lines radiating from the centre of the frozen character.
     * @param cx       centre X in pixels
     * @param cy       centre Y in pixels
     * @param count    number of spike lines
     * @param stepDeg  angular step between spikes in degrees
     * @param len      length of each spike in pixels
     */
    private void drawIceSpikes(double cx, double cy, int count, int stepDeg, double len) {
        for (int i = 0; i < count; i++) {
            double a = Math.toRadians(i * stepDeg);
            gc.strokeLine(cx, cy, cx + Math.cos(a) * len, cy + Math.sin(a) * len);
        }
    }

    /** Emits ice particles expanding outward from the frozen character's centre. */
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

    /** Renders each live ice particle as a light-blue fading circle. */
    private void drawIceParticles() {
        for (Particle p : iceParticles) {
            double t = p.lifeRatio();
            double r = 1.5 + t * 3;
            gc.setFill(Color.color(0.65 + t * 0.35, 0.88 + t * 0.12, 1.0, t * 0.9));
            gc.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }
    }

    // ── Wind ─────────────────────────────────────────────────────────────────
    /** Draws a green tinted overlay on the wind-affected player's sprite area. */
    private void drawWindOverlay() {
        int wp = controller.getWindTargetPlayer();
        gc.setFill(Color.color(0.15, 0.85, 0.45, 0.18));
        gc.fillRect(charX(wp) - 2, GROUND_Y + 3, 84, 84);
    }

    /** Emits wind particles that drift horizontally away from the affected character. */
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

    /** Renders each live wind particle as a horizontal green-tinted streak. */
    private void drawWindParticles() {
        for (Particle p : windParticles) {
            double t = p.lifeRatio();
            gc.setFill(Color.color(0.2, 0.9, 0.5, t * 0.75));
            gc.fillOval(p.x - 6, p.y - 2, 12, 4);
        }
    }

    // ── Particle Update ──────────────────────────────────────────────────────
    /** Emits new particles, applies physics to all live particles, then draws them. */
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

    /**
     * Advances position, applies gravity, decrements life, and removes dead particles.
     * @param particles the pool to update
     * @param gravity   downward acceleration per frame
     */
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
    /**
     * Returns the wind-adjusted X coordinate for a player's sprite.
     * @param playerNum 1 or 2
     */
    private double charX(int playerNum) {
        double base = (playerNum == 1) ? P1_BASE_X : P2_BASE_X;
        return base + controller.getWindXOffset(playerNum);
    }

    // ── Particle ─────────────────────────────────────────────────────────────
    /**
     * A simple mutable value object representing one particle in a visual effect.
     * Position, velocity, and remaining life are updated directly by the outer class.
     */
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

        /** @return {@code true} when the particle's lifetime has expired */
        boolean isDead()    { return life <= 0; }
        /** @return fraction of lifetime remaining (1.0 = brand new, 0.0 = dead) */
        double  lifeRatio() { return life / maxLife; }
    }
}
