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

    public static final double LAUNCH_SPEED  = 16.0;
    public static final double GRAVITY       = 0.35;
    public static final double MIN_ANGLE     = 3;
    public static final double MAX_ANGLE     = 80;
    public static final double ANGLE_SPEED   = 0.5; // degrees per frame, auto-oscillation
    private static final int   TRAIL_MAX     = 10;

    // P1 sprite at x=100, P2 sprite at x=620, both width=80
    public static final double P1_START_X = 182, P1_START_Y = 262; // P1 right edge
    public static final double P2_START_X = 618, P2_START_Y = 262; // P2 left edge

    // Hit boxes matching the drawn sprite bounds
    public static final double P1_BOX_X1 = 98,  P1_BOX_X2 = 182;
    public static final double P2_BOX_X1 = 618, P2_BOX_X2 = 702;
    public static final double BOX_Y1 = 238, BOX_Y2 = 322;

    public void startAiming(boolean isPlayer1Turn) {
        this.isPlayer1Turn = isPlayer1Turn;
        this.angle    = MIN_ANGLE; // always sweep from flat upward
        this.angleDir = 1;
        this.hit      = false;
        trail.clear();
        state = State.AIMING;
    }

    public void fire() {
        if (state != State.AIMING) return;
        x  = isPlayer1Turn ? P1_START_X : P2_START_X;
        y  = isPlayer1Turn ? P1_START_Y : P2_START_Y;
        double rad = Math.toRadians(angle);
        vx = isPlayer1Turn ? LAUNCH_SPEED * Math.cos(rad) : -LAUNCH_SPEED * Math.cos(rad);
        vy = -LAUNCH_SPEED * Math.sin(rad);
        trail.clear();
        state = State.FLYING;
    }

    public void update() {
        // Auto-oscillate angle while aiming
        if (state == State.AIMING) {
            angle += ANGLE_SPEED * angleDir;
            if (angle >= MAX_ANGLE) { angle = MAX_ANGLE; angleDir = -1; }
            else if (angle <= MIN_ANGLE) { angle = MIN_ANGLE; angleDir =  1; }
            return;
        }

        if (state != State.FLYING) return;

        trail.add(new double[]{x, y});
        if (trail.size() > TRAIL_MAX) trail.remove(0);

        x += vx;
        y += vy;
        vy += GRAVITY;

        // Hit detection
        boolean inP2 = x >= P2_BOX_X1 && x <= P2_BOX_X2 && y >= BOX_Y1 && y <= BOX_Y2;
        boolean inP1 = x >= P1_BOX_X1 && x <= P1_BOX_X2 && y >= BOX_Y1 && y <= BOX_Y2;

        if ((isPlayer1Turn && inP2) || (!isPlayer1Turn && inP1)) {
            hit = true;
            state = State.IDLE;
            return;
        }

        // Miss: off-screen or below ground
        if (x < 0 || x > 800 || y > 306) {
            hit = false;
            state = State.IDLE;
        }
    }

    // Returns (x,y) sample points along the predicted parabolic arc.
    // Uses kinematic equations: x(t) = x0 + vx*t,  y(t) = y0 + vy*t + 0.5*g*t²
    public double[][] getTrajectoryPoints(int count) {
        double sx = isPlayer1Turn ? P1_START_X : P2_START_X;
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

    public void reset() { state = State.IDLE; hit = false; trail.clear(); }

    public State   getState()      { return state; }
    public boolean isHit()         { return hit; }
    public double  getX()          { return x; }
    public double  getY()          { return y; }
    public double  getAngle()      { return angle; }
    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    public List<double[]> getTrail() { return trail; }
}
