package warble;

import battlecode.common.*;
import warble.communication.Radio;

public class Tower {
    private static RobotController rc;
    private static Team enemyTeam;
    private static MapLocation myLocation;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;
        enemyTeam = rc.getTeam().opponent();
        myLocation = rc.getLocation();

        Radio.init(rcC);
        checkNearbySquares();

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

    private static void checkNearbySquares() throws GameActionException {
        MapLocation[] nearbySquares = MapLocation.getAllMapLocationsWithinRadiusSq(myLocation, RobotType.TOWER.sensorRadiusSquared);
        int voidCount = 0;
        for (MapLocation location : nearbySquares) {
            if (rc.senseTerrainTile(location) == TerrainTile.VOID) {
                voidCount++;
            }
        }

        Radio.towerReportVoidSquareCount(myLocation, voidCount);
    }

    private static void doYourThing() throws GameActionException {
        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.TOWER.sensorRadiusSquared, enemyTeam);
        int numberOfEnemies = enemiesInSensorRange.length;
        if (numberOfEnemies > 0) {
            Radio.enemiesSpotted(myLocation, numberOfEnemies);
        }

        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TOWER.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }
    }
}
