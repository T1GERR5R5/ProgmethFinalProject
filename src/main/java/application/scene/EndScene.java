package application.scene;

import application.Main;
import character.BasePlayer;
import game.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

public class EndScene {
    private BasePlayer p1;
    private BasePlayer p2;

    public Scene buildEndScene(String winner,BasePlayer p1,BasePlayer p2) {
        // ── Background image ─────────────────────────────────────────────────
        this.p1 = p1;
        this.p2 = p2;
        ImageView bg = new ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/background.jpg");
            if (stream != null) bg.setImage(new Image(stream));
        } catch (Exception ignored) {}
        bg.setFitWidth(800);
        bg.setFitHeight(400);
        bg.setPreserveRatio(false);
        SoundManager.stopAllSFX();
        SoundManager.playLobbyBGM();

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
            this.p1 = this.p1.recreate();
            this.p2 = this.p2.recreate();
            Main.window.setScene(new GamePlayScene().startGame(this.p1,this.p2));
        });

        Button selectBtn = buildEndButton("⚙  Change Characters",
                "-fx-background-color:#2980b9; -fx-text-fill:white;");
        selectBtn.setOnAction(e -> Main.window.setScene(new CharacterSelectScene().build()));

        Button menuBtn = buildEndButton("⌂  Main Menu",
                "-fx-background-color:#555; -fx-text-fill:white;");
        menuBtn.setOnAction(e -> Main.window.setScene(new StartScene().build()));

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


}
