package nemesis;

import battlecode.common.*;
import nemesis.communication.Channel;
import nemesis.communication.Radio;
import nemesis.constants.Symmetry;

public class MapBuilder {
    private static RobotController rc;

    private static int mapWidth;
    private static int mapHeight;
    private static int minX;
    private static int minY;

    private static MapLocation myHq;

    private static int symmetryType;

    private static int xLoop = 0;
    private static int yLoop = 0;

    private static int debugWaitingForLocation = 0;
    private static int debugStopForBytecodes = 0;

    private static final int MAX_BYTECODES_CONSUMED_IN_ONE_LOOP_PASS = 220;

    private static boolean[][] wasBroadcast;

    public static void init(int mapWidthC,
                            int mapHeightC,
                            MapLocation nwCornerC,
                            int symmetryTypeC,
                            MapLocation myHqC,
                            RobotController rcC) {
        mapWidth = mapWidthC;
        mapHeight = mapHeightC;
        minX = nwCornerC.x;
        minY = nwCornerC.y;
        symmetryType = symmetryTypeC;
        myHq = myHqC;
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
                if (Clock.getBytecodeNum() > finalBytecodeValue - MAX_BYTECODES_CONSUMED_IN_ONE_LOOP_PASS) {
                    debugStopForBytecodes++;
                    return false;
                }

                if (wasBroadcast[xLoop][yLoop]) {
                    continue;
                }

                MapLocation locationToCheck = getAbsoluteMapLocationForRelativeCoordinates(xLoop, yLoop);
                MapLocation reflected = getReflectedMapLocation(locationToCheck);
                TerrainTile tile = rc.senseTerrainTile(locationToCheck);
                if (tile == TerrainTile.UNKNOWN) {
                    tile = rc.senseTerrainTile(reflected);
                }

                if (tile == TerrainTile.UNKNOWN) {
                    MapLocation closerToOurHq =
                            myHq.distanceSquaredTo(locationToCheck) < myHq.distanceSquaredTo(reflected) ?
                            locationToCheck
                            : reflected;
                    Radio.setMapLocationOnChannel(closerToOurHq, Channel.LOCATION_TO_SURVEY);

//                    System.out.println("need location original " + locationToCheck);
//                    System.out.println("need location reflected " + closerToOurHq);
                    debugWaitingForLocation++;
                    return false;
                }
                else {
                    int offsetForFirstLocation = getHashForRelativeCoordinates(xLoop, yLoop);
                    int offsetForReflectedLocation = getReflectedChannelOffset(offsetForFirstLocation);
                    rc.broadcast(
                            Channel.NW_CORNER_TERRAIN_TILE + offsetForFirstLocation,
                            tile.ordinal());
                    rc.broadcast(
                            Channel.NW_CORNER_TERRAIN_TILE + offsetForReflectedLocation,
                            tile.ordinal());
                    wasBroadcast[getXValue(offsetForFirstLocation)][getYValue(offsetForFirstLocation)] = true;
                    wasBroadcast[getXValue(offsetForReflectedLocation)][getYValue(offsetForReflectedLocation)] = true;
                }
            }

            if (xLoop < mapWidth - 1) {
                yLoop = 0;
            }
        }

        System.out.printf(
                "\nWaited for location %d times\nBytecode break %d times\n",
                debugWaitingForLocation,
                debugStopForBytecodes);

        return true;
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
                        2 * minX + mapWidth - location.x - 1,
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
