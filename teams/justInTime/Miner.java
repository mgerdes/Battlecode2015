package justInTime;

import justInTime.constants.ChannelList;
import justInTime.util.Debug;
import justInTime.util.Helper;
import battlecode.common.*;
import justInTime.navigation.Bug;

public class Miner {
    private static RobotController rc;
    private static MapLocation myHqLocation;

    //--where to go when we run out of ore
    private static MapLocation defaultLocation;
    private static MapLocation enemyHqLocation;
    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        enemyTeam = rc.getTeam().opponent();
        Direction toEnemyHq = myHqLocation.directionTo(enemyHqLocation);
        defaultLocation = getDefaultLocation(toEnemyHq);

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

        MapLocation currentLocation = rc.getLocation();
        updateMinerRadius(currentLocation);

        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, enemyTeam);
        if (enemiesInSensorRange.length > 0) {
            Communication.setDistressLocation(enemiesInSensorRange[0].location);
        }

        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.MINER.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }

        if (rc.isCoreReady()) {
            if (rc.senseOre(currentLocation) > 0) {
                rc.mine();
            }
            else {
                Direction direction = findDirectionMostAwayFromEnemyHqWithOre(currentLocation);
                if (direction == null) {
                    Bug.setDestination(defaultLocation);
                    direction = Bug.getDirection(currentLocation);
                }

                rc.move(direction);
            }
        }
    }

    private static void updateMinerRadius(MapLocation currentLocation) throws GameActionException {
        int currentMinerRadius = rc.readBroadcast(ChannelList.MINER_DISTANCE_SQUARED_TO_HQ);
        int currentDistanceFromHq = currentLocation.distanceSquaredTo(myHqLocation);
        if (currentDistanceFromHq > currentMinerRadius) {
            rc.broadcast(ChannelList.MINER_DISTANCE_SQUARED_TO_HQ, currentDistanceFromHq);
            Debug.setString(1, "radius is " + currentDistanceFromHq, rc);
        }
    }

    private static MapLocation getDefaultLocation(Direction defaultDirection) {
        //--Miners will be randomly assigned one of three default locations
        int selector = rc.getID() % 3;
        switch (selector) {
            case 0:
                return myHqLocation.add(defaultDirection.rotateLeft(), 150);
            case 1:
                return myHqLocation.add(defaultDirection.rotateRight(), 150);
            default:
                return enemyHqLocation;
        }
    }

    private static Direction findDirectionMostAwayFromEnemyHqWithOre(MapLocation currentLocation) {
        int directionAwayFromHq = Helper.getInt(enemyHqLocation.directionTo(myHqLocation));
        int[] directions = new int[]{0, -1, 1, -2, 2, -3, 3, -4};
        for (int n : directions) {
            Direction direction = Helper.getDirection(directionAwayFromHq + n);
            MapLocation nextLocation = currentLocation.add(direction);
            if (rc.senseOre(nextLocation) > 0
                    && rc.canMove(direction)) {
                return direction;
            }
        }

        return null;
    }
}
