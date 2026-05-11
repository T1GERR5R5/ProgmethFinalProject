package game;

import attack.*;
import character.AbilityType;
import character.BasePlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Central game-logic hub for one match. Manages turn order, attack selection,
 * projectile lifecycle, ability usage, cooldowns, and delegates status-effect
 * tracking to {@link StatusManager}.
 *
 * <p>Called every frame by the AnimationTimer in {@link application.scene.GamePlayScene}.
 */
public class Controller {
    /** Identifier constant for Fire attacks (used as cooldown map key). */
    public static final String ATTACK_FIRE = "FIRE";
    /** Identifier constant for Ice attacks. */
    public static final String ATTACK_ICE  = "ICE";
    /** Identifier constant for Wind attacks. */
    public static final String ATTACK_WIND = "WIND";
    /** Turns before a player's special ability is available again. */
    public static final int ABILITY_COOLDOWN = 4;

    private final BasePlayer p1;
    private final BasePlayer p2;
    private boolean player1Turn = true;

    private final Projectile projectile = new Projectile();
    private String projectileResult = "";
    private Attackable selectedAttack = null;

    private final Map<String, int[]> cooldowns = new HashMap<>();
    private final int[] abilityCooldown = {0, 0};

    private final StatusManager statusManager = new StatusManager();

    /**
     * @param p1 Player 1's character
     * @param p2 Player 2's character
     */
    public Controller(BasePlayer p1, BasePlayer p2) {
        this.p1 = p1;
        this.p2 = p2;
        cooldowns.put(ATTACK_FIRE, new int[]{0, 0});
        cooldowns.put(ATTACK_ICE,  new int[]{0, 0});
        cooldowns.put(ATTACK_WIND, new int[]{0, 0});
    }

    // ── Update Loop ──────────────────────────────────────────────────────────

    /**
     * Advances the game by one frame: syncs wind offsets, updates the projectile,
     * processes burn ticks, and checks for a frozen-turn auto-skip.
     * Must be called every frame from the AnimationTimer.
     */
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

    /**
     * Sets the attack type for the current turn.
     * Validates that the attack is not on cooldown and the game state allows selection.
     * @param attack the attack to select; {@code null} is silently ignored
     */
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

    /**
     * Handles a SPACE key press.
     * First press → {@link Projectile#startAiming}; second press → {@link Projectile#fire}.
     */
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

    /**
     * Activates the current player's special ability if off cooldown and the projectile is IDLE.
     * Dog: heals and ends the turn. Cat: locks perfect-aim angle and cancels wind on self.
     */
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
            statusManager.clearWindEffect(2);

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

    // ── Public Getters ───────────────────────────────────────────────────────

    /**
     * Returns the cosine-based horizontal displacement for a wind-affected player.
     * @param playerNum 1 or 2
     * @return pixel offset; {@code 0} if the player is not wind-affected
     */
    public double getWindXOffset(int playerNum) {
        return PhysicsUtils.calculateWindXOffset(playerNum, statusManager.getWindTargetPlayer(), statusManager.getWindFrameCounter());
    }

    /** @return {@code true} if the current turn's player is frozen */
    public boolean isCurrentPlayerFrozen()   { return statusManager.getFrozenPlayer() == getCurrentPlayerNum(); }
    /** @return the selected attack's name, or {@code ""} if none selected */
    public String getSelectedAttackName()    { return selectedAttack == null ? "" : selectedAttack.getName(); }
    /** @param isP1 which player's ability type to return */
    public AbilityType getAbilityType(boolean isP1) { return isP1 ? p1.getAbilityType() : p2.getAbilityType(); }
    /** @param isP1 which player's ability label to return */
    public String getAbilityLabel(boolean isP1)     { return isP1 ? p1.getAbilityLabel() : p2.getAbilityLabel(); }

    /** @return the shared {@link Projectile} instance */
    public Projectile getProjectile()        { return projectile; }
    /** @return {@code true} if it is currently P1's turn */
    public boolean isPlayer1Turn()           { return player1Turn; }

    /** @return player number currently burning (0 if none) */
    public int getBurnTargetPlayer()         { return statusManager.getBurnTargetPlayer(); }
    /** @return remaining burn ticks */
    public int getBurnTicksLeft()            { return statusManager.getBurnTicksLeft(); }
    /** @return frames elapsed in the current burn tick */
    public int getBurnFrameTimer()           { return statusManager.getBurnFrameTimer(); }
    /** @return player number currently frozen (0 if none) */
    public int getFrozenPlayer()             { return statusManager.getFrozenPlayer(); }
    /** @return frames remaining until the frozen turn is skipped */
    public int getFrozenDisplayTimer()       { return statusManager.getFrozenDisplayTimer(); }
    /** @return player number currently affected by wind (0 if none) */
    public int getWindTargetPlayer()         { return statusManager.getWindTargetPlayer(); }
    /** @return remaining wind turns */
    public int getWindTurnsLeft()            { return statusManager.getWindTurnsLeft(); }

    /** @param isP1 which player's Fire cooldown to query */
    public int getFireCooldown(boolean isP1) { return getCooldown(ATTACK_FIRE, isP1); }
    /** @param isP1 which player's Ice cooldown to query */
    public int getIceCooldown(boolean isP1)  { return getCooldown(ATTACK_ICE,  isP1); }
    /** @param isP1 which player's Wind cooldown to query */
    public int getWindCooldown(boolean isP1) { return getCooldown(ATTACK_WIND, isP1); }
    /** @param isP1 which player's ability cooldown to query */
    public int getAbilityCooldown(boolean isP1) { return abilityCooldown[isP1 ? 0 : 1]; }

    private int getCooldown(String name, boolean isP1) {
        int[] cd = cooldowns.get(name);
        return cd != null ? cd[isP1 ? 0 : 1] : 0;
    }

    /**
     * Returns and clears the last projectile result string (e.g. {@code "HIT!  FIRE!"}).
     * Called once per frame by the renderer.
     * @return result string, or {@code ""} if no new result
     */
    public String getAndClearProjectileResult() {
        String r = projectileResult;
        projectileResult = "";
        return r;
    }
}
