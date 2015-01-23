package droneTest;

import battlecode.common.*;

public class Missile {
    private static RobotController rc;
    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;
        enemyTeam = rcC.getTeam().opponent();

        while (true) {
            try {
                loop();
            } catch (GameActionException e) {
                e.printStackTrace();
            }

            rc.yield();
        }
    }

    private static void loop() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.MISSILE.sensorRadiusSquared, enemyTeam);
        if (enemies.length == 0) {
            rc.disintegrate();
        }

        MapLocation enemyLocation = enemies[0].location;
        MapLocation currentLocation = rc.getLocation();
        if (enemyLocation.distanceSquaredTo(currentLocation) <= GameConstants.MISSILE_RADIUS_SQUARED) {
            rc.explode();
        }

        if (!rc.isCoreReady()) {
            return;
        }

        Direction directionToEnemy = currentLocation.directionTo(enemies[0].location);
        if (rc.canMove(directionToEnemy)) {
            rc.move(directionToEnemy);
        }
    }
}
