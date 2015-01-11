package droneRush;

import battlecode.common.*;

public class Drone {
    private static RobotController rc;

    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;
        Bug.init(rcC);
        Bug.setNewDestination(rcC.senseEnemyHQLocation());

        SupplySharing.init(rcC);

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
        SupplySharing.share();

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                MapLocation currentLocation = rc.getLocation();
                Direction direction = Bug.getSafeDirection(currentLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }
}
