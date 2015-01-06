package JobsQueue.Structures;

import battlecode.common.*;
import java.util.*;
import JobsQueue.*;

public class Tower {
	static RobotController rc;
	static int sensorRadiusSquared;
	static int attackRadiusSquared;
	static Team goodGuys;
	static Team badGuys;

	public static void init() {
		rc = RobotPlayer.rc;
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
			RobotInfo[] shootableBadGuys = rc.senseNearbyRobots(attackRadiusSquared, badGuys);			
			Attack.attackSomething(shootableBadGuys);
		}
	}
}
