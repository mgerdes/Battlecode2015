package underdog.navigation;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import underdog.Communication;

public class WallFormation {
    //--Distance between units, not squared
    private static int distanceBetweenRobots = 3;

    public static void updatePositions(MapLocation center,
                                       Direction perpendicular,
                                       int count,
                                       int firstChannel) throws GameActionException {
        if (count == 0) {
            return;
        }

        distanceBetweenRobots = perpendicular.isDiagonal() ? 2 : 3;

        MapLocation[] positions = new MapLocation[count];
        int lengthOfWall = (count - 1) * distanceBetweenRobots;
        positions[0] = center.add(perpendicular.rotateLeft().rotateLeft(), lengthOfWall / 2);
        for (int i = 1; i < count; i++) {
            positions[i] = positions[i - 1].add(perpendicular.rotateRight().rotateRight(), distanceBetweenRobots);
        }

        Communication.broadcastLocations(positions, firstChannel);
    }

    public static void updatePositions(MapLocation center,
                                       Direction perpendicular,
                                       int count,
                                       int layers,
                                       int firstChannel) throws GameActionException {
        if (count == 0) {
            return;
        }

        if (layers < 2) {
            updatePositions(center, perpendicular, count, firstChannel);
            return;
        }

        distanceBetweenRobots = perpendicular.isDiagonal() ? 2 : 3;

        MapLocation[] positions = new MapLocation[count];
        int lengthOfWall = (count - 1) * distanceBetweenRobots;
        int depthOfWall = (layers - 1) * distanceBetweenRobots;
        MapLocation sideOffset = center.add(perpendicular.rotateLeft().rotateLeft(), lengthOfWall / 2 / layers);
        positions[0] = sideOffset.add(perpendicular, depthOfWall / 2);
        for (int i = 1; i < count; i++) {
            if (i % layers == 0) {
                positions[i] = positions[i - layers].add(perpendicular.rotateRight().rotateRight(), distanceBetweenRobots);
            }
            else {
                positions[i] = positions[i - 1].add(perpendicular.opposite(), distanceBetweenRobots);
            }
        }

        Communication.broadcastLocations(positions, firstChannel);
    }
}
