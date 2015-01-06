package bytecodeTesting;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class RobotPlayer {

    public static void run(RobotController rc) {
        Clock.getBytecodeNum(); //--Consumes 2 bytecodes
        Clock.getRoundNum(); //--Consumes 2 bytecodes
        Clock.getBytecodesLeft(); //--Consumes 2 bytecodes
        
        int b0 = Clock.getBytecodeNum();
        rc.setIndicatorString(0, "" + b0);

        while (true) {
        }
    }
}
