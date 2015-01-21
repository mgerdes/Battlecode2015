package warble;

import battlecode.common.*;
import warble.communication.Channel;
import warble.communication.Radio;

public class PathBuilder {
    static RobotController rc;

    static int[] xOffsets = {1,0,-1, 0,-1,1,-1, 1};
    static int[] yOffsets = {0,1, 0,-1,-1,1, 1,-1};

    public static void init(RobotController rcin) {
        rc = rcin;
    }

    public static Direction getDirection(int hashedMapLocation, int poi) throws GameActionException {
        int broadcastedValue = rc.readBroadcast(Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation);
        int direction = (int)(broadcastedValue / (Math.pow(10, poi - 1))) % 10 - 1;
        return Direction.values()[direction];
    }

    public static void setup(MapLocation[] towerList, MapLocation enemyHq) throws GameActionException {
        //--Only the HQ should call this
        //--This should only be called once

        //--broadcast all of destinations (point of interests)
        //--broad the number of POI
        for (int i = 0; i < towerList.length; i++) {
            int relativeX = getRelativeMapLocationX(towerList[i].x);
            int relativeY = getRelativeMapLocationY(towerList[i].y);
            rc.broadcast(Channel.POI[i], getHashedLocation(relativeX, relativeY));
        }
        int relativeX = getRelativeMapLocationX(enemyHq.x);
        int relativeY = getRelativeMapLocationY(enemyHq.y);
        rc.broadcast(Channel.POI[towerList.length], getHashedLocation(relativeX, relativeY));
        rc.broadcast(Channel.NUMBER_OF_POIS, towerList.length);

        beginBuild(1);

        rc.broadcast(Channel.BFS_LOOP_STATE, -1);
    }

    public static void build(int bytecodeLimit) throws GameActionException {
        int currentPOI = rc.readBroadcast(Channel.CURRENT_POI);
        int loopState = rc.readBroadcast(Channel.BFS_LOOP_STATE);

        while (!isQueueEmpty() || loopState != -1 /* not sure if this is needed */) {

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
                    rc.broadcast(Channel.BFS_LOOP_STATE, loopStateToBroadcast); // cost ~ 25
                    return;
                }
                if (loopState != -1) {
                    rc.broadcast(Channel.BFS_LOOP_STATE, -1); // cost ~ 25
                }
            }
        }

        endBuild(currentPOI);
    }

    private static void beginBuild(int poi) throws GameActionException {
        resetQueue();
        rc.broadcast(Channel.CURRENT_POI, poi);
        enqueue(rc.readBroadcast(Channel.POI[poi - 1]));
    }

    private static void endBuild(int poi) throws GameActionException {
        if (poi + 1 < rc.readBroadcast(Channel.NUMBER_OF_POIS)) {
            beginBuild(poi + 1);
        } else {
            // Having a current POI of 1 higher then the number of POIs signifies building is complete.
            rc.broadcast(Channel.CURRENT_POI, poi + 1);
        }
    }

    public static boolean isComplete() throws GameActionException {
        //--Returns true when all paths are done and broadcast
        return rc.readBroadcast(Channel.CURRENT_POI) == rc.readBroadcast(Channel.NUMBER_OF_POIS);
    }

    private static int getLoopState(int i, int currentLocationHashed) {
        return i * 100000 + currentLocationHashed;
    }

    private static int getLoopIndex(int loopState) {
        return loopState / 100000;
    }

    private static int getCurrentLocationHashed(int loopState) {
        return loopState % 100000;
    }

    private static int getRelativeMapLocationX(int absoluteX) throws GameActionException {
        MapLocation NWCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);
        return absoluteX - NWCorner.x;
    }

    private static int getRelativeMapLocationY(int absoluteY) throws GameActionException {
        MapLocation NWCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);
        return absoluteY - NWCorner.y;
    }

    //--TODO NWCorner should be saved somewhere to avoid same readBroadcast call.
    private static MapLocation getAbsoluteMapLocation(int x, int y) throws GameActionException {
        MapLocation NWCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);
        int xOffset = NWCorner.x;
        int yOffset = NWCorner.y;
        return new MapLocation(x + xOffset, y + yOffset);
    }

    //--TODO store TerrainTile.values().
    private static TerrainTile getTerrainTile(int hashedMapLocation) throws GameActionException {
        int tileNumber = rc.readBroadcast(Channel.NW_CORNER_TERRAIN_TILE + hashedMapLocation);
        return TerrainTile.values()[tileNumber];
    }

    private static boolean wasVisited(int hashedMapLocation, int poi) throws GameActionException {
        int value = rc.readBroadcast(Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation);
        int direction = (int)(value / (Math.pow(10, poi - 1))) % 10;
        return direction != 0;
    }

    private static void broadcastDirection(int direction, int hashedMapLocation, int currentPOI) throws GameActionException {
        int channelToBroadcastTo = Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation;
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
        int backOfQueue = rc.readBroadcast(Channel.BFS_QUEUE_BACK);
        rc.broadcast(backOfQueue + 1, value);
        rc.broadcast(Channel.BFS_QUEUE_BACK, backOfQueue + 1);
    }

    private static int dequeue() throws GameActionException {
        int frontOfQueue = rc.readBroadcast(Channel.BFS_QUEUE_FRONT);
        int returnValue = rc.readBroadcast(frontOfQueue);
        rc.broadcast(Channel.BFS_QUEUE_FRONT, frontOfQueue + 1);
        return returnValue;
    }

    private static boolean isQueueEmpty() throws GameActionException {
        return rc.readBroadcast(Channel.BFS_QUEUE_BACK) == rc.readBroadcast(Channel.BFS_QUEUE_FRONT);
    }

    private static void resetQueue() throws GameActionException {
        rc.broadcast(Channel.BFS_QUEUE_BACK, Channel.BFS_QUEUE_START);
        rc.broadcast(Channel.BFS_QUEUE_FRONT, Channel.BFS_QUEUE_START);
    }
}
