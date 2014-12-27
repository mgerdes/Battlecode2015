package Navigation;

import battlecode.common.*;

public class CachedMap {
    private static RobotController rc;
    private static TerrainTile[][] map;
    private static int mapHeight;
    private static int mapWidth;

    public static void init(RobotController rcC) {
        rc = rcC;
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        map = new TerrainTile[mapHeight][mapWidth];
    }

    public static TerrainTile getTile(MapLocation location) {
        if (map[location.y][location.x] == null) {
            map[location.y][location.x] = rc.senseTerrainTile(location);
        }

        return map[location.y][location.x];
    }

    public static boolean isNavigable(MapLocation location, Direction direction) {
        MapLocation next = location.add(direction);
        if (next.x < 0
                || next.y < 0
                || next.x >= mapWidth
                || next.y >= mapHeight) {
            return false;
        }
        return CachedMap.getTile(next) != TerrainTile.VOID;
    }
}
