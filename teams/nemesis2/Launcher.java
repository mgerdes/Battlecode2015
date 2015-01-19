package nemesis2;

import battlecode.common.*;
import nemesis2.communication.Channel;
import nemesis2.communication.Radio;
import nemesis2.constants.Order;
import nemesis2.navigation.SafeBug;

public class Launcher {
    private static RobotController rc;

    private static Team enemyTeam;
    private static Team myTeam;
    private static int myId;

    private static final int MAXIMUM_DISTANCE_SQUARED_TO_GO_TO_HQ_FOR_SUPPLY = 100;
    private static MapLocation myHq;

    public static void run(RobotController rcC) {
        rc = rcC;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHq = rc.senseHQLocation();
        myId = rc.getID();

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
        SupplySharing.shareOnlyWithType(RobotType.LAUNCHER);

        Order order = MessageBoard.getOrder(RobotType.LAUNCHER);
        switch (order) {
            case Rally:
                rally();
                break;
            case AttackEnemyStructure:
                attackEnemyStructure();
                break;
        }
    }

    private static void rally() throws GameActionException {
        //--TODO: Add some logic to handle nearby enemies

        MapLocation currentLocation = rc.getLocation();

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation rallyPoint = Radio.readMapLocationFromChannel(Channel.RALLY_POINT);
        SafeBug.setDestination(rallyPoint);
        Direction direction = SafeBug.getDirection(currentLocation);
        if (direction != Direction.NONE) {
            rc.move(direction);
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

        //--There are enemies nearby. Let's attack
        MapLocation locationToAttack = enemiesInSensorRange[0].location;
        Direction attackDirection = currentLocation.directionTo(locationToAttack);
        if (rc.canLaunch(attackDirection)) {
            rc.launchMissile(attackDirection);
        }
    }
}
