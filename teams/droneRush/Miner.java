package droneRush;

import battlecode.common.*;

import java.util.Random;

public class Miner {
    private static RobotController rc;
    private static Direction[] directions = Direction.values();
    private static Random random;
    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;

        random = new Random(rcC.getID());
        enemyTeam = rc.getTeam().opponent();

        Communication.init(rcC);

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
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.BEAVER.sensorRadiusSquared, enemyTeam);
        if (enemies.length > 0) {
            Communication.broadcastEnemyInBase(enemies[0].location);
        }

        if (!rc.isCoreReady()) {
            return;
        }

        if (rc.senseOre(rc.getLocation()) > 0) {
            rc.mine();
        }
        else {
            moveInRandomDirection();
        }
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
