package superCircle;

import battlecode.common.*;
import superCircle.navigation.Bug;
import superCircle.navigation.CircleNav;
import superCircle.navigation.SafeBug;
import superCircle.util.ChannelList;
import superCircle.util.Tactic;

public class Drone {
    private static RobotController rc;

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
        MapLocation attackLocation = Communication.getAttackLocation();
        SafeBug.setDestination(attackLocation);

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                MapLocation currentLocation = rc.getLocation();
                Direction direction = SafeBug.getDirection(currentLocation, attackLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }

    private static void swarm() throws GameActionException {
        if (rc.getSupplyLevel() == 0) {
            SafeBug.setDestination(myHqLocation);
        }
        else {
            SafeBug.setDestination(enemyHqLocation);
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                MapLocation currentLocation = rc.getLocation();
                Direction direction = SafeBug.getDirection(currentLocation);
                rc.move(direction);
            }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }
}
