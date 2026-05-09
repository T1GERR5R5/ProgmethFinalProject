package game;

import attack.*;
import character.AbilityType;
import character.BasePlayer;

import java.util.HashMap;
import java.util.Map;

public class Controller {
    // ── Constants ────────────────────────────────────────────────────────────
    public static final String ATTACK_FIRE = "FIRE";
    public static final String ATTACK_ICE  = "ICE";
    public static final String ATTACK_WIND = "WIND";
    public static final int ABILITY_COOLDOWN = 4;

    // ── Core Game State ──────────────────────────────────────────────────────
    private final BasePlayer p1;
    private final BasePlayer p2;
    private boolean player1Turn = true;

    private final Projectile projectile = new Projectile();
    private String projectileResult = "";
    private Attackable selectedAttack = null;

    private final Map<String, int[]> cooldowns = new HashMap<>();
    private final int[] abilityCooldown = {0, 0};

    // ── Managers ───────────────────────────────────────────────────────────────
    private final StatusManager statusManager = new StatusManager();

    public Controller(BasePlayer p1, BasePlayer p2) {
        this.p1 = p1;
        this.p2 = p2;
        cooldowns.put(ATTACK_FIRE, new int[]{0, 0});
        cooldowns.put(ATTACK_ICE,  new int[]{0, 0});
        cooldowns.put(ATTACK_WIND, new int[]{0, 0});
    }

    // ── Update Loop ──────────────────────────────────────────────────────────

    public void update() {
        statusManager.updateWindFrame();

        projectile.setTargetOffset(player1Turn ? getWindXOffset(2) : getWindXOffset(1));
        if (projectile.getState() == Projectile.State.AIMING) {
            projectile.setLaunchOffset(player1Turn ? getWindXOffset(1) : getWindXOffset(2));
        }

        boolean wasFlying = projectile.getState() == Projectile.State.FLYING;
        projectile.update();

        if (wasFlying && projectile.getState() == Projectile.State.IDLE) {
            handleProjectileImpact();
        }

        statusManager.updateBurn(p1, p2);

        if (statusManager.updateFreeze(isCurrentPlayerFrozen())) {
            selectedAttack = null;
            switchTurn();
        }
    }

    private void handleProjectileImpact() {
        if (projectile.isHit()) {
            String attackName = getSelectedAttackName();
            applySelectedAttack();
            projectileResult = "HIT!  " + attackName + "!";
        } else {
            projectileResult = "MISSED!  Turn lost.";
            System.out.println(getCurrentPlayerName() + " MISSED! Turn lost.");
        }
        selectedAttack = null;
        switchTurn();
    }

    // ── Actions & Inputs ─────────────────────────────────────────────────────

    public void selectAttack(Attackable attack) {
        if (attack == null || isCurrentPlayerFrozen() || projectile.getState() == Projectile.State.FLYING) return;

        int[] cd = cooldowns.get(attack.getName());
        if (cd != null && cd[getCurrentIndex()] > 0) {
            System.out.println(attack.getName() + " on cooldown! " + cd[getCurrentIndex()] + " turns left");
            return;
        }

        if (projectile.getState() == Projectile.State.AIMING) projectile.reset();
        selectedAttack = attack;
        System.out.println(getCurrentPlayerName() + " selected " + attack.getName());
    }

    public void handleSpacebar() {
        if (isCurrentPlayerFrozen()) return;
        if (selectedAttack == null) {
            System.out.println("Select an attack first!");
            return;
        }

        switch (projectile.getState()) {
            case IDLE   -> projectile.startAiming(player1Turn);
            case AIMING -> {
                projectile.fire();
                SoundManager.playThrow();
            }
            case FLYING -> {}
        }
    }

    private void applySelectedAttack() {
        if (selectedAttack == null) return;

        int[] cd = cooldowns.get(selectedAttack.getName());
        if (cd != null) cd[getCurrentIndex()] = selectedAttack.getCooldown();

        switch (selectedAttack.getName()) {
            case ATTACK_FIRE -> {
                statusManager.applyFire(getOpponentNum());
                System.out.println(getCurrentPlayerName() + " FIRE HIT! Burn on P" + statusManager.getBurnTargetPlayer());
            }
            case ATTACK_ICE -> {
                statusManager.applyIce(getOpponentNum(), getOpponent());
                System.out.println(getCurrentPlayerName() + " ICE HIT! 1 damage + P" + statusManager.getFrozenPlayer() + " frozen!");
            }
            case ATTACK_WIND -> {
                statusManager.applyWind(getOpponentNum(), getOpponent());
                System.out.println(getCurrentPlayerName() + " WIND HIT! P" + statusManager.getWindTargetPlayer() + " blown for " + StatusManager.WIND_TURNS + " turns! 1 damage!");
            }
            default -> {
                selectedAttack.attack(getOpponent());
                System.out.println(getCurrentPlayerName() + " NORMAL HIT! 1 damage.");
            }
        }
    }

