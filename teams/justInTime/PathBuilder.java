package justInTime;

import battlecode.common.*;
import justInTime.constants.ChannelList;

public class PathBuilder {
    static RobotController rc;

    static int[] xOffsets = {1,0,-1, 0,-1,1,-1, 1};
    static int[] yOffsets = {0,1, 0,-1,-1,1, 1,-1};

    public static void setup(MapLocation[] towerList, MapLocation enemyHq) throws GameActionException {
        //--Only the HQ should call this
        //--This should only be called once

        //--broadcast all of destinations (point of interests)
        //--broad the number of POI

        rc.broadcast(ChannelList.BFS_LOOP_STATE, -1); // cost ~ 25
        resetQueue();
    }

    public static void build(int bytecodeLimit) throws GameActionException {
        int loopState = rc.readBroadcast(ChannelList.BFS_LOOP_STATE);

        while (!isQueueEmpty() || loopState != -1 /* not sure if this is needed */) {
            int currentPOI = rc.readBroadcast(ChannelList.CURRENT_POI);

            int currentLocationHashed;
            if (loopState == -1) {
                currentLocationHashed = dequeue();
            } else {
                currentLocationHashed = getCurrentLocationHashed(loopState);
            }

            int currentX = getXCoordinate(currentLocationHashed);
            int currentY = getYCoordinate(currentLocationHashed);

            //--Can use relative locations because directionTo will also remain relative.
            MapLocation currentLocation = new MapLocation(currentX, currentY);

            for (int i = 0; i < 8; i++) {
                if (loopState != -1) {
                    i = getLoopIndex(loopState);
                }
                int approximateByteCodeCost = 200;
                if (Clock.getBytecodeNum() + approximateByteCodeCost < bytecodeLimit) {
                    int nextX = currentX + xOffsets[i];
                    int nextY = currentY + yOffsets[i];
                    int nextLocationHashed = getHashedLocation(nextX, nextY);
                    if (getTerrainTile(nextLocationHashed) == TerrainTile.NORMAL
                            && !wasVisited(nextLocationHashed, currentPOI)) { // cost ~ 10
                        MapLocation nextLocation = new MapLocation(nextX, nextY);
                        Direction directionToGo = nextLocation.directionTo(currentLocation); // cost ~ 10
                        broadcastDirection(directionToGo.ordinal(), nextLocationHashed, currentPOI); // cost ~ 30
                        enqueue(nextLocationHashed); // cost ~ 55
                    }
                }
                else {
                    int loopStateToBroadcast = getLoopState(i, currentLocationHashed);
                    rc.broadcast(ChannelList.BFS_LOOP_STATE, loopStateToBroadcast); // cost ~ 25
                    return;
                }
                if (loopState != -1) {
                    rc.broadcast(ChannelList.BFS_LOOP_STATE, -1); // cost ~ 25
                }
            }

        }
    }

    public static boolean isComplete() {
        //--Returns true when all paths are done and broadcast
        return false;
    }

    public static int getLoopState(int i, int currentLocationHashed) {
        return i * 100000 + currentLocationHashed;
    }

    public static int getLoopIndex(int loopState) {
        return loopState / 100000;
    }

    public static int getCurrentLocationHashed(int loopState) {
        return loopState % 100000;
    }

    //--TODO NWCorner should be saved somewhere to avoid same readBroadcast call.
    private static MapLocation getAbsoluteMapLocation(int x, int y) throws GameActionException {
        MapLocation NWCorner = Communication.readMapLocationFromChannel(ChannelList.NW_MAP_CORNER);
        int xOffset = NWCorner.x;
        int yOffset = NWCorner.y;
        return new MapLocation(x + xOffset, y + yOffset);
    }


    //--TODO store TerrainTile.values().
    private static TerrainTile getTerrainTile(int hashedMapLocation) throws GameActionException {
        int tileNumber = rc.readBroadcast(ChannelList.NW_CORNER_TERRAIN_TILE + hashedMapLocation);
        return TerrainTile.values()[tileNumber];
    }

    private static boolean wasVisited(int hashedMapLocation, int poi) throws GameActionException {
        int value = rc.readBroadcast(ChannelList.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation);
        int direction = (int)(value / (Math.pow(10, poi - 1))) % 10;
        return direction != 0;
    }

    private static void broadcastDirection(int direction, int hashedMapLocation, int currentPOI) throws GameActionException {
        int channelToBroadcastTo = ChannelList.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation;
        int currentValue = rc.readBroadcast(channelToBroadcastTo);
        int valueToBroadcast = currentValue + (direction + 1) * (int)Math.pow(10, (currentPOI - 1));
        rc.broadcast(channelToBroadcastTo, valueToBroadcast);
    }

    private static int getHashedLocation(int x, int y) {
        return 120 * x + y;
    }

    private static int getXCoordinate(int hashedMapLocation) {
        return hashedMapLocation / 120;
    }

    private static int getYCoordinate(int hashedMapLocation) {
        return hashedMapLocation % 120;
    }

    // Queue for BFS
    private static void enqueue(int value) throws GameActionException {
        int backOfQueue = rc.readBroadcast(ChannelList.BFS_QUEUE_BACK);
        rc.broadcast(backOfQueue + 1, value);
        rc.broadcast(ChannelList.BFS_QUEUE_BACK, backOfQueue + 1);
    }

    private static int dequeue() throws GameActionException {
        int frontOfQueue = rc.readBroadcast(ChannelList.BFS_QUEUE_FRONT);
        int returnValue = rc.readBroadcast(frontOfQueue);
        rc.broadcast(ChannelList.BFS_QUEUE_FRONT, frontOfQueue + 1);
        return returnValue;
    }

    private static boolean isQueueEmpty() throws GameActionException {
        return rc.readBroadcast(ChannelList.BFS_QUEUE_BACK) == rc.readBroadcast(ChannelList.BFS_QUEUE_FRONT);
    }

    private static void resetQueue() throws GameActionException {
        rc.broadcast(ChannelList.BFS_QUEUE_BACK, ChannelList.BFS_QUEUE_START);
        rc.broadcast(ChannelList.BFS_QUEUE_FRONT, ChannelList.BFS_QUEUE_START);
    }
}
