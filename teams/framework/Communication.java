package framework;

import framework.constants.ChannelList;
import framework.constants.Order;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import framework.constants.Job;

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
            case Order.DRONE_DEFEND:
                updateOrder(ChannelList.DRONE_DEFEND, value);
                break;
            case Order.DRONE_SWARM:
                updateOrder(ChannelList.DRONE_SWARM, value);
                break;
            case Order.DRONE_ATTACK:
                updateOrder(ChannelList.DRONE_ATTACK, value);
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
