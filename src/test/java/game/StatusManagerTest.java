package game;

import character.BasePlayer;
import character.DogPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusManagerTest {

    private StatusManager statusManager;
    private BasePlayer p1;
    private BasePlayer p2;

    @BeforeEach
    void setUp() {
        statusManager = new StatusManager();
        p1 = new DogPlayer();
        p2 = new DogPlayer();
    }

    // ── Test Fire (Burn) ──────────────────────────────────────────────────────

    @Test
    void testApplyFireSetsCorrectVariables() {
        statusManager.applyFire(2);
        assertEquals(2, statusManager.getBurnTargetPlayer());
        assertEquals(StatusManager.BURN_TICKS, statusManager.getBurnTicksLeft());
        assertEquals(0, statusManager.getBurnFrameTimer());
    }

    @Test
    void testUpdateBurnDealsDamageAfterFrames() {
        statusManager.applyFire(2);
        int initialHp = p2.getHp();

        // จำลองการอัปเดตเฟรมจนครบ 1 Tick
        for (int i = 0; i < StatusManager.BURN_FRAMES_PER_TICK; i++) {
            statusManager.updateBurn(p1, p2);
        }

        assertEquals(initialHp - 1, p2.getHp());
        assertEquals(StatusManager.BURN_TICKS - 1, statusManager.getBurnTicksLeft());
    }

    // ── Test Ice (Freeze) ─────────────────────────────────────────────────────

    @Test
    void testApplyIceDealsInstantDamageAndSetsFreeze() {
        int initialHp = p2.getHp();
        statusManager.applyIce(2, p2);

        assertEquals(2, statusManager.getFrozenPlayer());
        assertEquals(initialHp - 1, p2.getHp());
        assertEquals(StatusManager.FROZEN_DISPLAY_FRAMES, statusManager.getFrozenDisplayTimer());
    }

    @Test
    void testUpdateFreezeSignalsTurnSkipWhenTimerEnds() {
        statusManager.applyIce(2, p2);

        boolean shouldSkipTurn = false;
        // จำลองเฟรมของคนที่ถูกแช่แข็ง
        for (int i = 0; i < StatusManager.FROZEN_DISPLAY_FRAMES; i++) {
            shouldSkipTurn = statusManager.updateFreeze(true);
        }

        assertTrue(shouldSkipTurn, "เมื่อเวลาแช่แข็งหมด ควรรีเทิร์น true เพื่อบอกให้ข้ามเทิร์น");
        assertEquals(0, statusManager.getFrozenPlayer(), "สถานะแช่แข็งควรถูกเคลียร์");
    }

    // ── Test Wind (Wind) ──────────────────────────────────────────────────────

    @Test
    void testApplyWindDealsDamageAndSetsWind() {
        int initialHp = p2.getHp();
        statusManager.applyWind(2, p2);

        assertEquals(2, statusManager.getWindTargetPlayer());
        assertEquals(initialHp - 1, p2.getHp());
        assertEquals(StatusManager.WIND_TURNS, statusManager.getWindTurnsLeft());
    }

    @Test
    void testClearWindEffect() {
        statusManager.applyWind(2, p2);
        statusManager.clearWindEffect(2);

        assertEquals(0, statusManager.getWindTargetPlayer());
        assertEquals(0, statusManager.getWindTurnsLeft());
    }
}