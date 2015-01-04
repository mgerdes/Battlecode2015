package Navigation;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class FollowBug {

    private static RobotController rc;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int ACTIVE_DIRECTION_VALUE = 16;
    private static boolean isLeader;

    public static void init(MapLocation destinationC, RobotController rcC) throws GameActionException {
        rc = rcC;
        Bug.init(destinationC, rcC);

        //--TODO: Handle when leader dies before the path is complete
        //--TODO: Handle when a secondary robot is in the lead after a maze type obstacle
        if (rc.readBroadcast(65535) == 1) {
            isLeader = false;
        }
        else {
            isLeader = true;
            rc.broadcast(65535, 1);
        }
    }

    //--Returns a navigable direction that
    //  leads (eventually) to the destination
    //--If another robot has already travelled here,
    //  we will use the direction that was already calculated
    public static Direction getDirection() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        int locationChannel = currentLocation.x + 100 * currentLocation.y;
        int value = rc.readBroadcast(locationChannel);

        if (value < ACTIVE_DIRECTION_VALUE || isLeader) {
            Direction direction = Bug.getDirection(currentLocation);
            rc.broadcast(locationChannel, direction.ordinal() + ACTIVE_DIRECTION_VALUE);
            Debug.setIndicatorString(String.format("set %s - %s", currentLocation.toString(), direction.toString()), rc);
            return direction;
        }

        Direction direction = DIRECTIONS[value - ACTIVE_DIRECTION_VALUE];
        if (rc.canMove(direction)) {
            return direction;
        }

        return Bug.getDirection(currentLocation);
    }
}
