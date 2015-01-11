package tanks;

import battlecode.common.*;

import java.util.Random;

public class Beaver {
    private static RobotController rc;
    private static Team myTeam;
    private static Direction[] directions = Direction.values();
    private static Random random;

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
        if (shouldBuildBarracks(allFriendlies)) {
            build(RobotType.BARRACKS);
            return;
        }

        if (shouldBuildTankFactory(allFriendlies)) {
            build(RobotType.TANKFACTORY);
            return;
        }

        if (rc.senseOre(rc.getLocation()) > 0) {
            rc.mine();
        }
        else {
            moveInRandomDirection();
        }
    }

    private static boolean shouldBuildBarracks(RobotInfo[] allFriendlies) {
        if (rc.getTeamOre() < RobotType.BARRACKS.oreCost) {
            return false;
        }

        int barracksCount = getRobotsOfType(allFriendlies, RobotType.BARRACKS);
        return barracksCount < 1;
    }

    private static boolean shouldBuildTankFactory(RobotInfo[] allFriendlies) {
        if (rc.getTeamOre() < RobotType.TANKFACTORY.oreCost) {
            return false;
        }

        int tankFactoryCount = getRobotsOfType(allFriendlies, RobotType.TANKFACTORY);
        return tankFactoryCount < 1;
    }

    private static void build(RobotType building) throws GameActionException {
        int direction = 0;
        while (!rc.canBuild(directions[direction], building)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.build(directions[direction], building);
    }

    private static int getRobotsOfType(RobotInfo[] robots, RobotType type) {
        int count = 0;
        for (RobotInfo robot : robots) {
            if (robot.type == type) {
                count++;
            }
        }

        return count;
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
