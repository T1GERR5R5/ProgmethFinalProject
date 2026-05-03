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

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    Stage window;
    Scene menuScene, gameScene, endScene;

    double barWidth = 250, barHeight = 20;

    Player1 p1 = new Player1();
    Player2 p2 = new Player2();

    AnimationTimer timer;
    Controller controller;

    String lastProjectileResult = "";
    int resultDisplayTimer = 0;

    // Particle lists — [x, y, vx, vy, life, maxLife]
    List<double[]> fireParticles = new ArrayList<>();
    List<double[]> iceParticles  = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        window = stage;

        Text title = new Text("2D Fighting Game");
        title.setFont(Font.font(40));

        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(150, 40);
        startBtn.setOnAction(e -> { resetGame(); createGameScene(); window.setScene(gameScene); });

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

    void createGameScene() {
        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        gameScene = new Scene(root);

        controller = new Controller(p1, p2);
        new HandleInput(gameScene, controller).process();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                controller.update();

                String result = controller.getAndClearProjectileResult();
                if (!result.isEmpty()) {
                    lastProjectileResult = result;
                    resultDisplayTimer   = 100;
                }

                setUpBackground(gc);
                update(gc);

                if (p1.getHp() <= 0 || p2.getHp() <= 0) {
                    this.stop();
                    createEndScene(p1.getHp() <= 0 ? "Player 2" : "Player 1");
                    window.setScene(endScene);
                }
            }
        };
        timer.start();
    }

    void createEndScene(String winner) {
        Text resultText = new Text(winner + " Wins!");
        resultText.setFont(Font.font(40));

        Button restartBtn = new Button("Restart");
        restartBtn.setPrefSize(150, 40);
        restartBtn.setOnAction(e -> { resetGame(); createGameScene(); window.setScene(gameScene); });

        Button menuBtn = new Button("Back to Menu");
        menuBtn.setPrefSize(150, 40);
        menuBtn.setOnAction(e -> window.setScene(menuScene));

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(resultText, restartBtn, menuBtn);
        endScene = new Scene(layout, 800, 400);
    }

    void resetGame() {
        p1.setHp(p1.getMaxHp());
        p2.setHp(p2.getMaxHp());
        lastProjectileResult = "";
        resultDisplayTimer   = 0;
        fireParticles.clear();
        iceParticles.clear();
    }

    // Returns canvas X of character's sprite origin (P1=100, P2=620)
    private double charX(int playerNum) { return playerNum == 1 ? 100 : 620; }

    void setUpBackground(GraphicsContext gc) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 400);
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 300, 800, 100);

        if (p1.getSprite() != null) gc.drawImage(p1.getSprite(), 100, 240, 80, 80);
        else { gc.setFill(Color.RED);  gc.fillRect(100, 250, 50, 50); }

        if (p2.getSprite() != null) gc.drawImage(p2.getSprite(), 620, 240, 80, 80);
        else { gc.setFill(Color.BLUE); gc.fillRect(620, 250, 50, 50); }

        gc.setFill(Color.GRAY);
        gc.fillRect(20, 20, barWidth, barHeight);
        gc.fillRect(800 - barWidth - 20, 20, barWidth, barHeight);
    }

    void update(GraphicsContext gc) {
        // ── HP bars ──────────────────────────────────────────────────────────
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(20, 20, (p1.getHp() / (double) p1.getMaxHp()) * barWidth, barHeight);
        double p2w = (p2.getHp() / (double) p2.getMaxHp()) * barWidth;
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(800 - 20 - p2w, 20, p2w, barHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.fillText("P1  " + p1.getHp() + "/" + p1.getMaxHp(), 22, 52);
        gc.fillText("P2  " + p2.getHp() + "/" + p2.getMaxHp(), 800 - 100, 52);

        // ── Turn indicator ────────────────────────────────────────────────────
        gc.setFont(Font.font(15));
        boolean frozenIsNow = controller.getFrozenPlayer() != 0 &&
                controller.getFrozenPlayer() == (controller.isPlayer1Turn() ? 1 : 2);
        if (frozenIsNow) {
            gc.setFill(Color.CYAN);
            String fp = "P" + controller.getFrozenPlayer();
            gc.fillText("[ " + fp + " - FROZEN ]", 325, 52);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillText(controller.isPlayer1Turn() ? "[ P1 TURN ]" : "[ P2 TURN ]", 350, 52);
        }

        // ── Controls hint (context-sensitive) ────────────────────────────────
        String selName   = controller.getSelectedAttackName();
        Projectile.State projState = controller.getProjectile().getState();

        if (frozenIsNow) {
            gc.setFont(Font.font(11));
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Turn is being skipped...", 320, 78);
        } else if (projState == Projectile.State.FLYING) {
            gc.setFont(Font.font(12));
            gc.setFill(Color.YELLOW);
            gc.fillText("[ " + selName + " ] projectile in flight...", 305, 78);
        } else if (!selName.isEmpty()) {
            // Attack chosen — prompt to aim
            Color selColor = selName.equals("FIRE") ? Color.ORANGERED
                           : selName.equals("ICE")  ? Color.CYAN
                           : Color.WHITE;
            gc.setFont(Font.font(13));
            gc.setFill(selColor);
            gc.fillText("[ " + selName + " ] selected  →  SPACE to aim", 255, 78);
        } else {
            // Nothing selected yet
            gc.setFont(Font.font(11));
            gc.setFill(Color.LIGHTYELLOW);
            if (controller.isPlayer1Turn())
                gc.fillText("Select attack:  A = Normal   S = Fire   D = Ice", 190, 78);
            else
                gc.fillText("Select attack:  J = Normal   K = Fire   L = Ice", 430, 78);
        }

        // ── Selected attack badge above the attacking character ───────────────
        if (!selName.isEmpty() && projState != Projectile.State.FLYING) {
            double badgeX = controller.isPlayer1Turn() ? 104 : 618;
            Color bc = selName.equals("FIRE") ? Color.ORANGERED
                     : selName.equals("ICE")  ? Color.CYAN
                     : Color.WHITE;
            gc.setFill(bc);
            gc.setFont(Font.font(13));
            gc.fillText("[ " + selName + " ]", badgeX, 226);
        }

        // ── Ice overlay + animation ──────────────────────────────────────────
        if (controller.getFrozenPlayer() != 0) {
            drawIceEffect(gc);
        }

        // ── Fire glow base ───────────────────────────────────────────────────
        if (controller.getBurnTargetPlayer() != 0) {
            drawFireGlow(gc);
        }

        // ── Emit & update particles ───────────────────────────────────────────
        emitFireParticles();
        emitIceParticles();
        updateAndDrawParticles(gc);

        // ── Status badges over characters ─────────────────────────────────────
        if (controller.getBurnTargetPlayer() != 0) {
            double cx = charX(controller.getBurnTargetPlayer());
            gc.setFill(Color.ORANGERED);
            gc.setFont(Font.font(13));
            gc.fillText("BURNING! (" + controller.getBurnTicksLeft() + ")", cx - 2, 226);
        }

        // ── Projectile system ─────────────────────────────────────────────────
        Projectile proj = controller.getProjectile();
        Projectile.State state = proj.getState();
        if (state == Projectile.State.AIMING) drawAiming(gc, proj);
        else if (state == Projectile.State.FLYING) drawFlying(gc, proj);

        // ── Hit/Miss flash ────────────────────────────────────────────────────
        if (resultDisplayTimer > 0) {
            resultDisplayTimer--;
            gc.setFont(Font.font(26));
            gc.setFill(lastProjectileResult.contains("HIT") ? Color.YELLOW : Color.ORANGERED);
            gc.fillText(lastProjectileResult, 270, 195);
        }
    }

    // ── Fire ─────────────────────────────────────────────────────────────────

    private void drawFireGlow(GraphicsContext gc) {
        double cx = charX(controller.getBurnTargetPlayer());
        int burnTimer = controller.getBurnFrameTimer();
        double pulse = 0.35 + 0.2 * Math.sin(burnTimer * 0.25);
        gc.setFill(Color.color(1.0, 0.35, 0.0, pulse));
        gc.fillOval(cx + 5, 295, 70, 18); // glow at feet
    }

    private void emitFireParticles() {
        if (controller.getBurnTargetPlayer() == 0) return;
        double cx = charX(controller.getBurnTargetPlayer());
        for (int i = 0; i < 4; i++) {
            double px  = cx + 8 + Math.random() * 64;
            double py  = 255 + Math.random() * 50;
            double pvx = (Math.random() - 0.5) * 1.5;
            double pvy = -1.5 - Math.random() * 2.0;
            double life = 35 + Math.random() * 20;
            fireParticles.add(new double[]{px, py, pvx, pvy, life, life});
        }
    }

    // ── Ice ──────────────────────────────────────────────────────────────────

    private void drawIceEffect(GraphicsContext gc) {
        int fp = controller.getFrozenPlayer();
        double cx = charX(fp) + 40;
        double cy = 280;
        int timer    = controller.getFrozenDisplayTimer();
        double progress = 1.0 - (timer / (double) Controller.FROZEN_DISPLAY_FRAMES);

        // Blue overlay on character
        gc.setFill(Color.color(0.2, 0.6, 1.0, 0.2 + progress * 0.3));
        gc.fillRect(charX(fp) - 2, 238, 84, 84);

        // Crystal lines growing outward from center
        gc.setStroke(Color.color(0.75, 0.93, 1.0, 0.85));
        gc.setLineWidth(1.5);
        double maxLen = 30 * progress;
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(i * 45);
            gc.strokeLine(cx, cy, cx + Math.cos(a) * maxLen, cy + Math.sin(a) * maxLen);
        }

        // Full snowflake once half-frozen
        if (progress > 0.5) {
            gc.setStroke(Color.color(0.9, 1.0, 1.0, 0.95));
            gc.setLineWidth(2);
            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(i * 60);
                gc.strokeLine(cx, cy, cx + Math.cos(a) * 13, cy + Math.sin(a) * 13);
            }
        }

        // "FROZEN!" and countdown
        gc.setFill(Color.color(0.55, 0.9, 1.0));
        gc.setFont(Font.font(13));
        gc.fillText("FROZEN!", charX(fp) + 12, 226);
        int secsLeft = (int) Math.ceil(timer / 60.0);
        gc.fillText("Skip in " + secsLeft + "s", charX(fp) + 8, 362);
    }

    private void emitIceParticles() {
        if (controller.getFrozenPlayer() == 0) return;
        double cx = charX(controller.getFrozenPlayer()) + 40;
        double cy = 280;
        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.0 + Math.random() * 2.5;
            double life  = 20 + Math.random() * 15;
            iceParticles.add(new double[]{
                cx + Math.cos(angle) * 12,
                cy + Math.sin(angle) * 12,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed - 0.8,
                life, life
            });
        }
    }

    // ── Particle update + draw ────────────────────────────────────────────────

    private void updateAndDrawParticles(GraphicsContext gc) {
        // Update fire particles
        for (double[] p : fireParticles) {
            p[0] += p[2]; p[1] += p[3];
            p[3] += 0.05; // slight gravity
            p[4]--;
        }
        fireParticles.removeIf(p -> p[4] <= 0);

        // Draw fire particles
        for (double[] p : fireParticles) {
            double t = p[4] / p[5];
            double r = 2 + t * 5;
            Color c = t > 0.65 ? Color.color(1.0, 0.95, 0.0, t * 0.85)   // yellow
                    : t > 0.35 ? Color.color(1.0, 0.50, 0.0, t * 0.85)   // orange
                               : Color.color(0.9, 0.10, 0.0, t * 0.85);  // red
            gc.setFill(c);
            gc.fillOval(p[0] - r, p[1] - r, r * 2, r * 2);
        }

        // Update ice particles
        for (double[] p : iceParticles) {
            p[0] += p[2]; p[1] += p[3];
            p[3] += 0.03;
            p[4]--;
        }
        iceParticles.removeIf(p -> p[4] <= 0);

        // Draw ice particles
        for (double[] p : iceParticles) {
            double t = p[4] / p[5];
            double r = 1.5 + t * 3;
            gc.setFill(Color.color(0.65 + t * 0.35, 0.88 + t * 0.12, 1.0, t * 0.9));
            gc.fillOval(p[0] - r, p[1] - r, r * 2, r * 2);
        }
    }

    // ── Projectile ───────────────────────────────────────────────────────────

    private void drawAiming(GraphicsContext gc, Projectile proj) {
        boolean p1Turn = proj.isPlayer1Turn();
        double sx  = p1Turn ? Projectile.P1_START_X : Projectile.P2_START_X;
        double sy  = p1Turn ? Projectile.P1_START_Y : Projectile.P2_START_Y;
        double rad = Math.toRadians(proj.getAngle());
        double dirX = p1Turn ? Math.cos(rad) : -Math.cos(rad);
        double dirY = -Math.sin(rad);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(sx, sy, sx + dirX * 55, sy + dirY * 55);
        gc.setFill(Color.WHITE);
        gc.fillOval(sx + dirX * 55 - 4, sy + dirY * 55 - 4, 8, 8);

        double[][] pts = proj.getTrajectoryPoints(22);
        for (int i = 0; i < pts.length; i++) {
            double px = pts[i][0], py = pts[i][1];
            if (px < 0 || px > 800 || py > 308) break;
            boolean onTarget = isInTargetBox(px, py, p1Turn);
            double alpha = 0.9 - (i / 22.0) * 0.55;
            double r = 5 - (i / 22.0) * 2;
            gc.setFill(onTarget ? Color.color(0.1, 1.0, 0.1, alpha)
                                : Color.color(1.0, 0.55, 0.0, alpha));
            gc.fillOval(px - r, py - r, r * 2, r * 2);
        }

        gc.setStroke(Color.color(1, 1, 0, 0.55));
        gc.setLineWidth(2);
        if (p1Turn)
            gc.strokeRect(Projectile.P2_BOX_X1, Projectile.BOX_Y1,
                          Projectile.P2_BOX_X2 - Projectile.P2_BOX_X1,
                          Projectile.BOX_Y2 - Projectile.BOX_Y1);
        else
            gc.strokeRect(Projectile.P1_BOX_X1, Projectile.BOX_Y1,
                          Projectile.P1_BOX_X2 - Projectile.P1_BOX_X1,
                          Projectile.BOX_Y2 - Projectile.BOX_Y1);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(13));
        gc.fillText(String.format("Angle: %.0f°", proj.getAngle()), p1Turn ? 105 : 535, 236);
        gc.fillText("SPACE to lock & fire!", p1Turn ? 105 : 535, 358);
    }

    private void drawFlying(GraphicsContext gc, Projectile proj) {
        List<double[]> trail = proj.getTrail();
        for (int i = 0; i < trail.size(); i++) {
            double a = (i + 1.0) / (trail.size() + 1.0);
            double r = 3 + a * 4;
            gc.setFill(Color.color(1.0, 0.4, 0.0, a * 0.65));
            gc.fillOval(trail.get(i)[0] - r, trail.get(i)[1] - r, r * 2, r * 2);
        }
        gc.setFill(Color.ORANGERED);
        gc.fillOval(proj.getX() - 9, proj.getY() - 9, 18, 18);
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1.5);
        gc.strokeOval(proj.getX() - 9, proj.getY() - 9, 18, 18);
    }

    private boolean isInTargetBox(double px, double py, boolean p1Turn) {
        if (p1Turn)
            return px >= Projectile.P2_BOX_X1 && px <= Projectile.P2_BOX_X2
                && py >= Projectile.BOX_Y1    && py <= Projectile.BOX_Y2;
        else
            return px >= Projectile.P1_BOX_X1 && px <= Projectile.P1_BOX_X2
                && py >= Projectile.BOX_Y1    && py <= Projectile.BOX_Y2;
    }

    public static void main(String[] args) { launch(); }
}
