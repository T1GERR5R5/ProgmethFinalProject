package attack;

import character.BasePlayer;

public interface Attackable {
    void   attack(BasePlayer enemy);
    String getName();
    int    getCooldown();
}
