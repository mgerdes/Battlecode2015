package superCircle;

import battlecode.common.*;

public class Miner {
    private static RobotController rc;
    private static MapLocation myHqLocation;
    private static MapLocation enemyHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;
        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();

        Bug.init(rcC);

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
                Bug.setDestination(enemyHqLocation);
                direction = Bug.getSafeDirection(currentLocation);
            }

            rc.move(direction);
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
