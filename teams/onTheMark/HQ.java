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


//			JobsQueue.addJob(RobotType.BARRACKS);
//			JobsQueue.addJob(RobotType.MINERFACTORY);
//			JobsQueue.addJob(RobotType.MINER);
//			for (int i = 0; i < 10; i++) {
//				if (i == 3) {
//					JobsQueue.addJob(RobotType.TANKFACTORY);
//				}
//				if (i == 8) {
//					JobsQueue.addJob(RobotType.TECHNOLOGYINSTITUTE);
//				}
//				JobsQueue.addJob(RobotType.SOLDIER);
//			}
//			for (int i = 0; i < 20; i++) {
//				if (i == 5) {
//					JobsQueue.addJob(RobotType.TRAININGFIELD);
//				}
//				if (i == 10) {
//					JobsQueue.addJob(RobotType.COMMANDER);
//				}
//				JobsQueue.addJob(RobotType.TANK);
//			}
//			for (int i = 0; i < 100; i++) {
//				JobsQueue.addJob(RobotType.SOLDIER);
//			}

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
