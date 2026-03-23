import Charactor.Player1;
import Charactor.Player2;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class HandleInput {
    Scene scene;
    Player1 player1;
    Player2 player2;
    private boolean isPlayer1Turn = true;

    public HandleInput(Scene scene, Player1 player1, Player2 player2) {
        this.scene = scene;
        this.player1 = player1;
        this.player2 = player2;
    }

    public void process() {

        scene.setOnKeyPressed(event -> {

            // Player 1 turn
            if (event.getCode() == KeyCode.W && isPlayer1Turn) {
                player2.decreaseHp(1); // P1 attacks P2
                System.out.println("Player 1 attacks!");
                isPlayer1Turn = false; // 🔄 switch turn
            }

            // Player 2 turn
            if (event.getCode() == KeyCode.O && !isPlayer1Turn) {
                player1.decreaseHp(1); // P2 attacks P1
                System.out.println("Player 2 attacks!");
                isPlayer1Turn = true; // 🔄 switch turn
            }
        });
    }
}