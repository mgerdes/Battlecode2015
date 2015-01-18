package justInTime;

import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;
import justInTime.constants.Symmetry;

public class MapBuilder {
    private static int mapWidth;
    private static int mapHeight;

    private static int smallestX;
    private static int smallestY;

    private static int symmetryType;

    private static MapLocation myHqLocation;

    private static TerrainTile[][] map;

    public static void init(int mapWidthC,
                            int mapHeightC,
                            MapLocation nwCornerC,
                            int symmetryTypeC,
                            MapLocation myHqC) {
        mapWidth = mapWidthC;
        mapHeight = mapHeightC;
        smallestX = nwCornerC.x;
        smallestY = nwCornerC.y;
        symmetryType = symmetryTypeC;
        myHqLocation = myHqC;

        map = new TerrainTile[mapHeightC][mapWidthC];
    }

    public static MapLocation processAndReturnUnknown(int bytecodeLimit) {
        //--Broadcast terrain tiles
        //--When we reach an unknown, check its symmetrical point
        //--If both are unknow, return that location (the one that we can go to easier)

        return new MapLocation(0, 0);
    }

    public static MapLocation getReflected(MapLocation location) {
        switch (symmetryType) {
            case Symmetry.VERTICAL:
                return new MapLocation(2 * smallestX + mapWidth - location.x - 1, location.y);
            case Symmetry.HORIZONTAL:
                return new MapLocation(location.x, 2 * smallestY + mapHeight - location.y - 1);
            case Symmetry.ROTATION:
                return new MapLocation(2 * smallestX + mapHeight - location.x - 1,
                                       2 * smallestY + mapHeight - location.y - 1);
        }

        return null;
    }
}
