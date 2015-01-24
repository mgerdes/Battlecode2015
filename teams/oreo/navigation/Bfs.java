package oreo.navigation;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import oreo.PathBuilder;
import oreo.communication.Channel;
import oreo.communication.Radio;

public class Bfs {

    private static MapLocation nwCorner;

    private static int minX;
    private static int minY;

    public static void init(RobotController rcIn) {
        Radio.init(rcIn);
    }

    public static Direction getDirection(MapLocation currentLocation, int pointOfInterest) throws GameActionException {
        if (nwCorner == null) {
            nwCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);

            minX = nwCorner.x;
            minY = nwCorner.y;
        }

        //--Convert absolute location to relative location using nwCorner
        int x = currentLocation.x - minX;
        int y = currentLocation.y - minY;

        //--Read the channel corresponding to the relative current location
        int hashedMapLocation = PathBuilder.getHashedLocation(x, y);

        //--Return the direction corresponding to the point of interest
        return PathBuilder.getDirection(hashedMapLocation, pointOfInterest);
    }
}
