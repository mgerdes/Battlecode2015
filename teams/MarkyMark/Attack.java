package MarkyMark;

import battlecode.common.*;

// TODO -- Make attacking smarter.
// Have towers target the most powerful enemies, maybe other robots go after weaker ones first.
public class Attack {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
	}

	public static void attack() throws GameActionException {
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
}
