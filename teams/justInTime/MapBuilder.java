package justInTime;

import battlecode.common.*;
import justInTime.constants.ChannelList;
import justInTime.constants.Symmetry;

public class MapBuilder {
    private static RobotController rc;

    private static int mapWidth;
    private static int mapHeight;
    private static int minX;
    private static int minY;

    private static int symmetryType;

    private static int xLoop = 0;
    private static int yLoop = 0;
    private static boolean[][] wasBroadcast;

    public static void init(int mapWidthC,
                            int mapHeightC,
                            MapLocation nwCornerC,
                            int symmetryTypeC,
                            RobotController rcC) {
        mapWidth = mapWidthC;
        mapHeight = mapHeightC;
        minX = nwCornerC.x;
        minY = nwCornerC.y;
        symmetryType = symmetryTypeC;
        rc = rcC;

        wasBroadcast = new boolean[mapWidth][mapHeight];
    }

    public static boolean processUntilComplete(int bytecodeLimit) throws GameActionException {
        //--Broadcast terrain tiles
        //--When we reach an unknown, check its symmetrical point
        //--If both are unknown, return that location (the one that we can go to easier)

        int initialBytecodeValue = Clock.getBytecodeNum();
        int finalBytecodeValue = initialBytecodeValue + bytecodeLimit;
        for (; xLoop < mapWidth; xLoop++) {
            for (; yLoop < mapHeight; yLoop++) {
                System.out.println(xLoop + " " + yLoop);
                if (Clock.getBytecodeNum() > finalBytecodeValue) {
                    System.out.println("stopping due to bytecodes");
                    return false;
                }

                if (wasBroadcast[xLoop][yLoop]) {
                    continue;
                }

                MapLocation locationToCheck = getAbsoluteMapLocationForRelativeCoordinates(xLoop, yLoop);
                MapLocation reflected = getReflectedMapLocation(locationToCheck);
                TerrainTile tile = rc.senseTerrainTile(locationToCheck);
                System.out.printf("sensed %s : %s\n", locationToCheck, tile);
                if (tile == TerrainTile.UNKNOWN) {
                    tile = rc.senseTerrainTile(reflected);
                    System.out.printf("reflected %s : %s\n", reflected, tile);
                }

                if (tile == TerrainTile.UNKNOWN) {
                    System.out.println("need location " + locationToCheck);
                    Communication.setMapLocationOnChannel(locationToCheck, ChannelList.LOCATION_TO_SURVEY);
                    return false;
                }
                else {
                    int offsetForFirstLocation = getHashForRelativeCoordinates(xLoop, yLoop);
                    int offsetForReflectedLocation = getReflectedChannelOffset(offsetForFirstLocation);
                    rc.broadcast(
                            ChannelList.NW_CORNER_TERRAIN_TILE + offsetForFirstLocation,
                            tile.ordinal());
                    rc.broadcast(
                            ChannelList.NW_CORNER_TERRAIN_TILE + offsetForReflectedLocation,
                            tile.ordinal());
                    wasBroadcast[getXValue(offsetForFirstLocation)][getYValue(offsetForFirstLocation)] = true;
                    wasBroadcast[getXValue(offsetForReflectedLocation)][getYValue(offsetForReflectedLocation)] = true;
                }
            }

            if (xLoop < mapWidth - 1) {
                yLoop = 0;
            }
        }

        System.out.printf("\nxLoop:%d\nmapWidth:%s\nyLoop:%s\nmapHeight:%s\n",
                          xLoop,
                          mapWidth,
                          yLoop,
                          mapHeight);

        return xLoop == mapWidth
                && yLoop == mapHeight;
    }

    private static MapLocation getAbsoluteMapLocationForRelativeCoordinates(int x, int y) {
        return new MapLocation(minX + x, minY + y);
    }

    public static MapLocation getReflectedMapLocation(MapLocation location) {
        switch (symmetryType) {
            case Symmetry.VERTICAL:
                return new MapLocation(2 * minX + mapWidth - location.x - 1, location.y);
            case Symmetry.HORIZONTAL:
                return new MapLocation(location.x, 2 * minY + mapHeight - location.y - 1);
            case Symmetry.ROTATION:
                return new MapLocation(
                        2 * minX + mapHeight - location.x - 1,
                        2 * minY + mapHeight - location.y - 1);
        }

        return null;
    }

    public static int getReflectedChannelOffset(int relativeAddress) {
        switch (symmetryType) {
            case Symmetry.VERTICAL:
                int verticalX = mapWidth - getXValue(relativeAddress) - 1;
                return getHashForRelativeCoordinates(verticalX, getYValue(relativeAddress));
            case Symmetry.HORIZONTAL:
                int horizontalY = mapHeight - getYValue(relativeAddress) - 1;
                return getHashForRelativeCoordinates(getXValue(relativeAddress), horizontalY);
            case Symmetry.ROTATION:
                int rotatedX = mapWidth - getXValue(relativeAddress) - 1;
                int rotatedY = mapHeight - getYValue(relativeAddress) - 1;
                return getHashForRelativeCoordinates(rotatedX, rotatedY);
        }

        return -1;
    }

    private static int getXValue(int channel) {
        return channel / 120;
    }

    private static int getYValue(int channel) {
        return channel % 120;
    }

    private static int getHashForRelativeCoordinates(int x, int y) {
        return 120 * x + y;
    }
}
