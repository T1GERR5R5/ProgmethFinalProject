package Application;

import AttackLogic.FireAttack;
import AttackLogic.NormalAttack;
import Charactor.Player1;
import Charactor.Player2;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class HandleInput {
    Scene scene;
    Player1 player1;
    Player2 player2;
    NormalAttack normalAttack;
    FireAttack fireAttack;
    boolean player1State;

    public HandleInput(Scene scene, Player1 player1, Player2 player2) {
        this.scene = scene;
        this.player1 = player1;
        this.player2 = player2;
        this.normalAttack = new NormalAttack();
        this.fireAttack = new FireAttack();
        this.player1State = true;
    }

    public void process() {

        scene.setOnKeyPressed(event -> {
            if(player1State){
                if(event.getCode() == KeyCode.A){
                    normalAttack.attack(player2);
                    System.out.println("Normal Attack to player2");
                    player1State = false;
                }
                else if (event.getCode() == KeyCode.S) {
                    fireAttack.attack(player2);
                    System.out.println("Fire Attack to player2");
                    player1State = false;
                }
                else if (event.getCode() == KeyCode.D){
                    normalAttack.attack(player2);
                    System.out.println("Ice Attack to player2");
                }
            }
            else {
                if(event.getCode() == KeyCode.J){
                    normalAttack.attack(player1);
                    System.out.println("Normal Attack to player1");
                    player1State = true;
                }
                else if (event.getCode() == KeyCode.K) {
                    fireAttack.attack(player1);
                    System.out.println("Fire Attack to player1");
                    player1State = true;
                }
                else if (event.getCode() == KeyCode.L){
                    normalAttack.attack(player1);
                    System.out.println("Ice Attack to player1");
                }
            }
        });
    }
}