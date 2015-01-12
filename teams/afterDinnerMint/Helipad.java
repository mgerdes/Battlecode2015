package afterDinnerMint;

import battlecode.common.*;
import afterDinnerMint.util.ChannelList;
import afterDinnerMint.util.Debug;

public class Helipad {
    private static RobotController rc;

    private static final int MIN_DRONE_ROUND = 140;

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
        broadcastDroneCount(allFriendlies);

        if (!rc.isCoreReady()) {
            return;
        }

        if (rc.getTeamOre() >= RobotType.DRONE.oreCost
                && Clock.getRoundNum() > MIN_DRONE_ROUND) {
            spawn(RobotType.DRONE);
        }
    }

    private static void broadcastDroneCount(RobotInfo[] friendlyRobots) throws GameActionException {
        int droneCount = Helper.getRobotsOfType(friendlyRobots, RobotType.DRONE);
        rc.broadcast(ChannelList.DRONE_COUNT, droneCount);
        Debug.setString(1, String.format("broadcasting %s on channel %s", droneCount, ChannelList.DRONE_COUNT), rc);
    }

    private static void spawn(RobotType type) throws GameActionException {
        int direction = 0;
        while (!rc.canSpawn(Helper.getDirection(direction), type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(Helper.getDirection(direction), type);
    }
}
