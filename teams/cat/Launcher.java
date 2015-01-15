package cat;

import battlecode.common.*;
import cat.constants.ChannelList;
import cat.navigation.SafeBug;
import cat.util.Helper;

public class Launcher {
    private static RobotController rc;

    private static final int MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES = 25;
    private static final int ROBOT_NOT_SET = -1;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;
    private static Team myTeam;
    private static int robotThatNeedsSupplyId;

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
        if (!destination.equals(currentLocation)) {
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
