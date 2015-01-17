package justInTime;

import battlecode.common.*;
import justInTime.navigation.SafeBug;
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
        MapLocation currentLocation = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.MISSILE.sensorRadiusSquared, enemyTeam);
        MapLocation locationToAttack = null;
        int smallestDistance = 10000000;
        for (RobotInfo robot : enemies) {
            //--TODO: does this number make sense?
            int distance = robot.location.distanceSquaredTo(currentLocation);
            if (distance < smallestDistance && distance <= 9) {
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

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation destination = Helper.getWaypoint(0.75, myHqLocation, enemyHqLocation);
        SafeBug.setDestination(destination);
        Direction direction = SafeBug.getDirection(currentLocation);
        if (direction != Direction.NONE) {
            rc.move(SafeBug.getDirection(currentLocation));
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