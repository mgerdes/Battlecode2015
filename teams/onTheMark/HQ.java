package onTheMark;

import battlecode.common.*;

public class HQ {
	public static RobotController rc;

	public static void init() {
		rc = RobotPlayer.rc;
		try {
			JobsQueue.init();

			JobsQueue.addJob(RobotType.BEAVER);
			JobsQueue.addJob(RobotType.BARRACKS);
			JobsQueue.addJob(RobotType.BEAVER);
			JobsQueue.addJob(RobotType.BEAVER);
			JobsQueue.addJob(RobotType.BEAVER);
			JobsQueue.addJob(RobotType.TANKFACTORY);
			JobsQueue.addJob(RobotType.MINERFACTORY);
			for (int i = 0; i < 500; i++) {
				if (i == 10) {
					JobsQueue.addJob(RobotType.MINER);
					JobsQueue.addJob(RobotType.MINER);
				}
				JobsQueue.addJob(RobotType.TANK);
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
		if (JobsQueue.canDoCurrentJob()) {
			int job = JobsQueue.getCurrentJob();			
			doJob(job);
		}

		shareSupplyWithNearbyFriendlies();
	}

	private static void shareSupplyWithNearbyFriendlies() throws GameActionException {
		RobotInfo[] friendlies = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
		double mySupply = rc.getSupplyLevel();
		for (int i = 0; i < friendlies.length; i++) {
			if (friendlies[i].supplyLevel == 0) {
				rc.transferSupplies((int) mySupply / friendlies.length, friendlies[i].location);
			}
		}
	}

	static void doJob(int job) throws GameActionException {
		RobotType spawnType = JobsQueue.getRobotTypeToCreate(job);
		spawn(spawnType);
	}

	static void spawn(RobotType type) throws GameActionException {
		Direction d = Navigation.randomDirection();		
		if (rc.isCoreReady() && rc.canSpawn(d, type)) {
			JobsQueue.currentJobCompleted();
			rc.spawn(d, type);
		}
	}
}
