package Navigation;

import battlecode.common.*;

//--Version 0.1

public class Bug {
    private static final boolean DEFAULT_LEFT = true;
    private MapLocation destination;
    private RobotController rc;
    private boolean followingWall;
    private Direction followDirection;

    public Bug(MapLocation destination, RobotController rc) {
        this.destination = destination;
        this.rc = rc;
    }

    //--This method returns a navigable direction that
    //- leads to the destination

    public Direction getDirection() {
        if (!followingWall) {
            rc.setIndicatorString(0, "direct");
            MapLocation current = rc.getLocation();
            Direction direct = current.directionTo(destination);
            if (rc.canMove(direct)) {
                return direct;
            }

            followingWall = true;
            Direction turnDirection = getTurnDirection(direct);
            rc.setIndicatorString(1, "turning");
            followDirection = turnDirection;
            return getTurnDirection(followDirection);
        }

        rc.setIndicatorString(0, "following");
        return followDirection;
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
