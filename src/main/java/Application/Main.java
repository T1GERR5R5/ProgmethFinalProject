package Application;

import Charactor.BasePlayer;
import Charactor.DogPlayer;
import Charactor.CatPlayer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage  window;
    private Scene  menuScene;

    private BasePlayer p1;
    private BasePlayer p2;

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
        // ── Background ───────────────────────────────────────────────────────
        ImageView bg = new ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/background.jpg");
            if (stream != null) bg.setImage(new Image(stream));
        } catch (Exception ignored) {}
        bg.setFitWidth(800);
        bg.setFitHeight(400);
        bg.setPreserveRatio(false);

        Region overlay = new Region();
        overlay.setPrefSize(800, 400);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.38);");

        // ── Title ────────────────────────────────────────────────────────────
        Text titleShadow = new Text("AnimalWizard");
        titleShadow.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        titleShadow.setFill(Color.color(0, 0, 0, 0.5));
        titleShadow.setTranslateX(3);
        titleShadow.setTranslateY(3);

        Text title = new Text("AnimalWizard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        title.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.color(1.0, 0.95, 0.4)),
            new Stop(1.0, Color.color(1.0, 0.55, 0.1))));
        DropShadow glow = new DropShadow(20, Color.color(1.0, 0.75, 0.1, 0.9));
        title.setEffect(glow);

        StackPane titlePane = new StackPane(titleShadow, title);

        Text sub = new Text("Choose your fighter and battle!");
        sub.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sub.setFill(Color.color(0.9, 0.9, 0.9, 0.85));

        // ── Buttons ──────────────────────────────────────────────────────────
        Button startBtn = buildMenuButton("▶  Start Game",
            "-fx-background-color:#27ae60; -fx-text-fill:white;");
        startBtn.setOnAction(e -> showCharacterSelect());

        Button quitBtn = buildMenuButton("✕  Quit",
            "-fx-background-color:#7f1c1c; -fx-text-fill:white;");
        quitBtn.setOnAction(e -> window.close());

        VBox content = new VBox(14, titlePane, sub, startBtn, quitBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        StackPane root = new StackPane(bg, overlay, content);
        menuScene = new Scene(root, 800, 400);
        return menuScene;
    }

    private static Button buildMenuButton(String label, String colorStyle) {
        Button btn = new Button(label);
        btn.setPrefSize(190, 46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setStyle(colorStyle + " -fx-background-radius:8; -fx-cursor:hand;");
        DropShadow ds = new DropShadow(10, Color.color(0, 0, 0, 0.55));
        btn.setEffect(ds);
        return btn;
    }

    private void showCharacterSelect() {
        CharacterSelectScene selector = new CharacterSelectScene();
        window.setScene(selector.build(() -> {
            p1 = selector.getP1();
            p2 = selector.getP2();
            startGame();
        }));
    }

    private void startGame() {
        if (timer    != null) timer.stop();
        if (renderer != null) renderer.reset();

        p1.setHp(p1.getMaxHp());
        p2.setHp(p2.getMaxHp());

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
        // ── Background image ─────────────────────────────────────────────────
        ImageView bg = new ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/background.jpg");
            if (stream != null) bg.setImage(new Image(stream));
        } catch (Exception ignored) {}
        bg.setFitWidth(800);
        bg.setFitHeight(400);
        bg.setPreserveRatio(false);

        // Dark overlay so text pops
        Region overlay = new Region();
        overlay.setPrefSize(800, 400);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.52);");

        // ── Winner banner ────────────────────────────────────────────────────
        Text shadow = new Text(winner + " Wins!");
        shadow.setFont(Font.font("Arial", FontWeight.BOLD, 58));
        shadow.setFill(Color.color(0, 0, 0, 0.55));
        shadow.setTranslateX(3);
        shadow.setTranslateY(3);

        Text winText = new Text(winner + " Wins!");
        winText.setFont(Font.font("Arial", FontWeight.BOLD, 58));
        winText.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.color(1.0, 0.92, 0.3)),
            new Stop(1.0, Color.color(1.0, 0.55, 0.1))));
        DropShadow glow = new DropShadow(18, Color.color(1.0, 0.75, 0.1, 0.85));
        winText.setEffect(glow);

        StackPane banner = new StackPane(shadow, winText);

        // ── Subtitle ─────────────────────────────────────────────────────────
        Text sub = new Text("— BATTLE OVER —");
        sub.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        sub.setFill(Color.color(0.85, 0.85, 0.85, 0.9));

        // ── Buttons ──────────────────────────────────────────────────────────
        Button restartBtn = buildEndButton("▶  Rematch",
            "-fx-background-color:#c0392b; -fx-text-fill:white;");
        restartBtn.setOnAction(e -> {
            p1 = p1 instanceof DogPlayer ? new DogPlayer() : new CatPlayer();
            p2 = p2 instanceof DogPlayer ? new DogPlayer() : new CatPlayer();
            startGame();
        });

        Button selectBtn = buildEndButton("⚙  Change Characters",
            "-fx-background-color:#2980b9; -fx-text-fill:white;");
        selectBtn.setOnAction(e -> showCharacterSelect());

        Button menuBtn = buildEndButton("⌂  Main Menu",
            "-fx-background-color:#555; -fx-text-fill:white;");
        menuBtn.setOnAction(e -> window.setScene(menuScene));

        HBox buttons = new HBox(16, restartBtn, selectBtn, menuBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, banner, sub, buttons);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        StackPane root = new StackPane(bg, overlay, content);
        return new Scene(root, 800, 400);
    }

    private static Button buildEndButton(String label, String colorStyle) {
        Button btn = new Button(label);
        btn.setPrefSize(172, 42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle(colorStyle + " -fx-background-radius:8; -fx-cursor:hand;");
        DropShadow ds = new DropShadow(8, Color.color(0, 0, 0, 0.5));
        btn.setEffect(ds);
        return btn;
    }

    public static void main(String[] args) { launch(); }
}
