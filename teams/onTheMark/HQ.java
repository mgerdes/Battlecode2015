package onTheMark;

import battlecode.common.*;

public class HQ {
	public static RobotController rc;

	public static void init() {
		rc = RobotPlayer.rc;
		try {
			JobsQueue.init();
			JobsQueue.addJob(RobotType.BEAVER, 4);
			JobsQueue.addJob(RobotType.MINERFACTORY);
			JobsQueue.addJob(RobotType.MINER, 5);
			JobsQueue.addJob(RobotType.HELIPAD);
			JobsQueue.addJob(RobotType.DRONE, 8);

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
			doJob();
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

	static void doJob() throws GameActionException {
		int job = JobsQueue.getCurrentJob();
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
