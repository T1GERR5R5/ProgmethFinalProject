package game;

import attack.*;
import character.AbilityType;
import character.BasePlayer;

import java.util.HashMap;
import java.util.Map;

public class Controller {
    private final BasePlayer p1;
    private final BasePlayer p2;
    private boolean    player1Turn     = true;
    private Projectile projectile      = new Projectile();
    private String     projectileResult = "";

    private Attackable selectedAttack = null;

    // ── Burn (FireAttack): 1 HP per tick ────────────────────────────────────
    private int burnTargetPlayer = 0;
    private int burnTicksLeft    = 0;
    private int burnFrameTimer   = 0;
    public static final int BURN_TICKS           = 2;
    public static final int BURN_FRAMES_PER_TICK = 60;

    // ── Freeze (IceAttack): target's next turn is auto-skipped ──────────────
    private int frozenPlayer       = 0;
    private int frozenDisplayTimer = 0;
    public static final int FROZEN_DISPLAY_FRAMES = 120;

    // ── Cooldowns: [0]=P1, [1]=P2, keyed by attack name ─────────────────────
    private final Map<String, int[]> cooldowns = new HashMap<>();

    // ── Wind (WindAttack): target oscillates for WIND_TURNS turns ────────────
    public static final int WIND_TURNS = 1;
    private int windTargetPlayer = 0;
    private int windTurnsLeft    = 0;
    private int windFrameCounter = 0;

    // ── Ability cooldown ─────────────────────────────────────────────────────
    public static final int ABILITY_COOLDOWN = 4;
    private int[] abilityCooldown = {0, 0};

    public Controller(BasePlayer p1, BasePlayer p2) {
        this.p1 = p1;
        this.p2 = p2;
        cooldowns.put("FIRE", new int[]{0, 0});
        cooldowns.put("ICE",  new int[]{0, 0});
        cooldowns.put("WIND", new int[]{0, 0});
    }

    public boolean isCurrentPlayerFrozen() {
        return frozenPlayer != 0 && frozenPlayer == (player1Turn ? 1 : 2);
    }

    public void selectAttack(Attackable attack) {
        if (attack == null) return;
        if (isCurrentPlayerFrozen()) return;
        if (projectile.getState() == Projectile.State.FLYING) return;

        int   idx = player1Turn ? 0 : 1;
        int[] cd  = cooldowns.get(attack.getName());
        if (cd != null && cd[idx] > 0) {
            System.out.println(attack.getName() + " on cooldown! " + cd[idx] + " turns left");
            return;
        }

        if (projectile.getState() == Projectile.State.AIMING) projectile.reset();
        selectedAttack = attack;
        System.out.println((player1Turn ? "P1" : "P2") + " selected " + attack.getName());
    }

    public void handleSpacebar() {
        if (isCurrentPlayerFrozen()) return;
        if (selectedAttack == null) {
            System.out.println("Select an attack first!");
            return;
        }
        switch (projectile.getState()) {
            case IDLE   -> projectile.startAiming(player1Turn);
            case AIMING -> projectile.fire();
            case FLYING -> {}
        }
    }

