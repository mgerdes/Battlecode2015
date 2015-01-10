package soldiersAgainstBashers.Structures;

import battlecode.common.*;
import soldiersAgainstBashers.*;

public class Spawner {
	public static RobotController rc;

	public static void init() {
		rc = RobotPlayer.rc;
		Navigation.init(rc);
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
		}
	}

	static void doJob(int job) throws GameActionException {
		RobotType spawnType = JobsQueue.getRobotTypeToCreate(job);
		spawn(spawnType);
	}

	static void spawn(RobotType type) throws GameActionException {
		Direction d = Navigation.getRandomDirection();
		if (rc.isCoreReady() && rc.canSpawn(d, type)) {
			JobsQueue.currentJobCompleted();
			rc.spawn(d, type);
			rc.setIndicatorString(0, String.format("Round %d: Spawning a %s", Clock.getRoundNum(), type));
		}
	}
}
