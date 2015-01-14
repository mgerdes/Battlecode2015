package underdog;

import battlecode.common.*;
import underdog.navigation.SafeBug;
import underdog.util.Helper;

public class Tank {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static MapLocation myHqLocation;

    private static int MIN_SUPPLY_LEVEL_BEFORE_LEAVING = 1500;
    private static int MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES = 36;

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
        SupplySharing.shareOnlyWithType(RobotType.TANK);
        MapLocation currentLocation = rc.getLocation();
        MapLocation destination = Helper.getWaypoint(.6, myHqLocation, enemyHqLocation);

        //--Don't leave home without supplies
        if (rc.getSupplyLevel() < MIN_SUPPLY_LEVEL_BEFORE_LEAVING
                && currentLocation.distanceSquaredTo(myHqLocation) <= MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES) {
            SafeBug.setDestination(myHqLocation);
        }
        else {
            SafeBug.setDestination(destination);
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                Direction direction = SafeBug.getDirection(currentLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }
}
