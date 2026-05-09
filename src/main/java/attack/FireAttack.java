package attack;

public class FireAttack extends BaseAttack {
    public static final int COOLDOWN = 3;
    @Override public String getName()     { return "FIRE"; }
    @Override public int    getCooldown() { return COOLDOWN; }
}
