package tankAgainstTower;

import battlecode.common.*;

public class Beaver {
    private static RobotController rc;
    private static MapLocation buildingLocation = new MapLocation(8140, 11978);

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void run() {
        try {
            while (true) {
                Bug.init(buildingLocation, rc);

                if (rc.isCoreReady()) {
                    MapLocation current = rc.getLocation();
                    if (current.distanceSquaredTo(buildingLocation) > 0) {
                        rc.move(Bug.getDirection(current));
                    } else {
                        rc.build(Direction.SOUTH, BuildingLogic.getNextBuilding());
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
