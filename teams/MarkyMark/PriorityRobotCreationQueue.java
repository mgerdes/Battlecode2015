package MarkyMark;

import battlecode.common.*;

// If this has dependency issues then we fucked :\.
public class PriorityRobotCreationQueue {
    static final int LENGTH_CHANNEL = 64000;
    static final int HEAD_POS_CHANNEL = 64001;
    static final int TAIL_POS_CHANNEL = 64002;
    static final int START_POS = 64003;

    static RobotController rc;
    static RobotType[] robotTypes = RobotType.values();

    public static void reset() throws GameActionException {
        rc.broadcast(LENGTH_CHANNEL, 0);
        rc.broadcast(HEAD_POS_CHANNEL, START_POS);
        rc.broadcast(TAIL_POS_CHANNEL, START_POS);
    }

    public static void init(RobotController rcin) throws GameActionException {
        rc = rcin;
        robotTypes = RobotType.values();
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
        if (length() > 0) {
            RobotType typeToCreate = robotTypes[getGetItemInQueue()];
            RobotType typeRequired = getTypeRequiredToCreate(typeToCreate);

            if (rc.getType() == typeRequired && rc.getTeamOre() > typeToCreate.oreCost) {
                return typeToCreate;
            }
        }
        return null;
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
        int tailPos = getTailPos();
        updateTailPos(tailPos + 1);
        decreaseLength();
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

