package renderer;

import game.Controller;
import game.Projectile;
import character.BasePlayer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Top-level renderer that orchestrates all per-frame drawing for a match.
 *
 * <p>Composes four sub-renderers:
 * <ul>
 *   <li>{@link HudRenderer} — HP bars, player labels, turn indicator</li>
 *   <li>{@link SkillButtonRenderer} — clickable skill circles at the top</li>
 *   <li>{@link EffectRenderer} — Burn/Freeze/Wind particle effects</li>
 *   <li>{@link ProjectileRenderer} — aim arrow and flying projectile trail</li>
 * </ul>
 * Also draws the background image, player sprites (P2 mirrored), and the
 * temporary HIT/MISSED result flash.
 */
public class GameRenderer {

    private final GraphicsContext gc;
    private final Controller      controller;
    private final BasePlayer      p1;
    private final BasePlayer      p2;

    private final HudRenderer        hud;
    private final SkillButtonRenderer buttons;
    private final EffectRenderer      effects;
    private final ProjectileRenderer  projectileRenderer;

    private String lastResult  = "";
    private int    resultTimer = 0;

    private Image backgroundImage;

    private static final double GROUND_Y = Projectile.GROUND_Y;

    /**
     * Creates the renderer and all its sub-renderers.
     * @param gc         the canvas graphics context to draw on
     * @param controller game-logic hub used to query state
     * @param p1         Player 1's character
     * @param p2         Player 2's character
     */
    public GameRenderer(GraphicsContext gc, Controller controller, BasePlayer p1, BasePlayer p2) {
        this.gc = gc;
        this.controller = controller;
        this.p1 = p1;
        this.p2 = p2;
        hud                = new HudRenderer(gc, controller, p1, p2);
        buttons            = new SkillButtonRenderer(gc, controller);
        effects            = new EffectRenderer(gc, controller);
        projectileRenderer = new ProjectileRenderer(gc, controller);

        try {
            var stream = getClass().getResourceAsStream("/images/background.jpg");
            if (stream != null) backgroundImage = new Image(stream);
        } catch (Exception ignored) {}
    }

    /**
     * Stores a projectile result string to be displayed as a flash overlay.
     * The flash lasts 100 frames; an empty string is ignored.
     * @param result e.g. {@code "HIT!  FIRE!"} or {@code "MISSED!  Turn lost."}
     */
    public void onResult(String result) {
        if (result.isEmpty()) return;
        lastResult  = result;
        resultTimer = 100;
    }

    /** Clears the result flash and resets all particle effects. */
    public void reset() {
        effects.reset();
        lastResult  = "";
        resultTimer = 0;
    }

    /**
     * Draws one complete frame in layer order:
     * background → HUD → skill buttons → effects → projectile → result flash.
     * Called every frame by the AnimationTimer in {@link application.scene.GamePlayScene}.
     */
    public void render() {
        drawBackground();
        hud.draw();
        buttons.draw();
        effects.draw();
        projectileRenderer.draw();
        drawResultFlash();
    }

    /** Draws the background image and both player sprites (P2 horizontally mirrored). */
    private void drawBackground() {
        if (backgroundImage != null) gc.drawImage(backgroundImage, 0, 0, 800, 400);

        double p1X = charX(1), p2X = charX(2);
        gc.drawImage(p1.getSprite(), p1X, GROUND_Y + 5, 80, 80);
        gc.drawImage(p2.getSprite(), -(p2X + 80), GROUND_Y + 5, 80, 80);
        gc.save();
        gc.scale(-1, 1);
        gc.drawImage(p2.getSprite(), -(p2X + 80), GROUND_Y + 5, 80, 80);
        gc.restore();

    }

    /** Renders the HIT/MISSED flash text with a drop-shadow effect for 100 frames. */
    private void drawResultFlash() {
        if (resultTimer <= 0) return;
        resultTimer--;
        gc.setFont(Font.font(26));
        Color col = lastResult.contains("HIT") ? Color.YELLOW : Color.ORANGERED;
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillText(lastResult, 272, 197);
        gc.setFill(col);
        gc.fillText(lastResult, 270, 195);
    }

    /**
     * Returns the wind-adjusted X coordinate for a player's sprite.
     * @param playerNum 1 or 2
     * @return screen X position in pixels
     */
    private double charX(int playerNum) {
        return (playerNum == 1 ? 100.0 : 620.0) + controller.getWindXOffset(playerNum);
    }
}
