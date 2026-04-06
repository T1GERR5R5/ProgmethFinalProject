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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    Stage window;
    Scene menuScene, gameScene, endScene;

    double barWidth = 250;
    double barHeight = 20;

    Player1 p1 = new Player1();
    Player2 p2 = new Player2();

    AnimationTimer timer;

    @Override
    public void start(Stage stage) {
        window = stage;

        // ===== MENU =====
        Text title = new Text("2D Fighting Game");
        title.setFont(Font.font(40));

        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(150, 40);
        startBtn.setOnAction(e -> {
            resetGame();
            createGameScene();
            window.setScene(gameScene);
        });

        Button quitBtn = new Button("Quit Game");
        quitBtn.setPrefSize(150, 40);
        quitBtn.setOnAction(e -> window.close());

        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(title, startBtn, quitBtn);

        menuScene = new Scene(menuLayout, 800, 400);

        window.setScene(menuScene);
        window.setTitle("2D Fighting Game");
        window.show();
    }

    // ===== GAME SCENE =====
    void createGameScene() {
        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        gameScene = new Scene(root);

        HandleInput handleInput = new HandleInput(gameScene, p1, p2);
        handleInput.process();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                setUpBackground(gc);
                update(gc);

                // 🔥 CHECK GAME OVER
                if (p1.getHp() <= 0 || p2.getHp() <= 0) {
                    this.stop();

                    String winner = (p1.getHp() <= 0) ? "Player 2" : "Player 1";
                    createEndScene(winner);
                    window.setScene(endScene);
                }
            }
        };
        timer.start();
    }

    // ===== END SCENE =====
    void createEndScene(String winner) {
        Text resultText = new Text(winner + " Wins!");
        resultText.setFont(Font.font(40));

        Button restartBtn = new Button("Restart");
        restartBtn.setPrefSize(150, 40);
        restartBtn.setOnAction(e -> {
            resetGame();
            createGameScene();
            window.setScene(gameScene);
        });

        Button menuBtn = new Button("Back to Menu");
        menuBtn.setPrefSize(150, 40);
        menuBtn.setOnAction(e -> window.setScene(menuScene));

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(resultText, restartBtn, menuBtn);

        endScene = new Scene(layout, 800, 400);
    }

    // ===== RESET GAME =====
    void resetGame() {
        p1.setHp(p1.getMaxHp());
        p2.setHp(p2.getMaxHp());
    }

    // ===== DRAW BACKGROUND =====
    void setUpBackground(GraphicsContext gc) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 400);

        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 300, 800, 100);

        // Player 1
        gc.setFill(Color.RED);
        gc.fillRect(200, 250, 50, 50);
        gc.setFill(Color.GRAY);
        gc.fillRect(20, 20, barWidth, barHeight);

        // Player 2
        gc.setFill(Color.BLUE);
        gc.fillRect(550, 250, 50, 50);
        gc.setFill(Color.GRAY);
        gc.fillRect(800 - barWidth - 20, 20, barWidth, barHeight);
    }

    // ===== UPDATE HP BAR =====
    void update(GraphicsContext gc) {
        // P1 HP
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(20, 20,
                (p1.getHp() / (double) p1.getMaxHp()) * barWidth,
                barHeight);

        // P2 HP
        double p2Width = (p2.getHp() / (double) p2.getMaxHp()) * barWidth;
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(800 - 20 - p2Width, 20, p2Width, barHeight);
    }

    public static void main(String[] args) {
        launch();
    }
}