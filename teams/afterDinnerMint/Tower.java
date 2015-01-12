package afterDinnerMint;

import battlecode.common.*;

public class Tower {
    private static RobotController rc;
    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;
        enemyTeam = rc.getTeam().opponent();
        loop();
    }

    private static void loop() {
        while (true) {
            try {
                doYourThing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void doYourThing() throws GameActionException {
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TOWER.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }
    }
}
