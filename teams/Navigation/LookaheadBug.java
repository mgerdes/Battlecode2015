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
        if (followingWall) {
            rc.setIndicatorString(0, "following wall");
            return getDirectionFollowingWall(currentLocation);
        }

        rc.setIndicatorString(0, "not following wall");
        return getDirectionNotFollowingWall(currentLocation, lookahead);
    }

    private static Direction getDirectionFollowingWall(MapLocation currentLocation) {
        int currentDistance = currentLocation.distanceSquaredTo(destination);
        rc.setIndicatorString(1, "current distance" + currentDistance);
        if (currentDistance < distanceStartBugging) {
            followingWall = false;
        }

        overallDirection = getFollowDirection(overallDirection);
        return overallDirection;
    }

    private static Direction getFollowDirection(Direction initial) {
        //TODO: use DEFAULT_DIRECTION so that left and right are supported
        Direction turn = initial.rotateRight().rotateRight();

        while (!rc.canMove(turn)) {
            turn = turn.rotateLeft();
        }

        return turn;
    }

    private static Direction getDirectionNotFollowingWall(MapLocation currentLocation, int lookahead) {
		//--Get direction to destination. This is our overall direction.
        overallDirection = currentLocation.directionTo(destination);

        MapLocation location = currentLocation;

		//--Follow the path in that direction
		//  until we reach an obstacle or we go all lookahead steps.
		Direction other = null;
		for (int i = 0; i < lookahead; i++) {
			other = getDirection(location, overallDirection);

			if (other != overallDirection) {
				//--If this is our first step, we have hit a wall
				if (i == 0) {
					followingWall = true;
					distanceStartBugging = currentLocation.distanceSquaredTo(destination);
                    rc.setIndicatorString(2, "bug distance" + distanceStartBugging);
                    overallDirection = other;
					return other;
				}

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

	private static Direction getDirection(MapLocation location, Direction direction) {
        if (CachedMap.isNavigable(location, direction)) {
            return direction;
        }

        return getTurnDirection(location, direction);
	}

    private static Direction getTurnDirection(MapLocation location, Direction initial) {
        if (DEFAULT_LEFT) {
            Direction turn = initial.rotateLeft();
            return rotateLeftUntilCanMove(location, turn);
        }

        Direction turn = initial.rotateRight();
        return rotateRightUntilCanMove(location, turn);
    }

    private static Direction rotateLeftUntilCanMove(MapLocation location, Direction direction) {
        while (!CachedMap.isNavigable(location, direction)) {
            direction = direction.rotateLeft();
        }

        return direction;
    }

    private static Direction rotateRightUntilCanMove(MapLocation location, Direction direction) {
        while (!CachedMap.isNavigable(location, direction)) {
            direction = direction.rotateRight();
        }

        return direction;
    }
}
