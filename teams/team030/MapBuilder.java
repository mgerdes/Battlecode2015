package team030;

import battlecode.common.*;
import team030.communication.Channel;
import team030.communication.Radio;
import team030.constants.Symmetry;

public class MapBuilder {
    private static RobotController rc;

    private static int mapWidth;
    private static int mapHeight;
    private static int minX;
    private static int minY;
    private static int symmetryType;

    private static int totalTileCount;
    private static int tilesBroadcastCount;

    private static int xLoop = 0;
    private static int yLoop = 0;

    private static final int MAX_BYTECODES_CONSUMED_IN_ONE_LOOP_PASS = 220;
    private static boolean[][] wasBroadcast;

    public static void init(RobotController rcC) throws GameActionException {
        rc = rcC;

        Radio.init(rcC);

        MapLocation nwMapCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);
        minX = nwMapCorner.x;
        minY = nwMapCorner.y;

        symmetryType = rc.readBroadcast(Channel.MAP_SYMMETRY);
        mapWidth = rcC.readBroadcast(Channel.MAP_WIDTH);
        mapHeight = rcC.readBroadcast(Channel.MAP_HEIGHT);

        totalTileCount = mapHeight * mapWidth;
        wasBroadcast = new boolean[mapWidth][mapHeight];
    }

    public static MapLocation getReflectedMapLocation(MapLocation location) {
        switch (symmetryType) {
            case Symmetry.VERTICAL:
                return new MapLocation(2 * minX + mapWidth - location.x - 1, location.y);
            case Symmetry.HORIZONTAL:
                return new MapLocation(location.x, 2 * minY + mapHeight - location.y - 1);
            case Symmetry.ROTATION:
                return new MapLocation(
                        2 * minX + mapWidth - location.x - 1,
                        2 * minY + mapHeight - location.y - 1);
        }

        return null;
    }

    //--This method should be called by one robot
    //--It will broadcast all of the terrain tiles that it can either sense or infer from symmetry
    public static boolean processUntilComplete(int bytecodeLimit) throws GameActionException {
        int initialBytecodeValue = Clock.getBytecodeNum();
        int finalBytecodeValue = initialBytecodeValue + bytecodeLimit;

        //--Start the loop where we left off
        for (; xLoop < mapWidth; xLoop++) {
            for (; yLoop < mapHeight; yLoop++) {
                //--Stop when we run out of bytecodes
                if (Clock.getBytecodeNum() > finalBytecodeValue - MAX_BYTECODES_CONSUMED_IN_ONE_LOOP_PASS) {
                    return false;
                }

                if (wasBroadcast[xLoop][yLoop]) {
                    continue;
                }

                MapLocation locationToCheck = getAbsoluteMapLocationForRelativeCoordinates(xLoop, yLoop);
                MapLocation reflected = getReflectedMapLocation(locationToCheck);
                TerrainTile tile = rc.senseTerrainTile(locationToCheck);

                //--If tile is unknown check reflected tile
                if (tile == TerrainTile.UNKNOWN) {
                    tile = rc.senseTerrainTile(reflected);
                }

                //--If reflected tile is unknown, keep going.
                //--Our BFS pathfinding will treat unknown as VOID
                if (tile == TerrainTile.UNKNOWN) {
                    continue;
                }

                int offsetForFirstLocation = getHashForRelativeCoordinates(xLoop, yLoop);
                int offsetForReflectedLocation = getReflectedChannelOffset(offsetForFirstLocation);
                rc.broadcast(
                        Channel.NW_CORNER_TERRAIN_TILE + offsetForFirstLocation,
                        tile.ordinal() + 1);
                rc.broadcast(
                        Channel.NW_CORNER_TERRAIN_TILE + offsetForReflectedLocation,
                        tile.ordinal() + 1);

                wasBroadcast[getXValue(offsetForFirstLocation)][getYValue(offsetForFirstLocation)] = true;
                wasBroadcast[getXValue(offsetForReflectedLocation)][getYValue(offsetForReflectedLocation)] = true;

                tilesBroadcastCount += 2;
            }

            yLoop = 0;
        }

        System.out.printf("Broadcasted %f percent of tiles\n", (double) tilesBroadcastCount / totalTileCount * 100);
        rc.broadcast(Channel.READY_FOR_BFS, 1);
        return true;
    }

    private static MapLocation getAbsoluteMapLocationForRelativeCoordinates(int x, int y) {
        return new MapLocation(minX + x, minY + y);
    }

    private static int getReflectedChannelOffset(int relativeAddress) {
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
