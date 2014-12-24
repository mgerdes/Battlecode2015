package Navigation;

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

    //--Returns a navigable direction that
    //- leads (eventually) to the destination
    public static Direction getDirection(int lookahead) {
        MapLocation currentLocation = rc.getLocation();
        Direction direction;
        if (followingWall) {
            direction = getDirectionFollowingWall(currentLocation, lookahead);
        }
        else {
            overallDirection = currentLocation.directionTo(destination);
            direction = getDirectionWithLookahead(currentLocation, lookahead);
        }

        rc.setIndicatorString(0, String.format("overall:%s followingWall:%s", overallDirection.toString(), followingWall));

        if (rc.canMove(direction)) {
            return direction;
        }

        return null;
    }

    private static Direction getDirectionWithLookahead(MapLocation currentLocation, int lookahead) {
        //--Get direction to destination. This is our overall direction.
        Direction other = getDirection(currentLocation, overallDirection);

        if (other != overallDirection) {
            //--We have hit a wall
            followingWall = true;
            distanceStartBugging = currentLocation.distanceSquaredTo(destination);
            overallDirection = other;
            return other;
        }

        MapLocation location = currentLocation.add(other);

        //--Follow the path in that direction
        //  until we reach an obstacle or we go all lookahead steps.
        for (int i = 1; i < lookahead; i++) {
            other = getDirection(location, overallDirection);

            if (other != overallDirection) {
                //--If turn direction is 90 degrees from the overall direction,
                //  go in the diagonal direction
                if (other == overallDirection.rotateLeft().rotateLeft()) {
                    return overallDirection.rotateLeft();
                }
            }

            location = location.add(other);
        }

        //--We looked ahead and have seen no obstacles. Press on.
        return overallDirection;
    }

    private static Direction getDirectionFollowingWall(MapLocation currentLocation, int lookahead) {
        int currentDistance = currentLocation.distanceSquaredTo(destination);

        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            return getDirectionWithLookahead(currentLocation, lookahead);
        }

        //--Try to round the corner
        Direction checkDirection = getCheckDirection(overallDirection);
        if (checkDirection != null) {
            return checkDirection;
        }

        return getDirectionWithLookahead(currentLocation, lookahead);
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

    private static Direction getDirection(MapLocation location, Direction direction) {
        if (CachedMap.isNavigable(location, direction)) {
            return direction;
        }

        return getTurnDirection(location, direction);
    }

    private static Direction getTurnDirection(MapLocation location, Direction initial) {
        if (DEFAULT_LEFT) {
            Direction turn = initial.rotateLeft();
            return rotateLeftUntilNoWall(location, turn);
        }

        Direction turn = initial.rotateRight();
        return rotateRightUntilNoWall(location, turn);
    }

    private static Direction rotateLeftUntilNoWall(MapLocation location, Direction direction) {
        while (!CachedMap.isNavigable(location, direction)) {
            direction = direction.rotateLeft();
        }

        return direction;
    }

    private static Direction rotateRightUntilNoWall(MapLocation location, Direction direction) {
        while (!CachedMap.isNavigable(location, direction)) {
            direction = direction.rotateRight();
        }

        return direction;
    }
}
