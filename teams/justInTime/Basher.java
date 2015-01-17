package justInTime;

import battlecode.common.*;
import justInTime.navigation.SafeBug;
import justInTime.util.Helper;

public class Basher {
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
        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        MapLocation destination = Helper.getWaypoint(0.7, myHqLocation, enemyHqLocation);

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.BASHER.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length > 0) {
            destination = enemiesInAttackRange[0].location;
        }
        if (!currentLocation.equals(destination)) {
            SafeBug.setDestination(destination);
            Direction direction = SafeBug.getDirection(rc.getLocation());
            rc.move(direction);
        }
    }
}
