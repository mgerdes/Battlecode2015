package soldiersAgainstBashers;

import battlecode.common.*;

public class Move {
    private static RobotController rc;
    private static MapLocation previousDestination;

    public static void init(RobotController rcC) {
        rc = rcC;
        Bug.init(rcC);
        Navigation.init(rcC);
    }

    public static void awayFromTeam(Team team, MapLocation currentLocation) throws GameActionException {
        Direction direction = Navigation.getDirectionAwayFromTeam(team, currentLocation);
        rc.setIndicatorString(2, direction.toString());
        if (direction == Direction.NONE
                || direction == Direction.OMNI) {
            return;
        }

        if (rc.canMove(direction)) {
            rc.move(direction);
        }
    }

    public static void toward(MapLocation destination, MapLocation currentLocation) throws GameActionException {
        if (!destination.equals(previousDestination)) {
            Bug.setNewDestination(destination);
            previousDestination = destination;
        }

        Direction direction = Bug.getDirection(currentLocation);

        if (rc.canMove(direction)) {
            rc.move(direction);
        }
    }

    public static void inRandomDirection() throws GameActionException {
        Direction direction = Navigation.getRandomDirection();

        if (rc.canMove(direction)) {
            rc.move(direction);
        }
    }
}
