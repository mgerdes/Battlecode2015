package justInTime;

import battlecode.common.*;
import justInTime.constants.ChannelList;

public class Communication {
    private static RobotController rc;

    private static final int MAP_COORDINATE_ACTIVE_FLAG = 1000000;
    private static MapLocation myHq;
    private static int myType;
    private static int myId;

    public static void init(RobotController rcC) {
        rc = rcC;

        myHq = rc.senseHQLocation();
        myType = rc.getType().ordinal();
        myId = rc.getID();
    }

    public static void iNeedSupply() throws GameActionException {
        //--We are signalling one robot that needs supply
        //--When one unit needs supply, their request cannot be overriden unless
        //  the robot is of a higher priority

        //--The supply context channel is type, round number (T-RRRR)

        //--If I made the request, update it
        int currentRound = Clock.getRoundNum();
        int requestId = rc.readBroadcast(ChannelList.NEED_SUPPLY_ROBOT_ID);
        if (myId == requestId) {
            rc.broadcast(ChannelList.NEED_SUPPLY_CONTEXT,
                         myType * 10000 + currentRound);
            return;
        }

        //--If the request is expired, I can overwrite it
        int supplyContextValue = rc.readBroadcast(ChannelList.NEED_SUPPLY_CONTEXT);
        int supplyRound = supplyContextValue % 10000;
        if (supplyRound < currentRound - 1) {
            rc.broadcast(ChannelList.NEED_SUPPLY_ROBOT_ID, myId);
            rc.broadcast(ChannelList.NEED_SUPPLY_CONTEXT,
                         myType * 10000 + currentRound);
            return;
        }

        //--If my type is a higher priority, I can overwrite it
        int currentType = supplyContextValue / 10000;
        if (myType > currentType) {
            rc.broadcast(ChannelList.NEED_SUPPLY_ROBOT_ID, myId);
            rc.broadcast(ChannelList.NEED_SUPPLY_CONTEXT,
                         myType * 10000 + currentRound);
            return;
        }
    }

    public static int getRobotIdThatNeedsSupply() throws GameActionException {
        int supplyContextValue = rc.readBroadcast(ChannelList.NEED_SUPPLY_CONTEXT);
        int supplyRound = supplyContextValue % 10000;
        int currentRound = Clock.getRoundNum();

        //--If context is expired, return 0
        if (supplyRound < currentRound - 1) {
            return 0;
        }

        return rc.readBroadcast(ChannelList.NEED_SUPPLY_ROBOT_ID);
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
            return;
        }

        //--Or we can update it if this distress location is closer to HQ
        int x = rc.readBroadcast(ChannelList.DISTRESS_LOCATION_X);
        int y = rc.readBroadcast(ChannelList.DISTRESS_LOCATION_Y);
        MapLocation currentLocation = new MapLocation(x, y);
        if (mapLocation.distanceSquaredTo(myHq) < currentLocation.distanceSquaredTo(myHq)) {
            rc.broadcast(ChannelList.DISTRESS_SIGNAL_ROUND_NUMBER, Clock.getRoundNum());
            rc.broadcast(ChannelList.DISTRESS_LOCATION_X, mapLocation.x);
            rc.broadcast(ChannelList.DISTRESS_LOCATION_Y, mapLocation.y);
            return;
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
