package framework;

import framework.constants.ChannelList;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
    private static RobotController rc;

    private static final int MAP_COORDINATE_ACTIVE_FLAG = 1000000;

    public static void init(RobotController rcC) {
        rc = rcC;
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

    public static void iAmASupplyTower() throws GameActionException {
        //--If count is expired, set to 1
        //--Otherwise increment the count by 1
        int lastUpdated = rc.readBroadcast(ChannelList.SUPPLY_DEPOT_ROUND_UPDATED);
        int currentRound = Clock.getRoundNum();
        if (lastUpdated < currentRound) {
            rc.broadcast(ChannelList.SUPPLY_DEPOT_ROUND_UPDATED, currentRound);
            rc.broadcast(ChannelList.SUPPLY_DEPOT_COUNT, 1);
        }
        else {
            int currentCount = rc.readBroadcast(ChannelList.SUPPLY_DEPOT_COUNT);
            rc.broadcast(ChannelList.SUPPLY_DEPOT_COUNT, currentCount + 1);
        }
    }

    public static MapLocation readMapLocationFromChannel(int channel) throws GameActionException {
        int x = rc.readBroadcast(channel);
        int y = rc.readBroadcast(channel + 1);
        if (!isActiveCoordinate(x)) {
            return null;
        }

        return new MapLocation(decodeCoordinate(x), y);
    }

    public static void setMapLocationOnChannel(MapLocation location, int channel) throws GameActionException {
        rc.broadcast(channel, encodeCoordinate(location.x));
        rc.broadcast(channel + 1, location.y);
    }

    public static void towerReportVoidSquareCount(MapLocation towerLocation, int count) throws
            GameActionException {
        int currentLowCount = rc.readBroadcast(ChannelList.TOWER_VOID_COUNT);
        if (count < currentLowCount) {
            rc.broadcast(ChannelList.TOWER_VOID_COUNT, count);
            setMapLocationOnChannel(towerLocation, ChannelList.OUR_TOWER_WITH_LOWEST_VOID_COUNT);
        }
    }

    private static boolean isActiveCoordinate(int coordinate) {
        if (coordinate < 0) {
            return coordinate <= MAP_COORDINATE_ACTIVE_FLAG;
        }

        return coordinate >= MAP_COORDINATE_ACTIVE_FLAG;
    }

    private static int encodeCoordinate(int value) {
        if (value < 0) {
            return value - MAP_COORDINATE_ACTIVE_FLAG;
        }

        return value + MAP_COORDINATE_ACTIVE_FLAG;
    }

    private static int decodeCoordinate(int value) {
        if (value < 0) {
            return value + MAP_COORDINATE_ACTIVE_FLAG;
        }

        return value - MAP_COORDINATE_ACTIVE_FLAG;
    }
}
