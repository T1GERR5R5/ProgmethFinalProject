package game;

import java.util.LinkedList;
import java.util.List;

/**
 * Models a single projectile with a three-state lifecycle: IDLE → AIMING → FLYING → IDLE.
 * Handles parabolic physics, auto-oscillating aim angle, hit-box detection against both
 * players, and a rolling trail buffer used for rendering.
 *
 * <p>Also serves as the single source of truth for all world-coordinate constants
 * ({@link #GROUND_Y}, start positions, hit-box bounds).
 *
 * <h2>Physics (each FLYING frame)</h2>
 * <pre>
 *   x  += vx
 *   y  += vy
 *   vy += GRAVITY
 * </pre>
 */
public class Projectile {

    /** Lifecycle states. */
    public enum State {
        /** Not in use. */
        IDLE,
        /** Angle is oscillating; player can press SPACE to fire. */
        AIMING,
        /** Projectile is in flight; physics applied each frame. */
        FLYING
    }

    private State  state = State.IDLE;
    private double x, y, vx, vy;
    private double angle    = MIN_ANGLE;
    private int    angleDir = 1;
    private boolean hit         = false;
    private boolean player1Turn;
    private final List<double[]> trail = new LinkedList<>();

    private double  launchXOffset = 0;
    private double  targetXOffset = 0;
    private boolean angleLocked   = false;

    /** Launch speed in pixels per frame. */
    public static final double LAUNCH_SPEED = 16.0;
    /** Downward acceleration in pixels per frame². */
    public static final double GRAVITY      = 0.35;
    /** Minimum oscillation angle in degrees. */
    public static final double MIN_ANGLE    = 3;
    /** Maximum oscillation angle in degrees. */
    public static final double MAX_ANGLE    = 80;
    /** Degrees added to the angle each frame during oscillation. */
    public static final double ANGLE_SPEED  = 0.5;

    private static final int TRAIL_MAX = 10;

    /** Ground Y coordinate (pixels from top of canvas). All vertical constants derive from this. */
    public static final double GROUND_Y   = 270.0;
    /** X coordinate of P1's launch point. */
    public static final double P1_START_X = 182;
    /** Y coordinate of P1's launch point. */
    public static final double P1_START_Y = GROUND_Y + 25;
    /** X coordinate of P2's launch point. */
    public static final double P2_START_X = 618;
    /** Y coordinate of P2's launch point. */
    public static final double P2_START_Y = GROUND_Y + 25;
    /** Left edge of P1's hit box. */
    public static final double P1_BOX_X1  = 98;
    /** Right edge of P1's hit box. */
    public static final double P1_BOX_X2  = 182;
    /** Left edge of P2's hit box. */
    public static final double P2_BOX_X1  = 618;
    /** Right edge of P2's hit box. */
    public static final double P2_BOX_X2  = 702;
    /** Top edge of both hit boxes. */
    public static final double BOX_Y1     = GROUND_Y + 5;
    /** Bottom edge of both hit boxes. */
    public static final double BOX_Y2     = GROUND_Y + 85;

    /**
     * Transitions to {@link State#AIMING} for the given player's turn.
     * Resets the angle to {@link #MIN_ANGLE} unless locked (Cat perfect-aim).
     * @param player1Turn {@code true} if it is P1's turn
     */
    public void startAiming(boolean player1Turn) {
        this.player1Turn = player1Turn;
        if (!angleLocked) this.angle = MIN_ANGLE;
        this.angleDir = 1;
        this.hit      = false;
        trail.clear();
        state = State.AIMING;
    }

    /**
     * Transitions from {@link State#AIMING} to {@link State#FLYING}.
     * Computes initial velocity from the current angle and player direction.
     * Does nothing if state is not {@code AIMING}.
     */
    public void fire() {
        if (state != State.AIMING) return;
        x  = (player1Turn ? P1_START_X : P2_START_X) + launchXOffset;
        y  = player1Turn ? P1_START_Y : P2_START_Y;
        double rad = Math.toRadians(angle);
        vx = player1Turn ?  LAUNCH_SPEED * Math.cos(rad)
                         : -LAUNCH_SPEED * Math.cos(rad);
        vy = -LAUNCH_SPEED * Math.sin(rad);
        trail.clear();
        state = State.FLYING;
    }

