package moneyMaker;

import battlecode.common.*;

public class Building {
    private static RobotController rc;

    private static final int MAX_MINER_COUNT = 20;
    private static final int MIN_DRONE_ROUND = 140;

    private static Direction[] directions = Direction.values();
    private static RobotType myType;
    private static Team myTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

        myType = rcC.getType();
        myTeam = rcC.getTeam();

        loop();
    }

    private static void loop() {
        while (true) {
            try {
                rc.setIndicatorString(1, String.format("current ore: %f", rc.getTeamOre()));
                doYourThing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void doYourThing() throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }
        
        RobotInfo[] allFriendlies = rc.senseNearbyRobots(1000000, myTeam);
        if (canSpawn(RobotType.MINER)) {
            int minerCount = Helper.getRobotsOfType(allFriendlies, RobotType.MINER);
            if (minerCount < MAX_MINER_COUNT) {
                spawn(RobotType.MINER);
                return;
            }
        }

        if (canSpawn(RobotType.DRONE)) {
            if (Clock.getRoundNum() > MIN_DRONE_ROUND) {
                spawn(RobotType.DRONE);
                return;
            }
        }
    }

    private static boolean canSpawn(RobotType type) {
        if (type.spawnSource != myType
            || rc.getTeamOre() < type.oreCost) {
            return false;
        }

        return true;
    }

    private static void spawn(RobotType type) throws GameActionException {
        int direction = 0;
        while (!rc.canSpawn(directions[direction], type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(directions[direction], type);
    }
}
