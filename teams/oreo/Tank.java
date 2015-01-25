package oreo;

import battlecode.common.*;
import oreo.communication.Channel;
import oreo.communication.HqOrders;
import oreo.communication.Radio;
import oreo.constants.Order;
import oreo.navigation.Bfs;
import oreo.navigation.SafeBug;
import oreo.util.Helper;

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

        if (rc.getSupplyLevel() < 600
                && currentLocation.distanceSquaredTo(myHq) < MAXIMUM_DISTANCE_SQUARED_TO_GO_TO_HQ_FOR_SUPPLY) {
            SafeBug.setDestination(myHq);
            Direction direction = SafeBug.getDirection(currentLocation);
            if (rc.isCoreReady()
                    && direction != Direction.NONE) {
                rc.move(direction);
                return;
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
            if (Radio.isBfsReady(poiToAttack)) {
                Direction direction = Bfs.getDirection(currentLocation, poiToAttack);

                if (rc.canMove(direction)) {
                    rc.move(direction);
                    return;
                }

                if (rc.canMove(direction.rotateLeft())) {
                    rc.move(direction.rotateLeft());
                    return;
                }

                if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                    return;
                }
            }
            else {
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
