package character;

/**
 * Enumerates the two special ability types available to playable characters.
 * Used by {@link game.Controller} and {@link renderer.SkillButtonRenderer}
 * to determine ability behaviour and button colour.
 */
public enum AbilityType {
    /** Dog's ability: restore 1 HP and end the turn. */
    HEAL,
    /** Cat's ability: lock a perfect-aim angle and cancel wind on self. */
    AIM
}
