package justInTime;

import justInTime.constants.Building;
import justInTime.util.Helper;
import battlecode.common.*;
import justInTime.navigation.Bug;

import java.util.Random;

public class Beaver {
    private static RobotController rc;

    private static final int MAX_DISTANCE_SQUARED_FROM_HQ = 25;

    private static Random random;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) throws GameActionException {
        rc = rcC;

        myHqLocation = rcC.senseHQLocation();
        random = new Random(rcC.getID());

        BuildingQueue.init(rcC);
        Bug.init(rcC);
        SupplySharing.init(rcC);

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
        SupplySharing.shareMore();

        if (!rc.isCoreReady()) {
            return;
        }

        boolean builtSomethingThisTurn = tryToBuild();
        if (builtSomethingThisTurn) {
            return;
        }

        if (rc.senseOre(rc.getLocation()) > 0) {
            rc.mine();
        }
        else {
            MapLocation currentLocation = rc.getLocation();
            if (currentLocation.distanceSquaredTo(myHqLocation) > MAX_DISTANCE_SQUARED_FROM_HQ) {
                Bug.setDestination(myHqLocation);
                rc.move(Bug.getDirection(currentLocation));
            }
            else {
                moveInRandomDirection();
            }
        }
    }

    private static boolean tryToBuild() throws GameActionException {
        int nextBuilding = BuildingQueue.getNextBuilding();
        switch (nextBuilding) {
            case Building.MINER_FACTORY:
                return build(RobotType.MINERFACTORY);
            case Building.HELIPAD:
                return build(RobotType.HELIPAD);
            case Building.SUPPLY_DEPOT:
                return build(RobotType.SUPPLYDEPOT);
            case Building.BARRACKS:
                return build(RobotType.BARRACKS);
            case Building.TANK_FACTORY:
                return build(RobotType.TANKFACTORY);
            default:
                return false;
        }
    }

    private static boolean build(RobotType type) throws GameActionException {
        if (rc.getTeamOre() < type.oreCost) {
            return false;
        }

        int direction = 0;
        while (!rc.canBuild(Helper.getDirection(direction), type)) {
            direction++;
            if (direction > 7) {
                return false;
            }
        }

        rc.build(Helper.getDirection(direction), type);
        BuildingQueue.confirmBuildingBegun();
        return true;
    }

    private static void moveInRandomDirection() throws GameActionException {
        int direction = random.nextInt(8);
        int endDirection = direction + 8;
        while (!rc.canMove(Helper.getDirection(direction))) {
            direction++;
            if (direction == endDirection) {
                return;
            }
        }

        rc.move(Helper.getDirection(direction));
    }
}
