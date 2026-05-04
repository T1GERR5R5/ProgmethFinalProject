package Application;

import Application.renderer.*;
import Charactor.BasePlayer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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

    public GameRenderer(GraphicsContext gc, Controller controller, BasePlayer p1, BasePlayer p2) {
        this.gc = gc; this.controller = controller;
        this.p1 = p1; this.p2 = p2;
        hud                = new HudRenderer(gc, controller, p1, p2);
        buttons            = new SkillButtonRenderer(gc, controller);
        effects            = new EffectRenderer(gc, controller);
        projectileRenderer = new ProjectileRenderer(gc, controller);

        try {
            var stream = getClass().getResourceAsStream("/images/background.jpg");
            if (stream != null) backgroundImage = new Image(stream);
        } catch (Exception ignored) {}
    }

    public void onResult(String result) {
        if (result.isEmpty()) return;
        lastResult  = result;
        resultTimer = 100;
    }

    public void reset() {
        effects.reset();
        lastResult  = "";
        resultTimer = 0;
    }

    public void render() {
        drawBackground();
        hud.draw();
        buttons.draw();
        effects.draw();
        projectileRenderer.draw();
        drawResultFlash();
    }

    // ── Background + sprites ─────────────────────────────────────────────────

    private void drawBackground() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, 800, 400);
        }

        // Characters
        double p1X = charX(1), p2X = charX(2);
        if (p1.getSprite() != null) gc.drawImage(p1.getSprite(), p1X, GROUND_Y + 5, 80, 80);
        else { gc.setFill(Color.RED);  gc.fillRect(p1X, GROUND_Y - 20, 50, 50); }
        if (p2.getSprite() != null) {
            gc.save();
            gc.scale(-1, 1);
            gc.drawImage(p2.getSprite(), -(p2X + 80), GROUND_Y + 5, 80, 80);
            gc.restore();
        } else { gc.setFill(Color.BLUE); gc.fillRect(p2X, GROUND_Y - 20, 50, 50); }
    }

    private static final double GROUND_Y = Projectile.GROUND_Y;
 

    // ── Result flash ─────────────────────────────────────────────────────────

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

    // ── Helper ───────────────────────────────────────────────────────────────

    private double charX(int playerNum) {
        return (playerNum == 1 ? 100.0 : 620.0) + controller.getWindXOffset(playerNum);
    }
}
