import Charactor.BasePlayer;
import Charactor.Player1;
import Charactor.Player2;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    double barWidth = 250;
    double barHeight = 20;
    Player1 p1 = new Player1();
    Player2 p2 = new Player2();

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        HandleInput handleInput = new HandleInput(scene,p1,p2);

        handleInput.process();

        new AnimationTimer() {
            public void handle(long now) {
                setUpBackground(gc);
                update(gc);
            }
        }.start();

        stage.setScene(scene);
        stage.setTitle("2D Fighting Game");
        stage.show();
    }
    void setUpBackground(GraphicsContext gc) {
        // Background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 400);

        // Ground
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 300, 800, 100);

        double barWidth = 250;
        double barHeight = 20;

        // Players1
        gc.setFill(Color.RED);
        gc.fillRect(200, 250, 50, 50);
        gc.setFill(Color.GRAY); // background
        gc.fillRect(20, 20, barWidth, barHeight);

        //Players2
        gc.setFill(Color.BLUE);
        gc.fillRect(550, 250, 50, 50);
        gc.setFill(Color.GRAY); // background
        gc.fillRect(800 - barWidth - 20, 20, barWidth, barHeight);
    }
    void update(GraphicsContext gc){
        // Players1
        gc.setFill(Color.LIMEGREEN); // HP
        gc.fillRect(20, 20, (p1.getHp() / (double) p1.getMaxHp()) * barWidth, barHeight);

        //Players2
        double p2Width = (p2.getHp() / (double) p2.getMaxHp()) * barWidth;
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(800 - 20 - p2Width, 20, p2Width, barHeight);
    }
    public static void main(String[] args) {
        launch();
    }
}