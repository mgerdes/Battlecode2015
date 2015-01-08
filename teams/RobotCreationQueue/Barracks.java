package RobotCreationQueue;

import battlecode.common.*;
import java.util.*;

public class Barracks {
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
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	static void tryToCreateRobotFromQueue() throws GameActionException {
		RobotType robotToCreate = RobotCreationQueue.getNextRobotToCreate();
		if (robotToCreate != null) {
			if (rc.isCoreReady() && rc.canSpawn(Direction.NORTH, robotToCreate)) {
				RobotCreationQueue.completedCreatingRobot();
				rc.spawn(Direction.NORTH, robotToCreate);
			}
		}
	}
}
