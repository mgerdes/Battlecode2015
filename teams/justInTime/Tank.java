package justInTime;

import battlecode.common.*;
import justInTime.constants.ChannelList;
import justInTime.constants.Order;
import justInTime.navigation.Bug;
import justInTime.navigation.SafeBug;
import justInTime.util.Debug;
import justInTime.util.Helper;

public class Tank {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static MapLocation myHqLocation;
    private static int MAX_DISTANCE_SQUARED_TO_GO_TO_HQ_FOR_SUPPLIES = 49;

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

        int initialBytecode = Clock.getBytecodeNum();
        Order order = MessageBoard.getOrder(rc.getType());
        Debug.setString(
                0,
                String.format("%s bytecodes to get order", Clock.getBytecodeNum() - initialBytecode),
                rc);

        switch (order) {
            case Rally:
                goToRallyPoint();
                break;
            case AttackEnemyMiners:
                attackEnemyMiners();
                break;
            case DefendMiners:
                defendMiners();
                break;
            case AttackEnemyStructure:
                attackEnemyStructure();
                break;
        }
    }

    private static void attackEnemyStructure() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        MapLocation attackLocation = Communication.readMapLocationFromChannel(ChannelList.STRUCTURE_TO_ATTACK);

        //--Don't leave home without supplies
        if (rc.getSupplyLevel() == 0
                && currentLocation.distanceSquaredTo(myHqLocation) <= MAX_DISTANCE_SQUARED_TO_GO_TO_HQ_FOR_SUPPLIES) {
            SafeBug.setDestination(myHqLocation);
        }
        else {
            SafeBug.setDestination(attackLocation);
        }

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

    private static void defendMiners() throws GameActionException {
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, enemyTeam);
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
//            RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.TANK.sensorRadiusSquared, enemyTeam);
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
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, enemyTeam);
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
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.TANK.sensorRadiusSquared, enemyTeam);
        RobotType[] enemiesToIgnore = new RobotType[]{RobotType.BEAVER, RobotType.MINER};
        Direction direction = SafeBug.getDirection(currentLocation, null, enemies, enemiesToIgnore);
        rc.move(direction);
    }

    private static void goToRallyPoint() throws GameActionException {
        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        MapLocation distressLocation = Communication.getDistressLocation();
        if (distressLocation != null) {
            SafeBug.setDestination(distressLocation);
        }
        else {
            MapLocation destination = Communication.readMapLocationFromChannel(ChannelList.RALLY_POINT);
            SafeBug.setDestination(destination);
        }

        Direction direction = SafeBug.getDirection(currentLocation);
        rc.move(direction);
    }
}

