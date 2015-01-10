package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.Strategy;
import soldiersAgainstBashers.StrategyEnum;

public class Computer {
    private static RobotController rc;
    private static Team myTeam;
    private static Team enemyTeam;

    public static void init(RobotController rcC) {
        rc = rcC;
        Strategy.init(rcC);

        myTeam = rcC.getTeam();
        enemyTeam = myTeam.opponent();
        loop();
    }

    static void loop() {
        while (true) {
            try {
                doYourThing();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    static void doYourThing() throws GameActionException {
        RobotInfo[] friendlies = rc.senseNearbyRobots(Integer.MAX_VALUE, myTeam);
        boolean oneIsADrone = false;
        for (RobotInfo friend : friendlies) {
            if (friend.type == RobotType.DRONE) {
                oneIsADrone = true;
                break;
            }
        }

        if (oneIsADrone) {
            Strategy.set(StrategyEnum.Expand);
        } else {
            Strategy.set(StrategyEnum.Circle);
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(Integer.MAX_VALUE, enemyTeam);
    }
}
