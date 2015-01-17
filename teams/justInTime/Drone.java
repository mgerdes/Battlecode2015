package justInTime;

import justInTime.constants.ChannelList;
import battlecode.common.*;
import justInTime.constants.Order;
import justInTime.constants.Symmetry;
import justInTime.navigation.Bug;
import justInTime.navigation.SafeBug;
import justInTime.util.Debug;
import justInTime.util.Helper;

public class Drone {
    private static RobotController rc;

    private static final int ROBOT_NOT_SET = -1;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;
    private static Team myTeam;
    private static int robotThatNeedsSupplyId;

    private static int[] directions = new int[]{0, -1, 1, -2, 2};
    private static boolean sizeAndCornersBroadcasted = false;

    private static int symmetry = 0;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        Bug.init(rcC);
        SafeBug.init(rcC);
        SupplySharing.init(rcC);
        Communication.init(rcC);
        MessageBoard.init(rcC);

        loop();
    }

    private static void loop() {
        while (true) {
            try {
                doYourThing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void doYourThing() throws GameActionException {
        SupplySharing.share();

        Order order = MessageBoard.getOrder(RobotType.DRONE);
        switch (order) {
            case SurveyMap:
                surveyMap();
                break;
            case AttackEnemyMiners:
                swarm();
                break;
            case Rally:
                rally();
                break;
        }
    }

    private static void surveyMap() throws GameActionException {
        Debug.setString(0, "surveying...", rc);
        if (!rc.isCoreReady()) {
            return;
        }

        if (sizeAndCornersBroadcasted) {
            return;
        }

        if (symmetry == Symmetry.UNKNOWN) {
            symmetry = rc.readBroadcast(ChannelList.MAP_SYMMETRY);
        }

        if (symmetry == Symmetry.UNKNOWN) {
            return;
        }

        if (symmetry == Symmetry.REFLECTION) {
            //--Start at our hq and go away from their hq until we hit a wall
            //--The distance between our hq and the wall will tell us one of the map dimensions
            //--Then travel back and forth in the other direction to find the other map dimension
        }
        else {
            //--Symmetry is rotation, so the map must be square
            //--Start at our hq and move away from their hq until we hit a corner.
            //--With this corner, we can calculate the map dimensions
            findCornerAndBroadcastMapDataForRotationalSymmetry();
        }
    }

    private static void findCornerAndBroadcastMapDataForRotationalSymmetry() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        if (isMapCorner(currentLocation)) {
            int mapHeight =
                    Math.abs(currentLocation.y - myHqLocation.y) * 2 + Math.abs(myHqLocation.y - enemyHqLocation.y);
            int mapWidth =
                    Math.abs(currentLocation.x - myHqLocation.x) * 2 + Math.abs(myHqLocation.x - enemyHqLocation.x);

            rc.broadcast(ChannelList.MAP_WIDTH, mapWidth);
            rc.broadcast(ChannelList.MAP_HEIGHT, mapHeight);

            Direction cornerDirection = getCornerDirection(currentLocation);
            broadcastFourCorners(currentLocation, cornerDirection, mapWidth, mapHeight);
        }
        else {
            int dx = myHqLocation.x - enemyHqLocation.x;
            int dy = myHqLocation.y - enemyHqLocation.y;
            MapLocation destination = myHqLocation.add(dx * 1000, dy * 1000);
            SafeBug.setDestination(destination);

            //--Need to pass in enemies for extra safety
            Direction directionTowardsCorner = SafeBug.getDirection(currentLocation);
            Debug.setString(2, "direction is " + directionTowardsCorner.toString(), rc);

            if (directionTowardsCorner != Direction.NONE) {
                rc.move(directionTowardsCorner);
            }
        }
    }

    private static void broadcastFourCorners(MapLocation corner, Direction cornerDirection, int mapWidth, int mapHeight) throws GameActionException {
        switch (cornerDirection) {
            case NORTH_EAST:
                Communication.setMapLocationOnChannel(corner, ChannelList.NE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(0 , mapHeight), ChannelList.SE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(-mapWidth, mapHeight), ChannelList.SW_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(-mapWidth, 0), ChannelList.NW_MAP_CORNER);
                break;
            case SOUTH_EAST:
                Communication.setMapLocationOnChannel(corner.add(0, -mapHeight), ChannelList.NE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner, ChannelList.SE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(-mapWidth, 0), ChannelList.SW_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(-mapWidth, -mapHeight), ChannelList.NW_MAP_CORNER);
                break;
            case SOUTH_WEST:
                Communication.setMapLocationOnChannel(corner.add(mapWidth, -mapHeight), ChannelList.NE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(mapWidth, 0), ChannelList.SE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner, ChannelList.SW_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(0, -mapHeight), ChannelList.NW_MAP_CORNER);
                break;
            case NORTH_WEST:
                Communication.setMapLocationOnChannel(corner.add(mapWidth, 0), ChannelList.NE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(mapWidth, mapHeight), ChannelList.SE_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner.add(0, mapHeight), ChannelList.SW_MAP_CORNER);
                Communication.setMapLocationOnChannel(corner, ChannelList.NW_MAP_CORNER);
                break;
        }

        sizeAndCornersBroadcasted = true;
        System.out.println("from corner " + cornerDirection.toString());
        System.out.printf("\nNE %s\nSE %s\nSW %s\nNW %s\n",
                          Communication.readMapLocationFromChannel(ChannelList.NE_MAP_CORNER),
                          Communication.readMapLocationFromChannel(ChannelList.SE_MAP_CORNER),
                          Communication.readMapLocationFromChannel(ChannelList.SW_MAP_CORNER),
                          Communication.readMapLocationFromChannel(ChannelList.NW_MAP_CORNER));
    }

    private static Direction getCornerDirection(MapLocation currentLocation) {
        //--We assume this is a corner tile
        boolean northIsOffMap = rc.senseTerrainTile(currentLocation.add(Direction.NORTH)) == TerrainTile.OFF_MAP;
        boolean eastIsOffMap = rc.senseTerrainTile(currentLocation.add(Direction.EAST)) == TerrainTile.OFF_MAP;
        boolean westIsOffMap = rc.senseTerrainTile(currentLocation.add(Direction.WEST)) == TerrainTile.OFF_MAP;
        if (northIsOffMap) {
            if (eastIsOffMap) {
                return Direction.NORTH_EAST;
            }

            return Direction.NORTH_WEST;
        }

        if (westIsOffMap) {
            return Direction.SOUTH_WEST;
        }

        return Direction.SOUTH_EAST;
    }

    private static boolean isMapCorner(MapLocation currentLocation) {
        int edgeCount = 0;
        for (int i = 0; i < 8; i += 2) {
            if (rc.senseTerrainTile(currentLocation.add(Helper.getDirection(i))) == TerrainTile.OFF_MAP) {
                edgeCount++;
                if (edgeCount > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void rally() throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        MapLocation rallyPoint = Communication.readMapLocationFromChannel(ChannelList.RALLY_POINT);
        SafeBug.setDestination(rallyPoint);
        Direction direction = SafeBug.getDirection(currentLocation);

        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }

    private static void supplyMiners() throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        if (rc.getSupplyLevel() < 1000) {
            SafeBug.setDestination(myHqLocation);
            Direction direction = SafeBug.getDirection(currentLocation);
            rc.move(direction);
            Debug.setString(0, "going back home", rc);
            return;
        }

        MapLocation destination = null;
        if (robotThatNeedsSupplyId != ROBOT_NOT_SET) {
            //--Find the location of specific robot
            RobotInfo[] friendlies = rc.senseNearbyRobots(1000000, myTeam);
            for (RobotInfo robot : friendlies) {
                if (robot.ID == robotThatNeedsSupplyId) {
                    destination = robot.location;
                    break;
                }
            }
        }

        //--Find any miner with no supply and save the ID for next round
        if (destination == null) {
            RobotInfo[] friendlies = rc.senseNearbyRobots(1000000, myTeam);
            for (RobotInfo robot : friendlies) {
                if (robot.type == RobotType.MINER
                        && robot.supplyLevel == 0) {
                    destination = robot.location;
                    robotThatNeedsSupplyId = robot.ID;
                    break;
                }
            }
        }

        Debug.setString(0, String.format("going to robot %d at %s", robotThatNeedsSupplyId, destination), rc);

        if (currentLocation.distanceSquaredTo(destination) > GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
            SafeBug.setDestination(destination);
            Direction direction = SafeBug.getDirection(currentLocation);
            rc.move(direction);
        }
        else {
            rc.transferSupplies((int) rc.getSupplyLevel(), destination);
            robotThatNeedsSupplyId = ROBOT_NOT_SET;
        }
    }

    private static void swarm() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        //--Go home if we run out of supplies
        if (rc.getSupplyLevel() < 10) {
            SafeBug.setDestination(myHqLocation);
        }
        else {
            SafeBug.setDestination(enemyHqLocation);
        }

        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, enemyTeam);
        RobotType[] typesToIgnore = new RobotType[]{RobotType.BEAVER, RobotType.MINER};

        boolean iAmCoreReady = rc.isCoreReady();

        if (iAmCoreReady) {
            //--If any fighting units can shoot us, move away
            for (RobotInfo enemy : enemiesInSensorRange) {
                if (enemy.type != RobotType.MINER
                        && enemy.type != RobotType.BEAVER
                        && enemy.type.attackRadiusSquared >= currentLocation.distanceSquaredTo(enemy.location)) {
                    Direction runawayDirection = SafeBug.getDirection(
                            currentLocation,
                            null,
                            enemiesInSensorRange,
                            typesToIgnore);
                    Debug.setString(0, runawayDirection.toString(), rc);
                    if (runawayDirection != Direction.NONE) {
                        rc.move(runawayDirection);
                        return;
                    }
                    else {
                        //--We could not find a runaway direction... turn around!
                        runawayDirection = SafeBug.getPreviousDirection().opposite();
                        if (runawayDirection != Direction.NONE) {
                            tryMove(runawayDirection);
                            return;
                        }
                    }
                }
            }
        }

        //--If I can shoot any units, shoot them or wait until I can shoot them
        //--Otherwise, continue towards enemy using the awesome safe bug
        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length > 0) {
            if (rc.isWeaponReady()) {
                double lowestHealth = 1000000;
                int index = 0;
                for (int i = 0; i < enemiesInAttackRange.length; i++) {
                    double thisHealth = enemiesInAttackRange[0].health;
                    if (thisHealth < lowestHealth) {
                        lowestHealth = thisHealth;
                        index = i;
                    }
                }

                rc.attackLocation(enemiesInAttackRange[index].location);
            }
        }
        else if (iAmCoreReady) {
            Direction direction = SafeBug.getDirection(currentLocation, null, enemiesInSensorRange, typesToIgnore);
            if (direction != Direction.NONE) {
                rc.move(direction);
            }
        }
    }

    private static void tryMove(Direction initial) throws GameActionException {
        int initialDirectionValue = Helper.getInt(initial);
        for (int i = 0; i < directions.length; i++) {
            Direction direction = Helper.getDirection(initialDirectionValue + i);
            if (rc.canMove(direction)) {
                rc.move(direction);
                return;
            }
        }
    }
}
