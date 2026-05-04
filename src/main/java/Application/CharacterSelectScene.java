package Application;

import Charactor.BasePlayer;
import Charactor.CatPlayer;
import Charactor.DogPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CharacterSelectScene {

    private static final double CARD_W      = 130;
    private static final double CARD_H      = 160;
    private static final double SPRITE_SIZE = 80;

    private BasePlayer p1Selected = null;
    private BasePlayer p2Selected = null;

    private Button p1DogBtn, p1CatBtn, p2DogBtn, p2CatBtn;
    private final Button fightBtn = new Button("⚔  FIGHT!");

    public Scene build(Runnable onFight) {
        // ── Background ───────────────────────────────────────────────────────
        ImageView bg = new ImageView();
        try {
            var s = getClass().getResourceAsStream("/images/background.jpg");
            if (s != null) bg.setImage(new Image(s));
        } catch (Exception ignored) {}
        bg.setFitWidth(800);
        bg.setFitHeight(400);
        bg.setPreserveRatio(false);

        Region overlay = new Region();
        overlay.setPrefSize(800, 400);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        // ── Title ────────────────────────────────────────────────────────────
        Text titleShadow = new Text("SELECT YOUR CHARACTER");
        titleShadow.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleShadow.setFill(Color.color(0, 0, 0, 0.55));
        titleShadow.setTranslateX(2); titleShadow.setTranslateY(2);

        Text title = new Text("SELECT YOUR CHARACTER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.color(1.0, 0.95, 0.45)),
            new Stop(1.0, Color.color(1.0, 0.60, 0.15))));
        title.setEffect(new DropShadow(14, Color.color(1.0, 0.75, 0.1, 0.8)));

        StackPane titlePane = new StackPane(titleShadow, title);

        // ── Player panels ────────────────────────────────────────────────────
        VBox p1Panel = buildPlayerPanel(true);
        VBox p2Panel = buildPlayerPanel(false);

        // ── VS ───────────────────────────────────────────────────────────────
        Text vsShadow = new Text("VS");
        vsShadow.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        vsShadow.setFill(Color.color(0, 0, 0, 0.5));
        vsShadow.setTranslateX(2); vsShadow.setTranslateY(2);

        Text vs = new Text("VS");
        vs.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        vs.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.color(1.0, 0.92, 0.3)),
            new Stop(1.0, Color.color(1.0, 0.55, 0.1))));
        vs.setEffect(new DropShadow(16, Color.color(1.0, 0.75, 0.1, 0.9)));

        StackPane vsPane = new StackPane(vsShadow, vs);
        vsPane.setPadding(new Insets(0, 28, 0, 28));

        HBox center = new HBox(0, p1Panel, vsPane, p2Panel);
        center.setAlignment(Pos.CENTER);
        HBox.setHgrow(p1Panel, Priority.ALWAYS);
        HBox.setHgrow(p2Panel, Priority.ALWAYS);

        // ── Fight button ─────────────────────────────────────────────────────
        fightBtn.setPrefSize(200, 50);
        fightBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        fightBtn.setDisable(true);
        fightBtn.setStyle(fightDisabledStyle());
        fightBtn.setOnAction(e -> onFight.run());

        VBox content = new VBox(14, titlePane, center, fightBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(18, 30, 18, 30));
        content.setStyle("-fx-background-color: transparent;");

        StackPane root = new StackPane(bg, overlay, content);
        return new Scene(root, 800, 400);
    }

    private VBox buildPlayerPanel(boolean isP1) {
        // Player label
        Text labelShadow = new Text(isP1 ? "PLAYER 1" : "PLAYER 2");
        labelShadow.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        labelShadow.setFill(Color.color(0, 0, 0, 0.5));
        labelShadow.setTranslateX(1); labelShadow.setTranslateY(1);

        Text label = new Text(isP1 ? "PLAYER 1" : "PLAYER 2");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        label.setFill(isP1 ? Color.color(0.45, 0.75, 1.0) : Color.color(1.0, 0.45, 0.45));
        label.setEffect(new DropShadow(10,
            isP1 ? Color.color(0.3, 0.6, 1.0, 0.7) : Color.color(1.0, 0.3, 0.3, 0.7)));

        StackPane labelPane = new StackPane(labelShadow, label);

        Button dogCard = buildCharCard(true, isP1);
        Button catCard = buildCharCard(false, isP1);

        if (isP1) { p1DogBtn = dogCard; p1CatBtn = catCard; }
        else       { p2DogBtn = dogCard; p2CatBtn = catCard; }

        HBox cards = new HBox(14, dogCard, catCard);
        cards.setAlignment(Pos.CENTER);

        VBox box = new VBox(8, labelPane, cards);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        return box;
    }

    private Button buildCharCard(boolean isDog, boolean isP1) {
        String imgPath   = isDog ? "/images/dog.png" : "/images/cat.png";
        String charName  = isDog ? "Dog" : "Cat";
        String abilityTx = isDog ? "✚  Heal" : "◎  Perfect Aim";

        // Fixed-size sprite container so both cards are identical dimensions
        ImageView sprite = new ImageView();
        try {
            var s = getClass().getResourceAsStream(imgPath);
            if (s != null) sprite.setImage(new Image(s));
        } catch (Exception ignored) {}
        sprite.setFitWidth(SPRITE_SIZE);
        sprite.setFitHeight(SPRITE_SIZE);
        sprite.setPreserveRatio(false);
        if (!isP1) sprite.setScaleX(-1);

        StackPane spriteBox = new StackPane(sprite);
        spriteBox.setPrefSize(SPRITE_SIZE, SPRITE_SIZE);
        spriteBox.setMinSize(SPRITE_SIZE, SPRITE_SIZE);
        spriteBox.setMaxSize(SPRITE_SIZE, SPRITE_SIZE);

        Text name = new Text(charName);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        name.setFill(Color.WHITE);
        name.setEffect(new DropShadow(6, Color.color(0, 0, 0, 0.7)));

        Text ability = new Text(abilityTx);
        ability.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        ability.setFill(isDog ? Color.color(0.4, 1.0, 0.5) : Color.color(0.4, 0.85, 1.0));

        VBox content = new VBox(5, spriteBox, name, ability);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10, 12, 10, 12));
        content.setPrefSize(CARD_W, CARD_H);
        content.setMinSize(CARD_W, CARD_H);
        content.setMaxSize(CARD_W, CARD_H);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setPrefSize(CARD_W + 6, CARD_H + 6);
        btn.setMinSize(CARD_W + 6, CARD_H + 6);
        btn.setMaxSize(CARD_W + 6, CARD_H + 6);
        btn.setStyle(cardUnselected());
        btn.setOnAction(e -> selectChar(isDog, isP1));
        return btn;
    }

    private void selectChar(boolean isDog, boolean isP1) {
        if (isP1) {
            p1Selected = isDog ? new DogPlayer() : new CatPlayer();
            p1DogBtn.setStyle(isDog  ? cardSelected(true) : cardUnselected());
            p1CatBtn.setStyle(!isDog ? cardSelected(true) : cardUnselected());
        } else {
            p2Selected = isDog ? new DogPlayer() : new CatPlayer();
            p2DogBtn.setStyle(isDog  ? cardSelected(false) : cardUnselected());
            p2CatBtn.setStyle(!isDog ? cardSelected(false) : cardUnselected());
        }
        if (p1Selected != null && p2Selected != null) {
            fightBtn.setDisable(false);
            fightBtn.setStyle(fightReadyStyle());
        }
    }

    public BasePlayer getP1() { return p1Selected; }
    public BasePlayer getP2() { return p2Selected; }

    private static String cardUnselected() {
        return "-fx-background-color:rgba(15,25,40,0.72); -fx-border-color:#3a4a5c; "
             + "-fx-border-width:2; -fx-border-radius:10; -fx-background-radius:10; -fx-cursor:hand;";
    }

    private static String cardSelected(boolean isP1) {
        String glow = isP1 ? "#5599ff" : "#ff5555";
        return "-fx-background-color:rgba(30,50,70,0.85); -fx-border-color:" + glow + "; "
             + "-fx-border-width:3; -fx-border-radius:10; -fx-background-radius:10; -fx-cursor:hand; "
             + "-fx-effect:dropshadow(gaussian," + glow + ",16,0.6,0,0);";
    }

    private static String fightDisabledStyle() {
        return "-fx-background-color:rgba(50,50,50,0.8); -fx-text-fill:#777; "
             + "-fx-background-radius:10; -fx-border-radius:10;";
    }

    private static String fightReadyStyle() {
        return "-fx-background-color:#c0392b; -fx-text-fill:white; -fx-background-radius:10; "
             + "-fx-effect:dropshadow(gaussian,#ff6b6b,14,0.55,0,0); -fx-cursor:hand;";
    }
}
