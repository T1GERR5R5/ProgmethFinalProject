package application.scene;

import application.Main;
import game.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static game.SoundManager.playLobbyBGM;

/**
 * The main-menu scene shown when the application starts.
 *
 * <p>Displays the game title, a subtitle, and two buttons:
 * <ul>
 *   <li><b>Start Game</b> — navigates to {@link CharacterSelectScene}</li>
 *   <li><b>Quit</b> — closes the application window</li>
 * </ul>
 * The lobby BGM is started when the scene is built.
 */
public class StartScene {
    /** The background image view (public for potential external access). */
    public ImageView bg;

    /**
     * Constructs and returns the main-menu {@link javafx.scene.Scene}.
     * @return the fully assembled 800×400 Scene
     */
    public Scene build() {
        bg = new ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/background.jpg");
            if (stream != null) bg.setImage(new Image(stream));
        } catch (Exception ignored) {}
        bg.setFitWidth(800);
        bg.setFitHeight(400);
        bg.setPreserveRatio(false);
        SoundManager.playLobbyBGM();

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

        startBtn.setOnAction(e -> Main.window.setScene(new CharacterSelectScene().build()));

        Button quitBtn = buildMenuButton("✕  Quit",
                "-fx-background-color:#7f1c1c; -fx-text-fill:white;");
        quitBtn.setOnAction(e -> Main.window.close());

        VBox content = new VBox(14, titlePane, sub, startBtn, quitBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        StackPane root = new StackPane(bg, overlay, content);
        Scene startScene = new Scene(root, 800, 400);
        return startScene;
    }

    /**
     * Creates a uniformly styled main-menu button.
     * @param label      button text
     * @param colorStyle inline JavaFX CSS for background and text colour
     * @return the configured {@link Button}
     */
    private static Button buildMenuButton(String label, String colorStyle) {
        Button btn = new Button(label);
        btn.setPrefSize(190, 46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setStyle(colorStyle + " -fx-background-radius:8; -fx-cursor:hand;");
        DropShadow ds = new DropShadow(10, Color.color(0, 0, 0, 0.55));
        btn.setEffect(ds);
        return btn;
    }
}
