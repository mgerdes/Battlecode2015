package onTheMark.Units;

import battlecode.common.*;
import onTheMark.Attack;
import onTheMark.Navigation;
import onTheMark.Orders;
import onTheMark.RobotPlayer;

public class Basher {
    static RobotController rc = RobotPlayer.rc;
    static Team goodGuys;
    static Team badGuys;
    static int senseRadius;

    public static void init() {
        rc = RobotPlayer.rc;
        Orders.init();
        senseRadius = RobotType.BASHER.sensorRadiusSquared;
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
        if (rc.isCoreReady()) {
            RobotInfo[] enemies = rc.senseNearbyRobots(senseRadius, badGuys);
            if (enemies.length > 0) {
                Navigation.tryMoveTowards(enemies[0].location);
            }
            else {
                MapLocation order = Orders.getBasherDestination();
                if (order == null) {
                    Navigation.moveRandomly();
                }
                else {
                    Navigation.tryMoveTowards(order);
                }
            }
        }
    }
}
