package team030;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;
import team030.util.Helper;

public class MapAnalysis {
    private static RobotController rc;

    private static MapLocation[] towerLocations;
    private static boolean[] visited;
    private static int numberOfTowers;
    private static int startingTower;
    private static int endingTower;

    private static final int MAXIMUM_TOWER_SEPARATION_DISTANCE_SQUARED = 81;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static boolean towersFormWall(MapLocation[] towerLocationsC) {
        towerLocations = towerLocationsC;
        numberOfTowers = towerLocations.length;
        if (numberOfTowers == 1) {
            return false;
        }

        //--Towers must touch two different map edges
        //--Method side effect sets startingTower and endingTower
        boolean twoTowersTouchWall = getStartingAndEndingTowers(towerLocations);
        if (!twoTowersTouchWall) {
            return false;
        }

        //--We must make a line from one wall tower to the other
        visited = new boolean[numberOfTowers];
        visited[startingTower] = true;

        int[] towerQueue = new int[numberOfTowers];
        towerQueue[0] = startingTower;
        int head = 0;
        int tail = 1;

        while (head < numberOfTowers
                && head < tail) {
            int current = towerQueue[head++];
            for (int i = 0; i < numberOfTowers; i++) {
                if (!visited[i]
                        && towersAreConnected(current, i)) {

                    if (i == endingTower) {
                        return true;
                    }

                    visited[i] = true;
                    towerQueue[tail++] = i;
                }
            }
        }

        return false;
    }

    private static boolean towersAreConnected(int current, int i) {
        return towerLocations[current].distanceSquaredTo(towerLocations[i]) < MAXIMUM_TOWER_SEPARATION_DISTANCE_SQUARED;
    }

    private static boolean getStartingAndEndingTowers(MapLocation[] towerLocations) {
        //--Sets the starting and ending towers
        //--Returns false if cannot find two towers
        int numberOfTowers = towerLocations.length;
        boolean foundFirstTower = false;
        for (int i = 0; i < numberOfTowers; i++) {
            if (canShootUpToWall(towerLocations[i])) {
                if (foundFirstTower) {
                    endingTower = i;
                    return true;
                }
                else {
                    startingTower = i;
                    foundFirstTower = true;
                }
            }
        }

        return false;
    }

    private static boolean canShootUpToWall(MapLocation towerLocation) {
        for (int i = 0; i < 8; i++) {
            MapLocation check = towerLocation.add(Helper.getDirection(i), 5);
            if (rc.senseTerrainTile(check) == TerrainTile.OFF_MAP) {
                return true;
            }
        }

        return false;
    }
}
