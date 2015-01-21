package warble;

import warble.communication.Channel;
import battlecode.common.*;
import warble.communication.HqOrders;
import warble.communication.Radio;
import warble.constants.Order;
import warble.constants.Symmetry;
import warble.navigation.BasicNav;
import warble.navigation.Bug;
import warble.navigation.CircleNav;
import warble.navigation.SafeBug;
import warble.util.Debug;
import warble.util.Helper;

public class Drone {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;
    private static Direction awayFromEnemyHq;
    private static Team myTeam;


    private static int symmetry = 0;

    private static boolean deliveringSupply = false;

    private static boolean sizeAndCornersBroadcasted = false;

    private static boolean reflectionFoundFirstEdge = false;
    private static boolean reflectionFoundSegment = false;
    private static boolean onFullPass = false;

    private static int northEdge = 0;
    private static int southEdge = 0;
    private static int eastEdge = 0;
    private static int westEdge = 0;

    private static int valueAtBeginningOfPass;
    private static Direction segmentTravelDirection;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        awayFromEnemyHq = enemyHqLocation.directionTo(myHqLocation);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        BasicNav.init(rcC);
        Bug.init(rcC);
        SafeBug.init(rcC);
        SupplySharing.init(rcC);
        Radio.init(rcC);
        HqOrders.init(rcC);

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
        Order order = HqOrders.getOrder(RobotType.DRONE);
        switch (order) {
            case SurveyMap:
                Debug.setString(0, "surveying...", rc);
                SupplySharing.share();
                surveyMap();
                break;
            case Swarm:
                Debug.setString(0, "swarming...", rc);
                SupplySharing.shareOnlyWithType(RobotType.DRONE);
                swarmMicro();
                break;
            case Rally:
                Debug.setString(0, "rally...", rc);
                SupplySharing.share();
                rally();
                break;
            case MoveSupply:
                Debug.setString(0, "moving supply...", rc);
                moveSupply();
                break;
        }
    }

    private static void moveSupply() throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        RobotInfo[] enemiesInSensor = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, enemyTeam);
        RobotType[] enemiesToIgnore = new RobotType[]{RobotType.BEAVER, RobotType.DRONE};

        double currentSupply = rc.getSupplyLevel();
        //--Go back to HQ if we are delivering supply and we have less than 1000
        //--If we are not delivering supply, then we are going to the hq for supply,
        //  and we should keep going until we get 3000 supply
        if (deliveringSupply
                && currentSupply < 1000
                || !deliveringSupply
                && currentSupply < 5000) {
            deliveringSupply = false;
            SafeBug.setDestination(myHqLocation);
            Direction direction = SafeBug.getDirection(currentLocation, null, enemiesInSensor, enemiesToIgnore);
            if (direction != Direction.NONE) {
                rc.move(direction);
            }

            return;
        }

        if (currentSupply >= 5000) {
            deliveringSupply = true;
        }

        int robotID = Radio.getRobotIdThatNeedsSupply();
        if (robotID == 0) {
            //--There is no robot to supply, we call this method to get the default order.
            doYourThing();
            return;
        }

        MapLocation robotToSupplyLocation = rc.senseRobot(robotID).location;
        if (robotToSupplyLocation.distanceSquaredTo(currentLocation) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
            rc.transferSupplies((int) rc.getSupplyLevel(), robotToSupplyLocation);
            Debug.setString(
                    1,
                    String.format("passed supply to robot %d at location %s\n", robotID, robotToSupplyLocation),
                    rc);
            return;
        }

        SafeBug.setDestination(robotToSupplyLocation);
        Direction direction = SafeBug.getDirection(currentLocation, null, enemiesInSensor, enemiesToIgnore);
        if (direction != Direction.NONE) {
            rc.move(direction);
        }

        return;
    }

    private static void surveyMap() throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }

        if (sizeAndCornersBroadcasted) {
            MapLocation destination = Radio.readMapLocationFromChannel(Channel.LOCATION_TO_SURVEY);
            if (destination == null) {
                return;
            }
            else {
                Debug.setString(1, "going to " + destination.toString(), rc);

                SafeBug.setDestination(destination);
                Direction direction = SafeBug.getDirection(rc.getLocation());
                if (direction != Direction.NONE) {
                    rc.move(direction);
                }
            }
        }

        if (symmetry == Symmetry.UNKNOWN) {
            symmetry = rc.readBroadcast(Channel.MAP_SYMMETRY);
        }

        if (symmetry == Symmetry.UNKNOWN) {
            return;
        }

        if (symmetry == Symmetry.ROTATION) {
            findCornerAndBroadcastMapDataForRotationalSymmetry();
        }
        else {
            surveryAndBroadcastDataForReflectedMap();
        }
    }

    private static void surveryAndBroadcastDataForReflectedMap() throws GameActionException {
        //--The distance between our hq and the wall will tell us one of the map dimensions
        //--Then travel back and forth in the other direction to find the other map dimension
        if (!reflectionFoundFirstEdge) {
            travelToFirstEdgeAndBroadcastData();
        }

        if (!reflectionFoundSegment) {
            travelSegmentAndBroadcastData();
        }

        if (reflectionFoundFirstEdge
                && reflectionFoundSegment) {
            Radio.setMapLocationOnChannel(new MapLocation(eastEdge, northEdge), Channel.NE_MAP_CORNER);
            Radio.setMapLocationOnChannel(new MapLocation(eastEdge, southEdge), Channel.SE_MAP_CORNER);
            Radio.setMapLocationOnChannel(new MapLocation(westEdge, southEdge), Channel.SW_MAP_CORNER);
            Radio.setMapLocationOnChannel(new MapLocation(westEdge, northEdge), Channel.NW_MAP_CORNER);
            rc.broadcast(Channel.PERIMETER_SURVEY_COMPLETE, 1);
            sizeAndCornersBroadcasted = true;
        }
    }

    private static void travelToFirstEdgeAndBroadcastData() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        //--If we've hit the wall, update the data
        if (rc.senseTerrainTile(currentLocation.add(awayFromEnemyHq)) == TerrainTile.OFF_MAP) {
            int segmentLength;
            //--Were we going N/S or E/W
            if (awayFromEnemyHq == Direction.NORTH
                    || awayFromEnemyHq == Direction.SOUTH) {
                segmentLength = Math.abs(currentLocation.y - myHqLocation.y) * 2
                        + Math.abs(myHqLocation.y - enemyHqLocation.y);
                rc.broadcast(Channel.MAP_HEIGHT, segmentLength + 1);
            }
            else {
                segmentLength = Math.abs(currentLocation.x - myHqLocation.x) * 2
                        + Math.abs(myHqLocation.x - enemyHqLocation.x);
                rc.broadcast(Channel.MAP_WIDTH, segmentLength + 1);
            }

            setTwoMapEdges(awayFromEnemyHq, currentLocation, segmentLength);
            reflectionFoundFirstEdge = true;
            return;
        }

        //--We have not hit the wall. Keep going...
        if (!rc.isCoreReady()) {
            return;
        }

        SafeBug.setDestination(myHqLocation.add(awayFromEnemyHq, 1000));
        Direction d = SafeBug.getDirection(currentLocation);
        if (d != Direction.NONE) {
            rc.move(d);
        }
    }

    private static void travelSegmentAndBroadcastData() throws GameActionException {
        //--We need to traverse the map back and forth in the dimension
        //  perpendicular to the line that connects the hq
        MapLocation currentLocation = rc.getLocation();

        //--Have we reached a wall
        if (segmentTravelDirection != null
                && rc.senseTerrainTile(currentLocation.add(segmentTravelDirection)) == TerrainTile.OFF_MAP) {
            boolean goingNorthSouth = segmentTravelDirection == Direction.NORTH
                    || segmentTravelDirection == Direction.SOUTH;
            if (onFullPass) {
                int currentValue = goingNorthSouth ? currentLocation.y : currentLocation.x;
                int segmentLength = Math.abs(valueAtBeginningOfPass - currentValue);
                int channelToBroadcast = goingNorthSouth ? Channel.MAP_HEIGHT : Channel.MAP_WIDTH;
                rc.broadcast(channelToBroadcast, segmentLength + 1);
                reflectionFoundSegment = true;
                setTwoMapEdges(segmentTravelDirection, currentLocation, segmentLength);
            }
            else {
                valueAtBeginningOfPass = goingNorthSouth ? currentLocation.y : currentLocation.x;
                onFullPass = true;
                segmentTravelDirection = segmentTravelDirection.opposite();
            }

            return;
        }

        //--We have not reached a wall. Keep going...
        if (!rc.isCoreReady()) {
            return;
        }

        if (segmentTravelDirection == null) {
            segmentTravelDirection = awayFromEnemyHq.rotateLeft().rotateLeft();
        }

        SafeBug.setDestination(myHqLocation.add(segmentTravelDirection, 1000));
        Direction d = SafeBug.getDirection(currentLocation);
        if (d != Direction.NONE) {
            rc.move(d);
        }
    }

    private static void setTwoMapEdges(Direction awayFromEnemyHq, MapLocation currentLocation, int segmentLength) {
        switch (awayFromEnemyHq) {
            case NORTH:
                northEdge = currentLocation.y;
                southEdge = northEdge + segmentLength;
                break;
            case SOUTH:
                southEdge = currentLocation.y;
                northEdge = southEdge - segmentLength;
                break;
            case EAST:
                eastEdge = currentLocation.x;
                westEdge = eastEdge - segmentLength;
                break;
            case WEST:
                westEdge = currentLocation.x;
                eastEdge = westEdge + segmentLength;
                break;
        }
    }

    private static void findCornerAndBroadcastMapDataForRotationalSymmetry() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        //--If we are on a corner broadcast all the data
        if (isMapCorner(currentLocation)) {
            int mapHeight =
                    Math.abs(currentLocation.y - myHqLocation.y) * 2 + Math.abs(myHqLocation.y - enemyHqLocation.y);
            int mapWidth =
                    Math.abs(currentLocation.x - myHqLocation.x) * 2 + Math.abs(myHqLocation.x - enemyHqLocation.x);

            rc.broadcast(Channel.MAP_WIDTH, mapWidth + 1);
            rc.broadcast(Channel.MAP_HEIGHT, mapHeight + 1);

            Direction cornerDirection = getCornerDirection(currentLocation);
            broadcastFourCorners(currentLocation, cornerDirection, mapWidth, mapHeight);
            sizeAndCornersBroadcasted = true;
            return;
        }

        //--Go to the corner
        if (!rc.isCoreReady()) {
            return;
        }

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

    private static void broadcastFourCorners(MapLocation corner,
                                             Direction cornerDirection,
                                             int mapWidth,
                                             int mapHeight) throws GameActionException {
        switch (cornerDirection) {
            case NORTH_EAST:
                Radio.setMapLocationOnChannel(corner, Channel.NE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(0, mapHeight), Channel.SE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(-mapWidth, mapHeight), Channel.SW_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(-mapWidth, 0), Channel.NW_MAP_CORNER);
                break;
            case SOUTH_EAST:
                Radio.setMapLocationOnChannel(corner.add(0, -mapHeight), Channel.NE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner, Channel.SE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(-mapWidth, 0), Channel.SW_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(-mapWidth, -mapHeight), Channel.NW_MAP_CORNER);
                break;
            case SOUTH_WEST:
                Radio.setMapLocationOnChannel(corner.add(mapWidth, -mapHeight), Channel.NE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(mapWidth, 0), Channel.SE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner, Channel.SW_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(0, -mapHeight), Channel.NW_MAP_CORNER);
                break;
            case NORTH_WEST:
                Radio.setMapLocationOnChannel(corner.add(mapWidth, 0), Channel.NE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(mapWidth, mapHeight), Channel.SE_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner.add(0, mapHeight), Channel.SW_MAP_CORNER);
                Radio.setMapLocationOnChannel(corner, Channel.NW_MAP_CORNER);
                break;
        }

        rc.broadcast(Channel.PERIMETER_SURVEY_COMPLETE, 1);
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
        MapLocation rallyPoint = Radio.readMapLocationFromChannel(Channel.RALLY_POINT);
        CircleNav.init(rc, rallyPoint, rallyPoint.directionTo(enemyHqLocation));

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        MapLocation circle = CircleNav.getDestination(6, currentLocation);
        SafeBug.setDestination(circle);
        Direction direction = SafeBug.getDirection(currentLocation);

        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }

    private static void swarmMicro() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        SafeBug.setDestination(enemyHqLocation);
        boolean isCoreReady = rc.isCoreReady();
        boolean isWeaponReady = rc.isWeaponReady();

        if (rc.getSupplyLevel() < 500) {
            Radio.iNeedSupply();
        }

        //--Should micro consider attack delays?

        //--Should I stay or should I go?
        //
        //--I should go if many enemies can attack me, and I have no teammates to help.
        //--I should stay if I am a group that is attacking a weak enemy.
        //
        //--Are there enough friendlies to participate in attacking the group of enemies
        //  that can attack me?
        //

        //--Find the enemies that can attack me
        RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, enemyTeam);
        RobotInfo[] robotsCanAttackMe = getRobotsCanAttackLocation(enemiesInSensorRange, currentLocation);
        int canAttackMeCount = robotsCanAttackMe.length;

        //--If more than two enemies can attack me, move away
        if (canAttackMeCount > 2) {
            if (!isCoreReady) {
                return;
            }

            Direction away = Helper.getDirectionAwayFrom(robotsCanAttackMe, currentLocation);
            if (away == Direction.NONE) {
                //--Should we try to attack here since we can't move?
                return;
            }

            Direction navigableAway = BasicNav.getNavigableDirectionClosestTo(away);
            if (navigableAway == Direction.NONE) {
                //--Should we try to attack here since we can't move?
                return;
            }

            rc.move(navigableAway);
            return;
        }

        //--If two enemies can attack me,
        //    Run away if I cannot shoot either of them
        //    Run away if I have no friends that can shoot one of them
        if (canAttackMeCount == 2) {
            boolean[] iCanShoot = new boolean[2];
            int myRange = RobotType.DRONE.attackRadiusSquared;
            iCanShoot[0] = myRange >= currentLocation.distanceSquaredTo(robotsCanAttackMe[0].location);
            iCanShoot[1] = myRange >= currentLocation.distanceSquaredTo(robotsCanAttackMe[0].location);
            if (!iCanShoot[0]
                    && !iCanShoot[1]) {
                return;
            }

            boolean friendCanHelp = false;
            RobotInfo[] friendliesInSensorRange = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, myTeam);
            int friendlyCount = friendliesInSensorRange.length;
            for (int i = 0; i < friendlyCount; i++) {
                int attackRadiusSqured = friendliesInSensorRange[i].type.attackRadiusSquared;
                MapLocation friendlyLocation = friendliesInSensorRange[i].location;
                if (attackRadiusSqured >= friendlyLocation.distanceSquaredTo(robotsCanAttackMe[0].location)
                        || attackRadiusSqured >= friendlyLocation.distanceSquaredTo(robotsCanAttackMe[1].location)) {
                    friendCanHelp = true;
                    break;
                }
            }

            //--We can shoot one of the enemies, but we a need a friend to help
            if (friendCanHelp) {
                //--Attack the enemy with the lowest HP
                if (!isWeaponReady) {
                    return;
                }

                if (iCanShoot[0]
                        && iCanShoot[1]) {
                    int indexToShoot = robotsCanAttackMe[0].health < robotsCanAttackMe[1].health ? 0 : 1;
                    rc.attackLocation(robotsCanAttackMe[indexToShoot].location);
                }
                else if (iCanShoot[0]) {
                    rc.attackLocation(robotsCanAttackMe[0].location);
                }
                else {
                    rc.attackLocation(robotsCanAttackMe[1].location);
                }
            }
            else {
                //--No friend can help. time to go away.
                if (!isCoreReady) {
                    return;
                }
                
                Direction away = Helper.getDirectionAwayFrom(robotsCanAttackMe, currentLocation);
                if (away == Direction.NONE) {
                    //--Should we try to attack here since we can't move?
                    return;
                }

                Direction navigableAway = BasicNav.getNavigableDirectionClosestTo(away);
                if (navigableAway == Direction.NONE) {
                    //--Should we try to attack here since we can't move?
                    return;
                }

                rc.move(navigableAway);
            }

            return;
        }

        //--If one enemy can attack me, engage if I have more HP?
        //    Run away if I cannot shoot it
        if (canAttackMeCount == 1) {
            MapLocation enemyLocation = robotsCanAttackMe[0].location;
            if (currentLocation.distanceSquaredTo(enemyLocation) <= RobotType.DRONE.attackRadiusSquared) {
                if (isWeaponReady) {
                    rc.attackLocation(enemyLocation);
                }
            }
            else {
                Direction away = BasicNav.getNavigableDirectionClosestTo(enemyLocation.directionTo(currentLocation));
                if (away != Direction.NONE) {
                    rc.move(away);
                }
            }

            return;
        }

        //--No enemies are nearby that can attack me
        //--Go to destination
        if (!isCoreReady) {
            return;
        }

        RobotType[] typesToIgnore = new RobotType[]{RobotType.BEAVER, RobotType.MINER};
        Direction direction = SafeBug.getDirection(currentLocation, null, enemiesInSensorRange, typesToIgnore);
        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }

    private static MapLocation[] getLocationsCanAttackLocation(RobotInfo[] robots, MapLocation location) {
        int allRobotCount = robots.length;
        int attackRobotCount = 0;
        boolean[] canAttack = new boolean[allRobotCount];

        for (int i = 0; i < allRobotCount; i++) {
            if (robots[i].location.distanceSquaredTo(location) <= robots[i].type.attackRadiusSquared) {
                attackRobotCount++;
                canAttack[i] = true;
            }
        }

        MapLocation[] locationsCanAttack = new MapLocation[attackRobotCount];
        int index = 0;
        for (int i = 0; i < allRobotCount; i++) {
            if (canAttack[i]) {
                locationsCanAttack[index++] = robots[i].location;
            }
        }

        return locationsCanAttack;
    }

    private static RobotInfo[] getRobotsCanAttackLocation(RobotInfo[] robots, MapLocation location) {
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

    private static boolean canAttackNextTurn(RobotInfo robot) {
        //--If a robot has supply, next turn its weapon delay will be decremented by 1,
        //  otherwise, it will be decremented by 0.5
        return (robot.supplyLevel > 0
                && robot.weaponDelay < 2)
                || robot.weaponDelay < 1.5;
        //--NOTE: This is not accurate for robots that have less supply than their
        //  supply upkeep. In that case, their weapon delay is decremented by a value
        //  between 0 and 0.5. That is a rare situation, and it will only happen
        //  for one round, so I am ignoring it.
    }

    private static void swarm() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        SafeBug.setDestination(enemyHqLocation);

        if (rc.getSupplyLevel() < 500) {
            Radio.iNeedSupply();
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
                            Direction navigableRunaway = BasicNav.getNavigableDirectionClosestTo(runawayDirection);
                            if (navigableRunaway != Direction.NONE) {
                                rc.move(navigableRunaway);
                            }

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
}
