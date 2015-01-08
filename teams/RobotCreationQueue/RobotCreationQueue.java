package RobotCreationQueue;

    import battlecode.common.*;
import java.util.*;

// use slots 60000 - 65536

public class RobotCreationQueue {
	static final int LENGTH_CHANNEL = 60000;
	static final int HEAD_POS_CHANNEL = 60001;
	static final int TAIL_POS_CHANNEL = 60002;
	static final int START_POS = 60003;

	static RobotController rc;
	static RobotType[] robotTypes;

	// Special init from HQ so it's run only once at beginning. Or can be used to reset queue if a change in strategy is needed.
	public static void initFromHQ(RobotController rcin) throws GameActionException {
		init(rcin);
		rc.broadcast(LENGTH_CHANNEL, 0);
		rc.broadcast(HEAD_POS_CHANNEL, START_POS);
		rc.broadcast(TAIL_POS_CHANNEL, START_POS);
	}

	public static void init(RobotController rcin) {
		rc = rcin;	
		robotTypes = RobotType.values();
	}

	public static void addRobotToCreate(RobotType robotTypeToCreate) throws GameActionException {
		addRobotToCreate(robotTypeToCreate.ordinal());
	}

	public static void addRobotToCreate(int robotTypeToCreate) throws GameActionException {
		int headPos = getHeadPos();
		rc.broadcast(headPos, robotTypeToCreate);
		updateHeadPos(headPos + 1);
		increaseLength();
	}

	// TODO -- also deal with dependencies.
	// This returns NULL if the caller of the method cannot create the robot.
	public static RobotType getNextRobotToCreate() throws GameActionException {
		if (length() > 0) {
			RobotType typeToCreate = robotTypes[getItemInQueue()];
			RobotType typeRequired = getTypeRequiredToCreateRobot(typeToCreate);

			if (rc.getType() == typeRequired && rc.getTeamOre() > typeToCreate.oreCost) {
				return typeToCreate;
			} 
		} 
		return null;
	}

	// Returns the robot type that can create the type of robot we want to create.
	public static RobotType getTypeRequiredToCreateRobot(RobotType robotTypeToCreate) throws GameActionException {
		if (robotTypeToCreate.isBuilding) {
			return RobotType.BEAVER;
		} else {
			return robotTypeToCreate.spawnSource;
		}
	}

	public static void completedCreatingRobot() throws GameActionException {
		int tailPos = getTailPos();
		updateTailPos(tailPos + 1);
		decreaseLength();
	}

	public static int getItemInQueue() throws GameActionException {
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
