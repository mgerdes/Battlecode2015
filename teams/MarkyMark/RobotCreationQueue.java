package MarkyMark;

import battlecode.common.*;

// use slots 60000 - 65536

// TODO -- Deal with dependencies.
public class RobotCreationQueue {
	static final int LENGTH_CHANNEL = 60000;
	static final int HEAD_POS_CHANNEL = 60001;
	static final int TAIL_POS_CHANNEL = 60002;
	static final int START_POS = 60003;

	static RobotController rc;
	static RobotType[] robotTypes = RobotType.values();

	public static void reset() throws GameActionException {
		PriorityRobotCreationQueue.reset();
		rc.broadcast(LENGTH_CHANNEL, 0);
		rc.broadcast(HEAD_POS_CHANNEL, START_POS);
		rc.broadcast(TAIL_POS_CHANNEL, START_POS);
	}

	public static void init(RobotController rcin) throws GameActionException {
		rc = rcin;
		robotTypes = RobotType.values();
		PriorityRobotCreationQueue.init(rc);
	}

	public static void addRobotToCreate(RobotType robotTypeToCreate) throws GameActionException {
		addRobotToCreate(robotTypeToCreate.ordinal());
	}

	public static void addRobotToCreate(RobotType robotTypeToCreate, int count) throws GameActionException {
		for (int i = 0; i < count; i++) {
			addRobotToCreate(robotTypeToCreate);
		}
	}

	public static void addRobotToCreate(int robotTypeToCreate) throws GameActionException {
		int headPos = getHeadPos();
		rc.broadcast(headPos, robotTypeToCreate);
		updateHeadPos(headPos + 1);
		increaseLength();
	}

	// This returns NULL if the caller of the method cannot create the robot.
	public static RobotType getNextRobotToCreate() throws GameActionException {
		if (PriorityRobotCreationQueue.length() == 0) {
			if (length() > 0) {
				RobotType typeToCreate = robotTypes[getGetItemInQueue()];
				RobotType typeRequired = getTypeRequiredToCreate(typeToCreate);

				// Make sure dependencies are met, if not then add them to priority queue.
				RobotType dependency1;
				if (typeToCreate.isBuilding) {
					dependency1 = typeToCreate.dependency;
				} else {
					dependency1 = typeToCreate.spawnSource;
				}
				if (rc.checkDependencyProgress(dependency1) == DependencyProgress.NONE) {
					PriorityRobotCreationQueue.addRobotToCreate(RobotType.BEAVER);
					RobotType dependency2;
					if (dependency1.isBuilding) {
						dependency2 = dependency1.dependency;
					} else {
						dependency2 = dependency1.spawnSource;
					}
					if (rc.checkDependencyProgress(dependency2) == DependencyProgress.NONE) {
						PriorityRobotCreationQueue.addRobotToCreate(dependency2);
					}
					PriorityRobotCreationQueue.addRobotToCreate(dependency1);
				}

				if (rc.getType() == typeRequired && rc.getTeamOre() > typeToCreate.oreCost) {
					return typeToCreate;
				}
			}
			return null;
		} else {
			return PriorityRobotCreationQueue.getNextRobotToCreate();
		}
	}

	// Returns the robot type that can create the type of robot we want to create.
	public static RobotType getTypeRequiredToCreate(RobotType robotTypeToCreate) throws GameActionException {
		if (robotTypeToCreate.isBuilding) {
			return RobotType.BEAVER;
		} else {
			return robotTypeToCreate.spawnSource;
		}
	}

	public static void completedCreatingRobot() throws GameActionException {
		int priorityCreationQueueLength = PriorityRobotCreationQueue.length();
		if (priorityCreationQueueLength == 0) {
			int tailPos = getTailPos();
			updateTailPos(tailPos + 1);
			decreaseLength();
		} else {
			PriorityRobotCreationQueue.completedCreatingRobot();
		}
	}

	public static int getGetItemInQueue() throws GameActionException {
		return rc.readBroadcast(getTailPos());
	}

	public static int getHeadPos() throws GameActionException {
		return rc.readBroadcast(HEAD_POS_CHANNEL);
	}

	public static int getTailPos() throws GameActionException {
		return rc.readBroadcast(TAIL_POS_CHANNEL);
	}

	public static void updateHeadPos(int headPos) throws GameActionException {
		rc.broadcast(HEAD_POS_CHANNEL, headPos);
	}

	public static void updateTailPos(int tailPos)throws GameActionException {
		rc.broadcast(TAIL_POS_CHANNEL, tailPos);
	}

	public static int length() throws GameActionException {
		return rc.readBroadcast(LENGTH_CHANNEL);
	}

	public static void increaseLength() throws GameActionException {
		rc.broadcast(LENGTH_CHANNEL, length() + 1);
	}

	public static void decreaseLength() throws GameActionException {
		rc.broadcast(LENGTH_CHANNEL, length() - 1);
	}
}

