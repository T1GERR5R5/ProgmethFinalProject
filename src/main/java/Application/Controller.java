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

        if (selectedAttack instanceof FireAttack) {
            burnTargetPlayer = player1Turn ? 2 : 1;
            burnTicksLeft    = BURN_TICKS;
            burnFrameTimer   = 0;
            System.out.println(who + " FIRE HIT! Burn on " + (burnTargetPlayer == 1 ? "P1" : "P2"));
        } else if (selectedAttack instanceof IceAttack) {
            frozenPlayer       = player1Turn ? 2 : 1;
            frozenDisplayTimer = FROZEN_DISPLAY_FRAMES;
            System.out.println(who + " ICE HIT! " + (frozenPlayer == 1 ? "P1" : "P2") + " frozen!");
        } else {
            BasePlayer target = player1Turn ? p2 : p1;
            selectedAttack.attack(target);
            System.out.println(who + " NORMAL HIT! 1 damage.");
        }
    }

    // Called every frame from the game loop
    public void update() {
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
            player1Turn    = !player1Turn;
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
                player1Turn    = !player1Turn;
                selectedAttack = null;
            }
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getSelectedAttackName() {
        if (selectedAttack == null) return "";
        if (selectedAttack instanceof FireAttack) return "FIRE";
        if (selectedAttack instanceof IceAttack)  return "ICE";
        return "NORMAL";
    }

    public int  getBurnTargetPlayer()   { return burnTargetPlayer; }
    public int  getBurnTicksLeft()      { return burnTicksLeft; }
    public int  getBurnFrameTimer()     { return burnFrameTimer; }
    public int  getFrozenPlayer()       { return frozenPlayer; }
    public int  getFrozenDisplayTimer() { return frozenDisplayTimer; }
    public Projectile getProjectile()   { return projectile; }
    public boolean isPlayer1Turn()      { return player1Turn; }

    public String getAndClearProjectileResult() {
        String r = projectileResult;
        projectileResult = "";
        return r;
    }
}