    private void applySelectedAttack() {
        if (selectedAttack == null) return;
        String who = player1Turn ? "P1" : "P2";
        int    idx = player1Turn ? 0 : 1;

        int[] cd = cooldowns.get(selectedAttack.getName());
        if (cd != null) cd[idx] = selectedAttack.getCooldown();

        switch (selectedAttack.getName()) {
            case "FIRE" -> {
                burnTargetPlayer = player1Turn ? 2 : 1;
                burnTicksLeft    = BURN_TICKS;
                burnFrameTimer   = 0;
                System.out.println(who + " FIRE HIT! Burn on P" + burnTargetPlayer);
            }
            case "ICE" -> {
                BasePlayer iceTarget = player1Turn ? p2 : p1;
                iceTarget.decreaseHp(1);
                frozenPlayer       = player1Turn ? 2 : 1;
                frozenDisplayTimer = FROZEN_DISPLAY_FRAMES;
                System.out.println(who + " ICE HIT! 1 damage + P" + frozenPlayer + " frozen!");
            }
            case "WIND" -> {
                windTargetPlayer = player1Turn ? 2 : 1;
                windTurnsLeft    = WIND_TURNS;
                windFrameCounter = 0;
                BasePlayer windTarget = player1Turn ? p2 : p1;
                windTarget.decreaseHp(1);
                System.out.println(who + " WIND HIT! P" + windTargetPlayer + " blown for " + WIND_TURNS + " turns! 1 damage!");
            }
            default -> {
                BasePlayer target = player1Turn ? p2 : p1;
                selectedAttack.attack(target);
                System.out.println(who + " NORMAL HIT! 1 damage.");
            }
        }
    }

    public void update() {
        if (windTargetPlayer != 0) windFrameCounter++;
        projectile.setTargetOffset(player1Turn ? getWindXOffset(2) : getWindXOffset(1));
        if (projectile.getState() == Projectile.State.AIMING)
            projectile.setLaunchOffset(player1Turn ? getWindXOffset(1) : getWindXOffset(2));

        boolean wasFlying = projectile.getState() == Projectile.State.FLYING;
        projectile.update();
        if (wasFlying && projectile.getState() == Projectile.State.IDLE) {
            if (projectile.isHit()) {
                String name = getSelectedAttackName();
                applySelectedAttack();
                projectileResult = "HIT!  " + name + "!";
            } else {
                projectileResult = "MISSED!  Turn lost.";
                System.out.println((player1Turn ? "P1" : "P2") + " MISSED! Turn lost.");
            }
            selectedAttack = null;
            switchTurn();
        }

        if (burnTicksLeft > 0) {
            burnFrameTimer++;
            if (burnFrameTimer >= BURN_FRAMES_PER_TICK) {
                burnFrameTimer = 0;
                burnTicksLeft--;
                BasePlayer bt = (burnTargetPlayer == 1) ? p1 : p2;
                bt.decreaseHp(1);
                System.out.println("P" + burnTargetPlayer + " burn tick! " + burnTicksLeft + " left");
            }
        } else {
            burnTargetPlayer = 0;
        }

        if (frozenPlayer != 0 && isCurrentPlayerFrozen()) {
            frozenDisplayTimer--;
            if (frozenDisplayTimer <= 0) {
                System.out.println("P" + frozenPlayer + " frozen turn skipped!");
                frozenPlayer   = 0;
                selectedAttack = null;
                switchTurn();
            }
        }
    }

    private void switchTurn() {
        if (windTargetPlayer != 0 && windTargetPlayer == (player1Turn ? 1 : 2)) {
            windTurnsLeft--;
            if (windTurnsLeft <= 0) { windTargetPlayer = 0; windFrameCounter = 0; }
        }
        projectile.clearAngleLock();
        player1Turn = !player1Turn;
        int idx = player1Turn ? 0 : 1;
        for (int[] cd : cooldowns.values()) if (cd[idx] > 0) cd[idx]--;
        if (abilityCooldown[idx] > 0) abilityCooldown[idx]--;
    }

    public void handleAbility() {
        if (isCurrentPlayerFrozen()) return;
        if (projectile.getState() != Projectile.State.IDLE) return;
        int idx = player1Turn ? 0 : 1;
        if (abilityCooldown[idx] > 0) {
            System.out.println("Ability on cooldown! " + abilityCooldown[idx] + " turns left");
            return;
        }
        BasePlayer current = player1Turn ? p1 : p2;
        current.ability();
        abilityCooldown[idx] = ABILITY_COOLDOWN;

        if (current.isPerfectAimReady()) {
            if (windTargetPlayer == 2) { windTargetPlayer = 0; windFrameCounter = 0; windTurnsLeft = 0; }
            projectile.setLockedAngle(calculatePerfectAngle());
            current.resetAbilityEffect();
            System.out.println("Cat perfect-aim locked! Wind cancelled.");
        } else {
            System.out.println("Dog healed to " + current.getHp());
            selectedAttack = null;
            switchTurn();
        }
    }

