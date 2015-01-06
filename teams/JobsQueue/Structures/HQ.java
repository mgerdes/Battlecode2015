package JobsQueue.Structures;

import battlecode.common.*;
import java.util.*;
import JobsQueue.*;

public class HQ {
	static RobotController rc = RobotPlayer.rc;

	public static void init() {
		try {
			JobsQueue.init();
			for (int i = 0; i < 5; i++) {
				JobsQueue.addJob(RobotType.HQ.ordinal(), RobotType.BEAVER.ordinal(), RobotType.BEAVER.oreCost); 	
			}
			JobsQueue.addJob(RobotType.BEAVER.ordinal(), RobotType.BARRACKS.ordinal(), RobotType.BARRACKS.oreCost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		loop();
	}

	static void loop() {
		while(true) {
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
			rc.setIndicatorString(0, "job is " + job);
			doJob(job);
		}
	}

	public static void doJob(int job) throws GameActionException {
		if (rc.isCoreReady() && rc.canSpawn(Navigation.randomDirection(), RobotType.BEAVER)) {
			rc.spawn(Navigation.randomDirection(), RobotType.BEAVER);
			JobsQueue.currentJobCompleted();	
		}
	}
}
