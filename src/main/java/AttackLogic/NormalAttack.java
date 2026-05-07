package AttackLogic;

import Charactor.BasePlayer;

public class NormalAttack implements Attackable {

    @Override
    public void attack(BasePlayer enemy) {
        enemy.decreaseHp(1);
    }
}
