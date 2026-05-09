package game;

import java.util.LinkedList;
import java.util.List;

public class Projectile {

    public enum State { IDLE, AIMING, FLYING }

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

    public static final double LAUNCH_SPEED = 16.0;
    public static final double GRAVITY      = 0.35;
    public static final double MIN_ANGLE    = 3;
    public static final double MAX_ANGLE    = 80;
    public static final double ANGLE_SPEED  = 0.5;

    private static final int TRAIL_MAX = 10;

    public static final double GROUND_Y   = 270.0;
    public static final double P1_START_X = 182;
    public static final double P1_START_Y = GROUND_Y + 25;
    public static final double P2_START_X = 618;
    public static final double P2_START_Y = GROUND_Y + 25;
    public static final double P1_BOX_X1  = 98;
    public static final double P1_BOX_X2  = 182;
    public static final double P2_BOX_X1  = 618;
    public static final double P2_BOX_X2  = 702;
    public static final double BOX_Y1     = GROUND_Y + 5;
    public static final double BOX_Y2     = GROUND_Y + 85;

    public void startAiming(boolean player1Turn) {
        this.player1Turn = player1Turn;
        if (!angleLocked) this.angle = MIN_ANGLE;
        this.angleDir = 1;
        this.hit      = false;
        trail.clear();
        state = State.AIMING;
    }

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

    public void reset() {
        state = State.IDLE; hit = false; trail.clear();
        launchXOffset = 0; targetXOffset = 0; angleLocked = false;
    }

    public void setLaunchOffset(double off) { launchXOffset = off; }
    public void setTargetOffset(double off) { targetXOffset = off; }

    public void    setLockedAngle(double a) { angle = a; angleLocked = true; }
    public void    clearAngleLock()         { angleLocked = false; }

    public double  getLaunchXOffset()       { return launchXOffset; }
    public boolean isAngleLocked()          { return angleLocked; }
    public State   getState()               { return state; }
    public boolean isHit()                  { return hit; }
    public double  getX()                   { return x; }
    public double  getY()                   { return y; }
    public double  getAngle()               { return angle; }
    public boolean isPlayer1Turn()          { return player1Turn; }
    public List<double[]> getTrail()        { return trail; }
}
