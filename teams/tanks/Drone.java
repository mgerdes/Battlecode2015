package tanks;

import battlecode.common.*;

public class Drone {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;

        Bug.init(rcC);
        SupplySharing.init(rcC);
        Communication.init(rcC);

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        enemyTeam = rc.getTeam().opponent();

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
            case Tactic.SWARM:
                swarm();
                break;
            case Tactic.ATTACK_ENEMY_STRUCTURE:
                attackEnemyStructure();
                break;
        }
    }

    private static void attackEnemyStructure() throws GameActionException {
        MapLocation attackLocation = Communication.getAttackLocation();
        Bug.setDestination(attackLocation);

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
            if (rc.isCoreReady()) {
                MapLocation currentLocation = rc.getLocation();
                Direction direction = Bug.getSafeDirection(currentLocation, attackLocation);
                rc.move(direction);
            }
        }
        else  if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }

    private static void swarm() throws GameActionException {
        if (rc.getSupplyLevel() == 0) {
            Bug.setDestination(myHqLocation);
        }
        else {
            Bug.setDestination(enemyHqLocation);
        }

        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length == 0) {
           if (rc.isCoreReady()) {
               MapLocation currentLocation = rc.getLocation();
               Direction direction = Bug.getSafeDirection(currentLocation);
               rc.move(direction);
           }
        }
        else if (rc.isWeaponReady()) {
            rc.attackLocation(enemiesInAttackRange[0].location);
        }
    }
}
