package MarkyMark;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

// TODO -- Make attacking smarter.
public class Attack {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
	}

	public static void something() throws GameActionException {
		RobotInfo[] enemies = Info.badGuysICanAttack;
		if (rc.isWeaponReady() && enemies.length > 0 && rc.canAttackLocation(enemies[0].location)) {
			rc.attackLocation(enemies[0].location);
		}
	}

	public static void attack(RobotType typeToAttack) throws GameActionException {
		RobotInfo[] enemies = Info.badGuysICanAttack;
		for (RobotInfo enemy : enemies) {
			if (enemy.type == typeToAttack) {
				if (rc.isWeaponReady() && rc.canAttackLocation(enemies[0].location)) {
					rc.attackLocation(enemy.location);
				}
			}
		}
	}

	public static void attackTowers() throws GameActionException {

	}
}
