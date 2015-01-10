package soldiersAgainstBashers;

import battlecode.common.*;

public class Move {
    private static RobotController rc;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void awayFromTeam(Team team) throws GameActionException {
        Direction direction = Navigation.getDirectionAwayFromTeam(team);
        rc.setIndicatorString(2, direction.toString());
        if (direction == Direction.NONE
                || direction == Direction.OMNI) {
            return;
        }

        if (rc.canMove(direction)) {
            rc.move(direction);
        }
    }
}
