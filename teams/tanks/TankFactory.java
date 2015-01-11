package tanks;

import battlecode.common.*;

public class TankFactory {
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
            spawn(RobotType.TANK);
        }
    }

    private static void spawn(RobotType type) throws GameActionException {
        if (rc.getTeamOre() < type.oreCost) {
            return;
        }

        int direction = 0;
        while (!rc.canSpawn(directions[direction], type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(directions[direction], type);
    }
}
