package MarkyMark;

import battlecode.common.*;
import MarkyMark.*;

// TODO -- Clean this up.
public class Navigation {
	static RobotController rc;
	static MapLocation currentDestination;

	public static void init(RobotController rcin) throws GameActionException {
		Bug.init(rcin);
		rc = rcin;
	}

	public static void moveTo(MapLocation destination) throws GameActionException {
		if (!destination.equals(currentDestination)) {
			currentDestination = destination;
			Bug.beginBugTowards(currentDestination);
			//rc.setIndicatorString(0, "Changing Destination to " + destination.x + ", " + destination.y);
		}
		moveInDirection(Bug.getDirection());
	}

	public static void moveInDirection(Direction d) throws GameActionException {
		if (rc.isCoreReady() && rc.canMove(d)) {
			rc.move(d);
		}
	}

	public static void moveRandomly() throws GameActionException {
		if (rc.isCoreReady()) {
            moveInDirection(randomDirection());
		}
	}

	public static Direction randomDirection() throws GameActionException {
		return Info.directions[Info.rand.nextInt(8)];
	}

}
