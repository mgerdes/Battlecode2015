package tankers;

import battlecode.common.*;
import tankers.communication.Channel;
import tankers.communication.HqOrders;
import tankers.communication.Radio;
import tankers.constants.Order;
import tankers.navigation.SafeBug;
import tankers.util.Helper;

public class Tank {
    private static RobotController rc;

    private static Team enemyTeam;
    private static Team myTeam;
    private static MapLocation myHq;

    private static final int MAXIMUM_DISTANCE_SQUARED_TO_GO_TO_HQ_FOR_SUPPLY = 100;

    public static void run(RobotController rcC) {
        rc = rcC;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHq = rc.senseHQLocation();

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
        SupplySharing.shareOnlyWithType(RobotType.TANK);

        Order order = HqOrders.getOrder(RobotType.TANK);
        switch (order) {
            case AttackEnemyStructure:
                attackEnemyStructure();
                break;
        }
    }

    private static void attackEnemyStructure() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        if (rc.getSupplyLevel() < 600) {
            if (currentLocation.distanceSquaredTo(myHq) < MAXIMUM_DISTANCE_SQUARED_TO_GO_TO_HQ_FOR_SUPPLY) {
                SafeBug.setDestination(myHq);
                Direction direction = SafeBug.getDirection(currentLocation);
                if (rc.isCoreReady()
                        && direction != Direction.NONE) {
                    rc.move(direction);
                }

                return;
            }
            else {
                Radio.iNeedSupply();
            }
        }

        MapLocation structureToAttack = Radio.readMapLocationFromChannel(Channel.STRUCTURE_TO_ATTACK);

        //--If there are launchers nearby, it is priority
        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.TANK.sensorRadiusSquared, enemyTeam);
        int indexOfLauncher = Helper.getIndexOfClosestRobot(RobotType.LAUNCHER, enemiesInSensorRange, currentLocation);
        if (indexOfLauncher != -1) {
            MapLocation tankLocation = enemiesInSensorRange[indexOfLauncher].location;
            if (currentLocation.distanceSquaredTo(tankLocation) > RobotType.TANK.attackRadiusSquared) {
                if (!rc.isCoreReady()) {
                    return;
                }

                SafeBug.setDestination(tankLocation);
                Direction d = SafeBug.getDirection(currentLocation, structureToAttack);
                if (d != Direction.NONE) {
                    rc.move(d);
                }
            }
            else {
                if (rc.isWeaponReady()) {
                    rc.attackLocation(tankLocation);
                }
            }

            return;
        }

        //--If there are no nearby enemies, move closer to structure
        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, enemyTeam);
        int enemyCount = enemiesInAttackRange.length;
        if (enemyCount == 0) {
            SafeBug.setDestination(structureToAttack);
            Direction direction = SafeBug.getDirection(currentLocation, structureToAttack);
            if (rc.isCoreReady()
                    && direction != Direction.NONE) {
                rc.move(direction);
            }

            return;
        }

        //--There are enemies. Attack the closest one
        if (!rc.isWeaponReady()) {
            return;
        }

        int closestRobotIndex = Helper.getIndexOfClosestRobot(enemiesInAttackRange, currentLocation);
        rc.attackLocation(enemiesInAttackRange[closestRobotIndex].location);
    }
}
