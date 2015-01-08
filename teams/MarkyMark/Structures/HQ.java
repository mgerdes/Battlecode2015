package MarkyMark.Structures;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

public class HQ {
	public static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		try {
			RobotCreationQueue.reset();

			RobotCreationQueue.addRobotToCreate(RobotType.BEAVER);
			RobotCreationQueue.addRobotToCreate(RobotType.BARRACKS);
			RobotCreationQueue.addRobotToCreate(RobotType.MINERFACTORY);
			RobotCreationQueue.addRobotToCreate(RobotType.TANKFACTORY);
			RobotCreationQueue.addRobotToCreate(RobotType.MINER);
			RobotCreationQueue.addRobotToCreate(RobotType.MINER);
			for (int i = 0; i < 100; i++) {
				RobotCreationQueue.addRobotToCreate(RobotType.TANK);
			}

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
		Info.getRoundInfo();
		tryToCreateRobot();
	}

	static void tryToCreateRobot() throws GameActionException {
		RobotType robotToCreate = RobotCreationQueue.getNextRobotToCreate();
		if (robotToCreate != null) {
			createRobot(robotToCreate);
		}
	}

	static void createRobot(RobotType type) throws GameActionException {
		Direction d = Navigation.randomDirection();
		if (rc.isCoreReady() && rc.canSpawn(d, type)) {
			RobotCreationQueue.completedCreatingRobot();
			rc.spawn(d, type);
		}
	}
}
