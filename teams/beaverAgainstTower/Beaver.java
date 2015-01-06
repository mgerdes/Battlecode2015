package beaverAgainstTower;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

//--This is a dumb beaver used to test the bug nav
public class Beaver {
    private static RobotController rc;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void run() {
        try {
            MapLocation enemyTower = rc.senseEnemyTowerLocations()[0];
            MapLocation myHQ = rc.senseHQLocation();
            MapLocation rally = MapLocationHelper.getMidpoint(enemyTower, myHQ);
            Bug.init(rally, rc);
            
            while (true) {
                if (rc.isCoreReady()) {
                    rc.move(Bug.getDirection());
                }

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
    }
}
