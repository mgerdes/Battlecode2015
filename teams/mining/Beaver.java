package mining;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Beaver {
    private static RobotController rc;
    private static Random random;
    private static final Direction[] DIRECTIONS = Direction.values();

    public static void init(RobotController rcC) {
        rc = rcC;
        random = new Random(rc.getID());
    }

    public static void run() {
        try {
            MapLocation myHQ = rc.senseHQLocation();
            while (true) {
                if (rc.isCoreReady()) {
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

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
    }
}
