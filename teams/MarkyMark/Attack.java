package MarkyMark;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

public class Attack {
	static RobotController rc = RobotPlayer.rc;

	public static void something(RobotInfo[] enemies) throws GameActionException {
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

	public static void attackTower() throws GameActionException {
				
	}
}
