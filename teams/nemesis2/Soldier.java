package nemesis2;

import battlecode.common.*;
import nemesis2.communication.Channel;
import nemesis2.communication.Radio;
import nemesis2.constants.Order;
import nemesis2.navigation.SafeBug;
import nemesis2.util.Debug;
import nemesis2.util.Helper;

public class Soldier {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static MapLocation myHqLocation;

    private static final int[] directions = new int[]{0, -1, 1, -2, 2};
    private static final int MIN_DISTANCE_SQUARED_AWAY_FROM_HQ = 16;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        SafeBug.init(rcC);
        SupplySharing.init(rcC);
        Radio.init(rcC);
        MessageBoard.init(rcC);

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
        SupplySharing.share();

        Order order = MessageBoard.getOrder(rc.getType());

        switch (order) {
            case DefendMiners:
                defendMiners();
                break;
            case AttackEnemyMiners:
                attackEnemyMiners();
                break;
        }
    }

    private static void attackEnemyMiners() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        SafeBug.setDestination(enemyHqLocation);

        if (rc.getSupplyLevel() < 500) {
            Radio.iNeedSupply();
        }

        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
        RobotType[] typesToIgnore = new RobotType[]{RobotType.BEAVER, RobotType.MINER};

        boolean iAmCoreReady = rc.isCoreReady();

        if (iAmCoreReady) {
            //--If any fighting units can shoot us, move away
            for (RobotInfo enemy : enemiesInSensorRange) {
                if (enemy.type != RobotType.MINER
                        && enemy.type != RobotType.BEAVER
                        && enemy.type.attackRadiusSquared >= currentLocation.distanceSquaredTo(enemy.location)) {
                    Direction runawayDirection = SafeBug.getDirection(
                            currentLocation,
                            null,
                            enemiesInSensorRange,
                            typesToIgnore);
                    Debug.setString(0, runawayDirection.toString(), rc);
                    if (runawayDirection != Direction.NONE) {
                        rc.move(runawayDirection);
                        return;
                    }
                    else {
                        //--We could not find a runaway direction... turn around!
                        runawayDirection = SafeBug.getPreviousDirection().opposite();
                        if (runawayDirection != Direction.NONE) {
                            tryMove(runawayDirection);
                            return;
                        }
                    }
                }
            }
        }

        //--If I can shoot any units, shoot them or wait until I can shoot them
        //--Otherwise, continue towards enemy using the awesome safe bug
        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length > 0) {
            if (rc.isWeaponReady()) {
                double lowestHealth = 1000000;
                int index = 0;
                for (int i = 0; i < enemiesInAttackRange.length; i++) {
                    double thisHealth = enemiesInAttackRange[0].health;
                    if (thisHealth < lowestHealth) {
                        lowestHealth = thisHealth;
                        index = i;
                    }
                }

                rc.attackLocation(enemiesInAttackRange[index].location);
            }
        }
        else if (iAmCoreReady) {
            Direction direction = SafeBug.getDirection(currentLocation, null, enemiesInSensorRange, typesToIgnore);
            if (direction != Direction.NONE) {
                rc.move(direction);
            }
        }
    }

    private static void defendMiners() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length > 0) {
            Radio.setDistressLocation(currentLocation);
        }

        if (rc.getSupplyLevel() < 200) {
            Radio.iNeedSupply();
        }

        if (rc.isWeaponReady()) {
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
                return;
            }
        }

        if (!rc.isCoreReady()) {
            return;
        }

        //--Distress locations take priority.
        MapLocation distressLocation = Radio.getDistressLocation();
        if (distressLocation != null) {
            SafeBug.setDestination(distressLocation);
        }
        //--If there is no distress signal, the priorities are
        //  1. nearby enemies
        //  2. enemies near towers
        //  3. avoid standing next to other soldiers
        //  4. don't stand too close to our hq
        //--If none of these three things needs to happen, the soldier sits.
        else {
            RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
            RobotInfo[] teamInCloseRange = rc.senseNearbyRobots(2, myTeam);
            MapLocation towerWhereEnemySpotted = Radio.getTowerLocationWhereEnemySpotted();
            int soldiersInCloseRange = Helper.getRobotsOfType(teamInCloseRange, RobotType.SOLDIER);

            int minerDistanceSquared = rc.readBroadcast(Channel.MINER_DISTANCE_SQUARED_TO_HQ);
            int myDistanceToHq = currentLocation.distanceSquaredTo(myHqLocation);

            if (enemiesInSensorRange.length > 0
                    && myDistanceToHq < minerDistanceSquared + 4) {
                SafeBug.setDestination(enemiesInSensorRange[0].location);
            }
            else if (towerWhereEnemySpotted != null) {
                SafeBug.setDestination(towerWhereEnemySpotted);
            }
            else if (soldiersInCloseRange > 2) {
                Direction direction = getDirectionAwayFrom(currentLocation, teamInCloseRange);
                if (direction != null) {
                    SafeBug.setDestination(currentLocation.add(direction));
                }
            }
            else if (myDistanceToHq < MIN_DISTANCE_SQUARED_AWAY_FROM_HQ) {
                SafeBug.setDestination(enemyHqLocation);
            }
            else {
                return;
            }
        }

        Direction direction = SafeBug.getDirection(currentLocation);
        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }

    private static Direction getDirectionAwayFrom(MapLocation currentLocation, RobotInfo[] teamInCloseRange) {
        int length = teamInCloseRange.length;
        Direction[] allDirection = new Direction[length];
        for (int i = 0; i < length; i++) {
            allDirection[i] = currentLocation.directionTo(teamInCloseRange[i].location);
        }

        Direction sum = Helper.getSumOfDirections(allDirection);
        if (sum != Direction.NONE) {
            return sum.opposite();
        }

        return null;
    }

    private static void tryMove(Direction initial) throws GameActionException {
        int initialDirectionValue = Helper.getInt(initial);
        for (int i = 0; i < directions.length; i++) {
            Direction direction = Helper.getDirection(initialDirectionValue + i);
            if (rc.canMove(direction)) {
                rc.move(direction);
                return;
            }
        }
    }
}
