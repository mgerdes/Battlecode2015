package droneRush;

import battlecode.common.*;

//--Version 1.0.2

public class Bug {
    private static RobotController rc;

    private static boolean DEFAULT_LEFT = true;

    private static MapLocation destination;
    private static MapLocation currentLocation;
    private static boolean followingWall;
    private static Direction previousDirection;
    private static int previousDistance = Integer.MAX_VALUE;
    private static int distanceStartBugging;
    private static MapLocation enemyHQLocation;
    private static MapLocation[] enemyTowerLocations;

    public static void init(RobotController rcC) {
        rc = rcC;
    }

    public static void setNewDestination(MapLocation destinationC) {
        destination = destinationC;
        followingWall = false;
        previousDirection = null;
        distanceStartBugging = 0;
        previousDistance = Integer.MAX_VALUE;
    }

    //--Returns a navigable direction that
    //- leads (eventually) to the destination
    public static Direction getSafeDirection(MapLocation currentLocationC) {
        currentLocation = currentLocationC;
        enemyHQLocation = rc.senseEnemyHQLocation();
        enemyTowerLocations = rc.senseEnemyTowerLocations();

        if (previousDirection == null) {
            previousDirection = currentLocationC.directionTo(destination);
        }

        if (followingWall) {
            return getDirectionFollowingWall();
        }

        return getDirectionNotFollowingWall();
    }

    private static Direction getDirectionFollowingWall() {
        int currentDistance = currentLocation.distanceSquaredTo(destination);
        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            return getDirectionNotFollowingWall();
        }

        if (currentDistance > previousDistance
                && onMapEdge()) {
            DEFAULT_LEFT = !DEFAULT_LEFT;
        }

        previousDistance = currentDistance;

        Direction followDirection = getFollowDirection(previousDirection);
        previousDirection = followDirection;
        rc.setIndicatorString(0, previousDirection.toString());
        return followDirection;
    }

    private static boolean onMapEdge() {
        Direction wallDirection = DEFAULT_LEFT
                ? previousDirection.rotateRight().rotateRight()
                : previousDirection.rotateLeft().rotateLeft();
        return rc.senseTerrainTile(currentLocation.add(wallDirection)) == TerrainTile.OFF_MAP;
    }

    private static Direction getDirectionNotFollowingWall() {
        Direction direct = currentLocation.directionTo(destination);
        if (canMoveSafely(direct)) {
            return direct;
        }

        followingWall = true;
        distanceStartBugging = currentLocation.distanceSquaredTo(destination);

        Direction turnDirection = getTurnDirection(direct);
        previousDirection = turnDirection;
        rc.setIndicatorString(0, previousDirection.toString());
        return turnDirection;
    }

    //--We turn the opposite way because we may need
    //- to round the corner
    private static Direction getFollowDirection(Direction initial) {
        //--TODO: optimize the double turn
        if (DEFAULT_LEFT) {
            return rotateLeftUntilCanMove(initial.rotateRight().rotateRight());
        }

        return rotateRightUntilCanMove(initial.rotateLeft().rotateLeft());
    }

    private static Direction getTurnDirection(Direction initial) {
        if (DEFAULT_LEFT) {
            Direction turn = initial.rotateLeft();
            return rotateLeftUntilCanMove(turn);
        }

        Direction turn = initial.rotateRight();
        return rotateRightUntilCanMove(turn);
    }

    private static Direction rotateLeftUntilCanMove(Direction direction) {
        while (!canMoveSafely(direction)) {
            direction = direction.rotateLeft();
        }

        return direction;
    }

    private static Direction rotateRightUntilCanMove(Direction direction) {
        while (!canMoveSafely(direction)) {
            direction = direction.rotateRight();
        }

        return direction;
    }

    //--Checks if the location will be within the attack range of enemy HQ or tower
    private static boolean canMoveSafely(Direction direction) {
        MapLocation next = currentLocation.add(direction);
        return rc.canMove(direction)
                && !withinHqAttackRange(next)
                && !withinTowerAttackRange(next);
    }

    private static boolean withinTowerAttackRange(MapLocation location) {
        for (MapLocation towerLocation : enemyTowerLocations) {
            if (location.distanceSquaredTo(towerLocation) <= RobotType.TOWER.attackRadiusSquared) {
                return true;
            }
        }

        return false;
    }

    private static boolean withinHqAttackRange(MapLocation location) {
        int hqAttackRange = enemyTowerLocations.length == 0 ?
                RobotType.HQ.attackRadiusSquared
                : GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
        return location.distanceSquaredTo(enemyHQLocation) <= hqAttackRange;
    }
}
