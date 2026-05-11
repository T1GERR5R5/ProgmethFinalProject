package application.scene;

import application.HandleInput;
import application.Main;
import character.BasePlayer;
import game.Controller;
import game.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import renderer.GameRenderer;

/**
 * Wires together all game subsystems and drives the main game loop for one match.
 *
 * <p>Creates the JavaFX {@link Canvas}, instantiates {@link Controller},
 * {@link GameRenderer}, and {@link HandleInput}, then starts an
 * {@link AnimationTimer} that calls {@code controller.update()} and
 * {@code renderer.render()} every frame. When either player's HP reaches zero the
 * timer stops and the scene transitions to {@link EndScene}.
 */
public class GamePlayScene {
    private AnimationTimer timer;
    private Controller     controller;
    private GameRenderer   renderer;
    private BasePlayer p1;
    private BasePlayer p2;

    /**
     * Initialises and starts a new match for the two given players.
     * Resets HP to max, builds the canvas scene, attaches input handling,
     * starts the fight BGM, and launches the AnimationTimer.
     *
     * @param p1 Player 1's character
     * @param p2 Player 2's character
     * @return the new JavaFX {@link javafx.scene.Scene} that has been set on the primary stage
     */
    public Scene startGame(BasePlayer p1,BasePlayer p2) {
        this.p1 = p1;
        this.p2 = p2;
        if (timer != null) timer.stop();
        if (renderer != null) renderer.reset();

        p1.setHp(p1.getMaxHp());
        p2.setHp(p2.getMaxHp());

        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        SoundManager.playFightBGM();

        controller = new Controller(p1, p2);
        renderer = new GameRenderer(gc, controller, p1, p2);

        Scene gameScene = new Scene(new StackPane(canvas));
        new HandleInput(gameScene, controller).process();
        Main.window.setScene(gameScene);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                controller.update();
                renderer.onResult(controller.getAndClearProjectileResult());
                renderer.render();

                if (p1.getHp() <= 0 || p2.getHp() <= 0) {
                    stop();
                    Main.window.setScene(new EndScene().buildEndScene(p1.getHp() <= 0 ? "Player 2" : "Player 1",p1,p2));
                }
            }
        };
        timer.start();
        return gameScene;
    }
}
