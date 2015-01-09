package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.Navigation;
import soldiersAgainstBashers.Orders;
import soldiersAgainstBashers.RobotPlayer;

public class Soldier {
    static RobotController rc = RobotPlayer.rc;
    static Team goodGuys;
    static Team badGuys;
    static int attackRadius;

    public static void init() {
        rc = RobotPlayer.rc;
        Orders.init();
        attackRadius = RobotType.SOLDIER.attackRadiusSquared;
        goodGuys = rc.getTeam();
        badGuys = goodGuys.opponent();
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
        RobotInfo[] enemies = rc.senseNearbyRobots(attackRadius, badGuys);
        if (enemies.length > 0 && rc.isWeaponReady()) {
            MapLocation currentLocation = rc.getLocation();
            double health = 10000;
            int index = 0;
            for (int i = 0; i < enemies.length; i++) {
                if (enemies[i].health < health) {
                    health = enemies[i].health;
                    index = i;
                }

                if (enemies[i].location.distanceSquaredTo(currentLocation) < 4) {
                    index = i;
                    break;
                }
            }

            rc.attackLocation(enemies[index].location);
            return;
        }

        if (rc.isCoreReady()) {
            MapLocation order = Orders.getSoldierDestination();
            if (order == null) {
                Navigation.moveRandomly();
            } else {
                MapLocation current = rc.getLocation();
                if (current.distanceSquaredTo(order) > 9) {
                    Navigation.tryMoveTowards(order);
                }
            }
        }
    }
}