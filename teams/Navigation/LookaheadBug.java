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

    //--Returns a navigable direction that leads (eventually) to the destination
    public static Direction getDirection(int lookahead) {
        MapLocation currentLocation = rc.getLocation();

        Direction direction;
        if (followingWall) {
            direction = getDirectionFollowingWall(currentLocation, lookahead);
        }
        else {
            overallDirection = currentLocation.directionTo(destination);
            Debug.setIndicatorString(String.format("overall (31): %s", overallDirection), rc);

            direction = getDirectionWithLookahead(currentLocation, lookahead);
        }

        if (rc.canMove(direction)) {
            return direction;
        }

        return null;
    }

    private static Direction getDirectionWithLookahead(MapLocation currentLocation, int lookahead) {
        Direction other = getDirectionFromHere(overallDirection);

        if (other != overallDirection) {
            //--We have hit a wall
            if (!followingWall) {
                distanceStartBugging = currentLocation.distanceSquaredTo(destination);
                Debug.setIndicatorString(String.format("bugging distance: %d", distanceStartBugging), rc);
            }

            followingWall = true;
            overallDirection = other;
            Debug.setIndicatorString(String.format("overall (52): %s", overallDirection), rc);

            return other;
        }

        //--Follow the path in that direction
        //  until we reach an obstacle or we go all lookahead steps.
        for (int i = 0; i < lookahead; i++) {
            MapLocation location = currentLocation.add(other);
            other = getDirectionFrom(location, overallDirection);

            if (other != overallDirection) {
                //--If turn direction is 90 degrees from the overall direction,
                //  go in the diagonal direction
                if (other == overallDirection.rotateLeft().rotateLeft()) {
                    return overallDirection.rotateLeft();
                }
            }
        }

        //--We looked ahead and have seen no obstacles. Press on.
        return overallDirection;
    }

    private static Direction getDirectionFollowingWall(MapLocation currentLocation, int lookahead) {
        int currentDistance = currentLocation.distanceSquaredTo(destination);

        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            Debug.setIndicatorString("not following wall", rc);
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

        Debug.setIndicatorString(String.format("can't move %s", turn), rc);

        turn = turn.rotateLeft();
        if (rc.canMove(turn)) {
            overallDirection = turn;
            return turn;
        }

        return null;
    }

    private static Direction getDirectionFromHere(Direction direction) {
        if (rc.canMove(direction)) {
            return direction;
        }

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

    private static Direction getDirectionFrom(MapLocation location, Direction direction) {
        if (CachedMap.isNavigable(location, direction)) {
            return direction;
        }

        if (DEFAULT_LEFT) {
            Direction turn = direction.rotateLeft();
            while (!CachedMap.isNavigable(location, turn)) {
                turn = turn.rotateLeft();
            }

            return turn;
        }

        Direction turn = direction.rotateRight();
        while (!CachedMap.isNavigable(location, turn)) {
            turn = turn.rotateRight();
        }

        return turn;
    }
}
