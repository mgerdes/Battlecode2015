package onTheMark;

import battlecode.common.*;

public class HQ {
	public static RobotController rc;

	public static void init() {
		rc = RobotPlayer.rc;
		Orders.init();

		try {
			JobsQueue.init();
			JobsQueue.addJob(RobotType.BEAVER, 2);
			JobsQueue.addJob(RobotType.MINERFACTORY);
			JobsQueue.addJob(RobotType.BEAVER, 2);
			JobsQueue.addJob(RobotType.MINER, 5);
			JobsQueue.addJob(RobotType.BARRACKS);
			JobsQueue.addJob(RobotType.MINER, 5);
			JobsQueue.addJob(RobotType.BASHER, 10);
			rc.setIndicatorString(0, String.format("used %d bytecodes in init", Clock.getBytecodeNum()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		loop();
	}

	static void loop() {
		while (true) {
			try {
				rc.setIndicatorString(1, String.format("current ore: %f", rc.getTeamOre()));
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

		if (Clock.getRoundNum() == 10) {
			Orders.sendBashersTo(new MapLocation(8150, 11984));
		}

		if (Clock.getRoundNum() == 900) {
			Orders.sendBashersTo(new MapLocation(8157, 11992));
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
