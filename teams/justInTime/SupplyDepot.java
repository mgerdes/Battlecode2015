package justInTime;

import battlecode.common.RobotController;

public class SupplyDepot {
    private static RobotController rc;

    public static void run(RobotController rcC) {
        rc = rcC;

        Communication.init(rcC);

        loop();
    }

    private static void loop() {
        while (true) {
            try {
                Communication.iAmASupplyTower();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}
