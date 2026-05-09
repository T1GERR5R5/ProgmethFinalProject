package attack;

import character.DogPlayer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AttackTest {

    private DogPlayer target;

    @BeforeEach
    void setUp() {
        target = new DogPlayer();
    }

    // ── NormalAttack ─────────────────────────────────────────────────────────

    @Test
    void testNormalAttackDealsOneDamage() {
        int before = target.getHp();
        new NormalAttack().attack(target);
        assertEquals(before - 1, target.getHp());
    }

    @Test
    void testNormalAttackStacksWithMultipleHits() {
        new NormalAttack().attack(target);
        new NormalAttack().attack(target);
        new NormalAttack().attack(target);
        assertEquals(7, target.getHp());
    }

    @Test
    void testNormalAttackCanReduceHpToZero() {
        target.setHp(1);
        new NormalAttack().attack(target);
        assertEquals(0, target.getHp());
    }

    // ── FireAttack ───────────────────────────────────────────────────────────

    @Test
    void testFireAttackDealsNoDirectDamage() {
        int before = target.getHp();
        new FireAttack().attack(target);
        assertEquals(before, target.getHp());
    }

    // ── IceAttack ────────────────────────────────────────────────────────────

    @Test
    void testIceAttackDealsNoDirectDamage() {
        int before = target.getHp();
        new IceAttack().attack(target);
        assertEquals(before, target.getHp());
    }

    // ── WindAttack ───────────────────────────────────────────────────────────

    @Test
    void testWindAttackDealsNoDirectDamage() {
        int before = target.getHp();
        new WindAttack().attack(target);
        assertEquals(before, target.getHp());
    }
}
