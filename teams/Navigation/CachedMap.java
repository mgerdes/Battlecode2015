package Navigation;

import battlecode.common.*;

public class CachedMap {
    private static RobotController rc;
    private static TerrainTile[][] map;

    public static void init(RobotController rcC) {
        rc = rcC;
        map = new TerrainTile[rc.getMapWidth()][rc.getMapHeight()];
    }

    public static TerrainTile getTile(int row, int col) {
        if (map[row][col] == null) {
            map[row][col] = rc.senseTerrainTile(new MapLocation(row, col));
        }

        return map[row][col];
    }

    public static TerrainTile getTile(MapLocation location) {
        if (map[location.x][location.y] == null) {
            map[location.x][location.y] = rc.senseTerrainTile(location);
        }

        return map[location.x][location.y];
    }

    public static boolean isNavigable(MapLocation location, Direction direction) {
        MapLocation next = location.add(direction);
        return CachedMap.getTile(next) != TerrainTile.VOID;
    }
}
