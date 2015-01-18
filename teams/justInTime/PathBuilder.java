package justInTime;

import battlecode.common.GameActionException;
import justInTime.constants.ChannelList;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class PathBuilder {
    static RobotController rc;

    public static void setup(MapLocation[] towerList, MapLocation enemyHq) {
        //--Only the HQ should call this
        //--This should only be called once

        //--broadcast all of destinations (point of interests)
        //--broad the number of POI

    }

    public static void build(int bytecodeLimit) throws GameActionException {
        //--Read the state (last location and point of interest number)
        //--Do the work and broadcast the work
        //--Broadcast the state
        return;
    }

    public static boolean isComplete() {
        //--Returns true when all paths are done and broadcast
        return false;
    }
}
