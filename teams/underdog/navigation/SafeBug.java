package underdog.navigation;

import battlecode.common.*;

//--Version 1.0.0

public class SafeBug {
    //--Set once with init()
    private static RobotController rc;
    private static boolean defaultLeft;

    //--Map info, set every call to getDirection()
    private static MapLocation enemyHqLocations;
    private static MapLocation[] enemyTowerLocations;

    //--Per navigation path, set on setDestination()
    private static MapLocation destination;
    private static MapLocation ignoreLocation;

    //--Per round
    private static MapLocation currentLocation;
    private static Direction previousDirection;
    private static int previousDistance = Integer.MAX_VALUE;

    //--Bug path info
    private static boolean followingWall;
    private static int distanceStartBugging;
    private static int numberOfNinetyDegreeRotations;

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
        numberOfNinetyDegreeRotations = 0;
    }

    public static Direction getDirection(MapLocation currentLocationC, MapLocation ignoreC) {
        currentLocation = currentLocationC;
        ignoreLocation = ignoreC;
        enemyHqLocations = rc.senseEnemyHQLocation();
        enemyTowerLocations = rc.senseEnemyTowerLocations();

        if (previousDirection == null) {
            previousDirection = currentLocationC.directionTo(destination);
        }

        if (followingWall) {
            return getDirectionFollowingWall();
        }

        return getDirectionNotFollowingWall();
    }

    public static Direction getDirection(MapLocation currentLocationC) {
        return getDirection(currentLocationC, null);
    }

    private static Direction getDirectionFollowingWall() {
        int currentDistance = currentLocation.distanceSquaredTo(destination);
        if (currentDistance < distanceStartBugging) {
            followingWall = false;
            return getDirectionNotFollowingWall();
        }

        //--Hack to stop robots from going in circles!
        if (numberOfNinetyDegreeRotations == 4) {
            followingWall = false;
            return getDirectionNotFollowingWall();
        }

        if (currentDistance > previousDistance
                && onMapEdge()) {
            defaultLeft = !defaultLeft;
        }

        previousDistance = currentDistance;

        //--Check if we can go around the corner...
        Direction checkDirection = defaultLeft ?
                previousDirection.rotateRight().rotateRight()
                : previousDirection.rotateLeft().rotateLeft();
        if (canMoveSafely(checkDirection)) {
            numberOfNinetyDegreeRotations++;
            previousDirection = checkDirection;
            return checkDirection;
        }

        numberOfNinetyDegreeRotations = 0;
        Direction followDirection = getTurnDirection(checkDirection);
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
        numberOfNinetyDegreeRotations = 0;
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
        if (enemyHqLocations.equals(ignoreLocation)) {
            return false;
        }

        int hqAttackRange;
        if (enemyTowerLocations.length > 4) {
            //--Bonus for 2 and 5 towers
            hqAttackRange = GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED + GameConstants.HQ_BUFFED_SPLASH_RADIUS_SQUARED;
        }
        else if (enemyTowerLocations.length > 1) {
            //--Bonus for 2 towers
            hqAttackRange = GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
        }
        else {
            hqAttackRange = RobotType.HQ.attackRadiusSquared;
        }

        return location.distanceSquaredTo(enemyHqLocations) <= hqAttackRange;
    }
}
