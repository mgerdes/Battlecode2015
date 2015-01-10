package soldiersAgainstBashers;

import battlecode.common.*;

public class HQ {
	public static RobotController rc;

	public static void init() {

		rc = RobotPlayer.rc;
		Orders.init();

		//--Starting units
		try {
			JobsQueue.init();
			JobsQueue.addJob(RobotType.BEAVER, 2);
			JobsQueue.addJob(RobotType.BARRACKS);
			JobsQueue.addJob(RobotType.BEAVER, 2);
			JobsQueue.addJob(RobotType.SOLDIER, 2);
			JobsQueue.addJob(RobotType.MINERFACTORY);
			JobsQueue.addJob(RobotType.SOLDIER, 5);
			JobsQueue.addJob(RobotType.MINER, 5);
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

		int roundNumber = Clock.getRoundNum();

		if (roundNumber % 8 == 0) {
			JobsQueue.addJob(RobotType.SOLDIER);
			JobsQueue.addJob(RobotType.BASHER);
		}

		if (roundNumber == 10) {
			Orders.sendSoldiersTo(new MapLocation(8157, 11992));
		}

		shareSupplyWithNearbyFriendlies();
	}

	private static void shareSupplyWithNearbyFriendlies() throws GameActionException {
		RobotInfo[] friendlies = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
		double mySupply = rc.getSupplyLevel();
		for (RobotInfo ri : friendlies) {
			if (ri.type == RobotType.DRONE && ri.supplyLevel < mySupply) {
				double total = mySupply + ri.supplyLevel;
				double transfer = total * 2 / 3 - ri.supplyLevel;
				rc.transferSupplies((int) transfer, ri.location);
				mySupply = rc.getSupplyLevel();
			} else if (ri.supplyLevel < 200) {
				rc.transferSupplies((int) mySupply / 8, ri.location);
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
