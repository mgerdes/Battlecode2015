package soldiersAgainstBashers;

import battlecode.common.*;

public class Attack {
	static RobotController rc = RobotPlayer.rc;

	public static void something(RobotInfo[] enemies) throws GameActionException {
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

//        RobotInfo[] enemies = rc.senseNearbyRobots(attackRadius, badGuys);
//        if (enemies.length > 0 && rc.isWeaponReady()) {
//            MapLocation currentLocation = rc.getLocation();
//            double health = 10000;
//            int index = 0;
//            for (int i = 0; i < enemies.length; i++) {
//                if (enemies[i].health < health) {
//                    health = enemies[i].health;
//                    index = i;
//                }
//
//                if (enemies[i].location.distanceSquaredTo(currentLocation) < 4) {
//                    index = i;
//                    break;
//                }
//            }
//
//            rc.attackLocation(enemies[index].location);
//            return;
//        }
}
