package towerDefense;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class RobotPlayer {

    public static void run(RobotController rc) {
        while (true) {
            if (rc.getType() == RobotType.TOWER) {
                try {
                    RobotInfo[] robotInAttackRange = rc.senseNearbyRobots(RobotType.TOWER.attackRadiusSquared, rc.getTeam().opponent());
                    if (robotInAttackRange.length > 0
                            && rc.isWeaponReady()) {
                        rc.attackLocation(robotInAttackRange[0].location);
                    }

                    rc.yield();
                } catch (Exception e) {
                    System.out.println("Tower Exception");
                    e.printStackTrace();
                }
            }

        }
    }
}
