package justInTime;

import battlecode.common.*;
import justInTime.constants.ChannelList;
import justInTime.constants.Order;
import justInTime.navigation.SafeBug;
import justInTime.util.Debug;
import justInTime.util.Helper;

public class Launcher {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;
    private static Team myTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        SafeBug.init(rcC);
        SupplySharing.init(rcC);
        Communication.init(rcC);
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
        MapLocation currentLocation = rc.getLocation();
        attackEnemies(currentLocation);

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation rallyPoint = Communication.readMapLocationFromChannel(ChannelList.RALLY_POINT);
        SafeBug.setDestination(rallyPoint);
        Direction direction = SafeBug.getDirection(currentLocation);
        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }

    private static void attackEnemyStructure() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        attackEnemies(currentLocation);

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation structureToAttack = Communication.readMapLocationFromChannel(ChannelList.STRUCTURE_TO_ATTACK);
        SafeBug.setDestination(structureToAttack);

        Direction direction = SafeBug.getDirection(currentLocation, structureToAttack);
        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }

    private static void attackEnemies(MapLocation currentLocation) throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.MISSILE.sensorRadiusSquared, enemyTeam);
        MapLocation locationToAttack = null;
        int smallestDistance = 10000000;
        //--Need to check if we are near our own teammates!
        for (RobotInfo robot : enemies) {
            int distance = robot.location.distanceSquaredTo(currentLocation);
            if (distance < smallestDistance
                    && distance <= 9) {
                smallestDistance = distance;
                locationToAttack = robot.location;
            }
        }

        if (locationToAttack != null) {
            Direction attackDirection = currentLocation.directionTo(locationToAttack);
            if (rc.canLaunch(attackDirection)) {
                rc.launchMissile(attackDirection);
                if (rc.isCoreReady()) {
                    Direction awayFromMissile = attackDirection.opposite();
                    if (tryMove(awayFromMissile)) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean tryMove(Direction direction) throws GameActionException {
        if (rc.canMove(direction)) {
            rc.move(direction);
            return true;
        }

        direction = direction.rotateLeft();
        if (rc.canMove(direction)) {
            rc.move(direction);
            return true;
        }

        direction = direction.rotateRight().rotateRight();
        if (rc.canMove(direction)) {
            rc.move(direction);
            return true;
        }

        return false;
    }
}
