package droneRush;

import battlecode.common.*;

public class HQ {
    private static RobotController rc;
    private static int beaverSpawnCount;
    private static Direction[] directions = Direction.values();
    private static Team enemyTeam;
    private static Team myTeam;
    private static MapLocation myHqLocation;
    private static MapLocation enemyHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;
        SupplySharing.init(rcC);

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHqLocation = rc.getLocation(); //--This is the HQ!
        enemyHqLocation = rc.senseEnemyHQLocation();
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

        setTactic();

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

    private static void setTactic() throws GameActionException {
        if (Clock.getRoundNum() < 500) {
            return;
        }

        int droneCount = 0;
        RobotInfo[] friendlyRobots = rc.senseNearbyRobots(1000000, myTeam);
        for (RobotInfo robot : friendlyRobots) {
            if (robot.type == RobotType.DRONE) {
                droneCount++;
            }
        }

        if (droneCount < 25) {
            rc.broadcast(ChannelList.TACTIC, Tactic.SWARM);
            return;
        }

        if (droneCount > 35) {
            rc.broadcast(ChannelList.TACTIC, Tactic.ATTACK_ENEMY_STRUCTURE);
            MapLocation enemyStructure = getStructureToAttack();
            rc.broadcast(ChannelList.STRUCTURE_TO_ATTACK_X, enemyStructure.x);
            rc.broadcast(ChannelList.STRUCTURE_TO_ATTACK_Y, enemyStructure.y);
            return;
        }
    }

    private static MapLocation getStructureToAttack() {
        //--if enemy has towers
        //    return the closest one to our HQ
        //--else return enemy HQ
        MapLocation[] enemyTowerLocations = rc.senseEnemyTowerLocations();
        if (enemyTowerLocations.length == 0) {
            return enemyHqLocation;
        }

        int index = 0;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < enemyTowerLocations.length; i++) {
            int thisDistance = myHqLocation.distanceSquaredTo(enemyTowerLocations[i]);
            if (thisDistance < minDistance) {
                minDistance = thisDistance;
                index = i;
            }
        }

        return enemyTowerLocations[index];
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
