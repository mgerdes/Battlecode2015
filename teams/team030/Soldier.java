package team030;

import battlecode.common.*;
import team030.communication.HqOrders;
import team030.communication.Radio;
import team030.constants.Order;
import team030.navigation.BasicNav;
import team030.navigation.SafeBug;
import team030.util.Debug;
import team030.util.Helper;

public class Soldier {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        BasicNav.init(rcC);
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
            case Swarm:
                swarm();
                break;
        }
    }

    private static void swarm() throws GameActionException {
        if (rc.getSupplyLevel() < 350) {
            Radio.iNeedSupply();
        }

        MapLocation currentLocation = rc.getLocation();
        SafeBug.setDestination(enemyHqLocation);
        boolean isCoreReady = rc.isCoreReady();
        boolean isWeaponReady = rc.isWeaponReady();

        //--Find the enemies that can attack me
        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
        RobotInfo[] robotsCanAttackMe = Helper.getRobotsCanAttackLocation(enemiesInSensorRange, currentLocation);
        RobotInfo[] friendlySoldiersInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, myTeam);
        int canAttackMeCount = robotsCanAttackMe.length;

        //--If there is a commander, run away unless 2 friends can attack with me
        if (Helper.getRobotsOfType(enemiesInSensorRange, RobotType.COMMANDER) > 0) {
            MapLocation commanderLocation = null;
            for (int i = 0; i < enemiesInSensorRange.length; i++) {
                if (enemiesInSensorRange[i].type == RobotType.COMMANDER) {
                    commanderLocation = enemiesInSensorRange[i].location;
                }
            }

            if (Helper.getRobotCountCanAttackLocation(friendlySoldiersInSensorRange, commanderLocation) < 2) {
                Direction away = commanderLocation.directionTo(currentLocation);
                Direction navigableAway = BasicNav.getNavigableDirectionClosestTo(away);
                if (navigableAway == Direction.NONE) {
                    //--Should we try to attack here since we can't move?
                    return;
                }

                rc.move(navigableAway);
                return;
            }
        }

        //--If more than two enemies can attack me, move away
        if (canAttackMeCount > 2) {
            Debug.setString(1, "over two enemies can attack me", rc);
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
            Debug.setString(1, "2 enemies can attack me", rc);
            boolean[] iCanShoot = new boolean[2];
            int myRange = RobotType.SOLDIER.attackRadiusSquared;
            iCanShoot[0] = myRange >= currentLocation.distanceSquaredTo(robotsCanAttackMe[0].location);
            iCanShoot[1] = myRange >= currentLocation.distanceSquaredTo(robotsCanAttackMe[1].location);
            if (!iCanShoot[0]
                    && !iCanShoot[1]) {
                return;
            }

            boolean friendCanHelp = false;
            int friendlyCount = friendlySoldiersInSensorRange.length;
            for (int i = 0; i < friendlyCount; i++) {
                int attackRadiusSqured = friendlySoldiersInSensorRange[i].type.attackRadiusSquared;
                MapLocation friendlyLocation = friendlySoldiersInSensorRange[i].location;
                if (attackRadiusSqured >= friendlyLocation.distanceSquaredTo(robotsCanAttackMe[0].location)
                        || attackRadiusSqured >= friendlyLocation.distanceSquaredTo(robotsCanAttackMe[1].location)) {
                    friendCanHelp = true;
                    break;
                }
            }

            //--We can shoot one of the enemies, but we a need a friend to help
            if (friendCanHelp) {
                Debug.setString(1, "2 enemies can attack me and friend can help", rc);
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
}

