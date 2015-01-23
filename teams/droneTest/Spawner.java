package droneTest;

import droneTest.communication.Channel;
import droneTest.communication.HqOrders;
import droneTest.util.Debug;
import droneTest.util.Helper;
import battlecode.common.*;

public class Spawner {
    private static RobotController rc;
    private static RobotType[] typesBuiltHere;
    private static int[] robotCountChannels;
    private static Team myTeam;

    public static void init(RobotController rcC) {
        rc = rcC;

        myTeam = rc.getTeam();

        HqOrders.init(rcC);

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
        for (RobotType type : typesBuiltHere) {
            if (rc.getTeamOre() >= type.oreCost
                    && HqOrders.shouldSpawn(type)) {
                spawn(type);
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
        switch (myType) {
            case MINERFACTORY:
                typesBuiltHere = new RobotType[]{RobotType.MINER};
                robotCountChannels = new int[]{Channel.MINER_COUNT};
                break;
            case BARRACKS:
                typesBuiltHere = new RobotType[]{RobotType.SOLDIER};
                robotCountChannels = new int[]{Channel.SOLDIER_COUNT};
                break;
            case HELIPAD:
                typesBuiltHere = new RobotType[]{RobotType.DRONE};
                robotCountChannels = new int[]{Channel.DRONE_COUNT};
                break;
            case AEROSPACELAB:
                typesBuiltHere = new RobotType[]{RobotType.LAUNCHER};
                robotCountChannels = new int[]{Channel.LAUNCHER_COUNT};
                break;
            case TANKFACTORY:
                typesBuiltHere = new RobotType[]{RobotType.TANK};
                robotCountChannels = new int[]{Channel.TANK_COUNT};
                break;
        }
    }
}
