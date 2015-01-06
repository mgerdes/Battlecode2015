package mining;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static int currentDirection = 0;

    public static void run(RobotController rc) {
        while (true) {
            if (rc.getType() == RobotType.HQ) {
                try {
                    //Spawn robots
                    if (rc.isCoreReady()) {
                        Direction spawnDirection = DIRECTIONS[currentDirection % 8];
                        if (rc.canSpawn(spawnDirection, RobotType.BEAVER)) {
                            rc.spawn(spawnDirection, RobotType.BEAVER);
                        }
                        else {
                            currentDirection++;
                        }
                    }

                    rc.yield();
                } catch (Exception e) {
                    System.out.println("HQ Exception");
                    e.printStackTrace();
                }
            }

            if (rc.getType() == RobotType.BEAVER) {
                Beaver.init(rc);
                Beaver.run();
            }
        }
    }
}
