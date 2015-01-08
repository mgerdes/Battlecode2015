package MarkyMark.Units;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

public class Beaver {
	public static RobotController rc;
	static RobotType type;
	static int sensorRadiusSquared;
	static int attackRadiusSquared;
	static Team goodGuys;
	static Team badGuys;

	public static void init(RobotController rcin) {
		rc = rcin;
		type = rc.getType();
		sensorRadiusSquared = type.sensorRadiusSquared;
		attackRadiusSquared = type.attackRadiusSquared;
		goodGuys = rc.getTeam();
		badGuys = goodGuys.opponent();
		try {
			RobotCreationQueue.init(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		loop();
	}

	static void loop() {
		while (true) {
			try {
				doYourThing();
			} catch (Exception e) {
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

		RobotType robotToCreate = RobotCreationQueue.getNextRobotToCreate(); 
		if (robotToCreate != null) {
			createRobot(robotToCreate);
		}
	}

	static void createRobot(RobotType robotToCreate) throws GameActionException {
		Direction randomDirection = Navigation.randomDirection();
		if (rc.isCoreReady() && rc.canBuild(randomDirection, robotToCreate)) {
			RobotCreationQueue.completedCreatingRobot();
			rc.build(randomDirection, robotToCreate);
		}
	}
}
