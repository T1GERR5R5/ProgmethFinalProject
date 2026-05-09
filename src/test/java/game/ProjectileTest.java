package game;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectileTest {

    private Projectile proj;

    @BeforeEach
    void setUp() {
        proj = new Projectile();
    }

    // ── State machine ─────────────────────────────────────────────────────────

    @Test
    void testInitialStateIsIdle() {
        assertEquals(Projectile.State.IDLE, proj.getState());
    }

    @Test
    void testStartAimingTransitionsToAiming() {
        proj.startAiming(true);
        assertEquals(Projectile.State.AIMING, proj.getState());
    }

    @Test
    void testFireTransitionsToFlying() {
        proj.startAiming(true);
        proj.fire();
        assertEquals(Projectile.State.FLYING, proj.getState());
    }

    @Test
    void testFireFromIdleDoesNothing() {
        proj.fire();
        assertEquals(Projectile.State.IDLE, proj.getState());
    }

    @Test
    void testResetReturnsToIdle() {
        proj.startAiming(true);
        proj.fire();
        proj.reset();
        assertEquals(Projectile.State.IDLE, proj.getState());
    }

    @Test
    void testResetClearsTrail() {
        proj.startAiming(true);
        proj.setLockedAngle(15.0);
        proj.fire();
        for (int i = 0; i < 5; i++) proj.update();
        proj.reset();
        assertEquals(0, proj.getTrail().size());
    }

    @Test
    void testResetClearsHitFlag() {
        proj.startAiming(true);
        proj.setLockedAngle(15.0);
        proj.fire();
        for (int i = 0; i < 200; i++) {
            proj.update();
            if (proj.getState() == Projectile.State.IDLE) break;
        }
        proj.reset();
        assertFalse(proj.isHit());
    }

    // ── Launch position ──────────────────────────────────────────────────────

    @Test
    void testP1LaunchX() {
        proj.startAiming(true);
        proj.fire();
        assertEquals(Projectile.P1_START_X, proj.getX(), 0.001);
    }

    @Test
    void testP2LaunchX() {
        proj.startAiming(false);
        proj.fire();
        assertEquals(Projectile.P2_START_X, proj.getX(), 0.001);
    }

    @Test
    void testLaunchOffsetShiftsP1StartX() {
        proj.setLaunchOffset(30.0);
        proj.startAiming(true);
        proj.fire();
        assertEquals(Projectile.P1_START_X + 30.0, proj.getX(), 0.001);
    }

    // ── Direction ────────────────────────────────────────────────────────────

    @Test
    void testP1MovesRight() {
        proj.startAiming(true);
        proj.setLockedAngle(30.0);
        proj.fire();
        double startX = proj.getX();
        proj.update();
        assertTrue(proj.getX() > startX);
    }

    @Test
    void testP2MovesLeft() {
        proj.startAiming(false);
        proj.setLockedAngle(30.0);
        proj.fire();
        double startX = proj.getX();
        proj.update();
        assertTrue(proj.getX() < startX);
    }

    // ── Angle ────────────────────────────────────────────────────────────────

    @Test
    void testAngleOscillatesWhileAiming() {
        proj.startAiming(true);
        double a0 = proj.getAngle();
        proj.update();
        assertNotEquals(a0, proj.getAngle());
    }

    @Test
    void testAngleLockFreezesAngle() {
        proj.startAiming(true);
        proj.setLockedAngle(45.0);
        assertEquals(45.0, proj.getAngle(), 0.001);
        proj.update();
        assertEquals(45.0, proj.getAngle(), 0.001);
    }

    @Test
    void testIsAngleLockedAfterSet() {
        proj.setLockedAngle(20.0);
        assertTrue(proj.isAngleLocked());
    }

    @Test
    void testClearAngleLockUnlocks() {
        proj.setLockedAngle(20.0);
        proj.clearAngleLock();
        assertFalse(proj.isAngleLocked());
    }

    // ── Hit detection ────────────────────────────────────────────────────────

    @Test
    void testP1HitsP2AtFifteenDegrees() {
        proj.startAiming(true);
        proj.setLockedAngle(15.0);
        proj.fire();
        for (int i = 0; i < 200; i++) {
            proj.update();
            if (proj.getState() == Projectile.State.IDLE) break;
        }
        assertTrue(proj.isHit());
    }

    @Test
    void testP2HitsP1AtFifteenDegrees() {
        proj.startAiming(false);
        proj.setLockedAngle(15.0);
        proj.fire();
        for (int i = 0; i < 200; i++) {
            proj.update();
            if (proj.getState() == Projectile.State.IDLE) break;
        }
        assertTrue(proj.isHit());
    }

    @Test
    void testP1MissesAtMinimumAngle() {
        proj.startAiming(true);
        proj.setLockedAngle(Projectile.MIN_ANGLE);
        proj.fire();
        for (int i = 0; i < 300; i++) {
            proj.update();
            if (proj.getState() == Projectile.State.IDLE) break;
        }
        assertEquals(Projectile.State.IDLE, proj.getState());
        assertFalse(proj.isHit());
    }

    // ── Trail ────────────────────────────────────────────────────────────────

    @Test
    void testTrailGrowsWhileFlying() {
        proj.startAiming(true);
        proj.setLockedAngle(15.0);
        proj.fire();
        for (int i = 0; i < 5; i++) proj.update();
        assertTrue(proj.getTrail().size() > 0);
    }

    // ── Constants ────────────────────────────────────────────────────────────

    @Test
    void testGroundYConstant() {
        assertEquals(270.0, Projectile.GROUND_Y, 0.001);
    }

    @Test
    void testHitBoxYAlignedWithGround() {
        assertEquals(Projectile.GROUND_Y + 5,  Projectile.BOX_Y1, 0.001);
        assertEquals(Projectile.GROUND_Y + 85, Projectile.BOX_Y2, 0.001);
    }

    @Test
    void testLaunchYAlignedWithGround() {
        assertEquals(Projectile.GROUND_Y + 25, Projectile.P1_START_Y, 0.001);
        assertEquals(Projectile.GROUND_Y + 25, Projectile.P2_START_Y, 0.001);
    }

    @Test
    void testTrajectoryPointsHaveCorrectCount() {
        proj.startAiming(true);
        double[][] pts = proj.getTrajectoryPoints(10);
        assertEquals(10, pts.length);
    }

    @Test
    void testTrajectoryPointsAreForwardForP1() {
        proj.startAiming(true);
        proj.setLockedAngle(30.0);
        double[][] pts = proj.getTrajectoryPoints(5);
        assertTrue(pts[1][0] > pts[0][0], "P1 trajectory should move right");
    }
}
