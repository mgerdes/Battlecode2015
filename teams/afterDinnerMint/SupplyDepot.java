package afterDinnerMint;

import battlecode.common.RobotController;

public class SupplyDepot {
    private static RobotController rc;

    public static void run(RobotController rcC) {
        rc = rcC;

        loop();
    }

    private static void loop() {
        while (true) {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}
