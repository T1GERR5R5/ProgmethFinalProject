package AttackLogic;

import Application.HandleInput;
import Charactor.BasePlayer;
import Charactor.Player2;
public class FireAttack implements Attackable{
    @Override
    public void attack(BasePlayer enemy){
        enemy.decreaseHp(2);
    }
}
