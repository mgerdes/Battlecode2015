package afterDinnerMint;

import afterDinnerMint.constants.ChannelList;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class BuildingQueue {

    private static RobotController rc;


    public static void init(RobotController rcC) throws GameActionException {
        rc = rcC;

        if (rc.readBroadcast(ChannelList.NEXT_BUILDING_POINTER) == 0) {
            rc.broadcast(ChannelList.NEXT_BUILDING_POINTER, ChannelList.BUILDING_QUEUE_START);
            rc.broadcast(ChannelList.QUEUE_END_POINTER, ChannelList.BUILDING_QUEUE_START);
        }
    }

    public static void addBuilding(int building) throws GameActionException {
        //--The HQ may initialize the queue and add a building in the same turn,
        //  but the broadcasts will not go out until the end of turn, so the
        //  channel will have no value.
        int queueEndPointer = rc.readBroadcast(ChannelList.QUEUE_END_POINTER);
        rc.broadcast(queueEndPointer != 0 ? queueEndPointer : ChannelList.BUILDING_QUEUE_START, building);
        incrementChannel(ChannelList.QUEUE_END_POINTER);
    }

    public static int getNextBuilding() throws GameActionException {
        return rc.readBroadcast(rc.readBroadcast(ChannelList.NEXT_BUILDING_POINTER));
    }

    public static void confirmBuildingBegun() throws GameActionException {
        incrementChannel(ChannelList.NEXT_BUILDING_POINTER);
    }

    private static void incrementChannel(int channel) throws GameActionException {
        int value = rc.readBroadcast(channel);
        rc.broadcast(channel, value + 1);
    }
}
