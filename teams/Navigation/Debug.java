package navigation;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class Debug {
    private static int previousRound = 0;
    private static int channel = 0;

    public static void setIndicatorString(String message, RobotController rc) {
        int roundNumber = Clock.getRoundNum();
        if (roundNumber != previousRound) {
            channel = 0;
            previousRound = roundNumber;
        }

        rc.setIndicatorString(channel % 3, String.format("round %d message %d: %s", Clock.getRoundNum(), channel, message));
        channel++;
    }
}
