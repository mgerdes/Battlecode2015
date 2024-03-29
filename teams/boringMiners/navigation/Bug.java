package boringMiners.navigation;

//--Version 1.0.4

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class Bug {
    //--Set once with init()
    private static RobotController rc;
    private static boolean defaultLeft;

    //--Per navigation path, set on setDestination()
    private static MapLocation destination;

    //--Per round
    private static MapLocation currentLocation;
    private static Direction previousDirection;
    private static int previousDistance = Integer.MAX_VALUE;

    //--Bug path info
    private static boolean followingWall;
    private static int distanceStartBugging;
    private static int numberOfNinetyDegreeRotations;

    public static void init(RobotController rcC) {
        rc = rcC;
        defaultLeft = rcC.getID() % 2 == 0;
    }

    public static void setDestination(MapLocation destinationC) {
        //--Ignore if already set
        if (destinationC.equals(destination)) {
            return;
        }

        //--Reset bug state for new destination
        destination = destinationC;
        followingWall = false;
        previousDirection = null;
        distanceStartBugging = 0;
        previousDistance = Integer.MAX_VALUE;
        numberOfNinetyDegreeRotations = 0;
    }

    public static Direction getDirection(MapLocation currentLocationC) {
        currentLocation = currentLocationC;

        if (previousDirection == null) {
            previousDirection = currentLocationC.directionTo(destination);
        }

        if (followingWall) {
            return getDirectionFollowingWall();
        }

        return getDirectionNotFollowingWall();
    }

    private static Direction getDirectionFollowingWall() {
        int currentDistance = currentLocation.distanceSquaredTo(destination);
        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            return getDirectionNotFollowingWall();
        }

        //--Hack to stop robots from going in circles!
        if (numberOfNinetyDegreeRotations == 4) {
            followingWall = false;
            return getDirectionNotFollowingWall();
        }

        if (currentDistance > previousDistance
                && onMapEdge()) {
            defaultLeft = !defaultLeft;
        }

        previousDistance = currentDistance;

        //--Check if we can go around the corner...
        Direction checkDirection = defaultLeft ?
                previousDirection.rotateRight().rotateRight()
                : previousDirection.rotateLeft().rotateLeft();
        if (rc.canMove(checkDirection)) {
            numberOfNinetyDegreeRotations++;
            previousDirection = checkDirection;
            return checkDirection;
        }

        numberOfNinetyDegreeRotations = 0;
        Direction followDirection = getTurnDirection(checkDirection);
        previousDirection = followDirection;
        return followDirection;
    }

    private static boolean onMapEdge() {
        Direction wallDirection = defaultLeft
                ? previousDirection.rotateRight().rotateRight()
                : previousDirection.rotateLeft().rotateLeft();
        return rc.senseTerrainTile(currentLocation.add(wallDirection)) == TerrainTile.OFF_MAP;
    }

    private static Direction getDirectionNotFollowingWall() {
        numberOfNinetyDegreeRotations = 0;
        Direction direct = currentLocation.directionTo(destination);
        if (rc.canMove(direct)) {
            return direct;
        }

        followingWall = true;
        distanceStartBugging = currentLocation.distanceSquaredTo(destination);

        Direction turnDirection = getTurnDirection(direct);
        previousDirection = turnDirection;
        return turnDirection;
    }

    private static Direction getTurnDirection(Direction initial) {
        if (defaultLeft) {
            Direction turn = initial.rotateLeft();
            return rotateLeftUntilCanMove(turn);
        }

        Direction turn = initial.rotateRight();
        return rotateRightUntilCanMove(turn);
    }

    private static Direction rotateLeftUntilCanMove(Direction direction) {
        while (!rc.canMove(direction)) {
            direction = direction.rotateLeft();
        }

        return direction;
    }

    private static Direction rotateRightUntilCanMove(Direction direction) {
        while (!rc.canMove(direction)) {
            direction = direction.rotateRight();
        }

        return direction;
    }
}
