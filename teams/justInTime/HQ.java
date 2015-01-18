package justInTime;

import justInTime.constants.Building;
import justInTime.constants.ChannelList;
import justInTime.constants.Order;
import justInTime.constants.Symmetry;
import justInTime.util.Helper;
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
    private static MapLocation[] enemyTowers;
    private static MapLocation[] myTowers;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHqLocation = rc.getLocation(); //--This is the HQ!
        enemyHqLocation = rc.senseEnemyHQLocation();
        enemyTowers = rc.senseEnemyTowerLocations();
        myTowers = rc.senseTowerLocations();

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

    private static void analyzeMap() throws GameActionException {
        distanceBetweenHq = myHqLocation.distanceSquaredTo(enemyHqLocation);
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

        int symmetryType = getSymmetryType();
        rc.broadcast(ChannelList.MAP_SYMMETRY, symmetryType);

        System.out.printf(
                "hqDist: %d\ncount %d\ntower2tower: %f\ntower2Hq: %f\noreNearHQ: %f\nsymmetryType: %s\n",
                distanceBetweenHq,
                towerCount,
                averageTowerToTowerDistance,
                averageTowerToHqDistance,
                oreNearHq,
                symmetryType == Symmetry.REFLECTION ? "Reflection" : "Rotation");
    }

    private static int getSymmetryType() {
        boolean hqSameX = myHqLocation.x == enemyHqLocation.x;
        boolean hqSameY = myHqLocation.y == enemyHqLocation.y;
        if (!hqSameX
                && !hqSameY) {
            return Symmetry.ROTATION;
        }

        if (hqSameX) {
            //--For all of my towers, enemy must have one with same x value
            for (MapLocation tower : myTowers) {
                if (!oneHasMatchingX(enemyTowers, tower.x)) {
                    return Symmetry.ROTATION;
                }
            }
        }
        else {
            //--For all of my towers, enemy must have one with same y value
            for (MapLocation tower : myTowers) {
                if (!oneHasMatchingY(enemyTowers, tower.y)) {
                    return Symmetry.ROTATION;
                }
            }
        }

        return Symmetry.REFLECTION;
    }

    private static boolean oneHasMatchingX(MapLocation[] locations, int xValue) {
        for (MapLocation location : locations) {
            if (location.x == xValue) {
                return true;
            }
        }

        return false;
    }

    private static boolean oneHasMatchingY(MapLocation[] locations, int yValue) {
        for (MapLocation location : locations) {
            if (location.y == yValue) {
                return true;
            }
        }

        return false;
    }

    private static void setInitialBuildings() throws GameActionException {
        BuildingQueue.addBuilding(Building.MINER_FACTORY);
        BuildingQueue.addBuilding(Building.HELIPAD);
        BuildingQueue.addBuilding(Building.BARRACKS);
        BuildingQueue.addBuilding(Building.AEROSPACE_LAB);
        BuildingQueue.addBuilding(Building.BARRACKS);
        BuildingQueue.addBuilding(Building.AEROSPACE_LAB);
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
    }

    private static void queueSupplyTowers() throws GameActionException {
        //--Add 2 supply depos if we are not producing enough
        int supplyConsumption = rc.readBroadcast(ChannelList.LAUNCHER_COUNT) * RobotType.LAUNCHER.supplyUpkeep
                + rc.readBroadcast(ChannelList.MINER_COUNT) * RobotType.MINER.supplyUpkeep
                + rc.readBroadcast(ChannelList.SOLDIER_COUNT) * RobotType.SOLDIER.supplyUpkeep
                + rc.readBroadcast(ChannelList.DRONE_COUNT) * RobotType.DRONE.supplyUpkeep;

        int numberOfSupplyTowers = rc.readBroadcast(ChannelList.SUPPLY_DEPOT_COUNT);

        double supplyProduction = GameConstants.SUPPLY_GEN_BASE
                * (GameConstants.SUPPLY_GEN_MULTIPLIER
                + Math.pow(numberOfSupplyTowers, GameConstants.SUPPLY_GEN_EXPONENT));

        if (supplyProduction < supplyConsumption * 1.1) {
            BuildingQueue.addBuildingWithPostDelay(Building.SUPPLY_DEPOT, RobotType.SUPPLYDEPOT.buildTurns);
        }
    }

    private static void setOrders() throws GameActionException {
        //--Spawn up to 35 miners
        int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
        MessageBoard.setSpawn(RobotType.MINER, minerCount < 35 ? SPAWN_ON : SPAWN_OFF);

        //--Spawn up to 20 drones
        int droneCount = rc.readBroadcast(ChannelList.DRONE_COUNT);
        MessageBoard.setSpawn(RobotType.DRONE, droneCount < 20 ? SPAWN_ON : SPAWN_OFF);

        //--Spawn up to 25 soldiers
        int soldierCount = rc.readBroadcast(ChannelList.SOLDIER_COUNT);
        MessageBoard.setSpawn(RobotType.SOLDIER, soldierCount < 25 ? SPAWN_ON : SPAWN_OFF);

        //--Spawn launchers!
        MessageBoard.setSpawn(RobotType.LAUNCHER, SPAWN_ON);

        //--Orders for early/mid game
        MessageBoard.setDefaultOrder(RobotType.SOLDIER, Order.DefendMiners);

        int launcherCount = rc.readBroadcast(ChannelList.LAUNCHER_COUNT);

        MessageBoard.setDefaultOrder(RobotType.DRONE, Order.AttackEnemyMiners);

        if (rc.readBroadcast(ChannelList.SURVEY_COMPLETE) == 0) {
            MessageBoard.setPriorityOrder(1, RobotType.DRONE, Order.SurveyMap);
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
