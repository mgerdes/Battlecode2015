package team030;

import team030.communication.HqOrders;
import team030.communication.Radio;
import team030.constants.Building;
import team030.communication.Channel;
import team030.constants.Order;
import team030.constants.Symmetry;
import team030.util.Helper;
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

    private static final int LAUNCHERS_REQUIRED_FOR_ATTACK = 3;

    private static final int SPAWN_ON = 1;
    private static final int SPAWN_OFF = 0;

    //--Map analysis data
    private static int distanceBetweenHq;
    private static int towerCount;
    private static double averageTowerToTowerDistance;
    private static double averageTowerToHqDistance;
    private static double oreNearHq;
    private static MapLocation[] originalEnemyTowers;
    private static MapLocation[] myTowers;
    private static boolean printedMapDataForDebug;
    private static boolean mapBuilderInitialized;
    private static boolean allTerrainTilesBroadcast;
    private static boolean towersFormWall;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHqLocation = rc.getLocation(); //--This is the HQ!
        enemyHqLocation = rc.senseEnemyHQLocation();
        originalEnemyTowers = rc.senseEnemyTowerLocations();
        myTowers = rc.senseTowerLocations();

        BuildingQueue.init(rcC);
        Radio.init(rcC);
        SupplySharing.init(rcC);
        HqOrders.init(rcC);
        MapAnalysis.init(rcC);
        PathBuilder.init(rcC);

        analyzeMap();
        initializeChannels();
        setInitialBuildings();
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

    private static void doYourThing() throws GameActionException {
        int roundNumber = Clock.getRoundNum();
        if (roundNumber == 2) {
            broadcastAbsolutePoiCoordinates();
        }

        SupplySharing.shareMore();

        RobotInfo[] friendlyRobots = rc.senseNearbyRobots(1000000, myTeam);

        updateSpawningAndOrders();
        queueSupplyTowers();

        if (roundNumber > 200) {
            queueBuildings();
        }

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

        broadcastMapInformation();
    }

    private static void broadcastAbsolutePoiCoordinates() throws GameActionException {
        for (int i = 0; i < towerCount; i++) {
            Radio.setMapLocationOnChannel(originalEnemyTowers[i], Channel.POI_ABSOLUTE[i]);
        }

        Radio.setMapLocationOnChannel(enemyHqLocation, Channel.POI_ABSOLUTE[towerCount]);
    }

    private static void broadcastMapInformation() throws GameActionException {
        if (allTerrainTilesBroadcast
                || rc.readBroadcast(Channel.PERIMETER_SURVEY_COMPLETE) != 1) {
            return;
        }

        if (!mapBuilderInitialized) {
            MapBuilder.init(rc);
            mapBuilderInitialized = true;
        }

        if (!printedMapDataForDebug) {
            System.out.printf("\nMapWidth: %d, MapHeight %s\n",
                              rc.readBroadcast(Channel.MAP_WIDTH),
                              rc.readBroadcast(Channel.MAP_HEIGHT));

            System.out.printf("\nNE %s\nSE %s\nSW %s\nNW %s\n",
                              Radio.readMapLocationFromChannel(Channel.NE_MAP_CORNER),
                              Radio.readMapLocationFromChannel(Channel.SE_MAP_CORNER),
                              Radio.readMapLocationFromChannel(Channel.SW_MAP_CORNER),
                              Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER));

            System.out.println("my hq" + myHqLocation);
            System.out.println("my hq" + MapBuilder.getReflectedMapLocation(enemyHqLocation));

            System.out.println("enemy hq" + enemyHqLocation);
            System.out.println("enemy hq" + MapBuilder.getReflectedMapLocation(myHqLocation));

            printedMapDataForDebug = true;
        }

        if (!allTerrainTilesBroadcast) {
            allTerrainTilesBroadcast = MapBuilder.processUntilComplete(Clock.getBytecodesLeft() - 30);
            if (Clock.getBytecodeNum() < 1000) {
                System.out.println("bytecodes exceeded?");
            }
        }

        if (allTerrainTilesBroadcast) {
            PathBuilder.setup(originalEnemyTowers, enemyHqLocation);
            rc.broadcast(Channel.READY_FOR_BFS, 1);
        }
    }

    private static void initializeChannels() throws GameActionException {
        rc.broadcast(Channel.TOWER_VOID_COUNT, 1000000);
    }

    private static void analyzeMap() throws GameActionException {
        distanceBetweenHq = myHqLocation.distanceSquaredTo(enemyHqLocation);
        towerCount = originalEnemyTowers.length;

        double sumDistance = 0;
        int numberOfDistances = 0;
        for (int i = 0; i < towerCount; i++) {
            for (int j = i; j < towerCount; j++) {
                sumDistance += originalEnemyTowers[i].distanceSquaredTo(originalEnemyTowers[j]);
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
            sumDistance += originalEnemyTowers[i].distanceSquaredTo(enemyHqLocation);
        }

        averageTowerToHqDistance = sumDistance / towerCount;

        int symmetryType = getSymmetryType();
        rc.broadcast(Channel.MAP_SYMMETRY, symmetryType);

        String symmetryString;
        if (symmetryType == Symmetry.HORIZONTAL) {
            symmetryString = "Horizontal";
        }
        else if (symmetryType == Symmetry.VERTICAL) {
            symmetryString = "Vertical";
        }
        else {
            symmetryString = "Rotational";
        }

        towersFormWall = MapAnalysis.towersFormWall(myTowers);

        System.out.printf(
                "hqDist: %d\ncount %d\ntower2tower: %f\ntower2Hq: %f\noreNearHQ: %f\nsymmetryType: %s\n",
                distanceBetweenHq,
                towerCount,
                averageTowerToTowerDistance,
                averageTowerToHqDistance,
                oreNearHq,
                symmetryString);
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
                if (!oneHasMatchingX(originalEnemyTowers, tower.x)) {
                    return Symmetry.ROTATION;
                }
            }
        }
        else {
            //--For all of my towers, enemy must have one with same y value
            for (MapLocation tower : myTowers) {
                if (!oneHasMatchingY(originalEnemyTowers, tower.y)) {
                    return Symmetry.ROTATION;
                }
            }
        }

        return hqSameX ? Symmetry.HORIZONTAL : Symmetry.VERTICAL;
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
        BuildingQueue.addBuilding(Building.SUPPLY_DEPOT);
        BuildingQueue.addBuilding(Building.AEROSPACE_LAB);
    }

    private static void queueBuildings() throws GameActionException {
        queueSupplyTowers();

        if (rc.getTeamOre() > RobotType.AEROSPACELAB.oreCost
                && Clock.getRoundNum() > 600) {
            BuildingQueue.addBuildingWithPostDelay(
                    Building.AEROSPACE_LAB,
                    (int) (RobotType.AEROSPACELAB.buildTurns * 1.3));
        }
    }

    private static void updateRallyPoint() throws GameActionException {
        MapLocation currentRallyPoint = Radio.readMapLocationFromChannel(Channel.RALLY_POINT);
        if (currentRallyPoint == null) {
            MapLocation towerWithFewestVoids = Radio.readMapLocationFromChannel(
                    Channel.OUR_TOWER_WITH_LOWEST_VOID_COUNT);
            if (towerWithFewestVoids != null) {
                Radio.setMapLocationOnChannel(
                        towerWithFewestVoids.add(towerWithFewestVoids.directionTo(enemyHqLocation), 4),
                        Channel.RALLY_POINT);
            }
        }
    }

    private static void broadcastAttackLocation() throws GameActionException {
        //--if enemy has towers
        //    return the closest one to our HQ
        //--else return enemy HQ
        MapLocation[] currentEnemyTowers = rc.senseEnemyTowerLocations();
        if (currentEnemyTowers.length == 0) {
            rc.broadcast(Channel.POI_TO_ATTACK, towerCount);
            return;
        }

        int index = 0;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < currentEnemyTowers.length; i++) {
            int thisDistance = myHqLocation.distanceSquaredTo(currentEnemyTowers[i]);
            if (thisDistance < minDistance) {
                minDistance = thisDistance;
                index = i;
            }
        }

        rc.broadcast(Channel.POI_TO_ATTACK, getPoiNumberForEnemyTower(currentEnemyTowers[index]));
    }

    private static int getPoiNumberForEnemyTower(MapLocation location) {
        //--Assuming that the location is an enemy tower
        for (int i = 0; i < towerCount; i++) {
            if (originalEnemyTowers[i].equals(location)) {
                return i;
            }
        }

        return -1;
    }

    private static void queueSupplyTowers() throws GameActionException {
        //--Add 2 supply depos if we are not producing enough
        int supplyConsumption = rc.readBroadcast(Channel.LAUNCHER_COUNT) * RobotType.LAUNCHER.supplyUpkeep
                + rc.readBroadcast(Channel.MINER_COUNT) * RobotType.MINER.supplyUpkeep
                + rc.readBroadcast(Channel.SOLDIER_COUNT) * RobotType.SOLDIER.supplyUpkeep
                + rc.readBroadcast(Channel.DRONE_COUNT) * RobotType.DRONE.supplyUpkeep;

        int numberOfSupplyTowers = rc.readBroadcast(Channel.SUPPLY_DEPOT_COUNT);

        double supplyProduction = GameConstants.SUPPLY_GEN_BASE
                * (GameConstants.SUPPLY_GEN_MULTIPLIER
                + Math.pow(numberOfSupplyTowers, GameConstants.SUPPLY_GEN_EXPONENT));

        if (supplyProduction < supplyConsumption * 1.1) {
            BuildingQueue.addBuildingWithPostDelay(Building.SUPPLY_DEPOT, RobotType.SUPPLYDEPOT.buildTurns);
        }
    }

    private static void updateSpawningAndOrders() throws GameActionException {
        int currentRound = Clock.getRoundNum();

        int launcherCount = rc.readBroadcast(Channel.LAUNCHER_COUNT);
        boolean doTheBigAttack = launcherCount >= LAUNCHERS_REQUIRED_FOR_ATTACK;

        //--Spawn up to 35 miners
        int minerCount = rc.readBroadcast(Channel.MINER_COUNT);
        HqOrders.setSpawn(RobotType.MINER, minerCount < 35 ? SPAWN_ON : SPAWN_OFF);

        //--Spawn up to 20 drones
        int droneCount = rc.readBroadcast(Channel.DRONE_COUNT);
        int droneMax = 20;


        if (towersFormWall
                && averageTowerToHqDistance > 180) {
            droneMax = 1;
        }

        HqOrders.setSpawn(RobotType.DRONE, droneCount < droneMax ? SPAWN_ON : SPAWN_OFF);

        //--Spawn up to 20 soldiers
        int soldierCount = rc.readBroadcast(Channel.SOLDIER_COUNT);
        int soldierMax = 20;

        if (towersFormWall
                && averageTowerToHqDistance > 180
                && launcherCount < 1) {
            soldierMax = 0;
        }

        HqOrders.setSpawn(RobotType.SOLDIER, soldierCount < soldierMax ? SPAWN_ON : SPAWN_OFF);

        //--Spawn launchers!
        HqOrders.setSpawn(RobotType.LAUNCHER, SPAWN_ON);

        //--Set orders
        if (doTheBigAttack) {
            HqOrders.setDefaultFor(RobotType.LAUNCHER, Order.AttackEnemyStructure);
            HqOrders.setDefaultFor(RobotType.SOLDIER, Order.AttackEnemyStructure);
        }
        else {
            HqOrders.setDefaultFor(RobotType.LAUNCHER, Order.Rally);
            HqOrders.setDefaultFor(RobotType.SOLDIER, Order.DefendMiners);
        }


        HqOrders.setDefaultFor(RobotType.DRONE, Order.Swarm);

        if (rc.readBroadcast(Channel.PERIMETER_SURVEY_COMPLETE) == 0) {
            HqOrders.setPriorityFor(1, RobotType.DRONE, Order.SurveyMap);
        }
        else {
            HqOrders.setPriorityFor(1, RobotType.DRONE, Order.MoveSupply);
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
            RobotInfo[] enemiesInSplashRange = rc.senseNearbyRobots(64, enemyTeam);
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

        //--Build the second beaver after we have started a mining factory
        return rc.checkDependencyProgress(RobotType.MINERFACTORY) != DependencyProgress.NONE;
    }

    private static void spawn(RobotType type) throws GameActionException {
        int startingDirection = Helper.getInt(enemyHqLocation.directionTo(myHqLocation));
        int direction = startingDirection;
        while (!rc.canSpawn(directions[direction], type)) {
            direction = (direction + 1) % 8;
            if (direction == startingDirection) {
                return;
            }
        }

        rc.spawn(directions[direction], type);
    }
}
