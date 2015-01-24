package bfsTesting;

import battlecode.common.*;
import bfsTesting.communication.Channel;
import bfsTesting.communication.Radio;
import bfsTesting.constants.Symmetry;

public class MapBuilder {
    private static RobotController rc;

    private static int mapWidth;
    private static int mapHeight;

    private static int totalTileCount;
    private static int tilesBroadcastCount;

    private static MapLocation myHq;


    private static int xLoop = 0;
    private static int yLoop = 0;

    public static int queueTail;

    private static final int MAX_BYTECODES_CONSUMED_IN_ONE_LOOP_PASS = 220;

    private static boolean[][] wasBroadcast;

    public static void init(MapLocation myHqC,
                            RobotController rcC) throws GameActionException {

        mapWidth = rcC.readBroadcast(Channel.MAP_WIDTH);
        mapHeight = rcC.readBroadcast(Channel.MAP_HEIGHT);
        myHq = myHqC;
        rc = rcC;

        MapEncoder.init(mapWidth, mapHeight, rcC);

        wasBroadcast = new boolean[mapWidth][mapHeight];

        totalTileCount = mapHeight * mapWidth;
        tilesBroadcastCount = 0;
        queueTail = Channel.SURVEY_LOCATION_QUEUE_START;

        //--Initialize queue head
        rc.broadcast(Channel.SURVEY_LOCATION_QUEUE_HEAD, Channel.SURVEY_LOCATION_QUEUE_START);
    }

    //--This method should be called by one robot
    //--It will broadcast all of the terrain tiles that it can either sense or infer from symmetry
    //--Unknown tiles are broadcast one at a time to Channel.LOCATION_TO_SURVEY
    public static boolean processUntilComplete(int bytecodeLimit) throws GameActionException {
        int initialBytecodeValue = Clock.getBytecodeNum();
        int finalBytecodeValue = initialBytecodeValue + bytecodeLimit;

        //--Start the loop where we left off
        for (; xLoop < mapWidth; xLoop++) {
            for (; yLoop < mapHeight; yLoop++) {
                //--Stop when we run out of bytecodes
                if (Clock.getBytecodeNum() > finalBytecodeValue - MAX_BYTECODES_CONSUMED_IN_ONE_LOOP_PASS) {
                    System.out.printf("Queue tail is %d\n", queueTail);
                    rc.broadcast(Channel.SURVEY_LOCATION_QUEUE_TAIL, queueTail);
                    return false;
                }

                if (wasBroadcast[xLoop][yLoop]) {
                    continue;
                }

                MapLocation locationToCheck = MapEncoder.getAbsoluteMapLocationForRelativeCoordinates(xLoop, yLoop);
                MapLocation reflected = MapEncoder.getReflectedMapLocation(locationToCheck);
                TerrainTile tile = rc.senseTerrainTile(locationToCheck);

                //--If tile is unknown check reflected tile
                if (tile == TerrainTile.UNKNOWN) {
                    tile = rc.senseTerrainTile(reflected);
                }

                //--If reflected tile is unknown, broadcast the one closer to our HQ
                if (tile == TerrainTile.UNKNOWN) {
                    MapLocation closerToOurHq =
                            myHq.distanceSquaredTo(locationToCheck) < myHq.distanceSquaredTo(reflected)
                            ? locationToCheck
                            : reflected;
                    rc.broadcast(queueTail++, MapEncoder.getHashForAbsoluteMapLocation(closerToOurHq));
                }
                else {
                    int offsetForFirstLocation = MapEncoder.getHashForRelativeCoordinates(xLoop, yLoop);
                    int offsetForReflectedLocation = MapEncoder.getReflectedChannelOffset(offsetForFirstLocation);
                    rc.broadcast(
                            Channel.NW_CORNER_TERRAIN_TILE + offsetForFirstLocation,
                            tile.ordinal());
                    rc.broadcast(
                            Channel.NW_CORNER_TERRAIN_TILE + offsetForReflectedLocation,
                            tile.ordinal());
                    wasBroadcast[MapEncoder.getXValue(offsetForFirstLocation)]
                            [MapEncoder.getYValue(offsetForFirstLocation)] = true;
                    wasBroadcast[MapEncoder.getXValue(offsetForReflectedLocation)]
                            [MapEncoder.getYValue(offsetForReflectedLocation)] = true;
                    tilesBroadcastCount += 2;
                }
            }

            yLoop = 0;
        }

        System.out.printf("Broadcast %f percent of tiles\n", (double) tilesBroadcastCount / totalTileCount * 100);
        return true;
    }
}
