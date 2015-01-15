package framework;

import battlecode.common.*;
import framework.navigation.SafeBug;
import framework.util.Helper;

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
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        MapLocation destination = Helper.getWaypoint(0.7, myHqLocation, enemyHqLocation);
        if (!currentLocation.equals(destination)) {
            SafeBug.setDestination(destination);
            Direction direction = SafeBug.getDirection(rc.getLocation());
            rc.move(direction);
        }
    }
}
