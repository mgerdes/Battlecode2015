package tanks;

import battlecode.common.*;

//--Version 1.0.2

public class Bug {
    //--Global
    private static RobotController rc;
    private static boolean defaultLeft;

    //--Map info
    private static MapLocation enemyHQLocation;
    private static MapLocation[] enemyTowerLocations;

    //--Per navigation path
    private static MapLocation destination;
    private static MapLocation ignoreLocation;

    //--Per round
    private static MapLocation currentLocation;
    private static Direction previousDirection;
    private static int previousDistance = Integer.MAX_VALUE;

    //--Bug path info
    private static boolean followingWall;
    private static int distanceStartBugging;
    private static MapLocation loopCheck;

    public static void init(RobotController rcC) {
        rc = rcC;
        defaultLeft = rcC.getID() % 2 == 0;
    }

    public static void setDestination(MapLocation destinationC) {
        //--Ignore if already set
        if (destinationC.equals(destination)) {
            return;
        }

        //--Reset bug state for new destination
        destination = destinationC;
        followingWall = false;
        previousDirection = null;
        distanceStartBugging = 0;
        previousDistance = Integer.MAX_VALUE;
    }

    public static Direction getSafeDirection(MapLocation currentLocationC, MapLocation ignoreC) {
        currentLocation = currentLocationC;
        ignoreLocation = ignoreC;
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

    public static Direction getSafeDirection(MapLocation currentLocationC) {
        return getSafeDirection(currentLocationC, null);
    }

    private static Direction getDirectionFollowingWall() {
        int currentDistance = currentLocation.distanceSquaredTo(destination);
        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            return getDirectionNotFollowingWall();
        }

        //--Hack to stop robots from going in circles!
        if (Clock.getRoundNum() % 4 == 0) {
            if (currentLocation.equals(loopCheck)) {
                followingWall = false;
                return getDirectionNotFollowingWall();
            }

            loopCheck = currentLocation;
        }

        if (currentDistance > previousDistance
                && onMapEdge()) {
            defaultLeft = !defaultLeft;
        }

        previousDistance = currentDistance;

        Direction followDirection = getFollowDirection(previousDirection);
        previousDirection = followDirection;
        return followDirection;
    }

    private static boolean onMapEdge() {
        Direction wallDirection = defaultLeft
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
        return turnDirection;
    }

    //--We turn the opposite way because we may need
    //- to round the corner
    private static Direction getFollowDirection(Direction initial) {
        //--TODO: optimize the double turn
        if (defaultLeft) {
            return rotateLeftUntilCanMove(initial.rotateRight().rotateRight());
        }

        return rotateRightUntilCanMove(initial.rotateLeft().rotateLeft());
    }

    private static Direction getTurnDirection(Direction initial) {
        if (defaultLeft) {
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
            if (towerLocation.equals(ignoreLocation)) {
                continue;

            }
            if (location.distanceSquaredTo(towerLocation) <= RobotType.TOWER.attackRadiusSquared) {
                return true;
            }
        }

        return false;
    }

    private static boolean withinHqAttackRange(MapLocation location) {
        if (enemyHQLocation.equals(ignoreLocation)) {
            return false;
        }

        int hqAttackRange = enemyTowerLocations.length == 0 ?
                RobotType.HQ.attackRadiusSquared
                : GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
        return location.distanceSquaredTo(enemyHQLocation) <= hqAttackRange;
    }
}
