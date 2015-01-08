package MarkyMark.Structures;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

public class Tower {
	static RobotController rc;
	static int sensorRadiusSquared;
	static int attackRadiusSquared;
	static Team goodGuys;
	static Team badGuys;

	public static void init(RobotController rcin) {
		rc = rcin;
		sensorRadiusSquared = RobotType.TOWER.sensorRadiusSquared;
		attackRadiusSquared = RobotType.TOWER.attackRadiusSquared;
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
			RobotInfo[] enemies = rc.senseNearbyRobots(attackRadiusSquared, badGuys);			
			Attack.something(enemies);
		}
	}
}
