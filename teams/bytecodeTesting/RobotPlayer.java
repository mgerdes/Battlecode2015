package bytecodeTesting;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import navigation.Soldier;

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
