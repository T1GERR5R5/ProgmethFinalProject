package Application;

import AttackLogic.*;
import Charactor.BasePlayer;
import Charactor.Player1;
import Charactor.Player2;

public class Controller {
    private Player1 p1;
    private Player2 p2;
    private boolean player1Turn = true;
    private boolean isFrozen = false;

    public Controller(Player1 p1, Player2 p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void executeAttack(Attackable attack) {
        if (attack == null) return;

        // เช็กเงื่อนไขแช่แข็ง: ถ้า Frozen อยู่ ต้องเป็น NormalAttack เท่านั้น
        if (isFrozen && !(attack instanceof NormalAttack)) {
            System.out.println("Blocked! Must use Normal Attack.");
            return;
        }

        BasePlayer target = player1Turn ? p2 : p1;
        attack.attack(target);

        System.out.println((player1Turn ? "P1" : "P2") + " used " + attack.getClass().getSimpleName());

        // จัดการสถานะแช่แข็งและเทิร์น
        if (attack instanceof IceAttack) {
            isFrozen = true;
            // ไม่สลับเทิร์น (player1Turn คงเดิม)
            System.out.println("Target is Frozen! Extra turn.");
        } else {
            isFrozen = false;
            player1Turn = !player1Turn; // สลับเทิร์น
        }
    }

    public boolean isPlayer1Turn() { return player1Turn; }
}