package Navigation;

import battlecode.common.*;

//--This is a dumb soldier used to test the bug nav
public class Soldier {
    private static RobotController rc;
    private static final MapLocation TEST_DESTINATION = new MapLocation(19, 20);

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void run() {
        Bug.init(TEST_DESTINATION, rc);
        while (true) {
            try {
                if (rc.isActive()) {
                    Direction d = Bug.getDirection();
                    rc.move(d);
                }

                rc.yield();
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
