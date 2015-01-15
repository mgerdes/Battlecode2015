package framework;

import battlecode.common.*;
import battlecode.world.Robot;
import framework.constants.ChannelList;
import framework.constants.Order;
import framework.navigation.Bug;
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
        Bug.init(rcC);
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
        SupplySharing.share();

        if (rc.readBroadcast(ChannelList.SOLDIER_ATTACK_ENEMY_MINERS) == Order.YES) {
            attackEnemyMiners();
        }
        else if (rc.readBroadcast(ChannelList.SOLDIER_DEFEND_MINERS) == Order.YES) {
            defendMiners();
        }
        else {
            goToWayPoint();
        }
    }

    private static void defendMiners() throws GameActionException {
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
                return;
            }
        }

        MapLocation currentLocation = rc.getLocation();
        if (rc.isCoreReady()) {
            MapLocation distressLocation = Communication.getDistressLocation();
            if (distressLocation != null) {
                Bug.setDestination(distressLocation);
                rc.move(Bug.getDirection(currentLocation));
                return;
            }

            //--This code chases down enemies..
            //--It might be nice for defending, but the defender should probably return to
            //  its swarm after it gets a certain distance away.
//            RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
//            if (enemiesInSensorRange.length > 0) {
//                Bug.setDestination(enemiesInSensorRange[0].location);
//                rc.move(Bug.getDirection(currentLocation));
//                return;
//            }

            MapLocation destination = Helper.getWaypoint(0.7, myHqLocation, enemyHqLocation);
            if (!currentLocation.equals(destination)) {
                Bug.setDestination(destination);
                Direction direction = Bug.getDirection(rc.getLocation());
                rc.move(direction);
            }
        }
    }

    private static void attackEnemyMiners() throws GameActionException {
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
            for (RobotInfo robot : enemiesInAttackRange) {
                if (robot.type == RobotType.BEAVER
                        || robot.type == RobotType.MINER) {
                    rc.attackLocation(robot.location);
                    return;
                }
            }
        }

        SafeBug.setDestination(enemyHqLocation);

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
        RobotType[] enemiesToIgnore = new RobotType[]{RobotType.BEAVER, RobotType.MINER};
        Direction direction = SafeBug.getDirection(currentLocation, null, enemies, enemiesToIgnore);
        rc.move(direction);
    }

    private static void goToWayPoint() throws GameActionException {
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
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
