package mining;

import battlecode.common.*;

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

                    RobotInfo[] friendlies = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
                    double mySupply = rc.getSupplyLevel();
                    for (int i = 0; i < friendlies.length; i++) {
                        if (friendlies[i].supplyLevel == 0) {
                            rc.transferSupplies((int) mySupply / friendlies.length, friendlies[i].location);
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
