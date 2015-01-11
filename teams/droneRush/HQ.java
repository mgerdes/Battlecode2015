package droneRush;

import battlecode.common.*;

public class HQ {
    private static RobotController rc;
    private static Direction[] directions = Direction.values();
    private static Team enemyTeam;
    private static Team myTeam;
    private static MapLocation myHqLocation;
    private static MapLocation enemyHqLocation;

    private static final int BEAVER_COUNT = 3;

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

        RobotInfo[] friendlyRobots = rc.senseNearbyRobots(1000000, myTeam);
        setTactic(friendlyRobots);
        broadcastFortifyPoints(friendlyRobots);

        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.HQ.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }

        if (rc.isCoreReady()) {
            if (shouldSpawnBeaver(friendlyRobots)) {
                spawn(RobotType.BEAVER);
            }
        }
    }

    private static void broadcastFortifyPoints(RobotInfo[] friendlyRobots) throws GameActionException {
        int droneCount = Helper.getRobotsOfType(friendlyRobots,RobotType.DRONE);
        int count = 0;
        int distanceAwayFromHq = Math.max(6, droneCount / 2);
        for (Direction d : directions) {
            if (d == Direction.NONE
                    || d == Direction.OMNI) {
                continue;
            }

            MapLocation point = myHqLocation.add(d, distanceAwayFromHq);
            if (rc.senseTerrainTile(point) == TerrainTile.OFF_MAP) {
                continue;
            }

            rc.broadcast(ChannelList.FORTIFY_POINT_START + 2 * count, point.x);
            rc.broadcast(ChannelList.FORTIFY_POINT_START + 2 * count + 1, point.y);
            count++;
        }

        rc.broadcast(ChannelList.FORTIFY_POINT_COUNT, count);
    }

    private static boolean shouldSpawnBeaver(RobotInfo[] friendlyRobots) {
        if (rc.getTeamOre() < RobotType.BEAVER.oreCost) {
            return false;
        }

        int beaverCount = Helper.getRobotsOfType(friendlyRobots, RobotType.BEAVER);
        return beaverCount < BEAVER_COUNT;
    }

    private static void setTactic(RobotInfo[] friendlyRobots) throws GameActionException {
        if (Clock.getRoundNum() < 600) {
            rc.broadcast(ChannelList.TACTIC, Tactic.FORTIFY);
            return;
        }

        int droneCount = Helper.getRobotsOfType(friendlyRobots, RobotType.DRONE);

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

    private static void spawn(RobotType type) throws GameActionException {
        int direction = 0;
        while (!rc.canSpawn(directions[direction], type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(directions[direction], type);
    }
}
