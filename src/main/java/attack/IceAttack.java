package attack;

public class IceAttack extends BaseAttack {
    public static final int COOLDOWN = 2;
    @Override public String getName()     { return "ICE"; }
    @Override public int    getCooldown() { return COOLDOWN; }
}
