package boringMiners;

import boringMiners.communication.Channel;
import boringMiners.communication.Radio;
import boringMiners.util.Debug;
import boringMiners.util.Helper;
import battlecode.common.*;
import boringMiners.navigation.Bug;

public class Miner {
    private static RobotController rc;
    private static MapLocation myHqLocation;

    //--where to go when we run out of ore
    private static MapLocation defaultLocation;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        Direction toEnemyHq = myHqLocation.directionTo(enemyHqLocation);
        defaultLocation = getDefaultLocation(toEnemyHq);

        Bug.init(rcC);
        SupplySharing.init(rcC);
        Radio.init(rcC);

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

        if (rc.getSupplyLevel() < 200) {
            Radio.iNeedSupply();
        }

        MapLocation currentLocation = rc.getLocation();
        updateMinerRadius(currentLocation);

        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, enemyTeam);
        if (enemiesInSensorRange.length > 0) {
            Radio.setDistressLocation(enemiesInSensorRange[0].location);
        }

        if (rc.isWeaponReady()) {
            RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.MINER.attackRadiusSquared, enemyTeam);
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
            }
        }

        if (rc.isCoreReady()) {
            RobotInfo[] teammatesThatAreVeryClose = rc.senseNearbyRobots(2, myTeam);
            int minersThatAreVeryClose = Helper.getRobotsOfType(teammatesThatAreVeryClose, RobotType.MINER);
            if (minersThatAreVeryClose > 2) {
                Direction direction = findDirectionMostAwayFromOurHqWithOre(currentLocation);
                if (direction == null) {
                    Bug.setDestination(defaultLocation);
                    direction = Bug.getDirection(currentLocation);
                }

                rc.move(direction);
                return;
            }
            else if (rc.senseOre(currentLocation) == 0) {
                Direction direction = findDirectionMostAwayFromEnemyHqWithOre(currentLocation);
                if (direction == null) {
                    Bug.setDestination(defaultLocation);
                    direction = Bug.getDirection(currentLocation);
                }

                rc.move(direction);
                return;
            }
            else {
                rc.mine();
            }
        }
    }

    private static Direction findDirectionMostAwayFromOurHqWithOre(MapLocation currentLocation) {
        int directionAwayFromOurHq = Helper.getInt(myHqLocation.directionTo(currentLocation));
        int[] directions = new int[]{0, -1, 1, -2, 2, -3, 3, -4};
        for (int n : directions) {
            Direction direction = Helper.getDirection(directionAwayFromOurHq + n);
            MapLocation nextLocation = currentLocation.add(direction);
            if (rc.senseOre(nextLocation) > 0
                    && rc.canMove(direction)) {
                return direction;
            }
        }

        return null;
    }

    private static void updateMinerRadius(MapLocation currentLocation) throws GameActionException {
        int currentMinerRadius = rc.readBroadcast(Channel.MINER_DISTANCE_SQUARED_TO_HQ);
        int currentDistanceFromHq = currentLocation.distanceSquaredTo(myHqLocation);
        if (currentDistanceFromHq > currentMinerRadius) {
            rc.broadcast(Channel.MINER_DISTANCE_SQUARED_TO_HQ, currentDistanceFromHq);
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