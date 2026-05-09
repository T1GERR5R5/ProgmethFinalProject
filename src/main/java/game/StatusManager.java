package game;

import character.BasePlayer;

public class StatusManager {
    public static final int BURN_TICKS           = 2;
    public static final int BURN_FRAMES_PER_TICK = 60;
    public static final int FROZEN_DISPLAY_FRAMES = 120;
    public static final int WIND_TURNS           = 1;

    private int burnTargetPlayer = 0;
    private int burnTicksLeft    = 0;
    private int burnFrameTimer   = 0;

    private int frozenPlayer       = 0;
    private int frozenDisplayTimer = 0;

    private int windTargetPlayer = 0;
    private int windTurnsLeft    = 0;
    private int windFrameCounter = 0;

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

    public boolean updateFreeze(boolean isCurrentPlayerFrozen) {
        if (frozenPlayer != 0 && isCurrentPlayerFrozen) {
            frozenDisplayTimer--;
            if (frozenDisplayTimer <= 0) {
                System.out.println("P" + frozenPlayer + " frozen turn skipped!");
                SoundManager.stopIce();
                frozenPlayer = 0;
                return true; // ส่งค่า true เพื่อบอกให้ Controller ข้ามเทิร์น
            }
        }
        return false;
    }

    public void updateWindFrame() {
        if (windTargetPlayer != 0) windFrameCounter++;
    }

    public void applyFire(int targetNum) {
        burnTargetPlayer = targetNum;
        burnTicksLeft    = BURN_TICKS;
        burnFrameTimer   = 0;
        SoundManager.playFireLoop();
    }

    public void applyIce(int targetNum, BasePlayer target) {
        frozenPlayer       = targetNum;
        frozenDisplayTimer = FROZEN_DISPLAY_FRAMES;
        target.decreaseHp(1);
        SoundManager.playIceLoop();
    }

    public void applyWind(int targetNum, BasePlayer target) {
        windTargetPlayer = targetNum;
        windTurnsLeft    = WIND_TURNS;
        windFrameCounter = 0;
        target.decreaseHp(1);
        SoundManager.playWindLoop();
    }

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

    public void clearWindEffect(int targetNum) {
        if (windTargetPlayer == targetNum) {
            windTargetPlayer = 0;
            windFrameCounter = 0;
            windTurnsLeft = 0;
        }
    }

    // Getters สำหรับส่งค่ากลับไปให้ Controller (เพื่อส่งต่อให้ Renderer อีกที)
    public int getBurnTargetPlayer()   { return burnTargetPlayer; }
    public int getBurnTicksLeft()      { return burnTicksLeft; }
    public int getBurnFrameTimer()     { return burnFrameTimer; }
    public int getFrozenPlayer()       { return frozenPlayer; }
    public int getFrozenDisplayTimer() { return frozenDisplayTimer; }
    public int getWindTargetPlayer()   { return windTargetPlayer; }
    public int getWindTurnsLeft()      { return windTurnsLeft; }
    public int getWindFrameCounter()   { return windFrameCounter; }
}