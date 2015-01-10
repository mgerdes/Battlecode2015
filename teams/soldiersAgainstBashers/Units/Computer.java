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
        int fighterCount = 0;
        for (RobotInfo friend : friendlies) {
            if (friend.type == RobotType.DRONE) {
                oneIsADrone = true;
            }

            if (friend.type == RobotType.SOLDIER
                    || friend.type == RobotType.BASHER) {
                fighterCount++;
            }
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(Integer.MAX_VALUE, enemyTeam);
        int enemyWithoutSupply = 0;
        for (RobotInfo enemy : enemies) {
            if (enemy.supplyLevel == 0) {
                enemyWithoutSupply++;
            }
        }

        if (!oneIsADrone) {
            Strategy.set(StrategyEnum.Circle);
            return;
        }

        if (fighterCount > 20
                && enemyWithoutSupply > 5) {
            Strategy.set(StrategyEnum.AttackUnits);
        }
        else {
            Strategy.set(StrategyEnum.Expand);
        }
    }
}
