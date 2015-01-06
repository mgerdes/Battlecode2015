package JobsQueue.Structures;

import battlecode.common.*;
import java.util.*;
import JobsQueue.*;

public class TankFactory {
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
	}

	public static void doJob(int job) throws GameActionException {
		Direction randomDirection = Navigation.randomDirection();
		if (rc.isCoreReady() && rc.canSpawn(randomDirection, RobotType.TANK)) {
			rc.spawn(randomDirection, RobotType.TANK);
			JobsQueue.currentJobCompleted();	
		}
	}
}
