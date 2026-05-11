package game;

/**
 * Stateless utility class for physics calculations used by {@link Controller}.
 * All methods are {@code static}; this class is not meant to be instantiated.
 */
public class PhysicsUtils {
    private static final double HIT_TOLERANCE          = 20.0;
    private static final double DEFAULT_PERFECT_ANGLE  = 15.0;
    private static final double MIN_VELOCITY           = 0.001;

    /**
     * Brute-force searches for the launch angle (in 0.1° steps) that lands the projectile
     * within {@code HIT_TOLERANCE} pixels of the opponent's hitbox centre.
     *
     * @param player1Turn {@code true} if P1 is shooting
     * @param launchOff   horizontal offset applied to the shooter's start X
     * @param targetOff   horizontal offset applied to the opponent's hitbox
     * @return best matching angle in degrees, or {@code DEFAULT_PERFECT_ANGLE} if none found
     */
    public static double calculatePerfectAngle(boolean player1Turn, double launchOff, double targetOff) {
        double sx = (player1Turn ? Projectile.P1_START_X : Projectile.P2_START_X) + launchOff;
        double sy = player1Turn ? Projectile.P1_START_Y : Projectile.P2_START_Y;

        double targetX = player1Turn
                ? (Projectile.P2_BOX_X1 + Projectile.P2_BOX_X2) / 2.0 + targetOff
                : (Projectile.P1_BOX_X1 + Projectile.P1_BOX_X2) / 2.0 + targetOff;
        double targetY = (Projectile.BOX_Y1 + Projectile.BOX_Y2) / 2.0;

        double sign = player1Turn ? 1.0 : -1.0;

        for (double a = Projectile.MIN_ANGLE; a <= Projectile.MAX_ANGLE; a += 0.1) {
            double rad = Math.toRadians(a);
            double pvx = sign * Projectile.LAUNCH_SPEED * Math.cos(rad);
            double pvy = -Projectile.LAUNCH_SPEED * Math.sin(rad);

            if (Math.abs(pvx) < MIN_VELOCITY) continue;

            double dx = targetX - sx;
            double t  = dx / pvx;

            if (t <= 0) continue;

            double predictedY = sy + pvy * t + 0.5 * Projectile.GRAVITY * t * t;
            if (Math.abs(predictedY - targetY) < HIT_TOLERANCE) return a;
        }
        return DEFAULT_PERFECT_ANGLE;
    }

    /**
     * Calculates the cosine-based horizontal displacement for the wind-affected player.
     *
     * @param playerNum        the player whose offset is requested (1 or 2)
     * @param windTargetPlayer the player currently affected by wind (0 if none)
     * @param windFrameCounter frames elapsed since wind was applied
     * @return pixel offset in the x-axis; {@code 0} if the player is not wind-affected
     */
    public static double calculateWindXOffset(int playerNum, int windTargetPlayer, int windFrameCounter) {
        if (windTargetPlayer != playerNum) return 0;
        double baseX = playerNum == 1 ? 100.0 : 620.0;
        double distance = 400.0 - baseX;
        return distance * (1 - Math.cos(windFrameCounter * 0.06)) / 2.0;
    }
}
