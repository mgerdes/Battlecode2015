package droneTest;

import droneTest.communication.Channel;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class BuildingQueue {

    private static RobotController rc;

    private static int[] buildAfterRound = new int[20];

    public static void init(RobotController rcC) throws GameActionException {
        rc = rcC;

        if (rc.readBroadcast(Channel.NEXT_BUILDING_POINTER) == 0) {
            rc.broadcast(Channel.NEXT_BUILDING_POINTER, Channel.BUILDING_QUEUE_START);
            rc.broadcast(Channel.QUEUE_END_POINTER, Channel.BUILDING_QUEUE_START);
        }
    }

    public static boolean addBuilding(int building) throws GameActionException {
        if (Clock.getRoundNum() < buildAfterRound[building]) {
            return false;
        }

        //--The HQ may initialize the queue and add a building in the same turn,
        //  but the broadcasts will not go out until the end of turn, so the
        //  channel will have no value.
        int queueEndPointer = rc.readBroadcast(Channel.QUEUE_END_POINTER);
        rc.broadcast(queueEndPointer != 0 ? queueEndPointer : Channel.BUILDING_QUEUE_START, building);
        incrementChannel(Channel.QUEUE_END_POINTER);

        System.out.println("adding building " + building + " to queue.");

        return true;
    }

    public static int getNextBuilding() throws GameActionException {
        return rc.readBroadcast(rc.readBroadcast(Channel.NEXT_BUILDING_POINTER));
    }

    public static void confirmBuildingBegun() throws GameActionException {
        incrementChannel(Channel.NEXT_BUILDING_POINTER);
    }

    private static void incrementChannel(int channel) throws GameActionException {
        int value = rc.readBroadcast(channel);
        rc.broadcast(channel, value + 1);
    }

    public static void addBuildingWithPostDelay(int building, int numberOfRounds) throws GameActionException {
        if (addBuilding(building)) {
            buildAfterRound[building] = Clock.getRoundNum() + numberOfRounds;
        }
    }
}
