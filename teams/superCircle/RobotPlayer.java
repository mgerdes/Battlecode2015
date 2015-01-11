package superCircle;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {
    public static void run(RobotController rc) {
        RobotType type = rc.getType();

        if (type == RobotType.HQ) {
            HQ.run(rc);
        }
        else if (type == RobotType.TOWER) {
            Tower.run(rc);
        }
        else if (type == RobotType.BEAVER) {
            Beaver.run(rc);
        }
        else if (type == RobotType.MINER) {
            Miner.run(rc);
        }
        else if (type == RobotType.DRONE) {
            Drone.run(rc);
        }
        else  {
            Building.run(rc);
        }
    }
}
