package bfsTesting.navigation;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;
import bfsTesting.util.Helper;

public class CircleNav {
    private static RobotController rc;
    private static MapLocation center;

    //--Current rotation is a number that represents
    //  what point in the circle we are going to.
    //--For a clock, current rotation would represent
    //  the direction that the hour hand is pointed
    //  i.e. 3'oclock is east, 6 o'clock is south
    private static int currentRotation = 0;
    private static int startingRotation = 0;

    private static MapLocation currentDestination;
    private static boolean goingClockwise = true;
    private static int radius;

    public static void init(RobotController rcC, MapLocation centerC, Direction startingDirection) {
        rc = rcC;
        center = centerC;
        startingRotation = Helper.getInt(startingDirection);
        currentRotation = startingRotation;
    }

    //--Depending on the robot's position, this will return the next position
    //  to produce an octogonal path
    public static MapLocation getDestination(int radiusC, MapLocation currentLocation) {
        radius = radiusC;

        if (currentDestination == null) {
            currentDestination = getLocationForRotation(currentRotation);
        }

        if (aboutToHitAWall(currentLocation)) {
            goingClockwise = !goingClockwise;
            currentRotation = startingRotation;
            currentDestination = getLocationForRotation(currentRotation);
        }
        else  if (currentLocation.distanceSquaredTo(currentDestination) <= 4) {
            currentRotation = getNextRotation();
            currentDestination = getLocationForRotation(currentRotation);
        }

        return currentDestination;
    }

    private static MapLocation getLocationForRotation(int rotation) {
        Direction d = Helper.getDirection(rotation);
        return d.isDiagonal() ?
                center.add(d, (int) (radius / 1.414))
                : center.add(d, radius);
    }

    private static int getNextRotation() {
        if (goingClockwise) {
            int value = (currentRotation + 1) % 8;
            return value < 0 ? value + 8 : value;
        }

        int value = (currentRotation - 1) % 8;
        return value < 0 ? value + 8 : value;
    }

    private static boolean aboutToHitAWall(MapLocation currentLocation) {
        return rc.senseTerrainTile(currentLocation.add(getMovementDirection(), 2)) == TerrainTile.OFF_MAP
                || rc.senseTerrainTile(currentLocation.add(Helper.getDirection(currentRotation))) == TerrainTile.OFF_MAP;
    }

    private static Direction getMovementDirection() {
        //--We are moving orthogonal to our current rotation
        if (goingClockwise) {
            return Helper.getDirection(currentRotation).rotateRight().rotateRight();
        }

        return Helper.getDirection(currentRotation).rotateLeft().rotateLeft();
    }
}
