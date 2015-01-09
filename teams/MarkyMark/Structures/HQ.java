package MarkyMark.Structures;

import MarkyMark.Info;
import MarkyMark.Navigation;
import MarkyMark.RobotCreationQueue;
import MarkyMark.Attack;
import battlecode.common.*;

public class HQ {
	public static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		try {
			RobotCreationQueue.reset();

			RobotCreationQueue.addRobotToCreate(RobotType.BEAVER);
			RobotCreationQueue.addRobotToCreate(RobotType.BARRACKS);
			RobotCreationQueue.addRobotToCreate(RobotType.HELIPAD);
			for (int i = 0; i < 10; i++) {
				if (i == 2) RobotCreationQueue.addRobotToCreate(RobotType.MINERFACTORY);
				if (i == 4) RobotCreationQueue.addRobotToCreate(RobotType.TANKFACTORY);
				if (i == 6) RobotCreationQueue.addRobotToCreate(RobotType.MINER);
				RobotCreationQueue.addRobotToCreate(RobotType.DRONE);
				RobotCreationQueue.addRobotToCreate(RobotType.BASHER);
				RobotCreationQueue.addRobotToCreate(RobotType.SOLDIER);
			}
			RobotCreationQueue.addRobotToCreate(RobotType.MINER);
			for (int i = 0; i < 100; i++) {
				if (i == 5) RobotCreationQueue.addRobotToCreate(RobotType.TANKFACTORY);
				RobotCreationQueue.addRobotToCreate(RobotType.BARRACKS);
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
		Attack.attack();
		provideSupplies();
		tryToCreateRobot();
	}

	// TODO -- Improve this.
	static void provideSupplies() throws GameActionException {
		RobotInfo[] robots = Info.goodGuysICanSee;
		for (RobotInfo robot : robots) {
			if (robot.supplyLevel < robot.type.supplyUpkeep * 500 && robot.location.distanceSquaredTo(Info.currentLocation) <= 15) {
				rc.transferSupplies(robot.type.supplyUpkeep * 500, robot.location);
			}
		}
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
