package justInTime2;

import battlecode.common.*;
import justInTime2.communication.Channel;
import justInTime2.communication.HqOrders;
import justInTime2.communication.Radio;
import justInTime2.constants.Order;
import justInTime2.navigation.BasicNav;
import justInTime2.navigation.Bfs;
import justInTime2.navigation.SafeBug;
import justInTime2.util.Helper;

public class Soldier {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static MapLocation myHqLocation;

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
        HqOrders.init(rcC);
        BasicNav.init(rcC);
        Bfs.init(rcC);

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

        Order order = HqOrders.getOrder(rc.getType());

        switch (order) {
            case DefendMiners:
                defendMiners();
                break;
            case AttackEnemyStructure:
                attackEnemyStructure();
                break;
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

    private static void attackEnemyStructure() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        //--Check supply
        if (rc.getSupplyLevel() < 300) {
            Radio.iNeedSupply();
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);

        //--If there are no nearby enemies, move closer
        if (enemiesInAttackRange.length == 0) {
            int poiToAttack = rc.readBroadcast(Channel.POI_TO_ATTACK);
            MapLocation structureToAttack = Radio.readMapLocationFromChannel(Channel.POI_ABSOLUTE[poiToAttack]);
            Direction direction = Bfs.getDirection(currentLocation, poiToAttack);
            if (direction != Direction.NONE) {
                direction = BasicNav.getNavigableDirectionClosestTo(direction);
            }

            //--If bfs doesn't work, use bug direction
            if (direction == Direction.NONE) {
                SafeBug.setDestination(structureToAttack);
                direction = SafeBug.getDirection(currentLocation, structureToAttack);
            }

            //--Check if going here would put us un range of a different tower
            MapLocation next = currentLocation.add(direction);
            MapLocation enemyTower = Helper.getTowerLocationThatCanAttackLocation(next, rc.senseEnemyTowerLocations());

            if (enemyTower == null
                    && rc.isCoreReady()
                    && direction != Direction.NONE) {
                rc.move(direction);
            }

            return;
        }

        //--Attack an enemy
        int closest = Helper.getIndexOfClosestRobot(enemiesInAttackRange, currentLocation);
        MapLocation locationToAttack = enemiesInAttackRange[closest].location;
        if (rc.isWeaponReady()) {
            rc.attackLocation(locationToAttack);
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
}
