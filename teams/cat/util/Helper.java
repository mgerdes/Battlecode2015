package cat.util;

import battlecode.common.MapLocation;
import cat.constants.ChannelList;
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

    public static int getCountChannelFor(RobotType type) {
        if (type == RobotType.MINER) {
            return ChannelList.MINER_COUNT;
        }
        else if (type == RobotType.DRONE) {
            return ChannelList.DRONE_COUNT;
        }
        else if (type == RobotType.SOLDIER) {
            return ChannelList.SOLDIER_COUNT;
        }
        else if (type == RobotType.BASHER) {
            return ChannelList.BASHER_COUNT;
        }
        else if (type == RobotType.TANK) {
            return ChannelList.TANK_COUNT;
        }
        else if (type == RobotType.LAUNCHER) {
            return ChannelList.LAUNCHER_COUNT;
        }

        return -1;
    }

    public static int getProductionChannelFor(RobotType type) {
        if (type == RobotType.MINER) {
            return ChannelList.MORE_MINERS;
        }
        else if (type == RobotType.DRONE) {
            return ChannelList.MORE_DRONES;
        }
        else if (type == RobotType.SOLDIER) {
            return ChannelList.MORE_SOLDIERS;
        }
        else if (type == RobotType.BASHER) {
            return ChannelList.MORE_BASHERS;
        }
        else if (type == RobotType.TANK) {
            return ChannelList.MORE_TANKS;
        }
        else if (type == RobotType.LAUNCHER) {
            return ChannelList.MORE_LAUNCHER;
        }

        return -1;
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

    public static boolean isInRectangle(MapLocation test, MapLocation corner1, MapLocation corner2) {
        return test.x > Math.min(corner1.x, corner2.x)
                && test.x < Math.max(corner1.x, corner2.x)
                && test.y > Math.min(corner1.y, corner2.y)
                && test.y < Math.max(corner1.y, corner2.y);
    }
}
