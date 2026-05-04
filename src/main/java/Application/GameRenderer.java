package Application;

import Application.renderer.*;
import Charactor.Player1;
import Charactor.Player2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameRenderer {

    private final GraphicsContext gc;
    private final Controller      controller;
    private final Player1         p1;
    private final Player2         p2;

    private final HudRenderer        hud;
    private final SkillButtonRenderer buttons;
    private final EffectRenderer      effects;
    private final ProjectileRenderer  projectileRenderer;

    private String lastResult  = "";
    private int    resultTimer = 0;

    public GameRenderer(GraphicsContext gc, Controller controller, Player1 p1, Player2 p2) {
        this.gc = gc; this.controller = controller;
        this.p1 = p1; this.p2 = p2;
        hud                = new HudRenderer(gc, controller, p1, p2);
        buttons            = new SkillButtonRenderer(gc, controller);
        effects            = new EffectRenderer(gc, controller);
        projectileRenderer = new ProjectileRenderer(gc, controller);
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
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 400);
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 300, 800, 100);

        double p1X = charX(1), p2X = charX(2);
        if (p1.getSprite() != null) gc.drawImage(p1.getSprite(), p1X, 240, 80, 80);
        else { gc.setFill(Color.RED);  gc.fillRect(p1X, 250, 50, 50); }
        if (p2.getSprite() != null) gc.drawImage(p2.getSprite(), p2X, 240, 80, 80);
        else { gc.setFill(Color.BLUE); gc.fillRect(p2X, 250, 50, 50); }

        gc.setFill(Color.GRAY);
        gc.fillRect(20, 20, 250, 20);
        gc.fillRect(800 - 250 - 20, 20, 250, 20);
    }

    // ── Result flash ─────────────────────────────────────────────────────────

    private void drawResultFlash() {
        if (resultTimer <= 0) return;
        resultTimer--;
        gc.setFont(Font.font(26));
        gc.setFill(lastResult.contains("HIT") ? Color.YELLOW : Color.ORANGERED);
        gc.fillText(lastResult, 270, 195);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private double charX(int playerNum) {
        return (playerNum == 1 ? 100.0 : 620.0) + controller.getWindXOffset(playerNum);
    }
}
