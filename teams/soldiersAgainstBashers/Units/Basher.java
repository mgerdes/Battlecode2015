package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.Move;
import soldiersAgainstBashers.Navigation;
import soldiersAgainstBashers.Orders;
import soldiersAgainstBashers.RobotPlayer;

public class Basher {
    static RobotController rc = RobotPlayer.rc;
    static Team goodGuys;
    static Team badGuys;
    static int senseRadius;

    public static void init() {
        rc = RobotPlayer.rc;
        Orders.init();
        Move.init(rc);
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
            MapLocation currentLocation = rc.getLocation();
            RobotInfo[] enemies = rc.senseNearbyRobots(senseRadius, badGuys);
            if (enemies.length > 0) {
                Move.toward(enemies[0].location, currentLocation);
            }
            else {
                MapLocation order = Orders.getBasherDestination();
                if (order == null) {
                    Move.inRandomDirection();
                }
                else {
                    Move.toward(order, currentLocation);
                }
            }
        }
    }
}