    /**
     * Advances the projectile by one frame.
     * <ul>
     *   <li>AIMING: oscillates angle unless locked.</li>
     *   <li>FLYING: applies physics, appends trail point, checks hit boxes and out-of-bounds.</li>
     * </ul>
     */
    public void update() {
        if (state == State.AIMING) {
            if (!angleLocked) {
                angle += ANGLE_SPEED * angleDir;
                if (angle >= MAX_ANGLE) { angle = MAX_ANGLE; angleDir = -1; }
                else if (angle <= MIN_ANGLE) { angle = MIN_ANGLE; angleDir =  1; }
            }
            return;
        }

        if (state != State.FLYING) return;

        trail.add(new double[]{x, y});
        if (trail.size() > TRAIL_MAX) trail.remove(0);

        x  += vx;
        y  += vy;
        vy += GRAVITY;

        boolean inP2 = x >= P2_BOX_X1 + targetXOffset && x <= P2_BOX_X2 + targetXOffset
                    && y >= BOX_Y1 && y <= BOX_Y2;
        boolean inP1 = x >= P1_BOX_X1 + targetXOffset && x <= P1_BOX_X2 + targetXOffset
                    && y >= BOX_Y1 && y <= BOX_Y2;

        if ((player1Turn && inP2) || (!player1Turn && inP1)) {
            hit   = true;
            state = State.IDLE;
            return;
        }

        if (x < 0 || x > 800 || y > GROUND_Y + 90) {
            hit   = false;
            state = State.IDLE;
        }
    }

    /**
     * Computes sample positions along the predicted parabolic arc for the aiming preview.
     * @param count number of points to compute
     * @return array of {@code [x, y]} pairs sampled every 3 frames
     */
    public double[][] getTrajectoryPoints(int count) {
        double sx  = (player1Turn ? P1_START_X : P2_START_X) + launchXOffset;
        double sy  = player1Turn ? P1_START_Y : P2_START_Y;
        double rad = Math.toRadians(angle);
        double pvx = player1Turn ?  LAUNCH_SPEED * Math.cos(rad)
                                 : -LAUNCH_SPEED * Math.cos(rad);
        double pvy = -LAUNCH_SPEED * Math.sin(rad);

        double[][] pts = new double[count][2];
        for (int i = 0; i < count; i++) {
            double t = (i + 1) * 3.0;
            pts[i][0] = sx + pvx * t;
            pts[i][1] = sy + pvy * t + 0.5 * GRAVITY * t * t;
        }
        return pts;
    }

    /** Resets all state to IDLE and clears trail and offsets. */
    public void reset() {
        state = State.IDLE; hit = false; trail.clear();
        launchXOffset = 0; targetXOffset = 0; angleLocked = false;
    }

    /** @param off horizontal offset applied to the shooter's start X (wind) */
    public void setLaunchOffset(double off) { launchXOffset = off; }
    /** @param off horizontal offset applied to the target's hit box (wind) */
    public void setTargetOffset(double off) { targetXOffset = off; }

    /**
     * Locks the launch angle (Cat's perfect-aim ability).
     * @param a angle in degrees to lock to
     */
    public void    setLockedAngle(double a) { angle = a; angleLocked = true; }
    /** Releases the angle lock so oscillation resumes on the next {@link #startAiming} call. */
    public void    clearAngleLock()         { angleLocked = false; }

    /** @return current launch X offset */
    public double  getLaunchXOffset()       { return launchXOffset; }
    /** @return {@code true} if the angle is currently locked */
    public boolean isAngleLocked()          { return angleLocked; }
    /** @return current lifecycle state */
    public State   getState()               { return state; }
    /** @return {@code true} if the last flight ended with a hit */
    public boolean isHit()                  { return hit; }
    /** @return current X position in pixels */
    public double  getX()                   { return x; }
    /** @return current Y position in pixels */
    public double  getY()                   { return y; }
    /** @return current aiming angle in degrees */
    public double  getAngle()               { return angle; }
    /** @return {@code true} if the projectile belongs to P1's turn */
    public boolean isPlayer1Turn()          { return player1Turn; }
    /** @return the trail point list (each entry is {@code [x, y]}) */
    public List<double[]> getTrail()        { return trail; }
}
