package justInTime;

import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;
import justInTime.constants.Symmetry;

public class MapBuilder {
    private static MapLocation nwCorner;
    private static int mapWidth;
    private static int mapHeight;
    private static int symmetryType;

    private static TerrainTile[][] map;

    public static void init(int mapWidthC, int mapHeightC, MapLocation nwCornerC, int symmetryTypeC) {
        mapWidth = mapWidthC;
        mapHeight = mapHeightC;
        nwCorner = nwCornerC;
        symmetryType = symmetryTypeC;

        map = new TerrainTile[mapHeightC][mapWidthC];
    }

    public static MapLocation processAndReturnUnknown(int bytecodeLimit) {
        //--Broadcast terrain tiles
        //--When we reach an unknown, check its symmetrical point
        //--If both are unknow, return that location (the one that we can go to easier)

        return new MapLocation(0, 0);
    }
}
