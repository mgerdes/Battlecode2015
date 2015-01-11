package droneRush;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Drone {
    private static RobotController rc;

    public static void run(RobotController rcC) {
        rc = rcC;
        Bug.init(rcC);
        Bug.setNewDestination(rcC.senseEnemyHQLocation());
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
        Direction direction = Bug.getDirection(currentLocation);
        rc.move(direction);
    }
}
