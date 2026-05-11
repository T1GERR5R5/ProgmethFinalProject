package game;

import character.BasePlayer;

/**
 * Manages the three time-limited status effects: Burn, Freeze, and Wind.
 * Called each frame by {@link Controller#update()} and on turn switches.
 */
public class StatusManager {
    /** Number of burn damage ticks after a Fire hit. */
    public static final int BURN_TICKS           = 2;
    /** Frames between consecutive burn ticks (~1 second at 60 fps). */
    public static final int BURN_FRAMES_PER_TICK = 60;
    /** Frames the frozen overlay is displayed before the turn is auto-skipped. */
    public static final int FROZEN_DISPLAY_FRAMES = 120;
    /** Number of turns the wind oscillation lasts. */
    public static final int WIND_TURNS           = 1;

    private int burnTargetPlayer = 0;
    private int burnTicksLeft    = 0;
    private int burnFrameTimer   = 0;

    private int frozenPlayer       = 0;
    private int frozenDisplayTimer = 0;

    private int windTargetPlayer = 0;
    private int windTurnsLeft    = 0;
    private int windFrameCounter = 0;

    /**
     * Advances burn timers and applies 1 HP damage per completed tick.
     * Stops the fire sound when burn expires.
     * @param p1 Player 1 reference
     * @param p2 Player 2 reference
     */
    public void updateBurn(BasePlayer p1, BasePlayer p2) {
        if (burnTicksLeft > 0) {
            burnFrameTimer++;
            if (burnFrameTimer >= BURN_FRAMES_PER_TICK) {
                burnFrameTimer = 0;
                burnTicksLeft--;
                BasePlayer target = (burnTargetPlayer == 1) ? p1 : p2;
                target.decreaseHp(1);
                System.out.println("P" + burnTargetPlayer + " burn tick! " + burnTicksLeft + " left");

                if (burnTicksLeft <= 0) {
                    SoundManager.stopFire();
                }
            }
        } else {
            if (burnTargetPlayer != 0) SoundManager.stopFire();
            burnTargetPlayer = 0;
        }
    }

    /**
     * Counts down the frozen display timer and signals turn-skip when it expires.
     * @param isCurrentPlayerFrozen whether the current turn's player is frozen
     * @return {@code true} when the freeze timer reaches 0 and the turn must be skipped
     */
    public boolean updateFreeze(boolean isCurrentPlayerFrozen) {
        if (frozenPlayer != 0 && isCurrentPlayerFrozen) {
            frozenDisplayTimer--;
            if (frozenDisplayTimer <= 0) {
                System.out.println("P" + frozenPlayer + " frozen turn skipped!");
                SoundManager.stopIce();
                frozenPlayer = 0;
                return true;
            }
        }
        return false;
    }

    /** Increments the wind frame counter if a wind effect is active. */
    public void updateWindFrame() {
        if (windTargetPlayer != 0) windFrameCounter++;
    }

    /**
     * Applies a burn effect to the given player and starts the fire sound loop.
     * @param targetNum player number to burn (1 or 2)
     */
    public void applyFire(int targetNum) {
        burnTargetPlayer = targetNum;
        burnTicksLeft    = BURN_TICKS;
        burnFrameTimer   = 0;
        SoundManager.playFireLoop();
    }

    /**
     * Applies a freeze effect: deals 1 HP to the target and starts the ice sound loop.
     * @param targetNum player number to freeze (1 or 2)
     * @param target    the {@link BasePlayer} instance to apply damage to
     */
    public void applyIce(int targetNum, BasePlayer target) {
        frozenPlayer       = targetNum;
        frozenDisplayTimer = FROZEN_DISPLAY_FRAMES;
        target.decreaseHp(1);
        SoundManager.playIceLoop();
    }

    /**
     * Applies a wind effect: deals 1 HP to the target and starts the wind sound loop.
     * @param targetNum player number to oscillate (1 or 2)
     * @param target    the {@link BasePlayer} instance to apply damage to
     */
    public void applyWind(int targetNum, BasePlayer target) {
        windTargetPlayer = targetNum;
        windTurnsLeft    = WIND_TURNS;
        windFrameCounter = 0;
        target.decreaseHp(1);
        SoundManager.playWindLoop();
    }

    /**
     * Decrements the wind turn counter for the player whose turn just ended.
     * Clears the wind effect and stops the sound when turns run out.
     * @param currentPlayerNum the player whose turn is ending (1 or 2)
     */
    public void onTurnSwitched(int currentPlayerNum) {
        if (windTargetPlayer == currentPlayerNum) {
            windTurnsLeft--;
            if (windTurnsLeft <= 0) {
                SoundManager.stopWind();
                windTargetPlayer = 0;
                windFrameCounter = 0;
            }
        }
    }

    /**
     * Forcefully clears the wind effect on a specific player (used by Cat's AIM ability).
     * @param targetNum player number whose wind effect should be cancelled (1 or 2)
     */
    public void clearWindEffect(int targetNum) {
        if (windTargetPlayer == targetNum) {
            windTargetPlayer = 0;
            windFrameCounter = 0;
            windTurnsLeft = 0;
        }
    }

    /** @return player number currently burning (0 if none) */
    public int getBurnTargetPlayer()   { return burnTargetPlayer; }
    /** @return remaining burn ticks */
    public int getBurnTicksLeft()      { return burnTicksLeft; }
    /** @return frames elapsed in the current burn tick */
    public int getBurnFrameTimer()     { return burnFrameTimer; }
    /** @return player number currently frozen (0 if none) */
    public int getFrozenPlayer()       { return frozenPlayer; }
    /** @return frames remaining until the frozen turn is skipped */
    public int getFrozenDisplayTimer() { return frozenDisplayTimer; }
    /** @return player number currently affected by wind (0 if none) */
    public int getWindTargetPlayer()   { return windTargetPlayer; }
    /** @return remaining wind turns */
    public int getWindTurnsLeft()      { return windTurnsLeft; }
    /** @return frames elapsed since wind was applied */
    public int getWindFrameCounter()   { return windFrameCounter; }
}
