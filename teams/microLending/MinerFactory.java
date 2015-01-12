package microLending;

import battlecode.common.*;

public class MinerFactory {
    private static RobotController rc;

    private static final int MAX_MINER_COUNT = 26;

    private static Team myTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

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
        RobotInfo[] allFriendlies = rc.senseNearbyRobots(1000000, myTeam);

        if (!rc.isCoreReady()) {
            return;
        }

        if (rc.getTeamOre() >= RobotType.MINER.oreCost) {
            int minerCount = Helper.getRobotsOfType(allFriendlies, RobotType.MINER);
            if (minerCount < MAX_MINER_COUNT) {
                spawnMiner();
            }
        }
    }

    private static void spawnMiner() throws GameActionException {
        int direction = 0;
        while (!rc.canSpawn(Helper.getDirection(direction), RobotType.MINER)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(Helper.getDirection(direction), RobotType.MINER);
    }
}
