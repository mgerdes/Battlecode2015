package team030;

import battlecode.common.*;
import team030.communication.Channel;
import team030.communication.HqOrders;
import team030.communication.Radio;
import team030.constants.Order;
import team030.navigation.BasicNav;
import team030.navigation.SafeBug;
import team030.util.Helper;

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
            case SupportTanks:
                supportTanks();
                break;
            case Swarm:
                swarm();
                break;
        }
    }

    private static void swarm() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        SafeBug.setDestination(enemyHqLocation);
        boolean isCoreReady = rc.isCoreReady();
        boolean isWeaponReady = rc.isWeaponReady();

        //--Find the enemies that can attack me
        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
        RobotInfo[] robotsCanAttackMe = Helper.getRobotsCanAttackLocation(enemiesInSensorRange, currentLocation);
        int canAttackMeCount = robotsCanAttackMe.length;

        //--If more than two enemies can attack me, move away
        if (canAttackMeCount > 2) {
            if (!isCoreReady) {
                return;
            }

            Direction away = Helper.getDirectionAwayFrom(robotsCanAttackMe, currentLocation);
            if (away == Direction.NONE) {
                //--Should we try to attack here since we can't move?
                return;
            }

            Direction navigableAway = BasicNav.getNavigableDirectionClosestTo(away);
            if (navigableAway == Direction.NONE) {
                //--Should we try to attack here since we can't move?
                return;
            }

            rc.move(navigableAway);
            return;
        }

        //--If two enemies can attack me,
        //    Run away if I cannot shoot either of them
        //    Run away if I have no friends that can shoot one of them
        if (canAttackMeCount == 2) {
            boolean[] iCanShoot = new boolean[2];
            int myRange = RobotType.SOLDIER.attackRadiusSquared;
            iCanShoot[0] = myRange >= currentLocation.distanceSquaredTo(robotsCanAttackMe[0].location);
            iCanShoot[1] = myRange >= currentLocation.distanceSquaredTo(robotsCanAttackMe[1].location);
            if (!iCanShoot[0]
                    && !iCanShoot[1]) {
                return;
            }

            boolean friendCanHelp = false;
            RobotInfo[] friendliesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, myTeam);
            int friendlyCount = friendliesInSensorRange.length;
            for (int i = 0; i < friendlyCount; i++) {
                int attackRadiusSqured = friendliesInSensorRange[i].type.attackRadiusSquared;
                MapLocation friendlyLocation = friendliesInSensorRange[i].location;
                if (attackRadiusSqured >= friendlyLocation.distanceSquaredTo(robotsCanAttackMe[0].location)
                        || attackRadiusSqured >= friendlyLocation.distanceSquaredTo(robotsCanAttackMe[1].location)) {
                    friendCanHelp = true;
                    break;
                }
            }

            //--We can shoot one of the enemies, but we a need a friend to help
            if (friendCanHelp) {
                //--Attack the enemy with the lowest HP
                if (!isWeaponReady) {
                    return;
                }

                if (iCanShoot[0]
                        && iCanShoot[1]) {
                    int indexToShoot = robotsCanAttackMe[0].health < robotsCanAttackMe[1].health ? 0 : 1;
                    rc.attackLocation(robotsCanAttackMe[indexToShoot].location);
                }
                else if (iCanShoot[0]) {
                    rc.attackLocation(robotsCanAttackMe[0].location);
                }
                else {
                    rc.attackLocation(robotsCanAttackMe[1].location);
                }
            }
            else {
                //--No friend can help. time to go away.
                if (!isCoreReady) {
                    return;
                }

                Direction away = Helper.getDirectionAwayFrom(robotsCanAttackMe, currentLocation);
                if (away == Direction.NONE) {
                    //--Should we try to attack here since we can't move?
                    return;
                }

                Direction navigableAway = BasicNav.getNavigableDirectionClosestTo(away);
                if (navigableAway == Direction.NONE) {
                    //--Should we try to attack here since we can't move?
                    return;
                }

                rc.move(navigableAway);
            }

            return;
        }

        //--If one enemy can attack me, engage if I have more HP?
        //    Run away if I cannot shoot it
        if (canAttackMeCount == 1) {
            MapLocation enemyLocation = robotsCanAttackMe[0].location;
            if (currentLocation.distanceSquaredTo(enemyLocation) <= RobotType.SOLDIER.attackRadiusSquared) {
                if (isWeaponReady) {
                    rc.attackLocation(enemyLocation);
                }
            }
            else {
                if (isCoreReady) {
                    Direction away = BasicNav.getNavigableDirectionClosestTo(enemyLocation.directionTo(currentLocation));
                    if (away != Direction.NONE) {
                        rc.move(away);
                    }
                }
            }

            return;
        }

        //--No enemies are nearby that can attack me
        //--Go to destination (if it is safe)
        if (!isCoreReady) {
            return;
        }

        RobotType[] typesToIgnore = new RobotType[] {RobotType.BEAVER, RobotType.MINER};
        Direction direction = SafeBug.getDirection(currentLocation, null, enemiesInSensorRange, typesToIgnore);
        if (direction == Direction.NONE) {
            return;
        }

        //--Check if the direction will be safe
        MapLocation next = currentLocation.add(direction);
        RobotInfo[] robotsCanAttackDestination = Helper.getRobotsCanAttackLocation(enemiesInSensorRange, next);
        int canAttackDestinationCount = robotsCanAttackDestination.length;
        if (canAttackDestinationCount > 1) {
            return;
        }

        //--Did the safe bug already check for this?
        if (canAttackDestinationCount == 1) {
            RobotType enemyType = robotsCanAttackDestination[0].type;
            if (enemyType != RobotType.MINER
                    && enemyType != RobotType.BEAVER) {
                return;
            }
        }

        rc.move(direction);
    }

    private static void supportTanks() throws GameActionException {
        //--Go to the structure we will attack
        //--Use safe nav so we don't get blown up by a tower

        MapLocation currentLocation = rc.getLocation();

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
        int enemyCount = enemiesInAttackRange.length;
        if (enemyCount > 0) {
            int closestEnemy = Helper.getIndexOfClosestRobot(enemiesInAttackRange, currentLocation);
            if (rc.isWeaponReady()) {
                rc.attackLocation(enemiesInAttackRange[closestEnemy].location);
                return;
            }
        }

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation structureToAttack = null;
        SafeBug.setDestination(structureToAttack);

        Direction d = SafeBug.getDirection(currentLocation);
        if (d == Direction.NONE) {
            return;
        }

        rc.move(d);
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
}
