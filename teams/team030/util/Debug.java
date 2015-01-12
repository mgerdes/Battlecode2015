package team030.util;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class Debug {
    public static void setString(int index, String message, RobotController rc) {
        rc.setIndicatorString(index, String.format("round: %s - %s", Clock.getRoundNum(), message));
    }
}
