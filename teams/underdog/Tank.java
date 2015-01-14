package underdog;

import battlecode.common.*;
import underdog.constants.ChannelList;
import underdog.constants.Order;
import underdog.navigation.SafeBug;

public class Tank {
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
        SupplySharing.shareOnlyWithType(RobotType.TANK);
        if (rc.readBroadcast(ChannelList.TANK_FORTIFY) == Order.YES) {
            fortify();
        }
        else if (rc.readBroadcast(ChannelList.TANK_ATTACK) == Order.YES) {
            attack();
        }
    }

    private static void attack() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        MapLocation attackLocation = Communication.getMapLocation(ChannelList.STRUCTURE_TO_ATTACK);

        SafeBug.setDestination(attackLocation);

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                Direction direction = SafeBug.getDirection(currentLocation, attackLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }

    private static void fortify() throws GameActionException {
        //--We have to get the location every turn or someone else will grab it
        MapLocation myPositionInTheFormation =
                Communication.getUnclaimedLocation(ChannelList.TANK_FORMATION_FIRST_CHANNEL);

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
        if (!currentLocation.equals(myPositionInTheFormation)) {
            SafeBug.setDestination(myPositionInTheFormation);
            Direction direction = SafeBug.getDirection(rc.getLocation());
            rc.move(direction);
        }
    }
}
