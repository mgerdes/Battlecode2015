package onTheMark.Units;

import battlecode.common.*;
import onTheMark.Attack;
import onTheMark.Navigation;
import onTheMark.RobotPlayer;

public class Drone {
	static RobotController rc = RobotPlayer.rc;
	static RobotType type;
	static int sensorRadiusSquared;
	static int attackRadiusSquared;
	static Team goodGuys;
	static Team badGuys;

	public static void init() {
		rc = RobotPlayer.rc;
		type = rc.getType();
		sensorRadiusSquared = type.sensorRadiusSquared;
		attackRadiusSquared = type.attackRadiusSquared;
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
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(attackRadiusSquared, badGuys);
			Attack.something(enemies);
		}
		Navigation.moveRandomly();
	}
}
