package droneTest.navigation;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import droneTest.util.Helper;

public class BasicNav {
    private static RobotController rc;

    private static final int[] directions = new int[]{0, -1, 1, -2, 2};

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static Direction getNavigableDirectionClosestTo(Direction initial) {
        int initialDirectionValue = Helper.getInt(initial);
        for (int i = 0; i < directions.length; i++) {
            Direction direction = Helper.getDirection(initialDirectionValue + directions[i]);
            if (rc.canMove(direction)) {
                return direction;
            }
        }

        return Direction.NONE;
    }
}
