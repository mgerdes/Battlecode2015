package superCircle;

import battlecode.common.Direction;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Helper {
    private static Direction[] directions = new Direction[]{Direction.NORTH,
                                         Direction.NORTH_EAST,
                                         Direction.EAST,
                                         Direction.SOUTH_EAST,
                                         Direction.SOUTH,
                                         Direction.SOUTH_WEST,
                                         Direction.WEST,
                                         Direction.NORTH_WEST};


    public static int getRobotsOfType(RobotInfo[] robots, RobotType type) {
        int count = 0;
        for (RobotInfo robot : robots) {
            if (robot.type == type) {
                count++;
            }
        }

        return count;
    }

    public static Direction getDirection(int n) {
        if (n < 0) {
            n = n + 8;
        }

        return directions[n % 8];
    }

    public static int getInt(Direction d) {
        switch(d) {
            case NORTH:
                return 0;
            case NORTH_EAST:
                return 1;
            case EAST:
                return 2;
            case SOUTH_EAST:
                return 3;
            case SOUTH:
                return 4;
            case SOUTH_WEST:
                return 5;
            case WEST:
                return 6;
            case NORTH_WEST:
                return 7;
            default:
                return -1;
        }
    }
}
