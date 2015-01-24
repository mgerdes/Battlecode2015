package droneTest;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droneTest.communication.Radio;

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
                Radio.iAmABuilding(RobotType.SUPPLYDEPOT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}