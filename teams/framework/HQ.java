package framework;

import framework.constants.Building;
import framework.constants.ChannelList;
import framework.constants.Order;
import framework.util.Helper;
import battlecode.common.*;
import framework.util.Debug;

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

        System.out.printf("hqDist: %d\ncount %d\ntower2tower: %f\ntower2Hq: %f\n", distanceBetweenHq,
                towerCount, averageTowerToTowerDistance, averageTowerToHqDistance);
    }

    private static void setInitialBuildings() throws GameActionException {
        if (distanceBetweenHq < 2000) {
            BuildingQueue.addBuilding(Building.HELIPAD);
            BuildingQueue.addBuilding(Building.MINER_FACTORY);
        }
        else {
            BuildingQueue.addBuilding(Building.MINER_FACTORY);
            BuildingQueue.addBuilding(Building.HELIPAD);
        }
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
        if (Clock.getRoundNum() < 250) {
            //--Early buildings are handled by the initial buildings method
            return;
        }

        if (rc.getTeamOre() > RobotType.HELIPAD.oreCost) {
            BuildingQueue.addBuildingWithPostDelay(Building.HELIPAD, RobotType.HELIPAD.buildTurns * 2);
        }

        int unitCount = rc.readBroadcast(ChannelList.MINER_COUNT) + rc.readBroadcast(ChannelList.DRONE_COUNT);
        if (!trigger1
                && unitCount > 40) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger1 = true;
        }

        if (!trigger2
                && unitCount > 60) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger2 = true;
        }

        if (!trigger3
                && unitCount > 70) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger3 = true;
        }

        if (!trigger4
                && unitCount > 80) {
            BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
            trigger4 = true;
        }
    }

    private static void setOrders() throws GameActionException {
        int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
        int droneCount = rc.readBroadcast(ChannelList.DRONE_COUNT);

        Communication.setOrder(Order.SPAWN_MORE_DRONES, Order.YES);

        if (distanceBetweenHq < 2000) {
            Communication.setOrder(Order.SPAWN_MORE_MINERS, minerCount < 20 ? Order.YES : Order.NO);
            Communication.setOrder(Order.DRONE_SWARM, Order.YES);
        }
        else {
            Communication.setOrder(Order.SPAWN_MORE_MINERS, minerCount < 30 ? Order.YES : Order.NO);
            Communication.setOrder(Order.DRONE_DEFEND, Order.YES);
        }

        if (droneCount > 60) {
            if (droneCount > 80) {
                Communication.setOrder(Order.DRONE_ATTACK, Order.YES);
                Communication.setOrder(Order.DRONE_SWARM, Order.NO);
                MapLocation enemyStructure = getStructureToAttack();
                rc.broadcast(ChannelList.STRUCTURE_TO_ATTACK_X, enemyStructure.x);
                rc.broadcast(ChannelList.STRUCTURE_TO_ATTACK_Y, enemyStructure.y);
            }
            else {
                Communication.setOrder(Order.DRONE_ATTACK, Order.NO);
                Communication.setOrder(Order.DRONE_SWARM, Order.YES);
            }
        }
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
            RobotInfo[] enemiesInSplashRange = rc.senseNearbyRobots(attackRadiusSquared + GameConstants
                    .HQ_BUFFED_SPLASH_RADIUS_SQUARED, enemyTeam);
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
