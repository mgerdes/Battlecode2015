package mapAnalysis;

import battlecode.common.*;

public class RobotPlayer {
    //--Map analysis data
    private static int distanceBetweenHq;
    private static int towerCount;
    private static double averageTowerToTowerDistance;
    private static double averageTowerToHqDistance;
    private static double oreNearHq;

    private static Team myTeam;
    private static Team enemyTeam;
    private static MapLocation myHqLocation;
    private static MapLocation enemyHqLocation;
    private static RobotController rc;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;
        myTeam = rcC.getTeam();
        enemyTeam = myTeam.opponent();
        myHqLocation = rcC.getLocation(); //--This is the HQ!
        enemyHqLocation = rcC.senseEnemyHQLocation();

        if (rcC.getType() == RobotType.HQ) {
            analyzeMap();
        }

        if (rcC.getType() == RobotType.TOWER) {
            towerAnalyzeMap();
        }

        while (true);
    }

    private static void towerAnalyzeMap() {
        int distanceToClosestTower = 1000000;
        MapLocation[] myTowers = rc.senseTowerLocations();
        MapLocation myLocation = rc.getLocation();

        for (MapLocation location : myTowers) {
            if (location.equals(myLocation)) {
                continue;
            }

            int distanceToTower = myLocation.distanceSquaredTo(location);
            if (distanceToTower < distanceToClosestTower) {
                distanceToClosestTower = distanceToTower;
            }
        }

        System.out.printf("closest tower is %s units away\n\n", distanceToClosestTower);
    }

    private static void analyzeMap() {
        distanceBetweenHq = myHqLocation.distanceSquaredTo(enemyHqLocation);
        MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
        towerCount = enemyTowers.length;

        double sumDistance = 0;
        int numberOfDistances = 0;
        for (int i = 0; i < towerCount; i++) {
            for (int j = i; j < towerCount; j++) {
                sumDistance += enemyTowers[i].distanceSquaredTo(enemyTowers[j]);
                numberOfDistances++;
            }
        }

        MapLocation[] mapLocationsCloseToHq =
                MapLocation.getAllMapLocationsWithinRadiusSq(myHqLocation, RobotType.HQ.sensorRadiusSquared);
        for (MapLocation location : mapLocationsCloseToHq) {
            oreNearHq += rc.senseOre(location);
        }

        averageTowerToTowerDistance = sumDistance / numberOfDistances;

        sumDistance = 0;
        for (int i = 0; i < towerCount; i++) {
            sumDistance += enemyTowers[i].distanceSquaredTo(enemyHqLocation);
        }

        averageTowerToHqDistance = sumDistance / towerCount;

        System.out.printf(
                "hqDist: %d\ncount %d\ntower2tower: %f\ntower2Hq: %f\noreNearHQ: %f\nbytecodesUsed: %s\n\n",
                distanceBetweenHq,
                towerCount,
                averageTowerToTowerDistance,
                averageTowerToHqDistance,
                oreNearHq,
                Clock.getBytecodeNum());
    }
}
