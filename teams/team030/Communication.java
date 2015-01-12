package team030;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import team030.util.ChannelList;
import team030.util.Job;

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
}
