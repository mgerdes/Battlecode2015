package soldiersAgainstBashers;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Orders {
    private static RobotController rc;

    private static final int BASHER_CHANNEL_X = 72;
    private static final int BASHER_CHANNEL_Y = 73;

    public static void sendBashersTo(MapLocation mapLocation) throws GameActionException {
        rc.broadcast(BASHER_CHANNEL_X, mapLocation.x);
        rc.broadcast(BASHER_CHANNEL_Y, mapLocation.y);
    }

    public static MapLocation getBasherDestination() throws GameActionException {
        int x = rc.readBroadcast(BASHER_CHANNEL_X);
        int y = rc.readBroadcast(BASHER_CHANNEL_Y);
        if (x == 0
                && y == 0) {
            return null;
        }

        return new MapLocation(x, y);
    }

    public static void init() {
        rc = RobotPlayer.rc;
    }
}
