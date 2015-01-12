package afterDinnerMint;

import battlecode.common.*;
import afterDinnerMint.navigation.Bug;
import afterDinnerMint.util.ChannelList;

import java.util.Random;

public class Beaver {
    private static RobotController rc;

    private static final int MAX_DISTANCE_SQUARED_FROM_HQ = 25;

    private static final int MAX_MINER_FACTORY_COUNT = 1;
    private static final int MAX_HELIPAD_COUNT = 4;
    private static final int MAX_SUPPLY_DEPOT_COUNT = 1;
    private static final int DRONE_AND_MINER_COUNT_NEEDED_FOR_SUPPLY_DEPOT = 30;

    private static Team myTeam;
    private static Random random;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;

        myTeam = rcC.getTeam();
        myHqLocation = rcC.senseHQLocation();
        random = new Random(rcC.getID());

        Bug.init(rcC);

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

        if (shouldBuildSupplyDepot(allFriendlies)) {
            build(RobotType.SUPPLYDEPOT);
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

    private static boolean shouldBuildSupplyDepot(RobotInfo[] friendlyRobots) throws GameActionException {
        if (rc.getTeamOre() < RobotType.SUPPLYDEPOT.oreCost) {
            return false;
        }

        int supplyDepotCount = Helper.getRobotsOfType(friendlyRobots, RobotType.SUPPLYDEPOT);
        int droneAndMinerCount = rc.readBroadcast(ChannelList.DRONE_COUNT) + rc.readBroadcast(ChannelList.MINER_COUNT);
        return supplyDepotCount < MAX_SUPPLY_DEPOT_COUNT
                && droneAndMinerCount >= DRONE_AND_MINER_COUNT_NEEDED_FOR_SUPPLY_DEPOT;
    }

    private static boolean shouldBuildMinerFactory(RobotInfo[] friendlyRobots) {
        if (rc.getTeamOre() < RobotType.MINERFACTORY.oreCost) {
            return false;
        }

        int minerFactoryCount = Helper.getRobotsOfType(friendlyRobots, RobotType.MINERFACTORY);
        return minerFactoryCount < MAX_MINER_FACTORY_COUNT;
    }

    private static boolean shouldBuildHelipad(RobotInfo[] friendlyRobots) {
        if (rc.getTeamOre() < RobotType.HELIPAD.oreCost) {
            return false;
        }

        int helipadCount = Helper.getRobotsOfType(friendlyRobots, RobotType.HELIPAD);
        if (helipadCount >= MAX_HELIPAD_COUNT) {
            return false;
        }

        //--Build a helipad, then miner factory, then rest of helipads
        int minerFactoryCount = Helper.getRobotsOfType(friendlyRobots, RobotType.MINERFACTORY);
        return helipadCount < 1
                || (helipadCount < MAX_HELIPAD_COUNT && minerFactoryCount > 0);
    }

    private static void build(RobotType type) throws GameActionException {
        int direction = 0;
        while (!rc.canBuild(Helper.getDirection(direction), type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.build(Helper.getDirection(direction), type);
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
