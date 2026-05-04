package Application;

import AttackLogic.*;
import Charactor.BasePlayer;
import Charactor.Player1;
import Charactor.Player2;

public class Controller {
    private Player1 p1;
    private Player2 p2;
    private boolean player1Turn = true;
    private Projectile projectile = new Projectile();
    private String projectileResult = "";

    // Step 1 — player chooses an attack type before aiming
    private Attackable selectedAttack = null;

    // ── Burn (FireAttack): 1 HP/sec × 3 ticks ──────────────────────────────
    private int burnTargetPlayer = 0;
    private int burnTicksLeft    = 0;
    private int burnFrameTimer   = 0;
    public static final int BURN_TICKS           = 3;
    public static final int BURN_FRAMES_PER_TICK = 60;

    // ── Freeze (IceAttack): target's next turn is auto-skipped ─────────────
    private int frozenPlayer       = 0;
    private int frozenDisplayTimer = 0;
    public static final int FROZEN_DISPLAY_FRAMES = 120;

    // ── Cooldowns [0]=P1, [1]=P2 ────────────────────────────────────────────
    public static final int FIRE_COOLDOWN   = 3;
    public static final int ICE_COOLDOWN    = 2;
    public static final int WIND_COOLDOWN   = 2;
    private int[] fireCooldown = {0, 0};
    private int[] iceCooldown  = {0, 0};
    private int[] windCooldown = {0, 0};

    // ── Wind (WindAttack): target oscillates in x for WIND_TURNS turns ──────
    public static final int WIND_TURNS = 1;
    private int windTargetPlayer = 0;
    private int windTurnsLeft    = 0;
    private int windFrameCounter = 0;

    // ── Ability (Q / U): Dog heals, Cat locks perfect-aim angle ─────────────
    public static final int ABILITY_COOLDOWN = 4;
    private int[] abilityCooldown = {0, 0};

