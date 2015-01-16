package framework;

import framework.constants.ChannelList;
import battlecode.common.*;
import framework.constants.Order;
import framework.navigation.Bug;
import framework.navigation.CircleNav;
import framework.navigation.SafeBug;
import framework.util.Debug;

public class Drone {
    private static RobotController rc;

    private static final int MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES = 25;
    private static final int ROBOT_NOT_SET = -1;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;
    private static Team myTeam;
    private static int robotThatNeedsSupplyId;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        Bug.init(rcC);
        SafeBug.init(rcC);
        CircleNav.init(rcC, myHqLocation, myHqLocation.directionTo(enemyHqLocation));
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
            //--Stuff here...
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

    private static void fortify() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        int minerRadius = rc.readBroadcast(ChannelList.MINER_RADIUS_FROM_HQ);

        if (rc.getSupplyLevel() == 0) {
            Bug.setDestination(myHqLocation);
        }
        else {
            MapLocation circleLocation = CircleNav.getDestination(minerRadius + 3, currentLocation);
            Bug.setDestination(circleLocation);
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                Direction direction = Bug.getDirection(currentLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }

    private static void attackEnemyStructure() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        MapLocation attackLocation = Communication.readMapLocationFromChannel(ChannelList.STRUCTURE_TO_ATTACK);

        //--Don't leave home without supplies
        if (rc.getSupplyLevel() == 0
                && currentLocation.distanceSquaredTo(myHqLocation) <= MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES) {
            SafeBug.setDestination(myHqLocation);
        }
        else {
            SafeBug.setDestination(attackLocation);
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                Direction direction = SafeBug.getDirection(currentLocation, attackLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }

    private static void swarm() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        //--Don't leave home without supplies
        if (rc.getSupplyLevel() == 0
                && currentLocation.distanceSquaredTo(myHqLocation) <= MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES) {
            SafeBug.setDestination(myHqLocation);
        }
        else {
            SafeBug.setDestination(enemyHqLocation);
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                Direction direction = SafeBug.getDirection(currentLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }
}
