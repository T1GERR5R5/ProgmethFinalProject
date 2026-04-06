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
    // ย้ายตัวแปรที่ต้องใช้บ่อยๆ มาเป็น Field
    Stage window;
    Scene menuScene, gameScene;

    double barWidth = 250;
    double barHeight = 20;
    Player1 p1 = new Player1();
    Player2 p2 = new Player2();

    @Override
    public void start(Stage stage) {
        window = stage;

        // --- 1. สร้างหน้า Menu ---
        Text title = new Text("2D Fighting Game");
        title.setFont(Font.font(40));

        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(150, 40);
        startBtn.setOnAction(e -> {
            createGameScene(); // สร้าง Game Scene เมื่อกดเริ่ม
            window.setScene(gameScene);
        });

        Button quitBtn = new Button("Quit Game");
        quitBtn.setPrefSize(150, 40);
        quitBtn.setOnAction(e -> window.close()); // ปิดโปรแกรม

        VBox menuLayout = new VBox(20); // เว้นระยะห่างระหว่างปุ่ม 20px
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(title, startBtn, quitBtn);

        menuScene = new Scene(menuLayout, 800, 400);

        // --- 2. ตั้งค่าเริ่มต้น ---
        window.setScene(menuScene); // เริ่มที่หน้า Menu
        window.setTitle("2D Fighting Game");
        window.show();
    }

    // แยก Logic การสร้างฉากเกมออกมา
    void createGameScene() {
        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        gameScene = new Scene(root);

        // ส่งฉากใหม่ไปให้ HandleInput
        HandleInput handleInput = new HandleInput(gameScene, p1, p2);
        handleInput.process();

        new AnimationTimer() {
            public void handle(long now) {
                setUpBackground(gc);
                update(gc);
            }
        }.start();
    }

    // เมธอด setUpBackground และ update ใช้โค้ดเดิมของคุณได้เลย
    void setUpBackground(GraphicsContext gc) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 400);
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 300, 800, 100);

        gc.setFill(Color.RED);
        gc.fillRect(200, 250, 50, 50);
        gc.setFill(Color.GRAY);
        gc.fillRect(20, 20, barWidth, barHeight);

        gc.setFill(Color.BLUE);
        gc.fillRect(550, 250, 50, 50);
        gc.setFill(Color.GRAY);
        gc.fillRect(800 - barWidth - 20, 20, barWidth, barHeight);
    }

    void update(GraphicsContext gc){
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(20, 20, (p1.getHp() / (double) p1.getMaxHp()) * barWidth, barHeight);

        double p2Width = (p2.getHp() / (double) p2.getMaxHp()) * barWidth;
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(800 - 20 - p2Width, 20, p2Width, barHeight);
    }

    public static void main(String[] args) {
        launch();
    }
}