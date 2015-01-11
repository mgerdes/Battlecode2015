package superCircle;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Miner {
    private static RobotController rc;
    private static Direction[] directions = Direction.values();
    private static Random random;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;
        random = new Random(rcC.getID());
        myHqLocation = rc.senseHQLocation();

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

        if (rc.senseOre(rc.getLocation()) > 0) {
            rc.mine();
        }
        else {
            //--Only move away from HQ is the ore isn't there
            Direction direction = findBestDirection();
            if (direction != null) {
                rc.move(direction);
            }
        }
    }

    private static Direction findBestDirection() {
        MapLocation currentLocation = rc.getLocation();
        int directionToHq = Helper.getInt(currentLocation.directionTo(myHqLocation));
        int[] directions = new int[] {0, -1, 1, -2, 2, -3, 3, -4};
        for (int n : directions) {
            Direction direction = Helper.getDirection(directionToHq + n);
            if (rc.senseOre(currentLocation.add(direction)) > 0
                    && rc.canMove(direction)) {
                return direction;
            }
        }

        return null;
    }
}
