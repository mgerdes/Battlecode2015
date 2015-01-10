package MarkyMark.Units;

import MarkyMark.Info;
import MarkyMark.Navigation;
import MarkyMark.RobotCreationQueue;
import battlecode.common.*;

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
		if (!tryToMine()) {
			//Navigation.moveRandomly();
		}
	}

	static void createRobot(RobotType robotToCreate) throws GameActionException {
		Direction randomDirection = Navigation.randomDirection();
		if (rc.isCoreReady() && rc.canBuild(randomDirection, robotToCreate)) {
			RobotCreationQueue.completedCreatingRobot();
			rc.build(randomDirection, robotToCreate);
		}
	}

	static boolean tryToMine() throws GameActionException {
		if (rc.senseOre(rc.getLocation()) > 5) {
			if (rc.isCoreReady()) {
				rc.mine();
				return true;
			}
		}
		return false;
	}
}
