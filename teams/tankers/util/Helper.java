package tankers.util;

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
        int dx = 0;
        int dy = 0;
        int length = allDirection.length;
        for (int i = 0; i < length; i++) {
            dx += allDirection[i].dx;
            dy += allDirection[i].dy;
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

    public static Direction getDirectionAwayFrom(MapLocation[] locations, MapLocation currentLocation) {
        int locationCount = locations.length;
        if (locationCount == 1) {
            return locations[0].directionTo(currentLocation);
        }

        int dx = 0;
        int dy = 0;
        for (int i = 0; i < locationCount; i++) {
            Direction d = locations[i].directionTo(currentLocation);
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
}
