package Charactor;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DogPlayerTest {

    private DogPlayer dog;

    @BeforeEach
    void setUp() {
        dog = new DogPlayer();
    }

    // ── Identity ─────────────────────────────────────────────────────────────

    @Test
    void testName() {
        assertEquals("Dog", dog.getName());
    }

    @Test
    void testAbilityLabel() {
        assertEquals("HEAL", dog.getAbilityLabel());
    }

    @Test
    void testSpritePath() {
        assertEquals("/images/dog.png", dog.getSpritePath());
    }

    // ── HP management ────────────────────────────────────────────────────────

    @Test
    void testInitialHp() {
        assertEquals(10, dog.getHp());
    }

    @Test
    void testMaxHp() {
        assertEquals(10, dog.getMaxHp());
    }

    @Test
    void testDecreaseHp() {
        dog.decreaseHp(3);
        assertEquals(7, dog.getHp());
    }

    @Test
    void testSetHp() {
        dog.setHp(4);
        assertEquals(4, dog.getHp());
    }

    // ── Ability ──────────────────────────────────────────────────────────────

    @Test
    void testHealIncreasesHpByOne() {
        dog.setHp(7);
        dog.ability();
        assertEquals(8, dog.getHp());
    }

    @Test
    void testHealDoesNotExceedMaxHp() {
        dog.setHp(dog.getMaxHp());
        dog.ability();
        assertEquals(dog.getMaxHp(), dog.getHp());
    }

    @Test
    void testNotPerfectAimReady() {
        assertFalse(dog.isPerfectAimReady());
    }

    @Test
    void testResetAbilityEffectDoesNothing() {
        dog.resetAbilityEffect();
        assertFalse(dog.isPerfectAimReady());
    }
}
