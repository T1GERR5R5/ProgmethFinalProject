package Application;

import java.util.ArrayList;
import java.util.List;

public class Projectile {
    public enum State { IDLE, AIMING, FLYING }

    private State state = State.IDLE;
    private double x, y, vx, vy;
    private double angle    = MIN_ANGLE;
    private int    angleDir = 1; // +1 = sweeping up, -1 = sweeping down
    private boolean hit = false;
    private boolean isPlayer1Turn;
    private final List<double[]> trail = new ArrayList<>();

    // Wind offsets — set each frame by Controller
    private double launchXOffset = 0; // shifts the shooter's start position
    private double targetXOffset = 0; // shifts the target's hit box

    // Cat perfect-aim: freeze angle at the calculated value
    private boolean angleLocked = false;

    public static final double LAUNCH_SPEED  = 16.0;
    public static final double GRAVITY       = 0.35;
    public static final double MIN_ANGLE     = 3;
    public static final double MAX_ANGLE     = 80;
    public static final double ANGLE_SPEED   = 0.5;
    private static final int   TRAIL_MAX     = 10;

    public static final double GROUND_Y = 270.0;

    // P1 sprite at x=100, P2 sprite at x=620, both width=80
    public static final double P1_START_X = 182, P1_START_Y = GROUND_Y + 25;
    public static final double P2_START_X = 618, P2_START_Y = GROUND_Y + 25;

    // Hit boxes matching the drawn sprite bounds (sprite drawn at GROUND_Y+5, size 80x80)
    public static final double P1_BOX_X1 = 98,  P1_BOX_X2 = 182;
    public static final double P2_BOX_X1 = 618, P2_BOX_X2 = 702;
    public static final double BOX_Y1 = GROUND_Y + 5, BOX_Y2 = GROUND_Y + 85;

    public void startAiming(boolean isPlayer1Turn) {
        this.isPlayer1Turn = isPlayer1Turn;
        if (!angleLocked) this.angle = MIN_ANGLE; // preserve locked angle if set
        this.angleDir = 1;
        this.hit      = false;
        trail.clear();
        state = State.AIMING;
    }

    public void fire() {
        if (state != State.AIMING) return;
        x  = (isPlayer1Turn ? P1_START_X : P2_START_X) + launchXOffset;
        y  = isPlayer1Turn ? P1_START_Y : P2_START_Y;
        double rad = Math.toRadians(angle);
        vx = isPlayer1Turn ? LAUNCH_SPEED * Math.cos(rad) : -LAUNCH_SPEED * Math.cos(rad);
        vy = -LAUNCH_SPEED * Math.sin(rad);
        trail.clear();
        state = State.FLYING;
    }

    public void update() {
        // Auto-oscillate angle while aiming (skip if angle is locked by Cat ability)
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

        x += vx;
        y += vy;
        vy += GRAVITY;

        // Hit detection — target box shifts with wind offset
        boolean inP2 = x >= P2_BOX_X1 + targetXOffset && x <= P2_BOX_X2 + targetXOffset && y >= BOX_Y1 && y <= BOX_Y2;
        boolean inP1 = x >= P1_BOX_X1 + targetXOffset && x <= P1_BOX_X2 + targetXOffset && y >= BOX_Y1 && y <= BOX_Y2;

        if ((isPlayer1Turn && inP2) || (!isPlayer1Turn && inP1)) {
            hit = true;
            state = State.IDLE;
            return;
        }

        // Miss: off-screen or below ground
        if (x < 0 || x > 800 || y > GROUND_Y + 90) {
            hit = false;
            state = State.IDLE;
        }
    }

    // Returns (x,y) sample points along the predicted parabolic arc.
    // Uses kinematic equations: x(t) = x0 + vx*t,  y(t) = y0 + vy*t + 0.5*g*t²
    public double[][] getTrajectoryPoints(int count) {
        double sx = (isPlayer1Turn ? P1_START_X : P2_START_X) + launchXOffset;
        double sy = isPlayer1Turn ? P1_START_Y : P2_START_Y;
        double rad = Math.toRadians(angle);
        double pvx = isPlayer1Turn ? LAUNCH_SPEED * Math.cos(rad) : -LAUNCH_SPEED * Math.cos(rad);
        double pvy = -LAUNCH_SPEED * Math.sin(rad);

        double[][] pts = new double[count][2];
        for (int i = 0; i < count; i++) {
            double t = (i + 1) * 3.0; // sample every 3 frames
            pts[i][0] = sx + pvx * t;
            pts[i][1] = sy + pvy * t + 0.5 * GRAVITY * t * t;
        }
        return pts;
    }

    public void reset() { state = State.IDLE; hit = false; trail.clear(); launchXOffset = 0; targetXOffset = 0; angleLocked = false; }
    public void setLaunchOffset(double off) { launchXOffset = off; }
    public void setTargetOffset(double off) { targetXOffset = off; }
    public double getLaunchXOffset()        { return launchXOffset; }
    public void   setLockedAngle(double a)  { angle = a; angleLocked = true; }
    public void   clearAngleLock()          { angleLocked = false; }
    public boolean isAngleLocked()          { return angleLocked; }

    public State   getState()      { return state; }
    public boolean isHit()         { return hit; }
    public double  getX()          { return x; }
    public double  getY()          { return y; }
    public double  getAngle()      { return angle; }
    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    public List<double[]> getTrail() { return trail; }
}
