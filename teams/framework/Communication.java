package framework;

import framework.constants.ChannelList;
import battlecode.common.Clock;
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

    public static MapLocation getDistressLocation() throws GameActionException {
        //--Ignore old signals
        if (rc.readBroadcast(ChannelList.DISTRESS_SIGNAL_ROUND_NUMBER) < Clock.getRoundNum() - 2) {
            return null;
        }

        int x = rc.readBroadcast(ChannelList.DISTRESS_LOCATION_X);
        int y = rc.readBroadcast(ChannelList.DISTRESS_LOCATION_Y);
        return new MapLocation(x, y);
    }

    public static void setDistressLocation(MapLocation mapLocation) throws GameActionException {
        //--We can override the signal if it is old
        int roundSet = rc.readBroadcast(ChannelList.DISTRESS_SIGNAL_ROUND_NUMBER);
        if (roundSet < Clock.getRoundNum() - 3) {
            rc.broadcast(ChannelList.DISTRESS_SIGNAL_ROUND_NUMBER, Clock.getRoundNum());
            rc.broadcast(ChannelList.DISTRESS_SIGNAL_CREATOR, rc.getID());
            rc.broadcast(ChannelList.DISTRESS_LOCATION_X, mapLocation.x);
            rc.broadcast(ChannelList.DISTRESS_LOCATION_Y, mapLocation.y);
            return;
        }

        //--We can update the signal if we set it
        int distressOriginator = rc.readBroadcast(ChannelList.DISTRESS_SIGNAL_CREATOR);
        if (rc.getID() == distressOriginator) {
            rc.broadcast(ChannelList.DISTRESS_SIGNAL_ROUND_NUMBER, Clock.getRoundNum());
            rc.broadcast(ChannelList.DISTRESS_LOCATION_X, mapLocation.x);
            rc.broadcast(ChannelList.DISTRESS_LOCATION_Y, mapLocation.y);
        }
    }
}
