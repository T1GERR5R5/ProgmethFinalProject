package Application;

import AttackLogic.*;
import Charactor.BasePlayer;
import Charactor.Player1;
import Charactor.Player2;
import javafx.scene.Scene;

public class HandleInput {
    Scene scene;
    Player1 player1;
    Player2 player2;
    BasePlayer goal;
    String attacker;

    boolean player1State;

    public HandleInput(Scene scene, Player1 player1, Player2 player2) {
        this.scene = scene;
        this.player1 = player1;
        this.player2 = player2;
        this.player1State = true;
    }

    public void process() {

        scene.setOnKeyPressed(event -> {

            Attackable attack = null;

            if (player1State) {
                this.goal = player2;
                switch (event.getCode()) {
                    case A -> attack = new NormalAttack();
                    case S -> attack = new FireAttack();
                    //case D -> attack = new IceAttack();
                }
                if (attack!=null){player1State = false;}
            } else {
                this.goal = player1;
                switch (event.getCode()) {
                    case J -> attack = new NormalAttack();
                    case K -> attack = new FireAttack();
                    //case L -> attack = new IceAttack();

                }
                if (attack!=null){player1State = true;}
            }
            attack.attack(goal);
            if (goal == player1){attacker = "player2";}
            else {attacker = "player1";}
            System.out.println(attacker + " used " + attack.getClass().getSimpleName());
        });
    }
}