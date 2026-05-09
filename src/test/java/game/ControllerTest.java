package game;

import attack.*;
import character.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    private DogPlayer p1;
    private DogPlayer p2;
    private Controller controller;

    @BeforeEach
    void setUp() {
        p1 = new DogPlayer();
        p2 = new DogPlayer();
        controller = new Controller(p1, p2);
    }

    /** Fire an attack from the current player at the given angle; return hit result string. */
    private String fireAndWait(Attackable attack, double angle, int maxFrames) {
        controller.selectAttack(attack);
        controller.handleSpacebar();
        controller.getProjectile().setLockedAngle(angle);
        controller.handleSpacebar();
        String result = "";
        for (int i = 0; i < maxFrames; i++) {
            controller.update();
            String r = controller.getAndClearProjectileResult();
            if (!r.isEmpty()) { result = r; break; }
        }
        return result;
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    void testInitiallyP1Turn() {
        assertTrue(controller.isPlayer1Turn());
    }

    @Test
    void testProjectileInitiallyIdle() {
        assertEquals(Projectile.State.IDLE, controller.getProjectile().getState());
    }

    @Test
    void testWindXOffsetZeroInitially() {
        assertEquals(0.0, controller.getWindXOffset(1));
        assertEquals(0.0, controller.getWindXOffset(2));
    }

    @Test
    void testNoBurnInitially() {
        assertEquals(0, controller.getBurnTargetPlayer());
        assertEquals(0, controller.getBurnTicksLeft());
    }

    @Test
    void testNotFrozenInitially() {
        assertEquals(0, controller.getFrozenPlayer());
    }

    // ── Spacebar guards ───────────────────────────────────────────────────────

    @Test
    void testSpacebarWithoutAttackDoesNothing() {
        controller.handleSpacebar();
        assertEquals(Projectile.State.IDLE, controller.getProjectile().getState());
    }

    @Test
    void testFirstSpacebarStartsAiming() {
        controller.selectAttack(new NormalAttack());
        controller.handleSpacebar();
        assertEquals(Projectile.State.AIMING, controller.getProjectile().getState());
    }

    @Test
    void testSecondSpacebarFires() {
        controller.selectAttack(new NormalAttack());
        controller.handleSpacebar();
        controller.handleSpacebar();
        assertEquals(Projectile.State.FLYING, controller.getProjectile().getState());
    }

    // ── Turn switching ────────────────────────────────────────────────────────

    @Test
    void testTurnSwitchesAfterHit() {
        fireAndWait(new NormalAttack(), 15.0, 200);
        assertFalse(controller.isPlayer1Turn());
    }

    @Test
    void testTurnSwitchesAfterMiss() {
        fireAndWait(new NormalAttack(), Projectile.MIN_ANGLE, 300);
        assertFalse(controller.isPlayer1Turn());
    }

    @Test
    void testResultStringContainsHitOnHit() {
        String result = fireAndWait(new NormalAttack(), 15.0, 200);
        assertTrue(result.contains("HIT"));
    }

    @Test
    void testResultStringContainsMissedOnMiss() {
        String result = fireAndWait(new NormalAttack(), Projectile.MIN_ANGLE, 300);
        assertTrue(result.contains("MISSED"));
    }

    // ── Normal attack ─────────────────────────────────────────────────────────

    @Test
    void testNormalHitDealsDamageToP2() {
        fireAndWait(new NormalAttack(), 15.0, 200);
        assertEquals(9, p2.getHp());
    }

    @Test
    void testMissDealsNoDamage() {
        fireAndWait(new NormalAttack(), Projectile.MIN_ANGLE, 300);
        assertEquals(10, p2.getHp());
    }

    // ── Fire attack ───────────────────────────────────────────────────────────

    @Test
    void testFireHitSetsBurnOnP2() {
        fireAndWait(new FireAttack(), 15.0, 200);
        assertEquals(2, controller.getBurnTargetPlayer());
        assertEquals(StatusManager.BURN_TICKS, controller.getBurnTicksLeft());
    }

    @Test
    void testFireCooldownSetAfterHit() {
        fireAndWait(new FireAttack(), 15.0, 200);
        assertEquals(FireAttack.COOLDOWN, controller.getFireCooldown(true));
    }

    @Test
    void testBurnTickDealsDamage() {
        fireAndWait(new FireAttack(), 15.0, 200);
        int hpAfterHit = p2.getHp();
        for (int i = 0; i < StatusManager.BURN_FRAMES_PER_TICK; i++) controller.update();
        assertEquals(hpAfterHit - 1, p2.getHp());
    }

    @Test
    void testBurnTicksDecrementAfterEachTick() {
        fireAndWait(new FireAttack(), 15.0, 200);
        int ticksBefore = controller.getBurnTicksLeft();
        for (int i = 0; i < StatusManager.BURN_FRAMES_PER_TICK; i++) controller.update();
        assertEquals(ticksBefore - 1, controller.getBurnTicksLeft());
    }

    // ── Ice attack ────────────────────────────────────────────────────────────

    @Test
    void testIceHitFreezesP2() {
        fireAndWait(new IceAttack(), 15.0, 200);
        assertEquals(2, controller.getFrozenPlayer());
    }

    @Test
    void testIceHitDealsDamageToP2() {
        fireAndWait(new IceAttack(), 15.0, 200);
        assertEquals(9, p2.getHp());
    }

    @Test
    void testIceCooldownSetAfterHit() {
        fireAndWait(new IceAttack(), 15.0, 200);
        assertEquals(IceAttack.COOLDOWN, controller.getIceCooldown(true));
    }

    @Test
    void testFrozenPlayerTurnIsAutoSkipped() {
        fireAndWait(new IceAttack(), 15.0, 200);
        for (int i = 0; i < StatusManager.FROZEN_DISPLAY_FRAMES + 10; i++) controller.update();
        assertTrue(controller.isPlayer1Turn());
    }

    // ── Wind attack ───────────────────────────────────────────────────────────

    @Test
    void testWindHitSetsWindOnP2() {
        fireAndWait(new WindAttack(), 15.0, 200);
        assertEquals(2, controller.getWindTargetPlayer());
        assertEquals(StatusManager.WIND_TURNS, controller.getWindTurnsLeft());
    }

    @Test
    void testWindHitDealsDamageToP2() {
        fireAndWait(new WindAttack(), 15.0, 200);
        assertEquals(9, p2.getHp());
    }

    @Test
    void testWindCooldownSetAfterHit() {
        fireAndWait(new WindAttack(), 15.0, 200);
        assertEquals(WindAttack.COOLDOWN, controller.getWindCooldown(true));
    }

    @Test
    void testWindXOffsetNonZeroAfterEffect() {
        fireAndWait(new WindAttack(), 15.0, 200);
        for (int i = 0; i < 10; i++) controller.update();
        assertNotEquals(0.0, controller.getWindXOffset(2));
    }

    // ── Cooldown decrement ────────────────────────────────────────────────────

    @Test
    void testFireCooldownDecrementsAfterOpponentTurn() {
        fireAndWait(new FireAttack(), 15.0, 200);
        int cdAfterHit = controller.getFireCooldown(true);
        fireAndWait(new NormalAttack(), Projectile.MIN_ANGLE, 300);
        assertEquals(cdAfterHit - 1, controller.getFireCooldown(true));
    }

    @Test
    void testIceIsRejectedWhileOnCooldown() {
        fireAndWait(new IceAttack(), 15.0, 200);
        for (int i = 0; i < StatusManager.FROZEN_DISPLAY_FRAMES + 10; i++) controller.update();
        assertTrue(controller.getIceCooldown(true) > 0);
        controller.selectAttack(new IceAttack());
        controller.handleSpacebar();
        assertEquals(Projectile.State.IDLE, controller.getProjectile().getState());
    }

    // ── Dog ability ───────────────────────────────────────────────────────────

    @Test
    void testDogAbilityHeals() {
        p1.setHp(7);
        controller.handleAbility();
        assertEquals(8, p1.getHp());
    }

    @Test
    void testDogAbilitySwitchesTurn() {
        controller.handleAbility();
        assertFalse(controller.isPlayer1Turn());
    }

    @Test
    void testDogAbilityCooldownSet() {
        controller.handleAbility();
        assertEquals(Controller.ABILITY_COOLDOWN, controller.getAbilityCooldown(true));
    }

    // ── Cat ability ───────────────────────────────────────────────────────────

    @Test
    void testCatAbilityLocksAngle() {
        CatPlayer catP1 = new CatPlayer();
        Controller c = new Controller(catP1, new DogPlayer());
        c.handleAbility();
        assertTrue(c.getProjectile().isAngleLocked());
    }

    @Test
    void testCatAbilityDoesNotSwitchTurn() {
        CatPlayer catP1 = new CatPlayer();
        Controller c = new Controller(catP1, new DogPlayer());
        c.handleAbility();
        assertTrue(c.isPlayer1Turn());
    }

    // ── Ability label ─────────────────────────────────────────────────────────

    @Test
    void testAbilityLabelForDogPlayer() {
        assertEquals("HEAL", controller.getAbilityLabel(true));
        assertEquals("HEAL", controller.getAbilityLabel(false));
    }

    @Test
    void testAbilityLabelForCatPlayer() {
        Controller c = new Controller(new CatPlayer(), new CatPlayer());
        assertEquals("AIM", c.getAbilityLabel(true));
        assertEquals("AIM", c.getAbilityLabel(false));
    }

    @Test
    void testMixedAbilityLabels() {
        Controller c = new Controller(new DogPlayer(), new CatPlayer());
        assertEquals("HEAL", c.getAbilityLabel(true));
        assertEquals("AIM",  c.getAbilityLabel(false));
    }
}
