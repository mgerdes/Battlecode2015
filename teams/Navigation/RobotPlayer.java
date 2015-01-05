package navigation;

import battlecode.common.*;

public class RobotPlayer {

    public static void run(RobotController rc) {
        while (true) {
            if (rc.getType() == RobotType.HQ) {
                try {
                    //Check if a robot is spawnable and spawn one if it is
                    if (rc.isActive() && rc.senseRobotCount() < 25) {
                        Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());

                        if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                            rc.spawn(toEnemy);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("HQ Exception");
                }
            }

            if (rc.getType() == RobotType.SOLDIER) {
                Soldier.init(rc);
                Soldier.run();
            }
        }
    }
}
