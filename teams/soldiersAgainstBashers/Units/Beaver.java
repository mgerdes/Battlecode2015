package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.*;

public class Beaver {
	public static RobotController rc;

	static final int BEAVER_MIN_ORE = 40;

	static RobotType type;
	static int sensorRadiusSquared;
	static int attackRadiusSquared;
	static Team goodGuys;
	static Team badGuys;

	public static void init() {
		rc = RobotPlayer.rc;
		type = rc.getType();
		Move.init(rc);
		Navigation.init(rc);

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
		if (!rc.isCoreReady()) {
			return;
		}

		if (JobsQueue.canDoCurrentJob()) {
			int job = JobsQueue.getCurrentJob();
			doJob(job);
		} else if (rc.senseOre(rc.getLocation()) > BEAVER_MIN_ORE) {
			rc.mine();
		} else {
			Move.inRandomDirection();
		}
	}

	static void doJob(int job) throws GameActionException {
		RobotType typeToCreate = JobsQueue.getRobotTypeToCreate(job);

		Direction randomDirection = Navigation.getRandomDirection();
		if (rc.canBuild(randomDirection, typeToCreate)) {
			JobsQueue.currentJobCompleted();
			rc.build(randomDirection, typeToCreate);
		}
	}
}
