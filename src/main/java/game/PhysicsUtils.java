package game;

public class PhysicsUtils {
    private static final double HIT_TOLERANCE = 20.0;
    private static final double DEFAULT_PERFECT_ANGLE = 15.0;
    private static final double MIN_VELOCITY = 0.001;

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

    public static double calculateWindXOffset(int playerNum, int windTargetPlayer, int windFrameCounter) {
        if (windTargetPlayer != playerNum) return 0;
        double baseX = playerNum == 1 ? 100.0 : 620.0;
        double distance = 400.0 - baseX;
        return distance * (1 - Math.cos(windFrameCounter * 0.06)) / 2.0;
    }
}