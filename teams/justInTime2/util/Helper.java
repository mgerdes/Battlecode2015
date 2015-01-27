package justInTime2.util;

import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Helper {
    private static Direction[] directions = new Direction[]{Direction.NORTH,
                                                            Direction.NORTH_EAST,
                                                            Direction.EAST,
                                                            Direction.SOUTH_EAST,
                                                            Direction.SOUTH,
                                                            Direction.SOUTH_WEST,
                                                            Direction.WEST,
                                                            Direction.NORTH_WEST};


    public static int getRobotsOfType(RobotInfo[] robots, RobotType type) {
        int count = 0;
        for (RobotInfo robot : robots) {
            if (robot.type == type) {
                count++;
            }
        }

        return count;
    }

    public static RobotInfo[] getRobotsCanAttackLocation(RobotInfo[] robots, MapLocation location) {
        int allRobotCount = robots.length;
        int attackRobotCount = 0;
        boolean[] canAttack = new boolean[allRobotCount];

        for (int i = 0; i < allRobotCount; i++) {
            if (robots[i].location.distanceSquaredTo(location) <= robots[i].type.attackRadiusSquared) {
                attackRobotCount++;
                canAttack[i] = true;
            }
        }

        RobotInfo[] robotsCanAttack = new RobotInfo[attackRobotCount];
        int index = 0;
        for (int i = 0; i < allRobotCount; i++) {
            if (canAttack[i]) {
                robotsCanAttack[index++] = robots[i];
            }
        }

        return robotsCanAttack;
    }

    public static boolean canBeAttackedByTowers(MapLocation location, MapLocation[] towerLocations) {
        int towerCount = towerLocations.length;
        for (int i = 0; i < towerCount; i++) {
            if (location.distanceSquaredTo(towerLocations[i]) <= RobotType.TOWER.attackRadiusSquared) {
                return true;
            }
        }

        return false;
    }

    public static boolean canBeDamagedByHq(MapLocation location, MapLocation hq, int towerCount) {
        int attackRadiusSquared;
        if (towerCount > 4) {
            attackRadiusSquared = 62;
        }
        else if (towerCount > 1) {
            attackRadiusSquared = 35;
        }
        else {
            attackRadiusSquared = 24;
        }

        return location.distanceSquaredTo(hq) <= attackRadiusSquared;
    }

    public static Direction getDirectionAwayFrom(RobotInfo[] robots, MapLocation currentLocation) {
        int robotCount = robots.length;
        if (robotCount == 1) {
            return robots[0].location.directionTo(currentLocation);
        }

        int dx = 0;
        int dy = 0;
        for (int i = 0; i < robotCount; i++) {
            Direction d = robots[i].location.directionTo(currentLocation);
            dx += d.dx;
            dy += d.dy;
        }

        if (dx > 0) {
            if (dy < 0) {
                return Direction.NORTH_EAST;
            }
            else if (dy > 0) {
                return Direction.SOUTH_EAST;
            }
            else {
                return Direction.EAST;
            }
        }
        else if (dx < 0) {
            if (dy < 0) {
                return Direction.NORTH_WEST;
            }
            else if (dy > 0) {
                return Direction.SOUTH_WEST;
            }
            else {
                return Direction.WEST;
            }
        }
        else {
            if (dy < 0) {
                return Direction.NORTH;
            }
            else if (dy > 0) {
                return Direction.SOUTH;
            }
        }

        return Direction.NONE;
    }

    public static int getRobotsExcludingType(RobotInfo[] robots, RobotType typeToExclude) {
        int count = 0;
        int length = robots.length;
        for (int i = 0; i < length; i++) {
            if (robots[i].type != typeToExclude) {
                count++;
            }
        }

        return count;
    }

    public static MapLocation getTowerLocationThatCanAttackLocation(MapLocation location,
                                                                    MapLocation[] towerLocations) {
        int towerCount = towerLocations.length;
        for (int i = 0; i < towerCount; i++) {
            if (location.distanceSquaredTo(towerLocations[i]) <= RobotType.TOWER.attackRadiusSquared) {
                return location;
            }
        }

        return null;
    }

    public static int getRobotsOfATypeWithNoSupply(RobotInfo[] robots, RobotType type, int max) {
        int count = 0;
        for (RobotInfo robot : robots) {
            if (robot.type == type
                    && robot.supplyLevel == 0) {
                count++;
            }
        }

        return count;
    }

    public static Direction getDirection(int n) {
        if (n < 0) {
            n = n + 8;
        }

        return directions[n % 8];
    }

    public static int getInt(Direction d) {
        switch (d) {
            case NORTH:
                return 0;
            case NORTH_EAST:
                return 1;
            case EAST:
                return 2;
            case SOUTH_EAST:
                return 3;
            case SOUTH:
                return 4;
            case SOUTH_WEST:
                return 5;
            case WEST:
                return 6;
            case NORTH_WEST:
                return 7;
            default:
                return -1;
        }
    }

    public static MapLocation getMidpoint(MapLocation pointA, MapLocation pointB) {
        int xAve = (pointA.x + pointB.x) / 2;
        int yAve = (pointA.y + pointB.y) / 2;
        return new MapLocation(xAve, yAve);
    }

    public static MapLocation getWaypoint(double percentage, MapLocation pointA, MapLocation pointB) {
        int xAve = (int) (pointA.x * percentage + pointB.x * (1 - percentage));
        int yAve = (int) (pointA.y * percentage + pointB.y * (1 - percentage));
        return new MapLocation(xAve, yAve);
    }

    public static Direction getSumOfDirections(Direction[] allDirection) {
        //--Note: This method will favor diagonal directions
        int dx = 0;
        int dy = 0;
        int length = allDirection.length;
        for (int i = 0; i < length; i++) {
            dx = allDirection[i].dx;
            dy = allDirection[i].dy;
        }

        if (dx > 0) {
            if (dy < 0) {
                return Direction.NORTH_EAST;
            }
            else if (dy > 0) {
                return Direction.SOUTH_EAST;
            }
            else {
                return Direction.EAST;
            }
        }
        else if (dx < 0) {
            if (dy < 0) {
                return Direction.NORTH_WEST;
            }
            else if (dy > 0) {
                return Direction.SOUTH_WEST;
            }
            else {
                return Direction.WEST;
            }
        }
        else {
            if (dy < 0) {
                return Direction.NORTH;
            }
            else if (dy > 0) {
                return Direction.SOUTH;
            }
        }

        return Direction.NONE;
    }

    public static int getIndexOfClosestRobot(RobotInfo[] robots, MapLocation location) {
        int count = robots.length;
        if (count == 1) {
            return 0;
        }

        int smallestDistance = location.distanceSquaredTo(robots[0].location);
        int index = 0;
        for (int i = 1; i < count; i++) {
            int distance = location.distanceSquaredTo(robots[i].location);
            if (distance < smallestDistance) {
                index = i;
            }
        }

        return index;
    }
}
