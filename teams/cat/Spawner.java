package cat;

import cat.constants.Order;
import cat.util.Debug;
import cat.util.Helper;
import battlecode.common.*;

public class Spawner {
    private static RobotController rc;
    private static RobotType[] typesBuiltHere;
    private static int[] robotCountChannels;
    private static int[] robotProductionChannels;
    private static Team myTeam;

    public static void init(RobotController rcC) {
        rc = rcC;

        myTeam = rc.getTeam();

        buildUnitData();

        loop();
    }

    private static void loop() {
        while (true) {
            try {
                if (rc.isCoreReady()) {
                    tryToSpawn();
                }

                broadcastRobotCounts();


            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void broadcastRobotCounts() throws GameActionException {
        RobotInfo[] allFriendlies = rc.senseNearbyRobots(1000000, myTeam);
        for (int i = 0; i < typesBuiltHere.length; i++) {
            int count = Helper.getRobotsOfType(allFriendlies, typesBuiltHere[i]);
            int channel = robotCountChannels[i];
            rc.broadcast(channel, count);
        }
    }

    private static void tryToSpawn() throws GameActionException {
        for (int i = 0; i < typesBuiltHere.length; i++) {
            int channel = robotProductionChannels[i];
            if (rc.readBroadcast(channel) == Order.YES
                    && rc.getTeamOre() >= typesBuiltHere[i].oreCost) {
                spawn(typesBuiltHere[i]);
                return;
            }
        }
    }

    private static void spawn(RobotType type) throws GameActionException {
        int direction = 0;
        while (!rc.canSpawn(Helper.getDirection(direction), type)) {
            direction++;
            if (direction > 7) {
                return;
            }
        }

        rc.spawn(Helper.getDirection(direction), type);
        Debug.setString(1, String.format("%s will spawn in round %d", type.name(), Clock.getRoundNum() + type
                .buildTurns), rc);
    }

    private static void buildUnitData() {
        RobotType myType = rc.getType();
        //--TODO: fix this!
        if (myType == RobotType.MINERFACTORY) {
            typesBuiltHere = new RobotType[]{RobotType.MINER};
        }
        else if (myType == RobotType.BARRACKS) {
            typesBuiltHere = new RobotType[]{RobotType.SOLDIER, RobotType.BASHER};
        }
        else if (myType == RobotType.HELIPAD) {
            typesBuiltHere = new RobotType[]{RobotType.DRONE};
        }
        else if (myType == RobotType.TANKFACTORY) {
            typesBuiltHere = new RobotType[]{RobotType.TANK};
        }
        else if (myType == RobotType.AEROSPACELAB) {
            typesBuiltHere = new RobotType[]{RobotType.LAUNCHER};
        }

        robotCountChannels = new int[typesBuiltHere.length];
        robotProductionChannels = new int[typesBuiltHere.length];

        for (int i = 0; i < typesBuiltHere.length; i++) {
            robotCountChannels[i] = Helper.getCountChannelFor(typesBuiltHere[i]);
            robotProductionChannels[i] = Helper.getProductionChannelFor(typesBuiltHere[i]);
        }
    }
}
