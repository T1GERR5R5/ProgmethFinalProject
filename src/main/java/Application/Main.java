package Application;

import Charactor.Player1;
import Charactor.Player2;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage  window;
    private Scene  menuScene;

    private Player1 p1 = new Player1();
    private Player2 p2 = new Player2();

    private AnimationTimer timer;
    private Controller     controller;
    private GameRenderer   renderer;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("2D Fighting Game");
        window.setScene(buildMenuScene());
        window.show();
    }

    private Scene buildMenuScene() {
        Text title = new Text("2D Fighting Game");
        title.setFont(Font.font(40));

        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(150, 40);
        startBtn.setOnAction(e -> startGameScene());

        Button quitBtn = new Button("Quit Game");
        quitBtn.setPrefSize(150, 40);
        quitBtn.setOnAction(e -> window.close());

        VBox layout = new VBox(20, title, startBtn, quitBtn);
        layout.setAlignment(Pos.CENTER);
        menuScene = new Scene(layout, 800, 400);
        return menuScene;
    }

    private void startGameScene() {
        resetGame();

        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        controller = new Controller(p1, p2);
        renderer   = new GameRenderer(gc, controller, p1, p2);

        Scene gameScene = new Scene(new StackPane(canvas));
        new HandleInput(gameScene, controller).process();
        window.setScene(gameScene);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                controller.update();
                renderer.onResult(controller.getAndClearProjectileResult());
                renderer.render();

                if (p1.getHp() <= 0 || p2.getHp() <= 0) {
                    stop();
                    window.setScene(buildEndScene(p1.getHp() <= 0 ? "Player 2" : "Player 1"));
                }
            }
        };
        timer.start();
    }

    private Scene buildEndScene(String winner) {
        Text resultText = new Text(winner + " Wins!");
        resultText.setFont(Font.font(40));

        Button restartBtn = new Button("Restart");
        restartBtn.setPrefSize(150, 40);
        restartBtn.setOnAction(e -> startGameScene());

        Button menuBtn = new Button("Back to Menu");
        menuBtn.setPrefSize(150, 40);
        menuBtn.setOnAction(e -> window.setScene(menuScene));

        VBox layout = new VBox(20, resultText, restartBtn, menuBtn);
        layout.setAlignment(Pos.CENTER);
        return new Scene(layout, 800, 400);
    }

    private void resetGame() {
        p1.setHp(p1.getMaxHp());
        p2.setHp(p2.getMaxHp());
        if (renderer != null) renderer.reset();
        if (timer    != null) timer.stop();
    }

    public static void main(String[] args) { launch(); }
}