    public void handleAbility() {
        if (isCurrentPlayerFrozen() || projectile.getState() != Projectile.State.IDLE) return;

        int idx = getCurrentIndex();
        if (abilityCooldown[idx] > 0) {
            System.out.println("Ability on cooldown! " + abilityCooldown[idx] + " turns left");
            return;
        }

        BasePlayer current = getCurrentPlayer();
        current.ability();
        abilityCooldown[idx] = ABILITY_COOLDOWN;

        if (current.isPerfectAimReady()) {
            statusManager.clearWindEffect(2); // Cancel P2 Wind

            double launchOff = getWindXOffset(getCurrentPlayerNum());
            double targetOff = getWindXOffset(getOpponentNum());
            projectile.setLockedAngle(PhysicsUtils.calculatePerfectAngle(player1Turn, launchOff, targetOff));

            current.resetAbilityEffect();
            System.out.println("Cat perfect-aim locked! Wind cancelled.");
        } else {
            System.out.println("Dog healed to " + current.getHp());
            selectedAttack = null;
            switchTurn();
        }
    }

    private void switchTurn() {
        statusManager.onTurnSwitched(getCurrentPlayerNum());

        projectile.clearAngleLock();
        player1Turn = !player1Turn;

        int newIdx = getCurrentIndex();
        for (int[] cd : cooldowns.values()) {
            if (cd[newIdx] > 0) cd[newIdx]--;
        }
        if (abilityCooldown[newIdx] > 0) abilityCooldown[newIdx]--;
    }

    // ── Helper Methods ───────────────────────────────────────────────────────
    private int getCurrentIndex()            { return player1Turn ? 0 : 1; }
    private BasePlayer getCurrentPlayer()    { return player1Turn ? p1 : p2; }
    private BasePlayer getOpponent()         { return player1Turn ? p2 : p1; }
    private int getCurrentPlayerNum()        { return player1Turn ? 1 : 2; }
    private int getOpponentNum()             { return player1Turn ? 2 : 1; }
    private String getCurrentPlayerName()    { return player1Turn ? "P1" : "P2"; }

    // ── Public Getters (รักษาไว้เพื่อให้ไฟล์อื่นเรียกใช้ได้เหมือนเดิม) ────────────

    public double getWindXOffset(int playerNum) {
        return PhysicsUtils.calculateWindXOffset(playerNum, statusManager.getWindTargetPlayer(), statusManager.getWindFrameCounter());
    }

    public boolean isCurrentPlayerFrozen()   { return statusManager.getFrozenPlayer() == getCurrentPlayerNum(); }
    public String getSelectedAttackName()    { return selectedAttack == null ? "" : selectedAttack.getName(); }
    public AbilityType getAbilityType(boolean isP1) { return isP1 ? p1.getAbilityType() : p2.getAbilityType(); }
    public String getAbilityLabel(boolean isP1)     { return isP1 ? p1.getAbilityLabel() : p2.getAbilityLabel(); }

    public Projectile getProjectile()        { return projectile; }
    public boolean isPlayer1Turn()           { return player1Turn; }

    // ดึงค่า Status ผ่าน Manager
    public int getBurnTargetPlayer()         { return statusManager.getBurnTargetPlayer(); }
    public int getBurnTicksLeft()            { return statusManager.getBurnTicksLeft(); }
    public int getBurnFrameTimer()           { return statusManager.getBurnFrameTimer(); }
    public int getFrozenPlayer()             { return statusManager.getFrozenPlayer(); }
    public int getFrozenDisplayTimer()       { return statusManager.getFrozenDisplayTimer(); }
    public int getWindTargetPlayer()         { return statusManager.getWindTargetPlayer(); }
    public int getWindTurnsLeft()            { return statusManager.getWindTurnsLeft(); }

    public int getFireCooldown(boolean isP1) { return getCooldown(ATTACK_FIRE, isP1); }
    public int getIceCooldown(boolean isP1)  { return getCooldown(ATTACK_ICE,  isP1); }
    public int getWindCooldown(boolean isP1) { return getCooldown(ATTACK_WIND, isP1); }
    public int getAbilityCooldown(boolean isP1) { return abilityCooldown[isP1 ? 0 : 1]; }

    private int getCooldown(String name, boolean isP1) {
        int[] cd = cooldowns.get(name);
        return cd != null ? cd[isP1 ? 0 : 1] : 0;
    }

    public String getAndClearProjectileResult() {
        String r = projectileResult;
        projectileResult = "";
        return r;
    }
}