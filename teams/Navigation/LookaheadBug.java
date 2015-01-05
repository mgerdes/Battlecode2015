package navigation;

import battlecode.common.*;

public class LookaheadBug {
    private static final boolean DEFAULT_LEFT = true;

    private static MapLocation destination;
    private static RobotController rc;
    private static boolean followingWall;
    private static Direction overallDirection;
    private static int distanceStartBugging;

    public static void init(MapLocation destinationC, RobotController rcC) {
        destination = destinationC;
        rc = rcC;

        CachedMap.init(rc);
    }

    //--Returns a navigable direction that leads (eventually) to the destination
    //--Looks ahead one square and optimizes a 90 degree turn to a diagonal
    public static Direction getDirection() {
        MapLocation currentLocation = rc.getLocation();
        if (followingWall) {
            return getDirectionFollowingWall(currentLocation);
        } else {
            return getDirectionWithLookahead(currentLocation);
        }
    }

    private static Direction getDirectionWithLookahead(MapLocation currentLocation) {
        if (!followingWall) {
            overallDirection = currentLocation.directionTo(destination);
        }

        Direction firstDirection;
        if (rc.canMove(overallDirection)) {
            firstDirection = overallDirection;
        } else {
            //--We have hit a wall
            Direction other = getTurnDirectionFromHere(overallDirection);
            if (!followingWall) {
                distanceStartBugging = currentLocation.distanceSquaredTo(destination);
                followingWall = true;
            }

            overallDirection = other;
            firstDirection = other;
        }

        //--Get direction for next move and do diagonal if possible
        MapLocation location = currentLocation.add(overallDirection);
        Direction secondDirection = getDirectionFrom(location, firstDirection);

        if (firstDirection == secondDirection) {
            return firstDirection;
        }

        if (secondDirection == firstDirection.rotateLeft().rotateLeft()) {
            Direction diagonal = firstDirection.rotateLeft();
            if (rc.canMove(diagonal)) {
                overallDirection = secondDirection;
                return diagonal;
            }
        }

        return firstDirection;
    }

    private static Direction getDirectionFollowingWall(MapLocation currentLocation) {
        int currentDistance = currentLocation.distanceSquaredTo(destination);

        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            Debug.setIndicatorString("not following wall", rc);
            return getDirectionWithLookahead(currentLocation);
        }

        //--Try to round the corner if we are on wall edge
        Direction checkDirection = getCheckDirection(overallDirection);
        if (checkDirection != null) {
            return checkDirection;
        }

        return getDirectionWithLookahead(currentLocation);
    }

    private static Direction getCheckDirection(Direction initial) {
        //TODO: use DEFAULT_DIRECTION so that left and right are supported
        Direction turn = initial.rotateRight().rotateRight();
        if (rc.canMove(turn)) {
            overallDirection = turn;
            return turn;
        }

        turn = turn.rotateLeft();
        if (rc.canMove(turn)) {
            overallDirection = turn;
            return turn;
        }

        return null;
    }

    private static Direction getTurnDirectionFromHere(Direction direction) {
        if (DEFAULT_LEFT) {
            Direction turn = direction.rotateLeft();
            while (!rc.canMove(turn)) {
                turn = turn.rotateLeft();
            }

            return turn;
        }

        Direction turn = direction.rotateRight();
        while (!rc.canMove(turn)) {
            turn = turn.rotateRight();
        }

        return turn;
    }

    //--Returns the next direction given a location and an orientation
    private static Direction getDirectionFrom(MapLocation location, Direction direction) {
        //--If we are following wall, we first try to round the corner
        //  Then, continue to turn in the turn direction
        Direction turn = direction;

        if (DEFAULT_LEFT) {
            if (followingWall) {
                turn = direction.rotateRight().rotateRight();
            }

            while (!CachedMap.isNavigable(location, turn)) {
                turn = turn.rotateLeft();
            }

            return turn;
        }

        if (followingWall) {
            turn = direction.rotateLeft().rotateLeft();
        }

        while (!CachedMap.isNavigable(location, turn)) {
            turn = turn.rotateRight();
        }

        return turn;
    }
}
