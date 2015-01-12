package moneyMaker;

import battlecode.common.*;
import moneyMaker.navigation.Bug;
import moneyMaker.navigation.CircleNav;
import moneyMaker.navigation.SafeBug;
import moneyMaker.util.ChannelList;
import moneyMaker.util.Tactic;

public class Drone {
    private static RobotController rc;

    private static final int MAX_DISTANCE_TO_GO_TO_HQ_FOR_SUPPLIES = 25;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        enemyTeam = rc.getTeam().opponent();

        Bug.init(rcC);
        SafeBug.init(rcC);
        CircleNav.init(rcC, myHqLocation, myHqLocation.directionTo(enemyHqLocation));
        SupplySharing.init(rcC);
        Communication.init(rcC);

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

        int tactic = rc.readBroadcast(ChannelList.TACTIC);
        switch (tactic) {
            case Tactic.FORTIFY:
                fortify();
                break;
            case Tactic.SWARM:
                swarm();
                break;
            case Tactic.ATTACK_ENEMY_STRUCTURE:
                attackEnemyStructure();
                break;
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
        MapLocation attackLocation = Communication.getAttackLocation();

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
