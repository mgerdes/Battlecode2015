package bfsTesting;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bfsTesting.communication.Channel;
import bfsTesting.communication.Radio;
import bfsTesting.constants.Symmetry;

public class MapEncoder {
    private static int mapWidth;
    private static int mapHeight;
    private static int minX;
    private static int minY;
    private static int symmetryType;

    public static void init(RobotController rcC) throws GameActionException {
        mapWidth = rcC.readBroadcast(Channel.MAP_WIDTH);
        mapHeight = rcC.readBroadcast(Channel.MAP_HEIGHT);

        Radio.init(rcC);
        MapLocation nwCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);

        minX = nwCorner.x;
        minY = nwCorner.y;
        symmetryType = rcC.readBroadcast(Channel.MAP_SYMMETRY);
    }

    public static void init(int mapWidthC,
                            int mapHeightC,
                            RobotController rcC) throws GameActionException {
        mapWidth = mapWidthC;
        mapHeight = mapHeightC;

        Radio.init(rcC);
        MapLocation nwCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);

        minX = nwCorner.x;
        minY = nwCorner.y;
        symmetryType = rcC.readBroadcast(Channel.MAP_SYMMETRY);
    }

    public static MapLocation getAbsoluteMapLocationForRelativeCoordinates(int x, int y) {
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

    public static int getXValue(int channel) {
        return channel / 120;
    }

    public static int getYValue(int channel) {
        return channel % 120;
    }

    public static int getHashForRelativeCoordinates(int x, int y) {
        return 120 * x + y;
    }

    public static int getHashForAbsoluteMapLocation(MapLocation location) {
        return 120 * (location.x - minX) + location.y - minY;
    }

    public static MapLocation getAbsoluteMapLocationFromHash(int hashedMapLocation) {
        int x = getXValue(hashedMapLocation) + minX;
        int y = getYValue(hashedMapLocation) + minY;
        return new MapLocation(x, y);
    }
}
