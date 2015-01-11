package droneRush;

import battlecode.common.*;

public class HQ {
    private static RobotController rc;
    private static int beaverSpawnCount;
    private static Direction[] directions = Direction.values();
    private static Team enemyTeam;
    private static Team myTeam;

    public static void run(RobotController rcC) {
        rc = rcC;
        SupplySharing.init(rcC);

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        loop();
    }

    private static void loop() {
        while (true) {
            try {
                rc.setIndicatorString(1, String.format("current ore: %f", rc.getTeamOre()));
                doYourThing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void doYourThing() throws GameActionException {
        SupplySharing.shareMore();

        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.HQ.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }

        if (rc.isCoreReady()) {
            spawnBeaver();
        }
    }

    private static void spawnBeaver() throws GameActionException {
        if (beaverSpawnCount > 1
            || rc.getTeamOre() < RobotType.BEAVER.oreCost) {
            return;
        }

        int direction = 0;
        while (!rc.canSpawn(directions[direction], RobotType.BEAVER)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(directions[direction], RobotType.BEAVER);
        beaverSpawnCount++;
    }
}
