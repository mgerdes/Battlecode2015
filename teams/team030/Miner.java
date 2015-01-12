package team030;

import battlecode.common.*;
import team030.navigation.Bug;
import team030.util.ChannelList;

public class Miner {
    private static RobotController rc;
    private static MapLocation myHqLocation;

    //--where to go when we run out of ore
    private static MapLocation defaultLocation;
    private static MapLocation enemyHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;
        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        Direction toEnemyHq = myHqLocation.directionTo(enemyHqLocation);
        defaultLocation = getDefaultLocation(toEnemyHq);

        Bug.init(rcC);
        SupplySharing.init(rcC);

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

        if (!rc.isCoreReady()) {
            return;
        }

        int currentMinerRadius = rc.readBroadcast(ChannelList.MINER_RADIUS_FROM_HQ);
        MapLocation currentLocation = rc.getLocation();
        int currentDistanceFromHq = (int) Math.sqrt(currentLocation.distanceSquaredTo(myHqLocation));
        if (currentDistanceFromHq > currentMinerRadius) {
            rc.broadcast(ChannelList.MINER_RADIUS_FROM_HQ, currentDistanceFromHq);
        }

        if (rc.senseOre(currentLocation) > 0) {
            rc.mine();
        }
        else {
            Direction direction = findDirectionClosestToHqWithOre(currentLocation);
            if (direction == null) {
                Bug.setDestination(defaultLocation);
                direction = Bug.getDirection(currentLocation);
            }

            rc.move(direction);
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

    private static Direction findDirectionClosestToHqWithOre(MapLocation currentLocation) {
        int directionToHq = Helper.getInt(currentLocation.directionTo(myHqLocation));
        int[] directions = new int[]{0, -1, 1, -2, 2, -3, 3, -4};
        for (int n : directions) {
            Direction direction = Helper.getDirection(directionToHq + n);
            MapLocation nextLocation = currentLocation.add(direction);
            if (rc.senseOre(nextLocation) > 0
                    && rc.canMove(direction)) {
                return direction;
            }
        }

        return null;
    }
}
