package RobotCreationQueue;

import battlecode.common.*;
import java.util.*;

public class Beaver {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		RobotCreationQueue.init(rc);
		run();
	}

	public static void run() {
		while (true) {
			try {
				tryToCreateRobotFromQueue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	static void tryToCreateRobotFromQueue() throws GameActionException {
		RobotType robotToCreate = RobotCreationQueue.getNextRobotToCreate();
		if (robotToCreate != null) {
			if (rc.isCoreReady() && rc.canBuild(Direction.NORTH, robotToCreate)) {
				RobotCreationQueue.completedCreatingRobot();
				rc.build(Direction.NORTH, robotToCreate);
			}
		}
	}
}
