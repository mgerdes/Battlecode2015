package tanks;

import battlecode.common.*;

public class Helipad {
    private static RobotController rc;
    private static Direction[] directions = Direction.values();

    public static void run(RobotController rcC) {
        rc = rcC;
        loop();
    }

    private static void loop() {
        while (true) {
            try {
                rc.setIndicatorString(1, String.format("current ore: %f", rc.getTeamOre()));
                doYourThing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void doYourThing() throws GameActionException {
        if (rc.isCoreReady()) {
            spawnDrone();
        }
    }

    private static void spawnDrone() throws GameActionException {
        if (rc.getTeamOre() < RobotType.DRONE.oreCost) {
            return;
        }

        int direction = 0;
        while (!rc.canSpawn(directions[direction], RobotType.DRONE)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(directions[direction], RobotType.DRONE);
    }
}
