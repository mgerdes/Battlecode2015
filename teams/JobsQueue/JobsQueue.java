package JobsQueue;

import battlecode.common.*;
import java.util.*;

// use slots 65000 - 65536

public class JobsQueue {
	static final int LENGTH_CHANNEL = 60000;
	static final int HEAD_POS_CHANNEL = 60001;
	static final int TAIL_POS_CHANNEL = 60002;
	static final int START_POS = 60003;

	static RobotController rc = RobotPlayer.rc; 

	public static void init() throws GameActionException {
		rc.broadcast(LENGTH_CHANNEL, 0);
		rc.broadcast(HEAD_POS_CHANNEL, START_POS);
		rc.broadcast(TAIL_POS_CHANNEL, START_POS);
	}

	public static void addJob(int robotTypeRequired, int robotTypeToCreate, int cost) throws GameActionException {
		int headPos = getHeadPos();
		// XXYYZZZZ - XX = robotTypeRequired, YY = robotTypeToBeCreated, ZZZZ = cost
		int whatToBroadcast = robotTypeRequired * 1000000 + robotTypeToCreate * 10000 + cost ;
		rc.broadcast(headPos, whatToBroadcast);
		updateHeadPos(headPos + 1);
		increaseLength();
	}

	public static void currentJobCompleted() throws GameActionException {
		int tailPos = getTailPos();
		updateTailPos(tailPos + 1);
		decreaseLength();
	}

	// also check dependencies.
	public static boolean canDoCurrentJob()  throws GameActionException {
		if (length() > 0) {
			int job = getCurrentJob();
			int typeRequired = getTypeRequired(job);
			int cost = getCost(job);

			if (rc.getType().ordinal() == typeRequired && rc.getTeamOre() > cost) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static int getCurrentJob() throws GameActionException {
		return rc.readBroadcast(getTailPos());
	}

	public static int getTypeRequired(int job) throws GameActionException {
		return job / 1000000;
	}

	public static int getTypeToCreate(int job) throws GameActionException {
		return (job % 1000000) / 10000;
	}

	public static int getCost(int job) throws GameActionException {
		return job % 10000;
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
