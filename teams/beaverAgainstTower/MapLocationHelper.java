package beaverAgainstTower;

import battlecode.common.MapLocation;

public class MapLocationHelper {
    public static MapLocation getMidpoint(MapLocation loc1, MapLocation loc2) {
        return new MapLocation((loc1.x + loc2.x) / 2, (loc1.y + loc2.y) / 2);
    }
}
