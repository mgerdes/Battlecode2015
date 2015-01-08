package MarkyMark.Units;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

public class Beaver {
	public static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
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
		Info.getRoundInfo();
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
