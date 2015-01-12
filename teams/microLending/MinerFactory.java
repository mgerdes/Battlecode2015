package microLending;

import battlecode.common.*;
import microLending.util.ChannelList;
import microLending.util.Debug;
import microLending.util.Job;

public class MinerFactory {
    private static RobotController rc;

    private static final int MAX_MINER_COUNT = 26;
    private static final int MINER_WITH_NO_SUPPLY_TOP_THRESHOLD = 4;
    private static final int MINER_WITH_NO_SUPPLY_BOTTOM_THRESHOLD = 1;

    private static Team myTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

        myTeam = rcC.getTeam();

        Communication.init(rcC);

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
        int minerCount = Helper.getRobotsOfType(allFriendlies, RobotType.MINER);
        rc.broadcast(ChannelList.MINER_COUNT, minerCount);
        Debug.setString(1, String.format("broadcasting %s on channel %s", minerCount, ChannelList.MINER_COUNT), rc);

        boolean currentlyNeedJob = Communication.someoneIsNeededFor(Job.SUPPLY_MINERS);
        int minersWithoutSupply = Helper.getRobotsOfATypeWithNoSupply(allFriendlies, RobotType.MINER,
                MINER_WITH_NO_SUPPLY_TOP_THRESHOLD + 1);

        if (currentlyNeedJob) {
            if (minersWithoutSupply >= MINER_WITH_NO_SUPPLY_BOTTOM_THRESHOLD) {
                Communication.weNeed(Job.SUPPLY_MINERS);
            }
        }
        else {
            if (minersWithoutSupply >= MINER_WITH_NO_SUPPLY_TOP_THRESHOLD) {
                Communication.weNeed(Job.SUPPLY_MINERS);
            }
        }

        if (!rc.isCoreReady()) {
            return;
        }

        if (rc.getTeamOre() >= RobotType.MINER.oreCost) {
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
