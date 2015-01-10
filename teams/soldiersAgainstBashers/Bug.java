package soldiersAgainstBashers;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

//--Version 1.0.1

public class Bug {
    private static RobotController rc;

    private static final boolean DEFAULT_LEFT = true;

    private static MapLocation destination;
    private static boolean followingWall;
    private static Direction previousDirection;
    private static int distanceStartBugging;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void setNewDestination(MapLocation destinationC) {
        destination = destinationC;
        followingWall = false;
        previousDirection = null;
        distanceStartBugging = 0;
    }

    //--Returns a navigable direction that
    //- leads (eventually) to the destination
    public static Direction getDirection(MapLocation currentLocation) {
        if (previousDirection == null) {
            previousDirection = currentLocation.directionTo(destination);
        }

        if (followingWall) {
            return getDirectionFollowingWall(currentLocation);
        }

        return getDirectionNotFollowingWall(currentLocation);
    }

    private static Direction getDirectionFollowingWall(MapLocation currentLocation) {
        if (currentLocation.distanceSquaredTo(destination) < distanceStartBugging) {
            followingWall = false;
            return getDirectionNotFollowingWall(currentLocation);
        }

        Direction followDirection = getFollowDirection(previousDirection);
        previousDirection = followDirection;
        rc.setIndicatorString(0, previousDirection.toString());
        return followDirection;
    }

    private static Direction getDirectionNotFollowingWall(MapLocation currentLocation) {
        Direction direct = currentLocation.directionTo(destination);
        if (rc.canMove(direct)) {
            return direct;
        }

        followingWall = true;
        distanceStartBugging = currentLocation.distanceSquaredTo(destination);

        Direction turnDirection = getTurnDirection(direct);
        previousDirection = turnDirection;
        rc.setIndicatorString(0, previousDirection.toString());
        return turnDirection;
    }

    //--We turn the opposite way because we may need
    //- to round the corner
    private static Direction getFollowDirection(Direction initial) {
        //--TODO: optimize the double turn
        if (DEFAULT_LEFT) {
            return rotateLeftUntilCanMove(initial.rotateRight().rotateRight());
        }

        return rotateRightUntilCanMove(initial.rotateLeft().rotateLeft());
    }

    private static Direction getTurnDirection(Direction initial) {
        if (DEFAULT_LEFT) {
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
