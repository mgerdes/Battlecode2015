package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.*;

public class Soldier {
    static RobotController rc = RobotPlayer.rc;
    static Team myTeam;
    static int attackRadius;
    static int senseRadius;
    static MapLocation myHQ;
    static MapLocation enemyHQ;

    public static void init() {
        rc = RobotPlayer.rc;
        Orders.init();
        Move.init(rc);
        Strategy.init(rc);
        senseRadius = RobotType.SOLDIER.sensorRadiusSquared;
        attackRadius = RobotType.SOLDIER.attackRadiusSquared;
        myTeam = rc.getTeam();
        myHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        loop();
    }

    static void loop() {
        while (true) {
            try {
                doYourThing();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    static void doYourThing() throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        StrategyEnum strategy = Strategy.get();
        if (strategy == StrategyEnum.Circle) {
            //--Move away from HQ, but stay within HQ transfer radius
            if (currentLocation.distanceSquaredTo(myHQ) < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                Move.toward(enemyHQ, currentLocation);
            }
        } else if (strategy == StrategyEnum.Expand) {
            Move.awayFromTeam(myTeam, currentLocation);
        }
    }
}
