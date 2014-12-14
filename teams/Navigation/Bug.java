package Navigation;

import battlecode.common.*;

//--Version 1.0

public class Bug {
    private static final boolean DEFAULT_LEFT = true;
    private MapLocation destination;
    private RobotController rc;
    private boolean followingWall;
    private Direction previousDirection;
    private int distanceStartBugging;

    public Bug(MapLocation destination, RobotController rc) {
        this.destination = destination;
        this.rc = rc;
    }

    //--Returns a navigable direction that
    //- leads (eventually) to the destination
    public Direction getDirection() {
        MapLocation currentLocation = rc.getLocation();
        return getDirection(currentLocation);
    }

    public Direction getDirection(MapLocation currentLocation) {
        if (followingWall) {
            return getDirectionFollowingWall(currentLocation);
        }

        return getDirectionNotFollowingWall(currentLocation);
    }

    private Direction getDirectionFollowingWall(MapLocation currentLocation) {
        if (currentLocation.distanceSquaredTo(destination) < distanceStartBugging) {
            followingWall = false;
            return getDirectionNotFollowingWall(currentLocation);
        }

        Direction followDirection = getFollowDirection(previousDirection);
        previousDirection = followDirection;
        return followDirection;
    }

    private Direction getDirectionNotFollowingWall(MapLocation currentLocation) {
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

    //--We turn the opposite way because we may need
    //- to round the corner
    private Direction getFollowDirection(Direction initial) {
        if (DEFAULT_LEFT) {
            //--TODO: optimize
            return getTurnDirection(initial.rotateRight().rotateRight().rotateRight());
        }

        //--TODO: optimize
        return getTurnDirection(initial.rotateLeft().rotateLeft().rotateLeft());
    }

    private Direction getTurnDirection(Direction initial) {
        if (DEFAULT_LEFT) {
            Direction turn = initial.rotateLeft();
            while (!rc.canMove(turn)) {
                turn = turn.rotateLeft();
            }

            return turn;
        }

        Direction turn = initial.rotateRight();
        while (!rc.canMove(turn)) {
            turn = turn.rotateRight();
        }

        return turn;
    }
}
