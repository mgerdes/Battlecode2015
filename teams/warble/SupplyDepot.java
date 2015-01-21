package warble;

import battlecode.common.RobotController;
import warble.communication.Radio;

public class SupplyDepot {
    private static RobotController rc;

    public static void run(RobotController rcC) {
        rc = rcC;

        Radio.init(rcC);

        loop();
    }

    private static void loop() {
        while (true) {
            try {
                Radio.iAmASupplyTower();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}
