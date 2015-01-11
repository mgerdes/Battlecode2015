package droneRush;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
    private static RobotController rc;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static MapLocation getAttackLocation() throws GameActionException {
        int x = rc.readBroadcast(ChannelList.STRUCTURE_TO_ATTACK_X);
        int y = rc.readBroadcast(ChannelList.STRUCTURE_TO_ATTACK_Y);
        return new MapLocation(x, y);
    }

    public static MapLocation getFortifyPoint(int point) throws GameActionException {
        int x = rc.readBroadcast(ChannelList.FORTIFY_POINT_START + 2 * point);
        int y = rc.readBroadcast(ChannelList.FORTIFY_POINT_START + 2 * point + 1);
        return new MapLocation(x, y);
    }
}
