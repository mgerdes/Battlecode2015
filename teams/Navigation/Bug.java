package Navigation;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

//--Version 0.1

public class Bug {
    private static final boolean DEFAULT_LEFT = true;
    private MapLocation destination;
    private RobotController rc;
    private boolean followingWall;

    public Bug(MapLocation destination, RobotController rc) {
        this.destination = destination;
        this.rc = rc;
    }

    //--This method returns a navigable direction that
    //- leads to the destination

    public Direction getDirection() {
        if (!followingWall) {
            MapLocation current = rc.getLocation();
            Direction direct = current.directionTo(destination);
            if (rc.canMove(direct)) {
                return direct;
            }

            followingWall = true;
            return getTurnDirection(direct);
        }

        return null;
    }

    private Direction getTurnDirection(Direction initial) {
        if (DEFAULT_LEFT) {
            Direction turn = initial.rotateLeft();
            while (!rc.canMove(turn)) {
                turn.rotateLeft();
            }

            return turn;
        }

        Direction turn = initial.rotateRight();
        while (!rc.canMove(turn)) {
            turn.rotateRight();
        }

        return turn;
    }
}
