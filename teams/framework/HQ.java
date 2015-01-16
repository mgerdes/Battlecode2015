package framework;

import framework.constants.Building;
import framework.constants.ChannelList;
import framework.constants.Order;
import framework.util.Helper;
import battlecode.common.*;

public class HQ {
    private static RobotController rc;
    private static Direction[] directions = Direction.values();
    private static Team enemyTeam;
    private static Team myTeam;
    private static MapLocation myHqLocation;
    private static MapLocation enemyHqLocation;

    private static final int HQ_TRY_ATTACK_AFTER_ROUND = 100;
    private static final int HQ_BROADCAST_ATTACK_LOCATION_AFTER_ROUND = 100;

    private static final int SPAWN_ON = 1;
    private static final int SPAWN_OFF = 0;

    //--Map analysis data
    private static int distanceBetweenHq;
    private static int towerCount;
    private static double averageTowerToTowerDistance;
    private static double averageTowerToHqDistance;
    private static double oreNearHq;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHqLocation = rc.getLocation(); //--This is the HQ!
        enemyHqLocation = rc.senseEnemyHQLocation();

        BuildingQueue.init(rcC);
        Communication.init(rcC);
        SupplySharing.init(rcC);
        MessageBoard.init(rcC);

        analyzeMap();
        initializeChannels();
        setInitialBuildings();
        loop();
    }

    private static void initializeChannels() throws GameActionException {
        rc.broadcast(ChannelList.TOWER_VOID_COUNT, 1000000);
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
                "hqDist: %d\ncount %d\ntower2tower: %f\ntower2Hq: %f\noreNearHQ: %f\n",
                distanceBetweenHq,
                towerCount,
                averageTowerToTowerDistance,
                averageTowerToHqDistance,
                oreNearHq);
    }

    private static void setInitialBuildings() throws GameActionException {
        if (rc.getTeam() == Team.A) {
            BuildingQueue.addBuilding(Building.MINER_FACTORY);
            BuildingQueue.addBuilding(Building.BARRACKS);
            BuildingQueue.addBuilding(Building.TANK_FACTORY);
        }
        else {
            BuildingQueue.addBuilding(Building.MINER_FACTORY);
            BuildingQueue.addBuilding(Building.HELIPAD);
            BuildingQueue.addBuilding(Building.HELIPAD);
        }
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
        SupplySharing.shareMore();

        RobotInfo[] friendlyRobots = rc.senseNearbyRobots(1000000, myTeam);

        setOrders();
        queueBuildings();
        updateRallyPoint();

        if (Clock.getRoundNum() > HQ_BROADCAST_ATTACK_LOCATION_AFTER_ROUND) {
            broadcastAttackLocation();
        }

        if (rc.isWeaponReady()
                && Clock.getRoundNum() > HQ_TRY_ATTACK_AFTER_ROUND) {
            tryToAttack();
        }

        if (rc.isCoreReady()) {
            if (shouldSpawnBeaver(friendlyRobots)) {
                spawn(RobotType.BEAVER);
            }
        }
    }

    private static void updateRallyPoint() throws GameActionException {
        MapLocation currentRallyPoint = Communication.readMapLocationFromChannel(ChannelList.RALLY_POINT);
        if (currentRallyPoint == null) {
            MapLocation towerWithFewestVoids = Communication.readMapLocationFromChannel(ChannelList.OUR_TOWER_WITH_LOWEST_VOID_COUNT);
            if (towerWithFewestVoids != null) {
                Communication.setMapLocationOnChannel(
                        towerWithFewestVoids.add(towerWithFewestVoids.directionTo(enemyHqLocation), 4),
                        ChannelList.RALLY_POINT);
            }
        }
    }

    private static void broadcastAttackLocation() throws GameActionException {
        //--if enemy has towers
        //    return the closest one to our HQ
        //--else return enemy HQ
        MapLocation[] enemyTowerLocations = rc.senseEnemyTowerLocations();
        if (enemyTowerLocations.length == 0) {
            Communication.setMapLocationOnChannel(enemyHqLocation, ChannelList.STRUCTURE_TO_ATTACK);
            return;
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

        Communication.setMapLocationOnChannel(enemyTowerLocations[index], ChannelList.STRUCTURE_TO_ATTACK);
    }

    private static void queueBuildings() throws GameActionException {
        queueSupplyTowers();

        if (rc.getTeam() == Team.A) {
            if (Clock.getRoundNum() > 400
                    && rc.getTeamOre() > RobotType.TANKFACTORY.oreCost) {
                BuildingQueue.addBuildingWithPostDelay(Building.TANK_FACTORY, RobotType.TANKFACTORY.buildTurns * 2);
            }
        }
        else {
            if (Clock.getRoundNum() > 300
                    && rc.getTeamOre() > RobotType.HELIPAD.oreCost) {
                BuildingQueue.addBuildingWithPostDelay(Building.HELIPAD, RobotType.HELIPAD.buildTurns * 2);
            }
        }
    }

    private static void queueSupplyTowers() throws GameActionException {
        //--Add 2 supply depos if we are not producing enough
        int supplyConsumption = rc.readBroadcast(ChannelList.BASHER_COUNT) * RobotType.BASHER.supplyUpkeep
                + rc.readBroadcast(ChannelList.MINER_COUNT) * RobotType.MINER.supplyUpkeep
                + rc.readBroadcast(ChannelList.SOLDIER_COUNT) * RobotType.SOLDIER.supplyUpkeep;

        int numberOfSupplyTowers = rc.readBroadcast(ChannelList.SUPPLY_DEPOT_COUNT);

        double supplyProduction = GameConstants.SUPPLY_GEN_BASE
                * (GameConstants.SUPPLY_GEN_MULTIPLIER
                + Math.pow(numberOfSupplyTowers, GameConstants.SUPPLY_GEN_EXPONENT));

        if (supplyProduction < supplyConsumption * 1.1) {
            BuildingQueue.addBuildingWithPostDelay(Building.SUPPLY_DEPOT, RobotType.SUPPLYDEPOT.buildTurns);
        }
    }

    private static void setOrders() throws GameActionException {
        if (rc.getTeam() == Team.A) {
            //--Spawn up to 30 soldiers
            int soldierCount = rc.readBroadcast(ChannelList.SOLDIER_COUNT);
            MessageBoard.setSpawn(RobotType.SOLDIER, soldierCount < 30 ? SPAWN_ON : SPAWN_OFF);

            //--Spawn up to 35 miners
            int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
            MessageBoard.setSpawn(RobotType.MINER, minerCount < 35 ? SPAWN_ON : SPAWN_OFF);

            MessageBoard.setSpawn(RobotType.TANK, SPAWN_ON);

            MessageBoard.setDefaultOrder(RobotType.SOLDIER, Order.Rally);
            MessageBoard.setDefaultOrder(RobotType.TANK, Order.Rally);
        }
        else {
            //--Spawn up to 35 miners
            int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
            MessageBoard.setSpawn(RobotType.MINER, minerCount < 35 ? SPAWN_ON : SPAWN_OFF);

            //--Spawn a bunch of drones
            MessageBoard.setSpawn(RobotType.DRONE, SPAWN_ON);

            MessageBoard.setDefaultOrder(RobotType.DRONE, Order.AttackEnemyMiners);
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
            RobotInfo[] enemiesInSplashRange = rc.senseNearbyRobots(
                    attackRadiusSquared + GameConstants
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

    private static boolean shouldSpawnBeaver(RobotInfo[] friendlyRobots) throws GameActionException {
        if (rc.getTeamOre() < RobotType.BEAVER.oreCost) {
            return false;
        }

        //--In early game we want one beaver.
        //--When we have enough miners, we want two beavers
        int beaverCount = Helper.getRobotsOfType(friendlyRobots, RobotType.BEAVER);
        if (beaverCount == 0) {
            return true;
        }

        if (beaverCount > 1) {
            return false;
        }

        int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
        return minerCount > 10;
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
