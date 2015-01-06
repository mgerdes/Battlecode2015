package mining;

import battlecode.common.*;

import java.util.Map;
import java.util.Random;

public class Beaver {
    private static final int BUILD_CHANNEL = 0;
    private static MapLocation myHQ;
    private static RobotController rc;
    private static Random random;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static boolean isBuilder;
    private static RobotType building;

    public static void init(RobotController rcC) {
        rc = rcC;
        random = new Random(rc.getID());
        myHQ = rc.senseHQLocation();
    }

    public static void run() {
        try {
            MapLocation myHQ = rc.senseHQLocation();
            while (true) {
                if (isBuilder
                        || needBuilder()) {
                    building = claimBuildAssignment();
                    isBuilder = true;
                }

                if (isBuilder && rc.isCoreReady()) {
                    build();
                }
                else {
                    mine();
                }

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
    }

    private static void mine() throws GameActionException {
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(myHQ) < 4) {
            Direction away = current.directionTo(myHQ).opposite();
            if (rc.canMove(away)) {
                rc.move(away);
            }
        }
        else if (rc.senseOre(current) > 3 && rc.canMine()) {
            rc.mine();
        }
        else {
            Direction direction = DIRECTIONS[random.nextInt(8)];
            if (rc.canMove(direction)) {
                rc.move(direction);
            }
        }
    }

    private static void build() {
        MapLocation buildLocation = new MapLocation(8140, 11978);
    }

    private static boolean needBuilder() throws GameActionException {
        return (Clock.getRoundNum() - rc.readBroadcast(BUILD_CHANNEL) > 5);
    }

    private static RobotType claimBuildAssignment() throws GameActionException {
        rc.broadcast(BUILD_CHANNEL, Clock.getRoundNum());
        return RobotType.SUPPLYDEPOT;
    }
}
