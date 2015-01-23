package tankers;

import battlecode.common.*;
import tankers.communication.Channel;
import tankers.communication.HqOrders;
import tankers.communication.Radio;
import tankers.constants.Order;
import tankers.navigation.SafeBug;
import tankers.util.Helper;

public class Launcher {
    private static RobotController rc;

    private static Team enemyTeam;
    private static Team myTeam;
    private static MapLocation myHq;

    private static final int[] directions = new int[]{0, -1, 1, -2, 2};
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
        SupplySharing.shareOnlyWithType(RobotType.LAUNCHER);

        Order order = HqOrders.getOrder(RobotType.LAUNCHER);
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

        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.MISSILE.sensorRadiusSquared, enemyTeam);

        //--If there are no nearby enemies, move closer
        if (enemiesInSensorRange.length == 0) {
            MapLocation structureToAttack = Radio.readMapLocationFromChannel(Channel.STRUCTURE_TO_ATTACK);
            SafeBug.setDestination(structureToAttack);
            Direction direction = SafeBug.getDirection(currentLocation, structureToAttack);

            //--See if our missiles would be close enough to attack
            MapLocation missileLocation = currentLocation.add(direction);
            if (missileLocation.distanceSquaredTo(structureToAttack) <= RobotType.MISSILE.sensorRadiusSquared) {
                if (rc.canLaunch(direction)) {
                    rc.launchMissile(direction);
                    return;
                }
            }
            else if (rc.isCoreReady()
                    && direction != Direction.NONE) {
                rc.move(direction);
            }

            return;
        }

        //--If the enemies are too close, move away
        if (rc.isCoreReady()) {
            int enemiesInSensorCount = enemiesInSensorRange.length;
            for (int i = 0; i < enemiesInSensorCount; i++) {
                if (currentLocation.distanceSquaredTo(enemiesInSensorRange[i].location) <= 5) {
                    tryMove(enemiesInSensorRange[i].location.directionTo(currentLocation));
                    break;
                }
            }
        }

        //--Attack a non-missile enemy
        int nonMissileEnemyCount = Helper.getRobotsExcludingType(enemiesInSensorRange, RobotType.MISSILE);
        if (nonMissileEnemyCount != 0) {
            int index = 0;
            for (int i = 0; i < enemiesInSensorRange.length; i++) {
                if (enemiesInSensorRange[i].type != RobotType.MISSILE) {
                    index = i;
                    break;
                }
            }

            MapLocation locationToAttack = enemiesInSensorRange[index].location;
            Direction attackDirection = currentLocation.directionTo(locationToAttack);
            if (rc.canLaunch(attackDirection)) {
                rc.launchMissile(attackDirection);
            }
        }
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
