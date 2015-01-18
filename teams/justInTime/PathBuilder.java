package justInTime;

import battlecode.common.MapLocation;

public class PathBuilder {
    public static void setup(MapLocation[] towerList, MapLocation enemyHq) {
        //--Only the HQ should call this
        //--This should only be called once

        //--broadcast all of destinations (point of interests)
        //--broad the number of POI
    }

    public static void build(int bytecodeLimit) {
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
