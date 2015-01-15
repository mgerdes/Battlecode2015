package cat;

import cat.constants.Building;
import cat.constants.ChannelList;
import cat.constants.Order;
import cat.util.Helper;
import battlecode.common.*;
import cat.util.Debug;

public class HQ {
    private static RobotController rc;
    private static Direction[] directions = Direction.values();
    private static Team enemyTeam;
    private static Team myTeam;
    private static MapLocation myHqLocation;
    private static MapLocation enemyHqLocation;


    private static final int BEAVER_COUNT = 1;
    private static final int DO_NOT_ATTACK_BEFORE_ROUND = 40;

    //--Map analysis data
    private static int distanceBetweenHq;
    private static int towerCount;
    private static double averageTowerToTowerDistance;
    private static double averageTowerToHqDistance;

    //--One time triggers
    private static boolean trigger1;
    private static boolean trigger2;
    private static boolean trigger3;
    private static boolean trigger4;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHqLocation = rc.getLocation(); //--This is the HQ!
        enemyHqLocation = rc.senseEnemyHQLocation();

        BuildingQueue.init(rcC);
        Communication.init(rcC);
        SupplySharing.init(rcC);

        analyzeMap();
        setInitialBuildings();
        loop();
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

        averageTowerToTowerDistance = sumDistance / numberOfDistances;

        sumDistance = 0;
        for (int i = 0; i < towerCount; i++) {
            sumDistance += enemyTowers[i].distanceSquaredTo(enemyHqLocation);
        }

        averageTowerToHqDistance = sumDistance / towerCount;

        System.out.printf(
                "hqDist: %d\ncount %d\ntower2tower: %f\ntower2Hq: %f\n",
                distanceBetweenHq,
                towerCount,
                averageTowerToTowerDistance,
                averageTowerToHqDistance);
    }

    private static void setInitialBuildings() throws GameActionException {
        BuildingQueue.addBuilding(Building.MINER_FACTORY);
        BuildingQueue.addBuilding(Building.HELIPAD);
        BuildingQueue.addBuilding(Building.HELIPAD);
        BuildingQueue.addBuilding(Building.AEROSPACE_LAB);
    }

    private static void loop() {
        while (true) {
            try {
                Debug.setString(0, String.format("%f at beginning of round", rc.getTeamOre()), rc);
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

        setOrders();
        queueBuildings();

        if (rc.isWeaponReady()
                && Clock.getRoundNum() > DO_NOT_ATTACK_BEFORE_ROUND) {
            tryToAttack();
        }

        if (rc.isCoreReady()) {
            if (shouldSpawnBeaver(friendlyRobots)) {
                spawn(RobotType.BEAVER);
            }
        }
    }

    private static void queueBuildings() throws GameActionException {
        if (Clock.getRoundNum() < 350) {
            //--Early buildings are handled by the initial buildings method
            return;
        }

        int unitCount = rc.readBroadcast(ChannelList.MINER_COUNT)
                + rc.readBroadcast(ChannelList.DRONE_COUNT)
                + rc.readBroadcast(ChannelList.LAUNCHER_COUNT);

        if (!trigger1
                && unitCount > 40) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger1 = true;
        }

        if (!trigger2
                && unitCount > 55) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger2 = true;
        }

        if (!trigger3
                && unitCount > 65) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger3 = true;
        }

        //--Only add an aerospace lab if we already have a launcher
        int launcherCount = rc.readBroadcast(ChannelList.LAUNCHER_COUNT);
        if (rc.getTeamOre() > RobotType.AEROSPACELAB.oreCost
                && launcherCount > 0) {
            BuildingQueue.addBuildingWithPostDelay(Building.AEROSPACE_LAB, RobotType.AEROSPACELAB.buildTurns);
        }
    }

    private static void setOrders() throws GameActionException {
        Communication.setOrder(Order.DRONE_DEFEND, Order.YES);
        Communication.setOrder(Order.DRONE_PESTER, Clock.getRoundNum() < 400 ? Order.YES : Order.NO);
        Communication.setOrder(Order.SPAWN_MORE_LAUNCHERS, Order.YES);

        int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
        Communication.setOrder(Order.SPAWN_MORE_MINERS, minerCount < 35 ? Order.YES : Order.NO);

        int droneCount = rc.readBroadcast(ChannelList.DRONE_COUNT);
        Communication.setOrder(Order.SPAWN_MORE_DRONES, Order.YES);
    }

    private static void tryToAttack() throws GameActionException {
        int myTowerCount = rc.senseTowerLocations().length;
        int attackRadiusSquared = myTowerCount > 1 ?
                                  GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED
                                                   : RobotType.HQ.attackRadiusSquared;

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length > 0) {
            rc.attackLocation(enemiesInAttackRange[0].location);
            return;
        }

        //--Try splash attack!
        if (myTowerCount > 4) {
            RobotInfo[]
                    enemiesInSplashRange =
                    rc.senseNearbyRobots(
                            attackRadiusSquared + GameConstants.HQ_BUFFED_SPLASH_RADIUS_SQUARED,
                            enemyTeam);
            if (enemiesInSplashRange.length == 0) {
                return;
            }

            MapLocation enemyToSplash = enemiesInSplashRange[0].location;
            MapLocation locationToSplash = enemyToSplash;
            while (myHqLocation.distanceSquaredTo(locationToSplash) > attackRadiusSquared) {
                locationToSplash = locationToSplash.add(locationToSplash.directionTo(myHqLocation));
            }

            if (enemyToSplash.distanceSquaredTo(locationToSplash) <= GameConstants.HQ_BUFFED_SPLASH_RADIUS_SQUARED) {
                rc.setIndicatorString(0, "splash on round " + Clock.getRoundNum());
                rc.attackLocation(locationToSplash);
            }
        }
    }

    private static boolean shouldSpawnBeaver(RobotInfo[] friendlyRobots) {
        if (rc.getTeamOre() < RobotType.BEAVER.oreCost) {
            return false;
        }

        int beaverCount = Helper.getRobotsOfType(friendlyRobots, RobotType.BEAVER);
        return beaverCount < BEAVER_COUNT;
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

    private static MapLocation getFortifyLocation() {
        //--TODO: This logic needs to account for VOID tiles, etc...
        return Helper.getWaypoint(.7, myHqLocation, enemyHqLocation);
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
