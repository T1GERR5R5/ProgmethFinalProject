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

    /** Helper: ยิงและรอให้กระสุนตก */
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

    // ── Core Controller Logic ────────────────────────────────────────────────

    @Test
    void testInitiallyP1TurnAndIdle() {
        assertTrue(controller.isPlayer1Turn());
        assertEquals(Projectile.State.IDLE, controller.getProjectile().getState());
    }

    @Test
    void testSpacebarFlow() {
        controller.selectAttack(new NormalAttack());

        // กดครั้งแรก -> เริ่มเล็ง
        controller.handleSpacebar();
        assertEquals(Projectile.State.AIMING, controller.getProjectile().getState());

        // กดครั้งที่สอง -> ยิง
        controller.handleSpacebar();
        assertEquals(Projectile.State.FLYING, controller.getProjectile().getState());
    }

    @Test
    void testTurnSwitchesAfterHitOrMiss() {
        // ทดสอบโดนเป้า
        fireAndWait(new NormalAttack(), 15.0, 200);
        assertFalse(controller.isPlayer1Turn(), "สลับเทิร์นเป็น P2 หลังจากยิงโดน");

        // ทดสอบยิงพลาด
        fireAndWait(new NormalAttack(), Projectile.MIN_ANGLE, 300);
        assertTrue(controller.isPlayer1Turn(), "สลับเทิร์นกลับมาเป็น P1 หลังจาก P2 ยิงพลาด");
    }

    // ── Cooldown Logic ────────────────────────────────────────────────────────

    @Test
    void testAttackCooldownIsEnforced() {
        // P1 ใช้ท่า Fire
        fireAndWait(new FireAttack(), 15.0, 200);
        assertTrue(controller.getFireCooldown(true) > 0);

        // ตอนนี้เป็นตา P2 ยิงทิ้ง 1 ที
        fireAndWait(new NormalAttack(), 15.0, 200);

        // กลับมาตา P1 พยายามเลือกท่า Fire ซ้ำ
        controller.selectAttack(new FireAttack());
        controller.handleSpacebar();

        // กระสุนไม่ควรยิงออกไป เพราะติดคูลดาวน์
        assertEquals(Projectile.State.IDLE, controller.getProjectile().getState());
    }

    // ── Ability Logic ─────────────────────────────────────────────────────────

    @Test
    void testDogAbilityHealsAndSwitchesTurn() {
        p1.setHp(7);
        controller.handleAbility();

        assertEquals(8, p1.getHp(), "เลือดควรเด้งขึ้น 1");
        assertFalse(controller.isPlayer1Turn(), "ใช้สกิลหมาเสร็จต้องจบเทิร์น");
        assertEquals(Controller.ABILITY_COOLDOWN, controller.getAbilityCooldown(true));
    }

    @Test
    void testCatAbilityLocksAngleWithoutSwitchingTurn() {
        CatPlayer catP1 = new CatPlayer();
        controller = new Controller(catP1, new DogPlayer());

        controller.handleAbility();

        assertTrue(controller.getProjectile().isAngleLocked(), "องศาควรถูกล็อก (Perfect Aim)");
        assertTrue(controller.isPlayer1Turn(), "ใช้สกิลแมว ไม่จบเทิร์น (ยังยิงต่อได้)");
    }
}