package droneRush;

import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Helper {
    public static int getRobotsOfType(RobotInfo[] robots, RobotType type) {
        int count = 0;
        for (RobotInfo robot : robots) {
            if (robot.type == type) {
                count++;
            }
        }

        return count;
    }
}
