package bfsTesting;

import battlecode.common.*;
import bfsTesting.communication.Channel;
import bfsTesting.communication.Radio;

public class PathBuilder {
    static RobotController rc;

    static int[] xOffsets = {1,0,-1, 0,-1,1,-1, 1};
    static int[] yOffsets = {0,1, 0,-1,-1,1, 1,-1};

    static int backOfQueue;

    // locally stored map data
    static boolean initedMapData;
    static MapLocation NWCorner;
    static int mapWidth;
    static int mapHeight;
    static TerrainTile[] terrainTiles;

    public static void init(RobotController rcin) {
        rc = rcin;
    }

    public static Direction getDirection(int hashedMapLocation, int poi) throws GameActionException {
        int broadcastedValue = rc.readBroadcast(Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation);
        int direction = ((int)(broadcastedValue / (Math.pow(10, poi))) % 10) - 1;
        if (direction == -1) {
            return Direction.NONE;
        }
        return Direction.values()[direction];
    }

    public static void initMapData() throws GameActionException {
        NWCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);
        mapWidth = rc.readBroadcast(Channel.MAP_WIDTH);
        mapHeight = rc.readBroadcast(Channel.MAP_HEIGHT);
        initedMapData = true;
        terrainTiles = TerrainTile.values();
    }

    public static void setup(MapLocation[] towerList, MapLocation enemyHq) throws GameActionException {
        //--Only the HQ should call this
        //--This should only be called once

        //--broadcast all of destinations (point of interests)
        //--broad the number of POI
        if (!initedMapData) initMapData();

        for (int i = 0; i < towerList.length; i++) {
            int relativeX = getRelativeMapLocationX(towerList[i].x);
            int relativeY = getRelativeMapLocationY(towerList[i].y);
            rc.broadcast(Channel.POI[i], getHashedLocation(relativeX, relativeY));
        }
        int relativeX = getRelativeMapLocationX(enemyHq.x);
        int relativeY = getRelativeMapLocationY(enemyHq.y);
        rc.broadcast(Channel.POI[towerList.length], getHashedLocation(relativeX, relativeY));
        rc.broadcast(Channel.NUMBER_OF_POIS, towerList.length + 1);
        rc.broadcast(Channel.BFS_LOOP_STATE, -1);

        beginBuild(0);
    }

    public static void build(int bytecodeLimit) throws GameActionException {
        if (!initedMapData) initMapData();

        int loopState = rc.readBroadcast(Channel.BFS_LOOP_STATE);

        int currentPOI = rc.readBroadcast(Channel.CURRENT_POI);
        if (currentPOI == rc.readBroadcast(Channel.NUMBER_OF_POIS)) {
            return;
        }

        backOfQueue = rc.readBroadcast(Channel.BFS_QUEUE_BACK);

        // debuging
        //int loopStateLocation = getCurrentLocationHashed(loopState);
        //System.out.println("Current poi : " + currentPOI);
        //System.out.println("loop state index : " + getLoopIndex(loopState));
        //System.out.println("loop state current position : "
        //        + getXCoordinate(loopStateLocation) + ", "
        //        + getYCoordinate(loopStateLocation));
        //System.out.println("Current destination: " + rc.readBroadcast(Channel.POI[currentPOI - 1]));

        while (!isQueueEmpty()) {
            int currentLocationHashed;
            int i = 0;
            if (loopState == -1) {
                currentLocationHashed = dequeue();
            } else {
                currentLocationHashed = getCurrentLocationHashed(loopState);
                i = getLoopIndex(loopState);
            }

            int currentX = getXCoordinate(currentLocationHashed);
            int currentY = getYCoordinate(currentLocationHashed);

            //--Can use relative locations because directionTo will also remain relative.
            MapLocation currentLocation = new MapLocation(currentX, currentY);

            for (; i < 8; i++) {
                int approximateMaxByteCodeCost = 230; // Each iteration of loop below is ~75, beginBuild is ~155
                if (Clock.getBytecodeNum() + approximateMaxByteCodeCost < bytecodeLimit) {
                    int nextX = currentX + xOffsets[i];
                    int nextY = currentY + yOffsets[i];
                    int nextLocationHashed = getHashedLocation(nextX, nextY);

                    if (nextX >= 0 && nextX <= mapWidth && nextY >= 0 && nextY <= mapHeight
                            && getTerrainTile(nextLocationHashed) == TerrainTile.NORMAL
                            && !wasVisited(nextLocationHashed, currentPOI)) { // cost ~ 10
                        MapLocation nextLocation = new MapLocation(nextX, nextY);
                        Direction directionToGo = nextLocation.directionTo(currentLocation); // cost ~ 10
                        broadcastDirection(directionToGo.ordinal(), nextLocationHashed, currentPOI); // cost ~ 30
                        enqueue(nextLocationHashed); // cost ~ 25
                    }
                } else {
                    int loopStateToBroadcast = getLoopState(i, currentLocationHashed);
                    updateBackOfQueue(); // cost ~ 25
                    rc.broadcast(Channel.BFS_LOOP_STATE, loopStateToBroadcast); // cost ~ 25
                    return;
                }
            }
            loopState = -1;
        }

        endBuild(currentPOI);
    }

    // cost ~ 155
    private static void beginBuild(int poi) throws GameActionException {
        rc.broadcast(Channel.BFS_LOOP_STATE, -1);
        rc.broadcast(Channel.CURRENT_POI, poi);

        resetQueue(); // cost ~ 50
        enqueue(rc.readBroadcast(Channel.POI[poi])); // cost ~ 30
        updateBackOfQueue(); // cost ~ 25
    }

    private static void endBuild(int poi) throws GameActionException {
        System.out.println("Finished BFS number " + poi);
        if (poi + 1 < rc.readBroadcast(Channel.NUMBER_OF_POIS))
            beginBuild(poi + 1);
        else
            rc.broadcast(Channel.CURRENT_POI, poi + 1);
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
        return absoluteX - NWCorner.x;
    }

    private static int getRelativeMapLocationY(int absoluteY) throws GameActionException {
        return absoluteY - NWCorner.y;
    }

    private static MapLocation getAbsoluteMapLocation(int x, int y) throws GameActionException {
        int xOffset = NWCorner.x;
        int yOffset = NWCorner.y;
        return new MapLocation(x + xOffset, y + yOffset);
    }

    private static TerrainTile getTerrainTile(int hashedMapLocation) throws GameActionException {
        int tileNumber = rc.readBroadcast(Channel.NW_CORNER_TERRAIN_TILE + hashedMapLocation);
        return terrainTiles[tileNumber];
    }

    private static boolean wasVisited(int hashedMapLocation, int poi) throws GameActionException {
        int value = rc.readBroadcast(Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation);
        int direction = (int)(value / (Math.pow(10, poi))) % 10;
        return direction != 0;
    }

    private static void broadcastDirection(int direction, int hashedMapLocation, int currentPOI) throws GameActionException {
        int channelToBroadcastTo = Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation;
        int currentValue = rc.readBroadcast(channelToBroadcastTo);
        int valueToBroadcast = currentValue + (direction + 1) * (int)Math.pow(10, (currentPOI));
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
        //System.out.println("enqueued " + value + " to " + backOfQueue);
        rc.broadcast(backOfQueue, value);
        backOfQueue++;
    }

    private static void updateBackOfQueue() throws GameActionException {
        //System.out.println("back of queue updated to " + backOfQueue);
        rc.broadcast(Channel.BFS_QUEUE_BACK, backOfQueue);
    }

    private static int dequeue() throws GameActionException {
        int frontOfQueue = rc.readBroadcast(Channel.BFS_QUEUE_FRONT);
        int returnValue = rc.readBroadcast(frontOfQueue);
        //System.out.println("dequeued " + returnValue + " to " + frontOfQueue);
        rc.broadcast(Channel.BFS_QUEUE_FRONT, frontOfQueue + 1);
        return returnValue;
    }

    private static boolean isQueueEmpty() throws GameActionException {
        return backOfQueue == rc.readBroadcast(Channel.BFS_QUEUE_FRONT);
    }

    private static void resetQueue() throws GameActionException {
        rc.broadcast(Channel.BFS_QUEUE_BACK, Channel.BFS_QUEUE_START);
        rc.broadcast(Channel.BFS_QUEUE_FRONT, Channel.BFS_QUEUE_START);
        backOfQueue = Channel.BFS_QUEUE_START;
    }

    public static void printDirectionField(int poi) throws GameActionException {
        int mapLocationHashed = rc.readBroadcast(Channel.POI[poi]);
        int xc = getXCoordinate(mapLocationHashed);
        int yc = getYCoordinate(mapLocationHashed);

        for (int y = 0; y <= mapHeight; y++) {
            for (int x = 0; x <= mapWidth; x++) {
                if (x == xc && y == yc) {
                    System.out.print("X");
                    continue;
                }
                switch(getDirection(getHashedLocation(x, y), poi)) {
                    case EAST:
                        System.out.print(">");
                        break;
                    case WEST:
                        System.out.print("<");
                        break;
                    case NORTH:
                        System.out.print("^");
                        break;
                    case SOUTH:
                        System.out.print("v");
                        break;
                    case NORTH_EAST:
                        System.out.print("/");
                        break;
                    case NORTH_WEST:
                        System.out.print("\\");
                        break;
                    case SOUTH_EAST:
                        System.out.print("\\");
                        break;
                    case SOUTH_WEST:
                        System.out.print("/");
                        break;
                    default:
                        System.out.print("?");
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }
}
