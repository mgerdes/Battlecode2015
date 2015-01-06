package navigation;

import battlecode.common.*;

//--This is a dumb beaver used to test the bug nav
public class Beaver {
    private static RobotController rc;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void run() {
        try {
            Bug.init(rc.senseEnemyHQLocation(), rc);
            while (true) {
                if (rc.isCoreReady()) {
                    rc.move(Bug.getDirection());
                }

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
    }
}
