package underdog.navigation;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import underdog.Communication;

public class WallFormation {
    //--Distance between units, not squared
    private static final int DEFAULT_ORTHOGONAL_DISTANCE = 3;
    private static final int DEFAULT_DIAGONAL_DISTANCE = 2;

    public static void updatePositions(MapLocation frontAndCenter,
                                       Direction perpendicular,
                                       int count,
                                       int firstChannel) throws GameActionException {
        if (count == 0) {
            return;
        }

        int distanceBetweenRobots =
                perpendicular.isDiagonal() ? DEFAULT_DIAGONAL_DISTANCE : DEFAULT_ORTHOGONAL_DISTANCE;

        MapLocation[] positions = new MapLocation[count];
        int lengthOfWall = (count - 1) * distanceBetweenRobots;
        positions[0] = frontAndCenter.add(perpendicular.rotateLeft().rotateLeft(), lengthOfWall / 2);
        for (int i = 1; i < count; i++) {
            positions[i] = positions[i - 1].add(perpendicular.rotateRight().rotateRight(), distanceBetweenRobots);
        }

        Communication.broadcastLocations(positions, firstChannel);
    }

    public static void updatePositions(MapLocation frontAndCenter,
                                       Direction perpendicular,
                                       int count,
                                       int wallWidth,
                                       int firstChannel) throws GameActionException {
        if (count == 0) {
            return;
        }

        int numberOfLayers = (int) Math.ceil((double) count / wallWidth);
        for (int i = 0; i < numberOfLayers; i++) {
            int distanceBetweenRows =
                    perpendicular.isDiagonal() ? DEFAULT_DIAGONAL_DISTANCE : DEFAULT_ORTHOGONAL_DISTANCE;
            MapLocation rowCenter = frontAndCenter.add(perpendicular.opposite(), distanceBetweenRows * (i - 1));
            updatePositions(rowCenter, perpendicular, Math.min(count, wallWidth), firstChannel + i * wallWidth * 3);
        }
    }
}
