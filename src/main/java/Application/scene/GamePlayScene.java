package application.scene;

import application.HandleInput;
import application.Main;
import character.BasePlayer;
import game.Controller;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import renderer.GameRenderer;

public class GamePlayScene {
    private AnimationTimer timer;
    private Controller     controller;
    private GameRenderer   renderer;
    private BasePlayer p1;
    private BasePlayer p2;

    public Scene startGame(BasePlayer p1,BasePlayer p2) {
        this.p1 = p1;
        this.p2 = p2;
        if (timer != null) timer.stop();
        if (renderer != null) renderer.reset();

        p1.setHp(p1.getMaxHp());
        p2.setHp(p2.getMaxHp());

        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

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