package MarkyMark;

import battlecode.common.*;

//--Version 1.0

public class Bug {
    private static boolean DEFAULT_LEFT = true;

    private static MapLocation destination;
    private static RobotController rc;
    private static boolean followingWall;
    private static Direction previousDirection;
    private static int distanceStartBugging;

    public static void init(RobotController rcC) {
        rc = rcC;
        DEFAULT_LEFT = Info.rand.nextDouble() < .5;
    }

    public static void beginBugTowards(MapLocation destinationC) {
        followingWall = false;
        previousDirection = null;
        distanceStartBugging = 0;
        destination = destinationC;
    }

    //--Returns a navigable direction that
    //- leads (eventually) to the destination
    public static Direction getDirection() {
        MapLocation currentLocation = rc.getLocation();
        return getDirection(currentLocation);
    }

    public static Direction getDirection(MapLocation currentLocation) {
        if (followingWall) {
            //rc.setIndicatorString(0, "following wall");
            return getDirectionFollowingWall(currentLocation);
        }

        //:w
        // rc.setIndicatorString(0, "not following wall");
        return getDirectionNotFollowingWall(currentLocation);
    }

    private static boolean obstaclesAroundMe() {
        int[] xoffsets = {1,1,0,-1,-1,-1, 0, 1};
        int[] yoffsets = {0,1,1, 1, 0,-1,-1,-1};
        for (int i = 0; i < 8; i++) {
            MapLocation currentLocation = rc.getLocation();
            MapLocation loc = new MapLocation(currentLocation.x + xoffsets[i], currentLocation.y + yoffsets[i]);
            TerrainTile tile = rc.senseTerrainTile(loc);
            if (!tile.isTraversable()) {
                //rc.setIndicatorString(0, "obstacles around me");
                return true;
            }
        }
        //rc.setIndicatorString(0, "no obstacles around me");
        return false;
    }

    private static Direction getDirectionFollowingWall(MapLocation currentLocation) {
        if (currentLocation.distanceSquaredTo(destination) < distanceStartBugging || !obstaclesAroundMe()) {
            followingWall = false;
            return getDirectionNotFollowingWall(currentLocation);
        }

        Direction followDirection = getFollowDirection(previousDirection);
        previousDirection = followDirection;
        //rc.setIndicatorString(0, previousDirection.toString());
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
        //rc.setIndicatorString(0, previousDirection.toString());
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
