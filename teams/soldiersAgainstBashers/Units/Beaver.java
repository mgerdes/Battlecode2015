package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.*;

public class Beaver {
	public static RobotController rc;
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
			} catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	static void doYourThing() throws GameActionException {
		if (JobsQueue.canDoCurrentJob()) {
			int job = JobsQueue.getCurrentJob();			
			doJob(job);
		} else if (mine()) {
		} else {
			Navigation.moveRandomly();
		}
	}

	static boolean mine() throws GameActionException {
		if (rc.senseOre(rc.getLocation()) > 0) {
			if (rc.isCoreReady()) {
				rc.mine();
				return true;
			}
		}
		return false;
	}

	static void doJob(int job) throws GameActionException {
		RobotType typeToCreate = JobsQueue.getRobotTypeToCreate(job);

		Direction randomDirection = Navigation.randomDirection();
		if (rc.isCoreReady() && rc.canBuild(randomDirection, typeToCreate)) {
			JobsQueue.currentJobCompleted();
			rc.build(randomDirection, typeToCreate);
		}
	}
}
