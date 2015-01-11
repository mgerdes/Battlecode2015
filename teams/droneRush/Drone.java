package droneRush;

import battlecode.common.*;

public class Drone {
    private static RobotController rc;

    private static Team enemyTeam;

    public static void run(RobotController rcC) {
        rc = rcC;
        Bug.init(rcC);
        Bug.setDestination(rcC.senseEnemyHQLocation());

        SupplySharing.init(rcC);
        Communication.init(rcC);

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
