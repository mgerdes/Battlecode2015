package droneRush;

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

        if (shouldBuildHelipad()) {
            buildHelipad();
            return;
        }

        if (rc.senseOre(rc.getLocation()) > 0) {
            rc.mine();
        }
        else {
            moveInRandomDirection();
        }
    }

    private static boolean shouldBuildHelipad() {
        if (rc.getTeamOre() < RobotType.HELIPAD.oreCost) {
            return false;
        }

        RobotInfo[] allFriendlies = rc.senseNearbyRobots(Integer.MAX_VALUE, myTeam);
        int helipadCount = getHelipadStationCount(allFriendlies);
        int roundNumber = Clock.getRoundNum();
        return helipadCount < 1
                || (helipadCount < 2 && roundNumber > 150);
    }

    private static void buildHelipad() throws GameActionException {
        int direction = 0;
        while (!rc.canBuild(directions[direction], RobotType.HELIPAD)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.build(directions[direction], RobotType.HELIPAD);
    }

    private static int getHelipadStationCount(RobotInfo[] robots) {
        int count = 0;
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HELIPAD) {
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
