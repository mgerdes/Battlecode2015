package justInTime2.navigation;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import justInTime2.communication.Channel;
import justInTime2.communication.Encoding;
import justInTime2.communication.Radio;

public class Bfs {
    private static RobotController rc;

    private static MapLocation nwCorner;

    private static int minX;
    private static int minY;

    private static Direction[] directions;

    public static void init(RobotController rcIn) {
        rc = rcIn;
        Radio.init(rcIn);

        directions = Direction.values();
    }

    public static Direction getDirection(MapLocation currentLocation, int pointOfInterest) throws GameActionException {
        if (nwCorner == null) {
            nwCorner = Radio.readMapLocationFromChannel(Channel.NW_MAP_CORNER);

            //--Survey is not complete yet
            if (nwCorner == null) {
                return Direction.NONE;
            }

            minX = nwCorner.x;
            minY = nwCorner.y;
        }

        //--Convert absolute location to relative location using nwCorner
        int x = currentLocation.x - minX;
        int y = currentLocation.y - minY;

        //--Read the channel corresponding to the relative current location
        int hashedMapLocation = Encoding.getHashedLocation(x, y);

        //--Return the direction corresponding to the point of interest
        return getDirection(hashedMapLocation, pointOfInterest);
    }

    public static Direction getDirection(int hashedMapLocation, int poi) throws GameActionException {
        int broadcastedValue = rc.readBroadcast(Channel.NW_CORNER_BFS_DIRECTIONS + hashedMapLocation);
        int direction = ((int)(broadcastedValue / (Math.pow(10, poi))) % 10) - 1;
        if (direction == -1) {
            return Direction.NONE;
        }

        return directions[direction];
    }

    public static void printDirectionField(int poi) throws GameActionException {
        int mapLocationHashed = rc.readBroadcast(Channel.POI[poi]);
        int xc = Encoding.getXCoordinate(mapLocationHashed);
        int yc = Encoding.getYCoordinate(mapLocationHashed);

        int mapWidth = rc.readBroadcast(Channel.MAP_WIDTH);
        int mapHeight = rc.readBroadcast(Channel.MAP_HEIGHT);

        for (int y = 0; y <= mapHeight; y++) {
            for (int x = 0; x <= mapWidth; x++) {
                if (x == xc && y == yc) {
                    System.out.print("X");
                    continue;
                }
                switch(getDirection(Encoding.getHashedLocation(x, y), poi)) {
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