    private double calculatePerfectAngle() {
        double launchOff = player1Turn ? getWindXOffset(1) : getWindXOffset(2);
        double targetOff = player1Turn ? getWindXOffset(2) : getWindXOffset(1);
        double sx = (player1Turn ? Projectile.P1_START_X : Projectile.P2_START_X) + launchOff;
        double sy = player1Turn ? Projectile.P1_START_Y : Projectile.P2_START_Y;
        double targetX = player1Turn
            ? (Projectile.P2_BOX_X1 + Projectile.P2_BOX_X2) / 2.0 + targetOff
            : (Projectile.P1_BOX_X1 + Projectile.P1_BOX_X2) / 2.0 + targetOff;
        double targetY = (Projectile.BOX_Y1 + Projectile.BOX_Y2) / 2.0;
        double sign    = player1Turn ? 1.0 : -1.0;

        for (double a = Projectile.MIN_ANGLE; a <= Projectile.MAX_ANGLE; a += 0.1) {
            double rad = Math.toRadians(a);
            double pvx = sign * Projectile.LAUNCH_SPEED * Math.cos(rad);
            double pvy = -Projectile.LAUNCH_SPEED * Math.sin(rad);
            double dx  = targetX - sx;
            if (Math.abs(pvx) < 0.001) continue;
            double t = dx / pvx;
            if (t <= 0) continue;
            double predictedY = sy + pvy * t + 0.5 * Projectile.GRAVITY * t * t;
            if (Math.abs(predictedY - targetY) < 20) return a;
        }
        return 15.0;
    }

    public double getWindXOffset(int playerNum) {
        if (windTargetPlayer != playerNum) return 0;
        double baseX    = playerNum == 1 ? 100.0 : 620.0;
        double distance = 400.0 - baseX;
        return distance * (1 - Math.cos(windFrameCounter * 0.06)) / 2.0;
    }

    public String getSelectedAttackName() {
        return selectedAttack == null ? "" : selectedAttack.getName();
    }

    public AbilityType getAbilityType(boolean isP1) {
        return isP1 ? p1.getAbilityType() : p2.getAbilityType();
    }

    public int  getBurnTargetPlayer()            { return burnTargetPlayer; }
    public int  getBurnTicksLeft()               { return burnTicksLeft; }
    public int  getBurnFrameTimer()              { return burnFrameTimer; }
    public int  getFrozenPlayer()                { return frozenPlayer; }
    public int  getFrozenDisplayTimer()          { return frozenDisplayTimer; }
    public Projectile getProjectile()            { return projectile; }
    public boolean    isPlayer1Turn()            { return player1Turn; }
    public int  getFireCooldown(boolean isP1)    { return getCooldown("FIRE", isP1); }
    public int  getIceCooldown(boolean isP1)     { return getCooldown("ICE",  isP1); }
    public int  getWindCooldown(boolean isP1)    { return getCooldown("WIND", isP1); }
    public int  getWindTargetPlayer()            { return windTargetPlayer; }
    public int  getWindTurnsLeft()               { return windTurnsLeft; }
    public int  getAbilityCooldown(boolean isP1) { return abilityCooldown[isP1 ? 0 : 1]; }

    private int getCooldown(String name, boolean isP1) {
        int[] cd = cooldowns.get(name);
        return cd != null ? cd[isP1 ? 0 : 1] : 0;
    }

    public String getAbilityLabel(boolean isP1) {
        return isP1 ? p1.getAbilityLabel() : p2.getAbilityLabel();
    }

    public String getAndClearProjectileResult() {
        String r = projectileResult;
        projectileResult = "";
        return r;
    }
}
