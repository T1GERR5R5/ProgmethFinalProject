package character;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CatPlayerTest {

    private CatPlayer cat;

    @BeforeEach
    void setUp() {
        cat = new CatPlayer();
    }

    // ── Identity ─────────────────────────────────────────────────────────────

    @Test
    void testName() {
        assertEquals("Cat", cat.getName());
    }

    @Test
    void testAbilityLabel() {
        assertEquals("AIM", cat.getAbilityLabel());
    }

    @Test
    void testSpritePath() {
        assertEquals("/images/cat.png", cat.getSpritePath());
    }

    // ── HP management ────────────────────────────────────────────────────────

    @Test
    void testInitialHp() {
        assertEquals(10, cat.getHp());
    }

    @Test
    void testMaxHp() {
        assertEquals(10, cat.getMaxHp());
    }

    @Test
    void testDecreaseHp() {
        cat.decreaseHp(4);
        assertEquals(6, cat.getHp());
    }

    @Test
    void testSetHp() {
        cat.setHp(2);
        assertEquals(2, cat.getHp());
    }

    // ── Ability ──────────────────────────────────────────────────────────────

    @Test
    void testAbilitySetsPerfectAimReady() {
        assertFalse(cat.isPerfectAimReady());
        cat.ability();
        assertTrue(cat.isPerfectAimReady());
    }

    @Test
    void testResetAbilityEffectClearsPerfectAim() {
        cat.ability();
        cat.resetAbilityEffect();
        assertFalse(cat.isPerfectAimReady());
    }

    @Test
    void testAbilityDoesNotChangeHp() {
        cat.setHp(5);
        cat.ability();
        assertEquals(5, cat.getHp());
    }
}
