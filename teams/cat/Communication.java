package cat;

import cat.constants.ChannelList;
import cat.constants.Order;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import cat.constants.Job;

public class Communication {
    private static RobotController rc;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    //--Locations are broadcast on three channels
    //--The format is (X, Y, claimed)
    //--Claimed will be the round number that the robot last claimed it

    public static MapLocation getMapLocation(int firstChannel) throws GameActionException {
        int x = rc.readBroadcast(firstChannel);
        int y = rc.readBroadcast(firstChannel + 1);
        return new MapLocation(x, y);
    }

    public static void setMapLocation(int firstChannel, MapLocation location) throws GameActionException {
        rc.broadcast(firstChannel, location.x);
        rc.broadcast(firstChannel + 1, location.y);
    }

    public static void broadcastLocations(MapLocation[] positions, int firstChannel) throws GameActionException {
        for (int i = 0; i < positions.length; i++) {
            setMapLocation(firstChannel + 3 * i, positions[i]);
        }
    }

    public static MapLocation getUnclaimedLocation(int firstChannel) throws GameActionException {
        while (positionClaimed(firstChannel)) {
            firstChannel += 3;
        }

        rc.broadcast(firstChannel + 2, Clock.getRoundNum());
        return getMapLocation(firstChannel);
    }

    private static boolean positionClaimed(int firstChannel) throws GameActionException {
        //--It is claimed if a robot updated it this round
        return rc.readBroadcast(firstChannel + 2) == Clock.getRoundNum();
    }

    public static void weNeed(int job) throws GameActionException {
        switch (job) {
            case Job.SUPPLY_MINERS:
                rc.broadcast(ChannelList.SUPPLY_MINERS_JOB_IS_NEEDED, Clock.getRoundNum());
        }
    }

    public static boolean someoneIsNeededFor(int job) throws GameActionException {
        switch (job) {
            case Job.SUPPLY_MINERS:
                return rc.readBroadcast(ChannelList.SUPPLY_MINERS_JOB_IS_NEEDED) >= Clock.getRoundNum() - 1
                        && rc.readBroadcast(ChannelList.SUPPLY_MINERS_JOB_REPORTING) < Clock.getRoundNum();
            default:
                return false;
        }
    }

    public static void reportTo(int job) throws GameActionException {
        switch (job) {
            case Job.SUPPLY_MINERS:
                rc.broadcast(ChannelList.SUPPLY_MINERS_JOB_REPORTING, Clock.getRoundNum());
        }
    }

    public static void setOrder(int order, int value) throws GameActionException {
        switch (order) {
            case Order.SPAWN_MORE_MINERS:
                updateOrder(ChannelList.MORE_MINERS, value);
                break;
            case Order.SPAWN_MORE_DRONES:
                updateOrder(ChannelList.MORE_DRONES, value);
                break;
            case Order.SPAWN_MORE_TANKS:
                updateOrder(ChannelList.MORE_TANKS, value);
                break;
            case Order.SPAWN_MORE_LAUNCHERS:
                updateOrder(ChannelList.MORE_LAUNCHER, value);
                break;
            case Order.DRONE_DEFEND:
                updateOrder(ChannelList.DRONE_DEFEND, value);
                break;
            case Order.DRONE_SWARM:
                updateOrder(ChannelList.DRONE_SWARM, value);
                break;
            case Order.DRONE_ATTACK:
                updateOrder(ChannelList.DRONE_ATTACK, value);
                break;
            case Order.TANK_ATTACK:
                updateOrder(ChannelList.TANK_ATTACK, value);
                break;
            case Order.TANK_FORTIFY:
                updateOrder(ChannelList.TANK_FORTIFY, value);
                break;
        }
    }

    private static void updateOrder(int channel, int newValue) throws GameActionException {
        //--Only broadcast the value if it needs to be changed
        int currentValue = rc.readBroadcast(channel);
        if (newValue == Order.YES
                && currentValue == Order.NO) {
            rc.broadcast(channel, Order.YES);
        }
        else if (newValue == Order.NO
                && currentValue == Order.YES) {
            rc.broadcast(channel, Order.NO);
        }
    }
}
