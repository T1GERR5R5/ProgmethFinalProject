package attack;

public class WindAttack extends BaseAttack {
    public static final int COOLDOWN = 2;
    @Override public String getName()     { return "WIND"; }
    @Override public int    getCooldown() { return COOLDOWN; }
}