    public Controller(Player1 p1, Player2 p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    private boolean currentPlayerIsFrozen() {
        return frozenPlayer != 0 && frozenPlayer == (player1Turn ? 1 : 2);
    }

    // ── Step 1: select attack type (A/S/D or J/K/L) ────────────────────────
    public void selectAttack(Attackable attack) {
        if (attack == null) return;
        if (currentPlayerIsFrozen()) return;
        if (projectile.getState() == Projectile.State.FLYING) return;

        int idx = player1Turn ? 0 : 1;
        if (attack instanceof FireAttack && fireCooldown[idx] > 0) {
            System.out.println("Fire on cooldown! " + fireCooldown[idx] + " turns left"); return;
        }
        if (attack instanceof IceAttack && iceCooldown[idx] > 0) {
            System.out.println("Ice on cooldown! " + iceCooldown[idx] + " turns left"); return;
        }
        if (attack instanceof WindAttack && windCooldown[idx] > 0) {
            System.out.println("Wind on cooldown! " + windCooldown[idx] + " turns left"); return;
        }

        // Changing selection while aiming resets the arc
        if (projectile.getState() == Projectile.State.AIMING) projectile.reset();

        selectedAttack = attack;
        System.out.println((player1Turn ? "P1" : "P2") + " selected " + attack.getClass().getSimpleName());
    }

    // ── Step 2: SPACE starts aiming, second SPACE fires ────────────────────
    public void handleSpacebar() {
        if (currentPlayerIsFrozen()) return;
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

    // Apply the selected attack when the projectile hits
    private void applySelectedAttack() {
        if (selectedAttack == null) return;
        String who = player1Turn ? "P1" : "P2";

        int idx = player1Turn ? 0 : 1;
        if (selectedAttack instanceof FireAttack) {
            fireCooldown[idx] = FIRE_COOLDOWN;
            burnTargetPlayer  = player1Turn ? 2 : 1;
            burnTicksLeft     = BURN_TICKS;
            burnFrameTimer    = 0;
            System.out.println(who + " FIRE HIT! Burn on " + (burnTargetPlayer == 1 ? "P1" : "P2"));
        } else if (selectedAttack instanceof IceAttack) {
            iceCooldown[idx]   = ICE_COOLDOWN;
            BasePlayer iceTarget = player1Turn ? p2 : p1;
            iceTarget.decreaseHp(1);
            frozenPlayer       = player1Turn ? 2 : 1;
            frozenDisplayTimer = FROZEN_DISPLAY_FRAMES;
            System.out.println(who + " ICE HIT! 1 damage + " + (frozenPlayer == 1 ? "P1" : "P2") + " frozen!");
        } else if (selectedAttack instanceof WindAttack) {
            windCooldown[idx]  = WIND_COOLDOWN;
            windTargetPlayer   = player1Turn ? 2 : 1;
            windTurnsLeft      = WIND_TURNS;
            windFrameCounter   = 0;
            System.out.println(who + " WIND HIT! P" + windTargetPlayer + " blown for " + WIND_TURNS + " turns!");
        } else {
            BasePlayer target = player1Turn ? p2 : p1;
            selectedAttack.attack(target);
            System.out.println(who + " NORMAL HIT! 1 damage.");
        }
    }

    // Called every frame from the game loop
    public void update() {
        // ── Wind frame counter + projectile offset sync ─────────────────────
        if (windTargetPlayer != 0) windFrameCounter++;
        projectile.setTargetOffset(player1Turn ? getWindXOffset(2) : getWindXOffset(1));
        if (projectile.getState() == Projectile.State.AIMING)
            projectile.setLaunchOffset(player1Turn ? getWindXOffset(1) : getWindXOffset(2));

        // ── Projectile ──────────────────────────────────────────────────────
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

        // ── Burn ticks (time-based, independent of turns) ───────────────────
        if (burnTicksLeft > 0) {
            burnFrameTimer++;
            if (burnFrameTimer >= BURN_FRAMES_PER_TICK) {
                burnFrameTimer = 0;
                burnTicksLeft--;
                BasePlayer bt = (burnTargetPlayer == 1) ? p1 : p2;
                bt.decreaseHp(1);
                System.out.println((burnTargetPlayer == 1 ? "P1" : "P2")
                        + " burn tick! " + burnTicksLeft + " left");
            }
        } else {
            burnTargetPlayer = 0;
        }

        // ── Frozen turn auto-skip ────────────────────────────────────────────
        if (frozenPlayer != 0 && currentPlayerIsFrozen()) {
            frozenDisplayTimer--;
            if (frozenDisplayTimer <= 0) {
                System.out.println((frozenPlayer == 1 ? "P1" : "P2") + " frozen turn skipped!");
                frozenPlayer   = 0;
                selectedAttack = null;
                switchTurn();
            }
        }
    }

    // ── Turn switch: flip turn then tick cooldowns for the next player ───────
    private void switchTurn() {
        // Decrement wind turns when the wind-affected player's turn ends
        if (windTargetPlayer != 0 && windTargetPlayer == (player1Turn ? 1 : 2)) {
            windTurnsLeft--;
            if (windTurnsLeft <= 0) { windTargetPlayer = 0; windFrameCounter = 0; }
        }
        projectile.clearAngleLock(); // Cat's perfect aim doesn't carry over
        player1Turn = !player1Turn;
        int idx = player1Turn ? 0 : 1;
        if (fireCooldown[idx]    > 0) fireCooldown[idx]--;
        if (iceCooldown[idx]     > 0) iceCooldown[idx]--;
        if (windCooldown[idx]    > 0) windCooldown[idx]--;
        if (abilityCooldown[idx] > 0) abilityCooldown[idx]--;
    }

    // ── Ability (Q for Dog, U for Cat) ───────────────────────────────────────
    public void handleAbility() {
        if (currentPlayerIsFrozen()) return;
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
            // Cat: cancel any wind effect on self, then lock the perfect angle
            if (windTargetPlayer == 2) { windTargetPlayer = 0; windFrameCounter = 0; windTurnsLeft = 0; }
            projectile.setLockedAngle(calculatePerfectAngle());
            current.resetAbilityEffect();
            System.out.println("Cat perfect-aim locked! Wind cancelled.");
        } else {
            // Dog: heal then end turn immediately
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
            double dx = targetX - sx;
            if (Math.abs(pvx) < 0.001) continue;
            double t = dx / pvx;
            if (t <= 0) continue;
            double predictedY = sy + pvy * t + 0.5 * Projectile.GRAVITY * t * t;
            if (Math.abs(predictedY - targetY) < 20) return a;
        }
        return 15.0; // fallback
    }

    // ── Wind x-offset (drives both rendering and hit-box shift) ─────────────
    public double getWindXOffset(int playerNum) {
        if (windTargetPlayer != playerNum) return 0;
        double baseX    = playerNum == 1 ? 100.0 : 620.0;
        double distance = 400.0 - baseX; // P1: +300 (moves right), P2: -220 (moves left)
        return distance * (1 - Math.cos(windFrameCounter * 0.06)) / 2.0;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getSelectedAttackName() {
        if (selectedAttack == null) return "";
        if (selectedAttack instanceof FireAttack) return "FIRE";
        if (selectedAttack instanceof IceAttack)  return "ICE";
        if (selectedAttack instanceof WindAttack)  return "WIND";
        return "NORMAL";
    }

    public int  getBurnTargetPlayer()   { return burnTargetPlayer; }
    public int  getBurnTicksLeft()      { return burnTicksLeft; }
    public int  getBurnFrameTimer()     { return burnFrameTimer; }
    public int  getFrozenPlayer()       { return frozenPlayer; }
    public int  getFrozenDisplayTimer() { return frozenDisplayTimer; }
    public Projectile getProjectile()   { return projectile; }
    public boolean isPlayer1Turn()      { return player1Turn; }
    public int    getFireCooldown(boolean p1)  { return fireCooldown[p1 ? 0 : 1]; }
    public int    getIceCooldown(boolean p1)   { return iceCooldown[p1 ? 0 : 1]; }
    public int    getWindCooldown(boolean p1)  { return windCooldown[p1 ? 0 : 1]; }
    public int    getWindTargetPlayer()        { return windTargetPlayer; }
    public int    getWindTurnsLeft()           { return windTurnsLeft; }
    public int    getAbilityCooldown(boolean p1) { return abilityCooldown[p1 ? 0 : 1]; }

    public String getAndClearProjectileResult() {
        String r = projectileResult;
        projectileResult = "";
        return r;
    }
}
