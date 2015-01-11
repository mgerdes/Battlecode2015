package team030;

import battlecode.common.*;

import java.util.Random;

public class Beaver {
    private static RobotController rc;
    private static Team myTeam;
    private static Direction[] directions = Direction.values();
    private static Random random;
    private static final int MINER_FACTORY_COUNT = 1;

    public static void run(RobotController rcC) {
        rc = rcC;
        myTeam = rcC.getTeam();
        random = new Random(rcC.getID());
        loop();
    }

    private static void loop() {
        while (true) {
            try {
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

        RobotInfo[] allFriendlies = rc.senseNearbyRobots(Integer.MAX_VALUE, myTeam);

        if (shouldBuildMinerFactory(allFriendlies)) {
            build(RobotType.MINERFACTORY);
            return;
        }

        if (shouldBuildHelipad(allFriendlies)) {
            build(RobotType.HELIPAD);
            return;
        }

        if (rc.senseOre(rc.getLocation()) > 0) {
            rc.mine();
        }
        else {
            moveInRandomDirection();
        }
    }

    private static boolean shouldBuildMinerFactory(RobotInfo[] friendlyRobots) {
        if (rc.getTeamOre() < RobotType.MINERFACTORY.oreCost) {
            return false;
        }

        int minerFactoryCount = Helper.getRobotsOfType(friendlyRobots, RobotType.MINERFACTORY);
        return minerFactoryCount < MINER_FACTORY_COUNT;
    }

    private static boolean shouldBuildHelipad(RobotInfo[] friendlyRobots) {
        if (rc.getTeamOre() < RobotType.HELIPAD.oreCost) {
            return false;
        }

        int helipadCount = Helper.getRobotsOfType(friendlyRobots, RobotType.HELIPAD);
        if (helipadCount > 1) {
            return false;
        }

        int minerFactoryCount = Helper.getRobotsOfType(friendlyRobots, RobotType.MINERFACTORY);
        return helipadCount < 1
                || (helipadCount < 2 && minerFactoryCount > 0);
    }

    private static void build(RobotType type) throws GameActionException {
        int direction = 0;
        while (!rc.canBuild(directions[direction], type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.build(directions[direction], type);
    }

    private static void moveInRandomDirection() throws GameActionException {
        int firstDirection = random.nextInt(8);
        int direction = firstDirection;
        while (!rc.canMove(directions[direction])) {
            direction = (direction + 1) % 8;
            if (direction == firstDirection) {
                return;
            }
        }

        rc.move(directions[direction]);
    }
}
