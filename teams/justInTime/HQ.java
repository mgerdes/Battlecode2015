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

    private static final int MIDGAME_ROUND_NUMBER = 600;
    private static final int LAUNCHER_ATTACK_ROUND_NUMBER = 1200;

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
    private static boolean printedMapDataForDebug;

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

        updateSpawningAndOrders();
        queueBuildings();
        moreMapAnalysis();

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

    private static void moreMapAnalysis() throws GameActionException {
        if (rc.readBroadcast(ChannelList.SURVEY_COMPLETE) != 1) {
            return;
        }

        if (!printedMapDataForDebug) {
            System.out.printf("\nMapWidth: %d, MapHeight %s\n",
                              rc.readBroadcast(ChannelList.MAP_WIDTH),
                              rc.readBroadcast(ChannelList.MAP_HEIGHT));
            System.out.printf("\nNE %s\nSE %s\nSW %s\nNW %s\n",
                              Communication.readMapLocationFromChannel(ChannelList.NE_MAP_CORNER),
                              Communication.readMapLocationFromChannel(ChannelList.SE_MAP_CORNER),
                              Communication.readMapLocationFromChannel(ChannelList.SW_MAP_CORNER),
                              Communication.readMapLocationFromChannel(ChannelList.NW_MAP_CORNER));
            printedMapDataForDebug = true;
        }

        //--This is where the HQ can call some awesome BFS class
        //  or other pathfinding algorithm
        //--The class should take a bytecode limit as a parameter
        //  so we can limit the number of bytecodes the HQ uses per round
        //--Ideally we can broadcast paths to all enemy towers
        //  and a path to the enemy hq.
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
    }

    private static void queueBuildings() throws GameActionException {
        queueSupplyTowers();

        if (Clock.getRoundNum() > MIDGAME_ROUND_NUMBER
                && rc.getTeamOre() > RobotType.AEROSPACELAB.oreCost) {
            BuildingQueue.addBuildingWithPostDelay(Building.AEROSPACE_LAB,
                                                   (int) (RobotType.AEROSPACELAB.buildTurns * 1.3));
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

    private static void updateSpawningAndOrders() throws GameActionException {
        int currentRound = Clock.getRoundNum();

        //--Spawn up to 35 miners
        int minerCount = rc.readBroadcast(ChannelList.MINER_COUNT);
        MessageBoard.setSpawn(RobotType.MINER, minerCount < 35 ? SPAWN_ON : SPAWN_OFF);

        //--Spawn up to 20 drones in early game, 40 in mid-game
        int droneCount = rc.readBroadcast(ChannelList.DRONE_COUNT);
        int droneMax = currentRound > MIDGAME_ROUND_NUMBER ? 40 : 20;
        MessageBoard.setSpawn(RobotType.DRONE, droneCount < droneMax ? SPAWN_ON : SPAWN_OFF);

        //--Spawn up to 25 soldiers in early game, 40 in mid-game
        int soldierCount = rc.readBroadcast(ChannelList.SOLDIER_COUNT);
        int soldierMax = currentRound > MIDGAME_ROUND_NUMBER ? 40 : 25;
        MessageBoard.setSpawn(RobotType.SOLDIER, soldierCount < 25 ? SPAWN_ON : SPAWN_OFF);

        //--Spawn launchers!
        MessageBoard.setSpawn(RobotType.LAUNCHER, SPAWN_ON);

        //--Set orders
        MessageBoard.setDefaultOrder(RobotType.SOLDIER, Order.DefendMiners);

        if (rc.readBroadcast(ChannelList.SURVEY_COMPLETE) == 0) {
            MessageBoard.setPriorityOrder(1, RobotType.DRONE, Order.SurveyMap);
        }

        int launcherCount = rc.readBroadcast(ChannelList.LAUNCHER_COUNT);
        if (launcherCount > 1) {
            MessageBoard.setDefaultOrder(RobotType.DRONE, Order.Rally);
        }
        else {
            MessageBoard.setDefaultOrder(RobotType.DRONE, Order.AttackEnemyMiners);
        }

        if (currentRound < LAUNCHER_ATTACK_ROUND_NUMBER) {
            MessageBoard.setDefaultOrder(RobotType.LAUNCHER, Order.Rally);
        }
        else {
            MessageBoard.setDefaultOrder(RobotType.LAUNCHER, Order.AttackEnemyStructure);
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
