package team030;

import battlecode.common.*;
import team030.communication.Channel;
import team030.communication.HqOrders;
import team030.communication.Radio;
import team030.constants.Order;
import team030.navigation.BasicNav;
import team030.navigation.Bfs;
import team030.navigation.SafeBug;
import team030.util.Debug;
import team030.util.Helper;

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

        Bfs.init(rcC);
        SafeBug.init(rcC);
        BasicNav.init(rcC);
        PathBuilder.init(rcC);
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
                    return;
                }
            }
            else {
                Radio.iNeedSupply();
            }
        }

        //--If there are no nearby enemies, move closer to structure
        if (!rc.isCoreReady()) {
            return;
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, enemyTeam);
        int enemyCount = enemiesInAttackRange.length;
        if (enemyCount == 0) {
            int poiToAttack = rc.readBroadcast(Channel.POI_TO_ATTACK);
            Direction bfsDirection = Bfs.getDirection(currentLocation, poiToAttack);
            if (bfsDirection != Direction.NONE) {
                Direction direction = Bfs.getDirection(currentLocation, poiToAttack);
                Debug.setString(1, String.format("bfs direction is %s", direction), rc);

                direction = BasicNav.getNavigableDirectionClosestTo(direction);
                if (direction != Direction.NONE) {
                    rc.move(direction);
                    return;
                }
            }

            MapLocation structureToAttack = Radio.readMapLocationFromChannel(Channel.POI_ABSOLUTE[poiToAttack]);
            SafeBug.setDestination(structureToAttack);

            RobotInfo[] friendles = rc.senseNearbyRobots(RobotType.TANK.sensorRadiusSquared, myTeam);
            int nearbyTanks = Helper.getRobotsOfType(friendles, RobotType.TANK);
            if (nearbyTanks < 5
                    && currentLocation.distanceSquaredTo(structureToAttack) <= 36) {
                return;
            }

            Direction direction = SafeBug.getDirection(currentLocation, structureToAttack);
            if (rc.isCoreReady()
                    && direction != Direction.NONE) {
                rc.move(direction);
                return;
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
