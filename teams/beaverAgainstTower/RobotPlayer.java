package beaverAgainstTower;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {

    public static void run(RobotController rc) {
        while (true) {
            if (rc.getType() == RobotType.HQ) {
                try {
                    //Check if a robot is spawnable and spawn one if it is
                    if (rc.isCoreReady()) {
                        Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                        if (rc.canSpawn(toEnemy, RobotType.BEAVER)) {
                            rc.spawn(toEnemy, RobotType.BEAVER);
                        }
                    }

                    rc.yield();
                } catch (Exception e) {
                    System.out.println("HQ Exception");
                    e.printStackTrace();
                }
            }

            if (rc.getType() == RobotType.BEAVER) {
                Beaver.init(rc);
                Beaver.run();
            }
        }
    }
}
