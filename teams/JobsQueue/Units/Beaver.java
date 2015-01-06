package JobsQueue.Units;

import battlecode.common.*;
import java.util.*;
import JobsQueue.*;

public class Beaver {
	static RobotController rc = RobotPlayer.rc;

	public static void init() {
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
		if (JobsQueue.canDoCurrentJob()) {
			int job = JobsQueue.getCurrentJob();			
			doJob(job);
		} 
		Navigation.moveRandomly();
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
