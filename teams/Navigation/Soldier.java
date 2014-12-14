package Navigation;

import battlecode.common.*;

//--This is a dumb soldier used to test the bug nav
public class Soldier {
    private RobotController rc;
    private Bug bugNav;
    private static final MapLocation TEST_DESTINATION = new MapLocation(19, 20);

    public Soldier(RobotController rc) {
        this.rc = rc;
    }

    public void run() {
        Bug bugNav = new Bug(TEST_DESTINATION, rc);
        while (true) {
            try {
                if (rc.isActive()) {
                    Direction d = bugNav.getDirection();
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
