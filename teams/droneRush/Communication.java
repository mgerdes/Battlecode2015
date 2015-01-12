package droneRush;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
    private static RobotController rc;
    private static MapLocation enemyInBaseLocation;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static MapLocation getAttackLocation() throws GameActionException {
        int x = rc.readBroadcast(ChannelList.STRUCTURE_TO_ATTACK_X);
        int y = rc.readBroadcast(ChannelList.STRUCTURE_TO_ATTACK_Y);
        return new MapLocation(x, y);
    }

    public static void broadcastEnemyInBase(MapLocation location) throws GameActionException {
        rc.broadcast(ChannelList.ENEMY_IN_BASE_ROUND, Clock.getRoundNum());
        rc.broadcast(ChannelList.ENEMY_IN_BASE_X, location.x);
        rc.broadcast(ChannelList.ENEMY_IN_BASE_Y, location.y);
    }

    public static MapLocation getEnemyInBaseLocation() throws GameActionException {
        int x = rc.readBroadcast(ChannelList.ENEMY_IN_BASE_X);
        int y = rc.readBroadcast(ChannelList.ENEMY_IN_BASE_Y);
        return new MapLocation(x, y);
    }
}
